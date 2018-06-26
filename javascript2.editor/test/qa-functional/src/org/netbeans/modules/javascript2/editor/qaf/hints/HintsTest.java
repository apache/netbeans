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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
