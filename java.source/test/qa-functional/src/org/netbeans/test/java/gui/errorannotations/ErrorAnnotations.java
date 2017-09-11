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
