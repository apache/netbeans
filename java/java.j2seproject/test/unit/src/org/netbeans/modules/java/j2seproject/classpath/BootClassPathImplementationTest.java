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

package org.netbeans.modules.java.j2seproject.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * @author Tomas Zezula
 */
public class BootClassPathImplementationTest extends NbTestCase {
    
    public BootClassPathImplementationTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject defaultPlatformBootRoot;
    private FileObject explicitPlatformBootRoot;
    private ProjectManager pm;
    private J2SEProject pp;
    private TestPlatformProvider tp;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        this.defaultPlatformBootRoot = scratch.createFolder("DefaultPlatformBootRoot");
        this.explicitPlatformBootRoot = scratch.createFolder("ExplicitPlatformBootRoot");
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[]{defaultPlatformBootRoot.toURL()});
        ClassPath expBCP = ClassPathSupport.createClassPath(new URL[]{explicitPlatformBootRoot.toURL()});
        tp = new TestPlatformProvider (defBCP, expBCP);
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            tp
        );
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    
    private void prepareProject (String platformName) throws IOException {
        projdir = scratch.createFolder("proj");
        final JavaPlatform[] jps = JavaPlatformManager.getDefault().getPlatforms(platformName, null);
        assertEquals(1, jps.length);
        final JavaPlatform jp = jps[0];
        AntProjectHelper helper = new J2SEProjectBuilder(FileUtil.toFile(projdir), "Test Project")  //NOI18N
                .addDefaultSourceRoots()
                .setJavaPlatform(jp)
                .build();
        pm = ProjectManager.getDefault();
        final Project p = pm.findProject(projdir);
        pp = p.getLookup().lookup(J2SEProject.class);
        sources = FileUtil.createFolder(projdir, "src");    //NOI18N
        tests = FileUtil.createFolder(projdir, "test");     //NOI18N
    }
    
    public void testBootClassPathImplementation () throws Exception {
        this.prepareProject("ExplicitPlatform");        
        FileObject file = sources.createData("a.java");
        ClassPath bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        FileObject[] roots = bootCP.getRoots();
        assertEquals("Boot classpath size",1, roots.length);
        assertEquals("Boot classpath",explicitPlatformBootRoot, roots[0]);
        
        tp.setExplicitPlatformVisible(false);
        bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        roots = bootCP.getRoots();
        //Change of the behavior of the BootClassPathImplementation,
        // it should not return the default platform in case of broken project's platform
        // see issue #57641:rt.jar and src.zip are scanned when it's not neccessary
        assertEquals("Boot classpath size",0, roots.length);
                
        tp.setExplicitPlatformVisible(true);
        bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        roots = bootCP.getRoots();
        assertEquals("Boot classpath size",1, roots.length);        
        assertEquals("Boot classpath",explicitPlatformBootRoot, roots[0]);
    }

    public void testSrcTestSplitBootCp() throws IOException {
        this.prepareProject("ExplicitPlatform");
        final ClassPathProviderImpl cpProvider = ClassPathProviderImpl.Builder.create(
                pp.getAntProjectHelper(),
                pp.evaluator(),
                pp.getSourceRoots(),
                pp.getTestSourceRoots())
                .setProject(pp)
                .build();
        final ClassPath bcpSrc = cpProvider.findClassPath(sources, ClassPath.BOOT);
        assertNotNull(bcpSrc);
        final ClassPath bcpTests = cpProvider.findClassPath(tests, ClassPath.BOOT);
        assertNotNull(bcpTests);
        assertNotSame(bcpSrc, bcpTests);
    }
    
    private static class TestPlatformProvider implements JavaPlatformProvider {
        
        private JavaPlatform defaultPlatform;
        private JavaPlatform explicitPlatform;
        private PropertyChangeSupport support;
        private boolean hideExplicitPlatform;
        
        public TestPlatformProvider (ClassPath defaultPlatformBootClassPath, ClassPath explicitPlatformBootClassPath) {
            this.support = new PropertyChangeSupport (this);
            this.defaultPlatform = new TestPlatform ("DefaultPlatform", defaultPlatformBootClassPath);
            this.explicitPlatform = new TestPlatform ("ExplicitPlatform", explicitPlatformBootClassPath);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener (listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public JavaPlatform[] getInstalledPlatforms()  {
            if (this.hideExplicitPlatform) {
                return new JavaPlatform[] {
                    this.defaultPlatform,
                };
            }
            else {
                return new JavaPlatform[] {
                    this.defaultPlatform,
                    this.explicitPlatform,
                };
            }
        }
       
        public JavaPlatform getDefaultPlatform () {            
            return this.defaultPlatform;
        }
        
        public void setExplicitPlatformVisible (boolean value) {
            this.hideExplicitPlatform = !value;
            this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS,null,null);
        }
    }
    
    private static class TestPlatform extends JavaPlatform {
        
        private String systemName;
        private Map<String,String> properties;
        private ClassPath bootClassPath;
        
        public TestPlatform (String systemName, ClassPath bootCP) {
            this.systemName = systemName;
            this.bootClassPath = bootCP;
            this.properties = Collections.singletonMap("platform.ant.name",this.systemName);
        }
        
        public FileObject findTool(String toolName) {
            return null;
        }

        public String getVendor() {
            return "me";    
        }

        public ClassPath getStandardLibraries() {
            return null;
        }

        public Specification getSpecification() {
            return new Specification ("j2se", new SpecificationVersion ("1.5"));
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public Map<String,String>  getProperties() {
            return this.properties;
        }

        public List<URL> getJavadocFolders() {
            return null;
        }

        public Collection<FileObject> getInstallFolders() {
            return null;
        }

        public String getDisplayName() {
            return this.systemName;
        }

        public ClassPath getBootstrapLibraries() {
            return this.bootClassPath;
        }
        
    }
    
}
