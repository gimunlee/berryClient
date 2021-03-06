package com.berry.second.secondprojectclient;

import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {
    public static final String port = "10900";
    public static final String urlPrefix = "http://ec2-52-78-67-28.ap-northeast-2.compute.amazonaws.com:" + port;
    public static String urlTestUserQuery = "?fid=" + "default";
    android.support.v7.app.ActionBar mActionBar;
    ViewSwitcher mViewSwitcher;
    FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookHelper.setup(this);
        setContentView(R.layout.activity_main);
        urlTestUserQuery = "?fid=" + FacebookHelper.mUserEmail;
        mActionBar = getSupportActionBar();

        {
            final LoginButton loginButton = (LoginButton) findViewById(R.id.loginButton);
            FacebookHelper.setupLoginButton(loginButton);
        }

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.mainViewSwitcher);

        {
            mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
            mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
            mTabHost.addTab(mTabHost.newTabSpec("contacts")
                            .setIndicator("Contacts", null),
                    ContactFragment.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("gallery")
                            .setIndicator("Gallery", null),
                    GalleryFragment.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("ground")
                            .setIndicator("Ground", null),
                    GroundFragment.class, null);
        }
        if (FacebookHelper.isLogon() && mViewSwitcher.getCurrentView() == mTabHost )
            mViewSwitcher.showNext();
    }

    @Override
    public void onDestroy() {
        FacebookHelper.end();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void setupForCurrentUser() {
        Log.d("gimun","setup!!!!!!!!!!!!!!!");
        GalleryFragment galleryFragment = (GalleryFragment)getSupportFragmentManager().findFragmentByTag("gallery");
        if(galleryFragment!=null)
            galleryFragment.fragmentinit();

        if(FacebookHelper.isLogon()) {
            urlTestUserQuery = "?fid=" + FacebookHelper.mUserEmail;
            mActionBar.setTitle("Hello, " + FacebookHelper.mUserName);
            if (mViewSwitcher.getNextView() == mTabHost)
                mViewSwitcher.showNext();
        }
        else {
            urlTestUserQuery="?fid=" + "default";
            if(mActionBar!=null)
                mActionBar.setTitle("Please sign in");
            if(mViewSwitcher!=null && mViewSwitcher.getNextView() != mTabHost)
                mViewSwitcher.showNext();
        }
    }
}
