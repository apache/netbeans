/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
