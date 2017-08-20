package training.edu.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import training.edu.models.Fugitivo;

/**
 * @author Giovani González
 * Created by darkgeat on 8/16/17.
 */

public class DBProvider {

    private static final String TAG = DBProvider.class.getSimpleName();
    /** --------------------------------- Nombre de Base de Datos -------------------------------------**/
    private static final String DataBaseName = "DroidBountyHunterDataBase";
    /** --------------------------------- Version de Base de Datos ---------------------------------**/
    private static final int version = 1;
    /** --------------------------------- Tablas y Campos ---------------------------------**/
    private static final String TABLE_NAME = "fugitivos";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_STATUS = "status";
    /** --------------------------------- Declaración de Tablas ----------------------------------**/
    private static final String TFugitivos = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_NAME_NAME + " TEXT NOT NULL, " +
            COLUMN_NAME_STATUS + " INTEGER, " +
            "UNIQUE (" + COLUMN_NAME_NAME + ") ON CONFLICT REPLACE);";
    /** --------------------------------- Variables y Helpers ----------------------------------**/
    private DBHelper helper;
    private SQLiteDatabase database;
    private Context context;

    public DBProvider(Context c){
        context = c;
    }

    private DBProvider open(){
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
        return this;
    }

    private DBProvider open_read(){
        helper = new DBHelper(context);
        database = helper.getReadableDatabase();
        return this;
    }

    private void close(){
        helper.close();
        database.close();
    }

    private Cursor querySQL(String sql, String[] selectionArgs){
        Cursor regreso = null;
        open_read();
        regreso = database.rawQuery(sql, selectionArgs);
        return regreso;
    }

    public ArrayList<Fugitivo> GetFugitivos(boolean fueCapturado){
        ArrayList<Fugitivo> fugitivos = new ArrayList<>();
        String isCapturado = fueCapturado ? "1" : "0";
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_STATUS
                + "= ? ORDER BY " + COLUMN_NAME_NAME, new String[]{isCapturado});
        if (dataCursor != null && dataCursor.getCount() > 0){
            for (dataCursor.moveToFirst() ; !dataCursor.isAfterLast() ; dataCursor.moveToNext()){
                int id = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_ID));
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NAME));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                fugitivos.add(new Fugitivo(id,name,status));
            }
        }
        close();
        return fugitivos;
    }

    public void InsertFugitivo(Fugitivo fugitivo){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, fugitivo.getName());
        values.put(COLUMN_NAME_STATUS, fugitivo.getStatus());
        open();
        database.insert(TABLE_NAME,null,values);
        close();
    }

    public void UpdateFugitivo(Fugitivo fugitivo){
        open();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, fugitivo.getName());
        values.put(COLUMN_NAME_STATUS, fugitivo.getStatus());
        database.update(TABLE_NAME,values,COLUMN_NAME_NAME + "=?",new String[]{String.valueOf(fugitivo.getName())});
        close();
    }

    public void DeleteFugitivo(int idFugitivo){
        open();
        database.delete(TABLE_NAME, COLUMN_NAME_ID + "=?",new String[]{String.valueOf(idFugitivo)});
        close();
    }

    public int ContarFugitivos(){
        Cursor cursor = querySQL("SELECT " + COLUMN_NAME_ID + " FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_STATUS + "=?"
                ,new String[]{"0"});
        if (cursor != null){
            return cursor.getCount();
        }else {
            return 0;
        }
    }

    private static class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context) {
            super(context, DataBaseName, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TFugitivos);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Actualización de la BDD de la versión " + oldVersion + "a la " +
                    + newVersion + ", de la que se destruirá la información anterior");

            // Destruir BDD anterior y crearla nuevamente las tablas actualizadas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // Re-creando nuevamente la BDD actualizada
            onCreate(db);
        }
    }
}
