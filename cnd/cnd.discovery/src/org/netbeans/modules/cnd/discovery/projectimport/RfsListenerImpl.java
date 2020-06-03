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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.api.RfsListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

/**
 *
 */
final class RfsListenerImpl implements RfsListener {
    private final Map<String, File> storage = new HashMap<>();
    private final ExecutionEnvironment execEnv;

    RfsListenerImpl(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    @Override
    public void fileChanged(ExecutionEnvironment env, File localFile, String remotePath) {
        if (env.equals(execEnv)) {
            storage.put(remotePath, localFile);
        }
    }

    void download() {
        Map<String, File> copy = new HashMap<>(storage);
        for (Map.Entry<String, File> entry : copy.entrySet()) {
            downloadImpl(entry.getKey(), entry.getValue());
        }
    }

    private void downloadImpl(String remoteFile, File localFile) {
        try {
            Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, execEnv, localFile.getAbsolutePath(), null);
            if (ImportProject.TRACE) {
                ImportProject.logger.log(Level.INFO, "#download file {0}", localFile.getAbsolutePath()); // NOI18N
            }
            task.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
