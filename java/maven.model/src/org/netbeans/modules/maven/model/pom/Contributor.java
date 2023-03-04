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
public interface Contributor extends POMComponent {

//  <!--xs:complexType name="Contributor">
//    <xs:all>
//      <xs:element name="name" minOccurs="0" type="xs:string">
//      <xs:element name="email" minOccurs="0" type="xs:string">
//      <xs:element name="url" minOccurs="0" type="xs:string">
//      <xs:element name="organization" minOccurs="0" type="xs:string">
//      <xs:element name="organizationUrl" minOccurs="0" type="xs:string">
//      <xs:element name="roles" minOccurs="0">
//            <xs:element name="role" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//      <xs:element name="timezone" minOccurs="0" type="xs:string">
//      <xs:element name="properties" minOccurs="0">
//            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
//    </xs:all>
//  </xs:complexType-->
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
    String getName();
    void setName(String name);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getEmail();
    void setEmail(String email);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getOrganization();
    void setOrganization(String organization);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getOrganizationUrl();
    void setOrganizationUrl(String url);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getTimezone();
    void setTimezone(String zone);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    List<String> getRoles();
    void addRole(String role);
    void removeRole(String role);
}
