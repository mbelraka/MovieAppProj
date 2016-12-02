package com.example.mohamedbahgat.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.mohamedbahgat.movieapp.models.Movie;
import com.example.mohamedbahgat.movieapp.models.Review;
import com.example.mohamedbahgat.movieapp.models.Trailer;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MohamedBahgat on 2016-11-25.
 */

public class PortraitFragment extends Fragment {

    private MovieAdapter movieAdapter;
    private GridView gridView;

    public PortraitFragment(){

    }


    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.portrait_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.popularity_option){
            new FetchMoviesTask().execute(BuildConfig.PopularityParameter, "1");
            return true;
        }
        else if(id == R.id.rating_option){
            new FetchMoviesTask().execute(BuildConfig.RateParameter, "1");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    Movie movie = movieAdapter.getMovies().get(i);
                    Intent intent = new Intent(getActivity(), MovieActivity.class);
                    intent.putExtra(Intent.EXTRA_TITLE, movie.getTitle());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("movie", movie);
                    intent.putExtra("movie", bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    startActivity(intent);
                }catch (Exception e){
                    System.out.println("exception in listener" + e.getMessage());
                }
            }
        });

        movieAdapter = new MovieAdapter(this.getContext());

        FetchMoviesTask fetchTask = new FetchMoviesTask();
        fetchTask.execute(BuildConfig.PopularityParameter, "1");

        gridView.setAdapter(movieAdapter);

        return rootView;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void , List<Movie>> {

        @Override
        protected List<Movie> doInBackground(String... strings) {

            try{

                String link = generateURL(strings[0], Integer.parseInt(strings[1]));

                String data = getData(link);

                if(data == null){
                    return null;
                }

                List<Movie> dataItems = getDataFromJSON(data);

                return dataItems;

            } catch(Exception e){

                System.out.println("exception" + e.getMessage());

            }

            return null;
        }

        protected List<Movie> getDataFromJSON(String jsonString){

            try{

                JSONObject jsonObject = new JSONObject(jsonString);
                int pageNum = jsonObject.getInt("page");
                JSONArray itemsArray = jsonObject.getJSONArray("results");
                List<Movie> movies = new ArrayList<Movie>();

                for(int i = 0; i < itemsArray.length(); i++){

                    JSONObject movieJson = itemsArray.getJSONObject(i);
                    String id = movieJson.getString("id");
                    String title = movieJson.getString("original_title");
                    String poster_path = movieJson.getString("poster_path");
                    String overview = movieJson.getString("overview");
                    String releaseDate = movieJson.getString("release_date");
                    double popularity = movieJson.getDouble("popularity");
                    double rating = movieJson.getDouble("vote_average");

                    Movie movie = new Movie(id, title, poster_path, overview, releaseDate, popularity, rating);
                    movie.setTrailers(getTrailers(movie.getId()));
                    movie.setReviews(getReviews(movie.getId()));

                    movies.add(movie);
                }

                return movies;

            } catch (Exception e){

                System.out.println("exception" + e.getMessage());
            }


            return null;
        }

        protected List<Trailer> getTrailers(String movieId){

            try{

                String trailers_url = Uri.parse(BuildConfig.ParamsBaseURL + movieId + "/videos?").buildUpon()
                        .appendQueryParameter(BuildConfig.APIParameter, BuildConfig.APIKEY).build().toString();

                String trailers_data = getData(trailers_url);

                if(trailers_data == null){
                    return null;
                }

                JSONObject jsonObject = new JSONObject(trailers_data);
                JSONArray itemsArray = jsonObject.getJSONArray("results");
                List<Trailer> trailers = new ArrayList<Trailer>();

                for(int i = 0; i < itemsArray.length(); i++){

                    JSONObject trailerJson = itemsArray.getJSONObject(i);

                    String type = trailerJson.getString("type");

                    if(!type.equals("Trailer")){
                        continue;
                    }

                    String name = trailerJson.getString("name");
                    String key = trailerJson.getString("key");
                    key = Uri.parse(BuildConfig.trailerBaseURL).buildUpon().appendQueryParameter("v", key).build().toString();

                    Trailer trailer = new Trailer(name, key);

                    trailers.add(trailer);
                }

                return trailers;

            } catch (Exception e){

                System.out.println("exception" + e.getMessage());
            }


            return null;
        }

        protected List<Review> getReviews(String movieId){

            try{

                String reviews_url = Uri.parse(BuildConfig.ParamsBaseURL + movieId + "/reviews?").buildUpon()
                                .appendQueryParameter(BuildConfig.APIParameter, BuildConfig.APIKEY).build().toString();

                String reviews_data = getData(reviews_url);

                if(reviews_data == null){
                    return null;
                }

                JSONObject jsonObject = new JSONObject(reviews_data);
                JSONArray itemsArray = jsonObject.getJSONArray("results");
                List<Review> reviews = new ArrayList<Review>();

                for(int i = 0; i < itemsArray.length(); i++){

                    JSONObject reviewJson = itemsArray.getJSONObject(i);

                    String author = reviewJson.getString("author");
                    String content = reviewJson.getString("content");
                    String url = reviewJson.getString("url");
                    Review review = new Review(author, content, url);

                    reviews.add(review);
                }

                return reviews;

            } catch (Exception e){

                System.out.println("exception" + e.getMessage());
            }


            return null;
        }

        protected String getData(String link){

            try {

                HttpURLConnection connection = null;

                BufferedReader reader = null;

                URL url = new URL(link);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                if(inputStream == null){

                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer stringBuffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null){

                    stringBuffer.append(line + "\n");
                }

                if(stringBuffer.length() == 0){
                    return  null;
                }

                return stringBuffer.toString();

            } catch (Exception e){

            }

            return null;
        }

        protected String generateURL(String param, int pageNum){

            try{

                Uri builder = Uri.parse(BuildConfig.URL).buildUpon().
                        appendQueryParameter(BuildConfig.SortByParameter, param).
                        appendQueryParameter(BuildConfig.APIParameter, BuildConfig.APIKEY).build();

                return builder.toString();

            } catch (Exception e){

            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            movieAdapter.setMovies(movies);
            movieAdapter.notifyDataSetChanged();
        }
    }

}
