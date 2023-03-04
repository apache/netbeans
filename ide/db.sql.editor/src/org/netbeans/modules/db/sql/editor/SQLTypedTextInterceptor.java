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
package org.netbeans.modules.db.sql.editor;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionEnv;
import org.netbeans.modules.db.sql.lexer.SQLLexer;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 * Implement typed text interceptor for SQL files.
 * 
 * The class implements the intercepter necessary to handle automatic
 * closing brace insertion and skipping over closing braces when typed.
 * 
 * @author matthias42
 */
public class SQLTypedTextInterceptor implements TypedTextInterceptor {

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/x-sql", service = TypedTextInterceptor.Factory.class)
    })
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new SQLTypedTextInterceptor();
        }
    }

    @Override
    public boolean beforeInsert(final Context context) throws BadLocationException {
        if (!OptionsUtils.isPairCharactersCompletion()) {
            return false;
        }

        final Document doc = context.getDocument();

        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                int caretOffset = context.getOffset();
                char typedChar = context.getText().charAt(0); // guaranteed to be one character

                Character nextChar;
                try {
                    nextChar = doc.getText(caretOffset, 1).charAt(0);
                } catch (BadLocationException ex) {
                    nextChar = null;
                }

                if (nextChar != typedChar) {
                    return false;
                }

                Caret c = context.getComponent().getCaret();

                // Skip inserting typed character, if next char is a closing brace
                if (typedChar == ')') {
                    c.setDot(c.getDot() + 1);
                    return true;
                }

                // Skip closing quotes for identifiers and strings
                SQLCompletionEnv compEnv = SQLCompletionEnv.forDocument(doc, caretOffset);
                TokenSequence ts = compEnv.getTokenSequence();
                ts.move(compEnv.getCaretOffset());
                if (ts.moveNext()) {
                    Token currentToken = ts.token();
                    if (currentToken.id() == SQLTokenId.IDENTIFIER
                            || currentToken.id() == SQLTokenId.STRING) {
                        char quoteStart = currentToken.text().charAt(0);
                        if (SQLLexer.isEndStringQuoteChar(quoteStart, nextChar)
                                || SQLLexer.isEndIdentifierQuoteChar(quoteStart, nextChar)) {
                            c.setDot(c.getDot() + 1);
                            return true;
                        }
                    }
                }

                return false;
            }
        };

        Boolean result;

        try {
            if (doc instanceof BaseDocument) {
                FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ((BaseDocument) doc).runAtomic(task);
                result = task.get();
            } else {
                result = callable.call();
            }
        } catch (Exception ex) {
            result = false;
        }

        return result;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
    }

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        if (!OptionsUtils.isPairCharactersCompletion()) {
            return;
        }

        final Document doc = context.getDocument();

        Runnable r = new Runnable() {
            public void run() {
                int caretOffset = context.getOffset();
                String str = context.getText();

                Character nextChar;
                try {
                    nextChar = doc.getText(caretOffset + str.length(),
                            1).charAt(0);
                } catch (BadLocationException ex) {
                    nextChar = null;
                }

                if (!str.isEmpty()) {
                    char insertedChar = str.charAt(str.length() - 1);
                    if ((SQLLexer.isStartIdentifierQuoteChar(insertedChar)
                            && (canCompleteLookAhead(nextChar)
                            || nextChar.equals('.')))
                            || (SQLLexer.isStartStringQuoteChar(insertedChar)
                            && canCompleteLookAhead(nextChar))) {  //NOI18N
                        if (canCompleteQuote(doc, caretOffset)) {
                            try {
                                // add pair quote
                                doc.insertString(caretOffset + str.length(), String.valueOf((char) SQLLexer.getMatchingQuote(insertedChar)), null);
                                context.getComponent().getCaret().setDot(caretOffset
                                        + str.length());
                            } catch (BadLocationException ex) {
                            }
                        }
                    } else if (insertedChar == '(' && canCompleteLookAhead(nextChar)) {
                        if (canCompleteBrace(doc, caretOffset)) {
                            try {
                                // add pair quote
                                doc.insertString(caretOffset + str.length(), ")", null);
                                context.getComponent().getCaret().setDot(caretOffset
                                        + str.length());
                            } catch (BadLocationException ex) {
                            }
                        }
                    }
                }
            }
        };

        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(r);
        } else {
            r.run();
        }
    }

    // Check whether completion is sensible based on next character
    private static boolean canCompleteLookAhead(Character next) {
        return next == null || next == ';' || Character.isWhitespace(next);
    }
    
    /**
     * Returns true if completion of quote is wanted, i.e. cursor is on token
     * boundary and previous token is dot or whitespace.
     */
    private static boolean canCompleteQuote(Document doc, int caretOffset) {
        SQLCompletionEnv env = SQLCompletionEnv.forDocument(doc, caretOffset);
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        if (seq.move(env.getCaretOffset()) == 0 && seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case DOT:
                    return true;
            }
        }
        return false;
    }

    private static boolean canCompleteBrace(Document doc, int caretOffset) {
        SQLCompletionEnv env = SQLCompletionEnv.forDocument(doc, caretOffset);
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        if (seq.move(env.getCaretOffset()) == 0 && seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                    return true;
            }
        }
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }
}
