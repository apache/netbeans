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
package org.netbeans.modules.javascript2.sdoc;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.sdoc.elements.SDocDescriptionElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElementType;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocParserTest extends JsTestBase {

    public SDocParserTest(String name) {
        super(name);
    }

    private static final SDocElementType[] expectedTypes = new SDocElementType[] {
        // context sensitive type
        SDocElementType.DESCRIPTION, SDocElementType.UNKNOWN,

        // classic types
        SDocElementType.ALIAS, SDocElementType.AUTHOR, SDocElementType.CLASS_DESCRIPTION, SDocElementType.CONSTRUCTOR,
        SDocElementType.DEPRECATED,SDocElementType.EXAMPLE,SDocElementType.EXCEPTION,SDocElementType.ID,
        SDocElementType.INHERITS,SDocElementType.INTERNAL,SDocElementType.MEMBER_OF,SDocElementType.METHOD,
        SDocElementType.NAMESPACE,SDocElementType.PARAM,SDocElementType.PRIVATE,SDocElementType.PROJECT_DESCRIPTION,
        SDocElementType.PROPERTY,SDocElementType.RETURN,SDocElementType.SEE,SDocElementType.SINCE,SDocElementType.TYPE,
        SDocElementType.VERSION
    };

    public void testParsedTypesForAsterisksComment() throws Exception {
        checkElementTypes("testfiles/sdoc/allTypesAsterisks.js");
    }

    public void testParsedTypesForNoAsteriskComment() throws Exception {
        checkElementTypes("testfiles/sdoc/allTypesNoAsterisks.js");
    }

    public void testParsedContextSensitiveContentNoAsterisk() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/sdoc/allTypesNoAsterisks.js"));
        List<? extends SDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        assertEquals(SDocElementType.DESCRIPTION, tags.get(0).getType());
        assertEquals("This should be description.", ((SDocDescriptionElement) tags.get(0)).getDescription());
    }

    public void testParsedContextSensitiveContentAsterisks() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/sdoc/allTypesAsterisks.js"));
        List<? extends SDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        assertEquals(SDocElementType.DESCRIPTION, tags.get(0).getType());
        assertEquals("This should be description.", ((SDocDescriptionElement) tags.get(0)).getDescription());
    }

    public void testNoTagsInBlockComment() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/sdoc/blockComment.js"));
        Iterator<Entry<Integer, SDocComment>> iterator = SDocParser.parse(source.createSnapshot()).entrySet().iterator();
        assertTrue(!iterator.hasNext());
    }

    private void checkElementTypes(String filePath) throws Exception {
        Source source = getTestSource(getTestFile(filePath));
        List<? extends SDocElement> tags = getFirstDocumentationBlock(source.createSnapshot()).getTags();
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tags.get(i).getType());
        }
    }

    private SDocComment getFirstDocumentationBlock(Snapshot snapshot) {
        Iterator<Entry<Integer, SDocComment>> iterator = SDocParser.parse(snapshot).entrySet().iterator();
        return iterator.next().getValue();
    }

}
