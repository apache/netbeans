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
package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public final class ProjectPlatformTest extends NbTestCase {
    
    private File wd;

    public ProjectPlatformTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        wd = FileUtil.normalizeFile(getWorkDir());
    }
    
    public void testProjectPlatform() throws Exception {
        final Project p = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Project>() {
            @Override
            public Project run() throws Exception {
                final Project[] prjHolder = new Project[1];
                FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                                getWorkDir(),
                                "test",
                                null,
                                null,
                                null,
                                false);
                        final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.setProperty(ProjectProperties.PLATFORM_ACTIVE, "MyTestPlatform");
                        props.setProperty("platforms.MyTestPlatform.home", getJdkHome().getAbsolutePath());
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        final Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                        ProjectManager.getDefault().saveProject(p);
                        prjHolder[0] = p;
                    }
                });
                return prjHolder[0];
            }
        });
        assertNotNull(p);
        
        final J2SEProjectPlatform projectPlatform = p.getLookup().lookup(J2SEProjectPlatform.class);
        assertNotNull(projectPlatform);
        final JavaPlatform activePlatform = projectPlatform.getProjectPlatform();
        assertNotNull(activePlatform);
        assertNotSame(activePlatform, JavaPlatformManager.getDefault().getDefaultPlatform());
        assertEquals("MyTestPlatform", activePlatform.getDisplayName());
        
        final J2SEProject j2se = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(j2se);
        final ClassPath[] cps = j2se.getClassPathProvider().getProjectClassPaths(ClassPath.BOOT);
        assertEquals(2, cps.length);
        assertNotNull(cps[0]);
        assertFalse(cps[0].entries().isEmpty());
    }
    
    private static File getJdkHome() {
        File javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home")));
        return javaHome;
    }
    
//    private static boolean hasJavac(@NonNull final File home) {
//        final File bin = new File (home, "bin");                //NOI18N
//        final File[] children = bin.listFiles();
//        if (children != null) {
//            for (File cld : children) {
//                if ("javac".equals(name(cld.getName()))) {      //NOI18N
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    private static String name(String nameWithExt) {
//        final int index = nameWithExt.lastIndexOf('.'); //NOI18N
//        return index > 0 ?
//                nameWithExt.substring(0, index) :
//                nameWithExt;
//    }
    
}
