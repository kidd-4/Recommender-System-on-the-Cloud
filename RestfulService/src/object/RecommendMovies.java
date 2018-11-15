package object;

import java.util.ArrayList;

public class RecommendMovies {
	private ArrayList<String> movie;
	
	public RecommendMovies(){
		movie = new ArrayList<>();
	}

	public ArrayList<String> getMovie() {
		return movie;
	}

	public void setMovie(ArrayList<String> movie) {
		this.movie = movie;
	}

	

	
}
