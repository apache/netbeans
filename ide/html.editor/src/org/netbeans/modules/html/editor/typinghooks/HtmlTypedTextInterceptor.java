/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor.typinghooks;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.xhtml.XhtmlElTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class HtmlTypedTextInterceptor implements TypedTextInterceptor {

    /* test */ static boolean adjust_quote_type_after_eq = true; //can be disabled in unit tests
    /* test */ static char default_quote_char_after_eq = '"'; //TODO expose in UI options

    private static DocumentInsertIgnore insertIgnore;

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        int dotPos = context.getOffset();
        Caret caret = context.getComponent().getCaret();
        char ch = context.getText().charAt(0);

        //"ignore typed text support" 
        //depending on previous actions we are 
        //going to ignore some characters typed
        if (insertIgnore != null) {
            DocumentInsertIgnore local = insertIgnore;
            insertIgnore = null;
            if (local.getOffset() == dotPos && local.getChar() == ch) {
                //move the caret to specified position if needed
                if (local.getMoveCaretTo() != -1) {
                    caret.setDot(local.moveCaretTo);
                    //also close the completion window
                    Completion.get().hideAll();
                }
                return true; //stop subsequent processing of the change
            }
        } else {
            //normal before key typed processing
            switch (ch) {
                case '\'':
                case '"':
                    boolean result = skipExistingQuote(context);
                    if (!result) {
                        result = changeQuotesType(context);
                    }
                    return result;
            }
        }
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        char ch = context.getText().charAt(0);
        switch (ch) {
            case '=':
                addPairOfQuotes(context);
                break;
            case '\'':
            case '"':
                addSecondQuote(context);
                break;
            case '{':
                addClosingELDelimiter(context);
                break;
            case '/':
                addTagClosingSymbol(context);
                break;
            case '>':
                indentLineAfterTagClosingSymbol(context);
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        //no-op
    }

    @Override
    public void cancelled(Context context) {
        insertIgnore = null;
    }

    private static void indentLineAfterTagClosingSymbol(MutableContext context) {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        int offset = context.getOffset();
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence(doc, offset, HTMLTokenId.language());
        if (ts == null) {
            return; //no html ts at the caret position
        }
        ts.move(offset);
        if (!ts.moveNext()) {
            return; //no token
        }

        if (null != LexerUtils.followsToken(ts, EnumSet.of(HTMLTokenId.TAG_OPEN, HTMLTokenId.TAG_CLOSE), true, false,
                HTMLTokenId.ARGUMENT,
                HTMLTokenId.VALUE,
                HTMLTokenId.VALUE_CSS,
                HTMLTokenId.VALUE_JAVASCRIPT,
                HTMLTokenId.OPERATOR,
                HTMLTokenId.WS,
                HTMLTokenId.EL_CLOSE_DELIMITER,
                HTMLTokenId.EL_CONTENT,
                HTMLTokenId.EL_OPEN_DELIMITER)) {
            try {
                //we are in open or close tag

                //ok, the user just type tag closing symbol, lets reindent the line
                //since the code runs under document atomic lock, we cannot lock the
                //indentation infrastructure directly. Instead of that create a new
                //AWT task and post it for later execution.
                final Position from = doc.createPosition(LineDocumentUtils.getLineStartOffset(doc, offset));
                final Position to = doc.createPosition(LineDocumentUtils.getLineEndOffset(doc, offset));

                Runnable r = new Runnable() {
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
                if (SwingUtilities.isEventDispatchThread()) {
                    r.run();
                } else {
                    SwingUtilities.invokeLater(r);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private static void addTagClosingSymbol(MutableContext context) throws BadLocationException {
        TokenSequence<HTMLTokenId> ts = LexerUtils.getTokenSequence((BaseDocument) context.getDocument(), context.getOffset(), HTMLTokenId.language(), true);
        if (ts == null) {
            return; //no html ts at the caret position
        }
        ts.move(context.getOffset());
        if (!ts.moveNext()) {
            return; //no token
        }

        HTMLTokenId tid = ts.token().id();
        if (tid == HTMLTokenId.WS) {
            if (null != LexerUtils.followsToken(ts, HTMLTokenId.TAG_OPEN, true, false,
                    HTMLTokenId.ARGUMENT,
                    HTMLTokenId.VALUE,
                    HTMLTokenId.VALUE_CSS,
                    HTMLTokenId.VALUE_JAVASCRIPT,
                    HTMLTokenId.OPERATOR,
                    HTMLTokenId.WS,
                    HTMLTokenId.EL_CLOSE_DELIMITER,
                    HTMLTokenId.EL_CONTENT,
                    HTMLTokenId.EL_OPEN_DELIMITER)) {
                //we are in an open tag
                context.setText("/>", 2);
                //ignore subsequent '>' if typed
                insertIgnore = new DocumentInsertIgnore(context.getOffset() + 2, '>', -1); // NOI18N
            }
        }

    }

    private static boolean skipExistingQuote(final Context context) throws BadLocationException {
        if (!HtmlPreferences.autocompleteQuotes()) {
            return false;
        }

        final BaseDocument doc = (BaseDocument) context.getDocument();
        final AtomicBoolean result = new AtomicBoolean();
        doc.render(new Runnable() {

            @Override
            public void run() {
                int dotPos = context.getOffset();
                int qchar = context.getText().charAt(0);
                //test whether the user typed an ending quotation in the attribute value
                TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence(doc, dotPos, HTMLTokenId.language());
                if (ts == null) {
                    return;
                }
                int diff = ts.move(dotPos);
                if (diff == 0) {
                    if (!ts.movePrevious()) {
                        return;
                    }
                } else {
                    if (!ts.moveNext()) {
                        return;
                    }
                }

                Token<HTMLTokenId> token = ts.token();
                if (isHtmlValueToken(token)) {
                    //test if the user inserted the quotation in an attribute value and before
                    //an already existing end quotation
                    //the text looks following in such a situation:
                    //
                    //  atrname="abcd|"", where offset of the | == dotPos
                    if (diff > 0 && token.text().charAt(diff) == qchar) { // NOI18N
                        context.getComponent().setCaretPosition(dotPos + 1);
                        result.set(true);
                    }
                }
            }

        });
        return result.get();
    }

    private static void addSecondQuote(MutableContext context) throws BadLocationException {
        if (!HtmlPreferences.autocompleteQuotes()) {
            return;
        }

        int dotPos = context.getOffset();
        char qchar = context.getText().charAt(0);
        //test whether the user typed an ending quotation in the attribute value
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) context.getDocument(), dotPos, HTMLTokenId.language());
        if (ts == null) {
            return;
        }
        int diff = ts.move(dotPos);
        if (diff == 0) {
            if (!ts.movePrevious()) {
                return;
            }
        } else {
            if (!ts.moveNext()) {
                return;
            }
        }

        Token<HTMLTokenId> token = ts.token();
        if (token.id() == HTMLTokenId.OPERATOR) {
            //check if the next token is a value 
            if (ts.moveNext()) {
                Token<HTMLTokenId> next = ts.token();
                if (isHtmlValueToken(next)) {
                    return;
                }
            }

            //user typed quation just after equal sign after tag attribute name => complete the second quote
            StringBuilder insert = new StringBuilder().append(qchar).append(qchar);
            context.setText(insert.toString(), 1);
        }
    }

    private static boolean isHtmlValueToken(Token token) {
        TokenId id = token.id();

        return id == HTMLTokenId.VALUE
                || id == HTMLTokenId.VALUE_CSS
                || id == HTMLTokenId.VALUE_JAVASCRIPT;
    }

    private static void addPairOfQuotes(MutableContext context) {
        if (!HtmlPreferences.autocompleteQuotesAfterEqualSign()) {
            return;
        }

        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) context.getDocument(), context.getOffset(), HTMLTokenId.language());
        if (ts == null) {
            return; //no html ts at the caret position
        }
        ts.move(context.getOffset());
        if (LexerUtils.followsToken(ts, HTMLTokenId.ARGUMENT, true, false) != null) {
            //element's attribute: <div align|
            String insert = new StringBuilder().append('=').append(default_quote_char_after_eq).append(default_quote_char_after_eq).toString();
            context.setText(insert, 2); //set caret inside the quotes: ="|"
        }
    }

    private static boolean changeQuotesType(final Context context) {
        if (!HtmlPreferences.autocompleteQuotesAfterEqualSign()) {
            return false;
        }
        final AtomicBoolean result = new AtomicBoolean();
        final BaseDocument doc = (BaseDocument) context.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                int dotPos = context.getOffset();
                char expected = context.getText().charAt(0);
                TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence(doc, dotPos, HTMLTokenId.language());
                if (ts == null) {
                    return; //no html ts at the caret position
                }
                ts.move(dotPos);
                if (!ts.moveNext()) {
                    return; //no token
                }

                Token<HTMLTokenId> token = ts.token();

                int dotPosBeforeTypedChar = dotPos - 1;
                String text = token.text().toString();

                String pattern = expected == '\'' ? "\"\"" : "''";
                if (text.contentEquals(pattern)) { // NOI18N
                    try {
                        doc.remove(dotPosBeforeTypedChar, 2); //remove the existing quote pair
                        doc.insertString(dotPosBeforeTypedChar, new StringBuilder().append(expected).append(expected).toString(), null); //add new pair
                        context.getComponent().setCaretPosition(dotPosBeforeTypedChar + 1); //set caret between the quotes
                        result.set(true); //mark we've already handled the key event
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                //remember that user changed the default type so next time we'll autocomplete the wanted one
                if (adjust_quote_type_after_eq) {
                    default_quote_char_after_eq = expected;
                }
            }

        });
        return result.get();
    }

    //autocomplete ${ "}" and moves the caret inside the brackets
    private static void addClosingELDelimiter(MutableContext context) {
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        if (ts == null) {
            return;
        }
        int diff = ts.move(context.getOffset());
        if (diff == 0) {
            return; // ${ - the token diff must be > 0
        }

        if (!ts.moveNext()) {
            return; //no token
        }

        Token token = ts.token();
        int dotPosAfterTypedChar = context.getOffset() + 1;
        if (token.id() == XhtmlElTokenId.HTML) {
            char charBefore = token.text().charAt(diff - 1);
            if (charBefore == '$' || charBefore == '#') { // NOI18N
                char charAfter = token.text().charAt(diff);
                if (charAfter == '}') { // NOI18N
                    return;
                }
                context.setText("{}", 1); //NOI18N //set caret between the curly braces

                //open completion
                Completion.get().showCompletion();

                //ignore '}' char
                insertIgnore = new DocumentInsertIgnore(dotPosAfterTypedChar, '}', dotPosAfterTypedChar + 1); // NOI18N
            }

        }
    }

    private static class DocumentInsertIgnore {

        private final int offset;
        private final char ch;
        private final int moveCaretTo;

        public DocumentInsertIgnore(int offset, char ch, int moveCaretTo) {
            this.offset = offset;
            this.ch = ch;
            this.moveCaretTo = moveCaretTo;
        }

        public char getChar() {
            return ch;
        }

        public int getOffset() {
            return offset;
        }

        public int getMoveCaretTo() {
            return moveCaretTo;
        }
    }

    /*
     typing hook for text/xhtml and text/css languages.
     
     EL:
     Once we autocomplete the closing EL expression delimiter: #{"}" - 
     see {@link #addClosingELDelimiter(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor.MutableContext) },
     the the content of the expression changes to {@link XhtmlElTokenId.EL} and the HtmlTypedTextInterceptor won't
     be triggered for the content anymore. The "insert ignore" code won't run and if the user types the closing
     curly bracket it won't be skipped and s/he ends up with #{}}|.
     So we need to register a special hook for the text/xhtml content type
     and do the key event ignore check here. 
     
     CSS:
     Similar to EL - once use type <div class=, the quote pair is added automatically.
     However as the lexer language changes to text/css inside the attribute value, 
     the default HtmlTypedTextInterceptor is not called anymore and if the user subsequently
     types the quote, it will not skip the generated one, but create a new one.
     To resolve this, we need to register a TypedTextInterceptor also to the text/css
     to handle the "inser ignores".
    
     */
    private static class InsertIgnoreSupportTypedTextInterceptor extends TypedTextInterceptorAdapter {

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            //"ignore typed text support" 
            //depending on previous actions we are 
            //going to ignore some characters typed
            if (insertIgnore != null) {
                DocumentInsertIgnore local = insertIgnore;
                insertIgnore = null;
                if (local.getOffset() == context.getOffset() && local.getChar() == context.getText().charAt(0)) {
                    //move the caret to specified position if needed
                    if (local.getMoveCaretTo() != -1) {
                        context.getComponent().setCaretPosition(local.moveCaretTo);
                        //also close the completion window
                        Completion.get().hideAll();
                    }
                    return true; //stop subsequent processing of the change
                }
            }
            return false;
        }

    }

    private static class CssTypedTextInterceptor extends TypedTextInterceptorAdapter {

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            char ch = context.getText().charAt(0);
            switch (ch) {
                case '\'':
                case '"':
                    boolean result = skipExistingQuote(context);
                    if (!result) {
                        result = changeQuotesType(context);
                    }
                    return result;
            }
            return false;
        }
    }

    private static class TypedTextInterceptorAdapter implements TypedTextInterceptor {

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
        }

        @Override
        public void afterInsert(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }

    }

    @MimeRegistration(mimeType = "text/html", service = TypedTextInterceptor.Factory.class)
    public static final class HtmlFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new HtmlTypedTextInterceptor();
        }

    }

    @MimeRegistration(mimeType = "text/css", service = TypedTextInterceptor.Factory.class)
    public static final class CssFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new CssTypedTextInterceptor();
        }

    }

    //workaround for insert ignore in <div class="|" -- see 
    //the InsertIgnoreSupportTypedTextInterceptor documentation
    //workaround for insert ignore in #{|} -- see the {@liXhtmlTypedTextInterceptor} documentation
    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/css", service = TypedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = "text/xhtml", service = TypedTextInterceptor.Factory.class)
    })
    public static final class InsertIgnoreFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new InsertIgnoreSupportTypedTextInterceptor();
        }

    }

}
