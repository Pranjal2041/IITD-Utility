package com.example.iitdutility;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static java.util.Arrays.parallelSort;
import static java.util.Arrays.sort;

public class FreeSpace extends AppCompatActivity {

    Button date_picker,time_picker;
    int date,month,year;
    int hour,minute;
    String[][] data;
    TextView resDisplay;
    String LHNames[];
    private static final String TAG = "FreeSpace";

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_space);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Calendar c= Calendar.getInstance();
        year=c.get(Calendar.YEAR);
        month=c.get(Calendar.MONTH);
        date=c.get(Calendar.DAY_OF_MONTH);
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        readData();


        date_picker=findViewById(R.id.datePicker);
        time_picker=findViewById(R.id.timePicker);
        resDisplay=findViewById(R.id.freeHallsDisplay);
        changeTimePicker(hour,minute);
        changeDatePicker(date,year,month);

        time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimePickerDialog();
            }
        });

        date_picker.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                        setDatePickListener();
                }
        });

        showLectureHalls();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Started downloading schedule", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                downloadAllData();
            }
        });
    }

    void changeDatePicker(int dayOfMonth,int year,int month)
    {
        date_picker.setText(dayOfMonth+"/"+(month+1)+"/"+year);
    }

    void setDatePickListener()
    {


        DatePickerDialog datePickerDialog=new DatePickerDialog(FreeSpace.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth) {
                changeDatePicker(dayOfMonth,year1,month1);
                date=dayOfMonth;
                year=year1;
                month=month1;
                showLectureHalls();
            }
        },year,month,date);
        datePickerDialog.show();



    }

    void setTimePickerDialog()
    {
        TimePickerDialog timePickerDialog=new TimePickerDialog(FreeSpace.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute1) {
                changeTimePicker(hourOfDay,minute1);
                hour=hourOfDay;
                minute=minute1;
                showLectureHalls();
            }
        },hour,minute,true);
        timePickerDialog.show();

    }

    void changeTimePicker(int hour,int minute)
    {
        time_picker.setText(hour+":"+minute);

    }


    void showLectureHalls()
    {
        readData();
        StringBuilder res = new StringBuilder();
        if((hour<8)||(hour>20)) {
            resDisplay.setText("Everything is free");
            return;
        }

        if(data==null) {
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            return;
        }
            int[] timeavail=new int[data.length];
    //    int[] freeLH=new int[data.length];
    //    int tot_free=0;

        for(int i=0;i<data.length;i++)
        {
                int j=(hour-8)*2+minute/30;


                    for(int k=j;k<24;k++)
                    {
                        if(data[i][k].equals("Free"))
                            timeavail[i]++;
                        else break;
                    }

            //       res.append(LHNames[i]).append("\n");

        }

        int[] lhindexes=new int[data.length];
        for(int i=0;i<data.length;i++)
            lhindexes[i]=i;
        sort(timeavail,lhindexes);

        res.append("Best Options:-\n");
        int i;
        for(i=data.length-1;i>=data.length-3;i--)
        {

            if(timeavail[i]==0)
                break;
            res.append(LHNames[lhindexes[i]]).append(" (available for next ").append((timeavail[i] - 1) / 2).append(" hrs and ").append(((timeavail[i] + 1) % 2) * 30 + 30 - (minute % 30)).append("minutes)").append("\n");

        }

        res.append("\nOther options:-\n");
        for(;i>=0;i--)
        {
            if(timeavail[i]==0)
                break;
            res.append(LHNames[lhindexes[i]]).append("\n");
        }



       // Toast.makeText(this, "And the free LHs are:-"+res, Toast.LENGTH_SHORT).show();
        resDisplay.setText(res.toString());


    }

    void sort(int[] arr,int[] arr2)
    {//sorts both array based on values of arr
        int n=arr.length;
        for(int i=1;i<n;++i)
        {
            int key=arr[i];
            int key2=arr2[i];
            int j=i-1;

            while(j>=0&&arr[j]>key)
            {
                arr[j+1]=arr[j];
                arr2[j+1]=arr2[j];
                j=j-1;
            }
            arr[j+1]=key;
            arr2[j+1]=key2;
        }



    }

    void downloadAllData()
    {
        for(int i=1;i<29;i++) {
            Log.d(TAG, "downloadAllData: "+i);

            createFile(generateFileName(i, month, year));
        }
    }

    String generateFileName(int date,int month,int year)
    {
        return date+"_"+(month+1)+"_"+year;
    }

    void readData()
    {

        String filename = generateFileName(date,month,year);


        FileInputStream fileInputStream;
        try {
            fileInputStream=openFileInput(filename);
            InputStream inputStream=fileInputStream;

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            final StringBuilder stringBuilder = new StringBuilder();

            boolean done = false;

            while (!done) {
                final String line = reader.readLine();
                done = (line == null);

                if (line != null) {
                    stringBuilder.append(line);
                }
            }

            reader.close();
            inputStream.close();
            fileInputStream.close();

            readTable(stringBuilder.toString());

        }catch (FileNotFoundException fnfe)
        {
            createFile(filename);

            try {
                fileInputStream = openFileInput(filename);
                InputStream inputStream = fileInputStream;

                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                final StringBuilder stringBuilder = new StringBuilder();

                boolean done = false;

                while (!done) {
                    final String line = reader.readLine();
                    done = (line == null);

                    if (line != null) {
                        stringBuilder.append(line);
                    }
                }

                reader.close();
                inputStream.close();
                fileInputStream.close();

                readTable(stringBuilder.toString());
            }catch (Exception e){}


        }
        catch(Exception e)
        {
                e.printStackTrace();
        }

    }


    void readTable(String sHtml)
    {
        Document doc=Jsoup.parse(sHtml);

        Elements innerTable=doc.getElementsByTag("table");
        Elements rows=innerTable.select("tr");
        int row_size=rows.size();
        data=new String[row_size-1][rows.get(1).select("td").size()];
        LHNames=new String[row_size-1];
        for(int i=0;i<row_size-1;i++)
        {
            Elements cells= rows.get(i+1).select("td");
            for(int j=0;j<cells.size();j++)
            {
                data[i][j]= String.valueOf(cells.get(j)).replace("</td>","").replace("<td>","");
            }
            LHNames[i]=String.valueOf(rows.get(i+1).select("th")).replace("</th>","").replace("<th>","");


        }

        Log.d(TAG, "readTable: table has been read");



    }

    void createFile(final String filename)
    {
        /*try {
            StorageReference riversRef = mStorageRef.child(filename + ".html");

            File localFile = File.createTempFile("images", "jpg");
            riversRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            // ...
                            Log.d(TAG, "onSuccess: ");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                    Log.d(TAG, "onFailure: ");
                }
            });
        }catch (Exception e)
        {
            Log.d(TAG, "createFile: Exception "+e);
        }
        */
        try {
            //       Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
            StorageReference riversRef = mStorageRef.child(filename + ".html");
            riversRef.getBytes(1048576)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.d(TAG, "onSuccess: got bytes");

                            FileOutputStream outputStream;

                            try {
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                outputStream.write(bytes);
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            showLectureHalls();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FreeSpace.this, "Unable to load data", Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch (Exception e)
        {
            Log.d(TAG, "createFile: exception "+e);
        }




        //In actual we will be importing from some online server
       /* File file = new File(this.getFilesDir(), filename);

        String fileContents = "<table border=\"1\" class=\"dataframe\">\n" +
                "  <thead>\n" +
                "    <tr style=\"text-align: right;\">\n" +
                "      <th></th>\n" +
                "      <th>08:00</th>\n" +
                "      <th>08:30</th>\n" +
                "      <th>09:00</th>\n" +
                "      <th>09:30</th>\n" +
                "      <th>10:00</th>\n" +
                "      <th>10:30</th>\n" +
                "      <th>11:00</th>\n" +
                "      <th>11:30</th>\n" +
                "      <th>12:00</th>\n" +
                "      <th>12:30</th>\n" +
                "      <th>13:00</th>\n" +
                "      <th>13:30</th>\n" +
                "      <th>14:00</th>\n" +
                "      <th>14:30</th>\n" +
                "      <th>15:00</th>\n" +
                "      <th>15:30</th>\n" +
                "      <th>16:00</th>\n" +
                "      <th>16:30</th>\n" +
                "      <th>17:00</th>\n" +
                "      <th>17:30</th>\n" +
                "      <th>18:00</th>\n" +
                "      <th>18:30</th>\n" +
                "      <th>19:00</th>\n" +
                "      <th>19:30</th>\n" +
                "    </tr>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "    <tr>\n" +
                "      <th>LH 108</th>\n" +
                "      <td>CML100</td>\n" +
                "      <td>CML100</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>CLL252</td>\n" +
                "      <td>CLL252</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 111</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>CLL261</td>\n" +
                "      <td>CLL261</td>\n" +
                "      <td>COL333</td>\n" +
                "      <td>COL333</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 114</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL305</td>\n" +
                "      <td>ELL305</td>\n" +
                "      <td>MTL739</td>\n" +
                "      <td>MTL739</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CML100</td>\n" +
                "      <td>CML100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 121</th>\n" +
                "      <td>CVL100</td>\n" +
                "      <td>CVL100</td>\n" +
                "      <td>TXL111</td>\n" +
                "      <td>TXL111</td>\n" +
                "      <td>APL102</td>\n" +
                "      <td>APL102</td>\n" +
                "      <td>COL106</td>\n" +
                "      <td>COL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 308</th>\n" +
                "      <td>CVL111</td>\n" +
                "      <td>CVL111</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>APL103</td>\n" +
                "      <td>APL103</td>\n" +
                "      <td>COL341</td>\n" +
                "      <td>COL341</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>COL870</td>\n" +
                "      <td>COL870</td>\n" +
                "      <td>COL870</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 310</th>\n" +
                "      <td>MCL140</td>\n" +
                "      <td>MCL140</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>ELL409</td>\n" +
                "      <td>ELL409</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 316</th>\n" +
                "      <td>MCL211</td>\n" +
                "      <td>MCL211</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>ELL100</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>HUL275</td>\n" +
                "      <td>HUL275</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 318</th>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>CLL113</td>\n" +
                "      <td>CLL113</td>\n" +
                "      <td>BBL132</td>\n" +
                "      <td>BBL132</td>\n" +
                "      <td>ELL712</td>\n" +
                "      <td>ELL712</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 325</th>\n" +
                "      <td>SBL100</td>\n" +
                "      <td>SBL100</td>\n" +
                "      <td>MTL104</td>\n" +
                "      <td>MTL104</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 408</th>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>COL215</td>\n" +
                "      <td>COL215</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>COL780</td>\n" +
                "      <td>COL780</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 410</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 416</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CLL331</td>\n" +
                "      <td>CLL331</td>\n" +
                "      <td>CVL282</td>\n" +
                "      <td>CVL282</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>CVP772</td>\n" +
                "      <td>CVP772</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 418</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>COL334</td>\n" +
                "      <td>COL334</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 510</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 512</th>\n" +
                "      <td>APL701</td>\n" +
                "      <td>APL701</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MCL261</td>\n" +
                "      <td>MCL261</td>\n" +
                "      <td>MCL231</td>\n" +
                "      <td>MCL231</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL724</td>\n" +
                "      <td>PYL724</td>\n" +
                "      <td>PYL724</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 517</th>\n" +
                "      <td>MCL838</td>\n" +
                "      <td>MCL838</td>\n" +
                "      <td>BBL131</td>\n" +
                "      <td>BBL131</td>\n" +
                "      <td>CML674</td>\n" +
                "      <td>CML674</td>\n" +
                "      <td>MCL135</td>\n" +
                "      <td>MCL135</td>\n" +
                "      <td>HUL315</td>\n" +
                "      <td>HUL315</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL458</td>\n" +
                "      <td>MTL458</td>\n" +
                "      <td>MTL458</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 518</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CVL284</td>\n" +
                "      <td>CVL284</td>\n" +
                "      <td>CVL421</td>\n" +
                "      <td>CVL421</td>\n" +
                "      <td>PYL701</td>\n" +
                "      <td>PYL701</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PTL726</td>\n" +
                "      <td>PTL726</td>\n" +
                "      <td>PTL726</td>\n" +
                "      <td>PYL411</td>\n" +
                "      <td>PYL411</td>\n" +
                "      <td>PYL724</td>\n" +
                "      <td>PYL724</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 519</th>\n" +
                "      <td>BBL731</td>\n" +
                "      <td>BBL731</td>\n" +
                "      <td>BBL733</td>\n" +
                "      <td>BBL733</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>RDL700</td>\n" +
                "      <td>RDL700</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 520</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MCL316</td>\n" +
                "      <td>MCL316</td>\n" +
                "      <td>SBL201</td>\n" +
                "      <td>SBL201</td>\n" +
                "      <td>HUL331</td>\n" +
                "      <td>HUL331</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>CVL725</td>\n" +
                "      <td>CVL725</td>\n" +
                "      <td>CVL725</td>\n" +
                "      <td>BML720</td>\n" +
                "      <td>BML720</td>\n" +
                "      <td>BML720</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 521</th>\n" +
                "      <td>MCL341</td>\n" +
                "      <td>MCL341</td>\n" +
                "      <td>BBL231</td>\n" +
                "      <td>BBL231</td>\n" +
                "      <td>TXL721</td>\n" +
                "      <td>TXL721</td>\n" +
                "      <td>TXL749</td>\n" +
                "      <td>TXL749</td>\n" +
                "      <td>CVL423</td>\n" +
                "      <td>CVL423</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 526</th>\n" +
                "      <td>CLL701</td>\n" +
                "      <td>CLL701</td>\n" +
                "      <td>CVL243</td>\n" +
                "      <td>CVL243</td>\n" +
                "      <td>PYL203</td>\n" +
                "      <td>PYL203</td>\n" +
                "      <td>TXL242</td>\n" +
                "      <td>TXL242</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 527</th>\n" +
                "      <td>PYL707</td>\n" +
                "      <td>PYL707</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>TXL212</td>\n" +
                "      <td>TXL212</td>\n" +
                "      <td>CLL777</td>\n" +
                "      <td>CLL777</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL555</td>\n" +
                "      <td>PYL555</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 602</th>\n" +
                "      <td>TXL719</td>\n" +
                "      <td>TXL719</td>\n" +
                "      <td>ELL363</td>\n" +
                "      <td>ELL363</td>\n" +
                "      <td>CVL704</td>\n" +
                "      <td>CVL704</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL603</td>\n" +
                "      <td>MTL603</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 603</th>\n" +
                "      <td>CVL344</td>\n" +
                "      <td>CVL344</td>\n" +
                "      <td>BBL735</td>\n" +
                "      <td>BBL735</td>\n" +
                "      <td>PYL313</td>\n" +
                "      <td>PYL313</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL433</td>\n" +
                "      <td>PYL433</td>\n" +
                "      <td>PYL203</td>\n" +
                "      <td>PYL203</td>\n" +
                "      <td>PYL113</td>\n" +
                "      <td>PYL113</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 604</th>\n" +
                "      <td>CVL779</td>\n" +
                "      <td>CVL779</td>\n" +
                "      <td>MCL735</td>\n" +
                "      <td>MCL735</td>\n" +
                "      <td>CLL296</td>\n" +
                "      <td>CLL296</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ESL300</td>\n" +
                "      <td>ESL300</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 606</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CVL772</td>\n" +
                "      <td>CVL772</td>\n" +
                "      <td>CLL783</td>\n" +
                "      <td>CLL783</td>\n" +
                "      <td>CLL794</td>\n" +
                "      <td>CLL794</td>\n" +
                "      <td>BML830</td>\n" +
                "      <td>BML830</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>BML810</td>\n" +
                "      <td>BML810</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 611</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CLL725</td>\n" +
                "      <td>CLL725</td>\n" +
                "      <td>MTL505</td>\n" +
                "      <td>MTL505</td>\n" +
                "      <td>CML631</td>\n" +
                "      <td>CML631</td>\n" +
                "      <td>MTL712</td>\n" +
                "      <td>MTL712</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 612</th>\n" +
                "      <td>MCL787</td>\n" +
                "      <td>MCL787</td>\n" +
                "      <td>MCL840</td>\n" +
                "      <td>MCL840</td>\n" +
                "      <td>PYL650</td>\n" +
                "      <td>PYL650</td>\n" +
                "      <td>PYL655</td>\n" +
                "      <td>PYL655</td>\n" +
                "      <td>PTL724</td>\n" +
                "      <td>PTL724</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL740</td>\n" +
                "      <td>ELL740</td>\n" +
                "      <td>ELL740</td>\n" +
                "      <td>CVL747</td>\n" +
                "      <td>CVL747</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 613</th>\n" +
                "      <td>MAL860</td>\n" +
                "      <td>MAL860</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MCL742</td>\n" +
                "      <td>MCL742</td>\n" +
                "      <td>PYL755</td>\n" +
                "      <td>PYL755</td>\n" +
                "      <td>RDL760</td>\n" +
                "      <td>RDL760</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>CLN101</td>\n" +
                "      <td>CLN101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>BML770</td>\n" +
                "      <td>BML770</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 614</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PTL714</td>\n" +
                "      <td>PTL714</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CML726</td>\n" +
                "      <td>CML726</td>\n" +
                "      <td>MCL765</td>\n" +
                "      <td>MCL765</td>\n" +
                "      <td>TXL381</td>\n" +
                "      <td>TXL381</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL858</td>\n" +
                "      <td>PYL858</td>\n" +
                "      <td>PYL858</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 615</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MCL769</td>\n" +
                "      <td>MCL769</td>\n" +
                "      <td>PYL703</td>\n" +
                "      <td>PYL703</td>\n" +
                "      <td>ELL333</td>\n" +
                "      <td>ELL333</td>\n" +
                "      <td>ASL320</td>\n" +
                "      <td>ASL320</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 619</th>\n" +
                "      <td>MCL701</td>\n" +
                "      <td>MCL701</td>\n" +
                "      <td>CVL756</td>\n" +
                "      <td>CVL756</td>\n" +
                "      <td>PTL705</td>\n" +
                "      <td>PTL705</td>\n" +
                "      <td>MCL703</td>\n" +
                "      <td>MCL703</td>\n" +
                "      <td>PYL795</td>\n" +
                "      <td>PYL795</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 620</th>\n" +
                "      <td>CML662</td>\n" +
                "      <td>CML662</td>\n" +
                "      <td>MLL703</td>\n" +
                "      <td>MLL703</td>\n" +
                "      <td>CVL823</td>\n" +
                "      <td>CVL823</td>\n" +
                "      <td>CVL748</td>\n" +
                "      <td>CVL748</td>\n" +
                "      <td>MCL811</td>\n" +
                "      <td>MCL811</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL804</td>\n" +
                "      <td>ELL804</td>\n" +
                "      <td>ELL804</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 621</th>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>TXL731</td>\n" +
                "      <td>TXL731</td>\n" +
                "      <td>CVL442</td>\n" +
                "      <td>CVL442</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL751</td>\n" +
                "      <td>PYL751</td>\n" +
                "      <td>ESN712</td>\n" +
                "      <td>ESN712</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 622</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CML731</td>\n" +
                "      <td>CML731</td>\n" +
                "      <td>MCL781</td>\n" +
                "      <td>MCL781</td>\n" +
                "      <td>BML710</td>\n" +
                "      <td>BML710</td>\n" +
                "      <td>CVL765</td>\n" +
                "      <td>CVL765</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 623</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL113</td>\n" +
                "      <td>PYL113</td>\n" +
                "      <td>CML721</td>\n" +
                "      <td>CML721</td>\n" +
                "      <td>AML805</td>\n" +
                "      <td>AML805</td>\n" +
                "      <td>BML850</td>\n" +
                "      <td>BML850</td>\n" +
                "      <td>MTL601</td>\n" +
                "      <td>MTL601</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL717</td>\n" +
                "      <td>MTL717</td>\n" +
                "      <td>MTL717</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "  </tbody>\n" +
                "</table>\n" +
                "      <th>15:00</th>\n" +
                "      <th>15:30</th>\n" +
                "      <th>16:00</th>\n" +
                "      <th>16:30</th>\n" +
                "      <th>17:00</th>\n" +
                "      <th>17:30</th>\n" +
                "      <th>18:00</th>\n" +
                "      <th>18:30</th>\n" +
                "      <th>19:00</th>\n" +
                "      <th>19:30</th>\n" +
                "    </tr>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "    <tr>\n" +
                "      <th>LH 108</th>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>TXL222</td>\n" +
                "      <td>TXL222</td>\n" +
                "      <td>TXL222</td>\n" +
                "      <td>CLL252</td>\n" +
                "      <td>CLL252</td>\n" +
                "      <td>MTL107</td>\n" +
                "      <td>MTL107</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL100</td>\n" +
                "      <td>PYL100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 111</th>\n" +
                "      <td>MSL302</td>\n" +
                "      <td>MSL302</td>\n" +
                "      <td>MSL302</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>COL333</td>\n" +
                "      <td>COL333</td>\n" +
                "      <td>PYL102</td>\n" +
                "      <td>PYL102</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 114</th>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MTL739</td>\n" +
                "      <td>MTL739</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 121</th>\n" +
                "      <td>PYL100</td>\n" +
                "      <td>PYL100</td>\n" +
                "      <td>PYL100</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>COL106</td>\n" +
                "      <td>COL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 308</th>\n" +
                "      <td>CLL141</td>\n" +
                "      <td>CLL141</td>\n" +
                "      <td>CLL141</td>\n" +
                "      <td>CLL222</td>\n" +
                "      <td>CLL222</td>\n" +
                "      <td>CLL222</td>\n" +
                "      <td>APL103</td>\n" +
                "      <td>APL103</td>\n" +
                "      <td>BBL133</td>\n" +
                "      <td>BBL133</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>HUL242</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>COL761</td>\n" +
                "      <td>COL761</td>\n" +
                "      <td>COL761</td>\n" +
                "      <td>ASL360</td>\n" +
                "      <td>ASL360</td>\n" +
                "      <td>ASL360</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 310</th>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>CML102</td>\n" +
                "      <td>CML102</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL212</td>\n" +
                "      <td>HUL212</td>\n" +
                "      <td>HUL212</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 316</th>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>ELL201</td>\n" +
                "      <td>CVL121</td>\n" +
                "      <td>CVL121</td>\n" +
                "      <td>CVL121</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>CML103</td>\n" +
                "      <td>CML103</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL501</td>\n" +
                "      <td>MTL501</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL213</td>\n" +
                "      <td>HUL213</td>\n" +
                "      <td>HUL213</td>\n" +
                "      <td>HUL213</td>\n" +
                "      <td>HUL213</td>\n" +
                "      <td>HUL232</td>\n" +
                "      <td>HUL232</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 318</th>\n" +
                "      <td>HUL256</td>\n" +
                "      <td>HUL256</td>\n" +
                "      <td>HUL256</td>\n" +
                "      <td>CVL341</td>\n" +
                "      <td>CVL341</td>\n" +
                "      <td>CVL341</td>\n" +
                "      <td>BBL132</td>\n" +
                "      <td>BBL132</td>\n" +
                "      <td>CVL141</td>\n" +
                "      <td>CVL141</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL110</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL232</td>\n" +
                "      <td>HUL232</td>\n" +
                "      <td>HUL232</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 325</th>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>HSL800</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>COL100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 408</th>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CLL111</td>\n" +
                "      <td>CVL245</td>\n" +
                "      <td>CVL245</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL503</td>\n" +
                "      <td>MTL503</td>\n" +
                "      <td>MTL503</td>\n" +
                "      <td>HUL275</td>\n" +
                "      <td>HUL275</td>\n" +
                "      <td>HUL275</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 410</th>\n" +
                "      <td>MCL262</td>\n" +
                "      <td>MCL262</td>\n" +
                "      <td>MCL262</td>\n" +
                "      <td>ELL302</td>\n" +
                "      <td>ELL302</td>\n" +
                "      <td>ELL302</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL290</td>\n" +
                "      <td>HUL290</td>\n" +
                "      <td>HUL290</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 416</th>\n" +
                "      <td>MTL783</td>\n" +
                "      <td>MTL783</td>\n" +
                "      <td>MTL783</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>CLL703</td>\n" +
                "      <td>CLL703</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>MCL111</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>MSL306</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 418</th>\n" +
                "      <td>ELL211</td>\n" +
                "      <td>ELL211</td>\n" +
                "      <td>ELL211</td>\n" +
                "      <td>MCL242</td>\n" +
                "      <td>MCL242</td>\n" +
                "      <td>MCL242</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>MCL141</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>TXL755</td>\n" +
                "      <td>TXL755</td>\n" +
                "      <td>TXL755</td>\n" +
                "      <td>TXL772</td>\n" +
                "      <td>TXL772</td>\n" +
                "      <td>TXL772</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 510</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CLL723</td>\n" +
                "      <td>CLL723</td>\n" +
                "      <td>CLL723</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 512</th>\n" +
                "      <td>TXL711</td>\n" +
                "      <td>TXL711</td>\n" +
                "      <td>TXL711</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>MCL231</td>\n" +
                "      <td>MCL231</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL763</td>\n" +
                "      <td>MTL763</td>\n" +
                "      <td>MTL763</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MSL304</td>\n" +
                "      <td>MSL304</td>\n" +
                "      <td>MSL304</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 517</th>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>BBL331</td>\n" +
                "      <td>BBL331</td>\n" +
                "      <td>BBL331</td>\n" +
                "      <td>MCL135</td>\n" +
                "      <td>MCL135</td>\n" +
                "      <td>MTL731</td>\n" +
                "      <td>MTL731</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>COL351</td>\n" +
                "      <td>RDL770</td>\n" +
                "      <td>RDL770</td>\n" +
                "      <td>RDL770</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL375</td>\n" +
                "      <td>HUL375</td>\n" +
                "      <td>HUL375</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 518</th>\n" +
                "      <td>DSP731</td>\n" +
                "      <td>DSP731</td>\n" +
                "      <td>DSP731</td>\n" +
                "      <td>ESL340</td>\n" +
                "      <td>ESL340</td>\n" +
                "      <td>ESL340</td>\n" +
                "      <td>PYL701</td>\n" +
                "      <td>PYL701</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>MTL180</td>\n" +
                "      <td>APL765</td>\n" +
                "      <td>APL765</td>\n" +
                "      <td>APL765</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>CVL740</td>\n" +
                "      <td>CVL740</td>\n" +
                "      <td>CVL740</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 519</th>\n" +
                "      <td>ELL711</td>\n" +
                "      <td>ELL711</td>\n" +
                "      <td>ELL711</td>\n" +
                "      <td>BBL732</td>\n" +
                "      <td>BBL732</td>\n" +
                "      <td>BBL732</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MCL442</td>\n" +
                "      <td>MCL442</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL702</td>\n" +
                "      <td>ELL702</td>\n" +
                "      <td>ELL702</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>VEL700</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 520</th>\n" +
                "      <td>HSL384</td>\n" +
                "      <td>HSL384</td>\n" +
                "      <td>HSL384</td>\n" +
                "      <td>MCL322</td>\n" +
                "      <td>MCL322</td>\n" +
                "      <td>MCL322</td>\n" +
                "      <td>SBL201</td>\n" +
                "      <td>SBL201</td>\n" +
                "      <td>AML771</td>\n" +
                "      <td>AML771</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>HUL261</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL718</td>\n" +
                "      <td>HUL718</td>\n" +
                "      <td>HUL718</td>\n" +
                "      <td>MCL751</td>\n" +
                "      <td>MCL751</td>\n" +
                "      <td>MCL751</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 521</th>\n" +
                "      <td>MCL702</td>\n" +
                "      <td>MCL702</td>\n" +
                "      <td>MCL702</td>\n" +
                "      <td>HUL252</td>\n" +
                "      <td>HUL252</td>\n" +
                "      <td>HUL252</td>\n" +
                "      <td>TXL749</td>\n" +
                "      <td>TXL749</td>\n" +
                "      <td>CVL441</td>\n" +
                "      <td>CVL441</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>ELL202</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>PYL201</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL381</td>\n" +
                "      <td>HUL381</td>\n" +
                "      <td>HUL381</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 526</th>\n" +
                "      <td>CML741</td>\n" +
                "      <td>CML741</td>\n" +
                "      <td>CML741</td>\n" +
                "      <td>PYL115</td>\n" +
                "      <td>PYL115</td>\n" +
                "      <td>PYL115</td>\n" +
                "      <td>TXL242</td>\n" +
                "      <td>TXL242</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>PYL557</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>APL753</td>\n" +
                "      <td>APL753</td>\n" +
                "      <td>APL753</td>\n" +
                "      <td>ELL784</td>\n" +
                "      <td>ELL784</td>\n" +
                "      <td>ELL784</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 527</th>\n" +
                "      <td>CLL702</td>\n" +
                "      <td>CLL702</td>\n" +
                "      <td>CLL702</td>\n" +
                "      <td>TXL130</td>\n" +
                "      <td>TXL130</td>\n" +
                "      <td>TXL130</td>\n" +
                "      <td>CLL777</td>\n" +
                "      <td>CLL777</td>\n" +
                "      <td>CVL771</td>\n" +
                "      <td>CVL771</td>\n" +
                "      <td>PYL555</td>\n" +
                "      <td>PYL555</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL504</td>\n" +
                "      <td>MTL504</td>\n" +
                "      <td>MTL504</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 602</th>\n" +
                "      <td>HSL304</td>\n" +
                "      <td>HSL304</td>\n" +
                "      <td>HSL304</td>\n" +
                "      <td>MCL134</td>\n" +
                "      <td>MCL134</td>\n" +
                "      <td>MCL134</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL377</td>\n" +
                "      <td>HUL377</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>ELL203</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>MTL602</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>HUL315</td>\n" +
                "      <td>HUL315</td>\n" +
                "      <td>HUL315</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 603</th>\n" +
                "      <td>HUL361</td>\n" +
                "      <td>HUL361</td>\n" +
                "      <td>HUL361</td>\n" +
                "      <td>MCL345</td>\n" +
                "      <td>MCL345</td>\n" +
                "      <td>MCL345</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>DSL731</td>\n" +
                "      <td>DSL731</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>BMV703</td>\n" +
                "      <td>BMV703</td>\n" +
                "      <td>BMV703</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>SBV887</td>\n" +
                "      <td>SBV887</td>\n" +
                "      <td>SBV887</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 604</th>\n" +
                "      <td>HUL370</td>\n" +
                "      <td>HUL370</td>\n" +
                "      <td>HUL370</td>\n" +
                "      <td>HUL350</td>\n" +
                "      <td>HUL350</td>\n" +
                "      <td>HUL350</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>ELL304</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>COL202</td>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL502</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 606</th>\n" +
                "      <td>PYL551</td>\n" +
                "      <td>PYL551</td>\n" +
                "      <td>PYL551</td>\n" +
                "      <td>HUL355</td>\n" +
                "      <td>HUL355</td>\n" +
                "      <td>HUL355</td>\n" +
                "      <td>CLL794</td>\n" +
                "      <td>CLL794</td>\n" +
                "      <td>PYL553</td>\n" +
                "      <td>PYL553</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>TXL713</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>APL104</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>APL100</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>MTL101</td>\n" +
                "      <td>RDL705</td>\n" +
                "      <td>RDL705</td>\n" +
                "      <td>RDL705</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 611</th>\n" +
                "      <td>HUL376</td>\n" +
                "      <td>HUL376</td>\n" +
                "      <td>HUL376</td>\n" +
                "      <td>MTL105</td>\n" +
                "      <td>MTL105</td>\n" +
                "      <td>MTL105</td>\n" +
                "      <td>CML631</td>\n" +
                "      <td>CML631</td>\n" +
                "      <td>MTL501</td>\n" +
                "      <td>MTL501</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>CVL321</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>ELL311</td>\n" +
                "      <td>CLD353</td>\n" +
                "      <td>CLD353</td>\n" +
                "      <td>CLD353</td>\n" +
                "      <td>CLD353</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 612</th>\n" +
                "      <td>CVL741</td>\n" +
                "      <td>CVL741</td>\n" +
                "      <td>CVL741</td>\n" +
                "      <td>PTL707</td>\n" +
                "      <td>PTL707</td>\n" +
                "      <td>PTL707</td>\n" +
                "      <td>PYL655</td>\n" +
                "      <td>PYL655</td>\n" +
                "      <td>PYL753</td>\n" +
                "      <td>PYL753</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>APL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL657</td>\n" +
                "      <td>PYL657</td>\n" +
                "      <td>PYL657</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 613</th>\n" +
                "      <td>BMV701</td>\n" +
                "      <td>BMV701</td>\n" +
                "      <td>BMV701</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>PYL755</td>\n" +
                "      <td>PYL755</td>\n" +
                "      <td>BML741</td>\n" +
                "      <td>BML741</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL108</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>APL107</td>\n" +
                "      <td>CLN101</td>\n" +
                "      <td>CLN101</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL739</td>\n" +
                "      <td>PYL739</td>\n" +
                "      <td>PYL739</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 614</th>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL837</td>\n" +
                "      <td>MCL761</td>\n" +
                "      <td>MCL761</td>\n" +
                "      <td>MCL761</td>\n" +
                "      <td>CML726</td>\n" +
                "      <td>CML726</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>TXL381</td>\n" +
                "      <td>TXL381</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>ESL330</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ESQ304</td>\n" +
                "      <td>ESQ304</td>\n" +
                "      <td>ESQ304</td>\n" +
                "      <td>ESQ304</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 615</th>\n" +
                "      <td>PTL701</td>\n" +
                "      <td>PTL701</td>\n" +
                "      <td>PTL701</td>\n" +
                "      <td>CVL820</td>\n" +
                "      <td>CVL820</td>\n" +
                "      <td>CVL820</td>\n" +
                "      <td>ELL333</td>\n" +
                "      <td>ELL333</td>\n" +
                "      <td>MCL816</td>\n" +
                "      <td>MCL816</td>\n" +
                "      <td>PYL115</td>\n" +
                "      <td>PYL115</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>PESR (</td>\n" +
                "      <td>PESR (</td>\n" +
                "      <td>PESR (</td>\n" +
                "      <td>PESR (</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL800</td>\n" +
                "      <td>PYL800</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 619</th>\n" +
                "      <td>CVL758</td>\n" +
                "      <td>CVL758</td>\n" +
                "      <td>CVL758</td>\n" +
                "      <td>HUL340</td>\n" +
                "      <td>HUL340</td>\n" +
                "      <td>HUL340</td>\n" +
                "      <td>MCL703</td>\n" +
                "      <td>MCL703</td>\n" +
                "      <td>PYL723</td>\n" +
                "      <td>PYL723</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>ELL205</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>ELL751</td>\n" +
                "      <td>ELL751</td>\n" +
                "      <td>ELL751</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 620</th>\n" +
                "      <td>CVL711</td>\n" +
                "      <td>CVL711</td>\n" +
                "      <td>CVL711</td>\n" +
                "      <td>TXL712</td>\n" +
                "      <td>TXL712</td>\n" +
                "      <td>TXL712</td>\n" +
                "      <td>CVL748</td>\n" +
                "      <td>CVL748</td>\n" +
                "      <td>TXL724</td>\n" +
                "      <td>TXL724</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>HUL289</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PTL703</td>\n" +
                "      <td>PTL703</td>\n" +
                "      <td>PTL703</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 621</th>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL704</td>\n" +
                "      <td>MTL704</td>\n" +
                "      <td>MTL704</td>\n" +
                "      <td>TXL731</td>\n" +
                "      <td>TXL731</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>PYL116</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>MTL106</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 622</th>\n" +
                "      <td>ELL880</td>\n" +
                "      <td>ELL880</td>\n" +
                "      <td>ELL880</td>\n" +
                "      <td>MCL796</td>\n" +
                "      <td>MCL796</td>\n" +
                "      <td>MCL796</td>\n" +
                "      <td>BML710</td>\n" +
                "      <td>BML710</td>\n" +
                "      <td>BBL734</td>\n" +
                "      <td>BBL734</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>MTL342</td>\n" +
                "      <td>ELL743</td>\n" +
                "      <td>ELL743</td>\n" +
                "      <td>ELL743</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>PYL431</td>\n" +
                "      <td>PYL431</td>\n" +
                "      <td>PYL431</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <th>LH 623</th>\n" +
                "      <td>CML514</td>\n" +
                "      <td>CML514</td>\n" +
                "      <td>CML514</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>HUL271</td>\n" +
                "      <td>AML805</td>\n" +
                "      <td>AML805</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL601</td>\n" +
                "      <td>MTL601</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>MTL100</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>MTL766</td>\n" +
                "      <td>MTL766</td>\n" +
                "      <td>MTL766</td>\n" +
                "      <td>MTL757</td>\n" +
                "      <td>MTL757</td>\n" +
                "      <td>MTL757</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "      <td>Free</td>\n" +
                "    </tr>\n" +
                "  </tbody>\n" +
                "</table>";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }



}
