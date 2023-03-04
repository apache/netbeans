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

import java.util.List;


/**
 *
 * @author mkleint
 */
public interface Resource extends POMComponent {

//  <!--xs:complexType name="Resource">
//    <xs:all>
//      <xs:element name="targetPath" minOccurs="0" type="xs:string">
//      <xs:element name="filtering" minOccurs="0" type="xs:boolean" default="false">
//      <xs:element name="directory" minOccurs="0" type="xs:string">
//      <xs:element name="includes" minOccurs="0">
//            <xs:element name="include" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//      <xs:element name="excludes" minOccurs="0">
//            <xs:element name="exclude" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//    </xs:all>
//  </xs:complexType-->

    String getDirectory();
    void setDirectory(String directory);

    String getTargetPath();
    void setTargetPath(String path);

    Boolean isFiltering();
    void setFiltering(Boolean filtering);

    public List<String> getIncludes();
    public void addInclude(String include);
    public void removeInclude(String include);

    public List<String> getExcludes();
    public void addExclude(String exclude);
    public void removeExclude(String exclude);

}
