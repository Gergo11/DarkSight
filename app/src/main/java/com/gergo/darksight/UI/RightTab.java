package com.gergo.darksight.UI;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.IpContract;
import com.gergo.darksight.Logic.IpReaderDbHelper;
import com.gergo.darksight.R;

public class RightTab extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private Switch swAdvancedEnc = null;
    private IpReaderDbHelper mDBHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.right_tab_layout, container, false);
        mDBHelper = new IpReaderDbHelper(getContext());
        swAdvancedEnc = view.findViewById(R.id.swAdvancedEnc);
        Switch swNotifications = view.findViewById(R.id.swNotifications);
        Switch swSound = view.findViewById(R.id.swSound);
        ImageButton imageButton = view.findViewById(R.id.btnClearDB);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = mDBHelper.getWritableDatabase();
                db.delete(IpContract.TABLE_NAME, null, null);
            }
        });
        swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Common.SOUND = b;
            }
        });
        swAdvancedEnc.setOnCheckedChangeListener(this);
        swNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Common.NOTIFICATIONS = b;
            }
        });
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!Common.isConnected) {
            Common.ADVANCED_ENCRYPTION = b;
        }else {
            swAdvancedEnc.setOnCheckedChangeListener(null);
            swAdvancedEnc.setChecked(false);
            swAdvancedEnc.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setSwitch();
    }

    public void setSwitch() {
        swAdvancedEnc.setOnCheckedChangeListener(null);
        swAdvancedEnc.setChecked(Common.ADVANCED_ENCRYPTION);
        swAdvancedEnc.setOnCheckedChangeListener(this);
    }
}
