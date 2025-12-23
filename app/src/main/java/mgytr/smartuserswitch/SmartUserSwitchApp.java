package mgytr.smartuserswitch;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import android.os.UserHandle;
import android.app.Application;
import android.content.Context;

import mgytr.smartuserswitch.ConfigReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

public class SmartUserSwitchApp implements IXposedHookLoadPackage {
    // Change as needed:
    private String FIRST_USER_PIN = "________";
    private String SECOND_USER_PIN = "________"; // passcode to trigger test
    private String THIRD_USER_PIN = "________"; // passcode to trigger test
    private String FOURTH_USER_PIN = "________"; // passcode to trigger test

    private int FIRST_USER = 0;
    private int SECOND_USER = 1000;
    private int THIRD_USER = 2000;
    private int FOURTH_USER = 3000;

    public Context context;

    public static String shellEscape(String s) {
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }


    private void changeUser(String pin, int user) {
        try {
            // Switch user (as you do now)
            String[] cmd = { "su", "-c", "am switch-user " + user };
            Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            Logger.log("dabeeanss: switch-user output: " + output.toString().trim());
        } catch (Throwable ex) {
            Logger.log("dabeeanss: switch-user FAILED: " + ex);
        }
        return; // always exit after handling!
    }

    
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"com.android.systemui".equals(lpparam.packageName)) return;
        Dictionary<String, String> config = ConfigReader.readWithSu();

        if (config.get("user1passcode") != null) {
            FIRST_USER_PIN = config.get("user1passcode");
        }
        if (config.get("user2passcode") != null) {
            SECOND_USER_PIN = config.get("user2passcode");
        }
        if (config.get("user3passcode") != null) {
            THIRD_USER_PIN = config.get("user3passcode");
        }
        if (config.get("user1id") != null) {
            try {
                FIRST_USER = Integer.parseInt(config.get("user1id"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (config.get("user2id") != null) {
            try {
                SECOND_USER = Integer.parseInt(config.get("user2id"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (config.get("user3id") != null) {
            try {
                THIRD_USER = Integer.parseInt(config.get("user3id"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        try {
            XposedHelpers.findAndHookMethod(
                "com.android.keyguard.KeyguardAbsKeyInputViewController",
                lpparam.classLoader,
                "verifyPasswordAndUnlock",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object controller = param.thisObject;
                        Object keyInputView = XposedHelpers.getObjectField(controller, "mView");
                        Object enteredCredential = XposedHelpers.callMethod(keyInputView, "getEnteredCredential");
                        int pinLen = (Integer) XposedHelpers.callMethod(enteredCredential, "size");

                        byte[] pinBytes = (byte[]) XposedHelpers.getObjectField(enteredCredential, "mCredential");
                        StringBuilder pinString = new StringBuilder();
                        if (pinBytes != null) {
                            for (byte b : pinBytes) pinString.append((char) b);
                        }
                        String submittedPin = pinString.toString();

                        //Logger.log("dabeeanss: PIN submitted: \"" + submittedPin + "\" (len=" + pinLen + ")");
                        Process process = Runtime.getRuntime().exec("am get-current-user");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = reader.readLine();
                        int userId = Integer.parseInt(line.trim()); 
                        if (FIRST_USER_PIN.equals(submittedPin) && userId != FIRST_USER) {
                            changeUser(FIRST_USER_PIN, FIRST_USER);
                            param.setResult(null); // skip unlock and lockout
                            return;
                        } else if (SECOND_USER_PIN.equals(submittedPin) && userId != SECOND_USER) { //second user login
                            changeUser(SECOND_USER_PIN, SECOND_USER); //mainthingies
                            param.setResult(null); // skip unlock and lockout
                            return;
                        } else if (THIRD_USER_PIN.equals(submittedPin) && userId != THIRD_USER) { //second user login
                            changeUser(SECOND_USER_PIN, SECOND_USER); //mainthingies
                            param.setResult(null); // skip unlock and lockout
                            return;
                        } else if (FOURTH_USER_PIN.equals(submittedPin) && userId != FOURTH_USER) { //second user login
                            changeUser(FOURTH_USER_PIN, FOURTH_USER); //mainthingies
                            param.setResult(null); // skip unlock and lockout
                            return;
                        }
                    }
                }
            );
        } catch (Throwable t) {
            Logger.log("dabeeanss: hook fail in verifyPasswordAndUnlock: " + t);
        }
    }
}
