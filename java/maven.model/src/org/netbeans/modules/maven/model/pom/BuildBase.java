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
public interface BuildBase extends PluginContainer {

//  <!--xs:complexType name="BuildBase">
//    <xs:all>
//      <xs:element name="defaultGoal" minOccurs="0" type="xs:string">
//      <xs:element name="resources" minOccurs="0">
//            <xs:element name="resource" minOccurs="0" maxOccurs="unbounded" type="Resource"/>
//      <xs:element name="testResources" minOccurs="0">
//            <xs:element name="testResource" minOccurs="0" maxOccurs="unbounded" type="Resource"/>
//      <xs:element name="directory" minOccurs="0" type="xs:string">
//      <xs:element name="finalName" minOccurs="0" type="xs:string">
//      <xs:element name="filters" minOccurs="0">
//            <xs:element name="filter" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//      <xs:element name="pluginManagement" minOccurs="0" type="PluginManagement">
//      <xs:element name="plugins" minOccurs="0">
//            <xs:element name="plugin" minOccurs="0" maxOccurs="unbounded" type="Plugin"/>
//    </xs:all>
//  </xs:complexType-->

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<Resource> getResources();
    public void addResource(Resource resource);
    public void removeResource(Resource resource);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public List<Resource> getTestResources();
    public void addTestResource(Resource testResource);
    public void removeTestResource(Resource testResource);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public PluginManagement getPluginManagement();
    public void setPluginManagement(PluginManagement pluginManagement);


    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getDefaultGoal();
    void setDefaultGoal(String goal);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getDirectory();
    void setDirectory(String directory);

    String getFinalName();
    void setFinalName(String finalName);

    //TODO filters
}
