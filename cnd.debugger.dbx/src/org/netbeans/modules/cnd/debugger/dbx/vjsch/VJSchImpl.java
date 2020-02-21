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
package org.netbeans.modules.cnd.debugger.dbx.vjsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.sun.tools.swdev.glue.vjsch.VirtJSch;
import com.sun.tools.swdev.glue.vjsch.VirtSession;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccess;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccessor;
import org.openide.util.Exceptions;

/**
 *
 *
 */
public final class VJSchImpl implements VirtJSch {

    public VirtSession getSession(String username, String hostname, int port) throws JSchException {
        return new VirtSessionImpl(username, hostname, port);
    }

    private static class VirtSessionImpl implements VirtSession {

        private final JSchAccess jschAccess;

        public VirtSessionImpl(String username, String hostname, int port) {
            jschAccess = JSchAccessor.get(ExecutionEnvironmentFactory.createNew(username, hostname, port));
        }

        public String getConfig(String key) {
            return jschAccess.getConfig(key);
        }

        public String getServerVersion() {
            try {
                return jschAccess.getServerVersion();
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            }
            throw new InternalError();
        }

        public void connect() throws JSchException {
        }

        public void disconnect() {
        }

        public Channel openChannel(String type) throws JSchException {
            try {
                return jschAccess.openChannel(type);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
            jschAccess.setPortForwardingR(bind_address, rport, host, lport);
        }

        public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
            return jschAccess.setPortForwardingL(lport, host, rport);
        }

        public void delPortForwardingR(int rport) throws JSchException {
            jschAccess.delPortForwardingR(rport);
        }

        public void delPortForwardingL(int lport) throws JSchException {
            jschAccess.delPortForwardingL(lport);
        }

        public void disposeChannel(Channel channel) {
            try {
                jschAccess.releaseChannel(channel);
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
