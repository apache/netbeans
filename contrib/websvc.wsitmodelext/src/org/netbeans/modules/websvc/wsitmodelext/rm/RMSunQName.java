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
import org.netbeans.modules.websvc.wsitmodelext.versioning.SchemaLocationProvider;

/**
 *
 * @author Martin Grebac
 */
public enum RMSunQName implements SchemaLocationProvider {
    ORDERED(createRMSunQName("Ordered")),                   //NOI18N
    ALLOWDUPLICATES(createRMSunQName("AllowDuplicates"));   //NOI18N

    public static final String RMSUN_NS_PREFIX = "sunrm";                   //NOI18N

    public static final String RMSUN_NS_URI = "http://sun.com/2006/03/rm";  //NOI18N
    public static final String RMSUN_NS_EXT = "http://fisheye5.atlassian.com/browse/~raw,r=1.3/wsit/wsit/etc/schemas/rx/sunrm-policy.xsd.xsd";  //NOI18N
    public static final String RMSUN_NS_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/sunrm-policy.xsd.xsd";  //NOI18N
    
    public static QName createRMSunQName(String localName){
        return new QName(RMSUN_NS_URI, localName, RMSUN_NS_PREFIX);
    }
    
    RMSunQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (RMSunQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(RMSUN_NS_URI, local ? RMSUN_NS_LOCAL : RMSUN_NS_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (RMSUN_NS_URI.equals(namespace)) {
            return local ? RMSUN_NS_LOCAL : RMSUN_NS_EXT;
        }
        return null;
    }

}
