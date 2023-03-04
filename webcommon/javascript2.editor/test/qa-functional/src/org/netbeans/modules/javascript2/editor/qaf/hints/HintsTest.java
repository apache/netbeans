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
package org.netbeans.modules.javascript2.editor.qaf.hints;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class HintsTest extends GeneralJavaScript {

    public HintsTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(HintsTest.class).addTest(
                "createApplication",
                "testComparison",
                "testMissingSemicolon",
                "testExtraComma",
                "testWeirdAssignement",
                "testDuplicateProperty").enableModules(".*").clusters(".*"));
    }

    public void createApplication() {
        startTest();
        HintsTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + "ht_" + NAME_ITERATOR);
        EditorOperator eo = createWebFile("ht", TEST_BASE_NAME + "ht_" + NAME_ITERATOR, "JavaScript File");
        HintsTest.currentFile = "ht.js";
        try {
            waitScanFinished();
        } catch (Exception e) {
            evt.waitNoEvent(3000); // fallback
        }
        cleanFile(eo);
        endTest();
    }

    public void testComparison() {
        startTest();

        EditorOperator eo = new EditorOperator(HintsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);

        type(eo, "var a,b; if (a == b) {}");
        Object[] anns = getAnnotations(eo, 0);
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Expected \"===\" and instead saw \"==\".\n"
                + "----\n"
                + "(Alt-Enter shows hints)";

        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), ideal.equals(desc.trim()));
        }
        endTest();
    }

    public void testMissingSemicolon() {
        startTest();

        EditorOperator eo = new EditorOperator(HintsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);

        type(eo, " var xx =1");
        Object[] anns = getAnnotations(eo, 0);
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Expected semicolon ; after \"1\".\n"
                + "----\n"
                + "(Alt-Enter shows hints)";

        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), ideal.equals(desc.trim()));
        }

        endTest();
    }

    public void testExtraComma() {
        startTest();

        EditorOperator eo = new EditorOperator(HintsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);

        type(eo, "var xd = {jedna:1,};");
        Object[] anns = getAnnotations(eo, 0);
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Unexpected \",\".\n"
                + "----\n"
                + "(Alt-Enter shows hints)";

        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), ideal.equals(desc.trim()));
        }

        endTest();
    }

    public void testWeirdAssignement() {
        startTest();

        EditorOperator eo = new EditorOperator(HintsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);

        type(eo, "var a,b; while (a = b) {}");
        Object[] anns = getAnnotations(eo, 0);
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Expected a conditional expression and instead saw an assignment.\n"
                + "----\n"
                + "(Alt-Enter shows hints)";

        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), ideal.equals(desc.trim()));
        }

        endTest();
    }

    public void testDuplicateProperty() {
        startTest();

        EditorOperator eo = new EditorOperator(HintsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);

        type(eo, "var a= {jedna:1, jedna:2};");
        Object[] anns = getAnnotations(eo, 0);
        assertEquals("More annotations than expected", 1, anns.length);
        String ideal = "Duplicate name of property \"jedna\".\n"
                + "----\n"
                + "(Alt-Enter shows hints)";

        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), ideal.equals(desc.trim()));
        }


        endTest();
    }
}
