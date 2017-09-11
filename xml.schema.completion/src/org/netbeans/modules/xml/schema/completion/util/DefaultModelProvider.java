/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
