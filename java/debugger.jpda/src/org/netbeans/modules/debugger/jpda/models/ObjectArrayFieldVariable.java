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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.openide.util.Exceptions;


/**
 * @author   Jan Jancura
 */
class ObjectArrayFieldVariable extends AbstractObjectVariable implements
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private final ObjectVariable parent;
    private int index;
    private int maxIndexLog;
    private String declaredType;

    ObjectArrayFieldVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String declaredType,
        ObjectVariable array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + index + "^"
        );
        this.index = index;
        this.maxIndexLog = ArrayFieldVariable.log10(maxIndex);
        this.declaredType = declaredType;
        this.parent = array;
        this.array = (ArrayReference) ((JDIVariable) array).getJDIValue();
    }

    public String getName () {
        return ArrayFieldVariable.getName(maxIndexLog, index);
    }
    
    public String getClassName () {
        return getType ();
    }
    
    public JPDAClassType getDeclaringClass() {
        try {
            return getDebugger().getClassType((ReferenceType) ValueWrapper.type(array));
        } catch (InternalExceptionWrapper ex) {
            throw ex.getCause();
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw ex.getCause();
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw ex.getCause();
        }
    }

    public ObjectVariable getParentVariable() {
        return parent;
    }

    public boolean isStatic () {
        return false;
    }
    
    public String getDeclaredType () {
        return declaredType;
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            ArrayReferenceWrapper.setValue(array, index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InternalExceptionWrapper ex) {
            throw new InvalidExpressionException (ex.getCause());
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Ignore
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex.getCause());
        }
    }

    public ObjectArrayFieldVariable clone() {
        ObjectArrayFieldVariable clon = new ObjectArrayFieldVariable(
                getDebugger(),
                (ObjectReference) getJDIValue(),
                getDeclaredType(),
                parent,
                index,
                0,
                getID());
        clon.maxIndexLog = this.maxIndexLog;
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        return "ObjectArrayFieldVariable " + getName ();
    }
}
