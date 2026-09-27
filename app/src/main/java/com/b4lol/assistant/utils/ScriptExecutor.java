package com.b4lol.assistant.utils;

import android.os.Handler;
import android.os.Looper;

public final class ScriptExecutor {
    public static final String SCRIPT_KILL_APP = "/data/adb/modules/QuiteKill/QuiteKill.sh";
    public static final String SCRIPT_PIF = "/data/adb/modules/playintegrityfix/action.sh";
    public static final String SCRIPT_KILL_GMS = "/data/adb/modules/playintegrityfix/webroot/common_scripts/gms.sh";
    public static final String SCRIPT_KEYBOX = "/data/adb/modules/playintegrityfix/webroot/common_scripts/key.sh";
    public static final String SCRIPT_REFRESH_TARGET = "/data/adb/modules/playintegrityfix/webroot/common_scripts/target.sh";
    public static final String SCRIPT_IMPORT_HMA = "/data/adb/modules/playintegrityfix/webroot/common_scripts/hma.sh";
    public static final String SCRIPT_HIDE_LINEAGE = "/data/adb/modules/playintegrityfix/webroot/common_scripts/override_lineage.sh";
    public static final String SCRIPT_OPEN_WEBUI = "/data/adb/modules/playintegrityfix/webroot/common_scripts/webui.sh";

    public static final String URL_QUITEKILL = "https://github.com/MeowDump/QuietKill/releases";
    public static final String URL_PIF = "https://github.com/MeowDump/Integrity-Box/releases";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ScriptExecutor() {}

    public interface ExecutionCallback {
        void onSuccess(String output);
        void onError(String error);
        void onModuleMissing(String moduleUrl);
    }

    public static void executeScript(String scriptPath, String moduleUrl, ExecutionCallback callback) {
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runScript(scriptPath);
            if (RootUtils.isModuleMissing(result)) {
                MAIN.post(() -> callback.onModuleMissing(moduleUrl));
            } else {
                boolean rootDenied = !result.isSuccess() && !RootUtils.hasRootAccess();
                MAIN.post(() -> {
                    if (result.isSuccess()) callback.onSuccess(result.getOutput());
                    else callback.onError(rootDenied ? "Please grant root access" : result.getError());
                });
            }
        });
    }

}
