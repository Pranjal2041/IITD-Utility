package com.example.iitdutility;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String pass;
    WebView webView;
    int counter=0;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button moodle=findViewById(R.id.button);
        final EditText password=findViewById(R.id.editText);
        webView=findViewById(R.id.webView);

        moodle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass=String.valueOf(password.getText());
                counter=0;
                loadMoodle();
            }
        });







    }





    void loadMoodle()
    {

        webView.getSettings().setJavaScriptEnabled(true);

        String url="https://moodle.iitd.ac.in/login/index.php";
        webView.loadUrl(url);
        final String js="javascript:document.getElementById('username').value='cs5190443';" +
                "javascript:document.getElementById('password').value='"+pass+"';"+
                "(function(){return window.document.body.outerHTML})()";

        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(final WebView view, String url)
            {

                final String[] captcha = {""};
                final int[] res = {99};
                view.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Toast.makeText(MainActivity.this, "value is "+value.indexOf("Please"), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onReceiveValue: index is"+value);
                       // if(captcha[0].indexOf("Please")==-1)
                         //   return;
                        try {
                            int ind=value.lastIndexOf("Please");
                            captcha[0] = value.substring(ind,ind+40);
                        }catch (Exception e){return;}
                        Log.d(TAG, "onReceiveValue: value is "+value.substring(4501));


                        // Toast.makeText(MainActivity.this, "captcha is "+captcha, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onPageFinished: captcha is "+captcha[0]);


                        int[] a=new int[2];
                        int count=0;
                        //Scanner sc=new Scanner(captcha[0]);
                        for(int i=0;i<captcha[0].length()&&count<2;i++)
                        {
                            if(Character.isDigit(captcha[0].charAt(i)))
                            {
                                a[count]=Integer.parseInt(captcha[0].substring(i,i+2).trim());
                                i+=1;
                                count++;
                            }
                        }


                        Log.d(TAG, "onPageFinished: nextInt"+a[0]+","+a[1]);
                        if(captcha[0].contains("subtract"))
                        {
                            res[0] =a[0]-a[1];
                        }
                        if(captcha[0].contains("add"))
                        {
                            res[0] =a[0]+a[1];
                        }
                        if(captcha[0].contains("first"))
                        {
                            res[0] =a[0];
                        }
                        if(captcha[0].contains("second"))
                        {
                            res[0] =a[1];
                        }
                        Log.d(TAG, "onReceiveValue: res="+res[0]);

                        view.evaluateJavascript("javascript:document.getElementById('valuepkg3').value='"+ res[0] +"';"
                                +"document.getElementById('loginbtn').click()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                            }

                        });



                    }

                });

            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                Log.e(TAG, "onReceivedSslError: "+error.toString() );
                //if(error.mUrl="https://moodle.iitd.ac.in/login/index.php")
                handler.proceed();
            }


        });







            }
}
