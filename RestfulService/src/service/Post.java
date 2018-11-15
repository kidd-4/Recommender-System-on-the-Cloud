package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

import object.Movie;
import object.RecommendMovies;

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
	        //convert java object to JSON format
	        String json = gson.toJson(movie);
	        System.out.println(json);
//			String input = "{\"moviesName\":\"Harry Potter and the Chamber of Secrets\",\"moviesRating\":5}";

			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes());
			os.flush();

//			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
//				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
//			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			String result = "";
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
//				System.out.println(output);
				result += output;
			}
//			System.out.println(result);	
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
