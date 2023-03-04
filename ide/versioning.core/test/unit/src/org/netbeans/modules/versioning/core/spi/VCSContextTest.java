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
package org.netbeans.modules.versioning.core.spi;

import java.io.IOException;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

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
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
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
        assertTrue(ctx.computeFiles(new DummyFileFilter()).isEmpty());
    }

    public void testForFileNodes() {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileFilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(dataRootDir) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileFilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(dataRootDir), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileFilter()).size() == 1);

        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "dummy2")), new DummyFileNode(new File(dataRootDir, "dummy")) });
        assertTrue(ctx.getRootFiles().size() == 2);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileFilter()).size() == 2);
        
        ctx = VCSContext.forNodes(new Node[] { new DummyFileNode(new File(dataRootDir, "workdir/root")), new DummyFileNode(new File(dataRootDir, "workdir/root/a.txt")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 2);
        assertTrue(ctx.getExclusions().isEmpty());
        assertTrue(ctx.computeFiles(new DummyFileFilter()).size() == 1);        
    }

    public void testForProjectNodes() throws IOException {
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyProjectNode(new File(dataRootDir, "workdir/root")) });
        assertTrue(ctx.getRootFiles().size() == 1);
        assertTrue(ctx.getFiles().size() == 1);
        assertTrue(ctx.getExclusions().size() == 1);
        assertTrue(ctx.computeFiles(new DummyFileFilter()).isEmpty());
    }


    public void testSubstract() throws IOException {
        MockServices.setServices(DummySharabilityImplementations.class);
        Node secondNode = new DummyProjectNode(new File(dataRootDir, "workdir/root-with-exclusions/folder"));
        VCSContext ctx = VCSContext.forNodes(new Node[] { new DummyProjectNode(new File(dataRootDir, "workdir/root-with-exclusions")), secondNode});
        assertTrue(ctx.getRootFiles().size() == 1);
        assertEquals(2, ctx.getFiles().size());
        assertEquals(2, ctx.getExclusions().size());
        assertEquals(2, ctx.computeFiles(new DummyFileFilter()).size());
    }

    private class DummyFileFilter implements VCSContext.FileFilter {

        private final boolean acceptAll;

        public DummyFileFilter() {
            this(true);
        }

        public DummyFileFilter(boolean acceptAll) {
            this.acceptAll = acceptAll;
        }

        @Override
        public boolean accept(VCSFileProxy pathname) {
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
