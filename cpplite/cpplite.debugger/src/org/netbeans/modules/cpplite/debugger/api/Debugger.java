/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cpplite.debugger.api;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebuggerConfig;

/**
 *
 * @author lahvac
 */
public class Debugger {
    
    public static Process startInDebugger(List<String> command) throws IOException {
        return CPPLiteDebugger.startDebugging(new CPPLiteDebuggerConfig(command)).second();
    }
}
