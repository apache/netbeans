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

package org.netbeans.modules.websvc.rest.client;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor;

/**
 *
 * @author mkuchtiak
 */
public class SecurityParams {
    String signature;
    List<String> params = new ArrayList<String>();
    List<MethodDescriptor> methodDescriptors = new ArrayList<MethodDescriptor>();
    List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
    List<ServletDescriptor> servletDescriptors = new ArrayList<ServletDescriptor>();

    public List<ServletDescriptor> getServletDescriptors() {
        return servletDescriptors;
    }

    public void setServletDescriptors(List<ServletDescriptor> servletDescriptors) {
        this.servletDescriptors = servletDescriptors;
    }

    public List<FieldDescriptor> getFieldDescriptors() {
        return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
        this.fieldDescriptors = fieldDescriptors;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(List<MethodDescriptor> methodDescriptors) {
        this.methodDescriptors = methodDescriptors;
    }
    
}
