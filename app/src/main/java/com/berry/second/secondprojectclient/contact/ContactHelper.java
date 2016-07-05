package com.berry.second.secondprojectclient.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactHelper {

    // Contants
    private static final String jsonPath = "contact_sample.json";
    private static final String port = "10012";
    private static final String urlPrefix = "http://ec2-52-78-67-28.ap-northeast-2.compute.amazonaws.com:"+port;
    private static final String urlTestUserQuery = "?fid=gaianofc";

    // Context and view
    private static Context mContext;
    private static ContactListViewAdapter mAdapter;

    // Data of contacts
    public static final List<Contact> mItems = new ArrayList<Contact>();
    public static final Map<Integer, Contact> mItemsMap = new HashMap<Integer, Contact>();

    // Static variable
    private static int sNextId = 10001;

    // Items for test
    public static void addItemWithTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        addItem(sdf.format(new Date(System.currentTimeMillis())), "time@korea", "010-3333-2222");
    }
    public static void addDefaultItems(int num) {
        for (int i = 0; i < num; i++)
            addItem("emptyName", "emptyEmail", "emptyPhoneNumber");
    }

    // Current status => Json
    public static String createJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < mItems.size(); i++) {
            Contact item = mItems.get(i);
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

    // Clear Current status
    public static void clearList() {
        mItems.clear();
        mItemsMap.clear();
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
    }

    // I/O with server
    public class PostThread extends Thread {
        private String mUrlStr;
        private String mJson;

        public PostThread(String urlStr, String json) {
            mUrlStr = urlStr;
            mJson=json;
        }

        public void run() {
            try {
                Log.d("gimun","posing json : " + mJson);
                URL url = new URL(mUrlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                Log.d("gimun", "setting for post request finished");

                OutputStream outputStream = conn.getOutputStream();
//                outputStream.write("[{\"name\":\"gimun\",\"email\":\"gim@kaist.ac.kr\",\"phone\":\"010-test-test\"}]".getBytes());
                outputStream.write(mJson.getBytes());
                outputStream.flush();

                Log.d("gimun","waiting for responsecode");
                if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }
                Log.d("gimun","reading body of response");
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Log.d("gimun","Output from Server for POST /contacts ...");
                String output;
                while((output=bufferedReader.readLine()) !=null)
                    Log.d("gimun",output);
                conn.disconnect();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (this) {
                this.notify();
            }
        }
    }
    public void postToServer() {
        PostThread postThread = new PostThread(urlPrefix+"/A/contacts"+urlTestUserQuery,createJson());
        synchronized (postThread) {
            postThread.start();
            try {
                postThread.wait();
                postToFile();
            } catch (InterruptedException e) {
                Log.d("gimun", "Post Thread interrupted");
                e.printStackTrace();
            }
        }
    }
    public class GetThread extends Thread {
        private String mUrlStr;
        private String mJson;

        public GetThread(String urlStr) {
            mUrlStr = urlStr;
        }

        public String getJson() {
            return mJson;
        }
        public void run() {
            try {
                URL url = new URL(mUrlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                Log.d("gimun", "setting for get request finished");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("gimun", "error HTTP code");
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }
                Log.d("gimun", "responseCode received");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String buffer;
                Log.d("gimun", "Output from server for GET /contacts...");
                while ((buffer = bufferedReader.readLine()) != null) {
                    Log.d("gimun", buffer);
                    stringBuilder.append(buffer);
                }
                conn.disconnect();
                mJson = stringBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("gimun","responseCode error");
                e.printStackTrace();
            }
            synchronized (this) {
                this.notify();
            }
        }
    }
    public void updateFromServer() {
        GetThread getThread = new GetThread(urlPrefix+"/A/contacts"+urlTestUserQuery);
        synchronized (getThread) {
            getThread.start();
            try {
                getThread.wait();
                clearList();
                importFromJson(getThread.getJson());
                postToFile();
            } catch (InterruptedException e) {
                    Log.d("gimun", "Get Thread interrupted");
                    e.printStackTrace();
            }
        }
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
    }

    // I/O with file
    public static String readJsonFromFile() {
        try {
            InputStream inputStream = mContext.openFileInput(jsonPath);
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
    public static void postToFile() {
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(jsonPath, Context.MODE_PRIVATE);
            outputStream.write(createJson().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void updateFromFile() {
        clearList();
        importFromJson(readJsonFromFile());
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
    }

    //Importing
    private static boolean isPhone(String str) {
        return str.length()>4 && str.matches("^[0-9|+|-]*$");
    }
    public static void importLocalContacts()
    {
        HashMap<String , JSONObject> temp = new HashMap<String,JSONObject>();
        ArrayList<JSONObject> res = new ArrayList<JSONObject>();
        ContentResolver cr = mContext.getContentResolver();
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";

        String[] projection = new String[] { ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.PHOTO_ID,
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA2, // type
                ContactsContract.Data.DATA1  // phone.number, organization.company
        };

        Cursor mCursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                ContactsContract.Data.MIMETYPE +"='"+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE +"' or " +
                        ContactsContract.Data.MIMETYPE +"='"+ ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE +"'",
                null,
                ContactsContract.Data.DISPLAY_NAME+","+ContactsContract.Data._ID+" COLLATE LOCALIZED ASC");

        int idIdx = mCursor.getColumnIndex( ContactsContract.Data.CONTACT_ID);
        int nameIdx = mCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
        int numIdx = mCursor.getColumnIndex(ContactsContract.Data.DATA1);
        int emailIdx = mCursor.getColumnIndex(ContactsContract.Data.DATA2);
        int photoIdx = mCursor.getColumnIndex(ContactsContract.Data.PHOTO_ID);
        if(mCursor.moveToFirst()) {
            String id = " ", name = " ", num = " ", email=" ", photo=" ";
            do {
                id = " "; name = " "; num = " "; email=" "; photo=" ";
                if (idIdx != -1) id = mCursor.getString(idIdx);
                if (nameIdx != -1) name = mCursor.getString(nameIdx);
                if (numIdx != -1) num = mCursor.getString(numIdx);
                if (emailIdx != -1) email = mCursor.getString(emailIdx);
                if (photoIdx != -1) photo = mCursor.getString(photoIdx);
                if(temp.containsKey(id)){
                    JSONObject now = temp.get(id);try{
                        if (id!=null &&!id.equals(" ") && !now.has("id")) now.put("id", id);
                        if (name!=null && !name.equals(" ")&& !now.has("name")) now.put("name", name);
                        if (num!=null && !num.equals(" ")) {
                            if(isPhone(num) && !now.has("phone")) now.put("phone",num);
                            else if(!isPhone(num) && !now.has("email")) now.put("email",num);
                        }
                        if (photo!=null && !photo.equals(" ") && !now.has("photo")) now.put("photo", photo);}catch(Exception e){
//                        debug("err1"+e.toString());
                    }
                } else {
                    JSONObject now = new JSONObject();
                    try {
                        if (!id.equals(" ")) now.put("id", id);
                        if (!name.equals(" ")) now.put("name", name);
                        if (!num.equals(" ")) {
                            if(isPhone(num)) now.put("phone",num);
                            else now.put("email",num);
                        }
                        if (photo!=null && !photo.equals(" ")) now.put("photo", photo);
                    } catch(Exception e){
//                        debug(e.toString());
                    }
                    temp.put(id,now);
                }
//                Log.d("gimun",""+id+name+num+email+photo);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        for(Map.Entry<String, JSONObject> i : temp.entrySet())
        {
            Log.d("gimun",""+i.getKey().toString() + i.getValue().toString());
            String name="empty", email="empty", phone="empty";
            JSONObject object=i.getValue();
            try {
                if (object.has("name")) name = object.get("name").toString();
                if(object.has("email")) email = object.get("email").toString();
                if(object.has("phone")) phone = object.get("phone").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            addItem(name,email,phone);

//            res.add(i.getValue());
        }
//        return res;
    }

    // Setting members
    public static void setup(Context context) {
        mContext = context;

        updateFromFile();
    }
    public static void setAdapter(ContactListViewAdapter adapter) {
        mAdapter = adapter;
    }

    // Manipulators
    private static void addItem(String name, String email, String phone) {
        Integer id = Integer.valueOf(sNextId);
        sNextId++;

        Contact item = new Contact(id, name, email, phone);
        int pos=mItems.size();
        mItems.add(item);
        mItemsMap.put(item.id, item);
        if(mAdapter!=null)
            mAdapter.notifyItemInserted(pos);
    }
    public static boolean importFromJson(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject contact = array.getJSONObject(i);
                addItem(contact.getString("name"), contact.getString("email"), contact.getString("phone"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    // Item class
    public static class Contact {
        public final Integer id;
        protected String name;
        protected String email;
        protected String phone;

        public Contact(Integer id, String name, String email, String phone) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public final String getName() {  return this.name;  }
        public final String getEmail() { return this.email; }
        public final String getPhone() { return this.phone; }

        @Override
        public String toString() {
            return "id : " + id.toString() +
                    "\tname : " + name +
                    "\temail : " + email +
                    "\tphone : " + phone;
        }
    }
}
