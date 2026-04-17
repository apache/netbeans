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

package org.netbeans.modules.weblogic.common.api;

import java.io.File;
import java.util.Objects;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicConfiguration {

    private final String id;

    private final File serverHome;

    private final File domainHome;

    private final String host;

    private final Integer port;

    private final Boolean secured;

    private final DomainConfiguration config;

    private final Credentials credentials;

    // @GuardedBy("this")
    private WebLogicLayout layout;

    // @GuardedBy("this")
    private WebLogicRemote remote;

    private WebLogicConfiguration(File serverHome, File domainHome, DomainConfiguration config,
            String host, Integer port, Boolean secured, Credentials credentials) {
        this.serverHome = serverHome;
        this.domainHome = domainHome;
        this.host = host;
        this.port = port;
        this.secured = secured;
        this.credentials = credentials;

        if (domainHome != null) {
            assert config != null;
            id = serverHome + ":" + domainHome;
            this.config = config;
        } else {
            id = host + ":" + port;
            this.config = null;
        }
    }

    @CheckForNull
    public static WebLogicConfiguration forLocalDomain(File serverHome, File domainHome,
            Credentials credentials) {
        DomainConfiguration config = DomainConfiguration.getInstance(domainHome, true);
        if (config == null) {
            return null;
        }
        return new WebLogicConfiguration(serverHome, domainHome, config, null, null, null, credentials);
    }

    @NonNull
    public static WebLogicConfiguration forRemoteDomain(File serverHome, String host,
            int port, boolean secured, Credentials credentials) {
        return new WebLogicConfiguration(serverHome, null, null, host, port, secured, credentials);
    }

    public String getId() {
        return id;
    }

    public boolean isRemote() {
        return domainHome == null;
    }

    @NonNull
    public File getServerHome() {
        return serverHome;
    }

    @NonNull
    public String getUsername() {
        return credentials.getUsername();
    }

    @NonNull
    public String getPassword() {
        return credentials.getPassword();
    }

    public String getHost() {
        if (host != null) {
            return host;
        }
        return config.getHost();
    }

    public int getPort() {
        if (port != null) {
            return port;
        }
        return config.getPort();
    }

    public boolean isSecured() {
        if (secured != null) {
            return secured;
        }
        return config.isSecured();
    }

    @NonNull
    public String getAdminURL() {
        if (config == null) {
            return getAdminURL(host, port, secured);
        }
        synchronized (config) {
            return getAdminURL(config.getHost(), config.getPort(), config.isSecured());
        }
    }

    @NullUnknown
    public File getDomainHome() {
        return domainHome;
    }

    @NullUnknown
    public String getDomainName() {
        if (config == null) {
            return null;
        }
        return config.getName();
    }

    @NullUnknown
    public String getDomainAdminServer() {
        if (config == null) {
            return null;
        }
        return config.getAdminServer();
    }

    @NullUnknown
    public File getLogFile() {
        if (config == null) {
            return null;
        }
        return config.getLogFile();
    }

    @CheckForNull
    public Version getDomainVersion() {
        if (config == null) {
            return null;
        }
        return config.getVersion();
    }

    @NonNull
    public synchronized WebLogicLayout getLayout() {
        if (layout == null) {
            layout = new WebLogicLayout(this);
        }
        return layout;
    }

    @NonNull
    public synchronized WebLogicRemote getRemote() {
        if (remote == null) {
            remote = new WebLogicRemote(this);
        }
        return remote;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebLogicConfiguration other = (WebLogicConfiguration) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    private static String getAdminURL(String host, int port, boolean secured) {
        StringBuilder sb = new StringBuilder();
        if (secured) {
            sb.append("t3s://"); // NOI18N
        } else {
            sb.append("t3://"); // NOI18N
        }
        sb.append(host).append(":").append(port); // NOI18N
        return sb.toString();
    }

    public static interface Credentials {

        @NonNull
        String getUsername();

        @NonNull
        String getPassword();

    }
}
