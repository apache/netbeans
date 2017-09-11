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

package org.netbeans.modules.nativeexecution;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 * The configuration of the environment for a {@link NativeProcess} execution.
 * ExecutionEnvirenment is about "<b>where</b>" to start a native process.
 */
final public class ExecutionEnvironmentImpl implements ExecutionEnvironment, Serializable {

    private final String user;
    private final String host;
    private final int sshPort;
    private final AtomicReference<String> displayNameRef = new AtomicReference<>();
    static final long serialVersionUID = 2098997126628923682L;

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>.
     *
     * @param user user name for ssh connection.
     * @param host host identification string. Either hostname or IP address.
     * @param sshPort port to be used to establish ssh connection.
     */
    /*package-local*/ ExecutionEnvironmentImpl(
            final String user,
            final String host,
            final int sshPort) {

        assert  host != null;
        assert  user != null || ExecutionEnvironmentFactoryServiceImpl.DEFAULT_USER == null;

        // that's caller who is responsible for passing not nulls
        this.user = user;
        
        String aHost = host;
        if (host.indexOf(':') >= 0) { // ipv6 address
            if (!(host.startsWith("[") && host.endsWith("]"))) { // NOI18N
                aHost = '[' + host + ']';
            }
        }
        this.host = aHost;
        this.sshPort = sshPort;
    }

    /**
     * Returns host identification string.
     * @return the same host name/ip string that was used for this
     * <tt>ExecutionEnvironment</tt> creation.
     */
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getHostAddress() {
        return getHost();
    }

    /**
     * Gets a string representation of the environment to show in the UI
     * @return a string representation of the environment for showing in UI
     */
    @Override
    public String getDisplayName() {
        String displayName = displayNameRef.get();
        if (displayName == null) {
            if (sshPort == 0) {
                displayName = HostInfoUtils.LOCALHOST;
            } else {
                StringBuilder sb = new StringBuilder();

                if (user != null) {
                    sb.append(user).append('@');
                }

                sb.append(host);

                if (sshPort != 22) {
                    sb.append(':').append(sshPort);
                }

                displayName = sb.toString();
            }

            String old = displayNameRef.getAndSet(displayName);
            if (old != null) {
                displayName = old;
            }
        }
        return displayName;
    }

    /**
     * Returns string representation of this <tt>ExecutionEnvironment</tt> in
     * form <tt>user@host[:port]</tt>.
     * @return string representation of this <tt>ExecutionEnvironment</tt> in
     *         form user@host[:port]
     */
    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Returns username that is used for ssh connection.
     * @return username for ssh connection establishment.
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * Returns port number that is used for ssh connection.
     * @return port that is used for ssh connection in this environment.
     * <tt>0</tt> means that no ssh connection is required for this environment.
     */
    @Override
    public int getSSHPort() {
        return sshPort;
    }

    /**
     * Returns true if ssh connection is required for this environment.
     *
     * Generally, this means that host itself could be a localhost, but if
     * sshPort is set, it will be treated as a remote one.
     *
     * @return true if ssh connection is required for this environment.
     * @see #isLocal()
     *
     */
    @Override
    public boolean isRemote() {
        return !isLocal();
    }

    /**
     * Returns true if no ssh connection is required to start execution in this
     * environment. I.e. it returns <tt>true</tt> if host is the localhost and
     * no sshPort is specified for this environment.
     * @return true if no ssh connection is required for this environment.
     * @see #isRemote()
     */
    @Override
    public boolean isLocal() {
        return sshPort == 0;
    }

    /**
     * Returns true if <tt>obj</tt> represents the same
     * <tt>ExecutionEnvironment</tt>. Two execution environments are equal if
     * and only if <tt>host</tt>, <tt>user</tt> and <tt>sshPort</tt> are all
     * equal. If <tt>host</tt> refers to the localhost in both environments but
     * different host identification strings were used while creation
     * (i.e. <tt>localhost</tt>; <tt>127.0.0.1</tt>; hostname or it's real IP
     * address) <tt>host</tt>s are still treated as to be equal.
     *
     * @param obj object to compare with
     * @return <tt>true</tt> if this <tt>ExecutionEnvironment</tt> equals to
     * <tt>obj</tt> or not.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExecutionEnvironmentImpl)) {
            return false;
        }

        ExecutionEnvironmentImpl ee = (ExecutionEnvironmentImpl) obj;

        boolean bothLocalhost = ee.isLocal() && isLocal();

        boolean result = (bothLocalhost || ee.host.equals(host)) &&
                ee.user.equals(user) && ee.sshPort == sshPort;

        return result;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 97 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 97 * hash + this.sshPort;
        return hash;
    }

    @Override
    public void prepareForConnection() throws IOException, CancellationException {
    }

    /* Java serialization*/ Object writeReplace() throws ObjectStreamException {
        return new SerializedForm(ExecutionEnvironmentFactory.toUniqueID(this));
    }

    private static class SerializedForm implements Serializable {

        static final long serialVersionUID = -1;
        private final String id;

        public SerializedForm(String id) {
            this.id = id;
        }

        /* Java serialization*/ Object readResolve() throws ObjectStreamException {
            return ExecutionEnvironmentFactory.fromUniqueID(id);
        }
    }
}
