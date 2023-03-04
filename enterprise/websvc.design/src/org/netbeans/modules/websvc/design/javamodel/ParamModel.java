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

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class ParamModel {
    
    String name;
    String javaName;
    private FileObject implementationClass;
    private String partName;
    private String targetNamespace;
    private WebParam.Mode mode = WebParam.Mode.IN;
    private String paramType;
    private ElementHandle methodHandle;
    
    /** Creates a new instance of MethodModel */
    ParamModel() {
    }
    
    /** Creates a new instance of MethodModel */
    ParamModel(String name, String javaName) {
        this.name=name;
        this.javaName=javaName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (!this.name.equals(name)) {
            JaxWsUtils.setWebParamAttrValue(implementationClass, methodHandle, javaName, "name", name);
            this.name=(name==null?javaName:name);
        }
    }
    
    public FileObject getImplementationClass(){
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
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public Mode getMode() {
        return mode;
    }

    void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public String getParamType() {
        return paramType;
    }

    void setParamType(String paramType) {
        this.paramType = paramType;
    }
  
    public String getPartName() {
        return partName;
    }

    void setPartName(String partName) {
        this.partName = partName;
    }
        
    public boolean isEqualTo(ParamModel model) {
        if (!name.equals(model.name)) return false;
        if (!paramType.equals(model.paramType)) return false;
        if (!mode.equals(model.mode)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        if (!Utils.isEqualTo(partName, model.partName)) return false;
        return true;
    }
}
