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

package org.netbeans.modules.debugger.jpda.js.vars;

import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin
 */
public class JSVariable {
    
    private final JPDADebugger debugger;
    private final Variable valueInfoDesc;
    private final String key;
    private final String value;
    private final boolean expandable;
    private final ObjectVariable valueObject;
    
    protected JSVariable(JPDADebugger debugger, Variable valueInfoDesc) {
        this.debugger = debugger;
        this.valueInfoDesc = valueInfoDesc;
        value = getStringValue(debugger, valueInfoDesc);
        key = DebuggerSupport.getDescriptionKey(valueInfoDesc);
        expandable = DebuggerSupport.isDescriptionExpandable(valueInfoDesc);
        ObjectVariable valueObject = null;
        if (!expandable) {
            // Check if it's a script object:
            Variable valueObjectVar = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
            if (valueObjectVar instanceof ObjectVariable) {
                JPDAClassType classType = ((ObjectVariable) valueObjectVar).getClassType();
                if (classType != null) {
                    String className = classType.getName();
                    if (!className.startsWith("jdk.nashorn") && !className.startsWith("org.openjdk.nashorn") && !String.class.getName().equals(className)) {   // NOI18N
                        // Not a Nashorn's script class
                        valueObject = (ObjectVariable) valueObjectVar;
                    }
                }
            }
        }
        this.valueObject = valueObject;
    }
    
    private static String getStringValue(JPDADebugger debugger, Variable valueInfoDesc) {
        String value = DebuggerSupport.getDescriptionValue(valueInfoDesc);
        if ("{}".equals(value)) { // an object
            Variable valueObject = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
            value = DebuggerSupport.getVarValue(debugger, valueObject);
        }
        return value;
    }
    
    public static JSVariable[] createScopeVars(JPDADebugger debugger, Variable scope) {
        String value = scope.getValue();
        if ("null".equals(value)) {
            return new JSVariable[] {};
        }
        Variable[] valueInfos = DebuggerSupport.getValueInfos(debugger, scope, false);
        if (valueInfos == null) {
            return new JSVariable[] {};
        }
        int n = valueInfos.length;
        JSVariable[] jVars = new JSVariable[n];
        for (int i = 0; i < n; i++) {
            jVars[i] = new JSVariable(debugger, valueInfos[i]);
        }
        return jVars;
    }
    
    public static JSVariable create(JPDADebugger debugger, LocalVariable lv) {
        Variable valueInfoDesc = DebuggerSupport.getValueInfoDesc(debugger, lv.getName(), lv, false); // NOI18N
        if (valueInfoDesc == null) {
            return null;
        }
        return new JSVariable(debugger, valueInfoDesc);
    }
    
    public static JSVariable createIfScriptObject(JPDADebugger debugger, ObjectVariable ov, String name) {
        JPDAClassType classType = ov.getClassType();
        if (classType == null) {
            return null;
        }
        boolean isScript = classType.isInstanceOf("jdk.nashorn.internal.runtime.ScriptObject") ||  // NOI18N
                classType.isInstanceOf("org.openjdk.nashorn.internal.runtime.ScriptObject"); // NOI18N
        if (!isScript) {
            return null;
        }
        Variable valueInfoDesc = DebuggerSupport.getValueInfoDesc(debugger, name, ov, false);
        if (valueInfoDesc == null) {
            return null;
        }
        return new JSVariable(debugger, valueInfoDesc);
    }

    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * @return value object for non-JavaScript values.
     */
    public ObjectVariable getValueObject() {
        return valueObject;
    }
    
    public boolean isExpandable() {
        return expandable;
    }
    
    public JSVariable[] getChildren() {
        Variable descriptionValueObject = DebuggerSupport.getDescriptionValueObject(valueInfoDesc);
        return createScopeVars(debugger, descriptionValueObject);
    }
    
}
