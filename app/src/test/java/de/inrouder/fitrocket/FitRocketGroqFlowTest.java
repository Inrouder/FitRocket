package de.inrouder.fitrocket;

import android.app.*;
import android.content.*;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.*;
import java.io.*;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w360dp-h740dp-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FitRocketGroqFlowTest {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private SharedPreferences prefs,secrets;
    @Before public void setup(){
        Context app=RuntimeEnvironment.getApplication();
        prefs=app.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
        prefs.edit().clear().putBoolean("updateChecks",false).putBoolean("weekly28MigrationDone",true)
            .putBoolean("immediateActivityCreditMigrationDone",true).putString("appearance","light").commit();
        secrets=app.getSharedPreferences("fitrocket_ai_private",Context.MODE_PRIVATE);secrets.edit().clear().commit();
    }
    @After public void cleanup(){if(controller!=null)controller.pause().stop().destroy();}
    private void start(String provider)throws Exception {
        prefs.edit().putString("aiProvider",provider).commit();
        controller=Robolectric.buildActivity(MainActivity.class).setup();activity=controller.get();hub();
    }
    private void hub()throws Exception {Method method=MainActivity.class.getDeclaredMethod("aiHub");method.setAccessible(true);method.invoke(activity);idle();}
    private void idle(){Shadows.shadowOf(Looper.getMainLooper()).idle();}
    private View root(){return activity.getWindow().getDecorView();}
    private View find(View view,String text){
        if(view instanceof TextView&&text.contentEquals(((TextView)view).getText()))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            View found=find(((ViewGroup)view).getChildAt(i),text);if(found!=null)return found;
        }return null;
    }
    private void click(View view,String text){View found=find(view,text);assertNotNull(text,found);assertTrue(found.performClick());idle();}
    private AlertDialog dialog(){idle();return ShadowAlertDialog.getLatestAlertDialog();}
    private EditText input(View view){
        if(view instanceof EditText)return (EditText)view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            EditText found=input(((ViewGroup)view).getChildAt(i));if(found!=null)return found;
        }return null;
    }
    private void capture(View view)throws Exception {
        view.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1350,View.MeasureSpec.EXACTLY));view.layout(0,0,720,1350);
        Bitmap bitmap=Bitmap.createBitmap(720,1350,Bitmap.Config.ARGB_8888);view.draw(new Canvas(bitmap));
        File dir=new File("build/outputs/pet-previews");assertTrue(dir.isDirectory()||dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,"groq-setup.png"))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
    }
    @Test public void providerCanBeSelectedAndModelPersistsWithoutChangingDiary()throws Exception {
        prefs.edit().putString("meals","[]").commit();start(FitRocketAiSecrets.GEMINI);
        assertFalse(prefs.getBoolean(FitRocketAiFallback.KEY,false));
        click(root(),"KI-Anbieter wechseln");Shadows.shadowOf(dialog()).clickOnItem(1);idle();
        assertEquals(FitRocketAiSecrets.GROQ,prefs.getString("aiProvider",""));
        assertNotNull(find(root(),"Aktiver Anbieter: Groq"));
        click(root(),"Groq-Modell auswählen");Shadows.shadowOf(dialog()).clickOnItem(1);idle();
        assertEquals(FitRocketGroqApi.MODEL_LARGE,prefs.getString("groqModel",""));
        assertEquals("[]",prefs.getString("meals",""));assertFalse(FitRocketAiSecrets.configured(activity,FitRocketAiSecrets.GROQ));
    }
    @Test @Config(sdk=35) public void setupExplainsFreePlanMasksInputAndOpensCorrectConsole()throws Exception {
        prefs.edit().putString("appearance","dark").commit();start(FitRocketAiSecrets.GROQ);
        click(root(),"Groq-Schlüssel einrichten / entfernen");AlertDialog setup=dialog();
        View content=setup.getWindow().getDecorView();EditText key=input(content);assertNotNull(key);
        assertTrue(key.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod);
        assertEquals(View.IMPORTANT_FOR_AUTOFILL_NO,key.getImportantForAutofill());
        click(content,"🌐 Groq Console öffnen");
        Intent browser=Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(Intent.ACTION_VIEW,browser.getAction());assertEquals("https://console.groq.com/keys",browser.getDataString());
        key.setText("invalid-key");setup.getButton(-1).performClick();idle();
        assertTrue(setup.isShowing());assertFalse(FitRocketAiSecrets.configured(activity,FitRocketAiSecrets.GROQ));
        key.setText("");capture(content);setup.getButton(-2).performClick();
    }
    @Test public void reserveRequiresKeyAndDeletingItPreservesOtherCredentials()throws Exception {
        start(FitRocketAiSecrets.GEMINI);
        CheckBox toggle=(CheckBox)find(root(),"Bei Gemini-Ausfall automatisch Groq verwenden");
        toggle.setChecked(true);assertFalse(toggle.isChecked());assertFalse(prefs.getBoolean(FitRocketAiFallback.KEY,false));
        // Synthetic encrypted-entry placeholders: test routing/removal, never real credentials.
        secrets.edit().putString("gemini_cipher","gemini-placeholder").putString("gemini_iv","iv")
            .putString("cipher","openai-placeholder").putString("iv","iv")
            .putString("groq_cipher","groq-placeholder").putString("groq_iv","iv").commit();
        toggle.setChecked(true);assertTrue(prefs.getBoolean(FitRocketAiFallback.KEY,false));
        click(root(),"Groq-Reserve einrichten");AlertDialog setup=dialog();
        assertNotNull(find(setup.getWindow().getDecorView(),"Dein Groq-API-Schlüssel"));
        assertEquals(FitRocketAiSecrets.GEMINI,prefs.getString("aiProvider",""));
        setup.getButton(-3).performClick();idle();dialog().getButton(-1).performClick();idle();
        assertFalse(FitRocketAiSecrets.configured(activity,FitRocketAiSecrets.GROQ));
        assertFalse(prefs.getBoolean(FitRocketAiFallback.KEY,true));
        assertEquals("gemini-placeholder",secrets.getString("gemini_cipher",""));
        assertEquals("openai-placeholder",secrets.getString("cipher",""));
    }
}
