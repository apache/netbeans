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
package org.netbeans.modules.php.dbgp;

import java.util.List;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class DebuggerOptions {

    private static final DebuggerOptions GLOBAL_INSTANCE = new DefaultGlobal();
    int port = -1;
    int maxData = -2;
    int maxChildren = -1;
    int maxStructureDepth = -1;
    Boolean debugForFirstPageOnly;
    String projectEncoding;
    List<Pair<String, String>> pathMapping;
    Pair<String, Integer> debugProxy;

    public static DebuggerOptions getGlobalInstance() {
        return GLOBAL_INSTANCE;
    }

    public List<Pair<String, String>> getPathMapping() {
        return pathMapping;
    }

    /**
     *
     * @return debug proxy <host, port> or <code>null</code> if not used
     */
    public Pair<String, Integer> getDebugProxy() {
        return debugProxy;
    }

    public int getPort() {
        return (port != -1) ? port : getGlobalInstance().getPort();
    }

    public int getMaxData() {
        return (maxData != -2) ? maxData : getGlobalInstance().getMaxData();
    }

    public int getMaxChildren() {
        return (maxChildren != -1) ? maxChildren : getGlobalInstance().getMaxChildren();
    }

    public int getMaxStructuresDepth() {
        return (maxStructureDepth != -1) ? maxStructureDepth : getGlobalInstance().getMaxStructuresDepth();
    }

    public boolean isDebugForFirstPageOnly() {
        return (debugForFirstPageOnly != null) ? debugForFirstPageOnly : getGlobalInstance().isDebugForFirstPageOnly();
    }

    public boolean showRequestedUrls() {
        return getGlobalInstance().showRequestedUrls();
    }

    public boolean showDebuggerConsole() {
        return getGlobalInstance().showDebuggerConsole();
    }

    public boolean resolveBreakpoints() {
        return getGlobalInstance().resolveBreakpoints();
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return getGlobalInstance().isDebuggerStoppedAtTheFirstLine();
    }

    public String getPhpInterpreter() {
        return getGlobalInstance().getPhpInterpreter();
    }

    public String getProjectEncoding() {
        return projectEncoding != null ? projectEncoding : "UTF-8"; //NOI18N
    }

    private static class DefaultGlobal extends DebuggerOptions {

        public DefaultGlobal() {
        }

        @Override
        public int getPort() {
            return PhpOptions.getInstance().getDebuggerPort();
        }

        @Override
        public int getMaxData() {
            return PhpOptions.getInstance().getDebuggerMaxDataLength();
        }

        @Override
        public int getMaxChildren() {
            return PhpOptions.getInstance().getDebuggerMaxChildren();
        }

        @Override
        public int getMaxStructuresDepth() {
            return PhpOptions.getInstance().getDebuggerMaxStructuresDepth();
        }

        @Override
        public boolean isDebugForFirstPageOnly() {
            return false;
        }

        @Override
        public boolean showRequestedUrls() {
            return PhpOptions.getInstance().isDebuggerShowRequestedUrls();
        }

        @Override
        public boolean showDebuggerConsole() {
            return PhpOptions.getInstance().isDebuggerShowDebuggerConsole();
        }

        @Override
        public boolean resolveBreakpoints() {
            return PhpOptions.getInstance().isDebuggerResolveBreakpoints();
        }

        @Override
        public boolean isDebuggerStoppedAtTheFirstLine() {
            return PhpOptions.getInstance().isDebuggerStoppedAtTheFirstLine();
        }

        @Override
        public String getPhpInterpreter() {
            return PhpOptions.getInstance().getPhpInterpreter();
        }
    }
}
