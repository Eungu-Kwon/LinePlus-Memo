package com.eungu.lineplusnote.MemoHandler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class WorkHandler extends Handler {
    public static final String HANDLE_IN_SAVE_TO_GALLERY = "gallery";
    public static final String HANDLE_IN_SAVE_FROM_URL = "fromurl";

    Context c;
    HandlerListener listener;

    public WorkHandler(Context c) {
        this.c = c;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        Bundle bun = msg.getData();
        String result = bun.getString("RESULT");
        String request = bun.getString("REQUEST");
        if(request == HANDLE_IN_SAVE_TO_GALLERY){
            if(result == "OK"){
                Toast.makeText(c.getApplicationContext(), "이미지를 저장했습니다.", Toast.LENGTH_LONG).show();
            }
            else if(result == "FAIL"){
                Toast.makeText(c.getApplicationContext(), "이미지를 저장하지 못했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        else if(request == HANDLE_IN_SAVE_FROM_URL) {
            listener.messageFromHandler(bun);
        }

    }

    public void setListener(HandlerListener listener) {
        this.listener = listener;
    }
}
