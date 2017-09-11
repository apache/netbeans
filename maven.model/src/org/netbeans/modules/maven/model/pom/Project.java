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
