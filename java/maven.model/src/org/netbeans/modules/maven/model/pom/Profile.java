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
public interface Profile extends IdPOMComponent, DependencyContainer, RepositoryContainer {

//  <!--xs:complexType name="Profile">
//    <xs:all>
//      <xs:element name="id" minOccurs="0" type="xs:string" default="default">
//      <xs:element name="activation" minOccurs="0" type="Activation">
//      <xs:element name="build" minOccurs="0" type="BuildBase">
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

    public Activation getActivation();
    public void setActivation(Activation activation);

    public BuildBase getBuildBase();
    public void setBuildBase(BuildBase buildBase);

    public List<String> getModules();
    public void addModule(String module);
    public void removeModule(String module);
    
    public Reporting getReporting();
    public void setReporting(Reporting reporting);

    public DependencyManagement getDependencyManagement();
    public void setDependencyManagement(DependencyManagement dependencyManagement);

    public DistributionManagement getDistributionManagement();
    public void setDistributionManagement(DistributionManagement distributionManagement);

    Properties getProperties();
    void setProperties(Properties properties);

}
