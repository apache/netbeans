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
package org.netbeans.modules.xml.schema.model.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.openide.util.NbBundle;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author Nam Nguyen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class SchemaXsdBasedValidator extends XsdBasedValidator {
    
    private static Schema schema;
    
    protected Schema getSchema(Model model) {
        if (! (model instanceof SchemaModel)) {
            return null;
        }
        
        // This will not be used as validate(.....) method is being overridden here.
        // So just return a schema returned by newSchema().
        if(schema == null) {
            try {
                schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema();
            } catch(SAXException ex) {
                assert false: "Error while creating compiled schema for"; //NOI18N
            }
        }
        return schema;
    }
    
    public String getName() {
        return NbBundle.getMessage(SchemaXsdBasedValidator.class, "LBL_Schema_Validator");
    }
    
    @Override
    protected void validate(Model model, Schema schema, XsdBasedValidator.Handler handler) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            CatalogModel cm = (CatalogModel) model.getModelSource().getLookup()
		.lookup(CatalogModel.class);
	    if (cm != null) {
                sf.setResourceResolver(cm);
            }
            sf.setErrorHandler(handler);
            Source saxSource = getSource(model, handler);
            if (saxSource == null) {
                return;
            }
            sf.newSchema(saxSource);
        } catch(SAXException sax) {
            //already processed by handler
        } catch(Exception ex) {
            handler.logValidationErrors(Validator.ResultType.ERROR, ex.getMessage());
        }
    }
    
    public DocumentModel resolveResource(String systemId, Model currentModel) {
        
        try {
            CatalogModel cm = (CatalogModel) currentModel.getModelSource().getLookup()
            .lookup(CatalogModel.class);
            ModelSource ms = cm.getModelSource(new URI(systemId));
            if (ms != null) {
                return SchemaModelFactory.getDefault().getModel(ms);
            }
        } catch(URISyntaxException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        } catch(CatalogModelException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        }
        return null;
    }
    
}
