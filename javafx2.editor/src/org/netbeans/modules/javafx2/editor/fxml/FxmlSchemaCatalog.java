/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        Map m = new HashMap<String, String>();
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
