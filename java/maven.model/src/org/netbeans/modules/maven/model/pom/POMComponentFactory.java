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
package org.netbeans.modules.maven.model.pom;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author mkleint
 */
public interface POMComponentFactory extends ComponentFactory<POMComponent>  {

    /**
     * Creates a domain component generically.
     */
    POMComponent create(POMComponent context, QName qName);
    
    // The following are specific create method for each of the defined 
    // component interfaces

    Project createProject();
    Parent createParent();
    Organization createOrganization();
    DistributionManagement createDistributionManagement();
    Site createSite();
    DeploymentRepository createDistRepository();
    DeploymentRepository createDistSnapshotRepository();
    Prerequisites createPrerequisites();
    Contributor createContributor();
    Scm createScm();
    IssueManagement createIssueManagement();
    CiManagement createCiManagement();
    Notifier createNotifier();
    Repository createRepository();
    Repository createPluginRepository();
    RepositoryPolicy createSnapshotRepositoryPolicy();
    RepositoryPolicy createReleaseRepositoryPolicy();
    Profile createProfile();
    BuildBase createBuildBase();
    Plugin createPlugin();
    Dependency createDependency();
    Exclusion createExclusion();
    PluginExecution createExecution();
    Resource createResource();
    Resource createTestResource();
    PluginManagement createPluginManagement();
    Reporting createReporting();
    ReportPlugin createReportPlugin();
    ReportSet createReportSet();
    Activation createActivation();
    ActivationProperty createActivationProperty();
    ActivationOS createActivationOS();
    ActivationFile createActivationFile();
    ActivationCustom createActivationCustom();
    DependencyManagement createDependencyManagement();
    Build createBuild();
    Extension createExtension();
    License createLicense();
    MailingList createMailingList();
    Developer createDeveloper();
    Configuration createConfiguration();
    Properties createProperties();
    POMExtensibilityElement createPOMExtensibilityElement(QName name);
}
