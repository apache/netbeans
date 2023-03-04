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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.NbBundle;

/**
 * @author   Martin Entlicher
 */
public class ArgumentObjectVariable extends AbstractObjectVariable implements org.netbeans.api.debugger.jpda.LocalVariable {
        
    String              name;
    String              className;
    String              genericSignature;
    
    public ArgumentObjectVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String name,
        String className
    ) {
        super (
            debugger, 
            value, 
            name + className.hashCode() + "^"
        );
        this.name = name;
        this.className = className;
    }

    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return name;
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
        return className;
    }
    
    public Value getInnerValue() {
        return super.getInnerValue();
    }

    @Override
    public void setValue(String expression) throws InvalidExpressionException {
        throw new InvalidExpressionException(NbBundle.getMessage(ArgumentObjectVariable.class, "MSG_CanNotChangeArgumentValues"));
    }

    @Override
    protected void setValue(Value value) throws InvalidExpressionException {
        throw new InvalidExpressionException(NbBundle.getMessage(ArgumentObjectVariable.class, "MSG_CanNotChangeArgumentValues"));
    }
    
    /*
    protected final void setValue (Value value) throws InvalidExpressionException {
        try {
            StackFrame sf = ((CallStackFrameImpl) thread.getCallStack(depth, depth + 1)[0]).getStackFrame();
            sf.setValue (local, value);
        } catch (AbsentInformationException aiex) {
            throw new InvalidExpressionException(aiex);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
     */
    
    // other methods ...........................................................
    
    public ArgumentObjectVariable clone() {
        ArgumentObjectVariable clon;
        clon = new ArgumentObjectVariable(getDebugger(), (ObjectReference) getJDIValue(), name, className);
        return clon;
    }
    
    public String toString () {
        return "ArgumentObjectVariable " + name;
    }
}
