package com.gergo.darksight;


import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.ConnectDialog;
import com.gergo.darksight.Networking.SSLServer;
import com.gergo.darksight.UI.PagerAdapter;


public class MainActivity extends AppCompatActivity implements ConnectDialog.NoticeDialogListener {

    private ChatEngine chatEngine;
    private SSLServer sslServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        chatEngine = ChatEngine.getChatEngine(); //needs to be before the ssl server
        //chatEngine.setContext(this);
        sslServer = new SSLServer(this, chatEngine);
        sslServer.initializeServer();
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tab_pager);
        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onPause() {
        Common.inBackGround = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.inBackGround = false;
        if (Common.isConnected) {
            showDialog();
        }
    }

    public void showDialog() {
        DialogFragment dialog = new ConnectDialog();
        dialog.show(getSupportFragmentManager(), "ConnectDialog");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        chatEngine.sendMessage(Common.connectionCode);
        Common.isConsent=true;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Common.isConnected=false;
        sslServer.restartServer();
    }

}
