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
/*
 * PortTypeOperationCustomizationImpl.java
 *
 * Created on February 4, 2006, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collection;
import org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.api.customization.model.JavaMethod;
import org.netbeans.modules.websvc.api.customization.model.JavaParameter;
import org.netbeans.modules.websvc.api.customization.model.PortTypeOperationCustomization;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class PortTypeOperationCustomizationImpl extends CustomizationComponentImpl
    implements PortTypeOperationCustomization{
    
    /**
     * Creates a new instance of PortTypeOperationCustomizationImpl
     */
    public PortTypeOperationCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PortTypeOperationCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }

    public void setEnableAsyncMapping(EnableAsyncMapping async) {
        appendChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removeJavaParameter(JavaParameter parameter) {
        removeChild(JAVA_PARAMETER_PROPERTY, parameter);
    }

    public void addJavaParameter(JavaParameter parameter) {
        appendChild(JAVA_PARAMETER_PROPERTY, parameter);
    }

    public void setJavaMethod(JavaMethod method) {
        appendChild(JAVA_METHOD_PROPERTY, method);
    }

    public void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        appendChild(ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public Collection<JavaParameter> getJavaParameters() {
        return getChildren(JavaParameter.class);
    }

    public JavaMethod getJavaMethod() {
        return getChild(JavaMethod.class);
    }

    public EnableWrapperStyle getEnableWrapperStyle() {
        return getChild(EnableWrapperStyle.class);
    }

    public EnableAsyncMapping getEnableAsyncMapping() {
       return getChild(EnableAsyncMapping.class);
    }

    public void removeEnableAsyncMapping(EnableAsyncMapping async) {
        removeChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removeJavaMethod(JavaMethod method) {
        removeChild(JAVA_METHOD_PROPERTY, method);
    }

    public void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        removeChild(this.ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
