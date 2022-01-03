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

package org.netbeans.modules.cnd.debugger.common2;


import org.openide.windows.InputOutput;


import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;

/**
 * Implements debug-related actions on a project.
 */

public class DbgAttachActionHandler extends DbgActionHandler {

    private volatile DebugTarget target;

    /*
     * Called when user cancels execution from progressbar in output window
     */
    // interface ProjectActionHandler
    @Override
    public void cancel() {
        // find dbugger using target and kill it
        for (NativeDebugger debugger: NativeDebuggerManager.get().nativeDebuggers()) {
            if (target == debugger.getNDI().getDebugTarget()) {
                debugger.shutDown();
                break;
            }
        }
    }

    // class DbgActionHandler
    @Override
    protected void doExecute(final String executable, final NativeDebuggerManager dm, final InputOutput io) {
	executionStarted();

        target = pae.getContext().lookup(DebugTarget.class);
        NativeDebuggerManagerAccessor.get().attach(target, this);
        
        // executionFinished is called when debugger really finish (NativeDebuggerImpl.preKill)
//	executionFinished(0);
    }
}
