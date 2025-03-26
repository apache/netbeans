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
package org.netbeans.modules.nbcode.integration;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectService.DevopsConfigFinder;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhorvath
 */
@ServiceProvider(service = DevopsConfigFinder.class, position = 1000)
public class LspDevopsConfigFinder implements DevopsConfigFinder {

    @Override
    public List<FileObject> findDevopsConfig() {
        List<FileObject> result = new ArrayList<> ();
        LspServerState serverState = Lookup.getDefault().lookup(LspServerState.class);
        if (serverState != null) {
            List<FileObject> folders = serverState.getClientWorkspaceFolders();
            for (FileObject folder : folders) {
                FileObject f = folder.getFileObject(".vscode/devops.json"); //NOI18N
                if (f != null && f.isValid()) {
                    result.add(f);
                }
            }
        }
        return result;
    }
    
}
