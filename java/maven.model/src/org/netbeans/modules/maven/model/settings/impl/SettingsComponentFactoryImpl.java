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
import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.ActivationCustom;
import org.netbeans.modules.maven.model.settings.ActivationFile;
import org.netbeans.modules.maven.model.settings.ActivationOS;
import org.netbeans.modules.maven.model.settings.ActivationProperty;
import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.Profile;
import org.netbeans.modules.maven.model.settings.Properties;
import org.netbeans.modules.maven.model.settings.Proxy;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.RepositoryPolicy;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentFactory;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.spi.ElementFactory;
import org.netbeans.modules.maven.model.settings.spi.SettingsExtensibilityElementBase;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class SettingsComponentFactoryImpl implements SettingsComponentFactory {
    
    private SettingsModel model;
    
    /**
     * Creates a new instance of POMComponentFactoryImpl
     */
    public SettingsComponentFactoryImpl(SettingsModel model) {
        this.model = model;
    }    
 
    private static QName getQName(Element element, SettingsComponentImpl context) {
        String namespace = element.getNamespaceURI();
        String prefix = element.getPrefix();
        if (namespace == null && context != null) {
            namespace = context.lookupNamespaceURI(prefix);
        }
        String localName = element.getLocalName();
        assert(localName != null);
        if (namespace == null && prefix == null) {
            return new QName(localName);
        } else if (namespace != null && prefix == null) {
            return new QName(namespace, localName);
        } else {
            return new QName(namespace, localName, prefix);
        }
    }

    @Override
    public SettingsComponent create(Element element, SettingsComponent context) {
        // return new SCAComponentCreateVisitor().create(element, context);
        QName qName = getQName(element, (SettingsComponentImpl)context);
        ElementFactory elementFactory = ElementFactoryRegistry.getDefault().get(qName);
        return create(elementFactory, element, context);
    }
    
    private SettingsComponent create(ElementFactory elementFactory, Element element, SettingsComponent context) {
        if (elementFactory != null ){
            return elementFactory.create(context, element);
        } else {
            return new SettingsExtensibilityElementBase(model, element);
        }
    }
    
    @Override
    public SettingsComponent create(SettingsComponent context, QName qName) {
       String prefix = qName.getPrefix();
       if (prefix == null || prefix.length() == 0) {
           prefix = qName.getLocalPart();
       } else {
           prefix = prefix + ":" + qName.getLocalPart();
       }
 
       ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
       Element element = model.getDocument().createElementNS(qName.getNamespaceURI(), prefix);
       return create(factory, element, context);
    }
    
    @Override
    public Settings createSettings() {
        return new SettingsImpl(model);
    }


    @Override
    public Repository createRepository() {
        return new RepositoryImpl(model, false);
    }

    @Override
    public Repository createPluginRepository() {
        return new RepositoryImpl(model, true);
    }

    @Override
    public RepositoryPolicy createReleaseRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, model.getSettingsQNames().RELEASES);
    }

    @Override
    public RepositoryPolicy createSnapshotRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, model.getSettingsQNames().SNAPSHOTS);
    }


    @Override
    public Profile createProfile() {
        return new ProfileImpl(model);
    }

    @Override
    public Activation createActivation() {
        return new ActivationImpl(model);
    }

    @Override
    public ActivationProperty createActivationProperty() {
        return new ActivationPropertyImpl(model);
    }

    @Override
    public ActivationOS createActivationOS() {
        return new ActivationOSImpl(model);
    }

    @Override
    public ActivationFile createActivationFile() {
        return new ActivationFileImpl(model);
    }

    @Override
    public ActivationCustom createActivationCustom() {
        return new ActivationCustomImpl(model);
    }

    @Override
    public Properties createProperties() {
        return new PropertiesImpl(model);
    }

    @Override
    public SettingsExtensibilityElement createSettingsExtensibilityElement(QName name) {
        return new SettingsExtensibilityElementBase(model, name);
    }

    @Override
    public Configuration createConfiguration() {
        return new ConfigurationImpl(model);
    }

    @Override
    public Mirror createMirror() {
        return new MirrorImpl(model);
    }

    @Override
    public Proxy createProxy() {
        return new ProxyImpl(model);
    }

    @Override
    public Server createServer() {
        return new ServerImpl(model);
    }


}
