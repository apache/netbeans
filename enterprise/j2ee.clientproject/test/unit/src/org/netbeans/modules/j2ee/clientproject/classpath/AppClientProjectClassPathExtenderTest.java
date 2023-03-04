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
package org.netbeans.modules.j2ee.clientproject.classpath;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class AppClientProjectClassPathExtenderTest extends NbTestCase {

    private FileObject workDir;

    public AppClientProjectClassPathExtenderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    public void testPropertyChangeDeadlock74204() throws Exception {
        File prjDirF = new File(FileUtil.toFile(workDir), "test");
        AntProjectHelper helper = AppClientProjectGenerator.createProject(prjDirF, "test-project",
                "test.MyMain", J2eeModule.JAVA_EE_5, TestUtil.SERVER_URL);
        final Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        final Object privateLock = new Object();
        final CountDownLatch sync = new CountDownLatch(2);
        
        FileChangeListener l = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                try {
                    sync.countDown();
                    sync.await();
                    synchronized (privateLock) {}
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        project.getProjectDirectory().getFileObject("nbproject").getChildren();
        project.getProjectDirectory().getFileObject("nbproject").addFileChangeListener(l);
        project.getProjectDirectory().getFileObject("nbproject/project.properties").addFileChangeListener(l);
        
        new Thread() {
            @Override
            public void run() {
                synchronized (privateLock) {
                    try {
                        sync.countDown();
                        sync.await();
                        ProjectManager.mutex().readAccess(new Runnable() {
                            public void run() {
                            }
                        });
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }.start();
        
        EditableProperties ep = new EditableProperties();
        
        ep.put(ProjectProperties.JAVAC_CLASSPATH, "y");
        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
}
