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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public final class SelectedTables {

    // not private because used in tests
    enum Problem { NO_JAVA_IDENTIFIER, JPA_QL_IDENTIFIER, ALREADY_EXISTS };

    private final PersistenceGenerator persistenceGen;
    private final Map<Table, String> table2ClassName = new HashMap<Table, String>();
    private final Map<Table, Set<Problem>> table2Problems = new TreeMap<Table, Set<Problem>>();
    private final Map<Table, UpdateType> table2UpdateType = new HashMap<Table, UpdateType>();

    private final ChangeListener tableClosureListener = new TableClosureListener();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private TableClosure tableClosure;
    private SourceGroup location;
    private String packageName;
    private FileObject targetFolder;

    private Set<Table> validatedTables = Collections.emptySet();

    public SelectedTables(PersistenceGenerator persistenceGen, TableClosure tableClosure, SourceGroup location, String packageName) throws IOException {
        assert persistenceGen != null;

        this.persistenceGen = persistenceGen;
        setTableClosureAndTargetFolder(tableClosure, location, packageName);
    }

    /**
     * Sets the new table closure and target folder at once. This is needed
     * in order to avoid multiple validations in the table closure and the target
     * folder were set separately.
     */
    public void setTableClosureAndTargetFolder(TableClosure tableClosure, SourceGroup location, String packageName) throws IOException {
        assert tableClosure != null;

        boolean tableClosureChanged = changeTableClosure(tableClosure);
        boolean targetFolderChanged = changeTargetFolder(location, packageName);

        if (tableClosureChanged || targetFolderChanged) {
            revalidateTables();
        }
    }

    /**
     * Sets the new target folder.
     */
    public void setTargetFolder(SourceGroup location, String packageName) throws IOException {
        if (changeTargetFolder(location, packageName)) {
            revalidateTables();
        }
    }

    /**
     * Sets the new table closure, returning true
     * if  the new table closure was different than the current value and
     * false otherwise.
     */
    private boolean changeTableClosure(TableClosure tableClosure) {
        if (!tableClosure.equals(this.tableClosure)) {
            if (this.tableClosure != null) {
                this.tableClosure.removeChangeListener(tableClosureListener);
            }
            this.tableClosure = tableClosure;
            table2ClassName.clear();
            table2UpdateType.clear();
            this.tableClosure.addChangeListener(tableClosureListener);
            return true;
        }
        return false;
    }

    /**
     * Sets the new target folder, returning true
     * if the new target folder was different from the current value and
     * false otherwise.
     */
    private boolean changeTargetFolder(SourceGroup location, String packageName) throws IOException {
        if (!Utilities.compareObjects(location, this.location) || !Utilities.compareObjects(packageName, this.packageName)) {
            this.location = location;
            this.packageName = packageName;

            if (location != null && packageName != null) {
                targetFolder = SourceGroups.getFolderForPackage(location, packageName, false);
            } else {
                targetFolder = null;
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a new change listener, which will be called when the list of selected
     * tables changes, when a class name for a table changes or when the
     * return value of {@link #getFirstProblemDisplayName} changes.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a change listener.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void revalidateTables() {
        validatedTables = Collections.emptySet();
        table2Problems.clear();
        validateTables();
    }

    private void validateTables() {
        Set<Table> addedTables = new HashSet<Table>(tableClosure.getSelectedTables());
        addedTables.removeAll(validatedTables);

        Set<Table> removedTables = new HashSet<Table>(validatedTables);
        removedTables.removeAll(tableClosure.getSelectedTables());

        for (Table table : removedTables) {
            table2Problems.remove(table);
        }
        for (Table table : addedTables) {
            putProblems(table, validateClassName(getClassName(table)));
        }
        validatedTables = new HashSet<Table>(tableClosure.getSelectedTables());

        changeSupport.fireChange();
    }

    /**
     * Returns a sorted list of selected tables. This returns the same
     * tables as {@link TableClosure#getSelectedTables}.
     */
    public List<Table> getTables() {
        List<Table> result = new ArrayList<Table>(tableClosure.getSelectedTables());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the class name for the given table.
     */
    public String getClassName(Table table) {
        assert table != null;

        String className = table2ClassName.get(table);
        if (className == null) {
            String exClassName = persistenceGen.getFQClassName(table.getName());
            if(exClassName != null) {
                int i = exClassName.lastIndexOf('.');
                if(i>-1)exClassName = exClassName.substring(i+1);
                className = persistenceGen.generateEntityName(exClassName);
            } else {
                className = EntityMember.makeClassName(table.getName());
                className = persistenceGen.generateEntityName(className);
            }
        }
        return className;
    }

    /**
     * Sets the class name for the given table.
     */
    public void setClassName(Table table, String className) {
        assert table != null;
        assert className != null;

        table2ClassName.put(table, className);
        putProblems(table, validateClassName(className));

        changeSupport.fireChange();
    }

    public void setUpdateType(Table table, UpdateType updateType) {
        assert table != null;
        assert updateType != null;

        table2UpdateType.put(table, updateType);
        changeSupport.fireChange();
    }


    public UpdateType getUpdateType(Table table) {
        assert table != null;
        UpdateType ut = table2UpdateType.get(table);

        if (table.getDisabledReason() instanceof Table.ExistingDisabledReason){
            if (ut == null || ut == UpdateType.NEW){
                table2UpdateType.remove(table);
                ut = UpdateType.UPDATE;
            }
        } else {
            if (ut != null){
                table2UpdateType.remove(table);
            }
            ut = UpdateType.NEW;
        }

        return ut;
    }

    FileObject getTargetFolder(){
        return targetFolder;
    }

    private void putProblems(Table table, Set<Problem> problems) {
        if (problems.isEmpty()) {
            table2Problems.remove(table);
        } else {
            table2Problems.put(table, problems);
        }
    }

    private Set<Problem> validateClassName(String className) {
        Set<Problem> problems = EnumSet.noneOf(Problem.class);
        if (!Utilities.isJavaIdentifier(className)) {
            problems.add(Problem.NO_JAVA_IDENTIFIER);
        }
        if (JavaPersistenceQLKeywords.isKeyword(className)) {
            problems.add(Problem.JPA_QL_IDENTIFIER);
        }
/* commented to have an ability to update entity classes
        if (targetFolder != null && targetFolder.getFileObject(className, "java") != null) { // NOI18N
            problems.add(Problem.ALREADY_EXISTS);
        }
 */
        return problems;
    }

    public void ensureUniqueClassNames() {
        // make sure proposed class names are unique in the target package
        Set<Table> tables = tableClosure.getSelectedTables();
        if (targetFolder != null) {
            for (Table t : tables) {
                String className = getClassName(t);
                boolean existingFile = (targetFolder.getFileObject(className, "java") != null); // NOI18N

                if (existingFile){
                    setClassName(t, className+'1');
                }
            }
        }
    }

    /**
     * Returns the display name of the first problem regarding a table
     * or its class name (the class name may be an invalid Java identifier or
     * a JPA QL reserved keyword, or the class might exist in the target
     * folder.
     */
    public String getFirstProblemDisplayName() {
        Map.Entry<Table, Set<Problem>> firstProblemEntry = getFirstProblemEntry();
        if (firstProblemEntry == null) {
            return null;
        }
        return getProblemDisplayNameForTable(firstProblemEntry.getKey(), 
            firstProblemEntry.getValue());
    }

    private Map.Entry<Table, Set<Problem>> getFirstProblemEntry() {
        Set<Map.Entry<Table, Set<Problem>>> problemEntries = table2Problems.entrySet();
        if (problemEntries.isEmpty()) {
            return null;
        }
        return problemEntries.iterator().next();
    }

    private String getProblemDisplayNameForTable(Table table, Set<Problem> problems) {
        Problem problem = problems.iterator().next();
        return getProblemDisplayName(problem, getClassName(table));
    }

    String getProblemDisplayNameForTable(Table table) {
        Set<Problem> problems = table2Problems.get(table);
        return ((problems != null) ? 
            getProblemDisplayNameForTable(table, problems) : null);
    }

    boolean hasProblem(Table table) {
        return table2Problems.containsKey(table);
    }

    /**
     * Not private because used in tests.
     */
    Problem getFirstProblem() {
        Map.Entry<Table, Set<Problem>> firstProblemEntry = getFirstProblemEntry();
        if (firstProblemEntry == null) {
            return null;
        }
        Set<Problem> problems = firstProblemEntry.getValue();
        return problems.iterator().next();
    }

    private static String getProblemDisplayName(Problem problem, String className) {
        switch (problem) {
            case NO_JAVA_IDENTIFIER:
                return NbBundle.getMessage(SelectedTables.class, "ERR_NoJavaIdentifier", className);

            case JPA_QL_IDENTIFIER:
                return NbBundle.getMessage(SelectedTables.class, "ERR_ReservedQLKeyword", className);

            case ALREADY_EXISTS:
                return NbBundle.getMessage(SelectedTables.class, "ERR_AlreadyExists", className);

            default:
                assert false : problem + " should be handled in getProblemDisplayName()"; // NOI18N
        }
        return null;
    }

    private final class TableClosureListener implements ChangeListener {

        public void stateChanged(ChangeEvent event) {
            validateTables();
        }
    }
}
