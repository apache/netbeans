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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.lookup.Lookups;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.class)
public class MetaSchemaModelProvider extends CompletionModelProvider {
    
    private CompletionContextImpl context;
    
    public MetaSchemaModelProvider() {
    }

    /**
     * Returns a list of CompletionModel. Default implementation looks for
     * schemaLocation attribute in the document and if specified creates model
     * for each schema mentioned in there.
     */    
    public List<CompletionModel> getModels(CompletionContext context) {
        if(context == null ||
           context.getPrimaryFile() == null ||
           context.getPrimaryFile().getExt() == null ||
           !"xsd".equals(context.getPrimaryFile().getExt())) //NOI18N
            return null;
        SchemaModel sm = createMetaSchemaModel();
        if(sm == null)
            return null;        
        CompletionModel cm = new CompletionModelEx((CompletionContextImpl)context, "xsd", sm); //NOI18N
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        models.add(cm);
        return models;
    }
    
    private SchemaModel createMetaSchemaModel() {
        try {
            InputStream in = getClass().getResourceAsStream("XMLSchema.xsd"); //NOI18N
            try {
                javax.swing.text.Document d = AbstractDocumentModel.
                        getAccessProvider().loadSwingDocument(in);
                ModelSource ms = new ModelSource(Lookups.singleton(d), false);
                SchemaModel m = SchemaModelFactory.getDefault().createFreshModel(ms);
                m.sync();
                return m;
            } finally {
                in.close();
            }
        } catch (Exception ex) {
            //just catch
        } 
        return null;
    }
        
}
