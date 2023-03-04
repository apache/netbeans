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

import java.util.Collection;
import java.util.TreeSet;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class HtmlKeystrokeHandler implements KeystrokeHandler {

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

    //not used. HTMLBracesMatching coveres this functionality
    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        //since the HtmlKeystrokeHandler is also registered for XhtmlElLanguage the ELParserResult
        //may get here under some circumstances. Not a clean solution though...
        if(!(info instanceof HtmlParserResult)) {
            return Collections.emptyList();
        }
        HtmlParserResult result = (HtmlParserResult)info;

        //OffsetRange implements Comparable
        Collection<OffsetRange> ranges = new TreeSet<>();

        //include the text under the carat to the ranges.
        //I need to do it this lexical way since we do not
        //add the text nodes into the ast due to performance reasons
        Document doc = info.getSnapshot().getSource().getDocument(true);
        TokenHierarchy<Document> hierarchy = TokenHierarchy.get(doc);
        TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
        if(ts == null) {
            return Collections.emptyList();
        }
        ts.move(caretOffset);
        if(ts.moveNext() || ts.movePrevious()) {
            Token<HTMLTokenId> token = ts.token();

            if(token.id() == HTMLTokenId.TEXT) {
                CharSequence text = token.text();
                if(text.toString().trim().length() > 0) { //filter only whitespace tokens
                    int from = ts.offset();
                    int to = from + token.text().length();

                    //properly compute end offset of joined tokens
                    List<? extends Token<HTMLTokenId>> tokenParts = token.joinedParts();
                    if(tokenParts != null) {
                        //get last part token
                        Token<HTMLTokenId> last = tokenParts.get(tokenParts.size() - 1);
                        to = last.offset(hierarchy) + last.length();
                    }

                    //first add the range of trimmed text, then the whole text range
                    int trimmed_from = from;
                    for(int i = 0; i < text.length(); i++) {
                        char ch = text.charAt(i);
                        if(!Character.isWhitespace(ch)) {
                            trimmed_from = trimmed_from + i;
                            break;
                        }
                    }
                    int trimmed_to = to;
                    for(int i = text.length() - 1; i >= 0 ; i--) {
                        char ch = text.charAt(i);
                        if(!Character.isWhitespace(ch)) {
                            trimmed_to = to - ((text.length() - 1) - i);
                            break;
                        }
                    }

                    if(trimmed_from != from || trimmed_to != to) {
                        ranges.add(new OffsetRange(trimmed_from, trimmed_to));
                    }

                    ranges.add(new OffsetRange(from, to));
                }
            }
        }

        Snapshot snapshot = result.getSnapshot();
        Collection<Node> roots = new ArrayList<>(result.roots().values()); //all declared namespaces
        roots.add(result.rootOfUndeclaredTagsParseTree()); //undeclared content
        
        for(Node root : roots) {
            //find leaf at the position
            Element node = ElementUtils.findBySemanticRange(root, snapshot.getEmbeddedOffset(caretOffset), false);
            if(node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    int ast_to = node.type() == ElementType.OPEN_TAG 
                            ? ((OpenTag)node).semanticEnd()
                            : node.to();

                    int from = snapshot.getOriginalOffset(node.from());
                    int to = snapshot.getOriginalOffset(ast_to);

                    if(from == -1 || to == -1 || from == to) {
                        continue;
                    }
                    ranges.add(new OffsetRange(from, to));
                } while ((node = node.parent()) != null);
            }
        }

        //leaf to root order, the OffsetRange compareTo orders in the opposite manner
        List<OffsetRange> ret = new ArrayList<>(ranges);
        Collections.reverse(ret);
        return ret;
    }

    //TODO implement
    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
