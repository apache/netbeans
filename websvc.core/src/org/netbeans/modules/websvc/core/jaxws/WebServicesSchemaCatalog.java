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
