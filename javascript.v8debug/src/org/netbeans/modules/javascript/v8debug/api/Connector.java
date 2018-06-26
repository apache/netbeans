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
