package bio.savvy.musiccamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.Collections;

@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "CameraPreview";
    private CameraDevice cameraDevice_;

    public CameraPreview(Context context, @NonNull CameraDevice cameraDevice) {
        super(context);
        getHolder().addCallback(this);
        this.cameraDevice_ = cameraDevice;
    }

    private CameraCaptureSession.StateCallback createCaptureSessionStateCallback(final SurfaceHolder holder) {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    CaptureRequest.Builder requestBuilder =
                            cameraDevice_.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    requestBuilder.addTarget(holder.getSurface());
                    session.setRepeatingRequest(requestBuilder.build(),
                            new CameraCaptureSession.CaptureCallback() {
                            }, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                // TODO: fail gracefully
            }
        };
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        try {
            Log.d(LOG_TAG, "Surface Created");

            // TODO: set size of preview

            cameraDevice_.createCaptureSession(
                    Collections.singletonList(holder.getSurface()),
                    createCaptureSessionStateCallback(holder),
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(cameraDevice_ != null) {
            cameraDevice_.close();
        }
        cameraDevice_ = null;
    }
}
