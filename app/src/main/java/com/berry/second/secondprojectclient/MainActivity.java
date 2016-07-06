package com.berry.second.secondprojectclient;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.berry.second.secondprojectclient.facebook.FacebookHelper;
import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {
    public static final String port = "10900";
    public static final String urlPrefix = "http://ec2-52-78-67-28.ap-northeast-2.compute.amazonaws.com:"+port;
    public static final String urlTestUserQuery = "?fid=gaianofc";
    android.support.v7.app.ActionBar mActionBar;
    ViewSwitcher mViewSwitcher;
    FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookHelper.setup(this);
        setContentView(R.layout.activity_main);

        mActionBar=getSupportActionBar();

        {
            final LoginButton loginButton=(LoginButton) findViewById(R.id.loginButton);
            FacebookHelper.setupLoginButton(loginButton);
//            loginButton.setReadPermissions("user_friends");
//            loginButton.registerCallback(FacebookHelper.getCallBackManager(), FacebookHelper.getLoginCallback());
        }

        mViewSwitcher=(ViewSwitcher) findViewById(R.id.mainViewSwitcher);

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
        if(FacebookHelper.isLogon())
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

    public void onTokenChanged(AccessToken newToken) {
        if(newToken==null) {
            Log.d("gimun", "Facebook logout");
            mActionBar.setTitle("Bye~");
            mViewSwitcher.showPrevious();
        }
        else {
            Log.d("gimun", "new token " + newToken.getToken());
            mActionBar.setTitle("Hello, ");
            mViewSwitcher.showNext();
        }
    }
}
