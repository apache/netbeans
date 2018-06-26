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

import java.util.Collection;
import java.util.Set;
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
class EntityGenerateFromImplVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromImpl {
    
    private MethodModel intfMethod;
    private String destination;
    private String home;
    private String component;
    
    public EntityGenerateFromImplVisitor() {
    }
    
    public void getInterfaceMethodFromImpl(MethodType methodType, String home, String component) {
        this.home = home;
        this.component = component;
        methodType.accept(this);
    }
    
    public MethodModel getInterfaceMethod() {
        return intfMethod;
    }
    
    public String getDestinationInterface() {
        return destination;
    }
    
    public void visit(BusinessMethodType bmt) {
        intfMethod = bmt.getMethodElement();
        destination = component;
    }
    
    public void visit(CreateMethodType cmt) {
        intfMethod = cmt.getMethodElement();
        String origName = intfMethod.getName();
        String newName = null;
        if (origName.startsWith("ejbPostCreate")) {
            newName = chopAndUpper(origName,"ejbPost"); //NOI18N
        } else {
            newName = chopAndUpper(origName,"ejb"); //NOI18N
        }
        intfMethod = MethodModel.create(
                newName, 
                intfMethod.getReturnType(),
                intfMethod.getBody(),
                intfMethod.getParameters(),
                intfMethod.getExceptions(),
                intfMethod.getModifiers()
                );
        destination = home;
    }
    
    public void visit(HomeMethodType hmt) {
        intfMethod = hmt.getMethodElement();
        String origName = intfMethod.getName();
        String newName = chopAndUpper(origName,"ejbHome"); //NOI18N
        intfMethod = MethodModel.create(
                newName, 
                intfMethod.getReturnType(),
                intfMethod.getBody(),
                intfMethod.getParameters(),
                intfMethod.getExceptions(),
                intfMethod.getModifiers()
                );
        destination = home;
    }
    
    public void visit(FinderMethodType fmt) {
        intfMethod = fmt.getMethodElement();
        String origName = intfMethod.getName();
        String newName = chopAndUpper(origName,"ejb"); //NOI18N
        String fqn = intfMethod.getReturnType();
        boolean changeType = false;
        if (!fqn.equals(Collection.class.getName()) || !fqn.equals(Set.class.getName())) {
            changeType = true;
        }
        intfMethod = MethodModel.create(
                newName, 
                changeType ? component : intfMethod.getReturnType(),
                intfMethod.getBody(),
                intfMethod.getParameters(),
                intfMethod.getExceptions(),
                intfMethod.getModifiers()
                );
        //TODO: RETOUCHE need to empty the body?
//        intfMethod.setBody(null);
        destination = home;
    }
    
    private String chopAndUpper(String fullName, String chop) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.delete(0, chop.length());
        stringBuffer.setCharAt(0, Character.toLowerCase(stringBuffer.charAt(0)));
        return stringBuffer.toString();
    }
}
