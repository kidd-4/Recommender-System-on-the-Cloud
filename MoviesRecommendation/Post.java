package com.example.test8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

public class Post {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://172.30.43.238:5000/RecommendMovies");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            Movie movie = new Movie();
            movie.getMoviesName().add("Harry Potter and the Chamber of Secrets");
            movie.getMoviesRating().add(5);

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

            RecommendMovies recommendMovies = gson.fromJson(result, RecommendMovies.class);
            for(String string : recommendMovies.getMovie()){
                System.out.println(string);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
