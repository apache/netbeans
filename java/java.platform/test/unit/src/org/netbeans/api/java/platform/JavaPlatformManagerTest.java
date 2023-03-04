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

package org.netbeans.api.java.platform;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.FallbackDefaultJavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * @author Tomas Zezula, Jesse Glick
 */
public class JavaPlatformManagerTest extends NbTestCase {

    public JavaPlatformManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new TestJavaPlatformProvider());
    }

    public void testGetDefaultPlatform() {
        MockLookup.setInstances(); // make sure we are using pure defaults
        JavaPlatformManager manager = JavaPlatformManager.getDefault ();
        assertNotNull (manager);
        JavaPlatform p = manager.getDefaultPlatform();
        assertNotNull("some platform registered by default", p);
        ClassPath cp = p.getBootstrapLibraries();
        //JDK 9+: sun.boot.class.path does not exist, so there are no bootstrap libraries
        //JDK 9+ platforms need nbjrt filesystem to work properly, and that is 
        //in java.j2seplatform, and the real default platform is there as well, so what can be done here?
//        assertNotNull("is 1.5+ JRE: " + cp, cp.findResource("java/lang/StringBuilder.class"));
        assertFalse(p.getInstallFolders().isEmpty());
        //assertNotNull(p.findTool("javac"));
        assertNotNull(p.getDisplayName());
        assertNotNull(p.getSystemProperties().get("java.home"));
        assertNotNull(p.getSourceFolders());
        assertNotNull(p.getJavadocFolders());
        cp = p.getStandardLibraries();
        assertNotNull("contains test CP: " + cp, cp.findResource(JavaPlatformManager.class.getName().replace('.', '/') + ".class"));
        assertNotNull(p.getProperties());
        assertNotNull(p.getVendor());
        Specification spec = p.getSpecification();
        assertNotNull(spec);
        assertNotNull(spec.getName());
        // can be null: assertNotNull(spec.getProfiles());
        assertTrue(spec.getVersion().compareTo(new SpecificationVersion("1.5")) >= 0);
    }

    public void testGetInstalledPlatforms() {
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        assertNotNull (manager);
        TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault ();
        assertNotNull (provider);
        JavaPlatform[] platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertEquals (1,platforms.length);
        assertEquals(FallbackDefaultJavaPlatform.getInstance(), platforms[0]);
        JavaPlatform platform = new TestJavaPlatform ("Testing Platform",
            new Specification("j2se", new SpecificationVersion ("1.5")));
        provider.addPlatform (platform);
        platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertTrue (platforms.length == 1);
        assertTrue (platforms[0]==platform);
        provider.removePlatform(platform);
        platforms = manager.getInstalledPlatforms();
        assertNotNull (platforms);
        assertEquals (1,platforms.length);
        assertEquals(FallbackDefaultJavaPlatform.getInstance(), platforms[0]);
    }

    public void testGetPlatforms() {
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        assertNotNull (manager);
        TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault ();
        assertNotNull (provider);
        JavaPlatform p1 = new TestJavaPlatform ("P1", new Specification("P1",new SpecificationVersion ("1.4")));
        JavaPlatform p2 = new TestJavaPlatform ("P2", new Specification("P2",new SpecificationVersion ("1.4")));
        JavaPlatform p3 = new TestJavaPlatform ("P3", new Specification("P3",new SpecificationVersion ("1.4")));
        JavaPlatform p4 = new TestJavaPlatform ("P4", new Specification("P4",new SpecificationVersion ("1.5")));
        JavaPlatform p5 = new TestJavaPlatform ("P5", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0")),
            new Profile ("RMI", new SpecificationVersion ("1.0")),
        }));
        JavaPlatform p6 = new TestJavaPlatform ("P6", new Specification("CDC", new SpecificationVersion("1.0")));
        JavaPlatform p7 = new TestJavaPlatform ("P7", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0"))
        }));
        JavaPlatform p8 = new TestJavaPlatform ("P8", new Specification("CDC",new SpecificationVersion("1.0"), new Profile[] {
            new Profile ("PersonalJava", new SpecificationVersion ("1.0")),
            new Profile ("JNI", new SpecificationVersion ("1.0")),
            new Profile ("GIOP", new SpecificationVersion ("1.0"))
        }));
        provider.addPlatform (p1);
        provider.addPlatform (p2);
        provider.addPlatform (p3);
        provider.addPlatform (p4);
        provider.addPlatform (p5);
        provider.addPlatform (p6);
        provider.addPlatform (p7);
        provider.addPlatform (p8);
        assertNotNull (manager.getInstalledPlatforms());
        assertTrue (manager.getInstalledPlatforms().length == 8);
        JavaPlatform[] r = manager.getPlatforms("P1",null);
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms("P1", new Specification ("P1", new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms("P1", new Specification ("P1", null));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification ("P1", null));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification ("P1", new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p1);
        r = manager.getPlatforms(null, new Specification (null,new SpecificationVersion("1.4")));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[]{p1,p2,p3});
        // Test of profiles
        r = manager.getPlatforms (null, new Specification ("CDC", new SpecificationVersion("1.0")));        //Any CDC
        assertNotNull (r);
        assertTrue (r.length == 4);
        assertEquivalent (r, new JavaPlatform[] {p5, p6, p7, p8});
        r = manager.getPlatforms (null, new Specification ("CDC", null, new Profile[] {                     // CDC with PersonalJava/* and RMI/*
            new Profile ("PersonalJava",null),
            new Profile ("RMI",null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0]==p5);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       // CDC with any existing profile
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[] {p5,p7,p8});
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       // CDC with PersonalJava/* and */*
            new Profile ("PersonalJava",null),
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 3);
        assertEquivalent (r, new JavaPlatform[] {p5,p7,p8});
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with PersonalJava/*
            new Profile ("PersonalJava",null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p7);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with RMI/* and */*
            new Profile ("RMI",null),
            new Profile (null,null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 1);
        assertTrue (r[0] == p5);
        r = manager.getPlatforms (null, new Specification ("CDC",null,new Profile[] {                       //CDC with Gateway/* and */*
            new Profile ("Gateway",null),
            new Profile (null, null)
        }));
        assertNotNull (r);
        assertTrue (r.length == 0);
        r = manager.getPlatforms(null,null);                                                              //All platforms
        assertNotNull(r);
        assertTrue (r.length == 8);
        assertEquivalent (r, new JavaPlatform[] {p1,p2,p3,p4,p5,p6,p7, p8});

        //Done, clean up
        provider.removePlatform (p1);
        provider.removePlatform (p2);
        provider.removePlatform (p3);
        provider.removePlatform (p4);
        provider.removePlatform (p5);
        provider.removePlatform (p6);
        provider.removePlatform (p7);
        provider.removePlatform (p8);
        assertEquals (1,manager.getInstalledPlatforms().length);
        assertEquals (FallbackDefaultJavaPlatform.getInstance(),manager.getInstalledPlatforms()[0]);
    }


    private static void assertEquivalent (JavaPlatform[] a, JavaPlatform[] b) {
        assertTrue (a.length == b.length);
        List l = Arrays.asList(a);
        for (int i=0; i < b.length; i++) {
            if (!l.contains(b[i])) {
                assertTrue (false);
            }
        }
    }
    
    private static class TestJavaPlatform extends JavaPlatform {

        private String id;
        private Specification spec;

        public TestJavaPlatform (String id, Specification spec) {
            this.id = id;
            this.spec = spec;
        }

        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public String getDisplayName() {
            return this.id;
        }

        public Collection<FileObject> getInstallFolders() {
            return Collections.emptyList();
        }

        public List<URL> getJavadocFolders() {
            return Collections.emptyList();
        }

        public Map<String,String> getProperties() {
            return Collections.emptyMap();
        }

        public ClassPath getSourceFolders() {
            return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }

        public Specification getSpecification() {
            return this.spec;
        }

        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public String getVendor() {
            return "Me";
        }

        public FileObject findTool(String name) {
            return null;
        }
    }
}
