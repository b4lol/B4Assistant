package com.b4lol.assistant.tiles;

public class PIFTile extends BaseTile {
    
    @Override
    protected String getScriptPath() {
        return "/data/adb/modules/playintegrityfix/action.sh";
    }
    
    @Override
    protected String getModuleUrl() {
        return "https://github.com/MeowDump/Integrity-Box/releases";
    }
    
    @Override
    protected String getModuleName() {
        return "Play Integrity Fix";
    }
}
