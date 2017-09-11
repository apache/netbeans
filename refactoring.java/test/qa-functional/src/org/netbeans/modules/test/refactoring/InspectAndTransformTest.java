/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.*;
import org.netbeans.modules.test.refactoring.operators.*;

/**
 * @author (stanislav.sazonov@oracle.com)
 */
public class InspectAndTransformTest extends ModifyingRefactoring {

    public InspectAndTransformTest(String name) {
        super(name);
    }

    public static Test suite() {

        return JellyTestCase.emptyConfiguration().
            addTest(InspectAndTransformTest.class, "testSimple_A_A").
            addTest(InspectAndTransformTest.class, "testSimple_A_B").
            addTest(InspectAndTransformTest.class, "testSimple_A_C").
            addTest(InspectAndTransformTest.class, "testSimple_A_D").
            addTest(InspectAndTransformTest.class, "testSimple_A_E").
            addTest(InspectAndTransformTest.class, "testSimple_A_F").
            addTest(InspectAndTransformTest.class, "testSimple_A_G").
            suite();
    }

    public void testSimple_A_A() {

        InspectAndTransformOperator inspectAndTransform = null;

        openSourceFile("inspectAndTransform", "Class_A_A");
        EditorOperator editor = new EditorOperator("Class_A_A.java");

        new EventTool().waitNoEvent(1000);

        new InspectAndTransformAction().performPopup(editor);
        inspectAndTransform = new InspectAndTransformOperator();

        inspectAndTransform.selectSingleInspection();
        inspectAndTransform.setInspect("Current File (Class_A_A.java)");        
        inspectAndTransform.setSingleInspection("Add @Override Annotation");
        
        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.pressInspect();
        
        new EventTool().waitNoEvent(1000);
        
        RefactoringOperator rafectoring = new RefactoringOperator();        
        rafectoring.pressDoRefactoring();
        
        new EventTool().waitNoEvent(1000);
        
        // evalue result and discard changes
        ref(editor.getText());
        editor.closeDiscard();
    }

    public void testSimple_A_B() {
        openSourceFile("inspectAndTransform", "Class_A_B");
        EditorOperator editor = new EditorOperator("Class_A_B.java");

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);
//        
        inspectAndTransform.selectSingleInspection();
        inspectAndTransform.setInspect("Current File (Class_A_B.java)");
        inspectAndTransform.pressBrowse();

        new EventTool().waitNoEvent(1000);

        ManageInspectionsOperatot mio = new ManageInspectionsOperatot();
        mio.pushText("over");

        new EventTool().waitNoEvent(1000);

        mio.selectInspections("Add @Override Annotation");

        new EventTool().waitNoEvent(500);

        mio.pressOK();

        new EventTool().waitNoEvent(1000);

        inspectAndTransform.pressInspect();

        new EventTool().waitNoEvent(1000);

        RefactoringOperator rafectoring = new RefactoringOperator();
        rafectoring.pressDoRefactoring();

        new EventTool().waitNoEvent(1000);

