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
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyVetoException;
import java.io.InvalidObjectException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorVisitor;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalArgumentExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.openide.util.NbBundle;
import sun.reflect.ReflectionFactory;

/**
 *
 * @author Martin Entlicher
 */
public class VariableMirrorTranslator {
    
    private static final Logger logger = Logger.getLogger(VariableMirrorTranslator.class.getName());
    
    private static final Object NO_MIRROR = new String("NO_MIRROR");
    
    private VariableMirrorTranslator() {}
    
    static Object createMirrorObject(Value value) {
        return createMirrorObject(value, new HashMap<Value, Object>());
    }
    
    private static Object createMirrorObject(Value value, Map<Value, Object> mirrorsMap) {
        try {
            Type type = ValueWrapper.type(value);
            String typeStr;
            if (type instanceof ArrayType) {
                typeStr = TypeWrapper.signature(type);
                typeStr = typeStr.replace('/', '.');
            } else {
                typeStr = TypeWrapper.name(type);
            }
            if (value instanceof ObjectReference && type instanceof ReferenceType) {
                try {
                    Class<?> clazz = Class.forName(typeStr);
                    if (String.class.equals(clazz)) {
                        return ShortenedStrings.getStringWithLengthControl((StringReference) value);
                    }
                    return createMirrorObject((ObjectReference) value, (ReferenceType) type, clazz, mirrorsMap);
                } catch (ClassNotFoundException ex) {
                } catch (ClassNotPreparedExceptionWrapper ex) {
                }
            } else {
                if ("boolean".equals(typeStr)) {
                    return Boolean.valueOf(((BooleanValue) value).booleanValue());
                } else if ("byte".equals(typeStr)) {
                    return new Byte(((ByteValue) value).byteValue());
                } else if ("char".equals(typeStr)) {
                    return new Character(((CharValue) value).charValue());
                } else if ("short".equals(typeStr)) {
                    return new Short(((ShortValue) value).shortValue());
                } else if ("int".equals(typeStr)) {
                    return new Integer(((IntegerValue) value).intValue());
                } else if ("long".equals(typeStr)) {
                    return new Long(((LongValue) value).longValue());
                } else if ("float".equals(typeStr)) {
                    return new Float(((FloatValue) value).floatValue());
                } else if ("double".equals(typeStr)) {
                    return new Double(((DoubleValue) value).doubleValue());
                } else {
                    throw new IllegalArgumentException("Unknown primitive type: "+typeStr+" from "+type);
                }
            }
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        }
        return null;
    }
    
