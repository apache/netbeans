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
import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.ActivationCustom;
import org.netbeans.modules.maven.model.settings.ActivationFile;
import org.netbeans.modules.maven.model.settings.ActivationOS;
import org.netbeans.modules.maven.model.settings.ActivationProperty;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ActivationImpl extends SettingsComponentImpl implements Activation {

    public ActivationImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ActivationImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().ACTIVATION));
    }

    // attributes

    // child elements
    @Override
    public ActivationOS getActivationOS() {
        return getChild(ActivationOS.class);
    }

    @Override
    public void setActivationOS(ActivationOS activationOS) {
        List<Class<? extends SettingsComponent>> empty = Collections.emptyList();
        setChild(ActivationOS.class, getModel().getSettingsQNames().ACTIVATIONOS.getName(), activationOS, empty);
    }

    @Override
    public ActivationProperty getActivationProperty() {
        return getChild(ActivationProperty.class);
    }

    @Override
    public void setActivationProperty(ActivationProperty activationProperty) {
        List<Class<? extends SettingsComponent>> empty = Collections.emptyList();
        setChild(ActivationProperty.class, getModel().getSettingsQNames().ACTIVATIONPROPERTY.getName(), activationProperty, empty);
    }

    @Override
    public ActivationFile getActivationFile() {
        return getChild(ActivationFile.class);
    }

    @Override
    public void setActivationFile(ActivationFile activationFile) {
        List<Class<? extends SettingsComponent>> empty = Collections.emptyList();
        setChild(ActivationFile.class, getModel().getSettingsQNames().ACTIVATIONFILE.getName(), activationFile, empty);
    }

    @Override
    public ActivationCustom getActivationCustom() {
        return getChild(ActivationCustom.class);
    }

    @Override
    public void setActivationCustom(ActivationCustom activationCustom) {
        List<Class<? extends SettingsComponent>> empty = Collections.emptyList();
        setChild(ActivationCustom.class, getModel().getSettingsQNames().ACTIVATIONCUSTOM.getName(), activationCustom, empty);
    }

    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

}
