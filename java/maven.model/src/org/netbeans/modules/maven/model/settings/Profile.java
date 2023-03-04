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
package org.netbeans.modules.maven.model.settings;

import java.util.*;

/**
 *
 * @author mkleint
 */
public interface Profile extends SettingsComponent {

//  <xs:complexType name="Profile">
//      <xs:element name="id" minOccurs="0" type="xs:string" default="default">
//      <xs:element name="activation" minOccurs="0" type="Activation">
//      <xs:element name="properties" minOccurs="0">
//      <xs:element name="repositories" minOccurs="0">
//            <xs:element name="repository" minOccurs="0" maxOccurs="unbounded" type="Repository"/>
//      <xs:element name="pluginRepositories" minOccurs="0">
//            <xs:element name="pluginRepository" minOccurs="0" maxOccurs="unbounded" type="Repository"/>

    String getId();
    void setId(String id);

    public Activation getActivation();
    public void setActivation(Activation activation);

    public List<Repository> getRepositories();
    public void addRepository(Repository repository);
    public void removeRepository(Repository repository);

    public List<Repository> getPluginRepositories();
    public void addPluginRepository(Repository pluginRepository);
    public void removePluginRepository(Repository pluginRepository);

    Properties getProperties();
    void setProperties(Properties properties);

}
