package mgytr.smartuserswitch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Application;
import android.content.Context;
import android.util.Log;


import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;


import mgytr.smartuserswitch.ConfigReader;

public class MainActivity extends Activity {
    private EditText user1id, user2id, user3id, user4id;
    private EditText user1passcode, user2passcode, user3passcode, user4passcode;
    private Button savebutton;
    public Context context;
    private static final String TAG = "SmartUserSwitchPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.main);
        // Optionally, add settings UI here later
        user1id = (EditText) findViewById(R.id.user1id);
        user2id = (EditText) findViewById(R.id.user2id);
        user3id = (EditText) findViewById(R.id.user3id);
        user4id = (EditText) findViewById(R.id.user4id);
        
        user1passcode = (EditText) findViewById(R.id.user1passcode);
        user2passcode = (EditText) findViewById(R.id.user2passcode);
        user3passcode = (EditText) findViewById(R.id.user3passcode);
        user4passcode = (EditText) findViewById(R.id.user4passcode);

        // Read the config after initializing the EditTexts
        Dictionary<String, String> config = ConfigReader.readWithSu();

        if (config != null) {
            if (config.get("user1id") != null)
                user1id.setText(config.get("user1id"));
            if (config.get("user2id") != null)
                user2id.setText(config.get("user2id"));
            if (config.get("user3id") != null)
                user3id.setText(config.get("user3id"));
            if (config.get("user4id") != null)
                user4id.setText(config.get("user4id"));
            if (config.get("user1passcode") != null)
                user1passcode.setText(config.get("user1passcode"));
            if (config.get("user2passcode") != null)
                user2passcode.setText(config.get("user2passcode"));
            if (config.get("user3passcode") != null)
                user3passcode.setText(config.get("user3passcode"));
            if (config.get("user4passcode") != null)
                user4passcode.setText(config.get("user4passcode"));

            }
        savebutton = (Button) findViewById(R.id.savebutton);
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String configString = String.format(
                        "user1id=%s\nuser2id=%s\nuser3id=%s\nuser4id=%s\nuser1passcode=%s\nuser2passcode=%s\nuser3passcode=%s\nuser3passcode=%s",
                        user1id.getText(), user2id.getText(), user3id.getText(), user4id.getText(),
                        user1passcode.getText(), user2passcode.getText(), user3passcode.getText(), user4passcode.getText()
                );
                try {
                    // Write configString to a temp file in app's private dir
                    File tempFile = new File(context.getFilesDir(), "sus_config_tmp.txt");
                    FileWriter writer = new FileWriter(tempFile);
                    writer.write(configString);
                    writer.close();

                    // Use su to copy the file to SystemUI's data dir
                    String[] cmd = {"su", "-c",
                            "cp '" + tempFile.getAbsolutePath() + "' /data/adb/sus_config.txt"
                    };
                    Process proc = Runtime.getRuntime().exec(cmd);
                    int exitCode = proc.waitFor();
                    // Read error output from su process
                    StringBuilder errorOutput = new StringBuilder();
                    try (java.io.BufferedReader errReader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getErrorStream()))) {
                        String errLine;
                        while ((errLine = errReader.readLine()) != null) {
                            errorOutput.append(errLine).append("\n");
                        }
                    }
                    if (exitCode == 0) {
                        Toast.makeText(context, "Config saved, please restart SystemUI", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "su error output: " + errorOutput.toString());
                        Toast.makeText(context, "Error saving config, no superuser permission?", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving config", e);
                    Toast.makeText(context, "Error saving config, no superuser permission?" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
