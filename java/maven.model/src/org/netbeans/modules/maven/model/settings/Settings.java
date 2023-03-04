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

import java.util.*;

/**
 *
 * @author mkleint
 */
public interface Settings extends SettingsComponent {
//  <xs:complexType name="Settings">
//      <xs:element name="localRepository" minOccurs="0" type="xs:string">
//      <xs:element name="interactiveMode" minOccurs="0" type="xs:boolean" default="true">
//      <xs:element name="usePluginRegistry" minOccurs="0" type="xs:boolean" default="false">
//      <xs:element name="offline" minOccurs="0" type="xs:boolean" default="false">
//      <xs:element name="proxies" minOccurs="0">
//            <xs:element name="proxy" minOccurs="0" maxOccurs="unbounded" type="Proxy"/>
//      <xs:element name="servers" minOccurs="0">
//            <xs:element name="server" minOccurs="0" maxOccurs="unbounded" type="Server"/>
//      <xs:element name="mirrors" minOccurs="0">
//            <xs:element name="mirror" minOccurs="0" maxOccurs="unbounded" type="Mirror"/>
//      <xs:element name="profiles" minOccurs="0">
//            <xs:element name="profile" minOccurs="0" maxOccurs="unbounded" type="Profile"/>
//      <xs:element name="activeProfiles" minOccurs="0">
//            <xs:element name="activeProfile" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>
//      <xs:element name="pluginGroups" minOccurs="0">
//            <xs:element name="pluginGroup" minOccurs="0" maxOccurs="unbounded" type="xs:string"/>



    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<Profile> getProfiles();
    public void addProfile(Profile profile);
    public void removeProfile(Profile profile);
    Profile findProfileById(String id);

    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<String> getActiveProfiles();
    public void addActiveProfile(String profileid);
    public void removeActiveProfile(String profileid);

    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<String> getPluginGroups();
    public void addPluginGroup(String group);
    public void removePluginGroup(String group);

    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<Proxy> getProxies();
    public void addProxy(Proxy proxy);
    public void removeProxy(Proxy proxy);

    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<Server> getServers();
    public void addServer(Server server);
    public void removeServer(Server server);

    /**
     * Settings RELATED PROPERTY
     * @return
     */
    public List<Mirror> getMirrors();
    public void addMirror(Mirror mirror);
    public void removeMirror(Mirror mirror);
    Mirror findMirrorById(String id);

    String getLocalRepository();
    void setLocalRepository(String repo);

    Boolean isInteractiveMode();
    void setInteractiveMode(Boolean interactive);

    Boolean isUsePluginRegistry();
    void setUsePluginRegistry(Boolean use);

    Boolean isOffline();
    void setOffline(Boolean offline);

}
