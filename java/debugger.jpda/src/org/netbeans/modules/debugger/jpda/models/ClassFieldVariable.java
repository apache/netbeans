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

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 * A field whose value is a class instance.
 * 
 * @author Martin Entlicher
 */
public class ClassFieldVariable extends ObjectFieldVariable implements ClassVariable {
    
    public ClassFieldVariable (
        JPDADebuggerImpl debugger,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        super (
            debugger,
            field,
            parentID,
            genericSignature,
            objectReference
        );
    }
    
    private ClassFieldVariable (
        JPDADebuggerImpl debugger, 
        ClassObjectReference value, 
        //String className,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        super(debugger, value, field, parentID, genericSignature, objectReference);
    }
    
    @Override
    public JPDAClassType getReflectedType() {
        Value innerValue = getInnerValue();
        if (!(innerValue instanceof ClassObjectReference)) {
            throw new IllegalStateException("Field "+field+" value "+innerValue+" is not a class.");
        }
        ClassObjectReference cor = (ClassObjectReference) innerValue;
        try {
            return getDebugger().getClassType(ClassObjectReferenceWrapper.reflectedType(cor));
        } catch (InternalExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        } catch (ObjectCollectedExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        } catch (VMDisconnectedExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        }
    }
    
    @Override
    public ClassFieldVariable clone() {
        String name;
        try {
            name = TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            name = ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            name = "0";
        }
        ClassFieldVariable clon = new ClassFieldVariable(getDebugger(), (ClassObjectReference) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + name + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                genericSignature, objectReference);
        clon.classType = classType;
        return clon;
    }

    
    // other methods ...........................................................

    @Override
    public String toString () {
        try {
            return "ClassFieldVariable " + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
    }
    
}
