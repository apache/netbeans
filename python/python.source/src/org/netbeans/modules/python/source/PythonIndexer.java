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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.scopes.ScopeConstants;
import org.netbeans.modules.python.source.scopes.ScopeInfo;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.netbeans.modules.python.source.scopes.SymInfo;
import org.netbeans.modules.python.source.queries.DeprecationQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;

/**
 *
 * @todo Store information about all symbols exported by a module.
 *  I can use that to provide "unused import" help.
 * @todo Clean this stuff up: store data, functions, etc.
 * @todo Improve detection of builtins. Perhaps run from within Python,
 *   something like this:
>>> dir(__builtins__)
['ArithmeticError', 'AssertionError', 'AttributeError', 'BaseException', 'DeprecationWarning', 'EOFError', 'Ellipsis', 'EnvironmentError', 'Exception', 'False', 'FloatingPointError', 'FutureWarning', 'GeneratorExit', 'IOError', 'ImportError', 'ImportWarning', 'IndentationError', 'IndexError', 'KeyError', 'KeyboardInterrupt', 'LookupError', 'MemoryError', 'NameError', 'None', 'NotImplemented', 'NotImplementedError', 'OSError', 'OverflowError', 'PendingDeprecationWarning', 'ReferenceError', 'RuntimeError', 'RuntimeWarning', 'StandardError', 'StopIteration', 'SyntaxError', 'SyntaxWarning', 'SystemError', 'SystemExit', 'TabError', 'True', 'TypeError', 'UnboundLocalError', 'UnicodeDecodeError', 'UnicodeEncodeError', 'UnicodeError', 'UnicodeTranslateError', 'UnicodeWarning', 'UserWarning', 'ValueError', 'Warning', 'ZeroDivisionError', '_', '__debug__', '__doc__', '__import__', '__name__', 'abs', 'all', 'any', 'apply', 'basestring', 'bool', 'buffer', 'callable', 'chr', 'classmethod', 'cmp', 'coerce', 'compile', 'complex', 'copyright', 'credits', 'delattr', 'dict', 'dir', 'divmod', 'enumerate', 'eval', 'execfile', 'exit', 'file', 'filter', 'float', 'frozenset', 'getattr', 'globals', 'hasattr', 'hash', 'help', 'hex', 'id', 'input', 'int', 'intern', 'isinstance', 'issubclass', 'iter', 'len', 'license', 'list', 'locals', 'long', 'map', 'max', 'min', 'object', 'oct', 'open', 'ord', 'pow', 'property', 'quit', 'range', 'raw_input', 'reduce', 'reload', 'repr', 'reversed', 'round', 'set', 'setattr', 'slice', 'sorted', 'staticmethod', 'str', 'sum', 'super', 'tuple', 'type', 'unichr', 'unicode', 'vars', 'xrange', 'zip']
 *
 * My code for scanning for functions has to be smarter:
.. function:: ljust(s, width)
rjust(s, width)
center(s, width)
 * Here I need to pick up all 3 signatures!
 */
public class PythonIndexer extends EmbeddingIndexer {
    public static final String NAME = "PythonIndexer";
    public static final int VERSION = 1;
    public static boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing"); // NOI18N
    public static final String FIELD_MEMBER = "member"; //NOI18N
    public static final String FIELD_MODULE_NAME = "module"; //NOI18N
    public static final String FIELD_MODULE_ATTR_NAME = "modattrs"; //NOI18N
    public static final String FIELD_CLASS_ATTR_NAME = "clzattrs"; //NOI18N
    public static final String FIELD_EXTENDS_NAME = "extends"; //NOI18N
    public static final String FIELD_ITEM = "item"; //NOI18N
    public static final String FIELD_IN = "in"; //NOI18N
    public static final String FIELD_CLASS_NAME = "class"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    private FileObject prevParent;
    private boolean prevResult;

    public static boolean isIndexable(Indexable indexable, Snapshot snapshot) {
        FileObject fo = snapshot.getSource().getFileObject();
        String extension = fo.getExt();
        if ("py".equals(extension)) { // NOI18N
            return true;
        }

        if ("rst".equals(extension)) { // NOI18N
            // Index restructured text if it looks like it contains Python library
            // definitions
            return true;
        }

        if ("egg".equals(extension)) { // NOI18N
            return true;
        }

        return false;
    }

    
    public boolean isIndexable(Snapshot file) {
        FileObject fo = file.getSource().getFileObject();
        String extension = fo.getExt();
        if ("py".equals(extension)) { // NOI18N

            // Skip "test" folders under lib... Lots of weird files there
            // and we don't want to pollute the index with them
            FileObject parent = fo.getParent();

            if (parent != null && parent.getName().equals("test")) { // NOI18N
                // Make sure it's really a lib folder, we want to include the
                // user's files

                // Avoid double-indexing files that have multiple versions - e.g. foo.js and foo-min.js
                // or foo.uncompressed
                FileObject parentFo = fo.getParent();
                if (prevParent == parentFo) {
                    return prevResult;
                }
                prevResult = true;
                prevParent = parentFo;
                PythonPlatformManager manager = PythonPlatformManager.getInstance();
                Platforms:
                for (String name : manager.getPlatformList()) {
                    PythonPlatform platform = manager.getPlatform(name);
                    if (platform != null) {
                        for (FileObject root : platform.getLibraryRoots()) {
                            if (FileUtil.isParentOf(root, parentFo)) {
                                prevResult = false;
                                break Platforms;
                            }
                        }
                    }
                }
            }

            return true;
        }

        if ("rst".equals(extension)) { // NOI18N
            // Index restructured text if it looks like it contains Python library
            // definitions
            return true;
        }

        if ("egg".equals(extension)) { // NOI18N
            return true;
        }

        return false;
    }

    @Override
    protected void index(Indexable indexable, Parser.Result result, Context context) {
        PythonParserResult parseResult = (PythonParserResult)result;
        if (parseResult == null) {
            return;
        }
        
        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }
        
        support.removeDocuments(indexable);
        
        FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        String extension = fileObject.getNameExt();

