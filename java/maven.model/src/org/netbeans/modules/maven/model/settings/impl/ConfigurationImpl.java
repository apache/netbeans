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
package org.netbeans.modules.maven.model.settings.impl;

import java.util.*;
import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsQName;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ConfigurationImpl extends SettingsComponentImpl implements Configuration {

    public ConfigurationImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ConfigurationImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().CONFIGURATION));
    }

    // attributes

    // child elements

    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<SettingsExtensibilityElement> getConfigurationElements() {
        return getChildren(SettingsExtensibilityElement.class);
    }

    @Override
    public void setSimpleParameter(String parameter, String value) {
        List<SettingsExtensibilityElement> list = getConfigurationElements();
        for (SettingsExtensibilityElement e : list) {
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
            SettingsExtensibilityElement el = getModel().getFactory().createSettingsExtensibilityElement(
                    SettingsQName.createQName(
                        parameter,
                        getModel().getSettingsQNames().getNamespaceVersion()));
            el.setElementText(value);
            addExtensibilityElement(el);
        }
    }

    @Override
    public String getSimpleParameter(String parameter) {
        List<SettingsExtensibilityElement> list = getConfigurationElements();
        for (SettingsExtensibilityElement e : list) {
            if (parameter.equals(e.getQName().getLocalPart())) {
                return e.getElementText();
            }
        }
        return null;
    }


}
