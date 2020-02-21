/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.DebuggerToolRecognizer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 *
 */
@ServiceProvider(service = DebuggerToolRecognizer.class, position = 100)
public class DebuggerToolRecognizerDbxImpl implements DebuggerToolRecognizer{

    public boolean canHandle(String debuggerID) {
        return DbxEngineCapabilityProvider.ID.equals(debuggerID);
    }

    public boolean isTheSame(String debuggerID, Tool debuggerTool) {
        if (debuggerTool == null || PredefinedToolKind.DebuggerTool != debuggerTool.getKind()){
            return false;
        }
        return DbxEngineCapabilityProvider.isSupportedImpl((DebuggerDescriptor)debuggerTool.getDescriptor());
    }
    
}
