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

package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.TestRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class SourcesImplTest extends NbTestCase {

    private FileObject projDir;
    private FileObject srcDir;
    private Project project;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private TestRoots testRoots;
    private Sources src;

    public SourcesImplTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType(), TestUtil.testProjectFactory());
        final FileObject scratch = TestUtil.makeScratchDir(this);
        projDir = scratch.createFolder("proj"); //NOI18N
        srcDir = projDir.createFolder("src");   //NOI18N
        helper = ProjectGenerator.createProject(projDir, "test");
        eval = helper.getStandardPropertyEvaluator();
        project = FileOwnerQuery.getOwner(projDir);
        testRoots = new TestRoots(helper);
        testRoots.addRoot("src", srcDir, "Sources");
        src = QuerySupport.createSources(project, helper, eval, testRoots, Roots.nonSourceRoots("dist.dir"));
    }

    public void testBasic() throws Exception {
        final SourceGroup[] sg = src.getSourceGroups(TestRoots.TYPE_TEST);   //NOI18N
        assertNotNull(sg);
        assertEquals(1,sg.length);
        assertEquals(srcDir, sg[0].getRootFolder());
    }

    public void testIncludesExcludes() throws Exception {
        SourceGroup g = src.getSourceGroups(TestRoots.TYPE_TEST)[0];
        assertEquals(srcDir, g.getRootFolder());
        FileObject objectJava = FileUtil.createData(srcDir, "java/lang/Object.java");
        FileObject jcJava = FileUtil.createData(srcDir, "javax/swing/JComponent.java");
        FileObject doc = FileUtil.createData(srcDir, "javax/swing/doc-files/index.html");
        assertTrue(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertTrue(g.contains(doc));
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.INCLUDES, "javax/swing/");
        ep.setProperty(ProjectProperties.EXCLUDES, "**/doc-files/");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertFalse(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertFalse(g.contains(doc));
    }

    public void testFiring() throws Exception {
        SourceGroup[] groups = src.getSourceGroups(TestRoots.TYPE_TEST);
        assertEquals(1, groups.length);
        class EventCounter implements ChangeListener {
            final AtomicInteger count = new AtomicInteger();

            public void stateChanged(ChangeEvent e) {
                count.incrementAndGet();
            }
        };
        final EventCounter counter = new EventCounter();
        src.addChangeListener(counter);
        //test: adding of src root should fire once
        final FileObject srcDir2 = projDir.createFolder("src2");
        testRoots.addRoot("src2",srcDir2,"New Root");
        assertEquals(1, counter.count.get());
        groups = src.getSourceGroups(TestRoots.TYPE_TEST);
        assertEquals(2, groups.length);
        //test: removing of src root should fire once
        counter.count.set(0);
        testRoots.removeRoot("src2");
        assertEquals(1, counter.count.get());
        groups = src.getSourceGroups(TestRoots.TYPE_TEST);
        assertEquals(1, groups.length);
    }

    public void testNonSources() throws IOException {
        final FileObject dist1 = projDir.getParent().createFolder("dist1");
        assertEquals(null, FileOwnerQuery.getOwner(dist1));
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("dist.dir","../dist1");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertEquals(project, FileOwnerQuery.getOwner(dist1));

        final FileObject dist2 = projDir.getParent().createFolder("dist2");
        assertEquals(null, FileOwnerQuery.getOwner(dist2));
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("dist.dir","../dist2");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertEquals(project, FileOwnerQuery.getOwner(dist2));
        assertEquals(null, FileOwnerQuery.getOwner(dist1));

    }
}
