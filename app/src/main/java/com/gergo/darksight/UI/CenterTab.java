package com.gergo.darksight.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.MainActivity;
import com.gergo.darksight.R;

public class CenterTab extends Fragment implements View.OnClickListener {

    private ChatEngine chatEngine;
    private EditText txtInput;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.center_tab_layout, container, false);
        txtInput = view.findViewById(R.id.txtInMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        chatEngine = ChatEngine.getChatEngine();
        chatEngine.setContext(this.getContext());
        ListView chatListView = view.findViewById(R.id.lstViewChat);
        chatListView.setAdapter(chatEngine);
        btnSend.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (!txtInput.getText().toString().equals("")) {
            chatEngine.sendMessage(txtInput.getText().toString());
            txtInput.setOnClickListener(null);
            txtInput.setText("");
            txtInput.setOnClickListener(this
            );
        }
    }
}
