package com.example.test8;

import java.util.ArrayList;

public class Movie {
    private ArrayList<String> moviesName;
    private ArrayList<Integer> moviesRating;

    public Movie(){
        moviesName = new ArrayList<String>();
        moviesRating = new ArrayList<Integer>();
    }

    public ArrayList<String> getMoviesName() {
        return moviesName;
    }

    public void setMoviesName(ArrayList<String> moviesName) {
        this.moviesName = moviesName;
    }

    public ArrayList<Integer> getMoviesRating() {
        return moviesRating;
    }

    public void setMoviesRating(ArrayList<Integer> moviesRating) {
        this.moviesRating = moviesRating;
    }
}