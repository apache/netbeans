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

package org.netbeans.modules.debugger.jpda.ui.focus;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.SuspendController;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types = SuspendController.class)
public class FocusSuspendController implements SuspendController {
    
    private final AWTGrabHandler awtGrabHandler;
    
    public FocusSuspendController(ContextProvider contextProvider) {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        awtGrabHandler = new AWTGrabHandler(debugger);
    }

    @Override
    public boolean suspend(ThreadReference tRef) {
        return awtGrabHandler.solveGrabbing(tRef);
    }

    @Override
    public boolean suspend(VirtualMachine vm) {
        return awtGrabHandler.solveGrabbing(vm);
    }
    
}
