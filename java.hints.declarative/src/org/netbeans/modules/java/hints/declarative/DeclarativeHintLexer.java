/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Jan Lahoda
 */
class DeclarativeHintLexer implements Lexer<DeclarativeHintTokenId> {

    private final LexerInput input;
    private final TokenFactory<DeclarativeHintTokenId> fact;

    public DeclarativeHintLexer(LexerRestartInfo<DeclarativeHintTokenId> info) {
        input = info.input();
        fact  = info.tokenFactory();
    }

    public Token<DeclarativeHintTokenId> nextToken() {
        int read = input.read();

        if (read == LexerInput.EOF) {
            return null;
        }

        int whitespaceLength = 0;
        
        if (Character.isWhitespace(read)) {
            while ((read != LexerInput.EOF) && Character.isWhitespace((char) read)) {
                read = input.read();
            }

            if (read == LexerInput.EOF) {
                return fact.createToken(DeclarativeHintTokenId.WHITESPACE);
            }

            whitespaceLength = input.readLength() - 1;
        }

        while (read != LexerInput.EOF) {
            Matcher variableMatcher = VARIABLE_RE.matcher(input.readText());

            if (variableMatcher.find()) {
                int start = variableMatcher.start();

                if (start == 0) {
                    Matcher m;

                    while ((read = input.read()) != LexerInput.EOF && (m = VARIABLE_RE.matcher(input.readText())).find()) {
                        if (m.end() < input.readLength())
                            break;
                    }

                    if (read != LexerInput.EOF) {
                        input.backup(1);
                    }

                    return fact.createToken(DeclarativeHintTokenId.VARIABLE);
                }

                if (whitespaceLength == start) {
                    input.backup(input.readLength() - whitespaceLength);
                    return fact.createToken(DeclarativeHintTokenId.WHITESPACE);
                }

                input.backup(input.readLength() - start);

                return fact.createToken(DeclarativeHintTokenId.JAVA_SNIPPET);
            }

            if (input.readLength() > 1) {
                String inputString = input.readText().toString();
                
                for (Entry<String, DeclarativeHintTokenId> e : BLOCK_TOKEN_START.entrySet()) {
                    if (!inputString.substring(0, inputString.length() - 1).endsWith(e.getKey()))
                        continue;

                    input.backup(1);
                    
                    Token<DeclarativeHintTokenId> preread = resolvePrereadText(e.getKey().length(), whitespaceLength);

                    if (preread != null) {
                        return preread;
                    }

                    return readBlockToken(e.getValue(), BLOCK_TOKEN_END.get(e.getValue()));
                }
                
                Token<DeclarativeHintTokenId> t = testToken(inputString, whitespaceLength, false);

                if (t != null) {
                    return t;
                }

            }

            read = input.read();
        }

        Token<DeclarativeHintTokenId> t = testToken(input.readText().toString(), whitespaceLength, true);

        if (t != null) {
            return t;
        }

        return fact.createToken(DeclarativeHintTokenId.JAVA_SNIPPET);
    }

    public Object state() {
        return null;
    }

    public void release() {}

    private Token<DeclarativeHintTokenId> testToken(String toTest, int whitespaceLength, boolean eof) {
        if (toTest.length() < 2 && !eof) return null;

        DeclarativeHintTokenId id = null;
        boolean exact = false;
        int backup = (-1);
        int add = eof ? 0 : 1;
        String snip = toTest.substring(0, toTest.length() - add);
        String lastImage = "";
        
        for (Entry<String, DeclarativeHintTokenId> e : TOKENS.entrySet()) {
            int i = snip.indexOf(e.getKey());
            if (i != (-1) && (snip.length() - i + 1 > backup || e.getKey().startsWith(lastImage))) {
                id = e.getValue();
                backup = snip.length() - i + 1;
                exact = i == 0;
                lastImage = e.getKey();
            }

            if (!eof) {
                for (int c = 1; c <= e.getKey().length(); c++) {
                    if (toTest.endsWith(e.getKey().substring(e.getKey().length() - c))) {
                        return null;
                    }
                }
            }
        }

        if (id == null) {
            return null;
        }

        if (exact) {
            if (input.readLength() - lastImage.length() + (eof ? 1 : 0) > 0)
                input.backup(input.readLength() - lastImage.length() + (eof ? 1 : 0));
            return fact.createToken(id);
        }
        
        Token<DeclarativeHintTokenId> t = resolvePrereadText(backup, whitespaceLength);

        if (t != null) {
            return t;
        } else {
            if (!eof)
                input.backup(1);
            
            return fact.createToken(id);
        }
    }

