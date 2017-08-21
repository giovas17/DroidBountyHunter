package training.edu.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Giovani González
 * Created by darkgeat on 8/20/17.
 */

public class PictureTools {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = PictureTools.class.getSimpleName();

    private static PictureTools instance;
    private Context context;

    public PictureTools() {
    }

    private synchronized static void createInstance(){
        if (instance == null){
            instance = new PictureTools();
        }
    }

    public static PictureTools getInstance(){
        if (instance == null){
            createInstance();
        }
        return instance;
    }

    public static PictureTools with(Context context){
        getInstance().setContext(context);
        return getInstance();
    }

    private void setContext(Context context) {
        this.context = context;
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return FileProvider.getUriForFile(getInstance().context, getInstance().context.getPackageName() + ".provider",
                    getOutputMediaFile(type));
        }else {
            return Uri.fromFile(getOutputMediaFile(type));
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getPath() + File.separator + "DroidBountyHunterPictures" + File.separator);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("DroidBountyHunter", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private static int getCameraPhotoOrientation(String imagePath){
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.v(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap decodeSampledBitmapFromUri(String dir, int Width, int Height)
    {
        Bitmap rotatedBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(dir, options);

            options.inSampleSize = calculateInSampleSize(options, Width, Height);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(dir, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(PictureTools.getCameraPhotoOrientation(dir));
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch(Exception e) {
            return null;
        }
        return rotatedBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int Width, int Height)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int size_inicialize = 1;

        if (height > Height || width > Width)
        {
            if (width > height)
            {
                size_inicialize = Math.round((float)height / (float)Height);
            }
            else
            {
                size_inicialize = Math.round((float)width / (float)Width);
            }
        }
        return size_inicialize;
    }
}
