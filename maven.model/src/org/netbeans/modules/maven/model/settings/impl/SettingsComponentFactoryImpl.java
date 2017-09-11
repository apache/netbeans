/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
