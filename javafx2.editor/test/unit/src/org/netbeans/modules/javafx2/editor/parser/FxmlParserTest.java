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
package org.netbeans.modules.javafx2.editor.parser;

import java.util.Collection;
import java.util.Collections;
import junit.framework.TestSuite;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.GoldenFileTestBase;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author sdedic
 */
public class FxmlParserTest extends GoldenFileTestBase {
    private Collection<ErrorMark>   problems;

    public FxmlParserTest(String testName) {
        super(testName);
    }
    
    public void testParserInvocation() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                if (result instanceof FxmlParserResult) {
                    FxmlParserResult fxResult = (FxmlParserResult)result;
                    appendParsedTree(fxResult, report);
                    appendErrors(fxResult, report);
                    assertContents(report);
                }
            }
        });
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTest(new FxmlParserTest("testUnresolvedThings"));
        s.addTest(new FxmlParserTest("testDefinitionsResolved"));
        s.addTest(new FxmlParserTest("testInlineScript"));
        return s;
    }
    
    public void testDefinitionsResolved() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                appendParsedTree(fxResult, report);
                appendErrors(fxResult, report);
                assertContents(report);
            }
        });
    }
    
    public void testInlineScript() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                appendParsedTree(fxResult, report);
                appendErrors(fxResult, report);
                assertContents(report);
            }
        });
    }

    public void testUnresolvedThings() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                StringBuilder sb = new StringBuilder();
                appendErrors(fxResult, sb);
                assertContents(sb);
            }
        });
    }

    private FxModelBuilder builder;
    
    private void defaultTestContents() throws Exception {
        XmlLexerParser parser = new XmlLexerParser(hierarchy);
        builder = new FxModelBuilder();
        parser.setContentHandler(builder);
        parser.parse();

        StringBuilder sb = report;
        sb.append("\n\n");
        for (ErrorMark em : builder.getErrors()) {
            sb.append(em).append("\n");
        }
        
        assertContents(sb);
    }
}
