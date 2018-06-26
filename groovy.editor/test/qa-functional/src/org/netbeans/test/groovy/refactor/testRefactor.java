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
package org.netbeans.test.groovy.refactor;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.test.groovy.GeneralGroovy;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Vladimir Riha
 */
public class testRefactor extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovyref_";
    static int name_iterator = 0;

    public testRefactor(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testRefactor.class).addTest(
                "testInstantRefactor",
                "testRefactor").enableModules(".*").clusters(".*"));
    }

    public void testInstantRefactor() {
        startTest();
        createSampleProject("NB Project Generators", testRefactor.TEST_BASE_NAME + testRefactor.name_iterator);
        openFile("generators|NetBeansModule.groovy", testRefactor.TEST_BASE_NAME + testRefactor.name_iterator);
        waitScanFinished();
        EditorOperator ep = new EditorOperator("NetBeansModule.groovy");
        ep.setCaretPosition(7, 7);
        evt.waitNoEvent(2000);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_R, 2)).performShortcut(ep);
        type(ep, "packg");
        ep.pressKey(KeyEvent.VK_ENTER);
        assertTrue("file not refactored - contains old value: \n" + ep.getText(), !ep.getText().contains("pkgs"));
        assertTrue("file not refactored - does not contain new value: \n" + ep.getText(), ep.getText().contains("packg"));
        testRefactor.name_iterator++;
        endTest();
    }

    public void testRefactor() {
        startTest();
        createJavaApplication(testRefactor.TEST_BASE_NAME + testRefactor.name_iterator);
        createGroovyFile(testRefactor.TEST_BASE_NAME + testRefactor.name_iterator, "Groovy Class", "Foo");

        EditorOperator ep = new EditorOperator("Foo.groovy");
        ep.setCaretPosition("{", false);
        type(ep, "\n def id\n String test(){\n return this.id");
        ep.save();

        createGroovyFile(testRefactor.TEST_BASE_NAME + testRefactor.name_iterator, "Groovy Class", "Bar");
        ep = new EditorOperator("Bar.groovy");
        ep.setCaretPosition("{", false);
        type(ep, "\n def id\n String test(){\n def d = new Foo() \n d.id \n return this.id");
        ep.save();

        ep.setCaretPosition("d.id", false);
        ep.pressKey(KeyEvent.VK_LEFT);
        evt.waitNoEvent(2000);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_R, 2)).performShortcut(ep);
        JDialogOperator jo = new JDialogOperator("Rename");
        jo.typeKey('e');
        jo.pressKey(KeyEvent.VK_ENTER);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_R, 8)).performShortcut(ep);

        ep.pressKey(KeyEvent.VK_ENTER);
        ep = new EditorOperator("Bar.groovy");
        String t = ep.getText();
        assertTrue("file not refactored - contains old value: \n" + t, t.contains("d.e"));
        assertTrue("file not refactored - contains old value: \n" + t, t.contains("def e"));
        assertTrue("file not refactored - contains old value: \n" + t, t.contains("this.e"));

        ep = new EditorOperator("Foo.groovy");
        t = ep.getText();
        assertTrue("file not refactored - contains old value: \n" + t, t.contains("def e"));
        assertTrue("file not refactored - contains old value: \n" + t, t.contains("this.e"));
        testRefactor.name_iterator++;
        endTest();
    }

    public void testMarkOccurrences() {
        // depends on 226318
        startTest();
        EditorOperator ep = new EditorOperator("NetBeansModule.groovy");
        ep.setCaretPosition(6, 8);
        evt.waitNoEvent(100);
        int[] lines = new int[]{9, 13, 24, 25, 40, 50, 89, 6};
        int iterator = 0;
        while (iterator < lines.length && ep.getLineNumber() == lines[iterator]) {
            iterator++;
        }

        ep.pressKey(KeyEvent.VK_ENTER);
        assertTrue("Not all occurrences has been found: \n" + iterator, iterator == lines.length);
        endTest();
    }
}
