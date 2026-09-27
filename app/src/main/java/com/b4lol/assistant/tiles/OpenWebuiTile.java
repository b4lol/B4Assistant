package com.b4lol.assistant.tiles;

public class OpenWebuiTile extends BaseTile {
    
    @Override
    protected String getScriptPath() {
        return "/data/adb/modules/playintegrityfix/webroot/common_scripts/webui.sh";
    }
    
    @Override
    protected String getModuleUrl() {
        return "https://github.com/MeowDump/Integrity-Box/releases";
    }
    
    @Override
    protected String getModuleName() {
        return "Open WebUI";
    }
}
