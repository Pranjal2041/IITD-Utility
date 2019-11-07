package com.example.iitdutility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ComplexColorCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String pass;
    WebView webView;
    ImageView imageView;
    int counter=0;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button moodle=findViewById(R.id.button);
        Button ngu=findViewById(R.id.button2);
        final EditText password=findViewById(R.id.editText);
        webView=findViewById(R.id.webView);
        imageView=findViewById(R.id.imageView);
        Button freespace=findViewById(R.id.freespace);
        freespace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this
                ,FreeSpace.class);
                startActivity(intent);
            }
        });



        moodle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass=String.valueOf(password.getText());
                counter=0;
                loadMoodle();
            }
        });

        ngu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass=String.valueOf(password.getText());
                loadNgu();
            }
        });










    }

    void addImagePadding(Bitmap process)
    {
        for (int i = 0; i < 3; i++)
                for (int j = 0; j < process.getWidth(); j++)
                    process.setPixel(j, i, Color.BLACK);

            for (int i = process.getHeight() - 3; i < process.getHeight(); i++)
                for (int j = 0; j < process.getWidth(); j++)
                    process.setPixel(j, i, Color.BLACK);

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < process.getHeight(); j++)
                process.setPixel(i, j, Color.BLACK);

        for (int i = process.getWidth() - 3; i < process.getWidth(); i++)
            for (int j = 0; j < process.getHeight(); j++)
                process.setPixel(i, j, Color.BLACK);

    }



        int[] trimBorders(Bitmap process)
        {
            int startHeight = 0,startWidth=0,endHeight=0,endWidth=0;
            for(int i=0;i<process.getHeight()-5;i++)
            {
                int j=0;
                for(j=0;j<process.getWidth();j++)
                {
                        if(Color.red(process.getPixel(j,i+5))==255)
                            break;
                }
                if(j!=process.getWidth())
                    startHeight=i;
            }
            for(int i=process.getHeight()-1;i>5;i--)
            {
                int j=0;
                for(j=0;j<process.getWidth();j++)
                {
                    if(Color.red(process.getPixel(j,i-5))==255)
                        break;
                }
                if(j!=process.getWidth())
                    endHeight=i;
            }

            for(int i=0;i<process.getWidth()-5;i++)
            {
                int j=0;
                for(j=0;j<process.getHeight();j++)
                {
                    if(Color.red(process.getPixel(i+5,j))==255)
                        break;
                }
                if(j!=process.getHeight())
                    startWidth=i;
            }
            for(int i=process.getWidth()-1;i>5;i--)
            {
                int j=0;
                for(j=0;j<process.getHeight();j++)
                {
                    if(Color.red(process.getPixel(i-5,j))==255)
                        break;
                }
                if(j!=process.getWidth())
                    endWidth=i;
            }

            return new int[]{startHeight,endHeight,startWidth,endWidth};


        }

        //returns angle in degrees
        double findAngle(Bitmap process,int startWidth,int endWidth,int startHeight,int endHeight)
        {
            double min1=9999999;
            double min2=9999999;
            int[] index1=new int[2];
            int[] index2=new int[2];

            for(int i=startHeight;i<endHeight;i++)
            {
                for(int j=startWidth;j<endWidth;j++)
                {
                    if(Color.red(process.getPixel(j,i))==0)
                        continue;
                    double temp=Math.sqrt(Math.pow(endHeight-i,2)+Math.pow(j-startWidth,2));
                    double temp2=Math.sqrt(Math.pow(endHeight-i,2)+Math.pow(j-endWidth,2));
                    if(temp<min1) {
                        min1 = temp;
                        index1[0]=i;
                        index1[1]=j;
                    }if(temp2<min2)
                {   min2=temp2;
                    index2[0]=i;
                    index2[1]=j;
                }
                }
            }
            return Math.toDegrees(Math.atan(1.0*(index1[0]-index2[0])/(index1[1]-index2[1])));
        }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    void switchColor(Bitmap process)
    {
        for(int i=0;i<process.getHeight();i++)
            for(int j=0;j<process.getWidth();j++)
                if(Color.red(process.getPixel(j,i))==0)
                    process.setPixel(j,i,Color.WHITE);
                else
                    process.setPixel(j,i,Color.BLACK);
    }


        String processImage(Bitmap bitmap)
        {
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.unnamed);
            int width=bitmap.getWidth();
            int height=bitmap.getHeight();
            Bitmap process=Bitmap.createBitmap(width+6,height+6,Bitmap.Config.ARGB_8888);
            addImagePadding(process);

           /* for(int i=0;i<process.getHeight();i++)
                for(int j=0;j<process.getWidth();j++)
                    process.setPixel(j,i,Color.RED);
*/


            for(int i=0; i<bitmap.getHeight(); i++){
                for(int j=0; j<bitmap.getWidth(); j++){
                    try {
                        int p = bitmap.getPixel(j, i);

                    int r=Color.red(p);
                    if(r>120)
                        process.setPixel(j+3,i+3,Color.BLACK);
                    else
                        process.setPixel(j+3,i+3,Color.WHITE);
                    }catch(Exception e){
                        Log.e(TAG, "processImage: ");

                    }
                }
            }
            //imageView.setImageBitmap(process);


            MyTessOCR mTessOCR2;
            mTessOCR2 = new MyTessOCR(MainActivity.this);

            String temp = mTessOCR2.getOCRResult(process);


            int startWidth;
            int startHeight;
            int endWidth;
            int endHeight;
            int[] arr=trimBorders(process);
            startWidth=arr[3];
            startHeight=arr[1];
            endWidth=arr[2];
            endHeight=arr[0];
     

            double angle=-findAngle(process,startWidth,endWidth,startHeight,endHeight);
            process=RotateBitmap(process,(float)angle);
            Log.d(TAG, "processImage: ");


            switchColor(process);
           // imageView.setImageBitmap(process);
           // smoothenImage(process);
           // smoothenImage(process);


            MyTessOCR mTessOCR;
            mTessOCR = new MyTessOCR(MainActivity.this);

            String temp2 = mTessOCR.getOCRResult(process);
            String res;
            temp=temp.replace(" ","");
            temp2=temp2.replace(" ","");
            temp=mapCaptchaIssues(temp);
            temp2=mapCaptchaIssues(temp2);

            if((temp.length()==4)||(temp.length()==5))
                res=temp;
            else
                res=temp2;
            Toast.makeText(this, "And the captcha is "+res, Toast.LENGTH_SHORT).show();
            return res;
        }

        void smoothenImage(Bitmap bmp)
        {
            Bitmap original=Bitmap.createBitmap(bmp);
            for(int i=1;i<original.getWidth()-1;i++)
            {
                for(int j=1;j<original.getHeight()-1;j++)
                {
                    int count=0;
                    for(int p=-1;p<=1;p++)
                    {
                        for(int q=-1;q<=1;q++)
                        {
                            if(Color.red(original.getPixel(i+p,j+q))==0)
                                count++;
                        }
                    }
                    if(Color.red(original.getPixel(i,j))==0)
                    {
                        if(count<3)
                            bmp.setPixel(i,j,Color.WHITE);
                        else
                            bmp.setPixel(i,j,Color.BLACK);
                    }
                    else
                    {
                        if(count>3)
                            bmp.setPixel(i,j,Color.BLACK);
                        else
                            bmp.setPixel(i,j,Color.WHITE);
                    }
                }
            }

        }

        String mapCaptchaIssues(String a)
        {
            a=a.replace("cl","d");
            return a;


        }


        void loadNgu() {
            webView.getSettings().setJavaScriptEnabled(true);

            String url = "https://ngu.iitd.ac.in/index";
            webView.loadUrl(url);
            // new Document().getDocumentElement()

           // final String js = "javascript:document.getElementById('username').value='cs5190443';" +
             //       "javascript:document.getElementById('password').value='" + pass + "';" +
               //     "(function(){return window.document.body.outerHTML})()";

            final String js="javascript:document.getElementsByClassName('btn btn-success btn-block').item(0).click();";
    
   /*         webView.setPictureListener(new WebView.PictureListener() {
                @Override
                public void onNewPicture(WebView webView, Picture picture) {
                    Log.d(TAG, "onNewPicture: picture loaded");
                }
            });*/
            webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(final WebView view, String url) {

                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                            view.setWebViewClient(new WebViewClient(){





                                /*public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest wrr)
                                {
                                    *//*Log.d(TAG, "shouldInterceptRequest: url is "+wrr);
                                    try {
                                        InputStream is=(InputStream) new URL(String.valueOf(wrr.getUrl())).getContent();
                                        Drawable d = Drawable.createFromStream(is,"srcName");
                                        if(d!=null)
                                            imageView.setImageDrawable(d);

                                        Log.d(TAG, "shouldInterceptRequest: drawable is "+d);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    WebResourceResponse xyz=super.shouldInterceptRequest(view,wrr);
                                    Log.d(TAG, "shouldInterceptRequest: super gives"+xyz);
                                    return xyz;*//*
                                }*/

                                public void onPageCommitVisible(WebView view,String url)
                                {

                                    if(!url.startsWith("https://oath"))
                                        return;

                                }
                                
                                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail rpgd)
                                {
                                    Log.d(TAG, "onRenderProcessGone: inside rpgd");
                                    return true;
                                }
                                
                                public void onPageFinished(final WebView view,String url){

                                    view.evaluateJavascript("javascript:document.getElementsByName('username').item(0).value='cs5190443';"
                                            + "javascript:document.getElementsByName('password').item(0).value='"+pass+"';"
                                            , new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String value) {
                                            Log.d(TAG, "onReceiveValue: "+value);
                                            SystemClock.sleep(1000);
                                            Log.d(TAG, "onReceiveValue: after sleep"+value);

                                            Picture snapshot=view.capturePicture();
                                            Bitmap bmp=Bitmap.createBitmap(snapshot.getWidth(),snapshot.getHeight(),Bitmap.Config.ARGB_8888);
                                            Canvas canvas=new Canvas(bmp);
                                            snapshot.draw(canvas);
                                            Log.d(TAG, "onReceiveValue: drawn to canvas"+value);

                                            Bitmap cropped=Bitmap.createBitmap(bmp,172,630,360,155);
                                            Log.d(TAG, "onReceiveValue: created new bitmap "+value);

                                            String res=processImage(cropped);
                                            Log.d(TAG, "onReceiveValue: result="+res);
                                            view.evaluateJavascript("javascript:document.getElementsByName('captcha').item(0).value='"+ res + "';"
                                                    + "javascript:document.getElementsByName('submit').item(0).click();"
                                                    , new ValueCallback<String>() {
                                                @Override
                                                public void onReceiveValue(String value) {
                                                    Log.d(TAG, "onReceiveValue: not printing message"+value);
                                                }
                                            });


                                        }


                            });
                                }

                            });


                        }

                    });

                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Log.e(TAG, "onReceivedSslError: " + error.toString());
                    //if(error.mUrl="https://moodle.iitd.ac.in/login/index.php")
                    handler.proceed();
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
