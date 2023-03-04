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
