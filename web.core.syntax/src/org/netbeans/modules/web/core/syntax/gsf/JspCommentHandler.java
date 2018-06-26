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
