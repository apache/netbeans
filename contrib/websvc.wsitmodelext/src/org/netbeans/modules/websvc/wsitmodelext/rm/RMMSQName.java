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
public enum RMMSQName implements SchemaLocationProvider {
    MAXRECEIVEBUFFERSIZE(createRMMSQName("MaxReceiveBufferSize")),          //NOI18N
    RMFLOWCONTROL(createRMMSQName("RmFlowControl"));                   //NOI18N

    public static final String RMMS_NS_URI = "http://schemas.microsoft.com/net/2005/02/rm/policy";  //NOI18N
    public static final String RMMS_NS_EXT = "http://fisheye5.atlassian.com/browse/~raw,r=1.1/wsit/wsit/etc/schemas/rx/netrm-200502-policy.xsd";  //NOI18N
    public static final String RMMS_NS_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/netrm-200502-policy.xsd";  //NOI18N

    public static final String RMMS_NS_PREFIX = "net";                                              //NOI18N

    public static QName createRMMSQName(String localName){
        return new QName(RMMS_NS_URI, localName, RMMS_NS_PREFIX);
    }
    
    RMMSQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (RMMSQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(RMMS_NS_URI, local ? RMMS_NS_LOCAL : RMMS_NS_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (RMMS_NS_URI.equals(namespace)) {
            return local ? RMMS_NS_LOCAL : RMMS_NS_EXT;
        }
        return null;
    }

}
