package br.redcode.pedrofsn.photogrid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pedrofsn on 29/07/2016.
 */
public abstract class ActivityGeneric extends AppCompatActivity {

    public int tempPosition = Constantes.VALOR_INVALIDO;
    public PhotoGrid photoGrid;
    private String mTakePicturePath;

    private static File getNewImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public Intent getTakePictureIntent() {
        mTakePicturePath = null;
        File pictureFile = null;

        try {
            pictureFile = getNewImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (pictureFile == null)
            return null;

        mTakePicturePath = pictureFile.getAbsolutePath();
        Utils.log("Path: " + mTakePicturePath);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
        return takePictureIntent;
    }

    public Intent getAttachPictureIntent() {
        Intent attachPictureIntent = new Intent(Intent.ACTION_PICK);
        attachPictureIntent.setType("image/*");
        return attachPictureIntent;
    }

    public void handleTakePictureResult() {
        Utils.log("File Path: " + mTakePicturePath);

        addThumbnail(mTakePicturePath);
        addNewImageToGallery(this, mTakePicturePath);
    }

    public void handleAttachPictureResult(Intent data) {
        Uri uri = data.getData();
        addThumbnail(new File(getRealPathFromURI(this, uri)));
    }

    private void addNewImageToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private String getRealPathFromURI(Context context, Uri contentURI) {
        String result = null;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void addThumbnail(Object obj) {
        Utils.log("addThumbnail na posição " + tempPosition + ": " + obj);

        if (!Utils.isNullOrEmpty(obj) && !Utils.isNullOrEmpty(photoGrid) && !Utils.isNullOrEmpty(photoGrid.getData()) && Constantes.VALOR_INVALIDO != tempPosition) {
            if (tempPosition <= photoGrid.getData().size() - 1) {
                photoGrid.getData().get(tempPosition).setPath(obj);
                notifyItemChanged(tempPosition);
                tempPosition = Constantes.VALOR_INVALIDO;
            }
        }
    }

    public abstract void notifyItemChanged(int position);
}
