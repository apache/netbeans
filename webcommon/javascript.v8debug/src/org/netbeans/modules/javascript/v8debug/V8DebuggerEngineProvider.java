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

package org.netbeans.modules.javascript.v8debug;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerEngineProvider.Registration(path=V8DebuggerSessionProvider.SESSION_NAME)
public class V8DebuggerEngineProvider extends DebuggerEngineProvider {
    
    public static final String LANGUAGE = "JavaScript";                         // NOI18N
    public static final String ENGINE_NAME = "javascript-v8engine";             // NOI18N
    
    private final V8Debugger dbg;
    
    public V8DebuggerEngineProvider(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
    }

    @Override
    public String[] getLanguages() {
        return new String[] { LANGUAGE };
    }

    @Override
    public String getEngineTypeID() {
        return ENGINE_NAME;
    }

    @Override
    public Object[] getServices() {
        return new Object[0];
    }

    @Override
    public void setDestructor(DebuggerEngine.Destructor destructor) {
        dbg.setEngineDestructor(destructor);
    }
    
}
