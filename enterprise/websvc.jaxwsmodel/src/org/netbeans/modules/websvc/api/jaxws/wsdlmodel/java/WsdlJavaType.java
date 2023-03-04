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

import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType;

/**
 *
 * @author ayubskhan
 */
public class WsdlJavaType implements JavaType {

    private String name;
    private String realName;
    private com.sun.tools.ws.processor.model.java.JavaType type;

    public WsdlJavaType(com.sun.tools.ws.processor.model.java.JavaType type) {
        this.type = type;
    }
    
    public Object getInternalJAXWSJavaType() {
        return this.type;
    }

    public String getName() {
        if(this.name == null) {
            this.name = this.type.getName();
        }
        return this.name;
    }

    public String getRealName() {
        if(this.realName == null) {
            this.realName = this.type.getRealName();
        }
        return this.realName;
    }
    
    public String getFormalName() {
        return getName();
    }

    public boolean isPresent() {
        return this.type.isPresent();
    }

    public boolean isHolder() {
        return this.type.isHolder();
    }
    
    public boolean isHolderPresent() {
        return this.type.isHolderPresent();

    }

    public String getInitString() {
        return this.type.getInitString();
    }

    public String getHolderName() {
        return this.type.getHolderName();
    }
}
