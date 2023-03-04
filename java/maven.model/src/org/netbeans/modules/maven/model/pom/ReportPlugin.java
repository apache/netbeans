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
public interface ReportPlugin extends VersionablePOMComponent {

//  <!--xs:complexType name="ReportPlugin">
//    <xs:all>
//      <xs:element name="groupId" minOccurs="0" type="xs:string" default="org.apache.maven.plugins">
//      <xs:element name="artifactId" minOccurs="0" type="xs:string">
//      <xs:element name="version" minOccurs="0" type="xs:string">
//      <xs:element name="inherited" minOccurs="0" type="xs:string">
//      <xs:element name="configuration" minOccurs="0">
//            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
//      <xs:element name="reportSets" minOccurs="0">
//            <xs:element name="reportSet" minOccurs="0" maxOccurs="unbounded" type="ReportSet"/>
//    </xs:all>
//  </xs:complexType-->

    public List<ReportSet> getReportSets();
    public void addReportSet(ReportSet reportSet);
    public void removeReportSet(ReportSet reportSet);

    Boolean isInherited();
    void setInherited(Boolean inherited);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    Configuration getConfiguration();
    void setConfiguration(Configuration config);


}
