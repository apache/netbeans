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
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Roderico Cruz
 */
public enum BindingsQName {

    HANDLER_CHAINS(createHandlerQName("handler-chains")),
    HANDLER_CHAIN(createHandlerQName("handler-chain")),
    HANDLER(createHandlerQName("handler")),
    HANDLER_CLASS(createHandlerQName("handler-class")),
    HANDLER_NAME(createHandlerQName("handler-name")),
    BINDINGS(createBindingsQName("bindings"));
    
    public static final String JAVAEE_NS_URI = "http://java.sun.com/xml/ns/javaee";
    public static final String JAVAEE_NS_PREFIX = "jws";
    public static final String JAXWS_NS_URI = "http://java.sun.com/xml/ns/jaxws";
    public static final String JAXWS_NS_PREFIX = "jaxws";

    public static QName createHandlerQName(String localName) {
        return new QName(JAVAEE_NS_URI, localName, JAVAEE_NS_PREFIX);
    }

    public static QName createBindingsQName(String localName) {
        return new QName(JAXWS_NS_URI, localName, JAXWS_NS_PREFIX);
    }

    BindingsQName(QName name) {
        qName = name;
    }

    public QName getQName() {
        return qName;
    }
    private static Set<QName> qnames = null;

    public static Set<QName> getQNames() {
        return qnames;
    }
    
    public static void initQNames() {
        qnames = new HashSet<QName>();
        for (BindingsQName wq : values()) {
            qnames.add(wq.getQName());
        }
    }
    
    static {
        initQNames();
    }
    
    private final QName qName;
}

