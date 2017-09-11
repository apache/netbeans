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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.model.settings.SettingsQName.Version;

/**
 *
 * @author mkleint
 */
public final class SettingsQNames {
    
    public final SettingsQName SETTINGS; // NOI18N
    public final SettingsQName REPOSITORY; // NOI18N
    public final SettingsQName PLUGINREPOSITORY; // NOI18N
    public final SettingsQName RELEASES; // NOI18N
    public final SettingsQName SNAPSHOTS; // NOI18N
    public final SettingsQName PROFILE; // NOI18N
    public final SettingsQName ACTIVATION; // NOI18N
    public final SettingsQName ACTIVATIONPROPERTY; // NOI18N
    public final SettingsQName ACTIVATIONOS; // NOI18N
    public final SettingsQName ACTIVATIONFILE; // NOI18N
    public final SettingsQName ACTIVATIONCUSTOM; // NOI18N

    public final SettingsQName PROFILES; // NOI18N
    public final SettingsQName REPOSITORIES; // NOI18N
    public final SettingsQName PLUGINREPOSITORIES; // NOI18N

    public final SettingsQName ID; //NOI18N
    public final SettingsQName CONFIGURATION; //NOI18N
    public final SettingsQName PROPERTIES; //NOI18N

    public final SettingsQName URL; //NOI18N
    public final SettingsQName NAME; //NOI18N
    public final SettingsQName VALUE; //NOI18N

    public final SettingsQName LAYOUT; //NOI18N

    public final SettingsQName ACTIVEPROFILES; //NOI18N
    public final SettingsQName ACTIVEPROFILE; //NOI18N
    public final SettingsQName PLUGINGROUPS; //NOI18N
    public final SettingsQName PLUGINGROUP; //NOI18N

    public final SettingsQName MIRROROF; //NOI18N
    public final SettingsQName MIRROR; //NOI18N
    public final SettingsQName MIRRORS; //NOI18N

    public final SettingsQName PROXIES; //NOI18N
    public final SettingsQName PROXY; //NOI18N
    public final SettingsQName ACTIVE; //NOI18N
    public final SettingsQName HOST; //NOI18N
    public final SettingsQName PORT; //NOI18N
    public final SettingsQName USERNAME; //NOI18N
    public final SettingsQName PASSWORD; //NOI18N
    public final SettingsQName PROTOCOL; //NOI18N
    public final SettingsQName NONPROXYHOSTS; //NOI18N

    public final SettingsQName SERVER; //NOI18N
    public final SettingsQName SERVERS; //NOI18N

    public final SettingsQName PRIVATEKEY; //NOI18N
    public final SettingsQName PASSPHRASE; //NOI18N

    public final SettingsQName OFFLINE; //NOI18N
    public final SettingsQName INTERACTIVEMODE; //NOI18N
    public final SettingsQName USEPLUGINREGISTRY; //NOI18N
    public final SettingsQName LOCALREPOSITORY; //NOI18N
    
    /** 
     * in 1.1.0 schema only
     */
    public final @NullAllowed SettingsQName MIRROR_LAYOUT_110;
    /** 
     * in 1.1.0 schema only
     */
    public final @NullAllowed SettingsQName MIRROR_OF_LAYOUTS_110;
  
