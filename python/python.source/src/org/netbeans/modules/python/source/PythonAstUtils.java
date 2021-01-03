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
package org.netbeans.modules.python.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Finder;
import org.netbeans.editor.FinderFactory;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.python.api.Util;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.modules.python.source.lexer.PythonCommentTokenId;
import org.netbeans.modules.python.source.scopes.ScopeInfo;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.netbeans.modules.python.source.scopes.SymInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

/**
 * Utility functions for dealing with the Jython AST
 *
 */
public class PythonAstUtils {
    private PythonAstUtils() {
        // This is just a utility class, no instances expected so private constructor
    }

    public static int getAstOffset(ParserResult result, int lexOffset) {
        if (result != null) {
            return result.getSnapshot().getEmbeddedOffset(lexOffset);
        }

        return lexOffset;
    }

    public static OffsetRange getAstOffsets(ParserResult result, OffsetRange lexicalRange) {
        if (result != null) {
            int rangeStart = lexicalRange.getStart();
            int start = result.getSnapshot().getEmbeddedOffset(rangeStart);
            if (start == rangeStart) {
                return lexicalRange;
            } else if (start == -1) {
                return OffsetRange.NONE;
            } else {
                // Assumes the translated range maintains size
                return new OffsetRange(start, start + lexicalRange.getLength());
            }
        }
        return lexicalRange;
    }

    public static PythonParserResult getParseResult(ParserResult result) {
        if(result == null || !(result instanceof PythonParserResult)) {
            return null;
        } else {
            return ((PythonParserResult)result);
        }
    }

