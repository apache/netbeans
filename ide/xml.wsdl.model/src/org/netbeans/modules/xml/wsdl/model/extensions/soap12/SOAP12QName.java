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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;


/**
 * @author Sujit Biswas
 *
 */
public enum SOAP12QName {
    
    ADDRESS(createSOAPQName("address")),
    BINDING(createSOAPQName("binding")),
    BODY(createSOAPQName("body")),
    FAULT(createSOAPQName("fault")),
    HEADER(createSOAPQName("header")),
    HEADER_FAULT(createSOAPQName("headerfault")),
    OPERATION(createSOAPQName("operation"));
    
    public static final String SOAP_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP_NS_PREFIX = "soap12";
    
    public static QName createSOAPQName(String localName){
        return new QName(SOAP_NS_URI, localName, SOAP_NS_PREFIX);
    }
    
    SOAP12QName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (SOAP12QName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private final QName qName;
}
