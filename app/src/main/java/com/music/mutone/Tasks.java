package com.music.mutone;

import android.app.Activity;

public class Tasks {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String formatMilliSecond(int milliseconds) {
        String finalTimerString = "";
        String secondsString;
        String minuteString;

        // Convert total duration into time
        int hours = milliseconds / (1000 * 60 * 60);
        int minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000;

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
            if (minutes < 10) {
                minuteString = "0" + minutes;
                finalTimerString = finalTimerString + minuteString + ":" + secondsString;
            } else {
                minuteString = String.valueOf(minutes);
                finalTimerString = finalTimerString + minuteString + ":" + secondsString;
            }
        } else
            finalTimerString = finalTimerString + minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }


//    public static String formatTime(long dur) {
//        Date timeDuration = new Date(dur);
//        timeDuration.getTime();
//        SimpleDateFormat timeFormat = new SimpleDateFormat("m:ss", Locale.ENGLISH);
//        return timeFormat.format(timeDuration);
//    }

    public static void moveTaskToBack(Activity activity) {
        if (activity != null)
            activity.moveTaskToBack(true);
    }

}






//    private void showRequestPermissionDialog() {
//        AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(getContext());
//        reqAlertDialog.setTitle(R.string.storage_permission_needed);
//        reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);
//
//        reqAlertDialog.setPositiveButton(R.string.ok,
//                (dialog, which) -> ActivityCompat.requestPermissions(getActivity(),
//                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        StorageKeys.STORAGE_PERMISSION_CODE));
//        reqAlertDialog.setNegativeButton(R.string.cancel,
//                (dialog, which) -> dialog.dismiss());
//
//        reqAlertDialog.create().show();
//    }


