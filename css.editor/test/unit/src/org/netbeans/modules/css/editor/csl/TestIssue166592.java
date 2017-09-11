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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor.csl;

import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
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
        assertParserResultType(content, 0, HtmlParserResult.class);
        assertParserResultType(content, 3, HtmlParserResult.class);
        assertParserResultType(content, 48, HtmlParserResult.class);

        //works - css
        assertParserResultType(content, 12, CssParserResult.class);
        assertParserResultType(content, 38, CssParserResult.class);

        //fails between the two styles - returns css instead of html parser result

        //UNCOMMENT THIS LINE
//        assertParserResultType(content, 25, HtmlParserResult.class);

    }

    private void assertParserResultType(String content, final int offset, Class resultType) throws ParseException {
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
        assertEquals(resultType, result.getClass());
    }

}
