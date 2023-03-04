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

package org.netbeans.modules.search.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.openfile.OpenFileImpl;
import org.openide.filesystems.FileObject;

/**
 * Opens projects.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(
          service=org.netbeans.modules.openfile.OpenFileImpl.class, position=50)
public class ProjectOpenFileImpl implements OpenFileImpl {

    public boolean open(FileObject fileObject, int line) {
        if (fileObject.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(fileObject);
                if (p != null) {
                    openProject(p); // #171842
                    return true;
                }
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                                                           null, ex);
            }
        }
        return false;
    }

    /**
     * Opens the specified project asynchronously. This method helps to avoid
     * blocking of the current thread (e.g. AWT thread) on long-time operation
     * of opening the project.
     *
     * @param p the project.
     */
    private void openProject(final Project p) {
        Runnable r =new Runnable() {

            @Override
            public void run() {
                OpenProjects.getDefault().open(new Project[] {p}, false, true);
            }
        };
        new Thread(r, "Open " + p).start(); // NOI18N
    }

}
