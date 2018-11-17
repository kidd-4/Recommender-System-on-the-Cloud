package com.example.test8;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Rating extends MainActivity {
    public String str0;
    public String str1;
    public String str2;
    List<String> nameList = new ArrayList();
    List<String> scoreList = new ArrayList();
    List<String> resultMovies = new ArrayList();
    List<String> searchResult = new ArrayList();
    Button btn0;
    Button btn1;
    Button btn2;
    Button btn3;
    EditText editText1;
    EditText editText2;
    TextView textView1;
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        int REQUEST_CODE_CONTACT = 101;
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                return;
            }
        }

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        textView1 = findViewById(R.id.textView1);
        textView1.setMovementMethod(ScrollingMovementMethod.getInstance());

        //Button search
        btn0 = findViewById(R.id.button0);
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str0 = editText1.getText().toString();
                searchResult = readDataset(str0);
                print2();
            }
        });

        //Button confirm
        btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str1 = editText1.getText().toString();
                //output.add(str1);
                nameList.add(str1);
                str2 = editText2.getText().toString();
                //output.add(str2);
                scoreList.add(str2);
                if(str1.length() == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("Please Input The Movie Name！");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                if(str2.length() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("Please Rate The Movie！");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                print1();
            }
        });

        //Button submit
        btn2 = findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                try {
                    URL url = new URL("http://54.84.209.141:5001/RecommendMovies");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(5000);

                    Movie movie = new Movie();
                    for (int k = 0; k < nameList.size(); k++) {
                        movie.getMoviesName().add(nameList.get(k));
                        movie.getMoviesRating().add(Integer.valueOf(scoreList.get(k)));
                    }

                    Gson gson = new Gson();
                    String json = gson.toJson(movie);
                    System.out.println(json);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes());
                    os.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    String output;
                    String result = "";
                    System.out.println("Output from Server .... \n");
                    while ((output = br.readLine()) != null) {
                        result += output;
                    }

                    if(conn.getInputStream() != null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                        builder.setTitle("Submit Successfully！");
                        builder.setIcon(R.drawable.correct);
                        builder.create().show();
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                        //builder.setTitle("Error！Submit Failure.");
                        builder.setTitle("No Recommendations！");
                        builder.setIcon(R.drawable.error);
                        builder.create().show();
                    }

                        //System.out.println(result);
                    RecommendMovies recommendMovies = gson.fromJson(result, RecommendMovies.class);
                    for(String string : recommendMovies.getMovie()){
                        Log.i("jieguo",string);
                        System.out.println(string);
                        resultMovies.add(string);
                    }

                    conn.disconnect();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Button display
        btn3 = findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Rating.this, Display.class);
                startActivity(i);
            }
        });
    }

    public void print1() {
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int k = 0; k <nameList.size(); k++) {
            stringBuilder1.append(nameList.get(k) + "\n");
            stringBuilder1.append(scoreList.get(k) + "\n");
        }
        textView1.setText(stringBuilder1.toString());
    }

    public void print2() {
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int k = 0; k <searchResult.size(); k++) {
            stringBuilder1.append(searchResult.get(k) + "\n");
        }
        textView1.setText(stringBuilder1.toString());
    }

    private List readDataset(String str0) {
        List<String> existDB = new ArrayList();
        try {
            InputStream nameDataBase = getResources().openRawResource(R.raw.nametable);
            String result = new BufferedReader(new InputStreamReader(nameDataBase)).lines().collect(Collectors.joining(System.lineSeparator()));
            String nameDB[] = result.split("\n");
            for (int k = 0; k < nameDB.length; k++) {
                if (nameDB[k].indexOf(str0) != -1) {
                    existDB.add(nameDB[k]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return existDB;
    }
}