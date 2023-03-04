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
package org.netbeans.modules.groovy.editor.api.lexer;


import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;

/**
 * Class which represents a Call in the source
 */
public class Call {

    public static final Call LOCAL = new Call(null, null, false, false);
    public static final Call NONE = new Call(null, null, false, false);
    public static final Call UNKNOWN = new Call(null, null, false, false);
    private final String type;
    private final String lhs;
    private final boolean isStatic;
    private final boolean methodExpected;

    public Call(String type, String lhs, boolean isStatic, boolean methodExpected) {
        super();
        this.type = type;
        this.lhs = lhs;
        this.methodExpected = methodExpected;
        if (lhs == null) {
            lhs = type;
        }
        this.isStatic = isStatic;
    }

    public String getType() {
        return type;
    }

    public String getLhs() {
        return lhs;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isSimpleIdentifier() {
        if (lhs == null) {
            return false;
        }
        // TODO - replace with the new RubyUtil validations
        for (int i = 0, n = lhs.length(); i < n; i++) {
            char c = lhs.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                continue;
            }
            if ((c == '@') || (c == '$')) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (this == LOCAL) {
            return "LOCAL";
        } else if (this == NONE) {
            return "NONE";
        } else if (this == UNKNOWN) {
            return "UNKNOWN";
        } else {
            return "Call(" + type + "," + lhs + "," + isStatic + ")";
        }
    }

    /** foo.| or foo.b|  -> we're expecting a method call. */
    public boolean isMethodExpected() {
        return this.methodExpected;
    }
    
    /**
     * Determine whether the given offset corresponds to a method call on another
     * object. This would happen in these cases:
     *    Foo.|, Foo.x|, foo.|, foo.x|
     * and not here:
     *   |, Foo|, foo|
     * The method returns the left hand side token, if any, such as "Foo"
     * and "foo". If not, it will return null.
     * Note that "self" and "super" are possible return values for the lhs, which mean
     * that you don't have a call on another object. Clients of this method should
     * handle that return value properly (I could return null here, but clients probably
     * want to distinguish self and super in this case so it's useful to return the info.)
     *
     * This method will also try to be smart such that if you have a block or array
     * call, it will return the relevant classnames (e.g. for [1,2].x| it returns "Array").
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static Call getCallType(BaseDocument doc, TokenHierarchy<Document> th, int offset) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return Call.NONE;
        }

        ts.move(offset);

        boolean methodExpected = false;

        if (!ts.moveNext() && !ts.movePrevious()) {
            return Call.NONE;
        }

        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // position, which could be whitespace, e.g.
            //  "foo.x| " <-- looking at the whitespace
            ts.movePrevious();
        }

        Token<GroovyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            if (id == GroovyTokenId.WHITESPACE) {
                return Call.LOCAL;
            }

            // See if we're in the identifier - "x" in "foo.x"
            // I could also be a keyword in case the prefix happens to currently
            // match a keyword, such as "next"
            // However, if we're at the end of the document, x. will lex . as an
            // identifier of text ".", so handle this case specially
            if (id == GroovyTokenId.IDENTIFIER || id.primaryCategory().equals("keyword")) {
                String tokenText = token.text().toString();

                if (".".equals(tokenText)) {
                    // Special case - continue - we'll handle this part next
                    methodExpected = true;
                } else {
                    methodExpected = true;

                    if (Character.isUpperCase(tokenText.charAt(0))) {
                        methodExpected = false;
                    }

                    if (!ts.movePrevious()) {
                        return Call.LOCAL;
                    }
                }

                token = ts.token();
                id = token.id();
            }

            // If we're not in the identifier we need to be in the dot (in "foo.x").
            // I can't just check for tokens DOT and COLON3 because for unparseable source
            // (like "File.|") the lexer will return the "." as an identifier.
            if (id == GroovyTokenId.DOT) {
                methodExpected = true;
            } else if (id == GroovyTokenId.COLON) {
            } else if (id == GroovyTokenId.IDENTIFIER) {
                String t = token.text().toString();

                if (t.equals(".")) {
                    methodExpected = true;
                }
            } else {
                return Call.LOCAL;
            }

            int lastSeparatorOffset = ts.offset();
            int beginOffset = lastSeparatorOffset;
            int lineStart = 0;

            try {
                if (offset > doc.getLength()) {
                    offset = doc.getLength();
                }

                lineStart = Utilities.getRowStart(doc, offset);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
            
            // Find the beginning of the expression. We'll go past keywords, identifiers
            // and dots or double-colons
            while (ts.movePrevious()) {
                // If we get to the previous line we're done
                if (ts.offset() < lineStart) {
                    break;
                }

                token = ts.token();
                id = token.id();

                if (id == GroovyTokenId.WHITESPACE) {
                    break;
                } else if (id == GroovyTokenId.STRING_LITERAL) {
                    // TODO: GString
                    return new Call("java.lang.String", null, false, methodExpected); // NOI18N
                } else if (id == GroovyTokenId.RBRACKET) {
                    // Looks like we're operating on an array, e.g.
                    //  [1,2,3].each|
                    return new Call("java.util.ArrayList", null, false, methodExpected); // NOI18N
//                } else if (id == GroovyTokenId.RBRACE) { // XXX uh oh, what about blocks?  {|x|printx}.| ? type="Proc"
//                                                       // Looks like we're operating on a hash, e.g.
//                                                       //  {1=>foo,2=>bar}.each|
//
//                    return new Call("Hash", null, false, methodExpected);
//                } else if ((id == GroovyTokenId.STRING_END) || (id == GroovyTokenId.QUOTED_STRING_END)) {
//                    return new Call("String", null, false, methodExpected);
//                } else if (id == GroovyTokenId.REGEXP_END) {
//                    return new Call("Regexp", null, false, methodExpected);
                } else if (id == GroovyTokenId.NUM_INT) {
                    return new Call("java.lang.Integer", null, false, methodExpected); // Or Bignum?
                } else if (id == GroovyTokenId.NUM_DOUBLE) {
                    return new Call("java.math.BigDecimal", null, false, methodExpected);
//                } else if (id == GroovyTokenId.TYPE_SYMBOL) {
//                    return new Call("Symbol", null, false, methodExpected);
//                } else if (id == GroovyTokenId.RANGE_INCLUSIVE) {
//                    return new Call("Range", null, false, methodExpected);
                } else if ((id == GroovyTokenId.IDENTIFIER) ||
                        id.primaryCategory().equals("keyword") || (id == GroovyTokenId.DOT) ||
                        (id == GroovyTokenId.COLON) || (id == GroovyTokenId.SUPER_CTOR_CALL) ||
                        (id == GroovyTokenId.CTOR_CALL)) {

                    // We're building up a potential expression such as "Test::Unit" so continue looking
                    beginOffset = ts.offset();

                    continue;
                } else if ((id == GroovyTokenId.LPAREN) || (id == GroovyTokenId.LBRACE) ||
                        (id == GroovyTokenId.LBRACKET)) {
                    // It's an expression for example within a parenthesis, e.g.
                    // yield(^File.join())
                    // in this case we can do top level completion
                    // TODO: There are probably more valid contexts here
                    break;
                } else {
                    // Something else - such as "getFoo().x|" - at this point we don't know the type
                    // so we'll just return unknown
                    return Call.UNKNOWN;
                }
            }

            if (beginOffset < lastSeparatorOffset) {
                try {
                    String lhs = doc.getText(beginOffset, lastSeparatorOffset - beginOffset);

                    if (lhs.equals("super") || lhs.equals("this")) { // NOI18N

                        return new Call(lhs, lhs, false, true);
                    } else if (Character.isUpperCase(lhs.charAt(0))) {

                        String type = null;
                        // TODO: add also Groovy identifiers
                        if (org.openide.util.Utilities.isJavaIdentifier(lhs)) {
                            type = lhs;
                        }

                        return new Call(type, lhs, true, methodExpected);
                    } else {
                        return new Call(null, lhs, false, methodExpected);
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            } else {
                return Call.UNKNOWN;
            }
        }

        return Call.LOCAL;
    }
}
