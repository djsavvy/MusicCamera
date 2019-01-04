package bio.savvy.musiccamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import static android.view.View.*;

public class CameraPreviewActivity extends Activity {

    // Logging
    private static final String LOG_TAG = "CameraPreviewActivity";

    // Permissions
    private PermissionManager permissionManager_;

    // Camera management and preview
    private CameraManager cameraManager_;
    private CameraPreview preview_;

    private final CameraDevice.StateCallback deviceStateCallback_ = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // Check for camera device already opened
            destroyPreview();

            Log.d(LOG_TAG, "Camera device set to " + camera.toString());

            // Create preview and add it to activity
            preview_ = new CameraPreview(CameraPreviewActivity.this, camera);
            ConstraintLayout layout = findViewById(R.id.previewLayout);
            layout.addView(preview_);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(LOG_TAG, "Camera device " + camera.toString() + " disconnected");
            camera.close();

            destroyPreview();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            this.onDisconnected(camera);
            Log.d(LOG_TAG, "Camera device " + camera.toString() + " error");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        preview_ = null;

        /*
        Check permissions, and request if necessary
        This will set up the preview for us too
        */
        permissionManager_ = new PermissionManager(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}
        );
        /*
        If we add a permission, make sure to add it to manifest,
        as well as edit PermissionManager.requestNextPermissionFromUser()
         */

        // Pass control to onResume(), which will initialize the preview
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize preview
        if(!permissionManager_.haveAllPermissions()) {
            permissionManager_.requestNextPermissionFromUser();
        }
        else {
            initializePreview();
        }

        this.getWindow().getDecorView().setSystemUiVisibility(
                SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | SYSTEM_UI_FLAG_FULLSCREEN
                        | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionManager_.haveAllPermissions()) {
            // We still need more permissions
            permissionManager_.requestNextPermissionFromUser();
        } else {
            // Permissions granted -- set up preview
            initializePreview();
        }
    }

    private void initializeCameraManager() {
        if(cameraManager_ == null) {
            cameraManager_ = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            Log.i(LOG_TAG, "Initialized CameraManager");
        }
        else {
            Log.i(LOG_TAG, "CameraManager already exists.");
        }
    }

    private String[] getCameraIdList() {
        initializeCameraManager();

        try {
            String[] idList = cameraManager_.getCameraIdList();

            if (idList.length == 0) {
                Toast.makeText(this, getString(R.string.no_cameras_available), Toast.LENGTH_LONG).show();
                this.finishAndRemoveTask();
            }

            Log.i(LOG_TAG, "List of cameras: " + Arrays.toString(idList));
            return idList;
        } catch (CameraAccessException e) {
            // Unclear from documentation (and source code) why this would ever be thrown
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.no_cameras_available), Toast.LENGTH_LONG).show();
            this.finishAndRemoveTask();
            return new String[]{};
        }
    }

    @SuppressLint("MissingPermission")
    private void initializePreview() {
        if(preview_ != null) {
            Log.d(LOG_TAG, "CameraPreview already exists");
            return;
        }

        Log.d(LOG_TAG, "Initializing preview");

        // Initialize camera management
        initializeCameraManager();
        String[] cameraIDsList = getCameraIdList();
        // Exit if no cameras available
        if(cameraIDsList.length == 0) return;

        // Choose a camera
        // TODO: Add support for choosing among multiple cameras
        String cameraID = cameraIDsList[0];

        // Open the camera
        try {
            cameraManager_.openCamera(cameraID, deviceStateCallback_, null);
            Log.d(LOG_TAG, "Camera manager requested to open camera " + cameraID);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            this.finish();
            return;
        }
    }

    private void destroyPreview() {
        if(preview_ != null) {
            Log.d(LOG_TAG, "Destroying preview");
            ConstraintLayout layout = findViewById(R.id.previewLayout);
            layout.removeView(preview_);
            preview_ = null;
        }
    }
}
