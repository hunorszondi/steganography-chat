package com.hunorszondi.letstego.utils

import java.lang.Math.abs
import java.util.*
import java.text.SimpleDateFormat

/**
 * Used for getting an elegant date format from a timestamp
 */
class DateUtil {
    companion object {
        fun getElegantDate(timestamp: Long): String {
            // setting timezone
            val locale = Locale("hu", "HU")
            val tz = TimeZone.getTimeZone("Europe/Hungary")

            // result
            val elegantDate: String

            // date from given timestamp
            val date = Calendar.getInstance(tz, locale)
            date.timeInMillis = timestamp

            // date of today
            val today = Calendar.getInstance(tz, locale)

            // choosing the most appropriate format
            if(date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                if(date.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                    if(abs(date.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR)) <= 7) {
                        if(date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                            // TODAY, time
                            elegantDate = "Today, ${date.getCustomTime()}"
                        } else if(date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)-1) {
                            // YESTERDAY, time
                            elegantDate = "Yesterday, ${date.getCustomTime()}"
                        } else {
                            // DAY OF WEEK, time
                            elegantDate = "${date.getDayOfWeek()}, ${date.getCustomTime()}"
                        }
                    } else {
                        // MONTH, DAY, time
                        elegantDate = date.getMonthName() +
                                " ${date.get(Calendar.DAY_OF_MONTH)}, " +
                                date.getCustomTime()
                    }
                } else {
                    // MONTH, DAY, time
                    elegantDate = date.getMonthName() +
                            " ${date.get(Calendar.DAY_OF_MONTH)}, " +
                            date.getCustomTime()
                }
            } else {
                elegantDate = date.getCustomDateAndTime()
            }

            return elegantDate
        }

        private fun Calendar.getCustomDateAndTime(): String {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
            return dateFormat.format(this.time)
        }

        private fun Calendar.getMonthName(): String {
            val monthName = arrayOf(
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
            )

            return monthName[this.get(Calendar.MONTH)]
        }

        private fun Calendar.getDayOfWeek(): String {
            val dayOfWeek = arrayOf(
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
            )

            return dayOfWeek[this.get(Calendar.DAY_OF_WEEK)-2]
        }

        private fun Calendar.getCustomTime(): String {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
            return dateFormat.format(this.time)
        }
    }
}
