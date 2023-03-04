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
package org.netbeans.modules.test.refactoring;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.modules.test.refactoring.actions.RefactorRenameAction;
import org.netbeans.modules.test.refactoring.operators.RenameOperator;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class RenameTest extends ModifyingRefactoring {

    public RenameTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(RenameTest.class, "testRenameClass").
                addTest(RenameTest.class, "testRenamePackage").
                addTest(RenameTest.class, "testRenameMethod").
                addTest(RenameTest.class, "testRenameGenerics").
                addTest(RenameTest.class, "testRenameVariable").
                addTest(RenameTest.class, "testRenameParameter").
                addTest(RenameTest.class, "testRenameCtor").
                addTest(RenameTest.class, "testRenameProperty").
                suite();
    }

    public void testRenameClass() {
        performRename("Rename", "renameClass", "Renamed", 44, 17);
    }

    public void testRenamePackage() {
        performRename("RenamePkg", "renamePkg", "renamedPkg", 42, 12);
    }

    public void testRenameMethod() {
        performRename("RenameMethod", "renameClass", "renamedMethod", 46, 18);
    }

    public void testRenameGenerics() {
        performRename("RenameGenerics", "renameClass", "A", 44, 30);
    }

    public void testRenameVariable() {
        performRename("RenameLocalVariable", "renameClass", "renamed", 47, 16);
    }

    public void testRenameParameter() {
        performRename("RenameParameter", "renameClass", "renamned", 46, 34);
    }

    public void testRenameCtor() {
        performRename("RenameCtor", "renameClass", "RenamedCtor", 46, 34);
    }

    public void testRenameProperty() {
        performRename("RenameProperty", "renameClass", "renamed", 46, 21, true);
    }

    private void performRename(String className, String pkgName, String newName, int row, int col) {
        performRename(className, pkgName, newName, row, col, false);
    }

    private void performRename(String className, String pkgName, String newName, int row, int col, boolean property) {
        openSourceFile(pkgName, className);
        EditorOperator editor = new EditorOperator(className + ".java");
        editor.setCaretPosition(row, col);
        editor.select(row, col, col + 1);

        new RefactorRenameAction().performPopup(editor);

        RenameOperator ro = new RenameOperator();
        ro.getNewName().typeText(newName);
        if (property) {
            ro.getProperty().setSelected(true);
        }
        ro.preview();
        dumpRefactoringResults();

        editor.closeDiscard();
    }
}
