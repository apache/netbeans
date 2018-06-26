/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.weblogic.common.api;

import java.io.File;
import java.util.Objects;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.openide.util.WeakSet;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicConfiguration {

    private static final WeakSet<WebLogicConfiguration> INSTANCES = new WeakSet<WebLogicConfiguration>();

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
        WebLogicConfiguration instance = new WebLogicConfiguration(serverHome, domainHome, config, null, null, null, credentials);
        synchronized (INSTANCES) {
            return INSTANCES.putIfAbsent(instance);
        }
    }

    @NonNull
    public static WebLogicConfiguration forRemoteDomain(File serverHome, String host,
            int port, boolean secured, Credentials credentials) {
        WebLogicConfiguration instance = new WebLogicConfiguration(serverHome, null, null, host, port, secured, credentials);
        synchronized (INSTANCES) {
            return INSTANCES.putIfAbsent(instance);
        }
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
