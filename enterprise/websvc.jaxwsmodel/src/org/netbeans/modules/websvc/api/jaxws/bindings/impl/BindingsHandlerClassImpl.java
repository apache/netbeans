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
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;

import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerClassImpl extends BindingsComponentImpl implements BindingsHandlerClass{
    
    /**
     * Creates a new instance of BindingsHandlerClassImpl
     */
    public BindingsHandlerClassImpl(BindingsModelImpl model, Element element) {
        super(model, element);
    }
    
    public BindingsHandlerClassImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.HANDLER_CLASS.getQName(), model));
    }

    public void setClassName(String name) {
        setText(HANDLER_CLASS_NAME_PROPERTY, name);
    }


    public String getClassName() {
        return getText();
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }
}
