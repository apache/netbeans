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

package org.netbeans.modules.xml.catalog.impl.sun;

import java.io.*;
import java.beans.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;


import org.netbeans.modules.xml.catalog.spi.*;

import org.apache.xml.resolver.tools.NbCatalogResolver;
import org.apache.xml.resolver.NbCatalogManager;
import org.openide.util.NbBundle;

/**
 * SPI implementation that bridges to Sun's Resolvers 1.1.
 * <p>
 * It uses heavily lazy initialization to eliminate differences between an
 * instance constructed by the contructor <b>or</b> by deserialization process.
 * The approach also speeds up setup time.
 *
 * @author  Petr Kuzel
 */
public final class Catalog
    implements org.netbeans.modules.xml.catalog.spi.CatalogReader, CatalogDescriptorBase, Serializable, EntityResolver{

    private static final long serialVersionUID = 123659121L;

    private transient PropertyChangeSupport pchs;

    private transient EntityResolver peer;

    private transient String desc;

    // a catalog source location
    private String location;

    // a public preference
    private boolean preference = true;

    private static final String PROP_LOCATION = "cat-loc";

    private static final String PROP_PREF_PUBLIC = "cat-pref";

    private static final String PROP_DESC = CatalogDescriptorBase.PROP_CATALOG_DESC;

    /** Creates a new instance of Catalog */
    public Catalog() {
    }

    /**
     * Deserialization 'constructor'.
     */
    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // lazy init transient fields, see getPCHS() and getPeer() methods
        setShortDescription(NbBundle.getMessage(Catalog.class, "MSG_prepared", location));
    }

    /**
     * Set Catalog source (a URL).
     */
    public synchronized void setLocation(String location) {
        String old = this.location;
        this.location = location;
        peer = null;  // lazy init
        getPCHS().firePropertyChange(PROP_LOCATION, old, location);
        updateDisplayName();
    }

    /**
     * Access the location value.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set public resolving preference.
     */
    public void setPreferPublic(boolean val) {
        boolean old = preference;
        this.preference = val;
        getPCHS().firePropertyChange(PROP_PREF_PUBLIC, old, val);
    }

    /**
     * Access the public ID preference flag.
     */
    public boolean isPreferPublic() {
        return preference;
    }

    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void addCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }

    /** Registers new listener.  */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPCHS().addPropertyChangeListener(l);
    }

    /**
     * @return I18N display name
     */
    @Override
    public String getDisplayName() {
        String src = location;
        if (src == null || "".equals(src.trim())) {
            return NbBundle.getMessage(Catalog.class, "PROP_missing_location");
        } else {
            return NbBundle.getMessage(Catalog.class, "TITLE_catalog", location);
        }
    }

    public String getName() {
        return getClass() + location + preference;
    }

    /**
     * Notify listeners that display name have changed.
     */
    public void updateDisplayName() {
        String name = getDisplayName();
        getPCHS().firePropertyChange(CatalogDescriptorBase.PROP_CATALOG_NAME, null, name);
    }

    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public String getIconResource(int type) {
        return null;
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    @Override
    public Iterator getPublicIDs() {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.NbCatalogResolver) {
            org.apache.xml.resolver.Catalog cat = ((org.apache.xml.resolver.tools.NbCatalogResolver) p).getCatalog();
            return cat.getPublicIDs();
        }
        return null;
    }

    /**
     * @return I18N short description
     */
    @Override
    public String getShortDescription() {
        return desc;
    }

    public void setShortDescription(String desc) {
        String old = this.desc;
        this.desc = desc;
        getPCHS().firePropertyChange(PROP_DESC, old, desc);
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    @Override
    public String getSystemID(String publicId) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.NbCatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.NbCatalogResolver) p).getCatalog().resolveSystem(publicId);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    @Override
    public synchronized void refresh() {
        peer = createPeer(location, preference);
    }

    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void removeCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }

    /** Unregister the listener.  */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPCHS().removePropertyChangeListener(l);
    }

    /**
     * Delegate entity resution process to peer if exists.
     */
    @Override
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
        return getPeer().resolveEntity(publicID, systemID);
    }

/** We are a key and must retain equals immutability
    public boolean equals(Object obj) {
        if (obj instanceof Catalog) {
            Catalog cat = (Catalog) obj;
            if (this.location == null && cat.location != null) return false;
            if ((this.location != null && this.location.equals(cat.location)) == false) return false;
            return  (this.preference == cat.preference);
        }
        return false;
    }


    public int hashCode() {
        return (location != null ? location.hashCode() : 0) ^ (preference?13:7);
    }
*/
    /**
     * Factory new peer and load data into it.
     * As a side effect set short description.
     * @return EntityResolver never <code>null</code>
     */
    @SuppressWarnings("Convert2Lambda")
    private EntityResolver createPeer(String location, boolean pref) {
        try {
            NbCatalogManager manager = new NbCatalogManager(null) {
                @Override
                public org.apache.xml.resolver.Catalog getPrivateCatalog() {
                    try {
                        org.apache.xml.resolver.Catalog catalog = new NbCatalog();
                        catalog.setCatalogManager(this);
                        catalog.setupReaders();
                        catalog.loadSystemCatalogs();
                        return catalog;
                    } catch (IOException | RuntimeException ex) {
                        ex.printStackTrace();
                        return super.getPrivateCatalog();
                    }
                }
            };
            manager.setUseStaticCatalog(false);
            manager.setPreferPublic(pref);

            NbCatalogResolver catalogResolver = new NbCatalogResolver(manager);
            org.apache.xml.resolver.Catalog cat = catalogResolver.getCatalog();
            cat.parseCatalog(new URL(location));
            setShortDescription(NbBundle.getMessage(Catalog.class, "DESC_loaded"));
            return catalogResolver;
        } catch (IOException ex) {
            setShortDescription(NbBundle.getMessage(Catalog.class, "DESC_error_loading", ex.getLocalizedMessage()));
            //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("I/O error loading catalog " + location, ex);
        }

        // return dumb peer
        return new EntityResolver () {
            @Override
            public InputSource resolveEntity(String p, String s) {
                return null;
            }
        };
    }

    /**
     * Lazy init PropertyChangeSupport and return it.
     */
    private synchronized PropertyChangeSupport getPCHS() {
        if (pchs == null) pchs = new PropertyChangeSupport(this);
        return pchs;
    }

    /**
     * Lazy init peer and return it.
     */
    private synchronized EntityResolver getPeer() {

        if (peer == null) peer = createPeer(location, preference);
        return peer;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolveURI(String name) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.NbCatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.NbCatalogResolver) p).getCatalog().resolveURI(name);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolvePublic(String publicId) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.NbCatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.NbCatalogResolver) p).getCatalog().resolvePublic(publicId,null);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }

    /**
     * Check validity of the current entry. Only to be used by GUI!
     */
    public boolean isValid() {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.NbCatalogResolver) {
            org.apache.xml.resolver.Catalog catalog = ((NbCatalogResolver) p).getCatalog();
            if (catalog instanceof NbCatalog) {
                return !((NbCatalog) catalog).getCatalogEntries().isEmpty();
            }
        }
        return false;
    }

    public static class NbCatalog extends org.apache.xml.resolver.Catalog {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        public Vector getCatalogEntries() {
            return catalogEntries;
        }
    }
}
