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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;
import org.junit.Assume;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

abstract class SuiteCheck extends NbTestCase {
    SuiteCheck(String name) {
        super(name);
        log(Level.INFO, "Test created by %s classloader", getClass().getClassLoader());
    }

    static Test suite(final Class<? extends Test> clazz) {
        DumpStack.start();
        return NbModuleSuite.emptyConfiguration().
                gui(false).
                enableClasspathModules(true).
                addTest(clazz).
                suite();
    }

    @Override
    protected void setUp() throws Exception {
        log(Level.INFO, "setUp - init");
        super.setUp();
        final Logger tooVerboseLogger = Logger.getLogger("org.netbeans.core.startup.InstalledFileLocatorImpl");
        tooVerboseLogger.setUseParentHandlers(false);
        try {
            MxSuite.parse(null);
        } catch (LinkageError err) {
            Assume.assumeNoException("Cannot initialize Polyglot API, are you using GraalVM?", err);
        }
        log(Level.INFO, "setUp - exit");
    }

    @Override
    protected void tearDown() throws Exception {
        Enumeration<String> en = LogManager.getLogManager().getLoggerNames();
        while (en.hasMoreElements()) {
            String n = en.nextElement();
            Logger l = LogManager.getLogManager().getLogger(n);
            boolean first = true;
            if (l == null || l.getHandlers() == null) {
                continue;
            }
            for (Handler h : l.getHandlers()) {
                if (first) {
                    System.err.println("cleaning logger '" + n + "'");
                    first = false;
                }
                System.err.println("  removing handler: " + h);
                l.removeHandler(h);
            }
        }
    }

    @Override
    protected int timeOut() {
        return 1_200_000;
    }

    protected final void verifyNoErrorsInSuite(final String suiteName, String... onlySourceGroups) throws IllegalArgumentException, IOException, URISyntaxException {
        long begin = System.currentTimeMillis();
        File sibling = findSuite(suiteName);

        FileObject fo = FileUtil.toFileObject(sibling);
        assertNotNull("project directory found", fo);

        log(Level.INFO, "Recognizing project %s", fo);
        long now = System.currentTimeMillis();
        Project p = ProjectManager.getDefault().findProject(fo);
        long took = System.currentTimeMillis() - now;
        assertNotNull("project found", p);
        log(Level.INFO, "Project found %s in %d ms", p, took);
        assertEquals("It is suite project: " + p, "SuiteProject", p.getClass().getSimpleName());
        OpenProjects.getDefault().open(new Project[]{p}, false);

        StringBuilder errors = new StringBuilder();
        FileObject[] errornous = { null };
        Sources src = ProjectUtils.getSources(p);
        int cnt = 0;
        for (SourceGroup sourceGroup : src.getSourceGroups("java")) {
            if (sourceGroup instanceof Compliance.Provider) {
                Compliance c = ((Compliance.Provider) sourceGroup).getCompliance();
                if (!c.includes(11)) {
                    log(Level.INFO, "Skipping check of %s with compliance %s", sourceGroup, c);
                    continue;
                }
            }
            FOUND: if (onlySourceGroups.length > 0) {
                for (String gName : onlySourceGroups) {
                    if (sourceGroup.getDisplayName().equals(gName)) {
                        cnt++;
                        break FOUND;
                    }
                }
                // not found
                continue;
            }
            assertSourcesNoError(p, errornous, errors, sourceGroup.getRootFolder(), begin);
        }
        assertCompilationErrors(errors, errornous);

        assertEquals("Exactly as many source groups tested as requested", onlySourceGroups.length, cnt);
    }

    protected final File findSuite(String suite) throws URISyntaxException {
        File location = getDataDir();
        while (location != null) {
            File graal = new File(location, "graal");
            File suiteDir = new File(graal, suite);
            if (suiteDir.isDirectory()) {
                return suiteDir;
            }
            location = location.getParentFile();
        }
        fail("Cannot find truffle next to " + getDataDir());
        return null;
    }

