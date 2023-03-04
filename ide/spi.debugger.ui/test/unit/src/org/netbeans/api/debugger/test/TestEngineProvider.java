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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Represents a test debugger plug-in - one Debugger Implementation.
 *
 * @author Maros Sandor
 */
public class TestEngineProvider extends DebuggerEngineProvider {

    private DebuggerEngine.Destructor   destructor;
    private Session                     session;

    public TestEngineProvider (ContextProvider s) {
        session = s.lookupFirst(null, Session.class);
    }
    
    public String [] getLanguages () {
        return new String[] { "Basic" };
    }

    public String getEngineTypeID () {
        return TestDebugger.ENGINE_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.destructor = desctuctor;
    }
    
    public DebuggerEngine.Destructor getDestructor () {
        return destructor;
    }
    
    public Session getSession () {
        return session;
    }
}