        if (extension.endsWith(".rst")) { // NOI18N
            scanRst(fileObject, indexable, support, null);
        } else if (extension.endsWith(".egg")) { // NOI18N
            scanEgg(fileObject, indexable, parseResult, support);
        } else {
            // Normal python file
            new IndexTask(parseResult, support).scan();
        }
    }
    private static final Logger LOG = Logger.getLogger(PythonIndexer.class.getName());

    public boolean acceptQueryPath(String url) {
        return !url.contains("jsstubs"); // NOI18N
    }

    public String getPersistentUrl(File file) {
        String url;
        try {
            url = file.toURI().toURL().toExternalForm();

            // Make relative URLs for urls in the libraries
            return PythonIndex.getPreindexUrl(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return file.getPath();
        }
    }

    public String getIndexVersion() {
        return "0.123"; // NOI18N
    }

    public String getIndexerName() {
        return "python"; // NOI18N
    }

    public FileObject getPreindexedDb() {
        return null;
    }

    private static void appendFlags(StringBuilder sb, char c, SymInfo sym, int flags) {
        sb.append(';');
        sb.append(c);
        sb.append(';');

        if (sym.isPrivate()) {
            flags |= IndexedElement.PRIVATE;
        }
        if (c == 'c') {
            flags |= IndexedElement.CONSTRUCTOR;
        }

        sb.append(IndexedElement.encode(flags));
        sb.append(';');
    }
    private static final int DEFAULT_DOC_SIZE = 40; // TODO Measure

    private static class IndexTask {
        private PythonParserResult result;
        private FileObject file;
        private IndexingSupport support;
        private List<IndexDocument> documents = new ArrayList<>();
        private String url;
        private String module;
        private SymbolTable symbolTable;
        private String overrideUrl;

        private IndexTask(PythonParserResult result, IndexingSupport support) {
            this.result = result;
            this.file = result.getSnapshot().getSource().getFileObject();
            this.support = support;

            module = PythonUtils.getModuleName(file);
            //PythonTree root = PythonAstUtils.getRoot(result);
            //if (root instanceof Module) {
            //    Str moduleDoc = PythonAstUtils.getDocumentationNode(root);
            //    if (moduleDoc != null) {
            //        moduleAttributes = "d(" + moduleDoc.getCharStartIndex() + ")";
            //    }
            //}
        }

        private IndexTask(PythonParserResult result, IndexingSupport support, String overrideUrl) {
            this(result, support);
            this.overrideUrl = overrideUrl;
        }

        public List<IndexDocument> scan() {
            url = file.toURL().toExternalForm();
            // Make relative URLs for urls in the libraries
            url = PythonIndex.getPreindexUrl(url);

            IndexDocument doc = createDocument();
            doc.addPair(FIELD_MODULE_NAME, module, true, true);

            String moduleAttrs = null;
            if (url.startsWith(PythonIndex.CLUSTER_URL) || url.startsWith(PythonIndex.PYTHONHOME_URL)) {
                moduleAttrs = "S"; // NOI18N
            } else if (PREINDEXING) {
                String prj = System.getProperty("gsf.preindexing.projectpath");
                if (prj != null && !url.contains(prj)) {
                    System.err.println("WARNING -- not marking url " + url + " from " + file + " as a system library!");
                }
            }
            if (DeprecationQuery.isDeprecatedModule(module)) {
                if (moduleAttrs == null) {
                    moduleAttrs = "D"; // NOI18N
                } else {
                    moduleAttrs += "D"; // NOI18N
                }
            }
            if (moduleAttrs != null) {
                doc.addPair(FIELD_MODULE_ATTR_NAME, moduleAttrs, false, true);
            }

            PythonTree root = PythonAstUtils.getRoot(result);
            if (root == null) {
                return documents;
            }
            if (!(root instanceof Module)) {
                // Unexpected... http://netbeans.org/bugzilla/show_bug.cgi?id=165756
                // Maybe some kind of top level error node?
                System.err.println("WARNING - top level AST node type was " + root + " of type " + root.getClass().getName());
                return documents;
            }
            symbolTable = result.getSymbolTable();
            ScopeInfo scopeInfo = symbolTable.getScopeInfo(root);
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                SymInfo sym = entry.getValue();

                if (sym.isClass()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'C', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true, true);

                    if (sym.node instanceof ClassDef) {
                        assert sym.node instanceof ClassDef : sym.node;
                        indexClass(name, sym, (ClassDef)sym.node);
                    } else {
                        // Could be a symbol defined both as a class and a function
                        // (conditionally) such as _Environ in minicompat.py,
                        // and another trigger in socket.py.
                    }
                } else if (sym.isFunction()) {
                    if (sym.node instanceof Name) {
                        assert false : "Unexpected non-function node, " + ((Name)sym.node).getInternalId() + " - from symbol " + name + " in " + file + " with sym=" + sym;
                    }
                    assert sym.node instanceof FunctionDef : sym.node;
                    FunctionDef def = (FunctionDef)sym.node;
                    String sig = computeFunctionSig(name, def, sym);
                    doc.addPair(FIELD_ITEM, sig, true, true);
                } else if (sym.isImported()) {
                    if (!"*".equals(name)) { // NOI18N
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'I', sym, 0);
                        doc.addPair(FIELD_ITEM, sig.toString(), true, true);
                    }
                } else if (sym.isGeneratorExp()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'G', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true, true);
                } else if (sym.isData()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'D', sym, 0);
                    doc.addPair(FIELD_ITEM, sig.toString(), true, true);
                } else {
                    // XXX what the heck is this??
                }
            }

            return documents;
        }

        private void indexClass(String className, SymInfo classSym, ClassDef clz) {
            IndexDocument classDocument = createDocument();
            classDocument.addPair(FIELD_IN, module, true, true);

            // Superclass
            List<expr> bases = clz.getInternalBases();
            if (bases != null) {
                for (expr base : bases) {
                    String extendsName = PythonAstUtils.getExprName(base);
                    if (extendsName != null) {
                        classDocument.addPair(FIELD_EXTENDS_NAME, extendsName, true, true);
                    }
                }
            }

            classDocument.addPair(FIELD_CLASS_NAME, className, true, true);

            if (classSym.isPrivate()) {
                // TODO - store Documented, Deprecated, DocOnly, etc.
                classDocument.addPair(FIELD_CLASS_ATTR_NAME, IndexedElement.encode(IndexedElement.PRIVATE), false, true);
            }
            classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, className.toLowerCase(), true, true);

            //Str doc = PythonAstUtils.getDocumentationNode(clz);
            //if (doc != null) {
            //    StringBuilder sb = new StringBuilder();
            //    sb.append("d("); // NOI18N
            //    sb.append(doc.getCharStartIndex());
            //    sb.append(")"); // NOI18N
            //    classDocument.addPair(FIELD_CLASS_ATTRS, sb.toString(), false);
            //}

            ScopeInfo scopeInfo = symbolTable.getScopeInfo(clz);
            for (Map.Entry<String, SymInfo> entry : scopeInfo.tbl.entrySet()) {
                String name = entry.getKey();
                SymInfo sym = entry.getValue();

//                int flags = sym.flags;
//                assert !sym.isClass() : "found a class " + name + " of type " + sym.dumpFlags(scopeInfo) + " within class " + className + " in module " + module;
//                if (!(sym.isFunction() || sym.isMember() || sym.isData())) {
//                }
//                assert sym.isFunction() || sym.isMember() || sym.isData() : name + ";" + sym.toString();

                if (sym.isClass()) {
                    // Triggers in httplib _socket_close inside FakeSocket
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'C', sym, 0);
                    classDocument.addPair(FIELD_ITEM, sig.toString(), true, true);

                } else if (sym.isFunction() && sym.node instanceof FunctionDef) {
                    if (sym.node instanceof Name) {
                        assert false : "Unexpected non-function node, " + ((Name)sym.node).getInternalId() + " - from symbol " + name + " in " + file + " with sym=" + sym;
                    }
                    FunctionDef def = (FunctionDef)sym.node;
                    String sig = computeFunctionSig(name, def, sym);
                    classDocument.addPair(FIELD_MEMBER, sig, true, true);
                } else if (sym.isData()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'D', sym, 0);
                    classDocument.addPair(FIELD_MEMBER, sig.toString(), true, true);
                } else if (sym.isMember()) {
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);
                    appendFlags(sig, 'A', sym, 0);
                    classDocument.addPair(FIELD_MEMBER, sig.toString(), true, true);
                } else if (!sym.isBound()) {
                    continue;
                } else {
                    // XXX what the heck is this??
                    assert false : className + "::" + name + " : " + sym.dumpFlags(scopeInfo);
                }
            }

            if (scopeInfo.attributes.size() > 0) {
                for (Map.Entry<String, SymInfo> entry : scopeInfo.attributes.entrySet()) {
                    String name = entry.getKey();
                    SymInfo sym = entry.getValue();

                    if (sym.isClass()) {
                        // Triggers in httplib _socket_close inside FakeSocket
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'C', sym, 0);
                        classDocument.addPair(FIELD_ITEM, sig.toString(), true, true);

                    } else if (sym.isFunction() && sym.node instanceof FunctionDef) {
                        if (sym.node instanceof Name) {
                            assert false : "Unexpected non-function node, " + ((Name)sym.node).getInternalId() + " - from symbol " + name + " in " + file + " with sym=" + sym;
                        }
                        FunctionDef def = (FunctionDef)sym.node;
                        String sig = computeFunctionSig(name, def, sym);
                        classDocument.addPair(FIELD_MEMBER, sig, true, true);
                    } else if (sym.isData()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'D', sym, 0);
                        classDocument.addPair(FIELD_MEMBER, sig.toString(), true, true);
                    } else if (sym.isMember()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(name);
                        appendFlags(sig, 'A', sym, 0);
                        classDocument.addPair(FIELD_MEMBER, sig.toString(), true, true);
                    } else if (!sym.isBound()) {
                        continue;
                    } else {
                        // XXX what the heck is this??
                        assert false : className + "::" + name + " : " + sym.dumpFlags(scopeInfo);
                    }
                }
            }
        }


