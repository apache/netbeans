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
package org.netbeans.modules.javascript2.doc;

import java.util.Set;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationReaderTest extends CslTestBase {

    public JsDocumentationReaderTest(String name) {
        super(name);
    }

    public void testGetCommentTags() throws Exception {
        String commentText = "/**\n"
                + " * Construct a new Shape object.\n"
                + " * @class This is the basic {@link Shape} class.\n"
                + " * It can be considered an abstract class, even though no such thing\n"
                + " * really existing in JavaScript\n"
                + " * @constructor\n"
                + " * @throws MemoryException if there is no more memory\n"
                + " * @throws GeneralShapeException rarely (if ever)\n"
                + " * @return {Shape|Coordinate} A new shape.\n"
                + " */";
        Set<String> commentTags = JsDocumentationReader.getCommentTags(commentText);
        assertEquals(5, commentTags.size());
        assertTrue(commentTags.contains("@class"));
        assertTrue(commentTags.contains("@link"));
        assertTrue(commentTags.contains("@constructor"));
        assertTrue(commentTags.contains("@throws"));
        assertTrue(commentTags.contains("@return"));
    }

    public void testGetAllTags() throws Exception {
        Source source = getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/classWithJsDoc.js"));
        Set<String> allTags = JsDocumentationReader.getAllTags(source.createSnapshot());
        assertEquals(27, allTags.size());
        // randomly check several tags
        assertTrue(allTags.contains("@param"));
        assertTrue(allTags.contains("@example"));
        assertTrue(allTags.contains("@author"));
        assertTrue(allTags.contains("@field"));
        assertTrue(allTags.contains("@version"));
        assertTrue(allTags.contains("@see"));
    }
}
