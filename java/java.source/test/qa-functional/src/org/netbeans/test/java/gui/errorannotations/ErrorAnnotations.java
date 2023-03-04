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

package org.netbeans.test.java.gui.errorannotations;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.java.JavaTestCase;
import org.netbeans.test.java.Utilities;



/**
 * Tests Error annotations.
 * @author Roman Strobl
 */
public class ErrorAnnotations extends JavaTestCase {

    // default timeout for actions in miliseconds
    private static final int ACTION_TIMEOUT = 2000;

    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";

    // path to sample files
    private static final String TEST_PACKAGE_PATH = "org.netbeans.test.java.gui.errorannotations";

    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH + ".test";

    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
    private EditorOperator editor = null;
    
    /**
     * Needs to be defined because of JUnit
     * @param name test name
     */
    public ErrorAnnotations(String name) {
        super(name);
    }   

    /**
     * Sets up logging facilities.
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");        
        openDefaultProject();
        String path = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME;
        openFile(path, TEST_CLASS_NAME+".java");
        editor = new EditorOperator(TEST_CLASS_NAME);
    }

    @Override
    protected void tearDown() throws Exception {
        if(editor!=null) editor.closeDiscard();
    }
    
    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple() {        
        editor.insert(" ", 45, 3);

        Utilities.takeANap(ACTION_TIMEOUT);

        log(editor.getText());
        Object[] annots = editor.getAnnotations();
        assertNotNull(annots);
        assertEquals(annots.length,1);
        assertEquals("org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[0]));
        assertEquals("class, interface, or enum expected\n----\n(Alt-Enter shows hints)", EditorOperator.getAnnotationShortDescription(annots[0]));
    }    

    /**
     * Tests undo after simple annotations test.
     */
    public void testUndo() {
        editor.insert(" ", 45, 3);
        Utilities.takeANap(ACTION_TIMEOUT);
        // undo
        new ActionNoBlock("Edit|Undo", null).perform();

        Utilities.takeANap(ACTION_TIMEOUT);

        log(editor.getText());
        Object[] annots = editor.getAnnotations();

        // logging found annotations
        for (Object annot : annots) {
            log("Anotation: "+EditorOperator.getAnnotationShortDescription(annot));
        }
        // there should be 3 annotations - missing javadoc, create subclass and create test case
        assertEquals(1,annots.length);
    }

    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple2() {        
        // change class to klasa        
        editor.replace("class", "klasa");

        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        // check error annotations
        Object[] annots = editor.getAnnotations();        
        assertNotNull("There are not any annotations.", annots);
        assertEquals("There are not one annotation", 2, annots.length);
        assertEquals("Wrong annotation type ", "org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[0]));
        assertEquals("Wrong annotation short description", "class, interface, or enum expected\n----\n(Alt-Enter shows hints)", EditorOperator.getAnnotationShortDescription(annots[0]));
        assertEquals("Wrong annotation type ", "org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[1]));
        assertEquals("Wrong annotation short description", "class, interface, or enum expected\n----\n(Alt-Enter shows hints)", EditorOperator.getAnnotationShortDescription(annots[1]));
    }

    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple3() {               
        editor.replace(TEST_CLASS_NAME, TEST_CLASS_NAME + "xxx", 2);

        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        // check error annotations
        Object[] annots = editor.getAnnotations();
        assertNotNull("There are not any annotations.", annots);
        assertEquals("There are more than  one annotation: " + String.valueOf(annots.length), 1, annots.length);
        assertEquals("Wrong annotation type: " + EditorOperator.getAnnotationType(annots[0]), "org-netbeans-spi-editor-hints-parser_annotation_err_fixable", EditorOperator.getAnnotationType(annots[0]));
        assertEquals("Wrong annotation short description.","invalid method declaration; return type required\n----\n(Alt-Enter shows hints)", EditorOperator.getAnnotationShortDescription(annots[0]));                
    }

    public void testChangeCloseDiscart() {                                
        editor.insert(" ", 45, 3);
        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        Object[] annots = editor.getAnnotations();
        assertNotNull("There are not any annotations.", annots);
        assertEquals("There are annotations: " + String.valueOf(annots.length), 1, annots.length);
        editor.closeDiscard();
        String path = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME;
        openFile(path, TEST_CLASS_NAME+".java");                
        editor = new EditorOperator(TEST_CLASS_NAME);
	new EventTool().waitNoEvent(1000);
        annots = editor.getAnnotations();
        assertEquals(0, annots.length); //there should be no annotations        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ErrorAnnotations.class).enableModules(".*").clusters(".*"));
    }
}
