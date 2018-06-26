/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
