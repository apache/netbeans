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

import java.util.*;

/**
 *
 * @author mkleint
 */
public interface Project extends VersionablePOMComponent, DependencyContainer, RepositoryContainer {
//  <!--xs:complexType name="Model">
//    <xs:all>
//      <xs:element name="parent" minOccurs="0" type="Parent">
//      <xs:element name="modelVersion" minOccurs="0" type="xs:string">
//      <xs:element name="groupId" minOccurs="0" type="xs:string">
//      <xs:element name="artifactId" minOccurs="0" type="xs:string">
//      <xs:element name="packaging" minOccurs="0" type="xs:string" default="jar">
//      <xs:element name="name" minOccurs="0" type="xs:string">
//      <xs:element name="version" minOccurs="0" type="xs:string">
//      <xs:element name="description" minOccurs="0" type="xs:string">
//      <xs:element name="url" minOccurs="0" type="xs:string">
//      <xs:element name="prerequisites" minOccurs="0" type="Prerequisites">
//      <xs:element name="issueManagement" minOccurs="0" type="IssueManagement">
//      <xs:element name="ciManagement" minOccurs="0" type="CiManagement">
//      <xs:element name="inceptionYear" minOccurs="0" type="xs:string">
//      <xs:element name="mailingLists" minOccurs="0">
//            <xs:element name="mailingList" minOccurs="0" maxOccurs="unbounded" type="MailingList"/>
//      <xs:element name="developers" minOccurs="0">
//            <xs:element name="developer" minOccurs="0" maxOccurs="unbounded" type="Developer"/>
//      <xs:element name="contributors" minOccurs="0">
//            <xs:element name="contributor" minOccurs="0" maxOccurs="unbounded" type="Contributor"/>
//      <xs:element name="licenses" minOccurs="0">
//            <xs:element name="license" minOccurs="0" maxOccurs="unbounded" type="License"/>
//      <xs:element name="scm" minOccurs="0" type="Scm">
//      <xs:element name="organization" minOccurs="0" type="Organization">
//      <xs:element name="build" minOccurs="0" type="Build">
//      <xs:element name="profiles" minOccurs="0">
//            <xs:element name="profile" minOccurs="0" maxOccurs="unbounded" type="Profile"/>
//      <xs:element name="modules" minOccurs="0">
//            <xs:element name="module" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//      <xs:element name="repositories" minOccurs="0">
//            <xs:element name="repository" minOccurs="0" maxOccurs="unbounded" type="Repository"/>
//      <xs:element name="pluginRepositories" minOccurs="0">
//            <xs:element name="pluginRepository" minOccurs="0" maxOccurs="unbounded" type="Repository"/>
//      <xs:element name="dependencies" minOccurs="0">
//            <xs:element name="dependency" minOccurs="0" maxOccurs="unbounded" type="Dependency"/>
//      <xs:element name="reports" minOccurs="0">
//            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
//      <xs:element name="reporting" minOccurs="0" type="Reporting">
//      <xs:element name="dependencyManagement" minOccurs="0" type="DependencyManagement">
//      <xs:element name="distributionManagement" minOccurs="0" type="DistributionManagement">
//      <xs:element name="properties" minOccurs="0">
//            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
//    </xs:all>
//  </xs:complexType-->


    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Parent getPomParent();
    public void setPomParent(Parent parent);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getModelVersion();

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getPackaging();
    public void setPackaging(String pack);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getName();
    public void setName(String name);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getDescription();
    public void setDescription(String description);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getURL();
    public void setURL(String url);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Prerequisites getPrerequisites();
    public void setPrerequisites(Prerequisites prerequisites);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public String getInceptionYear();
    public void setInceptionYear(String inceptionYear);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public IssueManagement getIssueManagement();
    public void setIssueManagement(IssueManagement issueManagement);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public CiManagement getCiManagement();
    public void setCiManagement(CiManagement ciManagement);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<MailingList> getMailingLists();
    public void addMailingList(MailingList mailingList);
    public void removeMailingList(MailingList mailingList);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<Developer> getDevelopers();
    public void addDeveloper(Developer developer);
    public void removeDeveloper(Developer developer);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<Contributor> getContributors();
    public void addContributor(Contributor contributor);
    public void removeContributor(Contributor contributor);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<License> getLicenses();
    public void addLicense(License license);
    public void removeLicense(License license);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Scm getScm();
    public void setScm(Scm scm);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Organization getOrganization();
    public void setOrganization(Organization organization);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Build getBuild();
    public void setBuild(Build build);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<Profile> getProfiles();
    public void addProfile(Profile profile);
    public void removeProfile(Profile profile);
    Profile findProfileById(String id);

    public List<String> getModules();
    public void addModule(String module);
    public void removeModule(String module);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Reporting getReporting();
    public void setReporting(Reporting reporting);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public DependencyManagement getDependencyManagement();
    public void setDependencyManagement(DependencyManagement dependencyManagement);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public DistributionManagement getDistributionManagement();
    public void setDistributionManagement(DistributionManagement distributionManagement);


    Properties getProperties();
    void setProperties(Properties props);
}
