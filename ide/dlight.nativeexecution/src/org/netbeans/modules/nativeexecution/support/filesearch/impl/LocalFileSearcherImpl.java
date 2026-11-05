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
package org.netbeans.modules.nativeexecution.support.filesearch.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher;
import org.openide.util.lookup.ServiceProvider;

/**
 * In case of Windows use WINDOWS paths (c:\\mypath)... Also it requires exact
 * names (programm.exe and not just programm)
 */
@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher.class, position = 70)
public final class LocalFileSearcherImpl implements FileSearcher {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public final String searchFile(FileSearchParams fileSearchParams) {
        final ExecutionEnvironment execEnv = fileSearchParams.getExecEnv();

        if (!execEnv.isLocal()) {
            return null;
        }

        log.log(Level.FINE, "File Searching Task: {0}...", fileSearchParams.toString()); // NOI18N

        List<String> sp = new ArrayList<>(fileSearchParams.getSearchPaths());

        if (fileSearchParams.isSearchInUserPaths()) {
            try {
                Map<String, String> environment = HostInfoUtils.getHostInfo(execEnv).getEnvironment();
                String path = null;
                if (environment.containsKey("Path")) { // NOI18N
                    path = environment.get("Path"); // NOI18N
                } else if (environment.containsKey("PATH")) { // NOI18N
                    path = environment.get("PATH"); // NOI18N
                }
                if (path != null) {
                    sp.addAll(Arrays.asList(path.split(File.pathSeparator)));
                }
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        String file = fileSearchParams.getFilename();

        for (String path : sp) {
            try {
                File f = new File(path, file);
                log.log(Level.FINE, "   Test ''{0}''", f.toString()); // NOI18N
                if (f.canRead()) {
                    log.log(Level.FINE, "   FOUND ''{0}''", f.toString()); // NOI18N
                    return f.getCanonicalPath();
                }
            } catch (Throwable th) {
                log.log(Level.FINE, "Execption in LocalFileSearcherImpl:", th); // NOI18N
            }
        }

        return null;
    }
}
