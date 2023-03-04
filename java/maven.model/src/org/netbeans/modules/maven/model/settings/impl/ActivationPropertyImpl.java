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

import org.netbeans.modules.maven.model.settings.ActivationProperty;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ActivationPropertyImpl extends SettingsComponentImpl implements ActivationProperty {

    public ActivationPropertyImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ActivationPropertyImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().ACTIVATIONPROPERTY));
    }

    // attributes

    // child elements

    @Override
    public String getName() {
        return getChildElementText(getModel().getSettingsQNames().NAME.getQName());
    }

    @Override
    public void setName(String name) {
        setChildElementText(getModel().getSettingsQNames().NAME.getName(), name,
                getModel().getSettingsQNames().NAME.getQName());
    }

    @Override
    public String getValue() {
        return getChildElementText(getModel().getSettingsQNames().VALUE.getQName());
    }

    @Override
    public void setValue(String value) {
        setChildElementText(getModel().getSettingsQNames().VALUE.getName(), value,
                getModel().getSettingsQNames().VALUE.getQName());
    }

    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }


}
