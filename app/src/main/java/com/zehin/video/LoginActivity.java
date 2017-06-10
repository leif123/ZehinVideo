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
            startActivity(intent);
        }
    }
}
