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
package org.netbeans.modules.javafx2.editor.fxml;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
public class FxmlSchemaCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver {
    
    /**
     * XML instance schema
     */
    private static final String FXML_INSTANCE_URI = "http://javafx.com/fxml"; // NOI18N
    private static final String FXML_INSTANCE_URI2 = "http://javafx.com/javafx/2.2"; // NOI18N
    private static final String FXML_INSTANCE_LOCAL = "nbres:/org/netbeans/modules/javafx2/editor/resources/fxml.xsd"; // NOI18N
    
    /**
     * Maps public ID -> system ID
     */
    private Map<String, String> publicIdMap;
    
    public FxmlSchemaCatalog() {
        Map<String, String> m = new HashMap<>();
        m.put(FXML_INSTANCE_URI, FXML_INSTANCE_LOCAL);
        m.put(FXML_INSTANCE_URI2, FXML_INSTANCE_LOCAL);
        m.put(JavaFXEditorUtils.FXML_FX_NAMESPACE_CURRENT, FXML_INSTANCE_LOCAL);
        
        publicIdMap = Collections.unmodifiableMap(m);
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
        return NbBundle.getMessage(FxmlSchemaCatalog.class, "LBL_FxmlSchemas");
    }

    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/xml/catalog/impl/sysCatalog.gif"; //NOI18N
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(FxmlSchemaCatalog.class, "LBL_FxmlSchemasDescription");
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
        if (resolved != null) {
            return new org.xml.sax.InputSource(resolved);
        } else {
            return null;
        }
    }
}
