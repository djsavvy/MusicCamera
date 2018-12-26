package bio.savvy.musiccamera;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class CameraPreviewActivity extends AppCompatActivity {

    // Permission constants
    private static final int PERMISSION_CAMERA = 0;

    private static final String LOG_TAG = "CameraPreviewActivity";
    private CameraManager cameraManager_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting onCreate");
        super.onCreate(savedInstanceState);

        // Check permissions, and request if necessary
        if(!havePermissions()) requestPermissionsFromUser();

        // Initialize cameraManager_
        cameraManager_ = (CameraManager) getSystemService(Context.CAMERA_SERVICE);



        setContentView(R.layout.activity_camera_preview);
    }

    private boolean havePermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionsFromUser() {
        // Show request permission rationale
        Log.i(LOG_TAG, "Requesting permissions from user...");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        alertBuilder.setTitle(R.string.title_camera_permission_dialog)
                .setMessage(R.string.text_camera_permission_required)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.i(LOG_TAG,"User dismissed dialog... requesting camera permissions from system");
                        requestCameraPermissionFromSystem();
                    }
                })
                .setIcon(android.R.drawable.ic_menu_camera)
                .show();
    }

    private void requestCameraPermissionFromSystem() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_CAMERA:
                // If not granted, show dialog saying the app is unusable without permissions and try again
                if(!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestPermissionsFromUser();
                }
        }
    }


}
