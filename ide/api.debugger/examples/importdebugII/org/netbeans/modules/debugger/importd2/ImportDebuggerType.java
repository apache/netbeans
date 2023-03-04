/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.importd2;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

import org.netbeans.modules.debugger.AbstractDebuggerType;


/**
* Default debugger type for Import debugger.
*/
public class ImportDebuggerType extends AbstractDebuggerType {

    static final long serialVersionUID = 5234304898551299437L;

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger_Type");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ImportDebuggerType.class);
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain) 
    throws DebuggerException {
        TopManager.getDefault ().getDebugger ().startDebugger (
            new ImportDebuggerInfo (
                info.getClassName (),
                info.getArguments (),
                stopOnMain ? info.getClassName () : null
            )
        );
        return;
    }
    
    /**
     * Should return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}.
     *
     * @param obj DataObject to test
     * @return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}
     */
    public boolean supportsDebuggingOf (DataObject obj) {
        return obj.getPrimaryFile ().getMIMEType ().equals ("text/x-java");
    }
    
    /**
     * Starts debugging for a dataobject. Debugging should stop on given line.
     * This method is called from RunToCursorAction.
     *
     * @param obj object to run
     * @param stopOnLine should the debugging stop on given line or go to
     * first breakpoint (if stopOnLine == <code>null</code>)
     * @exception DebuggerException if debugger is not installed or cannot
     * be started
     */
    public void startDebugger (DataObject obj, Line stopOnline) throws DebuggerException {
        startDebugger (obj, false);
    }
    
}
