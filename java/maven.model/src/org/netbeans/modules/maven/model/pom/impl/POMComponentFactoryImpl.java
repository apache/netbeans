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

import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.pom.spi.ElementFactory;
import org.netbeans.modules.maven.model.pom.spi.POMExtensibilityElementBase;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class POMComponentFactoryImpl implements POMComponentFactory {
    
    private POMModel model;
    
    /**
     * Creates a new instance of POMComponentFactoryImpl
     */
    public POMComponentFactoryImpl(POMModel model) {
        this.model = model;
    }    
 
    private static QName getQName(Element element, POMComponentImpl context) {
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
    public POMComponent create(Element element, POMComponent context) {
        // return new SCAComponentCreateVisitor().create(element, context);
        QName qName = getQName(element, (POMComponentImpl)context);
        ElementFactory elementFactory = ElementFactoryRegistry.getDefault().get(qName);
        return create(elementFactory, element, context);
    }
    
    private POMComponent create(ElementFactory elementFactory, Element element, POMComponent context) {
        if (elementFactory != null ){
            return elementFactory.create(context, element);
        } else {
            return new POMExtensibilityElementBase(model, element);
        }
    }
    
    @Override
    public POMComponent create(POMComponent context, QName qName) {
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
    public Project createProject() {
        return new ProjectImpl(model);
    }

    @Override
    public Parent createParent() {
        return new ParentImpl(model);
    }

    @Override
    public Organization createOrganization() {
        return new OrganizationImpl(model);
    }

    @Override
    public DistributionManagement createDistributionManagement() {
        return new DistributionManagementImpl(model);
    }

    @Override
    public Site createSite() {
        return new SiteImpl(model);
    }

    @Override
    public DeploymentRepository createDistRepository() {
        return new DeploymentRepositoryImpl(model, model.getPOMQNames().DIST_REPOSITORY);
    }

    @Override
    public DeploymentRepository createDistSnapshotRepository() {
        return new DeploymentRepositoryImpl(model, model.getPOMQNames().DIST_SNAPSHOTREPOSITORY);
    }

    @Override
    public Prerequisites createPrerequisites() {
        return new PrerequisitesImpl(model);
    }

    @Override
    public Contributor createContributor() {
        return new ContributorImpl(model);
    }

    @Override
    public Scm createScm() {
        return new ScmImpl(model);
    }

    @Override
    public IssueManagement createIssueManagement() {
        return new IssueManagementImpl(model);
    }

    @Override
    public CiManagement createCiManagement() {
        return new CiManagementImpl(model);
    }

    @Override
    public Notifier createNotifier() {
        return new NotifierImpl(model);
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
        return new RepositoryPolicyImpl(model, model.getPOMQNames().RELEASES);
    }

    @Override
    public RepositoryPolicy createSnapshotRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, model.getPOMQNames().SNAPSHOTS);
    }


    @Override
    public Profile createProfile() {
        return new ProfileImpl(model);
    }

    @Override
    public BuildBase createBuildBase() {
        return new BuildBaseImpl(model);
    }

    @Override
    public Plugin createPlugin() {
        return new PluginImpl(model);
    }

    @Override
    public Dependency createDependency() {
        return new DependencyImpl(model);
    }
    

    @Override
    public Exclusion createExclusion() {
        return new ExclusionImpl(model);
    }

    @Override
    public PluginExecution createExecution() {
        return new PluginExecutionImpl(model);
    }

    @Override
    public Resource createResource() {
        return new ResourceImpl(model, false);
    }

    @Override
    public Resource createTestResource() {
        return new ResourceImpl(model, true);
    }

    @Override
    public PluginManagement createPluginManagement() {
        return new PluginManagementImpl(model);
    }

    @Override
    public Reporting createReporting() {
        return new ReportingImpl(model);
    }

    @Override
    public ReportPlugin createReportPlugin() {
        return new ReportPluginImpl(model);
    }

    @Override
    public ReportSet createReportSet() {
        return new ReportSetImpl(model);
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
    public DependencyManagement createDependencyManagement() {
        return new DependencyManagementImpl(model);
    }

    @Override
    public Build createBuild() {
        return new BuildImpl(model);
    }

    @Override
    public Extension createExtension() {
        return new ExtensionImpl(model);
    }

    @Override
    public License createLicense() {
        return new LicenseImpl(model);
    }

    @Override
    public MailingList createMailingList() {
        return new MailingListImpl(model);
    }


    @Override
    public Developer createDeveloper() {
        return new DeveloperImpl(model);
    }

    @Override
    public Configuration createConfiguration() {
        return new ConfigurationImpl(model);
    }

    @Override
    public Properties createProperties() {
        return new PropertiesImpl(model);
    }

    @Override
    public POMExtensibilityElement createPOMExtensibilityElement(QName name) {
        return new POMExtensibilityElementBase(model, name);
    }

}
