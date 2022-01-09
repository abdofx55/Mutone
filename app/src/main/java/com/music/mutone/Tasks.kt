package com.music.mutone

import android.app.Activity

object Tasks {
    // Function to convert milliseconds time to Timer Format Hours:Minutes:Seconds
    @JvmStatic
    fun formatMilliSecond(milliseconds: Int?): String {
        var finalTimerString = ""
        val secondsString: String
        val minuteString: String

        if (milliseconds != null) {
            // Convert total duration into time
            val hours = milliseconds / (1000 * 60 * 60)
            val minutes = milliseconds % (1000 * 60 * 60) / (1000 * 60)
            val seconds = milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000

            // Prepending 0 to minutes if it is one digit
            minuteString = if (minutes < 10) "0$minutes" else minutes.toString()


            // Prepending 0 to seconds if it is one digit
            secondsString = if (seconds < 10) "0$seconds" else seconds.toString()


            // Add hours if there
            if (hours > 0) {
                finalTimerString = "$hours"

            } else finalTimerString = "$finalTimerString$minuteString:$secondsString"
            // return timer string
        }
        return finalTimerString
    }

    @JvmStatic
    fun moveTaskToBack(activity: Activity?) {
        activity?.onBackPressed()
    }
}