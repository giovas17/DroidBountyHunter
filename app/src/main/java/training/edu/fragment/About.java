package training.edu.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import training.edu.droidbountyhunter.R;

/**
 * @author Giovani González
 * Created by darkgeat on 10/08/2017.
 */

public class About extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Se hace referencia al Fragment generado por XML en los Layouts y
        // se instanc’a en una View...
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        // Se accede a los elementos ajustables del Fragment...
        RatingBar ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);
        String rating_string = "0.0"; // Variable para lectura del Rating guardado en el property
        try {
            if (System.getProperty("rating") != null){
                rating_string = System.getProperty("rating");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (rating_string.isEmpty()){
            rating_string = "0.0";
        }
        ratingBar.setRating(Float.valueOf(rating_string));
        // Listener al Raiting para la actualizacion de la property...
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                System.setProperty("rating",String.valueOf(rating));
                ratingBar.setRating(rating);
            }
        });
        return view;
    }
}
