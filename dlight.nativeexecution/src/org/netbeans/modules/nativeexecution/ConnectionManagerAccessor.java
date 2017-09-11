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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccess;
import org.netbeans.modules.nativeexecution.api.util.Authentication;

public abstract class ConnectionManagerAccessor {

    private static volatile ConnectionManagerAccessor DEFAULT;

    public static void setDefault(ConnectionManagerAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException(
                    "ConnectionManagerAccessor is already defined"); // NOI18N
        }

        DEFAULT = accessor;
    }

    public static synchronized ConnectionManagerAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        try {
            Class.forName(ConnectionManager.class.getName(), true,
                    ConnectionManager.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
        }

        return DEFAULT;
    }
//    public abstract Session getConnectionSession(final ExecutionEnvironment env, boolean restoreLostConnection);

    /**
     * Opens and returns a jsch channel in a thread-safe manner. Env must be
     * connected prior to this method call
     *
     * @param env - env where channel should be opened
     * @param type - type of a channel to open
     * @param waitIfNoAvailable - whether should wait for available channel or
     * just return null in case no channel is available at the moment
     * @return Opened channel or null if waitIfNoAvailable is not set and no
     * channel is available
     * @throws InterruptedException
     * @throws JSchException
     * @throws IOException
     */
    public abstract Channel openAndAcquireChannel(final ExecutionEnvironment env, String type, boolean waitIfNoAvailable) throws InterruptedException, JSchException, IOException;

    /**
     * Closes (and releases a resource lock) previously opened by
     * openAndAcquireChannel() jsch channel.
     *
     * @param env
     * @param channel - a channel to close
     * @throws JSchException
     */
    public abstract void closeAndReleaseChannel(final ExecutionEnvironment env, final Channel channel) throws JSchException;

    public abstract void reconnect(final ExecutionEnvironment env) throws IOException;

    public abstract void changeAuth(ExecutionEnvironment env, Authentication auth);

    public abstract JSchAccess getJSchAccess(ExecutionEnvironment env);
}
