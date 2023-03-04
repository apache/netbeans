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
package org.netbeans.modules.java.api.common.ui;

import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;


public class PlatformFilterTest extends NbTestCase {
    
    private static final String PRESERVE = "ToBePreserved";
    private static final String FILTEROUT = "ToBeFilteredOut";
    private MockProject prj1;
    private MockProject prj2;
    private JavaPlatform platform1;
    private JavaPlatform platform2;
    private JavaPlatform platform3;
    
    public PlatformFilterTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        platform1 = new TestPlatform("DefaultPlatform" + PRESERVE);
        platform2 = new TestPlatform("Platform" + PRESERVE);
        platform3 = new TestPlatform("Platform" + FILTEROUT);
        MockLookup.setInstances(
                AntBasedTestUtil.testAntBasedProjectType(),
                new PlatformFilterTest.TestPlatformProvider(
                    platform1,
                    platform2,
                    platform3
                ));
        clearWorkDir();
        final FileObject projectDir1 = FileUtil.createFolder(
                new File(getWorkDir(),"project1"));  //NOI18N
        prj1 = new PlatformFilterTest.MockProject(projectDir1);
        final FileObject projectDir2 = FileUtil.createFolder(
                new File(getWorkDir(),"project2"));  //NOI18N
        prj2 = new PlatformFilterTest.MockProjectExtended(projectDir2);
    }    

    public void testMockProject1() {
        Collection<JavaPlatform> platforms = prj1.getAvailablePlatforms();
        assertEquals(platforms.size(), 3);
        assertTrue(platforms.contains(platform1));
        assertTrue(platforms.contains(platform2));
        assertTrue(platforms.contains(platform3));
    }
    
    public void testMockProject2() {
        Collection<JavaPlatform> platforms = prj2.getAvailablePlatforms();
        assertEquals(platforms.size(), 2);
        assertTrue(platforms.contains(platform1));
        assertTrue(platforms.contains(platform2));
        assertFalse(platforms.contains(platform3));
    }

    private static class MockProject implements Project {
        
        private final FileObject projectDir;
        
        MockProject(final FileObject projectDir) {
            assert projectDir != null;
            this.projectDir = projectDir;
        }
        
        public Collection<JavaPlatform> getAvailablePlatforms() {
            List<JavaPlatform> result = new ArrayList<>();
            final Collection<? extends PlatformFilter> filters = getLookup().lookupAll(PlatformFilter.class);
            JavaPlatform[] platforms = TestPlatformProvider.getDefault().getInstalledPlatforms();
            if(platforms != null)  {
                for(int i = 0; i < platforms.length; i++) {
                    boolean accepted = true;
                    if(filters != null) {
                        for(PlatformFilter filter : filters) {
                            if(!filter.accept(platforms[i])) {
                                accepted = false;
                                break;
                            }
                        }
                    }
                    if(accepted) {
                        result.add(platforms[i]);
                    }
                }
            }
            return result;
        }

        @Override
        public FileObject getProjectDirectory() {
            return projectDir;
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(this);
        }        
        
    }
    
    private static class MockProjectExtended extends MockProject {
              
        MockProjectExtended(final FileObject projectDir) {
            super(projectDir);
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(this, getFilter());
        }        

        private PlatformFilter getFilter() {
            return new PlatformFilter() {
                @Override
                public boolean accept(JavaPlatform platform) {
                    return !platform.getDisplayName().endsWith(FILTEROUT);
                }
            };
        }

    }
    
    
    private static class TestPlatformProvider implements JavaPlatformProvider {

        private JavaPlatform[] platforms;
        
        public static JavaPlatformProvider getDefault() {
            return MockLookup.getDefault().lookup(JavaPlatformProvider.class);
        }

        public TestPlatformProvider(JavaPlatform... platforms) {
            if(platforms.length == 0) {
                this.platforms = new JavaPlatform[]{new TestPlatform("DefaultPlatform")};
            } else {
                this.platforms = platforms;
            }
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms()  {
            return platforms;
        }

        @Override
        public JavaPlatform getDefaultPlatform()  {
            if (platforms.length == 0) {
                platforms = new JavaPlatform[]{new TestPlatform("DefaultPlatform")};
            }
            return platforms[0];
        }
    }

    private static class TestPlatform extends JavaPlatform {

        private String name;
        
        public TestPlatform(String name) {
            this.name = name;
        }
        
        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public String getVendor() {
            return "me";
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        @Override
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("1.7"));
        }

        @Override
        public ClassPath getSourceFolders() {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.singletonMap("platform.ant.name", getDisplayName());
        }

        @Override
        public List<URL> getJavadocFolders() {
            return null;
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return name == null ? "DefaultTestPlatform" : name;
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }
    }
}
