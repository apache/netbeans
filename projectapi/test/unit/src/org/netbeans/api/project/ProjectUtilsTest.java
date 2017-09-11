/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ProjectInformationProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * Test {@link ProjectUtils}.
 * @author Jesse Glick
 */
public class ProjectUtilsTest extends NbTestCase {

    static {
        MockLookup.setInstances(
            TestUtil.testProjectFactory(),
            new ProjectInformationProviderImpl());
    }
    
    public ProjectUtilsTest(String name) {
        super(name);
    }
    
    public void testHasSubprojectCycles() throws Exception {
        // Check static cycle detection.
        TestProj a = new TestProj("a");
        assertFalse("no cycles in a project with no declared subprojects", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[0];
        assertFalse("no cycles in a standalone project", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj b = new TestProj("b");
        a.subprojs = new Project[] {b};
        b.subprojs = new Project[0];
        assertFalse("no cycles in a -> b", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj c = new TestProj("c");
        c.subprojs = new Project[0];
        b.subprojs = new Project[] {c};
        assertFalse("no cycles in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj d = new TestProj("d");
        d.subprojs = new Project[0];
        b.subprojs = new Project[] {d};
        c.subprojs = new Project[] {d};
        assertFalse("no cycles in a -> {b, c} -> d (DAG)", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[] {a};
        assertTrue("self-loop cycle in a -> a", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[] {b};
        b.subprojs = new Project[] {a};
        assertTrue("simple cycle in a -> b -> a", ProjectUtils.hasSubprojectCycles(a, null));
        b.subprojs = new Project[] {c};
        c.subprojs = new Project[] {b};
        assertTrue("simple cycle not involing master in a -> b -> c -> b", ProjectUtils.hasSubprojectCycles(a, null));
        c.subprojs = new Project[] {a};
        a.subprojs = new Project[] {b, d};
        d.subprojs = new Project[] {a};
        assertTrue("multiple cycles in a -> b -> c -> a, a -> d -> a", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[0];
        b.subprojs = new Project[0];
        assertFalse("no cycle introduced by a -> b in a, b", ProjectUtils.hasSubprojectCycles(a, b));
        c.subprojs = new Project[0];
        b.subprojs = new Project[] {c};
        assertFalse("no cycle introduced by a -> b in a, b -> c", ProjectUtils.hasSubprojectCycles(a, b));
        a.subprojs = new Project[] {b};
        assertFalse("no cycle introduced by no-op a -> b in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, b));
        assertFalse("no cycle introduced by direct a -> c in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, c));
        assertTrue("cycle introduced by a -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, a));
        assertTrue("cycle introduced by b -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(b, a));
        assertTrue("cycle introduced by c -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(c, a));
        c.subprojs = null;
        assertTrue("cycle introduced by c -> a in a -> b -> c (no explicit subprojects in c)", ProjectUtils.hasSubprojectCycles(c, a));
        // Performance check.
        a = new TestProj("a");
        b = new TestProj("b");
        c = new TestProj("c");
        d = new TestProj("d");
        a.subprojs = new Project[] {b, c};
        b.subprojs = new Project[] {d};
        c.subprojs = new Project[] {d};
        d.subprojs = new Project[] {};
        assertFalse("diamond, no cycles", ProjectUtils.hasSubprojectCycles(a, null));
        assertEquals("A asked for subprojects just once", 1, a.getSubprojectsCalled());
        assertEquals("B asked for subprojects just once", 1, b.getSubprojectsCalled());
        assertEquals("C asked for subprojects just once", 1, c.getSubprojectsCalled());
        assertEquals("D asked for subprojects just once", 1, d.getSubprojectsCalled());
    }
    
    @RandomlyFails // http://deadlock.netbeans.org/job/NB-Core-Build/9880/testReport/
    public void testGenericSources() throws Exception {
        clearWorkDir();
        File topF = new File(getWorkDir(), "top");
        assertTrue(new File(topF, "testproject").mkdirs());
        assertTrue(new File(topF, "nested" + File.separator + "testproject").mkdirs());
        assertTrue(new File(topF, "file").createNewFile());
        FileObject top = FileUtil.toFileObject(topF);
        assertNotNull(top);
        Project p = ProjectManager.getDefault().findProject(top);
        assertNotNull(p);
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] grps = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals(1, grps.length);
        assertEquals(top, grps[0].getRootFolder());
        assertEquals("top", grps[0].getDisplayName());
        assertTrue(grps[0].contains(top));
        FileObject file = top.getFileObject("file");
        assertNotNull(file);
        assertTrue(grps[0].contains(file));
        FileObject nested = top.getFileObject("nested");
        assertNotNull(nested);
        assertFalse(grps[0].contains(nested));
        assertEquals(1, TestUtil.projectLoadCount(top));
        assertEquals("#67450: did not have to load nested project", 0, TestUtil.projectLoadCount(nested));
        // XXX could also test contains(...) on unsharable files
    }
    
    /**
     * Fake project with subprojects.
     */
    private static final class TestProj implements Project, SubprojectProvider {
        
        private final String name;
        /**
         * Subproject list.
         * Use null to not have a SubprojectProvider at all.
         */
        public Project[] subprojs = null;
        private int getSubprojectsCalled;

        /**
         * Create a fake project.
         * @param name a name for debugging purposes
         */
        public TestProj(String name) {
            this.name = name;
        }
        
        public Lookup getLookup() {
            if (subprojs == null) {
                return Lookup.EMPTY;
            } else {
                return Lookups.singleton(this);
            }
        }
        
        public Set<? extends Project> getSubprojects() {
            getSubprojectsCalled++;
            assert subprojs != null;
            return new HashSet<Project>(Arrays.asList(subprojs));
        }
        
        /**
         * Number of times {@link #getSubprojects} was called since last check.
         */
        public int getSubprojectsCalled() {
            int c = getSubprojectsCalled;
            getSubprojectsCalled = 0;
            return c;
        }

        public FileObject getProjectDirectory() {
            // irrelevant
            return FileUtil.createMemoryFileSystem().getRoot();
        }
        
        public void addChangeListener(ChangeListener l) {}
        
        public void removeChangeListener(ChangeListener l) {}
        
        public @Override String toString() {
            return name;
        }

    }

    public void testGetCacheDirectory() throws Exception {
        final FileObject pdir = FileUtil.createMemoryFileSystem().getRoot().createFolder("foo");
        final ProjectInformation info = new ProjectInformation() {
            public String getName() {
                return "/foo/project";
            }
            public String getDisplayName() {
                return getName();
            }
            public Icon getIcon() {
                return null;
            }
            public Project getProject() {
                return null;
            }
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        };
        Project p = new Project() {
            public FileObject getProjectDirectory() {
                return pdir;
            }
            public Lookup getLookup() {
                return Lookups.fixed(info);
            }
        };
        FileObject d = ProjectUtils.getCacheDirectory(p, Object.class);
        assertEquals(FileUtil.getConfigRoot().getFileSystem(), d.getFileSystem());
        assertEquals("Projects/extra/_foo_project-00018cc6/java-lang", d.getPath());
        d = ProjectUtils.getCacheDirectory(p, Object.class);
        assertEquals("Projects/extra/_foo_project-00018cc6/java-lang", d.getPath());
        final FileObject cache = FileUtil.createMemoryFileSystem().getRoot();
        p = new Project() {
            public FileObject getProjectDirectory() {
                return pdir;
            }
            public Lookup getLookup() {
                return Lookups.fixed(info, new CacheDirectoryProvider() {
                    public FileObject getCacheDirectory() throws IOException {
                        return cache;
                    }
                });
            }
        };
        d = ProjectUtils.getCacheDirectory(p, Object.class);
        assertEquals("java-lang", d.getNameExt());
        assertEquals(cache, d.getParent());
        d = ProjectUtils.getCacheDirectory(p, Object.class);
        assertEquals("java-lang", d.getNameExt());
        assertEquals(cache, d.getParent());
    }        

    private static class ProjectInformationProviderImpl implements ProjectInformationProvider {
        @Override
        public ProjectInformation getProjectInformation(final Project project) {
            ProjectInformation info =  project.getLookup().lookup(ProjectInformation.class);
            if (info == null) {
                info = new ProjectInformation() {
                    @Override
                    public String getName() {
                        return project.getProjectDirectory().getName();
                    }

                    @Override
                    public String getDisplayName() {
                        return getName();
                    }

                    @Override
                    public Icon getIcon() {
                        return null;
                    }

                    @Override
                    public Project getProject() {
                        return project;
                    }

                    @Override
                    public void addPropertyChangeListener(PropertyChangeListener listener) {
                    }

                    @Override
                    public void removePropertyChangeListener(PropertyChangeListener listener) {
                    }
                };
            }
            return info;
        }
    }
}
