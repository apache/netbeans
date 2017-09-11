/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.ant;

import java.util.TreeMap;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImplTest extends NbTestCase {

    private File workDir;

    public ProjectRunnerImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        workDir = FileUtil.normalizeFile(getWorkDir());
        MockServices.setServices(MockProjectFactory.class);
    }

    @Test
    public void testComputeProperties1() throws IOException {
        ClassPath cp = ClassPathSupport.createClassPath(new URL("file:///E/"));
        final String wd = workDir.getAbsolutePath();
        checkProperties(Arrays.asList("classname", "A", "platform.java", "J", "execute.classpath", cp, "work.dir", wd,"boot.classpath",cp,"runtime.encoding",Charset.defaultCharset()),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", "/E/", "work.dir", wd, "application.args", "", "run.jvmargs", "", "platform.bootcp", "/E","encoding",Charset.defaultCharset().name()));
    }

    @Test
    public void testComputeProperties2() throws MalformedURLException, IOException {
        FileObject fo = FileUtil.toFileObject(workDir);

        assertNotNull(fo);

        FileObject java = FileUtil.createData(fo, "prj/A.java");
        FileObject prj = java.getParent();

        String prjPath = FileUtil.toFile(prj).getAbsolutePath();

        Lookup.getDefault().lookup(MockProjectFactory.class).prjDir = prj;

        checkProperties(Arrays.asList("execute.file", java, "platform.java", "J"),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath + File.separatorChar, "work.dir", prjPath, "run.jvmargs", "", "encoding", "UTF-8", "platform.bootcp", JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().toString()),
                        "prj");
    }

    @Test
    public void testComputeProperties3() throws MalformedURLException, IOException {
        FileObject fo = FileUtil.toFileObject(workDir);

        assertNotNull(fo);

        FileObject java = FileUtil.createData(fo, "prj/A.java");
        FileObject prj  = java.getParent();
        FileObject dir  = FileUtil.createFolder(fo, "prj/test");

        String prjPath = FileUtil.toFile(prj).getAbsolutePath();

        Lookup.getDefault().lookup(MockProjectFactory.class).prjDir = prj;

        Project fake = new Project() {
            public FileObject getProjectDirectory() {
                return null;
            }
            public Lookup getLookup() {
                return Lookups.singleton(new ProjectInformation() {
                    public String getName() {
                        return null;
                    }
                    public String getDisplayName() {
                        return "fake";
                    }
                    public Icon getIcon() {
                        return null;
                    }
                    public Project getProject() {
                        return null;
                    }
                    public void addPropertyChangeListener(PropertyChangeListener listener) {}
                    public void removePropertyChangeListener(PropertyChangeListener listener) {}
                });
            }
        };

        Collection<String> args = Arrays.asList("test1", "test2");
        Collection<String> jvmArgs = Arrays.asList("test3", "test4");

        checkProperties(Arrays.asList("execute.file", java, "platform.java", "J", "work.dir", dir, "project", fake, "application.args", args, "run.jvmargs", jvmArgs),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath + File.separatorChar, "work.dir", FileUtil.toFile(dir).getAbsolutePath(), "run.jvmargs", "test3 test4", "encoding", "UTF-8", "platform.bootcp", JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().toString()),
                        "fake");
    }

    @Test public void testStartupExtender() throws Exception {
        File wd = workDir;
        FileObject fo = FileUtil.toFileObject(wd);
        FileObject java = FileUtil.createData(fo, "prj/A.java");
        FileObject prj = java.getParent();
        String prjPath = FileUtil.toFile(prj).getAbsolutePath();
        Lookup.getDefault().lookup(MockProjectFactory.class).prjDir = prj;
        checkProperties(JavaRunner.QUICK_RUN, Arrays.asList("execute.file", java, "platform.java", "J", JavaRunner.PROP_RUN_JVMARGS, Collections.singleton("-ea")),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath + File.separatorChar, "encoding", "UTF-8", "work.dir", FileUtil.toFile(prj).getAbsolutePath(), JavaRunner.PROP_RUN_JVMARGS, "-ea -Ddir=prj -Dvm=j2se", "platform.bootcp", JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().toString()));
        checkProperties(JavaRunner.QUICK_DEBUG, Arrays.asList("execute.file", java, "platform.java", "J", JavaRunner.PROP_RUN_JVMARGS, Collections.singleton("-ea")),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath + File.separatorChar, "encoding", "UTF-8", "work.dir", FileUtil.toFile(prj).getAbsolutePath(), JavaRunner.PROP_RUN_JVMARGS, "-ea", "platform.bootcp", JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().toString()));
        checkProperties(JavaRunner.QUICK_RUN, Arrays.asList(JavaRunner.PROP_WORK_DIR, prj, JavaRunner.PROP_CLASSNAME, "A", JavaRunner.PROP_EXECUTE_CLASSPATH, ClassPath.EMPTY, "platform.java", "J", JavaRunner.PROP_RUN_JVMARGS, Collections.singleton("-ea")),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", "", "encoding", "UTF-8", "work.dir", FileUtil.toFile(prj).getAbsolutePath(), JavaRunner.PROP_APPLICATION_ARGS, "", JavaRunner.PROP_RUN_JVMARGS, "-ea -Ddir=prj -Dvm=j2se", "platform.bootcp", JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries().toString()));
    }
    @StartupExtenderImplementation.Registration(displayName="mock", startMode=StartMode.NORMAL)
    public static class MockStartupExtender implements StartupExtenderImplementation {
        @Override public List<String> getArguments(Lookup context, StartMode mode) {
            return Arrays.asList("-Ddir=" + context.lookup(Project.class).getProjectDirectory().getNameExt(), "-Dvm=" + context.lookup(JavaPlatform.class).getSpecification().getName());
        }
    }

    private void checkProperties(Collection<?> source, Collection<String> target) {
        checkProperties(source, target, null);
    }

    private void checkProperties(String command, Collection<?> source, Collection<String> target) {
        checkProperties(command, source, target, null);
    }

    private void checkProperties(Collection<?> source, Collection<String> target, String displayName) {
        checkProperties("build", source, target, displayName);
    }

    private void checkProperties(String command, Collection<?> source, Collection<String> target, String displayName) {
        Map<String, Object> sourceMap = new HashMap<String, Object>();

        for (Iterator<?> it = source.iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = it.next();

            sourceMap.put(key, value);

        }

        Map<String,String> golden = new TreeMap<String,String>();

        for (Iterator<String> it = target.iterator(); it.hasNext();) {
            String key = it.next();
            String value = it.next();

            golden.put(key, value);
        }

        String[] projectName = new String[1];
        Map<String,String> out = ProjectRunnerImpl.computeProperties(command, sourceMap, projectName);

        assertEquals(golden.toString(), out.toString());

        if (displayName != null) {
            assertEquals(displayName, projectName[0]);
        }
    }

    private static final class ProjectImpl implements Project {

        private final FileObject dir;

        public ProjectImpl(FileObject dir) {
            this.dir = dir;
        }

        public FileObject getProjectDirectory() {
            return dir;
        }

        public Lookup getLookup() {
            return  Lookups.fixed(new ClassPathProvider() {
                private final ClassPath cp = ClassPathSupport.createClassPath(dir);

                public ClassPath findClassPath(FileObject file, String type) {
                    if (ClassPath.EXECUTE.equals(type) || ClassPath.SOURCE.equals(type)) {
                        return cp;
                    }
                    return null;
                }
            });
        }
    }

    public static final class MockProjectFactory implements ProjectFactory {

        volatile FileObject prjDir;

        @Override
        public boolean isProject(FileObject projectDirectory) {
            FileObject fo = prjDir;
            return fo != null && fo.equals(projectDirectory);
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                return new ProjectImpl(projectDirectory);
            }
            return null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
