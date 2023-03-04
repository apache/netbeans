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

package org.netbeans.modules.debugger.jpda.ant;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;


/**
 * Ant task, which is called after the debugged application is reloaded.
 * E.g. re-deployed to the server
 *
 * @author Martin Entlicher
 */
public class JPDAAppReloaded extends Task {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    @Override
    public void execute() throws BuildException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JPDAAppReloaded.execute()");
        }
        
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (debuggerEngine == null) {
            logger.fine("No debugging sessions was found.");
            return ;
        }
        JPDADebugger debugger = debuggerEngine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            logger.fine("Current debugger is not JPDA one.");
            return ;
        }
        try {
            Method fixBreakpointsMethod = debugger.getClass().getMethod("fixBreakpoints", new Class[] {});
            fixBreakpointsMethod.invoke(debugger, new Object[] {});
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Breakpoints fixed.");
        }
    }
    
}
