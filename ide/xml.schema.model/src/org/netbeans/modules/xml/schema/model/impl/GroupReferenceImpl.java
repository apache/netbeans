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
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public class GroupReferenceImpl extends SchemaComponentImpl
	implements GroupReference {
    /**
     *
     */
    public GroupReferenceImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.GROUP,model));
    }

    /**
     *
     */
    public GroupReferenceImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GroupReference.class;
	}
    
    /**
     *
     */
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
    /**
     *
     */
    public void setMinOccurs(Integer min) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    /**
     *
     */
    public void setMaxOccurs(String max) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
    
    /**
     *
     */
    public void setRef(NamedComponentReference<GlobalGroup> def) {
        setAttribute(REF_PROPERTY, SchemaAttributes.REF, def);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalGroup> getRef() {
	return resolveGlobalReference(GlobalGroup.class, 
		SchemaAttributes.REF);
    }
    
    /**
     *
     */
    public Integer getMinOccurs() {
	String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Integer.valueOf(s);
    }
    
    public int getMinOccursEffective() {
        Integer v = getMinOccurs();
        return v == null ? getMinOccursDefault() : v;
    }

    public int getMinOccursDefault() {
        return 1;
    }

    /**
     *
     */
    public String getMaxOccurs() {
	return getAttribute(SchemaAttributes.MAX_OCCURS);
    }

    public String getMaxOccursEffective() {
        String s = getMaxOccurs();
        return s == null ? getMaxOccursDefault() : s;
    }

    public String getMaxOccursDefault() {
        return String.valueOf(1);
    }
}
