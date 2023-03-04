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
package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.util.ImageUtilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.openide.util.NbBundle;

import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;

/** Catalog for project JAX-WS (JAX-RPC) related schemas that enables validation/completion support in
 *  editor.
 *
 * @author Milan Kuchiak
 *
 */
public class JaxWsSchemaCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    public static final String JAXWS_CONF_ID = "http://www.netbeans.org/ns/jax-ws/1"; // NOI18N
    public static final String URL_JAXWS_CONF = "nbres:/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xsd"; // NOI18N
 
    public JaxWsSchemaCatalog() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    @Override
    public Iterator<String> getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(JAXWS_CONF_ID);
        return list.listIterator();
    }

    /**
     * Get registered systemId for given public Id or null if not registered.
     * @return null if not registered
     */
    @Override
    public String getSystemID(String publicId) {
        if (JAXWS_CONF_ID.equals(publicId)) {
            return URL_JAXWS_CONF;
        }
        return null;
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    @Override
    public void refresh() {
    }

    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void addCatalogListener(CatalogListener l) {
    }

    /**
     * Optional operation coupled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void removeCatalogListener(CatalogListener l) {
    }

    /** Registers new listener.  */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(JaxWsSchemaCatalog.class, "LBL_JaxWsSchemaCatalog"); // NOI18N
    }

    /**
     * Return visualized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    @Override
    public java.awt.Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/jaxwsmodel/resources/JaxWsSchemaCatalog.png"); // NOI18N
    }

    /**
     * @return I18N short description
     */
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(JaxWsSchemaCatalog.class, "DESC_JaxWsSchemaCatalog");
    }

    /** Unregister the listener.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    @Override
    public InputSource resolveEntity(
            String publicId, String systemId) throws SAXException, IOException {
        if (JAXWS_CONF_ID.equals(publicId)) {
            return new InputSource(URL_JAXWS_CONF);
        }
        return null;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolveURI(
            String name) {
        if (JAXWS_CONF_ID.equals(name)) {
            return URL_JAXWS_CONF;
        }
        return null;
    }

    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolvePublic(
            String publicId) {
        return null;
    }
}
