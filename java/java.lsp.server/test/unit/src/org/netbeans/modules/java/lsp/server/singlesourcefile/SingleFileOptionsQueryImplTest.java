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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.protocol.Workspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class SingleFileOptionsQueryImplTest extends NbTestCase {

    public SingleFileOptionsQueryImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        clearWorkDir();
    }

    public void testFindWorkspaceFolder() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject workspace1 = FileUtil.createFolder(wd, "workspace1");
        FileObject source1 = FileUtil.createData(workspace1, "test/Test.java");
        FileObject prjRoot = FileUtil.createFolder(wd, "prjRoot");
        FileObject workspace2 = FileUtil.createFolder(prjRoot, "test2");
        FileObject source2 = FileUtil.createData(workspace2, "Test.java");

        Workspace workspace = new WorkspaceImpl(Arrays.asList(workspace1, workspace2));

        assertEquals(workspace1, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, source1));
        assertEquals(workspace1, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, source1.getParent()));
        assertEquals(workspace1, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, workspace1));
        assertEquals(workspace2, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, source2));
        assertEquals(workspace2, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, source2.getParent()));
        assertEquals(workspace2, SingleFileOptionsQueryImpl.findWorkspaceFolder(workspace, source2.getParent().getParent()));
    }

    public void testWorkspaceOptions() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject workspace1 = FileUtil.createFolder(wd, "workspace1");
        FileObject source1 = FileUtil.createData(workspace1, "test1/Test.java");
        FileObject workspace2 = FileUtil.createFolder(wd, "workspace2");
        FileObject source2 = FileUtil.createData(workspace2, "test2/Test.java");
        FileObject source3 = FileUtil.createData(wd, "test3/Test.java");

        SingleFileOptionsQueryImpl query = new SingleFileOptionsQueryImpl() {};
        Workspace workspace = new WorkspaceImpl(Arrays.asList(workspace1, workspace2));

        query.setConfiguration(workspace, "-Dtest=test", null);

        Lookups.executeWith(new ProxyLookup(Lookups.fixed(workspace), Lookup.getDefault()), () -> {
            assertEquals("-Dtest=test", query.optionsFor(source1).getOptions());
            assertEquals(workspace1.toURI(), query.optionsFor(source1).getWorkDirectory());
            assertEquals("-Dtest=test", query.optionsFor(source1.getParent()).getOptions());
            assertEquals(workspace1.toURI(), query.optionsFor(source1.getParent()).getWorkDirectory());

            assertEquals("-Dtest=test", query.optionsFor(source2).getOptions());
            assertEquals(workspace2.toURI(), query.optionsFor(source2).getWorkDirectory());
            assertEquals("-Dtest=test", query.optionsFor(source2.getParent()).getOptions());
            assertEquals(workspace2.toURI(), query.optionsFor(source2.getParent()).getWorkDirectory());

            AtomicInteger changeCount = new AtomicInteger();

            query.optionsFor(source1).addChangeListener(evt -> changeCount.incrementAndGet());

            query.setConfiguration(workspace, "-Dtest=test", null);

            assertEquals(0, changeCount.get());

            FileObject newWD = source1.getParent();

            query.setConfiguration(workspace, "-Dtest=test", FileUtil.toFile(newWD).getAbsolutePath());

            assertEquals(1, changeCount.get());

            assertEquals("-Dtest=test", query.optionsFor(source1).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source1).getWorkDirectory());
            assertEquals("-Dtest=test", query.optionsFor(source1.getParent()).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source1.getParent()).getWorkDirectory());

            assertEquals("-Dtest=test", query.optionsFor(source2).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source2).getWorkDirectory());
            assertEquals("-Dtest=test", query.optionsFor(source2.getParent()).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source2.getParent()).getWorkDirectory());

            query.setConfiguration(workspace, "-Dtest=test2", FileUtil.toFile(newWD).getAbsolutePath());

            assertEquals(2, changeCount.get());

            assertEquals("-Dtest=test2", query.optionsFor(source1).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source1).getWorkDirectory());
            assertEquals("-Dtest=test2", query.optionsFor(source1.getParent()).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source1.getParent()).getWorkDirectory());

            assertEquals("-Dtest=test2", query.optionsFor(source2).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source2).getWorkDirectory());
            assertEquals("-Dtest=test2", query.optionsFor(source2.getParent()).getOptions());
            assertEquals(newWD.toURI(), query.optionsFor(source2.getParent()).getWorkDirectory());

            query.setConfiguration(workspace, "-Dtest=test2", null);

            assertEquals(3, changeCount.get());

            assertEquals("-Dtest=test2", query.optionsFor(source1).getOptions());
            assertEquals(workspace1.toURI(), query.optionsFor(source1).getWorkDirectory());
            assertEquals("-Dtest=test2", query.optionsFor(source1.getParent()).getOptions());
            assertEquals(workspace1.toURI(), query.optionsFor(source1.getParent()).getWorkDirectory());

            assertEquals("-Dtest=test2", query.optionsFor(source2).getOptions());
            assertEquals(workspace2.toURI(), query.optionsFor(source2).getWorkDirectory());
            assertEquals("-Dtest=test2", query.optionsFor(source2.getParent()).getOptions());
            assertEquals(workspace2.toURI(), query.optionsFor(source2.getParent()).getWorkDirectory());
        });


        //with no workspace context:
        assertEquals("-Dtest=test2", query.optionsFor(source1).getOptions());
        assertEquals(workspace1.toURI(), query.optionsFor(source1).getWorkDirectory());
        assertEquals("-Dtest=test2", query.optionsFor(source1.getParent()).getOptions());
        assertEquals(workspace1.toURI(), query.optionsFor(source1.getParent()).getWorkDirectory());

        assertEquals("-Dtest=test2", query.optionsFor(source2).getOptions());
        assertEquals(workspace2.toURI(), query.optionsFor(source2).getWorkDirectory());
        assertEquals("-Dtest=test2", query.optionsFor(source2.getParent()).getOptions());
        assertEquals(workspace2.toURI(), query.optionsFor(source2.getParent()).getWorkDirectory());

        assertNull(query.optionsFor(source3));
        assertNull(query.optionsFor(source3.getParent()));
    }

    private static final class WorkspaceImpl implements Workspace {
        private final List<FileObject> workspaceFolders;

        public WorkspaceImpl(List<FileObject> workspaceFolders) {
            this.workspaceFolders = workspaceFolders;
        }

        @Override
        public List<FileObject> getClientWorkspaceFolders() {
            return workspaceFolders;
        }

    }
}
