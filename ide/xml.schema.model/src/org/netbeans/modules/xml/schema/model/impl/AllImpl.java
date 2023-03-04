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

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Occur;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This class implements the xml schema all type. The all
 * type describes an unordered group of elements.
 *
 * @author nn136682
 */
public class AllImpl extends SchemaComponentImpl implements All {
    
    public AllImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.ALL, model));
    }
    
    /** Creates a new instance of AllImpl */
    public AllImpl(SchemaModelImpl model, Element e) {
	super(model, e);
    }
    
    public Class<? extends SchemaComponent> getComponentType() {
	return All.class;
    }
    
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
    
    protected Class getAttributeType(SchemaAttributes attr) {
	switch(attr) {
	    case MIN_OCCURS:
		return Occur.ZeroOne.class;
	    default:
		return super.getAttributeType(attr);
	}
    }
    
    
    /**
     * @return minimum occurrences, must be 0 <= x <= 1
     */
    public Occur.ZeroOne getMinOccurs() {
	String s = super.getAttribute(SchemaAttributes.MIN_OCCURS);
	return s == null ? null : Util.parse(Occur.ZeroOne.class, s);
    }
    
    /**
     * set the minimum number of occurs.
     * @param occurs must satisfy 0 <= occurs <= 1
     */
    public void setMinOccurs(Occur.ZeroOne occurs) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, occurs);
    }
    
    public Occur.ZeroOne getMinOccursDefault() {
	return Occur.ZeroOne.ONE;
    }
    
    public Occur.ZeroOne getMinOccursEffective() {
	Occur.ZeroOne v = getMinOccurs();
	return v == null ? getMinOccursDefault() : v;
    }
    
    public Collection<LocalElement> getElements() {
	return super.getChildren(LocalElement.class);
    }
    
    public void addElement(LocalElement e) {
	appendChild(ELEMENT_PROPERTY, e);
    }
    
    public void removeElement(LocalElement e) {
	removeChild(ELEMENT_PROPERTY, e);
    }
    
    public Collection<ElementReference> getElementReferences() {
	return super.getChildren(ElementReference.class);
    }
    
    public void addElementReference(ElementReference e) {
	appendChild(ELEMENT_REFERENCE_PROPERTY, e);
    }
    
    public void removeElementReference(ElementReference e) {
	removeChild(ELEMENT_REFERENCE_PROPERTY, e);
    }
    
    public boolean allowsFullMultiplicity() {
	return !(getParent() instanceof GlobalGroup);
    }
}
