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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import java.util.Collection;
import java.util.Collections;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12MessageBase;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.w3c.dom.Element;

/**
 * @author Sujit Biswas
 *
 */
public abstract class SOAP12MessageBaseImpl extends SOAP12ComponentImpl implements SOAP12MessageBase {
    
    /** Creates a new instance of SOAPMessageBaseImpl */
    public SOAP12MessageBaseImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public Use getUse() {
        String s = getAttribute(SOAP12Attribute.USE);
        return s == null ? null : getUseValueOf(s);
    }
    
    public void setUse(Use use) {
        setAttribute(USE_PROPERTY, SOAP12Attribute.USE, use);
    }

    private Use getUseValueOf(String s) {
        return s == null ? null : Use.valueOf(s.toUpperCase());
    }
    
    protected Object getAttributeValueOf(SOAP12Attribute attr, String s) {
        if (attr == SOAP12Attribute.USE) {
            return getUseValueOf(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }
    
    public String getNamespace() {
        return getAttribute(SOAP12Attribute.NAMESPACE);
    }

    public void setNamespace(String namespaceURI) {
        setAttribute(NAMESPACE_PROPERTY, SOAP12Attribute.NAMESPACE, namespaceURI);
    }

    public void removeEncodingStyle(String encodingStyle) {
        Collection<String> styles = getEncodingStyles();
        if (styles != null && styles.remove(encodingStyle)) {
            setAttribute(ENCODING_STYLE_PROPERTY, SOAP12Attribute.ENCODING_STYLE, Util.toString(styles));
        }
    }

    public void addEncodingStyle(String encodingStyle) {
        Collection<String> styles = getEncodingStyles();
        if (styles != null) {
            styles.add(encodingStyle);
        } else {
            styles = Collections.singleton(encodingStyle);
        }
        setAttribute(ENCODING_STYLE_PROPERTY, SOAP12Attribute.ENCODING_STYLE, Util.toString(styles));
    }

    public java.util.Collection<String> getEncodingStyles() {
        String s = getAttribute(SOAP12Attribute.ENCODING_STYLE);
        return s == null ? null : Util.parse(s);
    }
}
