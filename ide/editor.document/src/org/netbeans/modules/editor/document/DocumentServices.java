/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Maintains a cache of {@link DocumentServiceFactory}s for individual implementation
 * classes.
 * @author sdedic
 */
public class DocumentServices {
    private static volatile DocumentServices INSTANCE;
    
    private Map<Class<?>, Lookup.Result<DocumentServiceFactory<?>>> factoryMap = new HashMap<>(5);
    
    public static DocumentServices getInstance() {
        if (INSTANCE == null) {
            synchronized (DocumentServices.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DocumentServices();
                }
            }
        }
        return INSTANCE;
    }
    
    public void initDocumentServices(Document doc) {
        doInitDocumentServices(doc, doc.getClass());
    }
    
    //@GuardedBy(this)
    private Lookup.Result<DocumentServiceFactory<?>> initDocumentFactories(Class<?> c) {
        List<Lookup> lkps = new ArrayList<Lookup>(5);
        do {
            String cn = c.getCanonicalName();
            if (cn != null) {
                lkps.add(Lookups.forPath("Editors/Documents/" + cn)); // NOI18N
            }
            c = c.getSuperclass();
        } while (c != null && c != java.lang.Object.class);
        Lookup[] arr = lkps.toArray(new Lookup[0]);
        @SuppressWarnings("rawtypes")
        Lookup.Result lookupResult = new ProxyLookup(arr).lookupResult(DocumentServiceFactory.class);
        @SuppressWarnings("unchecked")
        Lookup.Result<DocumentServiceFactory<?>> res = (Lookup.Result<DocumentServiceFactory<?>>) lookupResult;
        return res;
    }
    
    
    
    private <D extends Document> Lookup doInitDocumentServices(D doc, Class<?> c) {
        boolean stub = c != doc.getClass();
        Object k = stub ? STUB_KEY : DocumentServices.class;
        Lookup res;

        res = (Lookup)doc.getProperty(k);
        if (res != null) {
            return res;
        }
        Lookup.Result<DocumentServiceFactory<?>> factories;
        
        synchronized (this) {
            factories = factoryMap.get(c);
            if (factories == null) {
                factories = initDocumentFactories(c);
                factoryMap.put(c, factories);
            }
        }
        Collection<? extends DocumentServiceFactory<?>> col = factories.allInstances();
        Collection<Lookup> lkps = new ArrayList<Lookup>(col.size());
        for (DocumentServiceFactory<?> f : col) {
            try {
                @SuppressWarnings("unchecked")
                Lookup l = ((DocumentServiceFactory<D>)f).forDocument(doc);
                if (l == null) {
                    continue;
                }
                lkps.add(l);
            } catch (Exception ex) {
            }
        }
        res = new ProxyLookup(lkps.toArray(new Lookup[0]));
        doc.putProperty(k, res);
        
        return res;
    }
    
    private static final Object STUB_KEY = DocumentServices.class.getName() + ".stub"; // NOI18N
    
    public Lookup getStubLookup(Document doc) {
        return doInitDocumentServices(doc, Document.class);
    }
    
    public Lookup getLookup(Document doc) {
        return doInitDocumentServices(doc, doc.getClass());
    }
}
