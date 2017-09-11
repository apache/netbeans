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
