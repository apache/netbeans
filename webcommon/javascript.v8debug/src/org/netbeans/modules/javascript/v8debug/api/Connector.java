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

package org.netbeans.modules.javascript.v8debug.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.v8debug.V8Debugger;

/**
 * V8 debugger connector. Starts up a new debugging session.
 * 
 * @author Martin Entlicher
 */
public final class Connector {
    
    private Connector() {}
    
    /**
     * Connects debugger to a V8 JavaScript engine running in a debug mode.
     * @param properties The connection properties
     * @param finishCallback A runnable called when debugger finishes.
     *                       Might do some cleanup, e.g. kill the V8 engine.
     *                       Can be <code>null</code>.
     * @throws IOException When the connection can not be established.
     */
    public static void connect(Properties properties, @NullAllowed Runnable finishCallback) throws IOException {
        V8Debugger.startSession(properties, finishCallback);
    }
    
    /**
     * Debugger connection properties.
     */
    public static final class Properties {
        
        private final String hostName;
        private final int port;
        private final List<String> localPaths;
        private final List<String> serverPaths;
        private final Collection<String> localPathExclusionFilters;
        
        /**
         * Creates simple host and port connection properties.
         * @param hostName The host name
         * @param port The port number
         */
        public Properties(@NullAllowed String hostName, int port) {
            this(hostName, port, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_SET);
        }
        
        /**
         * Creates properties with specification of local and server paths.
         * <code>localPaths</code> limits the local paths that are used for breakpoints
         * submissions. Only sources below <code>localPaths</code> are considered
         * for debugging. There can be excluded some folders from these local paths
         * via <code>localPathExclusionFilters</code>.
         * <p>
         * If connecting to a remote server, <code>serverPaths</code> should be
         * defined and must contain the same number of entries as <code>localPaths</code>
         * in the same order, representing a server location of the copy of local files.
         * @param hostName The host name
         * @param port The port number
         * @param localPaths A list of local folders containing source files.
         * @param serverPaths Expected to be empty for local connections.
         *                    For remote connection it must have the same size
         *                    as <code>localPaths</code> and contain the server
         *                    location of folders corresponding to local source
         *                    folders in the same order.
         * @param localPathExclusionFilters List of folders that are to be excluded
         *                                  from sources.
         */
        public Properties(@NullAllowed String hostName, int port,
                          List<String> localPaths, List<String> serverPaths,
                          Collection<String> localPathExclusionFilters) {
            this.hostName = hostName;
            this.port = port;
            this.localPaths = localPaths;
            this.serverPaths = serverPaths;
            this.localPathExclusionFilters = localPathExclusionFilters;
            if (!serverPaths.isEmpty() && localPaths.size() != serverPaths.size()) {
                throw new IllegalArgumentException("Different size of localPaths and serverPaths: "+
                                                   "localPaths = "+localPaths+", serverPaths = "+serverPaths);
            }
        }

        @CheckForNull
        public String getHostName() {
            return hostName;
        }

        public int getPort() {
            return port;
        }

        public List<String> getLocalPaths() {
            return localPaths;
        }

        public List<String> getServerPaths() {
            return serverPaths;
        }

        public Collection<String> getLocalPathExclusionFilters() {
            return localPathExclusionFilters;
        }
        
    }
}
