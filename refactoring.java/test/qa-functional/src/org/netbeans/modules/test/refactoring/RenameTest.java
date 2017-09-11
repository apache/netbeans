/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
