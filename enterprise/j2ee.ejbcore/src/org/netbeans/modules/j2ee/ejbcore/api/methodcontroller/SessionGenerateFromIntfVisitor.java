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
