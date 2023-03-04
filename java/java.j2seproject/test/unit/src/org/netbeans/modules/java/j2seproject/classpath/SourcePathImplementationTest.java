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
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

public class SourcePathImplementationTest extends NbTestCase {

    public SourcePathImplementationTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private ProjectManager pm;
    private AntProjectHelper helper;
    private J2SEProject pp;

    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        MockLookup.setLayersAndInstances();
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir).getLookup().lookup(J2SEProject.class);
        sources = projdir.getFileObject("src");
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }

    public void testSourcePathImplementation () throws Exception {
        ClassPathProviderImpl cpProvider = pp.getClassPathProvider();
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        FileObject[] roots = cp.getRoots();
        assertNotNull ("Roots can not be null",roots);
        assertEquals("There must be one source root", 1, roots.length);
        assertEquals("There must be src root",roots[0],sources);
        TestListener tl = new TestListener();
        cp.addPropertyChangeListener (tl);
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper, projdir,"src.other.dir","other");
        assertTrue("Classpath must fire PROP_ENTRIES and PROP_ROOTS", tl.getEvents().containsAll(Arrays.asList(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS)));
        roots = cp.getRoots();
        assertNotNull ("Roots can not be null",roots);
        assertEquals("There must be two source roots", 2, roots.length);
        assertEquals("There must be src root",roots[0],sources);
        assertEquals("There must be other root",roots[1],newRoot);
        cp.removePropertyChangeListener(tl);
    }
    
    public void testWSClientSupport () throws Exception {
        ClassPathProviderImpl cpProvider = pp.getClassPathProvider();
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List<ClassPath.Entry> entries = cp.entries();
        assertNotNull ("Entries can not be null", entries);
        assertEquals ("There must be 3 src entries",1, entries.size());
        assertEquals("There must be src root", entries.get(0).getRoot(), sources);
    }

    public void testIncludesExcludes() throws Exception {
        ClassPath cp = pp.getClassPathProvider().getProjectSourcesClassPath(ClassPath.SOURCE);
        assertEquals(Collections.singletonList(sources), Arrays.asList(cp.getRoots()));
        FileObject objectJava = FileUtil.createData(sources, "java/lang/Object.java");
        FileObject jcJava = FileUtil.createData(sources, "javax/swing/JComponent.java");
        FileObject doc = FileUtil.createData(sources, "javax/swing/doc-files/index.html");
        assertTrue(cp.contains(objectJava));
        assertTrue(cp.contains(objectJava.getParent()));
        assertTrue(cp.contains(jcJava));
        assertTrue(cp.contains(jcJava.getParent()));
        assertTrue(cp.contains(doc));
        assertTrue(cp.contains(doc.getParent()));
        TestListener tl = new TestListener();
        // XXX #97391: sometimes, unpredictably, fired:
        tl.forbid(ClassPath.PROP_ENTRIES);
        tl.forbid(ClassPath.PROP_ROOTS);
        cp.addPropertyChangeListener(tl);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.INCLUDES, "javax/swing/");
        ep.setProperty(ProjectProperties.EXCLUDES, "**/doc-files/");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        pm.saveProject(pp);
        assertEquals(Collections.singleton(ClassPath.PROP_INCLUDES), tl.getEvents());
        assertFalse(cp.contains(objectJava));
        assertFalse(cp.contains(objectJava.getParent()));
        assertTrue(cp.contains(jcJava));
        assertTrue(cp.contains(jcJava.getParent()));
        assertTrue(cp.contains(jcJava.getParent().getParent()));
        assertFalse(cp.contains(doc));
        assertFalse(cp.contains(doc.getParent()));
    }

    public void testIncludesFiredJustOnce() throws Exception {
        File src1 = new File(getWorkDir(), "src1");
        src1.mkdir();
        File src2 = new File(getWorkDir(), "src2");
        src2.mkdir();
        AntProjectHelper h = J2SEProjectGenerator.createProject(new File(getWorkDir(), "prj"), "test", new File[] {src1, src2}, new File[0], null, null, null);
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        FileOwnerQuery.markExternalOwner(Utilities.toURI(src1), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        ClassPath cp = ClassPath.getClassPath(FileUtil.toFileObject(src1), ClassPath.SOURCE);
        assertNotNull(cp);        
        assertEquals(2, cp.getRoots().length);
        ClassPath.Entry cpe2 = cp.entries().get(1);
        assertEquals(Utilities.toURI(src2).toURL(), cpe2.getURL());
        assertTrue(cpe2.includes("stuff/"));
        assertTrue(cpe2.includes("whatever/"));
        class L implements PropertyChangeListener {
            int cnt;
            public void propertyChange(PropertyChangeEvent e) {
                if (ClassPath.PROP_INCLUDES.equals(e.getPropertyName())) {
                    cnt++;
                }
            }
        }
        L l = new L();
        cp.addPropertyChangeListener(l);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.INCLUDES, "whatever/");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals(1, l.cnt);
        assertFalse(cpe2.includes("stuff/"));
        assertTrue(cpe2.includes("whatever/"));
        ep.setProperty(ProjectProperties.INCLUDES, "whateverelse/");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals(2, l.cnt);
        assertFalse(cpe2.includes("stuff/"));
        assertFalse(cpe2.includes("whatever/"));
        ep.remove(ProjectProperties.INCLUDES);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals(3, l.cnt);
        assertTrue(cpe2.includes("stuff/"));
        assertTrue(cpe2.includes("whatever/"));
    }

    private static class TestListener implements PropertyChangeListener {
        private Set<String> events = new HashSet<String>();
        private Set<String> forbiddenEvents = new HashSet<String>();

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName != null) {
                assertFalse("Not supposed to have received " + propName, forbiddenEvents.contains(propName));
                this.events.add (propName);
            }
        }

        public Set<String> getEvents () {
            return Collections.unmodifiableSet(this.events);
        }

        public void forbid(String prop) {
            forbiddenEvents.add(prop);
        }

    }
}
