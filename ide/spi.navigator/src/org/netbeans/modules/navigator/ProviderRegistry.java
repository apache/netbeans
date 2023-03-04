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

package org.netbeans.modules.navigator;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanel.DynamicRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Storage/lookup of NavigatorPanel providers. Providers are mapped to
 * mime types they support. 
 *
 * @author Dafe Simonek
 */
class ProviderRegistry {
    
    /** folder in layer file system where navigator panels are searched for */
    static final String PANELS_FOLDER = "Navigator/Panels/"; //NOI18N
    /** template for finding all NavigatorPanel instances in lookup */
    private static final Lookup.Template<NavigatorPanel> NAV_PANEL_TEMPLATE = 
            new Lookup.Template<NavigatorPanel>(NavigatorPanel.class);
    
    /** singleton instance */
    private static ProviderRegistry instance;
    
    /** Mapping between mime types and provider instances. Note that 
     * Collections.EMPTY_LIST serves as special value telling us that
     * we already searched for providers for specific content type and found
     * no providers. This ensures no useless repetitive searches. 
     */
    private Map<String, Collection<? extends NavigatorPanel>> contentTypes2Providers;
    private Map<FileObject, Reference<Collection<? extends NavigatorPanel>>> file2Providers;


    /** Singleton, no external instantiation */
    private ProviderRegistry () {
    }

    /********* public area *********/
    
    public static ProviderRegistry getInstance () {
        if (instance == null ) {
            instance = new ProviderRegistry();
        }
        return instance;
    }
    
    /** Finds appropriate providers for given data content type
     * (similar to mime type)
     * and returns list of provider classes.
     *
     * @return Collection of providers, which implements NavigatorPanel interface.
     * Never return null, only empty List if no provider exists for given content type.
     */
    public Collection<? extends NavigatorPanel> getProviders (String contentType, FileObject file) {
        if (contentTypes2Providers == null) {
            contentTypes2Providers = new HashMap<String, Collection<? extends NavigatorPanel>>(15);
        }
        Collection<? extends NavigatorPanel> result = contentTypes2Providers.get(contentType);
        if (result == null) {
            // load and instantiate provider classes
            result = loadProviders(contentType);
            contentTypes2Providers.put(contentType, result);
        }
        if (file != null) {
            if (file2Providers == null) {
                file2Providers = new WeakHashMap<>();
            }
            URI uri = file.toURI();
            Reference<Collection<? extends NavigatorPanel>> fileResultRef = file2Providers.computeIfAbsent(file, f ->
                    new SoftReference<>(Lookup.getDefault().lookupAll(DynamicRegistration.class).stream().flatMap(reg -> reg.panelsFor(uri).stream()).collect(Collectors.toList()))
            );
            Collection<? extends NavigatorPanel> fileResult = fileResultRef != null ? fileResultRef.get() : null;
            if (result == null) return fileResult;
            if (fileResult == null) return result;
            List<NavigatorPanel> panels = new ArrayList<>();
            panels.addAll(result);
            panels.addAll(fileResult);
            return panels;
        } else {
            return result;
        }
    }
    
    /******* private stuff ***********/

    
    /** Returns collection of NavigatorPanels or empty collection if no provider
     * exist for given content type
     */
    private Collection<? extends NavigatorPanel> loadProviders (String contentType) {
        String path = PANELS_FOLDER + contentType;

        Lookup.Result<NavigatorPanel> result = Lookups.forPath(path).lookup(NAV_PANEL_TEMPLATE);

        return result.allInstances();
    }

}
