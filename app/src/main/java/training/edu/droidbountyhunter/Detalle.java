package training.edu.droidbountyhunter;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import training.edu.data.DBProvider;
import training.edu.interfaces.OnTaskListener;
import training.edu.models.Fugitivo;
import training.edu.network.NetServices;
import training.edu.utilities.PictureTools;

import static android.location.LocationManager.GPS_PROVIDER;
import static training.edu.utilities.PictureTools.MEDIA_TYPE_IMAGE;

/**
 * @author Giovani González
 * Created by darkgeat on 09/08/2017.
 */

public class Detalle extends AppCompatActivity implements LocationListener {

    private String titulo;
    private int mode;
    private int id;
    private Uri pathImage;

    private LocationManager locationManager;
    private double latitude, longitud;
    private static final int REQUEST_CODE_PHOTO_IMAGE = 1787;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        // Se obtiene la información del intent
        titulo = getIntent().getStringExtra("title");
        mode = getIntent().getIntExtra("mode", 0);
        id = getIntent().getIntExtra("id", 0);
        // Se pone el nombre del fugitivo como titulo
        setTitle(titulo + " - [" + id + "]");
        TextView message = (TextView) findViewById(R.id.mensajeText);
        // Se identifica si es Fugitivo o Capturado para el mensaje...
        if (mode == 0) {
            message.setText("El fugitivo sigue suelto...");
            ActivarGPS();
        } else {
            Button add = (Button) findViewById(R.id.buttonAgregar);
            add.setVisibility(View.GONE);
            Button photo = (Button) findViewById(R.id.buttonPhoto);
            photo.setVisibility(View.GONE);
            latitude = getIntent().getDoubleExtra("latitude",0L);
            longitud = getIntent().getDoubleExtra("longitude",0L);
            message.setText("Atrapado!!!");
            ImageView photoImageView = (ImageView) findViewById(R.id.pictureFugitive);
            String pathPhoto = getIntent().getStringExtra("photo");
            if (pathPhoto != null && pathPhoto.length() > 0) {
                Bitmap bitmap = PictureTools.decodeSampledBitmapFromUri(pathPhoto, 200, 200);
                photoImageView.setImageBitmap(bitmap);
            }
        }
    }

    public void OnCaptureClick(View view) {
        DBProvider database = new DBProvider(this);
        String pathPhoto = PictureTools.currentPhotoPath;
        database.UpdateFugitivo(new Fugitivo(id, titulo, "1", pathPhoto.length() == 0 ? "" : pathPhoto,
                String.valueOf(latitude),String.valueOf(longitud)));
        NetServices apiCall = new NetServices(new OnTaskListener() {
            @Override
            public void OnTaskCompleted(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String message = object.optString("mensaje", "");
                    MessageClose(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnTaskError(int errorCode, String message, String error) {

            }
        });
        apiCall.execute("Atrapado", String.valueOf(id));
    }

    public void OnDeleteClick(View view) {
        DBProvider database = new DBProvider(this);
        database.DeleteFugitivo(id);
        setResult(mode);
        finish();
    }

    public void MessageClose(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.create();
        builder.setTitle("Alerta!!!");
        builder.setMessage(message);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setResult(mode);
                finish();
            }
        });
        builder.show();
    }

    public void OnFotoClick(View view) {
        if (PictureTools.permissionReadMemmory(this)) {
            dispatchPicture();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTO_IMAGE) {
            if (resultCode == RESULT_OK) {
                ImageView imageFugitive = (ImageView) findViewById(R.id.pictureFugitive);
                Bitmap bitmap = PictureTools.decodeSampledBitmapFromUri(PictureTools.currentPhotoPath, 200, 200);
                imageFugitive.setImageBitmap(bitmap);
            }
        }
    }

    private void dispatchPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pathImage = PictureTools.with(Detalle.this).getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pathImage);
        startActivityForResult(intent, REQUEST_CODE_PHOTO_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PictureTools.REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d("RequestPermissions", "ya fueron aceptadas");
                dispatchPicture();
            } else {
                Log.d("RequestPermissions", "NO fueron aceptadas");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        newLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void newLocation(Location newLocation) {
        latitude = newLocation.getLatitude();
        longitud = newLocation.getLongitude();
        Toast.makeText(this, "Ubicación: " + latitude + ", " + longitud, Toast.LENGTH_LONG).show();
    }

    private void ActivarGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (permissionFineLocation(this)) {
            try {
                locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);

                String provider = locationManager.getBestProvider(criteria,true);
                Location location = locationManager.getLastKnownLocation(provider);

                if (location != null){
                    newLocation(location);
                }
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }

    private void ApagarGPS(){
        if (locationManager != null){
            try {
                locationManager.removeUpdates(this);
                Log.d("LocationManager","Apagando GPS...");
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean permissionFineLocation(Activity context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PHOTO_IMAGE);
                    return false;
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PHOTO_IMAGE);
                    return false;
                }
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    public void OnMapClick(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitud);
        intent.putExtra("name",titulo);
        startActivity(intent);
    }
}
