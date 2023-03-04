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
package org.netbeans.modules.debugger.jpda.remote;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;

/**
 * Services that manages uploaded classes into a remote JVM.
 *
 * @author Martin Entlicher
 */
public final class RemoteServices {

    private RemoteServices() {}

    /**
     * Upload a new remote class. The thread must already be locked for method
     * invocations before calling this method.
     * The class object has disabled collection.
     * Enable the collection when not used any more.
     * @param tr The thread to upload the class on.
     * @param rc The definition of the remote class.
     * @return The new uploaded class.
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @throws IOException
     * @throws PropertyVetoException
     * @throws InternalExceptionWrapper
     * @throws VMDisconnectedExceptionWrapper
     * @throws ObjectCollectedExceptionWrapper
     * @throws UnsupportedOperationExceptionWrapper
     * @throws ClassNotPreparedExceptionWrapper 
     */
    public static ClassObjectReference uploadClass(ThreadReference tr, RemoteClass rc) throws InvalidTypeException,
                                                                                              ClassNotLoadedException,
                                                                                              IncompatibleThreadStateException,
                                                                                              InvocationException,
                                                                                              IOException,
                                                                                              PropertyVetoException,
                                                                                              InternalExceptionWrapper,
                                                                                              VMDisconnectedExceptionWrapper,
                                                                                              ObjectCollectedExceptionWrapper,
                                                                                              UnsupportedOperationExceptionWrapper,
                                                                                              ClassNotPreparedExceptionWrapper {
        VirtualMachine vm = MirrorWrapper.virtualMachine(tr);
        ObjectReference classLoader = getContextClassLoader(tr, vm);
        ClassType classLoaderClass = (ClassType) ObjectReferenceWrapper.referenceType(classLoader);

        String className = rc.name;
        ClassObjectReference theUploadedClass = null;
        ArrayReference byteArray = createTargetBytes(vm, rc.bytes, new ByteValue[256]);
        StringReference nameMirror = null;
        try {
            Method defineClass = ClassTypeWrapper.concreteMethodByName(classLoaderClass,
                                                                       "defineClass",
                                                                       "(Ljava/lang/String;[BII)Ljava/lang/Class;");
            boolean uploaded = false;
            while (!uploaded) {
                nameMirror = VirtualMachineWrapper.mirrorOf(vm, className);
                try {
                    ObjectReferenceWrapper.disableCollection(nameMirror);
                    uploaded = true;
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    // Just collected, try again...
                } catch (UnsupportedOperationExceptionWrapper uex) {
                    // Hope it will not be GC'ed...
                    uploaded = true;
                }
            }
            uploaded = false;
            while (!uploaded) {
                theUploadedClass = (ClassObjectReference) ObjectReferenceWrapper.invokeMethod(
                        classLoader, tr, defineClass,
                        Arrays.asList(nameMirror, byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)),
                        ObjectReference.INVOKE_SINGLE_THREADED);
                try {
                    // Disable collection only of the basic class
                    ObjectReferenceWrapper.disableCollection(theUploadedClass);
                    uploaded = true;
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    // Just collected, try again...
                } catch (UnsupportedOperationExceptionWrapper uex) {
                    // Hope it will not be GC'ed...
                    uploaded = true;
                }
            }
            if (uploaded) {
                // Initialize the class:
                ClassType bc = ((ClassType) theUploadedClass.reflectedType());
                if (!bc.isInitialized()) {
                    // Trying to initialize the class
                    ClassType theClass = getClass(vm, Class.class.getName());
                    // Call some method that will prepare the class:
                    Method aMethod = ClassTypeWrapper.concreteMethodByName(theClass, "getConstructors", "()[Ljava/lang/reflect/Constructor;");
                    ObjectReferenceWrapper.invokeMethod(theUploadedClass, tr, aMethod, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                }
            }
        } finally {
            try {
                ObjectReferenceWrapper.enableCollection(byteArray); // We can dispose it now
                if (nameMirror != null) {
                    ObjectReferenceWrapper.enableCollection(nameMirror);
                }
            } catch (UnsupportedOperationExceptionWrapper uex) {}
        }
        return theUploadedClass;
    }

    private static ObjectReference getContextClassLoader(ThreadReference tr, VirtualMachine vm) throws InternalExceptionWrapper,
                                                                                                       VMDisconnectedExceptionWrapper,
                                                                                                       ClassNotPreparedExceptionWrapper,
                                                                                                       InvalidTypeException,
                                                                                                       ClassNotLoadedException,
                                                                                                       IncompatibleThreadStateException,
                                                                                                       InvocationException,
                                                                                                       ObjectCollectedExceptionWrapper {
        ReferenceType threadType = tr.referenceType();
        Method getContextCl = ClassTypeWrapper.concreteMethodByName((ClassType) threadType, "getContextClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference cl = (ObjectReference) ObjectReferenceWrapper.invokeMethod(tr, tr, getContextCl, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        ClassType classLoaderClass = null;
        if (cl == null) {
            classLoaderClass = getClass(vm, ClassLoader.class.getName());
            Method getSystemClassLoader = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
            cl = (ObjectReference) ClassTypeWrapper.invokeMethod(classLoaderClass, tr, getSystemClassLoader, Collections.<Value>emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
        }
        return cl;
    }

    private static ClassType getClass(VirtualMachine vm, String name) throws InternalExceptionWrapper,
                                                                             ObjectCollectedExceptionWrapper,
                                                                             VMDisconnectedExceptionWrapper {
        List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (ReferenceTypeWrapper.classLoader(c) == null) {
                clazz = c;
                break;
            }
        }
        if (clazz == null && classList.size() > 0) {
            clazz = classList.get(0);
        }
        return (ClassType) clazz;
    }
    
    private static ArrayType getArrayClass(VirtualMachine vm, String name) throws InternalExceptionWrapper,
                                                                                  ObjectCollectedExceptionWrapper,
                                                                                  VMDisconnectedExceptionWrapper {
        List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (ReferenceTypeWrapper.classLoader(c) == null) {
                clazz = c;
                break;
            }
        }
        return (ArrayType) clazz;
    }

    private static ArrayReference createTargetBytes(VirtualMachine vm, byte[] bytes,
                                                    ByteValue[] mirrorBytesCache) throws InvalidTypeException,
                                                                                         ClassNotLoadedException,
                                                                                         InternalExceptionWrapper,
                                                                                         VMDisconnectedExceptionWrapper,
                                                                                         ObjectCollectedExceptionWrapper {
        ArrayType bytesArrayClass = getArrayClass(vm, "byte[]");
        ArrayReference array = null;
        boolean disabledCollection = false;
        while (!disabledCollection) {
            array = ArrayTypeWrapper.newInstance(bytesArrayClass, bytes.length);
            try {
                ObjectReferenceWrapper.disableCollection(array);
                disabledCollection = true;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // Collected too soon, try again...
            } catch (UnsupportedOperationExceptionWrapper uex) {
                // Hope it will not be GC'ed...
                disabledCollection = true;
            }
        }
        List<Value> values = new ArrayList<Value>(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            ByteValue mb = mirrorBytesCache[128 + b];
            if (mb == null) {
                mb = VirtualMachineWrapper.mirrorOf(vm, b);
                mirrorBytesCache[128 + b] = mb;
            }
            values.add(mb);
        }
        ArrayReferenceWrapper.setValues(array, values);
        return array;
    }
    
}
