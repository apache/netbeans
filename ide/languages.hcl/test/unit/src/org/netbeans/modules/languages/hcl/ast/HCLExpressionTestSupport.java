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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;

/**
 *
 * @author lkishalmi
 */
public class HCLExpressionTestSupport {
    private HCLExpressionTestSupport() {}
    
    public static HCLExpression parse(String expr) {
        HCLLexer lexer = new HCLLexer(CharStreams.fromString(expr));
        HCLParser parser = new HCLParser(new CommonTokenStream(lexer));
        return new HCLExpressionFactory().process(parser.expression());
    }

    public static void assertExpr(String expected, HCLExpression expr) {
        assertNotNull(expr);
        assertEquals(expected, expr.asString());
    }

    public static void assertExpr(String expected) {
        assertExpr(expected, parse(expected));
    }
}
