package cloudsightapi.sampleapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {


    public static final int PERMISSION_STORAGE_TAG = 255;

    /*
     * Marshmallow permission system management
     */
    private static Dialog sAlertDialog;

    public static boolean canReadStorage(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkReadStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canReadStorage(activity)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showStoragePermissionDialog(activity);

            } else {
                requestStoragePermission(activity);

            }
            return false;
        }
        return true;
    }

    public static void showStoragePermissionDialog(final Activity activity) {
        if (activity.isFinishing() || (sAlertDialog != null && sAlertDialog.isShowing()))
            return;
        sAlertDialog = createDialog(activity);
    }

    private static Dialog createDialog(final Activity activity) {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(activity)
                .setTitle("Grant Permission")
                .setMessage("App needs your permission. Go to settings and grant permission")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Grant permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent i = new Intent();
                        i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            activity.startActivity(i);
                        } catch (Exception ex) {
                        }
                    }
                });
        return dialogBuilder.show();
    }


    private static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_STORAGE_TAG);
    }

}
