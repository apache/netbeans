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
package org.netbeans.modules.css.lib;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

/**
 *
 * @author marekfukala
 */
public class ExtCss3Parser extends Css3Parser {

    //less css unit tests sets this
    static boolean isLessSource_unit_tests = false;
    static boolean isScssSource_unit_tests = false;
    
    private boolean isLessSource = isLessSource_unit_tests;
    private boolean isScssSource = isScssSource_unit_tests;
    
    public ExtCss3Parser(TokenStream input, NbParseTreeBuilder dbg, String mimeType) {
        super(input, dbg);        
        if(mimeType != null) {
            this.isLessSource = mimeType.equals("text/less");
            this.isScssSource = mimeType.equals("text/scss");
        }
    }

    public ExtCss3Parser(TokenStream input, int port, RecognizerSharedState state) {
        super(input, port, state);
    }

    public ExtCss3Parser(TokenStream input) {
        super(input);
    }

    @Override
    protected boolean isLessSource() {
        return isLessSource;
    }

    @Override
    protected boolean isScssSource() {
        return isScssSource;
    }
    
    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
        //disable the default token auto-insertion/deletion recovery
        throw new MismatchedTokenException(ttype, input);
    }

    @Override
    public void emitErrorMessage(String msg) {
        //no-op since errors are reported via NbParseTreeBuilder (an impl of parser's DebugEventListener)
    }

    /** Consume tokens until one matches the given token set */
    @Override
    public void consumeUntil(IntStream i, BitSet set) {
//        System.out.println("consumeUntil(" + set.toString(getTokenNames()) + ")");
        Token ttype;
        List<Token> skipped = new ArrayList<>();
        beginResync();
        try {
            while ((ttype = input.LT(1)) != null && ttype.getType() != Token.EOF && !set.member(ttype.getType())) {
//            System.out.println("consume during recover LA(1)=" + getTokenNames()[input.LA(1)]);
                input.consume();
                skipped.add(ttype);
            }
        } finally {
            endResync();
        }
        ((NbParseTreeBuilder) dbg).consumeSkippedTokens(skipped);
    }
}
