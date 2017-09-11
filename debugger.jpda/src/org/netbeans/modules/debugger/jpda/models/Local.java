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
