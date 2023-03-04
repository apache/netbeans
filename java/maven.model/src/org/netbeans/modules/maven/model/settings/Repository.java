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


/**
 *
 * @author mkleint
 */
public interface Repository extends SettingsComponent {

//  <!--xs:complexType name="Repository">
//    <xs:all>
//      <xs:element name="releases" minOccurs="0" type="RepositoryPolicy">
//      <xs:element name="snapshots" minOccurs="0" type="RepositoryPolicy">
//      <xs:element name="id" minOccurs="0" type="xs:string">
//      <xs:element name="name" minOccurs="0" type="xs:string">
//      <xs:element name="url" minOccurs="0" type="xs:string">
//      <xs:element name="layout" minOccurs="0" type="xs:string" default="default">
//    </xs:all>
//  </xs:complexType-->

    String getId();
    void setId(String id);


    public RepositoryPolicy getReleases();
    public void setReleases(RepositoryPolicy releases);

    public RepositoryPolicy getSnapshots();
    public void setSnapshots(RepositoryPolicy snapshots);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getName();
    void setName(String name);
    
    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getUrl();
    void setUrl(String url);
    
    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getLayout();
    void setLayout(String layout);

}
