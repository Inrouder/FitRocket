package de.inrouder.fitrocket;

import org.json.JSONObject;

/** One-way, opt-in failover after an explicit Gemini service failure only. */
final class FitRocketAiFallback implements FitRocketAiService.Callback {
    static final String KEY="geminiGroqFallback";
    private final FitRocketAiService.Callback target;
    private final Runnable startGroq;
    private boolean finished;
    FitRocketAiFallback(FitRocketAiService.Callback target,Runnable startGroq){this.target=target;this.startGroq=startGroq;}
    public boolean isCancelled(){return finished||target.isCancelled();}
    public void onRetry(int attempt,int maximum,long wait){if(!isCancelled())target.onRetry(attempt,maximum,wait);}
    public void onSuccess(JSONObject result){if(!isCancelled()){finished=true;target.onSuccess(result);}}
    public void onFailure(String message){if(!isCancelled()){finished=true;target.onFailure(message);}}
    public void onUnavailable(String message){
        if(isCancelled())return;
        finished=true;
        target.onProviderFallback();
        if(!target.isCancelled())startGroq.run();
    }
}
