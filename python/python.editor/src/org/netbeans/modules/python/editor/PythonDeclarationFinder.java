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
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.elements.IndexedMethod;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonStringTokenId;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.python.source.lexer.PythonLexer;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.netbeans.modules.python.source.scopes.SymInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.alias;

public class PythonDeclarationFinder implements DeclarationFinder {
    @Override
    public OffsetRange getReferenceSpan(Document doc, int lexOffset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);

        //BaseDocument doc = (BaseDocument)document;

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(th, lexOffset);

        if (ts == null) {
            return OffsetRange.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return OffsetRange.NONE;
        }

        // Determine whether the caret position is right between two tokens
        boolean isBetween = (lexOffset == ts.offset());

        OffsetRange range = getReferenceSpan(ts, th, lexOffset);

        if ((range == OffsetRange.NONE) && isBetween) {
            // The caret is between two tokens, and the token on the right
            // wasn't linkable. Try on the left instead.
            if (ts.movePrevious()) {
                range = getReferenceSpan(ts, th, lexOffset);
            }
        }

        return range;
    }

    private OffsetRange getReferenceSpan(TokenSequence<?> ts,
            TokenHierarchy<Document> th, int lexOffset) {
        Token<?> token = ts.token();
        TokenId id = token.id();

//        if (id == PythonTokenId.IDENTIFIER) {
//            if (token.length() == 1 && id == PythonTokenId.IDENTIFIER && token.text().toString().equals(",")) {
//                return OffsetRange.NONE;
//            }
//        }

        // TODO: Tokens.SUPER, Tokens.THIS, Tokens.SELF ...
        if (id == PythonTokenId.IDENTIFIER) {
            return new OffsetRange(ts.offset(), ts.offset() + token.length());
        }

        // Look for embedded RDoc comments:
        TokenSequence<?> embedded = ts.embedded();

        if (embedded != null) {
            ts = embedded;
            embedded.move(lexOffset);

            if (embedded.moveNext()) {
                Token<?> embeddedToken = embedded.token();

                if (embeddedToken.id() == PythonStringTokenId.URL) {
                    return new OffsetRange(embedded.offset(),
                            embedded.offset() + embeddedToken.length());
                }
                // Recurse into the range - perhaps there is Ruby code (identifiers

                // etc.) to follow there
                OffsetRange range = getReferenceSpan(embedded, th, lexOffset);

                if (range != OffsetRange.NONE) {
                    return range;
                }
            }
        }


        return OffsetRange.NONE;
    }

    private DeclarationLocation findImport(PythonParserResult info, int lexOffset, BaseDocument doc) {
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, lexOffset);
        if (ts == null) {
            return DeclarationLocation.NONE;
        }
        if (ts.offset() == lexOffset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            if (!ts.movePrevious()) {
                return DeclarationLocation.NONE;
            }
        }

        Token<? extends PythonTokenId> token = ts.token();
        if (token == null) {
            return DeclarationLocation.NONE;
        }

        TokenId id = token.id();

        String moduleName = null;
        while (true) {
            if (id == PythonTokenId.IDENTIFIER || id.primaryCategory().equals(PythonLexer.KEYWORD_CAT)) {
                // Possibly inside the import string
                String tokenText = token.text().toString();
                if (moduleName == null) {
                    moduleName = tokenText;
                } else {
                    moduleName = tokenText + "." + moduleName;
                }
            } else if (id != PythonTokenId.DOT) {
                break;
            }
            if (!ts.movePrevious()) {
                return DeclarationLocation.NONE;
            }
            token = ts.token();
            id = token.id();
        }

        if (id != PythonTokenId.ERROR && id != PythonTokenId.NEWLINE &&
                id != PythonTokenId.WHITESPACE) {
            return DeclarationLocation.NONE;
        }

        if (!ts.movePrevious()) {
            return DeclarationLocation.NONE;
        }
        token = ts.token();
        id = token.id();
        if (id != PythonTokenId.IMPORT) {
            return DeclarationLocation.NONE;
        }
        if (moduleName == null) {
            return DeclarationLocation.NONE;
        }

        if (id == PythonTokenId.IMPORT || id == PythonTokenId.FROM) {
            if (id == PythonTokenId.IMPORT && ts.movePrevious() && ts.token().id() == PythonTokenId.WHITESPACE && ts.movePrevious()) {
                // See if this was "from foo import bar" such that we really should
                // be listing symbols inside the foo library
                token = ts.token();
                id = token.id();
                String library = null;
                while (true) {
                    if (id == PythonTokenId.IDENTIFIER || id.primaryCategory().equals(PythonLexer.KEYWORD_CAT)) {
                        // Possibly inside the import string
                        String tokenText = token.text().toString();
                        if (library == null) {
                            library = tokenText;
                        } else {
                            library = tokenText + "." + library;
                        }
                    } else if (id != PythonTokenId.DOT) {
                        break;
                    }
                    if (!ts.movePrevious()) {
                        return DeclarationLocation.NONE;
                    }
                    token = ts.token();
                    id = token.id();
                }
                if (library != null) {
                    if (id == PythonTokenId.WHITESPACE &&
                            ts.movePrevious() && ts.token().id() == PythonTokenId.FROM) {
                        return findImport(info, library, moduleName);
                    }
                }
            }

            return findImport(info, moduleName, null);
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findImport(PythonParserResult info, String moduleName, String symbol) {
        PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());

        Set<IndexedElement> elements = null;

        if (moduleName != null && symbol != null) {
            elements = index.getImportedElements(symbol, QuerySupport.Kind.EXACT, Collections.<String>singleton(moduleName), null);
        }

        if (symbol != null && (elements == null || elements.size() == 0)) {
            elements = index.getInheritedElements(null, symbol, QuerySupport.Kind.EXACT);
        }

        if (elements == null || elements.size() == 0) {
            elements = index.getModules(moduleName, QuerySupport.Kind.EXACT);
        }

        if (elements != null && elements.size() > 0) {
            AstPath path = null;
            PythonTree node = null;
            int astOffset = -1;
            int lexOffset = -1;
            return getDeclaration(info, null /*name*/, elements,
                    path, node, index, astOffset, lexOffset);
        }

// This never gets executed, right? Delete after EA
        for (IndexedElement candidate : elements) {
            // TODO - pick the best of the alternatives here?
            FileObject fileObject = candidate.getFileObject();
            if (fileObject == null) {
                return DeclarationLocation.NONE;
            }

            PythonTree node = candidate.getNode();
            int nodeOffset = node != null ? node.getCharStartIndex() : 0;

            DeclarationLocation loc = new DeclarationLocation(
                    fileObject, nodeOffset, candidate);

            if (elements.size() > 1) {
                // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
                // don't ask user!
                int not_nodoced = 0;
                for (final IndexedElement mtd : elements) {
                    if (!mtd.isNoDoc()) {
                        not_nodoced++;
                    }
                }
                if (not_nodoced >= 2) {
                    for (final IndexedElement mtd : elements) {
                        loc.addAlternative(new PythonAltLocation(mtd, mtd == candidate));
                    }
                }
            }

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    @SuppressWarnings("empty-statement")
    private DeclarationLocation findUrl(PythonParserResult info, Document doc, int lexOffset) {
        TokenSequence<?> ts = PythonLexerUtils.getPythonSequence((BaseDocument)doc, lexOffset);

        if (ts == null) {
            return DeclarationLocation.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return DeclarationLocation.NONE;
        }

        Token<?> token = ts.token();

        TokenSequence<?> embedded = ts.embedded();

        if (embedded != null) {
            ts = embedded;

            embedded.move(lexOffset);

            if (!embedded.moveNext() && !embedded.movePrevious()) {
                return DeclarationLocation.NONE;
            }

            token = embedded.token();
        }

        // Is this a comment? If so, possibly do rdoc-method reference jump
        if ((token != null) && (token.id() == PythonStringTokenId.URL)) {
            // TODO - use findLinkedMethod
            String method = token.text().toString();
            if (method.startsWith("www.")) { // NOI18N
                method = "http://" + method; // NOI18N
            }

            // A URL such as http://netbeans.org - try to open it in a browser!
            try {
                URL url = new URL(method);

                return new DeclarationLocation(url);
            } catch (MalformedURLException mue) {
                // URL is from user source... don't complain with exception dialogs etc.
                ;
            }
        }

        return DeclarationLocation.NONE;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int lexOffset) {

        final Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return DeclarationLocation.NONE;
        }

        final BaseDocument doc = (BaseDocument)document;

        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
            PythonTree root = PythonAstUtils.getRoot(parseResult);
            final int astOffset = PythonAstUtils.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                return DeclarationLocation.NONE;
            }

            AstPath path = null;
            PythonTree node = null;
            if (root != null) {
                path = AstPath.get(root, astOffset);
                node = path.leaf();
            }

            // See if it's an import
            DeclarationLocation imp = findImport(parseResult, lexOffset, doc);
            if (imp != DeclarationLocation.NONE) {
                return imp;
            }

            DeclarationLocation url = findUrl(parseResult, doc, lexOffset);
            if (url != DeclarationLocation.NONE) {
                return url;
            }

            final TokenHierarchy<Document> th = TokenHierarchy.get(document);
            org.netbeans.modules.python.source.lexer.Call call =
                    org.netbeans.modules.python.source.lexer.Call.getCallType(doc, th, lexOffset);
            
            FileObject fileObject = info.getSnapshot().getSource().getFileObject();

            // Search for local variables
            if (root != null && call.getLhs() == null) {
                if ((path.leaf() instanceof Name)) {
                    PythonTree scope = PythonAstUtils.getLocalScope(path);
                    SymbolTable symbolTable = parseResult.getSymbolTable();

                    String name = ((Name)path.leaf()).getInternalId();

                    SymInfo sym = symbolTable.findDeclaration(scope, name, true);
                    if (sym != null) {
                        if (sym.isFree()) {
                            PythonIndex index = PythonIndex.get(fileObject);

                            List<Import> imports = symbolTable.getImports();
                            List<ImportFrom> importsFrom = symbolTable.getImportsFrom();
                            Set<IndexedElement> elements = index.getImportedElements(name, QuerySupport.Kind.EXACT, parseResult, imports, importsFrom);
                            if (elements != null && elements.size() > 0) {
                                return getDeclaration(parseResult, null /*name*/, elements,
                                        path, node, index, astOffset, lexOffset);
                            }
                        // Must be defined by one of the imported symbols
                        }
                        if (sym.node != null) {
                            PythonTree declNode = sym.node;
                            if (sym.isImported()) { // Rather than showing the import symbol go to the definition in the library!
                                // Determine if it's an "as" name (import foo as bar) and if so just show the "as", if not,
                                // follow through to the library
                                if (declNode instanceof Import) {
                                    Import impNode = (Import)declNode;
                                    List<alias> names = impNode.getInternalNames();
                                    if (names != null) {
                                        for (alias at : names) {
                                            if (at.getInternalAsname() != null && name.equals(at.getInternalAsname())) {
                                                break;
                                            } else if (at.getInternalName().equals(name)) {
                                                // We found our library - just show it
                                                return findImport(parseResult, name, null);
                                            }
                                        }
                                    }
                                } else {
                                    assert declNode instanceof ImportFrom : declNode;
                                    ImportFrom impNode = (ImportFrom)declNode;
                                    List<alias> names = impNode.getInternalNames();
                                    if (names != null) {
                                        for (alias at : names) {
                                            if (at.getInternalAsname() != null && name.equals(at.getInternalAsname())) {
                                                break;
                                            } else if (at.getInternalName().equals(name)) {
                                                // We found our library - just show it
                                                return findImport(parseResult, impNode.getInternalModule(), name);
                                            }
                                        }
                                    }
                                }
                            }

                            if (sym.isUnresolved()) {
                                PythonIndex index = PythonIndex.get(fileObject);

                                List<Import> imports = symbolTable.getImports();
                                List<ImportFrom> importsFrom = symbolTable.getImportsFrom();
                                Set<IndexedElement> elements = index.getImportedElements(name, QuerySupport.Kind.EXACT, parseResult, imports, importsFrom);
                                if (elements != null && elements.size() > 0) {
                                    return getDeclaration(parseResult, null /*name*/, elements,
                                            path, node, index, astOffset, lexOffset);
                                }
                            } else {
                                OffsetRange astRange = PythonAstUtils.getNameRange(null, declNode);
                                int lexerOffset = PythonLexerUtils.getLexerOffset(parseResult, astRange.getStart());
                                if (lexerOffset == -1) {
                                    lexerOffset = 0;
                                }
                                return new DeclarationLocation(fileObject, lexerOffset);
                            }
                        }
                    }
//
//                    List<Name> localVars = PythonAstUtils.getLocalVarNodes(info, scope, name);
//                    if (localVars.size() > 0) {
//                        return new DeclarationLocation(info.getFileObject(), PythonAstUtils.getRange(localVars.get(0)).getStart());
//                    }
                }
            }



            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            String type = call.getType();
            if (type != null && "self".equals(type)) { // NOI18N
                type = PythonAstUtils.getFqnName(path);
            }
            if (type != null && type.length() > 0) {
                String name = null;
                PythonTree leaf = path.leaf();
                if (leaf instanceof Name) {
                    name = ((Name)path.leaf()).getInternalId();
                } else if (leaf instanceof Attribute) {
                    name = ((Attribute)leaf).getInternalAttr();
                }

                if (name != null) {
                    PythonIndex index = PythonIndex.get(fileObject);
                    // Add methods in the class (without an FQN)
                    Set<IndexedElement> elements = index.getInheritedElements(type, name, QuerySupport.Kind.EXACT);
                    if (elements != null && elements.size() > 0) {
                        return getDeclaration(parseResult, null /*name*/, elements,
                                path, node, index, astOffset, lexOffset);
                    }
                }
            }

            // Fallback: Index search on all names
            String prefix = new PythonCodeCompleter().getPrefix(info, lexOffset, false);
            if (prefix == null) {
                try {
                    prefix = Utilities.getIdentifier(doc, lexOffset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (prefix != null) {
                PythonIndex index = PythonIndex.get(fileObject);

                Set<? extends IndexedElement> elements = null;
                if (prefix.length() > 0 && Character.isUpperCase(prefix.charAt(0))) {
                    elements = index.getClasses(prefix, QuerySupport.Kind.EXACT, parseResult, true);
                }

                if (elements == null || elements.size() == 0) {
                    elements = index.getAllElements(prefix,
                            QuerySupport.Kind.EXACT, parseResult, true);
                }

                if (elements == null || elements.size() == 0) {
                    elements = index.getAllMembers(prefix,
                            QuerySupport.Kind.EXACT, parseResult, true);
                }

                if (elements != null && elements.size() > 0) {
                    return getDeclaration(parseResult, null /*name*/, elements,
                            path, node, index, astOffset, lexOffset);
                }

            // TODO - classes
//WORKING HERE                
//                if (elements == null || elements.size() == 0) {
//                    elements = index.getClasses(prefix, QuerySupport.Kind.EXACT, PythonIndex.ALL_SCOPE, parseResult, true);
//                }
//                if (elements != null && elements.size() > 0) {
//                    String name = null; // unused!
//                    
//                    return getMethodDeclaration(info, name, elements,
//                         path, null, index, astOffset, lexOffset);
//                }
            }
        } finally {
            doc.readUnlock();
        }
        return DeclarationLocation.NONE;
    }

    private DeclarationLocation getDeclaration(PythonParserResult info, String name, Set<? extends IndexedElement> methods,
            AstPath path, PythonTree closest, PythonIndex index, int astOffset, int lexOffset) {
        BaseDocument doc = (BaseDocument)info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return DeclarationLocation.NONE;
        }

        IndexedElement candidate =
                findBestMatch(info, name, methods, doc,
                astOffset, lexOffset, path, closest, index);

        return getDeclarationLocation(info, candidate, methods);
    }

    private DeclarationLocation getDeclarationLocation(PythonParserResult info, IndexedElement candidate, Set<? extends IndexedElement> methods) {
        BaseDocument doc = (BaseDocument)info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return DeclarationLocation.NONE;
        }

        if (candidate != null) {
            FileObject fileObject = candidate.getFileObject();
            if (fileObject == null) {
                return DeclarationLocation.NONE;
            }

            PythonTree node = candidate.getNode();
            int nodeOffset = 0;
            if (node != null) {
                nodeOffset = PythonAstUtils.getNameRange(info, node).getStart();
            }

            DeclarationLocation loc = new DeclarationLocation(
                    fileObject, nodeOffset, candidate);

            if (PythonUtils.isRstFile(fileObject)) {
                loc.setInvalidMessage(NbBundle.getMessage(PythonDeclarationFinder.class, "BuiltinPython", candidate.getName()));
                return loc;
            }

            if (methods.size() > 1) {
                // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
                // don't ask user!
                int not_nodoced = 0;
                for (final IndexedElement mtd : methods) {
                    if (!mtd.isNoDoc()) {
                        not_nodoced++;
                    }
                }
                if (not_nodoced >= 2) {
                    for (final IndexedElement mtd : methods) {
                        loc.addAlternative(new PythonAltLocation(mtd, mtd == candidate));
                    }
                }
            }

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    private IndexedElement findBestMatch(PythonParserResult info, String name, Set<? extends IndexedElement> methodSet,
            BaseDocument doc, int astOffset, int lexOffset, AstPath path, PythonTree call, PythonIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree

        Set<IndexedElement> methods = new HashSet<IndexedElement>(methodSet);

        while (!methods.isEmpty()) {
            IndexedElement method =
                    findBestMatchHelper(info, name, methods, doc, astOffset, lexOffset, path, call, index);
            PythonTree node = method.getNode();

            if (node != null) {
                return method;
            }

            if (!methods.contains(method)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best method and we keep trying it
                methods.remove(methods.iterator().next());
            } else {
                methods.remove(method);
            }
        }

        // Dynamic methods that don't have source (such as the TableDefinition methods "binary", "boolean", etc.
        if (methodSet.size() > 0) {
            return methodSet.iterator().next();
        }

        return null;
    }

    private IndexedElement findBestMatchHelper(PythonParserResult info, String name, Set<IndexedElement> elements,
            BaseDocument doc, int astOffset, int lexOffset, AstPath path, PythonTree callNode, PythonIndex index) {

        Set<IndexedElement> candidates = new HashSet<>();

        if (elements.size() == 0) {
            return null;
        } else if (elements.size() == 1) {
            return elements.iterator().next();
        }

        // 1. Prefer matches in the current file
        String searchUrl = info.getSnapshot().getSource().getFileObject().toURL().toExternalForm();
        candidates = new HashSet<>();

        for (IndexedElement element : elements) {
            String url = element.getFilenameUrl();

            if (url.equals(searchUrl)) {
                candidates.add(element);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            elements = candidates;
        }


        // 2. See which of the class references are defined in files directly
        //   included by this file.
        Set<String> included = new HashSet<>();
        candidates = new HashSet<>();

        SymbolTable table = PythonAstUtils.getParseResult(info).getSymbolTable();
        List<Import> imports = table.getImports();
        for (Import imp : imports) {
            List<alias> names = imp.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    included.add(at.getInternalName());
                }
            }
        }
        List<ImportFrom> importsFrom = table.getImportsFrom();
        for (ImportFrom imp : importsFrom) {
            included.add(imp.getInternalModule());
        }

        if (included.size() > 0) {
            for (IndexedElement element : elements) {
                String mod = element.getModule();

                if (included.contains(mod)) {
                    candidates.add(element);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                elements = candidates;
            }
        }

        // 4. Prefer builtins
        candidates = new HashSet<>();

        for (IndexedElement element : elements) {
            String url = element.getFilenameUrl();

            if (url != null && url.contains("pythonstubs")) { // NOI18N
                candidates.add(element);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            elements = candidates;
        }

        // 5. Prefer documented classes
        candidates = new HashSet<>();
        for (IndexedElement element : elements) {
            if (element.isDocumented()) {
                candidates.add(element);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            elements = candidates;
        }



        // TODO - use some heuristics here!
        return elements.iterator().next();
    }

    public DeclarationLocation getSuperImplementations(PythonParserResult info, int lexOffset) {
        // Figure out if we're on a method, and if so, locate the nearest
        // method it is overriding.
        // Otherwise, if we're on a class (anywhere, not just definition),
        // go to the super class.
        PythonTree root = PythonAstUtils.getRoot(info);
        if (root != null) {
            // Determine function or call under caret
            int astOffset = PythonAstUtils.getAstOffset(info, lexOffset);
            if (astOffset != -1) {
                AstPath path = AstPath.get(root, astOffset);
                PythonTree leaf = path.leaf();
                String name = null;
                boolean findClass = false; // false=function, true=class
                if (leaf instanceof FunctionDef) {
                    name = ((FunctionDef)leaf).getInternalName();
                } else if (leaf instanceof Name) {
                    name = ((Name)leaf).getInternalId();
                    if (path.leafParent() instanceof ClassDef) {
                        findClass = true;
                    }
                } else if (leaf instanceof ClassDef) {
                    name = ((ClassDef)leaf).getInternalName();
                    findClass = true;
                }

                Set<IndexedElement> elements = null;
                PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
                if (findClass) {
                    elements = index.getSuperClasses(name);
                } else {
                    ClassDef clz = PythonAstUtils.getClassDef(path);
                    if (clz != null) {
                        elements = index.getOverridingMethods(clz.getInternalName(), name);
                    }
                }

                if (elements != null && elements.size() > 0) {
                    // Pick the closest element as the default candidate
                    IndexedElement candidate = null;
                    int depth = Integer.MAX_VALUE;
                    for (IndexedElement element : elements) {
                        if (element.getOrder() < depth) {
                            candidate = element;
                            depth = element.getOrder();
                        }
                    }

                    return getDeclarationLocation(info, candidate, elements);
                }
            }
        }

        return DeclarationLocation.NONE;
    }

    public IndexedMethod findMethodDeclaration(PythonParserResult info, org.python.antlr.ast.Call call, AstPath path, Set<IndexedMethod>[] alternativesHolder) {
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
        Set<IndexedElement> functions = null;

        // TODO - do more accurate lookup of types here!
        // (a) For functions, look in imported symbols first!
        // (b) For methods, try to resolve the lhs type first and search within specific types!

        String callName = PythonAstUtils.getCallName(call);
        if (callName == null) {
            return null;
        }

        if (call.getInternalFunc() instanceof Attribute) {
            // Method/member access
            functions = index.getAllMembers(callName, QuerySupport.Kind.EXACT, parseResult, false);
        } else {
            functions = index.getAllElements(callName, QuerySupport.Kind.EXACT, parseResult, false);
        }

        if (functions != null && functions.size() > 0) {
            Set<IndexedElement> eligible = new HashSet<>();
            for (IndexedElement element : functions) {
                if (element instanceof IndexedMethod) {
                    eligible.add(element);
                }
            }

            int astOffset = call.getCharStartIndex();
            int lexOffset = PythonLexerUtils.getLexerOffset(info, astOffset);
            IndexedElement candidate =
                    findBestMatch(info, callName, eligible, (BaseDocument)info.getSnapshot().getSource().getDocument(false),
                    astOffset, lexOffset, path, call, index);
            assert candidate instanceof IndexedMethod; // Filtered into earlier already
            return (IndexedMethod)candidate;
        }

        return null;
    }

    /**
     * Compute the declaration location for a test string (such as MosModule::TestBaz/test_qux).
     *
     * @param fileInProject a file in the project where to perform the search
     * @param testString a string represening a test class and method, such as TestFoo/test_bar
     * @param classLocation if true, returns the location of the class rather then the method.
     */
    public static DeclarationLocation getTestDeclaration(FileObject fileInProject, String testString, boolean classLocation) {
        int methodIndex = testString.indexOf('/'); //NOI18N
        if (methodIndex == -1) {
            return DeclarationLocation.NONE;
        }

        String className = testString.substring(0, methodIndex);
        String methodName = testString.substring(methodIndex+1);

        PythonIndex index = PythonIndex.get(fileInProject);
        Set<IndexedElement> elements = index.getAllMembers(methodName, QuerySupport.Kind.EXACT, null, true);
        // Look for one that matches our class name
        if (elements.size() > 0) {
            IndexedElement candidate = null;
            for (IndexedElement element : elements) {
                if (element instanceof IndexedMethod) {
                    IndexedMethod method = (IndexedMethod)element;
                    if (className.startsWith(method.getModule()+".")) {
                        // Close!
                        candidate = method;
                        if (className.equals(method.getModule()+ "." + method.getClz())) {
                            break;
                        }
                    }
                }
            }

            if (candidate != null) {
                int offset = 0;
                PythonTree node = candidate.getNode();
                if (node != null) {
                    offset = PythonAstUtils.getRange(node).getStart();
                }
                return new DeclarationLocation(candidate.getFileObject(), offset);
            }
        }

        return DeclarationLocation.NONE;
    }

    private static class PythonAltLocation implements AlternativeLocation {
        private IndexedElement element;
        private boolean isPreferred;
        private String cachedDisplayItem;
        private int order;

        PythonAltLocation(IndexedElement element, boolean isPreferred) {
            this.element = element;
            this.isPreferred = isPreferred;
            order = element.getOrder();
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.setMaxLength(120);
            if (cachedDisplayItem == null) {
                formatter.reset();

                boolean nodoc = element.isNoDoc();
                boolean documented = element.isDocumented();
                if (isPreferred) {
                    formatter.emphasis(true);
                } else if (nodoc) {
                    formatter.deprecated(true);
                }

                if (element instanceof IndexedMethod) {
//                    if (element.getFqn() != null) {
//                        formatter.appendText(element.getFqn());
//                        formatter.appendText(".");
//                    }
                    formatter.appendText(element.getName());
                    IndexedMethod method = (IndexedMethod)element;
                    String[] parameters = method.getParams();

                    if ((parameters != null) && (parameters.length > 0)) {
                        formatter.appendText("("); // NOI18N

                        boolean first = true;
                        for (String parameter : parameters) {
                            if (first) {
                                first = false;
                            } else {
                                formatter.appendText(", "); // NOI18N
                            }
                            formatter.parameters(true);
                            formatter.appendText(parameter);
                            formatter.parameters(false);
                        }

                        formatter.appendText(")"); // NOI18N
                    }
                } else {
                    //formatter.appendText(element.getFqn());
                    formatter.appendText(element.getName());
                }

                if (element.getClz() != null) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(PythonDeclarationFinder.class, "In"));
                    formatter.appendText(" ");
                    formatter.appendText(element.getClz());
                    formatter.appendHtml(" "); // NOI18N
                }

                String filename = null;
                String url = element.getFilenameUrl();
                if (url == null) {
                    // Deleted file?
                    // Just leave out the file name
                } else if (url.contains("pythonstubs")) { // NOI18N
                    filename = NbBundle.getMessage(PythonDeclarationFinder.class, "PythonLib");
//                    
//                    if (url.indexOf("/stub_") == -1) {
//                        // Not a stub file, such as ftools.py
//                        // TODO - don't hardcode for version
//                        String stub = "pythonstubs/2.5/";
//                        int stubStart = url.indexOf(stub);
//                        if (stubStart != -1) {
//                            filename = filename+": " + url.substring(stubStart);
//                        }
//                    }
                } else {
                    FileObject fo = element.getFileObject();
                    if (fo != null) {
                        filename = fo.getNameExt();
                    } else {
                        // Perhaps a file that isn't present here, such as something in site_ruby
                        int lastIndex = url.lastIndexOf('/');
                        if (lastIndex != -1) {
                            String s = url.substring(0, lastIndex);
                            int almostLastIndex = s.lastIndexOf('/');
                            if (almostLastIndex != -1 && ((url.length() - almostLastIndex) < 40)) {
                                filename = url.substring(almostLastIndex + 1);
                                if (filename.indexOf(':') != -1) {
                                    // Don't include prefix like cluster:, file:, etc.
                                    filename = url.substring(lastIndex + 1);
                                }
                            } else {
                                filename = url.substring(lastIndex + 1);
                            }
                        }
                    }

//                    // TODO - make this work with 1.9 etc.
//                    //final String GEM_LOC = "lib/ruby/gems/1.8/gems/";
//                    Pattern p = Pattern.compile("lib/ruby/gems/\\d+\\.\\d+/gems/");
//                    Matcher m = p.matcher(url);
//                    //int gemIndex = url.indexOf(GEM_LOC);
//                    //if (gemIndex != -1) {
//                    if (m.find()) {
//                        //int gemIndex = m.start();
//                        //gemIndex += GEM_LOC.length();
//                        int gemIndex = m.end();
//                        int gemEnd = url.indexOf('/', gemIndex);
//                        if (gemEnd != -1) {
//                            //int libIndex = url.indexOf("lib/", gemEnd);
//                            //if (libIndex != -1) {
//                            //    filename = url.substring(libIndex+4);
//                            //}
//                            filename = url.substring(gemIndex, gemEnd) + ": " + filename;
//                        }
//                    }
                }

                if (filename != null) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(PythonDeclarationFinder.class, "In"));
                    formatter.appendText(" ");
                    formatter.appendText(filename);
                }

                if (documented) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(PythonDeclarationFinder.class, "Documented"));
                } else if (nodoc) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(PythonDeclarationFinder.class, "NoDoced"));
                }

                if (isPreferred) {
                    formatter.emphasis(false);
                } else if (nodoc) {
                    formatter.deprecated(false);
                }

                cachedDisplayItem = formatter.getText();
            }

            return cachedDisplayItem;
        }

        @Override
        public DeclarationLocation getLocation() {
            PythonTree node = element.getNode();
            int lineOffset = node != null ? node.getCharStartIndex() : -1;
            DeclarationLocation loc = new DeclarationLocation(element.getFileObject(),
                    lineOffset, element);

            return loc;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PythonAltLocation other = (PythonAltLocation)obj;
            if (this.element != other.element && (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            if (this.isPreferred != other.isPreferred) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        public int compareTo(AlternativeLocation alternative) {
            PythonAltLocation alt = (PythonAltLocation)alternative;

            // The preferred item should be chosen
            if (isPreferred) {
                return -1;
            } else if (alt.isPreferred) {
                return 1;
            } // Can't both be so no else == check

            if (order != alt.order) {
                return order - alt.order;
            }

            // Nodoced items last
            if (element.isNoDoc() != alt.element.isNoDoc()) {
                return element.isNoDoc() ? 1 : -1;
            }

            // Documented items on top
            if (element.isDocumented() != alt.element.isDocumented()) {
                return element.isDocumented() ? -1 : 1;
            }

            // TODO: Sort by classes?
            String thisClz = element.getClz() != null ? element.getClz() : "";
            String thatClz = alt.element.getClz() != null ? alt.element.getClz() : "";
            int cmp = thisClz.compareTo(thatClz);
            if (cmp != 0) {
                return cmp;
            }

            // TODO: Sort by gem?

            // Sort by containing clz - just do fqn here?
            String thisIn = element.getIn() != null ? element.getIn() : "";
            String thatIn = alt.element.getIn() != null ? alt.element.getIn() : "";
            cmp = thisIn.compareTo(thatIn);
            if (cmp != 0) {
                return cmp;
            }

            // Sort by file
            String thisFile = element.getFileObject() != null ? element.getFileObject().getNameExt() : "";
            String thatFile = alt.element.getFileObject() != null ? alt.element.getFileObject().getNameExt() : "";
            cmp = thisFile.compareTo(thatFile);

            return cmp;
        }
    }
}
