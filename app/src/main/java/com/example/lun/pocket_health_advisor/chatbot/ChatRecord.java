package com.example.lun.pocket_health_advisor.chatbot;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.lun.pocket_health_advisor.R;

/**
 * Created by Lun on 11/01/2018.
 */

public class ChatRecord extends RecyclerView.ViewHolder {

    TextView leftText, rightText;

    public ChatRecord(View itemView) {
        super(itemView);

        leftText = itemView.findViewById(R.id.leftText);
        rightText = itemView.findViewById(R.id.rightText);
    }
}