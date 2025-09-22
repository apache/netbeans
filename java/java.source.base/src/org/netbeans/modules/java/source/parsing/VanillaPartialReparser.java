/**
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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Flow;
import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.lib.nbjavac.services.CancelService;
import org.netbeans.lib.nbjavac.services.NBLog;
import org.netbeans.lib.nbjavac.services.NBParserFactory;
import org.netbeans.modules.java.source.CompilationInfoAccessor;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.JavacParser.PartialReparser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service = PartialReparser.class, position = 200)
public class VanillaPartialReparser implements PartialReparser {

    // level FINE and lower enables partial reparse verification and source dumps
    private static final Logger LOGGER = Logger.getLogger(VanillaPartialReparser.class.getName());

    private final Field lazyDocCommentsTable;
    private final Field parserDocComments;
    private final Method lineMapBuild;
    private final boolean allowPartialReparse;

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public VanillaPartialReparser() {
        Field lazyDocCommentsTable;
        try {
            lazyDocCommentsTable = LazyDocCommentTable.class.getDeclaredField("table");
            lazyDocCommentsTable.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            lazyDocCommentsTable = null;
        }
        this.lazyDocCommentsTable = lazyDocCommentsTable;
        Field parserDocComments;
        try {
            parserDocComments = com.sun.tools.javac.parser.JavacParser.class.getDeclaredField("docComments");
            parserDocComments.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            parserDocComments = null;
        }
        this.parserDocComments = parserDocComments;
        Method lineMapBuild;
        try {
            Class<?> lineMapImpl = Class.forName("com.sun.tools.javac.util.Position$LineMapImpl");
            lineMapBuild = lineMapImpl.getDeclaredMethod("build", char[].class, int.class);
            lineMapBuild.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException ex) {
            lineMapBuild = null;
        }
        this.lineMapBuild = lineMapBuild;
        allowPartialReparse = lazyDocCommentsTable != null && parserDocComments != null && lineMapBuild != null;
        if (!allowPartialReparse) {
            LOGGER.warning("Partial reparser disabled!");
        }
    }

    @Override
    public boolean reparseMethod (final CompilationInfoImpl ci,
            final Snapshot snapshot,
            final MethodTree orig,
            final String newBody) throws IOException {
        if (!allowPartialReparse)
            return false;
        assert ci != null;
        final FileObject fo = ci.getFileObject();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Reparse method in: {0}", fo);          //NOI18N
        }
        final Phase currentPhase = ci.getPhase();
        if (Phase.PARSED.compareTo(currentPhase) > 0) {
            return false;
        }
        try {
            final CompilationUnitTree cu = ci.getCompilationUnit();
            if (cu == null || newBody == null || orig.getBody() == null) {
                return false;
            }
            final JavacTaskImpl task = ci.getJavacTask();
            if (Options.instance(task.getContext()).isSet(JavacParser.LOMBOK_DETECTED)) {
                return false;
            }
            CompilationInfo info = JavaSourceAccessor.getINSTANCE().createCompilationInfo(ci);
            TreePath methodPath = info.getTreeUtilities().pathFor(((JCTree.JCMethodDecl) orig).pos + 1);
            if (methodPath.getLeaf().getKind() != Kind.METHOD) {
                return false;
            }
            Scope methodScope = info.getTrees().getScope(new TreePath(methodPath, ((MethodTree) methodPath.getLeaf()).getBody()));
//            PartialReparserService pr = PartialReparserService.instance(task.getContext());
//            if (((JCTree.JCMethodDecl)orig).localEnv == null) {
//                //We are seeing interface method or abstract or native method with body.
//                //Don't do any optimalization of this broken code - has no attr env.
//                return false;
//            }
            final JavacTrees jt = JavacTrees.instance(task);
            final int origStartPos = (int) jt.getSourcePositions().getStartPosition(cu, orig.getBody());
            final int origEndPos = (int) jt.getSourcePositions().getEndPosition(cu, orig.getBody());
            if (origStartPos < 0) {
                LOGGER.log(Level.WARNING, "Javac returned startpos: {0} < 0", new Object[]{origStartPos});  //NOI18N
                return false;
            }
            if (origStartPos > origEndPos) {
                LOGGER.log(Level.WARNING, "Javac returned startpos: {0} > endpos: {1}", new Object[]{origStartPos, origEndPos});  //NOI18N
                return false;
            }
            final FindAnonymousVisitor fav = new FindAnonymousVisitor();
            fav.scan(orig.getBody(), null);
            if (fav.hasLocalClass) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Skeep reparse method (old local classes): {0}", fo);   //NOI18N
                }
                return false;
            }
            final int noInner = fav.noInner;
            final Context ctx = task.getContext();
//            final TreeLoader treeLoader = TreeLoader.instance(ctx);
//            if (treeLoader != null) {
//                treeLoader.startPartialReparse();
//            }
            try {
                final NBLog l = NBLog.instance(ctx);
                l.startPartialReparse(cu.getSourceFile());
                final JavaFileObject prevLogged = l.useSource(cu.getSourceFile());
                JCTree.JCBlock block;
                try {
                    DiagnosticListener dl = ci.getDiagnosticListener();
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).startPartialReparse(origStartPos, origEndPos);
                    long start = System.currentTimeMillis();
                    Map<JCTree, Object> docComments = new HashMap<>();
                    block = reparseMethodBody(ctx, cu, orig, newBody + " ", docComments);
                    final EndPosTable endPos = ((JCTree.JCCompilationUnit)cu).endPositions;
                    LOGGER.log(Level.FINER, "Reparsed method in: {0}", fo);     //NOI18N
                    if (block == null) {
                        LOGGER.log(
                            Level.FINER,
                            "Skeep reparse method, invalid position, newBody: ",       //NOI18N
                            newBody);
                        return false;
                    }
                    final int newEndPos = (int) jt.getSourcePositions().getEndPosition(cu, block);
                    if (newEndPos != origStartPos + newBody.length()) {
                        return false;
                    }
                    fav.reset();
                    fav.scan(block, null);
                    final int newNoInner = fav.noInner;
                    if (fav.hasLocalClass || noInner != newNoInner) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Skeep reparse method (new local classes): {0}", fo);   //NOI18N
                        }
                        return false;
                    }
                    Map<JCTree, Object> docCommentsTable = (Map<JCTree, Object>) lazyDocCommentsTable.get(((JCTree.JCCompilationUnit)cu).docComments);
                    docCommentsTable.keySet().removeAll(fav.docOwners);
                    docCommentsTable.putAll(docComments);
                    long end = System.currentTimeMillis();
                    if (fo != null) {
                        JavacParser.logTime (fo,Phase.PARSED,(end-start));
                    }
                    final int delta = newEndPos - origEndPos;
                    final TranslatePositionsVisitor tpv = new TranslatePositionsVisitor(orig, endPos, delta);
                    tpv.scan(cu, null);
                    Enter.instance(ctx).unenter((JCTree.JCCompilationUnit) cu, ((JCTree.JCMethodDecl)orig).body);
                    ((JCTree.JCMethodDecl)orig).body = block;
                    if (Phase.RESOLVED.compareTo(currentPhase)<=0) {
                        start = System.currentTimeMillis();
                        reattrMethodBody(ctx, methodScope, orig, block);
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Resolved method in: {0}", fo);     //NOI18N
                        }
                        if (!((CompilationInfoImpl.DiagnosticListenerImpl)dl).hasPartialReparseErrors()) {
                            final JavacFlowListener fl = JavacFlowListener.instance(ctx);
                            if (fl != null && fl.hasFlowCompleted(fo)) {
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    final List<? extends Diagnostic> diag = ci.getDiagnostics();
                                    if (!diag.isEmpty()) {
                                        LOGGER.log(Level.FINER, "Reflow with errors: {0} {1}", new Object[]{fo, diag});     //NOI18N
                                    }
                                }
                                TreePath tp = TreePath.getPath(cu, orig);       //todo: store treepath in changed method => improve speed
                                Tree t = tp.getParentPath().getLeaf();
                                reflowMethodBody(ctx, cu, (ClassTree) t, orig);
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    LOGGER.log(Level.FINER, "Reflowed method in: {0}", fo); //NOI18N
                                }
                            }
                        }
                        end = System.currentTimeMillis();
                        if (fo != null) {
                            JavacParser.logTime (fo, Phase.ELEMENTS_RESOLVED,0L);
                            JavacParser.logTime (fo,Phase.RESOLVED,(end-start));
                        }
                    }

                    //fix CompilationUnitTree.getLineMap:
                    long startM = System.currentTimeMillis();
                    char[] chars = snapshot.getText().toString().toCharArray();
                    if (lineMapBuild != null) {
                        lineMapBuild.invoke(cu.getLineMap(), chars, chars.length);
                    }
                    LOGGER.log(Level.FINER, "Rebuilding LineMap took: {0}", System.currentTimeMillis() - startM);

                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).endPartialReparse (delta);
                } finally {
                    l.endPartialReparse(cu.getSourceFile());
                    l.useSource(prevLogged);
                }
                ci.update(snapshot);
            } finally {
//              if (treeLoader != null) {
//                  treeLoader.endPartialReparse();
//              }
            }
//        } catch (CouplingAbort ca) {
//            //Needs full reparse
//            return false;
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "partial reparse failed", t);
            if (LOGGER.isLoggable(Level.FINE)) {
                JavacParser.dumpSource(ci, t);
            }
            return false;
        }
        return true;
    }

    public JCTree.JCBlock reparseMethodBody(Context ctx, CompilationUnitTree topLevel, MethodTree methodToReparse, String newBodyText,
            final Map<JCTree, Object> docComments) throws IllegalArgumentException, IllegalAccessException {
        int startPos = ((JCTree.JCBlock)methodToReparse.getBody()).pos;
        char[] body = new char[startPos + newBodyText.length() + 1];
        Arrays.fill(body, 0, startPos, ' ');
        for (int i = 0; i < newBodyText.length(); i++) {
            body[startPos + i] = newBodyText.charAt(i);
        }
        body[startPos + newBodyText.length()] = '\u0000';
        CharBuffer buf = CharBuffer.wrap(body, 0, body.length - 1);
        com.sun.tools.javac.parser.JavacParser parser = newParser(ctx, buf, ((JCTree.JCBlock)methodToReparse.getBody()).pos, ((JCTree.JCCompilationUnit)topLevel).endPositions);
        final JCTree.JCStatement statement = parser.parseStatement();
        if (statement.getKind() == Tree.Kind.BLOCK) {
            if (docComments != null) {
                docComments.putAll((Map<JCTree, Object>) lazyDocCommentsTable.get(parserDocComments.get(parser)));
            }
            return (JCTree.JCBlock) statement;
        }
        return null;
    }

    private com.sun.tools.javac.parser.JavacParser newParser(Context context, CharSequence input, int startPos, final EndPosTable endPos) {
        NBParserFactory parserFactory = (NBParserFactory) NBParserFactory.instance(context); //TODO: eliminate the cast
        ScannerFactory scannerFactory = ScannerFactory.instance(context);
        CancelService cancelService = CancelService.instance(context);
        Scanner lexer = scannerFactory.newScanner(input, true);
//        lexer.seek(startPos);
        if (endPos instanceof NBParserFactory.NBJavacParser.EndPosTableImpl table) {
            table.resetErrorEndPos();
        }
        return new NBParserFactory.NBJavacParser(parserFactory, lexer, true, false, true, false, cancelService) {
            @Override protected com.sun.tools.javac.parser.JavacParser.AbstractEndPosTable newEndPosTable(boolean keepEndPositions) {
                return new com.sun.tools.javac.parser.JavacParser.AbstractEndPosTable(this) {

                    @Override
                    public void storeEnd(JCTree tree, int endpos) {
                        ((NBParserFactory.NBJavacParser.EndPosTableImpl)endPos).storeEnd(tree, endpos);
                    }

                    @Override
                    protected <T extends JCTree> T to(T t) {
                        storeEnd(t, token.endPos);
                        return t;
                    }

                    @Override
                    protected <T extends JCTree> T toP(T t) {
                        storeEnd(t, S.prevToken().endPos);
                        return t;
                    }

                    @Override
                    public int getEndPos(JCTree tree) {
                        return endPos.getEndPos(tree);
                    }

                    @Override
                    public int replaceTree(JCTree oldtree, JCTree newtree) {
                        return endPos.replaceTree(oldtree, newtree);
                    }

                    @Override
                    public void setErrorEndPos(int errPos) {
                        super.setErrorEndPos(errPos);
                        ((NBParserFactory.NBJavacParser.EndPosTableImpl)endPos).setErrorEndPos(errPos);
                    }
                };
            }
        };
    }

    public BlockTree reattrMethodBody(Context context, Scope scope, MethodTree methodToReparse, BlockTree block) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attr attr = Attr.instance(context);
//        assert ((JCTree.JCMethodDecl)methodToReparse).localEnv != null;
        JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) methodToReparse;
        final Names names = Names.instance(context);
        final Symtab syms = Symtab.instance(context);
        final Log log = Log.instance(context);
        final TreeMaker make = TreeMaker.instance(context);
        final Env<AttrContext> env = ((JavacScope) scope).getEnv();//this is a copy anyway...
        final Symbol.ClassSymbol owner = env.enclClass.sym;
        if (tree.name == names.init && !owner.type.isErroneous() && owner.type != syms.objectType) {
            JCTree.JCBlock body = tree.body;
            if (!TreeInfo.hasAnyConstructorCall(tree)) {
                body.stats = body.stats.
                prepend(make.at(body.pos).Exec(make.Apply(com.sun.tools.javac.util.List.nil(), make.Ident(names._super), com.sun.tools.javac.util.List.nil())));
            } else if ((env.enclClass.sym.flags() & Flags.ENUM) != 0 &&
                (tree.mods.flags & Flags.GENERATEDCONSTR) == 0 &&
                TreeInfo.isSuperCall(body.stats.head)) {
                // enum constructors are not allowed to call super
                // directly, so make sure there aren't any super calls
                // in enum constructors, except in the compiler
                // generated one.
                log.error(tree.body.stats.head.pos(),
                          new JCDiagnostic.Error("compiler",
                                    "call.to.super.not.allowed.in.enum.ctor",
                                    env.enclClass.sym));
                    }
        }
        attr.attribStat((JCTree.JCBlock)block, env);
        return block;
    }

    public BlockTree reflowMethodBody(Context context, CompilationUnitTree topLevel, ClassTree ownerClass, MethodTree methodToReparse) {
        Flow flow = Flow.instance(context);
        TreeMaker make = TreeMaker.instance(context);
        Enter enter = Enter.instance(context);
        flow.analyzeTree(enter.getEnv(((JCTree.JCClassDecl) ownerClass).sym), make);
        return methodToReparse.getBody();
    }

    public static class VerifyPartialReparse extends JavaParserResultTask<Result> {

        private final AtomicBoolean cancel = new AtomicBoolean();
        private final AtomicReference<JavacParser> parser = new AtomicReference<>();

        public VerifyPartialReparse() {
            super(Phase.UP_TO_DATE, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        public void run(Result result, SchedulerEvent event) {
            cancel.set(false);

            try {
                CompilationInfo info = CompilationInfo.get(result);

                if (info != null && info.getChangedTree() != null) {
                    CompilationInfoImpl reparsedInfoImpl = CompilationInfoAccessor.getInstance().getCompilationInfoImpl(info);
                    JavacParser verifyParser = new JavacParser(List.of(info.getSnapshot()), true);
                    parser.set(verifyParser);
                    if (cancel.get()) {
                        return ;
                    }
                    verifyParser.parse(info.getSnapshot(), this, null);
                    JavacParserResult verifyResult = verifyParser.getResult(this);
                    if (verifyResult == null || cancel.get()) {
                        return ;
                    }
                    CompilationInfo verifyInfo = CompilationInfo.get(verifyResult);
                    CompilationInfoImpl verifyInfoImpl = CompilationInfoAccessor.getInstance().getCompilationInfoImpl(verifyInfo);
                    verifyCompilationInfos(reparsedInfoImpl, verifyInfoImpl);
                }
            } catch (IOException | ParseException ex) {
                LOGGER.log(Level.WARNING, "verification failed", ex);
            } finally {
                parser.set(null);
            }
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            cancel.set(true);
            JavacParser currentParser = parser.get();
            if (currentParser != null) {
                currentParser.cancelParse();
            }
        }

        private void verifyCompilationInfos(CompilationInfoImpl reparsed, CompilationInfoImpl verifyInfo) throws IOException {
            if (cancel.get()) return ;
            String failInfo = "";
            //move to phase, and verify
            if (verifyInfo.toPhase(reparsed.getPhase()) != reparsed.getPhase()) {
                failInfo += "Expected phase: " + reparsed.getPhase() + "\n  Actual phase: " + verifyInfo.getPhase();
            }
            if (cancel.get()) return ;
            //verify diagnostics:
            Set<String> reparsedDiags = (Set<String>) reparsed.getDiagnostics().stream().map(this::diagnosticToString).collect(Collectors.toSet());
            if (cancel.get()) return ;
            Set<String> verifyDiags = (Set<String>) verifyInfo.getDiagnostics().stream().map(this::diagnosticToString).collect(Collectors.toSet());
            if (cancel.get()) return ;
            if (!Objects.equals(reparsedDiags, verifyDiags)) {
                failInfo += "Expected diags: " + reparsedDiags + "\n  Actual diags: " + verifyDiags;
            }
            if (cancel.get()) return ;
            String reparsedTree = treeToString(reparsed, reparsed.getCompilationUnit());
            if (cancel.get()) return ;
            String verifyTree = treeToString(verifyInfo, verifyInfo.getCompilationUnit());
            if (cancel.get()) return ;
            if (!Objects.equals(reparsedTree, verifyTree)) {
                failInfo += "Expected tree: " + reparsedTree + "\n  Actual tree: " + verifyTree;
            }
            if (!failInfo.isEmpty() && !cancel.get()) {
                Utilities.revalidate(reparsed.getFileObject());
                File dumpFile = JavacParser.createDumpFile(reparsed);
                if (dumpFile != null) {
                    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dumpFile), StandardCharsets.UTF_8))) {
                        writer.println("Incorrectly reparsed file: " + reparsed.getFileObject().toURI());
                        Snapshot original = reparsed.getPartialReparseLastGoodSnapshot();
                        if (original != null) {
                            writer.println("----- Original text: ---------------------------------------------");
                            writer.println(original.getText());
                        }
                        writer.println("----- Updated text: ---------------------------------------------");
                        writer.println(reparsed.getSnapshot().getText());
                        writer.println("----- Errors: ---------------------------------------------");
                        writer.println(failInfo);
                    }
                }
                LOGGER.log(Level.WARNING, "Incorrect partial reparse detected, dump filed: {0}", dumpFile);
            } else {
                reparsed.setPartialReparseLastGoodSnapshot(reparsed.getSnapshot());
            }
        }

        private String diagnosticToString(Diagnostic<JavaFileObject> d) {
            return d.getSource().toUri().toString() + ":" +
                   d.getKind() + ":" +
                   d.getStartPosition() + ":" +
                   d.getPosition() + ":" +
                   d.getEndPosition() + ":" +
                   d.getLineNumber() + ":" +
                   d.getColumnNumber() + ":" +
                   d.getCode() + ":" +
                   d.getMessage(null);
        }

        private String treeToString(CompilationInfoImpl info, CompilationUnitTree cut) {
            StringBuilder dump = new StringBuilder();
            new CancellableTreePathScanner<Void, Void>(cancel) {
                @Override
                public Void scan(Tree tree, Void p) {
                    if (tree == null) {
                        dump.append("null,");
                    } else {
                        TreePath tp = new TreePath(getCurrentPath(), tree);
                        dump.append(tree.getKind()).append(":");
                        dump.append(Trees.instance(info.getJavacTask()).getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree)).append(":");
                        dump.append(Trees.instance(info.getJavacTask()).getSourcePositions().getEndPosition(tp.getCompilationUnit(), tree)).append(":");
                        dump.append(String.valueOf(Trees.instance(info.getJavacTask()).getElement(tp))).append(":");
                        dump.append(normalizeCapture(String.valueOf(Trees.instance(info.getJavacTask()).getTypeMirror(tp)))).append(":");
                        dump.append(",");
                    }
                    return super.scan(tree, p);
                }
            }.scan(cut, null);
            return dump.toString();
        }

        private static final Pattern MIRROR_PATTERN = Pattern.compile("capture#(\\d+)");
        private static String normalizeCapture(String s) {
            // the toString result of a CapturedType contains the sequence
            // "capture#NUMBER" where number is the hashCode of the mirror
            // as hashCode is not overwriten, this is more or less a random
            // number and thus meaning less for the tree comparisson leading to
            // invalid incorrect partial reparsing reports
            //
            // This normalises it to a plain capture.
            return MIRROR_PATTERN.matcher(s).replaceAll("capture");
        }

        @MimeRegistration(service=TaskFactory.class, mimeType="text/x-java")
        public static final class FactoryImpl extends TaskFactory {

            @Override
            public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                // verifier is only enabled on FINE and lower levels
                return LOGGER.isLoggable(Level.FINE) ? List.of(new VerifyPartialReparse()) : List.of();
            }

        }
    }

}
