package mgytr.smartuserswitch;

import de.robv.android.xposed.XposedBridge;

public class Logger {
    public static void log(String msg) {
        XposedBridge.log("dabeeanss " + msg);
    }
}
