package com.b4lol.assistant.tiles;

public class ImportHmaTile extends BaseTile {
    
    @Override
    protected String getScriptPath() {
        return "/data/adb/modules/playintegrityfix/webroot/common_scripts/hma.sh";
    }
    
    @Override
    protected String getModuleUrl() {
        return "https://github.com/MeowDump/Integrity-Box/releases";
    }
    
    @Override
    protected String getModuleName() {
        return "Import HMA";
    }
}
