/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
