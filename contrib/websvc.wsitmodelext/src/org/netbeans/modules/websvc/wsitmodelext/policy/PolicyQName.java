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


package org.netbeans.modules.websvc.wsitmodelext.policy;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitmodelext.versioning.SchemaLocationProvider;

/**
 *
 * @author Martin Grebac
 */
public enum PolicyQName implements SchemaLocationProvider {
    ALL(createPolicyQName("All")),                              //NOI18N
    EXACTLYONE(createPolicyQName("ExactlyOne")),                //NOI18N
    POLICYREFERENCE(createPolicyQName("PolicyReference")),      //NOI18N
    OPTIONAL(createPolicyQName("Optional")),      //NOI18N
    POLICY(createPolicyQName("Policy"));                        //NOI18N

    private static final String POLICY_NS_PREFIX = "wsp";         //NOI18N

    static final String POLICY_NS_URI = 
            "http://schemas.xmlsoap.org/ws/2004/09/policy";      //NOI18N
    static final String POLICY_NS_EXT =
            "http://schemas.xmlsoap.org/ws/2004/09/policy/ws-policy.xsd";      //NOI18N
    static final String POLICY_NS_LOCAL =
            "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-policy-10.xsd";      //NOI18N

    static final String POLICY_12_NS_URI = 
            "http://www.w3.org/ns/ws-policy";      //NOI18N
    static final String POLICY_12_NS_EXT =
            "http://www.w3.org/2007/02/ws-policy.xsd";      //NOI18N
    static final String POLICY_12_NS_LOCAL =
            "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-policy-12.xsd";      //NOI18N

    static QName createPolicyQName(String localName){
        return new QName(POLICY_NS_URI, localName, POLICY_NS_PREFIX);
    }
    
    PolicyQName(QName name) {
        qName = name;
    }

    public QName getQName(ConfigVersion cfgVersion) {
        return new QName(getNamespaceUri(cfgVersion), qName.getLocalPart(), qName.getPrefix());
    }

    public static String getNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_1_0 : return POLICY_NS_URI;
            case CONFIG_1_3 :
            case CONFIG_2_0 : return POLICY_12_NS_URI;
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
        for (PolicyQName wq : values()) {
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
                /* ignore, the namespace doesn't exist */
            }
        }
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (POLICY_NS_URI.equals(namespace)) {
            return local ? POLICY_NS_LOCAL : POLICY_NS_EXT;
        }
        if (POLICY_12_NS_URI.equals(namespace)) {
            return local ? POLICY_12_NS_LOCAL : POLICY_12_NS_EXT;
        }
        return null;
    }

}
