package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.io.File;

public class WallpaperChangeActivity extends Activity {
    private static final String TAG = "LOCKIT";

    private String _imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_change);
    }

    @SuppressWarnings("deprecation")
    private String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    @SuppressWarnings("deprecation")
    private void setViewBackground(String path, View view) {
        File imgFile = new File(path);

        if(imgFile.exists()) {
            BitmapDrawable d = new BitmapDrawable(getResources(), imgFile.getAbsolutePath()); // BitmapDrawable.createFromPath(imgFile.getAbsolutePath());   //.createFromPath(imgFile.getAbsolutePath());
            d.setGravity(Gravity.CLIP_HORIZONTAL | Gravity.CENTER);
            d.setTileModeXY(Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            view.setMinimumWidth(d.getIntrinsicWidth());
            view.setBackgroundDrawable(d);
        }
    }

    public void setWallpaper(View v) {
        Intent intent = new Intent();
        intent.setType(AppConstants.IMAGE_SELECTION_LIST_ALL);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppConstants.SELECT_PICTURE_STRING), AppConstants.SELECT_PICTURE_ACTIVITY_CODE);
    }

    public void saveWallpaper(View v) {
        Intent i = this.getIntent();
        i.putExtra(AppConstants.WALLPAPER_URL, _imageUrl);
        this.setResult(RESULT_OK, i);
        finishActivity(AppConstants.SELECT_PICTURE_ACTIVITY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.SELECT_PICTURE_ACTIVITY_CODE:
                    try {
                        Uri selectedImageUri = data.getData();
                        String selectedImagePath = getPath(selectedImageUri);
                        setViewBackground(selectedImagePath, findViewById(R.id.wallpaperChangePreviewList));
                        _imageUrl = selectedImagePath.replace("/", "~");
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                    break;
            }
        }
    }
}
