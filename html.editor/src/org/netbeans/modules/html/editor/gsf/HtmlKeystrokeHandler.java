/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
