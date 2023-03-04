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


import java.util.List;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ConfigurationImpl extends POMComponentImpl implements Configuration {

    public ConfigurationImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ConfigurationImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().CONFIGURATION));
    }

    // attributes

    // child elements

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<POMExtensibilityElement> getConfigurationElements() {
        return getChildren(POMExtensibilityElement.class);
    }

    @Override
    public void setSimpleParameter(String parameter, String value) {
        List<POMExtensibilityElement> list = getConfigurationElements();
        for (POMExtensibilityElement e : list) {
            if (parameter.equals(e.getQName().getLocalPart())) {
                if (value == null) {
                    removeChild(e.getQName().getLocalPart(), e);
                } else {
                    e.setElementText(value);
                }
                return;
            }
        }
        if (value != null) {
            POMExtensibilityElement el = getModel().getFactory().createPOMExtensibilityElement(POMQName.createQName(parameter));
            el.setElementText(value);
            addExtensibilityElement(el);
        }
    }

    @Override
    public String getSimpleParameter(String parameter) {
        List<POMExtensibilityElement> list = getConfigurationElements();
        for (POMExtensibilityElement e : list) {
            if (parameter.equals(e.getQName().getLocalPart())) {
                return e.getElementText();
            }
        }
        return null;
    }


}
