package bio.savvy.musiccamera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

class PermissionManager {

    // Activity and Context
    private final Activity activity_;

    // Permissions
    private final String[] allPermissions_;

    // Logging
    private final String LOG_TAG;


    public PermissionManager(@NonNull final Activity activity, @NonNull String[] permissions, @NonNull String logTag) {
        this.activity_ = activity;
        this.allPermissions_ = permissions;
        this.LOG_TAG = logTag;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean haveAllPermissions() {
        for(String p : allPermissions_) {
            if(!havePermission(p)) return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean havePermission(String permission) {
        return (ContextCompat.checkSelfPermission(activity_, permission) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionFromSystem(String permission) {
        // Note that we always use the same request code
        ActivityCompat.requestPermissions(activity_, new String[]{permission}, 0);
        // Control is transferred to onRequestPermissionsResult() from here
    }

    public void requestPermissionFromUser(final String permission, @StringRes int dialogTitle, @StringRes int dialogText, @DrawableRes int iconID) {
        // Show request permission rationale
        Log.i(LOG_TAG, "Requesting permission " + permission + " from user.");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity_, android.R.style.Theme_Material_Dialog_Alert);
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

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void requestNextPermissionFromUser() {
        /* This pattern of checking for a permission then returning lets us ask for the permissions
        one by one by alternating control between this method and onRequestPermissionsResult().
         */

        // TODO: replace the title and text with strings built from the permission string itself

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

}
