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
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 *
 * @author Martin Entlicher
 */
public class ClassVariableImpl extends AbstractObjectVariable implements ClassVariable {
    
    private ClassObjectReference clazz;
    
    /** Creates a new instance of ClassVariableImpl */
    public ClassVariableImpl(
        JPDADebuggerImpl debugger,
        ClassObjectReference clazz,
        String parentID
    ) {
        super (
            debugger,
            clazz,
            parentID + ".class"
        );
        this.clazz = clazz;
    }
    
    public JPDAClassType getClassType() {
        try {
            return getDebugger().getClassType(ObjectReferenceWrapper.referenceType(clazz));
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

    public JPDAClassType getReflectedType() {
        try {
            return getDebugger().getClassType(ClassObjectReferenceWrapper.reflectedType(clazz));
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
    public ClassVariableImpl clone() {
        ClassVariableImpl clon = new ClassVariableImpl(getDebugger(), clazz,
                getID().substring(0, getID().length() - ".class".length()));
        return clon;
    }
    
    public String toString () {
        return "ClassVariable " + getClassType();
    }

}
