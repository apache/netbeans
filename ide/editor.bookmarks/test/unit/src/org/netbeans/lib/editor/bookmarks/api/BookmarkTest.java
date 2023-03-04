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
package org.netbeans.lib.editor.bookmarks.api;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;

/**
 * Various tests of bookmarks functionality.
 *
 * @author Miloslav Metelka
 */
public class BookmarkTest extends NbTestCase {
    
    public BookmarkTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateBookmark() throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, "nazdar\ncau\n\nhaf\nahoj\n", null);
        BookmarkList bList = BookmarkList.get(doc);
//        Bookmark b = bList.addBookmark("Bookmark1", 2, "");
//        assertEquals("Bookmark1", b.getName());
    }

}
