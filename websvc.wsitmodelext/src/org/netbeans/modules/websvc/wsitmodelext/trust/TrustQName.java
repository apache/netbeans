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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitmodelext.trust;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

/**
 *
 * @author Martin Grebac
 */
public enum TrustQName {
    TOKENTYPE(createTrustQName("TokenType")),                     //NOI18N
    KEYTYPE(createTrustQName("KeyType")),                     //NOI18N
    KEYSIZE(createTrustQName("KeySize"));                     //NOI18N

    public static final String TRUST_NS_PREFIX = "t";                                       //NOI18N

    public static final String TRUST_NS_URI = "http://schemas.xmlsoap.org/ws/2005/02/trust";    //NOI18N
    public static final String TRUST_NS_URI_EXT = "http://schemas.xmlsoap.org/ws/2005/02/trust/WS-Trust.xsd";    //NOI18N
    public static final String TRUST_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/WS-Trust.xsd";    //NOI18N

    public static final String TRUST_12_NS_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";    //NOI18N
    public static final String TRUST_12_NS_URI_EXT = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";    //NOI18N
    public static final String TRUST_12_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-trust-1.3.xsd";    //NOI18N
    
    static QName createTrustQName(String localName){
        return new QName(TRUST_NS_URI, localName, TRUST_NS_PREFIX);
    }

    TrustQName(QName name) {
        qName = name;
    }

    public QName getQName(ConfigVersion cfgVersion) {
        return new QName(getNamespaceUri(cfgVersion), qName.getLocalPart(), qName.getPrefix());
    }

    public static String getNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_1_0 : return TRUST_NS_URI;
            case CONFIG_1_3 : 
            case CONFIG_2_0 : return TRUST_12_NS_URI;
        }
        return null;
    }

    /* returns lowest compatible version */
    public static ConfigVersion getConfigVersion(QName q) {
        for (ConfigVersion cfgVersion : ConfigVersion.values()) {
            if (getQNames(cfgVersion).contains(q)) {
                return cfgVersion;
            }
        }
        return null;
    }

    public static Set<QName> getQNames(ConfigVersion cfgVersion) {
        Set<QName> qnames = new HashSet<QName>();
        for (TrustQName wq : values()) {
            qnames.add(wq.getQName(cfgVersion));
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        for (ConfigVersion cfg : ConfigVersion.values()) {
            try {
                String nsUri = getNamespaceUri(cfg);
                if (nsUri != null) {
                    hmap.put(nsUri, getSchemaLocation(nsUri, local));
                }
            } catch (IllegalArgumentException iae) {
                // ignore - just skip this
            }
        }
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (TRUST_NS_URI.equals(namespace)) {
            return local ? TRUST_NS_URI_LOCAL : TRUST_NS_URI_EXT;
        }
        if (TRUST_12_NS_URI.equals(namespace)) {
            return local ? TRUST_12_NS_URI_LOCAL : TRUST_12_NS_URI_EXT;
        }
        return null;
    }

}
