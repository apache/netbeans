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
