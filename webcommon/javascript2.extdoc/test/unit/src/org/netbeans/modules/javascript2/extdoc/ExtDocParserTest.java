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
package org.netbeans.modules.javascript2.extdoc;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocDescriptionElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocParserTest extends JsTestBase {

    public ExtDocParserTest(String name) {
        super(name);
    }

    private static final ExtDocElementType[] expectedTypes = new ExtDocElementType[] {
        // context sensitive type
        ExtDocElementType.DESCRIPTION, ExtDocElementType.UNKNOWN,

        // classic types
        ExtDocElementType.CFG, ExtDocElementType.CLASS, ExtDocElementType.CONSTRUCTOR, ExtDocElementType.EVENT,
        ExtDocElementType.EXTENDS,ExtDocElementType.HIDE,ExtDocElementType.IGNORE,ExtDocElementType.LINK,
        ExtDocElementType.MEMBER,ExtDocElementType.METHOD,ExtDocElementType.NAMESPACE,ExtDocElementType.PARAM,
        ExtDocElementType.PRIVATE,ExtDocElementType.PROPERTY,ExtDocElementType.RETURN,ExtDocElementType.SINGLETON,
        ExtDocElementType.STATIC,ExtDocElementType.TYPE
    };

    public void testParsedTypesForAsterisksComment() throws Exception {
        checkElementTypes("testfiles/extdoc/allTypesAsterisks.js");
    }

    public void testParsedTypesForNoAsteriskComment() throws Exception {
        checkElementTypes("testfiles/extdoc/allTypesNoAsterisks.js");
    }

    public void testParsedContextSensitiveContentNoAsterisk() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/allTypesNoAsterisks.js"));
        List<? extends ExtDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        assertEquals(ExtDocElementType.DESCRIPTION, tags.get(0).getType());
        assertEquals("This should be description.", ((ExtDocDescriptionElement) tags.get(0)).getDescription());
    }

    public void testParsedContextSensitiveContentAsterisks() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/allTypesAsterisks.js"));
        List<? extends ExtDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        assertEquals(ExtDocElementType.DESCRIPTION, tags.get(0).getType());
        assertEquals("This should be description.", ((ExtDocDescriptionElement) tags.get(0)).getDescription());
    }

    public void testNoTagsInBlockComment() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/blockComment.js"));
        Iterator<Entry<Integer, ExtDocComment>> iterator = ExtDocParser.parse(source.createSnapshot()).entrySet().iterator();
        assertTrue(!iterator.hasNext());
    }

    private void checkElementTypes(String filePath) throws Exception {
        Source source = getTestSource(getTestFile(filePath));
        List<? extends ExtDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tags.get(i).getType());
        }
    }

    private ExtDocComment getFirstDocumentationBlock(Snapshot snapshot) {
        Iterator<Entry<Integer, ExtDocComment>> iterator = ExtDocParser.parse(snapshot).entrySet().iterator();
        return iterator.next().getValue();
    }

}
