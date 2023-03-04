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
package org.netbeans.modules.websvc.wsitmodelext.tx;

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
public enum TxQName {
    ATASSERTION(createTxQName("ATAssertion")),                                  //NOI18N
    ATALWAYSCAPABILITY(createTxQName("ATAlwaysCapability"));                    //NOI18N

    public static final String TX_NS_PREFIX = "wsat";          //NOI18N

    public static final String TX_NS_URI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";    //NOI18N
    public static final String TX_NS_URI_EXT = "http://schemas.xmlsoap.org/ws/2004/10/wsat/wsat.xsd";    //NOI18N
    public static final String TX_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/wsat.xsd";    //NOI18N
    
    public static QName createTxQName(String localName){
        return new QName(TX_NS_URI, localName, TX_NS_PREFIX);
    }
    
    TxQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (TxQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(TX_NS_URI, getSchemaLocation(TX_NS_URI, local));
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (TX_NS_URI.equals(namespace)) {
            return local ? TX_NS_URI_LOCAL : TX_NS_URI_EXT;
        }
        return null;
    }

}
