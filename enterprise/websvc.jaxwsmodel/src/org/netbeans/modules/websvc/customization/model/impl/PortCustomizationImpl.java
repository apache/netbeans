
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
 * PortCustomizationImpl.java
 *
 * Created on February 4, 2006, 4:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.api.customization.model.JavaMethod;
import org.netbeans.modules.websvc.api.customization.model.PortCustomization;
import org.netbeans.modules.websvc.api.customization.model.Provider;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class PortCustomizationImpl extends CustomizationComponentImpl
        implements PortCustomization{
    
    /** Creates a new instance of PortCustomizationImpl */
    public PortCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PortCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }
    
    public void setJavaMethod(JavaMethod method) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(JavaMethod.class, JAVA_METHOD_PROPERTY, method,
                classes);
    }
    
    public JavaMethod getJavaMethod() {
        return getChild(JavaMethod.class);
    }
    
    public void removeJavaMethod(JavaMethod method) {
        removeChild(JAVA_METHOD_PROPERTY, method);
    }
    
    public void setProvider(Provider provider) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Provider.class, PROVIDER_PROPERTY, provider,
                classes);
    }
    
    public void removeProvider(Provider provider) {
        removeChild(PROVIDER_PROPERTY, provider);
    }
    
    public Provider getProvider() {
        return getChild(Provider.class);
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
