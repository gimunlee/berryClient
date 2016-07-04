package com.berry.second.secondprojectclient;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    FragmentTabHost mTabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost=(FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("contacts")
                                .setIndicator("Contacts",null),
                        ContactFragment.class,null);
        mTabHost.addTab(mTabHost.newTabSpec("gallery")
                        .setIndicator("Gallery",null),
                GalleryFragment.class,null);
        mTabHost.addTab(mTabHost.newTabSpec("ground")
                        .setIndicator("Ground",null),
                GroundFragment.class,null);
    }
}
