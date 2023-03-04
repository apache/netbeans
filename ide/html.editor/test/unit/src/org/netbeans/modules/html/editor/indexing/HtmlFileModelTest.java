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
package org.netbeans.modules.html.editor.indexing;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.common.api.FileReference;

/**
 *
 * @author marekfukala
 */
public class HtmlFileModelTest extends TestBase {

    public HtmlFileModelTest(String name) {
        super(name);
    }

    //Bug 211073 - NullPointerException at org.netbeans.modules.html.editor.indexing.HtmlFileModel$ReferencesSearch.visit
    public void testIssue211073() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test1.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        //the erroneous references are not used
        assertEquals(0, model.getReferences().size());
    }

    public void testReferences() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test2.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        List<HtmlLinkEntry> entries = model.getReferences();
        assertEquals(1, entries.size());
        HtmlLinkEntry entry = entries.get(0);
        
        assertEquals("link", entry.getTagName());
        FileReference ref = entry.getFileReference();
        assertNotNull(ref);
        
        assertEquals("test1.html", ref.linkPath());
        
    }
    
    public void testEmbeddedCSSSections() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test3.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        List<OffsetRange> entries = model.getEmbeddedCssSections();
        assertEquals(2, entries.size());
        OffsetRange entry = entries.get(0);
        
        //first section
        assertNotNull(entry);
        assertEquals(221, entry.getStart());
        assertEquals(295, entry.getEnd());

        //second section
        entry = entries.get(1);
        assertNotNull(entry);
        assertEquals(335, entry.getStart());
        assertEquals(411, entry.getEnd());
        
    }
}
