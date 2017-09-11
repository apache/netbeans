/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
                    ObjectReferenceWrapper.invokeMethod(theUploadedClass, tr, aMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
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
        ObjectReference cl = (ObjectReference) ObjectReferenceWrapper.invokeMethod(tr, tr, getContextCl, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        ClassType classLoaderClass = null;
        if (cl == null) {
            classLoaderClass = getClass(vm, ClassLoader.class.getName());
            Method getSystemClassLoader = ClassTypeWrapper.concreteMethodByName(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
            cl = (ObjectReference) ClassTypeWrapper.invokeMethod(classLoaderClass, tr, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
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
