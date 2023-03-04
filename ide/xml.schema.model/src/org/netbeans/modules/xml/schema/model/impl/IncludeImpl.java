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

import org.netbeans.modules.xml.schema.model.Include;
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
 * @author rico
 */
public class IncludeImpl extends SchemaComponentImpl implements Include{
    
    protected IncludeImpl(SchemaModelImpl model){
	this(model, createNewComponent(SchemaElements.INCLUDE, model));
    }
    
    public IncludeImpl(SchemaModelImpl model, Element el){
	super(model,el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return Include.class;
    }
    
    /**
     * Visitor
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
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
    public String getSchemaLocation() {
	return getAttribute(SchemaAttributes.SCHEMA_LOCATION);
    }
    
    public SchemaModel resolveReferencedModel() throws CatalogModelException {
        ModelSource ms = resolveModel(getSchemaLocation());
        return SchemaModelFactory.getDefault().getModel(ms);
    }

    @Override
    public String toString() {
        return getModel().toString() + " --include--> " + getSchemaLocation(); // NOI18N
    }

}
