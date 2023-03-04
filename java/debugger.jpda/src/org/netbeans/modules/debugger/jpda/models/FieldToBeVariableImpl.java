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
package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * Represents a variable containing value that is going to be set to a field.
 * 
 * @author Martin
 */
public class FieldToBeVariableImpl extends AbstractObjectVariable {
    
    private final Field fieldVar;
    
    /** Creates a new instance of FieldToBeVariableImpl */
    public FieldToBeVariableImpl(
        JPDADebuggerImpl debugger,
        Value toBeValue,
        String parentID,
        Field fieldVar
    ) {
        super (
            debugger,
            toBeValue,
            parentID + "." + fieldVar.getName() + "=" + ReturnVariableImpl.getStringValue(toBeValue) // To have good equals()
        );
        this.fieldVar = fieldVar;
    }
    
    public Field getFieldVariable() {
        return fieldVar;
    }
    
}
