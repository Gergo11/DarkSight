package com.gergo.darksight.UI;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.gergo.darksight.Audio.AudioMaker;
import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.IpContract;
import com.gergo.darksight.Logic.IpReaderDbHelper;
import com.gergo.darksight.Networking.IpUtil;
import com.gergo.darksight.Networking.SSLClient;
import com.gergo.darksight.R;

import java.util.ArrayList;
import java.util.List;

public class LeftTab extends Fragment {

    private IpUtil ipUtil = null;
    private SSLClient sslClient = null;
    private ChatEngine chatEngine = null;
    private IpReaderDbHelper mDbHelper;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.left_tab_layout, container, false);
        final EditText txtConnectIP = view.findViewById(R.id.txtInIP);
        final EditText txtUseName = view.findViewById(R.id.txtUserName);
        final ImageButton logoBtn = view.findViewById(R.id.logo_large_left);
        final Animation animation_logo_1 = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate);
        final Animation animation_logo_2 = AnimationUtils.loadAnimation(view.getContext(), R.anim.antirotate);
        ImageButton btnDisconnect = view.findViewById(R.id.btnDisconnect);
        TextView txtIP = view.findViewById(R.id.txtIP);
        ImageButton connectButton = view.findViewById(R.id.btnConnect);
        final CheckBox chkRemember = view.findViewById(R.id.chkRemember);
        final AudioMaker audioMaker = AudioMaker.getAudioMaker();
        mDbHelper = new IpReaderDbHelper(getContext());
        spinnerAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_item, readFromDBtoStringArray());
        final Spinner spinner = view.findViewById(R.id.spSavedIp);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!spinner.getSelectedItem().toString().equals(getResources().getString(R.string.spinnerNoIps))) {
                    txtConnectIP.setText(spinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ipUtil = IpUtil.getIpUtil();
        ipUtil.setAct(getActivity());
        chatEngine = ChatEngine.getChatEngine();
        setTxtIp(txtIP);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.isClientMode = true;
                if (chkRemember.isChecked()) {
                    instertToDB(txtConnectIP.getText().toString());
                }
                if (Common.SOUND) {
                    audioMaker.connect();
                }
                if (txtUseName.getText() != null) {
                    Common.USER_NAME = txtUseName.getText().toString();
                } else {
                    Common.USER_NAME = String.valueOf(R.string.txtInpUserNameDefault);
                }
                sslClient = new SSLClient(getContext(), chatEngine);
                chatEngine.setSslClient(sslClient);
                Common.secretConnectionInProgress = true;
                Common.SEND_KEYS = true;
                sslClient.initializeConnection(txtConnectIP.getText().toString());
            }
        });
        logoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(logoBtn, animation_logo_1, animation_logo_2);
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatEngine.disconnect();
            }
        });
        return view;
    }

    private List<String> readFromDBtoStringArray() {
        List<String> ipList = new ArrayList<>();
        for (IpContract item : readFromDB()) {
            ipList.add(item.getIp());
        }
        return ipList;
    }

    private void setTxtIp(TextView txtIp) {
        switch (ipUtil.getNetworkType()) {
            case 1:
                txtIp.setText(ipUtil.getMobileIP());
                break;
            case 2:
                txtIp.setText(ipUtil.getWifiIP());
                break;
            default:
                break;
        }
    }

    private void instertToDB(String ip) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + IpContract.TABLE_NAME + " WHERE "+IpContract.COLUMN_IP +"= ?", new String[]{ip});
        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(IpContract.COLUMN_IP, ip);
            long newRowId = db.insertWithOnConflict(IpContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
        spinnerAdapter.notifyDataSetChanged();
    }

    private List<IpContract> readFromDB() {
        List<IpContract> ipContracts = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selectQuery = "Select * from " + IpContract.TABLE_NAME + " ORDER BY " + IpContract.COLUMN_IP + " DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                IpContract ipContract = new IpContract();
                ipContract.setId(cursor.getInt(cursor.getColumnIndex(IpContract.COLUMN_ID)));
                ipContract.setIp(cursor.getString(cursor.getColumnIndex(IpContract.COLUMN_IP)));
                ipContracts.add(ipContract);
            } while (cursor.moveToNext());
        }
        db.close();
        return ipContracts;
    }

    public void animate(final ImageButton logoBtn, final Animation animation_logo_1, Animation animation_logo_2) {
        logoBtn.startAnimation(animation_logo_2);

        animation_logo_2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logoBtn.startAnimation(animation_logo_1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation_logo_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
