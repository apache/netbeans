/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 * Tests for {@link SourceLevelQueryImpl}.
 *
 * @author Tomas Mysik
 */
public class SourceLevelQueryImplTest extends NbTestCase {

    private static final String JDK_8 = "8";    //NOI18N
    private static final String JDK_8_ALIAS = "1.8";    //NOI18N
    private static final String JDK_7_ALIAS = "1.7";    //NOI18N
    private static final String JAVAC_SOURCE = "1.2";
    private static final String DEFAULT_JAVAC_SOURCE = "17.2";

    private static final String TEST_PLATFORM = "TestPlatform";
    private static final String BROKEN_PLATFORM = "BrokenPlatform";

    private FileObject scratch;
    private FileObject projdir;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private Project prj;

    public SourceLevelQueryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(
                AntBasedTestUtil.testAntBasedProjectType(),
                new TestPlatformProvider());
        this.clearWorkDir();
        Properties p = System.getProperties();
        if (p.getProperty("netbeans.user") == null) {
            p.put("netbeans.user", FileUtil.toFile(TestUtil.makeScratchDir(this)).getAbsolutePath());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        super.tearDown();
    }


    private void prepareProject(
            @NonNull final String platformName) throws IOException {
        prepareProject(platformName, null, null, null);
    }
    private void prepareProject(
            @NonNull final String platformName,
            @NullAllowed final String sourceLevel,
            @NullAllowed final String targetLevel,
            @NullAllowed final String profile) throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        helper = ProjectGenerator.createProject(projdir, "test");
        assertNotNull(helper);
        prj = ProjectManager.getDefault().findProject(projdir);
        assertNotNull(prj);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("javac.source", "${def}");
        props.setProperty("javac.target",
            targetLevel == null ?
            "${def}" :
            targetLevel);
        props.setProperty("platform.active", platformName);
        props.setProperty("def",
                sourceLevel != null ?
                sourceLevel :
                JAVAC_SOURCE);
        if (profile != null) {
            props.setProperty("javac.profile", profile);    //NOI18N
        }
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        props = PropertyUtils.getGlobalProperties();
        props.put("default.javac.source", DEFAULT_JAVAC_SOURCE);
        PropertyUtils.putGlobalProperties(props);
        eval = helper.getStandardPropertyEvaluator();
        assertNotNull(eval);
    }

    public void testGetSourceLevelWithValidPlatform() throws Exception {
        this.prepareProject(TEST_PLATFORM);

        FileObject dummy = projdir.createData("Dummy.java");
        SourceLevelQueryImplementation sourceLevelQuery = QuerySupport.createSourceLevelQuery(eval);

        String sl = sourceLevelQuery.getSourceLevel(dummy);
        assertEquals(JAVAC_SOURCE, sl);
    }

    public void testGetSourceLevelWithBrokenPlatform() throws Exception {
        this.prepareProject(BROKEN_PLATFORM);

        FileObject dummy = projdir.createData("Dummy.java");
        SourceLevelQueryImplementation sourceLevelQuery = QuerySupport.createSourceLevelQuery(eval);

        String sl = sourceLevelQuery.getSourceLevel(dummy);
        assertEquals(DEFAULT_JAVAC_SOURCE, sl);
    }

    public void testSourceLevelQuery2() throws Exception {
        this.prepareProject(TEST_PLATFORM);
        final FileObject dummy = projdir.createData("Dummy.java");
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertNotNull(result);
        assertEquals(JAVAC_SOURCE, result.getSourceLevel().toString());
    }

    public void testFiring() throws Exception {
        this.prepareProject(TEST_PLATFORM);
        final FileObject dummy = projdir.createData("Dummy.java");
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertNotNull(result);
        assertEquals(JAVAC_SOURCE, result.getSourceLevel().toString());
        class TestChangeListener implements ChangeListener {
            final AtomicInteger ec = new AtomicInteger();
            @Override
            public void stateChanged(final ChangeEvent event) {
                ec.incrementAndGet();
            }
        }
        final TestChangeListener tl = new TestChangeListener();
        result.addChangeListener(tl);
        final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("javac.source", "1.7");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertEquals(1, tl.ec.intValue());
        assertEquals("1.7", result.getSourceLevel().toString());
    }

    public void testProfilesJDK8ProfileGiven() throws IOException {
        this.prepareProject(TEST_PLATFORM, JDK_8, null, SourceLevelQuery.Profile.COMPACT2.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT2, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
    }

    public void testProfilesJDK8AliasProfileGiven() throws IOException {
        this.prepareProject(TEST_PLATFORM, JDK_8_ALIAS, null, SourceLevelQuery.Profile.COMPACT2.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT2, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
    }

    public void testProfilesJDK8AliasProfileNotGiven() throws IOException {
        this.prepareProject(TEST_PLATFORM, JDK_8, null, null);
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.DEFAULT, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
    }

    public void testProfilesOldJDKAliasProfileGiven() throws IOException {
        this.prepareProject(TEST_PLATFORM, JAVAC_SOURCE, null, SourceLevelQuery.Profile.COMPACT2.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.DEFAULT, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
    }

    public void testProfilesSourceJDK7AliasTargetJDK8ProfileGiven() throws IOException {
        this.prepareProject(TEST_PLATFORM, JDK_7_ALIAS, JDK_8_ALIAS, SourceLevelQuery.Profile.COMPACT2.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT2, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
    }

    public void testProfileChanges() throws Exception {
        prepareProject(TEST_PLATFORM, JDK_8, null, SourceLevelQuery.Profile.COMPACT1.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT1, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.setProperty("javac.profile", SourceLevelQuery.Profile.COMPACT2.getName());   //NOI18N
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
        result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT2, ((SourceLevelQueryImplementation2.Result2)result).getProfile());

    }

    public void testProfileListening() throws Exception {
        prepareProject(TEST_PLATFORM, JDK_8, null, SourceLevelQuery.Profile.COMPACT1.getName());
        final FileObject dummy = projdir.createData("Dummy.java");  //NOI18N
        final SourceLevelQueryImplementation2 sourceLevelQuery = QuerySupport.createSourceLevelQuery2(eval);
        final SourceLevelQueryImplementation2.Result result = sourceLevelQuery.getSourceLevel(dummy);
        assertTrue(result instanceof SourceLevelQueryImplementation2.Result2);
        assertEquals(SourceLevelQuery.Profile.COMPACT1, ((SourceLevelQueryImplementation2.Result2)result).getProfile());
        final AtomicInteger eventCount = new AtomicInteger();
        final ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                eventCount.getAndIncrement();
            }
        };
        result.addChangeListener(listener);
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.setProperty("javac.profile", SourceLevelQuery.Profile.COMPACT2.getName());   //NOI18N
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
        assertEquals(1, eventCount.get());
        assertEquals(SourceLevelQuery.Profile.COMPACT2, ((SourceLevelQueryImplementation2.Result2)result).getProfile());

    }

    private static class TestPlatformProvider implements JavaPlatformProvider {

        private JavaPlatform platform;

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms()  {
            return new JavaPlatform[] {
                getDefaultPlatform()
            };
        }

        @Override
        public JavaPlatform getDefaultPlatform()  {
            if (this.platform == null) {
                this.platform = new TestPlatform();
            }
            return this.platform;
        }
    }

    private static class TestPlatform extends JavaPlatform {

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
            return new Specification("j2se", new SpecificationVersion("1.5"));
        }

        @Override
        public ClassPath getSourceFolders() {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.singletonMap("platform.ant.name", TEST_PLATFORM);
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
            return "TestPlatform";
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }
    }
}
