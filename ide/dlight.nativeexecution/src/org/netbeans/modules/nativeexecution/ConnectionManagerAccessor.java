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
