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
public interface Server extends SettingsComponent {

//  <xs:complexType name="Server">
//      <xs:element name="username" minOccurs="0" type="xs:string">
//      <xs:element name="password" minOccurs="0" type="xs:string">
//      <xs:element name="privateKey" minOccurs="0" type="xs:string">
//      <xs:element name="passphrase" minOccurs="0" type="xs:string">
//      <xs:element name="filePermissions" minOccurs="0" type="xs:string">
//      <xs:element name="directoryPermissions" minOccurs="0" type="xs:string">
//      <xs:element name="configuration" minOccurs="0">
//            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
//      <xs:element name="id" minOccurs="0" type="xs:string" default="default">

    String getId();
    void setId(String id);
    
    String getUsername();
    void setUsername(String username);

    String getPrivateKey();
    void setPrivateKey(String key);

    String getPassphrase();
    void setPassphrase(String pass);

    Configuration getConfiguration();
    void setConfiguration(Configuration config);

}
