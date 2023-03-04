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
package org.netbeans.modules.lsp.client.bindings;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.eclipse.lsp4j.FoldingRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldInfo;

/**
 * Unit tests for FoldManagerImpl.
 *
 * @author MKuettner
 */
public class FoldManagerImplTest {

    private static final String EXAMPLE_SIMPLE = "line 1\n"
            + "line 2\n"
            + "line 3\n"
            + "line 4\n"
            + "line 5\n";

    private static final String EXAMPLE_PY_NEWLINE = "from pattern_imports import *\n"
            + "\n"
            + "def call(interface):\n"
            + "    \"\"\"\n"
            + "    \"\"\"\n"
            + "    print(\"Dummy call was called!\")\n"
            + "";

    private static final String EXAMPLE_PY_NO_NEWLINE = "from pattern_imports import *\n"
            + "\n"
            + "def call(interface):\n"
            + "    \"\"\"\n"
            + "    \"\"\"\n"
            + "    print(\"Dummy call was called!\")";

    @Test
    public void computeFoldInfosWithoutStartEndCharacterTest() throws Exception {
        Document doc = createDocument(EXAMPLE_SIMPLE);

        List<FoldingRange> ranges = new ArrayList<>();
        ranges.add(new FoldingRange(0, 2));

        List<FoldInfo> infos = FoldManagerImpl.computeInfos(doc, ranges);
        assertEquals(1, infos.size());
        assertEquals(6, infos.get(0).getStart());
        assertEquals(20, infos.get(0).getEnd());
    }

    @Test
    public void computeFoldInfosWithStartEndCharacterTest() throws Exception {
        Document doc = createDocument(EXAMPLE_SIMPLE);

        FoldingRange range = new FoldingRange(0, 2);
        range.setStartCharacter(0);
        range.setEndCharacter(6);

        List<FoldingRange> ranges = new ArrayList<>();
        ranges.add(range);

        List<FoldInfo> infos = FoldManagerImpl.computeInfos(doc, ranges);
        assertEquals(1, infos.size());
        assertEquals(0, infos.get(0).getStart());
        assertEquals(20, infos.get(0).getEnd());
    }

    @Test
    public void computeFoldInfosDuplicateFoldTest() throws Exception {
        Document doc = createDocument(EXAMPLE_SIMPLE);

        List<FoldingRange> ranges = new ArrayList<>();
        // duplicate ranges
        ranges.add(new FoldingRange(0, 2));
        ranges.add(new FoldingRange(0, 2));
        ranges.add(new FoldingRange(0, 2));

        List<FoldInfo> infos = FoldManagerImpl.computeInfos(doc, ranges);
        assertEquals(1, infos.size());
    }

    @Test
    public void computeFoldInfosDocEndsWithNewlineTest() throws Exception {
        Document doc = createDocument(EXAMPLE_PY_NEWLINE);
        List<FoldInfo> infos = FoldManagerImpl.computeInfos(doc, computeRanges());
        assertFoldInfos(infos);
    }

    /**
     * Test to reproduce https://issues.apache.org/jira/browse/NETBEANS-6328
     */
    @Test
    public void computeFoldInfosFoldUntilEndOfDocTest() throws Exception {
        Document doc = createDocument(EXAMPLE_PY_NO_NEWLINE);
        List<FoldInfo> infos = FoldManagerImpl.computeInfos(doc, computeRanges());
        assertFoldInfos(infos);
    }

    private void assertFoldInfos(List<FoldInfo> infos) {
        assertNotNull(infos);
        assertEquals(2, infos.size());
        // first fold
        assertEquals(51, infos.get(0).getStart());
        assertEquals(103, infos.get(0).getEnd());
        // second fold
        assertEquals(59, infos.get(1).getStart());
        assertEquals(67, infos.get(1).getEnd());
    }

    /**
     * Returns a fixed list of FoldingRanges comming e.g. from language server.
     */
    private List<FoldingRange> computeRanges() {
        List<FoldingRange> ranges = new ArrayList<>();
        // original ranges from language server
        ranges.add(new FoldingRange(2, 5));
        ranges.add(new FoldingRange(3, 4));
        return ranges;
    }

    /**
     * Creates a test Document from gievn content.
     */
    private Document createDocument(String content) throws Exception {
        BaseDocument doc = new BaseDocument(true, "text/python");
        doc.insertString(0, content, null);
        return doc;
    }
}
