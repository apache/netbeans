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

package org.netbeans.modules.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.LazyTreeLoader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Log.DiscardDiagnosticHandler;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.indexing.JavaBinaryIndexer;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.OutputFileManager.InvalidSourcePath;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class TreeLoader extends LazyTreeLoader {

    private static final String OPTION_OUTPUT_ROOT = "output-root"; //NOI18N
    private static final Pattern ctor_summary_name = Pattern.compile("constructor[_.]summary"); //NOI18N
    private static final Pattern method_summary_name = Pattern.compile("method[_.]summary"); //NOI18N
    private static final Pattern field_detail_name = Pattern.compile("field[_.]detail"); //NOI18N
    private static final Pattern ctor_detail_name = Pattern.compile("constructor[_.]detail"); //NOI18N
    private static final Pattern method_detail_name = Pattern.compile("method[_.]detail"); //NOI18N
    private static final ThreadLocal<Boolean> isTreeLoading = new ThreadLocal<Boolean>();

    public static void preRegister(final Context context, final ClasspathInfo cpInfo, final boolean detached) {
        context.put(lazyTreeLoaderKey, new TreeLoader(context, cpInfo, detached));
    }
    
    public static TreeLoader instance (final Context ctx) {
        final LazyTreeLoader tl = LazyTreeLoader.instance(ctx);
        return (tl instanceof TreeLoader) ? (TreeLoader)tl : null;
    }

    public static boolean isTreeLoading() {
        return isTreeLoading.get() == Boolean.TRUE;
    }

    private static final Logger LOGGER = Logger.getLogger(TreeLoader.class.getName());
    private static final boolean ALWAYS_ALLOW_JDOC_ARG_NAMES = Boolean.getBoolean("java.source.args.from.http.jdoc");  //NOI18N
    public  static boolean DISABLE_CONFINEMENT_TEST = false; //Only for tests!
    public  static boolean DISABLE_ARTIFICAL_PARAMETER_NAMES = false; //Only for tests!

    private Context context;
    private ClasspathInfo cpInfo;
    private Map<ClassSymbol, StringBuilder> couplingErrors;
    private boolean partialReparse;
    //@GuardedBy("this")
    private Thread owner;
    //@GuardedBy("this")
    private int depth;
    private final boolean checkContention;

    private TreeLoader(Context context, ClasspathInfo cpInfo, boolean detached) {
        this.context = context;
        this.cpInfo = cpInfo;
        this.checkContention = detached;
    }
    
    @Override
    public boolean loadTreeFor(final ClassSymbol clazz, boolean persist) {
        boolean contended = !attachTo(Thread.currentThread());
        try {
            assert DISABLE_CONFINEMENT_TEST || JavaSourceAccessor.getINSTANCE().isJavaCompilerLocked() || !contended;
            if (clazz != null) {
                if (Enter.instance(context).getEnv(clazz) != null) {
                    return true;
                }
                try {
                    FileObject fo = SourceUtils.getFile(clazz, cpInfo);
                    final JavacTaskImpl jti = (JavacTaskImpl) context.get(JavacTask.class);
                    JavaCompiler jc = JavaCompiler.instance(context);
                    if (fo != null && jti != null) {
                        final Log log = Log.instance(context);
                        log.nerrors = 0;
                        final JavaFileManager jfm = context.get(JavaFileManager.class);
                        final Symtab syms = Symtab.instance(context);
                        JavaFileObject jfo = FileObjects.sourceFileObject(fo, null);
                        Map<ClassSymbol, StringBuilder> oldCouplingErrors = couplingErrors;
                        boolean oldSkipAPT = jc.skipAnnotationProcessing;
                        try {
                            couplingErrors = new HashMap<ClassSymbol, StringBuilder>();
                            jc.skipAnnotationProcessing = true;
                            Iterable<? extends CompilationUnitTree> cuts = jti.parse(jfo);
                            for (CompilationUnitTree cut : cuts) {
                                ((JCTree.JCCompilationUnit)cut).modle = clazz.packge().modle;
                            }
                            jti.analyze(jti.enter(cuts));
                            if (persist && log.nerrors == 0) {
                                final File classFolder = getClassFolder(jfm, clazz);
                                if (classFolder != null) {
                                    jfm.handleOption(OPTION_OUTPUT_ROOT, Collections.singletonList(classFolder.getPath()).iterator()); //NOI18N
                                    try {
                                        if (jfm.hasLocation(StandardLocation.CLASS_OUTPUT) && canWrite(cpInfo)) {
                                            Env<AttrContext> env = Enter.instance(context).getEnv(clazz);
                                            HashMap<ClassSymbol, JCClassDecl> syms2trees;
                                            if (env != null && pruneTree(env.tree, Symtab.instance(context), syms2trees = new HashMap<>())) {
                                                isTreeLoading.set(Boolean.TRUE);
                                                try {
                                                    dumpSymFile(jti, clazz, syms2trees);
                                                } finally {
                                                    isTreeLoading.remove();
                                                }
                                            }
                                        } else {
                                            final JavaFileObject cfo = clazz.classfile;
                                            final FileObject cFileObject = URLMapper.findFileObject(cfo.toUri().toURL());
                                            FileObject root = null;
                                            if (cFileObject != null) {
                                                root = cFileObject;
                                                for (String pathElement : ElementUtilities.getBinaryName(clazz).split("\\.")) {
                                                    root = root.getParent();
                                                }
                                            }
                                            if (root != null) {
                                                final FileObject rootFin = root;
                                                IndexingUtils.runAsScanWork(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            JavaBinaryIndexer.preBuildArgs(rootFin,cFileObject);
                                                        } catch (IOException ioe) {
                                                            Exceptions.printStackTrace(ioe);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    } finally {
                                        jfm.handleOption(OPTION_OUTPUT_ROOT, Collections.singletonList("").iterator()); //NOI18N
                                    }
                                }
                            }
                            return true;
                        } finally {
                            jc.skipAnnotationProcessing = oldSkipAPT;
                            log.nerrors = 0;
                            for (Map.Entry<ClassSymbol, StringBuilder> e : couplingErrors.entrySet()) {
                                dumpCouplingAbort(new CouplingAbort(e.getKey(), null), e.getValue().toString());
                            }
                            couplingErrors = oldCouplingErrors;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return false;
        } finally {
            dettachFrom(Thread.currentThread());
        }
    }
    
    @Override
    public boolean loadParamNames(ClassSymbol clazz) {
        boolean contended = !attachTo(Thread.currentThread());
        try {
            assert DISABLE_CONFINEMENT_TEST || JavaSourceAccessor.getINSTANCE().isJavaCompilerLocked() || !contended;
            if (clazz != null) {
                JavadocHelper.TextStream page = JavadocHelper.getJavadoc(clazz, ALWAYS_ALLOW_JDOC_ARG_NAMES, null);
                if (page != null && (!page.isRemote() || !EventQueue.isDispatchThread())) {
                    if (getParamNamesFromJavadocText(page, clazz)) {
                        return true;
                    }
                }
                if (!DISABLE_ARTIFICAL_PARAMETER_NAMES) {
                    fillArtificalParamNames(clazz);
                    return true;
                }
            }

            return false;
        } finally {
            dettachFrom(Thread.currentThread());
        }
    }

    @Override
    public void couplingError(ClassSymbol clazz, Tree t) {
        if (this.partialReparse) {
            super.couplingError(clazz, t);
        }
        if (clazz != null && couplingErrors != null) {
            StringBuilder sb = couplingErrors.get(clazz);            
            if (sb != null)
                sb.append(getTreeInfo(t));
            else
                couplingErrors.put(clazz, getTreeInfo(t));
        } else {
            dumpCouplingAbort(new CouplingAbort(clazz, t), null);
        }
    }

    @Override
    public void updateContext(Context context) {
        this.context = context;
    }

    public final void startPartialReparse () {
        this.partialReparse = true;
    }

    public final void endPartialReparse () {
        this.partialReparse = false;
    }

    public static boolean pruneTree(final JCTree tree, final Symtab syms, final HashMap<ClassSymbol, JCClassDecl> syms2trees) {
        final AtomicBoolean ret = new AtomicBoolean(true);
        new TreeScanner() {
            @Override
            public void visitMethodDef(JCMethodDecl tree) {
                super.visitMethodDef(tree);
                if (tree.sym == null || tree.type == null || tree.type == syms.unknownType)
                    ret.set(false);
                tree.body = null;
            }
            @Override
            public void visitVarDef(JCVariableDecl tree) {
                super.visitVarDef(tree);
                if (tree.sym == null || tree.type == null || tree.type == syms.unknownType)
                    ret.set(false);
                tree.init = null;
            }
            @Override
            public void visitClassDef(JCClassDecl tree) {
                scan(tree.mods);
                scan(tree.typarams);
                scan(tree.extending);
                scan(tree.implementing);
                if (tree.defs != null) {
                    List<JCTree> prev = null;
                    for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                        scan(l.head);
                        if (l.head.getTag() == JCTree.Tag.BLOCK) {
                            if (prev != null)
                                prev.tail = l.tail;
                            else
                                tree.defs = l.tail;
                        }
                        prev = l;
                    }
                }
                if (tree.sym == null || tree.type == null || tree.type == syms.unknownType) {
                    ret.set(false);
                } else if (syms2trees != null) {
                    syms2trees.put(tree.sym, tree);
                }
            }
            @Override
            public void visitModuleDef(JCTree.JCModuleDecl tree) {
                ret.set(false);
                super.visitModuleDef(tree);
            }
        }.scan(tree);
        return ret.get();
    }

    public static void dumpSymFile(
            @NonNull final JavaFileManager jfm,
            @NonNull final JavacTaskImpl jti,
            @NonNull final ClassSymbol clazz,
            @NonNull final File classFolder,
            @NonNull final HashMap<ClassSymbol, JCClassDecl> syms2trees) throws IOException {
        jfm.handleOption(OPTION_OUTPUT_ROOT, Collections.singletonList(classFolder.getPath()).iterator()); //NOI18N
        try {
            dumpSymFile(jti, clazz, syms2trees);
        } finally {
            jfm.handleOption(OPTION_OUTPUT_ROOT, Collections.singletonList("").iterator()); //NOI18N
        }
    }

    @CheckForNull
    private static File getClassFolder(
        JavaFileManager jfm,
        ClassSymbol clazz) throws IOException {
        String binaryName = null;
        String surl = null;
        if (clazz.classfile != null) {
            binaryName = jfm.inferBinaryName(StandardLocation.PLATFORM_CLASS_PATH, clazz.classfile);
            if (binaryName == null)
                binaryName = jfm.inferBinaryName(StandardLocation.CLASS_PATH, clazz.classfile);
            surl = clazz.classfile.toUri().toURL().toExternalForm();
        } else if (clazz.sourcefile != null) {
            binaryName = jfm.inferBinaryName(StandardLocation.SOURCE_PATH, clazz.sourcefile);
            surl = clazz.sourcefile.toUri().toURL().toExternalForm();
        }
        if (binaryName == null || surl == null) {
            return null;
        }
        int index = surl.lastIndexOf(FileObjects.convertPackage2Folder(binaryName));
        if (index > 0) {
            return JavaIndex.getClassFolder(new URL(surl.substring(0, index)));
        } else {
            LOGGER.log(
               Level.INFO,
               "Invalid binary name when writing sym file for class: {0}, source: {1}, binary name {2}",    // NOI18N
               new Object[] {
                   clazz.flatname,
                   surl,
                   binaryName
               });
            return null;
        }
    }

    private static void dumpSymFile(
            @NonNull final JavacTaskImpl jti,
            @NonNull final ClassSymbol clazz,
            @NonNull final HashMap<ClassSymbol, JCClassDecl> syms2trees) throws IOException {
        Log log = Log.instance(jti.getContext());
        JavaCompiler compiler = JavaCompiler.instance(jti.getContext());
        JavaFileObject prevLogTo = log.useSource(null);
        DiscardDiagnosticHandler discardDiagnosticHandler = new Log.DiscardDiagnosticHandler(log);
        final TaskListener listener = new TaskListener() {
            @Override
            public void started(TaskEvent e) {
                if (e != null && e.getKind() == TaskEvent.Kind.GENERATE) {
                    JCClassDecl tree = syms2trees.get((ClassSymbol)e.getTypeElement());
                    if (tree != null)
                        pruneTree(tree, Symtab.instance(jti.getContext()), null);
                }
            }
            @Override
            public void finished(TaskEvent e) {
            }
        };
        boolean oldSkipAP = compiler.skipAnnotationProcessing;
        try {
            compiler.skipAnnotationProcessing = true;
            jti.addTaskListener(listener);
            jti.generate(Collections.singletonList(clazz));
        } catch (InvalidSourcePath isp) {
            LOGGER.log(Level.INFO, "InvalidSourcePath reported when writing sym file for class: {0}", clazz.flatname); // NOI18N
        } finally {
            jti.removeTaskListener(listener);
            compiler.skipAnnotationProcessing = oldSkipAP;
            log.popDiagnosticHandler(discardDiagnosticHandler);
            log.useSource(prevLogTo);
        }
    }

    private static final int MAX_DUMPS = Integer.getInteger("org.netbeans.modules.java.source.parsing.JavacParser.maxDumps", 255);
    
    public static void dumpCouplingAbort(CouplingAbort couplingAbort, String treeInfo) {
        if (treeInfo == null)
            treeInfo = getTreeInfo(couplingAbort.getTree()).toString();
        String dumpDir = System.getProperty("netbeans.user") + "/var/log/"; //NOI18N
        JavaFileObject classFile = couplingAbort.getClassFile();
        String cfURI = classFile != null ? classFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        JavaFileObject sourceFile = couplingAbort.getSourceFile();
        String sfURI = sourceFile != null ? sourceFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        String origName = classFile != null ? classFile.getName() : "unknown"; //NOI18N
        File f = new File(dumpDir + origName + ".dump"); // NOI18N
        boolean dumpSucceeded = false;
        int i = 1;
        while (i < MAX_DUMPS) {
            if (!f.exists())
                break;
            f = new File(dumpDir + origName + '_' + i + ".dump"); // NOI18N
            i++;
        }
        if (!f.exists()) {
            try {
                f.getParentFile().mkdirs();
                OutputStream os = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
                try {
                    writer.println("Coupling error:"); //NOI18N
                    writer.println(String.format("class file %s", cfURI)); //NOI18N
                    writer.println(String.format("source file %s", sfURI)); //NOI18N
                    writer.println("----- Source file content: ----------------------------------------"); // NOI18N
                    if (sourceFile != null) {
                        try {
                            writer.println(sourceFile.getCharContent(true));
                        } catch (UnsupportedOperationException uoe) {
                            writer.println("<unknown>"); //NOI18N
                        }
                    } else {
                        writer.println("<unknown>"); //NOI18N
                    }
                    writer.print("----- Trees: -------------------------------------------------------"); // NOI18N
                    writer.println(treeInfo);
                    writer.println("----- Stack trace: ---------------------------------------------"); // NOI18N
                    couplingAbort.printStackTrace(writer);
                } finally {
                    writer.close();
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing coupling error dump file!", ioe); // NOI18N
            }
        }
        LOGGER.log(Level.WARNING, "Coupling error:\nclass file: {0}\nsource file: {1}{2}\n", new Object[] {cfURI, sfURI, treeInfo}); //NOI18N
        if (!dumpSucceeded) {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }

    private static StringBuilder getTreeInfo(Tree t) {
        StringBuilder info = new StringBuilder("\n"); //NOI18N
        if (t != null) {
            switch (t.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    info.append("CLASS: ").append(((ClassTree) t).getSimpleName().toString()); //NOI18N
                    break;
                case VARIABLE:
                    info.append("VARIABLE: ").append(((VariableTree) t).getName().toString()); //NOI18N
                    break;
                case METHOD:
                    info.append("METHOD: ").append(((MethodTree) t).getName().toString()); //NOI18N
                    break;
                case TYPE_PARAMETER:
                    info.append("TYPE_PARAMETER: ").append(((TypeParameterTree) t).getName().toString()); //NOI18N
                    break;
                default:
                    info.append("TREE: <unknown>"); //NOI18N
                    break;
            }
        }
        return info;
    }

    private boolean getParamNamesFromJavadocText(final JavadocHelper.TextStream page, final ClassSymbol clazz) {
        HTMLEditorKit.Parser parser;
        InputStream is = null;        
        String charset = null;
        for (;;) {
            try{
                is = page.openStream();
                Reader reader = charset == null ? new InputStreamReader(is): new InputStreamReader(is, charset);
                parser = new ParserDelegator();
                parser.parse(reader, new ParserCallback() {                    

                    private int state = 0; //init
                    private String signature = null;
                    private StringBuilder sb = null;

                    @Override
                    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.A) {
                            String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                            if (attrName != null && ctor_summary_name.matcher(attrName).matches()) {
                                // we have found desired javadoc constructor info anchor
                                state = 10; //ctos open
                            } else if (attrName != null && method_summary_name.matcher(attrName).matches()) {
                                // we have found desired javadoc method info anchor
                                state = 20; //methods open
                            } else if (attrName != null && field_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (attrName != null && ctor_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (attrName != null && method_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (state == 12 || state == 22) {
                                String attrHref = (String)a.getAttribute(HTML.Attribute.HREF);
                                if (attrHref != null) {
                                    int idx = attrHref.indexOf('#');
                                    if (idx >= 0) {
                                        signature = attrHref.substring(idx + 1);
                                        sb = new StringBuilder();
                                    }
                                }
                            }
                        } else if (t == HTML.Tag.TABLE) {
                            if (state == 10 || state == 20)
                                state++;
                        } else if (t == HTML.Tag.CODE) {
                            if (state == 11 || state == 21)
                                state++;
                        } else if (t == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.CLASS, "block")) { //NOI18N
                            if (state == 11 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), true);
                                signature = null;
                                sb = null;
                            } else if (state == 21 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), false);
                                signature = null;
                                sb = null;
                            }
                        }
                    }

                    @Override
                    public void handleEndTag(Tag t, int pos) {
                        if (t == HTML.Tag.CODE) {
                            if (state == 12 || state == 22)
                                state--;
                        } else if (t == HTML.Tag.TABLE) {
                            if (state == 11 || state == 21)
                                state--;
                        }
                    }

                    @Override
                    public void handleText(char[] data, int pos) {
                        if (signature != null && sb != null && (state == 12 || state == 22))
                            sb.append(data);
                    }

                    @Override
                    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.BR) {
                            if (state == 11 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), true);
                                signature = null;
                                sb = null;
                            } else if (state == 21 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), false);
                                signature = null;
                                sb = null;
                            }
                        }
                    }

                    private void setParamNames(String signature, String names, boolean isCtor) {
                        ArrayList<String> paramTypes = new ArrayList<String>();
                        int idx = -1;
                        for(int i = 0; i < signature.length(); i++) {
                            switch(signature.charAt(i)) {
                                case '-':
                                case '(':
                                case ')':
                                case ',':
                                    if (idx > -1 && idx < i - 1) {
                                        String typeName = signature.substring(idx + 1, i).trim();
                                        if (typeName.endsWith("...")) //NOI18N
                                            typeName = typeName.substring(0, typeName.length() - 3) + "[]"; //NOI18N
                                        paramTypes.add(typeName);
                                    }
                                    idx = i;
                                    break;
                            }
                        }
                        String methodName = null;
                        ArrayList<String> paramNames = new ArrayList<String>();
                        idx = -1;
                        for(int i = 0; i < names.length(); i++) {
                            switch(names.charAt(i)) {
                                case '(':
                                    methodName = names.substring(0, i);
                                    break;
                                case ')':
                                case ',':
                                    if (idx > -1) {
                                        paramNames.add(names.substring(idx + 1, i));
                                        idx = -1;
                                    }
                                    break;
                                case 160: //&nbsp;
                                    idx = i;
                                    break;
                            }
                        }
                        assert methodName != null : "Null methodName. Signature: [" + signature + "], Names: [" + names + "]";
                        assert paramTypes.size() == paramNames.size() : "Inconsistent param types/names. Signature: [" + signature + "], Names: [" + names + "]";
                        if (paramNames.size() > 0) {
                            for (Symbol s : clazz.members().getSymbolsByName(isCtor
                                    ? clazz.name.table.names.init
                                    : clazz.name.table.fromString(methodName))) {
                                if (s.kind == Kinds.Kind.MTH && s.owner == clazz) {
                                    MethodSymbol sym = (MethodSymbol)s;
                                    List<VarSymbol> params = sym.params;
                                    if (checkParamTypes(params, paramTypes)) {
                                        for (String name : paramNames) {
                                            params.head.setName(clazz.name.table.fromString(name));
                                            params = params.tail;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    private boolean checkParamTypes(List<VarSymbol> params, ArrayList<String> paramTypes) {
                        Types types = Types.instance(context);
                        for (String typeName : paramTypes) {
                            if (params.isEmpty())
                                return false;
                            Type type = params.head.type;
                            if (type.isParameterized())
                                type = types.erasure(type);
                            if (!typeName.equals(type.toString()))
                                return false;
                            params = params.tail;
                        }
                        return params.isEmpty();
                    }
                }, charset != null);
                return true;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = JavadocHelper.getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch (InterruptedIOException x) {
                //Http javadoc timeout
                break;
            } catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
    
    private void fillArtificalParamNames(final ClassSymbol clazz) {
        for (Symbol s : clazz.getEnclosedElements()) {
            if (s instanceof MethodSymbol) {
                MethodSymbol ms = (MethodSymbol) s;

                if (ms.getParameters().isEmpty()) {
                    continue;
                }
                
                Set<String> usedNames = new HashSet<String>();
                
                for (VarSymbol vs : ms.getParameters()) {
                    String name = JavaSourceAccessor.getINSTANCE().generateReadableParameterName(vs.asType().toString(), usedNames);

                    vs.setName(clazz.name.table.fromString(name));
                }
            }
        }
    }

    private synchronized boolean attachTo(final Thread thread) {
        assert thread != null;
        if (!checkContention) {
            return true;
        } else if (this.owner == null) {
            assert this.depth == 0;
            this.owner = thread;
            this.depth++;
            return true;
        } else if (this.owner == thread) {
            assert this.depth > 0;
            this.depth++;
            return true;
        } else {
            assert this.depth > 0;
            return false;
        }
    }

    private synchronized boolean dettachFrom(final Thread thread) {
        assert thread != null;
        if (!checkContention) {
            return true;
        } else if (this.owner == thread) {
            assert depth > 0;
            this.depth--;
            if (this.depth == 0) {
                this.owner = null;
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean canWrite(final ClasspathInfo cpInfo) {
        final FileManagerTransaction fmTx = ClasspathInfoAccessor.getINSTANCE().getFileManagerTransaction(cpInfo);
        assert fmTx != null;
        return fmTx.canWrite();
    }
    
}
