package com.example.test8;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class Display extends Rating {
    TextView textView9;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        textView9=findViewById(R.id.textView9);
        textView9.setMovementMethod(ScrollingMovementMethod.getInstance());

        StringBuilder stringBuilder2 = new StringBuilder();
        for (String s : resultMovies) {
            stringBuilder2.append(s + "\n");
        }
        textView9.setText(stringBuilder2.toString());
    }
}