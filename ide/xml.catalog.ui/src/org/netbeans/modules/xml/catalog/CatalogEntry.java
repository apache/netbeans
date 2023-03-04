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
package org.netbeans.modules.xml.catalog;

import org.xml.sax.*;

import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;

/**
 * Represents catalog entry keyed by a public ID.
 * The implementation is not cached it queries underlaying catalog.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class CatalogEntry extends Object {

    private final String publicID;
    private final CatalogReader catalog;

    /** Creates new CatalogEntry */
    public CatalogEntry(String publicID, CatalogReader catalog) {
        this.publicID = publicID;
        this.catalog = catalog;
    }
    
    CatalogReader getCatalog() {
        return catalog;
    }

    /**
     * Use CatalogReader or alternatively EntityResolver interface to resolve the PID.
     */
    public String getSystemID() {
        String sid = catalog.getSystemID(publicID);
        if (sid == null) {
            if (catalog instanceof EntityResolver) {
                try {
                    InputSource in = ((EntityResolver) catalog).resolveEntity(publicID, null);
                    if (in != null) {
                        sid = in.getSystemId();
                    }
                } catch (Exception ex) {
                    // return null;
                }
            }
        }

        //#53710 URL space canonization (%20 form works in most cases)
        String patchedSystemId = sid;
        if (patchedSystemId != null) {
            patchedSystemId = patchedSystemId.replaceAll("\\+", "%20"); // NOI18N
            patchedSystemId = patchedSystemId.replaceAll("\\ ", "%20"); // NOI18N
            return patchedSystemId;
        }

        return null;
    }
    
    public String getPublicID() {
        return publicID;
    }
    
    public String getPublicIDValue() {
        String id = getPublicID();
        if (id.startsWith("PUBLIC:")) return id.substring(7); //NOI18N
        if (id.startsWith("URI:")) return id.substring(4); //NOI18N
        if (id.startsWith("SYSTEM:")) return ""; //NOI18N
        if (id.startsWith("SCHEMA:")) return ""; //NOI18N
        return id;
    }
    
    public String getSystemIDValue() {
        String id = getPublicID();
        if (id.startsWith("SYSTEM:")) return id.substring(7); //NOI18N
        if (id.startsWith("SCHEMA:")) return id.substring(7); //NOI18N
        return "";
    }
    
    public String getUriValue() {
        return getSystemID();
    }
    
    public String getName() {
        String id = getPublicID();
        if (id.startsWith("PUBLIC:")) return NbBundle.getMessage(CatalogEntry.class, "TXT_publicEntry",id.substring(7)); //NOI18N
        if (id.startsWith("SYSTEM:")) return NbBundle.getMessage(CatalogEntry.class, "TXT_systemEntry",id.substring(7)); //NOI18N
        if (id.startsWith("URI:")) return NbBundle.getMessage(CatalogEntry.class, "TXT_publicEntry",id.substring(4)); //NOI18N
        if (id.startsWith("SCHEMA:")) return NbBundle.getMessage(CatalogEntry.class, "TXT_systemEntry",id.substring(7)); //NOI18N
        return NbBundle.getMessage(CatalogEntry.class, "TXT_publicEntry",id);
    }
    
    public String toString() {
        return publicID + " => " + getSystemID(); // NOI18N
    }
}
