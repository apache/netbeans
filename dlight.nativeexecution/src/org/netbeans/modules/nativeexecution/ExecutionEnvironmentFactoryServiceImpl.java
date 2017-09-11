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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService.class, position = 100)
public final class ExecutionEnvironmentFactoryServiceImpl implements ExecutionEnvironmentFactoryService {

    /*package*/ static final String DEFAULT_USER = System.getProperty("user.name"); //NOI18N
    private static final int DEFAULT_PORT = Integer.getInteger("cnd.remote.port", 22); //NOI18N

    private static final ExecutionEnvironment LOCAL = new ExecutionEnvironmentImpl(DEFAULT_USER, HostInfoUtils.LOCALHOST, 0);
    private static final ConcurrentHashMap<String, ExecutionEnvironment> cache = new ConcurrentHashMap<>();

    /**
     * Returns an instance of <tt>ExecutionEnvironment</tt> for local execution.
     */
    @Override
    public ExecutionEnvironment getLocal() {
        return LOCAL;
    }

    /**
     * Creates a new instance of remote <tt>ExecutionEnvironment</tt>. 
     * using default ssh port (22).
     *
     * @param user user name to be used in this environment
     * @param host host identification string (either hostname or IP address)
     */
    @Override
    public ExecutionEnvironment createNew(String user, String host) {
        return createNew(user, host, DEFAULT_PORT);
    }

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>.
     * It is allowable to pass <tt>null</tt> values for <tt>user</tt> and/or
     * <tt>host</tt> params. In this case
     * <tt>System.getProperty("user.name")</tt> will be used as a username and
     * <tt>HostInfo.LOCALHOST</tt> will be used for <tt>host</tt>.
     * If sshPort == 0 and host identification string represents remote host,
     * port 22 will be used.
     *
     * @param user user name for ssh connection.
     * @param host host identification string. Either hostname or IP address.
     * @param sshPort port to be used to establish ssh connection.
     */
    @Override
    public ExecutionEnvironment createNew(String user, String host, int port) {
        if (user == null) {
            user = DEFAULT_USER; // NOI18N
        }
        if (host == null) {
            host = HostInfoUtils.LOCALHOST;
        }
        if (port == 0) {
            port = DEFAULT_PORT;
        }
        return new ExecutionEnvironmentImpl(user, host, port);
    }

    /**
     * Returns a string representation of the executionEnvironment,
     * so that client can store it (for example, in properties)
     * and restore later via fromUniqueID
     * either user@host or "localhost"
     */
    @Override
    public String toUniqueID(final ExecutionEnvironment executionEnvironment) {
        if (!(executionEnvironment instanceof ExecutionEnvironmentImpl)) {
            return null;
        }

        return toExternalForm((ExecutionEnvironmentImpl) executionEnvironment);
    }

    /**
     * Creates an instance of ExecutionEnvironment
     * by string that was got via toUniqueID() method
     * @param hostKey a string that was returned by toUniqueID() method.
     */
    @Override
    public ExecutionEnvironment fromUniqueID(String hostKey) {
        ExecutionEnvironment env = cache.get(hostKey);
        if (env == null) {
            env = fromExternalForm(hostKey);
            ExecutionEnvironment old = cache.putIfAbsent(hostKey, env);
            if (old != null) {
                env = old;
            }
        }
        return env;
    }

    static String toExternalForm(final ExecutionEnvironmentImpl env) {
        final int sshPort = env.getSSHPort();
        if (sshPort == 0) {
            return HostInfoUtils.LOCALHOST;
        }

        final String host = env.getHost();
        final String user = env.getUser();

        StringBuilder sb = new StringBuilder();
        if (user != null) {
            sb.append(user).append('@');
        }
        sb.append(host).append(':').append(sshPort);
        return sb.toString();
    }

    ExecutionEnvironment fromExternalForm(String externalForm) {
        // TODO: remove this check and refactor clients to use getLocal() instead
        if (HostInfoUtils.LOCALHOST.equals(externalForm) || "127.0.0.1".equals(externalForm) || "::1".contains(externalForm)) { //NOI18N
            return LOCAL;
        }

        String user;
        String host;
        String port;

        int atPos = externalForm.indexOf('@');
        user = (atPos > 0) ? externalForm.substring(0, atPos) : null;
        if (user != null) {
            externalForm = externalForm.substring(user.length() + 1);
        }

        int pos = externalForm.lastIndexOf(':');
        if (pos < 0) {
            port = null;
            host = externalForm;
        } else {
            port = externalForm.substring(pos + 1);
            host = externalForm.substring(0, pos);
        }

        int sshPort = 0;
        if (port != null) {
            try {
                sshPort = Integer.parseInt(port);
            } catch (NumberFormatException ex) {
            }
        }
        return createNew(user, host, sshPort);
    }

    @Override
    public ExecutionEnvironment createNew(String schema) {
        return null;
    }
}
