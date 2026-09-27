package com.b4lol.assistant.tiles;

public class KillAppTile extends BaseTile {
    
    @Override
    protected String getScriptPath() {
        return "/data/adb/modules/QuiteKill/QuiteKill.sh";
    }
    
    @Override
    protected String getModuleUrl() {
        return "https://github.com/MeowDump/QuietKill/releases";
    }
    
    @Override
    protected String getModuleName() {
        return "QuiteKill";
    }
}
