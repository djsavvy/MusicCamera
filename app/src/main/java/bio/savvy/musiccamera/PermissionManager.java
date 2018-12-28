package bio.savvy.musiccamera;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.TreeMap;

public class PermissionManager {

    // Activity and Context
    private Activity activity_;

    // Permissions
    private String[] allPermissions_;
    private TreeMap<String, Integer> permissionRequestConstants_;

    // Logging
    private String LOG_TAG;


    public PermissionManager(@NonNull final Activity activity, @NonNull String[] permissions, @NonNull String logTag) {
        this.activity_ = activity;
        this.allPermissions_ = permissions;
        permissionRequestConstants_ = new TreeMap<>();
        for(String p : allPermissions_) {
            permissionRequestConstants_.put(p, permissionRequestConstants_.size());
        }
        this.LOG_TAG = logTag;
    }

    public boolean haveAllPermissions() {
        for(String p : allPermissions_) {
            if(!havePermission(p)) return false;
        }
        return true;
    }

    public boolean havePermission(String permission) {
        return (ContextCompat.checkSelfPermission(activity_, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestPermissionFromSystem(String permission) {
        ActivityCompat.requestPermissions(activity_, new String[]{permission}, permissionRequestConstants_.get(permission));
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

    public void requestNextPermissionFromUser() {
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

}
