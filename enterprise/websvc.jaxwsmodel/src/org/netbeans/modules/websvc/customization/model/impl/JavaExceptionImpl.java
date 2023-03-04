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
 * JavaExceptionImpl.java
 *
 * Created on February 7, 2006, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.api.customization.model.JavaClass;
import org.netbeans.modules.websvc.api.customization.model.JavaException;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class JavaExceptionImpl extends CustomizationComponentImpl
     implements JavaException{
    
    /** Creates a new instance of JavaExceptionImpl */
    public JavaExceptionImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JavaExceptionImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }

    public void setPart(String part) {
        setAttribute(PART_PROPERTY, CustomizationAttribute.PART, part);
    }

    public void setJavaClass(JavaClass javaClass) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        this.setChild(JavaClass.class, this.JAVA_CLASS_PROPERTY, javaClass, classes);
 
    }

    public void removeJavaClass(JavaClass javaClass) {     
        removeChild(JAVA_CLASS_PROPERTY, javaClass);
    }

    public String getPart() {
        return getAttribute(CustomizationAttribute.PART);
    }

    public JavaClass getJavaClass() {
        return getChild(JavaClass.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
