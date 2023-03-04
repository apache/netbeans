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

package org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl;

import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.impl.ImportImpl;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaReferenceImpl<T extends ReferenceableSchemaComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of SchemaReferenceImpl */
    public SchemaReferenceImpl(
            T referenced, 
            Class<T> type, 
            AbstractDocumentComponent parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public SchemaReferenceImpl(Class<T> type, AbstractDocumentComponent parent, String refString){
        super(type, parent, refString);
    }

    public T get() {
        if (getReferenced() == null) {
            String localName = getLocalName();
            String namespace = getEffectiveNamespace();
            T target = null;

            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
                SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
                target = primitiveModel.resolve(namespace, localName, getType());
            } else {
                Types types = getParent().getModel().getDefinitions().getTypes();
                if (types != null) {
                    for (Schema s : types.getSchemas()) {
                        target = s.getModel().resolve(namespace, localName, getType());
                        if (target != null) {
                            break;
                        }
                    }
                }
                if (target == null) {
                    for (Import i : getParent().getModel().getDefinitions().getImports()) {
                        DocumentModel m = null;
                        try {
                            m = ((ImportImpl)i).resolveImportedModel();
                        } catch(CatalogModelException ex) {
                            // checked for null so ignore
                        }
                        if (m instanceof SchemaModel) {
                            target = ((SchemaModel)m).resolve(namespace, localName, getType());
                        }
                        if (target != null) {
                            break;
                        }
                    }
                }
            }
            
            if (target != null) {
                setReferenced(target);
            }
        }
        return getReferenced();
    }

    public WSDLComponentBase getParent() {
        return (WSDLComponentBase) super.getParent();
    }
    
    public String getEffectiveNamespace() {
        if (refString == null) {
            assert getReferenced() != null;
            return getReferenced().getModel().getSchema().getTargetNamespace();
        } else {
            if (getPrefix() == null) {
                return null;
            } else {
                return getParent().lookupNamespaceURI(getPrefix());
            }
        }
    }
}
