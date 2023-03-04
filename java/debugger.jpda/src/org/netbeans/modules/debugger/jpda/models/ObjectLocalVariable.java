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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MutableVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocalVariableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
class ObjectLocalVariable extends AbstractObjectVariable
                          implements MutableVariable,
                                     org.netbeans.api.debugger.jpda.LocalVariable {

    LocalVariable       local;
    JPDAThread          thread;
    int                 depth;
    String              className;
    String              genericSignature;

    ObjectLocalVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String className,
        LocalVariable local,
        String genericSignature,
        CallStackFrameImpl frame
    ) {
        this(debugger, value, className, local, genericSignature,
             getID(local),
             frame);
    }

    private static String getID(LocalVariable local) {
        try {
            return LocalVariableWrapper.name(local) + LocalVariableWrapper.hashCode(local) + "^";
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        }
    }

    private ObjectLocalVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String className, 
        LocalVariable local, 
        String genericSignature,
        String id,
        CallStackFrameImpl frame
    ) {
        super (debugger, 
            value, 
            genericSignature, 
            id);
        this.local = local;
        if (frame != null) {
            this.thread = frame.getThread();
            this.depth = frame.getFrameDepth();
        }
        this.className = className;
    }

    
    // LocalVariable impl.......................................................

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        try {
            return LocalVariableWrapper.name(local);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        }
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return className;
    }
    
    protected final void setClassName(String className) {
        this.className = className;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        try {
            return LocalVariableWrapper.typeName(local);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        }
    }
    
    @NbBundle.Messages({"# {0} - variable name",
                        "MSG_VarNotVisibleInCurrentFrame=Variable {0} is not visible in the current stack frame.",
                        "MSG_ObjectWasCollected=Object was collected already."})
    protected final void setValue (Value value) throws InvalidExpressionException {
        try {
            CallStackFrame[] frames = thread.getCallStack(depth, depth + 1);
            if (frames.length == 0) {
                // No top frame, can not set the value
                // Just some sample code that throws VMDisconnectedException
                // when the VM is already disconnected:
                VirtualMachineWrapper.mirrorOf(value.virtualMachine(), true);
                // If the VM lives, report the problem...
                throw new InvalidExpressionException(NbBundle.getMessage(ObjectLocalVariable.class, "MSG_NoTopFrame"));
            }
            StackFrame sf = ((CallStackFrameImpl) frames[0]).getStackFrame();
            if (!LocalVariableWrapper.isVisible(local, sf)) {
                throw new InvalidExpressionException(Bundle.MSG_VarNotVisibleInCurrentFrame(getName()));
            }
            StackFrameWrapper.setValue (sf, local, value);
            setInnerValue(value);
        } catch (AbsentInformationException |
                 InvalidTypeException |
                 ClassNotLoadedException |
                 InvalidStackFrameExceptionWrapper ex) {
            throw new InvalidExpressionException(ex);
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new InvalidExpressionException(Bundle.MSG_ObjectWasCollected(), ex);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
        }
    }
    
    private int cloneNumber = 1;

    public ObjectLocalVariable clone() {
        ObjectLocalVariable clon = new ObjectLocalVariable(getDebugger(), (ObjectReference) getJDIValue(), className, local, genericSignature, getID() + "_clone"+(cloneNumber++), null);
        clon.depth = this.depth;
        clon.thread = this.thread;
        return clon;
    }
    
    // other methods ...........................................................
    
    final void setFrame(CallStackFrameImpl frame) {
        this.thread = frame.getThread();
        this.depth = frame.getFrameDepth();
    }

    public String toString () {
        try {
            return "ObjectLocalVariable " + LocalVariableWrapper.name(local);
        } catch (InternalExceptionWrapper ex) {
            return "ObjectLocalVariable " + ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        }
    }
}
