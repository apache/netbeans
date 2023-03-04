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

package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.modules.debugger.jpda.projectsui.MainProjectManager;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class MainProjectManagerTest extends NbTestCase {

    public MainProjectManagerTest(String name) {
        super(name);
    }

    public void testLeakedProject() throws Exception {
        class MockProject implements Project {
            public @Override FileObject getProjectDirectory() {
                return FileUtil.createMemoryFileSystem().getRoot();
            }
            public @Override Lookup getLookup() {
                return Lookup.EMPTY;
            }
        }
        Project p = new MockProject();
        MainProjectManager mpm = MainProjectManager.getDefault();
        OpenProjects.getDefault().open(new Project[] {p}, false);
        OpenProjects.getDefault().setMainProject(p);
        OpenProjects.getDefault().setMainProject(null);
        OpenProjects.getDefault().close(new Project[] {p});
        mpm.enable(p);
        Reference<?> r = new WeakReference<Object>(p);
        p = null;
        assertGC("can collect project after closing", r);
    }

}
