package com.gergo.darksight.UI;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LeftTab extends Fragment{

    IpUtil ipUtil = null;
    SSLClient sslClient = null;
    ChatEngine chatEngine = null;
    IpReaderDbHelper mDbHelper;

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.left_tab_layout, container, false);
        final EditText txtConnectIP = view.findViewById(R.id.txtInIP);
        final EditText txtUseName = view.findViewById(R.id.txtUserName);
        TextView txtIP = view.findViewById(R.id.txtIP);
        ImageButton connectButton = view.findViewById(R.id.btnConnect);
        final CheckBox chkRemember = view.findViewById(R.id.chkRemember);
        final AudioMaker audioMaker = AudioMaker.getAudioMaker();
        mDbHelper = new IpReaderDbHelper(getContext());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(view.getContext(),R.layout.spinner_item,readFromDB());
        final Spinner spinner = view.findViewById(R.id.spSavedIp);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                txtConnectIP.setText(spinner.getSelectedItem().toString());
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
                if (chkRemember.isChecked()){
                    instertToDB(txtConnectIP.getText().toString());
                }
                if(Common.SOUND){
                    audioMaker.connect();
                }
                if(txtUseName.getText()!=null) {
                    Common.USER_NAME = txtUseName.getText().toString();
                }
                else{
                    Common.USER_NAME=String.valueOf(R.string.txtInpUserNameDefault);
                }
                sslClient = new SSLClient(getContext(), chatEngine);
                chatEngine.setSslClient(sslClient);
                Common.secretConnectionInProgress = true;
                sslClient.initializeConnection(txtConnectIP.getText().toString());
            }
        });
        return view;
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
    private void instertToDB(String ip){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(IpContract.IpEntry.COLUMN_NAME_IP, ip);

        long newRowId = db.insertWithOnConflict(IpContract.IpEntry.TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
    }

    private List<String> readFromDB() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                IpContract.IpEntry.COLUMN_NAME_IP
        };

        String selection = IpContract.IpEntry.COLUMN_NAME_IP + " = ?";
        String[] selectionArgs = {"STORE"};

        String sortOrder =
                IpContract.IpEntry.COLUMN_NAME_IP + " DESC";

        Cursor cursor = db.query(
                IpContract.IpEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<String> stringsForSpinner = new ArrayList<>();
        while (cursor.moveToNext()) {
            String item = cursor.getString(
                    cursor.getColumnIndexOrThrow(IpContract.IpEntry._ID));
            stringsForSpinner.add(item);
        }
        cursor.close();
        if (stringsForSpinner.size()==0){
            stringsForSpinner.add(String.valueOf(R.string.spinnerNoIps));
        }
        return stringsForSpinner;
    }
}
