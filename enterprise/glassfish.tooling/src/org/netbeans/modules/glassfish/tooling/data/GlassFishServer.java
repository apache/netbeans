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
package org.netbeans.modules.glassfish.tooling.data;

/**
 * GlassFish server entity interface.
 * <p/>
 * GlassFish Server entity interface allows to use foreign entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishServer {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods                                                      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server name.
     * <p/>
     * @return The name.
     */
    public String getName();

    /**
     * Get GlassFish server host.
     * <p/>
     * @return The host.
     */
    public String getHost();

    /**
     * Get GlassFish server port.
     * <p/>
     * @return The port.
     */
    public int getPort();

   /**
     * Get GlassFish server administration port.
     * <p/>
     * @return The administration port.
     */
    public int getAdminPort();

    /**
     * Get GlassFish server administration user name.
     * <p/>
     * @return The adminUser.
     */
    public String getAdminUser();

    /**
     * Get GlassFish server administration user password.
     * <p/>
     * @return The adminPassword.
     */
    public String getAdminPassword();

    /**
     * Get GlassFish server domains folder.
     * <p/>
     * @return Domains folder.
     */
    public String getDomainsFolder();

    /**
     * Get GlassFish server domain name.
     * <p/>
     * @return Server domain name.
     */
    public String getDomainName();

    /**
     * Get GlassFish server URL.
     * <p/>
     * @return Server URL.
     */
    public String getUrl();

    /**
     * Get GlassFish server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @return Server installation root.
     */
    public String getServerHome();

    /**
     * Get GlassFish server installation directory.
     * <p/>
     * @return Server server installation directory.
     */
    public String getServerRoot();

    /** Get GlassFish server version.
     * <p/>
     * @return The version
     */
    public GlassFishVersion getVersion();

    /**
     * Get GlassFish server administration interface type.
     * <p/>
     * @return GlassFish server administration interface type.
     */
    public GlassFishAdminInterface getAdminInterface();

    /**
     * Get information if this GlassFish server instance is local or remote.
     * <p/>
     * Local GlassFish server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this GlassFish server instance
     *         is remote or <code>false</code> otherwise.
     */
    public boolean isRemote();

}
