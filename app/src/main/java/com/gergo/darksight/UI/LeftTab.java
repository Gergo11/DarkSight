package com.gergo.darksight.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gergo.darksight.MainActivity;
import com.gergo.darksight.Networking.IpUtil;
import com.gergo.darksight.R;

public class LeftTab extends Fragment{

    IpUtil ipUtil = null;

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.left_tab_layout, container, false);
        TextView txtIP = view.findViewById(R.id.txtIP);
        ipUtil = IpUtil.getIpUtil();
        ipUtil.setAct(getActivity());
        setTxtIp(txtIP);
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


}
