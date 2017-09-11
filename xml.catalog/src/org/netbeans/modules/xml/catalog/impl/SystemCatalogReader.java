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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
