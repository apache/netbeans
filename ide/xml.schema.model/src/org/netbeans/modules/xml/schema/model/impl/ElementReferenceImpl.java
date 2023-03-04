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

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class ElementReferenceImpl extends LocalElementBaseImpl
    implements ElementReference {

    public ElementReferenceImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of LocalElementImpl
     */
    public ElementReferenceImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
    
    public Class<? extends SchemaComponent> getComponentType() {
	return ElementReference.class;
    }
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
    public boolean allowsFullMultiplicity() {
	return !(getParent() instanceof All);
    }
    
    @Override
    public String toString() {
        NamedComponentReference<GlobalElement> gElementRef = this.getRef();
        if (gElementRef != null) {
            GlobalElement gElement = gElementRef.get();
            if (gElement != null) {
                String name = gElement.getName();
                return "-> " + name; // NOI18N
            }
        }
        return null;
    }
}
