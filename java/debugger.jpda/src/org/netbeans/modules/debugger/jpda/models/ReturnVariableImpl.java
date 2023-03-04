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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import org.netbeans.api.debugger.jpda.ReturnVariable;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 *
 * @author Martin Entlicher
 */
public class ReturnVariableImpl extends AbstractObjectVariable implements ReturnVariable {
    
    private String methodName;
    
    /** Creates a new instance of ReturnVariableImpl */
    public ReturnVariableImpl(
        JPDADebuggerImpl debugger,
        Value returnValue,
        String parentID,
        String methodName
    ) {
        super (
            debugger,
            returnValue,
            parentID + ".return " + methodName + "=" + getStringValue(returnValue) // To have good equals()
        );
        this.methodName = methodName;
    }
    
    static String getStringValue(Value v) {
        if (v == null) {
            return "null";
        }
        if (v instanceof VoidValue) {
            return "void";
        }
        if (v instanceof PrimitiveValue) {
            return v.toString();
        } else {
            try {
                return "#" + ObjectReferenceWrapper.uniqueID((ObjectReference) v);
            } catch (InternalExceptionWrapper ex) {
                return "#"+ex.getLocalizedMessage();
            } catch (VMDisconnectedExceptionWrapper ex) {
                return "#0";
            } catch (ObjectCollectedExceptionWrapper ex) {
                return "#0";
            }
       }
    }
    
    @Override
    public String methodName() {
        return methodName;
    }

    @Override
    public ReturnVariableImpl clone() {
        return new ReturnVariableImpl(
                getDebugger(),
                getJDIValue(),
                getID().substring(0, getID().length() - ".return".length()),
                methodName);
    }
    
    @Override
    public String toString () {
        return "ReturnVariable " + getValue();
    }

}
