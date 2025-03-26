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
package org.netbeans.modules.lsp.client.debugger.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.lsp.client.debugger.DAPConfigurationAccessor;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger.Type;
import org.openide.util.Exceptions;

/**Configure and start the Debugger Adapter Protocol (DAP) client. Start with
 * {@link #create(java.io.InputStream, java.io.OutputStream) }.
 *
 * @since 1.29
 */
public class DAPConfiguration {
    private final InputStream in;
    private final OutputStream out;
    private Map<String, Object> configuration = new HashMap<>();
    private String sessionName = "";
    private boolean delayLaunch;

    /**
     * Start the configuration of the DAP client. The provided input and output
     * should will be used to communicate with the server.
     *
     * @param in stream from which the server's output will be read
     * @param out stream to which data for the server will be written
     * @return the DAP client configuration
     */
    public static DAPConfiguration create(InputStream in, OutputStream out) {
        return new DAPConfiguration(in, out);
    }

    private DAPConfiguration(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Add arbitrary configuration which will be sent to the server unmodified.
     *
     * @param configuration the configuration to send to the server
     * @return the DAP client configuration
     */
    public DAPConfiguration addConfiguration(Map<String, Object> configuration) {
        this.configuration.putAll(configuration);
        return this;
    }

    /**
     * Set the name of the UI session that will be created.
     *
     * @param sessionName the name of the UI session.
     * @return the DAP client configuration
     */
    public DAPConfiguration setSessionName(String sessionName) {
        this.sessionName = sessionName;
        return this;
    }

    /**
     * If set, the debuggee will only be launched, or attached to, after full
     * configuration. Should only be used if the given DAP server requires this
     * handling.
     *
     * @return the DAP client configuration
     */
    public DAPConfiguration delayLaunch() {
        this.delayLaunch = true;
        return this;
    }

    /**
     * Attach to an already running DAP server/debuggee, based on the configuration up to
     * this point.
     */
    public void attach() {
        try {
            DAPDebugger.startDebugger(this, Type.ATTACH);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Launch a new debuggee, based on the configuration so far.
     */
    public void launch() {
        try {
            DAPDebugger.startDebugger(this, Type.LAUNCH);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static {
        DAPConfigurationAccessor.setInstance(new DAPConfigurationAccessor() {
            @Override
            public OutputStream getOut(DAPConfiguration config) {
                return config.out;
            }

            @Override
            public InputStream getIn(DAPConfiguration config) {
                return config.in;
            }

            @Override
            public boolean getDelayLaunch(DAPConfiguration config) {
                return config.delayLaunch;
            }

            @Override
            public Map<String, Object> getConfiguration(DAPConfiguration config) {
                return config.configuration;
            }

            @Override
            public String getSessionName(DAPConfiguration config) {
                return config.sessionName;
            }
        });
    }
}
