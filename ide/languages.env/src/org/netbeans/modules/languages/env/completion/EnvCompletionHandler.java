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
package org.netbeans.modules.languages.env.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.languages.env.EnvKeyHandle;
import org.netbeans.modules.languages.env.lexer.EnvTokenId;
import org.netbeans.modules.languages.env.parser.EnvParserResult;
import org.openide.filesystems.FileObject;

public class EnvCompletionHandler implements CodeCompletionHandler2 {

    @Override
    public Documentation documentElement(ParserResult info, ElementHandle element, Callable<Boolean> cancel) {
        return null;
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {

        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }

        if (!(context.getParserResult() instanceof EnvParserResult)) {
            return CodeCompletionResult.NONE;
        }
       
        EnvParserResult parserResult = (EnvParserResult) context.getParserResult();
              
        //only interpolation are relevant
        if (parserResult.getInterpolationOccurences().isEmpty()) {
           return CodeCompletionResult.NONE; 
        }

        boolean isInInterpolationContext = false;
        
        for (Map.Entry<String, List<OffsetRange>> entry : parserResult.getInterpolationOccurences().entrySet()) {
            for (OffsetRange range : entry.getValue()) {
                if (range.containsInclusive(context.getCaretOffset())) {
                    isInInterpolationContext = true;
                    break;
                }
            }
        }

        if (!isInInterpolationContext) {
            return CodeCompletionResult.NONE; 
        }
        
        int offset = context.getCaretOffset();
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();

        final List<CompletionProposal> completionProposals = new ArrayList<>();

        String contextPrefix = context.getPrefix();
        
        for (Map.Entry<String, OffsetRange> entry : parserResult.getDefinedKeys().entrySet()) {
            if (entry.getValue().getEnd() > offset) {
                continue;
            }
            if (entry.getKey().startsWith(contextPrefix)) {
                int anchorOffset = computeAnchorOffset(contextPrefix, offset);
                EnvKeyHandle handle = new EnvKeyHandle(entry.getKey(), fo);
                completionProposals.add(new KeyCompletionProposal(handle, anchorOffset));
            }
        }

        return new DefaultCompletionResult(completionProposals, false);
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return PrefixResolver.create(info, caretOffset).resolve();
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        QueryType result = QueryType.ALL_COMPLETION;
        if (typedText.length() == 0 || typedText.isBlank()) {
            result = QueryType.NONE;
        }

        return result;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private int computeAnchorOffset(@NonNull String prefix, int offset) {
        return offset - prefix.length();
    }

    private static final class PrefixResolver {

        private final ParserResult info;
        private final int offset;

        static PrefixResolver create(ParserResult info, int offset) {
            return new PrefixResolver(info, offset);
        }

        private PrefixResolver(ParserResult info, int offset) {
            this.info = info;
            this.offset = offset;
        }

        private String resolve() {
            AbstractDocument doc = (AbstractDocument) info.getSnapshot().getSource().getDocument(false);
            doc.readLock();
            try {
                TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                TokenSequence<?> ts = th.tokenSequence();
                ts.move(offset);
                ts.movePrevious();
                ts.moveNext();
                Token<?> token = ts.token();
                String tokenText = token.text().toString();

                if (token.id().equals(EnvTokenId.INTERPOLATION_OPERATOR)) {
                    if (tokenText.equals("{")) { //NOI18N
                        ts.moveNext();
                        token = ts.token();
                    } else {
                        ts.movePrevious();
                        token = ts.token();
                    }

                    if (token.id().equals(EnvTokenId.INTERPOLATION_OPERATOR)) {
                        return token.text().toString();
                    }
                }

                return null;
            } finally {
                doc.readUnlock();
            }
        }
    }
}
