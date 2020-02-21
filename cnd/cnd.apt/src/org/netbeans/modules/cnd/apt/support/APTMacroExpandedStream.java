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

package org.netbeans.modules.cnd.apt.support;

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.RecognitionException;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.MacroExpandedToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public class APTMacroExpandedStream extends APTExpandedStream {
    private final boolean emptyExpansionAsComment;
    private int generatedTokens = 0;
    private final static int EOF_MARKER = -1;
    private final static int MAX_GENERATED_TOKENS = Integer.getInteger("apt.limit.tokens", 8000000); // NOI18N
    private final static int MIN_REPORTED_TOKENS = Integer.getInteger("apt.report.tokens", Integer.MAX_VALUE); // NOI18N
    
    public APTMacroExpandedStream(TokenStream stream, APTMacroCallback callback, boolean emptyExpansionAsComment) {
        super(stream, callback);
        this.emptyExpansionAsComment = emptyExpansionAsComment;
    }

    @Override
    protected APTTokenStream createMacroBodyWrapper(APTToken token, APTMacro macro) throws TokenStreamException, RecognitionException {
        APTTokenStream origExpansion = super.createMacroBodyWrapper(token, macro);
        APTToken last = getLastExtractedParamRPAREN();
        if (last == null) {
            last = token;
        }
        APTTokenStream expandedMacros = new MacroExpandWrapper(token, origExpansion, last, emptyExpansionAsComment);
        return expandedMacros;
    }   

    @Override
    public APTToken nextToken() {
        if (generatedTokens == EOF_MARKER) {
            return APTUtils.EOF_TOKEN;
        }
        if (++generatedTokens > MAX_GENERATED_TOKENS) {
            APTUtils.LOG.log(Level.SEVERE, "stop ({0} is too much) generating tokens {1}", new Object[]{MAX_GENERATED_TOKENS, Thread.currentThread().getName()});
            generatedTokens = EOF_MARKER;
            return APTUtils.EOF_TOKEN;
        }
        APTToken token = super.nextToken();
        if (token == APTUtils.EOF_TOKEN) {
            if (generatedTokens > MIN_REPORTED_TOKENS) {
                APTUtils.LOG.log(Level.SEVERE, "generated {0} tokens for {1}", new Object[]{generatedTokens, Thread.currentThread().getName()});
            }
            generatedTokens = EOF_MARKER;
        }
        return token;
    }
    
    private static final class MacroExpandWrapper implements TokenStream, APTTokenStream {
        private final APTToken expandedFrom;
        private final APTTokenStream expandedMacros;
        private final APTToken endOffsetToken;
        private boolean firstToken = true;
        private final boolean emptyExpansionAsComment;
        
        public MacroExpandWrapper(APTToken expandedFrom, APTTokenStream expandedMacros, APTToken endOffsetToken, boolean emptyExpansionAsComment) {
            this.expandedFrom = expandedFrom;
            this.expandedMacros = expandedMacros;
            assert endOffsetToken != null : "end offset token must be valid";
            this.endOffsetToken = endOffsetToken;
            this.emptyExpansionAsComment = emptyExpansionAsComment;
        }
        
        @Override
        public APTToken nextToken() {
            APTToken expandedTo = expandedMacros.nextToken();
            if (emptyExpansionAsComment && firstToken && APTUtils.isEOF(expandedTo)) {
                // adding empty comment as expansion of empty macro
                expandedTo = new APTCommentToken();
                expandedTo.setType(APTTokenTypes.COMMENT);
            }
            firstToken = false;
            APTToken outToken = new MacroExpandedToken(expandedFrom, expandedTo, endOffsetToken);
            return outToken;
        }        
    }
}
