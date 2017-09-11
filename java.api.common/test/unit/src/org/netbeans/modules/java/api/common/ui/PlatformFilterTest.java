/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
