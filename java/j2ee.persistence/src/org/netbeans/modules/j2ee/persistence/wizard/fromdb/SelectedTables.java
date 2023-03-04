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
    private final Map<Table, String> table2ClassName = new HashMap<>();
    private final Map<Table, Set<Problem>> table2Problems = new TreeMap<>();
    private final Map<Table, UpdateType> table2UpdateType = new HashMap<>();

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
        Set<Table> addedTables = new HashSet<>(tableClosure.getSelectedTables());
        addedTables.removeAll(validatedTables);

        Set<Table> removedTables = new HashSet<>(validatedTables);
        removedTables.removeAll(tableClosure.getSelectedTables());

        table2Problems.keySet().removeAll(removedTables);

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
        List<Table> result = new ArrayList<>(tableClosure.getSelectedTables());
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
            if (exClassName != null) {
                int i = exClassName.lastIndexOf('.');
                if (i > -1) {
                    exClassName = exClassName.substring(i + 1);
                }
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

        @Override
        public void stateChanged(ChangeEvent event) {
            validateTables();
        }
    }
}
