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

package org.netbeans.modules.debugger.jpda.js.vars;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;

/**
 *
 * @author Martin Entlicher
 */
public final class ScopeVariable {
    
    private final JPDADebugger debugger;
    private final LocalVariable lv;
    private final String name;
    private JSVariable[] scopeVars;
    
    private ScopeVariable(JPDADebugger debugger, LocalVariable lv) {
        this.debugger = debugger;
        this.lv = lv;
        //((ObjectVariable) lv).getClassType().getName();
        String vv = DebuggerSupport.getVarValue(debugger, lv);
        if (vv.startsWith("[") && vv.endsWith("]")) {
            vv = vv.substring(1, vv.length() - 1).trim();
        }
        if (vv.toLowerCase().startsWith("object")) {
            vv = vv.substring("object".length()).trim();
        }
        vv = Character.toUpperCase(vv.charAt(0)) + vv.substring(1);
        name = vv;
    }
    
    public static ScopeVariable create(JPDADebugger debugger, LocalVariable lv) {
        return new ScopeVariable(debugger, lv);
    }
    
    public String getName() {
        return name;
    }
    
    public JSVariable[] getScopeVars() {
        if (scopeVars == null) {
            scopeVars = JSVariable.createScopeVars(debugger, lv);
        }
        return scopeVars;
    }
}
