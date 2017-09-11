/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
