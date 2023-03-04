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
package org.netbeans.modules.html.editor.lib.api.elements;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;

/**
 *
 * @author marekfukala
 */
public class ElementUtilsTest extends NbTestCase {

    public ElementUtilsTest(String name) {
        super(name);
    }

    public void testEncodeToString() throws ParseException {
        String code = "<table><tr><td>a cell</td><td>another cell</td></tr></table>";
        //             0123456789012345678901234567890123456789012345678901234567890
        //             0         1         2         3         4         5         6

        SyntaxAnalyzer analyzer = SyntaxAnalyzer.create(new HtmlSource(code));
        Node root = analyzer.analyze().parseHtml().root();

        String path = "html/body/table/tbody/tr/td";

        Element td1 = ElementUtils.query(root, path);
        assertNotNull(td1);
        assertEquals(11, td1.from());

        String encoded = ElementUtils.encodeToString(new TreePath(td1));
        assertNotNull(encoded);
        
        assertEquals(path, encoded);
        
    }
}
