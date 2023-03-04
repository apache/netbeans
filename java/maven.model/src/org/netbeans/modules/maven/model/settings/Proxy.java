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
public interface Proxy extends SettingsComponent {

//  <xs:complexType name="Proxy">
//      <xs:element name="active" minOccurs="0" type="xs:boolean" default="false">
//      <xs:element name="protocol" minOccurs="0" type="xs:string" default="http">
//      <xs:element name="username" minOccurs="0" type="xs:string">
//      <xs:element name="password" minOccurs="0" type="xs:string">
//      <xs:element name="port" minOccurs="0" type="xs:int" default="8080">
//      <xs:element name="host" minOccurs="0" type="xs:string">
//      <xs:element name="nonProxyHosts" minOccurs="0" type="xs:string">
//      <xs:element name="id" minOccurs="0" type="xs:string" default="default">

    String getId();
    void setId(String id);

    Boolean getActive();
    void setActive(Boolean active);

    String getProtocol();
    void setProtocol(String protocol);

    String getUsername();
    void setUsername(String username);

    String getPassword();
    void setPassword(String password);

    String getPort();
    void setPort(String port);

    String getHost();
    void setHost(String host);

    String getNonProxyHosts();
    void setNonProxyHosts(String nonProxyHosts);

}
