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

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Loaded;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.hints.providers.spi.ClassPathBasedHintProvider;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.hints.spiimpl.JackpotTrees.CatchWildcard;
import org.netbeans.modules.java.hints.spiimpl.JackpotTrees.VariableWildcard;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.lib.nbjavac.services.CancelService;
import org.netbeans.lib.nbjavac.services.NBParserFactory.NBJavacParser;
import org.netbeans.lib.nbjavac.services.NBParserFactory;
import org.netbeans.lib.nbjavac.services.NBResolve;
import org.netbeans.modules.java.hints.spiimpl.JackpotTrees.AnnotationWildcard;
import org.netbeans.modules.java.hints.spiimpl.JackpotTrees.FakeBlock;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {

    private Utilities() {}
    
    public static Set<Severity> disableErrors(FileObject file) {
        if (file.getAttribute(DISABLE_ERRORS) != null) {
            return EnumSet.allOf(Severity.class);
        }
        if (!file.canWrite() && FileUtil.getArchiveFile(file) != null) {
            return EnumSet.allOf(Severity.class);
        }

        return EnumSet.noneOf(Severity.class);
    }

    private static final String DISABLE_ERRORS = "disable-java-errors";
    private static final String SWITCH_EXPRESSION = "SWITCH_EXPRESSION";
    

    public static <E> Iterable<E> checkedIterableByFilter(final Iterable<?> raw, final Class<E> type, final boolean strict) {
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return NbCollections.checkedIteratorByFilter(raw.iterator(), type, strict);
            }
        };
    }
    
