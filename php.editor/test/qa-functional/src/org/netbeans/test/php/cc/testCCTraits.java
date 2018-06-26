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
package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class testCCTraits extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_0003";

    public testCCTraits(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCTraits.class).addTest(
                "CreateApplication",
                "testPhp54TraitsSameFile",
                "testPhp54TraitsDifferentFile").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CreatePHPFile() {
        startTest();
        SetAspTags(TEST_PHP_NAME, true);
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        endTest();
    }

    public void testPhp54TraitsSameFile() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "trait Test{ \n public function test(){} \n } \n class Foo{ \n use Test; \n function bar(){\n $this->");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"test", "bar"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }

    public void testPhp54TraitsDifferentFile() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "TraitTest");
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace testA; \n trait Test{ \n public function test(){}");
        file.save();
        file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "TraitTest2");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n class Bar{ \n use testA\\Test; \n public function testfoo(){\n $this->");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"test", "testfoo"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }
}
