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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.pom.spi.ElementFactory;
import org.netbeans.modules.maven.model.pom.spi.POMExtensibilityElementBase;
import org.netbeans.modules.maven.model.pom.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
@ServiceProvider(service=ElementFactory.class)
public class POMElementFactoryProvider implements ElementFactory {

    private POMQNames ns = new POMQNames(true);
    private POMQNames nonns = new POMQNames(false);
    private Set<QName> all;


    public POMElementFactoryProvider() {
        all = new HashSet<QName>();
        all.addAll(ns.getElementQNames());
        all.addAll(nonns.getElementQNames());
    }

    @Override
    public Set<QName> getElementQNames() {
        return all;
    }

    @Override
    public POMComponent create(POMComponent context, Element element) {
        return new POMComponentCreateVisitor().create(element, context);
    }
}

class POMComponentCreateVisitor extends DefaultVisitor {
    private Element element;
    private POMComponent created;
        
    public POMComponent create(Element element, POMComponent context) {
        this.element = element;
        context.accept(this);
        return created;
    }

    private boolean isElementQName(POMQName q) {
        return areSameQName(q, element);
    }
      
    public static boolean areSameQName(POMQName q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }

    private void createExtensibilityElement(POMComponent context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }

    @Override
    public void visit(Project context) {
        if (isElementQName(context.getModel().getPOMQNames().PARENT)) {
            created = new ParentImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PREREQUISITES)) {
            created = new PrerequisitesImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().ISSUEMANAGEMENT)) {
            created = new IssueManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().CIMANAGEMENT)) {
            created = new CiManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().MAILINGLISTS)) {
            created = new MailingListImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEVELOPERS)) {
            created = new DeveloperImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().CONTRIBUTORS)) {
            created = new ContributorImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().LICENSES)) {
            created = new LicenseImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().SCM)) {
            created = new ScmImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().ORGANIZATION)) {
            created = new OrganizationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().BUILD)) {
            created = new BuildImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PROFILES)) {
            created = new ProfileImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().MODULES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().MODULE);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCIES)) {
            created = new ProjectImpl.PList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().REPORTING)) {
            created = new ReportingImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCYMANAGEMENT)) {
            created = new DependencyManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DISTRIBUTIONMANAGEMENT)) {
            created = new DistributionManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Parent context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }

    @Override
    public void visit(Organization context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(DistributionManagement context) {
        if (isElementQName(context.getModel().getPOMQNames().DIST_REPOSITORY)) {
            created = new DeploymentRepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DIST_SNAPSHOTREPOSITORY)) {
            created = new DeploymentRepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().SITE)) {
            created = new SiteImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Site context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(DeploymentRepository context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Prerequisites context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Contributor context) {
        if (isElementQName(context.getModel().getPOMQNames().ROLES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().ROLE);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Scm context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(IssueManagement context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(CiManagement context) {
        if (isElementQName(context.getModel().getPOMQNames().NOTIFIER)) {
            created = new NotifierImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Notifier context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Repository context) {
        if (isElementQName(context.getModel().getPOMQNames().RELEASES)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().SNAPSHOTS)) {
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
        if (isElementQName(context.getModel().getPOMQNames().ACTIVATION)) {
            created = new ActivationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().BUILD)) {
            created = new BuildBaseImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().MODULES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().MODULE);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().REPORTING)) {
            created = new ReportingImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCYMANAGEMENT)) {
            created = new DependencyManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DISTRIBUTIONMANAGEMENT)) {
            created = new DistributionManagementImpl(context.getModel(), element);
            return;
        }
        
        if (isElementQName(context.getModel().getPOMQNames().PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(BuildBase context) {
        if (isElementQName(context.getModel().getPOMQNames().RESOURCES)) {
            created = new ResourceImpl.ResList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().TESTRESOURCES)) {
            created = new ResourceImpl.TestResList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Plugin context) {
        if (isElementQName(context.getModel().getPOMQNames().EXECUTIONS)) {
            created = new PluginExecutionImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().GOALS)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().GOAL);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(StringList context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Dependency context) {
        if (isElementQName(context.getModel().getPOMQNames().EXCLUSIONS)) {
            created = new ExclusionImpl.List(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Exclusion context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(PluginExecution context) {
        if (isElementQName(context.getModel().getPOMQNames().GOALS)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().GOAL);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Resource context) {
        if (isElementQName(context.getModel().getPOMQNames().INCLUDES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().INCLUDE);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().EXCLUDES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().EXCLUDE);
            return;
        }
        createExtensibilityElement(context);
    }

    @Override
    public void visit(PluginManagement context) {
        if (isElementQName(context.getModel().getPOMQNames().PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Reporting context) {
        if (isElementQName(context.getModel().getPOMQNames().REPORTPLUGINS)) {
            created = new ReportPluginImpl.List(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(ReportPlugin context) {
        if (isElementQName(context.getModel().getPOMQNames().REPORTSETS)) {
            created = new ReportSetImpl.List(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(ReportSet context) {
        if (isElementQName(context.getModel().getPOMQNames().REPORTS)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().REPORT);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Activation context) {
        if (isElementQName(context.getModel().getPOMQNames().ACTIVATIONOS)) {
            created = new ActivationOSImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().ACTIVATIONPROPERTY)) {
            created = new ActivationPropertyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().ACTIVATIONFILE)) {
            created = new ActivationFileImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().ACTIVATIONCUSTOM)) {
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
    public void visit(DependencyManagement context) {
        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCIES)) {
            created = new DependencyManagementImpl.DMList(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Build context) {
        visit((BuildBase) context);

        if (isElementQName(context.getModel().getPOMQNames().EXTENSIONS)) {
            created = new ExtensionImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().RESOURCES)) {
            created = new ResourceImpl.ResList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().TESTRESOURCE)) {
            created = new ResourceImpl.TestResList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getPOMQNames().PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(Extension context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(License context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(MailingList context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Developer context) {
        if (isElementQName(context.getModel().getPOMQNames().ROLES)) {
            created = new StringListImpl(context.getModel(), element, context.getModel().getPOMQNames().ROLE);
            return;
        }

        createExtensibilityElement(context);
    }

    @Override
    public void visit(ModelList context) {
        if (isElementQName(context.getModel().getPOMQNames().MAILINGLIST) && context.getListClass().equals(MailingList.class)) {
            created = new MailingListImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().DEPENDENCY) && context.getListClass().equals(Dependency.class)) {
            created = new DependencyImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().DEVELOPER) && context.getListClass().equals(Developer.class)) {
            created = new DeveloperImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().CONTRIBUTOR) && context.getListClass().equals(Contributor.class)) {
            created = new ContributorImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().LICENSE) && context.getListClass().equals(License.class)) {
            created = new LicenseImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().PROFILE) && context.getListClass().equals(Profile.class)) {
            created = new ProfileImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().REPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().PLUGINREPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().EXCLUSION) && context.getListClass().equals(Exclusion.class)) {
            created = new ExclusionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().PLUGIN) && context.getListClass().equals(Plugin.class)) {
            created = new PluginImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().REPORTPLUGIN) && context.getListClass().equals(ReportPlugin.class)) {
            created = new ReportPluginImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().REPORTSET) && context.getListClass().equals(ReportSet.class)) {
            created = new ReportSetImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().EXTENSION) && context.getListClass().equals(Extension.class)) {
            created = new ExtensionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().EXECUTION) && context.getListClass().equals(PluginExecution.class)) {
            created = new PluginExecutionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().RESOURCE) && context.getListClass().equals(Resource.class)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getPOMQNames().TESTRESOURCE) && context.getListClass().equals(Resource.class)) {
            created = new ResourceImpl(context.getModel(), element);
        }
    }

    @Override
    public void visit(Configuration context) {
        createExtensibilityElement(context);
    }

    @Override
    public void visit(Properties context) {
        createExtensibilityElement(context);
    }


    @Override
    public void visit(POMExtensibilityElement context) {
        createExtensibilityElement(context);
    }
}
    
