package com.berry.second.secondprojectclient.person;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PersonHelper {

    private static final String jsonPath = "contact_sample.json";
    private static final String urlPrefix = "http://ec2-52-78-67-28.ap-northeast-2.compute.amazonaws.com:10900";

    private static Context mContext;
    private static PersonListViewAdapter mAdapter;

    public static final List<Person> mItems = new ArrayList<Person>();
    public static final Map<Integer, Person> mItemsMap = new HashMap<Integer, Person>();

    private static int sNextId = 10001;

    private static final int COUNT = 3;

    public static void addItemWithTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        addItem(sdf.format(new Date(System.currentTimeMillis())), "time@korea", "010-3333-2222");
    }

    public static String createJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < mItems.size(); i++) {
            PersonHelper.Person item = mItems.get(i);
            stringBuilder.append("{\n");
            stringBuilder.append("\"name\":\"" + item.name + "\",\n");
            stringBuilder.append("\"email\":\"" + item.email + "\",\n");
            stringBuilder.append("\"phone\":\"" + item.phone + "\"\n");
            stringBuilder.append("}");
            if (i < mItems.size() - 1)
                stringBuilder.append(",");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public static void clearList() {
        mItems.clear();
        mItemsMap.clear();
        writeCurrentList();
        updateFromJson();
    }

    public class GetThread extends Thread {
        private String mUrlStr;
        public Object lock;

        public GetThread(String urlStr) {
            mUrlStr = urlStr;
            lock = new Object();
        }

        public void run() {
            try {
                String json;
                URL url = new URL(mUrlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                Log.d("gimun", "setting for request finished");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("gimun", "error HTTP code");
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }
                Log.d("gimun", "responseCode received");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String buffer;
                Log.d("gimun", "Output from server for /contacts...");
                while ((buffer = bufferedReader.readLine()) != null) {
                    Log.d("gimun", buffer);
                    stringBuilder.append(buffer);
                }
                json = stringBuilder.toString();
                conn.disconnect();

                importFromJson(json);
                writeJson(json);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("gimun","responseCode error");
                e.printStackTrace();
            }
            synchronized (this.lock) {
                this.lock.notify();
            }
        }
    }

    public void updateFromServer() {
        String receivedJson;

        mItems.clear();
        mItemsMap.clear();

        GetThread getThread = new GetThread(urlPrefix+"/A/contacts");
        synchronized (getThread.lock) {
            getThread.start();
            try {
                getThread.lock.wait();
            } catch (Exception e) {
                Log.d("gimun", "Failed to update from server");
                e.printStackTrace();
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public static void writeJson(String json) {
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(jsonPath, Context.MODE_PRIVATE);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCurrentList() {
        writeJson(createJson());
    }

    public static void updateFromJson() {
        mItems.clear();
        mItemsMap.clear();
        importFromJson(readJsonFromPath(jsonPath));
        mAdapter.notifyDataSetChanged();
    }

    public static void setup(Context context) {
//        addDefaultItems(COUNT);
        mContext = context;

        importFromJson(readJsonFromPath(jsonPath));
    }

    public static void setAdapter(PersonListViewAdapter adapter) {
        mAdapter = adapter;
    }

    public static void addDefaultItems(int num) {
        for (int i = 0; i < num; i++)
            addItem("emptyName", "emptyEmail", "emptyPhoneNumber");
    }

    private static void addItem(String name, String email, String phone) {
        Integer id = Integer.valueOf(sNextId);
        sNextId++;

        Person item = new Person(id, name, email, phone);
        mItems.add(item);
        mItemsMap.put(item.id, item);
    }

    public static String readJsonFromPath(String path) {
        try {
            InputStream inputStream = mContext.openFileInput(path);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receivedString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receivedString = bufferedReader.readLine()) != null)
                    stringBuilder.append(receivedString);
                inputStream.close();
                return stringBuilder.toString();
            } else
                return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean importFromJson(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject person = array.getJSONObject(i);
                addItem(person.getString("name"), person.getString("email"), person.getString("phone"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static class Person {
        public final Integer id;
        protected String name;
        protected String email;
        protected String phone;

        public Person(Integer id, String name, String email, String phone) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public final String getName() {
            return this.name;
        }

        public final Person setName(String name) {
            this.name = name;
            return this;
        }

        public final String getEmail() {
            return this.email;
        }

        public final Person setEmail(String email) {
            this.email = email;
            return this;
        }

        public final String getPhone() {
            return this.phone;
        }

        public final Person setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        @Override
        public String toString() {
            return "id : " + id.toString() +
                    "\tname : " + name +
                    "\temail : " + email +
                    "\tphone : " + phone;
        }
    }
}
