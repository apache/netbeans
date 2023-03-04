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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Locale;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class ImportImpl extends WSDLComponentBase implements Import {
    
    /** Creates a new instance of ImportImpl */
    public ImportImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ImportImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.IMPORT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setNamespace(String namespaceURI) {
        setAttribute(NAMESPACE_URI_PROPERTY, WSDLAttribute.NAMESPACE_URI, namespaceURI);
    }

    public void setLocation(String locationURI) {
        setAttribute(LOCATION_PROPERTY, WSDLAttribute.LOCATION, locationURI);
    }

    public String getNamespace() {
        return getAttribute(WSDLAttribute.NAMESPACE_URI);
    }

    public String getLocation() {
        return getAttribute(WSDLAttribute.LOCATION);
    }

    public WSDLModel getImportedWSDLModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof WSDLModel) {
            return (WSDLModel) m;
        } else {
            String msg = NbBundle.getMessage(ImportImpl.class, "MSG_CANNOT_LOAD_WSDL", getLocation());
            throw new CatalogModelException(msg);
        }
    }
    
    public WSDLModel resolveToWSDLModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof WSDLModel) {
            return (WSDLModel) m;
        } else {
            return null;
        }
    }
    
    public SchemaModel resolveToSchemaModel() throws CatalogModelException {
        DocumentModel m = resolveImportedModel();
        if (m instanceof SchemaModel) {
            return (SchemaModel) m;
        } else {
            return null;
        }
    }
    
    public DocumentModel resolveImportedModel() throws CatalogModelException {
        ModelSource ms = resolveModel(getLocation());
        
        String location = getLocation().toLowerCase(Locale.US);
        if (location.endsWith(".wsdl")) { //NOI18N
            return loadAsWSDL(ms);
        } else if (location.endsWith(".xsd")) { //NOI18N
            return loadAsSchema(ms);
        } else {
            DocumentModel m = loadAsWSDL(ms);
            if (m == null) {
                m = loadAsSchema(ms);
            }
            return m;
        }
    }
    
    private WSDLModel loadAsWSDL(ModelSource ms) {
        WSDLModel m = WSDLModelFactory.getDefault().getModel(ms);
        if (m != null && m.getState() == DocumentModel.State.NOT_WELL_FORMED) {
            return null;
        }
        return m;
    }
    
    private SchemaModel loadAsSchema(ModelSource ms) {
        SchemaModel m = SchemaModelFactory.getDefault().getModel(ms);
        if (m != null && m.getState() == DocumentModel.State.NOT_WELL_FORMED) {
            return null;
        }
        return m;
    }
}
