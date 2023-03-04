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
package org.netbeans.modules.html.editor.gsf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
class HtmlRenameHandler implements InstantRenamer {

    public HtmlRenameHandler() {
    }

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (astCaretOffset == -1) {
            return false;
        }

        HtmlParserResult result = (HtmlParserResult) info;
        Element node = result.findByPhysicalRange(astCaretOffset, true);

        if (node == null) {
            return false;
        }

        switch (node.type()) {
            case OPEN_TAG:
                OpenTag ot = (OpenTag)node;
                //enable only if the caret is in the tag name
                int from = node.from();
                int to = from + 1 + ot.name().length() ; //"<" + "body" length
                return astCaretOffset >= from && astCaretOffset <= to;
            case CLOSE_TAG:
                return true;
        }

        return false;

    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (astCaretOffset == -1) {
            return Collections.emptySet();
        }

        HtmlParserResult result = (HtmlParserResult) info;
        Element node = result.findByPhysicalRange(astCaretOffset, true);

        if (node == null) {
            return Collections.emptySet();
        }

        OpenTag open;
        CloseTag close;
        switch (node.type()) {
            case OPEN_TAG:
                open = (OpenTag) node;
                close = open.matchingCloseTag();
                break;
            case CLOSE_TAG:
                close = (CloseTag) node;
                open = close.matchingOpenTag();
                break;
            default:
                return Collections.emptySet();
        }

        if (open == null || close == null) {
            return Collections.emptySet();
        }

        Snapshot s = info.getSnapshot();
        Set<OffsetRange> set = new HashSet<>();
        
        set.add(new OffsetRange(s.getOriginalOffset(open.from()) + 1,
                s.getOriginalOffset(open.from() + 1 + open.name().length()))); //1 == "<".len

        set.add(new OffsetRange(s.getOriginalOffset(close.from() + 2),
                s.getOriginalOffset(close.from() + 2 + close.name().length()))); //2 == "</".len

        return set;
    }

}
