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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author nn136682
 */
public enum WSDLAttribute implements Attribute {
        BINDING("binding"),
        ELEMENT("element"),
        LOCATION("location"),
        MESSAGE("message"),
        NAME("name"),
        NAMESPACE_URI("namespace"),
        TARGET_NAMESPACE("targetNamespace"),
        PARAMETER_ORDER("parameterOrder"),
        PORT_TYPE("type"),
        TYPE("type");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of WSDLAttribute */
    WSDLAttribute(String name) {
        this(name, String.class);
    }
    WSDLAttribute(String name, Class type) {
        this(name, type, null);
    }
    WSDLAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }

    private QName qname() {
        return new QName(name);
    }
    
    private static Map<QName,List<QName>> qnameValuedAttributes = null;
    private static void initAttributeMap() {
        qnameValuedAttributes = new HashMap<QName,List<QName>>();
        qnameValuedAttributes.put(
                WSDLQNames.BINDING.getQName(), Arrays.asList(new QName[] { PORT_TYPE.qname()}));
        qnameValuedAttributes.put(
                WSDLQNames.PART.getQName(), Arrays.asList(new QName[] { ELEMENT.qname(), TYPE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.INPUT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.OUTPUT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.FAULT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.PORT.getQName(), Arrays.asList(new QName[] { BINDING.qname() }));
    }
    
    static Map<QName,List<QName>> getQNameValuedAttributes() {
        if (qnameValuedAttributes == null) {
            initAttributeMap();
        }
        return Collections.unmodifiableMap(qnameValuedAttributes);
    }
}
