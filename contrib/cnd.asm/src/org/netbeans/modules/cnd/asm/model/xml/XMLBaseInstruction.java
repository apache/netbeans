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

    
package org.netbeans.modules.cnd.asm.model.xml;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.lang.instruction.InstructionArgs;

public abstract class XMLBaseInstruction implements Instruction {
    
    private final String name;
    private final String groupName;
    private final String description;
    private final Collection<InstructionArgs> args;
    
    private Map<String, String> propMap;
    
    public XMLBaseInstruction(String name, String groupName, String description, 
                              Collection<InstructionArgs> args) {
        this.name = name;
        this.groupName = groupName;
        this.args = args;
        this.description = description;
    }    
    
    public String getName() {
        return name;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Collection<InstructionArgs> getArguments() {
        return args;
    }
    
    public String getProperty(String name) {
        if (propMap != null) {
            return propMap.get(name);
        }
        
        return null;
    }

    @Override
    public String toString() {
        return name + " " + description; // NOI18N
    }
    
    protected void setProperty(String name, String value) {
        try {            
            String t = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1); // NOI18N
            Method m = this.getClass().getMethod(t, String.class);                        
            m.invoke(this, value);            
        } catch (Exception ex) {
            setMap(name, value);
        }
    }        
    
    private void setMap(String name, String value) {
        // Note: it's lazy method used only in initialiation => 
        // not thread safe !!! 
        if (propMap == null) {
            propMap = new HashMap<String, String>();
        }
        
        propMap.put(name, value);
    }

}
