package ir.simpletodo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class AppUtil {
    public static String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }
}
