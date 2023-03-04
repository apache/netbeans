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
public interface Dependency extends VersionablePOMComponent {

//  <!--xs:complexType name="Dependency">
//    <xs:all>
//      <xs:element name="groupId" minOccurs="0" type="xs:string">
//      <xs:element name="artifactId" minOccurs="0" type="xs:string">
//      <xs:element name="version" minOccurs="0" type="xs:string">
//      <xs:element name="type" minOccurs="0" type="xs:string" default="jar">
//      <xs:element name="classifier" minOccurs="0" type="xs:string">
//      <xs:element name="scope" minOccurs="0" type="xs:string">
//      <xs:element name="systemPath" minOccurs="0" type="xs:string">
//      <xs:element name="exclusions" minOccurs="0">
//            <xs:element name="exclusion" minOccurs="0" maxOccurs="unbounded" type="Exclusion"/>
//      <xs:element name="optional" minOccurs="0" type="xs:boolean" default="false">
//    </xs:all>
//  </xs:complexType-->

    /**
     * POM RELATED PROPERTY
     * @return
     */
    List<Exclusion> getExclusions();
    void addExclusion(Exclusion exclusion);
    void removeExclusion(Exclusion exclusion);
    Exclusion findExclusionById(String groupId, String artifactId);


    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getType();
    void setType(String type);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getClassifier();
    void setClassifier(String classifier);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getScope();
    void setScope(String scope);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getSystemPath();
    void setSystemPath(String systemPath);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    Boolean isOptional();
    void setOptional(Boolean optional);

}
