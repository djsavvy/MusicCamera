package bio.savvy.musiccamera;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;

import static android.support.constraint.ConstraintLayout.*;


public class CameraPreviewActivity extends AppCompatActivity {

    // Logging
    private static final String LOG_TAG = "CameraPreviewActivity";

    // Permissions
    private PermissionManager permissionManager_;

    // Camera management
    private CameraManager cameraManager_;
    private CameraDevice cameraDevice_;

    private final SurfaceHolder.Callback surfaceHolderCallback_ = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                        /* TODO: This is never getting called -- to fix this, create my own CameraPreview class that extends SurfaceView and implements SurfaceHolder.Callback
                        https://stackoverflow.com/questions/5912053/surfacecreated-is-not-called
                         */
                Log.e(LOG_TAG, "Surface Created");
//                        previewHolder_.setSizeFromLayout();
                cameraDevice_.createCaptureSession(Collections.singletonList(previewHolder_.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            CaptureRequest.Builder requestBuilder = cameraDevice_.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            requestBuilder.addTarget(previewHolder_.getSurface());
                            session.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                                @Override
                                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                }, null);
            }
            catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private final CameraDevice.StateCallback deviceStateCallback_ = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice_ = camera;
            Log.e(LOG_TAG, "cameraDevice_ set to " + cameraDevice_.toString());

            // Set up preview
//            previewSurfaceView_ = new SurfaceView(CameraPreviewActivity.this);
            previewSurfaceView_ = findViewById(R.id.previewSurfaceView);
            previewHolder_ = previewSurfaceView_.getHolder();
//            previewHolder_.addCallback(surfaceHolderCallback_);
//            previewSurfaceView_.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT));
//            ((ConstraintLayout) findViewById(R.id.previewLayout)).addView(previewSurfaceView_);
            surfaceHolderCallback_.surfaceCreated(previewHolder_);
            Log.e(LOG_TAG, "View added");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice_ = null;
            Log.i(LOG_TAG, "CameraDevice disconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            this.onDisconnected(camera);
        }
    };

    // Preview
    private SurfaceView previewSurfaceView_;
    private SurfaceHolder previewHolder_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        // Check permissions, and request if necessary
        // This will set up the preview for us too
        permissionManager_ = new PermissionManager(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                LOG_TAG);
        // If we add a permission, make sure to add it to manifest, as well as edit PermissionManager.requestNextPermissionFromUser()

        if(!permissionManager_.haveAllPermissions()) {
            permissionManager_.requestNextPermissionFromUser();
        }
        else {
            instantiatePreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionManager_.haveAllPermissions()) {
            // We still need more permissions
            permissionManager_.requestNextPermissionFromUser();
        } else {
            // Permissions granted -- set up preview
            instantiatePreview();
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

    private void instantiatePreview() {
        // TODO: Check if the preview has already been instantiated -- if so, return

        Log.i(LOG_TAG, "Instantiating preview");

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
            // TODO: clean this up
            cameraManager_.openCamera(cameraID, deviceStateCallback_, null);
            Log.e(LOG_TAG, "Camera manager requested to open camera " + cameraID);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            this.finish();
            return;
        }
    }

}