    private Token<DeclarativeHintTokenId> resolvePrereadText(int backupLength, int whitespaceLength) {
        if (whitespaceLength > 0) {
            if (input.readLengthEOF() == whitespaceLength + backupLength) {
                input.backup(input.readLengthEOF() - whitespaceLength);

                return fact.createToken(DeclarativeHintTokenId.WHITESPACE);
            } else {
                input.backup(backupLength);

                return fact.createToken(DeclarativeHintTokenId.JAVA_SNIPPET);
            }
        } else {
            if (input.readLengthEOF() == backupLength) {
                return null;
            } else {
                input.backup(backupLength);

                return fact.createToken(DeclarativeHintTokenId.JAVA_SNIPPET);
            }
        }
    }

    private Token<DeclarativeHintTokenId> readBlockToken(DeclarativeHintTokenId tokenId, String tokenEnd) {
        while (input.read() != LexerInput.EOF && !input.readText().toString().endsWith(tokenEnd))
            ;

        return fact.createToken(tokenId);
    }

    private static final Pattern DISPLAY_NAME_RE = Pattern.compile("'[^']*':");
    private static final Pattern VARIABLE_RE = Pattern.compile("\\$[A-Za-z0-9_$]+");

    private static final Map<String, DeclarativeHintTokenId> TOKENS;
    private static final Map<String, DeclarativeHintTokenId> BLOCK_TOKEN_START;
    private static final Map<DeclarativeHintTokenId, String> BLOCK_TOKEN_END;

    static {
        Map<String, DeclarativeHintTokenId> map = new HashMap<String, DeclarativeHintTokenId>();

        map.put("=>", DeclarativeHintTokenId.LEADS_TO);
        map.put("::", DeclarativeHintTokenId.DOUBLE_COLON);
        map.put("&&", DeclarativeHintTokenId.AND);
        map.put("!", DeclarativeHintTokenId.NOT);
        map.put(";;", DeclarativeHintTokenId.DOUBLE_SEMICOLON);
        map.put("%%", DeclarativeHintTokenId.DOUBLE_PERCENT);
        map.put("instanceof", DeclarativeHintTokenId.INSTANCEOF);
        map.put("otherwise", DeclarativeHintTokenId.OTHERWISE);
        map.put(":", DeclarativeHintTokenId.COLON);

        TOKENS = Collections.unmodifiableMap(map);

        Map<String, DeclarativeHintTokenId> blockStartMap = new HashMap<String, DeclarativeHintTokenId>();

        blockStartMap.put("/*", DeclarativeHintTokenId.BLOCK_COMMENT);
        blockStartMap.put("//", DeclarativeHintTokenId.LINE_COMMENT);
        blockStartMap.put("<?", DeclarativeHintTokenId.JAVA_BLOCK);
        blockStartMap.put("<!", DeclarativeHintTokenId.OPTIONS);
        blockStartMap.put("'", DeclarativeHintTokenId.CHAR_LITERAL);
        blockStartMap.put("\"", DeclarativeHintTokenId.STRING_LITERAL);

        BLOCK_TOKEN_START = Collections.unmodifiableMap(blockStartMap);

        Map<DeclarativeHintTokenId, String> blockEndMap = new HashMap<DeclarativeHintTokenId, String>();

        blockEndMap.put(DeclarativeHintTokenId.BLOCK_COMMENT, "*/");
        blockEndMap.put(DeclarativeHintTokenId.LINE_COMMENT, "\n");
        blockEndMap.put(DeclarativeHintTokenId.JAVA_BLOCK, "?>");
        blockEndMap.put(DeclarativeHintTokenId.OPTIONS, ">");
        blockEndMap.put(DeclarativeHintTokenId.CHAR_LITERAL, "'");
        blockEndMap.put(DeclarativeHintTokenId.STRING_LITERAL, "\"");

        BLOCK_TOKEN_END = Collections.unmodifiableMap(blockEndMap);
    }
}
