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
package org.netbeans.modules.xml.jaxb.catalog;

import java.util.Collections;
import java.util.Iterator;

import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;




/**
 * @author ads
 *
 */
public final class MappingCatalog implements CatalogReader {
    
    private static final String ECLIPSE_LINK_SCHEMA = "eclipselink_oxm_2_3.xsd";    // NOI18N
    private static final String MOXY_NS ="http://www.eclipse.org/eclipselink/xsds/persistence/oxm";
    
    private static final String MOXY_PUBLIC_ID = "SCHEMA:" + MOXY_NS + "/" + ECLIPSE_LINK_SCHEMA;// NOI18N
    
    private static final String MOXY_RESOURCE_PATH = 
        "nbres:/org/netbeans/modules/xml/jaxb/resources/"+ECLIPSE_LINK_SCHEMA; //NO18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
     */
    @Override
    public void addCatalogListener( CatalogListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#getPublicIDs()
     */
    @Override
    public Iterator getPublicIDs() {
        return Collections.singletonList(MOXY_PUBLIC_ID).iterator();    
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#getSystemID(java.lang.String)
     */
    @Override
    public String getSystemID( String publicId ) {
        if ( MOXY_PUBLIC_ID.equals( publicId ) ){
            return MOXY_RESOURCE_PATH;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#refresh()
     */
    @Override
    public void refresh() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
     */
    @Override
    public void removeCatalogListener( CatalogListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#resolvePublic(java.lang.String)
     */
    @Override
    public String resolvePublic( String publicId ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.catalog.spi.CatalogReader#resolveURI(java.lang.String)
     */
    @Override
    public String resolveURI( String arg0 ) {
        return null;
    }

}
