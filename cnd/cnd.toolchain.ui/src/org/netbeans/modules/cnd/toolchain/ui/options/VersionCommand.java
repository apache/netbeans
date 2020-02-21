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

package org.netbeans.modules.cnd.toolchain.ui.options;

import java.io.File;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 */
/*package-local*/ final class VersionCommand {

    private final Tool tool;
    private String path;
    private boolean alreadyRun;
    private String version;

    /**
     * Creates a new instance of VersionCommand
     */
    public VersionCommand(Tool tool, String path) {
        this.tool = tool;
        this.path = path;
    }

    public String getVersion() {
        if (!alreadyRun) {
            run();
        }
        return version;
    }

    private void run() {
        if (tool.getExecutionEnvironment().isLocal()) {
            // we're dealing with a local toolchain
            path = LinkSupport.resolveWindowsLink(path);
            File file = new File(path);
            if (!file.exists()) {
                alreadyRun = true;
                return;
            }
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(tool.getExecutionEnvironment());
        npb.setExecutable(path);
        npb.setArguments(getVersionFlags());
        npb.redirectError();
        try {
            NativeProcess p = npb.call();
            p.getOutputStream().close();
            final List<String> processOutput = ProcessUtils.readProcessOutput(p);
            if (processOutput != null && processOutput.size() > 0) {
                version = processOutput.get(0);
            }
        } catch (Exception ex) {
            // silently drop
        }
        
        alreadyRun = true;
    }

    private String getVersionFlags() {
        String flags = null;
        if (tool.getDescriptor() != null) {
            flags = tool.getDescriptor().getVersionFlags();
        }
        if (flags == null) {
            return "--version"; // NOI18N
        } else {
            return flags;
        }
    }
}
