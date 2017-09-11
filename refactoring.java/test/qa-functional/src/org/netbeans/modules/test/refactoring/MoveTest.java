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
