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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.ActivationCustom;
import org.netbeans.modules.maven.model.settings.ActivationFile;
import org.netbeans.modules.maven.model.settings.ActivationOS;
import org.netbeans.modules.maven.model.settings.ActivationProperty;
import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.ModelList;
import org.netbeans.modules.maven.model.settings.Profile;
import org.netbeans.modules.maven.model.settings.Properties;
import org.netbeans.modules.maven.model.settings.Proxy;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.RepositoryPolicy;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.SettingsQName;
import org.netbeans.modules.maven.model.settings.SettingsQName.Version;
import org.netbeans.modules.maven.model.settings.SettingsQNames;
import org.netbeans.modules.maven.model.settings.StringList;
import org.netbeans.modules.maven.model.settings.spi.ElementFactory;
import org.netbeans.modules.maven.model.settings.spi.SettingsExtensibilityElementBase;
import org.netbeans.modules.maven.model.settings.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;



/**
 *
 * @author mkleint
 */
@ServiceProvider(service=ElementFactory.class)
public class SettingsElementFactoryProvider implements ElementFactory {

    private final Set<QName> all;


    public SettingsElementFactoryProvider() {
        all = new HashSet<QName>();
        for (Version v : Version.values()) {
            all.addAll(new SettingsQNames(v).getElementQNames());
        }
    }

    @Override
    public Set<QName> getElementQNames() {
        return all;
    }

    @Override
    public SettingsComponent create(SettingsComponent context, Element element) {
        return new SettingsComponentCreateVisitor().create(element, context);
    }
}
class SettingsComponentCreateVisitor extends DefaultVisitor {
    private Element element;
    private SettingsComponent created;
        
    public SettingsComponent create(Element element, SettingsComponent context) {
        this.element = element;
        context.accept(this);
        return created;
    }

    private boolean isElementQName(SettingsQName q) {
        return areSameQName(q, element);
    }
      
    public static boolean areSameQName(SettingsQName q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }

    private void createExtensibilityElement(SettingsComponent context) {
        created = new SettingsExtensibilityElementBase(context.getModel(), element);
    }

    @Override
    public void visit(Settings context) {

        if (isElementQName(context.getModel().getSettingsQNames().PROFILES)) {
            created = new ProfileImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().MIRRORS)) {
            created = new MirrorImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().SERVERS)) {
            created = new ServerImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PROXIES)) {
            created = new ProxyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().ACTIVEPROFILES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getSettingsQNames().ACTIVEPROFILE);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PLUGINGROUPS)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getSettingsQNames().PLUGINGROUP);
            return;
        }
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Configuration context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Mirror context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Proxy context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Server context) {
        if (isElementQName(context.getModel().getSettingsQNames().CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }
        createExtensibilityElement(context);

    }


    @Override
    public void visit(Repository context) {
        if (isElementQName(context.getModel().getSettingsQNames().RELEASES)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().SNAPSHOTS)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(RepositoryPolicy context) {
        createExtensibilityElement(context);
    }


    @Override
    public void visit(Profile context) {
        if (isElementQName(context.getModel().getSettingsQNames().ACTIVATION)) {
            created = new ActivationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }


    @Override
    public void visit(StringList context) {
        createExtensibilityElement(context);
    }


    @Override
    public void visit(Activation context) {
        if (isElementQName(context.getModel().getSettingsQNames().ACTIVATIONOS)) {
            created = new ActivationOSImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().ACTIVATIONPROPERTY)) {
            created = new ActivationPropertyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().ACTIVATIONFILE)) {
            created = new ActivationFileImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getSettingsQNames().ACTIVATIONCUSTOM)) {
            created = new ActivationCustomImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationProperty context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationOS context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationFile context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationCustom context) {
        createExtensibilityElement(context);
    }


    @Override
    public void visit(ModelList context) {
        if (isElementQName(context.getModel().getSettingsQNames().PROFILE) && context.getListClass().equals(Profile.class)) {
            created = new ProfileImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getSettingsQNames().REPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getSettingsQNames().PLUGINREPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getSettingsQNames().PROXY) && context.getListClass().equals(Proxy.class)) {
            created = new ProxyImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getSettingsQNames().MIRROR) && context.getListClass().equals(Mirror.class)) {
            created = new ProxyImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getSettingsQNames().PROXY) && context.getListClass().equals(Proxy.class)) {
            created = new ProxyImpl(context.getModel(), element);
        }
    }


    @Override
    public void visit(Properties context) {
        createExtensibilityElement(context);
    }


    @Override
    public void visit(SettingsExtensibilityElement context) {
        createExtensibilityElement(context);
    }
}
    
