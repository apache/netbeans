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
package org.netbeans.modules.cnd.modelimpl.parser;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CXXParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 *
 */
public class CXXParserEx extends CXXParser {
    
    private static final boolean RECOVER_DECLARATIONS = true;
    private static final int RECOVERY_LIMIT = 3;
    private static final BitSet stopSet = new BitSet();
    static {
        stopSet.add(LCURLY);
        stopSet.add(RCURLY);
        stopSet.add(RPAREN);
        stopSet.add(LPAREN);
    }
        
    private final CXXParserActionEx action;
    private final boolean trace;
    private int level = 0; // indentation based trace
    private int recoveryCounter = 0;
    private final boolean reportErrors;

    public CXXParserEx(TokenStream input, CXXParserActionEx action) {
        super(input, action);
        this.action = action;
        this.trace = TraceFlags.TRACE_CPP_PARSER_RULES;
        this.reportErrors = TraceFlags.REPORT_PARSING_ERRORS;
    }
    
    private CsmParserProvider.ParserErrorDelegate errorDelegate;
    
    public void setErrorDelegate(CsmParserProvider.ParserErrorDelegate delegate) {
        errorDelegate = delegate;
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        CsmParserProvider.ParserError parserError;
        if (e instanceof MyRecognitionException) {
            MyRecognitionException ex = (MyRecognitionException) e;
            String hdr = getSourceName();
            if (APTUtils.isEOF(ex.getToken())) {
                parserError = new CsmParserProvider.ParserError(hdr+" "+ex.getMessage(), -1, -1, ex.getToken().getText(), true);
            } else {
                parserError = new CsmParserProvider.ParserError(hdr+":"+ex.getToken().getLine()+":"+ex.getToken().getColumn()+": error: "+ex.getMessage(), ex.getToken().getLine(), ex.getToken().getColumn(), ex.getToken().getText(), false); // NOI18N
            }
        } else {
            String hdr = getSourceName();
            String msg = getErrorMessage(e, tokenNames);
            parserError = new CsmParserProvider.ParserError(hdr+":"+e.line+":"+e.charPositionInLine+": error: "+msg, e.line, e.charPositionInLine, e.token.getText(), e.token.getType() == -1); // NOI18N
        }
        if (errorDelegate != null) {
            errorDelegate.onError(parserError);
        }
        if (reportErrors) {
            System.err.println(parserError);
        }
    }

    @Override
    public String getSourceName() {
        CsmFile currentFile = action.getCurrentFile();
        if (currentFile != null) {
            return currentFile.getAbsolutePath().toString();
        }
        return ""; // NOI18N
    }

    public int backtrackingLevel() {
        return state.backtracking;
    }        
     
    /**
     * Recover from an error found on the input stream. This is for NoViableAlt
     * and mismatched symbol exceptions. If you enable single token insertion
     * and deletion, this will usually not handle mismatched symbol exceptions
     * but there could be a mismatched token that the match() routine could not
     * recover from.
     */
    @Override
    public void recover(IntStream input, RecognitionException re) {
        BitSet followSet = computeErrorRecoverySet();
        if (state.lastErrorIndex == input.index()) {
            //<editor-fold defaultstate="collapsed" desc="Original Implementation">
            // uh oh, another error at same token index; must be a case
            // where LT(1) is in the recovery token set so nothing is
            // consumed; consume a single token so at least to prevent
            // an infinite loop; this is a failsafe.
            //input.consume();
            //</editor-fold>
            // our solution:
            if (recoveryCounter >= RECOVERY_LIMIT) {
                input.consume();
                recoveryCounter = 0;
                //followSet.orInPlace(stopSet);
            } else {
                recoveryCounter++;
            }
        } else {
            recoveryCounter = 0;
        }
        state.lastErrorIndex = input.index();
        beginResync();
        consumeUntil(input, followSet);
        endResync();
    }
    
    /**
     * Use the current stacked followset to work out the valid tokens that can
     * follow on from the current point in the parse, then recover by eating
     * tokens that are not a member of the follow set we compute.
     *
     * This method is used whenever we wish to force a sync, even though the
     * parser has not yet checked LA(1) for alt selection. This is useful in
     * situations where only a subset of tokens can begin a new construct (such
     * as the start of a new statement in a block) and we want to proactively
     * detect garbage so that the current rule does not exit on on an exception.
     *
     * We could override recover() to make this the default behavior but that is
     * too much like using a sledge hammer to crack a nut. We want finer grained
     * control of the recovery and error mechanisms.
     */
    @Override
    protected void sync_declaration_impl() {
        // Compute the followset that is in context wherever we are in the
        // rule chain/stack
        if (RECOVER_DECLARATIONS) {
            BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();
            syncToSet(follow);
        }
    }

    @Override
    protected void sync_member_impl() {
        if (RECOVER_DECLARATIONS) {
            BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();
            syncToSet(follow);
        }
    }

    @Override
    protected void sync_parameter_impl() {
        if (RECOVER_DECLARATIONS) {
            BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();
            syncToSet(follow);
        }
    }

    @Override
    protected void sync_statement_impl() {
        if (RECOVER_DECLARATIONS) {
            BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();
            syncToSet(follow);
        }
    }

    protected void syncToSet(BitSet follow) {
        beginResync();
        try {
            // Consume all tokens in the stream until we find a member of the follow
            // set, which means the next production should be guaranteed to be happy.
            while (!follow.member(input.LA(1))) {
                if (input.LA(1) == org.antlr.runtime.Token.EOF) {
                    // Looks like we didn't find anything at all that can help us here
                    // so we need to rewind to where we were and let normal error handling
                    // bail out.
                    return;
                }
                final Token token = ParserProviderImpl.convertToken(input.LT(1));
                reportError(new MyRecognitionException("Skip unexpected token at '"+token.getText()+"'", token)); // NOI18N
                input.consume();
                // Now here, because you are consuming some tokens, yu will probably want
                // to raise an error message such as "Spurious elements after the class member were discarded"
                // using whatever your override of displayRecognitionError() routine does to record
                // error messages. The exact error my depend on context etc.
            }
        } catch (Exception e) {
            // Just ignore any errors here, we will just let the recognizer
            // try to resync as normal - something must be very screwed.
        } finally {
            // Always release the mark we took
            endResync();
        }
    }
    
    @Override
    public void traceIn(String ruleName, int ruleIndex) {
        if (trace) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            super.traceIn(buf.toString() + ruleName, ruleIndex);
            level++;
        }
    }

    @Override
    public void traceOut(String ruleName, int ruleIndex) {
        if (trace) {
            level--;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append(' '); //NOI18N
            super.traceOut(buf.toString() + ruleName, ruleIndex);
        }
    }
    
    public static class MyRecognitionException extends RecognitionException {
        private final String message;
        private final Token myToken;
        public MyRecognitionException(String message, Token token) {
            this.message = message;
            myToken = token;
        }

        public Token getToken() {
            return myToken;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
