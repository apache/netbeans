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
