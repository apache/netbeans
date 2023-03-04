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
package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;

/**
 * Create values with disabled collection, which can be released later on.
 * This is necessary e.g. for temporary variables so that they are not collected
 * prematurely.
 */
public final class PersistentValues {

    private final VirtualMachine vm;
    private final List<ObjectReference> references = new ArrayList<>(5);

    public PersistentValues(VirtualMachine vm) {
        this.vm = vm;
    }

    public StringReference mirrorOf(String string) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, UnsupportedOperationExceptionWrapper {
        StringReference stringMirror = null;
        while (stringMirror == null) {
            stringMirror = VirtualMachineWrapper.mirrorOf(vm, string);
            try {
                ObjectReferenceWrapper.disableCollection(stringMirror);
            } catch (ObjectCollectedExceptionWrapper ce) {
                stringMirror = null;
            }
        }
        references.add(stringMirror);
        return stringMirror;
    }

    /**
     * @return the reference or <code>null</code> in case of JDI exceptions.
     */
    public StringReference mirrorOf0(String string) {
        try {
            return mirrorOf(string);
        } catch (InternalExceptionWrapper | UnsupportedOperationExceptionWrapper | VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }

    public <T extends ObjectReference> T valueOf(ValueSupplier<T> valueSupplier) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, UnsupportedOperationExceptionWrapper {
        T value = null;
        while (value == null) {
            value = valueSupplier.get();
            try {
                ObjectReferenceWrapper.disableCollection(value);
            } catch (ObjectCollectedExceptionWrapper ce) {
                value = null;
            }
        }
        references.add(value);
        return value;
    }

    public <T extends ObjectReference> T invokeOf(InvokeSupplier<T> valueSupplier) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException, InvocationException, ObjectCollectedExceptionWrapper {
        T value = null;
        while (value == null) {
            value = valueSupplier.get();
            try {
                ObjectReferenceWrapper.disableCollection(value);
            } catch (ObjectCollectedExceptionWrapper ce) {
                value = null;
            }
        }
        references.add(value);
        return value;
    }

    public void collect() {
        for (ObjectReference reference : references) {
            try {
                ObjectReferenceWrapper.enableCollection(reference);
            } catch (InternalExceptionWrapper | ObjectCollectedExceptionWrapper | UnsupportedOperationExceptionWrapper ex) {
                // continue
            } catch (VMDisconnectedExceptionWrapper ex) {
                break;
            }
        }
        references.clear();
    }

    @FunctionalInterface
    public interface ValueSupplier<T> {

        public T get() throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, UnsupportedOperationExceptionWrapper;
    }

    @FunctionalInterface
    public interface InvokeSupplier<T> {

        public T get() throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, UnsupportedOperationExceptionWrapper, ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException, InvocationException, ObjectCollectedExceptionWrapper;
    }
}
