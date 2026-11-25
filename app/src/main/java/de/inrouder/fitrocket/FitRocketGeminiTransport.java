package de.inrouder.fitrocket;

import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/** Bounded retries for explicit Gemini HTTP failures only. No raw errors, keys or prompts are logged. */
final class FitRocketGeminiTransport {
    static final int MAX_ATTEMPTS=3;
    static final long BUDGET_MS=120000,MAX_WAIT_MS=15000;
    interface Control {
        boolean cancelled();
        void retrying(int attempt,int maximum,long waitMillis);
    }
    interface Environment {
        HttpURLConnection open(URL url)throws IOException;
        long elapsed();
        long wallTime();
        long jitter();
        void sleep(long millis)throws InterruptedException;
    }
    static final class HttpFailure extends IOException {
        final int status,attempts;
        HttpFailure(int status,int attempts){super("Gemini HTTP "+status);this.status=status;this.attempts=attempts;}
    }
    static final class Cancelled extends IOException {Cancelled(){super("Gemini-Anfrage abgebrochen.");}}
    private static final Environment LIVE=new Environment(){
        public HttpURLConnection open(URL url)throws IOException{return (HttpURLConnection)url.openConnection();}
        public long elapsed(){return System.nanoTime()/1000000L;}
        public long wallTime(){return System.currentTimeMillis();}
        public long jitter(){return ThreadLocalRandom.current().nextLong(751);}
        public void sleep(long millis)throws InterruptedException{Thread.sleep(millis);}
    };
    private FitRocketGeminiTransport(){}
    static boolean retryable(int status){return status==500||status==502||status==503||status==504;}
    /** -1 means Google's requested wait exceeds our foreground retry window. */
    static long waitMillis(int failedAttempt,String retryAfter,long now,long jitter){
        long minimum=0;
        if(retryAfter!=null&&!retryAfter.trim().isEmpty()){
            String value=retryAfter.trim();
            try{
                if(value.matches("[0-9]+")){
                    if(value.length()>9)return -1;
                    long seconds=Long.parseLong(value);if(seconds>MAX_WAIT_MS/1000)return -1;
                    minimum=seconds*1000;
                }else minimum=Math.max(0,ZonedDateTime.parse(value,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-now);
            }catch(Exception ignored){minimum=0;}
            if(minimum>MAX_WAIT_MS)return -1;
        }
        long backoff=failedAttempt<=1?2000:4000;
        return Math.max(minimum,backoff+Math.max(0,Math.min(750,jitter)));
    }
    static byte[] post(URL url,String key,byte[] body,Control control)throws IOException,InterruptedException {
        return post(url,key,body,control,LIVE);
    }
    static byte[] post(URL url,String key,byte[] body,Control control,Environment env)throws IOException,InterruptedException {
        if(body==null||body.length>90000)throw new IllegalArgumentException("KI-Anfrage zu umfangreich.");
        long deadline=env.elapsed()+BUDGET_MS;
        for(int attempt=1;attempt<=MAX_ATTEMPTS;attempt++){
            check(control);Response response=send(url,key,body,control,env,deadline);
            check(control);
            if(response.status==200)return response.body;
            long wait=waitMillis(attempt,response.retryAfter,env.wallTime(),env.jitter());
            if(!retryable(response.status)||attempt==MAX_ATTEMPTS||wait<0||deadline-env.elapsed()<=wait+1000)
                throw new HttpFailure(response.status,attempt);
            control.retrying(attempt+1,MAX_ATTEMPTS,wait);
            long until=env.elapsed()+wait;
            while(env.elapsed()<until){check(control);env.sleep(Math.min(100,until-env.elapsed()));}
        }
        throw new IllegalStateException("Gemini-Wiederholungslimit.");
    }
    private static final class Response {
        final int status;final byte[] body;final String retryAfter;
        Response(int status,byte[] body,String retryAfter){this.status=status;this.body=body;this.retryAfter=retryAfter;}
    }
    private static void check(Control control)throws Cancelled {
        if(Thread.currentThread().isInterrupted()||control.cancelled())throw new Cancelled();
    }
    private static int timeout(Environment env,long deadline,int cap)throws SocketTimeoutException {
        long remaining=deadline-env.elapsed();
        if(remaining<=0)throw new SocketTimeoutException("Gemini-Zeitbudget erreicht.");
        return (int)Math.min(cap,remaining);
    }
    private static Response send(URL url,String key,byte[] body,Control control,Environment env,long deadline)throws IOException {
        HttpURLConnection connection=env.open(url);
        try{
            check(control);connection.setInstanceFollowRedirects(false);connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeout(env,deadline,15000));
            connection.setReadTimeout(timeout(env,deadline,90000));connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
            connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("x-goog-api-key",key);
            connection.setRequestProperty("User-Agent","FitRocket-Android");connection.setFixedLengthStreamingMode(body.length);
            try(OutputStream stream=connection.getOutputStream()){check(control);stream.write(body);}
            check(control);connection.setReadTimeout(timeout(env,deadline,90000));
            int status=connection.getResponseCode();
            if(status!=200)return new Response(status,null,connection.getHeaderField("Retry-After"));
            try(InputStream input=connection.getInputStream();ByteArrayOutputStream output=new ByteArrayOutputStream()){
                byte[] buffer=new byte[4096];int n;
                while(true){
                    check(control);connection.setReadTimeout(timeout(env,deadline,90000));
                    n=input.read(buffer);if(n<0)break;output.write(buffer,0,n);
                    if(output.size()>350000)throw new IllegalArgumentException("KI-Antwort zu umfangreich. Bitte die Anfrage vereinfachen.");
                }
                return new Response(status,output.toByteArray(),null);
            }
        }finally{connection.disconnect();}
    }
}
