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

import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.EmbeddableRoot.ForeignParent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class ImportImpl extends SchemaComponentImpl implements Import {
	
        public ImportImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.IMPORT,model));
        }
    
	/**
	 * Creates a new instance of ImportImpl
	 */
	public ImportImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Import.class;
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
	public void setSchemaLocation(String uri) {
		setAttribute(SCHEMA_LOCATION_PROPERTY, SchemaAttributes.SCHEMA_LOCATION, uri);
	}
	
	/**
	 *
	 */
	public void setNamespace(String uri) {
		setAttribute(NAMESPACE_PROPERTY, SchemaAttributes.NAMESPACE, uri);
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
	public String getNamespace() {
		   return getAttribute(SchemaAttributes.NAMESPACE);
	}

	public SchemaModel resolveReferencedModel() throws CatalogModelException {
            SchemaModel result = resolveEmbeddedReferencedModel();
            if (result == null) {
                ModelSource ms = resolveModel(getSchemaLocation());
		result = SchemaModelFactory.getDefault().getModel(ms);
            }
            return result;
	}
	
    protected SchemaModel resolveEmbeddedReferencedModel() {
        if (getNamespace() == null) {
            return null;
        }
        if (! (getModel().getSchema().getForeignParent() instanceof ForeignParent)) {
            return null;
        }
        ForeignParent fr = (ForeignParent) getModel().getSchema().getForeignParent();
        if (fr == null) return null;
        for (EmbeddableRoot embedded : fr.getAdoptedChildren()) {
            if (embedded instanceof Schema && embedded != getModel().getSchema()) {
                Schema es = (Schema) embedded;
                if (getNamespace().equals(es.getTargetNamespace())) {
                    return es.getModel();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getModel().toString() + " --import--> " + getSchemaLocation(); // NOI18N
    }

}
