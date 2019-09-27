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
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.javascript2.editor.JsonTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.parser.SanitizingParser.Context;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Hejl
 */
public class JsonParserTest extends JsonTestBase {

    public JsonParserTest(String testName) {
        super(testName);
    }

    public void testComments1() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"  //line comment\n"
            + "/*comment*/\n"
            + "}\n",
            false,
            Arrays.asList(
                    "token recognition error at: '//line comment",
                    "token recognition error at: '/*comment"
                    ));
    }

    public void testComments2() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"  //line comment\n"
            + "}\n",
            false,
            Collections.singletonList("token recognition error at: '//line comment"));
    }

    public void testComments3() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"\n"
            + "/*comment*/\n"
            + "}\n",
            false,
            Collections.singletonList("token recognition error at: '/*comment"));
    }

    public void testComments4() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"  //line comment\n"
            + "/*comment*/\n"
            + "}\n",
            true,
            Collections.emptyList());
    }

    public void testStringLiteral1() throws Exception {
        parse("{ \"a\" : \"test\\tw\" }", false, Collections.<String>emptyList());
    }

    public void testStringLiteral2() throws Exception {
        parse("{ \"a\" : \"test\\xw\" }",
                false,
                //Todo: Consider to join antlr errors in String token
                Arrays.asList(
                        "token recognition error at: '\"test\\x'",
                        "no viable alternative at input '}'"));
    }

    public void testStringLiteral3() throws Exception {
        parse("{ \"a\" : \"test\\\nw\" }",
                false,
                Arrays.asList(
                        "token recognition error at: '\"test\\\\n'",
                        "token recognition error at: 'w'",
                        "token recognition error at: '\" }'",
                        "no viable alternative at input '<EOF>'"));
    }

    public void testStringLiteral4() throws Exception {
        parse("{ \"a\" : \"test\\u000fw\" }", false, Collections.<String>emptyList());
    }

    public void testStringLiteral5() throws Exception {
        parse("{ \"a\" : \"test\\u000gw\" }",
                false,
                //Todo: Consider to join antlr errors in String token
                Arrays.asList(
                        "token recognition error at: '\"test\\u000g'",
                        "no viable alternative at input '}'"
                ));
    }

    public void testStringLiteral6() throws Exception {
        parse("{ \"a\" : \"t'est\" }", false, Collections.<String>emptyList());
    }

    public void testStringLiteral7() throws Exception {
        parse("{ \"a\" : \"t\\'est\" }",
                false,
                //Todo: Consider to join antlr errors in String token
                Arrays.asList(
                    "token recognition error at: '\"t\\''",
                    "no viable alternative at input '}'"
                ));
    }

    public void testTrailingComma1() throws Exception {
        parse("{ \"a\" : \"test\" }", false, Collections.<String>emptyList());
    }

    public void testTrailingComma2() throws Exception {
        parse("{ \"a\" : [1, 2] }", false, Collections.<String>emptyList());
    }

    public void testTrailingComma3() throws Exception {
        parse("{ \"a\" : \"test\", }", false, Collections.singletonList("mismatched input '}' expecting STRING"));
    }

    public void testTrailingComma4() throws Exception {
        parse("{ \"a\" : [1, 2,] }", false, Collections.singletonList("no viable alternative at input ']'"));
    }

    public void testTrailingComma5() throws Exception {
        parse("{ \"a\" : [{\"w\":1}, {\"e\":2},] }", false, Collections.singletonList("no viable alternative at input ']'"));
    }

    public void testEmpty() throws Exception {
        try {
            // NETBEANS-2881
            String original = "";
            JsonParser parser = new JsonParser(false);
            Document doc = getDocument(original);
            Snapshot snapshot = Source.create(doc).createSnapshot();
            JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.jsonLanguage());
            parser.parseSource(snapshot, null, JsParser.Sanitize.NEVER, manager);
        } catch (StringIndexOutOfBoundsException e) {
            fail("StringIndexOutOfBoundsException occurred: " + e.getMessage());
        }
    }

    private void parse(
            String original,
            boolean allowComments,
            List<String> errors) throws Exception {
        JsonParser parser = new JsonParser(allowComments);
        Document doc = getDocument(original);
        Snapshot snapshot = Source.create(doc).createSnapshot();
        Context context = new JsParser.Context("test.json", snapshot, -1, JsTokenId.jsonLanguage());
        JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.jsonLanguage());
        parser.parseContext(context, JsParser.Sanitize.NEVER, manager);

        assertEquals(errors.size(), manager.getErrors().size());
        for (int i = 0; i < errors.size(); i++) {
            if (!manager.getErrors().get(i).getDisplayName().startsWith(errors.get(i))) {
                fail("Error was expected to start with: " + errors.get(i) + " but was: "
                        + manager.getErrors().get(i).getDisplayName());
            }
        }
    }
}
