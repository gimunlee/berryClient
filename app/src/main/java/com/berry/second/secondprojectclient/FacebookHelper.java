package com.berry.second.secondprojectclient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by q on 2016-07-06.
 */
public class FacebookHelper {
    public static String mUserId = "";
    public static AccessToken mUserToken;
    public static String mUserEmail=null;
    public static String mUserName = null;

    public static boolean isLogon() {
        return mUserToken != null;
    }

    public static FacebookCallback<LoginResult> loginCallback;

    public static FacebookCallback<LoginResult> getLoginCallback() {
        return loginCallback;
    }

    private static String updateUserName() {
        if(!isLogon()) {
            mUserName="default";
            return "";
        }
        Log.d("request", "update starts");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mUserToken) {
                    GraphRequest request = GraphRequest.newMeRequest(mUserToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                mUserName = object.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "name");
                    request.setParameters(parameters);
                    request.executeAndWait();
                    mUserToken.notify();
                }
            }
        });
        synchronized (mUserToken) {
            t.start();
            try {
                mUserToken.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mUserName;
        }
    }
    private static String updateUserEmail() {
        if(!isLogon()) {
            mUserEmail="";
            return "";
        }
        Log.d("request", "update email starts");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mUserToken) {
                    GraphRequest request = GraphRequest.newMeRequest(mUserToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                mUserEmail = object.getString("email");
//                                Log.d("gimun", "json name : " + object.getString("name"));
//                                Log.d("request", "updated. your name : " + mUserName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
                    request.setParameters(parameters);
                    request.executeAndWait();
//                    Log.d("request", "right after request.executeAndWait()");
                    mUserToken.notify();
                }
            }
        });
        synchronized (mUserToken) {
            t.start();
            try {
//                Log.d("request", "waiting");
                mUserToken.wait();
//                Log.d("request", "wait finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mUserEmail;
        }
    }

    private static MainActivity mActivity;
    private static CallbackManager callbackManager;

    public static CallbackManager getCallBackManager() {
        return callbackManager;
    }

    private static AccessTokenTracker accessTokenTracker;

    public static void updateWithToken(AccessToken token) {
        mUserToken=token;
        updateUserName();
        updateUserEmail();
        if(mActivity!=null)
            mActivity.setupForCurrentUser();
        Log.d("setup","update for new query : " + MainActivity.urlTestUserQuery);
    }
    public static void setup(MainActivity mainActivity)
    {
        mActivity = mainActivity;
        Log.d("gimun","mActivity : "+mActivity.getClass());
        FacebookSdk.sdkInitialize(mActivity.getApplicationContext());
        AppEventsLogger.activateApp(mActivity.getApplication());

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateWithToken(currentAccessToken);
            }
        };

        loginCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        };
        updateWithToken(AccessToken.getCurrentAccessToken());
    }

    public static void setupLoginButton(LoginButton button) {
        button.setReadPermissions("user_friends");
        button.setReadPermissions("email");
        button.registerCallback(getCallBackManager(), getLoginCallback());
    }

    public static void end() {
        accessTokenTracker.stopTracking();
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
