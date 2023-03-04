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
package org.netbeans.modules.javascript2.json.parser;

import java.io.IOException;
import java.io.StringReader;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Tomas Zezula
 */
public final class ParseTreeToXmlTest extends NbTestCase {
    private static final String TEST_JSON =
        "{\"name\":\"g1\",\n" +
        "\"nodes\": [\n" +
        "{\"name\": \"n1\",\n" +
        "\"os\":\"linux\",\n" +
        "\"arch\":32},\n" +
        "{\"name\": \"n2\",\n" +
        "\"os\":\"solaris\",\n" +
        "\"arch\":64}]\n" +
        "}";
    private static final String TEST_JSON_RESULT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<json start=\"0\" stop=\"109\">\n" +
        "<value start=\"0\" stop=\"109\">\n" +
        "<object start=\"0\" stop=\"109\">\n" +
        "<LBRACE start=\"0\" stop=\"0\" text=\"{\"/>\n" +
        "<pair start=\"1\" stop=\"11\">\n" +
        "<key start=\"1\" stop=\"6\">\n" +
        "<STRING start=\"1\" stop=\"6\" text=\"&quot;name&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"7\" stop=\"7\" text=\":\"/>\n" +
        "<value start=\"8\" stop=\"11\">\n" +
        "<STRING start=\"8\" stop=\"11\" text=\"&quot;g1&quot;\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<COMMA start=\"12\" stop=\"12\" text=\",\"/>\n" +
        "<pair start=\"14\" stop=\"107\">\n" +
        "<key start=\"14\" stop=\"20\">\n" +
        "<STRING start=\"14\" stop=\"20\" text=\"&quot;nodes&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"21\" stop=\"21\" text=\":\"/>\n" +
        "<value start=\"23\" stop=\"107\">\n" +
        "<array start=\"23\" stop=\"107\">\n" +
        "<LBRACKET start=\"23\" stop=\"23\" text=\"[\"/>\n" +
        "<value start=\"25\" stop=\"63\">\n" +
        "<object start=\"25\" stop=\"63\">\n" +
        "<LBRACE start=\"25\" stop=\"25\" text=\"{\"/>\n" +
        "<pair start=\"26\" stop=\"37\">\n" +
        "<key start=\"26\" stop=\"31\">\n" +
        "<STRING start=\"26\" stop=\"31\" text=\"&quot;name&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"32\" stop=\"32\" text=\":\"/>\n" +
        "<value start=\"34\" stop=\"37\">\n" +
        "<STRING start=\"34\" stop=\"37\" text=\"&quot;n1&quot;\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<COMMA start=\"38\" stop=\"38\" text=\",\"/>\n" +
        "<pair start=\"40\" stop=\"51\">\n" +
        "<key start=\"40\" stop=\"43\">\n" +
        "<STRING start=\"40\" stop=\"43\" text=\"&quot;os&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"44\" stop=\"44\" text=\":\"/>\n" +
        "<value start=\"45\" stop=\"51\">\n" +
        "<STRING start=\"45\" stop=\"51\" text=\"&quot;linux&quot;\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<COMMA start=\"52\" stop=\"52\" text=\",\"/>\n" +
        "<pair start=\"54\" stop=\"62\">\n" +
        "<key start=\"54\" stop=\"59\">\n" +
        "<STRING start=\"54\" stop=\"59\" text=\"&quot;arch&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"60\" stop=\"60\" text=\":\"/>\n" +
        "<value start=\"61\" stop=\"62\">\n" +
        "<NUMBER start=\"61\" stop=\"62\" text=\"32\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<RBRACE start=\"63\" stop=\"63\" text=\"}\"/>\n" +
        "</object>\n" +
        "</value>\n" +
        "<COMMA start=\"64\" stop=\"64\" text=\",\"/>\n" +
        "<value start=\"66\" stop=\"106\">\n" +
        "<object start=\"66\" stop=\"106\">\n" +
        "<LBRACE start=\"66\" stop=\"66\" text=\"{\"/>\n" +
        "<pair start=\"67\" stop=\"78\">\n" +
        "<key start=\"67\" stop=\"72\">\n" +
        "<STRING start=\"67\" stop=\"72\" text=\"&quot;name&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"73\" stop=\"73\" text=\":\"/>\n" +
        "<value start=\"75\" stop=\"78\">\n" +
        "<STRING start=\"75\" stop=\"78\" text=\"&quot;n2&quot;\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<COMMA start=\"79\" stop=\"79\" text=\",\"/>\n" +
        "<pair start=\"81\" stop=\"94\">\n" +
        "<key start=\"81\" stop=\"84\">\n" +
        "<STRING start=\"81\" stop=\"84\" text=\"&quot;os&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"85\" stop=\"85\" text=\":\"/>\n" +
        "<value start=\"86\" stop=\"94\">\n" +
        "<STRING start=\"86\" stop=\"94\" text=\"&quot;solaris&quot;\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<COMMA start=\"95\" stop=\"95\" text=\",\"/>\n" +
        "<pair start=\"97\" stop=\"105\">\n" +
        "<key start=\"97\" stop=\"102\">\n" +
        "<STRING start=\"97\" stop=\"102\" text=\"&quot;arch&quot;\"/>\n" +
        "</key>\n" +
        "<COLON start=\"103\" stop=\"103\" text=\":\"/>\n" +
        "<value start=\"104\" stop=\"105\">\n" +
        "<NUMBER start=\"104\" stop=\"105\" text=\"64\"/>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<RBRACE start=\"106\" stop=\"106\" text=\"}\"/>\n" +
        "</object>\n" +
        "</value>\n" +
        "<RBRACKET start=\"107\" stop=\"107\" text=\"]\"/>\n" +
        "</array>\n" +
        "</value>\n" +
        "</pair>\n" +
        "<RBRACE start=\"109\" stop=\"109\" text=\"}\"/>\n" +
        "</object>\n" +
        "</value>\n" +
        "</json>";

    public ParseTreeToXmlTest(@NonNull final String name) {
        super(name);
    }

    public void testParseTreeToXml() throws IOException, SAXException {
        final ANTLRInputStream in = new ANTLRInputStream(TEST_JSON);
        final JsonLexer lex = new JsonLexer(in, true);
        final CommonTokenStream tokens = new CommonTokenStream(lex);
        final JsonParser parser = new JsonParser(tokens);
        final JsonParser.JsonContext ctx = parser.json();
        final ParseTreeToXml visitor = new ParseTreeToXml(lex, parser);
        final Document doc = visitor.visit(ctx);
        final Document exp = XMLUtil.parse(
                new InputSource(new StringReader(TEST_JSON_RESULT)),
                false,
                false,
                null,
                null);
        assertEquals(
            ParseTreeToXml.stringify(exp),
            ParseTreeToXml.stringify(doc));
    }
}