    public static PythonTree getRoot(ParserResult r) {
        assert r instanceof PythonParserResult;

        PythonParserResult result = (PythonParserResult)r;

        return result.getRoot();
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getNameRange(PythonParserResult info, PythonTree node) {
//        final int type = node.getType();
//        switch (type) {
//        case Token.FUNCTION: {
//            if (node.hasChildren()) {
//                for (PythonTree child = node.getFirstChild(); child != null; child = child.getNext()) {
//                    if (child.getType() == Token.FUNCNAME) {
//                        return getNameRange(child);
//                    }
//                }
//            }
//
//            return getRange(node);
//        }
//        case Token.NAME:
//        case Token.BINDNAME:
//        case Token.FUNCNAME:
//        case Token.PARAMETER:
//        case Token.OBJLITNAME:
//            int start = node.getSourceStart();
//            String name = node.getString();
//            return new OffsetRange(start, start+name.length());
//        case Token.CALL:
//            PythonTree namePythonTree = findCallNamePythonTree(node);
//            if (namePythonTree != null) {
//                return getNameRange(namePythonTree);
//            }
//        }

        // XXX Is there a faster way to determine if it's a function,
        // e.g. some kind of "kind" or "id" or "type" enum attribute on the tree node
        if (node instanceof FunctionDef) {
            FunctionDef def = (FunctionDef)node;
            //node.getType();

            int defStart = def.getCharStartIndex();

            // Turns out that when you have decorators, the function start offset
            // -includes- the decorators which precede the "def" keyword, thus we
            // have to scan forwards to find the true beginning.
            List<expr> decorators = def.getInternalDecorator_list();
            if (decorators != null && decorators.size() > 0) {
                int maxEnd = 0;
                for (expr expr : decorators) {
                    int exprEnd = expr.getCharStopIndex();
                    if (exprEnd > maxEnd) {
                        maxEnd = exprEnd;
                    }
                }
                if (decorators.size() > 1) {
                    maxEnd++;
                }
                defStart = maxEnd;

                // At first I was justlooking for the largest end offset of the decorators,
                // but if you have additional comments etc. that won't work right, so
                // in this case, go and look at the actual document
                if (info != null) {
                    BaseDocument doc = GsfUtilities.getDocument(info.getSnapshot().getSource().getFileObject(), false);
                    if (doc != null) {
                        int lexOffset = PythonLexerUtils.getLexerOffset(info, defStart);
                        int limitOffset = PythonLexerUtils.getLexerOffset(info, def.getCharStopIndex());
                        if (lexOffset != -1 && limitOffset != -1) {
                            Finder finder = new FinderFactory.StringFwdFinder("def ", true);
                            try {
                                int foundOffset = doc.find(finder, lexOffset, limitOffset);
                                if (foundOffset != -1) {
                                    defStart = foundOffset;
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }

            // HACK: There's no separate node for the name offset itself, so I need
            // to figure it out. For now assume that it's exactly 4 characters away
            // from the beginning of "def" - def plus space. If there are multiple spaces
            // this won't work. I ought to look in the document and ensure that the character
            // there in fact is the start of the name, and if not, search forwards for it.
            int DELTA = 4; // HACK:
            int start = defStart + DELTA;
            int end = start + def.getInternalName().length();

            // TODO - look up offset

            return new OffsetRange(start, end);
        } else if (node instanceof ClassDef) {
            ClassDef def = (ClassDef)node;
            //node.getType();

            // HACK: There's no separate node for the name offset itself, so I need
            // to figure it out. For now assume that it's exactly 6 characters away
            // from the beginning of "class" - class plus space. If there are multiple spaces
            // this won't work. I ought to look in the document and ensure that the character
            // there in fact is the start of the name, and if not, search forwards for it.
            int DELTA = 6; // HACK:
            int start = def.getCharStartIndex() + DELTA;
            int end = start + def.getInternalName().length();

            // TODO - look up offset

            return new OffsetRange(start, end);
        } else if (node instanceof Attribute) {
            Attribute attr = (Attribute)node;
            return getNameRange(info, attr.getInternalValue());
        } else if (node instanceof Call) {
            Call call = (Call)node;
            if (call.getInternalFunc() instanceof Name) {
                return getNameRange(info, call.getInternalFunc());
            } else if (call.getInternalFunc() instanceof Attribute) {
                // The call name is in the value part of the name.value part
                Attribute attr = (Attribute)call.getInternalFunc();
                int start = attr.getInternalValue().getCharStopIndex() + 1; // +1: Skip .
                String name = attr.getInternalAttr();
                if (name == null) {
                    name = "";
                }
                return new OffsetRange(start, start + name.length());
            } else {
                String name = getCallName(call);
                if (name != null) {
                    int start = call.getCharStartIndex();
                    return new OffsetRange(start, start + name.length());
                }
            }
        }

        return getRange(node);
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getRange(PythonTree node) {
        final int start = node.getCharStartIndex();
        final int end = node.getCharStopIndex();

//        assert end >= start : "Invalid offsets for " + node + ": start=" + start + " and end=" + end;
        if (end < start) {
            Logger logger = Logger.getLogger(PythonAstUtils.class.getName());
            logger.log(Level.WARNING, "Invalid offsets for " + node + ": start=" + start + " and end=" + end);
            return new OffsetRange(start, start);
        }

        return new OffsetRange(start, end);
    }

    public static boolean isNameNode(PythonTree node) {
        if (node instanceof Name) {
            return true;
        }

        return false;
    }

    /** Return if a function is a staticmethod **/
    public static boolean isStaticMethod(PythonTree node) {
        if (node instanceof FunctionDef) {
            FunctionDef def = (FunctionDef)node;
            List<expr> decorators = def.getInternalDecorator_list();
            if (decorators != null && decorators.size() > 0) {
                for (expr decorator : decorators) {
                    if (decorator instanceof Name) {
                        String decoratorName = ((Name)decorator).getText();
                        if (decoratorName.equals("staticmethod")) { // NOI18N
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Compute the module/class name for the given node path */
    public static String getFqnName(AstPath path) {
        StringBuilder sb = new StringBuilder();

        Iterator<PythonTree> it = path.rootToLeaf();

        while (it.hasNext()) {
            PythonTree node = it.next();

            if (node instanceof ClassDef) {
                if (sb.length() > 0) {
                    sb.append('.'); // NOI18N
                }
                ClassDef cls = (ClassDef)node;
                sb.append(cls.getInternalName());
            }
        }

        return sb.toString();
    }

    /** Return the node for the local scope containing the given node */
    public static PythonTree getLocalScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return node;
            }
        }

        return path.root();
    }

    public static PythonTree getClassScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return node;
            }
        }

        return path.root();
    }

    public static ClassDef getClassDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return (ClassDef)node;
            }
        }

        return null;
    }

    public static boolean isClassMethod(AstPath path, FunctionDef def) {
        // Check to see if (a) the function is inside a class, and (b) it's
        // not nested in a function
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return true;
            }
            // Nested method private to this one?
            if (node instanceof FunctionDef && node != def) {
                return false;
            }
        }

        return false;
    }

    public static FunctionDef getFuncDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return (FunctionDef)node;
            }
        }

