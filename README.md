# SmartUserSwitch
NOTE: This app was heavily made with GenAI (GitHub Copilot), because I sadly don't know Android development yet. I might rewrite the project with human code when I learn how to.
# How to build
```./gradlew assembleDebug```  
APK will be availble in app/build/outputs/apk/debug/app-debug.apk

# Configuration
First, make sure you have LSPosed and root. I recommend the [JingMatrix fork](https://github.com/JingMatrix/LSPosed/actions/workflows/core.yml?query=is%3Asuccess++).  
Next, install the APK. You can download the one from [the releases](https://github.com/mgytr/SmartUserSwitch/releases) or build it yourself.
Then, enable the module in LSPosed, and enable Superuser/su access for the app. (if you're on Magisk, it will ask you when pressing the save button)  
Open the app, edit the settings to your User IDs and Passcodes, and click the save button (you can check your user id's with `adb shell pm list users` with ADB or `su -c "pm list users"` in a Terminal like Termux)  
Pattern is not supported, and I'm not planning to add it, since it's an insecure screen lock, but contributions are welcome!)
