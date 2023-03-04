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

/**
 *
 * @author mkuchtiak
 */
public class ResultModel {

    private String name;
    private String partName;
    private String targetNamespace;
    private String resultType;
    
    /** Creates a new instance of MethodModel */
    ResultModel() {
    }
    
    /** Creates a new instance of MethodModel */
    ResultModel(String resultType) {
        this.resultType=resultType;
    }
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name=name;
    }
    
    public String getPartame() {
        return partName;
    }

    void setPartName(String partName) {
        this.partName = partName;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
    
    public String getResultType() {
        return resultType;
    }

    void setResultType(String faultType) {
        this.resultType = faultType;
    }
        
    public boolean isEqualTo(ResultModel model) {
        if (!Utils.isEqualTo(resultType,model.resultType)) return false;
        if (!Utils.isEqualTo(name,model.name)) return false;
        if (!Utils.isEqualTo(partName,model.partName)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        return true;
    }
}
