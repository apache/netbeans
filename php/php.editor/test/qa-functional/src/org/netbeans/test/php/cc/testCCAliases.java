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
package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.EventTool;

/**
 * This test checks code completion for aliases
 * @author Lada Riha, vriha@netbeans.org
 */
public class testCCAliases extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_alias";

    public testCCAliases(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCAliases.class).addTest(
                "VerifyAliases").enableModules(".*").clusters(".*"));
    }

    public void VerifyAliases() {
        startTest();
        // create new application
        CreatePHPApplicationInternal(TEST_PHP_NAME);

        // Create new file
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "test_aliases");

        // Include first file
        EditorOperator eoPHP = new EditorOperator("test_aliases.php");
        eoPHP.setCaretPosition("*/\n", false);
        // type sample source
        TypeCode(eoPHP, "function reallyLongNameForUselessFunction(){\n");
        eoPHP.setCaretPosition("}", false);
        TypeCode(eoPHP,"\n use reallyLongNameForUselessFunction as fooFunction;\n\n fooF");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CheckResult(eoPHP, "fooFunction");
        endTest();
    }
}
