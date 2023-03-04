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
 * DefinitionsCustomizationImpl.java
 *
 * Created on February 2, 2006, 5:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.api.customization.model.JavaPackage;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class DefinitionsCustomizationImpl extends CustomizationComponentImpl
        implements DefinitionsCustomization{
    
    /**
     * Creates a new instance of DefinitionsCustomizationImpl
     */
    public DefinitionsCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public DefinitionsCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }
    
    public void setEnableAsyncMapping(EnableAsyncMapping async) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableAsyncMapping.class,
                ENABLE_ASYNC_MAPPING_PROPERTY, async, classes);
    }
    
    public void setPackage(JavaPackage pack) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(JavaPackage.class, PACKAGE_PROPERTY, pack, classes);
    }
    
    public void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableWrapperStyle.class, ENABLE_WRAPPER_STYLE_PROPERTY,
                wrapperStyle, classes);
    }
    
    public void setEnableMIMEContent(EnableMIMEContent mime) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableMIMEContent.class,
                ENABLE_MIME_CONTENT_PROPERTY, mime, classes);
    }
    
    public JavaPackage getPackage() {
        return getChild(JavaPackage.class);
    }
    
    public EnableWrapperStyle getEnableWrapperStyle() {
        return getChild(EnableWrapperStyle.class);
    }
    
    public EnableMIMEContent getEnableMIMEContent() {
        return getChild(EnableMIMEContent.class);
    }
    
    public EnableAsyncMapping getEnableAsyncMapping() {
        return getChild(EnableAsyncMapping.class);
    }
    
     public void removeEnableAsyncMapping(EnableAsyncMapping async) {
        removeChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removePackage(JavaPackage pack) {
        removeChild(PACKAGE_PROPERTY, pack);
    }

    public void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        removeChild(ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public void removeEnableMIMEContent(EnableMIMEContent mime) {
        removeChild(ENABLE_MIME_CONTENT_PROPERTY, mime);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
