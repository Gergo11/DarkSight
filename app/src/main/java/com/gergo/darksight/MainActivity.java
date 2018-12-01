package com.gergo.darksight;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.gergo.darksight.Audio.AudioMaker;
import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.ConnectDialog;
import com.gergo.darksight.Networking.SSLServer;
import com.gergo.darksight.UI.PagerAdapter;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;


public class MainActivity extends AppCompatActivity implements ConnectDialog.NoticeDialogListener {

    private ChatEngine chatEngine;
    private SSLServer sslServer;
    private SSLServerSocket serverSocket;
    private SSLSocket serverClient;
    private SSLSocket clientSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        AudioMaker audioMaker = AudioMaker.getAudioMaker();
        audioMaker.setContext(this);
        Common.USER_NAME = getResources().getString(R.string.txtInpUserNameDefault);
        chatEngine = ChatEngine.getChatEngine();
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
        if (!Common.isConsent && Common.isConnected) {
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

    public void sendNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this,"0")
                .setTicker(r.getString(R.string.notification_title))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(r.getString(R.string.notification_title))
                .setContentText(r.getString(R.string.notification_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
    public void sendConsent(){
        sslServer.sendMesseage(Common.connectionCode);
        if(Common.ADVANCED_ENCRYPTION){
            sslServer.sendMesseage(Common.advancedEncryptionCode);
        }
    }


    @Override
    protected void onPostResume() {
        if(Common.isConnected) {
            chatEngine.setSSLSocket();
        }
        super.onResume();
        super.onPostResume();
    }
}