    static private Object createPristineInstanceOf(Class clazz) {
        try {
            //return clazz.newInstance(); - not sufficient
            Constructor constructor;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException nsmex) {
                ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
                constructor = rf.newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor());
            }
            Object newInstance = constructor.newInstance();
            return newInstance;
            
        } catch (InstantiationException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        } catch (IllegalAccessException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        } catch (InvocationTargetException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        } catch (NoSuchMethodException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        } catch (SecurityException ex) {
            logger.log(Level.INFO, "Problem while creating a pristine instance of "+clazz, ex);
        }
        return null;
    }
    
    private static java.lang.reflect.Field getDeclaredOrInheritedField(Class clazz, String name) throws SecurityException {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(name);
            return field;
        } catch (NoSuchFieldException ex) {}
        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            return getDeclaredOrInheritedField(superClass, name);
        } else {
            return null;
        }
    }
    
    static private Object createMirrorObject(ObjectReference value, ReferenceType type,
                                             Class clazz, Map<Value, Object> mirrorsMap)
                                             throws ClassNotPreparedExceptionWrapper,
                                                    InternalExceptionWrapper,
                                                    VMDisconnectedExceptionWrapper,
                                                    ObjectCollectedExceptionWrapper {
        logger.log(Level.FINE, "createMirrorObject({0}, {1}, {2})", new Object[]{value, type, clazz});
        if (clazz.isArray() && value instanceof ArrayReference) {
            ArrayReference arrayRef = (ArrayReference) value;
            int l = ArrayReferenceWrapper.length(arrayRef);
            Object array = Array.newInstance(clazz.getComponentType(), l);
            List<Value> values = ArrayReferenceWrapper.getValues(arrayRef);
            for (int i = 0; i < l; i++) {
                Value v = values.get(i);
                Object element;
                if (v == null) {
                    element = null;
                } else {
                    element = mirrorsMap.get(v);
                    if (element == null) {
                        element = createMirrorObject(v, mirrorsMap);
                    }
                    if (element == null) {
                        return null;
                    }
                }
                Array.set(array, i, element);
            }
            logger.log(Level.FINE, "  return {0}", array);
            return array;
        } else {
            if (clazz.equals(Class.class)) {
                // TODO: can try to create the same class that is referenced by value
                return null; // Ignore Class classes. They can not be instantiated.
            }
            Object newInstance = createSpecialized(value, type, clazz, mirrorsMap);
            if (newInstance != null) {
                if (NO_MIRROR == newInstance) {
                    // Do not auto-translate such values, it's dangerous
                    return null;
                }
                mirrorsMap.put(value, newInstance);
                return newInstance;
            }
            newInstance = createPristineInstanceOf(clazz);
            if (newInstance == null) {
                return null;
            }
            mirrorsMap.put(value, newInstance);
            List<Field> fields = ReferenceTypeWrapper.allFields0(type);
            List<Field> fieldsToAskFor = new ArrayList<Field>();
            for (Field f : fields) {
                if (!f.isStatic()) {
                    fieldsToAskFor.add(f);
                }
            }
            Map<Field, Value> fieldValues = ObjectReferenceWrapper.getValues(value, fieldsToAskFor);
            newInstance = setFieldsValues(newInstance, clazz, fieldValues, mirrorsMap);
            logger.log(Level.FINE, "  return {0}", newInstance);
            return newInstance;
        }
    }
    
    private static Object setFieldsValues(Object newInstance, Class clazz, Map<Field, Value> fieldValues, Map<Value, Object> mirrorsMap) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        for (Field f : fieldValues.keySet()) {
            String name = TypeComponentWrapper.name(f);
            java.lang.reflect.Field field = getDeclaredOrInheritedField(clazz, name);
            if (field == null) {
                logger.log(Level.CONFIG, "No Such Field({0}) of class {1}", new Object[]{name, clazz});
                return null;
            }
            field.setAccessible(true);
            Value v = fieldValues.get(f);
            try {
                if (v == null) {
                    field.set(newInstance, null);
                } else {
                    Object mv = mirrorsMap.get(v);
                    if (mv == null) {
                        mv = createMirrorObject(v, mirrorsMap);
                    }
                    if (mv != null) {
                        field.set(newInstance, mv);
                    } else {
                        logger.log(Level.CONFIG, "Unable to translate field {0} of class {1}", new Object[]{name, clazz});
                        return null;
                    }
                }
            } catch (IllegalAccessException ex) {
                logger.log(Level.CONFIG, "IllegalAccessException({0}) of field {1}", new Object[]{ex.getLocalizedMessage(), field});
                return null;
            }
        }
        return newInstance;
    }
    
    private static Object createSpecialized(ObjectReference value, ReferenceType type,
                                            Class clazz, Map<Value, Object> mirrorsMap) throws InternalExceptionWrapper,
                                                                                               VMDisconnectedExceptionWrapper,
                                                                                               ObjectCollectedExceptionWrapper,
                                                                                               ClassNotPreparedExceptionWrapper {
        if (java.awt.Font.class.isAssignableFrom(clazz)) {
            //Font.getAttributes();
            Map<Field, Value> fieldValues = getFieldValues(value, type, "values", "name", "size", "pointSize", "style");  // NOI18N
            if (fieldValues == null) {
                return NO_MIRROR;
            }
            Object fontInstance = createPristineInstanceOf(java.awt.Font.class);
            if (fontInstance == null) {
                return NO_MIRROR;
            }
            mirrorsMap.put(value, fontInstance);
            fontInstance = setFieldsValues(fontInstance, java.awt.Font.class, fieldValues, mirrorsMap);
            if (fontInstance == null) {
                return NO_MIRROR;
            } else {
                return fontInstance;
            }
        } else if (java.net.URL.class.isAssignableFrom(clazz)) {
            Map<Field, Value> fieldValues = getFieldValues(value, type, "handler");  // NOI18N
            if (fieldValues == null) {
                return NO_MIRROR;
            }
            // No URL without handler
            for (Value v : fieldValues.values()) {
                if (v == null) {
                    return NO_MIRROR;
                }
            }
            return null;
        } else {
            return null;
        }
    }
    
    private static Map<Field, Value> getFieldValues(ObjectReference value, ReferenceType type, String... names) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper, ClassNotPreparedExceptionWrapper {
        final int n = names.length;
        List<Field> fieldsToAskFor = new ArrayList<Field>(n);
        for (String name : names) {
            Field field = ReferenceTypeWrapper.fieldByName(type, name);
            if (field == null) {
                return null;
            }
            fieldsToAskFor.add(field);
        }
        Map<Field, Value> fieldValues = ObjectReferenceWrapper.getValues(value, fieldsToAskFor);
        return fieldValues;
    }
    
    private static boolean willInvokeMethods(boolean isObject, Class clazz) {
        return isObject &&
               !clazz.isPrimitive() &&
               !String.class.equals(clazz);
    }
    
    public static Value createValueFromMirror(Object mirror, boolean isObject, JPDADebuggerImpl debugger)
                                             throws InvalidObjectException, InternalExceptionWrapper,
                                                    VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper,
                                                    InvalidTypeException, ClassNotLoadedException,
                                                    ClassNotPreparedExceptionWrapper, IllegalArgumentExceptionWrapper {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            throw new VMDisconnectedExceptionWrapper(new VMDisconnectedException());
        }
        Class<?> clazz = mirror.getClass();
        JPDAThreadImpl currentThread;
        Lock lock;
        if (willInvokeMethods(isObject, clazz)) {
            currentThread = (JPDAThreadImpl) debugger.getCurrentThread();
            if (currentThread == null) {
                throw new InvalidObjectException
                        (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContext"));
            }
            lock = currentThread.accessLock.writeLock();
            lock.lock();
        } else {
            currentThread = null;
            lock = null;
        }
        boolean invoking = false;
        try {
            ThreadReference threadReference;
            if (currentThread != null) {
                threadReference = currentThread.getThreadReference();
                currentThread.notifyMethodInvoking();
                invoking = true;
            } else {
                threadReference = null;
            }
            
            return createValueFromMirror(mirror, clazz, isObject, vm, threadReference);
            
        } catch (IncompatibleThreadStateException ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (UnsupportedOperationExceptionWrapper ex) {
            InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
            ioex.initCause(ex);
            throw ioex;
        } catch (PropertyVetoException pvex) {
            InvalidObjectException ioex = new InvalidObjectException(pvex.getLocalizedMessage());
            ioex.initCause(pvex);
            throw ioex;
        } finally {
            if (invoking) {
                currentThread.notifyMethodInvokeDone();
            }
            if (lock != null) {
                lock.unlock();
            }
        }
    }
    
    private static Value createValueFromMirror(Object mirror, Class clazz, boolean isObject, VirtualMachine vm, ThreadReference thread)
                                               throws InvalidObjectException, InternalExceptionWrapper,
                                                      VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper,
                                                      InvalidTypeException, ClassNotLoadedException,
                                                      ClassNotPreparedExceptionWrapper, IllegalArgumentExceptionWrapper, IncompatibleThreadStateException, UnsupportedOperationExceptionWrapper {
        boolean isPrimitive = clazz.isPrimitive();
        if (!isObject || isPrimitive) { // Primitive type
            if (Boolean.TYPE.equals(clazz) || Boolean.class.equals(clazz)) {
                return vm.mirrorOf(((Boolean) mirror).booleanValue());
            }
            if (Byte.TYPE.equals(clazz) || Byte.class.equals(clazz)) {
                return vm.mirrorOf(((Byte) mirror).byteValue());
            }
            if (Character.TYPE.equals(clazz) || Character.class.equals(clazz)) {
                return vm.mirrorOf(((Character) mirror).charValue());
            }
            if (Short.TYPE.equals(clazz) || Short.class.equals(clazz)) {
                return vm.mirrorOf(((Short) mirror).shortValue());
            }
            if (Integer.TYPE.equals(clazz) || Integer.class.equals(clazz)) {
                return vm.mirrorOf(((Integer) mirror).intValue());
            }
            if (Long.TYPE.equals(clazz) || Long.class.equals(clazz)) {
                return vm.mirrorOf(((Long) mirror).longValue());
            }
            if (Float.TYPE.equals(clazz) || Float.class.equals(clazz)) {
                return vm.mirrorOf(((Float) mirror).floatValue());
            }
            if (Double.TYPE.equals(clazz) || Double.class.equals(clazz)) {
                return vm.mirrorOf(((Double) mirror).doubleValue());
            }
            throw new InvalidObjectException("Unknown primitive type "+clazz);
        } else {
            if (String.class.equals(clazz)) {
                return vm.mirrorOf((String) mirror);
            } else if (clazz.isArray()) {
                //Class ct = clazz.getComponentType();
                int l = Array.getLength(mirror);
                String typeName = clazz.getCanonicalName();
                ArrayType arrayType = (ArrayType) getOrLoadClass(vm, typeName);
                if (arrayType == null) {
                    throw new InvalidObjectException("Unknown class "+typeName);
                }
                ArrayReference array = ArrayTypeWrapper.newInstance(arrayType, l);
                for (int i = 0; i < l; i++) {
                    Object element = Array.get(mirror, i);
                    if (element != null) {
                        Value v = createValueFromMirror(element, clazz.getComponentType(), true, vm, thread);
                        ArrayReferenceWrapper.setValue(array, i, v);
                    }
                }
                return array;
            } else {
                String typeName = clazz.getName();
                ReferenceType type = getOrLoadClass(vm, typeName);
                if (type instanceof ClassType) {
                    ObjectReference obj;
                    try {
                        obj = createObjectOfType((ClassType) type, vm, thread);
                    } catch (InvocationException ex) {
                        InvalidObjectException ioex = new InvalidObjectException(ex.getLocalizedMessage());
                        ioex.initCause(ex);
                        throw ioex;
                    }
                    List<Field> fields = ReferenceTypeWrapper.allFields0(type);
                    List<Field> fieldsToSet = new ArrayList<Field>();
                    for (Field f : fields) {
                        if (!f.isStatic()) {
                            fieldsToSet.add(f);
                        }
                    }
                    for (Field f : fieldsToSet) {
                        String name = TypeComponentWrapper.name(f);
                        Object fm;
                        Class<?> fieldType;
                        try {
                            java.lang.reflect.Field field = getDeclaredOrInheritedField(clazz, name);
                            if (field == null) {
                                InvalidObjectException ioex = new InvalidObjectException("No field "+name+" of class "+clazz);
                                throw ioex;
                            }
                            field.setAccessible(true);
                            fm = field.get(mirror);
                            fieldType = field.getType();
                        //} catch (NoSuchFieldException nsfex) {
                        //    InvalidObjectException ioex = new InvalidObjectException("No field "+name+" of class "+clazz);
                        //    ioex.initCause(nsfex);
                        //    throw ioex;
                        } catch (SecurityException seex) {
                            InvalidObjectException ioex = new InvalidObjectException("Retrieving field "+name+" of class "+clazz);
                            ioex.initCause(seex);
                            throw ioex;
                        } catch (IllegalAccessException iaex) {
                            InvalidObjectException ioex = new InvalidObjectException("Illegal access to field "+name+" of class "+clazz);
                            ioex.initCause(iaex);
                            throw ioex;
                        } catch (IllegalArgumentException iaex) {
                            InvalidObjectException ioex = new InvalidObjectException("Illegal argument to field "+name+" of class "+clazz);
                            ioex.initCause(iaex);
                            throw ioex;
                        }
                        Value fv;
                        if (fm == null) {
                            fv = null;
                        } else {
                            if (fieldType.isPrimitive()) {
                                // Primitive type
                                fv = createValueFromMirror(fm, fieldType, true, vm, thread);
                            } else {
                                fv = createValueFromMirror(fm, fm.getClass(), true, vm, thread);
                            }
                        }
                        // java.lang.reflect.Field.set() can modify values of final fields, by debugger can not! :-(
                        if (!f.isFinal()) {
                            ObjectReferenceWrapper.setValue(obj, f, fv);
                        } else {
                            setValueToFinalField(obj, name, (ClassType) type, fv, vm, thread);
                        }
                    }
                    return obj;
                } else {
                    throw new InvalidObjectException("Type "+typeName+" ("+type+") is not a class type.");
                }
            }
        }
    }
    
    private static ReferenceType getOrLoadClass(VirtualMachine vm, String name) {
        List<ReferenceType> types = vm.classesByName(name);
        if (types.size() > 0) {
            if (types.size() == 1) {
                return types.get(0);
            }
            try {
                ReferenceType preferedType = JPDAUtils.getPreferredReferenceType(types, null);
                if (preferedType != null) {
                    return preferedType;
                }
            } catch (VMDisconnectedExceptionWrapper ex) {
                throw ex.getCause();
            }
            // No preferred, just take the first one:
            return types.get(0);
        }
        // DO NOT TRY TO LOAD CLASSES AT ALL! See http://www.netbeans.org/issues/show_bug.cgi?id=168949
        return null;
    }
    
    private static ObjectReference createObjectOfType(ClassType type, VirtualMachine vm, ThreadReference thread)
                                                      throws InvalidObjectException, InternalExceptionWrapper,
                                                             VMDisconnectedExceptionWrapper, ClassNotPreparedExceptionWrapper,
                                                             ObjectCollectedExceptionWrapper, InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, UnsupportedOperationExceptionWrapper {
        com.sun.jdi.Method c = ClassTypeWrapper.concreteMethodByName(type, "<init>", "()V");
        if (c == null || !c.declaringType().equals(type)) { // No no-arg constructor
            //ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
            //constructor = rf.newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor());
            ClassType reflectionFactoryType = (ClassType) getOrLoadClass(vm, "sun.reflect.ReflectionFactory");
            if (reflectionFactoryType == null) {
                throw new InvalidObjectException("No constructor for "+TypeWrapper.name(type));
            }
            com.sun.jdi.Method getReflectionFactoryMethod = ClassTypeWrapper.concreteMethodByName(
                    reflectionFactoryType, "getReflectionFactory", "()Lsun/reflect/ReflectionFactory;");
            if (getReflectionFactoryMethod == null) {
                throw new InvalidObjectException("Reflection Factory getter not found.");
            }
            ClassType objectClassType = (ClassType) getOrLoadClass(vm, "java.lang.Object");
            if (objectClassType == null) {
                throw new InvalidObjectException("No Object class found in the target VM.");
            }
            ClassObjectReference objectClass = ReferenceTypeWrapper.classObject(objectClassType);
            com.sun.jdi.Method getDeclaredConstructorMethod = ClassTypeWrapper.concreteMethodByName(
                    ((ClassType) ObjectReferenceWrapper.referenceType(objectClass)),
                    "getDeclaredConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;");
            if (getDeclaredConstructorMethod == null) {
                throw new InvalidObjectException("getDeclaredConstructor() method not found on Object class. Can not create a new instance.");
            }
            com.sun.jdi.Method newConstructorForSerializationMethod = ClassTypeWrapper.concreteMethodByName(
                    reflectionFactoryType,
                    "newConstructorForSerialization",
                    "(Ljava/lang/Class;Ljava/lang/reflect/Constructor;)Ljava/lang/reflect/Constructor;");
            if (newConstructorForSerializationMethod == null) {
                throw new InvalidObjectException("newConstructorForSerialization() method not found on Object class. Can not create a new instance.");
            }
            
            ObjectReference objectConstructor = (ObjectReference)
                    ObjectReferenceWrapper.invokeMethod(objectClass, thread,
                                                  getDeclaredConstructorMethod,
                                                  Collections.EMPTY_LIST,
                                                  ClassType.INVOKE_SINGLE_THREADED);
            ObjectReference rf = (ObjectReference)
                    ClassTypeWrapper.invokeMethod(reflectionFactoryType, thread,
                                                  getReflectionFactoryMethod,
                                                  Collections.EMPTY_LIST,
                                                  ClassType.INVOKE_SINGLE_THREADED);
            ObjectReference constructor = (ObjectReference)
                    ObjectReferenceWrapper.invokeMethod(rf, thread,
                                                        newConstructorForSerializationMethod,
                                                        Arrays.asList(new Value[] {
                                                            ReferenceTypeWrapper.classObject(type),
                                                            objectConstructor }),
                                                        ClassType.INVOKE_SINGLE_THREADED);
            // Object newInstance = constructor.newInstance();
            com.sun.jdi.Method newInstanceMethod = ClassTypeWrapper.concreteMethodByName(
                    ((ClassType) ObjectReferenceWrapper.referenceType(constructor)),
                    "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;");
            if (newInstanceMethod == null) {
                throw new InvalidObjectException("newInstance() method not found on Constructor class. Can not create a new instance.");
            }
            ObjectReference newInstance = (ObjectReference)
                    ObjectReferenceWrapper.invokeMethod(constructor, thread,
                                                        newInstanceMethod,
                                                        Collections.EMPTY_LIST,
                                                        ClassType.INVOKE_SINGLE_THREADED);
            return newInstance;
        } else {
            return ClassTypeWrapper.newInstance(type, thread, c, Collections.EMPTY_LIST, ClassType.INVOKE_SINGLE_THREADED);
        }
    }
    
    private static void setValueToFinalField(ObjectReference obj, String name, ClassType clazz, Value fv, VirtualMachine vm, ThreadReference thread) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ClassNotPreparedExceptionWrapper, ClassNotLoadedException, ObjectCollectedExceptionWrapper, IncompatibleThreadStateException, UnsupportedOperationExceptionWrapper, InvalidTypeException, InvalidObjectException {
        ObjectReference fieldRef = getDeclaredOrInheritedField(clazz, name, vm, thread);
        if (fieldRef == null) {
            InvalidObjectException ioex = new InvalidObjectException("No field "+name+" of class "+clazz);
            throw ioex;
        }
        // field.setAccessible(true);
        ClassType fieldClassType = (ClassType) ValueWrapper.type(fieldRef);
        com.sun.jdi.Method setAccessibleMethod = ClassTypeWrapper.concreteMethodByName(
                fieldClassType, "setAccessible", "(Z)V");
        try {
            ObjectReferenceWrapper.invokeMethod(fieldRef, thread, setAccessibleMethod,
                                                Collections.singletonList(vm.mirrorOf(true)),
                                                ClassType.INVOKE_SINGLE_THREADED);
            // field.set(newInstance, fv);
            com.sun.jdi.Method setMethod = ClassTypeWrapper.concreteMethodByName(
                    fieldClassType, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            if (fv instanceof PrimitiveValue) {
                PrimitiveType pt = (PrimitiveType) ValueWrapper.type(fv);
                ReferenceType fieldBoxingClass = EvaluatorVisitor.adjustBoxingType(clazz, pt, null);
                fv = EvaluatorVisitor.box((PrimitiveValue) fv, fieldBoxingClass, thread, null);
            }
            List<Value> args = Arrays.asList(new Value[] { obj, fv });
            ObjectReferenceWrapper.invokeMethod(fieldRef, thread, setMethod,
                                                args,
                                                ClassType.INVOKE_SINGLE_THREADED);
        } catch (InvocationException iex) {
            throw new InvalidObjectException(
                    "Problem setting value "+fv+" to field "+name+" of class "+clazz+
                    " : "+iex.exception());
        }
    }
    
    private static Type boxType(Type t) {
        if (t instanceof ClassType) {
            String name = ((ClassType) t).name();
            if (name.equals("java.lang.Boolean")) {
                t = t.virtualMachine().mirrorOf(true).type();
            } else if (name.equals("java.lang.Byte")) {
                t = t.virtualMachine().mirrorOf((byte) 10).type();
            } else if (name.equals("java.lang.Character")) {
                t = t.virtualMachine().mirrorOf('a').type();
            } else if (name.equals("java.lang.Integer")) {
                t = t.virtualMachine().mirrorOf(10).type();
            } else if (name.equals("java.lang.Long")) {
                t = t.virtualMachine().mirrorOf(10l).type();
            } else if (name.equals("java.lang.Short")) {
                t = t.virtualMachine().mirrorOf((short)10).type();
            } else if (name.equals("java.lang.Float")) {
                t = t.virtualMachine().mirrorOf(10f).type();
            } else if (name.equals("java.lang.Double")) {
                t = t.virtualMachine().mirrorOf(10.0).type();
            }
        }
        return t;
    }

    private static ObjectReference getDeclaredOrInheritedField(ClassType clazz, String name, VirtualMachine vm, ThreadReference thread) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ClassNotPreparedExceptionWrapper, ClassNotLoadedException, ObjectCollectedExceptionWrapper, IncompatibleThreadStateException, UnsupportedOperationExceptionWrapper, InvalidTypeException {
        //try {
        //    java.lang.reflect.Field field = clazz.getDeclaredField(name);
        //    return field;
        //} catch (NoSuchFieldException ex) {}
        ClassType classType = (ClassType) getOrLoadClass(vm, "java.lang.Class");
        com.sun.jdi.Method getDeclaredFieldMethod = ClassTypeWrapper.concreteMethodByName(
                classType, "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;");
        try {
            ObjectReference fieldRef = (ObjectReference)
                ObjectReferenceWrapper.invokeMethod(ReferenceTypeWrapper.classObject(clazz),
                                                    thread,
                                                    getDeclaredFieldMethod,
                                                    Collections.singletonList(vm.mirrorOf(name)),
                                                    ClassType.INVOKE_SINGLE_THREADED);
            return fieldRef;
        } catch (InvocationException ex) {
            // Likely NoSuchFieldException, try the super class...
        }
        //Class superClass = clazz.getSuperclass();
        ClassType superClass = ClassTypeWrapper.superclass(clazz);
        if (superClass != null) {
            return getDeclaredOrInheritedField(superClass, name, vm, thread);
        } else {
            return null;
        }
    }
    
}
