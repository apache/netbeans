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
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MutableVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocalVariableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.openide.util.Exceptions;


/**
 * @author   Jan Jancura
 */
class Local extends AbstractVariable implements MutableVariable,
org.netbeans.api.debugger.jpda.LocalVariable {
        
    LocalVariable       local;
    JPDAThread          thread;
    int                 depth;
    String              className;
    String              genericSignature;
    
    Local (
        JPDADebuggerImpl debugger,
        PrimitiveValue value, 
        String className,
        LocalVariable local,
        CallStackFrameImpl frame
    ) {
        super (
            debugger, 
            value, 
            getID(local, value)
        );
        this.local = local;
        if (frame != null) {
            this.thread = frame.getThread();
            this.depth = frame.getFrameDepth();
        }
        this.className = className;
    }

    private static String getID(LocalVariable local, PrimitiveValue value) {
        try {
            return LocalVariableWrapper.name(local) + LocalVariableWrapper.hashCode(local) + (value instanceof ObjectReference ? "^" : "");
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
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
    
    protected final void setValue (Value value) throws InvalidExpressionException {
        try {
            StackFrame sf = ((CallStackFrameImpl) thread.getCallStack(depth, depth + 1)[0]).getStackFrame();
            sf.setValue (local, value);
            setInnerValue(value);
        } catch (AbsentInformationException aiex) {
            throw new InvalidExpressionException(aiex);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InvalidStackFrameException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InvalidStackFrameExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper ex) {
        }
    }
    
    // other methods ...........................................................
    
    final void setFrame(CallStackFrameImpl frame) {
        this.thread = frame.getThread();
        this.depth = frame.getFrameDepth();
    }

    public Local clone() {
        Local clon;
        clon = new Local(getDebugger(), (PrimitiveValue) getJDIValue(), className, local, null);
        clon.depth = this.depth;
        clon.thread = this.thread;
        return clon;
    }
    
    public String toString () {
        try {
            return "LocalVariable " + LocalVariableWrapper.name(local);
        } catch (InternalExceptionWrapper ex) {
            return "LocalVariable " + ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        }
    }
}
