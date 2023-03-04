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

package org.netbeans.modules.java.j2seproject.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;
import org.openide.util.test.MockLookup;

/**
 * Test of class org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform
 * 
 * @author Tomas Zezula
 */
public class J2SEProjectPlatformTest extends NbTestCase {

    private File proj;
    
    public J2SEProjectPlatformTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        proj = new File(getWorkDir(), "proj");  //NOI18N
        proj.mkdirs();
        final File dp = new File(getWorkDir(), "dp");   //NOI18N
        final File op = new File(getWorkDir(), "op");   //NOI18N
        MockJavaPlatformProvider pp = MockJavaPlatformProvider.getInstance();
        pp.setPlatforms(new JavaPlatform[] {
            new MockJavaPlatform("default_platform", FileUtil.createFolder(dp)),   //NOI18N
            new MockJavaPlatform("other_platform", FileUtil.createFolder(op))      //NOI18N
        });
        MockLookup.setLayersAndInstances(pp);
    }


    
    public void testProjectPlatform() throws Exception {
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "TestProject", null, "manifest.mf", null, false);        //NOI18N
        Project prj = ProjectManager.getDefault().findProject(aph.getProjectDirectory());                                
        final JavaPlatform dp = MockJavaPlatformProvider.getInstance().getInstalledPlatforms()[0];
        final JavaPlatform op = MockJavaPlatformProvider.getInstance().getInstalledPlatforms()[1];
        final J2SEProjectPlatform pp = prj.getLookup().lookup(J2SEProjectPlatform.class);
        assertNotNull(pp);
        assertEquals(JavaPlatformManager.getDefault().getDefaultPlatform(), pp.getProjectPlatform());
        assertEquals(dp, pp.getProjectPlatform());
        pp.setProjectPlatform(op);
        assertEquals(op, pp.getProjectPlatform());
        pp.setProjectPlatform(dp);
        assertEquals(JavaPlatformManager.getDefault().getDefaultPlatform(), pp.getProjectPlatform());
        assertEquals(dp, pp.getProjectPlatform());
    }

    public void testListening() throws Exception {
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "TestProject", null, "manifest.mf", null, false);        //NOI18N
        Project prj = ProjectManager.getDefault().findProject(aph.getProjectDirectory());        
        final JavaPlatform dp = MockJavaPlatformProvider.getInstance().getInstalledPlatforms()[0];
        final JavaPlatform op = MockJavaPlatformProvider.getInstance().getInstalledPlatforms()[1];
        final J2SEProjectPlatform pp = prj.getLookup().lookup(J2SEProjectPlatform.class);
        assertNotNull(pp);
        assertEquals(dp, pp.getProjectPlatform());
        
        final AtomicBoolean event = new AtomicBoolean();
        pp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (J2SEProjectPlatform.PROP_PROJECT_PLATFORM.equals(evt.getPropertyName())) {
                    event.set(true);
                }
            }
        });
        pp.setProjectPlatform(op);
        assertTrue(event.get());
        assertEquals(op, pp.getProjectPlatform());
        event.set(false);
        pp.setProjectPlatform(dp);
        assertTrue(event.get());
        assertEquals(dp, pp.getProjectPlatform());
    }



    private static final class MockJavaPlatformProvider implements JavaPlatformProvider {

        private static final MockJavaPlatformProvider INSTANCE = new MockJavaPlatformProvider();

        private volatile JavaPlatform[] platforms = new JavaPlatform[0];

        private MockJavaPlatformProvider() {
        }


        static MockJavaPlatformProvider getInstance() {
            return INSTANCE;
        }

        void setPlatforms(@NonNull final JavaPlatform[] pls) {
            Parameters.notNull("pls", pls); //NOI18N
            platforms = pls;
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms() {
            final JavaPlatform[] pls = platforms;
            return Arrays.copyOf(pls, pls.length);
        }

        @Override
        public JavaPlatform getDefaultPlatform() {
            final JavaPlatform[] pls = platforms;
            return pls.length == 0 ? null : pls[0];
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {            
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static final class MockJavaPlatform extends JavaPlatform {

        private final String name;
        private final FileObject installFolder;

        MockJavaPlatform(
                @NonNull final String name,
                @NonNull final FileObject installFolder) {
            Parameters.notNull("name", name);   //NOI18N
            Parameters.notNull("installFolder", installFolder); //NOI18N
            this.name = name;
            this.installFolder = installFolder;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.<String,String>singletonMap("platform.ant.name", name);   //NOI18N
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public String getVendor() {
            return "xelfi.org"; //NOI18N
        }

        @Override
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("1.6"));  //NOI18N
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.<FileObject>singleton(installFolder);
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.<URL>emptyList();
        }

    }
    
}
