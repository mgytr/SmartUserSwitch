package mgytr.smartuserswitch;

import android.content.Context;
import android.util.Log;

import java.util.Dictionary;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConfigReader {

    public static Dictionary<String, String> readWithSu() {
      Dictionary<String, String> d = new Hashtable<>();
      final String TAG = "SmartUserSwitchConfigReader";
      try {
          Log.i(TAG, "Starting su process to read config.txt");
          Process process = Runtime.getRuntime().exec(
              new String[] { "su", "-c", "cat /data/adb/sus_config.txt" }
          );
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream())
          );
          String line;
          while ((line = reader.readLine()) != null) {
              String[] keyval = line.split("=", 2);
              if (keyval.length == 2) {
                  String key = keyval[0].trim();
                  String val = keyval[1].trim();
                  d.put(key, val);
              }
          }
          reader.close();
          int exitCode = process.waitFor();
          Log.i(TAG, "su process exited with code: " + exitCode);
          process.destroy();
      } catch (Exception e) {
          Log.e(TAG, "Exception in readWithSu", e);
      }
      return d;
    }
}
