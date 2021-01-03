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
package org.netbeans.modules.python.source.scopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.python.api.Util;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.PythonIndexer;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.modules.python.source.elements.AstElement;
import org.netbeans.modules.python.source.elements.Element;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.ImportEntry;
import org.netbeans.modules.python.source.ImportManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.alias;
import org.python.antlr.base.expr;
import static org.netbeans.modules.python.source.scopes.ScopeConstants.*;
import org.python.antlr.ParseException;

/**
 * A symbol table tracks a bunch of scopes and can answer questions about defined
 * symbols.
 *
 * Based on Jython's ScopeManager.
 *
 * @author Tor Norbye
 */
public class SymbolTable {
    private final static int YES = 1;
    private final static int NO = 0;
    private final static int CIRCULAR = -1;
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    private Map<PythonTree, ScopeInfo> scopes = new HashMap<>();
    private PythonTree root;
    private FileObject fileObject;
    private List<Import> imports = new ArrayList<>();
    private List<ImportFrom> importsFrom = new ArrayList<>();
    private List<PythonTree> mainImports = new ArrayList<>();
    private Set<PythonTree> topLevelImports = new HashSet<>();
    private List<Error> errors;
    /** List of symbols registered via __all__ = [ "foo", "bar" ] or __all__.extend() or __all__.append() */
    private List<Str> publicSymbols;
    private final static HashMap<String, String> classAttributes = new HashMap<String, String>() {
        {
            put("__class__", "__class__");
            put("__bases__", "__bases__");
            put("__dict__", "__dict__");
            put("__doc__", "__doc__");
            put("__name__", "__bases");
        }
    };
    private HashMap<String, ClassDef> classes = new HashMap<>();
    // TODO - use WeakHashMap?
    static Map<String, Set<IndexedElement>> importedElements = new HashMap<>();

    private HashMap<String, ClassDef> buildLocalClasses() {
        HashMap<String, ClassDef> localClasses = new HashMap<>();
        for (PythonTree cur : scopes.keySet()) {
            if (cur instanceof ClassDef) {
                ClassDef curClass = (ClassDef)cur;
                localClasses.put(curClass.getInternalName(), curClass);
            }
        }
        return localClasses;
    }

