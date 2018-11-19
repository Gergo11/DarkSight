package com.gergo.darksight.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.MainActivity;
import com.gergo.darksight.R;

public class CenterTab extends Fragment {

    private ChatEngine chatEngine;
    private MainActivity mainActivity;


    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.center_tab_layout, container, false);
        final EditText txtInput = view.findViewById(R.id.txtInMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        chatEngine = ChatEngine.getChatEngine();
        chatEngine.setContext(this.getContext());
        ListView chatListView = view.findViewById(R.id.lstViewChat);
        chatListView.setAdapter(chatEngine);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatEngine.sendMessage(txtInput.getText().toString());
            }
        });
        return view;
    }
}
