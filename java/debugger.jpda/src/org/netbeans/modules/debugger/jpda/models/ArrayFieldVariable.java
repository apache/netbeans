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
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
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
class ArrayFieldVariable extends AbstractVariable implements
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private final ObjectVariable parent;
    private int index;
    private int maxIndexLog;
    private String declaredType;

    ArrayFieldVariable (
        JPDADebuggerImpl debugger,
        PrimitiveValue value,
        String declaredType,
        ObjectVariable array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + index +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.index = index;
        this.maxIndexLog = log10(maxIndex);
        this.declaredType = declaredType;
        this.parent = array;
        this.array = (ArrayReference) ((JDIVariable) array).getJDIValue();
    }

    
    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return getName(maxIndexLog, index);
    }
    
    static String getName(int maxIndexLog, int index) {
        int num0 = maxIndexLog - log10(index);
        if (num0 > 0) {
            return "[" + zeros(num0) + index + "]";
        } else {
            return "[" + index + "]";
        }
    }
    
    static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) l++;
        return l;
    }
    
    //private static final String ZEROS = "000000000000"; // NOI18N
    private static final String ZEROS = "            "; // NOI18N
    
    static String zeros(int n) {
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) z += " "; // NOI18N
            return z;
        }
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return getType ();
    }

    public JPDAClassType getDeclaringClass() {
        try {
            return getDebugger().getClassType((ReferenceType) ValueWrapper.type(array));
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

    public ObjectVariable getParentVariable() {
        return parent;
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return false;
    }
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return declaredType;
    }

    /**
     * Sets new value of this variable.
     * 
     * @param value ne value
     * @throws InvalidExpressionException if the value is invalid
     */ 
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            ArrayReferenceWrapper.setValue(array, index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InternalExceptionWrapper ex) {
            // Ignore
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Ignore
        } catch (ObjectCollectedExceptionWrapper ex) {
            // Ignore, it's gone.
        }
    }

    public ArrayFieldVariable clone() {
        ArrayFieldVariable clon = new ArrayFieldVariable(
                getDebugger(),
                (PrimitiveValue) getJDIValue(),
                declaredType,
                parent,
                index,
                0,
                getID().substring(0, getID().length() - ('.' + index + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()));
        clon.maxIndexLog = this.maxIndexLog;
        return clon;
    }
    
    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + getName ();
    }
}

