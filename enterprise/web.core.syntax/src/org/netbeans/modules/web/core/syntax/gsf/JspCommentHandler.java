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
package org.netbeans.modules.web.core.syntax.gsf;

import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.spi.CommentHandler;

/**
 *
 * @author marekfukala
 */
public class JspCommentHandler extends CommentHandler.DefaultCommentHandler {

    @Override
    public int[] getAdjustedBlocks(final Document doc, int from, int to) {
        //check if we aren't in a jsp code which cannot be commented by jsp comments
        //like scriptlet content
        final int[] bounds = new int[]{from,to};
        final boolean[] inScriptlet = new boolean[]{false, false};
        Runnable task = new Runnable() {
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence ts = th.tokenSequence(JspTokenId.language());
                assert ts != null : "Not called on a JSP file!";

                //check beginning of the section
                ts.move(bounds[0]);
                if(ts.moveNext()) {
                    Token t = ts.token();
                    if(isScriptletOrDelimiter(t)) {
                        inScriptlet[0] = true;
                        //try to find end of the scriptlet 
                        //and shift the 'from' offset to that position
                        while(ts.moveNext() && ts.offset() <= bounds[1]) {
                            t = ts.token();
                            if(!isScriptletOrDelimiter(t)) {
                                //found non scriptlet code
                                bounds[0] = ts.offset();
                                inScriptlet[0] = false; //fixed
                                break;
                            }
                        }
                    }
                }

                //check section end
                ts.move(bounds[1]);
                if(ts.moveNext()) {
                    Token t = ts.token();

                    // in case of caret within the comment, find the ending bound
                    if (t.id() == JspTokenId.COMMENT) {
                        do {
                            bounds[1] += ts.token().length();
                        } while (ts.moveNext() && ts.token().id() == JspTokenId.COMMENT);
                    }

                    if(isScriptletOrDelimiter(t)) {
                        inScriptlet[1] = true;
                        //try to find beginning of the scriptlet
                        //and shift the 'to' to that position
                        while(ts.movePrevious() && ts.offset() + ts.token().length() > bounds[0]) {
                            t = ts.token();
                            if(!isScriptletOrDelimiter(t)) {
                                //found non scriptlet code
                                bounds[1] = ts.offset() + t.length();
                                inScriptlet[1] = false; //fixed
                                break;
                            }
                        }
                    }
                }

            }
        };

        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(task);
        } else {
            task.run();
        }

        if(inScriptlet[0] && inScriptlet[1]) {
            //cannot fix
            return new int[]{};
        }

        return new int[]{bounds[0], bounds[1]};
    }

    private boolean isScriptletOrDelimiter(Token t) {
        if(t.id() == JspTokenId.SCRIPTLET) {
            return true;
        }

        if(t.id() == JspTokenId.SYMBOL2) {
            if(CharSequenceUtilities.equals("<%", t.text()) ||
                    CharSequenceUtilities.equals("%>", t.text())) { //NOI18N
                return true;
            }
        }

        return false;
    }

    public String getCommentStartDelimiter() {
        return "<%--";
    }

    public String getCommentEndDelimiter() {
        return "--%>";
    }
}
