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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public class ComplexContentRestrictionImpl extends SchemaComponentImpl implements ComplexContentRestriction{
    
    /** Creates a new instance of ComplexContentRestrictionImpl */
    protected ComplexContentRestrictionImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.RESTRICTION, model));
    }
    
    public ComplexContentRestrictionImpl(SchemaModelImpl model, Element el){
	super(model,el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return ComplexContentRestriction.class;
    }
    
    public void setBase(NamedComponentReference<GlobalComplexType> type) {
	setAttribute(BASE_PROPERTY, SchemaAttributes.BASE, type);
	//setAttribute(SchemaAttributes.BASE, type.getRawURI());
    }
    
    public NamedComponentReference<GlobalComplexType> getBase() {
	return resolveGlobalReference(GlobalComplexType.class, SchemaAttributes.BASE);
    }
    
    public void addAttributeGroupReference(AttributeGroupReference ref) {
	List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(ComplexTypeDefinition.class);
	
	addAfter(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref, list);
    }
    
    public void removeAttributeGroupReference(AttributeGroupReference ref) {
	removeChild(ATTRIBUTE_GROUP_REFERENCE_PROPERTY, ref);
    }
    
    public Collection<AttributeGroupReference> getAttributeGroupReferences() {
	return getChildren(AttributeGroupReference.class);
    }
    
    public void setDefinition(ComplexTypeDefinition definition) {
	Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	setChild(ComplexTypeDefinition.class,
		DEFINITION_CHANGED_PROPERTY, definition, list);
    }
    
    public ComplexTypeDefinition getDefinition() {
	Collection<ComplexTypeDefinition> elements = getChildren(ComplexTypeDefinition.class);
	if(!elements.isEmpty()){
	    return elements.iterator().next();
	}
	return null;
    }
    
    public void addLocalAttribute(LocalAttribute attr) {
	List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(ComplexTypeDefinition.class);
	
	addAfter(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public void removeLocalAttribute(LocalAttribute attr) {
	removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public Collection<LocalAttribute> getLocalAttributes() {
	return getChildren(LocalAttribute.class);
    }
    
     public void addAttributeReference(AttributeReference attr) {
	List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(ComplexTypeDefinition.class);
	
	addAfter(LOCAL_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public void removeAttributeReference(AttributeReference attr) {
	removeChild(LOCAL_ATTRIBUTE_PROPERTY, attr);
    }
    
    public Collection<AttributeReference> getAttributeReferences() {
	return getChildren(AttributeReference.class);
    }
    
    public void setAnyAttribute(AnyAttribute attr) {
	Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(LocalGroupDefinition.class);
	list.add(All.class);
	list.add(Choice.class);
	list.add(Sequence.class);
	setChild(AnyAttribute.class, ANY_ATTRIBUTE_PROPERTY, attr, list);
    }
    
    public AnyAttribute getAnyAttribute() {
	Collection<AnyAttribute> elements = getChildren(AnyAttribute.class);
	if(!elements.isEmpty()){
	    return elements.iterator().next();
	}
	//TODO should we throw exception if there is no definition?
	return null;
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
}
