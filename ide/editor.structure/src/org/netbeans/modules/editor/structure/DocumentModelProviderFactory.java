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

package org.netbeans.modules.editor.structure;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Document model factory that obtains the DocumentModel instancies
 * by reading the xml layer.
 * <br>
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/DocumentModel
 * </pre>
 *
 * @author Marek Fukala
 */
public class DocumentModelProviderFactory {
    
    public static final String FOLDER_NAME = "DocumentModel"; //NOI18N
    
    private Map<String, DocumentModelProvider> mime2provider;
    
    private static DocumentModelProviderFactory defaultProvider = null;
    
    public static DocumentModelProviderFactory getDefault() {
        if(defaultProvider == null) {
            defaultProvider = new DocumentModelProviderFactory();
        }
        return defaultProvider;
    }
    
    private DocumentModelProviderFactory() {
        mime2provider = new WeakHashMap<String, DocumentModelProvider>();
    }
    
    /* returns a DocumentModelFactory according to the layer */
    public DocumentModelProvider getDocumentModelProvider(String mimeType) {
        DocumentModelProvider provider = null; // result
        if(mimeType != null) {
            provider = mime2provider.get(mimeType);
            if (provider == null) { // not cached yet
                Lookup mimeLookup = MimeLookup.getLookup(MimePath.get(mimeType));
                Collection<? extends DocumentModelProvider> providers = 
                        mimeLookup.lookup(new Lookup.Template<DocumentModelProvider>(DocumentModelProvider.class)).allInstances();
                if(providers.size() > 1)
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Only one DocumentModelProvider can be registered for one mimetype!");
                
                if(providers.size() == 0)
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("There isn't any DocumentModelProvider registered for " + mimeType + " mimetype!"));
                
                provider = providers.size() > 0 ? (DocumentModelProvider)providers.iterator().next() : null;
                mime2provider.put(mimeType, provider);
                
            } else return provider;
        } else
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("mimeType cannot be null!"));
        
        return provider;
    }
    
    
}
