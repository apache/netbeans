/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.data;

import java.io.File;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;

/**
 * GlassFish server entity.
 * <p/>
 * Local GlassFish Server entity instance which is used when not defined in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishServerEntity implements GlassFishServer {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server name in IDE. Used as key attribute. */
    private String name;

    /** GlassFish server URL.
     *  Used as key attribute. (GlassfishModule.URL_ATTR) */
    private String url;

    /** GlassFish server host. (GlassfishModule.HOSTNAME_ATTR) */
    private String host;

    /** GlassFish server port. (GlassfishModule.HTTPPORT_ATTR) */
    private int port;

    /** GlassFish server administration port.
     *  (GlassfishModule.ADMINPORT_ATTR) */
    private int adminPort;

    /** GlassFish server administration user name
     *  (GlassfishModule.USERNAME_ATTR). */
    private String adminUser;

    /** GlassFish server administration user password
     *  (GlassfishModule.PASSWORD_ATTR). */
    private String adminPassword;

    /** GlassFish server domains folder. (GlassfishModule.DOMAINS_FOLDER_ATTR) */
    private String domainsFolder;

    /** GlassFish server domain name. (GlassfishModule.DOMAIN_NAME_ATTR) */
    private String domainName;

    /** GlassFish server home (usually glassfish subdirectory under server root
     *  directory (GlassfishModule.GLASSFISH_FOLDER_ATTR). */
    private String serverHome;

    /** GlassFish server installation root
     *  (GlassfishModule.INSTALL_FOLDER_ATTR). */
    private String serverRoot;

    /** GlassFish server version. */
    private GlassFishVersion version;

    /** GlassFish server administration interface type. */
    private GlassFishAdminInterface adminInterface;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public GlassFishServerEntity() {
    }

    /**
     * Constructs class instance using server location directory.
     * <p/>
     * @param serverRoot Server installation directory.
     * @param serverHome Server home directory.
     * @param serverUrl Server URL assigned by IDE.
     * @throws DataException When server location does not contain GlassFish
     *         server.
     */
    public GlassFishServerEntity(
            final String serverName, final String serverRoot,
            final String serverHome, final String serverUrl) {
        if (serverRoot == null) {
            throw new DataException(DataException.SERVER_ROOT_NULL);
        }
        if (serverHome == null) {
            throw new DataException(DataException.SERVER_HOME_NULL);
        }
        if (serverUrl == null) {
            throw new DataException(DataException.SERVER_URL_NULL);
        }
        File root = new File(serverHome);
        if (!root.isDirectory()) {
            throw new DataException(DataException.SERVER_ROOT_NONEXISTENT,
                    serverHome);
        }
        File home = new File(serverHome);
        if (!home.isDirectory()) {
            throw new DataException(DataException.SERVER_HOME_NONEXISTENT,
                    serverHome);
        }
        this.version = ServerUtils.getServerVersion(serverHome);
        if (this.version == null) {
            throw new DataException(DataException.SERVER_HOME_NO_VERSION,
                    serverHome);
        }
        this.name = serverName;
        this.url = serverUrl;
        this.serverRoot = serverRoot;
        this.serverHome = serverHome;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server name.
     * <p/>
     * Key attribute.
     * <p/>
     * @return The name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set GlassFish server name.
     * <p/>
     * Key attribute.
     * <p/>
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get GlassFish server URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @return Server URL.
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Set GlassFish server URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @param url Server URL to set.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Get GlassFish server host.
     * <p/>
     * @return The host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set GlassFish server host.
     * <p/>
     * @param host The host to set.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Get GlassFish server port.
     * <p/>
     * @return The port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set GlassFish server port.
     * <p/>
     * @param port The port to set.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Get GlassFish server administration port.
     * <p/>
     * @return The administration port.
     */
    @Override
    public int getAdminPort() {
        return adminPort;
    }

    /**
     * Set GlassFish server administration port.
     * <p/>
     * @param adminPort The administration port to set.
     */
    public void setAdminPort(final int adminPort) {
        this.adminPort = adminPort;
    }

    /**
     * Get GlassFish server administration user name.
     * <p/>
     * @return The adminUser.
     */
    @Override
    public String getAdminUser() {
        return adminUser;
    }

    /**
     * Set GlassFish server administration user name.
     * <p/>
     * @param adminUser The adminUser to set.
     */
    public void setAdminUser(final String adminUser) {
        this.adminUser = adminUser;
    }

    /**
     * Get GlassFish server administration user password.
     * <p/>
     * @return The adminPassword.
     */
    @Override
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Set GlassFish server administration user password.
     * <p/>
     * @param adminPassword The adminPassword to set.
     */
    public void setAdminPassword(final String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * Get GlassFish server domains folder.
     * <p/>
     * @return Domains folder.
     */
    @Override
    public String getDomainsFolder() {
        return domainsFolder;
    }

    /**
     * Set GlassFish server domains folder.
     * <p/>
     * @param domainsFolder Domains folder to set.
     */
    public void setDomainsFolder(final String domainsFolder) {
        this.domainsFolder = domainsFolder;
    }

    /**
     * Get GlassFish server domain name.
     * <p/>
     * @return Server domain name.
     */
    @Override
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set GlassFish server domain name.
     * <p/>
     * @param domainName Server domain name to set.
     */
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get GlassFish server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @return Server installation root.
     */
    @Override
    public String getServerHome() {
        return serverHome;
    }

    /**
     * Set GlassFish server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @param serverHome Server server home directory to set.
     */
    public void setServerHome(final String serverHome) {
        this.serverHome = serverHome;
    }

    /**
     * Get GlassFish server installation directory.
     * <p/>
     * @return Server server installation directory.
     */
    @Override
    public String getServerRoot() {
        return serverRoot;
    }

    /**
     * Set GlassFish server server installation directory.
     * <p/>
     * @param serverRoot Server server installation directory to set.
     */
    public void setServerRoot(final String serverRoot) {
        this.serverRoot = serverRoot;
    }

    /**
     * Get GlassFish server version.
     * <p/>
     * @return The version.
     */
    @Override
    public GlassFishVersion getVersion() {
        return version;
    }

    /**
     * Set GlassFish server version.
     * <p/>
     * @param version The version to set.
     */
    public void setVersion(final GlassFishVersion version) {
        this.version = version;
    }

    /**
     * Get GlassFish server administration interface type.
     * <p/>
     * @return GlassFish server administration interface type.
     */
    @Override
    public GlassFishAdminInterface getAdminInterface() {
        return adminInterface;
    }

    /**
     * Set GlassFish server administration interface type.
     * <p/>
     * @param adminInterface GlassFish server administration interface type.
     */
    public void setAdminInterface(
            final GlassFishAdminInterface adminInterface) {
        this.adminInterface = adminInterface;
    }

    /**
     * Get information if this GlassFish server instance is local or remote.
     * <p/>
     * Local GlassFish server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this GlassFish server instance
     *         is remote or <code>false</code> otherwise.
     */
    @Override
    public boolean isRemote() {
        return domainsFolder == null;
    }

}
