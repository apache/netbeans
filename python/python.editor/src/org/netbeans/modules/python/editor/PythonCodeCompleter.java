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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.RstFormatter;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.python.source.elements.Element;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.elements.IndexedMethod;
import org.netbeans.modules.python.source.lexer.Call;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.PythonParser.Sanitize;
import org.netbeans.modules.python.source.elements.IndexedPackage;
import org.netbeans.modules.python.source.ImportManager;
import org.netbeans.modules.python.source.lexer.PythonCommentTokenId;
import org.netbeans.modules.python.source.lexer.PythonLexer;
import org.netbeans.modules.python.source.CodeStyle;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.base.expr;

/**
 * Code completion for Python.
 *
 * @todo Handle go to module, similar to code completion!
 * @todo Code completion after raise or except should only
 *   include things that extend the basic exception/error classes!
 * 
 */
public class PythonCodeCompleter implements CodeCompletionHandler {
    private static ImageIcon keywordIcon;
    private boolean caseSensitive;
    // http://docs.python.org/ref/strings.html
    private static final String[] STRING_ESCAPES =
            new String[]{};

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        ParserResult result = context.getParserResult();
        int lexOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        QueryType queryType = context.getQueryType();
        this.caseSensitive = context.isCaseSensitive();

        final Document document = result.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return CodeCompletionResult.NONE;
        }
        final BaseDocument doc = (BaseDocument)document;

        List<CompletionProposal> proposals = new ArrayList<>();
        DefaultCompletionResult completionResult = new PythonCompletionResult(context, proposals);

        PythonParserResult parseResult = PythonAstUtils.getParseResult(result);
        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
            PythonTree root = parseResult != null ? parseResult.getRoot() : null;
            final int astOffset = PythonAstUtils.getAstOffset(result, lexOffset);
            if (astOffset == -1) {
                return CodeCompletionResult.NONE;
            }
            final TokenHierarchy<Document> th = TokenHierarchy.get(document);
            final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
            //Call call = Call.getCallType(doc, th, lexOffset);

            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.completionResult = completionResult;
            request.result = parseResult;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.index = PythonIndex.get(fileObject);
            request.doc = doc;
            if(prefix == null) {
                prefix = "";
            }
            request.prefix = prefix;
            request.th = th;
            request.kind = context.isPrefixMatch()?QuerySupport.Kind.PREFIX:QuerySupport.Kind.EXACT;
            request.queryType = queryType;
            request.fileObject = fileObject;
            request.anchor = lexOffset - request.prefix.length();
            //request.call = call;
            request.searchUrl = request.fileObject.toURL().toExternalForm();
            if (request.searchUrl == null) {
                request.searchUrl = "";
            }

            if (root != null) {
                int offset = astOffset;

                OffsetRange sanitizedRange = parseResult.getSanitizedRange();
                if (sanitizedRange != OffsetRange.NONE && sanitizedRange.containsInclusive(offset)) {
                    offset = sanitizedRange.getStart();
                }

                final AstPath path = AstPath.get(root, offset);
                request.path = path;
                //request.fqn = PythonAstUtils.getFqn(path, null, null);

                final PythonTree closest = path.leaf();
                request.root = root;
                request.node = closest;
            }

            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, lexOffset);
            if (ts == null || ts.token() == null) {
                if (PythonUtils.isPythonFile(fileObject)) {
                    return completionResult;
                }

                // Embedding? Possibly in something like an EMPTY JAvaScript attribute,
                // e.g.   <input onclick="|"> - there's no token here. We have to
                // instead assume an empty prefix.
                request.prefix = prefix = "";
            } else {
                TokenId id = ts.token().id();
                if (id == PythonTokenId.NEWLINE && ts.offset() == lexOffset && ts.movePrevious()) {
                    id = ts.token().id();
                }
                if (id == PythonTokenId.COMMENT) {
                    completeComments(proposals, request, ts);
                    return completionResult;
                } else if (id == PythonTokenId.STRING_LITERAL || id == PythonTokenId.STRING_END) {
                    // Comment completion - rst tags and such

                    if (request.queryType == QueryType.DOCUMENTATION) {
                        if (id == PythonTokenId.STRING_END) {
                            ts.movePrevious();
                        }
                        String text = ts.token().text().toString();
                        Element element = new CommentElement(text);
                        PythonCompletionItem item = new PythonCompletionItem(element, request);
                        proposals.add(item);
                        return completionResult;
                    }

                    // Skip raw strings
                    // See if it's a raw string
                    boolean isRawString = false;
                    if (ts.movePrevious() && ts.token().id() == PythonTokenId.STRING_BEGIN) {
                        CharSequence begin = ts.token().text();
                        for (int i = 0; i < begin.length(); i++) {
                            char c = begin.charAt(i);
                            if (c == 'r' || c == 'R') {
                                isRawString = true;
                            }
                        }
                    }
                    if (!isRawString) {
                        completeStrings(proposals, request);
                        completionResult.setFilterable(false);
                    }
                    return completionResult;
                }
            }

            if (completeContextual(proposals, request)) {
                return completionResult;
            }

            // See if we should do a simple override completion
            AstPath path = request.path;
            if (path != null && prefix.length() == 0) {
                if (path.leaf() instanceof ClassDef) {
                    // We're in class context...
                    // Just offer override items
                    completeOverrides((ClassDef)path.leaf(), proposals, request);
                    completionResult.setFilterable(false);
                    return completionResult;
                } else if (path.leaf() instanceof FunctionDef && path.leafParent() instanceof ClassDef) {
                    // Special workaround is needed to see if we're between
                    // functions. Unfortunately the offsets for functions aren't
                    // correct; they include whitespace and comments up to the next
                    // function, so I have to account for that.
                    FunctionDef def = (FunctionDef)path.leaf();
                    OffsetRange astRange = PythonAstUtils.getRange(def);
                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(parseResult, astRange);
                    if (lexRange != OffsetRange.NONE) {
                        OffsetRange narrowed = PythonLexerUtils.narrow(doc, lexRange, true);
                        if (!narrowed.containsInclusive(lexOffset)) {
                            completeOverrides((ClassDef)path.leafParent(), proposals, request);
                            completionResult.setFilterable(false);
                            return completionResult;
                        }
                    }
                }
            }

            // Don't do empty-completion for parameters
            // Can't do this yet... requires canFilter() improvement in GSF such that
            // I don't just filter this empty result on the next iteration
            //if (inCall && proposals.size() > 0 && prefix.length() == 0) {
            //    return proposals;
            //}

            if (root == null) {
                completeKeywords(proposals, request);
                return completionResult;
            }

            String fqn = PythonAstUtils.getFqnName(request.path);

            if ((fqn == null) || (fqn.length() == 0)) {
                fqn = "Object"; // NOI18N
            }

            // If we're in a call, add in some info and help for the code completion call
            if (completeParameters(proposals, request)) {
                return completionResult;
            }

            org.netbeans.modules.python.source.lexer.Call call =
                    org.netbeans.modules.python.source.lexer.Call.getCallType(doc, th, lexOffset);
            request.call = call;

            if ((fqn != null) &&
                    completeObjectMethod(proposals, request, fqn, call)) {
                return completionResult;
            }

            // Class code completion: It's a class/exception if we have an uppercase character
            // or if it's a single _ followed by an uppercase character for builtin classes

            if (prefix.length() == 0 || prefix.equals("_") || Character.isUpperCase(prefix.charAt(0)) ||
                    (prefix.charAt(0) == '_' && prefix.length() > 1 && Character.isUpperCase(prefix.charAt(1)))) {
                completeClasses(proposals, request);
            }


            // Only call local and inherited methods if we don't have an LHS, such as Foo::
            if (call.getLhs() == null) {
                if (completeLocal(proposals, request)) {
                    return completionResult;
                }
            }

            if (proposals.size() == 0) {
                // Just complete on all methods regardless of type
                completeMethods(proposals, request);
            }

            completeKeywords(proposals, request);

        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } finally {
            doc.readUnlock();
        }

        return completionResult;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        if (element instanceof CommentElement) {
            // Text is packaged as the name
            String rst = element.getName();
            return RstFormatter.document(rst);
        } else if (element instanceof SpecifyTypeItem) {
            SpecifyTypeItem item = (SpecifyTypeItem)element;
            return NbBundle.getMessage(PythonCodeCompleter.class, "SpecifyTypeHtml", item.call.getLhs());
        }

        return RstFormatter.document(info, element);
    }

    public class CommentElement extends Element {
        private final String text;

        public CommentElement(String text) {
            super();
            this.text = text;
        }

        @Override
        public String getName() {
            return text;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public String getIn() {
            return null;
        }
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        JTextComponent last = EditorRegistry.lastFocusedComponent();
        if (last != null) {
            FileObject fo = GsfUtilities.findFileObject(last);
            if (fo != null) {
                PythonIndex index = PythonIndex.get(fo);
                boolean isMember = link.startsWith("meth:") || link.startsWith("attr:");
                if (isMember || link.startsWith("func:") || link.startsWith("data:")) { // NOI18N
                    String name = link.substring(link.indexOf(':') + 1);
                    int paren = name.indexOf('(');
                    if (paren != -1) {
                        name = name.substring(0, paren);
                    }
                    int dot = name.indexOf('.');
                    String cls = null;
                    if (dot != -1) {
                        cls = name.substring(0, dot);
                        name = name.substring(dot + 1);
                    }
                    Set<IndexedElement> elements;
                    if (isMember) {
                        elements = index.getAllMembers(name, QuerySupport.Kind.EXACT, null, false);
                    } else {
                        elements = index.getAllElements(name, QuerySupport.Kind.EXACT, null, false);
                    }
                    if (elements.size() == 0) {
                        if (isMember) {
                            elements = index.getAllElements(name, QuerySupport.Kind.EXACT, null, false);
                        } else {
                            elements = index.getAllMembers(name, QuerySupport.Kind.EXACT, null, false);
                        }
                    }
                    if (elements.size() > 0) {
                        if (cls != null && cls.length() > 0) {
                            for (IndexedElement element : elements) {
                                if (element.getIn() != null && element.getIn().equals(cls)) {
                                    return element;
                                }
                            }
                        }
                        // Pick the same one as the original element, if any
                        if (originalHandle instanceof IndexedElement) {
                            String oldUrl = ((IndexedElement)originalHandle).getFilenameUrl();
                            for (IndexedElement element : elements) {
                                if (oldUrl.equals(element.getFilenameUrl())) {
                                    return element;
                                }
                            }
                        }
                        return elements.iterator().next();
                    }
                } else if (link.startsWith("class:") || link.startsWith("exc:")) { // NOI18N
                    String name = link.substring(link.indexOf(':') + 1);
                    int paren = name.indexOf('(');
                    if (paren != -1) {
                        name = name.substring(0, paren);
                    }
                    Set<IndexedElement> classes = index.getClasses(name, QuerySupport.Kind.EXACT, null, false);
                    if (classes.size() > 0) {
                        // Pick the same one as the original element, if any
                        if (originalHandle instanceof IndexedElement) {
                            String oldUrl = ((IndexedElement)originalHandle).getFilenameUrl();
                            for (IndexedElement cls : classes) {
                                if (oldUrl.equals(cls.getFilenameUrl())) {
                                    return cls;
                                }
                            }
                        }
                        return classes.iterator().next();
                    }
                } // TODO: Attributes
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPrefix(ParserResult info, int lexOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument)info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return null;
            }

            doc.readLock(); // Read-lock due to token hierarchy use
            try {
                TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, lexOffset);

                if (ts == null) {
                    return null;
                }

                ts.move(lexOffset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }

                if (ts.offset() == lexOffset) {
                    // We're looking at the offset to the RIGHT of the caret
                    // and here I care about what's on the left
                    ts.movePrevious();
                }

                Token<? extends PythonTokenId> token = ts.token();

                if (token != null) {
                    TokenId id = token.id();


                    if (id == PythonTokenId.STRING_BEGIN || id == PythonTokenId.STRING_END ||
                            id == PythonTokenId.STRING_LITERAL) {
                        if (lexOffset > 0) {
                            char prevChar = doc.getText(lexOffset - 1, 1).charAt(0);
                            if (prevChar == '\\') {
                                return "\\";
                            }
                            return "";
                        }
                    }
                }
            } finally {
                doc.readUnlock();
            }
            // Else: normal identifier: just return null and let the machinery do the rest
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        // Default behavior
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '\n' || c == '(' || c == '[' || c == '{') {
            return QueryType.STOP;
        }

        if (c != '.') {
            return QueryType.NONE;
        }

        int offset = component.getCaretPosition();
        BaseDocument doc = (BaseDocument)component.getDocument();

        if (".".equals(typedText)) { // NOI18N
            // See if we're in Ruby context
            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, offset);
            if (ts == null) {
                return QueryType.NONE;
            }
            ts.move(offset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            if (ts.offset() == offset && !ts.movePrevious()) {
                return QueryType.NONE;
            }
            Token<? extends PythonTokenId> token = ts.token();
            TokenId id = token.id();

            // TODO: Ellipsis special casing
            //// "..." is an ellipsis / slicing
            //if (id == PythonTokenId.ELLIPSIS) {
            //    return QueryType.NONE;
            //}

            // TODO - handle embedded ruby
            if ("comment".equals(id.primaryCategory()) || // NOI18N
                    "string".equals(id.primaryCategory())) { // NOI18N
                return QueryType.NONE;
            }

            return QueryType.COMPLETION;
        }

        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        if (parseResult != null) {
            // HACK: The caret offset we're passed in is bogus. It -isn't- the code template
            // insert position. It's the current offset of the caret. And as we're editing,
            // it will keep changing. We don't want that. We want the initial one. As a trick
            // cache it on the caret offset, which will be new for each new code template session,
            // but will stay during a session. The first time we're called, we get the initial
            // offset. Use that from then on.
            int offset = parseResult.getCodeTemplateOffset();
            if (offset == -1) {
                offset = caretOffset;
                parseResult.setCodeTemplateOffset(offset);
            } else {
                caretOffset = offset;
            }
        }

        if ("initialindent".equals(variable)) { // NOI18N
            Document doc = info.getSnapshot().getSource().getDocument(false);
            try {
                int lineStart = IndentUtils.lineStartOffset(doc, Math.min(caretOffset, doc.getLength()));
                int initial = IndentUtils.lineIndent(doc, lineStart);
                return IndentUtils.createIndentString(doc, initial);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        } else if ("indent".equals(variable)) { // NOI18N
            Document doc = info.getSnapshot().getSource().getDocument(false);
            return IndentUtils.createIndentString(doc, IndentUtils.indentLevelSize(doc));
        }
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int lexOffset,
            CompletionProposal proposal) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int astOffset = PythonAstUtils.getAstOffset(info, lexOffset);
        if (!computeMethodCall((PythonParserResult) info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, null)) {

            return ParameterInfo.NONE;
        }

        IndexedMethod method = methodHolder[0];
        if (method == null) {
            return ParameterInfo.NONE;
        }
        int index = paramIndexHolder[0];
        int astAnchorOffset = anchorOffsetHolder[0];
        int anchorOffset = PythonLexerUtils.getLexerOffset((PythonParserResult) info, astAnchorOffset);

        // TODO: Make sure the caret offset is inside the arguments portion
        // (parameter hints shouldn't work on the method call name itself
        // See if we can find the method corresponding to this call
        //        if (proposal != null) {
        //            Element element = proposal.getElement();
        //            if (element instanceof IndexedFunction) {
        //                method = ((IndexedFunction)element);
        //            }
        //        }

        String[] params = method.getParams();

        if ((params != null) && (params.length > 0)) {
            List<String> parameterList = new ArrayList<>();
            for (String param : params) {
                // Filter out self-args
                if (parameterList.size() == 0 && "self".equals(param)) { // NOI18N
                    continue;
                } else {
                    parameterList.add(param);
                }
            }
            return new ParameterInfo(parameterList, index, anchorOffset);
        }

        return ParameterInfo.NONE;
    }

    private boolean completeOverrides(ClassDef classDef, List<CompletionProposal> proposals, CompletionRequest request) throws BadLocationException {
        PythonIndex index = request.index;
        String className = classDef.getInternalName();
        String prefix = request.prefix;
        Set<IndexedElement> methods = index.getInheritedElements(className, prefix, request.kind);

        String searchUrl = request.searchUrl;
        for (IndexedElement element : methods) {
            // Only override methods

            if (element instanceof IndexedMethod) {
                IndexedMethod method = (IndexedMethod)element;
                // For def completion, skip local methods, only include superclass and included
                if (searchUrl.equals(method.getFilenameUrl())) {
                    continue;
                }
                method.setInherited(false);
                // Don't include private or protected methods on other objects
                PythonMethodItem item = new PythonMethodItem(method, request);
                item.setOverride(true);
                // No need to import anything when overriding - we're defining the symbol
                item.setAddImport(""); // NOI18N
                item.setSmart(true);
                proposals.add(item);
            }
        }

        return true;
    }

    private final PythonCompletionItem createItem(Element element, CompletionRequest request) {
        PythonCompletionItem item;
        if (element instanceof IndexedMethod) {
            IndexedMethod method = (IndexedMethod)element;
            item = new PythonMethodItem(method, request);
            item.setSmart(method.isSmart());
        } else if (element instanceof IndexedElement) {
            IndexedElement ie = (IndexedElement)element;
            item = new PythonCompletionItem(request, ie);
            item.setSmart(ie.isSmart());
        } else {
            item = new PythonCompletionItem(element, request);
        }

        return item;
    }

    private final PythonCompletionItem createItem(IndexedElement element, CompletionRequest request) {
        PythonCompletionItem item;
        if (element instanceof IndexedMethod) {
            IndexedMethod method = (IndexedMethod)element;
            item = new PythonMethodItem(method, request);
        } else {
            item = new PythonCompletionItem(request, element);
        }
        String name = element.getName();
        // Internal names should not be shown as smart - don't emphasize these
        boolean smart = element.isSmart() && !(name.startsWith("__") && name.endsWith("__")); // NOI18N

        item.setSmart(smart);

        return item;
    }
    private static final String[] BUILTIN_TYPES =
            new String[]{
        "str", "StringType",
        "tuple", "TupleType",
        "list", "ListType",
        "dict", "DictType",
        "int", "IntegerType",
        "long", "LongType",
        "float", "FloatType",
        "bool", "BooleanType",
        "complex", "ComplexType",
        "unicode", "UnicodeType",
        "file", "FileType",
        "buffer", "BufferType",
        "xrange", "XRangeType",
        "slice", "SliceType",
        "ModuleType", "ModuleType",
        "MethodType", "MethodType",
        "None", "NoneType",
        "object", "ObjectType",};

    private boolean completeComments(List<CompletionProposal> proposals, CompletionRequest request, TokenSequence<? extends PythonTokenId> ts) throws BadLocationException {
        assert ts.token().id() == PythonTokenId.COMMENT;
        TokenSequence<PythonCommentTokenId> embedded = ts.embedded(PythonCommentTokenId.language());
        if (embedded == null) {
            return false;
        }

        embedded.move(request.lexOffset);
        if (embedded.moveNext() || embedded.movePrevious()) {
            Token<? extends PythonCommentTokenId> token = embedded.token();
            TokenId id = token.id();
            TokenId complete = null;
            String prefix = null;
            if (id == PythonCommentTokenId.VARNAME || id == PythonCommentTokenId.TYPE) {
                complete = id;
                prefix = request.prefix;
            } else if (id == PythonCommentTokenId.SEPARATOR) {
                prefix = "";

                // Look backwards to see what we're completing
                if (embedded.movePrevious()) {
                    id = embedded.token().id();
                    if (id == PythonCommentTokenId.VARNAME) {
                        complete = PythonCommentTokenId.TYPE;
                    } else if (id == PythonCommentTokenId.TYPEKEY) {
                        complete = PythonCommentTokenId.VARNAME;
                    }
                }
            }
            if (complete == PythonCommentTokenId.VARNAME) {
                // Complete variable names!
                SymbolTable symbolTable = request.result.getSymbolTable();
                PythonTree scope = PythonAstUtils.getLocalScope(request.path);
                Set<String> names = symbolTable.getVarNames(scope, true);
                for (String name : names) {
                    if (!name.startsWith(prefix)) {
                        continue;
                    }

                    KeywordItem item = new KeywordItem(name, null, request, name);
                    item.setKind(ElementKind.VARIABLE);
                    item.setInsertPrefix(name + ": "); // NOI18N
                    proposals.add(item);
                    item.smart = true;
                }
            } else if (complete == PythonCommentTokenId.TYPE) {
                // Complete type

                // Builtin/core
                for (int j = 0, n = BUILTIN_TYPES.length; j < n; j += 2) {
                    String word = BUILTIN_TYPES[j];
                    String desc = BUILTIN_TYPES[j + 1];

                    if (!word.startsWith(prefix)) {
                        continue;
                    }

                    KeywordItem item = new KeywordItem(word, desc, request, Integer.toString(10000 + j));
                    proposals.add(item);
                    item.smart = true;
                }

                // User defined and library classes
                PythonIndex index = request.index;
                Set<IndexedElement> elements = index.getClasses(prefix, request.kind, request.result, false);
                for (IndexedElement element : elements) {
                    if (element.isNoDoc()) {
                        continue;
                    }

                    PythonCompletionItem item = createItem(element, request);
                    item.setSmart(false);
                    proposals.add(item);
                }

                request.completionResult.setFilterable(false);
            }
        }

        // No other completions here at this point... Perhaps if we support epydoc

        return true;
    }

    /**
     * Study the lexical tokens to the left and see if we're in special completion spots. For example,
     *  after "import" or "from" we should only import modules; after "raise" we should only import
     *  classes extending Exception; after "class N(" we should only complete classes, and after
     *  "def " we should only complete methods in the superclasses.
     *
     * Returns true if we are done, or false otherwise.
     */
    private boolean completeContextual(List<CompletionProposal> proposals, CompletionRequest request) throws BadLocationException {
        PythonIndex index = request.index;
        if (index == null) {
            return false;
        }

        String prefix = request.prefix;
        int lexOffset = request.lexOffset;
        QuerySupport.Kind kind = request.kind;

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(request.doc, lexOffset);
        if (ts == null) {
            return false;
        }
        if (ts.offset() == lexOffset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            if (!ts.movePrevious()) {
                return false;
            }
        }

        Token<? extends PythonTokenId> token = ts.token();
        if (token == null) {
            return false;
        }

        TokenId id = token.id();
        boolean wasDot = id == PythonTokenId.DOT;

        int anchor = request.anchor;
        String library = null;
        int libraryStart = 0;
        while (true) {
            if (id == PythonTokenId.IDENTIFIER || id.primaryCategory().equals(PythonLexer.KEYWORD_CAT)) {
                // Possibly inside the import string
                String tokenText = token.text().toString();
                libraryStart = ts.offset();
                if (library == null) {
                    library = tokenText;
                } else {
                    library = tokenText + "." + library;
                }
            } else if (id != PythonTokenId.DOT) {
                break;
            }
            if (!ts.movePrevious()) {
                return false;
            }
            token = ts.token();
            id = token.id();
        }
        if (library != null) {
            if (wasDot) {
                prefix = library + "."; // NOI18N
            } else {
                prefix = library;
            }
            if (kind == QuerySupport.Kind.PREFIX || kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) {
                anchor = libraryStart;
                int length = lexOffset - libraryStart;
                if (length >= 0 && length < prefix.length()) {
                    prefix = prefix.substring(0, length);
                }
            }
        }

        // TODO - complete only classes inside class Foo(^)
        //if (id == PythonTokenId.LPAREN) {
        //    // Are we in a "class <identifier>(" scenario? If so, complete classes
        //    if (ts.movePrevious() && ts.token().id() == PythonTokenId.IDENTIFIER &&
        //            ts.movePrevious() && ts.token().id() == PythonTokenId.WHITESPACE &&
        //            ts.movePrevious() && ts.token().id() == PythonTokenId.CLASS) {
        //        //completeLocal(proposals, request);
        //        completeClasses(proposals, request);
        //
        //        return true;
        //    }
        //
        //    return false;
        //}

        if (id == PythonTokenId.DECORATOR) {
            completeDecorators(proposals, request);
            return true;
        }

        if (id != PythonTokenId.ERROR && id != PythonTokenId.NEWLINE &&
                id != PythonTokenId.WHITESPACE) {
            return false;
        }

        if (!ts.movePrevious()) {
            return false;
        }
        token = ts.token();
        id = token.id();
        // TODO - make this works for package paths!
        if (id == PythonTokenId.IMPORT || id == PythonTokenId.FROM) {
            if (id == PythonTokenId.IMPORT && ts.movePrevious() && ts.token().id() == PythonTokenId.WHITESPACE && ts.movePrevious()) {
                // See if this was "from foo import bar" such that we really should
                // be listing symbols inside the foo library
                token = ts.token();
                id = token.id();
                library = null;
                libraryStart = 0;
                while (true) {
                    if (id == PythonTokenId.IDENTIFIER || id.primaryCategory().equals(PythonLexer.KEYWORD_CAT)) {
                        // Possibly inside the import string
                        String tokenText = token.text().toString();
                        libraryStart = ts.offset();
                        if (library == null) {
                            library = tokenText;
                        } else {
                            library = tokenText + "." + library;
                        }
                    } else if (id != PythonTokenId.DOT) {
                        break;
                    }
                    if (!ts.movePrevious()) {
                        return false;
                    }
                    token = ts.token();
                    id = token.id();
                }
                if (library != null) {
                    if (id == PythonTokenId.WHITESPACE &&
                            ts.movePrevious() && ts.token().id() == PythonTokenId.FROM) {

                        boolean isFutureImport = "__future__".equals(library); // NOI18N
                        if ("*".equals(prefix) || (prefix.length() == 0 && !isFutureImport)) { // NOI18N
                            KeywordItem item = new KeywordItem("*", "Import All Exported Symbols", request, "*");
                            proposals.add(item);
                            item.sortPrioOverride = -10000;
                            if (prefix.length() > 0) {
                                // No other possible matches
                                return true;
                            }
                        }
                        Set<IndexedElement> symbols = index.getImportedElements(prefix, request.kind,
                                Collections.<String>singleton(library), null);
                        for (IndexedElement symbol : symbols) {
                            if (!symbol.isPublic()) {
                                continue;
                            }
                            String moduleName = symbol.getName();
                            if (moduleName == null) {
                                continue;
                            }
                            if (!moduleName.startsWith(prefix)) {
                                continue;
                            }

                            // The __future__ module imports some stuff we don't want to see in imports...
                            if (isFutureImport) {
                                char first = moduleName.charAt(0);
                                if (first == '_' || Character.isUpperCase(first)) {
                                    continue;
                                }
                            }

                            PythonCompletionItem item = createItem(symbol, request);
                            item.setSmart(true);
                            item.setAddImport(""); // No extra imports of these
                            item.setAnchorOffset(anchor);
                            item.setInImport(true);
                            proposals.add(item);
                        }
                        request.completionResult.setFilterable(false);

                        return true;
                    }
                }
            }

            Set<IndexedElement> modules = index.getModules(prefix, request.kind);
            for (IndexedElement module : modules) {
                String moduleName = module.getName();
                if (moduleName == null) {
                    continue;
                }
                if (!moduleName.startsWith(prefix)) {
                    continue;
                }
                KeywordItem item = new KeywordItem(moduleName, null, request, moduleName.toLowerCase());
                item.setHandle(module);
                //PythonCompletionItem item = new PythonCompletionItem(request, module);
                //item.setSmart(true);
                item.setAnchorOffset(anchor);
                item.deprecated = module.isDeprecated();
                proposals.add(item);
            }
            request.completionResult.setFilterable(false);


            return true;
        } else if (id == PythonTokenId.RAISE || id == PythonTokenId.EXCEPT) {
            Set<IndexedElement> classes = index.getExceptions(prefix, kind);
            for (IndexedElement clz : classes) {
                if (clz.isNoDoc()) {
                    continue;
                }

                clz.setInherited(true);
                PythonCompletionItem item = createItem(clz, request);
                item.setSmart(clz.isSmart());
                item.setAnchorOffset(anchor);
                proposals.add(item);
            }

            request.completionResult.setFilterable(false);
            return true;

        } else if (id == PythonTokenId.DEF && request.path != null) {
            // We're doing def-completion. See if we're in a class, and if so, provide the overriding
            // methods.
            AstPath path = request.path;
            ClassDef classDef = PythonAstUtils.getClassDef(path);
            if (classDef != null) {
                completeOverrides(classDef, proposals, request);
                return true;
            }
        }

        return false;
    }

    private boolean completeStrings(List<CompletionProposal> proposals, CompletionRequest request) throws BadLocationException {
        String prefix = request.prefix;
        for (int j = 0, n = STRING_ESCAPES.length; j < n; j += 2) {
            String word = STRING_ESCAPES[j];
            String desc = STRING_ESCAPES[j + 1];

            if (!word.startsWith(prefix)) {
                continue;
            }

            KeywordItem item = new KeywordItem(word, desc, request, Integer.toString(10000 + j));
            proposals.add(item);
        }

        return true;
    }

    private boolean completeLocal(List<CompletionProposal> proposals, CompletionRequest request) {
        PythonParserResult info = request.result;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;
        org.netbeans.modules.python.source.lexer.Call call = request.call;

        // Only call local and inherited methods if we don't have an LHS, such as Foo::
        if (call.getLhs() == null) {

            SymbolTable symbolTable = request.result.getSymbolTable();
            PythonTree scope = PythonAstUtils.getLocalScope(request.path);
            Set<Element> elements = symbolTable.getDefinedElements(info, scope, prefix, kind);
            String searchUrl = request.searchUrl;
            for (Element element : elements) {
                PythonCompletionItem item;
                if (element instanceof IndexedMethod) {
                    IndexedMethod method = (IndexedMethod)element;
                    if (method.isPrivate() && !searchUrl.equals(method.getFilenameUrl())) {
                        continue;
                    }
                    // Don't include private or protected methods on other objects
                    item = new PythonMethodItem(method, request);
                } else if (element instanceof IndexedElement) {
                    IndexedElement ie = (IndexedElement)element;
                    if (ie.isPrivate() && !searchUrl.equals(ie.getFilenameUrl())) {
                        continue;
                    }
                    item = new PythonCompletionItem(request, ie);
                    item.setSmart(ie.isSmart());
                } else {
                    item = new PythonCompletionItem(element, request);
                }
                proposals.add(item);

            }
//            
//            // TODO: Add local functions!
//            // ...perhaps I can just search by URL ?
//            // I need to also pull in imported stuff
//            ImportManager manager = new ImportManager(request.info);
//            List<Import> imports = manager.getImports();
//            List<ImportFrom> importsFrom = manager.getImportsFrom();
//            Set<IndexedElement> elements = index.getImportedElements(prefix, kind, PythonIndex.ALL_SCOPE, imports, importsFrom);
//
//            for (IndexedElement element : elements) {
//                if (!element.isPublic()) { // Don't import private elements
//                    continue;
//                }
//
//                PythonCompletionItem item;
//                if (element instanceof IndexedMethod) {
//                    IndexedMethod method = (IndexedMethod)element;
//                    item = new PythonMethodItem(method, request);
//                } else {
//                    item = new PythonCompletionItem(request, element);
//                }
//                item.setSmart(element.isSmart());
//                proposals.add(item);
//            }
//
//            // TODO - pull this into a completeInheritedMethod call
//            // Complete inherited methods or local methods only (plus keywords) since there
//            // is no receiver so it must be a local or inherited method call
//            Set<IndexedMethod> inheritedMethods =
//                index.getInheritedMethods(fqn, prefix, kind);
//
//            // Handle action view completion for RHTML and Markaby files
//            for (IndexedMethod method : inheritedMethods) {
//                // This should not be necessary - filtering happens in getInheritedMethods right?
//                if ((prefix.length() > 0) && !method.getName().startsWith(prefix)) {
//                    continue;
//                }
//
//                if (method.isNoDoc()) {
//                    continue;
//                }
//
//                // If a method is an "initialize" method I should do something special so that
//                // it shows up as a "constructor" (in a new() statement) but not as a directly
//                // callable initialize method (it should already be culled because it's private)
//
//                if (method.isNoDoc()) {
//                    continue;
//                }
//
//                PythonMethodItem item = new PythonMethodItem(method, request);
//                item.setSmart(method.isSmart());
////                    item.setSmart(true);
//                proposals.add(item);
//            }
        }

        return false;
    }

    /** Determine if we're trying to complete the name of a method on another object rather
     * than an inherited or local one. These should list ALL known methods, unless of course
     * we know the type of the method we're operating on (such as strings or regexps),
     * or types inferred through data flow analysis
     *
     * @todo Look for self or this or super; these should be limited to inherited.
     */
    private boolean completeObjectMethod(List<CompletionProposal> proposals, CompletionRequest request, String fqn,
            org.netbeans.modules.python.source.lexer.Call call) {

        PythonIndex index = request.index;
        String prefix = request.prefix;
        int astOffset = request.astOffset;
        int lexOffset = request.lexOffset;
        TokenHierarchy<Document> th = request.th;
        BaseDocument doc = request.doc;
        AstPath path = request.path;
        QuerySupport.Kind kind = request.kind;
        FileObject fileObject = request.fileObject;
        PythonTree node = request.node;

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(th, lexOffset);

        // Look in the token stream for constructs of the type
        //   foo.x^
        // or
        //   foo.^
        // and if found, add all methods
        // (no keywords etc. are possible matches)
        if ((index != null) && (ts != null)) {
            boolean skipPrivate = true;

            if ((call == org.netbeans.modules.python.source.lexer.Call.LOCAL) ||
                    (call == org.netbeans.modules.python.source.lexer.Call.NONE)) {
                return false;
            }

            // If we're not sure we're only looking for a method, don't abort after this
            boolean done = call.isMethodExpected();

            boolean skipInstanceMethods = call.isStatic();

            Set<IndexedElement> elements = Collections.emptySet();

            String type = call.getType();
            String lhs = call.getLhs();
            boolean addSpecifyTypeItem = false;

            if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                PythonTree method = PythonAstUtils.getLocalScope(path);

                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    PythonTypeAnalyzer analyzer = new PythonTypeAnalyzer(request.result, index, method, node, astOffset, lexOffset, fileObject);
                    type = analyzer.getType(lhs);

                    if (type == null) {
                        addSpecifyTypeItem = true;
                    }
                }
            }

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if ((type != null) && (type.length() > 0)) {
                if ("self".equals(lhs)) { // NOI18N
                    type = fqn;
                    skipPrivate = false;
//                } else if ("super".equals(lhs)) { // NOI18N
//                    skipPrivate = true;
//
//                    IndexedClass sc = index.getSuperclass(fqn);
//
//                    if (sc != null) {
//                        type = sc.getFqn();
//                    } else {
//                        ClassNode cls = PythonAstUtils.findClass(path);
//
//                        if (cls != null) {
//                            type = PythonAstUtils.getSuperclass(cls);
//                        }
//                    }
//
//                    if (type == null) {
//                        type = "Object"; // NOI18N
//                    }
                }

                if ((type != null) && (type.length() > 0)) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
                    //while (elements.isEmpty()) {
                    //    elements = index.getInheritedElements(fqn + "::" + type, prefix, kind);
                    //
                    //    int f = fqn.lastIndexOf("::");
                    //
                    //    if (f == -1) {
                    //        break;
                    //    } else {
                    //        fqn = fqn.substring(0, f);
                    //    }
                    //}

                    // Add methods in the class (without an FQN)
                    //Set<IndexedElement> m = index.getInheritedElements(type, prefix, kind);
                    elements = index.getInheritedElements(type, prefix, kind);

                    //if (!m.isEmpty()) {
                    //    elements.addAll(m);
                    //}
                }
            }

            if (/*type == null && */lhs != null) {
                boolean found = false;
                boolean alreadyImported = false;
                // See if it's an attempt to use a library, but we failed to import it
                // such as "sys.x" - access x in the sys module

                String moduleName = null;
                boolean moduleCompletion = true;
                SymbolTable symbolTable = request.result.getSymbolTable();
                if (moduleName == null) {
                    List<Import> imports = symbolTable.getImports();
                    if (imports != null && imports.size() > 0) {
                        for (Import imp : imports) {
                            List<alias> names = imp.getInternalNames();
                            if (names != null) {
                                for (alias at : names) {
                                    if (at.getInternalAsname() != null && at.getInternalAsname().equals(lhs)) {
                                        addSpecifyTypeItem = false;

                                        // Yes, imported symbol
                                        moduleName = at.getInternalName();
                                        alreadyImported = true;
                                        moduleCompletion = true;
                                        break;
                                    } else if (at.getInternalName().equals(lhs)) {
                                        addSpecifyTypeItem = false;

                                        if (at.getInternalAsname() != null) {
                                            moduleCompletion = false;
                                        } else {
                                            moduleName = at.getInternalName();
                                            alreadyImported = true;
                                            moduleCompletion = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    List<ImportFrom> importsFrom = symbolTable.getImportsFrom();
                    if (importsFrom != null && importsFrom.size() > 0) {
                        for (ImportFrom imp : importsFrom) {
                            List<alias> names = imp.getInternalNames();
                            if (names != null) {
                                for (alias at : names) {
                                    String internalName = at.getInternalName();
                                    if (Character.isUpperCase(internalName.charAt(0))) {
                                        continue;
                                    }
                                    if (at.getInternalAsname() != null && at.getInternalAsname().equals(lhs)) {
                                        addSpecifyTypeItem = false;

                                        // Yes, imported symbol
                                        moduleName = imp.getInternalModule() + "." + internalName; // NOI18N
                                        alreadyImported = true;
                                        moduleCompletion = true;
                                        break;
                                    } else if (internalName.equals(lhs)) {
                                        addSpecifyTypeItem = false;

                                        if (at.getInternalAsname() != null) {
                                            moduleCompletion = false;
                                        } else {
                                            moduleName = imp.getInternalModule() + "." + internalName; // NOI18N
                                            alreadyImported = true;
                                            moduleCompletion = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (moduleName == null) {
                    moduleName = lhs;
                }
                if (moduleCompletion) {
                    Set<IndexedElement> modules = index.getModules(moduleName, QuerySupport.Kind.EXACT);
                    if (modules.size() > 0) {
                        Set<IndexedElement> symbols = index.getImportedElements(prefix, request.kind,
                                Collections.<String>singleton(moduleName), null);
                        if (symbols.size() > 0) {
                            if (elements != null && elements.size() > 0) {
                                symbols.addAll(elements);
                            }
                            found = true;
                            for (IndexedElement element : symbols) {
                                PythonCompletionItem item = createItem(element, request);
                                item.setSmart(true);
                                if (!alreadyImported) {
                                    item.setAddImport(lhs);
                                }
                                proposals.add(item);
                            }
                        }
                    }
                }

                // Perhaps we're doing package completion, e.g. something like
                //   application = django.core.handl^
                Set<IndexedPackage> packages = index.getPackages(lhs + "." + prefix, kind); // NOI18N
                if (packages.size() > 0) {
                    found = true;
                    for (IndexedPackage element : packages) {
                        PythonPackageItem item = new PythonPackageItem(element, request);
                        item.setSmart(true);
                        proposals.add(item);
                    }
                    request.completionResult.setFilterable(false);
                }

                if (found) {
                    return true;
                }
            }

            // Try just the method call (e.g. across all classes). This is ignoring the
            // left hand side because we can't resolve it.
            if ((elements.isEmpty())) {
                elements = index.getAllMembers(prefix, kind, request.result, false);

                if (addSpecifyTypeItem) {
                    // Add a special code completion item to TELL us the type
                    proposals.add(new SpecifyTypeItem(request, call, lexOffset));
                }
            }

            for (IndexedElement element : elements) {
                if (element.isNoDoc()) {
                    continue;
                }

                if (skipPrivate && !element.isPublic() && element.getKind() != ElementKind.CONSTRUCTOR) {
                    continue;
                }

                if (skipInstanceMethods && !element.isStatic()) {
                    continue;
                }

                PythonCompletionItem item = createItem(element, request);
                proposals.add(item);
            }

            return done;
        }

        return false;
    }

    private boolean completeClasses(List<CompletionProposal> proposals, CompletionRequest request) {
        PythonIndex index = request.index;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;
        String searchUrl = request.searchUrl;

        Set<IndexedElement> classes = index.getClasses(prefix, kind, request.result, false);
        for (IndexedElement clz : classes) {
            if (clz.isNoDoc()) {
                continue;
            }
            if (clz.isPrivate() && !searchUrl.equals(clz.getFilenameUrl())) {
                // Only include private classes if they're from this module
                continue;
            }

            PythonCompletionItem item = createItem(clz, request);
            item.setSmart(clz.isSmart());
            proposals.add(item);
        }

        return true;
    }

    private boolean completeMethods(List<CompletionProposal> proposals, CompletionRequest request) {
        PythonIndex index = request.index;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;

        Set<IndexedElement> elements = index.getAllElements(prefix, kind, request.result, false);
        if (request.call.getLhs() != null || elements.size() == 0) {
            Set<IndexedElement> members = index.getAllMembers(prefix, kind, request.result, false);
            if (members.size() > 0) {
                elements.addAll(members);
            }
        }
        for (IndexedElement element : elements) {
            if (element.isNoDoc()) {
                continue;
            }

            PythonCompletionItem item = createItem(element, request);
            item.setSmart(true);
            proposals.add(item);
        }

        return true;
    }

    /**
     * Complete decorators. These are functions that (a) take a function as an argument, and
     * (b) return a function. Some may even take additional arguments.
     * See http://www.python.org/dev/peps/pep-0318
     */
    private boolean completeDecorators(List<CompletionProposal> proposals, CompletionRequest request) throws BadLocationException {
        PythonIndex index = request.index;
        String prefix = request.prefix;
        QuerySupport.Kind kind = request.kind;

        boolean found = false;
        Set<IndexedElement> elements = index.getAllElements(prefix, kind, request.result, false);
        for (IndexedElement element : elements) {
            if (element.isNoDoc()) {
                continue;
            }
            if (!(element instanceof IndexedMethod)) {
                continue;
            }
            // Filter out anything that doesn't take at least one argument
            IndexedMethod method = (IndexedMethod)element;
            String[] params = method.getParams();
            if (params == null || params.length != 1) {
                continue;
            }
            String name = params[0];
            if (!name.startsWith("func")) { // NOI18N
                continue;
            }

            // TODO - filter out anything that doesn't return a method
            PythonCompletionItem item = new PythonCompletionItem(request, element);
            item.setSmart(true);

            proposals.add(item);
            found = true;
        }

        if (!found) {
            // No matches - add all functions regardless of argument names
            for (IndexedElement element : elements) {
                if (element.isNoDoc()) {
                    continue;
                }
                if (!(element instanceof IndexedMethod)) {
                    continue;
                }
                // Filter out anything that doesn't take at least one argument
                IndexedMethod method = (IndexedMethod)element;
                String[] params = method.getParams();
                if (params == null || params.length < 1) {
                    continue;
                }

                // TODO - filter out anything that doesn't return a method

                PythonCompletionItem item = createItem(element, request);
                item.setSmart(true);
                proposals.add(item);
            }
        }

        request.completionResult.setFilterable(false);

        return true;
    }
    
    // According to https://hg.python.org/cpython/file/3.5/Lib/keyword.py
    // and https://hg.python.org/cpython/file/2.7/Lib/keyword.py
    // duplicate in org.netbeans.modules.python.source.PythonUtils
    static final String[] PYTHON_KEYWORDS = new String[]{
        "False", // NOI18N
        "None", // NOI18N
        "True", // NOI18N
        "and", // NOI18N
        "as", // NOI18N
        "assert", // NOI18N
        "break", // NOI18N
        "class", // NOI18N
        "continue", // NOI18N
        "def", // NOI18N
        "del", // NOI18N
        "elif", // NOI18N
        "else", // NOI18N
        "except", // NOI18N
        "finally", // NOI18N
        "for", // NOI18N
        "from", // NOI18N
        "global", // NOI18N
        "if", // NOI18N
        "import", // NOI18N
        "in", // NOI18N
        "is", // NOI18N
        "lambda", // NOI18N
        "nonlocal", // NOI18N
        "not", // NOI18N
        "or", // NOI18N
        "pass", // NOI18N
        "raise", // NOI18N
        "return", // NOI18N
        "try", // NOI18N
        "while", // NOI18N
        "with", // NOI18N
        "yield", // NOI18N
        "async", // NOI18N, Python 3.5 only
        "await", // NOI18N, Python 3.5 only
        "exec", // NOI18N, Python 2 only
        "print", // NOI18N, Pytohn 2 only, function in python 3
    };

    private void completeKeywords(List<CompletionProposal> proposals, CompletionRequest request) {
        // No keywords possible in the RHS of a call (except for "this"?)
//        if (request.call.getLhs() != null) {
//            return;
//        }

        String prefix = request.prefix;


        for (int i = 0, n = PYTHON_KEYWORDS.length; i < n; i++) {
            String keyword = PYTHON_KEYWORDS[i];
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, request, Integer.toString(10000 + i));

                proposals.add(item);
            }
        }
    }

    private boolean completeParameters(List<CompletionProposal> proposals, CompletionRequest request) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        @SuppressWarnings("unchecked")
        Set<IndexedMethod>[] alternatesHolder = new Set[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        PythonParserResult info = request.result;
        int lexOffset = request.lexOffset;
        int astOffset = request.astOffset;

        if (!computeMethodCall(info, lexOffset, astOffset,
                methodHolder, paramIndexHolder, anchorOffsetHolder, alternatesHolder)) {
            request.inCall = false;

            return false;
        }

        request.inCall = true;

        IndexedMethod targetMethod = methodHolder[0];
        int index = paramIndexHolder[0];

        CallItem callItem = new CallItem(targetMethod, index, request);
        proposals.add(callItem);
        // Also show other documented, not nodoc'ed items (except for those
        // with identical signatures, such as overrides of the same method)
        if (alternatesHolder[0] != null) {
            Set<String> signatures = new HashSet<>();
            signatures.add(targetMethod.getSignature().substring(targetMethod.getSignature().indexOf('#') + 1));
            for (IndexedMethod m : alternatesHolder[0]) {
                if (m != targetMethod && m.isDocumented() && !m.isNoDoc()) {
                    String sig = m.getSignature().substring(m.getSignature().indexOf('#') + 1);
                    if (!signatures.contains(sig)) {
                        CallItem item = new CallItem(m, index, request);
                        proposals.add(item);
                        signatures.add(sig);
                    }
                }
            }
        }

        return false;
    }
    private static int callLineStart = -1;
    private static IndexedMethod callMethod;

    /** Compute the current method call at the given offset. Returns false if we're not in a method call.
     * The argument index is returned in parameterIndexHolder[0] and the method being
     * called in methodHolder[0].
     */
    static boolean computeMethodCall(PythonParserResult info, int lexOffset, int astOffset,
            IndexedMethod[] methodHolder, int[] parameterIndexHolder, int[] anchorOffsetHolder,
            Set<IndexedMethod>[] alternativesHolder) {
        try {
            PythonTree root = PythonAstUtils.getRoot(info);

            if (root == null) {
                return false;
            }

            IndexedMethod targetMethod = null;
            int index = -1;

            AstPath path = null;
            // Account for input sanitation
            // TODO - also back up over whitespace, and if I hit the method
            // I'm parameter number 0
            int originalAstOffset = astOffset;

            // Adjust offset to the left
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return false;
            }
            lexOffset = Math.min(lexOffset, doc.getLength());
            int newLexOffset = PythonLexerUtils.findSpaceBegin(doc, lexOffset);
            if (newLexOffset < lexOffset) {
                astOffset -= (lexOffset - newLexOffset);
            }

            PythonParserResult rpr = PythonAstUtils.getParseResult(info);
            OffsetRange range = rpr.getSanitizedRange();
            if (range != OffsetRange.NONE && range.containsInclusive(astOffset)) {
                if (astOffset != range.getStart()) {
                    astOffset = range.getStart() - 1;
                    if (astOffset < 0) {
                        astOffset = 0;
                    }
                    path = AstPath.get(root, astOffset);
                }
            }

            if (path == null) {
                path = AstPath.get(root, astOffset);
            }

            int currentLineStart = Utilities.getRowStart(doc, lexOffset);
            if (callLineStart != -1 && currentLineStart == callLineStart) {
                // We know the method call
                targetMethod = callMethod;
                if (targetMethod != null) {
                    // Somehow figure out the argument index
                    // Perhaps I can keep the node tree around and look in it
                    // (This is all trying to deal with temporarily broken
                    // or ambiguous calls.
                }
            }
            // Compute the argument index

            org.python.antlr.ast.Call call = null;
            int anchorOffset = -1;

            if (targetMethod != null) {
                Iterator<PythonTree> it = path.leafToRoot();
                String name = targetMethod.getName();
                while (it.hasNext()) {
                    PythonTree node = it.next();

                    if (node.getClass() == FunctionDef.class) {
                        // See for example issue 149001
                        // If the call is outside the current function scope,
                        // we don't want to include it!
                        break;
                    }

                    if (node.getClass() == org.python.antlr.ast.Call.class) {
                        org.python.antlr.ast.Call c = (org.python.antlr.ast.Call)node;
                        if (name.equals(PythonAstUtils.getCallName(c))) {
                            call = c;
                            index = PythonAstUtils.findArgumentIndex(call, astOffset, path);
                            break;
                        }
                    }
                }
            }

            boolean haveSanitizedComma = rpr.getSanitized() == Sanitize.EDITED_DOT ||
                    rpr.getSanitized() == Sanitize.ERROR_DOT;
            if (haveSanitizedComma) {
                // We only care about removed commas since that
                // affects the parameter count
                if (rpr.getSanitizedContents() != null &&
                        rpr.getSanitizedContents().indexOf(',') == -1) {
                    haveSanitizedComma = false;
                }
            }

            if (call == null) {
                // Find the call in around the caret. Beware of
                // input sanitization which could have completely
                // removed the current parameter (e.g. with just
                // a comma, or something like ", @" or ", :")
                // where we accidentally end up in the previous
                // parameter.
                ListIterator<PythonTree> it = path.leafToRoot();
                nodesearch:
                while (it.hasNext()) {
                    PythonTree node = it.next();

                    if (node.getClass() == FunctionDef.class) {
                        // See for example issue 149001
                        // If the call is outside the current function scope,
                        // we don't want to include it!
                        break;
                    }

                    if (node.getClass() == org.python.antlr.ast.Call.class) {
                        call = (org.python.antlr.ast.Call)node;
                        index = PythonAstUtils.findArgumentIndex(call, astOffset, path);
                        break;
                    }
                }
            }

            if (index != -1 && haveSanitizedComma && call != null) {
                // Adjust the index to account for our removed
                // comma
                index++;
            }

//            String fqn = null;
            if ((call == null) || (index == -1)) {
                callLineStart = -1;
                callMethod = null;
                return false;
            } else if (targetMethod == null) {
                targetMethod = new PythonDeclarationFinder().findMethodDeclaration((PythonParserResult) info, call, path,
                        alternativesHolder);
                if (targetMethod == null) {
                    return false;
                }
            }

            callLineStart = currentLineStart;
            callMethod = targetMethod;

            // TODO - make dedicated result object?
            methodHolder[0] = callMethod;
            parameterIndexHolder[0] = index;
            // TODO - store the fqn too?

            if (call != null && callMethod != null) {
                // Try to set the anchor on the arg list instead of on the
                // call itself
                List<expr> args = call.getInternalArgs();
                if (args != null && args.size() > 0) {
                    anchorOffset = PythonAstUtils.getRange(args.get(0)).getStart();
                }
            }

            if (anchorOffset == -1) {
                anchorOffset = PythonAstUtils.getRange(call).getStart();
            }
            anchorOffsetHolder[0] = anchorOffset;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return false;
        }

        return true;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private static class CompletionRequest {
        private DefaultCompletionResult completionResult;
        private TokenHierarchy<Document> th;
        private AstPath path;
        private PythonTree node;
        private PythonTree root;
        private int anchor;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix;
        private PythonIndex index;
        private QuerySupport.Kind kind;
        private PythonParserResult result;
        private QueryType queryType;
        private FileObject fileObject;
        private org.netbeans.modules.python.source.lexer.Call call;
        private boolean inCall;
        private String fqn;
        private String searchUrl;
    }

    private static class PythonMethodItem extends PythonCompletionItem {
        protected IndexedMethod method;
        protected boolean override;

        protected PythonMethodItem(IndexedMethod method, CompletionRequest request) {
            super(request, method);
            this.method = method;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        @Override
        public String getCustomInsertTemplate() {
            String[] params = method.getParams();
            if (params == null || isInImport()) {
                return getInsertPrefix() + "${cursor}"; // NOI18N
            }

            int paramLength = params.length;
            StringBuilder sb = new StringBuilder();

            if (override) {
                // Insert leading def ?
                BaseDocument doc = request.doc;
                try {
                    int lineStart = Utilities.getRowFirstNonWhite(doc, anchorOffset);
                    if (lineStart != -1) {
                        String text = doc.getText(lineStart, Math.min(doc.getLength() - lineStart, 4));
                        if (!text.equals("def ")) { // NOI18N
                            lineStart = -1;
                        }
                    }
                    if (lineStart == -1) {
                        sb.append("def "); // NOI18N
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }

            sb.append(getInsertPrefix());
            String[] delimiters = getParamListDelimiters();
            assert delimiters.length == 2;
            sb.append(delimiters[0]);
            int id = 0;
            for (String paramDesc : params) {
                if (id == 0 && paramDesc.equals("self") && element.getIn() != null && !override) { // NOI18N
                    // Don't insert self parameters when -calling- methods
                    paramLength--;
                    continue;
                }
                sb.append("${"); //NOI18N
                // Ensure that we don't use one of the "known" logical parameters
                // such that a parameter like "path" gets replaced with the source file
                // path!
                sb.append("gsf-cc-"); // NOI18N
                sb.append(Integer.toString(id++));
                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc);
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                if (id < paramLength) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(delimiters[1]);
            if (override) {
                sb.append(":\n"); // NOI18N
                int indent;
                try {
                    BaseDocument doc = request.doc;
                    indent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(doc, anchorOffset));
                    sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            sb.append("${cursor}"); // NOI18N

            return sb.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.reset();

            boolean strike = false;
            if (indexedElement != null && indexedElement.isDeprecated()) {
                strike = true;
            }
            if (strike) {
                formatter.deprecated(true);
            }

            boolean emphasize = (kind != ElementKind.PACKAGE && indexedElement != null) ? !indexedElement.isInherited() : false;
            if (emphasize) {
                formatter.emphasis(true);
            }

            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);
            if (emphasize) {
                formatter.emphasis(false);
            }

            String[] params = method.getParams();
            formatter.appendHtml("("); // NOI18N
            boolean first = true;
            for (String param : params) {
                if (first) {
                    if (param.equals("self") && element.getIn() != null) { // NOI18N
                        // The first self parameter isn't really visible - you don't
                        // use it when calling, it's done by Python
                        continue;
                    }

                    first = false;
                } else {
                    formatter.appendText(", "); // NOI18N
                }

                formatter.parameters(true);
                formatter.appendText(param);
                formatter.parameters(false);
            }

            formatter.appendHtml(")"); // NOI18N

            if (strike) {
                formatter.deprecated(false);
            }

            if (override) {
                formatter.appendHtml(" - override");
            }

//            if (indexedElement != null) {
//                String type = indexedElement.getType();
//                if (type != null && type != PythonTree.UNKNOWN_TYPE) {
//                    formatter.appendHtml(" : "); // NOI18N
//                    formatter.appendText(JsUtils.normalizeTypeString(type));
//                }
//            }

            return formatter.getText();
        }
    }

    private static class PythonCompletionItem extends DefaultCompletionProposal {
        protected CompletionRequest request;
        protected Element element;
        protected IndexedElement indexedElement;
        protected short smartFlag;
        private String addImport;
        private boolean inImport;

        private PythonCompletionItem(Element element, CompletionRequest request) {
            this.element = element;
            this.request = request;
            this.anchorOffset = request.anchor;

            // Should be a PythonMethodItem:
            //assert this instanceof PythonMethodItem || (element.getKind() != ElementKind.METHOD && element.getKind() != ElementKind.CONSTRUCTOR) : element;
        }

        private PythonCompletionItem(CompletionRequest request, IndexedElement element) {
            this(element, request);
            this.indexedElement = element;
        }

        public String getAddImport() {
            return addImport;
        }

        public void setAddImport(String addImport) {
            this.addImport = addImport;
        }

        public boolean isInImport() {
            return inImport;
        }

        public void setInImport(boolean inImport) {
            this.inImport = inImport;
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public ElementHandle getElement() {
            // XXX Is this called a lot? I shouldn't need it most of the time
            return element;
        }

        @Override
        public ElementKind getKind() {
            return element.getKind();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.reset();
            boolean emphasize = (kind != ElementKind.PACKAGE && indexedElement != null) ? !indexedElement.isInherited() : false;
            if (emphasize) {
                formatter.emphasis(true);
            }

            boolean strike = false;
            if (indexedElement != null && indexedElement.isDeprecated()) {
                strike = true;
            }
            if (strike) {
                formatter.deprecated(true);
            }

            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);

            // For decorators we use completion items
            //assert element.getKind() != ElementKind.METHOD && element.getKind() != ElementKind.CONSTRUCTOR : element; // Should be in a PythonMethodItem

            if (strike) {
                formatter.deprecated(false);
            }
            if (emphasize) {
                formatter.emphasis(false);
            }

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            //if (element.getKind() == ElementKind.PACKAGE || element.getKind() == ElementKind.CLASS) {
            //    if (element instanceof IndexedElement) {
            //        String origin = ((IndexedElement)element).getOrigin();
            //        if (origin != null) {
            //            formatter.appendText(origin);
            //            return formatter.getText();
            //        }
            //    }
            //
            //    return null;
            //}

            if (indexedElement != null && indexedElement.getRhs() != null) {
                return indexedElement.getRhs();
            }

            String in = element.getIn();

            if (in != null) { // NOI18N
                formatter.appendText(in);
                return formatter.getText();
            } else if (element instanceof IndexedElement) {
                IndexedElement ie = (IndexedElement)element;
                String filename = ie.getFilenameUrl();
                if (filename != null) {
                    if (!filename.contains("pythonstubs")) { // NOI18N
                        int index = filename.lastIndexOf('/');
                        if (index != -1) {
                            filename = filename.substring(index + 1);
                        }
                        formatter.appendText(filename);
                        return formatter.getText();
                    } else {
                        String origin = ie.getOrigin();
                        if (origin != null) {
                            formatter.appendText(origin);
                            return formatter.getText();
                        }
                    }
                }

                return null;
            }

            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = this.getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        @Override
        public boolean isSmart() {
            if (smartFlag == 0) {
                return indexedElement != null ? indexedElement.isSmart() : true;
            } else {
                return smartFlag != -1;
            }
        }

        @Override
        public void setSmart(boolean smartFlag) {
            this.smartFlag = smartFlag ? 1 : (short)-1;
        }

        @Override
        public List<String> getInsertParams() {
            return null;
        }

        @Override
        public String[] getParamListDelimiters() {
            return new String[]{"(", ")"}; // NOI18N
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }

        @Override
        public ImageIcon getIcon() {
            if (element.getKind() == ElementKind.CLASS && element.getModifiers().contains(Modifier.PRIVATE)) {
                // GSF doesn't automatically handle icons on private classes, so I have to
                // work around that here
                return ImageUtilities.loadImageIcon("org/netbeans/modules/python/editor/resources/private-class.png", false); //NOI18N
            }

            return null;
        }
    }

    private static final class PythonPackageItem extends PythonCompletionItem {
        private IndexedPackage pkg;

        private PythonPackageItem(IndexedPackage pkg, CompletionRequest request) {
            super(request, pkg);
            this.pkg = pkg;
        }

        @Override
        public String getInsertPrefix() {
            if (pkg.hasMore()) {
                return pkg.getName() + "."; // NOI18N
            } else {
                return pkg.getName();
            }
        }

        @Override
        public String getLhsHtml(final HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(pkg.getPkg());
            formatter.appendText(" "); // NOI18N
            formatter.name(kind, false);
            return formatter.getText();
        }
//        @Override
//        public String getCustomInsertTemplate() {
//            String[] params = method.getParams();
//            if (params == null) {
//                return getInsertPrefix();
//            }
//
//            StringBuilder sb = new StringBuilder();
//            sb.append(getInsertPrefix());
//            String[] delimiters = getParamListDelimiters();
//            assert delimiters.length == 2;
//            sb.append(delimiters[0]);
//            int id = 0;
//            for (String paramDesc : params) {
//                if (id == 0 && paramDesc.equals("self") && element.getIn() != null) { // NOI18N
//                    // Don't insert self parameters when -calling- methods
//                    continue;
//                }
//                sb.append("${"); //NOI18N
//                // Ensure that we don't use one of the "known" logical parameters
//                // such that a parameter like "path" gets replaced with the source file
//                // path!
//                sb.append("gsf-cc-"); // NOI18N
//                sb.append(Integer.toString(id++));
//                sb.append(" default=\""); // NOI18N
//                sb.append(paramDesc);
//                sb.append("\""); // NOI18N
//                sb.append("}"); //NOI18N
//                if (id < params.length) {
//                    sb.append(", "); //NOI18N
//                }
//            }
//            sb.append(delimiters[1]);
//            sb.append("${cursor}"); // NOI18N
//
//            return sb.toString();
//        }
    }

    private static class KeywordItem implements CompletionProposal, ElementHandle {
        private CompletionRequest request;
        private static final String PYTHON_KEYWORD = "org/netbeans/modules/python/editor/resources/py_25_16.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final String sort;
        private ElementHandle handle = this;
        private int anchor;
        private int sortPrioOverride = 0;
        private boolean smart;
        private boolean deprecated;
        private ElementKind kind;
        private String insertPrefix;

        KeywordItem(String keyword, String description, CompletionRequest request, String sort) {
            this.keyword = keyword;
            this.description = description;
            this.request = request;
            this.sort = sort;
            anchor = request.anchor;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult pr) {
            return null;
        }

        void setHandle(ElementHandle handle) {
            this.handle = handle;
        }

        public void setInsertPrefix(String insertPrefix) {
            this.insertPrefix = insertPrefix;
        }

        public void setKind(ElementKind kind) {
            this.kind = kind;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return kind != null ? kind : ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(final HtmlFormatter formatter) {
            return null;
        }

        @Override
        public String getLhsHtml(final HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            if (deprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(keyword);
            if (deprecated) {
                formatter.deprecated(false);
            }
            formatter.appendText(" "); // NOI18N
            formatter.name(kind, false);
            if (description != null) {
                formatter.appendHtml(description);
            }
            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (kind != null && kind != ElementKind.KEYWORD) {
                return null;
            }

            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon(PYTHON_KEYWORD, false);
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return handle;
        }

        @Override
        public boolean isSmart() {
            return smart;
        }

        @Override
        public int getAnchorOffset() {
            return anchor;
        }

        @Override
        public String getInsertPrefix() {
            return insertPrefix != null ? insertPrefix : keyword;
        }

        @Override
        public String getSortText() {
            return sort;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }

        public List<String> getInsertParams() {
            return null;
        }

        public String[] getParamListDelimiters() {
            return null;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return PythonMIMEResolver.PYTHON_MIME_TYPE;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public int getSortPrioOverride() {
            return sortPrioOverride;
        }

        private void setAnchorOffset(int anchor) {
            this.anchor = anchor;
        }
    }

    private static class SpecifyTypeItem implements CompletionProposal, ElementHandle, Runnable {
        CompletionRequest request;
        private int lexOffset;
        private Call call;

        private SpecifyTypeItem(CompletionRequest request, Call call, int lexOffset) {
            this.request = request;
            this.call = call;
            this.lexOffset = lexOffset;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult pr) {
            return null;
        }

        public String getVariableName() {
            return call.getLhs();
        }

        @Override
        public int getAnchorOffset() {
            return request.anchor;
        }

        @Override
        public ElementHandle getElement() {
            return this;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getInsertPrefix() {
            // Return the prefix to ensure that when the prefix is nonempty we still
            // show this item first
            return request.prefix;
        }

        @Override
        public String getSortText() {
            return request.prefix;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return NbBundle.getMessage(PythonCodeCompleter.class, "SpecifyTypeOf", getVariableName());
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/gsfret/source/resources/icons/implement-glyph.gif", false); // NOI18N
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public int getSortPrioOverride() {
            // Sort to the very top
            return -30000;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return PythonMIMEResolver.PYTHON_MIME_TYPE;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        private void apply() {
            request.doc.runAtomic(this);
        }

        @Override
        public void run() {
            BaseDocument doc = request.doc;
            try {

                // Compute the best place to insert the string
                String var = call.getLhs();

                int indent = 0;
                int offset = Utilities.getRowFirstNonWhite(doc, lexOffset);
                if (offset == -1) {
                    offset = lexOffset;
                } else {
                    indent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(doc, offset));
                }

                StringBuilder sb = new StringBuilder();
                sb.append("# @type "); // NOI18N
                sb.append(var);
                sb.append(" "); // NOI18N
                int typeDelta = sb.length();
                //sb.append("string"); // NOI18N
                sb.append("\n"); // NOI18N
                if (indent > 0) {
                    sb.append(IndentUtils.createIndentString(doc, indent));
                }

                doc.insertString(offset, sb.toString(), null); // NOI18N

                final JTextComponent target = GsfUtilities.getPaneFor(request.fileObject);
                if (target != null) {
                    target.getCaret().setDot(offset + typeDelta);

                    // Invoke code completion again at the new location!
                    // Can't do this immediately, or even in an invokeLater -
                    // the current completion session has to be completely finished
                    // first.
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            if (target instanceof JEditorPane) {
                                Completion.get().showCompletion();
                            }
                        }
                    }, 100);
                }
            } catch (BadLocationException e) {
                // Can't update
            }
        }
    }

    public class PythonCompletionResult extends DefaultCompletionResult {
        private CodeCompletionContext context;

        public PythonCompletionResult(CodeCompletionContext completionContext, List<CompletionProposal> list) {
            super(list, false);
            this.context = completionContext;
        }

        @Override
        public boolean insert(CompletionProposal item) {
            if (item instanceof SpecifyTypeItem) {
                SpecifyTypeItem specify = (SpecifyTypeItem)item;
                specify.apply();

                return true;
            }

            return false;
        }

        @Override
        public void afterInsert(CompletionProposal item) {
            if (item.getKind() == ElementKind.CALL) {
                return;
            }

            if (item instanceof PythonCompletionItem) {
                PythonCompletionItem pythonItem = (PythonCompletionItem)item;
                org.netbeans.modules.python.source.lexer.Call call = pythonItem.request.call;
                // Only insert imports when we add new top-level items
                if (pythonItem.getAddImport() != null) {
                    String module = pythonItem.getAddImport();
                    if (module.length() == 0) { // Means: No import
                        return;
                    }
                    String symbol = null;
                    int split = module.indexOf(':');
                    if (split != -1) {
                        symbol = module.substring(split + 1);
                        module = module.substring(0, split);
                    }
                    CodeStyle cs = CodeStyle.getDefault(pythonItem.request.doc);
                    boolean packageImport = !cs.preferSymbolImports();
                    // TODO - if you're already applying this import on a LHS for an imported
                    // symbol, handle that
                    new ImportManager((PythonParserResult) context.getParserResult()).ensureImported(module, symbol, packageImport, false, false);

                } else if (call == null || call.getLhs() == null) {
                    if (pythonItem.getElement() instanceof IndexedElement) {
                        CodeStyle cs = CodeStyle.getDefault(pythonItem.request.doc);

                        final IndexedElement elem = (IndexedElement)pythonItem.getElement();
                        FileObject requestFile = context.getParserResult().getSnapshot().getSource().getFileObject();
                        FileObject elementFile = elem.getFileObject();
                        if (elementFile != requestFile) {
                            String module = elem.getModule();
                            if (requestFile != null) {
                                String searchModule = PythonUtils.getModuleName(requestFile);
                                if (searchModule.equals(module)) {
                                    return;
                                }
                            }
                            if (module != null) {
                                String symbol = elem.getName();
                                boolean packageImport = !cs.preferSymbolImports();
                                // TODO - if you're already applying this import on a LHS for an imported
                                // symbol, handle that
                                new ImportManager((PythonParserResult) context.getParserResult()).ensureImported(module, symbol, packageImport, false, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private class CallItem extends PythonMethodItem {
        private int index;

        CallItem(IndexedMethod method, int parameterIndex, CompletionRequest request) {
            super(method, request);
            this.index = parameterIndex;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CALL;
        }

        @Override
        public String getInsertPrefix() {
            return "";
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();
            formatter.name(kind, true);
            formatter.appendText(getName());

            formatter.appendHtml("("); // NOI18N

            String[] params = method.getParams();
            if (params != null && params.length > 0) {

                int argIndex = index;
                if (method != null && method.getClz() != null && argIndex == 0 && params[0].equals("self")) { // NOI18N
                    // Skip "self"
                    argIndex++;

                } else if (argIndex > 0 && argIndex < params.length) {
                    formatter.appendText("... , ");
                }

                formatter.active(true);
                formatter.appendText(params[(Math.min(params.length - 1, argIndex))]);
                formatter.active(false);

                if (argIndex < params.length - 1) {
                    formatter.appendText(", ...");
                }

            }
            formatter.appendHtml(")"); // NOI18N

            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }
    }
}
