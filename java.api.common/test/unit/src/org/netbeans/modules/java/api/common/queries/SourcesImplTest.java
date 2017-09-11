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