    public SymbolTable(PythonTree root, FileObject fileObject) {
        this.root = root;
        this.fileObject = fileObject;

        if (root != null) {
            try {
                ScopesCompiler compiler = new ScopesCompiler(this, scopes, root, imports, importsFrom, mainImports, topLevelImports);
                compiler.parse();
                publicSymbols = compiler.getPublicSymbols();
                classes = buildLocalClasses();
                if (publicSymbols != null) {
                    // Mark all other symbols private!
                    Set<String> names = new HashSet<>(publicSymbols.size() + 1);
                    names.add("__all__"); // __all__ itself is exported!
                    for (Str str : publicSymbols) {
                        String name = PythonAstUtils.getStrContent(str);
                        if (name != null) {
                            names.add(name);
                        }
                    }

                    ScopeInfo topScope = scopes.get(root);
                    if (topScope != null) {
                        for (Map.Entry<String, SymInfo> entry : topScope.tbl.entrySet()) {
                            String name = entry.getKey();
                            if (!names.contains(name)) {
                                SymInfo sym = entry.getValue();
                                sym.flags |= PRIVATE;
                                if (sym.isDef() && sym.node != null) {
                                    ScopeInfo scope = scopes.get(sym.node);
                                    scope.hidden = true;
                                }
                            }
                        }
                    }

                    for (Map.Entry<PythonTree, ScopeInfo> entry : scopes.entrySet()) {
                        ScopeInfo scope = entry.getValue();
                        boolean isHidden = false;
                        ScopeInfo curr = scope;
                        while (curr != null) {
                            if (curr.hidden) {
                                isHidden = true;
                                break;
                            }
                            if (curr.nested != null) {
                                curr = curr.nested;
                            } else {
                                curr = curr.up;
                            }

                        }
                        if (isHidden) {
                            scope.hidden = true;
                        }
                    }

                    // Mark all symbols private, unless the scope is a direct descendant
                    // of a public symbol
                    for (ScopeInfo scope : scopes.values()) {
                        if (scope.hidden) {
                            for (SymInfo sym : scope.tbl.values()) {
                                sym.flags |= PRIVATE;
                            }
                        }
                    }
                }
            } catch (ParseException ex) {
                OffsetRange range = PythonAstUtils.getRange((PythonTree) ex.node);
                Error error = new DefaultError(null, ex.getMessage(), null, fileObject, range.getStart(), range.getEnd(), Severity.ERROR);
                if (errors == null) {
                    errors = new ArrayList<>();
                }
                errors.add(error);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean isPrivate(PythonTree node, String name) {
        ScopeInfo scope = scopes.get(node);
        if (scope == null) {
            scope = scopes.get(root);
        }
        if (scope != null) {
            if (scope.up != null) {
                if (scope.hidden) {
                    return true;
                }
                // Look in parent's scope table
                if (scope.nested != null) {
                    scope = scope.nested;
                } else {
                    scope = scope.up;
                }
                if (scope != null) {
                    SymInfo sym = scope.tbl.get(name);
                    if (sym != null) {
                        return sym.isPrivate();
                    }
                }
            } else {
                SymInfo sym = scope.tbl.get(name);
                if (sym != null) {
                    return sym.isPrivate();
                }
            }
        }

        return false;
    }

    public SymInfo findDeclaration(PythonTree scope, String name, boolean allowFree) {
        ScopeInfo scopeInfo = getScopeInfo(scope);
        if (scopeInfo != null) {
            SymInfo sym = scopeInfo.tbl.get(name);
            SymInfo orig = sym;
            while (sym != null && sym.isFree()) {
                scopeInfo = scopeInfo.up;
                while (scopeInfo != null && scopeInfo.kind == CLASSSCOPE) {
                    scopeInfo = scopeInfo.up;
                }
                sym = scopeInfo.tbl.get(name);
            }

            if (allowFree && sym == null && orig != null) {
                // Free variable -- might have to resolve it
                return orig;
            }

            // Look for attributes too
            if (sym == null) {
                sym = scopeInfo.attributes.get(name);
                orig = sym;
                while (sym != null && sym.isFree()) {
                    scopeInfo = scopeInfo.up;
                    while (scopeInfo != null && scopeInfo.kind == CLASSSCOPE) {
                        scopeInfo = scopeInfo.up;
                    }
                    sym = scopeInfo.tbl.get(name);
                }

                if (allowFree && sym == null && orig != null) {
                    // Free variable -- might have to resolve it
                    return orig;
                }
            }

            return sym;
        }

        return null;
    }

    public ScopeInfo getScopeInfo(PythonTree node) {
        return scopes.get(node);
    }

    public List<Error> getErrors() {
        return errors != null ? errors : Collections.<Error>emptyList();
    }

    public SymInfo findBySignature(ElementKind kind, String signature) {
        PythonTree scope = root;
        String name = signature;
        int dot = signature.lastIndexOf('.');
        if (dot != -1) {
            String clz = signature.substring(0, dot);
            name = signature.substring(dot + 1);
            SymInfo sym = findDeclaration(root, clz, true);
            if (sym != null && sym.node != null) {
                scope = sym.node;
            }
        }
        SymInfo sym = findDeclaration(scope, name, true);

        return sym;
    }

    private List<String> getModulesToStarImport() {
        List<String> modules = new ArrayList<>();

        for (ImportFrom from : importsFrom) {
            List<alias> names = from.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    if ("*".equals(at.getInternalName())) { // NOI18N
                        modules.add(from.getInternalModule());
                    }
                }
            }
        }

        modules.addAll(PythonIndex.BUILTIN_MODULES);

        return modules;
    }

    private void addSymbolsFromModule(PythonParserResult info, String module, String prefix, QuerySupport.Kind kind, Set<? super IndexedElement> result) {
        if (PythonIndex.isBuiltinModule(module)) {
            Set<IndexedElement> all = getAllSymbolsFromModule(info, module);
            for (IndexedElement e : all) {
                if (kind == QuerySupport.Kind.PREFIX) {
                    if (e.getName().startsWith(prefix)) {
                        result.add(e);
                    }
                } else if (kind == QuerySupport.Kind.EXACT) {
                    if (prefix.equals(e.getName())) {
                        result.add(e);
                    }
                } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) {
                    if (e.getName().regionMatches(true, 0, prefix, 0, prefix.length())) {
                        result.add(e);
                    }
                }
            }
        } else {
            PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
            Set<IndexedElement> elements = index.getImportedElements(prefix, kind, Collections.singleton(module), null);
            for (IndexedElement e : elements) {
                result.add(e);
            }
        }
    }

