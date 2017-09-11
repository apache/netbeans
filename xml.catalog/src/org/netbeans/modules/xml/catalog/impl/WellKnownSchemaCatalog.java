/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.catalog.impl;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides well-known schemas from W3C.org directly from NB installation,
 * so they do not need to be downloaded each time from the Internet
 *
 * @author sdedic
 */
public class WellKnownSchemaCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver {
    
    /**
     * XML instance schema
     */
    private static final String XML_INSTANCE_URI = "http://www.w3.org/2001/XMLSchema-instance"; // NOI18N
    private static final String XML_INSTANCE_LOCAL = "nbres:/org/netbeans/modules/xml/catalog/resources/XMLSchema-instance.xsd"; // NOI18N
    
    /**
     * XML Schema itself
     */
    private static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema"; // NOI18N
    private static final String XML_SCHEMA_LOCAL = "nbres:/org/netbeans/modules/xml/catalog/resources/XMLSchema.xsd"; // NOI18N
    private static final String XML_SCHEMA_LOCATION = "http://www.w3.org/2001/XMLSchema.xsd"; // NOI18N
    
    /**
     * XML namespace content
     */
    private static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    private static final String XML_NAMESPACE_LOCAL = "nbres:/org/netbeans/modules/xml/catalog/resources/XMLNamespace.xsd"; // NOI18N
    private static final String XML_NAMESPACE_LOCATION = "http://www.w3.org/2001/xml.xsd"; // NOI18N

    /**
     * XSLT namespace. Perhaps should reside in XSLT support module ?
     */
    private static final String XSLT_URI = "http://www.w3.org/1999/XSL/Transform";
    private static final String XSLT_LOCAL = "nbres:/org/netbeans/modules/xml/catalog/resources/Transform.xsd"; // NOI18N

    /**
     * Maps public ID -> system ID
     */
    private Map<String, String> publicIdMap;
    
    /**
     * Captures system IDs to local resources
     */
    private Map<String, String> captureSystemIds;

    public WellKnownSchemaCatalog() {
        Map m = new HashMap<String, String>();
        m.put(XML_INSTANCE_URI, XML_INSTANCE_LOCAL);
        m.put(XML_SCHEMA_URI, XML_SCHEMA_LOCAL);
        m.put(XML_NAMESPACE_URI, XML_NAMESPACE_LOCAL);
        m.put(XSLT_URI, XSLT_LOCAL);
        
        publicIdMap = Collections.unmodifiableMap(m);
        
        m = new HashMap<String, String>();
        m.put(XML_NAMESPACE_LOCATION, XML_NAMESPACE_LOCAL);
        m.put(XML_SCHEMA_LOCATION, XML_SCHEMA_LOCAL);
        captureSystemIds = Collections.unmodifiableMap(m);
    }
    
    @Override
    public Iterator getPublicIDs() {
        return publicIdMap.keySet().iterator();
    }

    @Override
    public String getSystemID(String publicId) {
        return publicIdMap.get(publicId);
    }

    @Override
    public String resolvePublic(String publicId) {
        return getSystemID(publicId);
    }

    @Override
    public String resolveURI(String name) {
//        return null;
        return getSystemID(name);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WellKnownSchemaCatalog.class, "LBL_WellKnownSchemas");
    }

    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/xml/catalog/impl/sysCatalog.gif"; //NOI18N
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(WellKnownSchemaCatalog.class, "LBL_WellKnownSchemasDescription");
    }

    @Override
    public void refresh() {}

    @Override
    public void addCatalogListener(CatalogListener l) {}

    @Override
    public void removeCatalogListener(CatalogListener l) {}

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {}

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {}

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String resolved = null;
        if (publicId != null) {
            resolved = getSystemID(publicId);
        }
        if (systemId != null) {
            resolved = getSystemID(systemId);
        }
        if (resolved == null && systemId != null) {
            resolved = captureSystemIds.get(systemId);
        }
        if (resolved != null) {
            return new org.xml.sax.InputSource(resolved);
        } else {
            return null;
        }
    }
}
