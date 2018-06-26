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
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
final class SessionGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {

    private MethodModel implMethod;
    private static final String TODO = "//TODO implement "; //NOI18N
    
    public void getInterfaceMethodFromImpl(MethodType methodType) {
        methodType.accept(this);
    }
    
    public MethodModel getImplMethod() {
        return implMethod;
    }
    
    public MethodModel getSecondaryMethod() {
        return null;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = bmt.getMethodElement();
        String body = TODO + implMethod.getName() + EntityGenerateFromIntfVisitor.getReturnStatement(implMethod.getReturnType());
        implMethod = MethodModel.create(
                implMethod.getName(), 
                implMethod.getReturnType(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
       
    public void visit(CreateMethodType cmt) {
        implMethod = cmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName, "ejb"); //NOI18N
        String body = TODO + newName;
        implMethod = MethodModel.create(
                newName, 
                "void",
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(HomeMethodType hmt) {
        assert false: "session beans do not have home methods";
    }
    
    public void visit(FinderMethodType fmt) {
        assert false: "session beans do not have finder methods";
    }
    
    private String prependAndUpper(String fullName, String prefix) {
         StringBuffer buffer = new StringBuffer(fullName);
         buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
         return prefix+buffer.toString();
    }
}
