/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
