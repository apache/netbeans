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
package org.netbeans.modules.css.editor.typinghooks;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author marek
 */
public class CssTypedTextInterceptor implements TypedTextInterceptor {

    static boolean inTest;
    
    private static final char[][] PAIRS = new char[][]{
        {'{', '}'}, 
        {'"', '"'}, 
        {'\'', '\''}, 
        {'(', ')'}, 
        {'[', ']'}
    }; //NOI18N

    static char justAddedPair;
    static int justAddedPairOffset = -1;

    private int pairIndex(char ch) {
        for (int i = 0; i < PAIRS.length; i++) {
            char pair = PAIRS[i][0];
            if (pair == ch) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean beforeInsert(final Context context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        final AtomicBoolean result = new AtomicBoolean();
        final AtomicReference<BadLocationException> ble = new AtomicReference<>();
        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                try {
                    int offset = context.getOffset();
                    char ch = context.getText().charAt(0);
                    
                    if (justAddedPair == ch && justAddedPairOffset == offset) {
                        //skip
                        justAddedPair = 0;
                        justAddedPairOffset = -1;
                        
                        context.getComponent().setCaretPosition(offset + 1);
                        result.set(true); //stop further even processing
                        return ;
                    }
                    
                    justAddedPair = 0;
                    justAddedPairOffset = -1;
                    
                    TokenHierarchy<BaseDocument> hi = TokenHierarchy.get(doc);
                    TokenSequence<CssTokenId> ts = LexerUtils.getTokenSequence(hi, context.getOffset(), CssTokenId.language(), true);
                    if (ts == null) {
                        return; //no css code
                    }
                    
                    if (ch == '}') { //NOI18N
                        //handle curly bracket skipping
                        //if there is a matching opening bracket and there is no opened unpaired bracket before
                        //then just skip the typed char
                        ts.move(offset);
                        if (ts.moveNext()) {
                            //ts is already positioned
                            if (ts.token().id() == CssTokenId.RBRACE) {
                                //skip it
                                context.getComponent().setCaretPosition(offset + 1);
                                result.set(true); //stop further even processing
                                return ;
                            }
                        }
                    }
                    
                    //test if we care about the typed character
                    int pairIdx = pairIndex(ch);
                    if (pairIdx == -1) {
                        return;
                    }
                    
                    if (ch == '\'' || ch == '"') {
                        //handle quotations
                        
                        //issue 189711 workaround - if the css code is embedde in html attribute value
                        //and user types " at the end of the value additional quotation is incorrectly
                        //added: <div id="myid| + " => <div id="myid""
                        //
                        //we need to check if the actuall css code is embedded in an attribute
                        //value w/o depending on the html module. An SPI in web.common could do it as well
                        if (findHtmlValueToken(hi, offset)) {
                            //we are in a css value, do not complete the quote
                            return;
                        }
                        
                        int diff = ts.move(offset);
                        if (ts.moveNext()) {
                            Token t = ts.token();
                            if (t.id() == CssTokenId.STRING) {
                                //we are in or at a string
                                char front = t.text().charAt(diff);
                                if (front == ch) {
                                    //do not insert, just move caret
                                    context.getComponent().setCaretPosition(offset + 1);
                                    result.set(true);
                                    return;
                                } else {
                                    //found unmatched quotation mark - do nothing
                                    return;
                                }
                            } else {
                                //check whether the next token starts with a letter, if so do not autocomplete
                                //example: typeing quote in: div { background-image: url(|hello.png);
                                //should not add the pair quote
                                if(t.text().length() > 0 && Character.isJavaIdentifierPart(t.text().charAt(diff))) {
                                    return ;
                                }
                                
                            }
                            
                            //cover "text| and user types "
                            //in such case just the quotation should be added
                            //go back until we find " or ; { or } and test of the
                            //found quotation is a part of a string or not
                            diff = ts.move(offset);
                            if (ts.moveNext()) {
                                do {
                                    t = ts.token();
                                    if (t.text().charAt(0) == ch) {
                                        if (t.id() == CssTokenId.STRING) {
                                            //no unmatched quotation mark
                                            break;
                                        } else {
                                            //found unmatched quotation mark - do nothing
                                            return;
                                        }
                                    } else {
                                        //TODO fix - naive - is the token contains the typed quote, lets assume the quote about to be typed is the closing quote -> do not add a pair
                                        if(CharSequenceUtilities.indexOf(t.text(), ch) != -1) {
                                            return ;
                                        }
                                    }
                                    if (t.id() == CssTokenId.LBRACE || t.id() == CssTokenId.RBRACE || t.id() == CssTokenId.SEMI) {
                                        //break the loop, not quotation found - we can complete
                                        break;
                                    }
                                } while (ts.movePrevious());
                            }
                        }
                    }
                    
                    justAddedPair = PAIRS[pairIdx][1];
                    justAddedPairOffset = offset + 1;
                    
                    context.getDocument().insertString(offset, String.valueOf(PAIRS[pairIdx][0]), null);
                    context.getDocument().insertString(offset + 1, String.valueOf(justAddedPair), null);
                    context.getComponent().setCaretPosition(offset + 1);
                    
                    result.set(true); //cancel furher processing of the event
                    
                } catch (BadLocationException ex) {
                    ble.set(ex);
                }
            }

        });
        //rethrow the BLE if necessary
        if(ble.get() != null) {
            throw ble.get();
        }
        
