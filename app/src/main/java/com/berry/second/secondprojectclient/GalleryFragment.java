package com.berry.second.secondprojectclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

import com.amazonaws.Response;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Future;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link GalleryFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link GalleryFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class GalleryFragment extends Fragment {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 202;
    private static final String urlPrefix = MainActivity.urlPrefix;
    private static final String urlTestUserQuery = MainActivity.urlTestUserQuery;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // When an Image is picked
            if(requestCode == 2){
                fragmentinit();
            }
            else {
                if (requestCode == RESULT_LOAD_IMG ) {
                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    try {
                        //////////////////////////////////////////////////////////////////////////////////////////////

                        File f = new File(imgDecodableString);

                        Future uploading = Ion.with(getActivity())
                                .load(urlPrefix + "/B/upload" + urlTestUserQuery)
                                .setMultipartFile("image", f)
                                .asString().setCallback(new FutureCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception ex, String iv) {
                                    }

                                });

                        String extSD = Environment.getExternalStorageDirectory().toString();
                        String name = imgDecodableString.split("/")[imgDecodableString.split("/").length-1].split("[.]")[0] + ".jpg";
                        OutputStream outStream = null;
                        File file = new File(extSD, name);
                        Bitmap fibi = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imgDecodableString),300,300,true);
                        outStream = new FileOutputStream(file);
                        fibi.compress(Bitmap.CompressFormat.JPEG,100,outStream);
                        outStream.flush();
                        outStream.close();
                        f = new File(extSD, name);
                        Future uploadingsmall = Ion.with(getActivity())
                                .load(urlPrefix + "/B/uploadsmall" + urlTestUserQuery)
                                .setMultipartFile("image", f)
                                .asString().setCallback(new FutureCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception ex, String iv) {
                                        fragmentinit();
                                    }

                                });
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        //Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
                        Thread.sleep(1000);
                        fragmentinit();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Set the Image in ImageView after decoding the String
                    //imgView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));

                } else {
                    Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivityForResult(intent,2);
            }
        });

        Button button=(Button) rootView.findViewById(R.id.button);
        button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImagefromGallery(view);
            }
        }));
//        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loadImagefromGallery(view);
//            }
//        });

        return rootView;
    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();

        try {

            JSONArray images = getImages(null);

            for(int i=0; i<images.length(); i++){
                String s = images.getJSONObject(i).getString("url");
                URL url = new URL(s);

                imageItems.add(new ImageItem(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()),300,300,true), s, i));
            }
        }catch(Exception e){

        }

        return imageItems;
    }

    private JSONArray getImages(String fid){
        String urlString = urlPrefix + "/B/images" + urlTestUserQuery;



        try {
            // call API by using HTTPURLConnection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONArray json = new JSONArray(getStringFromInputStream(in));

            // parse JSON

            //return json.getJSONArray("images");
            return json;

        }catch(MalformedURLException e){
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;
        }catch(JSONException e) {
            System.err.println("JSON parsing error");
            e.printStackTrace();
            return null;
        }catch(IOException e){
            System.err.println("URL Connection failed");
            e.printStackTrace();
            return null;
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private void fragmentinit(){
        gridView = (GridView) getView().findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivityForResult(intent, 2);
            }
        });
    }




}
