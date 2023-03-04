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

package org.netbeans.modules.java.hints.declarative;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.CodeSource;
import javax.lang.model.element.Modifier;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.declarative.Condition.False;
import org.netbeans.modules.java.hints.declarative.Condition.Instanceof;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation.ParameterKind;
import org.netbeans.modules.java.hints.declarative.Condition.Otherwise;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;

import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

import static org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId.*;

/**
 *
 * @author lahvac
 */
public class DeclarativeHintsParser {

    public static boolean disableCustomCode = false;
    
    //used by tests:
    static Class<?>[] auxConditionClasses;

    private static final class Impl {
    private final FileObject file;
    private final CharSequence text;
    private final TokenSequence<DeclarativeHintTokenId> input;
    private final Map<String, String> options = new HashMap<>();
    private       String importsBlockCode;
    private       int[] importsBlockSpan;
    private final List<HintTextDescription> hints = new LinkedList<>();
    private final List<String> blocksCode = new LinkedList<>();
    private final List<int[]> blocksSpan = new LinkedList<>();
    private final List<ErrorDescription> errors = new LinkedList<>();
    private final MethodInvocationContext mic;

    private Impl(FileObject file, CharSequence text, TokenSequence<DeclarativeHintTokenId> input) {
        this.file = file;
        this.text = text;
        this.input = input;
        this.mic = new MethodInvocationContext();

        if (auxConditionClasses != null) {
            this.mic.ruleUtilities.addAll(Arrays.asList(auxConditionClasses));
        }
    }

    private boolean nextToken() {
        while (input.moveNext()) {
            if (id() != WHITESPACE && id() != BLOCK_COMMENT && id() != LINE_COMMENT) {
                return true;
            }
        }

        eof = true;
        
        return false;
    }

    private boolean eof;

    private Token<DeclarativeHintTokenId> token() {
        return input.token();
    }

    private DeclarativeHintTokenId id() {
        return token().id();
    }

    private boolean readToken(DeclarativeHintTokenId id) {
        if (id() == id) {
            nextToken();
            return true;
        }

        return false;
    }

    private void parseInput() {
        boolean wasFirstRule = false;
        
        while (nextToken()) {
            if (id() == JAVA_BLOCK) {
                if (disableCustomCode) {
                    int pos = token().offset(null);
                    errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "Custom code not allowed", file, pos, pos + 2));
                    break;
                }
                String block = token().text().toString();
                int soffs = block.startsWith("<?") ? 2 : 0; // NOI18N
                // handle a case <?> -- see #244576
                int eoffs = block.endsWith("?>") ? Math.max(soffs, block.length() - 2) : block.length(); // NOI18N
                block = block.substring(soffs, eoffs);
                int[] span = new int[] {token().offset(null) + soffs, token().offset(null) + eoffs};
                if (importsBlockCode == null && !wasFirstRule) {
                    importsBlockCode = block;
                    importsBlockSpan = span;
                } else {
                    blocksCode.add(block);
                    blocksSpan.add(span);
                }
            }

            wasFirstRule = true;
        }

        mic.setCode(importsBlockCode, blocksCode);
        input.moveStart();
        eof = false;
        