        return result.get();
    }

    private static boolean findHtmlValueToken(TokenHierarchy<?> hi, int offset) {
        boolean found = findHtmlValueToken(hi, offset, true);
        return found ? true : findHtmlValueToken(hi, offset, false);
    }

    private static boolean findHtmlValueToken(TokenHierarchy<?> hi, int offset, boolean backward) {
        List<TokenSequence<?>> embeddedTokenSequences = hi.embeddedTokenSequences(offset, backward);
        for (TokenSequence<?> htmlts : embeddedTokenSequences) {
            if (htmlts.language().mimeType().equals("text/html")) {
                //it relly looks like our parent ts
                int ediff = htmlts.move(offset);
                if (ediff == 0 && backward && htmlts.movePrevious() || htmlts.moveNext()) {
                    TokenId id = htmlts.token().id();
                    //XXX !!!! DEPENDENCY to HtmlTokenId !!!!
                    return id.name().equals("VALUE_CSS");
                }
            }
        }
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        char ch = context.getText().charAt(0);
        if ('}' == ch) {
            final int lineStart = Utilities.getRowFirstNonWhite((BaseDocument) context.getDocument(), context.getOffset());
            if (lineStart == context.getOffset()) {
                reindentLater((BaseDocument) context.getDocument(), context.getOffset(), context.getOffset());
            }
        }
    }

    //since the code runs under document atomic lock, we cannot lock the
    //indentation infrastructure directly. Instead of that create a new
    //AWT task and post it for later execution.
    private void reindentLater(final BaseDocument doc, int start, int end) throws BadLocationException {
        final Position from = doc.createPosition(Utilities.getRowStart(doc, start));
        final Position to = doc.createPosition(Utilities.getRowEnd(doc, end));
        Runnable rn = new Runnable() {

            @Override
            public void run() {
                final Indent indent = Indent.get(doc);
                indent.lock();
                try {
                    doc.runAtomic(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                indent.reindent(from.getOffset(), to.getOffset());
                            } catch (BadLocationException ex) {
                                //ignore
                            }
                        }
                    });
                } finally {
                    indent.unlock();
                }
            }
        };
        if (inTest) {
            rn.run();
        } else {
        SwingUtilities.invokeLater(rn);
        }
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistration(mimeType = "text/css", service = TypedTextInterceptor.Factory.class)
    public static final class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new CssTypedTextInterceptor();
        }

    }
}
