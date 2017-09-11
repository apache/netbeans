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
