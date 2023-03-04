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

import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentFactory;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsQName;
import org.netbeans.modules.maven.model.settings.SettingsQName.Version;
import org.netbeans.modules.maven.model.settings.SettingsQNames;
import org.netbeans.modules.maven.model.settings.visitor.ChildComponentUpdateVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class SettingsModelImpl extends SettingsModel {
    
    private SettingsComponent rootComponent;
    private final SettingsComponentFactory componentFactory;
    private SettingsQNames settingsQNames;
    
    public SettingsModelImpl(ModelSource source) {
        super(source);
        componentFactory = new SettingsComponentFactoryImpl(this);
    }
    
    @Override
    public SettingsComponent getRootComponent() {
        return rootComponent;
    }

    @Override
    public SettingsComponentFactory getFactory() {
        return componentFactory;
    }

    @Override
    public Settings getSettings() {
        return (Settings) getRootComponent();
    }

    @Override
    public SettingsComponent createRootComponent(Element root) {
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null ) {
            for (Version v : Version.values()) {
                if (SettingsQName.createQName("settings", v).equals(q)) {
                    settingsQNames = new SettingsQNames(v);
                    rootComponent = new SettingsImpl(this, root);
                }
            }
            
        } 
        
        return getRootComponent();
    }

    @Override
    protected ComponentUpdater<SettingsComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<SettingsComponent>();
    }

    @Override
    public SettingsComponent createComponent(SettingsComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    @Override
    public SettingsQNames getSettingsQNames() {
        return settingsQNames;
    }


}
