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

package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.openide.loaders.XMLDataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implements non-persistent catalog functionality as EntityResolver.
 * <p>Registations using this resolver are:
 * <li>transient
 * <li>of the hihgest priority
 * <li>last registration prevails
 * @see XMLDataObject#registerCatalogEntry(String,String)
 * @see XMLDataObject#registerCatalogEntry(String,String,ClassLoader)
 */
@ServiceProviders({
    @ServiceProvider(service=EntityCatalog.class),
    @ServiceProvider(service=RuntimeCatalog.class)
})
@Deprecated
public final class RuntimeCatalog extends EntityCatalog {

    // table mapping public IDs to (local) URIs
    private Map<String,String> id2uri;
    
    // tables mapping public IDs to resources and classloaders
    private Map<String,String> id2resource;
    private Map<String,ClassLoader> id2loader;
    
    public @Override InputSource resolveEntity(String name, String systemId) throws IOException, SAXException {
        
        InputSource retval;
        String mappedURI = name2uri(name);
        InputStream stream = mapResource(name);
        
        // prefer explicit URI mappings, then bundled resources...
        if (mappedURI != null) {
            retval = new InputSource(mappedURI);
            retval.setPublicId(name);
            return retval;
            
        } else if (stream != null) {
            retval = new InputSource(stream);
            retval.setPublicId(name);
            return retval;
            
        } else {
            return null;
        }
    }
    
    public void registerCatalogEntry(String publicId, String uri) {
        if (id2uri == null) {
            id2uri = new HashMap<String,String>();
        }
        id2uri.put(publicId, uri);
    }
    
    /** Map publicid to a resource accessible by a classloader. */
    public void registerCatalogEntry(String publicId, String resourceName, ClassLoader loader) {
        if (id2resource == null) {
            id2resource = new HashMap<String,String>();
        }
        id2resource.put(publicId, resourceName);
        
        if (loader != null) {
            if (id2loader == null) {
                id2loader = new HashMap<String,ClassLoader>();
            }
            id2loader.put(publicId, loader);
        }
    }
    
    // maps the public ID to an alternate URI, if one is registered
    private String name2uri(String publicId) {
        
        if (publicId == null || id2uri == null) {
            return null;
        }
        return id2uri.get(publicId);
    }
    
    
    // return the resource as a stream
    private InputStream mapResource(String publicId) {
        if (publicId == null || id2resource == null) {
            return null;
        }
        
        String resourceName = id2resource.get(publicId);
        ClassLoader loader = null;
        
        if (resourceName == null) {
            return null;
        }
        
        if (id2loader != null) {
            loader = id2loader.get(publicId);
        }
        
        if (loader == null) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        }
        return loader.getResourceAsStream(resourceName);
    }
    
}
