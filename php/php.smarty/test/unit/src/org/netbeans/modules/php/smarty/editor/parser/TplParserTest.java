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
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplParserTest extends TplTestBase {

    TplParserResult parserResult;

    public TplParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(PHPTokenId.language());
            TestLanguageProvider.register(JsTokenId.javascriptLanguage());
            TestLanguageProvider.register(TplTopTokenId.language());
            TestLanguageProvider.register(TplTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    private void parseSource(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof TplParserResult);
                parserResult = (TplParserResult) result;
            }
        });
    }

    public void testParser1() throws Exception {
        assertParserBlocks();
    }

    public void testParser2() throws Exception {
        assertParserBlocks();
    }

    public void testParser3() throws Exception {
        assertParserBlocks();
    }

    public void testParser4() throws Exception {
        assertParserBlocks();
    }

    public void testParser5() throws Exception {
        assertParserBlocks();
    }

    public void testParser6() throws Exception {
        assertParserBlocks();
    }

    public void testParser7() throws Exception {
        assertParserBlocks();
    }

    public void testParser8() throws Exception {
        assertParserBlocks();
    }

    public void testParser9() throws Exception {
        assertParserBlocks();
    }

    public void testParser10() throws Exception {
        assertParserBlocks();
    }

    private String getTestFileRelPath() {
        return "testfiles/parserBlocks/" + getName() + ".tpl";
    }

    private FileObject getTestFile() {
        return super.getTestFile(getTestFileRelPath());
    }

    private String serializeBlocks() {
        StringBuilder blocks = new StringBuilder("Detected parser blocks in the file:\n");
        for (TplParserResult.Block block : parserResult.getBlocks()) {
            blocks.append(serializeBlock(block));
        }
        return blocks.toString();
    }

    private String serializeBlock(TplParserResult.Block block) {
        StringBuilder blockSB = new StringBuilder();
        List<TplParserResult.Section> sections = block.getSections();
        assertTrue(!sections.isEmpty());
        blockSB.append("\n{").append(sectionToString(sections.get(0))).append("}\n");
        for (int i = 1; i <= sections.size() - 1; i++) {
            blockSB.append(" |\n");
            blockSB.append(" -- {");
            blockSB.append(sectionToString(sections.get(i))).append("}\n");
        }
        return blockSB.toString();
    }

    private String sectionToString(TplParserResult.Section section) {
        StringBuilder sb = new StringBuilder();
        sb.append(section.getName());
        sb.append(" <").append(section.getOffset().getStart()).append(":");
        sb.append(section.getOffset().getEnd()).append("> - ");
        sb.append(section.getText());
        return sb.toString();
    }

    private void assertParserBlocks() throws Exception {
        parseSource(getTestSource(getTestFile()));
        assertDescriptionMatches(getTestFileRelPath(), serializeBlocks(), false, ".blocks");
    }

}
