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

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum AddressingQName {
    ENDPOINTREFERENCE(createAddressingQName("EndpointReference")),              //NOI18N
    ADDRESS(createAddressingQName("Address")),                                  //NOI18N
    REFERENCEPROPERTIES(createAddressingQName("ReferenceProperties")),          //NOI18N
    REFERENCEPARAMETERS(createAddressingQName("ReferenceParameters")),          //NOI18N
    SERVICENAME(createAddressingQName("ServiceName")),                          //NOI18N
    PORTTYPE(createAddressingQName("PortType"));                                //NOI18N

    public static final String ADDRESSING_NS_PREFIX = "wsa";                                           //NOI18N

    public static final String ADDRESSING_NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/policy";  //NOI18N

    // TODO - find the schema location
    public static final String ADDRESSING_NS_URI_EXT = null;  //NOI18N
    public static final String ADDRESSING_NS_URI_LOCAL = null;  //NOI18N
    
    public static QName createAddressingQName(String localName){
        return new QName(ADDRESSING_NS_URI, localName, ADDRESSING_NS_PREFIX);
    }
    
    AddressingQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (AddressingQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
