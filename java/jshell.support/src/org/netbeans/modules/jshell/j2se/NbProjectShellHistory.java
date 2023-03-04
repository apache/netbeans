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
package org.netbeans.modules.jshell.j2se;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.launch.ShellOptions;
import org.netbeans.modules.jshell.support.FileHistory;
import org.netbeans.modules.jshell.support.ShellHistory;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider( service = ShellHistory.class, 
         projectType = {
            "org-netbeans-modules-java-j2seproject",
            "org.netbeans.modules.java.j2semodule"
        }
)

public class NbProjectShellHistory extends FileHistory {
    private static final String HISTORY_FILENAME = "nbproject/private/jshell.history"; // NOI18N
    private final Project project;

    public NbProjectShellHistory(Project project) {
        super(project.getProjectDirectory().getFileObject(HISTORY_FILENAME));
        this.project = project;
        setMaxHistoryItems(ShellOptions.get().getHistoryLines());
    }
    
    

    @Override
    protected FileObject createFile() throws IOException {
        return  FileUtil.createData(project.getProjectDirectory(), HISTORY_FILENAME);
    }
}
