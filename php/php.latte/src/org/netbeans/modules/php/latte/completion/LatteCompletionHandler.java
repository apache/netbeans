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
package org.netbeans.modules.php.latte.completion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.netbeans.modules.php.latte.completion.LatteCompletionProposal.CompletionRequest;
import org.netbeans.modules.php.latte.completion.LatteElement.HelperParameter;
import org.netbeans.modules.php.latte.completion.LatteElement.Parameter;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCompletionHandler implements CodeCompletionHandler2 {
    private static final Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<>(
            Arrays.asList('=', ';', '+', '-', '*', '%', '(', ')', '[', ']', '{', '}', '?', ' ', '\t', '\n'));
    private static final Logger LOGGER = Logger.getLogger(LatteCompletionHandler.class.getName());
    private static URL documentationUrl = null;
    static {
        try {
            documentationUrl = new URL("http://doc.nette.org/"); //NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }
    static final Set<LatteElement> MACROS = new HashSet<>();
    static {
        MACROS.add(LatteElement.MacroFactory.create("link", "Presenter:action", "link ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("plink", "Presenter:action", "plink ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("continueIf", "true", "continueIf ${true}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("breakIf", "true", "breakif ${true}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("if", "true", "if ${true}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("else")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("elseif", "true", "elseif ${true}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("ifset", "$var", "ifset ${var}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("ifset", "#block", "ifset #${block}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("elseifset", "$var", "elseifset ${var}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("ifCurrent", "Presenter:action", "ifCurrent ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("for", "init; cond; exec", "for ${init}; ${cond}; ${exec}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("foreach", "$array as $item", "foreach ${array} as ${item}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("while", "true", "while ${true}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("include", "'file.latte'", "include '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("include", "#block", "include #{block}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("extends", "'file.latte'", "extends '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("layout", "'file.latte'", "layout '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("control", "name", "control ${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("cache", "$key", "cache ${key}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("snippet", "$name", "snippet ${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("block", "#name", "block #${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("define", "#name", "define #${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("includeblock", "'file.latte'", "includeblock '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("contentType", "$type", "contentType ${type}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("status", "$code", "status ${code}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("capture", "$var", "capture ${var}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("assign")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("default", "$name = $value", "default ${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("var", "$name = $value", "var ${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("dump", "$var", "dump ${var}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("syntax", "mode", "syntax ${mode}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("use", "Class", "use ${Class}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("form", "$name", "form ${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("label", "$name", "label ${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("input", "$name", "input ${name}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("debugbreak", "$cond", "debugbreak ${cond}")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("l")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("r")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("first")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("last")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("sep")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("_")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("!")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("!_")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("=")); //NOI18N
        MACROS.add(LatteElement.MacroFactory.create("#")); //NOI18N
    }

    static final Set<LatteElement> END_MACROS = new HashSet<>();
    static {
        END_MACROS.add(LatteElement.MacroFactory.createEnd("if")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("ifset")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("ifCurrent")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("for")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("foreach")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("while")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("first")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("last")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("sep")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("capture")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("cache")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("syntax")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("_")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("block")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("form")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("label")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("snippet")); //NOI18N
        END_MACROS.add(LatteElement.MacroFactory.createEnd("define")); //NOI18N
    }

    static final Set<LatteElement> HELPERS = new HashSet<>();
    static {
        HELPERS.add(LatteElement.HelperFactory.create("truncate", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("append", "'…'")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("substr", Arrays.asList(new Parameter[] {//NOI18N
            new HelperParameter("offset"),  //NOI18N
            new HelperParameter("length", "stringLength")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("trim", Arrays.asList(new Parameter[] {new HelperParameter("charlist", "' \\t\\n\\r\\0\\x0B\\xC2\\xA0'")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("striptags")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("strip")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("webalize", Arrays.asList(new Parameter[] {//NOI18N
            new HelperParameter("charlist", "NULL"),  //NOI18N
            new HelperParameter("lower", "true")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("toAscii")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("indent", Arrays.asList(new Parameter[] {new HelperParameter("level", "1"), new HelperParameter("char", "'\\t'")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("replace", Arrays.asList(new Parameter[] {new HelperParameter("search"), new HelperParameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("replaceRE", Arrays.asList(new Parameter[] {new HelperParameter("pattern"), new HelperParameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("padLeft", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("padRight", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("repeat", Arrays.asList(new Parameter[] {new HelperParameter("count")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("implode", Arrays.asList(new Parameter[] {new HelperParameter("glue", "''")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("nl2br")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("lower")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("upper")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("firstUpper")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("capitalize")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("date", Arrays.asList(new Parameter[] {new HelperParameter("'format'")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("number", Arrays.asList(new Parameter[] {//NOI18N
            new HelperParameter("decimals", "0"), //NOI18N
            new HelperParameter("decPoint", "'.'")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("bytes", Arrays.asList(new Parameter[] {new HelperParameter("precision", "2")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("dataStream", Arrays.asList(new Parameter[] {new HelperParameter("mimetype", "NULL")}))); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("url")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("length")); //NOI18N
        HELPERS.add(LatteElement.HelperFactory.create("null")); //NOI18N
    }

    static final Set<LatteElement> KEYWORDS = new HashSet<>();
    static {
        KEYWORDS.add(LatteElement.KeywordFactory.create("true")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("false")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("null")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("and")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("or")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("xor")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("clone")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("new")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("instanceof")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("return")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("continue")); //NOI18N
        KEYWORDS.add(LatteElement.KeywordFactory.create("break")); //NOI18N
    }

    static final Set<LatteElement> ITERATOR_FIELD_ITEMS = new HashSet<>();
    static {
        ITERATOR_FIELD_ITEMS.add(LatteElement.IteratorItemFactory.create("first")); //NOI18N
        ITERATOR_FIELD_ITEMS.add(LatteElement.IteratorItemFactory.create("last")); //NOI18N
        ITERATOR_FIELD_ITEMS.add(LatteElement.IteratorItemFactory.create("counter")); //NOI18N
        ITERATOR_FIELD_ITEMS.add(LatteElement.IteratorItemFactory.create("odd")); //NOI18N
        ITERATOR_FIELD_ITEMS.add(LatteElement.IteratorItemFactory.create("even")); //NOI18N
    }

    static final Set<LatteElement> ITERATOR_METHOD_ITEMS = new HashSet<>();
    static {
        ITERATOR_METHOD_ITEMS.add(LatteElement.IteratorItemFactory.create("isFirst()")); //NOI18N
        ITERATOR_METHOD_ITEMS.add(LatteElement.IteratorItemFactory.create("isLast()")); //NOI18N
        ITERATOR_METHOD_ITEMS.add(LatteElement.IteratorItemFactory.create("getCounter()")); //NOI18N
        ITERATOR_METHOD_ITEMS.add(LatteElement.IteratorItemFactory.create("isOdd()")); //NOI18N
        ITERATOR_METHOD_ITEMS.add(LatteElement.IteratorItemFactory.create("isEven()")); //NOI18N
    }

    static final Set<LatteElement> DEFAULT_VARIABLES = new HashSet<>();
    static {
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$control")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$presenter")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$user")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$netteHttpResponse")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$netteCacheStorage")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$baseUri")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$basePath")); //NOI18N
        DEFAULT_VARIABLES.add(LatteElement.VariableFactory.create("$flashes")); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        final List<CompletionProposal> completionProposals = new ArrayList<>();
        ParserResult parserResult = context.getParserResult();
        if (parserResult instanceof LatteParserResult) {
            LatteParserResult latteParserResult = (LatteParserResult) parserResult;
            CompletionRequest request = new CompletionRequest();
            int caretOffset = context.getCaretOffset();
            request.prefix = context.getPrefix();
            String properPrefix = getPrefix(latteParserResult, caretOffset, true);
            request.anchorOffset = caretOffset - (properPrefix == null ? 0 : properPrefix.length());
            request.parserResult = latteParserResult;
            LatteCompletionContext completionContext = LatteCompletionContextFinder.find(request.parserResult, caretOffset);
            completionContext.complete(completionProposals, request);
        }
        return new DefaultCompletionResult(completionProposals, false);
    }

    @Override
    public Documentation documentElement(ParserResult info, ElementHandle element, Callable<Boolean> cancel) {
        Documentation result = null;
        if (element instanceof LatteElement) {
            result = Documentation.create(((LatteElement) element).getDocumentationText(), documentationUrl);
        }
        return result;
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
                TokenSequence<? extends LatteMarkupTokenId> ts = LatteLexerUtils.getLatteMarkupTokenSequence(document, offset);
                if (ts == null) {
                    result = QueryType.STOP;
                }
            }
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

    private static final class PrefixResolver {
        private static final String VARIABLE_PREFIX = "$"; //NOI18N
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
            TokenSequence<LatteTopTokenId> tts = th.tokenSequence(LatteTopTokenId.language());
            if (tts != null) {
                processTopSequence(tts);
            }
        }

        private void processTopSequence(TokenSequence<LatteTopTokenId> tts) {
            tts.move(offset);
            if (tts.moveNext() || tts.movePrevious()) {
                TokenSequence<LatteMarkupTokenId> embedded = tts.embedded(LatteMarkupTokenId.language());
                if (embedded == null && tts.movePrevious()) {
                    embedded = tts.embedded(LatteMarkupTokenId.language());
                }
                processSequence(embedded);
            }
        }

        private void processSequence(TokenSequence<LatteMarkupTokenId> ts) {
            if (ts != null) {
                processValidSequence(ts);
            }
        }

        private void processValidSequence(TokenSequence<LatteMarkupTokenId> ts) {
            ts.move(offset);
            if (ts.moveNext() || ts.movePrevious()) {
                processToken(ts);
            }
        }

        private void processToken(TokenSequence<LatteMarkupTokenId> ts) {
            if (ts.offset() == offset) {
                ts.movePrevious();
            }
            Token<LatteMarkupTokenId> token = ts.token();
            if (token != null) {
                processSelectedToken(ts);
            }
        }

        private void processSelectedToken(TokenSequence<LatteMarkupTokenId> ts) {
            if (isValidTokenId(ts.token())) {
                createResult(ts);
            }
        }

        private void createResult(TokenSequence<LatteMarkupTokenId> ts) {
            if (upToOffset) {
                String text = ts.token().text().toString();
                int endIndex = offset - ts.offset();
                if (endIndex <= text.length()) {
                    result = text.substring(0, endIndex);
                }
            }
        }

        private static boolean isValidTokenId(Token<LatteMarkupTokenId> token) {
            assert token != null;
            LatteMarkupTokenId id = token.id();
            return LatteMarkupTokenId.T_SYMBOL.equals(id) || LatteMarkupTokenId.T_VARIABLE.equals(id)
                    || LatteMarkupTokenId.T_MACRO_START.equals(id) || LatteMarkupTokenId.T_MACRO_END.equals(id)
                    || LatteMarkupTokenId.T_ERROR.equals(id) || (LatteMarkupTokenId.T_CHAR.equals(id) && VARIABLE_PREFIX.equals(token.text()));
        }

    }

}