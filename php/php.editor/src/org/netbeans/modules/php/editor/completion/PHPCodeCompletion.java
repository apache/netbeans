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
package org.netbeans.modules.php.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.CaseInsensitivePrefix;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.elements.AliasedElement.Trait;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.completion.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.php.editor.completion.CompletionContextFinder.KeywordCompletionType;
import static org.netbeans.modules.php.editor.completion.CompletionContextFinder.lexerToASTOffset;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.EnumCaseItem;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.FieldItem;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.MethodElementItem;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.TypeConstantItem;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.elements.VariableElementImpl;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ArrowFunctionScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.ParameterInfoSupport;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.VariablesScope;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements CodeCompletionHandler2 {

    // for unit tests
    static volatile PhpVersion PHP_VERSION = null;
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());

    private static enum UseType {
        TYPE,
        CONST,
        FUNCTION,
    };

    static final Map<String, KeywordCompletionType> PHP_KEYWORDS = new HashMap<>();

    static {
        PHP_KEYWORDS.put("use", KeywordCompletionType.SIMPLE); //NOI18N
        PHP_KEYWORDS.put("namespace", KeywordCompletionType.SIMPLE); //NOI18N
        PHP_KEYWORDS.put("class", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("trait", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("enum", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("const", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("continue", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("function", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("fn", KeywordCompletionType.SIMPLE); // NOI18N PHP 7.4
        PHP_KEYWORDS.put("new", KeywordCompletionType.SIMPLE); //NOI18N
        PHP_KEYWORDS.put("static", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("var", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("final", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("interface", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("instanceof", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("implements", KeywordCompletionType.SIMPLE); //NOI18N
        PHP_KEYWORDS.put("extends", KeywordCompletionType.SIMPLE); //NOI18N
        PHP_KEYWORDS.put("public", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("private", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("protected", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("abstract", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("readonly", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("clone", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("global", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("goto", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("throw", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("if", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("switch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("match", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("for", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("array", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("foreach", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("while", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("catch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("try", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("default", KeywordCompletionType.ENDS_WITH_COLON); //NOI18N
        PHP_KEYWORDS.put("default =>", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N PHP 8.0 match expression
        PHP_KEYWORDS.put("break", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("endif", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("endfor", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("endforeach", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("endwhile", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("endswitch", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("case", KeywordCompletionType.ENDS_WITH_COLON); //NOI18N
        PHP_KEYWORDS.put("and", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("as", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("declare", KeywordCompletionType.CURSOR_INSIDE_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("do", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("else", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("elseif", KeywordCompletionType.ENDS_WITH_BRACKETS_AND_CURLY_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("enddeclare", KeywordCompletionType.ENDS_WITH_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("or", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("xor", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
        PHP_KEYWORDS.put("finally", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS); //NOI18N
        PHP_KEYWORDS.put("yield", KeywordCompletionType.CURSOR_BEFORE_ENDING_SEMICOLON); //NOI18N
        PHP_KEYWORDS.put("yield from", KeywordCompletionType.ENDS_WITH_SPACE); //NOI18N
    }
    private static final String[] PHP_LANGUAGE_CONSTRUCTS_WITH_QUOTES = {
        "echo", "include", "include_once", "require", "require_once", "print" // NOI18N
    };
    private static final String[] PHP_LANGUAGE_CONSTRUCTS_WITH_PARENTHESES = {
        "die", "eval", "exit", "empty", "isset", "list", "unset" // NOI18N
    };
    private static final String[] PHP_LANGUAGE_CONSTRUCTS_WITH_SEMICOLON = {
        "return" // NOI18N
    };
    static final String PHP_CLASS_KEYWORD_THIS = "$this->"; //NOI18N
    static final String[] PHP_CLASS_KEYWORDS = {
        PHP_CLASS_KEYWORD_THIS, "self::", "parent::", "static::" //NOI18N
    };
    static final String[] PHP_STATIC_CLASS_KEYWORDS = {
        "self::", "parent::", "static::" //NOI18N
    };
    static final List<String> PHP_GLOBAL_CONST_KEYWORDS = Arrays.asList(
            "array", // NOI18N
            "new" // NOI18N
    );
    static final List<String> PHP_CLASS_CONST_KEYWORDS = Arrays.asList(
            "array", // NOI18N
            "self::", // NOI18N
            "parent::" // NOI18N
    );
    static final List<String> PHP_ATTRIBUTE_EXPRESSION_KEYWORDS = Arrays.asList(
            "new", // NOI18N
            "array", // NOI18N
            "self::", // NOI18N
            "parent::" // NOI18N
    );
    private static final List<String> PHP_MATCH_EXPRESSION_KEYWORDS = Arrays.asList(
            "function", // NOI18N
            "fn", // NOI18N
            "new", // NOI18N
            "static", // NOI18N
            "instanceof", // NOI18N
            "clone", // NOI18N
            "throw", // NOI18N
            "match", // NOI18N
            "array", // NOI18N
            "default =>", // NOI18N
            "and", // NOI18N
            "or", // NOI18N
            "xor" // NOI18N
    );
    private static final List<String> PHP_VISIBILITY_KEYWORDS = Arrays.asList(
            "public", // NOI18N
            "protected", // NOI18N
            "private" // NOI18N
    );
    private static final Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<>(
            Arrays.asList('=', ';', '+', '-', '*', '/',
            '%', '(', ')', '[', ']', '{', '}', '?'));
    private static final Collection<PHPTokenId> TOKENS_TRIGGERING_AUTOPUP_TYPES_WS =
            Arrays.asList(PHPTokenId.PHP_NEW, PHPTokenId.PHP_EXTENDS, PHPTokenId.PHP_IMPLEMENTS, PHPTokenId.PHP_INSTANCEOF);
    private static final List<String> INVALID_PROPOSALS_FOR_CLS_MEMBERS =
            Arrays.asList(new String[]{"__construct", "__destruct", "__call", "__callStatic",
                "__clone", "__get", "__invoke", "__isset", "__set", "__set_state",
                "__sleep", "__toString", "__unset", "__wakeup"}); //NOI18N
    private static final List<String> CLASS_CONTEXT_KEYWORD_PROPOSAL =
            Arrays.asList(new String[]{"abstract", "const", "function", "private", "final",
                "protected", "public", "static", "var", "readonly"}); //NOI18N
    private static final List<String> INTERFACE_CONTEXT_KEYWORD_PROPOSAL =
            Arrays.asList(new String[]{"const", "function", "public", "static"}); //NOI18N
    private static final List<String> INHERITANCE_KEYWORDS =
            Arrays.asList(new String[]{"extends", "implements"}); //NOI18N
    private static final String EXCEPTION_CLASS_NAME = "\\Exception"; // NOI18N
    private static final List<PHPTokenId> VALID_UNION_TYPE_TOKENS = Arrays.asList(
            PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.PHP_NS_SEPARATOR,
            PHPTokenId.PHP_TYPE_BOOL, PHPTokenId.PHP_TYPE_FLOAT, PHPTokenId.PHP_TYPE_INT, PHPTokenId.PHP_TYPE_STRING, PHPTokenId.PHP_TYPE_VOID,
            PHPTokenId.PHP_TYPE_OBJECT, PHPTokenId.PHP_TYPE_MIXED, PHPTokenId.PHP_SELF, PHPTokenId.PHP_PARENT, PHPTokenId.PHP_STATIC,
            PHPTokenId.PHP_NULL, PHPTokenId.PHP_FALSE, PHPTokenId.PHP_TRUE, PHPTokenId.PHP_ARRAY, PHPTokenId.PHP_ITERABLE, PHPTokenId.PHP_CALLABLE,
            PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END,
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END
    );
    private static final List<PHPTokenId> VALID_INTERSECTION_TYPE_TOKENS = Arrays.asList(
            PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.PHP_NS_SEPARATOR,
            PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END,
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END
    );
    private static final List<PHPTokenId> TYPE_TOKENS = Arrays.asList(
            PHPTokenId.PHP_CLASS, PHPTokenId.PHP_INTERFACE, PHPTokenId.PHP_TRAIT, PHPTokenId.PHP_ENUM
    );
    private boolean caseSensitive;
    private QuerySupport.Kind nameKind;

    @Override
    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
        long startTime = 0;
        if (LOGGER.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
        }

        BaseDocument doc = (BaseDocument) completionContext.getParserResult().getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return CodeCompletionResult.NONE;
        }

        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }

        // TODO: separate the code that uses informatiom from lexer
        // and avoid running the index/ast analysis under read lock
        // in order to improve responsiveness
        // doc.readLock();        //TODO: use token hierarchy from snapshot and not use read lock in CC #171702

        final PHPCompletionResult completionResult = new PHPCompletionResult(completionContext);
        ParserResult info = completionContext.getParserResult();
        final int caretOffset = completionContext.getCaretOffset();

        this.caseSensitive = completionContext.isCaseSensitive();
        this.nameKind = caseSensitive ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;

        PHPParseResult result = (PHPParseResult) info;

        if (result.getProgram() == null) {
            return CodeCompletionResult.NONE;
        }
        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return CodeCompletionResult.NONE;
        }

        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }
        CompletionContext context = CompletionContextFinder.findCompletionContext(info, caretOffset);
        LOGGER.log(Level.FINE, "CC context: {0}", context);

        if (context == CompletionContext.NONE) {
            return CodeCompletionResult.NONE;
        }

        PHPCompletionItem.CompletionRequest request = new PHPCompletionItem.CompletionRequest();
        request.context = context;
        QueryType queryType = completionContext.getQueryType();
        String prefix;
        // GH-4494 if query type is documentation, get a whole identifier as a prefix
        boolean upToOffset = queryType != QueryType.DOCUMENTATION;
        prefix = getPrefix(info, caretOffset, upToOffset, PrefixBreaker.WITH_NS_PARTS);
        String prefixUntilCaret = upToOffset ? prefix : getPrefix(info, caretOffset, true, PrefixBreaker.WITH_NS_PARTS); // GH-5881
        if (prefix == null
                || prefixUntilCaret == null
                || (queryType == QueryType.DOCUMENTATION && prefix.isEmpty())) {
            return CodeCompletionResult.NONE;
        }
        prefix = prefix.trim().isEmpty() ? completionContext.getPrefix() : prefix;
        // prefix for index search (used for group use, equals to the base NS (before curly open))
        String searchPrefix;
        switch (context) {
            case GROUP_USE_KEYWORD:
            case GROUP_USE_CONST_KEYWORD:
            case GROUP_USE_FUNCTION_KEYWORD:
                searchPrefix = getPrefix(info, findBaseNamespaceEnd(info, caretOffset), true, PrefixBreaker.WITH_NS_PARTS);
                break;
            case EXPRESSION: // no break
                if (prefix.startsWith("@")) { // NOI18N
                    prefix = prefix.substring(1);
                }
            default:
                searchPrefix = null;
                break;
        }
        request.extraPrefix = searchPrefix;

        int prefixLengthForAnchor = upToOffset ? prefix.length() : prefixUntilCaret.length();
        request.anchor = caretOffset
                // can't just use 'prefix.getLength()' here cos it might have been calculated with
                // the 'upToOffset' flag set to false
                - prefixLengthForAnchor;

        request.result = result;
        request.info = info;
        request.prefix = prefix;
        request.index = ElementQueryFactory.getIndexQuery(info);
        request.currentlyEditedFileURL = fileObject.toURL().toString();

        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }

        CodeStyle codeStyle;
        switch (context) {
            case DEFAULT_PARAMETER_VALUE:
                final CaseInsensitivePrefix nameKindPrefix = NameKind.caseInsensitivePrefix(request.prefix);
                autoCompleteKeywords(completionResult, request, Arrays.asList("array", "new")); //NOI18N
                autoCompleteNamespaces(completionResult, request);
                autoCompleteTypeNames(completionResult, request, null, true);
                if (CancelSupport.getDefault().isCancelled()) {
                    return CodeCompletionResult.NONE;
                }
                final ElementFilter forName = ElementFilter.forName(nameKindPrefix);
                final Model model = request.result.getModel();
                final Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);
                Set<ConstantElement> constants = request.index.getConstants(nameKindPrefix, aliasedNames, Trait.ALIAS);
                for (ConstantElement constant : forName.filter(constants)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return CodeCompletionResult.NONE;
                    }
                    completionResult.add(new PHPCompletionItem.ConstantItem(constant, request));
                }
                final EnclosingClass enclosingClass = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                if (enclosingClass != null) {
                    String clsName = enclosingClass.getClassName();
                    for (String classKeyword : PHP_STATIC_CLASS_KEYWORDS) {
                        if (classKeyword.toLowerCase().startsWith(request.prefix)) { //NOI18N
                            completionResult.add(new PHPCompletionItem.ClassScopeKeywordItem(clsName, classKeyword, request));
                        }
                    }
                }
                break;
            case NAMESPACE_KEYWORD:
                if (CancelSupport.getDefault().isCancelled()) {
                    return CodeCompletionResult.NONE;
                }
                Set<NamespaceElement> namespaces = request.index.getNamespaces(
                        NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix).toNotFullyQualified()));
                for (NamespaceElement namespace : namespaces) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return CodeCompletionResult.NONE;
                    }
                    completionResult.add(new PHPCompletionItem.NamespaceItem(namespace, request, QualifiedNameKind.QUALIFIED));
                }
                break;
            case GLOBAL:
                autoCompleteGlobals(completionResult, request);
                break;
            case MATCH_EXPRESSION:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteExpression(completionResult, request, PHP_MATCH_EXPRESSION_KEYWORDS);
                break;
            case ATTRIBUTE:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteAttribute(completionResult, request);
                break;
            case ATTRIBUTE_EXPRESSION:
                autoCompleteAttributeExpression(completionResult, request);
                break;
            case EXPRESSION:
                autoCompleteExpression(completionResult, request);
                break;
            case CONSTRUCTOR_PARAMETER_NAME:
                autoCompleteConstructorParameterName(completionResult, request);
                break;
            case CLASS_MEMBER_PARAMETER_NAME:
                autoCompleteExpression(completionResult, request);
                autoCompleteClassMethodParameterName(completionResult, request, false);
                break;
            case STATIC_CLASS_MEMBER_PARAMETER_NAME:
                autoCompleteExpression(completionResult, request);
                autoCompleteClassMethodParameterName(completionResult, request, true);
                break;
            case FUNCTION_PARAMETER_NAME:
                autoCompleteExpression(completionResult, request);
                autoCompleteFunctionParameterName(completionResult, request);
                break;
            case GLOBAL_CONST_EXPRESSION:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteTypeNames(completionResult, request, null, true);
                autoCompleteConstants(completionResult, request);
                autoCompleteKeywords(completionResult, request, PHP_GLOBAL_CONST_KEYWORDS);
                break;
            case CLASS_CONST_EXPRESSION: // no break
            case ENUM_CASE_EXPRESSION:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteTypeNames(completionResult, request, null, true);
                autoCompleteConstants(completionResult, request);
                autoCompleteKeywords(completionResult, request, PHP_CLASS_CONST_KEYWORDS);
                // NETBEANS-1855
                if (!request.prefix.contains("\\")) { // NOI18N
                    // e.g. const CONSTANT = \^Foo\Bar::CONSTANT;
                    autoCompleteClassConstants(completionResult, request);
                }
                break;
            case HTML:
            case OPEN_TAG:
                completionResult.add(new PHPCompletionItem.TagItem("<?php", 1, request)); //NOI18N
                completionResult.add(new PHPCompletionItem.TagItem("<?=", 2, request)); //NOI18N
                break;
            case NEW_CLASS:
                autoCompleteKeywords(completionResult, request, Arrays.asList("class")); //NOI18N
                autoCompleteNamespaces(completionResult, request);
                autoCompleteNewClass(completionResult, request);
                break;
            case CLASS_NAME:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteClassNames(completionResult, request, false);
                break;
            case INTERFACE_NAME:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteInterfaceNames(completionResult, request);
                break;
            case BACKING_TYPE:
                List<String> backingTypes = Type.getTypesForBackingType();
                for (String keyword : backingTypes) {
                    if (startsWith(keyword, request.prefix)) {
                        completionResult.add(new PHPCompletionItem.KeywordItem(keyword, request));
                    }
                }
                break;
            case GROUP_USE_KEYWORD:
                autoCompleteGroupUse(UseType.TYPE, completionResult, request);
                List<String> keywords = Arrays.asList("const", "function"); // NOI18N
                for (String keyword : keywords) {
                    if (startsWith(keyword, request.prefix)) {
                        completionResult.add(new PHPCompletionItem.KeywordItem(keyword, request));
                    }
                }
                break;
            case GROUP_USE_CONST_KEYWORD:
                autoCompleteGroupUse(UseType.CONST, completionResult, request);
                break;
            case GROUP_USE_FUNCTION_KEYWORD:
                autoCompleteGroupUse(UseType.FUNCTION, completionResult, request);
                break;
            case USE_KEYWORD:
                codeStyle = CodeStyle.get(request.result.getSnapshot().getSource().getDocument(caseSensitive));
                autoCompleteAfterUse(
                        UseType.TYPE,
                        completionResult,
                        request,
                        codeStyle.startUseWithNamespaceSeparator() ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.QUALIFIED);
                break;
            case USE_CONST_KEYWORD:
                codeStyle = CodeStyle.get(request.result.getSnapshot().getSource().getDocument(caseSensitive));
                autoCompleteAfterUse(
                        UseType.CONST,
                        completionResult,
                        request,
                        codeStyle.startUseWithNamespaceSeparator() ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.QUALIFIED);
                break;
            case USE_FUNCTION_KEYWORD:
                codeStyle = CodeStyle.get(request.result.getSnapshot().getSource().getDocument(caseSensitive));
                autoCompleteAfterUse(
                        UseType.FUNCTION,
                        completionResult,
                        request,
                        codeStyle.startUseWithNamespaceSeparator() ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.QUALIFIED);
                break;
            case USE_TRAITS:
                codeStyle = CodeStyle.get(request.result.getSnapshot().getSource().getDocument(caseSensitive));
                autoCompleteAfterUseTrait(
                        completionResult,
                        request,
                        codeStyle.startUseWithNamespaceSeparator() ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.QUALIFIED);
                break;
            case VISIBILITY_MODIFIER_OR_TYPE_NAME: // no break
                autoCompleteKeywords(completionResult, request, PHP_VISIBILITY_KEYWORDS);
                autoCompleteKeywords(completionResult, request, Arrays.asList("readonly")); // NOI18N
            case TYPE_NAME:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteTypeNames(completionResult, request);
                if (!isIntersectionType(info, caretOffset)) {
                    final ArrayList<String> typesForTypeName = new ArrayList<>(Type.getTypesForEditor());
                    if (isInType(request)) {
                        // add self and parent
                        typesForTypeName.addAll(Type.getSpecialTypesForType());
                    }
                    if (isNullableType(info, caretOffset)) {
                        // ?false, ?true is OK since PHP 8.2
                        typesForTypeName.remove(Type.NULL);
                    }
                    if (isUnionType(info, caretOffset)) {
                        typesForTypeName.remove(Type.MIXED);
                    }
                    autoCompleteKeywords(completionResult, request, typesForTypeName);
                }
                break;
            case RETURN_UNION_OR_INTERSECTION_TYPE_NAME: // no break
            case RETURN_TYPE_NAME:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteTypeNames(completionResult, request);
                if (!isIntersectionType(info, caretOffset)) {
                    final ArrayList<String> typesForReturnTypeName = new ArrayList<>(Type.getTypesForReturnType());
                    if (isInType(request)) {
                        // add self and parent
                        typesForReturnTypeName.addAll(Type.getSpecialTypesForType());
                        typesForReturnTypeName.add(Type.STATIC);
                    }
                    if (isNullableType(info, caretOffset)) {
                        // ?false, ?true is OK since PHP 8.2
                        typesForReturnTypeName.remove(Type.NULL);
                        typesForReturnTypeName.remove(Type.VOID);
                        typesForReturnTypeName.remove(Type.NEVER);
                    } else if (context == CompletionContext.RETURN_UNION_OR_INTERSECTION_TYPE_NAME) {
                        typesForReturnTypeName.remove(Type.VOID);
                        typesForReturnTypeName.remove(Type.NEVER);
                        typesForReturnTypeName.remove(Type.MIXED);
                    }
                    autoCompleteKeywords(completionResult, request, typesForReturnTypeName);
                }
                break;
            case FIELD_TYPE_NAME:
                autoCompleteFieldType(info, caretOffset, completionResult, request, false);
                break;
            case CONST_TYPE_NAME:
                if (isInType(request)) {
                    autoCompleteConstType(info, caretOffset, completionResult, request);
                }
                break;
            case STRING:
                // LOCAL VARIABLES
                completionResult.addAll(getVariableProposals(request, null));
                // are we in class?
                if (request.prefix.length() == 0 || startsWith(PHP_CLASS_KEYWORD_THIS, request.prefix)) {
                    final EnclosingClass enclosingCls = findEnclosingClass(info, caretOffset);
                    if (enclosingCls != null) {
                        final String className = enclosingCls.extractClassName();
                        if (className != null) {
                            completionResult.add(new PHPCompletionItem.ClassScopeKeywordItem(className, PHP_CLASS_KEYWORD_THIS, request));
                        }
                    }
                }
                break;
            case CLASS_MEMBER:
                autoCompleteClassMembers(completionResult, request, false);
                break;
            case STATIC_CLASS_MEMBER:
                autoCompleteClassMembers(completionResult, request, true);
                break;
            case PHPDOC:
                PHPDOCCodeCompletion.complete(completionResult, request);
                if (PHPDOCCodeCompletion.isTypeCtx(request)) {
                    autoCompleteTypeNames(completionResult, request);
                    autoCompleteNamespaces(completionResult, request);
                    autoCompleteKeywordsInPHPDoc(completionResult, request);
                }
                break;
            case CLASS_CONTEXT_KEYWORDS:
                autoCompleteInClassContext(info, caretOffset, completionResult, request);
                break;
            case INTERFACE_CONTEXT_KEYWORDS:
                autoCompleteInInterfaceContext(completionResult, request);
                break;
            case METHOD_NAME:
                autoCompleteMethodName(info, caretOffset, completionResult, request);
                break;
            case IMPLEMENTS:
                autoCompleteKeywords(completionResult, request, Collections.singletonList("implements")); //NOI18N
                break;
            case EXTENDS:
                autoCompleteKeywords(completionResult, request, Collections.singletonList("extends")); //NOI18N
                break;
            case INHERITANCE:
                autoCompleteKeywords(completionResult, request, INHERITANCE_KEYWORDS);
                break;
            case THROW_NEW:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteExceptions(completionResult, request, true);
                break;
            case THROW:
                autoCompleteKeywords(completionResult, request, Collections.singletonList("new")); // NOI18N
                autoCompleteNamespaces(completionResult, request);
                // XXX allow all class names for static factory methods? e.g. ExceptionFactory::create("Something");
                // currently, restrict to classes extending the Exception class
                autoCompleteExceptions(completionResult, request, false);
                break;
            case CATCH:
                autoCompleteNamespaces(completionResult, request);
                autoCompleteExceptions(completionResult, request, false);
                break;
            case CLASS_MEMBER_IN_STRING:
                autoCompleteClassFields(completionResult, request);
                break;
            case SERVER_ENTRY_CONSTANTS:
                //TODO: probably better PHPCompletionItem instance should be used
                //autoCompleteMagicItems(proposals, request, PredefinedSymbols.SERVER_ENTRY_CONSTANTS);
                for (String keyword : PredefinedSymbols.SERVER_ENTRY_CONSTANTS) {
                    if (keyword.startsWith(request.prefix)) {
                        completionResult.add(new PHPCompletionItem.KeywordItem(keyword, request) {
                            @Override
                            public ImageIcon getIcon() {
                                return null;
                            }
                        });
                    }
                }

                break;
            default:
                assert false : context;
        }

        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            long time = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("complete() took %d ms, result contains %d items", time, completionResult.getItems().size()));
        }

        return completionResult;
    }

    private List<ElementFilter> createTypeFilter(final EnclosingClass enclosingClass) {
        List<ElementFilter> superTypeIndices = new ArrayList<>();
        Expression superClass = enclosingClass.getSuperClass();
        if (superClass != null) {
            String superClsName = enclosingClass.extractUnqualifiedSuperClassName();
            superTypeIndices.add(ElementFilter.forSuperClassName(QualifiedName.create(superClsName)));
        }
        List<Expression> interfaces = enclosingClass.getInterfaces();
        Set<QualifiedName> superIfaceNames = new HashSet<>();
        for (Expression identifier : interfaces) {
            String ifaceName = CodeUtils.extractUnqualifiedName(identifier);
            if (ifaceName != null) {
                superIfaceNames.add(QualifiedName.create(ifaceName));
            }
        }
        if (!superIfaceNames.isEmpty()) {
            superTypeIndices.add(ElementFilter.forSuperInterfaceNames(superIfaceNames));
        }
        return superTypeIndices;
    }

    private void autoCompleteMethodName(ParserResult info, int caretOffset, final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        EnclosingClass enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
        if (enclosingClass != null) {
            List<ElementFilter> superTypeIndices = createTypeFilter(enclosingClass);
            String clsName = enclosingClass.getClassName();
            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(request.result.getModel().getFileScope(), request.anchor);
            String fullyQualifiedClassName = VariousUtils.qualifyTypeNames(clsName, request.anchor, namespaceScope);
            if (fullyQualifiedClassName != null) {
                final FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                final ElementFilter typeFilter = ElementFilter.allOf(
                        ElementFilter.forFiles(fileObject), ElementFilter.allOf(superTypeIndices));
                Set<TypeElement> types = typeFilter.filter(request.index.getTypes(NameKind.exact(fullyQualifiedClassName)));
                for (TypeElement typeElement : types) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ElementFilter methodFilter = ElementFilter.allOf(
                            ElementFilter.forExcludedNames(toNames(request.index.getDeclaredMethods(typeElement)), PhpElementKind.METHOD),
                            ElementFilter.forName(NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix))));
                    Set<MethodElement> accessibleMethods = methodFilter.filter(request.index.getAccessibleMethods(typeElement, typeElement));
                    for (MethodElement method : accessibleMethods) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        if (!method.isFinal()) {
                            completionResult.add(PHPCompletionItem.MethodDeclarationItem.forMethodName(method, request));
                        }
                    }
                    Set<MethodElement> magicMethods = methodFilter.filter(request.index.getAccessibleMagicMethods(typeElement));
                    for (MethodElement magicMethod : magicMethods) {
                        if (magicMethod != null) {
                            completionResult.add(PHPCompletionItem.MethodDeclarationItem.forMethodName(magicMethod, request));
                        }
                    }
                    break;
                }
            }
        }

    }

    /**
     * Finding item after new keyword.
     *
     * @param completionResult
     * @param request
     */
    private void autoCompleteNewClass(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        // At first find all classes that match the prefix
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final QualifiedName prefix = QualifiedName.create(request.prefix).toNotFullyQualified();
        final NameKind nameQuery = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);
        Model model = request.result.getModel();
        Set<ClassElement> classes = request.index.getClasses(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);
        if (!classes.isEmpty()) {
            completionResult.setFilterable(false);
        }
        boolean addedExact = false;
        final NameKind query;
        if (classes.size() == 1) {
            ClassElement clazz = (ClassElement) classes.toArray()[0];
            if (!clazz.isAbstract()
                    && !clazz.isAnonymous()) {
                // if there is only once class find constructors for it
                query = isCamelCase ? NameKind.create(prefix.toString(), QuerySupport.Kind.CAMEL_CASE) : NameKind.caseInsensitivePrefix(prefix);
                autoCompleteConstructors(completionResult, request, model, query);
            }
        } else {
            for (ClassElement clazz : classes) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (!clazz.isAbstract()
                        && !clazz.isAnonymous()) {
                    // check whether the prefix is exactly the class
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(request.result.getModel().getFileScope(), request.anchor);
                    String fqPrefixName = VariousUtils.qualifyTypeNames(request.prefix, request.anchor, namespaceScope);
                    if (clazz.getFullyQualifiedName().toString().equals(fqPrefixName)) {
                        // find constructor of the class
                        if (!addedExact) { // add the constructors only once
                            autoCompleteConstructors(completionResult, request, model, NameKind.exact(fqPrefixName));
                            addedExact = true;
                        }
                    } else {
                        // put to the cc just the class
                        completionResult.add(new PHPCompletionItem.ClassItem(clazz, request, false, null));
                    }
                }
            }
        }
    }

    private void autoCompleteAttribute(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind nameQuery = NameKind.create(
                request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX
        );
        Model model = request.result.getModel();
        Set<ClassElement> attributeClasses = request.index.getAttributeClasses(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);
        if (!attributeClasses.isEmpty()) {
            completionResult.setFilterable(false);
        }
        for (ClassElement attributeClass : attributeClasses) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!attributeClass.isAbstract()
                    && !attributeClass.isAnonymous()) {
                completionResult.add(new PHPCompletionItem.ClassItem(attributeClass, request, false, null));
                NameKind exactQuery = NameKind.create(attributeClass.getFullyQualifiedName(), Kind.EXACT);
                autoCompleteConstructors(completionResult, request, model, exactQuery, true);
            }
        }
    }

    private void autoCompleteConstructors(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request, final Model model, final NameKind query) {
        autoCompleteConstructors(completionResult, request, model, query, false);
    }

    private void autoCompleteConstructors(
            final PHPCompletionResult completionResult,
            final PHPCompletionItem.CompletionRequest request,
            final Model model,
            final NameKind query,
            boolean isAttributeClass
    ) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);
        Set<MethodElement> constructors = isAttributeClass
                ? request.index.getAttributeClassConstructors(query, aliasedNames, Trait.ALIAS)
                : request.index.getConstructors(query, aliasedNames, Trait.ALIAS);
        for (MethodElement constructor : constructors) {
            for (final PHPCompletionItem.NewClassItem newClassItem : PHPCompletionItem.NewClassItem.getNewClassItems(constructor, request)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                completionResult.add(newClassItem);
            }
        }
    }

    private void autoCompleteExceptions(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request, boolean withConstructors) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind nameQuery = NameKind.create(request.prefix, isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);
        final Set<ClassElement> classes = request.index.getClasses(nameQuery);
        final Model model = request.result.getModel();
        final Set<QualifiedName> constructorClassNames = new HashSet<>();
        for (ClassElement classElement : classes) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (isExceptionClass(classElement)) {
                completionResult.add(new PHPCompletionItem.ClassItem(classElement, request, false, null));
                if (withConstructors) {
                    constructorClassNames.add(classElement.getFullyQualifiedName());
                }
                continue;
            }
            if (classElement.getSuperClassName() != null) {
                Set<ClassElement> inheritedClasses = request.index.getInheritedClasses(classElement);
                for (ClassElement inheritedClass : inheritedClasses) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (isExceptionClass(inheritedClass)) {
                        completionResult.add(new PHPCompletionItem.ClassItem(classElement, request, false, null));
                        if (withConstructors) {
                            constructorClassNames.add(classElement.getFullyQualifiedName());
                        }
                        break;
                    }
                }
            }
        }
        for (QualifiedName qualifiedName : constructorClassNames) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            autoCompleteConstructors(completionResult, request, model, NameKind.exact(qualifiedName));
        }
    }

    private boolean isExceptionClass(ClassElement classElement) {
        return classElement.getFullyQualifiedName().toString().equals(EXCEPTION_CLASS_NAME);
    }

    private void autoCompleteClassNames(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, boolean endWithDoubleColon) {
        autoCompleteClassNames(completionResult, request, endWithDoubleColon, null);
    }

    private void autoCompleteClassNames(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, boolean endWithDoubleColon, QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind nameQuery = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);
        Model model = request.result.getModel();
        Set<ClassElement> classes = request.index.getClasses(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);

        if (!classes.isEmpty()) {
            completionResult.setFilterable(false);
        }
        for (ClassElement clazz : classes) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!clazz.isAnonymous()) {
                completionResult.add(new PHPCompletionItem.ClassItem(clazz, request, endWithDoubleColon, kind));
            }
        }
    }

    private void autoCompleteEnumNames(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, boolean endWithDoubleColon) {
        autoCompleteEnumNames(completionResult, request, endWithDoubleColon, null);
    }

    private void autoCompleteEnumNames(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, boolean endWithDoubleColon, QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind nameQuery = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);
        Model model = request.result.getModel();
        Set<EnumElement> enums = request.index.getEnums(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);

        if (!enums.isEmpty()) {
            completionResult.setFilterable(false);
        }
        for (EnumElement enumElement : enums) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.EnumItem(enumElement, request, endWithDoubleColon, kind));
        }
    }

    private void autoCompleteInterfaceNames(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        autoCompleteInterfaceNames(completionResult, request, null);
    }

    private void autoCompleteInterfaceNames(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind nameQuery = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);

        Model model = request.result.getModel();
        Set<InterfaceElement> interfaces = request.index.getInterfaces(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);
        if (!interfaces.isEmpty()) {
            completionResult.setFilterable(false);
        }

        for (InterfaceElement iface : interfaces) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
        }
    }

    private void autoCompleteTypeNames(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        autoCompleteTypeNames(completionResult, request, null, false);
    }

    private void autoCompleteAfterUseTrait(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request, final QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Set<NamespaceElement> namespaces = request.index.getNamespaces(
                NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix).toNotFullyQualified()));
        for (NamespaceElement namespace : namespaces) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.NamespaceItem(namespace, request, QualifiedNameKind.FULLYQUALIFIED));
        }
        final NameKind nameQuery = NameKind.caseInsensitivePrefix(request.prefix);
        Model model = request.result.getModel();
        Set<TraitElement> traits = request.index.getTraits(nameQuery, ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);
        for (TraitElement trait : traits) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.TraitItem(trait, request));
        }
    }

    private void autoCompleteGroupUse(UseType useType, PHPCompletionResult completionResult, CompletionRequest request) {
        assert request.extraPrefix != null;
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        request.insertOnlyMethodsName = true;
        // we will "complete" FQN so handle search prefix as well
        if (!request.extraPrefix.startsWith("\\")) { // NOI18N
            request.extraPrefix = "\\" + request.extraPrefix; // NOI18N
        }
        final String prefix = request.extraPrefix + request.prefix;
        Set<NamespaceElement> namespaces = request.index.getNamespaces(
                NameKind.caseInsensitivePrefix(QualifiedName.create(prefix).toNotFullyQualified()));
        for (NamespaceElement namespace : namespaces) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.NamespaceItem(namespace, request, QualifiedNameKind.FULLYQUALIFIED));
        }
        final NameKind nameQuery = NameKind.caseInsensitivePrefix(prefix);
        switch (useType) {
            case TYPE:
                for (ClassElement clazz : request.index.getClasses(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.ClassItem(clazz, request, false, QualifiedNameKind.FULLYQUALIFIED));
                }
                for (InterfaceElement iface : request.index.getInterfaces(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.InterfaceItem(iface, request, QualifiedNameKind.FULLYQUALIFIED, false));
                }
                // NETBEANS-4650
                for (TraitElement trait : request.index.getTraits(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.TraitItem(trait, request));
                }
                break;
            case CONST:
                for (ConstantElement constant : request.index.getConstants(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.ConstantItem(constant, request, QualifiedNameKind.FULLYQUALIFIED));
                }
                break;
            case FUNCTION:
                for (FunctionElement function : request.index.getFunctions(nameQuery)) {
                    for (PHPCompletionItem.FunctionElementItem item : PHPCompletionItem.FunctionElementItem.getItems(function, request, QualifiedNameKind.FULLYQUALIFIED)) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        completionResult.add(item);
                    }
                }
                break;
            default:
                assert false : "Unknown use type: " + useType;
        }
    }

    private void autoCompleteAfterUse(
            UseType useType,
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request,
            QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        Set<NamespaceElement> namespaces = request.index.getNamespaces(
                NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix).toNotFullyQualified()));
        for (NamespaceElement namespace : namespaces) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.NamespaceItem(namespace, request, kind));
        }
        final NameKind nameQuery = NameKind.caseInsensitivePrefix(request.prefix);
        switch (useType) {
            case TYPE:
                for (ClassElement clazz : request.index.getClasses(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.ClassItem(clazz, request, false, kind));
                }
                for (InterfaceElement iface : request.index.getInterfaces(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
                }
                // NETBEANS-4650
                for (TraitElement trait : request.index.getTraits(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.TraitItem(trait, request));
                }
                for (EnumElement enumElement : request.index.getEnums(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.EnumItem(enumElement, request, false, kind));
                }
                break;
            case CONST:
                for (ConstantElement constant : request.index.getConstants(nameQuery)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    completionResult.add(new PHPCompletionItem.ConstantItem(constant, request));
                }
                break;
            case FUNCTION:
                for (FunctionElement function : request.index.getFunctions(nameQuery)) {
                    List<PHPCompletionItem.FunctionElementItem> items = PHPCompletionItem.FunctionElementItem.getItems(function, request);
                    for (PHPCompletionItem.FunctionElementItem item : items) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        completionResult.add(item);
                    }
                }
                break;
            default:
                assert false : "Unknown use type: " + useType;
        }
    }

    private void autoCompleteTypeNames(
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request,
            QualifiedNameKind kind,
            boolean endWithDoubleColon) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        if (request.prefix.trim().length() > 0) {
            autoCompleteClassNames(completionResult, request, endWithDoubleColon, kind);
            autoCompleteEnumNames(completionResult, request, endWithDoubleColon, kind);
            autoCompleteInterfaceNames(completionResult, request, kind);
        } else {
            Model model = request.result.getModel();
            Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);
            Collection<PhpElement> allTopLevel = request.index.getTopLevelElements(NameKind.empty(), aliasedNames, Trait.ALIAS);
            for (PhpElement element : allTopLevel) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (element instanceof ClassElement) {
                    ClassElement classElement = (ClassElement) element;
                    if (!classElement.isAnonymous()) {
                        completionResult.add(new PHPCompletionItem.ClassItem(classElement, request, endWithDoubleColon, kind));
                    }
                } else if (element instanceof InterfaceElement) {
                    completionResult.add(new PHPCompletionItem.InterfaceItem((InterfaceElement) element, request, kind, endWithDoubleColon));
                } else if (element instanceof EnumElement) {
                    EnumElement enumElement = (EnumElement) element;
                    completionResult.add(new PHPCompletionItem.EnumItem(enumElement, request, endWithDoubleColon, kind));
                }
            }
        }
    }

    private void autoCompleteKeywords(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, List<String> keywordList) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        for (String keyword : keywordList) {
            if (keyword.startsWith(request.prefix)) {
                completionResult.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

    }

    private void autoCompleteKeywordsInPHPDoc(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        BaseDocument doc = (BaseDocument) request.info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return;
        }
        try {
            int start = request.anchor - 1;
            if (start >= 0) {
                String prefix = doc.getText(start, 1);
                if (CodeUtils.NULLABLE_TYPE_PREFIX.equals(prefix)) {
                    List<String> keywords = new ArrayList<>(Type.getTypesForEditor());
                    // ?false, ?true is OK since PHP 8.2
                    keywords.remove(Type.NULL);
                    autoCompleteKeywords(completionResult, request, keywords);
                } else {
                    autoCompleteKeywords(completionResult, request, Type.getTypesForPhpDoc());
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Incorrect offset for the nullable type prefix: {0}", ex.offsetRequested()); // NOI18N
        }
    }

    private void autoCompleteNamespaces(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request) {
        autoCompleteNamespaces(completionResult, request, null);
    }

    private void autoCompleteNamespaces(final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final QualifiedName prefix = QualifiedName.create(request.prefix).toNotFullyQualified();
        Model model = request.result.getModel();
        Set<NamespaceElement> namespaces = request.index.getNamespaces(NameKind.caseInsensitivePrefix(prefix),
                ModelUtils.getAliasedNames(model, request.anchor), Trait.ALIAS);
        for (NamespaceElement namespace : namespaces) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.NamespaceItem(namespace, request, kind));
        }
    }

    private void autoCompleteInInterfaceContext(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        autoCompleteKeywords(completionResult, request, INTERFACE_CONTEXT_KEYWORD_PROPOSAL);
    }

    private void autoCompleteInClassContext(
            ParserResult info,
            int caretOffset,
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence(PHPTokenId.language());
        assert tokenSequence != null;

        autoCompleteKeywords(completionResult, request, CLASS_CONTEXT_KEYWORD_PROPOSAL);
        if (offerMagicAndInherited(tokenSequence, caretOffset, th)) {
            EnclosingClass enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
            if (enclosingClass != null) {
                List<ElementFilter> superTypeIndices = createTypeFilter(enclosingClass);
                String clsName = enclosingClass.getClassName();
                NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(request.result.getModel().getFileScope(), request.anchor);
                String fullyQualifiedClassName = VariousUtils.qualifyTypeNames(clsName, request.anchor, namespaceScope);
                if (fullyQualifiedClassName != null) {
                    final FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                    final ElementFilter typeFilter = ElementFilter.allOf(
                            ElementFilter.forFiles(fileObject), ElementFilter.allOf(superTypeIndices));
                    Set<TypeElement> types = typeFilter.filter(request.index.getTypes(NameKind.exact(fullyQualifiedClassName)));
                    for (TypeElement typeElement : types) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        ElementFilter methodFilter = ElementFilter.allOf(
                                ElementFilter.forExcludedNames(toNames(request.index.getDeclaredMethods(typeElement)), PhpElementKind.METHOD),
                                ElementFilter.forName(NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix))));
                        Set<MethodElement> accessibleMethods = methodFilter.filter(request.index.getAccessibleMethods(typeElement, typeElement));
                        for (MethodElement method : accessibleMethods) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (!method.isFinal()) {
                                completionResult.add(PHPCompletionItem.MethodDeclarationItem.getDeclarationItem(method, request));
                            }
                        }
                        Set<MethodElement> magicMethods = methodFilter.filter(request.index.getAccessibleMagicMethods(typeElement));
                        for (MethodElement magicMethod : magicMethods) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (magicMethod != null) {
                                completionResult.add(PHPCompletionItem.MethodDeclarationItem.getDeclarationItem(magicMethod, request));
                            }
                        }
                        break;
                    }
                }
            }
        } else if (completeFieldTypes(tokenSequence, caretOffset, th, info.getSnapshot().getSource().getFileObject())){
            request.context = CompletionContext.FIELD_TYPE_NAME;
            autoCompleteFieldType(info, caretOffset, completionResult, request, true);
        }
    }

    private void autoCompleteConstType(ParserResult info, int caretOffset, final PHPCompletionResult completionResult, CompletionRequest request) {
        autoCompleteNamespaces(completionResult, request);
        autoCompleteTypeNames(completionResult, request);
        if (isIntersectionType(info, caretOffset)) {
            // Fatal Error: Foo&array, Foo&bool, Foo&callable, etc.
            return;
        }
        List<String> keywords = new ArrayList<>(Type.getTypesForConstType());
        boolean isNullableType = isNullableType(info, caretOffset);
        if (isNullableType) {
            keywords.remove(Type.NULL);
        }
        if (isUnionType(info, caretOffset)) {
            keywords.remove(Type.MIXED);
        }
        autoCompleteKeywords(completionResult, request, keywords);
    }

    private void autoCompleteFieldType(ParserResult info, int caretOffset, final PHPCompletionResult completionResult, CompletionRequest request, boolean isInClassContext) {
        if (!isPhp74OrNewer(info.getSnapshot().getSource().getFileObject())) {
            return;
        }
        // PHP 7.4 Typed Properties 2.0
        // https://wiki.php.net/rfc/typed_properties_v2
        autoCompleteNamespaces(completionResult, request);
        autoCompleteTypeNames(completionResult, request);
        if (isIntersectionType(info, caretOffset)) {
            // Fatal Error: Foo&array, Foo&bool, Foo&callable, etc.
            return;
        }
        List<String> keywords = new ArrayList<>(Type.getTypesForFieldType());
        boolean isNullableType = isNullableType(info, caretOffset);
        if (!isInClassContext && !isNullableType) {
            // e.g. private stat^
            TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
            TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence(PHPTokenId.language());
            assert tokenSequence != null;
            tokenSequence.move(caretOffset);
            boolean addStaticKeyword = false;
            boolean addReadonlyKeyword = false;
            boolean addVisibilityKeyword = false;
            if (!(!tokenSequence.moveNext() && !tokenSequence.movePrevious())) {
                Token<PHPTokenId> token = tokenSequence.token();
                int tokenIdOffset = tokenSequence.token().offset(th);
                addStaticKeyword = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(
                        PHPTokenId.PHP_STATIC,
                        PHPTokenId.PHP_READONLY,
                        PHPTokenId.PHP_OPERATOR // "|"
                ));
                addReadonlyKeyword = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(
                        PHPTokenId.PHP_READONLY,
                        PHPTokenId.PHP_OPERATOR // "|"
                ));
                addVisibilityKeyword = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(
                        PHPTokenId.PHP_PUBLIC,
                        PHPTokenId.PHP_PRIVATE,
                        PHPTokenId.PHP_PROTECTED,
                        PHPTokenId.PHP_OPERATOR // "|"
                ));
            }
            if (addStaticKeyword) {
                keywords.add("static"); // NOI18N
            }
            if (addReadonlyKeyword) {
                keywords.add("readonly"); // NOI18N
            }
            if (addVisibilityKeyword) {
                keywords.addAll(PHP_VISIBILITY_KEYWORDS);
            }
        }
        if (isNullableType) {
            // ?false, ?true is OK since PHP 8.2
            keywords.remove(Type.NULL);
        }
        if (isUnionType(info, caretOffset)) {
            keywords.remove(Type.MIXED);
        }
        autoCompleteKeywords(completionResult, request, keywords);
    }

    private boolean offerMagicAndInherited(TokenSequence<PHPTokenId> tokenSequence, int caretOffset, TokenHierarchy<?> th) {
        boolean offerMagicAndInherited = true;
        tokenSequence.move(caretOffset);
        if (!(!tokenSequence.moveNext() && !tokenSequence.movePrevious())) {
            Token<PHPTokenId> token = tokenSequence.token();
            int tokenIdOffset = tokenSequence.token().offset(th);
            offerMagicAndInherited = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(
                PHPTokenId.PHP_PRIVATE,
                PHPTokenId.PHP_PUBLIC,
                PHPTokenId.PHP_PROTECTED,
                PHPTokenId.PHP_ABSTRACT,
                PHPTokenId.PHP_VAR,
                PHPTokenId.PHP_STATIC,
                PHPTokenId.PHP_CONST,
                PHPTokenId.PHP_READONLY
            ));
        }
        return offerMagicAndInherited;
    }

    private boolean completeFieldTypes(TokenSequence<PHPTokenId> tokenSequence, int caretOffset, TokenHierarchy<?> th, FileObject fileObject) {
        if (!isPhp74OrNewer(fileObject)) {
            return false;
        }
        // e.g. private static s^tring|int $field; private bool ^$bool;
        boolean completeTypes = false;
        tokenSequence.move(caretOffset);
        if (!(!tokenSequence.moveNext() && !tokenSequence.movePrevious())) {
            Token<PHPTokenId> token = tokenSequence.token();
            int tokenIdOffset = tokenSequence.token().offset(th);
            completeTypes = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(
                PHPTokenId.PHP_TYPE_BOOL,
                PHPTokenId.PHP_TYPE_INT,
                PHPTokenId.PHP_TYPE_FLOAT,
                PHPTokenId.PHP_TYPE_STRING,
                PHPTokenId.PHP_ARRAY,
                PHPTokenId.PHP_TYPE_OBJECT,
                PHPTokenId.PHP_ITERABLE,
                PHPTokenId.PHP_SELF,
                PHPTokenId.PHP_PARENT,
                PHPTokenId.PHP_TRUE,
                PHPTokenId.PHP_FALSE,
                PHPTokenId.PHP_NULL,
                PHPTokenId.PHP_STRING,
                PHPTokenId.PHP_CONST
            ));
        }
        return completeTypes;
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<>();
        for (PhpElement elem : elements) {
            names.add(elem.getName());
        }
        return names;
    }

    private void autoCompleteClassMembers(
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request,
            boolean staticContext) {
        autoCompleteClassMembers(completionResult, request, staticContext, false);
    }

    private void autoCompleteClassMembers(
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request,
            boolean staticContext,
            boolean completeAccessPrefix) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        // TODO: remove duplicate/redundant code from here

        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);

        if (tokenSequence == null) {
            return;
        }

        tokenSequence.move(request.anchor);
        if (tokenSequence.movePrevious()) {
            boolean instanceContext = !staticContext;

            if (tokenSequence.token().id() != PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM
                    && tokenSequence.token().id() != PHPTokenId.PHP_OBJECT_OPERATOR
                    && tokenSequence.token().id() != PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR) {
                tokenSequence.movePrevious();
            }
            tokenSequence.movePrevious();
            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                tokenSequence.movePrevious();
            }
            final CharSequence varName = tokenSequence.token().text();
            tokenSequence.moveNext();

            List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
            Model model = request.result.getModel();

            boolean parentContext = false;
            boolean selfContext = false;
            boolean staticLateBindingContext = false;
            boolean specialVariable = false;
            if (TokenUtilities.textEquals(varName, "$this")) { // NOI18N
                specialVariable = true;
            } else if (TokenUtilities.textEquals(varName, "self")) { // NOI18N
                staticContext = true;
                selfContext = true;
                specialVariable = true;
            } else if (TokenUtilities.textEquals(varName, "parent")) { // NOI18N
                invalidProposalsForClsMembers = Collections.emptyList();
                staticContext = true;
                instanceContext = true;
                specialVariable = true;
                parentContext = true;
            } else if (TokenUtilities.textEquals(varName, "static")) { // NOI18N
                staticContext = true;
                instanceContext = false;
                staticLateBindingContext = true;
                specialVariable = true;
            }

            Collection<? extends TypeScope> types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor, specialVariable);
            if (types != null) {
                TypeElement enclosingType = getEnclosingType(request, types);
                if (completeAccessPrefix) {
                    // NETBEANS-1855
                    types = ModelUtils.resolveType(model, request.anchor);
                }
                Set<PhpElement> duplicateElementCheck = new HashSet<>();
                for (TypeScope typeScope : types) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    final ElementFilter staticFlagFilter = !completeAccessPrefix
                            ? new StaticOrInstanceMembersFilter(staticContext, instanceContext, selfContext, staticLateBindingContext, parentContext)
                            : new ElementFilter() { // NETBEANS-1855
                        @Override
                        public boolean isAccepted(PhpElement element) {
                            return true;
                        }
                    };

                    final ElementFilter methodsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.METHOD),
                            ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                            staticFlagFilter,
                            ElementFilter.forExcludedNames(invalidProposalsForClsMembers, PhpElementKind.METHOD),
                            ElementFilter.forInstanceOf(MethodElement.class));
                    final ElementFilter fieldsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.FIELD),
                            ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                            staticFlagFilter,
                            ElementFilter.forInstanceOf(FieldElement.class));
                    final ElementFilter constantsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.TYPE_CONSTANT),
                            ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                            ElementFilter.forInstanceOf(TypeConstantElement.class));
                    final ElementFilter enumCasesFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.ENUM_CASE),
                            ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                            ElementFilter.forInstanceOf(EnumCaseElement.class));
                    HashSet<TypeMemberElement> accessibleTypeMembers = new HashSet<>();
                    accessibleTypeMembers.addAll(request.index.getAccessibleTypeMembers(typeScope, enclosingType));
                    // for @mixin tag #241740
                    if (typeScope instanceof ClassElement) {
                        ClassElement classElement = (ClassElement) typeScope;
                        if (!classElement.getFQMixinClassNames().isEmpty()) {
                            // XXX currently, only when mixins are used directly in the class. should support all cases?
                            accessibleTypeMembers.addAll(request.index.getAccessibleMixinTypeMembers(typeScope, enclosingType));
                        }
                    } else if (typeScope instanceof EnumElement) {
                        // add methods of BackedEnum/UnitEnum interface
                        EnumElement enumElement = (EnumElement) typeScope;
                        String backingTypeName = enumElement.getBackingType() != null ? enumElement.getBackingType().toString() : ""; // NOI18N
                        String enumInterfaceName = !backingTypeName.isEmpty() ? "\\BackedEnum" : "\\UnitEnum"; // NOI18N
                        final NameKind nameQuery = NameKind.exact(QualifiedName.create(enumInterfaceName));
                        Set<InterfaceElement> enums = request.index.getInterfaces(nameQuery);
                        for (InterfaceElement backedEnum : enums) {
                            accessibleTypeMembers.addAll(request.index.getAccessibleTypeMembers(backedEnum, backedEnum));
                        }
                        if (!staticContext && "name".startsWith(request.prefix)) { // NOI18N
                            // All Cases have a read-only property, name
                            // see: https://www.php.net/manual/en/language.enumerations.basics.php
                            // e.g. E::Case->name;
                            completionResult.add(PHPCompletionItem.AdditionalFieldItem.getItem("name", Type.STRING, enumElement.getFullyQualifiedName().toString(), request)); // NOI18N
                        }
                        if (!staticContext
                                && !backingTypeName.isEmpty()
                                && "value".startsWith(request.prefix)) { // NOI18N
                            completionResult.add(PHPCompletionItem.AdditionalFieldItem.getItem("value", backingTypeName, enumElement.getFullyQualifiedName().toString(), request)); // NOI18N
                        }
                    }
                    for (final PhpElement phpElement : accessibleTypeMembers) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        // https://wiki.php.net/rfc/deprecations_php_8_1 Accessing static members on traits
                        // e.g. T::$staticField, T::staticMethod() : deprecated since PHP 8.1
                        // we can fix this here in the future
                        if (typeScope instanceof TraitScope
                                && !specialVariable
                                && phpElement instanceof TypeConstantElement) {
                            // PHP 8.2: prohibit direct access through a trait name
                            // e.g. T::CONSTANT;
                            continue;
                        }
                        if (duplicateElementCheck.add(phpElement)) {
                            if (methodsFilter.isAccepted(phpElement)) {
                                MethodElement method = (MethodElement) phpElement;
                                List<MethodElementItem> items = PHPCompletionItem.MethodElementItem.getItems(method, request, completeAccessPrefix);
                                for (MethodElementItem methodItem : items) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    completionResult.add(methodItem);
                                }
                            } else if (fieldsFilter.isAccepted(phpElement)) {
                                FieldElement field = (FieldElement) phpElement;
                                FieldItem fieldItem = PHPCompletionItem.FieldItem.getItem(field, request, false, completeAccessPrefix);
                                completionResult.add(fieldItem);
                            } else if ((staticContext || completeAccessPrefix) && constantsFilter.isAccepted(phpElement)) {
                                TypeConstantElement constant = (TypeConstantElement) phpElement;
                                TypeConstantItem constantItem = PHPCompletionItem.TypeConstantItem.getItem(constant, request, completeAccessPrefix);
                                completionResult.add(constantItem);
                            } else if ((staticContext || completeAccessPrefix) && enumCasesFilter.isAccepted(phpElement)) {
                                EnumCaseElement enumCase = (EnumCaseElement) phpElement;
                                EnumCaseItem enumCaseItem = EnumCaseItem.getItem(enumCase, request, completeAccessPrefix);
                                completionResult.add(enumCaseItem);
                            }
                        }
                    }
                    if (staticContext) {
                        Set<TypeConstantElement> magicConstants = constantsFilter.filter(request.index.getAccessibleMagicConstants(typeScope));
                        for (TypeConstantElement magicConstant : magicConstants) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (magicConstant != null) {
                                // NETBEANS-4443
                                // PHP 8.0 allows ::class on objects (e.g. $instance::class, create()::class)
                                // so don't restrict dynamic access any more
                                // https://wiki.php.net/rfc/class_name_literal_on_object
                                completionResult.add(PHPCompletionItem.TypeConstantItem.getItem(magicConstant, request));
                            }
                        }
                    }
                }
            }
        }
    }

    private void autoCompleteConstructorParameterName(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);
        if (tokenSequence == null) {
            return;
        }
        if (!tokenSequence.moveNext()) {
            return;
        }
        if (CompletionContextFinder.isInAttribute(request.anchor, tokenSequence, true)) {
            autoCompleteAttributeExpression(completionResult, request);
        } else {
            autoCompleteExpression(completionResult, request);
        }
        Token<? extends PHPTokenId> constructorTypeName = CompletionContextFinder.findFunctionInvocationName(tokenSequence, request.anchor);
        if (constructorTypeName != null) {
            NamespaceName namespaceName = findNamespaceName(request.info, tokenSequence.offset());
            String fqTypeName;
            if (namespaceName != null) {
                fqTypeName = CodeUtils.extractQualifiedName(namespaceName);
            } else {
                fqTypeName = constructorTypeName.text().toString();
            }
            Model model = request.result.getModel();
            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), request.anchor);
            fqTypeName = VariousUtils.qualifyTypeNames(fqTypeName, request.anchor, namespaceScope);
            Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);
            Set<MethodElement> constructors = request.index.getConstructors(NameKind.exact(fqTypeName), aliasedNames, Trait.ALIAS);
            Set<String> duplicateCheck = new HashSet<>();
            for (MethodElement constructor : constructors) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addParameterNameItems(completionResult, request, constructor.getParameters(), duplicateCheck);
            }
        }
    }

    private void autoCompleteClassMethodParameterName(
            final PHPCompletionResult completionResult,
            PHPCompletionItem.CompletionRequest request,
            boolean staticContext
    ) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);
        if (tokenSequence == null) {
            return;
        }
        Token<? extends PHPTokenId> functionName = CompletionContextFinder.findFunctionInvocationName(tokenSequence, request.anchor);
        if (functionName != null) {
            int originalAnchor = request.anchor;
            try {
                request.anchor = tokenSequence.offset();
                boolean isInstanceContext = !staticContext;
                boolean isStaticContext = staticContext;

                if (tokenSequence.token().id() != PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM
                        && tokenSequence.token().id() != PHPTokenId.PHP_OBJECT_OPERATOR
                        && tokenSequence.token().id() != PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR) {
                    tokenSequence.movePrevious();
                }
                tokenSequence.movePrevious();
                if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                    tokenSequence.movePrevious();
                }
                final CharSequence varName = tokenSequence.token().text();
                tokenSequence.moveNext();

                List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
                Model model = request.result.getModel();

                boolean parentContext = false;
                boolean selfContext = false;
                boolean staticLateBindingContext = false;
                boolean specialVariable = false;
                if (TokenUtilities.textEquals(varName, "$this")) { // NOI18N
                    specialVariable = true;
                } else if (TokenUtilities.textEquals(varName, "self")) { // NOI18N
                    isStaticContext = true;
                    selfContext = true;
                    specialVariable = true;
                } else if (TokenUtilities.textEquals(varName, "parent")) { // NOI18N
                    invalidProposalsForClsMembers = Collections.emptyList();
                    isStaticContext = true;
                    isInstanceContext = true;
                    specialVariable = true;
                    parentContext = true;
                } else if (TokenUtilities.textEquals(varName, "static")) { // NOI18N
                    isStaticContext = true;
                    isInstanceContext = false;
                    staticLateBindingContext = true;
                    specialVariable = true;
                }

                Collection<? extends TypeScope> types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor, specialVariable);
                TypeElement enclosingType = getEnclosingType(request, types);
                Set<PhpElement> duplicateElementCheck = new HashSet<>();
                for (TypeScope typeScope : types) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    final ElementFilter staticFlagFilter = new StaticOrInstanceMembersFilter(isStaticContext, isInstanceContext, selfContext, staticLateBindingContext, true);
                    final ElementFilter methodsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.METHOD),
                            ElementFilter.forName(NameKind.exact(functionName.text().toString())),
                            staticFlagFilter,
                            ElementFilter.forExcludedNames(invalidProposalsForClsMembers, PhpElementKind.METHOD),
                            ElementFilter.forInstanceOf(MethodElement.class));
                    HashSet<TypeMemberElement> accessibleTypeMembers = new HashSet<>();
                    accessibleTypeMembers.addAll(request.index.getAccessibleTypeMembers(typeScope, enclosingType));
                    if (typeScope instanceof ClassElement) {
                        ClassElement classElement = (ClassElement) typeScope;
                        if (!classElement.getFQMixinClassNames().isEmpty()) {
                            accessibleTypeMembers.addAll(request.index.getAccessibleMixinTypeMembers(typeScope, enclosingType));
                        }
                    }
                    Set<String> duplicateCheck = new HashSet<>();
                    for (final PhpElement phpElement : accessibleTypeMembers) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        if (duplicateElementCheck.add(phpElement)) {
                            if (methodsFilter.isAccepted(phpElement)) {
                                MethodElement method = (MethodElement) phpElement;
                                addParameterNameItems(completionResult, request, method.getParameters(), duplicateCheck);
                            }
                        }
                    }
                }
            } finally {
                request.anchor = originalAnchor;
            }
        }
    }

    private void autoCompleteFunctionParameterName(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);
        if (tokenSequence == null) {
            return;
        }
        Token<? extends PHPTokenId> functionName = CompletionContextFinder.findFunctionInvocationName(tokenSequence, request.anchor);
        if (functionName != null) {
            Set<PhpElement> elements = request.index.getTopLevelElements(NameKind.exact(functionName.text().toString()));
            // usually, php doesn't have the same name functions
            // but just check duplicate name
            Set<String> duplicateCheck = new HashSet<>();
            for (PhpElement element : elements) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (element instanceof FunctionElement) {
                    FunctionElement functionElement = (FunctionElement) element;
                    if (functionElement.isAnonymous()) {
                        continue;
                    }
                    addParameterNameItems(completionResult, request, functionElement.getParameters(), duplicateCheck);
                }
            }
        }
    }

    private void addParameterNameItems(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request,
            List<ParameterElement> parameters, Set<String> duplicateCheck) {
        for (ParameterElement parameter : parameters) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            String name = parameter.getName();
            if (!StringUtils.isEmpty(name)) {
                name = name.substring(1);
            }
            if (!StringUtils.isEmpty(name)
                    && name.startsWith(request.prefix)
                    && duplicateCheck.add(name)) {
                completionResult.add(new PHPCompletionItem.ParameterNameItem(parameter, request));
            }
        }
    }

    private void autoCompleteClassConstants(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        // NETBANS-1855
        // complete access prefix i.e. add "self::" to the top of constant names
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final ElementFilter constantsFilter = ElementFilter.allOf(
                ElementFilter.forKind(PhpElementKind.TYPE_CONSTANT),
                ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                ElementFilter.forInstanceOf(TypeConstantElement.class)
        );
        Model model = request.result.getModel();
        Collection<? extends TypeScope> types = ModelUtils.resolveType(model, request.anchor);
        TypeElement enclosingType = getEnclosingType(request, types);
        for (TypeScope typeScope : types) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            for (final PhpElement phpElement : request.index.getAccessibleTypeMembers(typeScope, enclosingType)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (constantsFilter.isAccepted(phpElement)) {
                    TypeConstantElement constant = (TypeConstantElement) phpElement;
                    TypeConstantItem constantItem = PHPCompletionItem.TypeConstantItem.getItem(constant, request, true);
                    completionResult.add(constantItem);
                }
            }
        }

    }

    private void autoCompleteClassFields(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);
        Model model = request.result.getModel();
        Collection<? extends TypeScope> types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor, false);
        final ElementFilter fieldsFilter = ElementFilter.allOf(
                ElementFilter.forKind(PhpElementKind.FIELD),
                ElementFilter.forName(NameKind.caseInsensitivePrefix(request.prefix)),
                ElementFilter.forInstanceOf(FieldElement.class));
        if (types != null) {
            TypeElement enclosingType = getEnclosingType(request, types);
            for (TypeScope typeScope : types) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                for (final PhpElement phpElement : request.index.getAccessibleTypeMembers(typeScope, enclosingType)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (fieldsFilter.isAccepted(phpElement)) {
                        FieldElement field = (FieldElement) phpElement;
                        FieldItem fieldItem = PHPCompletionItem.FieldItem.getItem(field, request);
                        completionResult.add(fieldItem);
                    }
                }
            }
        }
    }

    @CheckForNull
    private TypeElement getEnclosingType(CompletionRequest request, Collection<? extends TypeScope> types) {
        final EnclosingType enclosingType = findEnclosingType(request.info, lexerToASTOffset(request.result, request.anchor));
        final String enclosingTypeName = enclosingType != null ? enclosingType.extractTypeName() : null;
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(request.result.getModel().getFileScope(), request.anchor);
        final String enclosingFQTypeName = VariousUtils.qualifyTypeNames(enclosingTypeName, request.anchor, namespaceScope);
        final NameKind enclosingTypeNameKind = (enclosingFQTypeName != null && !enclosingFQTypeName.trim().isEmpty()) ? NameKind.exact(enclosingFQTypeName) : null;
        Set<FileObject> preferedFileObjects = new HashSet<>();
        Set<TypeElement> enclosingTypes = null;
        FileObject currentFile = request.result.getSnapshot().getSource().getFileObject();
        if (currentFile != null) {
            preferedFileObjects.add(currentFile);
        }
        for (TypeScope typeScope : types) {
            final FileObject fileObject = typeScope.getFileObject();
            if (fileObject != null) {
                preferedFileObjects.add(fileObject);
            }
            if (enclosingTypeNameKind != null && enclosingTypes == null) {
                if (enclosingTypeNameKind.matchesName(typeScope)) {
                    enclosingTypes = Collections.<TypeElement>singleton((TypeElement) typeScope);
                }
            }
        }
        if (enclosingTypeNameKind != null && enclosingTypes == null) {
            final ElementFilter forFiles = ElementFilter.forFiles(preferedFileObjects.toArray(new FileObject[0]));
            Set<TypeElement> indexTypes = forFiles.prefer(request.index.getTypes(enclosingTypeNameKind));
            if (!indexTypes.isEmpty()) {
                enclosingTypes = new HashSet<>(indexTypes);
            }
        }
        return (enclosingTypes == null || enclosingTypes.isEmpty()) ? null : enclosingTypes.iterator().next();
    }

    private static boolean isNullableType(ParserResult info, int caretOffset) {
        TokenSequence<PHPTokenId> tokenSequence = getTokenSequence(info, caretOffset);
        tokenSequence.move(caretOffset);
        if (tokenSequence.movePrevious()) {
            Token<? extends PHPTokenId> previousToken = LexUtilities.findPrevious(tokenSequence, Arrays.asList(PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING));
            if (previousToken.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(previousToken.text(), "?")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private static boolean isUnionType(ParserResult info, int caretOffset) {
        TokenSequence<PHPTokenId> tokenSequence = getTokenSequence(info, caretOffset);
        if (tokenSequence.movePrevious()) {
            Token<? extends PHPTokenId> previousToken = LexUtilities.findPrevious(tokenSequence, VALID_UNION_TYPE_TOKENS);
            if (previousToken.id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals(previousToken.text(), Type.SEPARATOR)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIntersectionType(ParserResult info, int caretOffset) {
        TokenSequence<PHPTokenId> tokenSequence = getTokenSequence(info, caretOffset);
        if (tokenSequence.movePrevious() && tokenSequence.moveNext()) {
            if ((tokenSequence.token().id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals(tokenSequence.token().text(), "&&"))) { // NOI18N
                return true;
            }
        }
        tokenSequence.move(caretOffset);
        if (tokenSequence.movePrevious()) {
            Token<? extends PHPTokenId> previousToken = LexUtilities.findPrevious(tokenSequence, VALID_INTERSECTION_TYPE_TOKENS);
            if (previousToken == null) {
                return false;
            }
            if ((previousToken.id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals(previousToken.text(), Type.SEPARATOR_INTERSECTION))) {
                return true;
            }
            if (CompletionContextFinder.isLeftParen(previousToken)) {
                if (!tokenSequence.movePrevious()) {
                    return false;
                }
                previousToken = tokenSequence.token();
                if (previousToken.id() == PHPTokenId.WHITESPACE
                        || CompletionContextFinder.isLeftParen(previousToken)
                        || CompletionContextFinder.isVerticalBar(previousToken)
                        || CompletionContextFinder.isComma(previousToken)
                    ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static TokenSequence<PHPTokenId> getTokenSequence(ParserResult info, int caretOffset) {
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence(PHPTokenId.language());
        assert tokenSequence != null;
        tokenSequence.move(caretOffset);
        return tokenSequence;
    }

    private static boolean isInType(CompletionRequest request) {
        return findEnclosingType(request.info, lexerToASTOffset(request.result, request.anchor)) != null;
    }

    @CheckForNull
    private static NamespaceName findNamespaceName(ParserResult info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            ASTNode node = nodes.get(i);
            if (node instanceof NamespaceName
                    && node.getStartOffset() < offset
                    && node.getEndOffset() > offset) {
                return (NamespaceName) node;
            }
        }
        return null;
    }

    @CheckForNull
    private static EnclosingType findEnclosingType(ParserResult info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            ASTNode node = nodes.get(i);
            if (node instanceof TypeDeclaration
                    && node.getEndOffset() > offset) {
                return EnclosingType.forTypeDeclaration((TypeDeclaration) node);
            }
            if (node instanceof ClassInstanceCreation
                    && node.getEndOffset() > offset) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
                if (classInstanceCreation.isAnonymous()) {
                    Block body = classInstanceCreation.getBody();
                    if (body != null
                            && body.getStartOffset() <= offset
                            && body.getEndOffset() >= offset) {
                        return EnclosingType.forClassInstanceCreation(classInstanceCreation);
                    }
                    List<Attribute> attributes = classInstanceCreation.getAttributes();
                    for (Attribute attribute : attributes) {
                        if (attribute.getStartOffset() < offset
                                && attribute.getEndOffset() > offset) {
                            return EnclosingType.forClassInstanceCreation(classInstanceCreation);
                        }
                    }
                }
            }
        }
        // check tokens
        // because can't check it correctly if there is an ASTError
        // e.g. the following has an ASTError, so, can't get a ClassDeclaration node
        // class Example {
        //     const CONSTANT = 1;
        //     const ^
        // }
        return findEclosingType(info, offset, TYPE_TOKENS);
    }

    @CheckForNull
    private static EnclosingType findEclosingType(ParserResult info, int offset, List<PHPTokenId> typeTokenIds) {
        TokenSequence<PHPTokenId> tokenSequence = getTokenSequence(info, offset);
        if (!tokenSequence.moveNext()) {
            return null;
        }
        int curlyBalance = 0;
        Token<? extends PHPTokenId> typeToken = LexUtilities.findPreviousToken(tokenSequence, typeTokenIds);
        if (typeToken == null) {
            return null;
        }
        TokenId typeId = typeToken.id();
        String typeName = getTypeName(tokenSequence);
        if (typeName == null && typeId == PHPTokenId.PHP_CLASS) {
            // anonymous class
            typeName = "#anon"; // NOI18N
        }
        if (typeName == null) {
            return null;
        }
        while (tokenSequence.moveNext()) {
            if (tokenSequence.offset() >= offset) {
                if (curlyBalance > 0) {
                    return EnclosingType.forTokenId(typeId, typeName);
                }
                break;
            }
            Token token = tokenSequence.token();
            TokenId id = token.id();
            if (id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                curlyBalance++;
            } else if (id.equals(PHPTokenId.PHP_CURLY_CLOSE)) {
                curlyBalance--;
                if (curlyBalance == 0) {
                    break;
                }
            }
        }
        return null;
    }

    @CheckForNull
    private static String getTypeName(TokenSequence<PHPTokenId> tokenSequence) {
        String typeName = null;
        List<PHPTokenId> typeNameTokenChains = Arrays.asList(PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING);
        for (PHPTokenId typeNameToken : typeNameTokenChains) {
            if (tokenSequence.moveNext() && typeNameToken == tokenSequence.token().id()) {
                if (typeNameToken == PHPTokenId.PHP_STRING) {
                    typeName = tokenSequence.token().text().toString();
                }
            }
        }
        return typeName;
    }

    @CheckForNull
    private static EnclosingClass findEnclosingClass(ParserResult info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            ASTNode node = nodes.get(i);
            if (node instanceof ClassDeclaration
                    && node.getEndOffset() > offset) {
                return EnclosingClass.forClassDeclaration((ClassDeclaration) node);
            }
            if (node instanceof EnumDeclaration
                    && node.getEndOffset() > offset) {
                return EnclosingClass.forEnumDeclaration((EnumDeclaration) node);
            }
            if (node instanceof ClassInstanceCreation
                    && node.getEndOffset() > offset) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
                if (classInstanceCreation.isAnonymous()) {
                    Block body = classInstanceCreation.getBody();
                    if (body != null
                            && body.getStartOffset() <= offset
                            && body.getEndOffset() >= offset) {
                        return EnclosingClass.forClassInstanceCreation((ClassInstanceCreation) node);
                    }
                }
            }
        }
        return null;
    }

    private void autoCompleteExpression(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        autoCompleteNamespaces(completionResult, request);
        List<String> defaultKeywords = new ArrayList<>(PHP_KEYWORDS.keySet());
        defaultKeywords.remove("default =>"); // NOI18N
        autoCompleteExpression(completionResult, request, defaultKeywords);
    }

    private void autoCompleteExpression(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request, List<String> keywords) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        // KEYWORDS
        for (String keyword : keywords) {
            if (startsWith(keyword, request.prefix)) {
                completionResult.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

        for (String keyword : PHP_LANGUAGE_CONSTRUCTS_WITH_QUOTES) {
            if (startsWith(keyword, request.prefix)) {
                completionResult.add(new PHPCompletionItem.LanguageConstructWithQuotesItem(keyword, request));
            }
        }

        for (String construct : PHP_LANGUAGE_CONSTRUCTS_WITH_PARENTHESES) {
            if (startsWith(construct, request.prefix)) {
                completionResult.add(new PHPCompletionItem.LanguageConstructWithParenthesesItem(construct, request));
            }
        }

        for (String construct : PHP_LANGUAGE_CONSTRUCTS_WITH_SEMICOLON) {
            if (startsWith(construct, request.prefix)) {
                completionResult.add(new PHPCompletionItem.LanguageConstructWithSemicolonItem(construct, request));
            }
        }

        final boolean offerGlobalVariables = OptionsUtils.codeCompletionVariablesScope().equals(VariablesScope.ALL);
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind prefix = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);

        final Set<VariableElement> globalVariables = new HashSet<>();

        Model model = request.result.getModel();
        Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);

        for (final PhpElement element : request.index.getTopLevelElements(prefix, aliasedNames, Trait.ALIAS)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (element instanceof FunctionElement) {
                FunctionElement functionElement = (FunctionElement) element;
                if (!functionElement.isAnonymous()) {
                    for (final PHPCompletionItem.FunctionElementItem functionItem
                            : PHPCompletionItem.FunctionElementItem.getItems(functionElement, request)) {
                        completionResult.add(functionItem);
                    }
                }
            } else if (element instanceof ClassElement) {
                ClassElement classElement = (ClassElement) element;
                if (!classElement.isAnonymous()) {
                    completionResult.add(new PHPCompletionItem.ClassItem(classElement, request, true, null));
                }
            } else if (element instanceof EnumElement) {
                EnumElement enumElement = (EnumElement) element;
                completionResult.add(new PHPCompletionItem.EnumItem(enumElement, request, true, null));
            } else if (element instanceof InterfaceElement) {
                completionResult.add(new PHPCompletionItem.InterfaceItem((InterfaceElement) element, request, true));
            } else if (offerGlobalVariables && element instanceof VariableElement) {
                globalVariables.add((VariableElement) element);
            } else if (element instanceof ConstantElement) {
                completionResult.add(new PHPCompletionItem.ConstantItem((ConstantElement) element, request));
            }
        }
        FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
        final ElementFilter forCurrentFile = ElementFilter.forFiles(fileObject);
        completionResult.addAll(getVariableProposals(request, forCurrentFile.reverseFilter(globalVariables)));

        // Special keywords applicable only inside a class, enum, or trait
        final EnclosingType enclosingType = findEnclosingType(request.info, lexerToASTOffset(request.result, request.anchor));
        if (enclosingType != null
                && (enclosingType.isClassDeclaration() || enclosingType.isTraitDeclaration() || enclosingType.isEnumDeclaration())) {
            final String typeName = enclosingType.extractTypeName();
            if (typeName != null) {
                for (final String keyword : PHP_CLASS_KEYWORDS) {
                    if (startsWith(keyword, request.prefix)) {
                        completionResult.add(new PHPCompletionItem.ClassScopeKeywordItem(typeName, keyword, request));
                    }
                }
                // NETBEANS-1855
                autoCompleteClassMembers(completionResult, request, false, true);
            }
        }
    }

    private void autoCompleteAttributeExpression(final PHPCompletionResult completionResult, final PHPCompletionItem.CompletionRequest request) {
        autoCompleteNamespaces(completionResult, request);
        final EnclosingType enclosingClassWithAttribute = findEnclosingType(request.info, request.anchor);
        if (enclosingClassWithAttribute != null) {
            // static is not allowed
            // PHP Fatal error:  "static::" is not allowed in compile-time constants
            autoCompleteKeywords(completionResult, request, PHP_ATTRIBUTE_EXPRESSION_KEYWORDS);
        }
        autoCompleteTypeNames(completionResult, request, null, true);
        autoCompleteConstants(completionResult, request);
    }

    private void autoCompleteGlobals(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        if (OptionsUtils.codeCompletionVariablesScope().equals(VariablesScope.ALL)) {
            final CaseInsensitivePrefix prefix = NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix));
            for (VariableElement variableElement : request.index.getTopLevelVariables(prefix)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                completionResult.add(new PHPCompletionItem.VariableItem(variableElement, request));
            }
        }
    }

    private void autoCompleteConstants(final PHPCompletionResult completionResult, PHPCompletionItem.CompletionRequest request) {
        final boolean isCamelCase = isCamelCaseForTypeNames(request.prefix);
        final NameKind prefix = NameKind.create(request.prefix,
                isCamelCase ? Kind.CAMEL_CASE : Kind.CASE_INSENSITIVE_PREFIX);
        Model model = request.result.getModel();
        Set<AliasedName> aliasedNames = ModelUtils.getAliasedNames(model, request.anchor);
        for (final ConstantElement element : request.index.getConstants(prefix, aliasedNames, Trait.ALIAS)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            completionResult.add(new PHPCompletionItem.ConstantItem((ConstantElement) element, request));
        }
    }

    /**
     * @param globalVariables (can be bull) if null then will be looked up in
     * index
     */
    private Collection<CompletionProposal> getVariableProposals(final CompletionRequest request, Set<VariableElement> globalVariables) {
        if (CancelSupport.getDefault().isCancelled()) {
            return Collections.emptyList();
        }
        final Map<String, CompletionProposal> proposals = new LinkedHashMap<>();
        Model model = request.result.getModel();
        VariableScope variableScope = model.getVariableScope(request.anchor);
        if (variableScope != null) {
            if (variableScope instanceof NamespaceScope
                    || variableScope instanceof ArrowFunctionScope) {
                if (globalVariables == null) {
                    FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                    final ElementFilter forCurrentFile = ElementFilter.forFiles(fileObject);
                    globalVariables = forCurrentFile.reverseFilter(request.index.getTopLevelVariables(NameKind.caseInsensitivePrefix(QualifiedName.create(request.prefix))));
                }

                for (final VariableElement globalVariable : globalVariables) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return Collections.emptyList();
                    }
                    proposals.put(globalVariable.getName(), new PHPCompletionItem.VariableItem(globalVariable, request));
                }
            }

            List<VariableName> allDeclaredVariables = new ArrayList<>(variableScope.getDeclaredVariables());
            // for nested arrow functions
            if (variableScope instanceof ArrowFunctionScope) {
                Scope inScope = variableScope.getInScope();
                while (inScope instanceof FunctionScope || inScope instanceof NamespaceScope) {
                    allDeclaredVariables.addAll(((VariableScope) inScope).getDeclaredVariables());
                    if (inScope instanceof FunctionScope
                            && !(inScope instanceof ArrowFunctionScope)) {
                        break;
                    }
                    inScope = inScope.getInScope();
                }
            }

            Collection<? extends VariableName> declaredVariables = ModelUtils.filter(allDeclaredVariables, nameKind, request.prefix);
            final int caretOffset = request.anchor + request.prefix.length();
            for (VariableName varName : declaredVariables) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                final FileObject realFileObject = varName.getRealFileObject();
                if (realFileObject != null || varName.getNameRange().getEnd() < caretOffset) {
                    final String name = varName.getName();
                    String notDollaredName = name.startsWith("$") ? name.substring(1) : name;
                    if (PredefinedSymbols.SUPERGLOBALS.contains(notDollaredName)) {
                        continue;
                    }
                    if (varName.representsThis()) {
                        continue;
                    }
                    final Collection<? extends String> typeNames = varName.getTypeNames(request.anchor);
                    String typeName = typeNames.size() > 1 ? Type.MIXED : ModelUtils.getFirst(typeNames);
                    final Set<Pair<QualifiedName, Boolean>> qualifiedNames = typeName != null
                            ? Collections.singleton(Pair.of(QualifiedName.create(typeName), false))
                            : Collections.<Pair<QualifiedName, Boolean>>emptySet();
                    if (realFileObject != null) {
                        //#183928 -  Extend model to allow CTRL + click for 'view/action' variables
                        proposals.put(name, new PHPCompletionItem.VariableItem(
                                VariableElementImpl.create(name, 0, realFileObject,
                                varName.getElementQuery(), TypeResolverImpl.forNames(qualifiedNames), varName.isDeprecated()), request) {
                            @Override
                            public boolean isSmart() {
                                return true;
                            }
                        });
                    } else {
                        proposals.put(name, new PHPCompletionItem.VariableItem(
                                VariableElementImpl.create(name, 0, request.currentlyEditedFileURL,
                                varName.getElementQuery(), TypeResolverImpl.forNames(qualifiedNames), varName.isDeprecated()), request));
                    }
                }
            }

            for (final String name : PredefinedSymbols.SUPERGLOBALS) {
                if (isPrefix("$" + name, request.prefix)) { //NOI18N
                    proposals.put(name, new PHPCompletionItem.SuperGlobalItem(request, name));
                }
            }

        }
        return proposals.values();
    }

    private boolean isPrefix(String name, String prefix) {
        return name != null && (name.startsWith(prefix)
                || nameKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    @Override
    public Documentation documentElement(ParserResult info, ElementHandle element, Callable<Boolean> cancel) {
        Documentation result;
        if (element instanceof ModelElement) {
            ModelElement mElem = (ModelElement) element;
            ModelElement parentElem = mElem.getInScope();
            FileObject fileObject = mElem.getFileObject();
            String fName = fileObject == null ? "?" : fileObject.getNameExt(); //NOI18N
            String tooltip;
            if (parentElem instanceof TypeScope) {
                tooltip = mElem.getPhpElementKind() + ": " + parentElem.getName() + "<b> " + mElem.getName() + " </b>" + "(" + fName + ")"; //NOI18N
            } else {
                tooltip = mElem.getPhpElementKind() + ":<b> " + mElem.getName() + " </b>" + "(" + fName + ")"; //NOI18N
            }
            result = Documentation.create(String.format("<div align=\"right\"><font size=-1>%s</font></div>", tooltip)); //NOI18N
        } else {
            result = ((element instanceof MethodElement) && ((MethodElement) element).isMagic()) ? null : DocRenderer.document(info, element);
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

    private static boolean isPHPIdentifierPart(char c) {
        return Character.isJavaIdentifierPart(c) || c == '@';
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE", justification = "Not sure about FB analysis correctness")
    private String getPrefix(ParserResult info, int caretOffset, boolean upToOffset, PrefixBreaker prefixBreaker) {
        try {
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return null;
            }
            int lineBegin = LineDocumentUtils.getLineStart(doc, caretOffset);
            if (lineBegin != -1) {
                int lineEnd = LineDocumentUtils.getLineEnd(doc, caretOffset);
                String line = doc.getText(lineBegin, lineEnd - lineBegin);
                int lineOffset = caretOffset - lineBegin;
                int start = lineOffset;
                if (lineOffset > 0) {
                    char c = 0;
                    for (int i = lineOffset - 1; i >= 0; i--) {
                        assert i >= 0 && i <= line.length() - 1 : "line:" + line + " | i:" + i + " | line.length():" + line.length() + " | lineBegin:" + lineBegin + " | lineEnd:" + lineEnd + " | caretOffset:" + caretOffset;
                        if (i >= 0 && i <= line.length() - 1) {
                            c = line.charAt(i);
                            if (!isPHPIdentifierPart(c) && c != '\\') {
                                break;
                            } else {
                                start = i;
                            }
                        }
                    }
                    if (start == lineOffset && c == '?'
                            && lineOffset - 2 >= 0 && line.charAt(lineOffset - 2) == '<') {
                        start -= 2;
                    }
                }

                // Find identifier end
                String prefix;
                if (upToOffset) {
                    prefix = line.substring(start, lineOffset);
                    int lastIndexOfDollar = prefix.lastIndexOf('$'); //NOI18N
                    if (lastIndexOfDollar > 0) {
                        prefix = prefix.substring(lastIndexOfDollar);
                    }
                } else {
                    if (lineOffset == line.length()) {
                        prefix = line.substring(start);
                    } else {
                        int n = line.length();
                        int end = lineOffset;
                        for (int j = lineOffset; j < n; j++) {
                            char d = line.charAt(j);
                            // Try to accept Foo::Bar as well
                            if (!isPHPIdentifierPart(d)) {
                                break;
                            } else {
                                end = j + 1;
                            }
                        }
                        prefix = line.substring(start, end);
                    }
                }

                if (prefix.length() > 0) {
                    if (prefix.endsWith("::")) {
                        return "";
                    }

                    if (prefix.endsWith(":") && prefix.length() > 1) {
                        return null;
                    }

                    // Strip out LHS if it's a qualified method, e.g.  Benchmark::measure -> measure
                    int q = prefix.lastIndexOf("::");

                    if (q != -1) {
                        prefix = prefix.substring(q + 2);
                    }

                    // The identifier chars identified by JsLanguage are a bit too permissive;
                    // they include things like "=", "!" and even "&" such that double-clicks will
                    // pick up the whole "token" the user is after. But "=" is only allowed at the
                    // end of identifiers for example.
                    if (prefix.length() == 1) {
                        char c = prefix.charAt(0);
                        if (prefixBreaker.isBreaker(c)) {
                            return null;
                        }
                    } else if (!"<?".equals(prefix)) {    //NOI18N
                        for (int i = prefix.length() - 1; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                            char c = prefix.charAt(i);
                            if (i == 0 && c == ':') {
                                // : is okay at the begining of prefixes
                            } else if (prefixBreaker.isBreaker(c)) {
                                prefix = prefix.substring(i + 1);
                                break;
                            }
                        }
                    }
                }

                if (prefix != null && prefix.startsWith("@")) { //NOI18N
                    final TokenHierarchy<?> tokenHierarchy = info.getSnapshot().getTokenHierarchy();
                    TokenSequence<PHPTokenId> tokenSequence = tokenHierarchy != null ? LexUtilities.getPHPTokenSequence(tokenHierarchy, caretOffset) : null;
                    if (tokenSequence != null) {
                        tokenSequence.move(caretOffset);
                        if (tokenSequence.moveNext() && tokenSequence.movePrevious()) {
                            Token<PHPTokenId> token = tokenSequence.token();
                            PHPTokenId id = token.id();
                            if (id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_TOKEN)) {
                                prefix = prefix.substring(1);
                            }
                        }
                    }
                }
                return prefix;
            }
            // Else: normal identifier: just return null and let the machinery do the rest
        } catch (BadLocationException ble) {
            //Exceptions.printStackTrace(ble);
        }

        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return getPrefix(info, caretOffset, upToOffset, PrefixBreaker.COMMON);
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if (typedText.length() == 0) {
            return QueryType.NONE;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);
        Document document = component.getDocument();
        //TokenHierarchy th = TokenHierarchy.get(document);
        int offset = component.getCaretPosition();
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, offset);
        if (ts == null) {
            return QueryType.STOP;
        }
        int diff = ts.move(offset);
        if (diff > 0 && ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();
            if (t != null) {
                if (t.id() == PHPTokenId.T_INLINE_HTML) {
                    return QueryType.NONE;
                } else {
                    if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))) {
                        return QueryType.STOP;
                    }
                    if (OptionsUtils.autoCompletionTypes()) {
                        if (lastChar == ' ' || lastChar == '\t') {
                            if (ts.movePrevious()
                                    && TOKENS_TRIGGERING_AUTOPUP_TYPES_WS.contains(ts.token().id())) {

                                return QueryType.ALL_COMPLETION;
                            } else {
                                return QueryType.STOP;
                            }
                        }

                        if (t.id() == PHPTokenId.PHP_OBJECT_OPERATOR
                                || t.id() == PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR
                                || t.id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM) {
                            return QueryType.ALL_COMPLETION;
                        }
                    }
                    if (OptionsUtils.autoCompletionVariables()) {
                        if ((t.id() == PHPTokenId.PHP_TOKEN && lastChar == '$')
                                || (t.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && lastChar == '$')) {
                            return QueryType.ALL_COMPLETION;
                        }
                    }
                    if (OptionsUtils.autoCompletionNamespaces()) {
                        if (t.id() == PHPTokenId.PHP_NS_SEPARATOR) {
                            return isPhp53OrNewer(document) ? QueryType.ALL_COMPLETION : QueryType.NONE;
                        }
                    }
                    if (t.id() == PHPTokenId.PHPDOC_COMMENT && lastChar == '@') {
                        return QueryType.ALL_COMPLETION;
                    }
                    if (OptionsUtils.autoCompletionFull()) {
                        TokenId id = t.id();
                        if ((id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_VARIABLE)) && t.length() > 0) {
                            return QueryType.ALL_COMPLETION;
                        }
                    }
                }
            }
        }
        return QueryType.NONE;
    }

    public static boolean isPhp53OrNewer(Document document) {
        final FileObject fileObject = CodeUtils.getFileObject(document);
        assert fileObject != null;
        return CodeUtils.isPhpVersionGreaterThan(fileObject, PhpVersion.PHP_5);
    }

    private static boolean isPhp74OrNewer(FileObject fileObject) {
        if (PHP_VERSION != null) {
            return PHP_VERSION.compareTo(PhpVersion.PHP_74) >= 0;
        }
        assert fileObject != null;
        return CodeUtils.isPhpVersionGreaterThan(fileObject, PhpVersion.PHP_73);
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
    public ParameterInfo parameters(final ParserResult info, final int caretOffset, CompletionProposal proposal) {
        final org.netbeans.modules.php.editor.model.Model model = ((PHPParseResult) info).getModel();
        ParameterInfoSupport infoSupport = model.getParameterInfoSupport(caretOffset);
        ParameterInfo parameterInfo = infoSupport.getParameterInfo();
        return parameterInfo == null ? ParameterInfo.NONE : parameterInfo;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private int findBaseNamespaceEnd(ParserResult info, int caretOffset) {
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        assert th != null;
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, caretOffset);
        assert tokenSequence != null;
        tokenSequence.move(caretOffset);
        final boolean moveNextSucces = tokenSequence.moveNext();
        if (!moveNextSucces && !tokenSequence.movePrevious()) {
            assert false;
            return caretOffset;
        }
        boolean hasCurly = false;
        while (tokenSequence.movePrevious()) {
            if (!hasCurly) {
                if (tokenSequence.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                    hasCurly = true;
                }
            } else {
                // possibly some whitespace before curly open?
                if (tokenSequence.token().id() != PHPTokenId.WHITESPACE) {
                    tokenSequence.moveNext();
                    break;
                }
            }
        }
        if (hasCurly) {
            return tokenSequence.offset();
        }
        assert false;
        return caretOffset;
    }


    private static class StaticOrInstanceMembersFilter extends ElementFilter {

        private final boolean forStaticContext;
        private final boolean forInstanceContext;
        private final boolean forSelfContext;
        private final boolean staticAllowed;
        private final boolean nonstaticAllowed;
        private final boolean forStaticLateBinding;
        private final boolean forParentContext;

        public StaticOrInstanceMembersFilter(final boolean forStaticContext, final boolean forInstanceContext,
                final boolean forSelfContext, final boolean forStaticLateBinding, final boolean forParentContext) {
            this.forStaticContext = forStaticContext;
            this.forInstanceContext = forInstanceContext;
            this.forSelfContext = forSelfContext;
            this.forStaticLateBinding = forStaticLateBinding;
            this.staticAllowed = OptionsUtils.codeCompletionStaticMethods();
            this.nonstaticAllowed = OptionsUtils.codeCompletionNonStaticMethods();
            this.forParentContext = forParentContext;
        }

        @Override
        public boolean isAccepted(final PhpElement element) {
            if (forSelfContext && isAcceptedForSelfContext(element)) {
                return true;
            }
            if (forStaticContext && isAcceptedForStaticContext(element)) {
                return true;
            }
            if (forInstanceContext && isAcceptedForNotStaticContext(element)) {
                return true;
            }
            return false;
        }

        private boolean isAcceptedForNotStaticContext(final PhpElement element) {
            final boolean isStatic = element.getPhpModifiers().isStatic();
            if (forParentContext
                    && !isStatic
                    && element.getPhpElementKind().equals(PhpElementKind.FIELD)) {
                // parent::fieldName is invalid
                // this is constant
                return false;
            }
            return !isStatic || (staticAllowed && element.getPhpElementKind().equals(PhpElementKind.METHOD));
        }

        private boolean isAcceptedForStaticContext(final PhpElement element) {
            final boolean isStatic = element.getPhpModifiers().isStatic();
            return isStatic || (nonstaticAllowed && !forStaticLateBinding && element.getPhpElementKind().equals(PhpElementKind.METHOD));
        }

        private boolean isAcceptedForSelfContext(final PhpElement element) {
            return forSelfContext && nonstaticAllowed && !element.getPhpElementKind().equals(PhpElementKind.FIELD);
        }
    }

    private interface PrefixBreaker {
        PrefixBreaker COMMON = new PrefixBreaker() {

            @Override
            public boolean isBreaker(char c) {
                return !(isPHPIdentifierPart(c) || c == ':');
            }
        };

        PrefixBreaker WITH_NS_PARTS = new PrefixBreaker() {

            @Override
            public boolean isBreaker(char c) {
                return !(isPHPIdentifierPart(c) || c == '\\' || c == ':');
            }
        };

        boolean isBreaker(char c);
    }

    private static boolean isCamelCaseForTypeNames(final String query) {
        return false;
    }

    private interface EnclosingType {

        boolean isClassDeclaration();

        boolean isTraitDeclaration();

        boolean isEnumDeclaration();

        String extractTypeName();

        //~ Factories

        static EnclosingType forTypeDeclaration(final TypeDeclaration typeDeclaration) {
            return new EnclosingType() {
                @Override
                public boolean isClassDeclaration() {
                    return typeDeclaration instanceof ClassDeclaration;
                }

                @Override
                public boolean isTraitDeclaration() {
                    return typeDeclaration instanceof TraitDeclaration;
                }

                @Override
                public boolean isEnumDeclaration() {
                    return typeDeclaration instanceof EnumDeclaration;
                }

                @Override
                public String extractTypeName() {
                    return CodeUtils.extractTypeName(typeDeclaration);
                }
            };
        }

        static EnclosingType forTokenId(final TokenId tokenId, String typeName) {
            return new EnclosingType() {
                @Override
                public boolean isClassDeclaration() {
                    return tokenId == PHPTokenId.PHP_CLASS;
                }

                @Override
                public boolean isTraitDeclaration() {
                    return tokenId == PHPTokenId.PHP_TRAIT;
                }

                @Override
                public boolean isEnumDeclaration() {
                    return tokenId == PHPTokenId.PHP_ENUM;
                }

                @Override
                public String extractTypeName() {
                    return typeName;
                }
            };
        }

        static EnclosingType forClassInstanceCreation(final ClassInstanceCreation classInstanceCreation) {
            assert classInstanceCreation.isAnonymous() : classInstanceCreation;
            return new EnclosingType() {
                @Override
                public boolean isClassDeclaration() {
                    return true;
                }

                @Override
                public boolean isTraitDeclaration() {
                    return false;
                }

                @Override
                public boolean isEnumDeclaration() {
                    return false;
                }

                @Override
                public String extractTypeName() {
                    return CodeUtils.extractClassName(classInstanceCreation);
                }
            };
        }

    }

    private interface EnclosingClass {

        String getClassName();

        Expression getSuperClass();

        List<Expression> getInterfaces();

        String extractClassName();

        String extractUnqualifiedSuperClassName();

        //~ Factories

        static EnclosingClass forClassDeclaration(final ClassDeclaration classDeclaration) {
            return new EnclosingClass() {
                @Override
                public String getClassName() {
                    return classDeclaration.getName().getName();
                }

                @Override
                public Expression getSuperClass() {
                    return classDeclaration.getSuperClass();
                }

                @Override
                public List<Expression> getInterfaces() {
                    return classDeclaration.getInterfaces();
                }

                @Override
                public String extractClassName() {
                    return CodeUtils.extractClassName(classDeclaration);
                }

                @Override
                public String extractUnqualifiedSuperClassName() {
                    return CodeUtils.extractUnqualifiedSuperClassName(classDeclaration);
                }
            };
        }

        static EnclosingClass forClassInstanceCreation(final ClassInstanceCreation classInstanceCreation) {
            assert classInstanceCreation.isAnonymous() : classInstanceCreation;
            return new EnclosingClass() {
                @Override
                public String getClassName() {
                    return CodeUtils.extractClassName(classInstanceCreation);
                }

                @Override
                public Expression getSuperClass() {
                    return classInstanceCreation.getSuperClass();
                }

                @Override
                public List<Expression> getInterfaces() {
                    return classInstanceCreation.getInterfaces();
                }

                @Override
                public String extractClassName() {
                    return CodeUtils.extractClassName(classInstanceCreation);
                }

                @Override
                public String extractUnqualifiedSuperClassName() {
                    return CodeUtils.extractUnqualifiedSuperClassName(classInstanceCreation);
                }
            };
        }

        static EnclosingClass forEnumDeclaration(final EnumDeclaration enumDeclaration) {
            return new EnclosingClass() {
                @Override
                public String getClassName() {
                    return enumDeclaration.getName().getName();
                }

                @Override
                public Expression getSuperClass() {
                    return null;
                }

                @Override
                public List<Expression> getInterfaces() {
                    return enumDeclaration.getInterfaces();
                }

                @Override
                public String extractClassName() {
                    return CodeUtils.extractTypeName(enumDeclaration);
                }

                @Override
                public String extractUnqualifiedSuperClassName() {
                    return null;
                }
            };
        }
    }

}
