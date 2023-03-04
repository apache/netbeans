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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class NotationImpl extends NamedImpl implements Notation {

    /** Creates a new instance of NotationImpl */
    public NotationImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.DOCUMENTATION,model));
    }
    
    /**
     * Creates a new instance of DocumentationImpl
     */
    public NotationImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

    public void setSystemIdentifier(String systemID) {
        setAttribute(SYSTEM_PROPERTY, SchemaAttributes.SYSTEM, systemID);
    }

    public String getSystemIdentifier() {
        return getAttribute(SchemaAttributes.SYSTEM);
    }

    public void setPublicIdentifier(String publicID) {
        setAttribute(SYSTEM_PROPERTY, SchemaAttributes.PUBLIC, publicID);
    }

    public String getPublicIdentifier() {
        return getAttribute(SchemaAttributes.PUBLIC);
    }

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public Class<? extends SchemaComponent> getComponentType() {
        return Notation.class;
    }
    
}
