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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class PropertiesImpl extends POMComponentImpl implements Properties {

    public PropertiesImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public PropertiesImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().PROPERTIES));
    }

    // attributes

    // child elements


    @Override
    public void setProperty(String key, String value) {
        QName qname = POMQName.createQName(key, getModel().getPOMQNames().isNSAware());
        setChildElementText(qname.getLocalPart(), value,
                qname);
    }

    @Override
    public String getProperty(String key) {
        return getChildElementText(POMQName.createQName(key, getModel().getPOMQNames().isNSAware()));
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> toRet = new HashMap<String, String>();
        List<POMComponent> chlds = getChildren();
        for (POMComponent pc : chlds) {
            Element el = pc.getPeer();
            String key = el.getLocalName();
            String val = getChildElementText(POMQName.createQName(key, getModel().getPOMQNames().isNSAware()));
            toRet.put(key, val);
        }
        return toRet;
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
