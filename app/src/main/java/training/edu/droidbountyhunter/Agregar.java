package training.edu.droidbountyhunter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import training.edu.data.DBProvider;
import training.edu.models.Fugitivo;

/**
 * @author Giovani González
 * Created by darkgeat on 09/08/2017.
 */

public class Agregar extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);
    }

    public void OnSaveClick(View view) {
        TextView name = (TextView)findViewById(R.id.editTextName);
        if (name.getText().toString().length() > 0){
            DBProvider database = new DBProvider(this);
            database.InsertFugitivo(new Fugitivo(0,name.getText().toString(),"0"));
            setResult(0);
            finish();
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("Alerta")
                    .setMessage("Favor de capturar el nombre del fugitivo.")
                    .show();
        }
    }
}
