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

package org.netbeans.modules.websvc.design.javamodel;

import java.util.List;
import javax.xml.soap.SOAPMessage;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class MethodModel {
    
    private FileObject implementationClass;
    String javaName;
    String operationName;
    String action;
    private ResultModel result;
    private List<ParamModel> params;
    boolean oneWay;
    private JavadocModel javadoc;
    private List<FaultModel> faults;
    private SOAPMessage soapRequest;
    private SOAPMessage soapResponse;
    private ElementHandle methodHandle; 
    
    /** Creates a new instance of MethodModel */
    MethodModel(FileObject implementationClass, String operationName) {
        this.implementationClass = implementationClass;
        this.operationName=operationName;
    }
    /** Creates a new instance of MethodModel */
    MethodModel() {
    }
    
    public FileObject getImplementationClass(){
        if(!implementationClass.isValid()){
            FileObject parent = implementationClass.getParent();
            implementationClass = parent.getFileObject(implementationClass.getNameExt());
        }
        return implementationClass;
    }
    
    void setImplementationClass(FileObject impl){
        implementationClass = impl;
    }
    
    public ElementHandle getMethodHandle() {
        return methodHandle;
    }
    
   void setMethodHandle(ElementHandle methodHandle) {
       this.methodHandle=methodHandle;
   }
    
    
    public String getOperationName() {
        return operationName;
    }
    
    public void setOperationName(String operationName) {
        if (this.operationName == null || !this.operationName.equals(operationName)) {
            JaxWsUtils.setWebMethodAttrValue(getImplementationClass(), methodHandle, 
                    "operationName", operationName); //NOI18N
            
            this.operationName=operationName==null?javaName:operationName;
        }
    }

    public ResultModel getResult() {
        return result;
    }

    void setResult(ResultModel result) {
        this.result = result;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<ParamModel> getParams() {
        return params;
    }

    void setParams(List<ParamModel> params) {
        this.params = params;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public JavadocModel getJavadoc() {
        return javadoc;
    }
    
    void setJavadoc(JavadocModel javadoc) {
        this.javadoc=javadoc;
    }
    
    public void setJavadoc(String javadoc) {
        Utils.setJavadoc(implementationClass, this, javadoc);
    }
    
    public List<FaultModel> getFaults() {
        return faults;
    }
    
    void setFaults(List<FaultModel> faults) {
        this.faults=faults;
    }
    
    public boolean isEqualTo(MethodModel model) {
        if (!operationName.equals(model.operationName)) return false;
        if (!result.isEqualTo(model.result)) return false;
        if (oneWay!=model.oneWay) return false;
        if (!Utils.isEqualTo(action, model.action)) return false;
        if (javadoc!=null) {
            if (!javadoc.isEqualTo(model.javadoc)) return false;
        } else if (model.javadoc!=null) return false;
        if (params.size()!=model.params.size()) return false;
        for(int i = 0;i<params.size();i++) {
            if (!params.get(i).isEqualTo(model.params.get(i))) return false;
        }
        if (faults.size()!=model.faults.size()) return false;
        for(int i = 0;i<faults.size();i++) {
            if (!faults.get(i).isEqualTo(model.faults.get(i))) return false;
        }
        return true;
    }
    
    public SOAPMessage getSoapRequest() {
        return soapRequest;
    }

    void setSoapRequest(SOAPMessage soapRequest) {
        this.soapRequest = soapRequest;
    }
    
    public SOAPMessage getSoapResponse() {
        return soapResponse;
    }

    void setSoapResponse(SOAPMessage soapResponse) {
        this.soapResponse = soapResponse;
    }

    
    
}
