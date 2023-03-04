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
package org.netbeans.modules.websvc.core.jaxws;

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
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;

/** Catalog for webservice related schemas that enables completion support in
 *  editor.
 *
 * @author Milan Kuchiak
 *
 */
public class WebServicesSchemaCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver {

    public static final String SUN_JAXWS_ID = "http://java.sun.com/xml/ns/jax-ws/ri/runtime"; // NOI18N
    private static final String URL_SUN_JAXWS = "nbres:/org/netbeans/modules/websvc/core/resources/sun-jaxws.xsd"; // NOI18N
    public static final String JAXWS_WSDL_BINDING_ID = "http://java.sun.com/xml/ns/jaxws"; // NOI18N
    private static final String URL_JAXWS_WSDL_BINDING = "nbres:/org/netbeans/modules/websvc/core/resources/wsdl_customizationschema_2_0.xsd"; // NOI18N
    public static final String JAXWS_HANDLER_CHAIN_ID = "http://java.sun.com/xml/ns/javaee"; // NOI18N
    private static final String URL_JAXWS_HANDLER_CHAIN = "nbres:/org/netbeans/modules/websvc/core/resources/javaee_web_services_metadata_handler_2_0.xsd"; // NOI18N

    public WebServicesSchemaCatalog() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator<String> getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(SUN_JAXWS_ID);
        list.add(JAXWS_WSDL_BINDING_ID);
        list.add(JAXWS_HANDLER_CHAIN_ID);
        return list.listIterator();
    }

    /**
     * Get registered systemId for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (SUN_JAXWS_ID.equals(publicId)) {
            return URL_SUN_JAXWS;
        } else if (JAXWS_WSDL_BINDING_ID.equals(publicId)) {
            return URL_JAXWS_WSDL_BINDING;
        } else if (JAXWS_HANDLER_CHAIN_ID.equals(publicId)) {
            return URL_JAXWS_HANDLER_CHAIN;
        }
        return null;
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }

    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }

    /**
     * Optional operation coupled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }

    /** Registers new listener.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(WebServicesSchemaCatalog.class, "LBL_WSSchemaCatalog"); // NOI18N
    }

    /**
     * Return visualized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public String getIconResource(int type) {
        return "org/netbeans/modules/websvc/core/resources/WSSchemaCatalog.png"; // NOI18N
    }

    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage(WebServicesSchemaCatalog.class, "DESC_WSSchemaCatalog");
    }

    /** Unregister the listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    public InputSource resolveEntity(
            String publicId, String systemId) throws SAXException, IOException {
        if (SUN_JAXWS_ID.equals(publicId)) {
            return new InputSource(URL_SUN_JAXWS);
        } else if (JAXWS_WSDL_BINDING_ID.equals(publicId)) {
            return new InputSource(URL_JAXWS_WSDL_BINDING);
        } else if (JAXWS_HANDLER_CHAIN_ID.equals(publicId)) {
            return new InputSource(URL_JAXWS_HANDLER_CHAIN);
        } else {
            return null;
        }

    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(
            String name) {
        if (SUN_JAXWS_ID.equals(name)) {
            return URL_SUN_JAXWS;
        } else if (JAXWS_WSDL_BINDING_ID.equals(name)) {
            return URL_JAXWS_WSDL_BINDING;
        } else if (JAXWS_HANDLER_CHAIN_ID.equals(name)) {
            return URL_JAXWS_HANDLER_CHAIN;
        }

        return null;
    }

    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    public String resolvePublic(
            String publicId) {
        return null;
    }
}
