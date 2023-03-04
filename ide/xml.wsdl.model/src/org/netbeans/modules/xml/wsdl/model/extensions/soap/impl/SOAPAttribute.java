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

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Nam Nguyen
 */
public enum SOAPAttribute implements Attribute {
        ENCODING_STYLE("encodingStyle", Collection.class, String.class),
        MESSAGE("message"),
        NAME("name"),
        NAMESPACE("namespace"),
        PART("part"),
        PARTS("parts"),
        USE("use", SOAPMessageBase.Use.class),
        SOAP_ACTION("soapAction"),
        STYLE("style", SOAPBinding.Style.class),
        TRANSPORT_URI("transport");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of SOAPAttribute */
    SOAPAttribute(String name) {
        this(name, String.class);
    }
    SOAPAttribute(String name, Class type) {
        this(name, type, null);
    }
    SOAPAttribute(String name, Class type, Class subtype) {
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
}
