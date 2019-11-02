package com.example.iitdutility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ComplexColorCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import android.widget.ImageView;
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


        processImage();







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


        void processImage()
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.unnamed);
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
                    if(r>150)
                        process.setPixel(j+3,i+3,Color.BLACK);
                    else
                        process.setPixel(j+3,i+3,Color.WHITE);
                    }catch(Exception e){
                        Log.e(TAG, "processImage: ");

                    }
                }
            }
            ImageView imageView=findViewById(R.id.imageView);
            imageView.setImageBitmap(process);


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
            imageView.setImageBitmap(process);


            MyTessOCR mTessOCR;
            mTessOCR = new MyTessOCR(MainActivity.this);

            String temp2 = mTessOCR.getOCRResult(process);
            String res;
            temp=temp.replace(" ","");
            temp2=temp2.replace(" ","");
            if((temp2.length()==4)||(temp2.length()==5))
                res=temp2;
            else
                res=temp;
            Toast.makeText(this, "And the captcha is "+res, Toast.LENGTH_SHORT).show();
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
