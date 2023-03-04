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

package org.netbeans.modules.ant.grammar;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

public class AntHighlightsContainerTest extends NbTestCase {

    public AntHighlightsContainerTest(String name) {
        super(name);
    }

    public void testHighlighting() throws Exception {
        assertEquals("<echo>whatever</echo>", highlight("<echo>whatever</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>${prop}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<ech|o>${prop}</e|cho>"));
        assertEquals("<echo>(${pr)(op})</echo>", highlight("<echo>${pr|op}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>|${prop}</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>${prop}|</echo>"));
        assertEquals("<echo>(${prop})</echo>", highlight("<echo>|${prop}|</echo>"));
        assertEquals("<echo>$${foo}=(${foo})</echo>", highlight("<echo>$${foo}=${foo}</echo>"));
        assertEquals("<echo>(@{prop})</echo>", highlight("<echo>@{prop}</echo>"));
        assertEquals("<echo>${(@{prop})}</echo>", highlight("<echo>${@{prop}}</echo>"));
    }
    
    /**
     * @param text document which may be split at {@code |} to indicate where segments are split
     * @return same document with highlighted parts surrounded by {@code (...)} (not merged at split points)
     */
    private static String highlight(String text) throws Exception {
        List<Integer> splits = new ArrayList<Integer>();
        AbstractDocument doc = new DefaultStyledDocument();
        int lastSplit = 0;
        for (String piece : text.split("[|]")) {
            doc.insertString(doc.getLength(), piece, null);
            lastSplit += piece.length();
            splits.add(lastSplit);
        }
        HighlightsContainer hc = new AntHighlightsContainer(doc, null);
        int pos = 0;
        StringBuilder b = new StringBuilder();
        for (int split : splits) {
            // XXX can also test passing Integer.MAX_VALUE as endOffset when split == doc.length
            HighlightsSequence hs = hc.getHighlights(pos, split);
            while (hs.moveNext()) {
//                System.err.println("pos=" + pos + " split=" + split + " startOffset=" + hs.getStartOffset() + " endOffset=" + hs.getEndOffset());
                b.append(doc.getText(pos, hs.getStartOffset() - pos));
                pos = hs.getEndOffset();
                b.append('(');
                b.append(doc.getText(hs.getStartOffset(), pos - hs.getStartOffset()));
                b.append(')');
            }
            b.append(doc.getText(pos, split - pos));
            pos = split;
        }
        return b.toString();
    }

}
