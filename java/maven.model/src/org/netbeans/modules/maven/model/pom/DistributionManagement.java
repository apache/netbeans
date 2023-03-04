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


/**
 *
 * @author mkleint
 */
public interface DistributionManagement extends POMComponent {

//  <!--xs:complexType name="DistributionManagement">
//    <xs:all>
//      <xs:element name="repository" minOccurs="0" type="DeploymentRepository">
//      <xs:element name="snapshotRepository" minOccurs="0" type="DeploymentRepository">
//      <xs:element name="site" minOccurs="0" type="Site">
//      <xs:element name="downloadUrl" minOccurs="0" type="xs:string">
//      <xs:element name="relocation" minOccurs="0" type="Relocation">
//      <xs:element name="status" minOccurs="0" type="xs:string">
//    </xs:all>
//  </xs:complexType-->
//
    /**
     * POM RELATED PROPERTY
     * @return
     */
    public DeploymentRepository getRepository();
    public void setRepository(DeploymentRepository distRepository);

        /**
     * POM RELATED PROPERTY
     * @return
     */
    public DeploymentRepository getSnapshotRepository();
    public void setSnapshotRepository(DeploymentRepository distSnapshotRepository);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    public Site getSite();
    public void setSite(Site site);

    /**
     * POM RELATED PROPERTY
     * @return
     */
    String getDownloadUrl();
    void setDownloadUrl(String url);

}