    private Set<IndexedElement> getAllSymbolsFromModule(PythonParserResult info, String module) {
        Set<IndexedElement> elements = importedElements.get(module);
        if (elements == null) {
            PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
            Set<String> systemHolder = new HashSet<>(3);
            elements = index.getImportedElements("", QuerySupport.Kind.PREFIX, Collections.singleton(module), systemHolder);
            // Cache system modules - don't cache local modules
            if (!systemHolder.isEmpty()) {
                importedElements.put(module, elements);
            }
        }

        return elements;
    }

    public Set<Element> getDefinedElements(PythonParserResult info, PythonTree scope, String prefix, QuerySupport.Kind kind) {
        Set<Element> elements = new HashSet<>(300);
        ScopeInfo scopeInfo = scopes.get(scope);
        String module = PythonUtils.getModuleName(fileObject);
        String url = fileObject.toURL().toExternalForm();

        // Get builtin symbols
        for (String mod : getModulesToStarImport()) {
            addSymbolsFromModule(info, mod, prefix, kind, elements);
        }

        // I can't just search the scope table for all variables in scope because this
        // will only include the local -bound- variables and the -used- free variables.
        // I need to find potential free variables as well. This means I should walk up
        // the scope chain and compute all eligible names. By keep track of the ones I've
        // already added I avoid adding references to variables I have re-bound in closer
        // scopes.

        Set<String> added = new HashSet<>();

        while (scopeInfo != null) {
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                if (added.contains(name)) {
                    // Something in narrower scope already processed this one
                    continue;
                }
                if (kind == QuerySupport.Kind.EXACT) {
                    if (!(name.equals(prefix))) {
                        continue;
                    }
                } else if (kind == QuerySupport.Kind.PREFIX) {
                    if (!name.startsWith(prefix)) {
                        continue;
                    }
                } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) {
                    if (!name.regionMatches(true, 0, prefix, 0, prefix.length())) {
                        continue;
                    }
                }
                SymInfo sym = entry.getValue();

                ScopeInfo curr = scopeInfo;
                while (sym != null && sym.isFree()) {
                    curr = curr.up;
                    while (curr != null && curr.kind == CLASSSCOPE) {
                        curr = curr.up;
                    }
                    if (curr == null) {
                        sym = null;
                        break;
                    }
                    sym = scopeInfo.tbl.get(name);
                }
                if (sym == null) {
                    continue;
                }
                if (sym.isUnresolved()) {
                    // Don't add completion items for stuff we're not sure about
                    continue;
                }

                PythonTree node = sym.node;
                if (node == null) {
                    continue;
                }


                if (sym.isImported()) {
                    Element element = new AstElement(this, node, name, Character.isUpperCase(name.charAt(0)) ? ElementKind.CLASS : ElementKind.MODULE);
                    elements.add(element);
                } else if (sym.isDef()) {
                    String signature;
                    if (sym.isClass() && node instanceof ClassDef) {
                        signature = PythonIndexer.computeClassSig((ClassDef)node, sym);
                    } else if (sym.isFunction() && node instanceof FunctionDef) {
                        assert sym.isFunction() && node instanceof FunctionDef : name + ";" + sym + " in " + module;
                        signature = PythonIndexer.computeFunctionSig(name, (FunctionDef)node, sym);
                    } else {
                        // Probably a generator expression
                        continue;
                    }
                    //Element element = AstElement.create(null, node);
                    IndexedElement element = IndexedElement.create(signature, module, url, null);
                    element.setSmart(true);
                    elements.add(element);
                } else {
                    // TODO - class attributes?
                    Element element = new AstElement(this, node, name, ElementKind.VARIABLE);
                    elements.add(element);
                }

                added.add(name);
            }

