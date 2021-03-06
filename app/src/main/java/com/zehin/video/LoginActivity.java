package com.zehin.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by wlf on 2017/6/7.
 */

public class LoginActivity extends Activity implements View.OnClickListener{

    private Button button1;
    private Button button2;

    private String IP = "123.234.227.107";
//    private String IP = "218.201.111.234";
//    private String IP = "192.168.3.158";
//    private int camId = 1062043;
//    private int camId = 1062091;
//   private int camId = 5126; // 交通公司
//    private int camId = 13558;
//    private int camId = 14920;
    private int camId = 13768;
//    private int camId = 14244; // 没有时间
    private int streamType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == button1){
            Intent intent = new Intent(LoginActivity.this,LiveVideoActivity.class);
            intent.putExtra("stunIP",IP);
            intent.putExtra("centerIP",IP);
            intent.putExtra("camId",camId);
            intent.putExtra("streamType",streamType);
            startActivity(intent);
        } else if(v == button2){
            Intent intent = new Intent(LoginActivity.this,BackPlayVideoActivity.class);
            intent.putExtra("stunIP",IP);
            intent.putExtra("centerIP",IP);
            intent.putExtra("camId",camId);
            intent.putExtra("streamType",streamType);
            startActivity(intent);
        }
    }
}
