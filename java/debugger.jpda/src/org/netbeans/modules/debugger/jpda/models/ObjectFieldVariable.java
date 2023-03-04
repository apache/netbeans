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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.MutableVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.FieldWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalArgumentExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;


/**
 * @author   Jan Jancura
 */
public class ObjectFieldVariable extends AbstractObjectVariable
                                 implements MutableVariable,
                                            org.netbeans.api.debugger.jpda.Field,
                                            Refreshable {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N

    protected Field field;
    protected ObjectReference objectReference;    // ObjectReference to retrieve value of an instance field from.
    protected ReferenceType classType;            // ReferenceType to retrieve value of a static field from.
    protected String genericSignature;
    private boolean valueSet = true;
    private final Object valueLock = new Object();
    private boolean valueRetrieved = false;
    private ObjectReference value;
    
    private ObjectFieldVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        //String className,
        Field field,
        String parentID,
        ObjectReference objectReference
    ) {
        super (
            debugger,
            value,
            getID(parentID, field)
        );
        this.field = field;
        //this.className = className;
        this.objectReference = objectReference;
    }

    private static String getID(String parentID, Field field) {
        try {
            return parentID + '.' + TypeComponentWrapper.name(field) + "^";
        } catch (InternalExceptionWrapper ex) {
            return parentID + '.' + ex.getCause().getLocalizedMessage() + "^";
        } catch (VMDisconnectedExceptionWrapper ex) {
            return parentID + ".0^";
        }
    }

    protected ObjectFieldVariable (
        JPDADebuggerImpl debugger, 
        ObjectReference value, 
        //String className,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        this (
            debugger,
            value,
            field,
            parentID,
            objectReference
        );
        this.genericSignature = genericSignature;
    }

    public ObjectFieldVariable (
        JPDADebuggerImpl debugger,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        this (
            debugger,
            null,
            field,
            parentID,
            genericSignature,
            objectReference
        );
        this.valueSet = false;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        try {
            return TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        try {
            return ReferenceTypeWrapper.name(TypeComponentWrapper.declaringType(field)); //className;
        } catch (InternalExceptionWrapper ex) {
            return ex.getCause().getLocalizedMessage();
        } catch (ObjectCollectedExceptionWrapper ex) {
            return "";
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    public JPDAClassType getDeclaringClass() {
        return getDebugger().getClassType(getTheDeclaringClassType());
    }

    private ReferenceType getTheDeclaringClassType() {
        ReferenceType type = classType;
        if (type == null) {
            classType = type = FieldVariable.getTheDeclaringClassType(objectReference, field);
        }
        return type;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        try {
            return FieldWrapper.typeName(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    public JPDAClassType getClassType() {
        Value value = getInnerValue();
        if (value != null) {
            return super.getClassType();
        }
        try {
            com.sun.jdi.Type type;
            try {
                type = FieldWrapper.type(field);
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return null;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                return null;
            }
            if (type instanceof ReferenceType) {
                return getDebugger().getClassType((ReferenceType) type);
            } else {
                return null;
            }
        } catch (ClassNotLoadedException cnlex) {
            return null;
        }
    }
    
    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return TypeComponentWrapper.isStatic0(field);
    }

    @Override
    public boolean hasAllTypes() {
        if (valueSet || valueRetrieved) {
            return super.hasAllTypes();
        } else {
            return false;
        }
    }
    
    @Override
    public Value getInnerValue() {
        if (valueSet) {
            return super.getInnerValue();
        }
        synchronized (valueLock) {
            if (!valueRetrieved) {
                Value v;
                if (logger.isLoggable(Level.FINE)) {
                    if (objectReference == null) {
                        logger.fine("STARTED (OFV): "+getTheDeclaringClassType()+".getValue("+field+")");
                    } else {
                        logger.fine("STARTED (OFV): "+objectReference+".getValue("+field+")");
                    }
                }
                assert !Mutex.EVENT.isReadAccess() : "Debugger communication in AWT Event Queue!";
                try {
                    if (objectReference == null) {
                        v = ReferenceTypeWrapper.getValue (getTheDeclaringClassType(), field);
                    } else {
                        v = ObjectReferenceWrapper.getValue (objectReference, field);
                    }
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    v = null;
                } catch (InternalExceptionWrapper ocex) {
                    v = null;
                } catch (VMDisconnectedExceptionWrapper ocex) {
                    v = null;
                }
                if (logger.isLoggable(Level.FINE)) {
                    if (objectReference == null) {
                        logger.fine("FINISHED(OFV): "+getTheDeclaringClassType()+".getValue("+field+") = "+v);
                    } else {
                        logger.fine("FINISHED(OFV): "+objectReference+".getValue("+field+") = "+v);
                    }
                    logger.log(Level.FINE, "Called from ", new IllegalStateException("TEST"));
                }
                this.value = (ObjectReference) v;
                this.valueRetrieved = true;
            }
            return value;
        }
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            boolean set = false;
            if (objectReference != null) {
                ObjectReferenceWrapper.setValue(objectReference, field, value);
                set = true;
            } else {
                ReferenceType rt = getTheDeclaringClassType();
                if (rt instanceof ClassType) {
                    ClassType ct = (ClassType) rt;
                    ClassTypeWrapper.setValue(ct, field, value);
                    set = true;
                }
            }
            if (!set) {
                throw new InvalidExpressionException(field.toString());
            } else if (!valueSet) {
                synchronized (valueLock) {
                    this.value = (ObjectReference) value;
                }
            }
        } catch (IllegalArgumentExceptionWrapper ex) {
            throw new InvalidExpressionException (ex.getCause());
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InternalExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    /** Does wait for the value to be evaluated. */
    @Override
    public void refresh() throws RefreshFailedException {
        if (valueSet) return ;
        synchronized (valueLock) {
            if (!valueRetrieved) {
                getInnerValue();
            }
        }
    }

    /** Tells whether the variable is fully initialized and getValue()
     *  returns the value immediately. */
    @Override
    public synchronized boolean isCurrent() {
        return valueSet || valueRetrieved;
    }

    @Override
    public ObjectFieldVariable clone() {
        String name;
        try {
            name = TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            name = ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            name = "0";
        }
        ObjectFieldVariable clon = new ObjectFieldVariable(getDebugger(), (ObjectReference) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + name + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                genericSignature, objectReference);
        clon.classType = classType;
        return clon;
    }

    
    // other methods ...........................................................

    @Override
    public String toString () {
        try {
            return "ObjectFieldVariable " + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
    }
}
