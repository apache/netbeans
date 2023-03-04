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

import java.util.Collections;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerImpl extends BindingsComponentImpl implements BindingsHandler {

    /**
     * Creates a new instance of BindingsHandlerImpl
     */
    public BindingsHandlerImpl(BindingsModelImpl model, Element element) {
        super(model, element);
    }

    public BindingsHandlerImpl(BindingsModelImpl model) {
        this(model, createPrefixedElement(BindingsQName.HANDLER.getQName(), model));
    }

    public void setHandlerClass(BindingsHandlerClass handlerClass) {
        java.util.List<Class<? extends BindingsComponent>> classes = Collections.emptyList();
        setChild(BindingsHandlerClass.class, HANDLER_CLASS_PROPERTY, handlerClass,
                classes);
    }

    public void removeHandlerClass(BindingsHandlerClass handlerClass) {
        removeChild(HANDLER_CLASS_PROPERTY, handlerClass);
    }

    public BindingsHandlerClass getHandlerClass() {
        return getChild(BindingsHandlerClass.class);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }

    public void setHandlerName(BindingsHandlerName handlerName) {
        java.util.List<Class<? extends BindingsComponent>> names = Collections.emptyList();
        setChild(BindingsHandlerName.class, HANDLER_NAME_PROPERTY, handlerName,
                names);
    }

    public BindingsHandlerName getHandlerName() {
        return getChild(BindingsHandlerName.class);
    }
}
