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

package org.netbeans.modules.web.javascript.debugger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerEngine.Destructor;
import static org.netbeans.modules.web.javascript.debugger.DebuggerConstants.*;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

@DebuggerEngineProvider.Registration(path="javascript-session")
public class DebuggerEngineProviderImpl extends DebuggerEngineProvider {
    
    private EngineDestructorProvider edp;
    
    public DebuggerEngineProviderImpl(ContextProvider contextProvider) {
        edp = contextProvider.lookupFirst(null, EngineDestructorProvider.class);
    }

    @Override
    public String[] getLanguages() {
        return new String[] {JAVASCRIPT};
    }

    @Override
    public String getEngineTypeID() {
        return ENGINE;
    }

    @Override
    public Object[] getServices() {
        return new Object[0];
    }

    @Override
    public void setDestructor(Destructor destructor) {
        edp.setDestructor(destructor);
    }
    
}
