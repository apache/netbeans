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
