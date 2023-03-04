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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.class)
public class DefaultModelProvider extends CompletionModelProvider {
    
    public DefaultModelProvider() {        
    }

    /**
     * Returns a list of CompletionModel. Default implementation looks for
     * schemaLocation attribute in the document and if specified creates model
     * for each schema mentioned in there.
     */    
    public synchronized List<CompletionModel> getModels(CompletionContext context) {
        if(context.getPrimaryFile() == null)
            return null;
        CompletionContextImpl contextImpl = (CompletionContextImpl)context;
        List<URI> uris = contextImpl.getSchemas();
        if(uris.isEmpty())
            return null;
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        for(URI uri : uris) {
            CompletionModel model = getCompletionModel(uri, true, contextImpl);
            if(model != null)
                models.add(model);
        }
        
        return models;
    }
    
    static CompletionModel getCompletionModel(URI schemaURI, boolean fetch, CompletionContextImpl context) {
        CompletionModel model = null;
        try {
            ModelSource modelSource = null;
            CatalogModel catalogModel = null;
            CatalogModelProvider catalogModelProvider = getCatalogModelProvider();
            if(catalogModelProvider == null) {
                modelSource = Utilities.getModelSource(context.getPrimaryFile(), true);
                CatalogModelFactory factory = CatalogModelFactory.getDefault();
                catalogModel = factory.getCatalogModel(modelSource);
            } else {
                //purely for unit testing purposes.
                modelSource = catalogModelProvider.getModelSource(context.getPrimaryFile(), true);
                catalogModel = catalogModelProvider.getCatalogModel();
            }
            //add special query params in the URI to be consumed by the CatalogModel.
            String uriString = schemaURI.toString();
            String addParams =  "fetch="+fetch+"&sync=true";
            int index = uriString.indexOf('?');
            if (index > -1) {
                uriString = uriString.substring(0, index + 1) + addParams + '&' + uriString.substring(index + 1);
            } else {
                index = uriString.indexOf('#');
                if (index  > -1) {
                    uriString = uriString.substring(0, index) + '?' + 
                            addParams + uriString.substring(index);
                } else {
                    uriString += '?' + addParams;
                }
            }
            
            URI uri = new URI(uriString);
            ModelSource schemaModelSource = catalogModel.getModelSource(uri, modelSource);
            SchemaModel sm = null;
            if(schemaModelSource.getLookup().lookup(FileObject.class) == null) {
                sm = SchemaModelFactory.getDefault().createFreshModel(schemaModelSource);
            } else {
                sm = SchemaModelFactory.getDefault().getModel(schemaModelSource);
            }
            String tns = sm.getSchema().getTargetNamespace();
            List<String> prefixes = CompletionUtil.getPrefixesAgainstNamespace(
                    context, tns);
            if(prefixes != null && prefixes.size() > 0)
                model = new CompletionModelEx(context, prefixes.get(0), sm);
            else
                model = new CompletionModelEx(context, context.suggestPrefix(tns), sm);
        } catch (Exception ex) {
            //no model for exception
        }
        return model;
    }
    
    /**
     * Uses lookup to find all CatalogModelProvider. If found uses the first one,
     * else returns null. This is purely to solve the problem of not being able to
     * use TestCatalogModel from unit tests.
     *
     * During actual CC from IDE, this will return null.
     */
    private static CatalogModelProvider getCatalogModelProvider() {
        Lookup.Template templ = new Lookup.Template(CatalogModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        if(impls.isEmpty())
            return null;
        
        return (CatalogModelProvider)impls.iterator().next();
    }
    
}
