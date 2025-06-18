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

package org.netbeans.modules.css.editor.csl;

import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;

/**
 *
 * @author mfukala@netbeans.org
 */
public class TestIssue166592 extends TestBase {

    public TestIssue166592() {
        super(TestIssue166592.class.getName());
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    public void testIncorrectOffsetsConversion() throws ParseException {
        String content = " <style>test { } </style>  <style> h4{ }      </style>";
        //         01234567890123456789012345678901234567890123456789
        //         0         1         2         3         4

        //works - html
        assertParserResultType(content, 0, "org.netbeans.modules.html.editor.api.gsf.HtmlParserResult");
        assertParserResultType(content, 3, "org.netbeans.modules.html.editor.api.gsf.HtmlParserResult");
        assertParserResultType(content, 48, "org.netbeans.modules.html.editor.api.gsf.HtmlParserResult");

        //works - css
        assertParserResultType(content, 12, CssParserResult.class.getName());
        assertParserResultType(content, 38, CssParserResult.class.getName());

        //fails between the two styles - returns css instead of html parser result

        //UNCOMMENT THIS LINE
//        assertParserResultType(content, 25, HtmlParserResult.class);

    }

    private void assertParserResultType(String content, final int offset, String resultType) throws ParseException {
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult(offset);
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertEquals(resultType, result.getClass().getName());
    }

}
