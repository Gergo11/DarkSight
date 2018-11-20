package com.gergo.darksight.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.R;

public class RightTab extends Fragment {

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.left_tab_layout, container, false);
        final Switch swSound = view.findViewById(R.id.swSound);
        Switch swAdvancedEnc = view.findViewById(R.id.swAdvancedEnc);
        Switch swNotifications = view.findViewById(R.id.swNotifications);
        swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Common.SOUND = b;
            }
        });
        swAdvancedEnc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Common.ADVANCED_ENCRYPTION = b;
            }
        });
        swNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Common.NOTIFICATIONS = b;
            }
        });
        return view;
    }
}
