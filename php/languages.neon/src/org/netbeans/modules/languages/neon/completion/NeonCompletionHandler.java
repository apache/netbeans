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
package org.netbeans.modules.languages.neon.completion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.neon.completion.NeonCompletionProposal.CompletionRequest;
import org.netbeans.modules.languages.neon.lexer.NeonTokenId;
import org.netbeans.modules.languages.neon.parser.NeonParser.NeonParserResult;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class NeonCompletionHandler implements CodeCompletionHandler {
    static final Set<NeonElement> SERVICE_CONFIG_OPTS = new HashSet<>();
    static {
        SERVICE_CONFIG_OPTS.add(NeonElement.Factory.create("setup", "setup:")); //NOI18N
        SERVICE_CONFIG_OPTS.add(NeonElement.Factory.create("class", "class: ${Class}")); //NOI18N
        SERVICE_CONFIG_OPTS.add(NeonElement.Factory.create("arguments", "arguments: [${argument}]")); //NOI18N
        SERVICE_CONFIG_OPTS.add(NeonElement.Factory.create("factory", "factory: ${Class}::${method}")); //NOI18N
        SERVICE_CONFIG_OPTS.add(NeonElement.Factory.create("autowired", "autowired: ${no}")); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        final List<CompletionProposal> completionProposals = new ArrayList<>();
        ParserResult parserResult = context.getParserResult();
        if (parserResult instanceof NeonParserResult) {
            NeonParserResult neonParserResult = (NeonParserResult) parserResult;
            CompletionRequest request = new CompletionRequest();
            int caretOffset = context.getCaretOffset();
            request.prefix = context.getPrefix();
            String properPrefix = getPrefix(neonParserResult, caretOffset, true);
            request.anchorOffset = caretOffset - (properPrefix == null ? 0 : properPrefix.length());
            request.parserResult = neonParserResult;
            NeonCompletionContext completionContext = NeonCompletionContextFinder.find(request.parserResult, caretOffset);
            completionContext.complete(completionProposals, request);
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
        return PrefixResolver.create(info, caretOffset, upToOffset).resolve();
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
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

    private static final class PrefixResolver {
        private final ParserResult info;
        private final int offset;
        private final boolean upToOffset;
        private String result = "";

        static PrefixResolver create(ParserResult info, int offset, boolean upToOffset) {
            return new PrefixResolver(info, offset, upToOffset);
        }

        private PrefixResolver(ParserResult info, int offset, boolean upToOffset) {
            this.info = info;
            this.offset = offset;
            this.upToOffset = upToOffset;
        }

        String resolve() {
            TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
            if (th != null) {
                processHierarchy(th);
            }
            return result;
        }

        private void processHierarchy(TokenHierarchy<?> th) {
            TokenSequence<NeonTokenId> tts = th.tokenSequence(NeonTokenId.language());
            if (tts != null) {
                processTopSequence(tts);
            }
        }

        private void processTopSequence(TokenSequence<NeonTokenId> tts) {
            tts.move(offset);
            if (tts.moveNext() || tts.movePrevious()) {
                processToken(tts);
            }
        }

        private void processToken(TokenSequence<NeonTokenId> ts) {
            if (ts.offset() == offset) {
                ts.movePrevious();
            }
            Token<NeonTokenId> token = ts.token();
            if (token != null) {
                processSelectedToken(ts);
            }
        }

        private void processSelectedToken(TokenSequence<NeonTokenId> ts) {
            NeonTokenId id = ts.token().id();
            if (isValidTokenId(id)) {
                createResult(ts);
            }
        }

        private void createResult(TokenSequence<NeonTokenId> ts) {
            if (upToOffset) {
                String text = ts.token().text().toString();
                result = text.substring(0, offset - ts.offset());
            }
        }

        private static boolean isValidTokenId(NeonTokenId id) {
            return NeonTokenId.NEON_LITERAL.equals(id) || NeonTokenId.NEON_BLOCK.equals(id) || NeonTokenId.NEON_VALUED_BLOCK.equals(id);
        }

    }

}
