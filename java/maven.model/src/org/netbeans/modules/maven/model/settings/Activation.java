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
public interface Activation extends SettingsComponent {

//  <!--xs:complexType name="Activation">
//    <xs:all>
//      <xs:element name="activeByDefault" minOccurs="0" type="xs:boolean" default="false">
//      <xs:element name="jdk" minOccurs="0" type="xs:string">
//      <xs:element name="os" minOccurs="0" type="ActivationOS">
//      <xs:element name="property" minOccurs="0" type="ActivationProperty">
//      <xs:element name="file" minOccurs="0" type="ActivationFile">
//      <xs:element name="custom" minOccurs="0" type="ActivationCustom">
//    </xs:all>
//  </xs:complexType-->


    public ActivationOS getActivationOS();
    public void setActivationOS(ActivationOS activationOS);

    public ActivationProperty getActivationProperty();
    public void setActivationProperty(ActivationProperty activationProperty);

    public ActivationFile getActivationFile();
    public void setActivationFile(ActivationFile activationFile);

    public ActivationCustom getActivationCustom();
    public void setActivationCustom(ActivationCustom activationCustom);

}
