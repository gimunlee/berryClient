package com.berry.second.secondprojectclient.gallery;

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
import android.widget.Toast;

import com.berry.second.secondprojectclient.R;
import com.koushikdutta.async.http.BasicNameValuePair;
import com.koushikdutta.async.http.NameValuePair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by q on 2016-06-29.
 */
public class DetailsActivity extends ActionBarActivity {
    private static final String port = "10900";
    private static final String urlPrefix = "http://ec2-52-78-67-28.ap-northeast-2.compute.amazonaws.com:"+port;
    private static final String urlTestUserQuery = "?fid=gaianofc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        final String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String imageurl_o = getIntent().getStringExtra("title");
        final String imageurl = imageurl_o.replace("/smallimages", "/images");
        final int position = getIntent().getIntExtra("position", -1);
        TextView titleTextView = (TextView) findViewById(R.id.title);
        String[] title_p = imageurl.split("/");

        titleTextView.setText(title_p[title_p.length-1].split("[.]")[0]);//title_parse[title_parse.length-1].split(".")[0]

        ImageView imageView = (ImageView) findViewById(R.id.image);
        try {
            URL url = new URL(imageurl);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            imageView.setImageBitmap(bitmap);
        }catch(Exception e){}


        Button delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    try {

                        URL url = new URL(urlPrefix + "/B/delete" + urlTestUserQuery);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type","application/json");
//Create JSONObject here
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("url",imageurl_o.split("/")[imageurl.split("/").length-1]);
                        OutputStream printout = conn.getOutputStream();
                        printout.write(jsonParam.toString().getBytes());
                        printout.flush();
                        printout.close();
                        String buffer = null;
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while((buffer = in.readLine()) != null){
                        }
                        in.close();


                    } catch (MalformedURLException e) {

                        e.printStackTrace();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }
                    finish();
                    onStop();

                }catch (Exception e){

                }


            }
        });

    }
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
