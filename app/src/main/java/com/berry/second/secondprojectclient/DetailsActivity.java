package com.berry.second.secondprojectclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by q on 2016-06-29.
 */
public class DetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        final String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String title = getIntent().getStringExtra("title");
        final int position = getIntent().getIntExtra("position", -1);
        Bitmap bitmap = BitmapFactory.decodeFile(title);//BitmapFactory.decodeResource(getResources(), imgs.getResourceId(getIntent().getIntExtra("image",-1), -1));
        TextView titleTextView = (TextView) findViewById(R.id.title);
        String[] title_p = title.split("/");

        titleTextView.setText(title_p[title_p.length-1].split("[.]")[0]);//title_parse[title_parse.length-1].split(".")[0]

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        Button delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    FileInputStream inputStream = openFileInput("mygallery.txt");
                    String dummy = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    for(int i=0; i<position; i++) {
                        line = reader.readLine(); //읽으며 이동
                        dummy += (line + "\r\n" );
                    }
                    String delData = reader.readLine();
                    while((line = reader.readLine())!=null) {
                        dummy += (line + "\r\n" );
                    }
                    reader.close();
                    inputStream.close();

                    FileOutputStream outputStream = openFileOutput("mygallery.txt", Context.MODE_PRIVATE);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write(dummy);
                    writer.close();
                    outputStream.close();
                    finish();
                    onStop();

                }catch (Exception e){

                }


            }
        });

    }

}