        ref(editor.getText());
        editor.closeDiscard();
    }

    public void testSimple_A_C() {
        openSourceFile("inspectAndTransform", "Class_A_C");
        EditorOperator editor = new EditorOperator("Class_A_C.java");

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);

        inspectAndTransform.selectSingleInspection();
        inspectAndTransform.setInspect("Current File (Class_A_C.java)");
        inspectAndTransform.pressBrowse();

        new EventTool().waitNoEvent(1000);

        ManageInspectionsOperatot mio = new ManageInspectionsOperatot();
        mio.pushText("diam");

        new EventTool().waitNoEvent(1000);

        mio.selectInspections("Can Use Diamond");
        mio.pressOK();

        new EventTool().waitNoEvent(500);

        inspectAndTransform.pressInspect();

        new EventTool().waitNoEvent(1000);

        RefactoringOperator rafectoring = new RefactoringOperator();
        rafectoring.pressDoRefactoring();

        new EventTool().waitNoEvent(1000);

        ref(editor.getText());
        editor.closeDiscard();
    }
    
    public void testSimple_A_D() {
        openSourceFile("inspectAndTransform", "Class_A_D");
        EditorOperator editor = new EditorOperator("Class_A_D.java");

        new EventTool().waitNoEvent(1000);

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.selectSingleInspection();
        inspectAndTransform.setInspect("Current File (Class_A_D.java)");
        inspectAndTransform.pressBrowse();

        new EventTool().waitNoEvent(1000);

        ManageInspectionsOperatot mio = new ManageInspectionsOperatot();
        mio.pushText("over");

        new EventTool().waitNoEvent(1000);

        try {
            mio.selectInspections("Can Use Diamond");
            fail("\"Can Use Diamond\" in ManageInspectionsOperatot should be disabled!");
        } catch (Exception e) {
        }
        
        new EventTool().waitNoEvent(1000);
        
        mio.pressCancel();
        inspectAndTransform.pressCancel();

        new EventTool().waitNoEvent(1000);

        ref(editor.getText());
        editor.closeDiscard();
    }

    public void testSimple_A_E() {
        openSourceFile("inspectAndTransform", "Class_A_E");
        EditorOperator editor = new EditorOperator("Class_A_E.java");

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);

        inspectAndTransform.selectConfiguration();
        inspectAndTransform.setInspect("Current File (Class_A_E.java)");
        inspectAndTransform.pressBrowse();
        inspectAndTransform.setConfiguration("Migrate to JDK 7");

        inspectAndTransform.pressInspect();

        new EventTool().waitNoEvent(1000);

        RefactoringOperator rafectoring = new RefactoringOperator();
        rafectoring.pressDoRefactoring();

        new EventTool().waitNoEvent(1000);

        ref(editor.getText());
        editor.closeDiscard();
    }

    public void testSimple_A_F() {
        openSourceFile("inspectAndTransform", "Class_A_F");
        EditorOperator editor = new EditorOperator("Class_A_F.java");

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);

        inspectAndTransform.selectConfiguration();
        inspectAndTransform.setInspect("Current Project (RefactoringTest)");
        inspectAndTransform.pressManage();

        new EventTool().waitNoEvent(1000);
        
        ManageInspectionsOperatot mio = new ManageInspectionsOperatot();
        
        new EventTool().waitNoEvent(1000);
        
        mio.createNewConfiguration("MyConfiguration_1");
        
        new EventTool().waitNoEvent(1000);
        
        mio.checkInspections(new String[]{"If-Else Statements Should Use Braces", "String.equals(\"\")", "toString() used on array instance"});
        
        new EventTool().waitNoEvent(1000);
        
        mio.pressOK();
        
        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.setConfiguration("MyConfiguration_1");

        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.pressInspect();
        
        new EventTool().waitNoEvent(1000);

        RefactoringOperator rafectoring = new RefactoringOperator();
        rafectoring.pressDoRefactoring();

        new EventTool().waitNoEvent(1000);

        String out = editor.getText();
        
        openSourceFile("inspectAndTransform_1", "Class_A");
        editor = new EditorOperator("Class_A.java");
        
        new EventTool().waitNoEvent(1000);
        
        out += editor.getText();
        
        openSourceFile("inspectAndTransform_2", "Class_A");
        editor = new EditorOperator("Class_A.java");

        new EventTool().waitNoEvent(1000);
        
        out += editor.getText();
        
        ref(out);
        editor.closeDiscard();
    }
    
    public void testSimple_A_G() {
        openSourceFile("inspectAndTransform", "Class_A_G");
        EditorOperator editor = new EditorOperator("Class_A_G.java");

        new InspectAndTransformAction().performPopup(editor);
        InspectAndTransformOperator inspectAndTransform = new InspectAndTransformOperator();

        new EventTool().waitNoEvent(1000);

        inspectAndTransform.selectConfiguration();
        inspectAndTransform.setInspect("Current Package (inspectAndTransform)");
        inspectAndTransform.pressManage();

        new EventTool().waitNoEvent(1000);
        
        ManageInspectionsOperatot mio = new ManageInspectionsOperatot();
        
        new EventTool().waitNoEvent(1000);
        
        mio.createNewConfiguration("MyConfiguration_2");
        
        new EventTool().waitNoEvent(1000);
        
        mio.checkInspections(new String[]{"Comparing Strings using == or !=", "Redundant if statement"});
        
        new EventTool().waitNoEvent(1000);
        
        mio.pressOK();
        
        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.setConfiguration("MyConfiguration_2");

        new EventTool().waitNoEvent(1000);
        
        inspectAndTransform.pressInspect();
        
        new EventTool().waitNoEvent(1000);

        RefactoringOperator rafectoring = new RefactoringOperator();
        rafectoring.pressDoRefactoring();

        new EventTool().waitNoEvent(1000);

        String out = editor.getText();
        
        openSourceFile("inspectAndTransform_1", "Class_B");
        editor = new EditorOperator("Class_B.java");
        
        new EventTool().waitNoEvent(1000);
        
        out += editor.getText();
        
        openSourceFile("inspectAndTransform_2", "Class_B");
        editor = new EditorOperator("Class_B.java");

        new EventTool().waitNoEvent(1000);
        
        out += editor.getText();
        
        ref(out);
        editor.closeDiscard();
    }
}
