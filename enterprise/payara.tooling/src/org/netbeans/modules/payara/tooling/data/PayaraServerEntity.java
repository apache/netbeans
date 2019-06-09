/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.data;

import java.io.File;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;

/**
 * Payara server entity.
 * <p/>
 * Local Payara Server entity instance which is used when not defined in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class PayaraServerEntity implements PayaraServer {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server name in IDE. Used as key attribute. */
    private String name;

    /** Payara server URL.
     *  Used as key attribute. (PayaraModule.URL_ATTR) */
    private String url;

    /** Payara server host. (PayaraModule.HOSTNAME_ATTR) */
    private String host;

    /** Payara server port. (PayaraModule.HTTPPORT_ATTR) */
    private int port;

    /** Payara server administration port.
     *  (PayaraModule.ADMINPORT_ATTR) */
    private int adminPort;

    /** Payara server administration user name
     *  (PayaraModule.USERNAME_ATTR). */
    private String adminUser;

    /** Payara server administration user password
     *  (PayaraModule.PASSWORD_ATTR). */
    private String adminPassword;

    /** Payara server domains folder. (PayaraModule.DOMAINS_FOLDER_ATTR) */
    private String domainsFolder;

    /** Payara server domain name. (PayaraModule.DOMAIN_NAME_ATTR) */
    private String domainName;

    /** Payara server home (usually glassfish subdirectory under server root
     *  directory (PayaraModule.PAYARA_FOLDER_ATTR). */
    private String serverHome;

    /** Payara server installation root
     *  (PayaraModule.INSTALL_FOLDER_ATTR). */
    private String serverRoot;

    /** Payara server version. */
    private PayaraVersion version;

    /** Payara server administration interface type. */
    private PayaraAdminInterface adminInterface;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public PayaraServerEntity() {
    }

    /**
     * Constructs class instance using server location directory.
     * <p/>
     * @param serverRoot Server installation directory.
     * @param serverHome Server home directory.
     * @param serverUrl Server URL assigned by IDE.
     * @throws DataException When server location does not contain Payara
     *         server.
     */
    public PayaraServerEntity(
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
     * Get Payara server name.
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
     * Set Payara server name.
     * <p/>
     * Key attribute.
     * <p/>
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get Payara server URL.
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
     * Set Payara server URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @param url Server URL to set.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Get Payara server host.
     * <p/>
     * @return The host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set Payara server host.
     * <p/>
     * @param host The host to set.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Get Payara server port.
     * <p/>
     * @return The port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set Payara server port.
     * <p/>
     * @param port The port to set.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Get Payara server administration port.
     * <p/>
     * @return The administration port.
     */
    @Override
    public int getAdminPort() {
        return adminPort;
    }

    /**
     * Set Payara server administration port.
     * <p/>
     * @param adminPort The administration port to set.
     */
    public void setAdminPort(final int adminPort) {
        this.adminPort = adminPort;
    }

    /**
     * Get Payara server administration user name.
     * <p/>
     * @return The adminUser.
     */
    @Override
    public String getAdminUser() {
        return adminUser;
    }

    /**
     * Set Payara server administration user name.
     * <p/>
     * @param adminUser The adminUser to set.
     */
    public void setAdminUser(final String adminUser) {
        this.adminUser = adminUser;
    }

    /**
     * Get Payara server administration user password.
     * <p/>
     * @return The adminPassword.
     */
    @Override
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Set Payara server administration user password.
     * <p/>
     * @param adminPassword The adminPassword to set.
     */
    public void setAdminPassword(final String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * Get Payara server domains folder.
     * <p/>
     * @return Domains folder.
     */
    @Override
    public String getDomainsFolder() {
        return domainsFolder;
    }

    /**
     * Set Payara server domains folder.
     * <p/>
     * @param domainsFolder Domains folder to set.
     */
    public void setDomainsFolder(final String domainsFolder) {
        this.domainsFolder = domainsFolder;
    }

    /**
     * Get Payara server domain name.
     * <p/>
     * @return Server domain name.
     */
    @Override
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set Payara server domain name.
     * <p/>
     * @param domainName Server domain name to set.
     */
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get Payara server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @return Server installation root.
     */
    @Override
    public String getServerHome() {
        return serverHome;
    }

    /**
     * Set Payara server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @param serverHome Server server home directory to set.
     */
    public void setServerHome(final String serverHome) {
        this.serverHome = serverHome;
    }

    /**
     * Get Payara server installation directory.
     * <p/>
     * @return Server server installation directory.
     */
    @Override
    public String getServerRoot() {
        return serverRoot;
    }

    /**
     * Set Payara server server installation directory.
     * <p/>
     * @param serverRoot Server server installation directory to set.
     */
    public void setServerRoot(final String serverRoot) {
        this.serverRoot = serverRoot;
    }

    /**
     * Get Payara server version.
     * <p/>
     * @return The version.
     */
    @Override
    public PayaraVersion getVersion() {
        return version;
    }

    /**
     * Set Payara server version.
     * <p/>
     * @param version The version to set.
     */
    public void setVersion(final PayaraVersion version) {
        this.version = version;
    }

    /**
     * Get Payara server administration interface type.
     * <p/>
     * @return Payara server administration interface type.
     */
    @Override
    public PayaraAdminInterface getAdminInterface() {
        return adminInterface;
    }

    /**
     * Set Payara server administration interface type.
     * <p/>
     * @param adminInterface Payara server administration interface type.
     */
    public void setAdminInterface(
            final PayaraAdminInterface adminInterface) {
        this.adminInterface = adminInterface;
    }

    /**
     * Get information if this Payara server instance is local or remote.
     * <p/>
     * Local Payara server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this Payara server instance
     *         is remote or <code>false</code> otherwise.
     */
    @Override
    public boolean isRemote() {
        return domainsFolder == null;
    }

}
