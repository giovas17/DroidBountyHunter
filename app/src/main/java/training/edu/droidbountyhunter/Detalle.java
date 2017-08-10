package training.edu.droidbountyhunter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * @author Giovani González
 * Created by darkgeat on 09/08/2017.
 */

public class Detalle extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        // Se obtiene la información del intent
        String titulo = getIntent().getStringExtra("title");
        int mode = getIntent().getIntExtra("mode",0);
        // Se pone el nombre del fugitivo como titulo
        setTitle(titulo);
        TextView message = (TextView) findViewById(R.id.mensajeText);
        // Se identifica si es Fugitivo o Capturado para el mensaje...
        if (mode == 0){
            message.setText("El fugitivo sigue suelto...");
        }else {
            message.setText("Atrapado!!!");
        }
    }
}
