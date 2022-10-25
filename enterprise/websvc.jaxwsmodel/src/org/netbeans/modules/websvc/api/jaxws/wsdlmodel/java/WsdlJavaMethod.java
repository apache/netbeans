/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.api.jaxws.wsdlmodel.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;

/**
 *
 * @author ayubskhan
 */
public class WsdlJavaMethod implements JavaMethod {
    private com.sun.tools.ws.processor.model.java.JavaMethod method;
    private String name;
    private WsdlJavaType returnType;
    private List<JavaParameter> parameters;

    public WsdlJavaMethod(com.sun.tools.ws.processor.model.java.JavaMethod method) {
        this.method = method;
    }

    public Object getInternalJAXWSJavaMethod() {
        return this.method;
    }
    
    public String getName() {
        if(this.name == null) {
            this.name = this.method.getName();
        }
        return this.name;
    }

    public WsdlJavaType getReturnType() {
        if(this.returnType == null) {
            this.returnType = new WsdlJavaType(this.method.getReturnType());
        }
        return this.returnType;
    }

    public boolean hasParameter(String paramName) {
        for(JavaParameter parameter : getParametersList()) {
            if (paramName.equals(parameter.getName())) {
                return true;
            }
        }
        return false;
    }

    public JavaParameter getParameter(String paramName) {
        for(JavaParameter parameter : getParametersList()) {
            if (paramName.equals(parameter.getName())) {
                return parameter;
            }
        }
        return null;
    }

    public List<JavaParameter> getParametersList() {
        if(this.parameters == null) {
            this.parameters = new ArrayList<>();
            for(com.sun.tools.ws.processor.model.java.JavaParameter p:this.method.getParametersList()) {
                this.parameters.add(new WsdlJavaParameter(p));
            }
        }
        return this.parameters;
    }

    public Iterator<String> getExceptions() {
        return this.method.getExceptions();
    }
}
