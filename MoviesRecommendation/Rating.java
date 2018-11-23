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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Rating extends MainActivity {
    public String str0;
    public String str1;
    public String str2;
    List<String> nameList = new ArrayList();
    //List<String> scoreList = new ArrayList();
    List<Integer> scoreList = new ArrayList();
    public List<String> resultMovies = new ArrayList();
    List<String> getSearch = new ArrayList<>();
    Button btn0;
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
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
               if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                try {
                    URL url = new URL("http://54.172.214.223:5001/GetSimilarMovies");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(10000);

                    SimilarMovies movie = new SimilarMovies(str0);

                    Gson gson = new Gson();
                    String json = gson.toJson(movie);
                    Log.i("Json1: ",json);

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
                        builder.setTitle("Submit Successfully! ");
                        builder.setIcon(R.drawable.correct);
                        builder.create().show();
                    }

                    Log.i("Output Result",result);
                    RecommendMovies recommendMovies = gson.fromJson(result, RecommendMovies.class);
                    for(String string : recommendMovies.getMovie()){
                        getSearch.add(string);
                    }

                    conn.disconnect();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

               print0();
               getSearch.clear();
            }
        });

        //Button confirm
        btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str1 = editText1.getText().toString();
                str2 = editText2.getText().toString();
                if(str1.length() == 0 && str2.length() != 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("Please Input The Movie Name! ");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                if(str2.length() == 0 && str1.length() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("Please Rate The Movie！");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                if(str1.length() == 0 && str2.length() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("Please Input and Rate A Movie! ");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                if(Integer.valueOf(str2) > 5 || Integer.valueOf(str2) < 0 ){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                    builder.setTitle("The Score Range From 0 to 5 ");
                    builder.setIcon(R.drawable.hint);
                    builder.create().show();
                }
                if(str1.length() != 0 && str2.length() != 0 ){
                    nameList.add(str1);
                    scoreList.add(Integer.valueOf(str2));
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
                    URL url = new URL("http://54.172.214.223:5001/RecommendMovies");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(10000);

                    Movie movie = new Movie();
                    for (int k = 0; k < nameList.size(); k++) {
                        movie.getMoviesName().add(nameList.get(k));
                        movie.getMoviesRating().add(scoreList.get(k));
                    }

                    Gson gson = new Gson();
                    String json = gson.toJson(movie);
                    Log.i("Json2",json);

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
                        builder.setTitle("Submit Successfully! ");
                        builder.setIcon(R.drawable.correct);
                        builder.create().show();
                    }

                    RecommendMovies recommendMovies = gson.fromJson(result, RecommendMovies.class);
                    for(String string : recommendMovies.getMovie()){
                        Log.i("Submit Result",string);
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
                i.putExtra("display",resultMovies.toArray(new String[resultMovies.size()]));
                Log.i("Result Size", String.valueOf(resultMovies.size()));
                for(String s : resultMovies.toArray(new String[resultMovies.size()])){
                    Log.i("Result Movies: ",s);
                }
                startActivity(i);
            }
        });

        //Button Reset
        btn4 = findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameList.clear();
                scoreList.clear();
                resultMovies.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(Rating.this);
                builder.setTitle("Reset Successfully！");
                builder.setIcon(R.drawable.correct);
                builder.create().show();
            }
        });
    }

    public void print0() {
        StringBuilder stringBuilder1 = new StringBuilder();
        if(getSearch.size() == 0){
            stringBuilder1.append("No Relevant Movies in the Database");
        }
        else {
            for (int k = 0; k < getSearch.size(); k++) {
                stringBuilder1.append(k + 1 + ": " + getSearch.get(k) + "\n");
            }
        }
        textView1.setText(stringBuilder1.toString());
    }

    public void print1() {
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int k = 0; k <nameList.size(); k++) {
            stringBuilder1.append(nameList.get(k) + "\n");
            stringBuilder1.append(scoreList.get(k) + "\n");
        }
        textView1.setText(stringBuilder1.toString());
    }
}