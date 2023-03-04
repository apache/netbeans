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
package org.netbeans.modules.css.editor.csl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleter implements KeystrokeHandler {

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        //XXX returning null or the default should cause GSF to use the IDE default matcher
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        ArrayList<OffsetRange> ranges = new ArrayList<>(2);

        Node root = ((CssParserResult) info).getParseTree();
        Snapshot snapshot = info.getSnapshot();

        if (root != null) {
            //find leaf at the position
            Node node = NodeUtil.findNodeAtOffset(root, snapshot.getEmbeddedOffset(caretOffset));
            if (node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    //filter some unwanted node types
                    switch(node.type()) {
                        case declarations:
                        case token:
                            continue;
                    }
                    
                    //use trimmed node range so nodes containing whitespaces at the start
                    //or at the end of their range can be considered as duplicate nodes
                    //of their children.
                    int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                    
                    int from = snapshot.getOriginalOffset(trimmedNodeRange[0]);
                    int to = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                    if (from == -1 || to == -1) {
                        continue;
                    }

                    OffsetRange last = ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
                    //skip duplicated ranges
                    if (last == null || ((last.getEnd() - last.getStart()) < (to - from))) {
                        ranges.add(new OffsetRange(from, to));
                    }
                } while ((node = node.parent()) != null);
            }
        }


        return ranges;
    }

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

   
}
