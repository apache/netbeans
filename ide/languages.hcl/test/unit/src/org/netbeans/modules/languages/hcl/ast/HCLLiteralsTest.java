/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.hcl.ast;

import org.junit.Test;
import static org.junit.Assert.*;

import static org.netbeans.modules.languages.hcl.ast.HCLExpression.parse;

/**
 *
 * @author lkishalmi
 */
public class HCLLiteralsTest {
    @Test
    public void testBool() throws Exception {
        assertEquals(HCLLiteral.TRUE, parse("true"));
        assertEquals(HCLLiteral.FALSE, parse("false"));
    }

    @Test
    public void testNull() throws Exception {
        assertEquals(HCLLiteral.NULL, parse("null"));
    }

    @Test
    public void testNumber() throws Exception {
        HCLExpression exp = parse("3.14");
        assertTrue(exp instanceof HCLLiteral.NumericLit);
        HCLLiteral.NumericLit num = (HCLLiteral.NumericLit) exp;
        assertEquals("3.14", num.value());
    }

    @Test
    public void testString() throws Exception {
        HCLExpression exp = parse("\"Hello\"");
        assertTrue(exp instanceof HCLLiteral.StringLit);
        HCLLiteral.StringLit str = (HCLLiteral.StringLit) exp;
        assertEquals("Hello", str.value());
    }

    @Test
    public void testStringEmpty1() throws Exception {
        HCLExpression exp = parse("\"\"");
        assertTrue(exp instanceof HCLLiteral.StringLit);
        HCLLiteral.StringLit str = (HCLLiteral.StringLit) exp;
        assertEquals("", str.value());
    }

    @Test
    public void testStringEmpty2() throws Exception {
        HCLExpression exp = parse("<<EOT\nEOT");
        assertTrue(exp instanceof HCLTemplate.HereDoc);
        HCLTemplate.HereDoc heredoc = (HCLTemplate.HereDoc) exp;
        assertTrue(heredoc.parts().isEmpty());
    }

}
