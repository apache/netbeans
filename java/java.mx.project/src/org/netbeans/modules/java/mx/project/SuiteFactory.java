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
package org.netbeans.modules.java.mx.project;

import java.io.IOException;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ProjectFactory.class)
public class SuiteFactory implements ProjectFactory2 {
    @Override
    public boolean isProject(FileObject fo) {
        return findSuitePy(fo) != null;
    }

    static FileObject findSuitePy(FileObject fo) {
        final String mxDirName = "mx." + fo.getNameExt();
        FileObject suitePy = fo.getFileObject(mxDirName + '/' + "suite.py");
        return suitePy;
    }

    @Override
    public Project loadProject(FileObject dir, ProjectState ps) throws IOException {
        FileObject suitePy = findSuitePy(dir);
        if (suitePy == null) {
            return null;
        }
        MxSuite suite = MxSuite.parse(suitePy.toURL());
        return new SuiteProject(dir, suitePy, suite);
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }

    @Override
    public ProjectManager.Result isProject2(FileObject fo) {
        if (isProject(fo)) {
            return new ProjectManager.Result(
                ImageUtilities.loadImageIcon("org/netbeans/modules/java/mx/project/mx-knife.png", false)
            );
        } else {
            return null;
        }
    }
}
