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

        Map<String, Set<String>> tablesAndRefs = new HashMap<String, Set<String>>();
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

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return rootFolder.equals(file) || FileUtil.isParentOf(rootFolder, file);
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public FileObject getRootFolder() {
            return rootFolder;
        }

        public String getName() {
            return null;
        }

        public String getDisplayName() {
            return null;
        }
    }
}
