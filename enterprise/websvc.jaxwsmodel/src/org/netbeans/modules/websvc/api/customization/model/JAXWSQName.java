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
 * JAXWSQName.java
 *
 * Created on February 22, 2006, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.customization.model;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Roderico Cruz
 */
public enum JAXWSQName {
    BINDINGS(createJAXWSQName("bindings")),
    PACKAGE(createJAXWSQName("package")),
    CLASS(createJAXWSQName("class")),
    ENABLEWRAPPERSTYLE(createJAXWSQName("enableWrapperStyle")),
    ENABLEASYNCMAPPING(createJAXWSQName("enableAsyncMapping")),
    ENABLEMIMECONTENT(createJAXWSQName("enableMIMEContent")),
    JAVAEXCEPTION(createJAXWSQName("exception")),
    METHOD(createJAXWSQName("method")),
    PARAMETER(createJAXWSQName("parameter")),
    JAVADOC(createJAXWSQName("javadoc")),
    PROVIDER(createJAXWSQName("provider"));
    
    public static final String JAXWS_NS_URI = "http://java.sun.com/xml/ns/jaxws";
    public static final String JAXWS_NS_PREFIX = "jaxws";
    
    public static QName createJAXWSQName(String localName){
        return new QName(JAXWS_NS_URI, localName, JAXWS_NS_PREFIX);
    }
    
    JAXWSQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames ;
    
    public static Set<QName> getQNames() {
        return qnames;
    }
    
    static {
        qnames = new HashSet<QName>();
        for (JAXWSQName wq : values()) {
            qnames.add(wq.getQName());
        }
    }
    
    private final QName qName;

}
