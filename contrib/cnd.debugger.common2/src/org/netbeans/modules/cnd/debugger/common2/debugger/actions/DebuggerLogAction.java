/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.utils.DebuggerLoggerSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.modules.Places;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@ActionID(category="Debug", id="org.netbeans.modules.cnd.debugger.common2.debugger.actions.DebuggerLogAction")
@ActionRegistration(displayName = "#MSG_DebuggerLogTab_name", iconInMenu = false, 
        iconBase = "org/netbeans/modules/cnd/debugger/common2/icons/debugger_log.png")//NOI18N
@ActionReference(path = "Menu/Window/Debug", position = 2500)
@Messages("MSG_DebuggerLogTab_name=Debugger &Log")
public class DebuggerLogAction extends AbstractAction {

    @Messages("MSG_ShortLogTab_name=Debugger Log")
    @Override public void actionPerformed(ActionEvent evt) {
        if ( NativeDebuggerManager.get().currentDebugger() == null) {
            Logger.getLogger(DebuggerLogAction.class.getName()).log(Level.INFO, "Showing Debugger log action failed as debug session ");//NOI18N
            return;
        }
        String logFile = NativeDebuggerManager.get().currentDebugger().getLogger();
        if (logFile == null) {
            //no log provided
            Logger.getLogger(DebuggerLogAction.class.getName()).log(Level.INFO, "Showing Debugger log action failed as  no log provided");//NOI18N
            return;
        }
        
        final ExecutionEnvironment executionEnvironment = NativeDebuggerManager.get().currentDebugger().getHost().executionEnvironment();
        Logger.getLogger(DebuggerLogAction.class.getName()).log(Level.INFO, "open debugger log file for {1} at {0}", new Object[]{executionEnvironment, logFile});//NOI18N
        FileObject fileObject = FileSystemProvider.getFileObject(executionEnvironment, logFile);
        DebuggerLoggerSupport p = new DebuggerLoggerSupport(fileObject, NbBundle.getMessage(DebuggerLogAction.class, "MSG_ShortLogTab_name"));//NOI18N
        try {
            p.showLogViewer();
        } catch (IOException e) {
            Logger.getLogger(DebuggerLogAction.class.getName()).log(Level.INFO, "Showing Debugger log action failed", e);//NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        return NativeDebuggerManager.get().currentDebugger() != null &&  NativeDebuggerManager.get().currentDebugger().getLogger() != null;
    }
    
    

}
