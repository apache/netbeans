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
