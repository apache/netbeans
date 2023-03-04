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

import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class RedefineImpl extends SchemaComponentImpl implements Redefine {
	
        public RedefineImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.REDEFINE,model));
        }
	/**
     * Creates a new instance of RedefineImpl
     */
	public RedefineImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Redefine.class;
	}
	
	/**
	 *
	 */
	public void setSchemaLocation(String uri) {
		setAttribute(SCHEMA_LOCATION_PROPERTY, SchemaAttributes.SCHEMA_LOCATION, uri);
	}
	
	/**
	 *
	 */
	public void addComplexType(GlobalComplexType type) {
		appendChild(COMPLEX_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void removeComplexType(GlobalComplexType type) {
		removeChild(COMPLEX_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void addAttributeGroup(GlobalAttributeGroup group) {
		appendChild(ATTRIBUTE_GROUP_PROPERTY, group);
	}
	
	/**
	 *
	 */
	public void removeAttributeGroup(GlobalAttributeGroup group) {
		removeChild(ATTRIBUTE_GROUP_PROPERTY, group);
	}
	
	/**
	 *
	 */
	public void removeSimpleType(GlobalSimpleType type) {
		removeChild(SIMPLE_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void addSimpleType(GlobalSimpleType type) {
		appendChild(SIMPLE_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void accept(SchemaVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 *
	 */
	public void addGroupDefinition(GlobalGroup def) {
		appendChild(GROUP_DEFINITION_PROPERTY, def);
	}
	
	/**
	 *
	 */
	public void removeGroupDefinition(GlobalGroup def) {
		removeChild(GROUP_DEFINITION_PROPERTY, def);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalAttributeGroup> getAttributeGroups() {
		return getChildren(GlobalAttributeGroup.class);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalComplexType> getComplexTypes() {
		return getChildren(GlobalComplexType.class);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalGroup> getGroupDefinitions() {
		return getChildren(GlobalGroup.class);
	}
	
	/**
	 *
	 */
	public String getSchemaLocation() {
		   return getAttribute(SchemaAttributes.SCHEMA_LOCATION);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalSimpleType> getSimpleTypes() {
		return getChildren(GlobalSimpleType.class);
	}
	
	public SchemaModel resolveReferencedModel() throws CatalogModelException {
	    ModelSource ms = resolveModel(getSchemaLocation());
        return SchemaModelFactory.getDefault().getModel(ms);
	}

    @Override
    public String toString() {
        return getModel().toString() + " --redefine--> " + getSchemaLocation(); // NOI18N
    }

}
