/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.util.List;

/**
 *
 * @author lahvac
 */
public class CPPLiteDebuggerConfig {

    private final List<String> executable;

    public CPPLiteDebuggerConfig(List<String> executable) {
        this.executable = executable;
    }
    
    public String getDisplayName() {
        return "XXX: displayName";
    }

    public String getDebuggingName() {
        return "XXX: debugging name (debug)";
    }
    
    public List<String> getExecutable() {
        return executable;
    }
}
