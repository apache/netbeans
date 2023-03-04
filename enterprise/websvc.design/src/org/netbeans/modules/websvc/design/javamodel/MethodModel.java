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
