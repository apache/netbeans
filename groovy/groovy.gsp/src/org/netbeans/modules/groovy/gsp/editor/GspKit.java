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

package org.netbeans.modules.groovy.gsp.editor;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.groovy.gsp.GspLanguage;
import org.netbeans.modules.html.editor.api.HtmlKit;

/**
 * Editor kit implementation for GSP content type
 *
 * @todo rewrite from Ruby to Groovy specifics
 * 
 * @author Marek Fukala
 * @author Tor Norbye
 * @author Martin Adamek
 */

public class GspKit extends HtmlKit {
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("org.netbeans.modules.groovy.gsp.editor.GspKit");
    }
    
    static final long serialVersionUID =-1381945567613910297L;
        
    public GspKit(){
        super(GspLanguage.GSP_MIME_TYPE);
    }
    
    @Override
    public String getContentType() {
        return GspLanguage.GSP_MIME_TYPE;
    }

    @Override
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        
        return TextAction.augmentList(superActions, new Action[] {
            new GspDeleteCharAction(deletePrevCharAction, false),
            new GspDefaultKeyTypedAction(),
            CslActions.createSelectCodeElementAction(true),
            CslActions.createSelectCodeElementAction(false),
            CslActions.createCamelCasePositionAction(findAction(superActions, nextWordAction), true),
            CslActions.createCamelCasePositionAction(findAction(superActions, previousWordAction), false),
            CslActions.createSelectCamelCasePositionAction(findAction(superActions, selectionNextWordAction), true),
            CslActions.createSelectCamelCasePositionAction(findAction(superActions, selectionPreviousWordAction), false),
            CslActions.createDeleteToCamelCasePositionAction(findAction(superActions, removeNextWordAction), true),
            CslActions.createDeleteToCamelCasePositionAction(findAction(superActions, removePreviousWordAction), false),
            CslActions.createInstantRenameAction()
         });
    }

    private static Action findAction(Action [] actions, String name) {
        for(Action a : actions) {
            Object nameObj = a.getValue(Action.NAME);
            if (nameObj instanceof String && name.equals(nameObj)) {
                return a;
            }
        }
        return null;
    }
    private boolean handleDeletion(BaseDocument doc, int dotPos) {

        // TODO Implement this correctly using Typing hooks API
        // Current handling is more than useless
//        if (dotPos > 0) {
//            try {
//                char ch = doc.getText(dotPos-1, 1).charAt(0);
//                if (ch == '%') {
//                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//                    TokenSequence<?> ts = th.tokenSequence();
//                    ts.move(dotPos);
//                    if (ts.movePrevious()) {
//                        Token<?> token = ts.token();
//                        if (token.id() == GspTokenId.DELIMITER && ts.offset()+token.length() == dotPos && ts.moveNext()) {
//                            token = ts.token();
//                            if (token.id() == GspTokenId.DELIMITER && ts.offset() == dotPos) {
//                                doc.remove(dotPos-1, 1+token.length());
//                                return true;
//                            }
//                        }
//                    }
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//        }
        
        return false;
    }

    private boolean handleInsertion(BaseDocument doc, Caret caret, char c) {
        // TODO Implement this correctly using Typing hooks API
        // Current handling is more than useless

//        int dotPos = caret.getDot();
//        // Bracket matching on <% %>
//        if (c == ' ' && dotPos >= 2) {
//            try {
//                String s = doc.getText(dotPos-2, 2);
//                if ("%=".equals(s) && dotPos >= 3) { // NOI18N
//                    s = doc.getText(dotPos-3, 3);
//                }
//                if ("<%".equals(s) || "<%=".equals(s)) { // NOI18N
//                    doc.insertString(dotPos, "  ", null);
//                    caret.setDot(dotPos+1);
//                    return true;
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//
//            return false;
//        }
//
//        if ((dotPos > 0) && (c == '%' || c == '>')) {
//            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//            TokenSequence<?> ts = th.tokenSequence();
//            ts.move(dotPos);
//            try {
//                if (ts.moveNext() || ts.movePrevious()) {
//                    Token<?> token = ts.token();
//                    if (token.id() == GspTokenId.HTML && doc.getText(dotPos-1, 1).charAt(0) == '<') {
//                        // See if there's anything ahead
//                        int first = Utilities.getFirstNonWhiteFwd(doc, dotPos, Utilities.getRowEnd(doc, dotPos));
//                        if (first == -1) {
//                            doc.insertString(dotPos, "%%>", null); // NOI18N
//                            caret.setDot(dotPos+1);
//                            return true;
//                        }
//                    } else if (token.id() == GspTokenId.DELIMITER) {
//                        String tokenText = token.text().toString();
//                        if (tokenText.endsWith("%>")) { // NOI18N
//                            // TODO - check that this offset is right
//                            int tokenPos = (c == '%') ? dotPos : dotPos-1;
//                            CharSequence suffix = DocumentUtilities.getText(doc, tokenPos, 2);
//                            if (CharSequenceUtilities.textEquals(suffix, "%>")) { // NOI18N
//                                caret.setDot(dotPos+1);
//                                return true;
//                            }
//                        } else if (tokenText.endsWith("<")) {
//                            // See if there's anything ahead
//                            int first = Utilities.getFirstNonWhiteFwd(doc, dotPos, Utilities.getRowEnd(doc, dotPos));
//                            if (first == -1) {
//                                doc.insertString(dotPos, "%%>", null); // NOI18N
//                                caret.setDot(dotPos+1);
//                                return true;
//                            }
//                        }
//                    } else if (token.id() == GspTokenId.GSTRING_CONTENT && dotPos >= 1 && dotPos <= doc.getLength()-3) {
//                        // If you type ">" one space away from %> it's likely that you typed
//                        // "<% foo %>" without looking at the screen; I had auto inserted %> at the end
//                        // and because I also auto insert a space without typing through it, you've now
//                        // ended up with "<% foo %> %>". Let's prevent this by interpreting typing a ""
//                        // right before %> as a duplicate for %>.   I can't just do this on % since it's
//                        // quite plausible you'd have
//                        //   <% x = %q(foo) %>  -- if I simply moved the caret to %> when you typed the
//                        // % in %q we'd be in trouble.
//                        String s = doc.getText(dotPos-1, 4);
//                        if ("% %>".equals(s)) { // NOI18N
//                            doc.remove(dotPos-1, 2);
//                            caret.setDot(dotPos+1);
//                            return true;
//                        }
//                    }
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//        }
        
        return false;
    }

    private class GspDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            String cmd = evt.getActionCommand();
            if (cmd.length() > 0) {
                char c = cmd.charAt(0);
                if (handleInsertion(doc, caret, c)) {
                    return;
                }
            }

            super.actionPerformed(evt, target);
        }
    }
    
    private class GspDeleteCharAction extends ExtDeleteCharAction {

        public GspDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            int dotPos = caret.getDot();
            if (handleDeletion(doc, dotPos)) {
                return;
            }
            super.actionPerformed(evt, target);
        }
    }
    
    @Override
    public Object clone() {
        return new GspKit();
    }
    
    private static Token<?> getToken(BaseDocument doc, int offset, boolean checkEmbedded) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        TokenSequence<?> ts = th.tokenSequence();
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        if (checkEmbedded) {
            TokenSequence<?> es = ts.embedded();
            if (es != null) {
                es.move(offset);
                if (es.moveNext() || es.movePrevious()) {
                    return es.token();
                }
            }
        }
        return ts.token();
    }
}