        while (nextToken()) {
            if (id() == JAVA_BLOCK) {
                continue;
            }
            
            maybeParseOptions(options);
            parseRule();
        }
    }

    private void parseRule() {
        String displayName = parseDisplayName();
        int patternStart = input.offset();
        
        while (   id() != LEADS_TO
               && id() != DOUBLE_COLON
               && id() != DOUBLE_SEMICOLON
               && id() != OPTIONS
               && !eof) {
            nextToken();
        }

        if (eof) {
            //XXX: should report an error
            return ;
        }

        int patternEnd = input.offset();

        Map<String, String> ruleOptions = new HashMap<>();

        maybeParseOptions(ruleOptions);

        List<Condition> conditions = new LinkedList<>();
        List<int[]> conditionsSpans = new LinkedList<>();

        if (id() == DOUBLE_COLON) {
            parseConditions(conditions, conditionsSpans);
        }

        List<FixTextDescription> targets = new LinkedList<>();

        while (id() == LEADS_TO && !eof) {
            nextToken();

            String fixDisplayName = parseDisplayName();

            int targetStart = input.offset();

            while (   id() != LEADS_TO
                   && id() != DOUBLE_COLON
                   && id() != DOUBLE_SEMICOLON
                   && id() != OPTIONS
                   && !eof) {
                nextToken();
            }

            int targetEnd = input.offset();
            
            Map<String, String> fixOptions = new HashMap<>();

            maybeParseOptions(fixOptions);

            int[] span = new int[] {targetStart, targetEnd};
            List<Condition> fixConditions = new LinkedList<>();
            List<int[]> fixConditionSpans = new LinkedList<>();

            if (id() == DOUBLE_COLON) {
                parseConditions(fixConditions, fixConditionSpans);
            }

            targets.add(new FixTextDescription(fixDisplayName, span, fixConditions, fixConditionSpans, fixOptions));
        }

        hints.add(new HintTextDescription(displayName, patternStart, patternEnd, input.offset() + input.token().length(), conditions, conditionsSpans, targets, ruleOptions));
    }
    
    private void parseConditions(List<Condition> conditions, List<int[]> spans) {
        do {
            nextToken();
            parseCondition(conditions, spans);
        } while (id() == AND && !eof);
    }

    private void parseCondition(List<Condition> conditions, List<int[]> spans) {
        int conditionStart = input.offset();

        if (id() == OTHERWISE) {
            nextToken();
            conditions.add(new Otherwise());
            spans.add(new int[] {conditionStart, input.offset()});
            return ;
        }

        boolean not = false;

        if (id() == NOT) {
            not = true;
            nextToken();
        }
        
        if (id() == VARIABLE) {
            String name = token().text().toString();

            nextToken();

            if (id() != INSTANCEOF) {
                //XXX: report an error
                return ;
            }
            
            nextToken();

            int typeStart = input.offset();

            nextToken();

            int typeEnd = input.offset();

            conditions.add(new Instanceof(not, name, text.subSequence(typeStart, typeEnd).toString(), new int[] {typeStart, typeEnd}));
            spans.add(new int[] {conditionStart, typeEnd});
            return ;
        }

        int start   = input.offset();
        
        while (id() != AND && id() != LEADS_TO && id() != DOUBLE_SEMICOLON && !eof) {
            nextToken();
        }
        
        int end = input.offset();

        try {
            Condition mi = resolve(mic, text.subSequence(start, end).toString(), not, conditionStart, file, errors);
            int[] span = new int[]{conditionStart, end};

            if ((mi instanceof MethodInvocation) && !((MethodInvocation) mi).link()) {
                if (file != null) {
                    errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "Cannot resolve method", file, span[0], span[1]));
                }

                mi = new False();
            }

            conditions.add(mi);
            spans.add(span);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void maybeParseOptions(Map<String, String> to) {
        if (id() != OPTIONS)
            return ;

        String opts = token().text().toString();

        if (opts.length() > 2) {
            parseOptions(opts.substring(2, opts.length() - 1), to);
        } else {
            //XXX: produce error
        }

        nextToken();
    }
    
    private String parseDisplayName() {
        if (token().id() == DeclarativeHintTokenId.CHAR_LITERAL || token().id() == DeclarativeHintTokenId.STRING_LITERAL) {
            Token<DeclarativeHintTokenId> t = token();

            if (input.moveNext()) {
                if (input.token().id() == DeclarativeHintTokenId.COLON) {
                    String displayName = t.text().subSequence(1, t.text().length() - 1).toString();

                    nextToken();
                    return displayName;
                } else {
                    input.movePrevious();
                }
            }
        }

        return null;
    }
    }

    private static final Pattern OPTION = Pattern.compile("([^=]+)=(([^\"].*?)|(\".*?\")),");
    
    static void parseOptions(String options, Map<String, String> to) {
        Matcher m = OPTION.matcher(options);
        int end = 0;

        while (m.find()) {
            to.put(m.group(1), unquote(m.group(2)));
            end = m.end();
        }

        String[] keyValue = options.substring(end).split("=");

        if (keyValue.length == 1) {
            //TODO: semantics? error?
            to.put(keyValue[0], "");
        } else {
            to.put(keyValue[0], unquote(keyValue[1]));
        }
    }
    
    private static String unquote(String what) {
        if (what.length() > 2 && what.charAt(0) == '"' && what.charAt(what.length() - 1) == '"')
            return what.substring(1, what.length() - 1);
        else
            return what;
    }
    
    public Result parse(@NullAllowed FileObject file, CharSequence text, TokenSequence<DeclarativeHintTokenId> ts) {
        Impl i = new Impl(file, text, ts);

        i.parseInput();

        return new Result(i.options, i.importsBlockSpan, i.hints, i.blocksSpan, i.errors);
    }
    
    // never null, just cleared by GC.
    private static volatile Reference<ClassPath> javacApiClasspath = new WeakReference<>(null);

    /**
     * Marker that javac api is not available.
     */
    private static final Reference<ClassPath> NONE = new WeakReference<>(null);
    
    private static ClassPath getJavacApiJarClasspath() {
        Reference<ClassPath> r = javacApiClasspath;
        ClassPath res = r.get();
        if (res != null) {
            return res;
        }
        if (r == NONE) {
            return null;
        }
        CodeSource codeSource = Modifier.class.getProtectionDomain().getCodeSource();
        URL javacApiJar = codeSource != null ? codeSource.getLocation() : null;
        if (javacApiJar != null) {
            Logger.getLogger(DeclarativeHintsParser.class.getName()).log(Level.FINE, "javacApiJar={0}", javacApiJar);
            File aj = FileUtil.archiveOrDirForURL(javacApiJar);
            if (aj != null) {
                res = ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(aj));
                javacApiClasspath = new WeakReference<>(res);
                return res;
            }
        }
        javacApiClasspath = NONE;
        return null;
    }
    
    /**
     * As long as the cachedInfo lives, Holder provides the original universalPath
     * instance. As cachedInfo hardrefs univesalPath already, the universalPath member does not prevent
     * GC. Keeps itself alive as long as the cachedInfo is alive.
     */
    private static class Holder implements ChangeListener {
        final Reference<ClasspathInfo>   cachedInfo;
        final ClasspathInfo   universalPath;

        public Holder(ClasspathInfo cachedInfo, ClasspathInfo universalPath) {
            this.cachedInfo = new WeakReference<>(cachedInfo);
            this.universalPath = universalPath;
            cachedInfo.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // NO-op
        }
    }
    
    private static volatile Reference<Holder>  cache = new WeakReference<>(null);

    private static @NonNull Condition resolve(MethodInvocationContext mic, final String invocation, final boolean not, final int offset, final FileObject file, final List<ErrorDescription> errors) throws IOException {
        final String[] methodName = new String[1];
        final Map<String, ParameterKind> params = new LinkedHashMap<>();
        ClasspathInfo cpInfo = Hacks.createUniversalCPInfo();
        
        ClassPath javacPath = getJavacApiJarClasspath();
        if (javacPath != null) {
            ClasspathInfo result = null;
            Reference<Holder> h = cache;
            Holder holder;
            if (h != null && (holder = h.get()) != null) {
                if (holder.universalPath == cpInfo) {
                    result = holder.cachedInfo.get();
                }
            }
            if (result == null) {
                ClassPath bootCP =
                        ClassPathSupport.createProxyClassPath(javacPath,
                                                              cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT)
                                                             );
                ClassPath systemCP = cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT);
                result = new ClasspathInfo.Builder(bootCP)
                                          .setModuleBootPath(systemCP)
                                          .build();
                cache = new WeakReference<>(new Holder(result, cpInfo));
            }
            cpInfo = result;
        }
        JavaSource.create(cpInfo).runUserActionTask((CompilationController parameter) -> {
            parameter.toPhase(JavaSource.Phase.RESOLVED);
            if (invocation == null || invocation.isEmpty()) {
                //XXX: report an error
                return ;
            }
            SourcePositions[] positions = new SourcePositions[1];
            ExpressionTree et = parameter.getTreeUtilities().parseExpression(invocation, positions);

            if (et.getKind() != Kind.METHOD_INVOCATION) {
                //XXX: report an error
                return ;
            }

            MethodInvocationTree mit = (MethodInvocationTree) et;

            if (mit.getMethodSelect().getKind() != Kind.IDENTIFIER) {
                //XXX: report an error
                return ;
            }

            Scope s = Hacks.constructScope(parameter, "javax.lang.model.SourceVersion", "javax.lang.model.element.Modifier", "javax.lang.model.element.ElementKind");

            parameter.getTreeUtilities().attributeTree(et, s);

            methodName[0] = ((IdentifierTree) mit.getMethodSelect()).getName().toString();

            for (ExpressionTree t : mit.getArguments()) {
                switch (t.getKind()) {
                    case STRING_LITERAL:
                        params.put(((LiteralTree) t).getValue().toString(), ParameterKind.STRING_LITERAL);
                        break;
                    case INT_LITERAL:
                        params.put(((LiteralTree) t).getValue().toString(), ParameterKind.INT_LITERAL);
                        break;
                    case IDENTIFIER:
                        String name = ((IdentifierTree) t).getName().toString();

                        if (name.startsWith("$")) {
                            params.put(name, ParameterKind.VARIABLE);
                            break;
                        }
                    case MEMBER_SELECT:
                        TreePath tp = parameter.getTrees().getPath(s.getEnclosingClass());
                        Element e = parameter.getTrees().getElement(new TreePath(tp, t));

                        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                            int start = (int) positions[0].getStartPosition(null, t) + offset;
                            int end = (int) positions[0].getEndPosition(null, t) + offset;
                            errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "Cannot resolve enum constant", file, start, end));
                            break;
                        }
                        name = ((TypeElement) e.getEnclosingElement()).getQualifiedName() + "." + e.getSimpleName();
                        params.put(name, ParameterKind.ENUM_CONSTANT);
                        break;
                }
            }
        }, true);

        if (methodName[0] == null) {
            return new False();
        }
        
        return new MethodInvocation(not, methodName[0], params, mic);
    }

    public static final class Result {

        public final Map<String, String> options;
        public final int[] importsBlock;
        public final List<HintTextDescription> hints;
        public final List<int[]> blocks;
        public final List<ErrorDescription> errors;

        public Result(Map<String, String> options, int[] importsBlock, List<HintTextDescription> hints, List<int[]> blocks, List<ErrorDescription> errors) {
            this.options = options;
            this.importsBlock = importsBlock;
            this.hints = hints;
            this.blocks = blocks;
            this.errors = errors;
        }

    }

    public static final class HintTextDescription {
        public final String displayName;
        public final int textStart;
        public final int textEnd;
        public final int hintEnd;
        public final List<Condition> conditions;
        public final List<int[]> conditionSpans;
        public final List<FixTextDescription> fixes;
        public final Map<String, String> options;

        public HintTextDescription(String displayName, int textStart, int textEnd, int hintEnd, List<Condition> conditions, List<int[]> conditionSpans, List<FixTextDescription> fixes, Map<String, String> options) {
            this.displayName = displayName;
            this.textStart = textStart;
            this.textEnd = textEnd;
            this.hintEnd = hintEnd;
            this.conditions = conditions;
            this.conditionSpans = conditionSpans;
            this.fixes = fixes;
            this.options = options;
        }

    }

    public static final class FixTextDescription {
        public final String displayName;
        public final int[] fixSpan;
        public final List<Condition> conditions;
        public final List<int[]> conditionSpans;
        public final Map<String, String> options;

        public FixTextDescription(String displayName, int[] fixSpan, List<Condition> conditions, List<int[]> conditionSpans, Map<String, String> options) {
            this.displayName = displayName;
            this.fixSpan = fixSpan;
            this.conditions = conditions;
            this.conditionSpans = conditionSpans;
            this.options = options;
        }
    }
}
