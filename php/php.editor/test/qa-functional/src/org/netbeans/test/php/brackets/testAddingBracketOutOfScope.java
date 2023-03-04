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
package org.netbeans.test.php.brackets;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * http://netbeans.org/bugzilla/show_bug.cgi?id=144824
 * 
 * @author  michaelnazarov@netbeans.org
 * @desc    it's about brackets completion if you are out of scope <??> tags in mixed HTML
 */
public class testAddingBracketOutOfScope extends brackets {

    static final String TEST_PHP_NAME = "PhpProject_brackets_Issue144824";

    public testAddingBracketOutOfScope(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testAddingBracketOutOfScope.class).addTest(
                "CreateApplication",
                "Issue144824",
                "Issue144824_caseWithOneBracketAlradyWritten"
                ).enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();

        CreatePHPApplicationInternal(TEST_PHP_NAME);

        endTest();
    }

    public void Issue144824() {
        startTest();

        // Get editor
        EditorOperator eoPHP = new EditorOperator("index.php");
        Sleep(1000);
        // Locate comment
        eoPHP.setCaretPosition("// put your code here", false);
        // Add new line
        TypeCode(eoPHP, "\n");
        Sleep(1000);

        // Empty block
        TypeCode(eoPHP, "{ \n");
        Sleep(1000);
        CheckResult(eoPHP, "}", 1);

        endTest();
    }

    public void Issue144824_caseWithOneBracketAlradyWritten() {
        startTest();
        EditorOperator eoPHP = new EditorOperator("index.php");
        Sleep(1000);

        eoPHP.setCaretPosition("// put your code here", false);
        Sleep(1000);
        TypeCode(eoPHP, "\n");
        Sleep(1000);

        TypeCode(eoPHP, "{ \n");
        Sleep(1000);
        CheckResult(eoPHP, "}", 1);
        
        endTest();
    }
}