// TODO - what about nested functions?
        private IndexDocument createDocument() {
            IndexDocument doc = support.createDocument(file);
            documents.add(doc);

            return doc;
        }
    }

    public static String computeClassSig(ClassDef def, SymInfo sym) {
        StringBuilder sig = new StringBuilder();
        sig.append(def.getInternalName());
        appendFlags(sig, 'C', sym, 0);

        return sig.toString();
    }

    public static String computeFunctionSig(String name, FunctionDef def, SymInfo sym) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        char type;
        int flags = 0;
        if ("__init__".equals(name)) { // NOI18N
            type = 'c';
        } else {
            type = 'F';

            List<expr> decorators = def.getInternalDecorator_list();
            if (decorators != null && decorators.size() > 0) {
                for (expr decorator : decorators) {
                    String decoratorName = PythonAstUtils.getExprName(decorator);
                    if ("property".equals(decoratorName)) { // NOI18N
                        type = 'A';
                    } else if ("classmethod".equals(decoratorName)) { // NOI18N
                        // Classmethods seem to be used mostly for constructors/inherited factories
                        type = 'c';
                        flags |= IndexedElement.CONSTRUCTOR | IndexedElement.STATIC;
                    } else if ("staticmethod".equals(decoratorName)) { // NOI18N
                        flags |= IndexedElement.STATIC;
                    }
                }
            }
        }
        appendFlags(sb, type, sym, flags);

        List<String> params = PythonAstUtils.getParameters(def);
        boolean first = true;
        for (String param : params) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(param);
        }
        sb.append(';');
        String sig = sb.toString();
        return sig;
    }

    private String cleanupSignature(String signature) {
        // Clean up signatures - remove [optional] areas, deal
        //   with arg=Default.Value parameters,
        //   or "literal" or (lit,er,al) default values.
        // See unit tests for details.
        boolean lastWasComma = false;
        StringBuilder sb = new StringBuilder();
        Loop:
        for (int i = 0, n = signature.length(); i < n; i++) {
            char c = signature.charAt(i);
            switch (c) {
            case ' ':
            case '[':
            case ']':
            case '\'':
            case '"':
            case '.':
                continue Loop;
            case '=': {
                int level = 0;
                for (i++; i < n; i++) {
                    c = signature.charAt(i);
                    if (c == '(') {
                        level++;
                    } else if (c == ')') {
                        if (level == 0) {
                            break;
                        }
                        level--;
                    }
                    if (c == ',' && level == 0) {
                        break;
                    }
                }
                i--; // compensate for loop-increment
                continue Loop;
            }
            case ')':
                if (lastWasComma) {
                    sb.setLength(sb.length() - 1);
                    lastWasComma = false;
                }
                break;
            case ',':
                if (lastWasComma) {
                    continue Loop;
                }
                lastWasComma = true;
                break;
            default:
                lastWasComma = false;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Determine if the definition beginning on lines[lineno] is deprecated.
     */
    private boolean isDeprecated(String[] lines, int lineno) {
        int firstIndent = RstFormatter.getIndentation(lines[lineno], 0);
        for (int i = lineno + 1; i < lines.length; i++) {
            String line = lines[i];
            int indent = RstFormatter.getIndentation(line, 0);
            if (indent == -1) { // empty line
                continue;
            }
            if (line.contains(":deprecated:") || line.contains(".. deprecated::")) { // NOI18N
                return true;
            }
            // Note - we checked for ::deprecated BEFORE bailing on the next
            // same-indent line, because in some cases, these appear on the same
            // level as the deprecated element (for exampe, modules)
            if (indent <= firstIndent) {
                return false;
            }

            // For classes we can have embedded definitions of functions/data/methods --
            // a deprecated note for these should not be considered a deprecation of
            // the whole class! See the unit test for bz2.zip for example.
            if (line.startsWith(".. attribute::", indent) || // NOI18N
                    line.startsWith(".. data::", indent) || // NOI18N
                    line.startsWith(".. function::", indent) || // NOI18N
                    line.startsWith(".. method::", indent)) { // NOI18N
                return false;
            }
        }

        return false;
    }

    private static class CachedIndexDocument {
        private List<CachedIndexDocumentEntry> entries = new ArrayList<>(DEFAULT_DOC_SIZE);

        private void addPair(String key, String value, boolean index) {
            entries.add(new CachedIndexDocumentEntry(key, value, index));
        }
    }

    private static class CachedIndexDocumentEntry {
        private String key;
        private String value;
        private boolean index;

        public CachedIndexDocumentEntry(String key, String value, boolean index) {
            this.key = key;
            this.value = value;
            this.index = index;
        }
    }

    private List<IndexDocument> scanRst(FileObject fo, Indexable indexable, IndexingSupport support, String overrideUrl) {
        List<CachedIndexDocument> documents = new ArrayList<>();

        List<IndexDocument> docs = new ArrayList<>();
        
        if (fo != null) {
            String module = fo.getNameExt();
            assert module.endsWith(".rst"); // NOI18N
            module = module.substring(0, module.length() - 4);

            // Skip files that are already in the standard Python libraries (as .py files).
            // For these, normal scanning applies
            // (I should consider checking that they are consistent with the official
            // documentation, at least during preindexing)
            if (PREINDEXING) {
                // XXX This doesn't work right for anything but the builtin Jython interpreter....
                // OTOH that's the only thing we're preindexing at this point
                FileObject lib = getLibDir();
                if (lib != null) {
                    String path = module.replace('.', '/');
                    FileObject py = lib.getFileObject(path); // Look for package dir
                    if (py == null) {
                        py = lib.getFileObject(path + ".py"); // NOI18N
                    }
                    if (py != null) {
                        System.err.println("DELETE " + FileUtil.getFileDisplayName(fo) + " because there is a corresponding " + FileUtil.getFileDisplayName(py)); // NOI18N
                        // No - it's in a zip archive now
                        //try {
                        //    // Delete it!
                        //    fo.delete();
                        //} catch (IOException ex) {
                        //    Exceptions.printStackTrace(ex);
                        //}
                        return Collections.emptyList();
                    }
                }
            }

            String name = fo.getName();

            // Skip some really obsolete libraries -- IRIX only etc
            if (name.equals("gl") || name.equals("cd") || // NOI18N
                    name.equals("al") || name.equals("fm") ||
                    name.equals("fl") || name.equals("imgfile") || // NOI18N
                    name.equals("jpeg") || // NOI18N
                    name.equals("sunau") || name.equals("sunaudio")) { // NOI!8N
                return Collections.emptyList();
            }

            Pattern PATTERN = Pattern.compile("\\s*\\.\\.\\s+(.*)::\\s*(.+)\\s*"); // NOI18N

            BaseDocument doc = GsfUtilities.getDocument(fo, true);
            if (doc != null) {
                Map<String, CachedIndexDocument> classDocs = new HashMap<>();
                CachedIndexDocument document = null;
                try {
                    String text = doc.getText(0, doc.getLength());
                    String[] lines = text.split("\n");
                    String currentClass = null;

                    for (int lineno = 0, maxLines = lines.length; lineno < maxLines; lineno++) {
                        String line = lines[lineno];
                        if (!line.startsWith(".. ") && !line.contains(" .. ")) { // NOI18N
                            continue;
                        }

                        Matcher m = PATTERN.matcher(line);
                        if (m.matches()) {
                            String key = m.group(1);

                            if (key.equals("attribute") || // NOI18N
                                    key.equals("currentmodule") || // NOI18N
                                    key.equals("class") || // NOI18N
                                    key.equals("exception") || // NOI18N
                                    key.equals("function") || // NOI18N
                                    key.equals("method") || // NOI18N
                                    key.equals("data") || // NOI18N
                                    key.equals("module")) {  // NOI18N


                                if (key.equals("module") || key.equals("currentmodule")) {  // NOI18N
                                    // TODO - determine package name
                                    module = m.group(2);
                                    document = new CachedIndexDocument();
                                    documents.add(document);
                                    document.addPair(FIELD_MODULE_NAME, module, true);
                                    String moduleAttrs = "S";
                                    if (isDeprecated(lines, lineno)) {
                                        moduleAttrs = "SD";
                                    }
                                    document.addPair(FIELD_MODULE_ATTR_NAME, moduleAttrs, false); // NOI18N
                                } else {
                                    // Methods described in an rst without an actual module definition...
                                    if (document == null) {
                                        document = new CachedIndexDocument();
                                        documents.add(document);
                                        document.addPair(FIELD_MODULE_NAME, module, true);
                                        document.addPair(FIELD_MODULE_ATTR_NAME, "S", false); // NOI18N
                                    }
                                    if (key.equals("method") || key.equals("attribute")) { // NOI18N) { // NOI18N
                                        String signature = m.group(2);

                                        if ("string.template".equals(signature)) { // NOI18N
                                            // Wrong - ignore this one (ends up on the String class)
                                            continue;
                                        }
                                        if (signature.startsWith("somenamedtuple.")) {
                                            // Ditto
                                            continue;
                                        }
                                        // Error in mailbox.rst - Python 2.6
                                        if (".et_folder(folder)".equals(signature)) {
                                            signature = "get_folder(folder)";
                                        }

                                        int dot = signature.indexOf('.');
                                        if (dot != -1) {
                                            int paren = signature.indexOf('(');
                                            if (paren == -1 || paren > dot) {
                                                assert signature.matches("\\w+\\.\\w+.*") : signature;
                                                String dottedName = signature.substring(0, dot);
                                                CachedIndexDocument dottedDoc = classDocs.get(dottedName);
                                                if (dottedDoc != null) {
                                                    currentClass = dottedName;
                                                } else /*if (currentClass == null)*/ {
                                                    currentClass = dottedName;
                                                    // New class without class:: declaration first.
                                                    CachedIndexDocument classDocument = new CachedIndexDocument();
                                                    documents.add(classDocument);
                                                    classDocs.put(currentClass, classDocument);
                                                    classDocument.addPair(FIELD_IN, module, true);

                                                    classDocument.addPair(FIELD_CLASS_NAME, currentClass, true);
                                                    classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, currentClass.toLowerCase(), true);
                                                }
                                                signature = signature.substring(dot + 1);
                                            }
                                        }


                                        CachedIndexDocument classDocument = classDocs.get(currentClass);
                                        assert classDocs != null;

                                        if (key.equals("method")) {
                                            signature = cleanupSignature(signature);
                                            if (signature.indexOf('(') == -1) {
                                                signature = signature + "()";
                                            }

                                            assert signature.indexOf('(') != -1 && signature.indexOf(')') != -1 &&
                                                    signature.indexOf(')') > signature.indexOf('(') : signature;
                                            int lparen = signature.indexOf('(');
                                            int rparen = signature.indexOf(')', lparen + 1);
                                            if (lparen != -1 && rparen != -1) {
                                                String methodName = signature.substring(0, lparen);
                                                String args = signature.substring(lparen + 1, rparen);
                                                char type;
                                                if (methodName.equals("__init__")) { // NOI18N
                                                    type = 'c';
                                                } else {
                                                    type = 'F';
                                                }
                                                StringBuilder sig = new StringBuilder();
                                                sig.append(methodName);

                                                int symFlags = 0;
                                                if (NameStyle.isPrivateName(methodName)) {
                                                    symFlags |= ScopeConstants.PRIVATE;
                                                }
                                                // TODO - look up deprecated etc.
                                                SymInfo fakeSym = new SymInfo(symFlags);

                                                int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                                if (isDeprecated(lines, lineno)) {
                                                    flags |= IndexedElement.DEPRECATED;
                                                }

                                                appendFlags(sig, type, fakeSym, flags);
                                                sig.append(args);
                                                sig.append(';');

                                                classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                            }
                                        } else {
                                            assert key.equals("attribute");

                                            StringBuilder sig = new StringBuilder();
                                            sig.append(signature);
                                            int symFlags = 0;
                                            if (NameStyle.isPrivateName(signature)) {
                                                symFlags |= ScopeConstants.PRIVATE;
                                            }
                                            // TODO - look up deprecated etc.
                                            SymInfo fakeSym = new SymInfo(symFlags);

                                            int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                            if (isDeprecated(lines, lineno)) {
                                                flags |= IndexedElement.DEPRECATED;
                                            }


                                            appendFlags(sig, 'A', fakeSym, flags);
                                            classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                        }
                                    } else if (key.equals("class") || key.equals("exception")) { // NOI18N
                                        assert module != null;
                                        String cls = m.group(2);

                                        int paren = cls.indexOf('(');
                                        String constructor = null;
                                        if (paren != -1) {
                                            // Some documents specify a constructor here
                                            constructor = cleanupSignature(cls);
                                            cls = cls.substring(0, paren);
                                        }
                                        currentClass = cls;

                                        CachedIndexDocument classDocument = new CachedIndexDocument();
                                        classDocs.put(currentClass, classDocument);
                                        documents.add(classDocument);
                                        classDocument.addPair(FIELD_IN, module, true);

                                        if (key.equals("exception") && !"Exception".equals(cls)) { // NOI18N
                                            classDocument.addPair(FIELD_EXTENDS_NAME, "Exception", true); // NOI18N
                                        }

                                        classDocument.addPair(FIELD_CLASS_NAME, cls, true);
                                        int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY | IndexedElement.CONSTRUCTOR;
                                        if (isDeprecated(lines, lineno)) {
                                            flags |= IndexedElement.DEPRECATED;
                                        }
                                        if (flags != 0) {
                                            // TODO - store Documented, Deprecated, DocOnly, etc.
                                            classDocument.addPair(FIELD_CLASS_ATTR_NAME, IndexedElement.encode(flags), false);
                                        }
                                        classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, cls.toLowerCase(), true);

                                        // TODO - determine extends
                                        //document.addPair(FIELD_EXTENDS_NAME, superClass, true);

                                        if (constructor != null) {
                                            assert constructor.indexOf('(') != -1 && constructor.indexOf(')') != -1 &&
                                                    constructor.indexOf(')') > constructor.indexOf('(') : constructor;

                                            String signature = constructor;
                                            int lparen = signature.indexOf('(');
                                            int rparen = signature.indexOf(')', lparen + 1);
                                            if (lparen != -1 && rparen != -1) {
                                                //String methodName = signature.substring(0, lparen);
                                                String methodName = "__init__"; // The constructor is always __init__ !
                                                String args = signature.substring(lparen + 1, rparen);
                                                StringBuilder sig = new StringBuilder();
                                                sig.append(methodName);
                                                int symFlags = 0;
                                                if (NameStyle.isPrivateName(methodName)) {
                                                    symFlags |= ScopeConstants.PRIVATE;
                                                }
                                                // TODO - look up deprecated etc.
                                                SymInfo fakeSym = new SymInfo(symFlags);

                                                flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY | IndexedElement.CONSTRUCTOR;
                                                if (isDeprecated(lines, lineno)) {
                                                    flags |= IndexedElement.DEPRECATED;
                                                }

                                                appendFlags(sig, 'c', fakeSym, flags);
                                                sig.append(args);
                                                sig.append(';');

                                                classDocument.addPair(FIELD_MEMBER, sig.toString(), true);
                                            }

                                        }
                                    } else if (key.equals("function") || (key.equals("data") && m.group(2).contains("("))) { // NOI18N
                                        // constants.rst for example registers a data item for "quit" which is really a function

                                        String signature = m.group(2);
                                        indexRstFunction(signature, lines, lineno, document);

                                        // See if we have any additional lines with signatures
                                        for (int lookahead = lineno + 1; lookahead < maxLines; lookahead++) {
                                            String l = lines[lookahead];
                                            String trimmed = l.trim();
                                            if (trimmed.length() == 0 || trimmed.startsWith(":")) { // NOI18N
                                                break;
                                            }
                                            lineno++;

                                            indexRstFunction(trimmed, lines, lookahead, document);
                                        }

                                    } else if (key.equals("data")) { // NOI18N
                                        String data = m.group(2);

                                        StringBuilder sig = new StringBuilder();
                                        sig.append(data);
                                        int symFlags = 0;
                                        if (NameStyle.isPrivateName(data)) {
                                            symFlags |= ScopeConstants.PRIVATE;
                                        }
                                        // TODO - look up deprecated etc.
                                        SymInfo fakeSym = new SymInfo(symFlags);

                                        int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                                        if (isDeprecated(lines, lineno)) {
                                            flags |= IndexedElement.DEPRECATED;
                                        }

                                        appendFlags(sig, 'D', fakeSym, flags);

                                        document.addPair(FIELD_ITEM, sig.toString(), true);
                                    } else {
                                        // TODO Handle deprecated attribute!

                                        //    currentmodule::
                                        //    deprecated::
                                        //    doctest::
                                        //    envvar::
                                        //    epigraph::
                                        //    highlight::
                                        //    highlightlang::
                                        //    index::
                                        //    literalinclude::
                                        //    moduleauthor::
                                        //    note::
                                        //    opcode::
                                        //    productionlist::
                                        //    rubric::
                                        //    sectionauthor::
                                        //    seealso::
                                        //    testcode::
                                        //    testsetup::
                                        //    toctree::
                                        //    versionadded::
                                        //    versionchanged::
                                        //    warning::
                                    }
                                }
                            }
                        } else if (line.startsWith(".. _bltin-file-objects:") || line.startsWith(".. _string-methods:")) { // NOI18N
                            if (currentClass != null) {
                                currentClass = null;
                            }
                        }
                    }

                    for (String clz : classDocs.keySet()) {
                        StringBuilder sig = new StringBuilder();
                        sig.append(clz);
                        int symFlags = 0;
                        if (NameStyle.isPrivateName(clz)) {
                            symFlags |= ScopeConstants.PRIVATE;
                        }
                        // TODO - look up deprecated etc.
                        SymInfo fakeSym = new SymInfo(symFlags);
                        appendFlags(sig, 'C', fakeSym, IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY);

                        document.addPair(FIELD_ITEM, sig.toString(), true);
                    }

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                // Post processing: Add missing attributes not found in the .rst files
                // but introspected using dir() in a python console
                if (document != null) {
                    if ("operator".equals(module)) { // Fill in missing operators!
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__abs__", "__add__", "__and__", "__div__", "__floordiv__", "__index__", "__invert__", "__lshift__", "__mod__", "__mul__", "__neg__", "__or__", "__pos__", "__pow__", "__rshift__", "__sub__", "__truediv__", "__xor__" },
                                document, classDocs, "int", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__abs__", "__add__", "__and__", "__div__", "__floordiv__", "__index__", "__invert__", "__lshift__", "__mod__", "__mul__", "__neg__", "__or__", "__pos__", "__pow__", "__rshift__", "__sub__", "__truediv__", "__xor__" },
                                document, classDocs, "long", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__abs__", "__add__", "__div__", "__eq__", "__floordiv__", "__ge__", "__gt__", "__le__", "__lt__", "__mod__", "__mul__", "__ne__", "__neg__", "__pos__", "__pow__", "__sub__", "__truediv__" },
                                document, classDocs, "float", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__abs__", "__add__", "__div__", "__eq__", "__floordiv__", "__ge__", "__gt__", "__le__", "__lt__", "__mod__", "__mul__", "__ne__", "__neg__", "__pos__", "__pow__", "__sub__", "__truediv__" },
                                document, classDocs, "complex", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__abs__", "__add__", "__and__", "__div__", "__floordiv__", "__index__", "__invert__", "__lshift__", "__mod__", "__mul__", "__neg__", "__or__", "__pos__", "__pow__", "__rshift__", "__sub__", "__truediv__", "__xor__" },
                                document, classDocs, "bool", documents, module, false, true);

                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__add__", "__contains__", "__eq__", "__ge__", "__getitem__", "__getslice__", "__gt__", "__le__", "__lt__", "__mod__", "__mul__", "__ne__", "index" },
                                document, classDocs, "str", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__add__", "__contains__", "__delitem__", "__delslice__", "__eq__", "__ge__", "__getitem__", "__getslice__", "__gt__", "__iadd__", "__imul__", "__le__", "__lt__", "__mul__", "__ne__", "__setitem__", "__setslice__", "index" },
                                document, classDocs, "list", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__contains__", "__delitem__", "__eq__", "__ge__", "__getitem__", "__gt__", "__le__", "__lt__", "__ne__", "__setitem__" },
                                document, classDocs, "dict", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__add__", "__contains__", "__eq__", "__ge__", "__getitem__", "__getslice__", "__gt__", "__le__", "__lt__", "__mul__", "__ne__", "index" },
                                document, classDocs, "tuple", documents, module, false, true);
                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
                                new String[] { "__add__", "__contains__", "__eq__", "__ge__", "__getitem__", "__getslice__", "__gt__", "__le__", "__lt__", "__mod__", "__mul__", "__ne__", "index" },
                                document, classDocs, "unicode", documents, module, false, true);
//                    } else if ("stdtypes".equals(module)) {
//                        // Found no definitions for these puppies
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__cmp__", "__coerce__", "__delattr__", "__divmod__", "__doc__", "__float__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__init__", "__int__", "__long__", "__new__", "__nonzero__", "__oct__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "__trunc__", "conjugate", "denominator", "imag", "numerator", "real" },
//                                document, classDocs, "int", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__cmp__", "__coerce__", "__delattr__", "__divmod__", "__doc__", "__float__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__init__", "__int__", "__long__", "__new__", "__nonzero__", "__oct__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "__trunc__", "conjugate", "denominator", "imag", "numerator", "real" },
//                                document, classDocs, "long", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__coerce__", "__delattr__", "__divmod__", "__doc__", "__float__", "__format__", "__getattribute__", "__getformat__", "__getnewargs__", "__hash__", "__init__", "__int__", "__long__", "__new__", "__nonzero__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rmod__", "__rmul__", "__rpow__", "__rsub__", "__rtruediv__", "__setattr__", "__setformat__", "__sizeof__", "__str__", "__subclasshook__", "__trunc__", "conjugate", "imag", "is_integer", "real" },
//                                document, classDocs, "float", documents, module, true, false);
//
//
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__coerce__", "__delattr__", "__divmod__", "__doc__", "__float__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__init__", "__int__", "__long__", "__new__", "__nonzero__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rmod__", "__rmul__", "__rpow__", "__rsub__", "__rtruediv__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "conjugate", "imag", "real" },
//                                document, classDocs, "complex", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__cmp__", "__coerce__", "__delattr__", "__divmod__", "__doc__", "__float__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__hex__", "__init__", "__int__", "__long__", "__new__", "__nonzero__", "__oct__", "__radd__", "__rand__", "__rdiv__", "__rdivmod__", "__reduce__", "__reduce_ex__", "__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__", "__ror__", "__rpow__", "__rrshift__", "__rsub__", "__rtruediv__", "__rxor__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "__trunc__", "conjugate", "denominator", "imag", "numerator", "real" },
//                                document, classDocs, "bool", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__delattr__", "__doc__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__init__", "__len__", "__new__", "__repr__", "__rmod__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "_formatter_field_name_split", "_formatter_parser" },
//                                document, classDocs, "str", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__delattr__", "__doc__", "__format__", "__getattribute__", "__hash__", "__init__", "__iter__", "__len__", "__new__", "__repr__", "__reversed__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "append", "count", "extend", "insert", "pop", "remove", "reverse", "sort" },
//                                document, classDocs, "list", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__cmp__", "__delattr__", "__doc__", "__format__", "__getattribute__", "__hash__", "__iter__", "__len__", "__new__", "__repr__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__" },
//                                document, classDocs, "dict", documents, module, true, false);
//
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__delattr__", "__doc__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__init__", "__iter__", "__len__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "count" },
//                                document, classDocs, "tuple", documents, module, true, false);
//                        addMissing(PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MEMBER,
//                              new String[] { "__class__", "__delattr__", "__doc__", "__format__", "__getattribute__", "__getnewargs__", "__hash__", "__init__", "__len__", "__new__", "__reduce__", "__reduce_ex__", "__repr__", "__rmod__", "__rmul__", "__setattr__", "__sizeof__", "__str__", "__subclasshook__", "_formatter_field_name_split", "_formatter_parser", "capitalize", "center", "count", "decode", "encode", "endswith", "expandtabs", "find", "format", "isalnum", "isalpha", "isdecimal", "isdigit", "islower", "isnumeric", "isspace", "istitle", "isupper", "join", "ljust", "lower", "lstrip", "partition", "replace", "rfind", "rindex", "rjust", "rpartition", "rsplit", "rstrip", "split", "splitlines", "startswith", "strip", "swapcase", "title", "translate", "upper", "zfill" },
//                                document, classDocs, "unicode", documents, module, true, false);
//
                    }
                }

                // And convert to a proper GSF search document. I didn't do this directly
                // because I want to modify the documents after adding documents and pairs.
                for (CachedIndexDocument cid : documents) {
                    List<CachedIndexDocumentEntry> entries = cid.entries;
                    IndexDocument indexedDoc = support.createDocument(indexable);
//                    IndexDocument indexedDoc = support.createDocument(entries.size(), overrideUrl);
                    docs.add(indexedDoc);
                    for (CachedIndexDocumentEntry entry : entries) {
                        indexedDoc.addPair(entry.key, entry.value, true, true); // XXX indexable and stored ???
                    }
                }
            }
        }

        return docs;
    }

    /** Add the given list of names, found in the given document with a given key, and add it
     * to the specified class (possibly found in the classDocs list - if not, add one to the
     * documents list)
     */
    private void addMissing(String key, String newKey, String[] names, CachedIndexDocument doc,
            Map<String, CachedIndexDocument> classDocs, String clz, List<CachedIndexDocument> documents, String module,
            boolean addUnknown, boolean search) {

        CachedIndexDocument classDocument = classDocs.get(clz);
        if (classDocument == null) {
            // New class without class:: declaration first.
            classDocument = new CachedIndexDocument();
            documents.add(classDocument);
            classDocs.put(clz, classDocument);
            classDocument.addPair(FIELD_IN, module, true);

            classDocument.addPair(FIELD_CLASS_NAME, clz, true);
            classDocument.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, clz.toLowerCase(), true);
        }

        assert classDocument != doc;

        List<String> namesFound = new ArrayList<>();
        List<String> namesMissing = new ArrayList<>();
        boolean noneFound = true;

        // Look for each of the given functions
        Search:
        for (String name : names) {
            boolean found = false;
            if (search) {
                int nameLength = name.length();

                // DEBUGGING: Look to make sure I don't already have it in the class doc!
                for (CachedIndexDocumentEntry entry : classDocument.entries) {
                    if (newKey.equals(entry.key)) {
                        if (entry.value.startsWith(name) &&
                                (entry.value.length() <= nameLength || entry.value.charAt(nameLength) == ';')) {
                             // Uh oh - what do I do here?
                            System.err.println("WARNING: I already have a definition for name " + name + " in class " + clz);
                            continue Search;
                        }
                    }
                }

                for (CachedIndexDocumentEntry entry : doc.entries) {
                    if (key.equals(entry.key)) {
                        if (entry.value.startsWith(name) &&
                                (entry.value.length() <= nameLength || entry.value.charAt(nameLength) == ';')) {
                            // Found it!
                            classDocument.addPair(newKey, entry.value, entry.index);
                            found = true;
                            namesFound.add(name);
                            break;
                        }
                    }
                }
            }

            if (!found) {
                if (addUnknown) {
                    // TODO - see if I can find a way to extract the signature too!
                    String args = "";
                    String signature = name + "()"; //

                    assert signature.indexOf('(') != -1 && signature.indexOf(')') != -1 &&
                            signature.indexOf(')') > signature.indexOf('(') : signature;
                    char type;
                    if (name.equals("__init__")) { // NOI18N
                        type = 'c';
                    } else {
                        type = 'F';
                    }
                    StringBuilder sig = new StringBuilder();
                    sig.append(name);

                    int symFlags = 0;
                    if (NameStyle.isPrivateName(name)) {
                        symFlags |= ScopeConstants.PRIVATE;
                    }
                    // TODO - look up deprecated etc.
                    SymInfo fakeSym = new SymInfo(symFlags);

                    int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
                    appendFlags(sig, type, fakeSym, flags);
                    sig.append(args);
                    sig.append(';');

                    classDocument.addPair(newKey, sig.toString(), true);
                } else {
                    namesMissing.add(name);
                }
            } else {
                noneFound = false;
            }
        }

        if (PREINDEXING) {
            if (namesFound.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("FOUND for ");
                sb.append(clz);
                sb.append(" in ");
                sb.append(module);
                sb.append(": ");
                appendList(sb, namesFound);
                System.err.println(sb.toString());
            }

            if (noneFound && search) {
                System.err.println("ERROR: NONE of the passed in names for " + clz + " were found!");
            }

            if (namesMissing.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("WARNING: Missing these names from ");
                sb.append(module);
                sb.append(" for use by class ");
                sb.append(clz);
                sb.append(" : ");
                appendList(sb, namesMissing);
                System.err.println(sb.toString());
            }
        }
    }

    private static void appendList(StringBuilder sb, List<String> list) {
        sb.append("{ ");
        boolean first = true;
        for (String m : list) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append('"');
            sb.append(m);
            sb.append('"');
        }
        sb.append(" }");
    }

    private void indexRstFunction(String signature, String[] lines, int lineno, CachedIndexDocument document) {
        int dot = signature.indexOf('.');
        if (dot != -1) {
            int paren = signature.indexOf('(');
            if (paren == -1 || paren > dot) {
                assert signature.matches("\\w+\\.\\w+.*") : signature; // NOI18N
                signature = signature.substring(dot + 1);
            }
        }
        signature = cleanupSignature(signature);
        if (signature.indexOf('(') == -1) {
            signature = signature + "()"; // NOI18N
        } else if (signature.indexOf(')') == -1) {
            //signature = signature + ")";
            assert signature.indexOf(')') != -1;
        }
        int lparen = signature.indexOf('(');
        int rparen = signature.indexOf(')', lparen + 1);
        if (lparen != -1 && rparen != -1) {
            String methodName = signature.substring(0, lparen);
            String args = signature.substring(lparen + 1, rparen);
            StringBuilder sig = new StringBuilder();
            sig.append(methodName);
            int symFlags = 0;
            if (NameStyle.isPrivateName(methodName)) {
                symFlags |= ScopeConstants.PRIVATE;
            }
            // TODO - look up deprecated etc.
            SymInfo fakeSym = new SymInfo(symFlags);
            int flags = IndexedElement.DOCUMENTED | IndexedElement.DOC_ONLY;
            if (isDeprecated(lines, lineno)) {
                flags |= IndexedElement.DEPRECATED;
            }
            appendFlags(sig, 'F', fakeSym, flags);
            sig.append(args);
            sig.append(';');

            document.addPair(FIELD_ITEM, sig.toString(), true);
        }
    }

    private List<IndexDocument> scanEgg(FileObject fo, Indexable indexable, ParserResult result, IndexingSupport support) {
        List<IndexDocument> documents = new ArrayList<>();

        if (fo == null) {
            return documents;
        }

        try {
            String s = fo.toURL().toExternalForm() + "!"; // NOI18N
            URL u = new URL("jar:" + s); // NOI18N
            FileObject root = URLMapper.findFileObject(u);
            String rootUrl = PythonIndex.getPreindexUrl(u.toExternalForm());
            indexScriptDocRecursively(support, documents, root, rootUrl);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

        return documents;
    }

    /**
     * Method which recursively indexes directory trees, such as the yui/ folder
     * for example
     */
    private void indexScriptDocRecursively(IndexingSupport support, List<IndexDocument> documents, final FileObject fo, String url) {
        if (fo.isFolder()) {
            for (FileObject c : fo.getChildren()) {
                indexScriptDocRecursively(support, documents, c, url + "/" + c.getNameExt()); // NOI18N
            }
            return;
        }

        String ext = fo.getExt();

//        if ("py".equals(ext)) { // NOI18N
//            DefaultParseListener listener = new DefaultParseListener();
//            List<ParserFile> files = Collections.<ParserFile>singletonList(new DefaultParserFile(fo, null, false));
//            SourceFileReader reader = new SourceFileReader() {
//                public CharSequence read(ParserFile file) throws IOException {
//                    BaseDocument doc = GsfUtilities.getDocument(fo, true);
//                    if (doc != null) {
//                        try {
//                            return doc.getText(0, doc.getLength());
//                        } catch (BadLocationException ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//
//                    return "";
//                }
//
//                public int getCaretOffset(ParserFile file) {
//                    return -1;
//                }
//            };
//            Job job = new Job(files, listener, reader, null);
//            new PythonParser().parseFiles(job);
//            ParserResult parserResult = listener.getParserResult();
//            if (parserResult != null && parserResult.isValid()) {
//                documents.addAll(new IndexTask((PythonParserResult)parserResult, support, url).scan());
//            }
//        } else if ("rst".equals(ext)) { // NOI18N
//            documents.addAll(scanRst(fo, support, url));
//        }
    }

    private FileObject getLibDir() {
        // TODO - fetch from projects!!!!
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
        if (platform != null) {
            String cmd = platform.getInterpreterCommand();
            File file = new File(cmd);
            if (file.exists()) {
                file = file.getAbsoluteFile();
                File home = file.getParentFile().getParentFile();
                if (home != null) {
                    // Look for Lib - Jython style
                    File lib = new File(home, "Lib"); // NOI18N
                    boolean exists = lib.exists();
                    if (!exists) { // Unix style
                        lib = new File(home, "lib" + File.separator + "python"); // NOI18N
                        exists = lib.exists();
                    }
                    if (exists) {
                        return FileUtil.toFileObject(lib);
                    }
                }
            }
        }

        return null;
    }
}
