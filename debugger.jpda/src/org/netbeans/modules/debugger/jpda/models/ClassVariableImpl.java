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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import com.sun.jdi.ClassObjectReference;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 *
 * @author Martin Entlicher
 */
public class ClassVariableImpl extends AbstractObjectVariable implements ClassVariable {
    
    private ClassObjectReference clazz;
    
    /** Creates a new instance of ClassVariableImpl */
    public ClassVariableImpl(
        JPDADebuggerImpl debugger,
        ClassObjectReference clazz,
        String parentID
    ) {
        super (
            debugger,
            clazz,
            parentID + ".class"
        );
        this.clazz = clazz;
    }
    
    public JPDAClassType getClassType() {
        try {
            return getDebugger().getClassType(ObjectReferenceWrapper.referenceType(clazz));
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

    public JPDAClassType getReflectedType() {
        try {
            return getDebugger().getClassType(ClassObjectReferenceWrapper.reflectedType(clazz));
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
    public ClassVariableImpl clone() {
        ClassVariableImpl clon = new ClassVariableImpl(getDebugger(), clazz,
                getID().substring(0, getID().length() - ".class".length()));
        return clon;
    }
    
    public String toString () {
        return "ClassVariable " + getClassType();
    }

}
