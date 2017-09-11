/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 * A field whose value is a class instance.
 * 
 * @author Martin Entlicher
 */
public class ClassFieldVariable extends ObjectFieldVariable implements ClassVariable {
    
    public ClassFieldVariable (
        JPDADebuggerImpl debugger,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        super (
            debugger,
            field,
            parentID,
            genericSignature,
            objectReference
        );
    }
    
    private ClassFieldVariable (
        JPDADebuggerImpl debugger, 
        ClassObjectReference value, 
        //String className,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        super(debugger, value, field, parentID, genericSignature, objectReference);
    }
    
    @Override
    public JPDAClassType getReflectedType() {
        Value innerValue = getInnerValue();
        if (!(innerValue instanceof ClassObjectReference)) {
            throw new IllegalStateException("Field "+field+" value "+innerValue+" is not a class.");
        }
        ClassObjectReference cor = (ClassObjectReference) innerValue;
        try {
            return getDebugger().getClassType(ClassObjectReferenceWrapper.reflectedType(cor));
        } catch (InternalExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        } catch (ObjectCollectedExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        } catch (VMDisconnectedExceptionWrapper ex) {
            // re-throw, we should not return null and can not throw anything checked.
            throw ex.getCause();
        }
    }
    
    @Override
    public ClassFieldVariable clone() {
        String name;
        try {
            name = TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            name = ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            name = "0";
        }
        ClassFieldVariable clon = new ClassFieldVariable(getDebugger(), (ClassObjectReference) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + name + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                genericSignature, objectReference);
        clon.classType = classType;
        return clon;
    }

    
    // other methods ...........................................................

    @Override
    public String toString () {
        try {
            return "ClassFieldVariable " + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
    }
    
}
