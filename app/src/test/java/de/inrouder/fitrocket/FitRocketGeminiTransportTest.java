package de.inrouder.fitrocket;

import org.junit.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

/** No live calls, real keys, timers or sleeps: exercise the HTTP lifecycle and retry policy together. */
public class FitRocketGeminiTransportTest {
    private static final String KEY="dummy-test-key";
    private static final byte[] BODY="{\"private-test-prompt\":\"meal\"}".getBytes(StandardCharsets.UTF_8);
    private static URL url()throws Exception{return new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent");}
    private static final class Connection extends HttpURLConnection {
        final int status;final ByteArrayOutputStream sent=new ByteArrayOutputStream();
        byte[] response="{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
        String retryAfter;boolean disconnected;int errorReads;long duration;IOException failure;Environment env;Runnable onResponse;
        Connection(int status)throws Exception{super(url());this.status=status;}
        public void connect(){}
        public boolean usingProxy(){return false;}
        public void disconnect(){disconnected=true;}
        public OutputStream getOutputStream()throws IOException{if(failure!=null)throw failure;return sent;}
        public int getResponseCode(){env.elapsed+=duration;if(onResponse!=null)onResponse.run();return status;}
        public String getHeaderField(String name){return "Retry-After".equalsIgnoreCase(name)?retryAfter:null;}
        public InputStream getInputStream(){return new ByteArrayInputStream(response);}
        public InputStream getErrorStream(){errorReads++;throw new AssertionError("Never read/echo raw error payloads");}
    }
    private static final class Environment implements FitRocketGeminiTransport.Environment {
        long elapsed,wall=1795608000000L,jitter;int opened;boolean interrupt;
        final List<Connection> connections=new ArrayList<>();final List<URL> urls=new ArrayList<>();
        final List<Long> sleeps=new ArrayList<>();Runnable onSleep;
        Environment(int... statuses)throws Exception{for(int status:statuses)connections.add(new Connection(status));}
        public HttpURLConnection open(URL url){Connection c=connections.get(opened++);c.env=this;urls.add(url);return c;}
        public long elapsed(){return elapsed;}
        public long wallTime(){return wall+elapsed;}
        public long jitter(){return jitter;}
        public void sleep(long millis)throws InterruptedException{if(interrupt)throw new InterruptedException();sleeps.add(millis);elapsed+=millis;if(onSleep!=null)onSleep.run();}
    }
    private static final class Control implements FitRocketGeminiTransport.Control {
        boolean cancelled;final List<Integer> attempts=new ArrayList<>();final List<Long> waits=new ArrayList<>();Runnable onRetry;
        public boolean cancelled(){return cancelled;}
        public void retrying(int attempt,int maximum,long wait){assertEquals(3,maximum);attempts.add(attempt);waits.add(wait);if(onRetry!=null)onRetry.run();}
    }
    private byte[] post(Environment env,Control control)throws Exception{return FitRocketGeminiTransport.post(url(),KEY,BODY,control,env);}
    @Test public void successUsesOneRequestAndClosesConnection()throws Exception {
        Environment env=new Environment(200);Control c=new Control();assertEquals("{\"ok\":true}",new String(post(env,c),StandardCharsets.UTF_8));
        assertEquals(1,env.opened);assertTrue(c.attempts.isEmpty());assertTrue(env.connections.get(0).disconnected);
        assertArrayEquals(BODY,env.connections.get(0).sent.toByteArray());assertEquals(KEY,env.connections.get(0).getRequestProperty("x-goog-api-key"));
        assertFalse(env.connections.get(0).getInstanceFollowRedirects());assertEquals("POST",env.connections.get(0).getRequestMethod());
    }
    @Test public void two503sThenSuccessRetryIdenticalBodyOnlyOnSameModel()throws Exception {
        Environment env=new Environment(503,503,200);env.jitter=500;Control c=new Control();
        c.onRetry=()->assertTrue("Disconnect before waiting",env.connections.get(env.opened-1).disconnected);
        post(env,c);assertEquals(3,env.opened);assertEquals(Arrays.asList(2,3),c.attempts);assertEquals(Arrays.asList(2500L,4500L),c.waits);
        for(Connection connection:env.connections){assertArrayEquals(BODY,connection.sent.toByteArray());assertEquals(0,connection.errorReads);assertTrue(connection.disconnected);}
        for(URL target:env.urls)assertEquals(url().toExternalForm(),target.toExternalForm());
    }
    @Test public void persistent503StopsAfterThreeAttemptsWithSafeMetadata()throws Exception {
        Environment env=new Environment(503,503,503,200);Control c=new Control();
        try{post(env,c);fail();}catch(FitRocketGeminiTransport.HttpFailure failure){
            assertEquals(503,failure.status);assertEquals(3,failure.attempts);assertFalse(failure.getMessage().contains(KEY));assertFalse(failure.getMessage().contains("private-test-prompt"));
        }
        assertEquals(3,env.opened);assertEquals(2,c.attempts.size());assertFalse(env.connections.get(3).disconnected);
    }
    @Test public void onlyTransientServerStatusesAreRetried()throws Exception {
        for(int status:new int[]{500,502,503,504}){Environment env=new Environment(status,200);post(env,new Control());assertEquals(2,env.opened);}
        for(int status:new int[]{400,401,403,404,408,429,501,302}){
            Environment env=new Environment(status,200);Control c=new Control();
            try{post(env,c);fail();}catch(FitRocketGeminiTransport.HttpFailure failure){assertEquals(status,failure.status);}
            assertEquals(1,env.opened);assertTrue(c.attempts.isEmpty());assertTrue(env.sleeps.isEmpty());assertTrue(env.connections.get(0).disconnected);
        }
    }
    @Test public void retryAfterSecondsAndHttpDateAreRespected()throws Exception {
        Environment env=new Environment(503,200);env.connections.get(0).retryAfter="8";Control c=new Control();post(env,c);assertEquals(8000L,(long)c.waits.get(0));
        long now=java.time.Instant.parse("2026-11-25T12:00:00Z").toEpochMilli();
        assertEquals(10000,FitRocketGeminiTransport.waitMillis(1,"Wed, 25 Nov 2026 12:00:10 GMT",now,0));
        assertEquals(2000,FitRocketGeminiTransport.waitMillis(1,"Tue, 24 Nov 2026 12:00:00 GMT",now,0));
    }
    @Test public void longRetryAfterDefersToUserRatherThanRetryingTooEarly()throws Exception {
        Environment env=new Environment(503,200);env.connections.get(0).retryAfter="60";Control c=new Control();
        try{post(env,c);fail();}catch(FitRocketGeminiTransport.HttpFailure failure){assertEquals(1,failure.attempts);}
        assertEquals(1,env.opened);assertTrue(c.waits.isEmpty());
        assertEquals(-1,FitRocketGeminiTransport.waitMillis(1,"9999999999999999999999999",0,0));
    }
    @Test public void malformedRetryAfterAndJitterAreBounded() {
        assertEquals(2000,FitRocketGeminiTransport.waitMillis(1,"invalid",0,-500));
        assertEquals(4750,FitRocketGeminiTransport.waitMillis(2,null,0,Long.MAX_VALUE));
        assertEquals(15000,FitRocketGeminiTransport.waitMillis(1,"15",0,750));
    }
    @Test public void cancellationBeforeRequestDoesNotSendAnything()throws Exception {
        Environment env=new Environment(200);Control c=new Control();c.cancelled=true;
        try{post(env,c);fail();}catch(FitRocketGeminiTransport.Cancelled expected){}assertEquals(0,env.opened);
    }
    @Test public void cancellationDuringBackoffPreventsAnotherRequest()throws Exception {
        Environment env=new Environment(503,200);Control c=new Control();env.onSleep=()->c.cancelled=true;
        try{post(env,c);fail();}catch(FitRocketGeminiTransport.Cancelled expected){}
        assertEquals(1,env.opened);assertTrue(env.connections.get(0).disconnected);assertTrue(env.elapsed<=100);
    }
    @Test public void cancellationAfterResponseDoesNotDeliverSuccess()throws Exception {
        Environment env=new Environment(200);Control c=new Control();
        env.connections.get(0).onResponse=()->c.cancelled=true;
        try{post(env,c);fail();}catch(FitRocketGeminiTransport.Cancelled expected){}
        assertEquals(1,env.opened);assertTrue(env.connections.get(0).disconnected);
    }
    @Test public void interruptedWaitDoesNotSendAnotherRequest()throws Exception {
        Environment env=new Environment(503,200);env.interrupt=true;
        try{post(env,new Control());fail();}catch(InterruptedException expected){}assertEquals(1,env.opened);
    }
    @Test public void networkTimeoutsAreNotAutomaticallyResubmitted()throws Exception {
        for(IOException failure:new IOException[]{new SocketTimeoutException("timeout"),new IOException("disconnected")}){
            Environment env=new Environment(200,200);env.connections.get(0).failure=failure;
            try{post(env,new Control());fail();}catch(IOException expected){assertSame(failure,expected);}
            assertEquals(1,env.opened);assertTrue(env.sleeps.isEmpty());assertTrue(env.connections.get(0).disconnected);
        }
    }
    @Test public void exhaustedTimeBudgetPreventsFurtherRetry()throws Exception {
        Environment env=new Environment(503,503,200);env.connections.get(0).duration=119000;
        try{post(env,new Control());fail();}catch(FitRocketGeminiTransport.HttpFailure failure){assertEquals(1,failure.attempts);}
        assertEquals(1,env.opened);
    }
    @Test public void successfulReadTimeoutUsesRemainingBudget()throws Exception {
        Environment env=new Environment(200);env.connections.get(0).duration=110000;
        post(env,new Control());assertEquals(10000,env.connections.get(0).getReadTimeout());
    }
    @Test public void responseLimitStillAppliesWithoutRetries()throws Exception {
        Environment env=new Environment(200,200);env.connections.get(0).response=new byte[350001];
        try{post(env,new Control());fail();}catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("umfangreich"));}
        assertEquals(1,env.opened);assertTrue(env.connections.get(0).disconnected);
    }
}
