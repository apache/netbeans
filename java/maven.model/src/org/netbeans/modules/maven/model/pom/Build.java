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
public interface Build extends POMComponent, BuildBase {

//  <!--xs:complexType name="Build">
//    <xs:all>
//      <xs:element name="sourceDirectory" minOccurs="0" type="xs:string">
//      <xs:element name="scriptSourceDirectory" minOccurs="0" type="xs:string">
//      <xs:element name="testSourceDirectory" minOccurs="0" type="xs:string">
//      <xs:element name="outputDirectory" minOccurs="0" type="xs:string">
//      <xs:element name="testOutputDirectory" minOccurs="0" type="xs:string">
//      <xs:element name="extensions" minOccurs="0">
//            <xs:element name="extension" minOccurs="0" maxOccurs="unbounded" type="Extension"/>
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
    List<Extension> getExtensions();
    void addExtension(Extension extension);
    void removeExtension(Extension extension);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getSourceDirectory();
    void setSourceDirectory(String directory);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getScriptSourceDirectory();
    void setScriptSourceDirectory(String directory);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getTestSourceDirectory();
    void setTestSourceDirectory(String directory);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getOutputDirectory();
    void setOutputDirectory(String directory);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getTestOutputDirectory();
    void setTestOutputDirectory(String directory);

}
