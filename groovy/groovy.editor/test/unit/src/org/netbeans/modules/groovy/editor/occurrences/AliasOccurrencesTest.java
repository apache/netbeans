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

package org.netbeans.modules.groovy.editor.occurrences;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class AliasOccurrencesTest extends GroovyTestBase {

    public AliasOccurrencesTest(String testName) {
        super(testName);
    }

    /**
     * Disabled, since with resolved types, AST does not contain original now().time PropertyExpression(MethodCall("now()"),Constant("time")),
     * but is already resolved to PropertyExpression(StaticMethodCall("Calender.getInstance"),Constant("time")).
     * See NETBEANS-5822
    public void testMethodAlias() throws Exception {
        testCaretLine("println n^ow().time");
    }
    */

    // #233956
    public void testMethodStaticImport() throws Exception {
        testCaretLine("println si^n()");
    }

    // #234000
    public void testFieldAlias() throws Exception {
        testCaretLine("println m^in");
    }

    /**
     * Disabled, since with resolved types, AST does not contain original now().time PropertyExpression(MethodCall("now()"),Constant("time")),
     * but is already resolved to PropertyExpression(StaticMethodCall("Calender.getInstance"),Constant("time")).
     * See NETBEANS-5822
    // #233956
    public void testFieldStaticImport() throws Exception {
        testCaretLine("println LIG^HT_GRAY");
    }
    */

    private void testCaretLine(String caretLine) throws Exception {
        checkOccurrences("testfiles/AliasOccurrencesTester.groovy", caretLine, true);
    }
}
