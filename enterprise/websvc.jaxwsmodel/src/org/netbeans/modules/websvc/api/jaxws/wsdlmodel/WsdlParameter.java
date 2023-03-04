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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.java.JavaParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;

/**
 *
 * @author mkuchtiak
 */
public class WsdlParameter implements WSParameter {
    private JavaParameter parameter;
    private String name;
    
    /** Creates a new instance of WsdlParameter */
    public WsdlParameter(JavaParameter parameter) {
        this.parameter=parameter;
    }
    
    public Object getInternalJAXWSParameter() {
        return parameter;
    }
    
    public String getName() {
        if (name == null) {
            name = parameter.getName();
        }
        return name;
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public String getTypeName() {
        String type = parameter.getType().getName();
        return isHolder()?"javax.xml.ws.Holder<"+wrapperType(type)+">":type;//NOI18N
    }
    
    public boolean isHolder() {
        return parameter.isHolder();
    }
    
    public String getHolderName() {
        return parameter.getHolderName();
    }
    
    private String wrapperType(String type) {
        if ("int".equals(type)) return "Integer"; //NOI18N
        else if ("float".equals(type)) return "Float"; //NOI18N
        else if ("double".equals(type)) return "Double"; //NOI18N
        else if ("byte".equals(type)) return "Byte"; //NOI18N
        else if ("long".equals(type)) return "Long"; //NOI18N
        else if ("boolean".equals(type)) return "Boolean"; //NOI18N
        else if ("char".equals(type)) return "Character"; //NOI18N
        else return type;
    }
}
