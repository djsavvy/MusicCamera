package bio.savvy.musiccamera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.TreeMap;

public class CameraPreviewActivity extends AppCompatActivity {

    // TODO:  Split off permission checks into a different class

    // Logging
    private static final String LOG_TAG = "CameraPreviewActivity";

    // Camera management
    private CameraManager cameraManager_;
    private CameraDevice cameraDevice_;
    private final CameraDevice.StateCallback deviceStateCallback_ = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice_ = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice_ = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            this.onDisconnected(camera);
        }
    };
    private CameraCaptureSession cameraCaptureSession_;
    private CameraCaptureSession.StateCallback sessionStateCallback_ = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            cameraCaptureSession_ = session;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            cameraCaptureSession_ = null;
        }
    };
    private CameraCaptureSession.CaptureCallback sessionCaptureCallback_ = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    // Preview
    private SurfaceView previewSurface_;
    private SurfaceHolder previewHolder_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        // Check permissions, and request if necessary
        // This will set up the preview for us too
        if(!haveAllPermissions()) {
            requestNextPermissionFromUser();
        }
        else {
            instantiatePreview();
        }
    }


    private void instantiatePreview() {
        // Check if the preview has already been instantiated -- if so, return
        if(cameraDevice_ != null) {
            Log.i(LOG_TAG, "Preview already instantiated");
            return;
        }

        Log.i(LOG_TAG, "Instantiating preview");

        /*
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
        if (cameraIDsList.length == 0) {
            Toast.makeText(this, getString(R.string.no_cameras_available), Toast.LENGTH_SHORT).show();
            this.finish();
        }

        Log.i(LOG_TAG, "List of cameras: " + Arrays.toString(cameraIDsList));


        // Choose a camera
        // TODO: Add support for choosing among multiple cameras
        String cameraID = cameraIDsList[0];
        StreamConfigurationMap config = null;
        try {
            config = cameraManager_.getCameraCharacteristics(cameraID).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "CameraAccessException", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        // Open the camera
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestNextPermissionFromUser();
                Log.i(LOG_TAG, "Didn't have permissions from user");
            }
            cameraManager_.openCamera(cameraID, deviceStateCallback_, null);
            Log.e(LOG_TAG, "Camera manager opened camera " + cameraID);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            this.finish();
        }

        // Set up preview
        previewSurface_ = new SurfaceView(this);
        previewHolder_ = previewSurface_.getHolder();
//        Size[] potentialPreviewSizes = config.getOutputSizes(previewSurface_.getClass());
//        Log.e(LOG_TAG, "potentialPreviewSizes: " + Arrays.toString(potentialPreviewSizes));
//        Arrays.sort(potentialPreviewSizes);
        previewHolder_.setSizeFromLayout();
        ConstraintLayout previewLayout = findViewById(R.id.previewLayout);
        previewLayout.addView(previewSurface_);

        try {
            cameraDevice_.createCaptureSession(Arrays.asList(previewHolder_.getSurface()), sessionStateCallback_, null);
            CaptureRequest.Builder requestBuilder = cameraDevice_.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            cameraCaptureSession_.setRepeatingRequest(requestBuilder.build(), sessionCaptureCallback_, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        */
    }

    // Permission request constants
    private static final String[] allPermissions_ = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final TreeMap<String, Integer> permissionRequestConstants_;
    static {
        permissionRequestConstants_ = new TreeMap<>();
        for(String p : allPermissions_) {
            permissionRequestConstants_.put(p, permissionRequestConstants_.size());
        }
    }

    private boolean haveAllPermissions() {
        for(String p : allPermissions_) {
            if(!havePermission(p)) return false;
        }
        return true;
    }

    private boolean havePermission(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionFromSystem(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionRequestConstants_.get(permission));
        // Control is transferred to onRequestPermissionsResult() from here
    }

    private void requestPermissionFromUser(final String permission, @StringRes int dialogTitle, @StringRes int dialogText, @DrawableRes int iconID) {
        // Show request permission rationale
        Log.i(LOG_TAG, "Requesting permission " + permission + " from user.");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        alertBuilder.setTitle(dialogTitle)
                .setMessage(dialogText)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.i(LOG_TAG, "User dismissed dialog... requesting permission " + permission + " from system");
                        requestPermissionFromSystem(permission);
                    }
                })
                .setIcon(iconID)
                .show();
    }

    private void requestNextPermissionFromUser() {
        /* This pattern of checking for a permission then returning lets us ask for the permissions
        one by one by alternating control between this method and onRequestPermissionsResult().
         */

        if(!havePermission(Manifest.permission.CAMERA)) {
            requestPermissionFromUser(Manifest.permission.CAMERA, R.string.title_camera_permission_dialog,
                    R.string.text_camera_permission_dialog, android.R.drawable.ic_menu_camera);
            return;
        }

        if(!havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionFromUser(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.title_write_ext_storage_permission_dialog,
                    R.string.text_write_ext_permission_dialog, android.R.drawable.stat_notify_sdcard);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!haveAllPermissions()) {
            // We still need more permissions
            requestNextPermissionFromUser();
        } else {
            // Permissions granted -- set up preview
            instantiatePreview();
        }
    }

}
