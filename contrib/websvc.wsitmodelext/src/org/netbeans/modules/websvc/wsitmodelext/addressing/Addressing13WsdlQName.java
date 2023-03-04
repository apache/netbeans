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


package org.netbeans.modules.websvc.wsitmodelext.addressing;

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
public enum Addressing13WsdlQName {
    ADDRESSING(createAddressingQName("Addressing")),                              //NOI18N
    ANONYMOUSRESPONSES(createAddressingQName("AnonymousResponses"));                //NOI18N

    private static final String A_NS_PREFIX = "wsam";        //NOI18N

    static final String A_NS_URI = 
            "http://www.w3.org/2007/05/addressing/metadata";      //NOI18N
    static final String A_NS_URI_EXT =
            "http://www.w3.org/2007/05/addressing/metadata/ws-addr-metadata.xsd";      //NOI18N
    static final String A_NS_URI_LOCAL =
            "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-addr-metadata.xsd";      //NOI18N

    static QName createAddressingQName(String localName){
        return new QName(A_NS_URI, localName, A_NS_PREFIX);
    }
    
    Addressing13WsdlQName(QName name) {
        qName = name;
    }

    public QName getQName(ConfigVersion cfgVersion) {
        return new QName(getNamespaceUri(cfgVersion), qName.getLocalPart(), qName.getPrefix());
    }

    public static String getNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_2_0 : return A_NS_URI;
            case CONFIG_1_3 : return A_NS_URI;
            case CONFIG_1_0 : throw new IllegalArgumentException("These ADDR assertions are supported for 1.3 only!");
        }
        return null;
    }
    
    public static ConfigVersion getConfigVersion(QName q) {
        for (ConfigVersion cfgVersion : ConfigVersion.values()) {
            if (getQNames(cfgVersion).contains(q)) {
                return cfgVersion;
            }
        }
        System.err.println("Not found config version for: " + q);
        return null;
    }
    
    public static Set<QName> getQNames(ConfigVersion cfgVersion) {
        Set<QName> qnames = new HashSet<QName>();
        for (Addressing13WsdlQName wq : values()) {
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
                hmap.put(nsUri, getSchemaLocation(nsUri, local));
            } catch (IllegalArgumentException iae) {
                // ignore - just skip this
            }
        }
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (A_NS_URI.equals(namespace)) {
            return local ? A_NS_URI_LOCAL : A_NS_URI_EXT;
        }
        return null;
    }

}
