/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.PrimitiveValue;
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
import org.openide.util.Mutex;


/**
 * @author   Jan Jancura
 */
public class FieldVariable extends AbstractVariable implements MutableVariable,
org.netbeans.api.debugger.jpda.Field, Refreshable {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N

    protected Field field;
    private ObjectReference objectReference;    // ObjectReference to retrieve value of an instance field from.
    private ReferenceType classType;            // ReferenceType to retrieve value of a static field from.
    private boolean valueSet = true;
    private final Object valueLock = new Object();
    private boolean valueRetrieved = false;
    private PrimitiveValue value;
    

    public FieldVariable (
        JPDADebuggerImpl debugger,
        PrimitiveValue value,
    //    String className,
        Field field,
        String parentID,
        ObjectReference objectReference    // instance or null for static fields
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

    public FieldVariable (
        JPDADebuggerImpl debugger,
        Field field,
        String parentID,
        ObjectReference objectReference
    ) {
        this (
            debugger,
            null,
            field,
            parentID,
            objectReference
        );
        this.valueSet = false;
    }

    private static String getID(String parentID, Field field) {
        try {
            return parentID + '.' + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return parentID + '.' + ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return parentID + ".0";
        }
    }

    // LocalVariable impl.......................................................
    

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
            classType = type = getTheDeclaringClassType(objectReference, field);
        }
        return type;
    }

    static ReferenceType getTheDeclaringClassType(ObjectReference objectReference, Field field) {
        ReferenceType type;
        try {
            type = TypeComponentWrapper.declaringType(field);
        } catch (InternalExceptionWrapper ex) {
            throw ex.getCause();
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw ex.getCause();
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

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return TypeComponentWrapper.isStatic0(field);
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
                        logger.fine("STARTED (FV): "+getTheDeclaringClassType()+".getValue("+field+")");
                    } else {
                        logger.fine("STARTED (FV): "+objectReference+".getValue("+field+")");
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
                        logger.fine("FINISHED(FV): "+getTheDeclaringClassType()+".getValue("+field+") = "+v);
                    } else {
                        logger.fine("FINISHED(FV): "+objectReference+".getValue("+field+") = "+v);
                    }
                    logger.log(Level.FINE, "Called from ", new IllegalStateException("TEST"));
                }
                this.value = (PrimitiveValue) v;
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
                    this.value = (PrimitiveValue) value;
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

    public FieldVariable clone() {
        String name;
        try {
            name = TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            name = ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            name = "0";
        }
        FieldVariable clon;
        clon = new FieldVariable(getDebugger(), (PrimitiveValue) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + name + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                objectReference);
        clon.classType = classType;
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        try {
            return "FieldVariable " + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
    }
}

