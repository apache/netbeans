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

/*
 * WSDLQNames.java
 *
 * Created on November 17, 2005, 6:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author rico
 */
public enum WSDLQNames {      
    BINDING(createWSDLQName("binding")),
    DEFINITIONS(createWSDLQName("definitions")),
    DOCUMENTATION(createWSDLQName("documentation")),
    FAULT(createWSDLQName("fault")),
    IMPORT(createWSDLQName("import")),
    INPUT(createWSDLQName("input")),
    MESSAGE(createWSDLQName("message")),
    OPERATION(createWSDLQName("operation")),
    OUTPUT(createWSDLQName("output")),
    PART(createWSDLQName("part")),
    PORT(createWSDLQName("port")),
    PORTTYPE(createWSDLQName("portType")),
    SERVICE(createWSDLQName("service")),
    TYPES(createWSDLQName("types"));
    
    public static final String WSDL_NS_URI = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_PREFIX = "wsdl";
    
    public static QName createWSDLQName(String localName){
        return new QName(WSDL_NS_URI, localName, WSDL_PREFIX);
    }
    
    WSDLQNames(QName name) {
        qName = name;
    }
    
    QName getQName(){
        return qName;
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (WSDLQNames wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    
    private final QName qName;
}
