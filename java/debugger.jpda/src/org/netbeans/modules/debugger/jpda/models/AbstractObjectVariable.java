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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
public class AbstractObjectVariable extends AbstractVariable implements ObjectVariable {
    // Customized for add/removePropertyChangeListener
    // Cloneable for fixed watches
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N
    
    public static final int MAX_STRING_LENGTH = 100000; // Limit retrieved String length to 100K characters to limit memory consumption.

    private String          genericType;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;
    private boolean         refreshFields;
    private final Object    fieldsLock = new Object();

    private com.sun.jdi.Type valueType;
    private String           valueTypeName;
    private final boolean[]  valueTypeLoaded = new boolean[] { false };
    private boolean          valueTypeLoading = false;
    private Super            superClass;
    private final boolean[]  superClassLoaded = new boolean[] { false };
    private boolean          superClassLoading = false;
    
    private DebuggetStateListener stateChangeListener = new DebuggetStateListener();

    
    public AbstractObjectVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String id
    ) {
        this(debugger, value, (com.sun.jdi.Type) null, id);
    }

    protected AbstractObjectVariable (
        JPDADebuggerImpl debugger,
        Value value,
        com.sun.jdi.Type valueType,
        String id
    ) {
        super(debugger, value, id);
        /*if (value instanceof ObjectReference) {
            try {
                // Disable collection of the value so that we do not loose it in the mean time.
                // Enable collection is called as soon as we do not need it.
                System.err.println("DISABLING collection for "+value);
                ObjectReferenceWrapper.disableCollection((ObjectReference) value);
            } catch (Exception ex) {}
        }*/
        if (valueType != null) {
            this.valueType = valueType;
            try {
                this.valueTypeName = TypeWrapper.name(valueType);
            } catch (InternalExceptionWrapper ex) {
                Exceptions.printStackTrace(ex);
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }
            this.valueTypeLoaded[0] = true;
        }
        debugger.addPropertyChangeListener(
                WeakListeners.propertyChange(stateChangeListener, debugger));
    }

    public AbstractObjectVariable (JPDADebuggerImpl debugger, Value value, String genericSignature,
                      String id) {
        this(debugger, value, id);
        try {
            if (genericSignature != null) {
                this.genericType = getTypeDescription(new PushbackReader(new StringReader(genericSignature), 1));
            }
        } catch (IOException e) {
            /// invalid signature
        }
    }

    
    // public interface ........................................................
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    @Override
    public int getFieldsCount () {
        Value v = getInnerValue ();
        if (v == null) {
            return 0;
        }
        if (v instanceof ArrayReference) {
            return ArrayReferenceWrapper.length0((ArrayReference) v);
        } else {
            synchronized (fieldsLock) {
                if (fields == null || refreshFields) {
                    initFields ();
                }
                return fields.length;
            }
        }
    }

    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    @Override
    public Field getField (String name) {
        Value v = getInnerValue();
        if (v == null) {
            return null;
        }
        com.sun.jdi.Field f;
        try {
            f = ReferenceTypeWrapper.fieldByName((ReferenceType) ValueWrapper.type(v), name);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        } catch (InternalExceptionWrapper iex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
        if (f == null) {
            return null;
        }
        return this.getField (
            f, 
            (ObjectReference) getInnerValue (),
            getID()
        );
    }
    
    /**
     * Returns all fields declared in this type that are in interval
     * &lt;<code>from</code>, <code>to</code>).
     */
    @Override
    public Field[] getFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null) {
            return new Field[] {};
        }
        try {
            if (v instanceof ArrayReference && (from > 0 || to < ArrayReferenceWrapper.length((ArrayReference) v))) {
                // compute only requested elements
                Type type = ValueWrapper.type(v);
                ReferenceType rt = (ReferenceType) type;
                if (to == 0) {
                    to = ArrayReferenceWrapper.length((ArrayReference) v);
                }
                Field[] elements = getFieldsOfArray (
                        (ArrayReference) v, 
                        ArrayTypeWrapper.componentTypeName((ArrayType) rt),
                        this.getID (),
                        from, to);
                return elements;
            } else {
                //either the fields are cached or we have to init them
                synchronized (fieldsLock) {
                    if (fields == null || refreshFields) {
                        initFields ();
                    }
                    return getSubFields(fields, from, to);
                }
            }
        } catch (InternalExceptionWrapper e) {
            return new Field[] {};
        } catch (ObjectCollectedExceptionWrapper e) {
            return new Field[] {};
        } catch (VMDisconnectedExceptionWrapper e) {
            return new Field[] {};
        }
    }
        
    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    @Override
    public Field[] getAllStaticFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        synchronized (fieldsLock) {
            if (fields == null || refreshFields) {
                initFields ();
            }
            return getSubFields(staticFields, from, to);
        }
    }

    /**
     * Return all inherited fields.
     * 
     * @return all inherited fields
     */
    @Override
    public Field[] getInheritedFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        synchronized (fieldsLock) {
            if (fields == null || refreshFields) {
                initFields ();
            }
            return getSubFields(inheritedFields, from, to);
        }
    }
    
    private static Field[] getSubFields (Field[] fields, int from, int to) {
        if (fields == null) {
            return new Field[] {};
        }
        if (to != 0) {
            to = Math.min(fields.length, to);
            from = Math.min(fields.length, from);
            Field[] fv = new Field [to - from];
            System.arraycopy (fields, from, fv, 0, to - from);
            fields = fv;
        }
        return fields;
        
    }

    @Override
    public Super getSuper () {
        synchronized (superClassLoaded) {
            if (superClassLoaded[0]) {
                return superClass;
            }
            if (superClassLoading) {
                try {
                    superClassLoaded.wait();
                } catch (InterruptedException ex) {
                    return null;
                }
                if (superClassLoaded[0]) {
                    return superClass;
                }
            } else {
                superClassLoading = true;
            }
        }
        Super sup = null;
        try {
            Type t = getCachedType();
            if (t instanceof ClassType) {
                ClassType superType;
                //assert !java.awt.EventQueue.isDispatchThread();
                superType = ClassTypeWrapper.superclass((ClassType) t);
                if (superType != null) {
                    Super s = new SuperVariable(
                            getDebugger(),
                            (ObjectReference) this.getInnerValue(),
                            superType,
                            getID()
                            );
                    sup = s;
                }
            }
        } catch (ObjectCollectedExceptionWrapper ocex) {
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper e) {
        } finally {
            synchronized (superClassLoaded) {
                if (superClassLoading) {
                    superClass = sup;
                    superClassLoading = false;
                    superClassLoaded[0] = true;
                    superClassLoaded.notifyAll();
                }
            }
        }
        return sup;
    }

    public boolean hasAllTypes() {
        if (getInnerValue () == null) {
            return true;
        }
        Type t;
        synchronized (valueTypeLoaded) {
            if (!valueTypeLoaded[0]) {
                return false;
            }
            t = valueType;
        }
        if (t instanceof ClassType) {
            ClassType ct = (ClassType) t;
            if (!getDebugger().hasAllInterfaces(ct)) {
                return false;
            }
            synchronized (superClassLoaded) {
                if (!superClassLoaded[0]) {
                    return false;
                }
            }
        }
        return true;
    }

    public void loadAllTypes() {
        getAllInterfaces();
        Super s = getSuper();
        while (s != null) {
            s = s.getSuper();
        }
    }
    
    public List<JPDAClassType> getAllInterfaces() {
        if (getInnerValue () == null) {
            return null;
        }
        try {
            Type t = getCachedType();
            if (!(t instanceof ClassType)) {
                return null;
            }
            ClassType ct = (ClassType) t;
            return getDebugger().getAllInterfaces(ct);
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }

    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    @Override
    public String getToStringValue () throws InvalidExpressionException {
        Value v = getInnerValue ();
        return getToStringValue(v, getDebugger(), 0);
    }
    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue (int maxLength) throws InvalidExpressionException {
        Value v = getInnerValue ();
        try {
            return getToStringValue(v, getCachedType(), getDebugger(), maxLength);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
    }

    static String getToStringValue (Value v, JPDADebuggerImpl debugger, int maxLength) throws InvalidExpressionException {
        if (v == null) {
            return null;
        }
        com.sun.jdi.Type type;
        try {
            type = ValueWrapper.type(v);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
        return getToStringValue(v, type, debugger, maxLength);
    }
    
    static String getToStringValue (Value v, com.sun.jdi.Type type, JPDADebuggerImpl debugger, int maxLength) throws InvalidExpressionException {
        if (v == null) {
            return null;
        }
        try {
            if (!(type instanceof ClassType)) {
                return AbstractVariable.getValue (v);
            }
            ClassType ct = (ClassType) type;
            if (v instanceof CharValue) {
                return "\'" + v.toString () + "\'";
            }
            /*JPDAThread ct = debugger.getCurrentThread();
            if (ct != null) { For the case that toString() should be blocked by pending actions
                Object pendingAction = ((JPDAThreadImpl) ct).getPendingAction();
                if (pendingAction != null) {
                    return ((JPDAThreadImpl) ct).getPendingString(pendingAction);
                    //return "Action "+pendingActions.iterator().next()+" is pending...";
                }
            }*/
            boolean addQuotation = false;
            boolean addDots = false;
            StringReference sr;
            maxLength = (maxLength > 0 && maxLength < MAX_STRING_LENGTH) ? maxLength : MAX_STRING_LENGTH;
            if (v instanceof StringReference) {
                sr = (StringReference) v;
                addQuotation = true;
            } else if (maxLength > 0 && maxLength < Integer.MAX_VALUE) {
                Method toStringMethod = ClassTypeWrapper.concreteMethodByName(ct,
                     "toString", "()Ljava/lang/String;");  // NOI18N
                if (toStringMethod == null) {
                    return NbBundle.getMessage(AbstractObjectVariable.class, "MSG_No_toString");
                }
                sr = (StringReference) debugger.invokeMethod (
                    (ObjectReference) v,
                    toStringMethod,
                    new Value [0],
                    maxLength + 1
                );
            } else {
                Method toStringMethod = ClassTypeWrapper.concreteMethodByName(ct,
                    "toString", "()Ljava/lang/String;");  // NOI18N
                if (toStringMethod == null) {
                    return NbBundle.getMessage(AbstractObjectVariable.class, "MSG_No_toString");
                }
                sr = (StringReference) debugger.invokeMethod (
                    (ObjectReference) v,
                    toStringMethod,
                    new Value [0]
                );
            }
            if (sr == null) {
                return null;
            }
            String str = ShortenedStrings.getStringWithLengthControl(sr);
            if (addQuotation) {
                str = "\"" + str + "\""; // NOI18N
            }
            return str;
        } catch (InternalExceptionWrapper | ClassNotPreparedExceptionWrapper |
                ClassNotLoadedException | IncompatibleThreadStateException |
                InvalidTypeException | InvocationException ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
    }
    
    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments a arguments to be used
     *
     * @return value of given method call on this instance
     */
    @Override
    public Variable invokeMethod (
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException {
        return invokeMethod(null, methodName, signature, arguments);
    }

    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param thread the thread on which the method invocation is performed.
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments a arguments to be used
     *
     * @return value of given method call on this instance
     */
    public Variable invokeMethod (
        JPDAThread thread,
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException {
        try {
             
            // 1) find corrent method
            Value v = this.getInnerValue ();
            if (v == null) {
                return null;
            }
            ClassType ct = (ClassType) getCachedType();
            Method method = null;
            if (signature != null) {
                method = ClassTypeWrapper.concreteMethodByName(
                        ct,
                        methodName, signature);
            } else {
                List l = ReferenceTypeWrapper.methodsByName(
                        ct,
                        methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++) {
                    if ( !MethodWrapper.isAbstract((Method) l.get (j)) &&
                         MethodWrapper.argumentTypeNames((Method) l.get (j)).isEmpty()
                    ) {
                        method = (Method) l.get (j);
                        break;
                    }
                }
            }
            
            // 2) method not found => print all method signatures
            if (method == null) {
                List l = ReferenceTypeWrapper.methodsByName(
                        ct, methodName);
                int j, jj = l.size ();
                logger.log(Level.INFO, "No method {0}.{1} with signature {2}",
                           new Object[]{ TypeWrapper.name(ct), methodName, signature });
                logger.info("Found following methods with signatures:");
                for (j = 0; j < jj; j++) {
                    logger.info (TypeComponentWrapper.signature((Method) l.get (j)));
                }
                throw new NoSuchMethodException (
                    TypeWrapper.name(ct) + "." +
                        methodName + " : " + signature
                );
            }
            
            // 3) call this method
            Value[] vs = new Value [arguments.length];
            int i, k = arguments.length;
            for (i = 0; i < k; i++) {
                if (arguments [i] == null) {
                    vs [i] = null;
                } else {
                    vs [i] = ((AbstractVariable) arguments [i]).getInnerValue ();
                }
            }
            v = getDebugger().invokeMethod (
                (JPDAThreadImpl) thread,
                (ObjectReference) v,
                method,
                vs
            );
            
            // 4) encapsulate result
            if (v instanceof ObjectReference) {
                return new AbstractObjectVariable ( // It's also ObjectVariable
                        getDebugger(),
                        (ObjectReference) v,
                        getID() + method + "^"
                    );
            }
            return new AbstractVariable (getDebugger(), v, getID() + method);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        }
    }

    /**
     * Evaluates the expression in the context of this variable.
     * All methods are invoked on this variable,
     * <code>this</code> can be used to refer to this variable.
     * 
     * @param expression
     * @return Variable containing the result
     */
    public Variable evaluate(String expression) throws InvalidExpressionException {
        if ("toString()".equals(expression) && String.class.getName().equals(getType())) {  // NOI18N
            // String.toString() = this
            return this;
        }
        return getDebugger().evaluate(expression, this);
    }

    private com.sun.jdi.Type getCachedType() throws InternalExceptionWrapper,
                                                    VMDisconnectedExceptionWrapper,
                                                    ObjectCollectedExceptionWrapper {
        return getCachedType(null);
    }
    
    private com.sun.jdi.Type getCachedType(String[] namePtr) throws InternalExceptionWrapper,
                                                                    VMDisconnectedExceptionWrapper,
                                                                    ObjectCollectedExceptionWrapper {
        synchronized (valueTypeLoaded) {
            if (valueTypeLoaded[0]) {
                if (namePtr != null) {
                    namePtr[0] = valueTypeName;
                }
                return valueType;
            }
            if (valueTypeLoading) {
                try {
                    valueTypeLoaded.wait();
                } catch (InterruptedException ex) {
                    return null;
                }
                if (valueTypeLoaded[0]) { // Check if it was really loaded
                    if (namePtr != null) {
                        namePtr[0] = valueTypeName;
                    }
                    return valueType;
                } else {
                    return null;
                }
            } else {
                valueTypeLoading = true;
            }
        }
        Value v = getInnerValue();
        Type type = null;
        String typeName = "";
        try {
            if (v != null) {
                //assert !java.awt.EventQueue.isDispatchThread();
                type = ValueWrapper.type(v);
                typeName = TypeWrapper.name(type);
            }
        } finally {
            synchronized (valueTypeLoaded) {
                if (valueTypeLoading) { // While we're sill interested in it
                    valueType = type;
                    valueTypeName = typeName;
                    valueTypeLoading = false;
                    valueTypeLoaded[0] = true;
                    valueTypeLoaded.notifyAll();
                }
            }
        }
        if (namePtr != null) {
            namePtr[0] = typeName;
        }
        return type;
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    @Override
    public String getType () {
        if (genericType != null) {
            return genericType;
        }
        try {
            String[] typeNamePtr = new String[1];
            getCachedType(typeNamePtr);
            return typeNamePtr[0];
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            // The session is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            // The object is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
    }
    
    @Override
    public JPDAClassType getClassType() {
        try {
            com.sun.jdi.Type type = getCachedType();
            if (type instanceof ReferenceType) {
                return getDebugger().getClassType((ReferenceType) type);
            } else {
                return null;
            }
        } catch (ObjectCollectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }
    
    @Override
    public boolean equals (Object o) {
        return  (o instanceof AbstractObjectVariable) &&
                (getID().equals (((AbstractObjectVariable) o).getID()));
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
    
    // other methods............................................................
    
    @Override
    protected void setInnerValue (Value v) {
        super.setInnerValue(v);
        /*Value old = getInnerValue();
        if (old instanceof ObjectReference) {
            try {
                ObjectReferenceWrapper.enableCollection((ObjectReference) old);
            } catch (Exception ex) {}
        }
        if (v instanceof ObjectReference) {
            try {
                ObjectReferenceWrapper.disableCollection((ObjectReference) v);
            } catch (Exception ex) {}
        }*/
        synchronized (fieldsLock) {
            fields = null;
            staticFields = null;
            inheritedFields = null;
        }
        synchronized (superClassLoaded) {
            superClassLoading = false;
            superClassLoaded[0] = false;
            superClassLoaded.notifyAll();
        }
        synchronized (valueTypeLoaded) {
            valueTypeLoading = false;
            valueTypeLoaded[0] = false;
            valueTypeLoaded.notifyAll();
        }
    }
    
    private static String getTypeDescription (PushbackReader signature) 
    throws IOException {
        int c = signature.read();
        switch (c) {
        case 'Z':
            return "boolean";
        case 'B':
            return "byte";
        case 'C':
            return "char";
        case 'S':
            return "short";
        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'F':
            return "float";
        case 'D':
            return "double";
        case '[':
        {
            int arrayCount = 1;
            for (; ;arrayCount++) {
                if ((c = signature.read()) != '[') {
                    signature.unread(c);
                    break;
                }
            }
            return getTypeDescription(signature) + " " + brackets(arrayCount);
        }
        case 'L':
        {
            StringBuilder typeName = new StringBuilder(50);
            for (;;) {
                c = signature.read();
                if (c == ';') {
                    int idx = typeName.lastIndexOf("/");
                    return idx == -1 ? 
                        typeName.toString() : typeName.substring(idx + 1);
                }
                else if (c == '<') {
                    int idx = typeName.lastIndexOf("/");
                    if (idx != -1) {
                        typeName.delete(0, idx + 1);
                    }
                    typeName.append("<");
                    for (;;) {
                        String td = getTypeDescription(signature);
                        typeName.append(td);
                        c = signature.read();
                        if (c == '>') {
                            break;
                        }
                        signature.unread(c);
                        typeName.append(',');
                    }
                    signature.read();   // should be a semicolon
                    typeName.append(">");
                    return typeName.toString();
                }
                typeName.append((char)c);
            }
        }
        }
        throw new IOException();
    }

    private static String brackets (int arrayCount) {
        StringBuilder sb = new StringBuilder (arrayCount * 2);
        do {
            sb.append ("[]");
        } while (--arrayCount > 0);
        return sb.toString ();
    }

    private void initFields () {
        // Called under synchronized (fieldsLock)
        assert Thread.holdsLock(fieldsLock);
        refreshFields = false;
        Value value = getInnerValue();
        Type type;
        if (value != null) {
            try {
                type = ValueWrapper.type(value);
            } catch (InternalExceptionWrapper ocex) {
                type = null;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                type = null;
            } catch (VMDisconnectedExceptionWrapper e) {
                type = null;
            }
        } else {
            type = null;
        }
        if ( !(value instanceof ObjectReference) ||
             !(type instanceof ReferenceType)
        ) {
            this.fields = new Field [0];
            this.staticFields = new Field [0];
            this.inheritedFields = new Field [0];
        } else {
            try {
                ObjectReference or = (ObjectReference) value;
                ReferenceType rt = (ReferenceType) type;
                if (or instanceof ArrayReference) {
                    this.fields = getFieldsOfArray (
                        (ArrayReference) or, 
                        ArrayTypeWrapper.componentTypeName((ArrayType) rt),
                        this.getID (),
                        0, ArrayReferenceWrapper.length((ArrayReference) or));
                    this.staticFields = new Field[0];
                    this.inheritedFields = new Field[0];
                }
                else {
                    initFieldsOfClass(or, rt, this.getID ());
                }
            } catch (InternalExceptionWrapper iex) {
            } catch (ObjectCollectedExceptionWrapper iex) {
                // The object is gone => no fields
            } catch (VMDisconnectedExceptionWrapper e) {
            }
        }
    }

    private Field[] getFieldsOfArray (
            ArrayReference ar, 
            String componentType,
            String parentID,
            int from,
            int to
        ) {
            List l;
            try {
                l = ArrayReferenceWrapper.getValues(ar, from, to - from);
            } catch (InternalExceptionWrapper ex) {
                return new Field[0];
            } catch (VMDisconnectedExceptionWrapper ex) {
                return new Field[0];
            } catch (ObjectCollectedExceptionWrapper ex) {
                // The array was collected => no fields.
                return new Field[0];
            }
            int i, k = l.size ();
            Field[] ch = new Field [k];
            for (i = 0; i < k; i++) {
                Value v = (Value) l.get (i);
                ch [i] = (v == null || v instanceof ObjectReference) ?
                    new ObjectArrayFieldVariable (
                        getDebugger(), 
                        (ObjectReference) v, 
                        componentType, 
                        this,
                        from + i,
                        to - 1,
                        parentID
                    ) :
                    new ArrayFieldVariable (
                        getDebugger(), 
                        (PrimitiveValue) v, 
                        componentType, 
                        this,
                        from + i,
                        to - 1,
                        parentID
                    );
            }
            return ch;
        }

    private void initFieldsOfClass (
        ObjectReference or, 
        ReferenceType rt,
        String parentID)
    {
        List<Field> classFields = new ArrayList<Field>();
        List<Field> classStaticFields = new ArrayList<Field>();
        List<Field> allInheretedFields = new ArrayList<Field>();
        
        List<com.sun.jdi.Field> l;
        Set<com.sun.jdi.Field> s;
        try {
            l = ReferenceTypeWrapper.allFields0(rt);
            s = new HashSet<com.sun.jdi.Field>(ReferenceTypeWrapper.fields0(rt));

            int i, k = l.size();
            for (i = 0; i < k; i++) {
                com.sun.jdi.Field f = l.get (i);
                Field field = this.getField (f, or, this.getID());
                if (TypeComponentWrapper.isStatic(f)) {
                    classStaticFields.add(field);
                } else {
                    if (s.contains (f)) {
                        classFields.add(field);
                    } else {
                        allInheretedFields.add(field);
                    }
                }
            }
        } catch (ClassNotPreparedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
            classFields.clear();
            classStaticFields.clear();
            allInheretedFields.clear();
        }
        this.fields = classFields.toArray(new Field[0]);
        this.inheritedFields = allInheretedFields.toArray(new Field[0]);
        this.staticFields = classStaticFields.toArray(new Field[0]);
    }
    
    org.netbeans.api.debugger.jpda.Field getField (
        com.sun.jdi.Field f, 
        ObjectReference or, 
        String parentID
    ) {
        return getField(getDebugger(), f, or, parentID);
    }
    
    public static org.netbeans.api.debugger.jpda.Field getField (
        JPDADebuggerImpl debugger,
        com.sun.jdi.Field f, 
        ObjectReference or, 
        String parentID
    ) {
        String signature = f.signature();
        if (signature.length() == 1) {
            // Must be a primitive type or the void type
            return new FieldVariable(debugger, f, parentID, or);
        } else {
            boolean isClassRef = "Ljava/lang/Class;".equals(signature);     // NOI18N
            /*
                try {
                    isClassRef = TypeComponentWrapper.declaringType(f) instanceof ClassType;
                } catch (InternalExceptionWrapper iex) {
                } catch (VMDisconnectedExceptionWrapper vex) {}
            }*/
            if (isClassRef) {
                return new ClassFieldVariable (
                    debugger,
                    f,
                    parentID,
                    JPDADebuggerImpl.getGenericSignature(f),
                    or
                );
            } else {
                return new ObjectFieldVariable (
                    debugger,
                    f,
                    parentID,
                    JPDADebuggerImpl.getGenericSignature(f),
                    or
                );
            }
        }
    }
    
    @Override
    public List<ObjectVariable> getReferringObjects(long maxReferrers) {
        Value v = getJDIValue();
        if (v instanceof ObjectReference) {
                final String name = Long.toString(getUniqueID());
                final List<ObjectReference> referrers;
                try {
                    referrers = ObjectReferenceWrapper.referringObjects((ObjectReference) v, maxReferrers);
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return Collections.emptyList();
                } catch (InternalExceptionWrapper ex) {
                    return Collections.emptyList();
                } catch (ObjectCollectedExceptionWrapper ex) {
                    return Collections.emptyList();
                }
                return new AbstractList<ObjectVariable>() {
                    @Override
                    public ObjectVariable get(int i) {
                        ObjectReference obj = referrers.get(i);
                        if (obj instanceof ClassObjectReference) {
                            ClassObjectReference clobj = (ClassObjectReference) obj;
                            return new ClassVariableImpl(getDebugger(), clobj, name+" referrer "+i);
                        } else {
                            return new AbstractObjectVariable(getDebugger(), obj, name+" referrer "+i);
                        }
                    }

                    @Override
                    public int size() {
                        return referrers.size();
                    }
                };
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public long getUniqueID() {
        Value value = getJDIValue();
        if (!(value instanceof ObjectReference)) { // null or anything else than Object
            return 0L;
        } else {
            try {
                return ObjectReferenceWrapper.uniqueID((ObjectReference) value);
            } catch (InternalExceptionWrapper ex) {
                return 0L;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return 0L;
            } catch (ObjectCollectedExceptionWrapper ex) {
                return 0L;
            }
        }
    }
    
    private int cloneNumber = 1;
    
    @Override
    public Variable clone() {
        AbstractObjectVariable clon = new AbstractObjectVariable(getDebugger(), getJDIValue(), getID() + "_clone"+(cloneNumber++));
        clon.genericType = this.genericType;
        return clon;
    }
    
    @Override
    public String toString () {
        return "ObjectVariable ";
    }

    /*@Override
    protected void finalize() throws Throwable {
        Value v = getInnerValue();
        if (v instanceof ObjectReference) {
            try {
                System.err.println("ENABLING collection for "+v);
                ObjectReferenceWrapper.enableCollection((ObjectReference) v);
            } catch (Exception ex) {}
        }
        super.finalize();
    }*/

    /* Uncomment when needed. Was used to create "readable" String and Char values.
    private static String convertToStringInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\"':
                    sb.append ("\\\"");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    
    private static String convertToCharInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\'':
                    sb.append ("\\\'");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
     */
    
    private class DebuggetStateListener extends Object implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof Integer &&
                    JPDADebugger.STATE_RUNNING == ((Integer) newValue).intValue()) {
                    synchronized (AbstractObjectVariable.this.fieldsLock) {
                        AbstractObjectVariable.this.refreshFields = true;
                    }
                }
            }
        }
        
    }
    
}
