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

/**
 * Payara server entity interface.
 * <p/>
 * Payara Server entity interface allows to use foreign entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface PayaraServer {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods                                                      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara server name.
     * <p/>
     * @return The name.
     */
    public String getName();

    /**
     * Get Payara server host.
     * <p/>
     * @return The host.
     */
    public String getHost();

    /**
     * Get Payara server port.
     * <p/>
     * @return The port.
     */
    public int getPort();

   /**
     * Get Payara server administration port.
     * <p/>
     * @return The administration port.
     */
    public int getAdminPort();

    /**
     * Get Payara server administration user name.
     * <p/>
     * @return The adminUser.
     */
    public String getAdminUser();

    /**
     * Get Payara server administration user password.
     * <p/>
     * @return The adminPassword.
     */
    public String getAdminPassword();

    /**
     * Get Payara server domains folder.
     * <p/>
     * @return Domains folder.
     */
    public String getDomainsFolder();

    /**
     * Get Payara server domain name.
     * <p/>
     * @return Server domain name.
     */
    public String getDomainName();

    /**
     * Get Payara server URL.
     * <p/>
     * @return Server URL.
     */
    public String getUrl();

    /**
     * Get Payara server home which is <code>payara</code> subdirectory
     * under installation root.
     * <p/>
     * @return Server installation root.
     */
    public String getServerHome();

    /**
     * Get Payara server installation directory.
     * <p/>
     * @return Server server installation directory.
     */
    public String getServerRoot();

    /** Get Payara server version.
     * <p/>
     * @return The version
     */
    public PayaraVersion getVersion();

    /**
     * Get Payara server administration interface type.
     * <p/>
     * @return Payara server administration interface type.
     */
    public PayaraAdminInterface getAdminInterface();

    /**
     * Get information if this Payara server instance is local or remote.
     * <p/>
     * Local Payara server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this Payara server instance
     *         is remote or <code>false</code> otherwise.
     */
    public boolean isRemote();

}
