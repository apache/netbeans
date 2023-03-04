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

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public class SequenceImpl extends SchemaComponentImpl
    implements Sequence, Cardinality {

    public SequenceImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.SEQUENCE,model));
    }

    public SequenceImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }

    public void removeContent(SequenceDefinition definition) {
	removeChild(CONTENT_PROPERTY, definition);
    }
    
    public void addContent(SequenceDefinition definition, int position) {
	insertAtIndex(CONTENT_PROPERTY, definition, position,
	    SequenceDefinition.class);
    }
    
    public void appendContent(SequenceDefinition definition) {
	appendChild(CONTENT_PROPERTY, definition);
    }
    
    public java.util.List<SequenceDefinition> getContent() {
	return getChildren(SequenceDefinition.class);
    }
    
    public Class<? extends SchemaComponent> getComponentType() {
	return Sequence.class;
    }
    
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
    public void setMinOccurs(Integer min) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    public void setMaxOccurs(String max) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
    
    public Integer getMinOccurs() {
	String s = getAttribute(SchemaAttributes.MIN_OCCURS);
	return s == null ? null : Integer.valueOf(s);
    }
    
    public String getMaxOccurs() {
	return getAttribute(SchemaAttributes.MAX_OCCURS);
    }
    
    public int getMinOccursEffective() {
	Integer v = getMinOccurs();
	return v == null ? getMinOccursDefault() : v;
    }
    
    public int getMinOccursDefault() {
	return 1;
    }
    
    public String getMaxOccursEffective() {
	String v = getMaxOccurs();
	return v == null ? getMaxOccursDefault() : v;
    }
    
    public String getMaxOccursDefault() {
	return String.valueOf(1);
    }
    
    public Cardinality getCardinality() {
	return getParent() instanceof GlobalGroup ? null: this;
    }

}