    private final Version version;
    
    
    @Deprecated
    public SettingsQNames(boolean ns, boolean old) {
        this(SettingsQName.resolveVersion(ns, old));
    }
/**
 * 
 * @param version 
 * @since 1.34
 */
    public SettingsQNames(@NonNull Version version) {

        this.version = version;
        SETTINGS = new SettingsQName("settings",version);
        REPOSITORY = new SettingsQName("repository", version);
        PLUGINREPOSITORY = new SettingsQName("pluginRepository", version);
        RELEASES = new SettingsQName("releases",version);
        SNAPSHOTS = new SettingsQName("snapshots",version);
        PROFILE = new SettingsQName("profile",version);
        ACTIVATION = new SettingsQName("activation",version);
        ACTIVATIONPROPERTY = new SettingsQName("property",version);
        ACTIVATIONOS = new SettingsQName("os",version);
        ACTIVATIONFILE = new SettingsQName("file",version);
        ACTIVATIONCUSTOM = new SettingsQName("custom",version);
        PROFILES = new SettingsQName("profiles",version);
        REPOSITORIES = new SettingsQName("repositories",version);
        PLUGINREPOSITORIES = new SettingsQName("pluginRepositories",version);

        ID = new SettingsQName("id",version);
        CONFIGURATION = new SettingsQName("configuration",version);
        PROPERTIES = new SettingsQName("properties",version);
        URL = new SettingsQName("url",version);
        NAME = new SettingsQName("name",version);

        VALUE = new SettingsQName("value",version);

        LAYOUT = new SettingsQName("layout",version);

        ACTIVEPROFILE = new SettingsQName("activeProfile",version);
        ACTIVEPROFILES = new SettingsQName("activeProfiles",version);

        PLUGINGROUP = new SettingsQName("pluginGroup",version);
        PLUGINGROUPS = new SettingsQName("pluginGroups",version);

        MIRROROF = new SettingsQName("mirrorOf",version);
        MIRROR = new SettingsQName("mirror",version);
        MIRRORS = new SettingsQName("mirrors",version);

        PROXIES = new SettingsQName("proxies",version);
        PROXY = new SettingsQName("proxy",version);
        ACTIVE = new SettingsQName("active",version);
        HOST = new SettingsQName("host",version);
        PORT = new SettingsQName("port",version);
        USERNAME = new SettingsQName("username",version);
        PASSWORD = new SettingsQName("password",version);
        PROTOCOL = new SettingsQName("protocol",version);
        NONPROXYHOSTS = new SettingsQName("nonProxyHosts",version);
        //when adding items here, need to add them to the set below as well.

        SERVER = new SettingsQName("server",version);
        SERVERS = new SettingsQName("servers",version);

        PASSPHRASE = new SettingsQName("passphrase",version);
        PRIVATEKEY = new SettingsQName("privateKey",version);

        OFFLINE = new SettingsQName("offline",version);
        USEPLUGINREGISTRY = new SettingsQName("usePluginRegistry",version);
        LOCALREPOSITORY = new SettingsQName("localRepository",version);
        INTERACTIVEMODE = new SettingsQName("interactiveMode",version);
        
        if (Version.NEW_110.equals(version)) {
            MIRROR_LAYOUT_110 = new SettingsQName("layout",version);
            MIRROR_OF_LAYOUTS_110 = new SettingsQName("mirrorOfLayouts",version);
        } else {
            MIRROR_LAYOUT_110 = null;
            MIRROR_OF_LAYOUTS_110 = null;
        }

    }

    public boolean isNSAware() {
        return version.getNamespace() != null;
    }
    @Deprecated
    public boolean isOldNS() {
        return version.equals(Version.OLD);
    }
    /**
     * 
     * @return 
     * @since 1.34
     */
    public Version getNamespaceVersion() {
        return version;
    }


    public Set<QName> getElementQNames() {
        QName[] names = new QName[] {
            SETTINGS.getQName(),
            REPOSITORY.getQName(),
            PLUGINREPOSITORY.getQName(),
            RELEASES.getQName(),
            SNAPSHOTS.getQName(),
            PROFILE.getQName(),
            ACTIVATION.getQName(),
            ACTIVATIONPROPERTY.getQName(),
            ACTIVATIONOS.getQName(),
            ACTIVATIONFILE.getQName(),
            ACTIVATIONCUSTOM.getQName(),
            PROFILES.getQName(),
            REPOSITORIES.getQName(),
            PLUGINREPOSITORIES.getQName(),
            ID.getQName(),
            CONFIGURATION.getQName(),
            PROPERTIES.getQName(),
            URL.getQName(),
            NAME.getQName(),
            VALUE.getQName(),
            LAYOUT.getQName(),
            ACTIVEPROFILE.getQName(),
            ACTIVEPROFILES.getQName(),
            PLUGINGROUPS.getQName(),
            PLUGINGROUP.getQName(),
            MIRROROF.getQName(),
            MIRRORS.getQName(),
            MIRROR.getQName(),
            PROXIES.getQName(),
            PROXY.getQName(),
            ACTIVE.getQName(),
            HOST.getQName(),
            PORT.getQName(),
            USERNAME.getQName(),
            PASSWORD.getQName(),
            PROTOCOL.getQName(),
            NONPROXYHOSTS.getQName(),
            SERVER.getQName(),
            SERVERS.getQName(),
            PRIVATEKEY.getQName(),
            PASSPHRASE.getQName(),
            OFFLINE.getQName(),
            INTERACTIVEMODE.getQName(),
            USEPLUGINREGISTRY.getQName(),
            LOCALREPOSITORY.getQName(),
        };
        List<QName> list = Arrays.asList(names);
        HashSet<QName> toret = new HashSet<QName>(list);
        if (MIRROR_LAYOUT_110 != null) { //in 1.1.0+
            toret.add(MIRROR_LAYOUT_110.getQName());
        }
        if (MIRROR_OF_LAYOUTS_110 != null) {//in 1.1.0+
            toret.add(MIRROR_OF_LAYOUTS_110.getQName());
        }
        return toret;
    }
    
}