    private void assertSourcesNoError(Project project, FileObject[] errornous, StringBuilder errors, FileObject dir, long begin) throws IOException {
        long now = System.currentTimeMillis();
        log(Level.INFO, "assertSourcesNoError for %s", dir);
        IndexingManager.getDefault().refreshIndexAndWait(dir.toURL(), null, false);
        log(Level.INFO, "      refresh done       %s", dir);
        Enumeration<? extends FileObject> en = dir.getChildren(true);
        int nonJavaCount = 0;
        int javaCount = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (fo.isFolder()) {
                continue;
            }
            Project prj = FileOwnerQuery.getOwner(fo);
            assertSame("FileOwnerQuery returns the right project", project, prj);
            if (!fo.hasExt("java")) {
                nonJavaCount++;
                continue;
            }
            JavaSource source = JavaSource.forFileObject(fo);
            if (source == null) {
                fail("No source for " + fo);
            }
            javaCount++;
            BinaryForSourceQuery.Result res = BinaryForSourceQuery.findBinaryRoots(dir.toURL());
            assertEquals("There is one binary root: " + Arrays.toString(res.getRoots()), 1, res.getRoots().length);
            OK: for (URL root : res.getRoots()) {
                SourceForBinaryQuery.Result2 res2 = SourceForBinaryQuery.findSourceRoots2(root);
                assertTrue("Has to prefer sources", res2.preferSources());
                for (FileObject src : res2.getRoots()) {
                    if (src.equals(dir)) {
                        break OK;
                    }
                }
                fail("Expecting to find " + dir + " among:\n" + Arrays.toString(res2.getRoots()));
            }
            source.runUserActionTask((CompilationController p) -> {
                p.toPhase(JavaSource.Phase.RESOLVED);
                for (Diagnostic<?> d : p.getDiagnostics()) {
                    if (d.getKind() != Diagnostic.Kind.ERROR) {
                        continue;
                    }
                    errornous[0] = fo;
                    StringBuilder msg = new StringBuilder();
                    msg.append(d.getSource()).append(": ").append(d.getLineNumber()).append(": ").append(d.getMessage(Locale.ENGLISH)).append("\n");
                    System.err.println(msg);
                    errors.append(msg.toString());
                }
            }, true);
        }
        long took = System.currentTimeMillis() - now;
        long tookOverall = System.currentTimeMillis() - begin;
        log(Level.INFO, "         verified %d java files and %d non-java files in %d ms, overall running for %d s", javaCount, nonJavaCount, took, tookOverall / 1000);
        if (errornous[0] == null) {
            log(Level.INFO, "assertSourcesNoError OK: %s", dir);
        } else {
            log(Level.WARNING, "         errors found in %s", errornous[0]);
            ClassPath cp = ClassPath.getClassPath(errornous[0], ClassPath.COMPILE);
            if (cp == null) {
                log(Level.WARNING, "Classpath for " + errornous[0] + " is null!");
            } else {
                log(Level.WARNING, "Classpath for " + errornous[0] + ":");
                int cnt = 0;
                for (FileObject fo : cp.getRoots()) {
                    log(Level.WARNING, " cp#" + ++cnt + ": " + fo);
                }
            }
        }
    }

    private void assertCompilationErrors(StringBuilder errors, FileObject[] errornous) {
        if (errors.length() > 0) {
            String[] types = { ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, JavaClassPathConstants.PROCESSOR_PATH };
            for (String t : types) {
                ClassPath cp = ClassPath.getClassPath(errornous[0], t);
                errors.append("\n\nClasspath (").append(t).append("):");
                if (cp == null) {
                    errors.append(" is null");
                } else {
                    for (FileObject root : cp.getRoots()) {
                        errors.append("\n  ").append(root.toString());
                    }
                }
                errors.append("\n\n");
            }
            fail(errors.toString());
        }
    }

    private void log(Level l, String msg, Object... args) {
        Logger.getLogger(getClass().getName()).log(l, String.format(msg, args));
    }

}
