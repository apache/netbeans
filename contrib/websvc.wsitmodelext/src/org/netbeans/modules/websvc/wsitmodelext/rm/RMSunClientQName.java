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
public enum RMSunClientQName implements SchemaLocationProvider {
    ACKREQUESTINTERVAL(createRMSunClientQName("AckRequestInterval")),           //NOI18N
    CLOSETIMEOUT(createRMSunClientQName("CloseTimeout")),           //NOI18N
    RESENDINTERVAL(createRMSunClientQName("ResendInterval"));           //NOI18N

    public static final String RMSUNCLIENT_NS_PREFIX = "sunrmc";                //NOI18N

    public static final String RMSUNCLIENT_NS_URI = "http://sun.com/2006/03/rm/client";  //NOI18N
    public static final String RMSUNCLIENT_NS_EXT = "http://fisheye5.atlassian.com/browse/~raw,r=1.1/wsit/wsit/etc/schemas/rx/sunrmc-policy.xsd.xsd";  //NOI18N
    public static final String RMSUNCLIENT_NS_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/sunrmc-policy.xsd.xsd";  //NOI18N
    
    public static QName createRMSunClientQName(String localName){
        return new QName(RMSUNCLIENT_NS_URI, localName, RMSUNCLIENT_NS_PREFIX);
    }
    
    RMSunClientQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (RMSunClientQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(RMSUNCLIENT_NS_URI, local ? RMSUNCLIENT_NS_LOCAL : RMSUNCLIENT_NS_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (RMSUNCLIENT_NS_URI.equals(namespace)) {
            return local ? RMSUNCLIENT_NS_LOCAL : RMSUNCLIENT_NS_EXT;
        }
        return null;
    }
}
