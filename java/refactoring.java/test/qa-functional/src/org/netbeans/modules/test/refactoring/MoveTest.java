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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.modules.test.refactoring.actions.RefactorMoveAction;
import org.netbeans.modules.test.refactoring.operators.MoveClassDialogOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class MoveTest extends ModifyingRefactoring {

    private enum TargetDestination {
        SOURCE, TESTS
    }

    public MoveTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(MoveTest.class, "testMoveClass").
                addTest(MoveTest.class, "testMoveToTest").
                addTest(MoveTest.class, "testMoveToNewPackage").
                suite();
    }

    public void testMoveClass() {
        performMove("Move.java", "moveSource", "modeDest", false, MoveTest.TargetDestination.SOURCE);
    }

    public void testMoveToTest() {
        performMove("MoveToTest.java", "moveSource", "dest", true, MoveTest.TargetDestination.TESTS);
    }

    public void testMoveToNewPackage() {
        performMove("MoveToNewPkg.java", "moveSource", "moveDestNew", true, MoveTest.TargetDestination.SOURCE);
    }

    private void performMove(String fileName, String pkgName, String newPkg, boolean performInEditor, MoveTest.TargetDestination target) {
        if (performInEditor) {
            openSourceFile(pkgName, fileName);
            EditorOperator editor = new EditorOperator(fileName);
            editor.setCaretPosition(1, 1);
            new RefactorMoveAction().performPopup(editor);
        } else {
            SourcePackagesNode src = new SourcePackagesNode(testProjectRootNode);
            Node node = new Node(src, pkgName + TREE_SEPARATOR + fileName);
            node.select();
            new RefactorMoveAction().performPopup(node);
        }
        MoveClassDialogOperator mo = new MoveClassDialogOperator();
        JComboBoxOperator location = mo.getLocationCombo();
        switch (target) {
            case SOURCE:
                location.selectItem(0);
                break;
            case TESTS:
                location.selectItem(1);
                break;
        }
        mo.getPackageCombo().clearText();
        mo.getPackageCombo().typeText(newPkg);
        mo.preview();
        dumpRefactoringResults();
    }
}
