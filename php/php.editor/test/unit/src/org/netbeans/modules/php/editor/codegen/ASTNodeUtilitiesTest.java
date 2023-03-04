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

package org.netbeans.modules.php.editor.codegen;

import java.util.Set;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;

/**
 *
 * @author Andrei Badea
 */
public class ASTNodeUtilitiesTest extends PHPNavTestBase {

    public ASTNodeUtilitiesTest(String testName) {
        super(testName);
    }

    public void testGetVariablesInScope() throws Exception {
        String code =
                "<?php" +
                "   $conn = 1;" +
                "   $global = 2;" +
                "   class Bingo {" +
                "       private $field = 3;" +
                "   }" +
                "   " +
                "   function func() {" +
                "       global $conn;" +
                "       $var = $xyz;" +
                "       |" +
                "   }" +
                "   $foo = 2;" +
                "?>";
        final int offset = code.indexOf('|');
        code = code.replace('|', ' ');
        performTest(new String[] { code }, new UserTask() {

            public void cancel() {}

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ParserResult info = (ParserResult) resultIterator.getParserResult();
                Set<String> varNames = ASTNodeUtilities.getVariablesInScope(info, offset, new ASTNodeUtilities.VariableAcceptor() {

                    public boolean acceptVariable(String variableName) {
                        return true;
                    }
                });
                assertTrue(varNames.contains("conn"));
                assertTrue(varNames.contains("var"));
                // Not sure about xyz. Since there is an assignement, the user probably knows
                // what he is doing, and knows that the variable will be in scope?
                assertTrue(varNames.contains("xyz"));
                assertFalse(varNames.contains("foo")); // Since not declared global in the function.
                assertFalse(varNames.contains("global")); // Since not declared global in the function.
                assertFalse(varNames.contains("field")); // Since in an entirely different scope.
            }
        }, false);
    }
}
