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
package org.netbeans.modules.websvc.wsitmodelext.mtom;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.SchemaLocationProvider;

/**
 *
 * @author Martin Grebac
 */
public enum MtomQName implements SchemaLocationProvider {
    OPTIMIZEDMIMESERIALIZATION(createMtomQName("OptimizedMimeSerialization"));                     //NOI18N

    public static final String MTOM_NS_PREFIX = "wsoma";                                            //NOI18N

    public static final String MTOM_NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";    //NOI18N
    public static final String MTOM_NS_URI_EXT = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization/optimizedmimeserialization-policy.xsd";    //NOI18N
    public static final String MTOM_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/optimizedmimeserialization-policy.xsd";    //NOI18N
    
    public static QName createMtomQName(String localName){
        return new QName(MTOM_NS_URI, localName, MTOM_NS_PREFIX);
    }
    
    MtomQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (MtomQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(MTOM_NS_URI, local ? MTOM_NS_URI_LOCAL : MTOM_NS_URI_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (MTOM_NS_URI.equals(namespace)) {
            return local ? MTOM_NS_URI_LOCAL : MTOM_NS_URI_EXT;
        }
        return null;
    }

}
