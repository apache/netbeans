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
 * PortTypeCustomizationImpl.java
 *
 * Created on February 4, 2006, 12:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.api.customization.model.JavaClass;
import org.netbeans.modules.websvc.api.customization.model.PortTypeCustomization;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class PortTypeCustomizationImpl extends CustomizationComponentImpl implements
   PortTypeCustomization{
    
    /**
     * Creates a new instance of PortTypeCustomizationImpl
     */
    public PortTypeCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PortTypeCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }

    public void setEnableAsyncMapping(EnableAsyncMapping async) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableAsyncMapping.class, ENABLE_ASYNC_MAPPING_PROPERTY, 
                async, classes);
    }

    public void setJavaClass(JavaClass javaClass) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(JavaClass.class, JAVA_CLASS_PROPERTY, 
                javaClass, classes);
    }

    public void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableWrapperStyle.class, 
                ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle, 
                classes);
    }

    public JavaClass getJavaClass() {
        return getChild(JavaClass.class);
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

    public void removeJavaClass(JavaClass javaClass) {
        removeChild(JAVA_CLASS_PROPERTY, javaClass);
    }

    public void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        removeChild(ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
