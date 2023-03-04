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
package org.netbeans.modules.jshell.launch;

import com.sun.jdi.ObjectReference;
import java.util.List;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;

/**
 *
 * @author sdedic
 */
class ShellDebuggerUtils {
    
    static String getAgentKey(Session debuggerSession) {
        JPDADebugger debugger = debuggerSession.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return null;
        }
        List<JPDAClassType> classes = debugger.getClassesByName("org.netbeans.lib.jshell.agent.NbJShellAgent"); // NOI18N
        if (classes == null || classes.size() != 1) {
            return null;
        }
        JPDAClassType ct = classes.get(0);
        for (Field ff : ct.staticFields()) {
            if ("debuggerKey".equals(ff.getName())) {  // NOI18N
                String s = ff.getValue();
                if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') { // NOI18N
                    return ""; // NOI18N
                } 
                return s.substring(1, s.length() -1);
            }
        }
        return null;
    }
    
    static ObjectReference getWorkerHandle(Session debuggerSession, int remotePortAddress) {
        // Until issue #269235 is resolved, we cannot get a handle on the target agent.
        if (debuggerSession == null || true) {
            return null;
        }
        JPDADebugger debugger = debuggerSession.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return null;
        }
        List<JPDAClassType> classes = debugger.getClassesByName("org.netbeans.lib.jshell.agent.AgentWorker"); // NOI18N
        if (classes == null || classes.size() != 1) {
            return null;
        }
        JPDAClassType ct = classes.get(0);
        List<ObjectVariable> list = ct.getInstances(1);
        for (ObjectVariable inst : list) {
            Field f = inst.getField("socketPort");
            if (f == null) {
                continue;
            } 
            int check = Integer.parseInt(f.getValue());
            if (check == remotePortAddress) {
                // got the agent, return its ObjectVariable.
                return (ObjectReference)((AbstractVariable)inst).getInnerValue();
            }
        }
        return null;
    }
}
