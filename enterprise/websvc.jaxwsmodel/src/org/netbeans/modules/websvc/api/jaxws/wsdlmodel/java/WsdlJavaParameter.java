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
package org.netbeans.modules.websvc.api.jaxws.wsdlmodel.java;

import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;

/**
 *
 * @author ayubskhan
 */
public class WsdlJavaParameter implements JavaParameter {

    private com.sun.tools.ws.processor.model.java.JavaParameter param;
    private String name;
    private WsdlJavaType type;

    public WsdlJavaParameter(com.sun.tools.ws.processor.model.java.JavaParameter param) {
        this.param = param;
    }

    public Object getInternalJAXWSJavaParameter() {
        return this.param;
    }
    
    public String getName() {
        if(this.name == null) {
            this.name = this.param.getName();
        }
        return this.name;
    }

    public WsdlJavaType getType() {
        if(this.type == null) {
            this.type = new WsdlJavaType(this.param.getType());
        }
        return this.type;
    }

    public Object getParameter() {
        return this.param.getParameter();
    }

    public boolean isHolder() {
        return this.param.isHolder();
    }
    
    public String getHolderName() {
        return this.param.getHolderName();
    }

    public boolean isIN() {
        return param.getParameter().isIN();
    }

    public boolean isINOUT() {
        return param.getParameter().isINOUT();
    }

    public boolean isOUT() {
        return param.getParameter().isOUT();
    }
}
