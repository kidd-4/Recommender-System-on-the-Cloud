package com.example.test8;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class Display extends Rating {
    TextView textView9;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        String[] data = intent.getStringArrayExtra("display");

        textView9=findViewById(R.id.textView9);
        textView9.setMovementMethod(ScrollingMovementMethod.getInstance());

        StringBuilder stringBuilder2 = new StringBuilder();
        if(data.length == 0){
            stringBuilder2.append("No Recommender Movies");
        }
        else {
            for (int i = 0; i < data.length; i++) {
                stringBuilder2.append(i + 1 + " :" + data[i] + "\n");
            }
        }

        textView9.setText(stringBuilder2.toString());
    }
}