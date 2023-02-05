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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SelectedTablesTest extends NbTestCase {

    public SelectedTablesTest(String testName) {
        super(testName);
    }

    public void testBasic() throws Exception {
        class CL implements ChangeListener {
            private int changeCount;

            @Override
            public void stateChanged(ChangeEvent event) {
                changeCount++;
            }
        }

        clearWorkDir();
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        FileObject locationFO = workDirFO.createFolder("location");
        String package1Name = "package1";
        FileObject package1FO = locationFO.createFolder(package1Name);
        String package2Name = "package2";
        FileObject package2FO = locationFO.createFolder(package2Name);
        package1FO.createData("Table3", "java");
        SourceGroup location = new SourceGroupImpl(locationFO);

        Map<String, Set<String>> tablesAndRefs = new HashMap<>();
        tablesAndRefs.put("TABLE1", Collections.<String>emptySet());
        tablesAndRefs.put("TABLE2", Collections.<String>emptySet());

        TableProviderImpl provider = new TableProviderImpl(null, null, tablesAndRefs);
        TableClosure closure = new TableClosure(provider);
        PersistenceGenerator persistenceGen = new PersistenceGeneratorImpl();

        SelectedTables selectedTables = new SelectedTables(persistenceGen, closure, location, package1Name);
        CL cl = new CL();
        selectedTables.addChangeListener(cl);

        assertEquals(0, selectedTables.getTables().size());
        assertNull(selectedTables.getFirstProblem());
        assertEquals(0, cl.changeCount);

        closure.addTables(Collections.singleton(provider.getTableByName("TABLE1")));

        assertEquals(1, selectedTables.getTables().size());
        assertNull(selectedTables.getFirstProblem());
        assertEquals(1, cl.changeCount);

        Table table = provider.getTableByName("TABLE2");
        closure.addTables(Collections.singleton(table));

        assertEquals(2, selectedTables.getTables().size());
        assertNull(selectedTables.getFirstProblem());
        assertEquals(2, cl.changeCount);

        selectedTables.setClassName(table,"Table@");

        assertEquals(2, selectedTables.getTables().size());
        assertEquals(SelectedTables.Problem.NO_JAVA_IDENTIFIER, selectedTables.getFirstProblem());
        assertEquals(3, cl.changeCount);

        selectedTables.setClassName(table,"SELECT");

        assertEquals(2, selectedTables.getTables().size());
        assertEquals(SelectedTables.Problem.JPA_QL_IDENTIFIER, selectedTables.getFirstProblem());
        assertEquals(4, cl.changeCount);

        selectedTables.setClassName(table,"Table3");

        assertEquals(2, selectedTables.getTables().size());
//        assertEquals(SelectedTables.Problem.ALREADY_EXISTS, selectedTables.getFirstProblem());
        assertNull(selectedTables.getFirstProblem());
        assertEquals(5, cl.changeCount);

        selectedTables.setTargetFolder(location, package2Name);

        assertEquals(2, selectedTables.getTables().size());
        assertNull(selectedTables.getFirstProblem());
        assertEquals(6, cl.changeCount);
    }

    public static final class SourceGroupImpl implements SourceGroup {

        private final FileObject rootFolder;

        public SourceGroupImpl(FileObject rootFolder) {
            this.rootFolder = rootFolder;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public boolean contains(FileObject file) throws IllegalArgumentException {
            return rootFolder.equals(file) || FileUtil.isParentOf(rootFolder, file);
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public FileObject getRootFolder() {
            return rootFolder;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
