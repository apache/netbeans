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


package org.netbeans.modules.websvc.wsitmodelext.rm;

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
public enum RMQName {
    RMASSERTION(createRMQName("RMAssertion")),                                  //NOI18N
    ACKNOWLEDGEMENTINTERVAL(createRMQName("AcknowledgementInterval")),          //NOI18N
    DELIVERYASSURANCE(createRMQName("DeliveryAssurance")),                    //NOI18N
    INORDER(createRMQName("InOrder")),                                        //NOI18N
    EXACTLYONCE(createRMQName("ExactlyOnce")),                                //NOI18N
    ATLEASTONCE(createRMQName("AtLeastOnce")),                                //NOI18N
    ATMOSTONCE(createRMQName("AtMostOnce")),                                //NOI18N
    SEQUENCETRANSPORTSECURITY(createRMQName("SequenceTransportSecurity")),    //NOI18N
    SEQUENCESTR(createRMQName("SequenceSTR"));                                //NOI18N


    static final String RM_NS_PREFIX = "wsrm";                                            //NOI18N

    static final String RM_NS_URI = "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";    //NOI18N
    static final String RM_NS_URI_EXT = "http://schemas.xmlsoap.org/ws/2005/02/rm/wsrm-policy.xsd";    //NOI18N
    static final String RM_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/wsrm-policy-200502.xsd";    //NOI18N
    static final String RM_NS_HEADERS_URI = "http://schemas.xmlsoap.org/ws/2005/02/rm";    //NOI18

    static final String RM_12_NS_URI = "http://docs.oasis-open.org/ws-rx/wsrmp/200702";    //NOI18N
    static final String RM_12_NS_URI_EXT = "http://docs.oasis-open.org/ws-rx/wsrmp/200702/wsrmp-1.2-schema-200702.xsd";    //NOI18N
    static final String RM_12_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/wsrmp-1.2-schema-200702.xsd";    //NOI18N
    static final String RM_12_NS_HEADERS_URI = "http://docs.oasis-open.org/ws-rx/wsrm/200702";    //NOI18

    static QName createRMQName(String localName){
        return new QName(RM_NS_URI, localName, RM_NS_PREFIX);
    }

    RMQName(QName name) {
        qName = name;
    }

    public QName getQName(ConfigVersion cfgVersion) {
        return new QName(getNamespaceUri(cfgVersion), qName.getLocalPart(), qName.getPrefix());
    }

    public static String getNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_1_0 : return RM_NS_URI;
            case CONFIG_1_3 :
            case CONFIG_2_0 : return RM_12_NS_URI;
        }
        return null;
    }

    public static String getHeaderNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_1_0 : return RM_NS_HEADERS_URI;
            case CONFIG_1_3 :
            case CONFIG_2_0 : return RM_12_NS_HEADERS_URI;
        }
        return null;
    }

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
        for (RMQName wq : values()) {
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
        if (RM_NS_URI.equals(namespace)) {
            return local ? RM_NS_URI_LOCAL : RM_NS_URI_EXT;
        }
        if (RM_12_NS_URI.equals(namespace)) {
            return local ? RM_12_NS_URI_LOCAL : RM_12_NS_URI_EXT;
        }
        return null;
    }

}
