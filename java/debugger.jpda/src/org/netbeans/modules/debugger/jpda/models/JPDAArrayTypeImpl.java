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

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.VariableType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAArrayTypeImpl extends JPDAClassTypeImpl implements JPDAArrayType {
    
    private final ArrayType arrayType;
    
    public JPDAArrayTypeImpl(JPDADebuggerImpl debugger, ArrayType arrayType) {
        super(debugger, arrayType);
        this.arrayType = arrayType;
    }

    @Override
    public String getComponentTypeName() {
        try {
            return ArrayTypeWrapper.componentTypeName(arrayType);
        } catch (InternalExceptionWrapper ex) {
            return "";
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    @Override
    public VariableType getComponentType() {
        try {
            Type componentType = ArrayTypeWrapper.componentType(arrayType);
            if (componentType instanceof ReferenceType) {
                return getDebugger().getClassType((ReferenceType) componentType);
            } else {
                return null;
            }
        } catch (ClassNotLoadedException ex) {
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
}
