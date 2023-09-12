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
package org.netbeans.modules.xml.catalog;

import java.util.*;
import java.io.*;
import java.net.URL;
import javax.xml.transform.Source;

import org.xml.sax.*;
import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.lib.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;

import org.netbeans.api.xml.services.*;
import javax.xml.transform.URIResolver;
import org.openide.util.lookup.ServiceProvider;

/**
 * An entity resolver that can resolve all registrations
 * in catalogs mounted by a user.
 * This is not exposed catalog package API. The
 * package funtionality is exposed via registering
 * this entity resolver in XMLDataObject resolver chain.
 * <p>
 * The class is public only for internal XML module reasons.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
@ServiceProvider(service = UserCatalog.class)
public class CatalogEntityResolver extends UserCatalog implements EntityResolver, URIResolver {

    /** Creates new CatalogEntityResolver */
    public CatalogEntityResolver() {
    }

    @Override
    public EntityResolver getEntityResolver() {
        return this;
    }

    /**
     * User's JAXP/TrAX <code>URIResolver</code>.
     * @return URIResolver or <code>null</code> if not supported.
     */
    @Override
    public URIResolver getURIResolver() {
        return this;
    }

    // SAX interface method implementation
    @Override
    public InputSource resolveEntity(String publicId,String systemId)
        throws SAXException, IOException {
        InputSource result = null;

        // try to use full featured entiry resolvers


        CatalogSettings mounted = CatalogSettings.getDefault();
        if (mounted != null) {
            Iterator it = mounted.getCatalogs( new Class[] {EntityResolver.class});

            while (it.hasNext()) {
                EntityResolver next = (EntityResolver) it.next();
                result = next.resolveEntity(publicId, systemId);
                if (result != null) break;
            }

            // fallback to ordinaly readers

            if (result == null && publicId != null) {

                it = mounted.getCatalogs(new Class[] {CatalogReader.class});

                while (it.hasNext()) {
                    CatalogReader next = (CatalogReader) it.next();
                    String sid = next.getSystemID(publicId);
                    if (sid != null) {
                        result =  new InputSource(sid);
                        break;
                    }
                }
            }
        // return result (null is allowed)
        }

        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("CatalogEntityResolver:PublicID: " + publicId + ", " + systemId + " => " + (result == null ? "null" : result.getSystemId())); // NOI18N

        // #56103 bootstrap XML catalog DTD
        if (result == null && "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN".equals(publicId)) {  // NOi18N
            URL url = org.apache.xml.resolver.Catalog.class.getResource("etc/catalog.dtd"); // NOI18N
            result = new InputSource(url.toExternalForm());
        }

        //#53710 URL space canonization (%20 form works in most cases)
        if (result != null) {
            String patchedSystemId = result.getSystemId();
            if (patchedSystemId != null) {
                patchedSystemId = patchedSystemId.replaceAll("\\+", "%20"); // NOI18N
                patchedSystemId = patchedSystemId.replaceAll("\\ ", "%20"); // NOI18N
                result.setSystemId(patchedSystemId);
            }
        }
        return result;

    }

    /**
     * Return all known public IDs.
     */
    @Override
    public Iterator getPublicIDs() {

        IteratorIterator ret = new IteratorIterator();

        CatalogSettings mounted = CatalogSettings.getDefault();
        Iterator it = mounted.getCatalogs( new Class[] {CatalogReader.class});

        while (it.hasNext()) {
            CatalogReader next = (CatalogReader) it.next();
            Iterator ids = next.getPublicIDs();
            if (ids != null) {
                ret.add(ids);
            }
        }

        return ret;
    }

    @Override
    public Source resolve(String href, String base)
        throws javax.xml.transform.TransformerException {

        Source result = null;

        CatalogSettings mounted = CatalogSettings.getDefault();

        if (href != null) {

            Iterator it = mounted.getCatalogs(new Class[] {CatalogReader.class});

            while (it.hasNext()) {
                CatalogReader next = (CatalogReader) it.next();
                try {
                    String sid;
                    if (href.startsWith("urn:publicid:")) { //NOI18N
                        // resolving publicId from catalog
                        String urn = href.substring(13);
                        sid = next.resolvePublic(URNtoPublic(urn));
                    } else {
                        sid = next.resolveURI(href);
                    }
                    if (sid != null) {
                        javax.xml.transform.Source source = new javax.xml.transform.sax.SAXSource();
                        source.setSystemId(sid);
                        result = source;
                        break;
                    }
                } catch (java.lang.Error error) {}
            }
        }

        return result;
    }

    /** Conversion of URN string to public identifier
     *  see : http://www.faqs.org/rfcs/rfc3151.html
     */
    private String URNtoPublic(String urn) {
        return urn.replace('+',' ').replace(":","//").replace(";","::").replace("%2B","+").replace("%3A",":").replace("%2F","/").replace("%3B",";").replace("%27","'").replace("%3F","?").replace("%23","#").replace("%25","%");
    }
}
