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
package org.netbeans.modules.xml.catalog.impl;

import java.awt.Image;
import java.lang.reflect.*;
import java.util.*;
import java.io.Serializable;

import org.xml.sax.*;

import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.openide.filesystems.*;

import org.netbeans.modules.xml.catalog.spi.*;
import java.io.IOException;

/**
 * Read mapping redistered in IDE system resolver/catalog.
 * It uses knowledge of IDE catalog implementation.
 *
 * @author  Petr Kuzel
 * @version 1.0
 *
 */
public class SystemCatalogReader implements EntityResolver, CatalogReader, Serializable {

    /** Serial Version UID */
    private static final long serialVersionUID = -6353123780493006631L;
    
    /** Creates new SystemCatalogReader */
    public SystemCatalogReader() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     */
    public Iterator getPublicIDs() {
        
        HashSet set = new HashSet();
        boolean found = false;
        
        // inspect system/xml/entities
        
        FileObject root = FileUtil.getConfigFile("xml/entities");
        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject next = (FileObject) en.nextElement();
            if (next.isData()) {
                Object hint = next.getAttribute("hint.originalPublicID");
                if (hint instanceof String) {
                    set.add(hint);
                    found = true;
                } else {
                    // we could guess it, BUT it is too dangerous
                }
            }
        }
                
        // get instance of system resolver that contains the catalog

        Lookup.Template templ = new Lookup.Template(EntityCatalog.class);
        Lookup.Result res = Lookup.getDefault().lookup(templ);

        Iterator it = res.allInstances().iterator();
        while (it.hasNext()) {                
            EntityCatalog next = (EntityCatalog) it.next();

            try {
                
                //BACKWARD COMPATABILITY it is explicit knowledge how it worked in NetBeans 3.2
                Field uriMapF = next.getClass().getDeclaredField("id2uri");  // NOI18N
                if (uriMapF == null) continue;

                uriMapF.setAccessible(true);
                found = true;

                Map uris = (Map) uriMapF.get(next);
                if (uris != null) {
                   set.addAll(uris.keySet());               
                }
            } catch (NoSuchFieldException ex) {
                // ignore unknown implementation
            } catch (IllegalAccessException ex) {
                // ignore unknown implementation
            } catch (IllegalArgumentException ex) {
                // ignore unknown implementation
            }
        }
        
        return (found == false) ? null : set.iterator();
    }
    
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     */
    public String getSystemID(String publicId) {
        
        try {
            EntityResolver sysResolver = EntityCatalog.getDefault();
            
            if (sysResolver == null) return null;

            InputSource in = sysResolver.resolveEntity(publicId, null);
            if (in == null) return null;
            
            return in.getSystemId();
            
        } catch (java.io.IOException ex) {            
            return null;
        } catch (SAXException ex) {
            return null;
        }
    }

    /**
     * No refresh is necessary, it is always fresh in RAM.
     */
    public void refresh() {
    }
    
   
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @see addCatalogListener
     */
    public void removeCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }

    /*
     * System catalog is singleton.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return getClass().equals(obj.getClass());
    }
    
    public int hashCode() {
        return getClass().hashCode();
    }
    
    /**
     * Delegate to entity catalog to resolve unlisted elements.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return EntityCatalog.getDefault().resolveEntity(publicId, systemId);
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    public String resolvePublic(String publicId) {
        return null;
    }
    
}