            scopeInfo = scopeInfo.up;
            while (scopeInfo != null && scopeInfo.kind == CLASSSCOPE) {
                scopeInfo = scopeInfo.up;
            }
        }

        return elements;
    }

    // Return all node references to the given name
    // This will include imports, calls, definitions, etc.
    public List<PythonTree> getOccurrences(PythonTree scope, String name, boolean abortOnFree) {
        ScopeInfo scopeInfo = scopes.get(scope);
        if (scopeInfo != null) {
            SymInfo sym = scopeInfo.tbl.get(name);
            while (sym != null && sym.isFree()) {
                if (abortOnFree) {
                    return null;
                }
                scopeInfo = scopeInfo.up;
                while (scopeInfo != null && scopeInfo.kind == CLASSSCOPE) {
                    scopeInfo = scopeInfo.up;
                }
                sym = scopeInfo.tbl.get(name);
            }

            if (sym != null) {
                NameNodeFinder finder = new NameNodeFinder(name, scopeInfo.scope_node);
                finder.run();
                return finder.getNodes();
            }
        }

        return Collections.emptyList();
    }

    /** Return a list of the variables visible from a given scope */
    public Set<String> getVarNames(PythonTree scope, boolean mustBeBound) {
        ScopeInfo scopeInfo = scopes.get(scope);
        Set<String> names = new HashSet<>();
        while (scopeInfo != null) {
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                SymInfo sym = entry.getValue();
                if (sym.isVariable(mustBeBound)) {
                    names.add(name);
                }
            }
            scopeInfo = scopeInfo.up;
            while (scopeInfo != null && scopeInfo.kind == CLASSSCOPE) {
                scopeInfo = scopeInfo.up;
            }
        }

        return names;
    }

    public List<ImportEntry> getUnusedImports() {
        List<ImportEntry> unused = new ArrayList<>();
        ScopeInfo scopeInfo = scopes.get(root);
        for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
            SymInfo sym = entry.getValue();
            if (sym.isImported() && !sym.isRead()) {
                String name = entry.getKey();
                if (name.equals("*")) { // NOI18N
                    // Not detecting usages of wildcard imports yet...
                    continue;
                }
                PythonTree node = sym.node;
                if (node instanceof Import) {
                    Import imp = (Import)node;
                    int ordinal = 0;
                    String module = null;
                    String asName = null;
                    List<alias> names = imp.getInternalNames();
                    if (names != null) {
                        for (alias at : names) {
                            if (name.equals(at.getInternalAsname())) {
                                module = at.getInternalName();
                                asName = at.getInternalAsname();
                                break;
                            } else if (name.equals(at.getInternalName())) {
                                module = at.getInternalName();
                                break;
                            }
                        }
                        if (module == null) {
                            // For imports with dotted names, like wsgiref.handlers,
                            // the symbol table entry is just "wsgiref", yet I have to match
                            // the symbols, so try again more carefully
                            for (alias at : names) {
                                if (at.getInternalAsname() != null && at.getInternalAsname().startsWith(name) &&
                                        at.getInternalAsname().charAt(name.length()) == '.') {
                                    module = at.getInternalName();
                                    asName = at.getInternalAsname();
                                    break;
                                } else if (at.getInternalName().startsWith(name) &&
                                        at.getInternalName().charAt(name.length()) == '.') {
                                    module = at.getInternalName();
                                    break;
                                }
                            }
                        }
                    }
                    unused.add(new ImportEntry(module, asName, true, imp, imp.getCharStartIndex() + (ordinal++)));
                } else if (node instanceof ImportFrom) {
                    ImportFrom imp = (ImportFrom)node;
                    if (ImportManager.isFutureImport(imp)) {
                        continue;
                    }
                    String module = imp.getInternalModule();
                    String origName = null;
                    String asName = null;
                    int ordinal = 0;
                    List<alias> names = imp.getInternalNames();
                    if (names != null) {
                        for (alias at : names) {
                            if (name.equals(at.getInternalAsname())) {
                                origName = at.getInternalName();
                                asName = at.getInternalAsname();
                                break;
                            } else if (name.equals(at.getInternalName())) {
                                origName = at.getInternalName();
                                break;
                            }
                        }
                        if (origName == null) {
                            // For imports with dotted names, like wsgiref.handlers,
                            // the symbol table entry is just "wsgiref", yet I have to match
                            // the symbols, so try again more carefully
                            for (alias at : names) {
                                if (at.getInternalAsname() != null && at.getInternalAsname().startsWith(name) &&
                                        at.getInternalAsname().charAt(name.length()) == '.') {
                                    origName = at.getInternalName();
                                    asName = at.getInternalAsname();
                                    break;
                                } else if (at.getInternalName().startsWith(name) &&
                                        at.getInternalName().charAt(name.length()) == '.') {
                                    origName = at.getInternalName();
                                    break;
                                }
                            }
                        }
                    }
                    unused.add(new ImportEntry(module, origName, asName, true, imp, imp.getCharStartIndex() + (ordinal++)));
                }
            }
        }

        return unused;
    }

    private class NameNodeFinder extends Visitor {
        private List<PythonTree> nodes = new ArrayList<>();
        private PythonTree startScope;
        private String name;

        public NameNodeFinder(String name, PythonTree startScope) {
            this.name = name;
            this.startScope = startScope;
        }

        public void run() {
            try {
                visit(startScope);
            } catch(ClassCastException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                LOGGER.log(Level.FINE, ex.getMessage());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Object visitImport(Import imp) throws Exception {
            List<alias> names = imp.getInternalNames();
            if (names != null && names.size() > 0) {
                boolean found = false;
                for (alias at : names) {
                    String asName = at.getInternalAsname();
                    if (asName != null) {
                        if (name.equals(asName)) {
                            found = true;
                            break;
                        }
                    } else if (name.equals(at.getInternalName())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    nodes.add(imp);
                }
            }
            return super.visitImport(imp);
        }

        @Override
        public Object visitImportFrom(ImportFrom imp) throws Exception {
            List<alias> names = imp.getInternalNames();
            if (names != null && names.size() > 0) {
                boolean found = false;
                for (alias at : names) {
                    String asName = at.getInternalAsname();
                    if (asName != null) {
                        if (name.equals(asName)) {
                            found = true;
                            break;
                        }
                    } else if (name.equals(at.getInternalName())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    nodes.add(imp);
                }
            }

            return super.visitImportFrom(imp);
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (node.getInternalId().equals(name)) {
                nodes.add(node);
            }
            return super.visitName(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            if (name.equals(node.getInternalName())) {
                nodes.add(node);
            }

            if (isIncludedScope(node)) {
                return super.visitFunctionDef(node);
            } else {
                return null;
            }
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            if (name.equals(node.getInternalName())) {
                nodes.add(node);
            }

            if (isIncludedScope(node)) {
                return super.visitClassDef(node);
            } else {
                return null;
            }
        }

        @Override
        public Object visitExpression(Expression node) throws Exception {
            if (isIncludedScope(node)) {
                return super.visitExpression(node);
            } else {
                return null;
            }
        }

        @Override
        public Object visitInteractive(Interactive node) throws Exception {
            if (isIncludedScope(node)) {
                return super.visitInteractive(node);
            } else {
                return null;
            }
        }

        @Override
        public Object visitLambda(Lambda node) throws Exception {
            if (isIncludedScope(node)) {
                return super.visitLambda(node);
            } else {
                return null;
            }
        }

        @Override
        public Object visitGeneratorExp(GeneratorExp node) throws Exception {
            if (isIncludedScope(node)) {
                return super.visitGeneratorExp(node);
            } else {
                return null;
            }
        }

        private boolean isIncludedScope(PythonTree node) {
            if (node == startScope) {
                return true;
            }

            ScopeInfo info = scopes.get(node);
            if (info == null) {
                return false;
            }

            SymInfo sym = info.tbl.get(name);
            // Skip scopes that redefine the variable
            if (sym != null && sym.isBound()) {
                return false;
            }

            return true;
        }

        public List<PythonTree> getNodes() {
            return nodes;
        }
    }

    public Map<String, SymInfo> getUnresolvedNames(PythonParserResult info) {
        Map<String, SymInfo> unresolved = new HashMap<>();
        Set<String> builtin = getBuiltin(info);

        for (ScopeInfo scopeInfo : scopes.values()) {
            Map<String, SymInfo> tbl = scopeInfo.tbl;
            for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
                SymInfo symInfo = entry.getValue();
                boolean isUnresolved = symInfo.isUnresolved();
                if (!isUnresolved && symInfo.isFree()) {
                    // Peek up scope stack
                    String name = entry.getKey();
                    SymInfo sym = symInfo;
                    ScopeInfo scope = scopeInfo;
                    while (sym != null && sym.isFree()) {
                        scope = scope.up;
                        while (scope != null && scope.kind == CLASSSCOPE) {
                            scope = scope.up;
                        }
                        sym = scope.tbl.get(name);
                    }
                    if (sym == null) {
                        isUnresolved = true;
                    } else {
                        isUnresolved = sym.isUnresolved();
                    }
                }
                if (isUnresolved) {
                    String key = entry.getKey();
                    if (!builtin.contains(key)) {
                        unresolved.put(key, symInfo);
                    }
                }
            }
        }

        return unresolved;
    }

    public List<Attribute> getNotInInitAttributes(PythonParserResult info) {
        List<Attribute> notInInitAttribs = new ArrayList<>();
        for (ScopeInfo scopeInfo : scopes.values()) {
            if (scopeInfo.scope_node instanceof ClassDef) {
                if (scopeInfo.attributes != null) {
                    for (Map.Entry<String, SymInfo> entry : scopeInfo.attributes.entrySet()) {
                        SymInfo symInfo = entry.getValue();
                        if (!symInfo.isBoundInConstructor()) {
                            notInInitAttribs.add((Attribute)symInfo.node);
                        }
                    }
                }
            }
        }
        return notInInitAttribs;
    }

    private ScopeInfo getClassScope(String className) {
        for (ScopeInfo scopeInfo : scopes.values()) {
            if (scopeInfo.scope_node instanceof ClassDef) {
                ClassDef curClass = (ClassDef)scopeInfo.scope_node;
                if (curClass.getInternalName().equals(className)) {
                    return scopeInfo;
                }
            }
        }
        return null;
    }

    private int belongsToParents(ClassDef cls, String name, HashMap<String, String> cycling) {
        List<expr> bases = cls.getInternalBases();
        if (bases == null || bases.size() == 0) {
            return NO; // no parents
        }
        for (expr base : bases) {
            String className = null;
            if (base instanceof Name) {
                className = ((Name)base).getInternalId();
            } else {
                // should be Attribute here( module.className form )
                // which imply imported from external scope
                // So we give up on scope returning optimistaically True
                return YES;
            }
            assert (className != null);
            if (cycling.get(className) != null) {
                cycling.clear();
                // put parent child conficting back in cycling
                cycling.put(className, cls.getInternalName());
                return CIRCULAR;
            }
            cycling.put(className, className);
            ScopeInfo localClassScope = getClassScope(className);
            if (localClassScope == null) {
                // return true (success) when at least one parent is outside module scope
                // just to notify caller to be optimistic and assume that
                // name is resolved by imported classes inheritance
                // scanning imported classed from here is discouraged for
                // performances reasons
                return YES;
            } else {
                if ((name != null) &&
                        (localClassScope.attributes.get(name) != null)) {
                    return YES;
                }
                // try recurse parentage to resolve attribute
                ClassDef parentClass = (ClassDef)localClassScope.scope_node;
                int recResult = belongsToParents(parentClass, name, cycling);
                if (recResult != NO) // stop on FOUND(YES) or CIRCULAR error
                {
                    return recResult;
                }
            }
        }
        return NO;
    }

    private boolean isImported(String moduleName) {
        for (Import imported : imports) {
            List<alias> names = imported.getInternalNames();
            if (names != null) {
                for (alias cur : names) {
                    String name = cur.getInternalName();
                    String asName = cur.getInternalAsname();
                    if (((name != null) && (name.equals(moduleName))) ||
                            ((asName != null) && (asName.equals(moduleName)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isImportedFrom(String className) {
        for (ImportFrom importedFrom : importsFrom) {
            List<alias> names = importedFrom.getInternalNames();
            if (names != null) {
                for (alias cur : names) {
                    String name = cur.getInternalName();
                    String asName = cur.getInternalAsname();
                    if (((name != null) && (name.equals(className))) ||
                            ((asName != null) && (asName.equals(className)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<PythonTree> getUnresolvedParents(PythonParserResult info) {
        // deal with unresolved parents in inherit trees
        List<PythonTree> unresolvedParents = new ArrayList<>();
        PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());

        for (String cur : classes.keySet()) {
            ClassDef cls = classes.get(cur);
            List<expr> bases = cls.getInternalBases();
            if (bases == null || bases.size() > 0) {
                // has parents
                for (expr base : bases) {
                    if (base instanceof Name) {
                        String className = ((Name)base).getInternalId();
                        Set<String> builtin = getBuiltin(info);
                        if ((!classes.containsKey(className)) &&
                                (!builtin.contains(className))) {
                            // check in from imports
                            if (!isImportedFrom(className)) {
                                unresolvedParents.add(base);
                            }
                        }
                    } else {
                        // should be Attribute here( module.className form )
                        // which imply imported from external scope
                        Attribute attr = (Attribute)base;
                        String clsName = attr.getInternalAttr();
                        if (attr.getInternalValue() instanceof Name) {
                            String moduleName = ((Name)(attr.getInternalValue())).getInternalId();
                            // check that import is resolved first
                            if (!isImported(moduleName)) {
                                unresolvedParents.add(base);
                            } else {
                                Set<IndexedElement> found = index.getImportedElements(clsName, QuerySupport.Kind.EXACT, Collections.<String>singleton(moduleName), null);
                                if (found.size() == 0) {
                                    unresolvedParents.add(base);
                                }
                            }
                        } else {
                            unresolvedParents.add(base);
                        }
                    }
                }
            }
        }
        return unresolvedParents;
    }

    public HashMap<ClassDef, String> getClassesCyclingRedundancies(PythonParserResult info) {
        HashMap<ClassDef, String> cyclingRedundancies = new HashMap<>();
        for (String cur : classes.keySet()) {
            HashMap<String, String> returned = new HashMap<>();
            ClassDef curClass = classes.get(cur);
            if (!cyclingRedundancies.containsKey(curClass)) {
                if (belongsToParents(curClass, null, returned) == CIRCULAR) {
                    // store hashMap returned
                    Map.Entry<String, String> cycling = returned.entrySet().iterator().next();
                    cyclingRedundancies.put(curClass, cycling.getKey());
                }
            }
        }
        return cyclingRedundancies;
    }

    public List<PythonTree> getUnresolvedAttributes(PythonParserResult info) {
        List<PythonTree> unresolvedNodes = new ArrayList<>();
        for (ScopeInfo scopeInfo : scopes.values()) {
            Set<String> unresolved = new HashSet<>();
            Map<String, SymInfo> tbl = scopeInfo.tbl;
            // unresolved attributes in local classes
            Map<String, SymInfo> attribs = scopeInfo.attributes;
            for (Map.Entry<String, SymInfo> curAttr : attribs.entrySet()) {
                SymInfo symInfo = curAttr.getValue();
                if (symInfo.isRead()) {
                    // check for builtin attribs first
                    if (classAttributes.get(curAttr.getKey()) == null) {
                        // not a builtin attribute
                        ScopeInfo parentScope = scopeInfo.getClassScope();
                        if (parentScope != null) {
                            // limit scope to Classes for self and inherited
                            Map<String, SymInfo> parentattribs = parentScope.attributes;
                            SymInfo classAttr = parentattribs.get(curAttr.getKey());
                            tbl = parentScope.tbl;
                            if (classAttr == null) {
                                // may be  also a reference to a method
                                classAttr = tbl.get(curAttr.getKey());
                            }
                            if (classAttr == null) {
                                // do not bother with method since they are
                                // managed by completion
                                ClassDef curClass = (ClassDef)parentScope.scope_node;
                                if (belongsToParents(curClass, curAttr.getKey(), new HashMap()) == NO) {
                                    if (!symInfo.isCalled()) {
                                        // no corresponding attributes
                                        //PythonTree tree = symInfo.node ;
                                        Attribute attr = (Attribute)symInfo.node;
                                        // Name name = new Name(tree.getToken(),attr.getInternalAttr(),attr.ctx) ;
                                        unresolvedNodes.add(attr);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (unresolved.size() > 0) {
                NameFinder finder = new NameFinder(unresolved);
                List<Name> nodes = finder.run(scopeInfo.scope_node);
                unresolvedNodes.addAll(nodes);
            }

        }

        if (unresolvedNodes.size() > 1) {
            Collections.sort(unresolvedNodes, PythonUtils.ATTRIBUTE_NAME_NODE_COMPARATOR);
            //Collections.sort(unusedNodes, PythonUtils.NODE_POS_COMPARATOR);
        }

        return unresolvedNodes;
    }

    public List<PythonTree> getUnresolved(PythonParserResult info) {
        List<PythonTree> unresolvedNodes = new ArrayList<>();
        Set<String> builtin = getBuiltin(info);

        for (ScopeInfo scopeInfo : scopes.values()) {
            Set<String> unresolved = new HashSet<>();
            Map<String, SymInfo> tbl = scopeInfo.tbl;
            for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
                SymInfo symInfo = entry.getValue();
                boolean isUnresolved = symInfo.isUnresolved();
                if (!isUnresolved && symInfo.isFree()) {
                    // Peek up scope stack
                    String name = entry.getKey();
                    SymInfo sym = symInfo;
                    ScopeInfo scope = scopeInfo;
                    while (sym != null && sym.isFree()) {
                        scope = scope.up;
                        while (scope != null && scope.kind == CLASSSCOPE) {
                            scope = scope.up;
                        }
                        sym = scope.tbl.get(name);
                    }
                    if (sym == null) {
                        isUnresolved = true;
                    } else {
                        isUnresolved = sym.isUnresolved();
                    }
                }
                if (isUnresolved) {
                    String key = entry.getKey();
                    if (!builtin.contains(key)) {
                        unresolved.add(key);
                    }
                }
            }


            if (unresolved.size() > 0) {
                // Check imports and see if it's resolved by existing imports
                PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
                // TODO - cache system libraries!
                // TODO - make method which doesn't create elements for these guys!
//                Set<IndexedElement> elements = index.getImportedElements("", NameKind.PREFIX, PythonIndex.ALL_SCOPE, imports, importsFrom);
//                for (IndexedElement e : elements) {
//                    unresolved.remove(e.getName());
//                }
                Set<String> wildcarded = index.getImportedFromWildcards(importsFrom);
                unresolved.removeAll(wildcarded);

                if (unresolved.size() > 0) {
                    NameFinder finder = new NameFinder(unresolved);
                    List<Name> nodes = finder.run(scopeInfo.scope_node);
                    unresolvedNodes.addAll(nodes);
                }
            }
        }

        if (unresolvedNodes.size() > 1) {
            Collections.sort(unresolvedNodes, PythonUtils.ATTRIBUTE_NAME_NODE_COMPARATOR);
            //Collections.sort(unusedNodes, PythonUtils.NODE_POS_COMPARATOR);
        }

        return unresolvedNodes;
    }

    public List<PythonTree> getUnused(boolean skipSelf, boolean skipParams) { // not used for unused imports, see separate method
        List<PythonTree> unusedNodes = new ArrayList<>();

        for (ScopeInfo scopeInfo : scopes.values()) {
            if (scopeInfo.kind != FUNCSCOPE && scopeInfo.kind != TOPSCOPE && scopeInfo.kind != CLASSSCOPE) {
                continue;
            }
            Set<String> unused = new HashSet<>();
            Map<String, SymInfo> tbl = scopeInfo.tbl;
            for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
                SymInfo symInfo = entry.getValue();
                if (symInfo.isUnused(scopeInfo) && (!skipParams || !symInfo.isParameter())) {
                    String key = entry.getKey();
                    if (skipSelf && "self".equals(key)) { // NOI18N
                        continue;
                    }
                    unused.add(key);
                }
            }

            if (unused.size() > 0) {
                NameFinder finder = new NameFinder(unused);
                List<Name> nodes = finder.run(scopeInfo.scope_node);
                unusedNodes.addAll(nodes);
            }
        }

        if (unusedNodes.size() > 1) {
            Collections.sort(unusedNodes, PythonUtils.NAME_NODE_COMPARATOR);
            //Collections.sort(unusedNodes, PythonUtils.NODE_POS_COMPARATOR);
        }

        return unusedNodes;
    }

    private static class NameFinder extends Visitor {
        private Set<String> names;
        private List<Name> nodes = new ArrayList<>();
        private PythonTree acceptDef;

        private NameFinder(Set<String> names) {
            this.names = names;
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            // Don't look in nested scopes
            if (node != acceptDef) {
                return null;
            }
            return super.visitClassDef(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            // Don't look in nested scopes
            if (node != acceptDef) {
                return null;
            }
            Object ret;
            try {
                ret = super.visitFunctionDef(node);
            } catch (RuntimeException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                if (ex.getMessage().startsWith("Unexpected node: <mismatched token: [@")) {
                   ret = null;
                   LOGGER.log(Level.FINE, ex.getMessage());
                } else {
                    throw ex;
                }
            }
            return ret;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            String name = node.getInternalId();
            if (names.contains(name)) {
                nodes.add(node);
            }

            return super.visitName(node);
        }

        public List<Name> run(PythonTree node) {
            this.acceptDef = node;
            try {
                visit(node);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            return nodes;
        }
    }
    private static Set<String> builtinSymbols;

    private Set<String> getBuiltin(PythonParserResult info) {
        if (builtinSymbols == null) {
            PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
            builtinSymbols = index.getBuiltinSymbols();
        }

        return builtinSymbols;
    }

    public void error(String msg, boolean err, PythonTree node) throws Exception {
        assert node != null;
        // TODO - record and register with the hints manager?
        OffsetRange range = PythonAstUtils.getRange(node);

        if (errors == null) {
            errors = new ArrayList<>();
        }
        Error error = new DefaultError(null, msg, null, fileObject, range.getStart(), range.getEnd(), err ? Severity.ERROR : Severity.WARNING);
        errors.add(error);
    }

    public String getFilename() {
        return FileUtil.getFileDisplayName(fileObject);
    }

    public Map<PythonTree, ScopeInfo> getScopes() {
        return scopes;
    }

    public List<Import> getImports() {
        return imports;
    }

    public List<ImportFrom> getImportsFrom() {
        return importsFrom;
    }

    public boolean isTopLevel(PythonTree node) {
        return topLevelImports.contains(node);
    }

    public List<PythonTree> getMainImports() {
        return mainImports;
    }

    public Set<PythonTree> getTopLevelImports() {
        return topLevelImports;
    }

    public List<Str> getPublicSymbols() {
        return publicSymbols;
    }
}
