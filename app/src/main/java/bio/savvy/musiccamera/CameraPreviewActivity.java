package bio.savvy.musiccamera;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

public class CameraPreviewActivity extends AppCompatActivity {

    // Permission constants
    private static final int PERMISSION_CAMERA = 0;

    // Logging
    private static final String LOG_TAG = "CameraPreviewActivity";

    // Camera management
    private CameraManager cameraManager_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting onCreate");
        super.onCreate(savedInstanceState);

        // Check permissions, and request if necessary
        requestPermissionsFromUser();

        // Initialize camera management
        cameraManager_ = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIDsList = {};
        try {
            cameraIDsList = cameraManager_.getCameraIdList();
        } catch (CameraAccessException e) {
            // Unclear from documentation why this would ever be thrown
            e.printStackTrace();
            this.finish();
        }
        if(cameraIDsList.length == 0) {
            Toast.makeText(this, "No cameras available. Cannot record video.", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        Log.i(LOG_TAG, "List of cameras: " + Arrays.toString(cameraIDsList));

        // TODO: Add support for choosing among multiple cameras








        setContentView(R.layout.activity_camera_preview);
    }

    private boolean havePermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionsFromUser() {
        if(havePermissions()) {
            Log.i(LOG_TAG, "Already have permissions. Don't need to request again.");
            return;
        }

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
                    // TODO: Check if the user clicked "do not show again" and show different prompt in that case
                }
        }
    }


}
