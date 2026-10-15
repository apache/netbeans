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
import static org.netbeans.modules.languages.hcl.grammar.HCLParser.*;
import static org.junit.Assert.*;

/**
 *
 * @author lkishalmi
 */
public class HCLParserTest {

    @Test
    public void testHereDoc1() {
        BodyContext ctx = parse("a = <<EOT\ntext\nEOT");
        assertEquals(1, ctx.attribute().size());

        AttributeContext attr = ctx.attribute(0);
        assertEquals("a", attr.IDENTIFIER().getText());
        HeredocTemplateContext heredoc = attr.expression().exprTerm().templateExpr().heredoc().heredocTemplate();
        assertEquals("text\n", heredoc.heredocContent(0).getText());
    }

    @Test
    public void testHereDocInterpolation() {
        BodyContext ctx = parse("a = <<EOT\nfoo-${a}-bar\nEOT");
        assertEquals(1, ctx.attribute().size());

        AttributeContext attr = ctx.attribute(0);
        assertEquals("a", attr.IDENTIFIER().getText());
        HeredocTemplateContext heredoc = attr.expression().exprTerm().templateExpr().heredoc().heredocTemplate();
        assertEquals("foo-",  heredoc.heredocContent(0).getText());
        assertEquals("${a}",  heredoc.interpolation(0).getText());
        assertEquals("-bar\n",  heredoc.heredocContent(1).getText());
    }

    @Test
    public void testHereDocTemplate() {
        BodyContext ctx = parse("a = <<EOT\n%{if a != \"\"}\n${a}\n%{ end }\nEOT");
        assertEquals(1, ctx.attribute().size());

        AttributeContext attr = ctx.attribute(0);
        assertEquals("a", attr.IDENTIFIER().getText());
        HeredocTemplateContext heredoc = attr.expression().exprTerm().templateExpr().heredoc().heredocTemplate();
        assertEquals("if a != ",  heredoc.template(0).templateContent(0).getText());
        assertEquals("\"\"",  heredoc.template(0).quotedTemplate(0).getText());
        assertEquals("${a}",  heredoc.interpolation(0).getText());
        assertEquals(" end ",  heredoc.template(1).templateContent(0).getText());
    }

    private BodyContext parse(String text) {
        HCLLexer lexer = new HCLLexer(CharStreams.fromString(text));
        HCLParser parser = new HCLParser(new CommonTokenStream(lexer));
        return parser.configFile().body();
        
    }
}
