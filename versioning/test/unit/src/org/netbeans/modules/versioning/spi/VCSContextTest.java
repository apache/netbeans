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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import java.io.IOException;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;

import java.io.FileFilter;
import java.io.File;
import junit.framework.TestSuite;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;

/**
 * Versioning SPI unit tests of VCSContext.
 * 
 * @author Maros Sandor
 */
public class VCSContextTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSContextTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir();
        File userdir = new File(getWorkDir() + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("data.root.dir", dataRootDir.getAbsolutePath());
        FileObject fo = FileUtil.toFileObject(getDataDir());
    }
    
    public static TestSuite suite () {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new VCSContextTest("testForEmptyNodes"));
        suite.addTest(new VCSContextTest("testForFileNodes"));
        suite.addTest(new VCSContextTest("testForProjectNodes"));
        suite.addTest(new VCSContextTest("testSubstract"));
        return suite;
    }

    public void testForEmptyNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[0]);
        assertTrue(ctx.getRootFiles().isEmpty());
        assertTrue(ctx.getFiles().isEmpty());
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).isEmpty());
    }

    public void testForFileNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "dummy2")), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 2);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 2);
        
        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "workdir/root")), new DummyFileNode(new File(dataRootDir, "workdir/root/a.txt")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileDilter()).size() == 1);        
    }

    public void testForProjectNodes() throws IOException {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyProjectNode(new File(dataRootDir, "workdir/root")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().size() == 1);
        assertTrue(ctx.computeFiles(new DummyFileDilter()).isEmpty());
    }


    public void testSubstract() throws IOException {
        MockServices.setServices(DummySharabilityImplementations.class);
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyProjectNode(new File(dataRootDir, "workdir/root-with-exclusions")), new DummyProjectNode(new File(dataRootDir, "workdir/root-with-exclusions/folder"))});
        assertTrue(ctx.getRootFiles().size() == 1);
        assertEquals(2, ctx.getFiles().size());
        assertEquals(2, ctx.getExclusions().size());
        assertEquals(2, ctx.computeFiles(new DummyFileDilter()).size());
    }

    private class DummyFileDilter implements FileFilter {

        private final boolean acceptAll;

        public DummyFileDilter() {
            this(true);
        }

        public DummyFileDilter(boolean acceptAll) {
            this.acceptAll = acceptAll;
        }

        @Override
        public boolean accept(File pathname) {
            return acceptAll;
        }
    }

    private class DummyFileNode extends AbstractNode {
        public DummyFileNode(File file) {
            super(Children.LEAF, Lookups.fixed(file));
        }
    }

    private class DummyProjectNode extends AbstractNode {

        public DummyProjectNode(File file) throws IOException {
            super(Children.LEAF, Lookups.fixed(new DummyProject(file)));
        }
    }

    private static class DummyProject implements Project {

        private final File file;

        public DummyProject(File file) throws IOException {
            this.file = file;
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        @Override
        public FileObject getProjectDirectory() {
            return FileUtil.toFileObject(file);
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(file);
        }
    }

    public static class DummySharabilityImplementations implements SharabilityQueryImplementation {

        @Override
        public int getSharability(File file) {
            if (!file.getAbsolutePath().startsWith(System.getProperty("data.root.dir"))) {
                return SharabilityQuery.UNKNOWN;
            }
            if (file.getName().contains("excl") && !file.getName().contains("root")) {
                return SharabilityQuery.SHARABLE;
            }
            return SharabilityQuery.NOT_SHARABLE;
        }

    }
}
