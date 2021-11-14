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
package org.netbeans.lib.java.lexer;

import java.util.Arrays;
import java.util.stream.Stream;
import org.netbeans.api.java.lexer.JavaMultiLineStringTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author jlahoda
 */
public class JavaMultiLineStringLexer implements Lexer<JavaMultiLineStringTokenId> {

    private final LexerInput input;

    private final TokenFactory<JavaMultiLineStringTokenId> tokenFactory;

    private int[] changePoints;
    private int changePointsPointer;

    public JavaMultiLineStringLexer(LexerRestartInfo<JavaMultiLineStringTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<JavaMultiLineStringTokenId> nextToken() {
        if (changePoints == null) {
            while (input.read() != LexerInput.EOF)
                ;
            String text = input.readText().toString();
            String[] lines = text.split("\n", -1);
            int indent = Arrays.stream(lines)
                               .mapToInt(this::leadingIndent)
                               .min()
                               .orElse(0);
            changePoints = Arrays.stream(lines).flatMap(l -> Stream.of(indent, l.length() - indent, 1)).mapToInt(i -> i).toArray();
            changePointsPointer = 0;
            input.backup(input.readLengthEOF());
        }
        if (changePointsPointer >= changePoints.length) {
            return null;
        }
        int len = 0;
        while (len == 0)
            len = changePoints[changePointsPointer++];
        for (int i = 0; i < len; i++) {
            if (input.read() == LexerInput.EOF && len == 1) {
                return null;
            }
        }
        JavaMultiLineStringTokenId id;
        switch (changePointsPointer % 3) {
            case 0: id = JavaMultiLineStringTokenId.NEWLINE; break;
            case 1: id = JavaMultiLineStringTokenId.INDENT; break;
            case 2: id = JavaMultiLineStringTokenId.TEXT; break;
            default: throw new IllegalStateException();
        }
        return tokenFactory.createToken(id);
    }

    private int leadingIndent(String line) {
        int indent = 0;

        for (int i = 0; i < line.length(); i++) { //TODO: code points
            if (Character.isWhitespace(line.charAt(i)))
                indent++;
            else
                break;
        }

        return indent;
    }


    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
        changePoints = null;
    }

}
