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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UseHtml4JavaTest extends TestBase {
    static {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
    }

    private FileObject dir;
    public UseHtml4JavaTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        System.setProperty("sync.project.execution", "true");
//        MockLookup.setLayersAndInstances(cgpi);
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        ((DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class)).reset();
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        File prjDir = new File(new File(getDataDir(), "example-external-projects"), "html4j");
        FileObject prj = FileUtil.toFileObject(prjDir);
        FileObject wrk = FileUtil.toFileObject(getWorkDir());
        dir = prj.copy(wrk, "html4j", null);
    }
    
    
    
    public void testCompileTheProject() throws Exception {
        Project prj = ProjectManager.getDefault().findProject(dir);
        assertNotNull("Project found for " + dir, prj);
        assertTrue("It is our project", prj instanceof NbModuleProject);
        ((NbModuleProject)prj).open();
        FileObject buildXML = dir.getFileObject("build.xml");
        assertNotNull("Ant script found", buildXML);
        final ExecutorTask task = ActionUtils.runTarget(buildXML, new String[]{"compile"}, null);
        task.waitFinished();
        assertEquals("Executed successfully", 0, task.result());
        assertNotNull("project was build", dir.getFileObject("build"));
    }
}