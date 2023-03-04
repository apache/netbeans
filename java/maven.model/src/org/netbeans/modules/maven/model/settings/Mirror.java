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
public interface Mirror extends SettingsComponent {

//  <xs:complexType name="Mirror">
//      <xs:element name="mirrorOf" minOccurs="0" type="xs:string">
//      <xs:element name="name" minOccurs="0" type="xs:string">
//      <xs:element name="url" minOccurs="0" type="xs:string">
//      <xs:element name="id" minOccurs="0" type="xs:string" default="default">

    String getId();
    void setId(String id);

    String getName();
    void setName(String name);

    String getUrl();
    void setUrl(String url);

    String getMirrorOf();
    void setMirrorOf(String mirrorof);
    
    /**
     * only applicable in <code>SettingsQName.Version.NEW_110</code>
     * @return 
     */
    String getLayout();
    /**
     * only applicable in <code>SettingsQName.Version.NEW_110</code>, noop otherwise
     * @param layout 
     */
    void setLayout(String layout);
     /**
     * only applicable in <code>SettingsQName.Version.NEW_110</code>
     * @return 
     */
    String getMirrorOfLayouts();
    /**
     * only applicable in <code>SettingsQName.Version.NEW_110</code>, noop otherwise 
     * @param mirrorOf
     */
    void setMirrorOfLayouts(String mirrorOf);
}
