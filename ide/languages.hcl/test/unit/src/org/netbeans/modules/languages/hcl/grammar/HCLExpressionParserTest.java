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
package org.netbeans.modules.languages.hcl.grammar;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.languages.hcl.grammar.HCLParser.ExpressionContext;

/**
 *
 * @author lkishalmi
 */
public class HCLExpressionParserTest {

    public HCLExpressionParserTest() {
    }

    @Test
    public void testTerminals() {
        assertEquals("42", parse(" 42 ").exprTerm().literalValue().NUMERIC_LIT().getText());
    }

    @Test
    public void testCompoundExpr1() {
        ExpressionContext expr = parse("1 + 2 / 3");
        assertEquals("1", expr.left.getText());
        assertEquals("+", expr.op.getText());
        assertEquals("2/3", expr.right.getText());
    }

    @Test
    public void testCompoundExpr2() {
        ExpressionContext expr = parse("(1 + 2) / 3");
        assertEquals("(1+2)", expr.left.getText());
        assertEquals("/", expr.op.getText());
        assertEquals("3", expr.right.getText());
        
        ExpressionContext left = expr.left.exprTerm().expression();
        assertEquals("1", left.left.getText());
        assertEquals("+", left.op.getText());
        assertEquals("2", left.right.getText());
    }

    @Test
    public void testCompoundExpr3() {
        ExpressionContext expr = parse("1 + -2");
        assertEquals("1", expr.left.getText());
        assertEquals("+", expr.op.getText());
        assertEquals("-2", expr.right.getText());

        ExpressionContext right = expr.right;
        assertNull(right.left);
        assertEquals("-", right.op.getText());
        assertEquals("2", right.right.getText());
    }

    @Test
    public void testConditionalq() {
        ExpressionContext expr = parse("1 == 2 ? 3 : 4");
        assertEquals("1==2", expr.exprCond.getText());
        assertEquals("3", expr.exprTrue.getText());
        assertEquals("4", expr.exprFalse.getText());
    }

    @Test
    public void testConditional2() {
        ExpressionContext expr = parse("(a <= b) && !c ? 0 : 1");
        assertEquals("(a<=b)&&!c", expr.exprCond.getText());
        assertEquals("0", expr.exprTrue.getText());
        assertEquals("1", expr.exprFalse.getText());

        ExpressionContext cond = expr.exprCond;
        assertEquals("a<=b", cond.left.exprTerm().expression().getText());
        assertEquals("!", cond.right.op.getText());
        assertEquals("c", cond.right.right.getText());

    }

    @Test
    public void testConditional3() {
        ExpressionContext expr = parse("a ? b ? 0 : 1 : 2");
        assertEquals("a", expr.exprCond.getText());
        assertEquals("b?0:1", expr.exprTrue.getText());
        assertEquals("2", expr.exprFalse.getText());

        ExpressionContext exprTrue = expr.exprTrue;
        assertEquals("b", exprTrue.exprCond.getText());
        assertEquals("0", exprTrue.exprTrue.getText());
        assertEquals("1", exprTrue.exprFalse.getText());

    }

    private static ExpressionContext parse(String expr){
        HCLLexer lexer = new HCLLexer(CharStreams.fromString(expr));
        HCLParser parser = new HCLParser(new CommonTokenStream(lexer));
        return parser.expression();
    }
}