//    public static AnnotationTree constructConstraint(WorkingCopy wc, String name, TypeMirror tm) {
//        TreeMaker make = wc.getTreeMaker();
//        ExpressionTree variable = prepareAssignment(make, "variable", make.Literal(name));
//        ExpressionTree type     = prepareAssignment(make, "type", make.MemberSelect((ExpressionTree) make.Type(wc.getTypes().erasure(tm)), "class"));
//        TypeElement constraint  = wc.getElements().getTypeElement(Annotations.CONSTRAINT.toFQN());
//
//        return make.Annotation(make.QualIdent(constraint), Arrays.asList(variable, type));
//    }

    public static ExpressionTree prepareAssignment(TreeMaker make, String name, ExpressionTree value) {
        return make.Assignment(make.Identifier(name), value);
    }

    public static ExpressionTree findValue(AnnotationTree m, String name) {
        for (ExpressionTree et : m.getArguments()) {
            if (et.getKind() == Kind.ASSIGNMENT) {
                AssignmentTree at = (AssignmentTree) et;
                String varName = ((IdentifierTree) at.getVariable()).getName().toString();

                if (varName.equals(name)) {
                    return at.getExpression();
                }
            }

            if (et instanceof LiteralTree/*XXX*/ && "value".equals(name)) {
                return et;
            }
        }

        return null;
    }

    public static List<AnnotationTree> findArrayValue(AnnotationTree at, String name) {
        ExpressionTree fixesArray = findValue(at, name);
        List<AnnotationTree> fixes = new LinkedList<AnnotationTree>();

        if (fixesArray != null && fixesArray.getKind() == Kind.NEW_ARRAY) {
            NewArrayTree trees = (NewArrayTree) fixesArray;

            for (ExpressionTree fix : trees.getInitializers()) {
                if (fix.getKind() == Kind.ANNOTATION) {
                    fixes.add((AnnotationTree) fix);
                }
            }
        }

        if (fixesArray != null && fixesArray.getKind() == Kind.ANNOTATION) {
            fixes.add((AnnotationTree) fixesArray);
        }
        
        return fixes;
    }

    public static boolean isPureMemberSelect(Tree mst, boolean allowVariables) {
        switch (mst.getKind()) {
            case IDENTIFIER: return allowVariables || ((IdentifierTree) mst).getName().charAt(0) != '$';
            case MEMBER_SELECT: return isPureMemberSelect(((MemberSelectTree) mst).getExpression(), allowVariables);
            default: return false;
        }
    }

    public static Map<String, Collection<HintDescription>> sortOutHints(Iterable<? extends HintDescription> hints, Map<String, Collection<HintDescription>> output) {
        for (HintDescription d : hints) {
            Collection<HintDescription> h = output.get(d.getMetadata().displayName);

            if (h == null) {
                output.put(d.getMetadata().displayName, h = new LinkedList<HintDescription>());
            }

            h.add(d);
        }

        return output;
    }

    public static List<HintDescription> listAllHints(Set<ClassPath> cps) {
        List<HintDescription> result = new LinkedList<HintDescription>();

        for (Collection<? extends HintDescription> hints : RulesManager.getInstance().readHints(null, cps, new AtomicBoolean()).values()) {
            for (HintDescription hd : hints) {
                if (!(hd.getTrigger() instanceof PatternDescription)) continue; //TODO: only pattern based hints are currently supported
                result.add(hd);
            }
        }

        result.addAll(listClassPathHints(Collections.<ClassPath>emptySet(), cps));

        return result;
    }

    public static List<HintDescription> listClassPathHints(Set<ClassPath> sourceCPs, Set<ClassPath> binaryCPs) {
        List<HintDescription> result = new LinkedList<HintDescription>();
        Set<FileObject> roots = new HashSet<FileObject>();

        for (ClassPath cp : binaryCPs) {
            for (FileObject r : cp.getRoots()) {
                Result2 src = SourceForBinaryQuery.findSourceRoots2(r.toURL());

                if (src != null && src.preferSources()) {
                    roots.addAll(Arrays.asList(src.getRoots()));
                } else {
                    roots.add(r);
                }
            }
        }

        Set<ClassPath> cps = new HashSet<ClassPath>(sourceCPs);

        cps.add(ClassPathSupport.createClassPath(roots.toArray(new FileObject[0])));

        ClassPath cp = ClassPathSupport.createProxyClassPath(cps.toArray(new ClassPath[0]));

        for (ClassPathBasedHintProvider p : Lookup.getDefault().lookupAll(ClassPathBasedHintProvider.class)) {
            result.addAll(p.computeHints(cp, new AtomicBoolean()));
        }

        return result;
    }
    
    public static Tree parseAndAttribute(CompilationInfo info, String pattern, Scope scope) {
        return parseAndAttribute(info, pattern, scope, null);
    }

    public static Tree parseAndAttribute(CompilationInfo info, String pattern, Scope scope, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        return parseAndAttribute(info, JavaSourceAccessor.getINSTANCE().getJavacTask(info), pattern, scope, errors);
    }

    public static Tree parseAndAttribute(CompilationInfo info, String pattern, Scope scope, SourcePositions[] sourcePositions, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        return parseAndAttribute(info, JavaSourceAccessor.getINSTANCE().getJavacTask(info), pattern, scope, sourcePositions, errors);
    }

    public static Tree parseAndAttribute(JavacTaskImpl jti, String pattern) {
        return parseAndAttribute(jti, pattern, null);
    }

    public static Tree parseAndAttribute(JavacTaskImpl jti, String pattern, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        return parseAndAttribute(null, jti, pattern, null, errors);
    }

    public static Tree parseAndAttribute(JavacTaskImpl jti, String pattern, SourcePositions[] sourcePositions, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        return parseAndAttribute(null, jti, pattern, null, sourcePositions, errors);
    }

    private static Tree parseAndAttribute(CompilationInfo info, JavacTaskImpl jti, String pattern, Scope scope, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        return parseAndAttribute(info, jti, pattern, scope, new SourcePositions[1], errors);
    }

    private static Tree parseAndAttribute(CompilationInfo info, JavacTaskImpl jti, String pattern, Scope scope, SourcePositions[] sourcePositions, Collection<Diagnostic<? extends JavaFileObject>> errors) {
        Context c = jti.getContext();
        JavaCompiler.instance(c); //force reasonable initialization order
        TreeFactory make = TreeFactory.instance(c);
        List<Diagnostic<? extends JavaFileObject>> patternTreeErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
        Tree toAttribute;
        Tree patternTree = toAttribute = !isStatement(pattern) ? parseExpression(c, pattern, true, sourcePositions, patternTreeErrors) : null;
        int offset = 0;
        boolean expression = true;
        boolean classMember = false;

        if (pattern.startsWith("case ")) {//XXX: should be a lexer token
            List<Diagnostic<? extends JavaFileObject>> currentPatternTreeErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
            Tree switchTree = parseStatement(c, "switch ($$foo) {" + pattern + "}", sourcePositions, currentPatternTreeErrors);

            offset = "switch ($$foo) {".length();
            patternTreeErrors = currentPatternTreeErrors;
            toAttribute =  switchTree;
            patternTree = ((SwitchTree) switchTree).getCases().get(0);
        }

        if (patternTree == null || isErrorTree(patternTree) || SWITCH_EXPRESSION.equals(patternTree.getKind().name())) {
            SourcePositions[] currentPatternTreePositions = new SourcePositions[1];
            List<Diagnostic<? extends JavaFileObject>> currentPatternTreeErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
            Tree currentPatternTree = parseStatement(c, "{" + pattern + "}", currentPatternTreePositions, currentPatternTreeErrors);

            assert currentPatternTree.getKind() == Kind.BLOCK : currentPatternTree.getKind();

            List<? extends StatementTree> statements = ((BlockTree) currentPatternTree).getStatements();

            if (statements.size() == 1) {
                currentPatternTree = statements.get(0);
            } else {
                com.sun.tools.javac.util.List<JCStatement> newStatements = com.sun.tools.javac.util.List.<JCStatement>nil();

                if (!statements.isEmpty() && !Utilities.isMultistatementWildcardTree(statements.get(0)))
                    newStatements = newStatements.append((JCStatement) make.ExpressionStatement(make.Identifier("$$1$")));
                for (StatementTree st : statements) {
                    newStatements = newStatements.append((JCStatement) st);
                }
                if (!statements.isEmpty() && !Utilities.isMultistatementWildcardTree(statements.get(statements.size() - 1)))
                    newStatements = newStatements.append((JCStatement) make.ExpressionStatement(make.Identifier("$$2$")));

                currentPatternTree = new FakeBlock(0L, newStatements);
            }

            if (!currentPatternTreeErrors.isEmpty() || containsError(currentPatternTree)) {
                //maybe a class member?
                SourcePositions[] classPatternTreePositions = new SourcePositions[1];
                List<Diagnostic<? extends JavaFileObject>> classPatternTreeErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
                Tree classPatternTree = parseExpression(c, "new Object() {" + pattern + "}", false, classPatternTreePositions, classPatternTreeErrors);

                if (!containsError(classPatternTree)) {
                    sourcePositions[0] = classPatternTreePositions[0];
                    offset = "new Object() {".length();
                    patternTreeErrors = classPatternTreeErrors;
                    patternTree = toAttribute = classPatternTree;
                    classMember = true;
                } else {
                    offset = 1;
                    sourcePositions[0] = currentPatternTreePositions[0];
                    VariableTree var;
                    Names names = Names.instance(jti.getContext());
                    if (currentPatternTree.getKind() == Kind.VARIABLE && (var = ((VariableTree) currentPatternTree)).getType().getKind() == Kind.ERRONEOUS && var.getName() == names.error && var.getInitializer() == null && var.getModifiers().getAnnotations().size() == 1 && !containsError(var.getModifiers().getAnnotations().get(0))) {
                        patternTreeErrors = currentPatternTreeErrors; //TODO: the errors are incorrect
                        toAttribute = currentPatternTree;
                        patternTree = var.getModifiers().getAnnotations().get(0);
                    } else {
                        patternTreeErrors = currentPatternTreeErrors;
                        patternTree = toAttribute = currentPatternTree;
                    }
                }
            } else {
                sourcePositions[0] = currentPatternTreePositions[0];
                offset = 1;
                patternTreeErrors = currentPatternTreeErrors;
                patternTree = toAttribute = currentPatternTree;
            }

            expression = false;
        }

        if (scope != null) {
            TypeMirror type = attributeTree(jti, toAttribute, scope, patternTreeErrors);

            if (isError(type) && expression) {
                //maybe type?
                if (Utilities.isPureMemberSelect(patternTree, false)) {
                    SourcePositions[] varPositions = new SourcePositions[1];
                    List<Diagnostic<? extends JavaFileObject>> varErrors = new LinkedList<Diagnostic<? extends JavaFileObject>>();
                    Tree var = parseExpression(c, pattern + ".Class.class;", false, varPositions, varErrors);

                    attributeTree(jti, var, scope, varErrors);

                    ExpressionTree typeTree = ((MemberSelectTree) ((MemberSelectTree) var).getExpression()).getExpression();
                    final Symtab symtab = Symtab.instance(c);
                    final Elements el = jti.getElements();
                    final Trees trees = JavacTrees.instance(c);
                    CompilationUnitTree cut = ((JavacScope) scope).getEnv().toplevel;
                    final boolean[] found = new boolean[1];

                    new ErrorAwareTreePathScanner<Void, Void>() {
                        @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                            Element currentElement = trees.getElement(getCurrentPath());

                            if (!isError(currentElement)) {
                                if (currentElement.getKind() == ElementKind.PACKAGE && el.getPackageElement(node.toString()) == null) {
                                    ((JCFieldAccess) node).sym = symtab.errSymbol;
                                    ((JCFieldAccess) node).type = symtab.errType;
                                } else {
                                    found[0] = true;
                                    return null;
                                }
                            }

                            return super.visitMemberSelect(node, p);
                        }
                        @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                            Element currentElement = trees.getElement(getCurrentPath());

                            if (!isError(currentElement)) {
                                if (currentElement.getKind() == ElementKind.PACKAGE && el.getPackageElement(node.toString()) == null) {
                                    ((JCIdent) node).sym = symtab.errSymbol;
                                    ((JCIdent) node).type = symtab.errType;
                                } else {
                                    found[0] = true;
                                    return null;
                                }
                            }
                            return super.visitIdentifier(node, p);
                        }

                    }.scan(new TreePath(new TreePath(cut), typeTree), null);

                    if (found[0]) {
                        sourcePositions[0] = varPositions[0];
                        offset = 0;
                        patternTreeErrors = varErrors;
                        patternTree = typeTree;
                    }
                }
            }
        }

        if (classMember) {
            List<? extends Tree> members = ((NewClassTree) patternTree).getClassBody().getMembers();
            
            int syntheticOffset = !members.isEmpty() && members.get(0).getKind() == Kind.METHOD && (((JCMethodDecl) members.get(0)).mods.flags & Flags.GENERATEDCONSTR) != 0 ? 1 : 0;

            if (members.size() > 1 + syntheticOffset) {
                ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                List<Tree> newMembers = new LinkedList<Tree>();

                newMembers.add(make.ExpressionStatement(make.Identifier("$$1$")));
                newMembers.addAll(members.subList(syntheticOffset, members.size()));

                patternTree = make.Class(mt, "$", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), newMembers);
            } else {
                patternTree = members.get(0 + syntheticOffset);
            }
        }

        if (errors != null) {
            for (Diagnostic<? extends JavaFileObject> d : patternTreeErrors) {
                if (d.getCode().equals("compiler.err.cant.resolve")) { // NOI18N
                    String msg = d.getMessage(Locale.ENGLISH);
                    if (msg != null) {
                        int symIdx = msg.indexOf("symbol: "); // NOI18N
                        if (symIdx > 0) {
                            symIdx += 8;
                            // ignore errors for $ placeholders; may be identified as classnames as well
                            if (msg.charAt(symIdx) == '$' || msg.substring(symIdx, symIdx + 7).equals("class $")) { // NOI18N
                                continue;
                            }
                        }
                    }
                }
                if (d.getStartPosition() == -1 || d.getEndPosition() == -1) {
                    continue;
                }
                errors.add(new OffsetDiagnostic<JavaFileObject>(d, sourcePositions[0], -offset));
            }
        }

        sourcePositions[0] = new OffsetSourcePositions(sourcePositions[0], -offset);
        
        return patternTree;
    }

    static boolean isError(Element el) {
        return (el == null || (el.getKind() == ElementKind.CLASS) && isError(((TypeElement) el).asType()));
    }

    private static boolean isError(TypeMirror type) {
        return type == null || type.getKind() == TypeKind.ERROR;
    }

    private static boolean isStatement(String pattern) {
        return pattern.trim().endsWith(";");
    }

    private static boolean isErrorTree(Tree t) {
        return t.getKind() == Kind.ERRONEOUS || (t.getKind() == Kind.IDENTIFIER && ((IdentifierTree) t).getName().contentEquals("<error>")); //TODO: <error>...
    }
    
    private static boolean containsError(Tree t) {
        return new ErrorAwareTreeScanner<Boolean, Void>() {
            @Override
            public Boolean scan(Tree node, Void p) {
                if (node != null && isErrorTree(node)) {
                    return true;
                }
                return super.scan(node, p) ==Boolean.TRUE;
            }
            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
            }
        }.scan(t, null);
    }

    private static JCStatement parseStatement(Context context, CharSequence stmt, SourcePositions[] pos, final List<Diagnostic<? extends JavaFileObject>> errors) {
        if (stmt == null || (pos != null && pos.length != 1))
            throw new IllegalArgumentException();
        JavaCompiler compiler = JavaCompiler.instance(context);
        JavaFileObject prev = compiler.log.useSource(new DummyJFO());
        Log.DiagnosticHandler discardHandler = new Log.DiscardDiagnosticHandler(compiler.log) {
            @Override
            public void report(JCDiagnostic diag) {
                errors.add(diag);
            }            
        };
        try {
            CharBuffer buf = CharBuffer.wrap((stmt+"\u0000").toCharArray(), 0, stmt.length());
            ParserFactory factory = ParserFactory.instance(context);
            ScannerFactory scannerFactory = ScannerFactory.instance(context);
            Names names = Names.instance(context);
            Parser parser = newParser(context, (NBParserFactory) factory, scannerFactory.newScanner(buf, false), false, false, CancelService.instance(context), names);
            if (parser instanceof JavacParser) {
                if (pos != null)
                    pos[0] = new ParserSourcePositions((JavacParser)parser);
                return parser.parseStatement();
            }
            return null;
        } finally {
            compiler.log.useSource(prev);
            compiler.log.popDiagnosticHandler(discardHandler);
        }
    }

    private static JCExpression parseExpression(Context context, CharSequence expr, boolean onlyFullInput, SourcePositions[] pos, final List<Diagnostic<? extends JavaFileObject>> errors) {
        if (expr == null || (pos != null && pos.length != 1))
            throw new IllegalArgumentException();
        JavaCompiler compiler = JavaCompiler.instance(context);
        JavaFileObject prev = compiler.log.useSource(new DummyJFO());
        Log.DiagnosticHandler discardHandler = new Log.DiscardDiagnosticHandler(compiler.log) {
            @Override
            public void report(JCDiagnostic diag) {
                errors.add(diag);
            }            
        };
        try {
            CharBuffer buf = CharBuffer.wrap((expr+"\u0000").toCharArray(), 0, expr.length());
            ParserFactory factory = ParserFactory.instance(context);
            ScannerFactory scannerFactory = ScannerFactory.instance(context);
            Names names = Names.instance(context);
            Scanner scanner = scannerFactory.newScanner(buf, false);
            Parser parser = newParser(context, (NBParserFactory) factory, scanner, false, false, CancelService.instance(context), names);
            if (parser instanceof JavacParser) {
                if (pos != null)
                    pos[0] = new ParserSourcePositions((JavacParser)parser);
                JCExpression result = parser.parseExpression();

                if (!onlyFullInput || scanner.token().kind == TokenKind.EOF) {
                    return result;
                }
            }
            return null;
        } finally {
            compiler.log.useSource(prev);
            compiler.log.popDiagnosticHandler(discardHandler);
        }
    }

    private static TypeMirror attributeTree(JavacTaskImpl jti, Tree tree, Scope scope, final List<Diagnostic<? extends JavaFileObject>> errors) {
        Log log = Log.instance(jti.getContext());
        JavaFileObject prev = log.useSource(new DummyJFO());
        Log.DiagnosticHandler discardHandler = new Log.DiscardDiagnosticHandler(log) {
            @Override
            public void report(JCDiagnostic diag) {
                errors.add(diag);
            }            
        };
        NBResolve resolve = NBResolve.instance(jti.getContext());
        resolve.disableAccessibilityChecks();
//        Enter enter = Enter.instance(jti.getContext());
//        enter.shadowTypeEnvs(true);
//        ArgumentAttr argumentAttr = ArgumentAttr.instance(jti.getContext());
//        ArgumentAttr.LocalCacheContext cacheContext = argumentAttr.withLocalCacheContext();
        try {
            Attr attr = Attr.instance(jti.getContext());
            Env<AttrContext> env = ((JavacScope) scope).getEnv();
            if (tree instanceof JCExpression)
                return attr.attribExpr((JCTree) tree,env, Type.noType);
            return attr.attribStat((JCTree) tree,env);
        } finally {
//            cacheContext.leave();
            log.useSource(prev);
            log.popDiagnosticHandler(discardHandler);
            resolve.restoreAccessbilityChecks();
//            enter.shadowTypeEnvs(false);
        }
    }

    public static @CheckForNull CharSequence getWildcardTreeName(@NonNull Tree t) {
        if (t.getKind() == Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree) t).getExpression().getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) ((ExpressionStatementTree) t).getExpression();
            
            return identTree.getName().toString();
        }

        if (t.getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) t;
            String name = identTree.getName().toString();

            if (name.startsWith("$")) {
                return name;
            }
        }
        
        if (t.getKind() == Kind.TYPE_PARAMETER) {
            String name = ((TypeParameterTree) t).getName().toString();

            if (name.startsWith("$")) {
                return name;
            }
        }

        return null;
    }

    public static boolean isMultistatementWildcard(@NonNull CharSequence name) {
        return name.charAt(name.length() - 1) == '$';
    }

    public static boolean isMultistatementWildcardTree(Tree tree) {
        CharSequence name = Utilities.getWildcardTreeName(tree);

        return name != null && Utilities.isMultistatementWildcard(name);
    }

    private static long inc;

    public static Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints) {
        return constructScope(info, constraints, Collections.<String>emptyList());
    }

    public static Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints, Iterable<? extends String> auxiliaryImports) {
        ScopeDescription desc = new ScopeDescription(constraints, auxiliaryImports);
        Scope result = (Scope) info.getCachedValue(desc);

        if (result != null) return result;
        
        StringBuilder clazz = new StringBuilder();

        clazz.append("package $$;");

        for (String i : auxiliaryImports) {
            clazz.append(i);
        }

        long count = inc++;

        String classname = "$$scopeclass$constraints$" + count;

        clazz.append("public class " + classname + "{");

        for (Entry<String, TypeMirror> e : constraints.entrySet()) {
            if (e.getValue() != null) {
                clazz.append("private ");
                clazz.append(e.getValue().toString()); //XXX
                clazz.append(" ");
                clazz.append(e.getKey());
                clazz.append(";\n");
            }
        }

        clazz.append("private void test() {\n");
        clazz.append("}\n");
        clazz.append("}\n");

        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        Context context = jti.getContext();
        JavaCompiler compiler = JavaCompiler.instance(context);
        Modules modules = Modules.instance(context);
        Log log = Log.instance(context);
        NBResolve resolve = NBResolve.instance(context);
        Annotate annotate = Annotate.instance(context);
        Names names = Names.instance(context);
        Symtab syms = Symtab.instance(context);
        Log.DiagnosticHandler discardHandler = new Log.DiscardDiagnosticHandler(compiler.log);

        JavaFileObject jfo = FileObjects.memoryFileObject("$", "$", new File("/tmp/$$scopeclass$constraints$" + count + ".java").toURI(), System.currentTimeMillis(), clazz.toString());

        try {
            resolve.disableAccessibilityChecks();
            if (compiler.isEnterDone()) {
                annotate.blockAnnotations();
//                try {
//                    Field f = compiler.getClass().getDeclaredField("enterDone");
//                    f.setAccessible(true);
//                    f.set(compiler, false);
//                } catch (Throwable t) {
//                    Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, t);
//                }
                //was:
//                compiler.resetEnterDone();
            }
            
            JCCompilationUnit cut = compiler.parse(jfo);
            ClassSymbol enteredClass = syms.enterClass(modules.getDefaultModule(), names.fromString("$$." + classname));
            modules.enter(com.sun.tools.javac.util.List.of(cut), enteredClass);
            compiler.enterTrees(com.sun.tools.javac.util.List.of(cut));

            Todo todo = compiler.todo;
            ListBuffer<Env<AttrContext>> defer = new ListBuffer<Env<AttrContext>>();
            
            while (todo.peek() != null) {
                Env<AttrContext> env = todo.remove();

                if (env.toplevel == cut)
                    compiler.attribute(env);
                else
                    defer = defer.append(env);
            }

            todo.addAll(defer);

            Scope res = new ScannerImpl().scan(cut, info);

            info.putCachedValue(desc, res, CacheClearPolicy.ON_SIGNATURE_CHANGE);

            return res;
        } finally {
            resolve.restoreAccessbilityChecks();
            log.popDiagnosticHandler(discardHandler);
        }
    }

    private static final class ScannerImpl extends ErrorAwareTreePathScanner<Scope, CompilationInfo> {

        @Override
        public Scope visitBlock(BlockTree node, CompilationInfo p) {
            return p.getTrees().getScope(getCurrentPath());
        }

        @Override
        public Scope visitMethod(MethodTree node, CompilationInfo p) {
            if (node.getReturnType() == null) {
                return null;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Scope reduce(Scope r1, Scope r2) {
            return r1 != null ? r1 : r2;
        }

    }

    private static final class ScopeDescription {
        private final Map<String, TypeMirror> constraints;
        private final Iterable<? extends String> auxiliaryImports;

        public ScopeDescription(Map<String, TypeMirror> constraints, Iterable<? extends String> auxiliaryImports) {
            this.constraints = constraints;
            this.auxiliaryImports = auxiliaryImports;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ScopeDescription other = (ScopeDescription) obj;
            if (this.constraints != other.constraints && (this.constraints == null || !this.constraints.equals(other.constraints))) {
                return false;
            }
            if (this.auxiliaryImports != other.auxiliaryImports && (this.auxiliaryImports == null || !this.auxiliaryImports.equals(other.auxiliaryImports))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this.constraints != null ? this.constraints.hashCode() : 0);
            hash = 47 * hash + (this.auxiliaryImports != null ? this.auxiliaryImports.hashCode() : 0);
            return hash;
        }

    }

//    private static Scope constructScope2(CompilationInfo info, Map<String, TypeMirror> constraints) {
//        JavacScope s = (JavacScope) info.getTrees().getScope(new TreePath(info.getCompilationUnit()));
//        Env<AttrContext> env = s.getEnv();
//
//        env = env.dup(env.tree);
//
//        env.info.
//    }

    public static String toHumanReadableTime(double d) {
        StringBuilder result = new StringBuilder();
        long inSeconds = (long) (d / 1000);
        int seconds = (int) (inSeconds % 60);
        long inMinutes = inSeconds / 60;
        int minutes = (int) (inMinutes % 60);
        long inHours = inMinutes / 60;

        if (inHours > 0) {
            result.append(inHours);
            result.append("h");
        }

        if (minutes > 0) {
            result.append(minutes);
            result.append("m");
        }
        
        result.append(seconds);
        result.append("s");

        return result.toString();
    }

    public static ClasspathInfo createUniversalCPInfo() {
        return Lookup.getDefault().lookup(SPI.class).createUniversalCPInfo();
    }

    @SuppressWarnings("deprecation")
    public static void waitScanFinished() throws InterruptedException {
        SourceUtils.waitScanFinished();
    }

    public static Set<? extends String> findSuppressedWarnings(CompilationInfo info, TreePath path) {
        //TODO: cache?
        Set<String> keys = new HashSet<String>();

        while (path != null) {
            Tree leaf = path.getLeaf();

            switch (leaf.getKind()) {
                case METHOD:
                    handleSuppressWarnings(info, path, ((MethodTree) leaf).getModifiers(), keys);
                    break;
                case CLASS:
                    handleSuppressWarnings(info, path, ((ClassTree) leaf).getModifiers(), keys);
                    break;
                case VARIABLE:
                    handleSuppressWarnings(info, path, ((VariableTree) leaf).getModifiers(), keys);
                    break;
            }

            path = path.getParentPath();
        }

        return Collections.unmodifiableSet(keys);
    }

    private static void handleSuppressWarnings(CompilationInfo info, TreePath path, ModifiersTree modifiers, final Set<String> keys) {
        Element el = info.getTrees().getElement(path);

        if (el == null) {
            return ;
        }

        for (AnnotationMirror am : el.getAnnotationMirrors()) {
            Name fqn = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
            
            if (!fqn.contentEquals("java.lang.SuppressWarnings")) {
                continue;
            }

            for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                if (!e.getKey().getSimpleName().contentEquals("value"))
                    continue;

                e.getValue().accept(new AnnotationValueVisitor<Void, Void>() {
                    public Void visit(AnnotationValue av, Void p) {
                        av.accept(this, p);
                        return null;
                    }
                    public Void visit(AnnotationValue av) {
                        av.accept(this, null);
                        return null;
                    }
                    public Void visitBoolean(boolean b, Void p) {
                        return null;
                    }
                    public Void visitByte(byte b, Void p) {
                        return null;
                    }
                    public Void visitChar(char c, Void p) {
                        return null;
                    }
                    public Void visitDouble(double d, Void p) {
                        return null;
                    }
                    public Void visitFloat(float f, Void p) {
                        return null;
                    }
                    public Void visitInt(int i, Void p) {
                        return null;
                    }
                    public Void visitLong(long i, Void p) {
                        return null;
                    }
                    public Void visitShort(short s, Void p) {
                        return null;
                    }
                    public Void visitString(String s, Void p) {
                        keys.add(s);
                        return null;
                    }
                    public Void visitType(TypeMirror t, Void p) {
                        return null;
                    }
                    public Void visitEnumConstant(VariableElement c, Void p) {
                        return null;
                    }
                    public Void visitAnnotation(AnnotationMirror a, Void p) {
                        return null;
                    }
                    public Void visitArray(List<? extends AnnotationValue> vals, Void p) {
                        for (AnnotationValue av : vals) {
                            av.accept(this, p);
                        }
                        return null;
                    }
                    public Void visitUnknown(AnnotationValue av, Void p) {
                        return null;
                    }
                }, null);
            }
        }
    }

    public static Tree generalizePattern(CompilationInfo info, TreePath original) {
        return generalizePattern(JavaSourceAccessor.getINSTANCE().getJavacTask(info), original);
    }

    public static Tree generalizePattern(CompilationTask task, TreePath original) {
        JavacTaskImpl jti = (JavacTaskImpl) task;
        com.sun.tools.javac.util.Context c = jti.getContext();
        TreeFactory make = TreeFactory.instance(c);
        Trees javacTrees = Trees.instance(task);
        GeneralizePattern gp = new GeneralizePattern(javacTrees, make);

        gp.scan(original, null);

        GeneralizePatternITT itt = new GeneralizePatternITT(gp.tree2Variable);

        itt.attach(c, new NoImports(c), null);

        return itt.translate(original.getLeaf());
    }

    public static Tree generalizePattern(CompilationInfo info, TreePath original, int firstStatement, int lastStatement) {
        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        com.sun.tools.javac.util.Context c = jti.getContext();
        TreeFactory make = TreeFactory.instance(c);
        Tree translated = Utilities.generalizePattern(jti, original);

        assert translated.getKind() == Kind.BLOCK;

        List<StatementTree> newStatements = new LinkedList<StatementTree>();
        BlockTree block = (BlockTree) translated;

        if (firstStatement != lastStatement) {
            newStatements.add(make.ExpressionStatement(make.Identifier("$s0$")));
            newStatements.addAll(block.getStatements().subList(firstStatement, lastStatement + 1));
            newStatements.add(make.ExpressionStatement(make.Identifier("$s1$")));

            translated = make.Block(newStatements, block.isStatic());
        } else {
            translated = block.getStatements().get(firstStatement);
        }

        return translated;
    }

    public interface SPI {
        public ClasspathInfo createUniversalCPInfo();
    }

    @ServiceProvider(service=SPI.class)
    public static final class NbSPIImpl implements SPI, PropertyChangeListener {
        
        /**
         * Cached reference to the ClasspathInfo created from the platform.
         */
        private volatile Reference<ClasspathInfo>    cached = new WeakReference<>(null);
        
        // @GuardedBy(this)
        private PropertyChangeListener weakL;

        public synchronized ClasspathInfo createUniversalCPInfo() {
            Reference<ClasspathInfo> r = cached;
            if (r != null) {
                ClasspathInfo c = r.get();
                if (c != null) {
                    return c;
                }
            }
            JavaPlatform select = JavaPlatform.getDefault();
            final JavaPlatformManager man = JavaPlatformManager.getDefault();
            if (select.getSpecification().getVersion() != null) {
                for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                    if (!"j2se".equals(p.getSpecification().getName()) || p.getSpecification().getVersion() == null) continue;
                    if (p.getSpecification().getVersion().compareTo(select.getSpecification().getVersion()) > 0) {
                        select = p;
                    }
                }
            }
            final ClasspathInfo result = new ClasspathInfo.Builder(select.getBootstrapLibraries())
                                                          .setModuleBootPath(select.getBootstrapLibraries())
                                                          .build();
            if (cached != null) {
                    this.cached = new WeakReference<>(result);
            }
            if (weakL == null) {
                man.addPropertyChangeListener(weakL = WeakListeners.propertyChange(this, man));
            }
            return result;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cached = null;
        }
    }
    
    private static final class GeneralizePattern extends ErrorAwareTreePathScanner<Void, Void> {

        public final Map<Tree, Tree> tree2Variable = new HashMap<Tree, Tree>();
        private final Map<Element, String> element2Variable = new HashMap<Element, String>();
        private final Trees javacTrees;
        private final TreeFactory make;

        private int currentVariableIndex = 0;

        public GeneralizePattern(Trees javacTrees, TreeFactory make) {
            this.javacTrees = javacTrees;
            this.make = make;
        }

        private @NonNull String getVariable(@NonNull Element el) {
            String var = element2Variable.get(el);

            if (var == null) {
                element2Variable.put(el, var = "$" + currentVariableIndex++);
            }

            return var;
        }

        private boolean shouldBeGeneralized(@NonNull Element el) {
            if (el.getModifiers().contains(Modifier.PRIVATE)) {
                return true;
            }

            switch (el.getKind()) {
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case PARAMETER:
                    return true;
            }

            return false;
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            Element e = javacTrees.getElement(getCurrentPath());

            if (e != null && shouldBeGeneralized(e)) {
                tree2Variable.put(node, make.Identifier(getVariable(e)));
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = javacTrees.getElement(getCurrentPath());

            if (e != null && shouldBeGeneralized(e)) {
                VariableTree nue = make.Variable(node.getModifiers(), getVariable(e), node.getType(), node.getInitializer());

                tree2Variable.put(node, nue);
            }

            return super.visitVariable(node, p);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void p) {
            //XXX:
            if (node.getEnclosingExpression() != null) {
                tree2Variable.put(node, make.Identifier("$" + currentVariableIndex++));
                return null;
            }

            NewClassTree nue = make.NewClass(node.getEnclosingExpression(), Collections.<ExpressionTree>singletonList(make.Identifier("$" + currentVariableIndex++ + "$")), make.Identifier("$" + currentVariableIndex++), Collections.<ExpressionTree>singletonList(make.Identifier("$" + currentVariableIndex++ + "$")), null);

            tree2Variable.put(node, nue);

            return null;
        }

    }

    private static final class GeneralizePatternITT extends ImmutableTreeTranslator {

        private final Map<Tree, Tree> tree2Variable;

        public GeneralizePatternITT(Map<Tree, Tree> tree2Variable) {
            super(null);
            this.tree2Variable = tree2Variable;
        }

        @Override
        public Tree translate(Tree tree) {
            Tree var = tree2Variable.remove(tree);

            if (var != null) {
                return super.translate(var);
            }

            return super.translate(tree);
        }

    }

    private static final class NoImports extends ImportAnalysis2 {

        public NoImports(Context env) {
            super(env);
        }

        @Override
        public void classEntered(ClassTree clazz) {}

        @Override
        public void enterVisibleThroughClasses(ClassTree clazz) {}

        @Override
        public void classLeft() {}

        @Override
        public ExpressionTree resolveImport(MemberSelectTree orig, Element element) {
            return orig;
        }

        @Override
        public void setCompilationUnit(CompilationUnitTree cut) {}

        @Override
        public void setImports(List<? extends ImportTree> importsToAdd) {}

        @Override
        public Set<? extends Element> getImports() {
            return Collections.emptySet();
        }

        @Override
        public void setPackage(ExpressionTree packageNameTree) {}

    }

    public static long patternValue(Tree pattern) {
        class VisitorImpl extends ErrorAwareTreeScanner<Void, Void> {
            private int value;
            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) value++;
                return super.scan(node, p);
            }
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (node.getName().toString().startsWith("$")) value--;
                
                return super.visitIdentifier(node, p);
            }
            @Override
            public Void visitNewClass(NewClassTree node, Void p) {
                return null;
            }
        }

        VisitorImpl vi = new VisitorImpl();

        vi.scan(pattern, null);

        return vi.value;
    }

    public static boolean containsMultistatementTrees(List<? extends Tree> statements) {
        for (Tree t : statements) {
            if (Utilities.isMultistatementWildcardTree(t)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isJavadocSupported(CompilationInfo info) { //TODO: unnecessary?
        return true;
    }

    private static Class<?> parserClass;
    private static synchronized Parser newParser(Context ctx, NBParserFactory fac,
                                                 Lexer S, boolean keepDocComments,
                                                 boolean keepLineMap, CancelService cancelService,
                                                 Names names) {
        try {
            if (parserClass == null) {
                Method switchBlockStatementGroup = JavacParser.class.getDeclaredMethod("switchBlockStatementGroup");
                Method delegate;
                if (switchBlockStatementGroup.getReturnType().equals(com.sun.tools.javac.util.List.class)) {
                    delegate = JackpotJavacParser.class.getDeclaredMethod("switchBlockStatementGroupListImpl");
                } else {
                    delegate = JackpotJavacParser.class.getDeclaredMethod("switchBlockStatementGroupImpl");
                }
                parserClass = load(new ByteBuddy()
                                    .subclass(JackpotJavacParser.class)
                                    .method(ElementMatchers.named("switchBlockStatementGroup")).intercept(MethodCall.invoke(delegate))
                                    .make())
                            .getLoaded();
            }
            return (Parser) parserClass.getConstructor(Context.class, NBParserFactory.class, Lexer.class, boolean.class, boolean.class, CancelService.class, Names.class)
                    .newInstance(ctx, fac, S, keepDocComments, keepLineMap, cancelService, names);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static <T> Loaded<T> load(Unloaded<T> unloaded) {
        ClassLoadingStrategy<ClassLoader> strategy;

        try {
            strategy = ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup());
        } catch (IllegalStateException ex) {
            strategy = new ClassLoadingStrategy.ForUnsafeInjection();
        }

        return unloaded.load(JackpotTrees.class.getClassLoader(), strategy);
    }

    private static class JackpotJavacParser extends NBJavacParser {

        private final Context ctx;
        private final com.sun.tools.javac.tree.TreeMaker make;
        private final com.sun.tools.javac.util.Name dollar;
        public JackpotJavacParser(Context ctx, NBParserFactory fac,
                         Lexer S,
                         boolean keepDocComments,
                         boolean keepLineMap,
                         CancelService cancelService,
                         Names names) {
            super(fac, S, keepDocComments, keepLineMap, true, false, cancelService);
            this.ctx = ctx;
            this.make = com.sun.tools.javac.tree.TreeMaker.instance(ctx);
            this.dollar = names.fromString("$");
        }

        @Override
        protected JCModifiers modifiersOpt(JCModifiers partial) {
            if (token.kind == TokenKind.IDENTIFIER) {
                String ident = token.name().toString();

                if (Utilities.isMultistatementWildcard(ident)) {
                    com.sun.tools.javac.util.Name name = token.name();

                    nextToken();
                    
                    JCModifiers result = super.modifiersOpt(partial);
                    
                    result.annotations = result.annotations.prepend(new AnnotationWildcard(name, F.Ident(name)));

                    return result;
                }
            }

            return super.modifiersOpt(partial);
        }

        @Override
        public JCVariableDecl formalParameter(boolean lambdaParam) {
            if (token.kind == TokenKind.IDENTIFIER) {
                if (token.name().startsWith(dollar)) {
                    com.sun.tools.javac.util.Name name = token.name();

                    Token peeked = S.token(1);

                    if (peeked.kind == TokenKind.COMMA || peeked.kind == TokenKind.RPAREN) {
                        nextToken();
                        return new VariableWildcard(ctx, name, F.Ident(name));
                    }
                }
            }

            return super.formalParameter(lambdaParam);
        }

        @Override
        protected JCVariableDecl implicitParameter() {
            if (token.kind == TokenKind.IDENTIFIER) {
                if (token.name().startsWith(dollar)) {
                    com.sun.tools.javac.util.Name name = token.name();

                    Token peeked = S.token(1);

                    if (peeked.kind == TokenKind.COMMA || peeked.kind == TokenKind.RPAREN) {
                        nextToken();
                        return new VariableWildcard(ctx, name, F.Ident(name));
                    }
                }
            }

            return super.implicitParameter();
        }
        
        @Override
        protected JCCatch catchClause() {
            if (token.kind == TokenKind.CATCH) {
                Token peeked = S.token(1);
                
                if (   peeked.kind == TokenKind.IDENTIFIER
                    && Utilities.isMultistatementWildcard(peeked.name().toString())) {
                    accept(TokenKind.CATCH);
                    
                    com.sun.tools.javac.util.Name name = token.name();

                    accept(TokenKind.IDENTIFIER);

                    return new CatchWildcard(ctx, name, F.Ident(name));
                } else {
                    nextToken();
                }
            }
            return super.catchClause();
        }

        @Override
        public com.sun.tools.javac.util.List<JCTree> classOrInterfaceBodyDeclaration(com.sun.tools.javac.util.Name className, boolean isInterface) {
            if (token.kind == TokenKind.IDENTIFIER) {
                if (token.name().startsWith(dollar)) {
                    com.sun.tools.javac.util.Name name = token.name();

                    Token peeked = S.token(1);

                    if (peeked.kind == TokenKind.SEMI) {
                        nextToken();
                        nextToken();
                        
                        return com.sun.tools.javac.util.List.<JCTree>of(F.Ident(name));
                    }
                }
            }
            return super.classOrInterfaceBodyDeclaration(className, isInterface);
        }
        
        @Override
        protected JCExpression checkExprStat(JCExpression t) {
            if (t.getTag() == JCTree.Tag.IDENT) {
                if (((IdentifierTree) t).getName().toString().startsWith("$")) {
                    return t;
                }
            }
            return super.checkExprStat(t);
        }

        protected JCCase switchBlockStatementGroupImpl() throws Throwable {
            if (token.kind == TokenKind.CASE) {
                Token peeked = S.token(1);

                if (peeked.kind == TokenKind.IDENTIFIER) {
                    String ident = peeked.name().toString();

                    if (ident.startsWith("$") && ident.endsWith("$")) {
                        nextToken();
                        
                        int pos = token.pos;
                        com.sun.tools.javac.util.Name name = token.name();

                        nextToken();

                        if (token.kind == TokenKind.SEMI) {
                            nextToken();
                        }

                        JCIdent identTree = F.at(pos).Ident(name);

                        return JackpotTrees.createInstance(ctx, JCCase.class, name, identTree, new Class[] {JCExpression.class, com.sun.tools.javac.util.List.class}, new Object[] {identTree, com.sun.tools.javac.util.List.nil()});
                    }
                }
            }

            return (JCCase) MethodHandles.lookup()
                                         .findSpecial(NBJavacParser.class, "switchBlockStatementGroup", MethodType.methodType(JCCase.class), JackpotJavacParser.class)
                                         .invoke(this);
        }

        protected com.sun.tools.javac.util.List<JCCase> switchBlockStatementGroupListImpl() throws Throwable {
            if (token.kind == TokenKind.CASE) {
                Token peeked = S.token(1);

                if (peeked.kind == TokenKind.IDENTIFIER) {
                    String ident = peeked.name().toString();

                    if (ident.startsWith("$") && ident.endsWith("$")) {
                        nextToken();

                        int pos = token.pos;
                        com.sun.tools.javac.util.Name name = token.name();

                        nextToken();

                        if (token.kind == TokenKind.SEMI) {
                            nextToken();
                        }

                        Class caseKind = Class.forName("com.sun.source.tree.CaseTree$CaseKind", false, JCCase.class.getClassLoader());
                        JCIdent identTree = F.at(pos).Ident(name);
                        return com.sun.tools.javac.util.List.of(JackpotTrees.createInstance(ctx, JCCase.class, name, identTree, new Class[] {caseKind, com.sun.tools.javac.util.List.class, com.sun.tools.javac.util.List.class, JCTree.class}, new Object[] {Enum.valueOf(caseKind, "STATEMENT"), com.sun.tools.javac.util.List.of(identTree), com.sun.tools.javac.util.List.nil(), null}));
                    }
                }
            }

            return (com.sun.tools.javac.util.List) MethodHandles.lookup()
                                                                .findSpecial(NBJavacParser.class, "switchBlockStatementGroup", MethodType.methodType(com.sun.tools.javac.util.List.class), JackpotJavacParser.class)
                                                                .invoke(this);
        }

        @Override
        protected JCTree resource() {
            if (token.kind == TokenKind.IDENTIFIER && token.name().startsWith(dollar)) {
                Token peeked = S.token(1);

                if (peeked.kind == TokenKind.SEMI || peeked.kind == TokenKind.RPAREN) {
                    int pos = token.pos;
                    com.sun.tools.javac.util.Name name = token.name();

                    nextToken();

                    return F.at(pos).Ident(name);
                }
            }
            return super.resource();
        }

    }

    private static final class DummyJFO extends SimpleJavaFileObject {
        private DummyJFO() {
            super(URI.create("dummy.java"), JavaFileObject.Kind.SOURCE);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return "";
        }
    };

    /**
     * Only for members (i.e. generated constructor):
     */
    public static List<? extends Tree> filterHidden(TreePath basePath, Iterable<? extends Tree> members) {
        List<Tree> result = new LinkedList<Tree>();

        for (Tree t : members) {
            if (!isSynthetic(basePath != null ? basePath.getCompilationUnit() : null, t)) {
                result.add(t);
            }
        }

        return result;
    }

    private static boolean isSynthetic(CompilationUnitTree cut, Tree leaf) throws NullPointerException {
        JCTree tree = (JCTree) leaf;

        if (tree.pos == (-1))
            return true;

        if (leaf.getKind() == Kind.METHOD) {
            //check for synthetic constructor:
            return (((JCMethodDecl)leaf).mods.flags & Flags.GENERATEDCONSTR) != 0L;
        }

        //check for synthetic superconstructor call:
        if (cut != null && leaf.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionStatementTree est = (ExpressionStatementTree) leaf;

            if (est.getExpression().getKind() == Kind.METHOD_INVOCATION) {
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();

                if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                    IdentifierTree it = (IdentifierTree) mit.getMethodSelect();

                    if ("super".equals(it.getName().toString())) {
                        return ((JCCompilationUnit) cut).endPositions.getEndPos(tree) == (-1);
                    }
                }
            }
        }

        return false;
    }

    public static boolean isFakeBlock(Tree t) {
        return t instanceof FakeBlock;
    }

    public static boolean isFakeClass(Tree t) {
        if (!(t instanceof ClassTree)) {
            return false;
        }

        ClassTree ct = (ClassTree) t;

        if (ct.getMembers().isEmpty()) {
            return false;
        }

        CharSequence wildcardTreeName = Utilities.getWildcardTreeName(ct.getMembers().get(0));

        if (wildcardTreeName == null) {
            return false;
        }

        return wildcardTreeName.toString().startsWith("$$");
    }

    private static final class OffsetSourcePositions implements SourcePositions {

        private final SourcePositions delegate;
        private final long offset;

        public OffsetSourcePositions(SourcePositions delegate, long offset) {
            this.delegate = delegate;
            this.offset = offset;
        }

        public long getStartPosition(CompilationUnitTree cut, Tree tree) {
            return delegate.getStartPosition(cut, tree) + offset;
        }

        public long getEndPosition(CompilationUnitTree cut, Tree tree) {
            return delegate.getEndPosition(cut, tree) + offset;
        }

    }

    private static final class OffsetDiagnostic<S> implements Diagnostic<S> {
        private final Diagnostic<? extends S> delegate;
        private final SourcePositions sp;
        private final long offset;

        public OffsetDiagnostic(Diagnostic<? extends S> delegate, SourcePositions sp, long offset) {
            this.delegate = delegate;
            this.sp = sp;
            this.offset = offset;
        }

        public Diagnostic.Kind getKind() {
            return delegate.getKind();
        }

        public S getSource() {
            return delegate.getSource();
        }

        public long getPosition() {
            return delegate.getPosition() + offset;
        }

        public long getStartPosition() {
            return delegate.getStartPosition() + offset;
        }

        public long getEndPosition() {
            if (delegate instanceof JCDiagnostic) {
                JCDiagnostic dImpl = (JCDiagnostic) delegate;
                
                return dImpl.getDiagnosticPosition().getEndPosition(new EndPosTable() {
                    @Override public int getEndPos(JCTree tree) {
                        return (int) sp.getEndPosition(null, tree);
                    }
                    @Override public void storeEnd(JCTree tree, int endpos) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    @Override public int replaceTree(JCTree oldtree, JCTree newtree) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }) + offset;
            }
            return delegate.getEndPosition() + offset;
        }

        public long getLineNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getColumnNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getCode() {
            return delegate.getCode();
        }

        public String getMessage(Locale locale) {
            return delegate.getMessage(locale);
        }

    }

    private static class ParserSourcePositions implements SourcePositions {

        private JavacParser parser;

        private ParserSourcePositions(JavacParser parser) {
            this.parser = parser;
        }

        public long getStartPosition(CompilationUnitTree file, Tree tree) {
            return parser.getStartPos((JCTree)tree);
        }

        public long getEndPosition(CompilationUnitTree file, Tree tree) {
            return parser.getEndPos((JCTree)tree);
        }
    }
}
