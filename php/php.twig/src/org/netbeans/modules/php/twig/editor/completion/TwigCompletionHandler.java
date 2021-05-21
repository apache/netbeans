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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.completion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.twig.editor.completion.TwigCompletionProposal.CompletionRequest;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentationFactory.FilterDocumentationFactory;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentationFactory.FunctionDocumentationFactory;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentationFactory.OperatorDocumentationFactory;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentationFactory.TagDocumentationFactory;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentationFactory.TestDocumentationFactory;
import org.netbeans.modules.php.twig.editor.completion.TwigElement.Parameter;
import org.netbeans.modules.php.twig.editor.lexer.TwigBlockTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigLexerUtils;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigVariableTokenId;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;

public class TwigCompletionHandler implements CodeCompletionHandler2 {
    private static final Logger LOGGER = Logger.getLogger(TwigCompletionHandler.class.getName());
    private static URL documentationUrl = null;
    static {
        try {
            documentationUrl = new URL("http://twig.sensiolabs.org/documentation"); //NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }
    private static final Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<>(
            Arrays.asList('=', ';', '+', '-', '*', '/', '%', '(', ')', '[', ']', '{', '}', '?', ' ', '\t', '\n'));

    private static final Set<TwigElement> TAGS = new HashSet<>();
    static {
        TwigDocumentationFactory documentationFactory = TagDocumentationFactory.getInstance();
        TAGS.add(TwigElement.Factory.create("autoescape", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endautoescape", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("block", documentationFactory, "block ${name}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endblock", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("do", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("embed", documentationFactory, "embed \"${template.twig}\"")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endembed", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("extends", documentationFactory, "extends \"${template.twig}\"")); //NOI18N
        TAGS.add(TwigElement.Factory.create("filter", documentationFactory, "filter ${name}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endfilter", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("flush", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("for", documentationFactory, "for ${item} in ${array}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endfor", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("from", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("if", documentationFactory, "if ${true}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("else", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("elseif", documentationFactory, "elseif ${true}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endif", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("import", documentationFactory, "import '${page.html}' as ${alias}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("include", documentationFactory, "include '${page.html}'")); //NOI18N
        TAGS.add(TwigElement.Factory.create("macro", documentationFactory, "macro ${name}()")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endmacro", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("raw", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endraw", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("verbatim", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endverbatim", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("sandbox", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endsandbox", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("set", documentationFactory, "set ${variable}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endset", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("spaceless", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endspaceless", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("use", documentationFactory, "use \"${page.html}\"")); //NOI18N
        TAGS.add(TwigElement.Factory.create("trans", documentationFactory)); //NOI18N
        TAGS.add(TwigElement.Factory.create("endtrans", documentationFactory)); //NOI18N
    }

    private static final Set<TwigElement> FILTERS = new HashSet<>();
    static {
        TwigDocumentationFactory documentationFactory = FilterDocumentationFactory.getInstance();
        FILTERS.add(TwigElement.Factory.create("abs", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("capitalize", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("convert_encoding", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'to'"), new Parameter("'from'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("date", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'format'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("date_modify", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'modifier'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("default", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'value'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("escape", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'html'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("format", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("var")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("join", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'separator'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("json_encode", documentationFactory, Collections.emptyList())); //NOI18N
        FILTERS.add(TwigElement.Factory.create("keys", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("length", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("lower", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("merge", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("array")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("nl2br", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("number_format", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("raw", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("replace", documentationFactory, Collections.emptyList())); //NOI18N
        FILTERS.add(TwigElement.Factory.create("reverse", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("slice", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("start"), new Parameter("length")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("sort", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("striptags", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("title", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("trim", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("upper", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("url_encode", documentationFactory, Collections.emptyList())); //NOI18N
        FILTERS.add(TwigElement.Factory.create("trans", documentationFactory)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("truncate", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("int")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create(
                "wordwrap",
                documentationFactory,
                Arrays.asList(new Parameter[] {new Parameter("width"), new Parameter("'break'"), new Parameter("cut")}))); //NOI18N
    }

    private static final Set<TwigElement> FUNCTIONS = new HashSet<>();
    static {
        TwigDocumentationFactory documentationFactory = FunctionDocumentationFactory.getInstance();
        FUNCTIONS.add(TwigElement.Factory.create(
                "attribute",
                documentationFactory,
                Arrays.asList(new Parameter[] {new Parameter("object"), new Parameter("method"), new Parameter("arguments", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("block", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'name'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("constant", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'name'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("cycle", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("array"), new Parameter("i")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create(
                "date",
                documentationFactory,
                Arrays.asList(new Parameter[] {new Parameter("'date'"), new Parameter("'timezone'", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("dump", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("variable", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("parent", documentationFactory, Collections.emptyList())); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("random", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'value'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create(
                "range",
                documentationFactory,
                Arrays.asList(new Parameter[] {new Parameter("start"), new Parameter("end"), new Parameter("step", Parameter.Need.OPTIONAL)}))); //NOI18N
    }

    private static final Set<TwigElement> TESTS = new HashSet<>();
    static {
        TwigDocumentationFactory documentationFactory = TestDocumentationFactory.getInstance();
        TESTS.add(TwigElement.Factory.create("constant", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("'const'")}))); //NOI18N
        TESTS.add(TwigElement.Factory.create("defined", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("divisibleby", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("number")}))); //NOI18N
        TESTS.add(TwigElement.Factory.create("empty", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("even", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("iterable", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("null", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("odd", documentationFactory)); //NOI18N
        TESTS.add(TwigElement.Factory.create("sameas", documentationFactory, Arrays.asList(new Parameter[] {new Parameter("variable")}))); //NOI18N
    }

    private static final Set<TwigElement> OPERATORS = new HashSet<>();
    static {
        TwigDocumentationFactory documentationFactory = OperatorDocumentationFactory.getInstance();
        OPERATORS.add(TwigElement.Factory.create("in", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("as", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("is", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("and", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("or", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("not", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-and", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-or", documentationFactory)); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-xor", documentationFactory)); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext codeCompletionContext) {
        final List<CompletionProposal> completionProposals = new ArrayList<>();
        ParserResult parserResult = codeCompletionContext.getParserResult();
        if (parserResult instanceof TwigParserResult) {
            TwigParserResult twigParserResult = (TwigParserResult) parserResult;
            CompletionRequest request = new CompletionRequest();
            request.prefix = codeCompletionContext.getPrefix();
            int caretOffset = codeCompletionContext.getCaretOffset();
            String properPrefix = getPrefix(twigParserResult, caretOffset, true);
            request.anchorOffset = caretOffset - (properPrefix == null ? 0 : properPrefix.length());
            request.parserResult = twigParserResult;
            request.context = TwigCompletionContextFinder.find(request.parserResult, caretOffset);
            doCompletion(completionProposals, request);
        }
        return new DefaultCompletionResult(completionProposals, false);
    }

    private void doCompletion(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        switch (request.context) {
            case FILTER:
                completeFilters(completionProposals, request);
                break;
            case BLOCK:
                completeAll(completionProposals, request);
                break;
            case VARIABLE:
                completeFilters(completionProposals, request);
                completeFunctions(completionProposals, request);
                completeTests(completionProposals, request);
                completeOperators(completionProposals, request);
                break;
            case ALL:
                completeAll(completionProposals, request);
                break;
            case NONE:
                break;
            default:
                completeAll(completionProposals, request);
        }
    }

    private void completeAll(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        completeTags(completionProposals, request);
        completeFilters(completionProposals, request);
        completeFunctions(completionProposals, request);
        completeTests(completionProposals, request);
        completeOperators(completionProposals, request);
    }

    private void completeTags(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement tag : TAGS) {
            if (startsWith(tag.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.TagCompletionProposal(tag, request));
            }
        }
    }

    private void completeFilters(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement parameterizedItem : FILTERS) {
            if (startsWith(parameterizedItem.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.FilterCompletionProposal(parameterizedItem, request));
            }
        }
    }

    private void completeFunctions(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement parameterizedItem : FUNCTIONS) {
            if (startsWith(parameterizedItem.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.FunctionCompletionProposal(parameterizedItem, request));
            }
        }
    }

    private void completeTests(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement test : TESTS) {
            if (startsWith(test.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.TestCompletionProposal(test, request));
            }
        }
    }

    private void completeOperators(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement operator : OPERATORS) {
            if (startsWith(operator.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.OperatorCompletionProposal(operator, request));
            }
        }
    }

    @Override
    public Documentation documentElement(ParserResult parserResult, ElementHandle elementHandle, Callable<Boolean> cancel) {
        Documentation result = null;
        if (elementHandle instanceof TwigElement) {
            result = Documentation.create(((TwigElement) elementHandle).getDocumentation().asText(), documentationUrl);
        }
        return result;
    }

    @Override
    public String document(ParserResult pr, ElementHandle eh) {
        return null;
    }

    @Override
    public ElementHandle resolveLink(String string, ElementHandle eh) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int offset, boolean upToOffset) {
        return PrefixResolver.create(info, offset, upToOffset).resolve();
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        QueryType result = QueryType.ALL_COMPLETION;
        if (typedText.length() == 0) {
            result = QueryType.NONE;
        } else {
            char lastChar = typedText.charAt(typedText.length() - 1);
            if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))) {
                result = QueryType.STOP;
            } else {
                Document document = component.getDocument();
                int offset = component.getCaretPosition();
                TokenSequence<? extends TokenId> ts = TwigLexerUtils.getTwigMarkupTokenSequence(document, offset);
                if (ts == null) {
                    result = QueryType.STOP;
                }
            }
        }
        return result;
    }

    @Override
    public String resolveTemplateVariable(String string, ParserResult pr, int i, String string1, Map map) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document dcmnt, int i, int i1) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult pr, int i, CompletionProposal cp) {
        return ParameterInfo.NONE;
    }

    private static boolean startsWith(String theString, String prefix) {
        return prefix.length() == 0 ? true : theString.toLowerCase().startsWith(prefix.toLowerCase());
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
            TokenSequence<TwigTopTokenId> tts = th.tokenSequence(TwigTopTokenId.language());
            if (tts != null) {
                processTopSequence(tts);
            }
        }

        private void processTopSequence(TokenSequence<TwigTopTokenId> tts) {
            tts.move(offset);
            if (tts.moveNext() || tts.movePrevious()) {
                TokenSequence<? extends TokenId> ts = tts.embedded(TwigBlockTokenId.language());
                if (ts == null) {
                    ts = tts.embedded(TwigVariableTokenId.language());
                }
                processSequence(ts);
            }
        }

        private void processSequence(TokenSequence<? extends TokenId> ts) {
            if (ts != null) {
                processValidSequence(ts);
            }
        }

        private void processValidSequence(TokenSequence<? extends TokenId> ts) {
            ts.move(offset);
            if (ts.moveNext() || ts.movePrevious()) {
                processToken(ts);
            }
        }

        private void processToken(TokenSequence<? extends TokenId> ts) {
            if (ts.offset() == offset) {
                ts.movePrevious();
            }
            Token<?> token = ts.token();
            if (token != null) {
                processSelectedToken(ts);
            }
        }

        private void processSelectedToken(TokenSequence<? extends TokenId> ts) {
            TokenId id = ts.token().id();
            if (isValidTokenId(id)) {
                createResult(ts);
            }
        }

        private void createResult(TokenSequence<? extends TokenId> ts) {
            if (upToOffset) {
                String text = ts.token().text().toString();
                result = text.substring(0, offset - ts.offset());
            }
        }

        private static boolean isValidTokenId(TokenId id) {
            return TwigBlockTokenId.T_TWIG_TAG.equals(id) || TwigBlockTokenId.T_TWIG_NAME.equals(id) || TwigVariableTokenId.T_TWIG_NAME.equals(id);
        }

    }

}