        return null;
    }

    /**
     * Return true iff this call looks like a "getter". If we're not sure,
     * return the default value passed into this method, unknownDefault. 
     */
    public static boolean isGetter(Call call, boolean unknownDefault) {
        String name = PythonAstUtils.getCallName(call);
        if (name == null) {
            return unknownDefault;
        }

        return name.startsWith("get") || name.startsWith("_get"); // NOI18N
    }

    public static String getCallName(Call call) {
        expr func = call.getInternalFunc();

        return getExprName(func);
    }

    public static String getExprName(expr type) {
        if (type instanceof Attribute) {
            Attribute attr = (Attribute)type;
            return attr.getInternalAttr();
        } else if (type instanceof Name) {
            return ((Name)type).getInternalId();
        } else if (type instanceof Call) {
            Call call = (Call)type;
            return getExprName(call.getInternalFunc());
            //} else if (type instanceof Str) {
            //    return ((Str)type).getText();
        } else {
            return null;
        }
    }

    public static String getName(PythonTree node) {
        if (node instanceof Name) {
            return ((Name)node).getInternalId();
        }
        if (node instanceof Attribute) {
            Attribute attrib = (Attribute)node;
            String prefix = getName(attrib.getInternalValue());
            return (prefix + '.' + attrib.getInternalAttr());
        }
        NameVisitor visitor = new NameVisitor();
        try {
            Object result = visitor.visit(node);
            if (result instanceof String) {
                return (String)result;
            } else {
                // TODO HANDLE THIS!
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public static List<String> getParameters(FunctionDef def) {
        arguments args = def.getInternalArgs();
        List<String> params = new ArrayList<>();

        NameVisitor visitor = new NameVisitor();

        for (expr e : args.getInternalArgs()) {
            try {
                Object result = visitor.visit(e);
                if (result instanceof String) {
                    params.add((String)result);
                } else {
                    // TODO HANDLE THIS!
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        String vararg = args.getInternalVararg();
        if (vararg != null) {
            params.add(vararg);
        }
        String kwarg = args.getInternalKwarg();
        if (kwarg != null) {
            params.add(kwarg);
        }

        return params;
    }

    private static Str searchForDocNode(stmt stmt) {
        if (stmt instanceof Expr) {
            Expr expr = (Expr)stmt;
            expr value = expr.getInternalValue();
            if (value instanceof Str) {
                return (Str)value;
            }
        }

        return null;
    }

    public static Str getDocumentationNode(PythonTree node) {
        // DocString processing.
        // See http://www.python.org/dev/peps/pep-0257/

        // For modules, it's the first Str in the document.
        // For classes and methods, it's the first Str in the object.
        // For others, nothing.

        if (node instanceof FunctionDef) {
            // Function
            FunctionDef def = (FunctionDef)node;
            List<stmt> body = def.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        } else if (node instanceof ClassDef) {
            // Class
            ClassDef def = (ClassDef)node;
            List<stmt> body = def.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        } else if (node instanceof Module) {
            // Module
            Module module = (Module)node;
            List<stmt> body = module.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        }
        // TODO: As per http://www.python.org/dev/peps/pep-0257/ I should
        // also look for "additional docstrings" (Str node following a Str node)
        // and Assign str nodes

        return null;
    }

    public static String getStrContent(Str str) {
        String doc = str.getText();

        // Strip quotes
        // and U and/or R for unicode/raw string. U must always preceede r if present.
        if (doc.startsWith("ur") || doc.startsWith("UR") || // NOI18N
                doc.startsWith("Ur") || doc.startsWith("uR")) { // NOI18N
            doc = doc.substring(2);
        } else if (doc.startsWith("r") || doc.startsWith("u") || // NOI18N
                doc.startsWith("R") || doc.startsWith("U")) { // NOI18N
            doc = doc.substring(1);
        }

        if (doc.startsWith("\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("\"") && doc.endsWith("\"")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        } else if (doc.startsWith("'") && doc.endsWith("'")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        }

        return doc;
    }

    public static String getDocumentation(PythonTree node) {
        Str str = getDocumentationNode(node);
        if (str != null) {
            return getStrContent(str);
        }

        return null;
    }

    public static PythonTree getForeignNode(final IndexedElement o, PythonParserResult[] parserResultRet) {
        FileObject fo = o.getFileObject();

        if (fo == null) {
            return null;
        }
        
        Source source = Source.create(fo);
        if(source == null) {
            return null;
        }
        final PythonParserResult[] resultHolder = new PythonParserResult[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    resultHolder[0] = (PythonParserResult) resultIterator.getParserResult();
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        PythonParserResult info = resultHolder[0];
        if (parserResultRet != null) {
            parserResultRet[0] = info;
        }
        PythonParserResult result = getParseResult(info);
        if (result == null) {
            return null;
        }

        PythonTree root = getRoot(result);
        if (root == null) {
            return null;
        }

        if (o.getKind() == ElementKind.MODULE && root instanceof Module) {
            return root;
        }

        String signature = o.getSignature();

        if (signature == null) {
            return null;
        }

        SymbolTable symbolTable = result.getSymbolTable();
        SymInfo sym = symbolTable.findBySignature(o.getKind(), signature);
        if (sym != null && sym.node != null) {
            // Temporary diagnostic checking
            //assert ((o.getKind() != ElementKind.CONSTRUCTOR && o.getKind() != ElementKind.METHOD) ||
            //        sym.node instanceof FunctionDef);
            //assert o.getKind() != ElementKind.CLASS || sym.node instanceof ClassDef;

            return sym.node;
        }

        // TODO - check args etc.
//        String name = o.getName();
//        boolean lookForFunction = o.getKind() == ElementKind.CONSTRUCTOR || o.getKind() == ElementKind.METHOD;
//        if (lookForFunction) {
//            for (AstElement element : result.getStructure().getElements()) {
//                if (element.getName().equals(name) && element.getSignature().equals(signature)) {
//                        return element.getNode();
//                    }
//                }
//            }
//        }

        ElementKind kind = o.getKind();
        List<PythonStructureItem> items = PythonStructureScanner.analyze(info).getElements();
        if (items != null) {
            return find(items, signature, kind);
        } else {
            return null;
        }
    }

    private static PythonTree find(List<? extends StructureItem> items, String signature, ElementKind kind) {
        for (StructureItem item : items) {
            ElementKind childKind = item.getKind();
            if (childKind == kind &&
                    item instanceof PythonStructureItem &&
                    signature.equals(((PythonStructureItem)item).getSignature())) {
                return ((PythonStructureItem)item).getNode();
            }
            if (childKind == ElementKind.CLASS && signature.contains(item.getName())) {
                @SuppressWarnings("unchecked")
                List<? extends StructureItem> children = item.getNestedItems();
                PythonTree result = find(children, signature, kind);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static Set<OffsetRange> getAllOffsets(PythonParserResult info, AstPath path, int lexOffset, String name, boolean abortOnFree) {
        if (path == null) {
            path = AstPath.get(PythonAstUtils.getRoot(info), lexOffset);
        }
        PythonTree scope = PythonAstUtils.getLocalScope(path);
        SymbolTable symbolTable = PythonAstUtils.getParseResult(info).getSymbolTable();
        List<PythonTree> nodes = symbolTable.getOccurrences(scope, name, abortOnFree);
        if (nodes == null) {
            return null;
        }
        Set<OffsetRange> offsets = new HashSet<>();
        Document doc = GsfUtilities.getDocument(info.getSnapshot().getSource().getFileObject(), false);
        if (doc == null) {
            return Collections.emptySet();
        }
        for (PythonTree node : nodes) {
            OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
            OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);

            if (node instanceof Import || node instanceof ImportFrom) {
                // Try to find the exact spot
                if (abortOnFree) {
                    return null;
                } else {
                    lexRange = PythonLexerUtils.getImportNameOffset((BaseDocument)doc, lexRange, node, name);
                }
            } else if (abortOnFree && (node instanceof FunctionDef || node instanceof ClassDef)) {
                return null;
            }

            if (lexRange != OffsetRange.NONE) {
                offsets.add(lexRange);
            }
        }
        // Look for type variables
        ScopeInfo scopeInfo = symbolTable.getScopeInfo(scope);
        if (scopeInfo != null) {
            SymInfo sym = scopeInfo.tbl.get(name);
            if (sym != null && sym.isVariable(false)) {
                // Look for type declarations that can apply to this variable
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, PythonAstUtils.getRange(scope));
                if (lexRange != OffsetRange.NONE) {
                    BaseDocument bdoc = (BaseDocument)doc;
                    try {
                        bdoc.readLock(); // For TokenHierarchy usage
                        TokenHierarchy hi = TokenHierarchy.get(doc);
                        LanguagePath languagePath = LanguagePath.get(LanguagePath.get(PythonTokenId.language()), PythonCommentTokenId.language());
                        int startOffset = Math.min(lexRange.getStart(), doc.getLength());
                        if (scope instanceof Module) {
                            startOffset = 0; // Pick up comments before code starts
                        }
                        int endOffset = Math.min(lexRange.getEnd(), doc.getLength());
                        @SuppressWarnings("unchecked")
                        List<TokenSequence<? extends PythonCommentTokenId>> tsl = hi.tokenSequenceList(languagePath, startOffset, endOffset);
                        for (TokenSequence<? extends PythonCommentTokenId> ts : tsl) {
                            ts.moveStart();
                            while (ts.moveNext()) {
                                PythonCommentTokenId id = ts.token().id();
                                if (id == PythonCommentTokenId.TYPEKEY) {
                                    if (ts.moveNext() && // skip separator
                                            ts.moveNext()) {
                                        if (ts.token().id() == PythonCommentTokenId.VARNAME) {
                                            if (TokenUtilities.equals(ts.token().text(), name)) {
                                                int start = ts.offset();
                                                OffsetRange nameRange = new OffsetRange(start, start + name.length());
                                                offsets.add(nameRange);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        bdoc.readUnlock();
                    }

                }
            }
        }

        return offsets;
    }

    private static final class NameVisitor extends Visitor {
        @Override
        public Object visitName(Name name) throws Exception {
            return name.getInternalId();
        }
    }

    public static Set<OffsetRange> getLocalVarOffsets(PythonParserResult info, int lexOffset) {
        int astOffset = getAstOffset(info, lexOffset);
        if (astOffset != -1) {
            PythonTree root = getRoot(info);
            if (root != null) {
                AstPath path = AstPath.get(root, astOffset);
                if (path != null) {
                    PythonTree closest = path.leaf();
                    PythonTree scope = getLocalScope(path);
                    String name = ((Name)closest).getInternalId();

                    return getLocalVarOffsets(info, scope, name);
                }
            }
        }

        return Collections.emptySet();
    }

    public static Set<OffsetRange> getLocalVarOffsets(PythonParserResult info, PythonTree scope, String name) {
        LocalVarVisitor visitor = new LocalVarVisitor(info, name, false, true);
        try {
            visitor.visit(scope);
            return visitor.getOffsets();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptySet();
        }
    }

    public static List<Name> getLocalVarNodes(PythonParserResult info, PythonTree scope, String name) {
        LocalVarVisitor visitor = new LocalVarVisitor(info, name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    public static List<Name> getLocalVarAssignNodes(PythonParserResult info, PythonTree scope, String name) {
        LocalVarAssignVisitor visitor = new LocalVarAssignVisitor(info, name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    private static class LocalVarVisitor extends Visitor {
        private List<Name> vars = new ArrayList<>();
        private Set<OffsetRange> offsets = new HashSet<>();
        private String name;
        private PythonParserResult info;
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarVisitor(PythonParserResult info, String name, boolean collectNames, boolean collectOffsets) {
            this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof Call && ((Call)parent).getInternalFunc() == node) {
                return super.visitName(node);
            }

            if ((name == null && !PythonUtils.isClassName(node.getInternalId(), false)) ||
                    (name != null && name.equals(node.getInternalId()))) {
                if (collectOffsets) {
                    OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                    if (lexRange != OffsetRange.NONE) {
                        offsets.add(astRange);
                    }
                }
                if (collectNames) {
                    vars.add(node);
                }
            }

            return super.visitName(node);
        }

        public Set<OffsetRange> getOffsets() {
            return offsets;
        }

        public List<Name> getVars() {
            return vars;
        }
    }

    private static class LocalVarAssignVisitor extends Visitor {
        private List<Name> vars = new ArrayList<>();
        private Set<OffsetRange> offsets = new HashSet<>();
        private String name;
        private PythonParserResult info;
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarAssignVisitor(PythonParserResult info, String name, boolean collectNames, boolean collectOffsets) {
            this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof FunctionDef || parent instanceof Assign) {
                if ((name == null && !PythonUtils.isClassName(node.getInternalId(), false)) ||
                        (name != null && name.equals(node.getInternalId()))) {
                    if (collectOffsets) {
                        OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
                        OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                        if (lexRange != OffsetRange.NONE) {
                            offsets.add(astRange);
                        }
                    }
                    if (collectNames) {
                        vars.add(node);
                    }
                }
            }

            return super.visitName(node);
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

        public Set<OffsetRange> getOffsets() {
            return offsets;
        }

        public List<Name> getVars() {
            return vars;
        }
    }

    /** Collect nodes of the given types (node.nodeId==NodeTypes.x) under the given root */
    public static void addNodesByType(PythonTree root, Class[] nodeClasses, List<PythonTree> result) {
        try {
            new NodeTypeVisitor(result, nodeClasses).visit(root);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class NodeTypeVisitor extends Visitor {
        private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
        private Class[] nodeClasses;
        private List<PythonTree> result;

        NodeTypeVisitor(List<PythonTree> result, Class[] nodeClasses) {
            this.result = result;
            this.nodeClasses = nodeClasses;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            for (Class nodeClasse : nodeClasses) {
                if (node.getClass() == nodeClasse) {
                    result.add(node);
                    break;
                }
            }

            try {
                super.traverse(node);
            } catch(RuntimeException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                if (ex.getMessage().startsWith("Unexpected node: <mismatched token: [@")) {
                   LOGGER.log(Level.FINE, ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }
    }

    public static Name getParentClassFromNode(AstPath path, PythonTree from, String name) {
        ClassDef curClass = (ClassDef)path.getTypedAncestor(ClassDef.class, from);
        if (curClass == null) {
            return null;
        }

        List<expr> baseClasses = curClass.getInternalBases();
        if (baseClasses == null) {
            return null; // no inheritance ;
        }
        int ii = 0;
        while (ii < baseClasses.size()) {
            if (baseClasses.get(ii) instanceof Name) {
                Name cur = (Name)baseClasses.get(ii);
                if (cur.getInternalId().equals(name)) {
                    return cur;
                }
            }
            ii++;
        }
        return null;
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    public static int findArgumentIndex(Call call, int astOffset, AstPath path) {

        // On the name part in the call rather than the args?
        if (astOffset <= call.getInternalFunc().getCharStopIndex()) {
            return -1;
        }
        List<expr> args = call.getInternalArgs();
        if (args != null) {
            int index = 0;
            for (; index < args.size(); index++) {
                expr et = args.get(index);
                if (et.getCharStopIndex() >= astOffset) {
                    return index;
                }
            }
        }

        // TODO what about the other stuff in there -- 
        //call.keywords;
        //call.kwargs;
        //call.starargs;

        return -1;
    }
}
