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

package org.netbeans.modules.java.completion;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.Symbols;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.parsing.api.Source;
import org.openide.util.Pair;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.SourceVersion.RELEASE_10;
import static javax.lang.model.SourceVersion.RELEASE_11;
import static javax.lang.model.SourceVersion.RELEASE_16;
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.lang.model.type.TypeKind.VOID;

/**
 *
 * @author Dusan Balek
 */
public final class JavaCompletionTask<T> extends BaseTask {

    public static <I> JavaCompletionTask<I> create(final int caretOffset, @NonNull final ItemFactory<I> factory, @NonNull final Set<Options> options, @NullAllowed final Callable<Boolean> cancel) {
        return new JavaCompletionTask<>(caretOffset, factory, cancel, options);
    }

    public static interface ItemFactory<T> {

        T createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType);

        T createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement);

        T createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType);

        T createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends);

        T createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements);

        T createTypeParameterItem(TypeParameterElement elem, int substitutionOffset);

        T createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset);

        T createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType);

        T createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef);

        default T createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean afterConstructorTypeParams, boolean smartType, int assignToVarOffset, boolean memberRef) {
            return createExecutableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, smartType, assignToVarOffset, memberRef);
        }

        T createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name);

        T createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement);

        T createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter);

        T createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType);

        T createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name);

        T createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated);

        T createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated);

        T createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount);

        T createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, boolean smartType);

        T createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, boolean smartType);

        T createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon);

        T createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset);
    }
    
    public static interface TypeCastableItemFactory<T> extends ItemFactory<T> {

        T createTypeCastableVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset);

        T createTypeCastableExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef);        
    }
    
    public static interface LambdaItemFactory<T> extends ItemFactory<T> {
        T createLambdaItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, boolean expression, boolean addSemicolon);
    }
    
    public static interface ModuleItemFactory<T> extends ItemFactory<T> {
        T createModuleItem(String moduleName, int substitutionOffset);
    }

    public static interface RecordPatternItemFactory<T> extends ItemFactory<T> {
        T createRecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars);
    }

    public static enum Options {

        ALL_COMPLETION,
        COMBINED_COMPLETION,
        SKIP_ACCESSIBILITY_CHECK
    }

    private static final String ERROR = "<error>"; //NOI18N
    private static final String INIT = "<init>"; //NOI18N
    private static final String SPACE = " "; //NOI18N
    private static final String COLON = ":"; //NOI18N
    private static final String SEMI = ";"; //NOI18N
    private static final String EMPTY = ""; //NOI18N

    private static final String ABSTRACT_KEYWORD = "abstract"; //NOI18N
    private static final String ASSERT_KEYWORD = "assert"; //NOI18N
    private static final String BOOLEAN_KEYWORD = "boolean"; //NOI18N
    private static final String BREAK_KEYWORD = "break"; //NOI18N
    private static final String BYTE_KEYWORD = "byte"; //NOI18N
    private static final String CASE_KEYWORD = "case"; //NOI18N
    private static final String CATCH_KEYWORD = "catch"; //NOI18N
    private static final String CHAR_KEYWORD = "char"; //NOI18N
    private static final String CLASS_KEYWORD = "class"; //NOI18N
    private static final String CONTINUE_KEYWORD = "continue"; //NOI18N
    private static final String DEFAULT_KEYWORD = "default"; //NOI18N
    private static final String DO_KEYWORD = "do"; //NOI18N
    private static final String DOUBLE_KEYWORD = "double"; //NOI18N
    private static final String ELSE_KEYWORD = "else"; //NOI18N
    private static final String ENUM_KEYWORD = "enum"; //NOI18N
    private static final String EXPORTS_KEYWORD = "exports"; //NOI18N
    private static final String EXTENDS_KEYWORD = "extends"; //NOI18N
    private static final String FALSE_KEYWORD = "false"; //NOI18N
    private static final String FINAL_KEYWORD = "final"; //NOI18N
    private static final String FINALLY_KEYWORD = "finally"; //NOI18N
    private static final String FLOAT_KEYWORD = "float"; //NOI18N
    private static final String FOR_KEYWORD = "for"; //NOI18N
    private static final String IF_KEYWORD = "if"; //NOI18N
    private static final String IMPLEMENTS_KEYWORD = "implements"; //NOI18N
    private static final String IMPORT_KEYWORD = "import"; //NOI18N
    private static final String INSTANCEOF_KEYWORD = "instanceof"; //NOI18N
    private static final String INT_KEYWORD = "int"; //NOI18N
    private static final String INTERFACE_KEYWORD = "interface"; //NOI18N
    private static final String LONG_KEYWORD = "long"; //NOI18N
    private static final String MODULE_KEYWORD = "module"; //NOI18N
    private static final String NATIVE_KEYWORD = "native"; //NOI18N
    private static final String NEW_KEYWORD = "new"; //NOI18N
    private static final String NON_SEALED_KEYWORD = "non-sealed"; //NOI18N
    private static final String NULL_KEYWORD = "null"; //NOI18N
    private static final String OPEN_KEYWORD = "open"; //NOI18N
    private static final String OPENS_KEYWORD = "opens"; //NOI18N
    private static final String PACKAGE_KEYWORD = "package"; //NOI18N
    private static final String PERMITS_KEYWORD = "permits"; //NOI18N
    private static final String PRIVATE_KEYWORD = "private"; //NOI18N
    private static final String PROTECTED_KEYWORD = "protected"; //NOI18N
    private static final String PROVIDES_KEYWORD = "provides"; //NOI18N
    private static final String PUBLIC_KEYWORD = "public"; //NOI18N
    private static final String RETURN_KEYWORD = "return"; //NOI18N
    private static final String REQUIRES_KEYWORD = "requires"; //NOI18N
    private static final String SEALED_KEYWORD = "sealed"; //NOI18N
    private static final String SHORT_KEYWORD = "short"; //NOI18N
    private static final String STATIC_KEYWORD = "static"; //NOI18N
    private static final String STRICT_KEYWORD = "strictfp"; //NOI18N
    private static final String SUPER_KEYWORD = "super"; //NOI18N
    private static final String SWITCH_KEYWORD = "switch"; //NOI18N
    private static final String SYNCHRONIZED_KEYWORD = "synchronized"; //NOI18N
    private static final String THIS_KEYWORD = "this"; //NOI18N
    private static final String THROW_KEYWORD = "throw"; //NOI18N
    private static final String THROWS_KEYWORD = "throws"; //NOI18N
    private static final String TO_KEYWORD = "to"; //NOI18N
    private static final String TRANSIENT_KEYWORD = "transient"; //NOI18N
    private static final String TRANSITIVE_KEYWORD = "transitive"; //NOI18N
    private static final String TRUE_KEYWORD = "true"; //NOI18N
    private static final String TRY_KEYWORD = "try"; //NOI18N
    private static final String USES_KEYWORD = "uses"; //NOI18N
    private static final String VAR_KEYWORD = "var"; //NOI18N
    private static final String VOID_KEYWORD = "void"; //NOI18N
    private static final String VOLATILE_KEYWORD = "volatile"; //NOI18N
    private static final String WHEN_KEYWORD = "when"; //NOI18N
    private static final String WHILE_KEYWORD = "while"; //NOI18N
    private static final String WITH_KEYWORD = "with"; //NOI18N
    private static final String YIELD_KEYWORD = "yield"; //NOI18N
    private static final String RECORD_KEYWORD = "record"; //NOI18N
    private static final String JAVA_LANG_CLASS = "java.lang.Class"; //NOI18N
    private static final String JAVA_LANG_OBJECT = "java.lang.Object"; //NOI18N
    private static final String JAVA_LANG_STRING = "java.lang.String"; //NOI18N
    private static final String JAVA_LANG_ITERABLE = "java.lang.Iterable"; //NOI18N

    private static final String[] PRIM_KEYWORDS = new String[]{
        BOOLEAN_KEYWORD, BYTE_KEYWORD, CHAR_KEYWORD, DOUBLE_KEYWORD,
        FLOAT_KEYWORD, INT_KEYWORD, LONG_KEYWORD, SHORT_KEYWORD
    };

    private static final String[] STATEMENT_KEYWORDS = new String[]{
        DO_KEYWORD, IF_KEYWORD, FOR_KEYWORD, SWITCH_KEYWORD, SYNCHRONIZED_KEYWORD, TRY_KEYWORD,
        VOID_KEYWORD, WHILE_KEYWORD
    };

    private static final String[] STATEMENT_SPACE_KEYWORDS = new String[]{
        ASSERT_KEYWORD, NEW_KEYWORD, THROW_KEYWORD
    };

    private static final String[] BLOCK_KEYWORDS = new String[]{
        ASSERT_KEYWORD, CLASS_KEYWORD, FINAL_KEYWORD, NEW_KEYWORD, STRICT_KEYWORD,
        THROW_KEYWORD
    };

    private static final String[] MODULE_BODY_KEYWORDS = new String[]{
        EXPORTS_KEYWORD, OPENS_KEYWORD, REQUIRES_KEYWORD, PROVIDES_KEYWORD, USES_KEYWORD
    };

    private static final String[] CLASS_BODY_KEYWORDS = new String[]{
        ABSTRACT_KEYWORD, CLASS_KEYWORD, ENUM_KEYWORD, FINAL_KEYWORD,
        INTERFACE_KEYWORD, NATIVE_KEYWORD, PRIVATE_KEYWORD, PROTECTED_KEYWORD,
        PUBLIC_KEYWORD, STATIC_KEYWORD, STRICT_KEYWORD, SYNCHRONIZED_KEYWORD,
        TRANSIENT_KEYWORD, VOID_KEYWORD, VOLATILE_KEYWORD
    };

    private final ItemFactory<T> itemFactory;
    private final Set<Options> options;
    private final AddSwitchRelatedItem addSwitchItemDefault;

    private ArrayList<T> results;
    private boolean hasAdditionalClasses;
    private boolean hasAdditionalMembers;
    private int anchorOffset;

    private JavaCompletionTask(final int caretOffset, final ItemFactory<T> factory, final Callable<Boolean> cancel, final Set<Options> options) {
        super(caretOffset, cancel);
        this.itemFactory = factory;
        this.options = options;
        addSwitchItemDefault = new AddSwitchRelatedItem() {
            @Override
            public void addTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
                results.add(itemFactory.createTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType));
            }

            @Override
            public void addVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
                results.add(itemFactory.createVariableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset));
            }
        };
    }

    public List<T> getResults() {
        return results;
    }

    public boolean hasAdditionalClasses() {
        return hasAdditionalClasses;
    }

    public boolean hasAdditionalMembers() {
        return hasAdditionalMembers;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    protected void resolve(CompilationController controller) throws IOException {
        Env env = getCompletionEnvironment(controller, true);
        if (env == null) {
            return;
        }
        if (options.contains(JavaCompletionTask.Options.SKIP_ACCESSIBILITY_CHECK)) {
            env.skipAccessibilityCheck();
        }
        results = new ArrayList<>();
        anchorOffset = controller.getSnapshot().getOriginalOffset(env.getOffset());
        TreePath path = env.getPath();
        switch (path.getLeaf().getKind()) {
            case COMPILATION_UNIT:
                insideCompilationUnit(env);
                break;
            case MODULE:
                insideModule(env);
                break;
            case EXPORTS:
                insideExports(env);
                break;
            case OPENS:
                insideOpens(env);
                break;
            case PROVIDES:
                insideProvides(env);
                break;
            case REQUIRES:
                insideRequires(env);
                break;
            case USES:
                insideUses(env);
                break;
            case PACKAGE:
                insidePackage(env);
                break;
            case IMPORT:
                insideImport(env);
                break;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                insideClass(env);
                break;
            case VARIABLE:
                insideVariable(env);
                break;
            case METHOD:
                insideMethod(env);
                break;
            case MODIFIERS:
                insideModifiers(env, path);
                break;
            case ANNOTATION:
            case TYPE_ANNOTATION:
                insideAnnotation(env);
                break;
            case ANNOTATED_TYPE:
                insideAnnotatedType(env);
                break;
            case TYPE_PARAMETER:
                insideTypeParameter(env);
                break;
            case PARAMETERIZED_TYPE:
                insideParameterizedType(env, path);
                break;
            case UNBOUNDED_WILDCARD:
            case EXTENDS_WILDCARD:
            case SUPER_WILDCARD:
                TreePath parentPath = path.getParentPath();
                if (parentPath.getLeaf().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                    insideParameterizedType(env, parentPath);
                }
                break;
            case BLOCK:
                insideBlock(env);
                break;
            case MEMBER_SELECT:
                insideMemberSelect(env);
                break;
            case MEMBER_REFERENCE:
                insideMemberReference(env);
                break;
            case LAMBDA_EXPRESSION:
                insideLambdaExpression(env);
                break;
            case METHOD_INVOCATION:
                insideMethodInvocation(env);
                break;
            case NEW_CLASS:
                insideNewClass(env);
                break;
            case ASSERT:
            case RETURN:
            case THROW:
                localResult(env);
                addValueKeywords(env);
                break;
            case TRY:
                insideTry(env);
                break;
            case CATCH:
                insideCatch(env);
                break;
            case UNION_TYPE:
                insideUnionType(env);
                break;
            case IF:
                insideIf(env);
                break;
            case WHILE_LOOP:
                insideWhile(env);
                break;
            case DO_WHILE_LOOP:
                insideDoWhile(env);
                break;
            case FOR_LOOP:
                insideFor(env);
                break;
            case ENHANCED_FOR_LOOP:
                insideForEach(env);
                break;
            case SWITCH:
                insideSwitch(env);
                break;
            case CASE:
                insideCase(env);
                break;
            case LABELED_STATEMENT:
                localResult(env);
                addKeywordsForStatement(env);
                break;
            case PARENTHESIZED:
                insideParens(env);
                break;
            case TYPE_CAST:
                insideExpression(env, path);
                break;
            case INSTANCE_OF:
                insideTypeCheck(env);
                break;
            case ARRAY_ACCESS:
                insideArrayAccess(env);
                break;
            case NEW_ARRAY:
                insideNewArray(env);
                break;
            case ASSIGNMENT:
                insideAssignment(env);
                break;
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case AND_ASSIGNMENT:
            case XOR_ASSIGNMENT:
            case OR_ASSIGNMENT:
                insideCompoundAssignment(env);
                break;
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case UNARY_PLUS:
            case UNARY_MINUS:
            case BITWISE_COMPLEMENT:
            case LOGICAL_COMPLEMENT:
                localResult(env);
                break;
            case AND:
            case CONDITIONAL_AND:
            case CONDITIONAL_OR:
            case DIVIDE:
            case EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case LEFT_SHIFT:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case MINUS:
            case MULTIPLY:
            case NOT_EQUAL_TO:
            case OR:
            case PLUS:
            case REMAINDER:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case XOR:
                insideBinaryTree(env);
                break;
            case CONDITIONAL_EXPRESSION:
                insideConditionalExpression(env);
                break;
            case EXPRESSION_STATEMENT:
                insideExpressionStatement(env);
                break;
            case BREAK:
            case CONTINUE:
                insideBreakOrContinue(env);
                break;
            case STRING_LITERAL:
                insideStringLiteral(env);
                break;
            case SWITCH_EXPRESSION:
                insideSwitch(env);
                break;
            case RECORD:
                insideRecord(env);
                break;
            case DEFAULT_CASE_LABEL:
                localResult(env);
                addKeywordsForBlock(env);
                break;
            case DECONSTRUCTION_PATTERN:
                insideDeconstructionRecordPattern(env);
                break;
        }
    }

    private void insideCompilationUnit(Env env) throws IOException {
        int offset = env.getOffset();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        Tree pkg = root.getPackageName();
        if (pkg == null || offset <= sourcePositions.getStartPosition(root, root)) {
            addKeywordsForCU(env);
            return;
        }
        if (offset <= sourcePositions.getStartPosition(root, pkg)) {
            addPackages(env, null, true);
        } else {
            TokenSequence<JavaTokenId> first = findFirstNonWhitespaceToken(env, (int) sourcePositions.getEndPosition(root, pkg), offset);
            if (first != null && first.token().id() == JavaTokenId.SEMICOLON) {
                addKeywordsForCU(env);
            }
        }
    }

    private void insideModule(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        CompilationController controller = env.getController();
        int startPos = (int) env.getSourcePositions().getStartPosition(env.getRoot(), path.getLeaf());
        String headerText = controller.getText().substring(startPos, offset);
        int idx = headerText.indexOf('{'); //NOI18N
        if (idx >= 0) {
            addKeywordsForModuleBody(env);
        } else if (!headerText.contains("module")) {
            addKeyword(env, MODULE_KEYWORD, SPACE, false);
        }
    }

    private void insideExports(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        ExportsTree exp = (ExportsTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (exp.getModuleNames() != null) {
            int startPos = (int) sourcePositions.getStartPosition(root, exp);
            Tree lastModule = null;
            for (Tree mdl : exp.getModuleNames()) {
                int implPos = (int) sourcePositions.getEndPosition(root, mdl);
                if (implPos == Diagnostic.NOPOS || offset <= implPos) {
                    break;
                }
                lastModule = mdl;
                startPos = implPos;
            }
            if (lastModule != null) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
                if (last != null && last.token().id() == JavaTokenId.COMMA) {
                    addModuleNames(env, null, true);
                }
                return;
            }
        }
        Tree name = exp.getPackageName();
        if (name != null) {
            int extPos = (int) sourcePositions.getEndPosition(root, name);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, extPos + 1, offset);
                if (last != null && last.token().id() == JavaTokenId.TO) {
                    addModuleNames(env, null, true);
                } else {
                    addKeyword(env, TO_KEYWORD, SPACE, false);
                }
                return;
            }
        }
        addPackages(env, null, true);
    }

    private void insideOpens(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        OpensTree op = (OpensTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (op.getModuleNames() != null) {
            int startPos = (int) sourcePositions.getStartPosition(root, op);
            Tree lastModule = null;
            for (Tree mdl : op.getModuleNames()) {
                int implPos = (int) sourcePositions.getEndPosition(root, mdl);
                if (implPos == Diagnostic.NOPOS || offset <= implPos) {
                    break;
                }
                lastModule = mdl;
                startPos = implPos;
            }
            if (lastModule != null) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
                if (last != null && last.token().id() == JavaTokenId.COMMA) {
                    addModuleNames(env, null, true);
                }
                return;
            }
        }
        Tree name = op.getPackageName();
        if (name != null) {
            int extPos = (int) sourcePositions.getEndPosition(root, name);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, extPos + 1, offset);
                if (last != null && last.token().id() == JavaTokenId.TO) {
                    addModuleNames(env, null, true);
                } else {
                    addKeyword(env, TO_KEYWORD, SPACE, false);
                }
                return;
            }
        }
        addPackages(env, null, true);
    }

    private void insideProvides(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        ProvidesTree prov = (ProvidesTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (prov.getImplementationNames() != null) {
            int startPos = (int) sourcePositions.getStartPosition(root, prov);
            Tree lastImpl = null;
            for (Tree impl : prov.getImplementationNames()) {
                int implPos = (int) sourcePositions.getEndPosition(root, impl);
                if (implPos == Diagnostic.NOPOS || offset <= implPos) {
                    break;
                }
                lastImpl = impl;
                startPos = implPos;
            }
            if (lastImpl != null) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
                if (last != null && last.token().id() != JavaTokenId.COMMA) {
                    return;
                }
            }
        }
        Tree serv = prov.getServiceName();
        if (serv != null) {
            int extPos = (int) sourcePositions.getEndPosition(root, serv);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, extPos + 1, offset);
                if (last != null && last.token().id() == JavaTokenId.WITH) {
                    CompilationController cc = env.getController();
                    cc.toPhase(Phase.RESOLVED);
                    Element el = cc.getTrees().getElement(new TreePath(path, serv));
                    options.add(Options.ALL_COMPLETION);
                    addTypes(env, EnumSet.of(CLASS), el != null && el.getKind().isInterface() ? (DeclaredType)el.asType() : null);
                } else {
                    addKeyword(env, WITH_KEYWORD, SPACE, false);
                }
                return;
            }
        }
        options.add(Options.ALL_COMPLETION);
        addTypes(env, EnumSet.of(ANNOTATION_TYPE, CLASS, INTERFACE), null);
    }

    private void insideRequires(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        RequiresTree req = (RequiresTree) path.getLeaf();
        Tree name = req.getModuleName();
        if (name != null) {
            int extPos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), name);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                return;
            }
        }
        if (!req.isStatic()) {
            addKeyword(env, STATIC_KEYWORD, SPACE, false);
        }
        if (!req.isTransitive()) {
            addKeyword(env, TRANSITIVE_KEYWORD, SPACE, false);
        }
        addModuleNames(env, null, false);
    }

    private void insideUses(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        UsesTree uses = (UsesTree) path.getLeaf();
        Tree name = uses.getServiceName();
        if (name != null) {
            int extPos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), name);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                return;
            }
        }
        options.add(Options.ALL_COMPLETION);
        addTypes(env, EnumSet.of(ANNOTATION_TYPE, CLASS, INTERFACE), null);
    }

    private void insidePackage(Env env) {
        int offset = env.getOffset();
        PackageTree pt = (PackageTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        if (offset <= sourcePositions.getStartPosition(env.getRoot(), pt.getPackageName())) {
            addPackages(env, null, true);
        }
    }

    private void insideImport(Env env) throws IOException {
        env.getController().toPhase(Phase.ELEMENTS_RESOLVED);
        int offset = env.getOffset();
        String prefix = env.getPrefix();
        ImportTree im = (ImportTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (offset <= sourcePositions.getStartPosition(root, im.getQualifiedIdentifier())) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, im, offset);
            if (last != null && last.token().id() == JavaTokenId.IMPORT && Utilities.startsWith(STATIC_KEYWORD, prefix)) {
                addKeyword(env, STATIC_KEYWORD, SPACE, false);
            }
            if (options.contains(Options.ALL_COMPLETION) || options.contains(Options.COMBINED_COMPLETION)) {
                EnumSet<ElementKind> classKinds = EnumSet.of(CLASS, INTERFACE, ENUM, ANNOTATION_TYPE);
                if (isRecordSupported(env)) {
                    classKinds.add(RECORD);
                }
                addTypes(env, classKinds, null);
            } else {
                addPackages(env, null, false);
            }
        }
    }

    private void insideClass(Env env) throws IOException {
        int offset = env.getOffset();
        env.insideClass();
        TreePath path = env.getPath();
        ClassTree cls = (ClassTree) path.getLeaf();
        CompilationController controller = env.getController();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int startPos = (int) sourcePositions.getEndPosition(root, cls.getModifiers());
        if (startPos <= 0) {
            startPos = (int) sourcePositions.getStartPosition(root, cls);
        }
        String headerText = controller.getText().substring(startPos, offset);
        int idx = headerText.indexOf('{'); //NOI18N
        if (idx >= 0) {
            addKeywordsForClassBody(env);
            addClassTypes(env, null);
            addElementCreators(env);
            return;
        }
        TreeUtilities tu = controller.getTreeUtilities();
        Tree lastPerm = null;
        List<? extends Tree> permits = cls.getPermitsClause();
        permits = permits == null ? new ArrayList<>() : permits;
        for (Tree perm : permits) {
            int permPos = (int) sourcePositions.getEndPosition(root, perm);
            if (permPos == Diagnostic.NOPOS || offset <= permPos) {
                break;
            }
            lastPerm = perm;
            startPos = permPos;
        }
        if (lastPerm != null) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
            if (last != null && last.token().id() == JavaTokenId.COMMA) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                env.addToExcludes(controller.getTrees().getElement(path));
                addClassTypes(env, null);
            }
            return;
        }
        Tree lastImpl = null;
        for (Tree impl : cls.getImplementsClause()) {
            int implPos = (int) sourcePositions.getEndPosition(root, impl);
            if (implPos == Diagnostic.NOPOS || offset <= implPos) {
                break;
            }
            lastImpl = impl;
            startPos = implPos;
        }
        if (lastImpl != null) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
            if (last != null && last.token().id() == JavaTokenId.COMMA) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                env.addToExcludes(controller.getTrees().getElement(path));
                addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
            } else if (isSealedSupported(env) && last != null && TokenUtilities.textEquals(last.token().text(),PERMITS_KEYWORD)) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                env.addToExcludes(controller.getTrees().getElement(path));
                addClassTypes(env, null);
            } else if (isSealedSupported(env)) {
                addKeyword(env, PERMITS_KEYWORD, SPACE, false);
            }
            return;
        }
        Tree ext = cls.getExtendsClause();
        if (ext != null) {
            int extPos = (int) sourcePositions.getEndPosition(root, ext);
            if (extPos != Diagnostic.NOPOS && offset > extPos) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, extPos + 1, offset);
                if (last != null && last.token().id() == JavaTokenId.IMPLEMENTS) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                } else if (isSealedSupported(env) && last != null && TokenUtilities.textEquals(last.token().text(),PERMITS_KEYWORD)) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addClassTypes(env, null);
                } else {
                    addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                    if (isSealedSupported(env)) {
                        addKeyword(env, PERMITS_KEYWORD, SPACE, false);
                    }
                }
                return;
            }
        }
        TypeParameterTree lastTypeParam = null;
        for (TypeParameterTree tp : cls.getTypeParameters()) {
            int tpPos = (int) sourcePositions.getEndPosition(root, tp);
            if (tpPos == Diagnostic.NOPOS || offset <= tpPos) {
                break;
            }
            lastTypeParam = tp;
            startPos = tpPos;
        }
        if (lastTypeParam != null) {
            TokenSequence<JavaTokenId> first = findFirstNonWhitespaceToken(env, startPos, offset);
            if (first != null && (first.token().id() == JavaTokenId.GT
                    || first.token().id() == JavaTokenId.GTGT
                    || first.token().id() == JavaTokenId.GTGTGT)) {
                first = nextNonWhitespaceToken(first);
                if (first != null && first.offset() < offset) {
                    if (first.token().id() == JavaTokenId.EXTENDS) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.afterExtends();
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addTypes(env, tu.isInterface(cls) ? EnumSet.of(INTERFACE, ANNOTATION_TYPE) : EnumSet.of(CLASS), null);
                        return;
                    }
                    if (first.token().id() == JavaTokenId.IMPLEMENTS) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                        return;
                    }
                }
                if (!tu.isAnnotation(cls)) {
                    if (!tu.isEnum(cls)) {
                        addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                        if (isSealedSupported(env)) {
                            addKeyword(env, PERMITS_KEYWORD, SPACE, false);
                        }
                    }
                    if (!tu.isInterface(cls)) {
                        addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                    }
                }
            } else {
                if (lastTypeParam.getBounds().isEmpty()) {
                    addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                }
            }
            return;
        }
        TokenSequence<JavaTokenId> lastNonWhitespaceToken = findLastNonWhitespaceToken(env, startPos, offset);
        if (lastNonWhitespaceToken != null) {
            switch (lastNonWhitespaceToken.token().id()) {
                case EXTENDS:
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.afterExtends();
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addTypes(env, tu.isInterface(cls) ? EnumSet.of(INTERFACE, ANNOTATION_TYPE) : EnumSet.of(CLASS), null);
                    break;
                case IMPLEMENTS:
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                    break;
                case IDENTIFIER:
                    if (isSealedSupported(env) && TokenUtilities.textEquals(lastNonWhitespaceToken.token().text(),PERMITS_KEYWORD)) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addClassTypes(env, null);
                        break;
                    }
                    if (!tu.isAnnotation(cls)) {
                        if (!tu.isEnum(cls)) {
                            addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                            if (isSealedSupported(env)) {
                                addKeyword(env, PERMITS_KEYWORD, SPACE, false);
                            }
                        }
                        if (!tu.isInterface(cls)) {
                            addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                        }
                    }
                    break;
            }
            return;
        }
        lastNonWhitespaceToken = findLastNonWhitespaceToken(env, (int) sourcePositions.getStartPosition(root, cls), offset);
        if (lastNonWhitespaceToken != null && lastNonWhitespaceToken.token().id() == JavaTokenId.AT) {
            addKeyword(env, INTERFACE_KEYWORD, SPACE, false);
            addTypes(env, EnumSet.of(ANNOTATION_TYPE), null);
        } else if (path.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            addClassModifiers(env, cls.getModifiers().getFlags());
        } else {
            addMemberModifiers(env, cls.getModifiers().getFlags(), false);
            addClassTypes(env, null);
        }
    }

    private void insideVariable(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        VariableTree var = (VariableTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        CompilationController controller = env.getController();
        Tree type = var.getType();
        int typePos = type.getKind() == Tree.Kind.ERRONEOUS && ((ErroneousTree) type).getErrorTrees().isEmpty()
                ? (int) sourcePositions.getEndPosition(root, type) : (int) sourcePositions.getStartPosition(root, type);
        if (offset <= typePos) {
            Tree parent = path.getParentPath().getLeaf();
            if (parent.getKind() == Tree.Kind.CATCH) {
                if (!options.contains(Options.ALL_COMPLETION)) {
                    TreeUtilities tu = controller.getTreeUtilities();
                    TreePath tryPath = tu.getPathElementOfKind(Tree.Kind.TRY, path);
                    Set<TypeMirror> exs = tu.getUncaughtExceptions(tryPath);
                    Elements elements = controller.getElements();
                    for (TypeMirror ex : exs) {
                        if (ex.getKind() == TypeKind.DECLARED && startsWith(env, ((DeclaredType) ex).asElement().getSimpleName().toString())
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(((DeclaredType) ex).asElement()))
                                && !Utilities.isExcluded(((TypeElement)((DeclaredType) ex).asElement()).getQualifiedName())) {
                            env.addToExcludes(((DeclaredType) ex).asElement());
                            results.add(itemFactory.createTypeItem(controller, (TypeElement) ((DeclaredType) ex).asElement(), (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(((DeclaredType) ex).asElement()), false, false, false, true, false));
                        }
                    }
                }
                TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                if (te != null) {
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
                }
            } else if (parent.getKind() == Tree.Kind.TRY) {
                TypeElement te = controller.getElements().getTypeElement("java.lang.AutoCloseable"); //NOI18N
                if (te != null) {
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
                }
            } else {
                if (path.getParentPath().getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                    LambdaExpressionTree let = (LambdaExpressionTree) path.getParentPath().getLeaf();

                    if (let.getParameters().size() == 1) {
                        addVarTypeForLambdaParam(env);
                    } else if (isLambdaVarType(env, let)) {
                        addVarTypeForLambdaParam(env);
                        return;
                    }
                }
                boolean isLocal = !TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind());
                addMemberModifiers(env, var.getModifiers().getFlags(), isLocal);
                addClassTypes(env, null);
                ModifiersTree mods = var.getModifiers();
                if (mods.getFlags().isEmpty() && mods.getAnnotations().isEmpty()) {
                    addElementCreators(env);
                }
            }
            return;
        }
        controller.toPhase(Phase.RESOLVED);
        Tree init = unwrapErrTree(var.getInitializer());
        if (init == null) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, (int) sourcePositions.getEndPosition(root, type), offset);
            if (last == null || last.token().id() == JavaTokenId.COMMA) {
                insideExpression(env, new TreePath(path, type));
            } else if (last.token().id() == JavaTokenId.EQ) {
                localResult(env);
                addValueKeywords(env);
            }
        } else {
            int pos = (int) sourcePositions.getStartPosition(root, init);
            if (pos < 0) {
                return;
            }
            if (offset <= pos) {
                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, (int) sourcePositions.getEndPosition(root, type), offset);
                if (last == null) {
                    insideExpression(env, new TreePath(path, type));
                } else if (last.token().id() == JavaTokenId.EQ) {
                    localResult(env);
                    addValueKeywords(env);
                }
            } else {
                insideExpression(env, new TreePath(path, init));
            }
        }
    }

    private void insideMethod(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        MethodTree mth = (MethodTree) path.getLeaf();
        CompilationController controller = env.getController();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int startPos = (int) sourcePositions.getStartPosition(root, mth);
        Tree lastTree = null;
        int state = 0;
        for (Tree thr : mth.getThrows()) {
            int thrPos = (int) sourcePositions.getEndPosition(root, thr);
            if (thrPos == Diagnostic.NOPOS || offset <= thrPos) {
                break;
            }
            lastTree = thr;
            startPos = thrPos;
            state = 4;
        }
        if (lastTree == null) {
            for (VariableTree param : mth.getParameters()) {
                int parPos = (int) sourcePositions.getEndPosition(root, param);
                if (parPos == Diagnostic.NOPOS || offset <= parPos) {
                    break;
                }
                lastTree = param;
                startPos = parPos;
                state = 3;
            }
        }
        if (lastTree == null) {
            Tree retType = mth.getReturnType();
            if (retType != null) {
                int retPos = (int) sourcePositions.getEndPosition(root, retType);
                if (retPos != Diagnostic.NOPOS && offset > retPos) {
                    lastTree = retType;
                    startPos = retPos;
                    state = 2;
                }
            }
        }
        if (lastTree == null) {
            for (TypeParameterTree tp : mth.getTypeParameters()) {
                int tpPos = (int) sourcePositions.getEndPosition(root, tp);
                if (tpPos == Diagnostic.NOPOS || offset <= tpPos) {
                    break;
                }
                lastTree = tp;
                startPos = tpPos;
                state = 1;
            }
        }
        if (lastTree == null) {
            Tree mods = mth.getModifiers();
            if (mods != null) {
                int modsPos = (int) sourcePositions.getEndPosition(root, mods);
                if (modsPos != Diagnostic.NOPOS && offset > modsPos) {
                    lastTree = mods;
                    startPos = modsPos;
                }
            }
        }
        TokenSequence<JavaTokenId> lastToken = findLastNonWhitespaceToken(env, startPos, offset);
        if (lastToken != null) {
            switch (lastToken.token().id()) {
                case LPAREN:
                    addMemberModifiers(env, Collections.<Modifier>emptySet(), true);
                    addClassTypes(env, null);
                    break;
                case RPAREN:
                    Tree mthParent = path.getParentPath().getLeaf();
                    switch (mthParent.getKind()) {
                        case ANNOTATION_TYPE:
                            addKeyword(env, DEFAULT_KEYWORD, SPACE, false);
                            break;
                        default:
                            addKeyword(env, THROWS_KEYWORD, SPACE, false);
                    }
                    break;
                case THROWS:
                    if (!options.contains(Options.ALL_COMPLETION) && mth.getBody() != null) {
                        controller.toPhase(Phase.RESOLVED);
                        Set<TypeMirror> exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, mth.getBody()));
                        Elements elements = controller.getElements();
                        for (TypeMirror ex : exs) {
                            if (ex.getKind() == TypeKind.DECLARED && startsWith(env, ((DeclaredType) ex).asElement().getSimpleName().toString())
                                    && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(((DeclaredType) ex).asElement()))
                                    && !Utilities.isExcluded(((TypeElement)((DeclaredType) ex).asElement()).getQualifiedName())) {
                                env.addToExcludes(((DeclaredType) ex).asElement());
                                results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) ((DeclaredType) ex).asElement(), (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(((DeclaredType) ex).asElement()), false, false, false, true, false));
                            }
                        }
                    }
                    TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                    if (te != null) {
                        addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
                    }
                    break;
                case DEFAULT:
                    addLocalConstantsAndTypes(env);
                    break;
                case GT:
                case GTGT:
                case GTGTGT:
                    addPrimitiveTypeKeywords(env);
                    addKeyword(env, VOID_KEYWORD, SPACE, false);
                    addClassTypes(env, null);
                    break;
                case COMMA:
                    switch (state) {
                        case 3:
                            addMemberModifiers(env, Collections.<Modifier>emptySet(), true);
                            addClassTypes(env, null);
                            break;
                        case 4:
                            if (!options.contains(Options.ALL_COMPLETION) && mth.getBody() != null) {
                                controller.toPhase(Phase.RESOLVED);
                                Set<TypeMirror> exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, mth.getBody()));
                                Trees trees = controller.getTrees();
                                Types types = controller.getTypes();
                                for (ExpressionTree thr : mth.getThrows()) {
                                    TypeMirror t = trees.getTypeMirror(new TreePath(path, thr));
                                    for (Iterator<TypeMirror> it = exs.iterator(); it.hasNext();) {
                                        if (types.isSubtype(it.next(), t)) {
                                            it.remove();
                                        }
                                    }
                                    if (thr == lastTree) {
                                        break;
                                    }
                                }
                                Elements elements = controller.getElements();
                                for (TypeMirror ex : exs) {
                                    if (ex.getKind() == TypeKind.DECLARED && startsWith(env, ((DeclaredType) ex).asElement().getSimpleName().toString())
                                            && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(((DeclaredType) ex).asElement()))
                                            && !Utilities.isExcluded(((TypeElement)((DeclaredType) ex).asElement()).getQualifiedName())) {
                                        env.addToExcludes(((DeclaredType) ex).asElement());
                                        results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) ((DeclaredType) ex).asElement(), (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(((DeclaredType) ex).asElement()), false, false, false, true, false));
                                    }
                                }
                            }
                            te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                            if (te != null) {
                                addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
                            }
                            break;
                    }
                    break;
            }
            return;
        }
        switch (state) {
            case 0:
                addMemberModifiers(env, mth.getModifiers().getFlags(), false);
                addClassTypes(env, null);
                break;
            case 1:
                if (((TypeParameterTree) lastTree).getBounds().isEmpty()) {
                    addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                }
                break;
            case 2:
                insideExpression(env, new TreePath(path, lastTree));
                break;
        }
    }

    private void insideModifiers(Env env, TreePath modPath) throws IOException {
        int offset = env.getOffset();
        ModifiersTree mods = (ModifiersTree) modPath.getLeaf();
        Set<Modifier> m = EnumSet.noneOf(Modifier.class);
        TokenSequence<JavaTokenId> ts = env.getController().getTreeUtilities().tokensFor(mods, env.getSourcePositions());
        JavaTokenId lastNonWhitespaceTokenId = null;
        while (ts.moveNext() && ts.offset() < offset) {
            lastNonWhitespaceTokenId = ts.token().id();
            switch (lastNonWhitespaceTokenId) {
                case PUBLIC:
                    m.add(PUBLIC);
                    break;
                case PROTECTED:
                    m.add(PROTECTED);
                    break;
                case PRIVATE:
                    m.add(PRIVATE);
                    break;
                case STATIC:
                    m.add(STATIC);
                    break;
                case DEFAULT:
                    m.add(DEFAULT);
                    break;
                case ABSTRACT:
                    m.add(ABSTRACT);
                    break;
                case FINAL:
                    m.add(FINAL);
                    break;
                case SYNCHRONIZED:
                    m.add(SYNCHRONIZED);
                    break;
                case NATIVE:
                    m.add(NATIVE);
                    break;
                case STRICTFP:
                    m.add(STRICTFP);
                    break;
                case TRANSIENT:
                    m.add(TRANSIENT);
                    break;
                case VOLATILE:
                    m.add(VOLATILE);
                    break;
            }
        }
        if (lastNonWhitespaceTokenId == JavaTokenId.AT) {
            addKeyword(env, INTERFACE_KEYWORD, SPACE, false);
            addTypes(env, EnumSet.of(ANNOTATION_TYPE), null);
            return;
        }
        TreePath parentPath = modPath.getParentPath();
        Tree parent = parentPath.getLeaf();
        TreePath grandParentPath = parentPath.getParentPath();
        Tree grandParent = grandParentPath != null ? grandParentPath.getLeaf() : null;
        if (isTopLevelClass(parent, env.getRoot())) {
            addClassModifiers(env, m);
        } else if (parent.getKind() != Tree.Kind.VARIABLE || grandParent == null || TreeUtilities.CLASS_TREE_KINDS.contains(grandParent.getKind())) {
            addMemberModifiers(env, m, false);
            addClassTypes(env, null);
        } else if (parent.getKind() == Tree.Kind.VARIABLE && grandParent.getKind() == Tree.Kind.METHOD) {
            addMemberModifiers(env, m, true);
            addClassTypes(env, null);
        } else {
            localResult(env);
            addKeywordsForBlock(env);
        }
    }

    private void insideAnnotation(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        AnnotationTree ann = (AnnotationTree) path.getLeaf();
        CompilationController controller = env.getController();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int typeEndPos = (int) sourcePositions.getEndPosition(root, ann.getAnnotationType());
        if (offset <= typeEndPos) {
            TreePath parentPath = path.getParentPath();
            if (parentPath.getLeaf().getKind() == Tree.Kind.MODIFIERS
                    && (parentPath.getParentPath().getLeaf().getKind() != Tree.Kind.VARIABLE
                    || parentPath.getParentPath().getParentPath().getLeaf().getKind() == Tree.Kind.CLASS)) {
                addKeyword(env, INTERFACE_KEYWORD, SPACE, false);
            }
            if (!options.contains(Options.ALL_COMPLETION)) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                Set<? extends TypeMirror> smarts = getSmartTypes(env);
                if (smarts != null) {
                    Elements elements = controller.getElements();
                    for (TypeMirror smart : smarts) {
                        if (smart.getKind() == TypeKind.DECLARED) {
                            TypeElement elem = (TypeElement) ((DeclaredType) smart).asElement();
                            if (elem.getKind() == ANNOTATION_TYPE && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                results.add(itemFactory.createTypeItem(env.getController(), elem, (DeclaredType) smart, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), false, false, false, true, false));
                            }
                        }
                    }
                }
            }
            addTypes(env, EnumSet.of(ANNOTATION_TYPE), null);
            return;
        }
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, ann, offset);
        if (ts == null || (ts.token().id() != JavaTokenId.LPAREN && ts.token().id() != JavaTokenId.COMMA)) {
            return;
        }
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        Trees trees = controller.getTrees();
        Element annTypeElement = trees.getElement(new TreePath(path, ann.getAnnotationType()));
        if (annTypeElement != null && annTypeElement.getKind() == ANNOTATION_TYPE) {
            HashSet<String> names = new HashSet<>();
            for (ExpressionTree arg : ann.getArguments()) {
                if (arg.getKind() == Tree.Kind.ASSIGNMENT && sourcePositions.getEndPosition(root, ((AssignmentTree) arg).getExpression()) < offset) {
                    ExpressionTree var = ((AssignmentTree) arg).getVariable();
                    if (var.getKind() == Tree.Kind.IDENTIFIER) {
                        names.add(((IdentifierTree) var).getName().toString());
                    }
                }
            }
            Elements elements = controller.getElements();
            ExecutableElement valueElement = null;
            for (Element e : ((TypeElement) annTypeElement).getEnclosedElements()) {
                if (e.getKind() == METHOD) {
                    String name = e.getSimpleName().toString();
                    if ("value".equals(name)) { //NOI18N
                        valueElement = (ExecutableElement) e;
                    } else if (((ExecutableElement) e).getDefaultValue() == null) {
                        valueElement = null;
                    }
                    if (!names.contains(name) && startsWith(env, name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))) {
                        results.add(itemFactory.createAttributeItem(env.getController(), (ExecutableElement) e, (ExecutableType) e.asType(), anchorOffset, elements.isDeprecated(e)));
                    }
                }
            }
            if (valueElement != null && names.isEmpty()) {
                Element el = null;
                TreePath pPath = path.getParentPath();
                if (pPath.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                    el = trees.getElement(pPath);
                } else {
                    pPath = pPath.getParentPath();
                    Tree.Kind pKind = pPath.getLeaf().getKind();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(pKind) || pKind == Tree.Kind.METHOD || pKind == Tree.Kind.VARIABLE) {
                        el = trees.getElement(pPath);
                    }
                }
                if (el != null) {
                    AnnotationMirror annotation = null;
                    for (AnnotationMirror am : el.getAnnotationMirrors()) {
                        if (annTypeElement == am.getAnnotationType().asElement()) {
                            annotation = am;
                            break;
                        }
                    }
                    if (annotation != null) {
                        addAttributeValues(env, el, annotation, valueElement);
                    }
                }
                addLocalConstantsAndTypes(env);
            }
        }
    }

    private void insideAnnotatedType(Env env) throws IOException {
        int offset = env.getOffset();
        AnnotatedTypeTree att = (AnnotatedTypeTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int pos = (int) sourcePositions.getStartPosition(root, att.getUnderlyingType());
        if (pos >= 0 && pos < offset) {
            insideExpression(env, new TreePath(env.getPath(), att.getUnderlyingType()));
        } else {
            addClassTypes(env, null);
        }
    }

    private void insideAnnotationAttribute(Env env, TreePath annotationPath, Name attributeName) throws IOException {
        CompilationController controller = env.getController();
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        Trees trees = controller.getTrees();
        AnnotationTree at = (AnnotationTree) annotationPath.getLeaf();
        Element annTypeElement = trees.getElement(new TreePath(annotationPath, at.getAnnotationType()));
        Element el = null;
        TreePath pPath = annotationPath.getParentPath();
        if (pPath.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            el = trees.getElement(pPath);
        } else {
            pPath = pPath.getParentPath();
            Tree.Kind pKind = pPath.getLeaf().getKind();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(pKind) || pKind == Tree.Kind.METHOD || pKind == Tree.Kind.VARIABLE) {
                el = trees.getElement(pPath);
            }
        }
        if (el != null && annTypeElement != null && annTypeElement.getKind() == ANNOTATION_TYPE) {
            ExecutableElement memberElement = null;
            for (Element e : ((TypeElement) annTypeElement).getEnclosedElements()) {
                if (e.getKind() == METHOD && attributeName.contentEquals(e.getSimpleName())) {
                    memberElement = (ExecutableElement) e;
                    break;
                }
            }
            if (memberElement != null) {
                AnnotationMirror annotation = null;
                for (AnnotationMirror am : el.getAnnotationMirrors()) {
                    if (annTypeElement == am.getAnnotationType().asElement()) {
                        annotation = am;
                        break;
                    }
                }
                if (annotation != null) {
                    addAttributeValues(env, el, annotation, memberElement);
                }
            }
        }
    }

    private void insideTypeParameter(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        TypeParameterTree tp = (TypeParameterTree) path.getLeaf();
        CompilationController controller = env.getController();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, tp, offset);
        if (ts != null) {
            switch (ts.token().id()) {
                case EXTENDS:
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    addTypes(env, EnumSet.of(CLASS, INTERFACE, ANNOTATION_TYPE), null);
                    break;
                case AMP:
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                    break;
                case IDENTIFIER:
                    if (ts.offset() == env.getSourcePositions().getStartPosition(env.getRoot(), tp)) {
                        addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                    }
                    break;
            }
        }
    }

    private void insideParameterizedType(Env env, TreePath ptPath) throws IOException {
        int offset = env.getOffset();
        ParameterizedTypeTree ta = (ParameterizedTypeTree) ptPath.getLeaf();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, ta, offset);
        if (ts != null) {
            switch (ts.token().id()) {
                case EXTENDS:
                case SUPER:
                case LT:
                case COMMA:
                    if (!options.contains(Options.ALL_COMPLETION)) {
                        CompilationController controller = env.getController();
                        SourcePositions sourcePositions = env.getSourcePositions();
                        CompilationUnitTree root = env.getRoot();
                        int index = 0;
                        for (Tree arg : ta.getTypeArguments()) {
                            int parPos = (int) sourcePositions.getEndPosition(root, arg);
                            if (parPos == Diagnostic.NOPOS || offset <= parPos) {
                                break;
                            }
                            index++;
                        }
                        Elements elements = controller.getElements();
                        Types types = controller.getTypes();
                        TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(ptPath, ta.getType()));
                        List<? extends TypeMirror> bounds = null;
                        if (tm.getKind() == TypeKind.DECLARED) {
                            TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
                            List<? extends TypeParameterElement> typeParams = te.getTypeParameters();
                            if (index < typeParams.size()) {
                                TypeParameterElement typeParam = typeParams.get(index);
                                bounds = typeParam.getBounds();
                            }
                        }
                        Set<? extends TypeMirror> smarts = getSmartTypes(env);
                        if (smarts != null) {
                            for (TypeMirror smart : smarts) {
                                if (smart != null) {
                                    if (smart.getKind() == TypeKind.DECLARED && types.isSubtype(tm, types.erasure(smart))) {
                                        List<? extends TypeMirror> typeArgs = ((DeclaredType) smart).getTypeArguments();
                                        if (index < typeArgs.size()) {
                                            TypeMirror lowerBound = typeArgs.get(index);
                                            TypeMirror upperBound = null;
                                            if (lowerBound.getKind() == TypeKind.WILDCARD) {
                                                upperBound = ((WildcardType) lowerBound).getSuperBound();
                                                lowerBound = ((WildcardType) lowerBound).getExtendsBound();
                                            }
                                            if (lowerBound != null && lowerBound.getKind() == TypeKind.TYPEVAR) {
                                                lowerBound = ((TypeVariable) lowerBound).getUpperBound();
                                            }
                                            if (upperBound != null && upperBound.getKind() == TypeKind.TYPEVAR) {
                                                upperBound = ((TypeVariable) upperBound).getUpperBound();
                                            }
                                            if (upperBound != null && upperBound.getKind() == TypeKind.DECLARED) {
                                                while (upperBound.getKind() == TypeKind.DECLARED) {
                                                    TypeElement elem = (TypeElement) ((DeclaredType) upperBound).asElement();
                                                    if (startsWith(env, elem.getSimpleName().toString()) && withinBounds(env, upperBound, bounds) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                                        results.add(itemFactory.createTypeItem(env.getController(), elem, (DeclaredType) upperBound, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), false, true, false, true, false));
                                                    }
                                                    env.addToExcludes(elem);
                                                    upperBound = elem.getSuperclass();
                                                }
                                            } else if (lowerBound != null && lowerBound.getKind() == TypeKind.DECLARED) {
                                                for (DeclaredType subtype : getSubtypesOf(env, (DeclaredType) lowerBound)) {
                                                    TypeElement elem = (TypeElement) subtype.asElement();
                                                    if (withinBounds(env, subtype, bounds) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                                        results.add(itemFactory.createTypeItem(env.getController(), elem, subtype, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), false, true, false, true, false));
                                                    }
                                                    env.addToExcludes(elem);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (bounds != null && !bounds.isEmpty()) {
                            TypeMirror lowerBound = bounds.get(0);
                            bounds = bounds.subList(0, bounds.size());
                            for (DeclaredType subtype : getSubtypesOf(env, (DeclaredType) lowerBound)) {
                                TypeElement elem = (TypeElement) subtype.asElement();
                                if (withinBounds(env, subtype, bounds) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                    results.add(itemFactory.createTypeItem(env.getController(), elem, subtype, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), false, true, false, true, false));
                                }
                                env.addToExcludes(elem);
                            }
                        }
                    }
                    addClassTypes(env, null);
                    break;
                case QUESTION:
                    addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                    addKeyword(env, SUPER_KEYWORD, SPACE, false);
                    break;
            }
        }
    }

    private void insideBlock(Env env) throws IOException {
        int offset = env.getOffset();
        BlockTree bl = (BlockTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int blockPos = (int) sourcePositions.getStartPosition(root, bl);
        String text = env.getController().getText().substring(blockPos, offset);
        if (text.indexOf('{') < 0) { //NOI18N
            addMemberModifiers(env, Collections.singleton(STATIC), false);
            addClassTypes(env, null);
            return;
        }
        StatementTree last = null;
        for (StatementTree stat : bl.getStatements()) {
            int pos = (int) sourcePositions.getStartPosition(root, stat);
            if (pos == Diagnostic.NOPOS || offset <= pos) {
                break;
            }
            last = stat;
        }
        if (last == null) {
            ExecutableElement enclMethod = env.getScope().getEnclosingMethod();
            if (enclMethod != null && enclMethod.getKind() == ElementKind.CONSTRUCTOR) {
                String prefix = env.getPrefix();
                if (Utilities.startsWith(THIS_KEYWORD, prefix)) {
                    Element element = enclMethod.getEnclosingElement();
                    addThisOrSuperConstructor(env, element.asType(), element, THIS_KEYWORD, enclMethod);
                }
                if (Utilities.startsWith(SUPER_KEYWORD, prefix)) {
                    Element element = enclMethod.getEnclosingElement();
                    element = ((DeclaredType) ((TypeElement) element).getSuperclass()).asElement();
                    addThisOrSuperConstructor(env, element.asType(), element, SUPER_KEYWORD, enclMethod);
                }
            }
        } else if (last.getKind() == Tree.Kind.TRY) {
            if (((TryTree) last).getFinallyBlock() == null) {
                addKeyword(env, CATCH_KEYWORD, null, true);
                addKeyword(env, FINALLY_KEYWORD, null, true);
                if (((TryTree) last).getCatches().isEmpty() && ((TryTree) last).getResources().isEmpty()) {
                    return;
                }
            }
        } else if (last.getKind() == Tree.Kind.IF) {
            if (((IfTree) last).getElseStatement() == null) {
                addKeyword(env, ELSE_KEYWORD, null, true);
            }
        }
        localResult(env);
        addKeywordsForBlock(env);
    }

    @SuppressWarnings("fallthrough")
    private void insideMemberSelect(Env env) throws IOException {
        int offset = env.getOffset();
        String prefix = env.getPrefix();
        TreePath path = env.getPath();
        MemberSelectTree fa = (MemberSelectTree) path.getLeaf();
        CompilationController controller = env.getController();
        CompilationUnitTree root = env.getRoot();
        SourcePositions sourcePositions = env.getSourcePositions();
        int expEndPos = (int) sourcePositions.getEndPosition(root, fa.getExpression());
        boolean afterDot = false;
        boolean afterLt = false;
        int openLtNum = 0;
        JavaTokenId lastNonWhitespaceTokenId = null;
        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(expEndPos);
        while (ts.moveNext()) {
            if (ts.offset() >= offset) {
                break;
            }
            switch (ts.token().id()) {
                case DOUBLE_LITERAL:
                case FLOAT_LITERAL:
                case FLOAT_LITERAL_INVALID:
                case LONG_LITERAL:
                case ELLIPSIS:
                    if (ts.offset() != expEndPos || ts.token().text().charAt(0) != '.') {
                        break;
                    }
                case DOT:
                    afterDot = true;
                    break;
                case LT:
                    afterLt = true;
                    openLtNum++;
                    break;
                case GT:
                    openLtNum--;
                    break;
                case GTGT:
                    openLtNum -= 2;
                    break;
                case GTGTGT:
                    openLtNum -= 3;
                    break;
            }
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                case JAVADOC_COMMENT_LINE_RUN:
                    break;
                default:
                    lastNonWhitespaceTokenId = ts.token().id();
            }
        }
        if (!afterDot) {
            if (expEndPos <= offset) {
                insideExpression(env, new TreePath(path, fa.getExpression()));
            }
            return;
        }
        if (openLtNum > 0) {
            switch (lastNonWhitespaceTokenId) {
                case QUESTION:
                    addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
                    addKeyword(env, SUPER_KEYWORD, SPACE, false);
                    break;
                case LT:
                case COMMA:
                    addClassTypes(env, null);
                    break;
                case EXTENDS:
                case SUPER:
                    addClassTypes(env, null);
                    break;
            }
        } else if (lastNonWhitespaceTokenId != JavaTokenId.STAR) {
            controller.toPhase(Phase.RESOLVED);
            if (withinModuleName(env)) {
                String fqnPrefix = fa.getExpression().toString() + '.';
                anchorOffset = (int) sourcePositions.getStartPosition(root, fa);
                addModuleNames(env, fqnPrefix, true);
                return;
            }
            TreePath parentPath = path.getParentPath();
            Tree parent = parentPath != null ? parentPath.getLeaf() : null;
            TreePath grandParentPath = parentPath != null ? parentPath.getParentPath() : null;
            Tree grandParent = grandParentPath != null ? grandParentPath.getLeaf() : null;
            ExpressionTree exp = fa.getExpression();
            TreePath expPath = new TreePath(path, exp);
            TypeMirror type = controller.getTrees().getTypeMirror(expPath);
            TypeMirror tempSwitchSelectorType;
            AddSwitchRelatedItem switchItemAdder = addSwitchItemDefault;
            AddSwitchRelatedItem tempSwitchItemAdder;
            if (type != null) {
                Element el = controller.getTrees().getElement(expPath);
                TreeUtilities tu = controller.getTreeUtilities();
                EnumSet<ElementKind> kinds;
                DeclaredType baseType = null;
                Set<TypeMirror> exs = null;
                boolean inImport = false;
                boolean insideNew = false;
                boolean srcOnly = false;
                if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && ((ClassTree) parent).getExtendsClause() == fa) {
                    kinds = EnumSet.of(CLASS);
                    env.afterExtends();
                } else if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && ((ClassTree) parent).getImplementsClause().contains(fa)) {
                    kinds = EnumSet.of(INTERFACE);
                } else if (parent.getKind() == Kind.PACKAGE) {
                    kinds = EnumSet.noneOf(ElementKind.class);
                    srcOnly = true;
                } else if (parent.getKind() == Tree.Kind.IMPORT) {
                    inImport = true;
                    kinds = ((ImportTree) parent).isStatic() ? EnumSet.of(CLASS, ENUM, INTERFACE, ANNOTATION_TYPE, RECORD, FIELD, METHOD, ENUM_CONSTANT, RECORD_COMPONENT) : EnumSet.of(CLASS, ANNOTATION_TYPE, ENUM, INTERFACE, RECORD);
                } else if (parent.getKind() == Tree.Kind.NEW_CLASS && ((NewClassTree) parent).getIdentifier() == fa) {
                    insideNew = true;
                    kinds = EnumSet.of(CLASS, INTERFACE, ANNOTATION_TYPE, RECORD);
                    if (grandParent.getKind() == Tree.Kind.THROW) {
                        TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                        if (te != null) {
                            baseType = controller.getTypes().getDeclaredType(te);
                        }
                    }
                } else if (parent.getKind() == Tree.Kind.PARAMETERIZED_TYPE && ((ParameterizedTypeTree) parent).getTypeArguments().contains(fa)) {
                    kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE);
                } else if (parent.getKind() == Tree.Kind.ANNOTATION || parent.getKind() == Tree.Kind.TYPE_ANNOTATION) {
                    if (((AnnotationTree) parent).getAnnotationType() == fa) {
                        kinds = EnumSet.of(ANNOTATION_TYPE);
                    } else {
                        Iterator<? extends ExpressionTree> it = ((AnnotationTree) parent).getArguments().iterator();
                        if (it.hasNext()) {
                            ExpressionTree et = it.next();
                            if (et == fa || (et.getKind() == Tree.Kind.ASSIGNMENT && ((AssignmentTree) et).getExpression() == fa)) {
                                if (type.getKind() == TypeKind.ERROR && el.getKind().isClass()) {
                                    el = controller.getElements().getPackageElement(((TypeElement) el).getQualifiedName());
                                }
                                if (el instanceof PackageElement) {
                                    addPackageContent(env, (PackageElement) el, EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE), null, false, false);
                                } else if (type.getKind() == TypeKind.DECLARED) {
                                    addMemberConstantsAndTypes(env, (DeclaredType) type, el);
                                }
                                return;
                            }
                        }
                        kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD, FIELD, METHOD, ENUM_CONSTANT, RECORD_COMPONENT);
                    }
                } else if (parent.getKind() == Tree.Kind.ASSIGNMENT && ((AssignmentTree) parent).getExpression() == fa && grandParent != null && grandParent.getKind() == Tree.Kind.ANNOTATION) {
                    if (type.getKind() == TypeKind.ERROR && el.getKind().isClass()) {
                        el = controller.getElements().getPackageElement(((TypeElement) el).getQualifiedName());
                    }
                    if (el instanceof PackageElement) {
                        addPackageContent(env, (PackageElement) el, EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD), null, false, false);
                    } else if (type.getKind() == TypeKind.DECLARED) {
                        addMemberConstantsAndTypes(env, (DeclaredType) type, el);
                    }
                    return;
                } else if (parent.getKind() == Tree.Kind.VARIABLE && ((VariableTree) parent).getType() == fa) {
                    if (grandParent.getKind() == Tree.Kind.CATCH) {
                        kinds = EnumSet.of(CLASS, INTERFACE);
                        if (!options.contains(Options.ALL_COMPLETION)) {
                            exs = controller.getTreeUtilities().getUncaughtExceptions(grandParentPath.getParentPath());
                        }
                        TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                        if (te != null) {
                            baseType = controller.getTypes().getDeclaredType(te);
                        }
                    } else {
                        kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD);
                    }
                } else if (parent.getKind() == Tree.Kind.METHOD && ((MethodTree) parent).getThrows().contains(fa)) {
                    Types types = controller.getTypes();
                    if (!options.contains(Options.ALL_COMPLETION) && ((MethodTree) parent).getBody() != null) {
                        controller.toPhase(Phase.RESOLVED);
                        exs = controller.getTreeUtilities().getUncaughtExceptions(new TreePath(path, ((MethodTree) parent).getBody()));
                        Trees trees = controller.getTrees();
                        for (ExpressionTree thr : ((MethodTree) parent).getThrows()) {
                            if (sourcePositions.getEndPosition(root, thr) >= offset) {
                                break;
                            }
                            TypeMirror t = trees.getTypeMirror(new TreePath(path, thr));
                            for (Iterator<TypeMirror> it = exs.iterator(); it.hasNext();) {
                                if (types.isSubtype(it.next(), t)) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    kinds = EnumSet.of(CLASS, INTERFACE);
                    TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                    if (te != null) {
                        baseType = controller.getTypes().getDeclaredType(te);
                    }
                } else if (parent.getKind() == Tree.Kind.METHOD && ((MethodTree) parent).getDefaultValue() == fa) {
                    if (type.getKind() == TypeKind.ERROR && el.getKind().isClass()) {
                        el = controller.getElements().getPackageElement(((TypeElement) el).getQualifiedName());
                    }
                    if (el instanceof PackageElement) {
                        addPackageContent(env, (PackageElement) el, EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD), null, false, false);
                    } else if (type.getKind() == TypeKind.DECLARED) {
                        addMemberConstantsAndTypes(env, (DeclaredType) type, el);
                    }
                    return;
                } else if (parent.getKind() == Tree.Kind.TYPE_PARAMETER) {
                    TypeParameterTree tpt = (TypeParameterTree) parent;
                    Trees trees = controller.getTrees();
                    boolean first = true;
                    for (Tree bound : tpt.getBounds()) {
                        int pos = (int) sourcePositions.getEndPosition(root, bound);
                        if (offset <= pos) {
                            break;
                        }
                        first = false;
                        env.addToExcludes(trees.getElement(new TreePath(parentPath, bound)));
                    }
                    kinds = first ? EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE) : EnumSet.of(ANNOTATION_TYPE, INTERFACE);
                } else if (parent.getKind() == Tree.Kind.AND) {
                    TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(path, ((BinaryTree) parent).getLeftOperand()));
                    if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                        env.addToExcludes(((DeclaredType) tm).asElement());
                        kinds = EnumSet.of(INTERFACE, ANNOTATION_TYPE);
                    } else if (tm != null && tm.getKind() == TypeKind.INTERSECTION) {
                        for (TypeMirror bound : ((IntersectionType) tm).getBounds()) {
                            if (bound.getKind() == TypeKind.DECLARED) {
                                env.addToExcludes(((DeclaredType) bound).asElement());
                            }
                        }
                        kinds = EnumSet.of(INTERFACE, ANNOTATION_TYPE);
                    } else {
                        kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD, FIELD, METHOD, ENUM_CONSTANT, RECORD_COMPONENT);
                    }
                } else if (afterLt) {
                    kinds = EnumSet.of(METHOD);
                } else if (parent.getKind() == Tree.Kind.ENHANCED_FOR_LOOP && ((EnhancedForLoopTree) parent).getExpression() == fa) {
                    env.insideForEachExpression();
                    kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD, FIELD, METHOD, ENUM_CONSTANT, RECORD_COMPONENT);
                } else if (tu.getPathElementOfKind(Tree.Kind.EXPORTS, path) != null) {
                    kinds = EnumSet.noneOf(ElementKind.class);
                    srcOnly = true;
                } else if (tu.getPathElementOfKind(Tree.Kind.PROVIDES, path) != null) {                    
                    kinds = withinProvidesService(env) ? EnumSet.of(ANNOTATION_TYPE, CLASS, INTERFACE) : EnumSet.of(CLASS);
                } else if (tu.getPathElementOfKind(Tree.Kind.USES, path) != null) {
                    kinds = EnumSet.of(ANNOTATION_TYPE, CLASS, INTERFACE);
                } else if (parent.getKind() == Kind.CONSTANT_CASE_LABEL &&
                           grandParent != null &&
                           grandParent.getKind() == Kind.CASE &&
                           (tempSwitchSelectorType = getSwitchSelectorType(env, grandParentPath.getParentPath())) != null &&
                           tempSwitchSelectorType.getKind() == TypeKind.DECLARED &&
                           (tempSwitchItemAdder = itemAdderForSwitchOrNull(env, grandParentPath.getParentPath())) != null) {
                    kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD, ENUM_CONSTANT);
                    baseType = (DeclaredType) tempSwitchSelectorType;
                    switchItemAdder = tempSwitchItemAdder;
                } else {
                    kinds = EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, RECORD, FIELD, METHOD, ENUM_CONSTANT, RECORD_COMPONENT);
                }
                switch (type.getKind()) {
                    case TYPEVAR:
                        while (type != null && type.getKind() == TypeKind.TYPEVAR) {
                            type = ((TypeVariable) type).getUpperBound();
                        }
                        if (type == null) {
                            return;
                        }
                        type = controller.getTypes().capture(type);
                    case ARRAY:
                    case DECLARED:
                    case UNION:
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case DOUBLE:
                    case FLOAT:
                    case INT:
                    case LONG:
                    case SHORT:
                    case VOID:
                        boolean b = exp.getKind() == Tree.Kind.PARENTHESIZED || exp.getKind() == Tree.Kind.TYPE_CAST;
                        while (b) {
                            if (exp.getKind() == Tree.Kind.PARENTHESIZED) {
                                exp = ((ParenthesizedTree) exp).getExpression();
                                expPath = new TreePath(expPath, exp);
                            } else if (exp.getKind() == Tree.Kind.TYPE_CAST) {
                                exp = ((TypeCastTree) exp).getExpression();
                                expPath = new TreePath(expPath, exp);
                            } else {
                                b = false;
                            }
                        }
                        el = controller.getTrees().getElement(expPath);
                        if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            if (parent.getKind() == Tree.Kind.NEW_CLASS && ((NewClassTree) parent).getIdentifier() == fa && prefix != null) {
                                String typeName = controller.getElementUtilities().getElementName(el, true) + "." + prefix; //NOI18N
                                TypeMirror tm = controller.getTreeUtilities().parseType(typeName, env.getScope().getEnclosingClass());
                                if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                                    addMembers(env, tm, ((DeclaredType) tm).asElement(), EnumSet.of(CONSTRUCTOR), null, inImport, insideNew, false, false, switchItemAdder);
                                }
                            }
                        }
                        if (exs != null && !exs.isEmpty()) {
                            Elements elements = controller.getElements();
                            for (TypeMirror ex : exs) {
                                if (ex.getKind() == TypeKind.DECLARED) {
                                    Element e = ((DeclaredType) ex).asElement();
                                    if (e.getEnclosingElement() == el && startsWith(env, e.getSimpleName().toString()) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && !Utilities.isExcluded(((TypeElement)e).getQualifiedName())) {
                                        env.addToExcludes(e);
                                        results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) e, (DeclaredType) ex, anchorOffset, null, elements.isDeprecated(e), insideNew, insideNew || env.isInsideClass(), true, true, false));
                                    }
                                }
                            }
                        } else {
                            if (el == null) {
                                if (exp.getKind() == Tree.Kind.ARRAY_TYPE) {
                                    TypeMirror tm = type;
                                    while (tm.getKind() == TypeKind.ARRAY) {
                                        tm = ((ArrayType) tm).getComponentType();
                                    }
                                    if (tm.getKind().isPrimitive()) {
                                        el = controller.getTypes().boxedClass((PrimitiveType) tm);
                                    } else if (tm.getKind() == TypeKind.DECLARED) {
                                        el = ((DeclaredType) tm).asElement();
                                    }
                                } else if (exp.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                                    if (type.getKind().isPrimitive()) {
                                        el = controller.getTypes().boxedClass((PrimitiveType) type);
                                    } else if (type.getKind() == TypeKind.VOID) {
                                        el = controller.getElements().getTypeElement("java.lang.Void"); //NOI18N
                                    }
                                }
                            }
                            addMembers(env, type, el, kinds, baseType, inImport, insideNew, false, false, switchItemAdder);
                        }
                        break;
                    default:
                        el = controller.getTrees().getElement(expPath);
                        if (type.getKind() == TypeKind.ERROR && el != null && el.getKind().isClass()) {
                            el = controller.getElements().getPackageElement(((TypeElement) el).getQualifiedName());
                        }
                        if (el != null && el.getKind() == PACKAGE) {
                            if (parent.getKind() == Tree.Kind.NEW_CLASS && ((NewClassTree) parent).getIdentifier() == fa && prefix != null) {
                                String typeName = controller.getElementUtilities().getElementName(el, true) + "." + prefix; //NOI18N
                                TypeMirror tm = controller.getTreeUtilities().parseType(typeName, env.getScope().getEnclosingClass());
                                if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                                    addMembers(env, tm, ((DeclaredType) tm).asElement(), EnumSet.of(CONSTRUCTOR), null, inImport, insideNew, false, false, switchItemAdder);
                                }
                            }
                            if (exs != null && !exs.isEmpty()) {
                                Elements elements = controller.getElements();
                                for (TypeMirror ex : exs) {
                                    if (ex.getKind() == TypeKind.DECLARED) {
                                        Element e = ((DeclaredType) ex).asElement();
                                        if (e.getEnclosingElement() == el && startsWith(env, e.getSimpleName().toString()) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && !Utilities.isExcluded(((TypeElement)e).getQualifiedName())) {
                                            env.addToExcludes(e);
                                            switchItemAdder.addTypeItem(env.getController(), (TypeElement) e, (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(e), false, env.isInsideClass(), true, true, false);
                                        }
                                    }
                                }
                            }
                            addPackageContent(env, (PackageElement) el, kinds, baseType, insideNew, srcOnly, switchItemAdder);
                            if (results.isEmpty() && ((PackageElement) el).getQualifiedName() == el.getSimpleName()) {
                                // no package content? Check for unimported class
                                ClassIndex ci = controller.getClasspathInfo().getClassIndex();
                                if (el.getEnclosedElements().isEmpty() && ci.getPackageNames(el.getSimpleName() + ".", true, EnumSet.allOf(ClassIndex.SearchScope.class)).isEmpty()) {
                                    Trees trees = controller.getTrees();
                                    Scope scope = env.getScope();
                                    for (ElementHandle<TypeElement> teHandle : ci.getDeclaredTypes(el.getSimpleName().toString(), ClassIndex.NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                                        TypeElement te = teHandle.resolve(controller);
                                        if (te != null && trees.isAccessible(scope, te)) {
                                            addMembers(env, te.asType(), te, kinds, baseType, inImport, insideNew, true, false, switchItemAdder);
                                        }
                                    }
                                }
                            }
                        }
                }
            } else if (parent.getKind() == Tree.Kind.COMPILATION_UNIT && ((CompilationUnitTree) parent).getPackageName() == fa) {
                PackageElement pe = controller.getElements().getPackageElement(fullName(exp));
                if (pe != null) {
                    addPackageContent(env, pe, EnumSet.of(ElementKind.PACKAGE), null, false, true);
                }
            }
        }
    }

    private TypeMirror getSwitchSelectorType(Env env, TreePath swtch) {
        TreePath selector;
        Tree leaf = swtch.getLeaf();

        switch (leaf.getKind()) {
            case SWITCH -> selector = new TreePath(swtch, ((SwitchTree) leaf).getExpression());
            case SWITCH_EXPRESSION -> selector = new TreePath(swtch, ((SwitchExpressionTree) leaf).getExpression());
            default -> {
                return null;
            }
        }

        return env.getController().getTrees().getTypeMirror(selector);
    }

    private void insideMemberReference(Env env) throws IOException {
        TreePath path = env.getPath();
        MemberReferenceTree mr = (MemberReferenceTree) path.getLeaf();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, mr, env.getOffset());
        if (ts != null) {
            switch (ts.token().id()) {
                case COLONCOLON:
                case GT:
                case GTGT:
                case GTGTGT:
                    CompilationController controller = env.getController();
                    ExpressionTree exp = mr.getQualifierExpression();
                    TreePath expPath = new TreePath(path, exp);
                    Trees trees = controller.getTrees();
                    TypeMirror type = trees.getTypeMirror(expPath);
                    if (type != null && type.getKind() == TypeKind.TYPEVAR) {
                        while (type != null && type.getKind() == TypeKind.TYPEVAR) {
                            type = ((TypeVariable) type).getUpperBound();
                        }
                        if (type != null) {
                            type = controller.getTypes().capture(type);
                        }
                    }
                    if (type != null && (type.getKind() == TypeKind.DECLARED
                            || type.getKind() == TypeKind.ARRAY || type.getKind() == TypeKind.TYPEVAR)) {
                        Element e = trees.getElement(expPath);
                        addMethodReferences(env, type, e);
                        if (e == null || e.getKind().isClass() || e.getKind().isInterface()) {
                            addKeyword(env, NEW_KEYWORD, SPACE, false);
                        }
                    }
                    break;
                case LT:
                case COMMA:
                    addClassTypes(env, null);
                    break;
            }
        }
    }

    private void insideLambdaExpression(Env env) throws IOException {
        TreePath path = env.getPath();
        LambdaExpressionTree let = (LambdaExpressionTree) path.getLeaf();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, let, env.getOffset());
        if (ts != null) {
            switch (ts.token().id()) {
                case ARROW -> {
                    localResult(env);
                    addValueKeywords(env);
                }
                case COMMA -> {
                    if (let.getParameters().isEmpty()
                            || !env.getController().getTreeUtilities().isSynthetic(new TreePath(path, let.getParameters().get(0).getType()))) {
                        addClassTypes(env, null);
                        addPrimitiveTypeKeywords(env);
                        addKeyword(env, FINAL_KEYWORD, SPACE, false);
                    } else {
                        boolean isFirstParamVarType = isLambdaVarType(env, let);
                        if (isFirstParamVarType) {
                            addVarTypeForLambdaParam(env);
                        }
                    }
                }
            }
        }
    }

    private void insideMethodInvocation(Env env) throws IOException {
        TreePath path = env.getPath();
        MethodInvocationTree mi = (MethodInvocationTree) path.getLeaf();
        String prefix = env.getPrefix();
        int offset = env.getOffset();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, mi, offset);
        if (path.getParentPath().getLeaf().getKind() == Kind.CONSTANT_CASE_LABEL) {
            CompilationController controller = env.getController();
            controller.toPhase(Phase.RESOLVED);
            TypeMirror tm = controller.getTreeUtilities().parseType(fullName(mi.getMethodSelect()), env.getScope().getEnclosingClass());
            if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
                if (te.getKind() == RECORD) {
                    List<? extends RecordComponentElement> recordComponents = te.getRecordComponents();
                    int size = mi.getArguments().size();
                    if (size <= recordComponents.size()) {
                        TypeMirror componentType = recordComponents.get(size - 1).getAccessor().getReturnType();
                        if (ts != null && (ts.token().id() == JavaTokenId.LPAREN || ts.token().id() == JavaTokenId.COMMA)) {
                            if (componentType.getKind() == TypeKind.DECLARED) {
                                if (prefix != null) {
                                    TypeMirror ptm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                                    if (ptm != null && ptm.getKind() == TypeKind.DECLARED) {
                                        TypeElement pte = (TypeElement) ((DeclaredType) ptm).asElement();
                                        if (pte != null && pte.getKind() == RECORD) {
                                            results.add(((RecordPatternItemFactory<T>) itemFactory).createRecordPatternItem(controller, pte, (DeclaredType) ptm, anchorOffset, null, controller.getElements().isDeprecated(pte), env.isInsideNew(), env.isInsideNew() || env.isInsideClass()));
                                            env.addToExcludes(pte);
                                        }
                                    }
                                }
                                addClassTypes(env, (DeclaredType) componentType);
                            }
                            addKeyword(env, VAR_KEYWORD, SPACE, false);
                        } else {
                            final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                            Scope scope = env.getScope();
                            final ExecutableElement method = scope.getEnclosingMethod();
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                @Override
                                public boolean accept(Element e, TypeMirror t) {
                                    return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                            || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                            && !illegalForwardRefs.containsKey(e.getSimpleName());
                                }
                            };
                            for (String name : Utilities.varNamesSuggestions(componentType, LOCAL_VARIABLE, EnumSet.noneOf(Modifier.class), null, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                                results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
                            }
                        }
                    }
                    return;
                }
            }
        }
        if (ts == null || (ts.token().id() != JavaTokenId.LPAREN && ts.token().id() != JavaTokenId.COMMA)) {
            SourcePositions sp = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            int lastTokenEndOffset = ts != null ? ts.offset() + ts.token().length() : -1;
            for (ExpressionTree arg : mi.getArguments()) {
                int pos = (int) sp.getEndPosition(root, arg);
                if (lastTokenEndOffset == pos) {
                    insideExpression(env, new TreePath(path, arg));
                    break;
                }
                if (offset <= pos) {
                    break;
                }
            }
            return;
        }
        if (prefix == null || prefix.length() == 0) {
            addMethodArguments(env, mi);
        }
        addLocalMembersAndVars(env);
        addValueKeywords(env);
        addClassTypes(env,null);
        addPrimitiveTypeKeywords(env);
    }

    private void insideNewClass(Env env) throws IOException {
        TreePath path = env.getPath();
        NewClassTree nc = (NewClassTree) path.getLeaf();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, nc, env.getOffset());
        if (ts != null) {
            switch (ts.token().id()) {
                case NEW:
                    String prefix = env.getPrefix();
                    CompilationController controller = env.getController();
                    controller.toPhase(Phase.RESOLVED);
                    TypeElement tel = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
                    DeclaredType base = path.getParentPath().getLeaf().getKind() == Tree.Kind.THROW && tel != null
                            ? controller.getTypes().getDeclaredType(tel) : null;
                    TypeElement toExclude = null;
                    if (nc.getIdentifier().getKind() == Tree.Kind.IDENTIFIER && prefix != null) {
                        TypeMirror tm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                        if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                            TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
                            addMembers(env, tm, te, EnumSet.of(CONSTRUCTOR), base, false, true, false);
                            if ((te.getTypeParameters().isEmpty() || SourceVersion.RELEASE_5.compareTo(controller.getSourceVersion()) > 0)
                                    && !hasAccessibleInnerClassConstructor(te, env.getScope(), controller.getTrees())) {
                                toExclude = te;
                            }
                        }
                    }
                    boolean insideNew = true;
                    ExpressionTree encl = nc.getEnclosingExpression();
                    if (!options.contains(Options.ALL_COMPLETION)) {
                        Set<? extends TypeMirror> smarts = getSmartTypes(env);
                        if (smarts != null) {
                            Elements elements = env.getController().getElements();
                            for (TypeMirror smart : smarts) {
                                if (smart != null) {
                                    if (smart.getKind() == TypeKind.DECLARED) {
                                        if (encl == null) {
                                            for (DeclaredType subtype : getSubtypesOf(env, (DeclaredType) smart)) {
                                                TypeElement elem = (TypeElement) subtype.asElement();
                                                if (toExclude != elem && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                                    results.add(itemFactory.createTypeItem(env.getController(), elem, (DeclaredType) SourceUtils.resolveCapturedType(controller, subtype), anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), true, true, false, true, false));
                                                }
                                                env.addToExcludes(elem);
                                            }
                                        }
                                    } else if (smart.getKind() == TypeKind.ARRAY) {
                                        insideNew = false;
                                        try {
                                            TypeMirror tm = smart;
                                            while (tm.getKind() == TypeKind.ARRAY) {
                                                tm = ((ArrayType) tm).getComponentType();
                                            }
                                            if (tm.getKind().isPrimitive() && startsWith(env, tm.toString())) {
                                                results.add(itemFactory.createArrayItem(env.getController(), (ArrayType) smart, anchorOffset, env.getReferencesCount(), env.getController().getElements()));
                                            } else if ((tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) && startsWith(env, ((DeclaredType) tm).asElement().getSimpleName().toString())) {
                                                results.add(itemFactory.createArrayItem(env.getController(), (ArrayType) smart, anchorOffset, env.getReferencesCount(), env.getController().getElements()));
                                            }
                                        } catch (IllegalArgumentException iae) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (toExclude != null) {
                        env.addToExcludes(toExclude);
                    }
                    if (insideNew) {
                        env.insideNew();
                    }
                    if (encl == null) {
                        EnumSet<ElementKind> classKinds = EnumSet.of(CLASS, INTERFACE, ENUM, ANNOTATION_TYPE);
                        if (isRecordSupported(env)) {
                            classKinds.add(RECORD);
                        }
                        addTypes(env, classKinds, base);
                    } else {
                        TypeMirror enclType = controller.getTrees().getTypeMirror(new TreePath(path, nc.getEnclosingExpression()));
                        if (enclType != null && enclType.getKind() == TypeKind.DECLARED) {
                            addMembers(env, enclType, ((DeclaredType) enclType).asElement(), EnumSet.of(CLASS, INTERFACE, ENUM, ANNOTATION_TYPE), base, false, insideNew, false);
                        }
                    }
                    break;
                case LPAREN:
                case COMMA:
                case RPAREN:
                    prefix = env.getPrefix();
                    if (prefix == null || prefix.length() == 0) {
                        addConstructorArguments(env, nc);
                    }
                    addLocalMembersAndVars(env);
                    addValueKeywords(env);
                    addClassTypes(env,null);
                    addPrimitiveTypeKeywords(env);
                    break;
                case GT:
                case GTGT:
                case GTGTGT:
                    controller = env.getController();
                    TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(path, nc.getIdentifier()));
                    addMembers(env, tm, ((DeclaredType) tm).asElement(), EnumSet.of(CONSTRUCTOR), null, false, false, false, true, addSwitchItemDefault);
                    break;
            }
        }
    }

    private void insideTry(Env env) throws IOException {
        CompilationController controller = env.getController();
        TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, env.getPath().getLeaf(), env.getOffset());
        if (last != null && (last.token().id() == JavaTokenId.LPAREN || last.token().id() == JavaTokenId.SEMICOLON)) {
            addKeyword(env, FINAL_KEYWORD, SPACE, false);
            if (controller.getSourceVersion().compareTo(SourceVersion.RELEASE_9) >= 0) {
                addEffectivelyFinalAutoCloseables(env);
            }
            TypeElement te = controller.getElements().getTypeElement("java.lang.AutoCloseable"); //NOI18N
            if (te != null) {
                addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
            }
        }
    }

    private void insideCatch(Env env) throws IOException {
        TreePath path = env.getPath();
        CatchTree ct = (CatchTree) path.getLeaf();
        CompilationController controller = env.getController();
        TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, ct, env.getOffset());
        if (last != null && last.token().id() == JavaTokenId.LPAREN) {
            addKeyword(env, FINAL_KEYWORD, SPACE, false);
            if (!options.contains(Options.ALL_COMPLETION)) {
                TreeUtilities tu = controller.getTreeUtilities();
                TreePath tryPath = tu.getPathElementOfKind(Tree.Kind.TRY, path);
                Set<TypeMirror> exs = tu.getUncaughtExceptions(tryPath != null ? tryPath : path.getParentPath());
                Elements elements = controller.getElements();
                for (TypeMirror ex : exs) {
                    if (ex.getKind() == TypeKind.DECLARED && startsWith(env, ((DeclaredType) ex).asElement().getSimpleName().toString())
                            && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(((DeclaredType) ex).asElement()))
                            && !Utilities.isExcluded(((TypeElement)((DeclaredType) ex).asElement()).getQualifiedName())) {
                        env.addToExcludes(((DeclaredType) ex).asElement());
                        results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) ((DeclaredType) ex).asElement(), (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(((DeclaredType) ex).asElement()), false, false, false, true, false));
                    }
                }
            }
            TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
            if (te != null) {
                addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
            }
        }
    }

    private void insideUnionType(Env env) throws IOException {
        TreePath path = env.getPath();
        UnionTypeTree dtt = (UnionTypeTree) path.getLeaf();
        CompilationController controller = env.getController();
        TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, dtt, env.getOffset());
        if (last != null && last.token().id() == JavaTokenId.BAR) {
            if (!options.contains(Options.ALL_COMPLETION)) {
                TreeUtilities tu = controller.getTreeUtilities();
                TreePath tryPath = tu.getPathElementOfKind(Tree.Kind.TRY, path);
                Set<TypeMirror> exs = tu.getUncaughtExceptions(tryPath);
                if (!exs.isEmpty()) {
                    Trees trees = controller.getTrees();
                    Types types = controller.getTypes();
                    for (Tree t : dtt.getTypeAlternatives()) {
                        TypeMirror tm = trees.getTypeMirror(new TreePath(path, t));
                        if (tm != null && tm.getKind() != TypeKind.ERROR) {
                            for (Iterator<TypeMirror> it = exs.iterator(); it.hasNext();) {
                                if (types.isSubtype(tm, it.next())) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    Elements elements = controller.getElements();
                    for (TypeMirror ex : exs) {
                        if (ex.getKind() == TypeKind.DECLARED && startsWith(env, ((DeclaredType) ex).asElement().getSimpleName().toString())
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(((DeclaredType) ex).asElement()))
                                && !Utilities.isExcluded(((TypeElement)((DeclaredType) ex).asElement()).getQualifiedName())) {
                            env.addToExcludes(((DeclaredType) ex).asElement());
                            results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) ((DeclaredType) ex).asElement(), (DeclaredType) ex, anchorOffset, env.getReferencesCount(), elements.isDeprecated(((DeclaredType) ex).asElement()), false, false, false, true, false));
                        }
                    }
                }
            }
            TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable"); //NOI18N
            if (te != null) {
                addTypes(env, EnumSet.of(CLASS, INTERFACE, TYPE_PARAMETER), controller.getTypes().getDeclaredType(te));
            }
        }
    }

    private void insideIf(Env env) throws IOException {
        IfTree iff = (IfTree) env.getPath().getLeaf();
        if (env.getSourcePositions().getEndPosition(env.getRoot(), iff.getCondition()) <= env.getOffset()) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, iff, env.getOffset());
            if (last != null && (last.token().id() == JavaTokenId.RPAREN || last.token().id() == JavaTokenId.ELSE)) {
                localResult(env);
                addKeywordsForStatement(env);
            }
        }
    }

    private void insideWhile(Env env) throws IOException {
        WhileLoopTree wlt = (WhileLoopTree) env.getPath().getLeaf();
        if (env.getSourcePositions().getEndPosition(env.getRoot(), wlt.getCondition()) <= env.getOffset()) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, wlt, env.getOffset());
            if (last != null && last.token().id() == JavaTokenId.RPAREN) {
                localResult(env);
                addKeywordsForStatement(env);
            }
        }
    }

    private void insideDoWhile(Env env) throws IOException {
        DoWhileLoopTree dwlt = (DoWhileLoopTree) env.getPath().getLeaf();
        if (env.getSourcePositions().getEndPosition(env.getRoot(), dwlt.getStatement()) <= env.getOffset()) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, dwlt, env.getOffset());
            if (last != null && (last.token().id() == JavaTokenId.RBRACE || last.token().id() == JavaTokenId.SEMICOLON)) {
                addKeyword(env, WHILE_KEYWORD, null, false);
            }
        }
    }

    private void insideFor(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        ForLoopTree fl = (ForLoopTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        Tree lastTree = null;
        int lastTreePos = offset;
        for (Tree update : fl.getUpdate()) {
            int pos = (int) sourcePositions.getEndPosition(root, update);
            if (pos == Diagnostic.NOPOS || offset <= pos) {
                break;
            }
            lastTree = update;
            lastTreePos = pos;
        }
        if (lastTree == null) {
            int pos = (int) sourcePositions.getEndPosition(root, fl.getCondition());
            if (pos != Diagnostic.NOPOS && pos < offset) {
                lastTree = fl.getCondition();
                lastTreePos = pos;
            }
        }
        if (lastTree == null) {
            for (Tree init : fl.getInitializer()) {
                int pos = (int) sourcePositions.getEndPosition(root, init);
                if (pos == Diagnostic.NOPOS || offset <= pos) {
                    break;
                }
                lastTree = init;
                lastTreePos = pos;
            }
        }
        if (lastTree == null) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, fl, offset);
            if (last != null && last.token().id() == JavaTokenId.LPAREN) {
                addLocalFieldsAndVars(env);
                addClassTypes(env,null);
                addPrimitiveTypeKeywords(env);
            }
        } else {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, lastTreePos, offset);
            if (last != null && last.token().id() == JavaTokenId.SEMICOLON) {
                localResult(env);
                addValueKeywords(env);
            } else if (last != null && last.token().id() == JavaTokenId.RPAREN) {
                localResult(env);
                addKeywordsForStatement(env);
            } else {
                switch (lastTree.getKind()) {
                    case VARIABLE:
                        Tree var = ((VariableTree) lastTree).getInitializer();
                        if (var != null) {
                            insideExpression(env, new TreePath(new TreePath(path, lastTree), var));
                        }
                        break;
                    case EXPRESSION_STATEMENT:
                        Tree exp = unwrapErrTree(((ExpressionStatementTree) lastTree).getExpression());
                        if (exp != null) {
                            insideExpression(env, new TreePath(new TreePath(path, lastTree), exp));
                        }
                        break;
                    default:
                        insideExpression(env, new TreePath(path, lastTree));
                }
            }
        }
    }

    private void insideForEach(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        EnhancedForLoopTree efl = (EnhancedForLoopTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (sourcePositions.getStartPosition(root, efl.getExpression()) >= offset) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, (int) sourcePositions.getEndPosition(root, efl.getVariable()), offset);
            if (last != null && last.token().id() == JavaTokenId.COLON) {
                env.insideForEachExpression();
                addKeyword(env, NEW_KEYWORD, SPACE, false);
                localResult(env);
            }
            return;
        }
        TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, (int) sourcePositions.getEndPosition(root, efl.getExpression()), offset);
        if (last != null && last.token().id() == JavaTokenId.RPAREN) {
            addKeywordsForStatement(env);
        } else {
            env.insideForEachExpression();
            addKeyword(env, NEW_KEYWORD, SPACE, false);
        }
        localResult(env);

    }

    private void insideSwitch(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        ExpressionTree exprTree = null;
        if (path.getLeaf().getKind() == Tree.Kind.SWITCH) {
            exprTree = ((SwitchTree) path.getLeaf()).getExpression();

        } else {
            exprTree = ((SwitchExpressionTree) path.getLeaf()).getExpression();
        }
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        if (sourcePositions.getStartPosition(root, exprTree) < offset) {
            CaseTree lastCase = null;
            List<? extends CaseTree> cases = path.getLeaf().getKind() == Kind.SWITCH ? ((SwitchTree) path.getLeaf()).getCases()
                                                                                     : ((SwitchExpressionTree) path.getLeaf()).getCases();
            for (CaseTree t : cases) {
                int pos = (int) sourcePositions.getStartPosition(root, t);
                if (pos == Diagnostic.NOPOS || offset <= pos) {
                    break;
                }
                lastCase = t;
            }
            if (lastCase != null) {
                List<? extends StatementTree> statements = lastCase.getStatements();
                if (statements == null) {
                    int pos = (int) sourcePositions.getStartPosition(root, lastCase.getBody());
                    if (pos != Diagnostic.NOPOS && pos < offset) {
                        addKeyword(env, CASE_KEYWORD, SPACE, false);
                        addKeyword(env, DEFAULT_KEYWORD, COLON, false);
                    }
                } else {
                    Tree last = null;
                    for (StatementTree stat : statements) {
                        int pos = (int) sourcePositions.getStartPosition(root, stat);
                        if (pos == Diagnostic.NOPOS || offset <= pos) {
                            break;
                        }
                        last = stat;
                    }
                    if (last != null) {
                        if (last.getKind() == Tree.Kind.TRY) {
                            if (((TryTree) last).getFinallyBlock() == null) {
                                addKeyword(env, CATCH_KEYWORD, null, false);
                                addKeyword(env, FINALLY_KEYWORD, null, false);
                                if (((TryTree) last).getCatches().isEmpty()) {
                                    return;
                                }
                            }
                        } else if (last.getKind() == Tree.Kind.IF) {
                            if (((IfTree) last).getElseStatement() == null) {
                                addKeyword(env, ELSE_KEYWORD, null, false);
                            }
                        }
                    }
                    localResult(env);
                    addKeywordsForBlock(env);
                }
            } else {
                TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, path.getLeaf(), offset);
                if (ts != null && ts.token().id() == JavaTokenId.LBRACE) {
                    addKeyword(env, CASE_KEYWORD, SPACE, false);
                    addKeyword(env, DEFAULT_KEYWORD, COLON, false);
                }
            }
        }
    }

    private void insideCase(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        CaseTree cst = (CaseTree) path.getLeaf();
        String prefix = env.getPrefix();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        CompilationController controller = env.getController();
        TreePath parentPath = path.getParentPath();
        CaseLabelTree firstCaseLabelTree = null;
        CaseLabelTree lastCaseLabelTree = null;
        ExpressionTree caseErroneousTree = null;
        List<? extends CaseLabelTree> caseTreeList = cst.getLabels();
        if (!caseTreeList.isEmpty()) {
            firstCaseLabelTree = caseTreeList.get(0);
            lastCaseLabelTree = caseTreeList.get(caseTreeList.size() - 1);
            if (lastCaseLabelTree != null && lastCaseLabelTree.getKind() == Tree.Kind.CONSTANT_CASE_LABEL) {
                ExpressionTree et = ((ConstantCaseLabelTree) lastCaseLabelTree).getConstantExpression();
                if (et != null && et.getKind() == Tree.Kind.ERRONEOUS) {
                    caseErroneousTree = et;
                }
            }
        }

        if (firstCaseLabelTree != null && ((sourcePositions.getStartPosition(root, firstCaseLabelTree) >= offset)
                || (caseErroneousTree != null && caseErroneousTree.getKind() == Tree.Kind.ERRONEOUS && ((ErroneousTree) caseErroneousTree).getErrorTrees().isEmpty() && sourcePositions.getEndPosition(root, caseErroneousTree) >= offset))) {
            if (firstCaseLabelTree.getKind() == Kind.CONSTANT_CASE_LABEL && ((ConstantCaseLabelTree) firstCaseLabelTree).getConstantExpression().getKind() == Kind.NULL_LITERAL) {
                addKeyword(env, DEFAULT_KEYWORD, null, false);
            } else if (firstCaseLabelTree.getKind() != Kind.DEFAULT_CASE_LABEL && (parentPath.getLeaf().getKind() == Tree.Kind.SWITCH || parentPath.getLeaf().getKind() == Kind.SWITCH_EXPRESSION)) {
                ExpressionTree exprTree;
                if (parentPath.getLeaf().getKind() == Tree.Kind.SWITCH) {
                    exprTree = ((SwitchTree) parentPath.getLeaf()).getExpression();
                } else {
                    exprTree = ((SwitchExpressionTree) parentPath.getLeaf()).getExpression();
                }
                TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(parentPath, exprTree));
                if (tm.getKind() == TypeKind.DECLARED) {
                    DeclaredType selectorDeclaredType = (DeclaredType) tm;
                    Element selectorTypeElement = selectorDeclaredType.asElement();
                    if (selectorTypeElement.getKind() == ENUM) {
                        addEnumConstants(env, (TypeElement) selectorTypeElement);
                    } else {
                        if (env.getController().getSourceVersion().compareTo(RELEASE_21) >= 0) {
                            if (prefix != null) {
                                TypeMirror ptm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                                if (ptm != null && ptm.getKind() == TypeKind.DECLARED) {
                                    TypeElement pte = (TypeElement) ((DeclaredType) ptm).asElement();
                                    if (pte != null && pte.getKind() == RECORD) {
                                        results.add(((RecordPatternItemFactory<T>) itemFactory).createRecordPatternItem(controller, pte, (DeclaredType) ptm, anchorOffset, null, controller.getElements().isDeprecated(pte), env.isInsideNew(), env.isInsideNew() || env.isInsideClass()));
                                        env.addToExcludes(pte);
                                    }
                                }
                            }
                            addCaseLabels(env, cst); //TODO: this belongs to the enum branch as well
                        }

                        AddSwitchRelatedItem addType = itemAdderForSwitchOrNull(env, parentPath);

                        if (addType == null) {
                            //if no filter can be determined, add all:
                            addLocalConstantsAndTypes(env);
                        } else {
                            EnumSet<ElementKind> allClassKind =
                                    EnumSet.of(ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                                               ElementKind.ENUM, ElementKind.INTERFACE,
                                               ElementKind.RECORD);
                            addTypes(env, allClassKind, (DeclaredType) tm, addType);
                        }
                    }
                } else {
                    addLocalConstantsAndTypes(env);
                }
            }
        } else if (lastCaseLabelTree != null && lastCaseLabelTree.getKind() == Tree.Kind.PATTERN_CASE_LABEL
                && env.getController().getSourceVersion().compareTo(RELEASE_21) >= 0 && cst.getBody() == null) {
            if (cst.getGuard() == null) {
                addKeyword(env, WHEN_KEYWORD, SPACE, false);
            } else {
                TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, cst, offset);
                if (ts != null && ts.token().id() == JavaTokenId.IDENTIFIER && WHEN_KEYWORD.contentEquals(ts.token().text())) {
                    localResult(env);
                }
            }
        } else {
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, cst, offset);
            if (ts != null && ts.token().id() == JavaTokenId.IDENTIFIER) {
                for (CaseLabelTree clt : caseTreeList) {
                    if (clt != null && clt.getKind() == Tree.Kind.CONSTANT_CASE_LABEL) {
                        ExpressionTree caseExpression = ((ConstantCaseLabelTree) clt).getConstantExpression();
                        if (caseExpression != null && caseExpression.getKind() == Tree.Kind.IDENTIFIER) {
                            TreePath tPath = new TreePath(path, caseExpression);
                            insideExpression(env, tPath);
                            return;
                        }
                    }
                }
            } else if (ts != null && ts.token().id() != JavaTokenId.NULL && ts.token().id() != JavaTokenId.DEFAULT) {
                localResult(env);
                addKeywordsForBlock(env);
            }
        }
    }

    private AddSwitchRelatedItem itemAdderForSwitchOrNull(Env env, TreePath switchPath) {
        TypeMirror selectorType = getSwitchSelectorType(env, switchPath);

        if (selectorType == null || selectorType.getKind() != TypeKind.DECLARED) {
            return null;
        }

        TypeElement jlObject = env.getController().getElements().getTypeElement(JAVA_LANG_OBJECT);
        TypeElement jlString = env.getController().getElements().getTypeElement(JAVA_LANG_STRING);
        Element selectorTypeElement = ((DeclaredType) selectorType).asElement();

        if (Objects.equals(selectorTypeElement, jlObject) ||
            Objects.equals(selectorTypeElement, jlString)) {
            //for java.lang.Object, any type can be a subtype,
            //so just use all types
            //for String, types might contain constant fields of
            //type String, give up and show all types:
            return null;
        } else {
            boolean selectorSealed = selectorTypeElement.getModifiers().contains(Modifier.SEALED);

            if (selectorSealed) {
                options.add(Options.ALL_COMPLETION);
            }

            Pair<Set<Element>, Set<TypeMirror>> alreadyUsed = computedUsedInSwitch(env, switchPath);
            Predicate<TypeMirror> checkTypeCovered = type -> {
                Types types = env.getController().getTypes();
                TypeMirror typeErasure = types.erasure(type);

                for (TypeMirror usedType : alreadyUsed.second()) {
                    if (env.getController().getTypes().isSubtype(typeErasure, usedType)) {
                        return true;
                    }
                }

                return false;
            };

            if (env.getController().getSourceVersion().compareTo(RELEASE_21) >= 0) {
                return new AddSwitchRelatedItem() {
                    @Override
                    public void addTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
                        if (!checkTypeCovered.test(type)) {
                            addSwitchItemDefault.addTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType);
                            if (elem.getKind() == ElementKind.ENUM) {
                                for (VariableElement enumConstant : ElementFilter.fieldsIn(elem.getEnclosedElements())) {
                                    if (enumConstant.getKind() != ElementKind.ENUM_CONSTANT || alreadyUsed.first().contains(enumConstant)) {
                                        continue;
                                    }
                                    //not filtering deprecated, etc., as those may be needed for exhaustiveness:
                                    results.add(itemFactory.createStaticMemberItem(info, type, enumConstant, enumConstant.asType(), false, anchorOffset, info.getElements().isDeprecated(enumConstant), false, selectorSealed));
                                }
                            }
                        }
                    }

                    @Override
                    public void addVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
                        if (!alreadyUsed.first().contains(elem)) {
                            addSwitchItemDefault.addVariableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset);
                        }
                    }
                };
            } else {
                return new AddSwitchRelatedItem() {
                    @Override
                    public void addTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
                        if (!checkTypeCovered.test(type)) {
                            addSwitchItemDefault.addTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType);
                        }
                    }

                    @Override
                    public void addVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
                        if (!alreadyUsed.first().contains(elem)) {
                            addSwitchItemDefault.addVariableItem(info, elem, type, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset);
                        }
                    }
                };
            }
        }
    }

    private void insideParens(Env env) throws IOException {
        TreePath path = env.getPath();
        ParenthesizedTree pa = (ParenthesizedTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        Tree exp = unwrapErrTree(pa.getExpression());
        if (exp == null || env.getOffset() <= sourcePositions.getStartPosition(root, exp)) {
            if (!options.contains(Options.ALL_COMPLETION) && path.getParentPath().getLeaf().getKind() != Tree.Kind.SWITCH) {
                Set<? extends TypeMirror> smarts = getSmartTypes(env);
                if (smarts != null) {
                    Elements elements = env.getController().getElements();
                    for (TypeMirror smart : smarts) {
                        if (smart != null) {
                            if (smart.getKind() == TypeKind.DECLARED) {
                                for (DeclaredType subtype : getSubtypesOf(env, (DeclaredType) smart)) {
                                    TypeElement elem = (TypeElement) subtype.asElement();
                                    if ((Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName())) {
                                        results.add(itemFactory.createTypeItem(env.getController(), elem, subtype, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), false, false, false, true, false));
                                    }
                                    env.addToExcludes(elem);
                                }
                                DeclaredType type = (DeclaredType) smart;
                                TypeElement element = (TypeElement) type.asElement();

                                if (elements.isFunctionalInterface(element)) {
                                    addVarTypeForLambdaParam(env);
                                }
                            } else if (smart.getKind() == TypeKind.ARRAY) {
                                try {
                                    TypeMirror tm = smart;
                                    while (tm.getKind() == TypeKind.ARRAY) {
                                        tm = ((ArrayType) tm).getComponentType();
                                    }
                                    if (tm.getKind().isPrimitive() && startsWith(env, tm.toString())) {
                                        results.add(itemFactory.createArrayItem(env.getController(), (ArrayType) smart, anchorOffset, env.getReferencesCount(), env.getController().getElements()));
                                    } else if ((tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) && startsWith(env, ((DeclaredType) tm).asElement().getSimpleName().toString())) {
                                        results.add(itemFactory.createArrayItem(env.getController(), (ArrayType) smart, anchorOffset, env.getReferencesCount(), env.getController().getElements()));
                                    }
                                } catch (IllegalArgumentException iae) {
                                }
                            }
                        }
                    }
                }
            }
            addLocalMembersAndVars(env);
            addClassTypes(env,null);
            addPrimitiveTypeKeywords(env);
            addValueKeywords(env);
        } else {
            insideExpression(env, new TreePath(path, exp));
        }
    }

    private void insideTypeCheck(Env env) throws IOException {
        CompilationController controller = env.getController();
        String prefix = env.getPrefix();
        InstanceOfTree iot = (InstanceOfTree) env.getPath().getLeaf();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, iot, env.getOffset());
        if (ts != null && ts.token().id() == JavaTokenId.INSTANCEOF) {
            if (prefix != null && controller.getSourceVersion().compareTo(RELEASE_21) >= 0) {
                TypeMirror tm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                    TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
                    if (te != null && te.getKind() == RECORD) {
                        results.add(((RecordPatternItemFactory<T>) itemFactory).createRecordPatternItem(controller, te, (DeclaredType) tm, anchorOffset, null, controller.getElements().isDeprecated(te), env.isInsideNew(), env.isInsideNew() || env.isInsideClass()));
                        env.addToExcludes(te);
                    }
                }
            }
            addClassTypes(env, null);
        }
    }

    private void insideArrayAccess(Env env) throws IOException {
        int offset = env.getOffset();
        ArrayAccessTree aat = (ArrayAccessTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int aaTextStart = (int) sourcePositions.getEndPosition(root, aat.getExpression());
        if (aaTextStart != Diagnostic.NOPOS) {
            Tree expr = unwrapErrTree(aat.getIndex());
            if (expr == null || offset <= (int) sourcePositions.getStartPosition(root, expr)) {
                String aatText = env.getController().getText().substring(aaTextStart, offset);
                int bPos = aatText.indexOf('['); //NOI18N
                if (bPos > -1) {
                    localResult(env);
                    addValueKeywords(env);
                }
            }
        }
    }

    private void insideNewArray(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        NewArrayTree nat = (NewArrayTree) path.getLeaf();
        if (nat.getInitializers() != null) { // UFFF!!!!
            SourcePositions sourcePositions = env.getSourcePositions();
            CompilationUnitTree root = env.getRoot();
            Tree last = null;
            int lastPos = offset;
            for (Tree init : nat.getInitializers()) {
                int pos = (int) sourcePositions.getEndPosition(root, init);
                if (pos == Diagnostic.NOPOS || offset <= pos) {
                    break;
                }
                last = init;
                lastPos = pos;
            }
            if (last != null) {
                TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, lastPos, offset);
                if (ts != null && ts.token().id() == JavaTokenId.COMMA) {
                    TreePath parentPath = path.getParentPath();
                    TreePath gparentPath = parentPath.getParentPath();
                    if (gparentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION && parentPath.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                        ExpressionTree var = ((AssignmentTree) parentPath.getLeaf()).getVariable();
                        if (var.getKind() == Tree.Kind.IDENTIFIER) {
                            insideAnnotationAttribute(env, gparentPath, ((IdentifierTree) var).getName());
                            addLocalConstantsAndTypes(env);
                        }
                    } else {
                        localResult(env);
                        addValueKeywords(env);
                    }
                }
                return;
            }
        }
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, nat, offset);
        switch (ts.token().id()) {
            case LBRACKET:
            case LBRACE:
                TreePath parentPath = path.getParentPath();
                TreePath gparentPath = parentPath.getParentPath();
                if (gparentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION && parentPath.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                    ExpressionTree var = ((AssignmentTree) parentPath.getLeaf()).getVariable();
                    if (var.getKind() == Tree.Kind.IDENTIFIER) {
                        insideAnnotationAttribute(env, gparentPath, ((IdentifierTree) var).getName());
                        addLocalConstantsAndTypes(env);
                    }
                } else {
                    localResult(env);
                    addValueKeywords(env);
                }
                break;
            case RBRACKET:
                if (nat.getDimensions().size() > 0) {
                    insideExpression(env, path);
                }
                break;
        }
    }

    private void insideAssignment(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        AssignmentTree as = (AssignmentTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int asTextStart = (int) sourcePositions.getEndPosition(root, as.getVariable());
        if (asTextStart != Diagnostic.NOPOS) {
            Tree expr = unwrapErrTree(as.getExpression());
            if (expr == null || offset <= (int) sourcePositions.getStartPosition(root, expr)) {
                CompilationController controller = env.getController();
                String asText = controller.getText().substring(asTextStart, offset);
                int eqPos = asText.indexOf('='); //NOI18N
                if (eqPos > -1) {
                    TreePath parentPath = path.getParentPath();
                    if (parentPath.getLeaf().getKind() != Tree.Kind.ANNOTATION) {
                        localResult(env);
                        addValueKeywords(env);
                    } else if (as.getVariable().getKind() == Tree.Kind.IDENTIFIER) {
                        insideAnnotationAttribute(env, parentPath, ((IdentifierTree) as.getVariable()).getName());
                        addLocalConstantsAndTypes(env);
                    }
                }
            } else {
                insideExpression(env, new TreePath(path, expr));
            }
        }
    }

    private void insideCompoundAssignment(Env env) throws IOException {
        int offset = env.getOffset();
        CompoundAssignmentTree cat = (CompoundAssignmentTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int catTextStart = (int) sourcePositions.getEndPosition(root, cat.getVariable());
        if (catTextStart != Diagnostic.NOPOS) {
            Tree expr = unwrapErrTree(cat.getExpression());
            if (expr == null || offset <= (int) sourcePositions.getStartPosition(root, expr)) {
                String catText = env.getController().getText().substring(catTextStart, offset);
                int eqPos = catText.indexOf('='); //NOI18N
                if (eqPos > -1) {
                    localResult(env);
                    addValueKeywords(env);
                }
            }
        }
    }

    private void insideStringLiteral(Env env) throws IOException {
        TreePath path = env.getPath();
        TreePath parentPath = path.getParentPath();
        TreePath grandParentPath = parentPath.getParentPath();
        if (grandParentPath != null && grandParentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION
                && parentPath.getLeaf().getKind() == Tree.Kind.ASSIGNMENT
                && ((AssignmentTree) parentPath.getLeaf()).getExpression() == path.getLeaf()) {
            ExpressionTree var = ((AssignmentTree) parentPath.getLeaf()).getVariable();
            if (var.getKind() == Tree.Kind.IDENTIFIER) {
                insideAnnotationAttribute(env, grandParentPath, ((IdentifierTree) var).getName());
            }
        }
    }

    private void insideBinaryTree(Env env) throws IOException {
        int offset = env.getOffset();
        TreePath path = env.getPath();
        BinaryTree bi = (BinaryTree) path.getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int pos = (int) sourcePositions.getEndPosition(root, bi.getRightOperand());
        if (pos != Diagnostic.NOPOS && pos < offset) {
            return;
        }
        pos = (int) sourcePositions.getEndPosition(root, bi.getLeftOperand());
        if (pos != Diagnostic.NOPOS) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, pos, offset);
            if (last != null) {
                CompilationController controller = env.getController();
                controller.toPhase(Phase.RESOLVED);
                TypeMirror tm = last.token().id() == JavaTokenId.AMP
                        ? controller.getTrees().getTypeMirror(new TreePath(path, bi.getLeftOperand())) : null;
                if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                    env.addToExcludes(((DeclaredType) tm).asElement());
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                } else if (tm != null && tm.getKind() == TypeKind.INTERSECTION) {
                    for (TypeMirror bound : ((IntersectionType) tm).getBounds()) {
                        if (bound.getKind() == TypeKind.DECLARED) {
                            env.addToExcludes(((DeclaredType) bound).asElement());
                        }
                    }
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                } else {
                    localResult(env);
                    addValueKeywords(env);
                }
            }
        }
    }

    private void insideConditionalExpression(Env env) throws IOException {
        ConditionalExpressionTree co = (ConditionalExpressionTree) env.getPath().getLeaf();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int coTextStart = (int) sourcePositions.getStartPosition(root, co);
        if (coTextStart != Diagnostic.NOPOS) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, coTextStart, env.getOffset());
            if (last != null && (last.token().id() == JavaTokenId.QUESTION || last.token().id() == JavaTokenId.COLON)) {
                localResult(env);
                addValueKeywords(env);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void insideExpressionStatement(Env env) throws IOException {
        TreePath path = env.getPath();
        ExpressionStatementTree est = (ExpressionStatementTree) path.getLeaf();
        CompilationController controller = env.getController();
        Tree t = est.getExpression();
        if (t.getKind() == Tree.Kind.ERRONEOUS) {
            Iterator<? extends Tree> it = ((ErroneousTree) t).getErrorTrees().iterator();
            if (it.hasNext()) {
                t = it.next();
            } else {
                localResult(env);
                Tree parentTree = path.getParentPath().getLeaf();
                switch (parentTree.getKind()) {
                    case FOR_LOOP:
                        if (((ForLoopTree) parentTree).getStatement() == est) {
                            addKeywordsForStatement(env);
                        } else {
                            addValueKeywords(env);
                        }
                        break;
                    case ENHANCED_FOR_LOOP:
                        if (((EnhancedForLoopTree) parentTree).getStatement() == est) {
                            addKeywordsForStatement(env);
                        } else {
                            addValueKeywords(env);
                        }
                        break;
                    case VARIABLE:
                        addValueKeywords(env);
                        break;
                    case LAMBDA_EXPRESSION:
                        addValueKeywords(env);
                        break;
                    default:
                        addKeywordsForStatement(env);
                        break;
                }
                return;
            }
        }
        TreePath tPath = new TreePath(path, t);
        if (t.getKind() == Tree.Kind.MODIFIERS) {
            insideModifiers(env, tPath);
        } else if (t.getKind() == Tree.Kind.IDENTIFIER && YIELD_KEYWORD.contentEquals(((IdentifierTree) t).getName())) {
            TreePath sPath = controller.getTreeUtilities().getPathElementOfKind(Tree.Kind.SWITCH_EXPRESSION, path);
            if (sPath != null) {
                localResult(env);
                addValueKeywords(env);
            }
        } else if (t.getKind() == Tree.Kind.MEMBER_SELECT && ERROR.contentEquals(((MemberSelectTree) t).getIdentifier())) {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            TreePath expPath = new TreePath(tPath, ((MemberSelectTree) t).getExpression());
            TypeMirror type = controller.getTrees().getTypeMirror(expPath);
            switch (type.getKind()) {
                case TYPEVAR:
                    type = ((TypeVariable) type).getUpperBound();
                    if (type == null) {
                        return;
                    }
                    type = controller.getTypes().capture(type);
                case ARRAY:
                case DECLARED:
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                case VOID:
                    addMembers(env, type, controller.getTrees().getElement(expPath), EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, FIELD, METHOD, ENUM_CONSTANT), null, false, false, false);
                    break;
                default:
                    Element el = controller.getTrees().getElement(expPath);
                    if (el instanceof PackageElement) {
                        addPackageContent(env, (PackageElement) el, EnumSet.of(CLASS, ENUM, ANNOTATION_TYPE, INTERFACE, FIELD, METHOD, ENUM_CONSTANT), null, false, false);
                    }
            }
        } else {
            insideExpression(env, tPath);
        }

    }

    private void insideExpression(Env env, TreePath exPath) throws IOException {
        int offset = env.getOffset();
        String prefix = env.getPrefix();
        Tree et = exPath.getLeaf();
        Tree parent = exPath.getParentPath().getLeaf();
        final CompilationController controller = env.getController();
        int endPos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), et);
        if (endPos != Diagnostic.NOPOS && endPos < offset) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, endPos, offset);
            if (last != null && last.token().id() != JavaTokenId.COMMA) {
                return;
            }
        }
        controller.toPhase(Phase.RESOLVED);
        ElementKind varKind = ElementKind.LOCAL_VARIABLE;
        Set<Modifier> varMods = EnumSet.noneOf(Modifier.class);
        if (parent.getKind() == Tree.Kind.VARIABLE) {
            varMods = ((VariableTree) parent).getModifiers().getFlags();
            Element varEl = controller.getTrees().getElement(exPath.getParentPath());
            if (varEl != null) {
                varKind = varEl.getKind();
            }
        }
        if (et.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            et = ((AnnotatedTypeTree) et).getUnderlyingType();
            exPath = new TreePath(exPath, et);
        }
        if (et.getKind() == Tree.Kind.INSTANCE_OF && endPos < offset && controller.getSourceVersion().compareTo(RELEASE_16) >= 0) {
            if (((InstanceOfTree) et).getPattern() == null) {
                TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(exPath, ((InstanceOfTree) et).getType()));
                final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                Scope scope = env.getScope();
                final ExecutableElement method = scope.getEnclosingMethod();
                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror t) {
                        return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                && !illegalForwardRefs.containsKey(e.getSimpleName());
                    }
                };
                for (String name : Utilities.varNamesSuggestions(tm, varKind, varMods, null, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                    results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
                }
            }
            return;
        }
        if (parent.getKind() != Tree.Kind.PARENTHESIZED
                && (et.getKind() == Tree.Kind.PRIMITIVE_TYPE || et.getKind() == Tree.Kind.ARRAY_TYPE || et.getKind() == Tree.Kind.PARAMETERIZED_TYPE)) {
            TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
            final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
            Scope scope = env.getScope();
            final ExecutableElement method = scope.getEnclosingMethod();
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                @Override
                public boolean accept(Element e, TypeMirror t) {
                    return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                            || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                            && !illegalForwardRefs.containsKey(e.getSimpleName());
                }
            };
            for (String name : Utilities.varNamesSuggestions(tm, varKind, varMods, null, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
            }
            return;
        }
        if (et.getKind() == Tree.Kind.UNION_TYPE) {
            for (Tree t : ((UnionTypeTree) et).getTypeAlternatives()) {
                et = t;
                exPath = new TreePath(exPath, t);
            }
        }
        if (et.getKind() == Tree.Kind.IDENTIFIER) {
            Element e = controller.getTrees().getElement(exPath);
            if (e == null) {
                return;
            }
            TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
            switch (simplifyElementKind(e.getKind())) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case PACKAGE:
                    if (parent.getKind() != Tree.Kind.PARENTHESIZED
                            || env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0) {
                        final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                        Scope scope = env.getScope();
                        final ExecutableElement method = scope.getEnclosingMethod();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            @Override
                            public boolean accept(Element e, TypeMirror t) {
                                return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                        || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                        && !illegalForwardRefs.containsKey(e.getSimpleName());
                            }
                        };
                        for (String name : Utilities.varNamesSuggestions(tm, varKind, varMods, null, prefix, controller.getTypes(), controller.getElements(),
                                controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                            results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
                        }
                    }
                    VariableElement ve = getFieldOrVar(env, e.getSimpleName().toString());
                    if (ve != null) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                    }
                    break;
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case FIELD:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                    if (tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR)) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                    }
                    TypeElement te = getTypeElement(env, e.getSimpleName().toString());
                    if (te != null) {
                        final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                        Scope scope = env.getScope();
                        final ExecutableElement method = scope.getEnclosingMethod();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            @Override
                            public boolean accept(Element e, TypeMirror t) {
                                return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                        || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                        && !illegalForwardRefs.containsKey(e.getSimpleName());
                            }
                        };
                        for (String name : Utilities.varNamesSuggestions(controller.getTypes().getDeclaredType(te), varKind, varMods, null, prefix, controller.getTypes(),
                                controller.getElements(), controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                            results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
                        }
                    }
                    break;
            }
            return;
        }
        Tree exp = null;
        if (et.getKind() == Tree.Kind.PARENTHESIZED) {
            exp = ((ParenthesizedTree) et).getExpression();
        } else if (et.getKind() == Tree.Kind.TYPE_CAST) {
            if (env.getSourcePositions().getEndPosition(env.getRoot(), ((TypeCastTree) et).getType()) <= offset) {
                exp = ((TypeCastTree) et).getType();
            }
        } else if (et.getKind() == Tree.Kind.ASSIGNMENT) {
            Tree t = ((AssignmentTree) et).getExpression();
            if (t.getKind() == Tree.Kind.PARENTHESIZED && env.getSourcePositions().getEndPosition(env.getRoot(), t) < offset) {
                exp = ((ParenthesizedTree) t).getExpression();
            }
        }
        if (exp != null) {
            exPath = new TreePath(exPath, exp);
            if (exp.getKind() == Tree.Kind.PRIMITIVE_TYPE || exp.getKind() == Tree.Kind.ARRAY_TYPE || exp.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                localResult(env);
                addValueKeywords(env);
                return;
            }
            Element e = controller.getTrees().getElement(exPath);
            if (e == null) {
                if (exp.getKind() == Tree.Kind.TYPE_CAST) {
                    addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                }
                return;
            }
            TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
            switch (simplifyElementKind(e.getKind())) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case PACKAGE:
                    if (exp.getKind() == Tree.Kind.IDENTIFIER) {
                        VariableElement ve = getFieldOrVar(env, e.getSimpleName().toString());
                        if (ve != null) {
                            addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                        }
                        if (ve == null || tm == null || tm.getKind() != TypeKind.ERROR) {
                            localResult(env);
                            addValueKeywords(env);
                        }
                    } else if (exp.getKind() == Tree.Kind.MEMBER_SELECT) {
                        if (tm != null && (tm.getKind() == TypeKind.ERROR || tm.getKind() == TypeKind.PACKAGE)) {
                            addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                        }
                        localResult(env);
                        addValueKeywords(env);
                    } else if (exp.getKind() == Tree.Kind.PARENTHESIZED && tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY)) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                    }
                    break;
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case FIELD:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                    if (tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR)) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                    }
                    TypeElement te = getTypeElement(env, e.getSimpleName().toString());
                    if (te != null || exp.getKind() == Tree.Kind.MEMBER_SELECT) {
                        localResult(env);
                        addValueKeywords(env);
                    }
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    if (tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR)) {
                        addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                    }
            }
            return;
        }
        Element e = controller.getTrees().getElement(exPath);
        TypeMirror tm = controller.getTrees().getTypeMirror(exPath);
        if (e == null) {
            if (tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY)) {
                addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
            }
            return;
        }
        switch (simplifyElementKind(e.getKind())) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case PACKAGE:
                final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                Scope scope = env.getScope();
                final ExecutableElement method = scope.getEnclosingMethod();
                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror t) {
                        return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                || EnumSet.of(LOCAL_VARIABLE, PARAMETER, EXCEPTION_PARAMETER, RESOURCE_VARIABLE).contains(simplifyElementKind(e.getKind())) && controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                && !illegalForwardRefs.containsKey(e.getSimpleName());
                    }
                };
                for (String name : Utilities.varNamesSuggestions(tm, varKind, varMods, null, prefix, controller.getTypes(), controller.getElements(), controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), CodeStyle.getDefault(controller.getDocument()))) {
                    results.add(itemFactory.createVariableItem(env.getController(), name, anchorOffset, true, false));
                }
                break;
            case ENUM_CONSTANT:
            case EXCEPTION_PARAMETER:
            case FIELD:
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case PARAMETER:
            case CONSTRUCTOR:
            case METHOD:
                if (tm != null && (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY || tm.getKind() == TypeKind.ERROR)) {
                    addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
                }
        }
    }

    private void insideBreakOrContinue(Env env) throws IOException {
        TreePath path = env.getPath();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, path.getLeaf(), env.getOffset());
        if (ts != null && (ts.token().id() == JavaTokenId.BREAK || ts.token().id() == JavaTokenId.CONTINUE)) {
            while (path != null) {
                if (path.getLeaf().getKind() == Tree.Kind.LABELED_STATEMENT) {
                    results.add(itemFactory.createVariableItem(env.getController(), ((LabeledStatementTree) path.getLeaf()).getLabel().toString(), anchorOffset, false, false));
                }
                path = path.getParentPath();
            }
        }
    }
    
    private void insideRecord(Env env) throws IOException {
        int offset = env.getOffset();
        env.insideClass();
        TreePath path = env.getPath();
        ClassTree cls = (ClassTree) path.getLeaf();
        CompilationController controller = env.getController();
        SourcePositions sourcePositions = env.getSourcePositions();
        CompilationUnitTree root = env.getRoot();
        int startPos = (int) sourcePositions.getEndPosition(root, cls.getModifiers());
        if (startPos <= 0) {
            startPos = (int) sourcePositions.getStartPosition(root, cls);
        }
        String headerText = controller.getText().substring(startPos, offset);
        int idx = headerText.indexOf('{'); //NOI18N
        if (idx >= 0) {
            addKeywordsForClassBody(env);
            addClassTypes(env, null);
            addElementCreators(env);
            return;
        }
        TreeUtilities tu = controller.getTreeUtilities();
        Tree lastImpl = null;
        for (Tree impl : cls.getImplementsClause()) {
            int implPos = (int) sourcePositions.getEndPosition(root, impl);
            if (implPos == Diagnostic.NOPOS || offset <= implPos) {
                break;
            }
            lastImpl = impl;
            startPos = implPos;
        }
        if (lastImpl != null) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, offset);
            if (last != null && last.token().id() == JavaTokenId.COMMA) {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                env.addToExcludes(controller.getTrees().getElement(path));
                addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
            }
            return;
        }
        List<? extends Tree> members = cls.getMembers();

        Tree lastParam = null;
        for (Tree member : members) {
            if (member.getKind() == Tree.Kind.VARIABLE) {
                ModifiersTree modifiers = ((VariableTree) member).getModifiers();
                Set<Modifier> modifierSet = modifiers.getFlags();

                if (!modifierSet.contains(Modifier.STATIC)) {
                    int paramPos = (int) sourcePositions.getEndPosition(root, member);
                    if (paramPos == Diagnostic.NOPOS || offset <= paramPos) {
                        break;
                    }
                    lastParam = member;
                    startPos = paramPos;
                }
            }

            if (lastParam != null) {
                TokenSequence<JavaTokenId> first = findFirstNonWhitespaceToken(env, startPos, offset);
                if (first != null && first.token().id() == JavaTokenId.COMMA) {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                    return;
                }
                if (first != null && first.token().id() == JavaTokenId.RPAREN) {
                    first = nextNonWhitespaceToken(first);
                    if (!tu.isInterface(cls) && first.token().id() == JavaTokenId.LBRACE) {
                        addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                    }

                }
                return;
            }

        }

        TypeParameterTree lastTypeParam = null;
        for (TypeParameterTree tp : cls.getTypeParameters()) {
            int tpPos = (int) sourcePositions.getEndPosition(root, tp);
            if (tpPos == Diagnostic.NOPOS || offset <= tpPos) {
                break;
            }
            lastTypeParam = tp;
            startPos = tpPos;
        }

        TokenSequence<JavaTokenId> lastNonWhitespaceToken = findLastNonWhitespaceToken(env, startPos, offset);
        if (lastNonWhitespaceToken != null) {
            switch (lastNonWhitespaceToken.token().id()) {
                case LPAREN:
                    addMemberModifiers(env, Collections.<Modifier>emptySet(), true);
                    addClassTypes(env, null);
                    break;
                case IMPLEMENTS:
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    env.addToExcludes(controller.getTrees().getElement(path));
                    addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                    break;
                case RPAREN:
                    if (!tu.isAnnotation(cls)) {
                        if (!tu.isInterface(cls)) {
                            addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                        }
                    }
                    break;
            }
            return;
        }

        if (lastTypeParam != null) {
            TokenSequence<JavaTokenId> first = findFirstNonWhitespaceToken(env, startPos, offset);

            if (first != null && (first.token().id() == JavaTokenId.GT
                    || first.token().id() == JavaTokenId.GTGT
                    || first.token().id() == JavaTokenId.GTGTGT)) {
                first = nextNonWhitespaceToken(first);

                TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, first.offset(), offset);
                TokenSequence<JavaTokenId> old = first;
                first = nextNonWhitespaceToken(first);
                if (last != null && first.token().id() == last.token().id()) {
                    first = nextNonWhitespaceToken(first);
                } else {
                    first = old;
                }

                if (first != null && first.offset() < offset) {
                    if (first.token().id() == JavaTokenId.EXTENDS) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.afterExtends();
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addTypes(env, tu.isInterface(cls) ? EnumSet.of(INTERFACE, ANNOTATION_TYPE) : EnumSet.of(CLASS), null);
                        return;
                    }
                    if (first.token().id() == JavaTokenId.IMPLEMENTS) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                        return;
                    }
                } else if (!tu.isAnnotation(cls)) {

                    if (!tu.isInterface(cls) && first.token().id() == JavaTokenId.LBRACE) {
                        addKeyword(env, IMPLEMENTS_KEYWORD, SPACE, false);
                    } else if (!tu.isInterface(cls) && first.token().id() == JavaTokenId.RPAREN) {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        env.addToExcludes(controller.getTrees().getElement(path));
                        addTypes(env, EnumSet.of(INTERFACE, ANNOTATION_TYPE), null);
                    }
                    return;
                }
            } else if (lastTypeParam.getBounds().isEmpty()) {
                addKeyword(env, EXTENDS_KEYWORD, SPACE, false);
            }
            return;
        }

        lastNonWhitespaceToken = findLastNonWhitespaceToken(env, (int) sourcePositions.getStartPosition(root, cls), offset);
        if (lastNonWhitespaceToken != null && lastNonWhitespaceToken.token().id() == JavaTokenId.AT) {
            addKeyword(env, INTERFACE_KEYWORD, SPACE, false);
            addTypes(env, EnumSet.of(ANNOTATION_TYPE), null);
        } else if (path.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            addClassModifiers(env, cls.getModifiers().getFlags());
        } else {
            addMemberModifiers(env, cls.getModifiers().getFlags(), false);
            addClassTypes(env, null);
        }

    }

    private void insideDeconstructionRecordPattern(final Env env) throws IOException {
        DeconstructionPatternTree dpt = (DeconstructionPatternTree) env.getPath().getLeaf();
        int offset = env.getOffset();
        String prefix = env.getPrefix();
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, dpt, offset);
        if (ts == null || (ts.token().id() != JavaTokenId.LPAREN && ts.token().id() != JavaTokenId.COMMA)) {
            return;
        }
        CompilationController controller = env.getController();
        controller.toPhase(Phase.RESOLVED);
        TypeMirror tm = controller.getTrees().getTypeMirror(env.getPath());
        if (tm != null && tm.getKind() == TypeKind.DECLARED) {
            TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
            if (te != null && te.getKind() == RECORD) {
                List<? extends RecordComponentElement> recordComponents = te.getRecordComponents();
                int size = dpt.getNestedPatterns().size();
                if (size <= recordComponents.size()) {
                    TypeMirror componentType = recordComponents.get(size - 1).getAccessor().getReturnType();
                    if (componentType.getKind() == TypeKind.DECLARED) {
                        if (prefix != null) {
                            TypeMirror ptm = controller.getTreeUtilities().parseType(prefix, env.getScope().getEnclosingClass());
                            if (ptm != null && ptm.getKind() == TypeKind.DECLARED) {
                                TypeElement pte = (TypeElement) ((DeclaredType) ptm).asElement();
                                if (pte != null && pte.getKind() == RECORD) {
                                    results.add(((RecordPatternItemFactory<T>) itemFactory).createRecordPatternItem(controller, pte, (DeclaredType) ptm, anchorOffset, null, controller.getElements().isDeprecated(pte), env.isInsideNew(), env.isInsideNew() || env.isInsideClass()));
                                    env.addToExcludes(pte);
                                }
                            }
                        }
                        addClassTypes(env, (DeclaredType) componentType);
                    }
                    addKeyword(env, VAR_KEYWORD, SPACE, false);
                }
            }
        }
    }

    private void addClassTypes(final Env env, DeclaredType baseType) throws IOException{
        EnumSet<ElementKind> classKinds = EnumSet.of(CLASS, INTERFACE, ENUM, ANNOTATION_TYPE, TYPE_PARAMETER);
        if (isRecordSupported(env)) {
            classKinds.add(RECORD);
        }
        addTypes(env, classKinds, baseType);
    }

    private boolean isRecordSupported(final Env env) {
        return env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_14) >= 0;
    }
    private boolean isSealedSupported(final Env env) {
        return env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_15) >= 0;
    }

    private void localResult(Env env) throws IOException {
        addLocalMembersAndVars(env);
        addClassTypes(env, null);
        addPrimitiveTypeKeywords(env);
    }

    private void addLocalConstantsAndTypes(final Env env) throws IOException {
        final String prefix = env.getPrefix();
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final Trees trees = controller.getTrees();
        final Scope scope = env.getScope();
        Set<? extends TypeMirror> smartTypes = null;
        boolean smartType = false;
        if (!options.contains(Options.ALL_COMPLETION)) {
            smartTypes = getSmartTypes(env);
            if (smartTypes != null) {
                for (TypeMirror st : smartTypes) {
                    if (st.getKind() == TypeKind.BOOLEAN) {
                        smartType = true;
                    }
                    if (st.getKind().isPrimitive()) {
                        st = types.boxedClass((PrimitiveType) st).asType();
                    }
                    if (st.getKind() == TypeKind.DECLARED) {
                        final DeclaredType type = (DeclaredType) st;
                        final TypeElement element = (TypeElement) type.asElement();
                        if (element.getKind() == ANNOTATION_TYPE && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(element))) {
                            results.add(itemFactory.createAnnotationItem(env.getController(), element, type, anchorOffset, env.getReferencesCount(), elements.isDeprecated(element)));
                        }
                        if (JAVA_LANG_CLASS.contentEquals(element.getQualifiedName())) {
                            addTypeDotClassMembers(env, type);
                        }
                        if (startsWith(env, element.getSimpleName().toString(), prefix)) {
                            final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                @Override
                                public boolean accept(Element e, TypeMirror t) {
                                    return (e.getKind() == ENUM_CONSTANT || e.getKind() == FIELD && ((VariableElement) e).getConstantValue() != null)
                                            && (!isStatic || e.getModifiers().contains(STATIC))
                                            && trees.isAccessible(scope, e, (DeclaredType) t)
                                            && types.isAssignable(((VariableElement) e).asType(), type);
                                }
                            };
                            for (Element ee : controller.getElementUtilities().getMembers(type, acceptor)) {
                                if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(ee)) {
                                    results.add(itemFactory.createStaticMemberItem(env.getController(), type, ee, asMemberOf(ee, type, types), false, anchorOffset, elements.isDeprecated(ee), false, true));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (env.getPath().getLeaf().getKind() != Tree.Kind.CASE) {
            if (Utilities.startsWith(FALSE_KEYWORD, prefix)) {
                results.add(itemFactory.createKeywordItem(FALSE_KEYWORD, null, anchorOffset, smartType));
            }
            if (Utilities.startsWith(TRUE_KEYWORD, prefix)) {
                results.add(itemFactory.createKeywordItem(TRUE_KEYWORD, null, anchorOffset, smartType));
            }
        }
        final TypeElement enclClass = scope.getEnclosingClass();
        for (Element e : getLocalMembersAndVars(env)) {
            switch (simplifyElementKind(e.getKind())) {
                case FIELD:
                    if (((VariableElement) e).getConstantValue() != null) {
                        TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                        results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                    }
                    break;
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case EXCEPTION_PARAMETER:
                case PARAMETER:
                case ENUM_CONSTANT:
                    if (((VariableElement) e).getConstantValue() != null) {
                        results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, e.asType(), anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, e.asType(), smartTypes), env.assignToVarPos()));
                    }
                    break;
            }
        }
        addClassTypes(env, null);
    }

    private void addLocalMembersAndVars(final Env env) throws IOException {
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final Trees trees = controller.getTrees();
        final Scope scope = env.getScope();
        Iterable<? extends Element> locals = getLocalMembersAndVars(env);
        Set<? extends TypeMirror> smartTypes = null;
        if (!options.contains(Options.ALL_COMPLETION)) {
            smartTypes = getSmartTypes(env);
            if (smartTypes != null) {
                for (TypeMirror st : smartTypes) {
                    if (st.getKind().isPrimitive()) {
                        st = types.boxedClass((PrimitiveType) st).asType();
                    }
                    if (st.getKind() == TypeKind.DECLARED) {
                        final DeclaredType type = (DeclaredType) st;
                        final TypeElement element = (TypeElement) type.asElement();
                        if (JAVA_LANG_CLASS.contentEquals(element.getQualifiedName())) {
                            addTypeDotClassMembers(env, type);
                        } else if (controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0
                                && elements.isFunctionalInterface(element) && itemFactory instanceof LambdaItemFactory) {
                            results.add(((LambdaItemFactory<T>)itemFactory).createLambdaItem(env.getController(), element, type, anchorOffset, true, env.addSemicolon()));
                            if (controller.getElementUtilities().getDescriptorElement(element).getReturnType().getKind() != VOID) {
                                results.add(((LambdaItemFactory<T>)itemFactory).createLambdaItem(env.getController(), element, type, anchorOffset, false, env.addSemicolon()));
                            }
                        }
                        final boolean startsWith = startsWith(env, element.getSimpleName().toString());
                        final boolean withinScope = withinScope(env, element);
                        if (withinScope && scope.getEnclosingClass() == element) {
                            continue;
                        }
                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                        final Set<? extends TypeMirror> finalSmartTypes = smartTypes;
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            @Override
                            public boolean accept(Element e, TypeMirror t) {
                                return (startsWith || startsWith(env, e.getSimpleName().toString()))
                                        && (!e.getSimpleName().contentEquals(CLASS_KEYWORD) && (!withinScope && (!isStatic || e.getModifiers().contains(STATIC))) || withinScope && e.getSimpleName().contentEquals(THIS_KEYWORD))
                                        && trees.isAccessible(scope, e, (DeclaredType) t)
                                        && (e.getKind().isField() && isOfSmartType(env, ((VariableElement) e).asType(), finalSmartTypes) || e.getKind() == METHOD && isOfSmartType(env, ((ExecutableElement) e).getReturnType(), finalSmartTypes));
                            }
                        };
                        for (Element ee : controller.getElementUtilities().getMembers(type, acceptor)) {
                            if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(ee)) {
                                results.add(itemFactory.createStaticMemberItem(env.getController(), type, ee, asMemberOf(ee, type, types), false, anchorOffset, elements.isDeprecated(ee), env.addSemicolon(), true));
                            }
                        }
                    }
                }
            }
        } else {
            addChainedMembers(env, locals);
            addAllStaticMemberNames(env);
        }
        final TypeElement enclClass = scope.getEnclosingClass();
        List<ExecutableElement> methodsIn = null;

        for (Element e : locals) {
            switch (simplifyElementKind(e.getKind())) {
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                    results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, e.asType(), anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, e.asType(), smartTypes), env.assignToVarPos()));
                    break;
                case FIELD:
                    String name = e.getSimpleName().toString();
                    if (THIS_KEYWORD.equals(name) || SUPER_KEYWORD.equals(name)) {
                        results.add(itemFactory.createKeywordItem(name, null, anchorOffset, isOfSmartType(env, e.asType(), smartTypes)));
                    } else {
                        TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                        results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                    }
                    break;
                case METHOD:
                    if (methodsIn == null) {
                        methodsIn = ElementFilter.methodsIn(locals);
                    }
                    ExecutableType et = (ExecutableType) asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                    if (e.getEnclosingElement() != enclClass && conflictsWithLocalMethods(e.getSimpleName(), enclClass, methodsIn)) {
                        results.add(itemFactory.createStaticMemberItem(env.getController(), (DeclaredType)e.getEnclosingElement().asType(), e, et, false, anchorOffset, elements.isDeprecated(e), env.addSemicolon(), true));
                    } else {
                        results.add(itemFactory.createExecutableItem(env.getController(), (ExecutableElement) e, et, anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), false, env.addSemicolon(), isOfSmartType(env, getCorrectedReturnType(env, et, (ExecutableElement) e, enclClass != null ? enclClass.asType() : null), smartTypes), env.assignToVarPos(), false));
                    }
                    break;
            }
        }
    }

    private void addLocalFieldsAndVars(final Env env) throws IOException {
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final Scope scope = env.getScope();
        Set<? extends TypeMirror> smartTypes = options.contains(Options.ALL_COMPLETION) ? null : getSmartTypes(env);
        final TypeElement enclClass = scope.getEnclosingClass();
        for (Element e : getLocalMembersAndVars(env)) {
            switch (simplifyElementKind(e.getKind())) {
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                    results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, e.asType(), anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, e.asType(), smartTypes), env.assignToVarPos()));
                    break;
                case FIELD:
                    String name = e.getSimpleName().toString();
                    if (THIS_KEYWORD.equals(name) || SUPER_KEYWORD.equals(name)) {
                        results.add(itemFactory.createKeywordItem(name, null, anchorOffset, isOfSmartType(env, e.asType(), smartTypes)));
                    } else {
                        TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                        results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                    }
                    break;
            }
        }
    }

    private void addEffectivelyFinalAutoCloseables(final Env env) throws IOException {
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final TypeElement te = elements.getTypeElement("java.lang.AutoCloseable"); //NOI18N
        if (te != null) {
            final Types types = controller.getTypes();
            final ElementUtilities eu = controller.getElementUtilities();
            final Scope scope = env.getScope();
            final Set<? extends TypeMirror> smartTypes = options.contains(Options.ALL_COMPLETION) ? null : getSmartTypes(env);
            final TypeElement enclClass = scope.getEnclosingClass();
            for (Element e : getLocalMembersAndVars(env)) {
                switch (simplifyElementKind(e.getKind())) {
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE:
                    case RESOURCE_VARIABLE:
                    case PARAMETER:
                        if (types.isSubtype(e.asType(), te.asType()) && eu.isEffectivelyFinal((VariableElement) e)) {
                            results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, e.asType(), anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, e.asType(), smartTypes), env.assignToVarPos()));
                        }
                        break;
                    case FIELD:
                        if (types.isSubtype(e.asType(), te.asType())) {
                            String name = e.getSimpleName().toString();
                            if (THIS_KEYWORD.equals(name) || SUPER_KEYWORD.equals(name)) {
                                results.add(itemFactory.createKeywordItem(name, null, anchorOffset, isOfSmartType(env, e.asType(), smartTypes)));
                            } else {
                                TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                                results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, null, env.getScope().getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                            }
                        }
                        break;
                }
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private Iterable<? extends Element> getLocalMembersAndVars(final Env env) throws IOException {
        final String prefix = env.getPrefix();
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Trees trees = controller.getTrees();
        final TreeUtilities tu = controller.getTreeUtilities();
        final ElementUtilities eu = controller.getElementUtilities();
        final Scope scope = env.getScope();
        final TypeElement enclClass = scope.getEnclosingClass();
        final boolean enclStatic = enclClass != null && enclClass.getModifiers().contains(Modifier.STATIC);
        final boolean ctxStatic = enclClass != null && (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic()));
        final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
        final ExecutableElement method = scope.getEnclosingMethod() != null && scope.getEnclosingMethod().getEnclosingElement() == enclClass ? scope.getEnclosingMethod() : null;
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                boolean isStatic = ctxStatic || (t != null && t.getKind() == TypeKind.DECLARED && ((DeclaredType) t).asElement() != enclClass && enclStatic);
                switch (simplifyElementKind(e.getKind())) {
                    case CONSTRUCTOR:
                        return false;
                    case LOCAL_VARIABLE:
                    case RESOURCE_VARIABLE:
                    case EXCEPTION_PARAMETER:
                    case PARAMETER:
                        return startsWith(env, e.getSimpleName().toString()) && 
                                    (method == e.getEnclosingElement() ||
                                    e.getModifiers().contains(Modifier.FINAL) ||
                                    env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && eu.isEffectivelyFinal((VariableElement)e)
                                || (method == null && (e.getEnclosingElement().getKind() == INSTANCE_INIT
                                || e.getEnclosingElement().getKind() == STATIC_INIT
                                || e.getEnclosingElement().getKind() == CONSTRUCTOR
                                || e.getEnclosingElement().getKind() == METHOD && e.getEnclosingElement().getEnclosingElement().getKind() == FIELD)))
                                && (!illegalForwardRefs.containsKey(e.getSimpleName()) || illegalForwardRefs.get(e.getSimpleName()).getEnclosingElement() != e.getEnclosingElement());
                    case FIELD:
                        if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)) {
                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) && !isStatic;
                        }
                    case ENUM_CONSTANT:
                        return startsWith(env, e.getSimpleName().toString())
                                && !illegalForwardRefs.containsValue(e)
                                && (!isStatic || e.getModifiers().contains(STATIC))
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && trees.isAccessible(scope, e, (DeclaredType) t);
                    case METHOD:
                        String sn = e.getSimpleName().toString();
                        return startsWith(env, sn)
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && (!isStatic || e.getModifiers().contains(STATIC))
                                && trees.isAccessible(scope, e, (DeclaredType) t)
                                && (!Utilities.isExcludeMethods() || !Utilities.isExcluded(eu.getElementName(e.getEnclosingElement(), true) + "." + sn)); //NOI18N
                    }
                return false;
            }
        };
        return controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor);
    }

    private void addTypeDotClassMembers(Env env, DeclaredType type) throws IOException {
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        Iterator<? extends TypeMirror> it = type.getTypeArguments().iterator();
        TypeMirror tm = it.hasNext() ? it.next() : elements.getTypeElement(JAVA_LANG_OBJECT).asType();
        Iterable<DeclaredType> dts = null;
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror bound = ((WildcardType) tm).getSuperBound();
            if (bound != null) {
                if (bound.getKind() == TypeKind.DECLARED) {
                    dts = getSupertypesOf(env, (DeclaredType) bound);
                }
            } else {
                bound = ((WildcardType) tm).getExtendsBound();
                if (bound != null) {
                    if (bound.getKind() == TypeKind.DECLARED) {
                        if (JAVA_LANG_OBJECT.contentEquals(((TypeElement) ((DeclaredType) bound).asElement()).getQualifiedName())) {
                            dts = Collections.singleton((DeclaredType) elements.getTypeElement(JAVA_LANG_OBJECT).asType());
                        } else {
                            dts = getSubtypesOf(env, (DeclaredType) bound);
                        }
                    }
                } else {
                    dts = Collections.singleton((DeclaredType) elements.getTypeElement(JAVA_LANG_OBJECT).asType());
                }
            }
        } else if (tm.getKind() == TypeKind.DECLARED) {
            dts = Collections.singleton((DeclaredType) tm);
        }
        if (dts != null) {
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                @Override
                public boolean accept(Element e, TypeMirror t) {
                    return e.getKind() == FIELD && e.getSimpleName().contentEquals(CLASS_KEYWORD);
                }
            };
            for (DeclaredType dt : dts) {
                if (startsWith(env, dt.asElement().getSimpleName().toString())) {
                    for (Element ee : controller.getElementUtilities().getMembers(dt, acceptor)) {
                        results.add(itemFactory.createStaticMemberItem(env.getController(), dt, ee, asMemberOf(ee, dt, types), false, anchorOffset, elements.isDeprecated(ee), env.addSemicolon(), true));
                    }
                }
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void addChainedMembers(final Env env, final Iterable<? extends Element> locals) throws IOException {
        final Set<? extends TypeMirror> smartTypes = getSmartTypes(env);
        if (smartTypes != null && !smartTypes.isEmpty()) {
            final CompilationController controller = env.getController();
            final Scope scope = env.getScope();
            final TypeElement enclClass = scope.getEnclosingClass();
            final Elements elements = controller.getElements();
            final Types types = controller.getTypes();
            final Trees trees = controller.getTrees();
            final ElementUtilities eu = controller.getElementUtilities();
            for (Element localElement : locals) {
                TypeMirror localElementType = null;
                TypeMirror type = null;
                switch (simplifyElementKind(localElement.getKind())) {
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE:
                    case RESOURCE_VARIABLE:
                    case PARAMETER:
                    case ENUM_CONSTANT:
                        type = localElementType = localElement.asType();
                        break;
                    case FIELD:
                        String name = localElement.getSimpleName().toString();
                        if (!THIS_KEYWORD.equals(name) && !SUPER_KEYWORD.equals(name)) {
                            type = localElementType = asMemberOf(localElement, enclClass != null ? enclClass.asType() : null, types);
                        }
                        break;
                    case METHOD:
                        localElementType = asMemberOf(localElement, enclClass != null ? enclClass.asType() : null, types);
                        type = ((ExecutableType) localElementType).getReturnType();
                        break;
                }
                if (type != null && type.getKind() == TypeKind.DECLARED && !isOfSmartType(env, type, smartTypes)) {
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        @Override
                        public boolean accept(Element e, TypeMirror t) {
                            switch (e.getKind()) {
                                case FIELD:
                                    if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)) {
                                        return false;
                                    }
                                case ENUM_CONSTANT:
                                    return trees.isAccessible(scope, e, (DeclaredType) t)
                                            && isOfSmartType(env, asMemberOf(e, t, types), smartTypes);
                                case METHOD:
                                    return trees.isAccessible(scope, e, (DeclaredType) t)
                                            && isOfSmartType(env, ((ExecutableType) asMemberOf(e, t, types)).getReturnType(), smartTypes);
                            }
                            return false;
                        }
                    };
                    for (Element e : eu.getMembers(type, acceptor)) {
                        if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) {
                            List<Element> chainedElements = new ArrayList<>(2);
                            chainedElements.add(localElement);
                            chainedElements.add(e);
                            List<TypeMirror> chainedTypes = new ArrayList<>(2);
                            chainedTypes.add(localElementType);
                            chainedTypes.add(asMemberOf(e, type, types));
                            results.add(itemFactory.createChainedMembersItem(env.getController(), chainedElements, chainedTypes, anchorOffset, elements.isDeprecated(localElement) || elements.isDeprecated(e), env.addSemicolon()));
                        }
                    }
                }
            }
        }
    }

    private void addAllStaticMemberNames(final Env env) {
        String prefix = env.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            CompilationController controller = env.getController();
            Set<? extends Element> excludes = env.getExcludes();
            Set<ElementHandle<Element>> excludeHandles = null;
            if (excludes != null) {
                excludeHandles = new HashSet<>(excludes.size());
                for (Element el : excludes) {
                    excludeHandles.add(ElementHandle.create(el));
                }
            }
            ClassIndex.NameKind kind = Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
            Iterable<Symbols> declaredSymbols = controller.getClasspathInfo().getClassIndex().getDeclaredSymbols(prefix, kind, EnumSet.allOf(ClassIndex.SearchScope.class));
            for (Symbols symbols : declaredSymbols) {
                if (Utilities.isExcluded(symbols.getEnclosingType().getQualifiedName())
                        || excludeHandles != null && excludeHandles.contains(symbols.getEnclosingType())
                        || isAnnonInner(symbols.getEnclosingType())) {
                    continue;
                }
                for (String name : symbols.getSymbols()) {
                    if (!Utilities.isExcludeMethods() || !Utilities.isExcluded(symbols.getEnclosingType().getQualifiedName() + '.' + name)) {
                        results.add(itemFactory.createStaticMemberItem(symbols.getEnclosingType(), name, anchorOffset, env.addSemicolon(), env.getReferencesCount(), controller.getSnapshot().getSource(), true));
                    }
                }
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void addMemberConstantsAndTypes(final Env env, final TypeMirror type, final Element elem) throws IOException {
        Set<? extends TypeMirror> smartTypes = options.contains(Options.ALL_COMPLETION) ? null : getSmartTypes(env);
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final Trees trees = controller.getTrees();
        TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) type).asElement() : null;
        final boolean isStatic = elem != null && (elem.getKind().isClass() || elem.getKind().isInterface() || elem.getKind() == TYPE_PARAMETER);
        final boolean isSuperCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(SUPER_KEYWORD);
        final Scope scope = env.getScope();
        TypeElement enclClass = scope.getEnclosingClass();
        final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                if (!startsWith(env, e.getSimpleName().toString())
                        || (isStatic && !e.getModifiers().contains(STATIC))) {
                    return false;
                }
                switch (e.getKind()) {
                    case FIELD:
                        if (((VariableElement) e).getConstantValue() == null && !CLASS_KEYWORD.contentEquals(e.getSimpleName())) {
                            return false;
                        }
                    case ENUM_CONSTANT:
                        return (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, e, (DeclaredType) (isSuperCall && enclType != null ? enclType : t));
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        return (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && !Utilities.isExcluded(((TypeElement)e).getQualifiedName()) && trees.isAccessible(scope, e, (DeclaredType) t);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
            switch (e.getKind()) {
                case FIELD:
                case ENUM_CONSTANT:
                    String name = e.getSimpleName().toString();
                    if (CLASS_KEYWORD.equals(name)) {
                        results.add(itemFactory.createKeywordItem(name, null, anchorOffset, false));
                    } else {
                        TypeMirror tm = asMemberOf(e, type, types);
                        results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                    }
                    break;
                case CLASS:
                case ENUM:
                case INTERFACE:
                    DeclaredType dt = (DeclaredType) asMemberOf(e, type, types);
                    results.add(itemFactory.createTypeItem(env.getController(), (TypeElement) e, dt, anchorOffset, null, elements.isDeprecated(e), false, env.isInsideClass(), true, false, false));
                    break;
            }
        }
    }

    private void addMethodReferences(final Env env, final TypeMirror type, final Element elem) throws IOException {
        Set<? extends TypeMirror> smartTypes = getSmartTypes(env);
        final String prefix = env.getPrefix();
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        final ElementUtilities eu = controller.getElementUtilities();
        TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) type).asElement() : null;
        final boolean isThisCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(THIS_KEYWORD);
        final boolean isSuperCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(SUPER_KEYWORD);
        final Scope scope = env.getScope();
        if ((isThisCall || isSuperCall) && tu.isStaticContext(scope)) {
            return;
        }
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case METHOD:
                        String sn = e.getSimpleName().toString();
                        return startsWith(env, sn, prefix)
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && env.isAccessible(scope, e, t, isSuperCall)
                                && (!Utilities.isExcludeMethods() || !Utilities.isExcluded(eu.getElementName(e.getEnclosingElement(), true) + "." + sn)); //NOI18N
                    }
                return false;
            }
        };
        for (Element e : eu.getMembers(type, acceptor)) {
            switch (e.getKind()) {
                case METHOD:
                    ExecutableType et = (ExecutableType) asMemberOf(e, type, types);
                    results.add(itemFactory.createExecutableItem(env.getController(), (ExecutableElement) e, et, anchorOffset, null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), false, false, isOfSmartType(env, et, smartTypes), env.assignToVarPos(), true));
                    break;
            }
        }
    }

    private void addMembers(final Env env, final TypeMirror type, final Element elem, final EnumSet<ElementKind> kinds, final DeclaredType baseType, final boolean inImport, final boolean insideNew, final boolean autoImport) throws IOException {
        addMembers(env, type, elem, kinds, baseType, inImport, insideNew, autoImport, false, addSwitchItemDefault);
    }

    private void addMembers(final Env env, final TypeMirror type, final Element elem, final EnumSet<ElementKind> kinds, final DeclaredType baseType, final boolean inImport, final boolean insideNew, final boolean autoImport, final boolean afterConstructorTypeParams, AddSwitchRelatedItem addSwitchItem) throws IOException {
        Set<? extends TypeMirror> smartTypes = getSmartTypes(env);
        final TreePath path = env.getPath();
        TypeMirror actualType = type;
        if (path != null && path.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
            actualType = adjustType(env, type, elem, new TreePath(path, ((MemberSelectTree)path.getLeaf()).getExpression()));
        }
        final CompilationController controller = env.getController();
        final Trees trees = controller.getTrees();
        final Elements elements = controller.getElements();
        final ElementUtilities eu = controller.getElementUtilities();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        TypeElement typeElem = actualType.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) actualType).asElement() : null;
        final boolean isStatic = elem != null && (elem.getKind().isClass() || elem.getKind().isInterface() || elem.getKind() == TYPE_PARAMETER) && elem.asType().getKind() != TypeKind.ERROR;
        final boolean isThisCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(THIS_KEYWORD);
        final boolean isSuperCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(SUPER_KEYWORD);
        final Scope scope = env.getScope();
        if ((isThisCall || isSuperCall) && tu.isStaticContext(scope)) {
            return;
        }
        final boolean[] ctorSeen = {false};
        final boolean[] nestedClassSeen = {false};
        final TypeElement enclClass = scope.getEnclosingClass();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                switch (simplifyElementKind(e.getKind())) {
                    case FIELD:
                        if (!startsWith(env, e.getSimpleName().toString())) {
                            return false;
                        }
                        if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)) {
                            TypeElement cls = enclClass;
                            while (cls != null) {
                                if (cls == elem) {
                                    return isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types);
                                }
                                TypeElement outer = eu.enclosingTypeElement(cls);
                                cls = !cls.getModifiers().contains(STATIC) ? outer : null;
                            }
                            return false;
                        }
                        if (isStatic) {
                            if (!e.getModifiers().contains(STATIC)
                                    || e.getSimpleName().contentEquals(CLASS_KEYWORD) && elem.getKind() == ElementKind.TYPE_PARAMETER) {
                                return false;
                            }
                        } else {
                            if (!options.contains(Options.ALL_COMPLETION) && e.getModifiers().contains(STATIC)) {
                                if ((Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                        && isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types)
                                        && env.isAccessible(scope, e, t, isSuperCall)
                                        && ((isStatic && !inImport) || !e.getSimpleName().contentEquals(CLASS_KEYWORD))) {
                                    hasAdditionalMembers = true;
                                }
                                return false;
                            }
                        }
                        return (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types)
                                && env.isAccessible(scope, e, t, isSuperCall)
                                && ((isStatic && !inImport) || !e.getSimpleName().contentEquals(CLASS_KEYWORD));
                    case ENUM_CONSTANT:
                    case EXCEPTION_PARAMETER:
                    case LOCAL_VARIABLE:
                    case RESOURCE_VARIABLE:
                    case PARAMETER:
                        return startsWith(env, e.getSimpleName().toString())
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types)
                                && env.isAccessible(scope, e, t, isSuperCall);
                    case METHOD:
                        String sn = e.getSimpleName().toString();
                        if (isStatic) {
                            if (!e.getModifiers().contains(STATIC)) {
                                return false;
                            }
                        } else {
                            if (!options.contains(Options.ALL_COMPLETION) && e.getModifiers().contains(STATIC)) {
                                if (startsWith(env, sn)
                                        && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                        && isOfKindAndType(((ExecutableType) asMemberOf(e, t, types)).getReturnType(), e, kinds, baseType, scope, trees, types)
                                        && env.isAccessible(scope, e, t, isSuperCall)
                                        && (!Utilities.isExcludeMethods() || !Utilities.isExcluded(eu.getElementName(e.getEnclosingElement(), true) + "." + sn))) { //NOI18N
                                    hasAdditionalMembers = true;
                                }
                                return false;
                            }
                        }
                        return startsWith(env, sn)
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && isOfKindAndType(((ExecutableType) asMemberOf(e, t, types)).getReturnType(), e, kinds, baseType, scope, trees, types)
                                && env.isAccessible(scope, e, t, isSuperCall)
                                && (!Utilities.isExcludeMethods() || !Utilities.isExcluded(eu.getElementName(e.getEnclosingElement(), true) + "." + sn)); //NOI18N
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                        if (!e.getModifiers().contains(STATIC)) {
                            nestedClassSeen[0] = true;
                        }
                        return startsWith(env, e.getSimpleName().toString())
                                && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && !Utilities.isExcluded(((TypeElement)e).getQualifiedName())
                                && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types)
                                && (!env.isAfterExtends() || containsAccessibleNonFinalType(e, scope, trees))
                                && env.isAccessible(scope, e, t, isSuperCall) && isStatic;
                    case CONSTRUCTOR:
                        ctorSeen[0] = true;
                        return (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && isOfKindAndType(e.getEnclosingElement().asType(), e, kinds, baseType, scope, trees, types)
                                && (env.isAccessible(scope, e, t, isSuperCall || insideNew) || (elem.getModifiers().contains(ABSTRACT) && !e.getModifiers().contains(PRIVATE)))
                                && isStatic;
                }
                return false;
            }
        };
        boolean addCast = actualType != type && elem instanceof VariableElement && !elem.getKind().isField();
        for (Element e : controller.getElementUtilities().getMembers(actualType, acceptor)) {
            switch (simplifyElementKind(e.getKind())) {
                case ENUM_CONSTANT:
                case EXCEPTION_PARAMETER:
                case FIELD:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                    String name = e.getSimpleName().toString();
                    if (THIS_KEYWORD.equals(name) || CLASS_KEYWORD.equals(name) || SUPER_KEYWORD.equals(name)) {
                        if (!env.isExcludedKW(name)) {
                            results.add(itemFactory.createKeywordItem(name, null, anchorOffset, isOfSmartType(env, e.asType(), smartTypes)));
                            env.addExcludedKW(name);
                        }
                    } else {
                        TypeMirror tm = asMemberOf(e, actualType, types);
                        if (addCast && itemFactory instanceof TypeCastableItemFactory) {
                            results.add(((TypeCastableItemFactory<T>)itemFactory).createTypeCastableVariableItem(env.getController(), (VariableElement) e, tm, actualType, anchorOffset, autoImport ? env.getReferencesCount() : null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos()));
                        } else {
                            addSwitchItem.addVariableItem(env.getController(), (VariableElement) e, tm, anchorOffset, autoImport ? env.getReferencesCount() : null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), isOfSmartType(env, tm, smartTypes), env.assignToVarPos());
                        }
                    }
                    break;
                case CONSTRUCTOR:
                    ExecutableType et = (ExecutableType) asMemberOf(e, actualType, types);
                    results.add(itemFactory.createExecutableItem(env.getController(), (ExecutableElement) e, et, anchorOffset, autoImport ? env.getReferencesCount() : null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), inImport, false, afterConstructorTypeParams, isOfSmartType(env, actualType, smartTypes), env.assignToVarPos(), false));
                    break;
                case METHOD:
                    et = (ExecutableType) asMemberOf(e, actualType, types);
                    if (addCast && itemFactory instanceof TypeCastableItemFactory
                            && !types.isSubtype(type, e.getEnclosingElement().asType())
                            && type.getKind() == TypeKind.DECLARED && !hasBaseMethod(elements, (DeclaredType) type, (ExecutableElement) e)) {
                        results.add(((TypeCastableItemFactory<T>)itemFactory).createTypeCastableExecutableItem(env.getController(), (ExecutableElement) e, et, actualType, anchorOffset, autoImport ? env.getReferencesCount() : null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), inImport, env.addSemicolon(), isOfSmartType(env, getCorrectedReturnType(env, et, (ExecutableElement) e, actualType), smartTypes), env.assignToVarPos(), false));
                    } else {
                        results.add(itemFactory.createExecutableItem(env.getController(), (ExecutableElement) e, et, anchorOffset, autoImport ? env.getReferencesCount() : null, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), inImport, env.addSemicolon(), isOfSmartType(env, getCorrectedReturnType(env, et, (ExecutableElement) e, actualType), smartTypes), env.assignToVarPos(), false));
                    }
                    break;
                case CLASS:
                case ENUM:
                case INTERFACE:
                case ANNOTATION_TYPE:
                    DeclaredType dt = (DeclaredType) asMemberOf(e, actualType, types);
                    addSwitchItem.addTypeItem(env.getController(), (TypeElement) e, dt, anchorOffset, null, elements.isDeprecated(e), insideNew, insideNew || env.isInsideClass(), true, isOfSmartType(env, dt, smartTypes), autoImport);
                    break;
            }
        }
        if (!ctorSeen[0] && kinds.contains(CONSTRUCTOR) && elem.getKind().isInterface()) {
            results.add(itemFactory.createDefaultConstructorItem((TypeElement) elem, anchorOffset, isOfSmartType(env, actualType, smartTypes)));
        }
        if (isStatic && enclClass != null && elem.getKind().isInterface() && env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0) {
            for (TypeMirror iface : enclClass.getInterfaces()) {
                if (((DeclaredType) iface).asElement() == elem) {
                    results.add(itemFactory.createKeywordItem(SUPER_KEYWORD, null, anchorOffset, isOfSmartType(env, actualType, smartTypes)));
                    break;
                }
            }
        }
        if (!isStatic && nestedClassSeen[0]) {
            addKeyword(env, NEW_KEYWORD, SPACE, false);
        }
    }

    private boolean hasBaseMethod(Elements elements, DeclaredType type, ExecutableElement invoked) {
        TypeElement clazz = (TypeElement) type.asElement();
        for (ExecutableElement existing : ElementFilter.methodsIn(elements.getAllMembers(clazz))) {
            if (existing.getSimpleName().equals(invoked.getSimpleName()) &&
                elements.overrides(invoked, existing, clazz)) {
                return true;
            }
        }
        return false;
    }

    private void addThisOrSuperConstructor(final Env env, final TypeMirror type, final Element elem, final String name, final ExecutableElement toExclude) throws IOException {
        final CompilationController controller = env.getController();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final Trees trees = controller.getTrees();
        final Scope scope = env.getScope();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case CONSTRUCTOR:
                        return toExclude != e && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                                && (trees.isAccessible(scope, e, (DeclaredType) t) || (elem.getModifiers().contains(ABSTRACT) && !e.getModifiers().contains(PRIVATE)));
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
            if (e.getKind() == CONSTRUCTOR) {
                ExecutableType et = (ExecutableType) asMemberOf(e, type, types);
                results.add(itemFactory.createThisOrSuperConstructorItem(env.getController(), (ExecutableElement) e, et, anchorOffset, elements.isDeprecated(e), name));
            }
        }
    }

    private void addEnumConstants(Env env, TypeElement elem) {
        Elements elements = env.getController().getElements();
        TreePath path = env.getPath().getParentPath();
        Pair<Set<Element>, Set<TypeMirror>> alreadyUsed = computedUsedInSwitch(env, path);

        for (Element e : elem.getEnclosedElements()) {
            if (e.getKind() == ENUM_CONSTANT && !alreadyUsed.first().contains(e)) {
                String name = e.getSimpleName().toString();
                if (startsWith(env, name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))) {
                    results.add(itemFactory.createVariableItem(env.getController(), (VariableElement) e, e.asType(), anchorOffset, null, false, elements.isDeprecated(e), false, env.assignToVarPos()));
                }
            }
        }
    }

    private Pair<Set<Element>, Set<TypeMirror>> computedUsedInSwitch(Env env, TreePath switchPath) {
        Trees trees = env.getController().getTrees();
        Types types = env.getController().getTypes();
        Set<Element> alreadyUsedElements = new HashSet<>();
        Set<TypeMirror> alreadyUsedTypes = new HashSet<>();
        List<? extends CaseTree> caseTrees = null;
        if (switchPath != null && switchPath.getLeaf().getKind() == Tree.Kind.SWITCH) {
            SwitchTree st = (SwitchTree) switchPath.getLeaf();
            caseTrees = st.getCases();
        } else if (switchPath != null && switchPath.getLeaf().getKind() == Tree.Kind.SWITCH_EXPRESSION) {
            caseTrees = ((SwitchExpressionTree) switchPath.getLeaf()).getCases();
        }

        if (caseTrees != null) {
            for (CaseTree ct : caseTrees) {
                for (CaseLabelTree label : ct.getLabels()) {
                    TreePath labelPath = new TreePath(switchPath, label);
                    switch (label.getKind()) {
                        case CONSTANT_CASE_LABEL -> {
                            //note that cases with constant case labels can't legally have guards
                            ConstantCaseLabelTree ccl = (ConstantCaseLabelTree) label;
                            Element e = ccl.getConstantExpression() != null ? trees.getElement(new TreePath(labelPath, ccl.getConstantExpression())) : null;
                            if (e != null && e.getKind() == ENUM_CONSTANT) {
                                alreadyUsedElements.add(e);
                            }
                        }
                        case PATTERN_CASE_LABEL -> {
                            PatternCaseLabelTree pcl = (PatternCaseLabelTree) label;
                            if (ct.getGuard() == null && pcl.getPattern().getKind() == Kind.BINDING_PATTERN) {
                                BindingPatternTree bp = (BindingPatternTree) pcl.getPattern();
                                TreePath typePath = new TreePath(new TreePath(new TreePath(labelPath, bp),
                                                                              bp.getVariable()),
                                                                 bp.getVariable().getType());
                                TypeMirror type = trees.getTypeMirror(typePath);
                                if (type != null) {
                                    alreadyUsedTypes.add(types.erasure(type));
                                }
                            }
                        }
                    }
                }
            }
        }

        return Pair.of(alreadyUsedElements, alreadyUsedTypes);
    }

    private void addCaseLabels(Env env, CaseTree cst) {
        TreePath path = env.getPath().getParentPath();
        boolean nullUsed = false;
        boolean defaultUsed = false;
        boolean patternUsedInCase = false;
        List<? extends CaseTree> caseTrees = null;
        if (path != null && path.getLeaf().getKind() == Tree.Kind.SWITCH) {
            caseTrees = ((SwitchTree) path.getLeaf()).getCases();
        } else if (path != null && path.getLeaf().getKind() == Tree.Kind.SWITCH_EXPRESSION) {
            caseTrees = ((SwitchExpressionTree) path.getLeaf()).getCases();
        }
        if (caseTrees != null) {
            for (CaseTree ct : caseTrees) {
                for (CaseLabelTree clt : ct.getLabels()) {
                    switch (clt.getKind()) {
                        case DEFAULT_CASE_LABEL:
                            defaultUsed = true;
                            break;
                        case CONSTANT_CASE_LABEL:
                            if ((((ConstantCaseLabelTree) clt).getConstantExpression()).getKind() == Tree.Kind.NULL_LITERAL) {
                                nullUsed = true;
                            }
                            break;
                        case PATTERN_CASE_LABEL:
                            if (ct == cst) {
                                patternUsedInCase = true;
                            }
                            break;
                    }
                }
            }
        }
        if (!nullUsed) {
            addKeyword(env, NULL_KEYWORD, null, false);
        }
        if (nullUsed && !defaultUsed && !patternUsedInCase) {
            addKeyword(env, DEFAULT_KEYWORD, null, false);
        }
    }

    private void addPackageContent(final Env env, PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType, boolean insideNew, boolean srcOnly) throws IOException {
        addPackageContent(env, pe, kinds, baseType, insideNew, srcOnly, addSwitchItemDefault);
    }

    private void addPackageContent(final Env env, PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType, boolean insideNew, boolean srcOnly, AddSwitchRelatedItem addSwitchItem) throws IOException {
        if (isRecordSupported(env)) {
            kinds.add(RECORD);
        }
        Set<? extends TypeMirror> smartTypes = options.contains(Options.ALL_COMPLETION) ? null : getSmartTypes(env);
        CompilationController controller = env.getController();
        Elements elements = controller.getElements();
        Types types = controller.getTypes();
        Trees trees = controller.getTrees();
        ElementUtilities eu = controller.getElementUtilities();
        Scope scope = env.getScope();
        for (Element e : pe.getEnclosedElements()) {
            if (e.getKind().isClass() || e.getKind().isInterface()) {
                String name = e.getSimpleName().toString();
                if ((env.getExcludes() == null || !env.getExcludes().contains(e))
                        && startsWith(env, name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                        && trees.isAccessible(scope, (TypeElement) e)
                        && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types)
                        && !Utilities.isExcluded(eu.getElementName(e, true))) {
                    addSwitchItem.addTypeItem(env.getController(), (TypeElement) e, (DeclaredType) e.asType(), anchorOffset, null, elements.isDeprecated(e), insideNew, insideNew || env.isInsideClass(), true, isOfSmartType(env, e.asType(), smartTypes), false);
                }
            }
        }
        String pkgName = pe.getQualifiedName() + "."; //NOI18N
        addPackages(env, pkgName, srcOnly);
    }

    private void addPackages(Env env, String fqnPrefix, boolean srcOnly) {
        if (fqnPrefix == null) {
            fqnPrefix = EMPTY;
        }
        String prefix = env.getPrefix() != null ? fqnPrefix + env.getPrefix() : fqnPrefix;
        CompilationController controller = env.getController();
        Elements elements = controller.getElements();
        Element el = controller.getTrees().getElement(new TreePath(controller.getCompilationUnit()));
        ModuleElement moduleElement = el != null ? controller.getElements().getModuleOf(el) : null;
        Set<String> seenPkgs = new HashSet<>();
        EnumSet<ClassIndex.SearchScope> scope = srcOnly ? EnumSet.of(ClassIndex.SearchScope.SOURCE) : EnumSet.allOf(ClassIndex.SearchScope.class);        
        for (String pkgName : env.getController().getClasspathInfo().getClassIndex().getPackageNames(fqnPrefix, false, scope)) {
            if (startsWith(env, pkgName, prefix) && !Utilities.isExcluded(pkgName + ".")
                    && (moduleElement != null ? elements.getPackageElement(moduleElement, pkgName) : elements.getPackageElement(pkgName)) != null) { //NOI18N
                if (fqnPrefix != null) {
                    pkgName = pkgName.substring(fqnPrefix.length());
                }
                int idx = pkgName.indexOf('.');
                if (idx > 0) {
                    pkgName = pkgName.substring(0, idx);
                }
                if (seenPkgs.add(pkgName)) {
                    results.add(itemFactory.createPackageItem(pkgName, anchorOffset, srcOnly));
                }
            }
        }
    }
    
    private void addModuleNames(Env env, String fqnPrefix, boolean srcOnly) {
        if (fqnPrefix == null) {
            fqnPrefix = EMPTY;
        }
        srcOnly = false;
        String prefix = env.getPrefix() != null ? fqnPrefix + env.getPrefix() : fqnPrefix;
        for (String name : SourceUtils.getModuleNames(env.getController(), srcOnly ? EnumSet.of(ClassIndex.SearchScope.SOURCE) : EnumSet.allOf(ClassIndex.SearchScope.class))) {
            if (startsWith(env, name, prefix) && itemFactory instanceof ModuleItemFactory) {
                results.add(((ModuleItemFactory<T>)itemFactory).createModuleItem(name, anchorOffset));
            }
        }
    }

    private void addTypes(Env env, EnumSet<ElementKind> kinds, DeclaredType baseType) throws IOException {
        addTypes(env, kinds, baseType, addSwitchItemDefault);
    }

    private void addTypes(Env env, EnumSet<ElementKind> kinds, DeclaredType baseType, AddSwitchRelatedItem addTypeItem) throws IOException {
        if (options.contains(Options.ALL_COMPLETION) || options.contains(Options.COMBINED_COMPLETION)) {
            if (baseType == null) {
                addAllTypes(env, kinds);
            } else {
                Elements elements = env.getController().getElements();
                Set<? extends Element> excludes = env.getExcludes();
                for (DeclaredType subtype : getSubtypesOf(env, baseType)) {
                    TypeElement elem = (TypeElement) subtype.asElement();
                    if ((excludes == null || !excludes.contains(elem)) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem)) && !Utilities.isExcluded(elem.getQualifiedName()) && (!env.isAfterExtends() || !elem.getModifiers().contains(Modifier.FINAL))) {
                        addTypeItem.addTypeItem(env.getController(), elem, subtype, anchorOffset, env.getReferencesCount(), elements.isDeprecated(elem), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), false, true, false);
                    }
                }
            }
        } else {
            addLocalAndImportedTypes(env, kinds, baseType, addTypeItem);
            hasAdditionalClasses = true;
        }
        addPackages(env, null, kinds.isEmpty());
    }

    private void addLocalAndImportedTypes(final Env env, final EnumSet<ElementKind> kinds, final DeclaredType baseType, AddSwitchRelatedItem addTypeItem) throws IOException {
        final CompilationController controller = env.getController();
        final Trees trees = controller.getTrees();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        final Scope scope = env.getScope();
        final ExecutableElement enclMethod = scope.getEnclosingMethod();
        final TypeElement enclClass = scope.getEnclosingClass();
        final boolean isStatic = enclClass == null ? false
                : (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic()));
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                if ((env.getExcludes() == null || !env.getExcludes().contains(e)) && (e.getKind().isClass() || e.getKind().isInterface() || e.getKind() == TYPE_PARAMETER) && (!env.isAfterExtends() || containsAccessibleNonFinalType(e, scope, trees))) {
                    String name = e.getSimpleName().toString();
                    return name.length() > 0 && !Character.isDigit(name.charAt(0)) && startsWith(env, name)
                            && (!isStatic || e.getModifiers().contains(STATIC) || e.getEnclosingElement() == enclMethod)
                            && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                            && (e.getKind() == TYPE_PARAMETER || !Utilities.isExcluded(((TypeElement)e).getQualifiedName()))
                            && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            switch (e.getKind()) {
                case CLASS:
                case ENUM:
                case INTERFACE:
                case ANNOTATION_TYPE:
                case RECORD:
                    addTypeItem.addTypeItem(env.getController(), (TypeElement) e, (DeclaredType) e.asType(), anchorOffset, null, elements.isDeprecated(e), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), false, false, false);
                    env.addToExcludes(e);
                    break;
                case TYPE_PARAMETER:
                    results.add(itemFactory.createTypeParameterItem((TypeParameterElement) e, anchorOffset));
                    break;
            }
        }
        acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                if ((e.getKind().isClass() || e.getKind().isInterface())) {
                    return (env.getExcludes() == null || !env.getExcludes().contains(e)) && startsWith(env, e.getSimpleName().toString())
                            && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                            && !Utilities.isExcluded(((TypeElement)e).getQualifiedName()) && trees.isAccessible(scope, (TypeElement) e)
                            && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types) && (!env.isAfterExtends() || containsAccessibleNonFinalType(e, scope, trees));
                }
                return false;
            }
        };
        for (TypeElement e : controller.getElementUtilities().getGlobalTypes(acceptor)) {
            Tree iot = env.getPath().getLeaf();
            TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, iot, env.getOffset());
            if (env.getPrefix() != null && e.getSimpleName().toString().contentEquals(env.getPrefix()) && (e.getKind() == ElementKind.RECORD)
                    && ts != null && ts.token().id() == JavaTokenId.INSTANCEOF) {
                results.add(((RecordPatternItemFactory<T>) itemFactory).createRecordPatternItem(controller, e, (DeclaredType) e.asType(), anchorOffset, null, controller.getElements().isDeprecated(e), env.isInsideNew(), env.isInsideNew() || env.isInsideClass()));
            } else {
                addTypeItem.addTypeItem(env.getController(), e, (DeclaredType) e.asType(), anchorOffset, null, elements.isDeprecated(e), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), false, false, false);
            }
        }
    }

    private interface AddSwitchRelatedItem {
        public void addTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType);
        public void addVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset);
    }

    private void addAllTypes(Env env, EnumSet<ElementKind> kinds) {
        String prefix = env.getPrefix();
        CompilationController controller = env.getController();
        Set<? extends Element> excludes = env.getExcludes();
        Set<ElementHandle<Element>> excludeHandles = null;
        if (excludes != null) {
            excludeHandles = new HashSet<>(excludes.size());
            for (Element el : excludes) {
                excludeHandles.add(ElementHandle.create(el));
            }
        }
        if (!kinds.contains(ElementKind.CLASS) && !kinds.contains(ElementKind.INTERFACE)) {
            Set<ElementHandle<TypeElement>> declaredTypes = controller.getClasspathInfo().getClassIndex().getDeclaredTypes(EMPTY, ClassIndex.NameKind.PREFIX, EnumSet.allOf(ClassIndex.SearchScope.class));
            Map<String, ElementHandle<TypeElement>> removed = new HashMap<>(declaredTypes.size());
            Set<String> doNotRemove = new HashSet<>();
            for (ElementHandle<TypeElement> name : declaredTypes) {
                if (excludeHandles != null && excludeHandles.contains(name) || isAnnonInner(name)) {
                    continue;
                }
                if (!kinds.contains(name.getKind()) && !doNotRemove.contains(name.getQualifiedName())) {
                    int idx = name.getQualifiedName().lastIndexOf('.');
                    String sName = idx < 0 ? name.getQualifiedName() : name.getQualifiedName().substring(idx + 1);
                    if (startsWith(env, sName, prefix)) {
                        removed.put(name.getQualifiedName(), name);
                    }
                    continue;
                }
                String qName = name.getQualifiedName();
                String sName = null;
                int idx;
                while ((idx = qName.lastIndexOf('.')) > 0) {
                    if (sName == null) {
                        sName = qName.substring(idx + 1);
                        if (sName.length() > 0 && startsWith(env, sName, prefix)) {
                            results.add(itemFactory.createTypeItem(name, kinds, anchorOffset, env.getReferencesCount(), controller.getSnapshot().getSource(), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), env.isAfterExtends()));
                        }
                    }
                    qName = qName.substring(0, idx);
                    doNotRemove.add(qName);
                    ElementHandle<TypeElement> r = removed.remove(qName);
                    if (r != null) {
                        results.add(itemFactory.createTypeItem(r, kinds, anchorOffset, env.getReferencesCount(), controller.getSnapshot().getSource(), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), env.isAfterExtends()));
                    }
                }
            }
        } else {
            String subwordsPattern = null;
            if (prefix != null && !env.isCamelCasePrefix() && Utilities.isSubwordSensitive()) {
                subwordsPattern = Utilities.createSubwordsPattern(prefix);
            }
            ClassIndex.NameKind kind = env.isCamelCasePrefix()
                    ? Utilities.isCaseSensitive() ? ClassIndex.NameKind.CAMEL_CASE : ClassIndex.NameKind.CAMEL_CASE_INSENSITIVE
                    : subwordsPattern != null ? ClassIndex.NameKind.REGEXP
                    : Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
            Set<ElementHandle<TypeElement>> declaredTypes = controller.getClasspathInfo().getClassIndex().getDeclaredTypes(subwordsPattern != null ? subwordsPattern : prefix != null ? prefix : EMPTY, kind, EnumSet.allOf(ClassIndex.SearchScope.class));
            results.ensureCapacity(results.size() + declaredTypes.size());
            for (ElementHandle<TypeElement> name : declaredTypes) {
                if (!kinds.contains(name.getKind()) || excludeHandles != null && excludeHandles.contains(name) || isAnnonInner(name)) {
                    continue;
                }
                results.add(itemFactory.createTypeItem(name, kinds, anchorOffset, env.getReferencesCount(), controller.getSnapshot().getSource(), env.isInsideNew(), env.isInsideNew() || env.isInsideClass(), env.isAfterExtends()));
            }
        }
    }

    private Set<DeclaredType> getSupertypesOf(Env env, DeclaredType type) {
        LinkedList<DeclaredType> bases = new LinkedList<>();
        bases.add(type);
        HashSet<DeclaredType> ret = new HashSet<>();
        while (!bases.isEmpty()) {
            DeclaredType head = bases.remove();
            TypeElement elem = (TypeElement) head.asElement();
            if (startsWith(env, elem.getSimpleName().toString())) {
                ret.add(head);
            }
            TypeMirror sup = elem.getSuperclass();
            if (sup.getKind() == TypeKind.DECLARED) {
                bases.add((DeclaredType) sup);
            }
            for (TypeMirror iface : elem.getInterfaces()) {
                if (iface.getKind() == TypeKind.DECLARED) {
                    bases.add((DeclaredType) iface);
                }
            }
        }
        return ret;
    }

    private List<DeclaredType> getSubtypesOf(Env env, DeclaredType baseType) throws IOException {
        if (((TypeElement) baseType.asElement()).getQualifiedName().contentEquals(JAVA_LANG_OBJECT)) {
            return Collections.emptyList();
        }
        LinkedList<DeclaredType> subtypes = new LinkedList<>();
        String prefix = env.getPrefix();
        CompilationController controller = env.getController();
        Types types = controller.getTypes();
        Trees trees = controller.getTrees();
        Scope scope = env.getScope();
        if (prefix != null && prefix.length() > 2 && baseType.getTypeArguments().isEmpty()) {
            String subwordsPattern = null;
            if (!env.isCamelCasePrefix() && Utilities.isSubwordSensitive()) {
                subwordsPattern = Utilities.createSubwordsPattern(prefix);
            }
            ClassIndex.NameKind kind = env.isCamelCasePrefix()
                    ? Utilities.isCaseSensitive() ? ClassIndex.NameKind.CAMEL_CASE : ClassIndex.NameKind.CAMEL_CASE_INSENSITIVE
                    : subwordsPattern != null ? ClassIndex.NameKind.REGEXP
                    : Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
            for (ElementHandle<TypeElement> handle : controller.getClasspathInfo().getClassIndex().getDeclaredTypes(subwordsPattern != null ? subwordsPattern : prefix, kind, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                TypeElement te = handle.resolve(controller);
                if (te != null && trees.isAccessible(scope, te) && types.isSubtype(types.getDeclaredType(te), baseType)) {
                    subtypes.add(types.getDeclaredType(te));
                }
            }
        } else {
            HashSet<TypeElement> elems = new HashSet<>();
            LinkedList<DeclaredType> bases = new LinkedList<>();
            bases.add(baseType);
            ClassIndex index = controller.getClasspathInfo().getClassIndex();
            while (!bases.isEmpty()) {
                DeclaredType head = bases.remove();
                TypeElement elem = (TypeElement) head.asElement();
                if (!elems.add(elem)) {
                    continue;
                }
                if (startsWith(env, elem.getSimpleName().toString())) {
                    subtypes.add(head);
                }
                List<? extends TypeMirror> tas = head.getTypeArguments();
                boolean isRaw = !tas.iterator().hasNext();
                subtypes:
                for (ElementHandle<TypeElement> eh : index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    TypeElement e = eh.resolve(controller);
                    if (e != null) {
                        if (trees.isAccessible(scope, e)) {
                            if (isRaw) {
                                DeclaredType dt = types.getDeclaredType(e);
                                bases.add(dt);
                            } else {
                                HashMap<Element, TypeMirror> map = new HashMap<>();
                                TypeMirror sup = e.getSuperclass();
                                if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType) sup).asElement() == elem) {
                                    DeclaredType dt = (DeclaredType) sup;
                                    Iterator<? extends TypeMirror> ittas = tas.iterator();
                                    Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                    while (it.hasNext() && ittas.hasNext()) {
                                        TypeMirror basetm = ittas.next();
                                        TypeMirror stm = it.next();
                                        if (basetm != stm) {
                                            if (stm.getKind() == TypeKind.TYPEVAR) {
                                                map.put(((TypeVariable) stm).asElement(), basetm);
                                            } else {
                                                continue subtypes;
                                            }
                                        }
                                    }
                                    if (it.hasNext() != ittas.hasNext()) {
                                        continue;
                                    }
                                } else {
                                    for (TypeMirror tm : e.getInterfaces()) {
                                        if (((DeclaredType) tm).asElement() == elem) {
                                            DeclaredType dt = (DeclaredType) tm;
                                            Iterator<? extends TypeMirror> ittas = tas.iterator();
                                            Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                            while (it.hasNext() && ittas.hasNext()) {
                                                TypeMirror basetm = ittas.next();
                                                TypeMirror stm = it.next();
                                                if (basetm != stm) {
                                                    if (stm.getKind() == TypeKind.TYPEVAR) {
                                                        map.put(((TypeVariable) stm).asElement(), basetm);
                                                    } else {
                                                        continue subtypes;
                                                    }
                                                }
                                            }
                                            if (it.hasNext() != ittas.hasNext()) {
                                                continue subtypes;
                                            }
                                            break;
                                        }
                                    }
                                }
                                bases.add(getDeclaredType(e, map, types));
                            }
                        }
                    } else {
                        Logger.getLogger("global").log(Level.FINE, String.format("Cannot resolve: %s on bootpath: %s classpath: %s sourcepath: %s\n", eh.toString(),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE),
                                controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE)));
                    }
                }
            }
        }
        return subtypes;
    }

    @SuppressWarnings("fallthrough")
    private void addMethodArguments(Env env, MethodInvocationTree mit) throws IOException {
        final CompilationController controller = env.getController();
        TreePath path = env.getPath();
        CompilationUnitTree root = env.getRoot();
        SourcePositions sourcePositions = env.getSourcePositions();
        List<Tree> argTypes = getArgumentsUpToPos(env, mit.getArguments(), (int) sourcePositions.getEndPosition(root, mit.getMethodSelect()), env.getOffset(), true);
        if (argTypes != null) {
            controller.toPhase(Phase.RESOLVED);
            TypeMirror[] types = new TypeMirror[argTypes.size()];
            int j = 0;
            for (Tree t : argTypes) {
                types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
            }
            List<Pair<ExecutableElement, ExecutableType>> methods = null;
            String name = null;
            Tree mid = mit.getMethodSelect();
            path = new TreePath(path, mid);
            switch (mid.getKind()) {
                case MEMBER_SELECT: {
                    ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                    path = new TreePath(path, exp);
                    final Trees trees = controller.getTrees();
                    final TypeMirror type = trees.getTypeMirror(path);
                    final Element element = trees.getElement(path);
                    final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface() || element.getKind() == TYPE_PARAMETER);
                    final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                    final Scope scope = env.getScope();
                    TypeElement enclClass = scope.getEnclosingClass();
                    final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        @Override
                        public boolean accept(Element e, TypeMirror t) {
                            return (!isStatic || e.getModifiers().contains(STATIC) || e.getKind() == CONSTRUCTOR) && (t.getKind() != TypeKind.DECLARED || trees.isAccessible(scope, e, (DeclaredType) (isSuperCall && enclType != null ? enclType : t)));
                        }
                    };
                    methods = getMatchingExecutables(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree) mid).getIdentifier().toString(), types, controller.getTypes());
                    break;
                }
                case IDENTIFIER: {
                    final Scope scope = env.getScope();
                    final TreeUtilities tu = controller.getTreeUtilities();
                    final Trees trees = controller.getTrees();
                    final TypeElement enclClass = scope.getEnclosingClass();
                    final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic())) : false;
                    final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
                    final ExecutableElement method = scope.getEnclosingMethod();
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        @Override
                        public boolean accept(Element e, TypeMirror t) {
                            switch (simplifyElementKind(e.getKind())) {
                                case LOCAL_VARIABLE:
                                case RESOURCE_VARIABLE:
                                case EXCEPTION_PARAMETER:
                                case PARAMETER:
                                    return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                            || controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                            && (!illegalForwardRefs.containsKey(e.getSimpleName()) || illegalForwardRefs.get(e.getSimpleName()).getEnclosingElement() != e.getEnclosingElement());
                                case FIELD:
                                    if (illegalForwardRefs.containsValue(e)) {
                                        return false;
                                    }
                                    if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)) {
                                        return !isStatic;
                                    }
                                default:
                                    return (!isStatic || e.getModifiers().contains(STATIC)) && trees.isAccessible(scope, e, (DeclaredType) t);
                            }
                        }
                    };
                    name = ((IdentifierTree) mid).getName().toString();
                    if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                        TypeMirror superclass = enclClass.getSuperclass();
                        methods = getMatchingExecutables(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types, controller.getTypes());
                    } else if (THIS_KEYWORD.equals(name) && enclClass != null) {
                        TypeMirror thisclass = enclClass.asType();
                        methods = getMatchingExecutables(thisclass, controller.getElementUtilities().getMembers(thisclass, acceptor), INIT, types, controller.getTypes());
                    } else {
                        methods = getMatchingExecutables(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), name, types, controller.getTypes());
                        name = null;
                    }
                    break;
                }
            }
            if (methods != null) {
                Elements elements = controller.getElements();
                for (Pair<ExecutableElement, ExecutableType> method : methods) {
                    if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(method.first())) {
                        results.add(itemFactory.createParametersItem(env.getController(), method.first(), method.second(), anchorOffset, elements.isDeprecated(method.first()), types.length, name));
                    }
                }
            }
        }
    }

    private void addConstructorArguments(Env env, NewClassTree nct) throws IOException {
        CompilationController controller = env.getController();
        TreePath path = env.getPath();
        CompilationUnitTree root = env.getRoot();
        SourcePositions sourcePositions = env.getSourcePositions();
        List<Tree> argTypes = getArgumentsUpToPos(env, nct.getArguments(), (int) sourcePositions.getEndPosition(root, nct.getIdentifier()), env.getOffset(), true);
        if (argTypes != null) {
            controller.toPhase(Phase.RESOLVED);
            TypeMirror[] types = new TypeMirror[argTypes.size()];
            int j = 0;
            for (Tree t : argTypes) {
                types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
            }
            path = new TreePath(path, nct.getIdentifier());
            final Trees trees = controller.getTrees();
            final TypeMirror type = trees.getTypeMirror(path);
            final Element el = trees.getElement(path);
            final Scope scope = env.getScope();
            final boolean isAnonymous = nct.getClassBody() != null || (el != null && (el.getKind().isInterface() || el.getModifiers().contains(ABSTRACT)));
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                @Override
                public boolean accept(Element e, TypeMirror t) {
                    return e.getKind() == CONSTRUCTOR && (trees.isAccessible(scope, e, (DeclaredType) t) || isAnonymous && e.getModifiers().contains(PROTECTED));
                }
            };
            List<Pair<ExecutableElement, ExecutableType>> ctors = getMatchingExecutables(type, controller.getElementUtilities().getMembers(type, acceptor), INIT, types, controller.getTypes());
            Elements elements = controller.getElements();
            for (Pair<ExecutableElement, ExecutableType> ctor : ctors) {
                if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(ctor.first())) {
                    results.add(itemFactory.createParametersItem(env.getController(), ctor.first(), ctor.second(), anchorOffset, elements.isDeprecated(ctor.first()), types.length, null));
                }
            }
        }
    }

    private void addAttributeValues(Env env, Element element, AnnotationMirror annotation, ExecutableElement member) throws IOException {
        CompilationController controller = env.getController();
        TreeUtilities tu = controller.getTreeUtilities();
        ElementUtilities eu = controller.getElementUtilities();
        for (javax.annotation.processing.Completion completion : SourceUtils.getAttributeValueCompletions(controller, element, annotation, member, env.getPrefix())) {
            String value = completion.getValue().trim();
            if (value.length() > 0 && startsWith(env, value)) {
                TypeMirror type = member.getReturnType();
                TypeElement typeElement = null;
                while (type.getKind() == TypeKind.ARRAY) {
                    type = ((ArrayType) type).getComponentType();
                }
                if (type.getKind() == TypeKind.DECLARED) {
                    CharSequence fqn = ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName();
                    if (JAVA_LANG_CLASS.contentEquals(fqn)) {
                        String name = value.endsWith(".class") ? value.substring(0, value.length() - 6) : value; //NOI18N
                        TypeMirror tm = tu.parseType(name, eu.outermostTypeElement(element));
                        typeElement = tm != null && tm.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) tm).asElement() : null;
                        if (typeElement != null && startsWith(env, typeElement.getSimpleName().toString())) {
                            env.addToExcludes(typeElement);
                        }
                    }
                }
                results.add(itemFactory.createAttributeValueItem(env.getController(), value, completion.getMessage(), typeElement, anchorOffset, env.getReferencesCount()));
            }
        }
    }

    private void addKeyword(Env env, String kw, String postfix, boolean smartType) {
        if (Utilities.startsWith(kw, env.getPrefix())) {
            results.add(itemFactory.createKeywordItem(kw, postfix, anchorOffset, smartType));
        }
    }

    private void addKeywordsForCU(Env env) {
        List<String> kws = new ArrayList<>();
        int offset = env.getOffset();
        String prefix = env.getPrefix();
        CompilationUnitTree cu = env.getRoot();
        boolean pkgInfo = env.getController().getTreeUtilities().isPackageInfo(cu);
        boolean mdlInfo = env.getController().getTreeUtilities().isModuleInfo(cu);
        SourcePositions sourcePositions = env.getSourcePositions();
        if (!pkgInfo && !mdlInfo) {
            kws.add(ABSTRACT_KEYWORD);
            kws.add(CLASS_KEYWORD);
            kws.add(ENUM_KEYWORD);
            kws.add(FINAL_KEYWORD);
            kws.add(INTERFACE_KEYWORD);
            if (isRecordSupported(env)) {
                kws.add(RECORD_KEYWORD);
            }
            if (isSealedSupported(env)) {
                kws.add(SEALED_KEYWORD);
                kws.add(NON_SEALED_KEYWORD);
            }
        }
        boolean beforeAnyClass = true;
        boolean beforePublicClass = true;
        for (Tree t : cu.getTypeDecls()) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                int pos = (int) sourcePositions.getEndPosition(cu, t);
                if (pos != Diagnostic.NOPOS && offset >= pos) {
                    beforeAnyClass = false;
                    if (((ClassTree) t).getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                        beforePublicClass = false;
                        break;
                    }
                }
            } else if (t.getKind() == Tree.Kind.MODULE) {
                int pos = (int) sourcePositions.getEndPosition(cu, t);
                if (pos != Diagnostic.NOPOS && offset >= pos) {
                    beforeAnyClass = false;
                }                
            }
        }
        if (beforePublicClass && !pkgInfo && !mdlInfo) {
            kws.add(PUBLIC_KEYWORD);
        }
        if (beforeAnyClass) {
            if (mdlInfo) {
                kws.add(MODULE_KEYWORD);
                kws.add(OPEN_KEYWORD);
            }
            kws.add(IMPORT_KEYWORD);
            Tree firstImport = null;
            for (Tree t : cu.getImports()) {
                firstImport = t;
                break;
            }
            Tree pd = cu.getPackageName();
            if (!mdlInfo && ((pd != null && offset <= sourcePositions.getStartPosition(cu, cu))
                    || (pd == null && (firstImport == null || sourcePositions.getStartPosition(cu, firstImport) >= offset)))) {
                kws.add(PACKAGE_KEYWORD);
            }
        }
        for (String kw : kws) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
    }

    private void addKeywordsForModuleBody(Env env) {
        String prefix = env.getPrefix();
        for (String kw : MODULE_BODY_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
    }

    private void addKeywordsForClassBody(Env env) {
        String prefix = env.getPrefix();
        for (String kw : CLASS_BODY_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
        if (isSealedSupported(env)) {
            if (Utilities.startsWith(SEALED_KEYWORD, prefix)) {
                results.add(itemFactory.createKeywordItem(SEALED_KEYWORD, SPACE, anchorOffset, false));
            }
            if (Utilities.startsWith(NON_SEALED_KEYWORD, prefix)) {
                results.add(itemFactory.createKeywordItem(NON_SEALED_KEYWORD, SPACE, anchorOffset, false));
            }
        }
        if (env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0
                && Utilities.startsWith(DEFAULT_KEYWORD, prefix)
                && env.getController().getTreeUtilities().getPathElementOfKind(Tree.Kind.INTERFACE, env.getPath()) != null) {
            results.add(itemFactory.createKeywordItem(DEFAULT_KEYWORD, SPACE, anchorOffset, false));
        }
        if (isRecordSupported(env) && Utilities.startsWith(RECORD_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(RECORD_KEYWORD, SPACE, anchorOffset, false));
        }
        addPrimitiveTypeKeywords(env);
    }

    private void addKeywordsForBlock(Env env) {
        String prefix = env.getPrefix();
        for (String kw : STATEMENT_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, null, anchorOffset, false));
            }
        }
        for (String kw : BLOCK_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
        if (isRecordSupported(env) && Utilities.startsWith(RECORD_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(RECORD_KEYWORD, SPACE, anchorOffset, false));
        }
        if (Utilities.startsWith(RETURN_KEYWORD, prefix)) {
            TreePath tp = env.getController().getTreeUtilities().getPathElementOfKind(EnumSet.of(Tree.Kind.METHOD, Tree.Kind.LAMBDA_EXPRESSION), env.getPath());
            String postfix = SPACE;
            if (tp != null) {
                if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                    Tree rt = ((MethodTree) tp.getLeaf()).getReturnType();
                    if (rt == null || (rt.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) rt).getPrimitiveTypeKind() == TypeKind.VOID)) {
                        postfix = SEMI;
                    }
                } else {
                    TypeMirror tm = env.getController().getTrees().getTypeMirror(tp);
                    if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                        ExecutableType dt = env.getController().getTypeUtilities().getDescriptorType((DeclaredType) tm);
                        if (dt != null && dt.getReturnType().getKind() == TypeKind.VOID) {
                            postfix = SEMI;
                        }
                    }
                }
            }
            results.add(itemFactory.createKeywordItem(RETURN_KEYWORD, postfix, anchorOffset, false));
        }
        boolean caseAdded = false;
        boolean breakAdded = false;
        boolean continueAdded = false;
        boolean yieldAdded = false;
        TreePath tp = env.getPath();
        while (tp != null) {
            switch (tp.getLeaf().getKind()) {
                case SWITCH:
                    CaseTree lastCase = null;
                    CompilationUnitTree root = env.getRoot();
                    SourcePositions sourcePositions = env.getSourcePositions();
                    for (CaseTree t : ((SwitchTree) tp.getLeaf()).getCases()) {
                        if (sourcePositions.getStartPosition(root, t) >= env.getOffset()) {
                            break;
                        }
                        lastCase = t;
                    }
                    if (!caseAdded && (lastCase == null || lastCase.getExpression() != null)) {
                        caseAdded = true;
                        if (Utilities.startsWith(CASE_KEYWORD, prefix)) {
                            results.add(itemFactory.createKeywordItem(CASE_KEYWORD, SPACE, anchorOffset, false));
                        }
                        if (Utilities.startsWith(DEFAULT_KEYWORD, prefix)) {
                            results.add(itemFactory.createKeywordItem(DEFAULT_KEYWORD, COLON, anchorOffset, false));
                        }
                    }
                    if (!breakAdded && Utilities.startsWith(BREAK_KEYWORD, prefix)) {
                        breakAdded = true;
                        results.add(itemFactory.createKeywordItem(BREAK_KEYWORD, withinLabeledStatement(env) ? null : SEMI, anchorOffset, false));
                    }
                    break;
                case SWITCH_EXPRESSION:
                    lastCase = null;
                    root = env.getRoot();
                    sourcePositions = env.getSourcePositions();
                    for (CaseTree t : ((SwitchExpressionTree) tp.getLeaf()).getCases()) {
                        if (sourcePositions.getStartPosition(root, t) >= env.getOffset()) {
                            break;
                        }
                        lastCase = t;
                    }
                    if (!caseAdded && (lastCase == null || lastCase.getExpression() != null)) {
                        caseAdded = true;
                        if (Utilities.startsWith(CASE_KEYWORD, prefix)) {
                            results.add(itemFactory.createKeywordItem(CASE_KEYWORD, SPACE, anchorOffset, false));
                        }
                        if (Utilities.startsWith(DEFAULT_KEYWORD, prefix)) {
                            results.add(itemFactory.createKeywordItem(DEFAULT_KEYWORD, COLON, anchorOffset, false));
                        }
                    }
                    if (!yieldAdded && Utilities.startsWith(YIELD_KEYWORD, prefix)) {
                        yieldAdded = true;
                        results.add(itemFactory.createKeywordItem(YIELD_KEYWORD, SPACE, anchorOffset, false));
                    }
                    break;
                case DO_WHILE_LOOP:
                case ENHANCED_FOR_LOOP:
                case FOR_LOOP:
                case WHILE_LOOP:
                    if (!breakAdded && Utilities.startsWith(BREAK_KEYWORD, prefix)) {
                        breakAdded = true;
                        results.add(itemFactory.createKeywordItem(BREAK_KEYWORD, withinLabeledStatement(env) ? null : SEMI, anchorOffset, false));
                    }
                    if (!continueAdded && Utilities.startsWith(CONTINUE_KEYWORD, prefix)) {
                        continueAdded = true;
                        results.add(itemFactory.createKeywordItem(CONTINUE_KEYWORD, withinLabeledStatement(env) ? null : SEMI, anchorOffset, false));
                    }
                    break;
            }
            tp = tp.getParentPath();
        }
        if (env.getController().getSourceVersion().compareTo(RELEASE_10) >= 0 && Utilities.startsWith(VAR_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(VAR_KEYWORD, SPACE, anchorOffset, false));
        }
    }

    @SuppressWarnings("fallthrough")
    private void addKeywordsForStatement(Env env) {
        String prefix = env.getPrefix();
        for (String kw : STATEMENT_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, null, anchorOffset, false));
            }
        }
        for (String kw : STATEMENT_SPACE_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
        if (Utilities.startsWith(RETURN_KEYWORD, prefix)) {
            TreePath tp = env.getController().getTreeUtilities().getPathElementOfKind(EnumSet.of(Tree.Kind.METHOD, Tree.Kind.LAMBDA_EXPRESSION), env.getPath());
            String postfix = SPACE;
            if (tp != null) {
                if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                    Tree rt = ((MethodTree) tp.getLeaf()).getReturnType();
                    if (rt == null || (rt.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) rt).getPrimitiveTypeKind() == TypeKind.VOID)) {
                        postfix = SEMI;
                    }
                } else {
                    TypeMirror tm = env.getController().getTrees().getTypeMirror(tp);
                    if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                        ExecutableType dt = env.getController().getTypeUtilities().getDescriptorType((DeclaredType) tm);
                        if (dt != null && dt.getReturnType().getKind() == TypeKind.VOID) {
                            postfix = SEMI;
                        }
                    }
                }
            }
            results.add(itemFactory.createKeywordItem(RETURN_KEYWORD, postfix, anchorOffset, false));
        }
        TreePath tp = env.getPath();
        boolean cAdded = false;
        boolean bAdded = false;
        boolean yAdded = false;
        while (tp != null && !(cAdded && bAdded)) {
            switch (tp.getLeaf().getKind()) {
                case DO_WHILE_LOOP:
                case ENHANCED_FOR_LOOP:
                case FOR_LOOP:
                case WHILE_LOOP:
                    if (!cAdded && Utilities.startsWith(CONTINUE_KEYWORD, prefix)) {
                        results.add(itemFactory.createKeywordItem(CONTINUE_KEYWORD, SEMI, anchorOffset, false));
                        cAdded = true;
                    }
                case SWITCH:
                    if (!bAdded && Utilities.startsWith(BREAK_KEYWORD, prefix)) {
                        results.add(itemFactory.createKeywordItem(BREAK_KEYWORD, SEMI, anchorOffset, false));
                        bAdded = true;
                    }
                    break;
                case SWITCH_EXPRESSION:
                    if (!yAdded && Utilities.startsWith(YIELD_KEYWORD, prefix)) {
                        results.add(itemFactory.createKeywordItem(YIELD_KEYWORD, SPACE, anchorOffset, false));
                        yAdded = true;
                    }
                    break;
            }
            tp = tp.getParentPath();
        }
    }

    private void addValueKeywords(Env env) throws IOException {
        String prefix = env.getPrefix();
        boolean smartType = false;
        if (!options.contains(Options.ALL_COMPLETION)) {
            Set<? extends TypeMirror> smartTypes = getSmartTypes(env);
            if (smartTypes != null && !smartTypes.isEmpty()) {
                for (TypeMirror st : smartTypes) {
                    if (st.getKind() == TypeKind.BOOLEAN) {
                        smartType = true;
                        break;
                    }
                }
            }
        }
        if (Utilities.startsWith(FALSE_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(FALSE_KEYWORD, null, anchorOffset, smartType));
        }
        if (Utilities.startsWith(TRUE_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(TRUE_KEYWORD, null, anchorOffset, smartType));
        }
        boolean isVar = env.getPath().getLeaf().getKind() == Tree.Kind.VARIABLE &&
                        env.getController().getTreeUtilities().isSynthetic(new TreePath(env.getPath(), ((VariableTree) env.getPath().getLeaf()).getType()));
        if (Utilities.startsWith(NULL_KEYWORD, prefix) && !isVar) {
            results.add(itemFactory.createKeywordItem(NULL_KEYWORD, null, anchorOffset, false));
        }
        if (Utilities.startsWith(NEW_KEYWORD, prefix)) {
            results.add(itemFactory.createKeywordItem(NEW_KEYWORD, SPACE, anchorOffset, false));
        }
    }

    private void addPrimitiveTypeKeywords(Env env) {
        String prefix = env.getPrefix();
        for (String kw : PRIM_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, null, anchorOffset, false));
            }
        }
    }

    private void addClassModifiers(Env env, Set<Modifier> modifiers) {
        String prefix = env.getPrefix();
        List<String> kws = new ArrayList<>();
        if (!modifiers.contains(PUBLIC) && !modifiers.contains(PRIVATE)) {
            kws.add(PUBLIC_KEYWORD);
        }
        if (!modifiers.contains(FINAL)) {
            if (!modifiers.contains(ABSTRACT)) {
                kws.add(ABSTRACT_KEYWORD);
            }
            if (!modifiers.contains(SEALED) && !modifiers.contains(NON_SEALED)) {
                if (!modifiers.contains(ABSTRACT)) {
                    kws.add(FINAL_KEYWORD);
                }
                if (isSealedSupported(env)) {
                    kws.add(SEALED_KEYWORD);
                    kws.add(NON_SEALED_KEYWORD);
                }
            }
        }
        kws.add(CLASS_KEYWORD);
        kws.add(INTERFACE_KEYWORD);
        kws.add(ENUM_KEYWORD);
        if (isRecordSupported(env)) {
            kws.add(RECORD_KEYWORD);
        }
        for (String kw : kws) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
    }

    private void addMemberModifiers(Env env, Set<Modifier> modifiers, boolean isLocal) {
        String prefix = env.getPrefix();
        List<String> kws = new ArrayList<>();
        if (isLocal) {
            if (!modifiers.contains(FINAL)) {
                kws.add(FINAL_KEYWORD);
            }
        } else {
            if (!modifiers.contains(PUBLIC) && !modifiers.contains(PROTECTED) && !modifiers.contains(PRIVATE)) {
                kws.add(PUBLIC_KEYWORD);
                kws.add(PROTECTED_KEYWORD);
                kws.add(PRIVATE_KEYWORD);
            }
            if (env.getController().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0
                    && env.getController().getTreeUtilities().getPathElementOfKind(Tree.Kind.INTERFACE, env.getPath()) != null
                    && !modifiers.contains(STATIC) && !modifiers.contains(ABSTRACT) && !modifiers.contains(DEFAULT)) {
                kws.add(DEFAULT_KEYWORD);
            }
            if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT) && !modifiers.contains(VOLATILE)) {
                kws.add(FINAL_KEYWORD);
            }
            if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT) && !modifiers.contains(DEFAULT)
                    && !modifiers.contains(NATIVE) && !modifiers.contains(SYNCHRONIZED)) {
                kws.add(ABSTRACT_KEYWORD);
            }
            if (!modifiers.contains(STATIC) && !modifiers.contains(DEFAULT)) {
                kws.add(STATIC_KEYWORD);
            }
            if (!modifiers.contains(ABSTRACT) && !modifiers.contains(NATIVE)) {
                kws.add(NATIVE_KEYWORD);
            }
            if (!modifiers.contains(STRICTFP)) {
                kws.add(STRICT_KEYWORD);
            }
            if (!modifiers.contains(SYNCHRONIZED) && !modifiers.contains(ABSTRACT)) {
                kws.add(SYNCHRONIZED_KEYWORD);
            }
            if (!modifiers.contains(TRANSIENT)) {
                kws.add(TRANSIENT_KEYWORD);
            }
            if (!modifiers.contains(FINAL) && !modifiers.contains(VOLATILE)) {
                kws.add(VOLATILE_KEYWORD);
            }
            kws.add(VOID_KEYWORD);
            kws.add(CLASS_KEYWORD);
            kws.add(INTERFACE_KEYWORD);
            kws.add(ENUM_KEYWORD);
            if (isRecordSupported(env)) {
                kws.add(RECORD_KEYWORD);
            }
        }
        for (String kw : kws) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
        for (String kw : PRIM_KEYWORDS) {
            if (Utilities.startsWith(kw, prefix)) {
                results.add(itemFactory.createKeywordItem(kw, SPACE, anchorOffset, false));
            }
        }
    }

    private void addElementCreators(Env env) throws IOException {
        final CompilationController controller = env.getController();
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        final TreeUtilities tu = controller.getTreeUtilities();
        final TreePath clsPath = tu.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, env.getPath());
        if (clsPath == null) {
            return;
        }
        final ClassTree cls = (ClassTree) clsPath.getLeaf();
        final CompilationUnitTree root = env.getRoot();
        final SourcePositions sourcePositions = env.getSourcePositions();
        Tree currentMember = null;
        int nextMemberPos = (int) Diagnostic.NOPOS;
        for (Tree member : cls.getMembers()) {
            int pos = (int) sourcePositions.getStartPosition(root, member);
            if (pos >= caretOffset) {
                nextMemberPos = pos;
                break;
            }
            pos = (int) sourcePositions.getEndPosition(root, member);
            if (caretOffset < pos) {
                currentMember = member;
                nextMemberPos = pos;
                break;
            }
        }
        if (nextMemberPos > caretOffset) {
            String text = controller.getText().substring(caretOffset, nextMemberPos);
            int idx = text.indexOf('\n'); // NOI18N
            if (idx >= 0) {
                text = text.substring(0, idx);
            }
            if (text.trim().length() > 0) {
                return;
            }
        }
        final Trees trees = controller.getTrees();
        final ElementUtilities eu = controller.getElementUtilities();
        final TypeElement te = (TypeElement) trees.getElement(clsPath);
        if (te == null || te.getKind() == ElementKind.ANNOTATION_TYPE) {
            return;
        }
        final String prefix = env.getPrefix();
        final Types types = controller.getTypes();
        DeclaredType clsType = (DeclaredType) te.asType();
        if (te.getKind().isClass() || te.getKind().isInterface() && SourceVersion.RELEASE_8.compareTo(controller.getSourceVersion()) <= 0) {
            for (ExecutableElement ee : eu.findUnimplementedMethods(te)) {
                if (startsWith(env, ee.getSimpleName().toString())) {
                    TypeMirror tm = asMemberOf(ee, clsType, types);
                    if (tm.getKind() == TypeKind.EXECUTABLE) {
                        results.add(itemFactory.createOverrideMethodItem(env.getController(), ee, (ExecutableType) tm, anchorOffset, true));
                    }
                }
            }
        }
        if (te.getKind().isClass() || te.getKind().isInterface()) {
            for (ExecutableElement ee : eu.findOverridableMethods(te)) {
                if (startsWith(env, ee.getSimpleName().toString())) {
                    TypeMirror tm = asMemberOf(ee, clsType, types);
                    if (tm.getKind() == TypeKind.EXECUTABLE) {
                        results.add(itemFactory.createOverrideMethodItem(env.getController(), ee, (ExecutableType) tm, anchorOffset, false));
                    }
                }
            }
        }
        if (!te.getKind().isClass()) {
            return;
        }
        if (prefix == null || startsWith(env, "get") || startsWith(env, "set") || startsWith(env, "is")
                || startsWith(env, prefix, "get") || startsWith(env, prefix, "set") || startsWith(env, prefix, "is")) {
            CodeStyle codeStyle = CodeStyle.getDefault(controller.getDocument());
            for (VariableElement variableElement : ElementFilter.fieldsIn(controller.getElements().getAllMembers(te))) {
                Name name = variableElement.getSimpleName();
                if (!name.contentEquals(ERROR)) {
                    boolean isStatic = variableElement.getModifiers().contains(Modifier.STATIC);
                    String setterName = CodeStyleUtils.computeSetterName(name, isStatic, codeStyle);
                    String getterName = CodeStyleUtils.computeGetterName(name, variableElement.asType().getKind() == TypeKind.BOOLEAN, isStatic, codeStyle);
                    if ((prefix == null || startsWith(env, getterName)) && !eu.hasGetter(te, variableElement, codeStyle)) {
                        results.add(itemFactory.createGetterSetterMethodItem(env.getController(), variableElement, asMemberOf(variableElement, clsType, types), anchorOffset, getterName, false));
                    }
                    if ((prefix == null || startsWith(env, setterName)) && !(variableElement.getModifiers().contains(Modifier.FINAL) || eu.hasSetter(te, variableElement, codeStyle))) {
                        results.add(itemFactory.createGetterSetterMethodItem(env.getController(), variableElement, asMemberOf(variableElement, clsType, types), anchorOffset, setterName, true));
                    }
                }
            }
        }
        if (startsWith(env, te.getSimpleName().toString())) {
            final Set<? extends VariableElement> uninitializedFields = tu.getUninitializedFields(clsPath);
            final List<ExecutableElement> constructors = ElementFilter.constructorsIn(te.getEnclosedElements());
            if (currentMember != null && currentMember.getKind() == Tree.Kind.VARIABLE) {
                Element e = trees.getElement(new TreePath(clsPath, currentMember));
                if (e.getKind().isField()) {
                    uninitializedFields.remove((VariableElement) e);
                }
            }
            Element dctor2generate = null;
            Map<ExecutableElement, boolean[]> ctors2generate = new LinkedHashMap<>();
            final Set<VariableElement> uninitializedFinalFields = new LinkedHashSet<>();
            for (VariableElement ve : uninitializedFields) {
                if (ve.getModifiers().contains(Modifier.FINAL)) {
                    uninitializedFinalFields.add(ve);
                }
            }
            int ufSize = uninitializedFields.size();
            int uffSize = uninitializedFinalFields.size();
            if (cls.getKind() != Tree.Kind.ENUM && te.getSuperclass().getKind() == TypeKind.DECLARED) {
                DeclaredType superType = (DeclaredType) te.getSuperclass();
                Scope scope = env.getScope();
                for (ExecutableElement ctor : ElementFilter.constructorsIn(superType.asElement().getEnclosedElements())) {
                    if (trees.isAccessible(scope, ctor, superType)) {
                        if (dctor2generate == null || ((ExecutableElement) dctor2generate).getParameters().size() > ctor.getParameters().size()) {
                            dctor2generate = ctor;
                        }
                        ctors2generate.put(ctor, new boolean[]{uffSize > 0 && uffSize < ufSize, ufSize > 0});
                    }
                }
            } else {
                dctor2generate = te;
                ctors2generate.put(null, new boolean[]{uffSize > 0 && uffSize < ufSize, ufSize > 0});
            }
            for (ExecutableElement ee : constructors) {
                if (!eu.isSynthetic(ee)) {
                    List<? extends VariableElement> parameters = ee.getParameters();
                    if (parameters.isEmpty()) {
                        dctor2generate = null;
                    }
                    for (Map.Entry<ExecutableElement, boolean[]> entry : ctors2generate.entrySet()) {
                        List<? extends VariableElement> params = entry.getKey() != null ? entry.getKey().getParameters() : Collections.<VariableElement>emptyList();
                        int paramSize = params.size();
                        if (uffSize > 0 && uffSize < ufSize && parameters.size() == paramSize + uffSize) {
                            Iterator<? extends VariableElement> proposed = uninitializedFinalFields.iterator();
                            Iterator<? extends VariableElement> original = parameters.iterator();
                            boolean same = true;
                            while (same && proposed.hasNext() && original.hasNext()) {
                                same &= types.isSameType(proposed.next().asType(), original.next().asType());
                            }
                            if (same) {
                                proposed = params.iterator();
                                while (same && proposed.hasNext() && original.hasNext()) {
                                    same &= types.isSameType(proposed.next().asType(), original.next().asType());
                                }
                                if (same) {
                                    entry.getValue()[0] = false;
                                }
                            }
                        }
                        if (parameters.size() == paramSize + ufSize) {
                            Iterator<? extends VariableElement> proposed = uninitializedFields.iterator();
                            Iterator<? extends VariableElement> original = parameters.iterator();
                            boolean same = true;
                            while (same && proposed.hasNext() && original.hasNext()) {
                                same &= types.isSameType(proposed.next().asType(), original.next().asType());
                            }
                            if (same) {
                                proposed = params.iterator();
                                while (same && proposed.hasNext() && original.hasNext()) {
                                    same &= types.isSameType(proposed.next().asType(), original.next().asType());
                                }
                                if (same) {
                                    entry.getValue()[1] = false;
                                }
                            }
                        }
                    }
                }
            }
            if (dctor2generate != null) {
                results.add(itemFactory.createInitializeAllConstructorItem(env.getController(), true, uninitializedFinalFields, dctor2generate.getKind() == CONSTRUCTOR ? (ExecutableElement) dctor2generate : null, te, anchorOffset));
            }
            for (Map.Entry<ExecutableElement, boolean[]> entry : ctors2generate.entrySet()) {
                if (entry.getValue()[0]) {
                    results.add(itemFactory.createInitializeAllConstructorItem(env.getController(), false, uninitializedFinalFields, entry.getKey(), te, anchorOffset));
                }
                if (entry.getValue()[1]) {
                    results.add(itemFactory.createInitializeAllConstructorItem(env.getController(), false, uninitializedFields, entry.getKey(), te, anchorOffset));
                }
            }
        }
    }

    private TypeElement getTypeElement(Env env, final String simpleName) throws IOException {
        if (simpleName == null || simpleName.length() == 0) {
            return null;
        }
        final CompilationController controller = env.getController();
        final TreeUtilities tu = controller.getTreeUtilities();
        final Trees trees = controller.getTrees();
        final Scope scope = env.getScope();
        final TypeElement enclClass = scope.getEnclosingClass();
        final boolean isStatic = enclClass == null ? false
                : (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic()));
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                return (e.getKind().isClass() || e.getKind().isInterface())
                        && e.getSimpleName().contentEquals(simpleName)
                        && (!isStatic || e.getModifiers().contains(STATIC))
                        && trees.isAccessible(scope, e, (DeclaredType) t);
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            return (TypeElement) e;
        }
        acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                return e.getSimpleName().contentEquals(simpleName)
                        && trees.isAccessible(scope, (TypeElement) e);
            }
        };
        for (TypeElement e : controller.getElementUtilities().getGlobalTypes(acceptor)) {
            if (simpleName.contentEquals(e.getSimpleName())) {
                return e;
            }
        }
        return null;
    }

    @SuppressWarnings("fallthrough")
    private VariableElement getFieldOrVar(Env env, final String simpleName) throws IOException {
        if (simpleName == null || simpleName.length() == 0) {
            return null;
        }
        final CompilationController controller = env.getController();
        final Scope scope = env.getScope();
        final TypeElement enclClass = scope.getEnclosingClass();
        final boolean isStatic = enclClass == null ? false
                : (controller.getTreeUtilities().isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic()));
        final Map<Name, ? extends Element> illegalForwardRefs = env.getForwardReferences();
        final ExecutableElement method = scope.getEnclosingMethod();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror t) {
                if (!e.getSimpleName().contentEquals(simpleName)) {
                    return false;
                }
                switch (simplifyElementKind(e.getKind())) {
                    case LOCAL_VARIABLE:
                    case RESOURCE_VARIABLE:
                        if (isStatic && (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))) {
                            return false;
                        }
                    case EXCEPTION_PARAMETER:
                    case PARAMETER:
                        return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL)
                                || controller.getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0 && controller.getElementUtilities().isEffectivelyFinal((VariableElement)e))
                                && (!illegalForwardRefs.containsKey(e.getSimpleName()) || illegalForwardRefs.get(e.getSimpleName()).getEnclosingElement() != e.getEnclosingElement());
                    case FIELD:
                        if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD)) {
                            return !isStatic;
                        }
                    case ENUM_CONSTANT:
                        return !illegalForwardRefs.containsValue(e);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            return (VariableElement) e;
        }
        return null;
    }

    private TypeMirror getCorrectedReturnType(Env env, ExecutableType et, ExecutableElement el, TypeMirror site) {
        TypeMirror type = et.getReturnType();
        if (site != null && site.getKind() == TypeKind.DECLARED) {
            if ("getClass".contentEquals(el.getSimpleName()) && et.getParameterTypes().isEmpty() //NOI18N
                    && type.getKind() == TypeKind.DECLARED
                    && JAVA_LANG_CLASS.contentEquals(((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName())
                    && ((TypeElement) ((DeclaredType) type).asElement()).getTypeParameters().size() == 1) {
                Types types = env.getController().getTypes();
                type = types.getDeclaredType((TypeElement) ((DeclaredType) type).asElement(), types.getWildcardType(site, null));
            }
        }
        return type;
    }

    private boolean isOfSmartType(Env env, TypeMirror type, Set<? extends TypeMirror> smartTypes) {
        if (smartTypes == null || smartTypes.isEmpty()) {
            return false;
        }
        if (env.isInsideForEachExpression()) {
            if (type.getKind() == TypeKind.ARRAY) {
                type = ((ArrayType) type).getComponentType();
            } else if (type.getKind() == TypeKind.DECLARED) {
                Elements elements = env.getController().getElements();
                Types types = env.getController().getTypes();
                TypeElement iterableTE = elements.getTypeElement(JAVA_LANG_ITERABLE); //NOI18N
                DeclaredType iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                if (iterable != null && types.isSubtype(type, iterable)) {
                    Iterator<? extends TypeMirror> it = ((DeclaredType) type).getTypeArguments().iterator();
                    type = it.hasNext() ? it.next() : elements.getTypeElement(JAVA_LANG_OBJECT).asType(); //NOI18N
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (type.getKind() == TypeKind.EXECUTABLE) {
            Types types = env.getController().getTypes();
            TypeUtilities tu = env.getController().getTypeUtilities();
            for (TypeMirror smartType : smartTypes) {
                if (smartType.getKind() == TypeKind.DECLARED) {
                    ExecutableType descriptorType = tu.getDescriptorType((DeclaredType) smartType);
                    if (descriptorType != null && types.isSubsignature((ExecutableType) type, descriptorType)
                            && types.isSubtype(((ExecutableType) type).getReturnType(), descriptorType.getReturnType())) {
                        return true;
                    }
                }
            }
            return false;
        }
        for (TypeMirror smartType : smartTypes) {
            if (type.getKind() == TypeKind.DECLARED){
                smartType = inferDeclaredType(env.getController().getTypes(), (DeclaredType) type, smartType);
            }
            if (SourceUtils.checkTypesAssignable(env.getController(), type, smartType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopLevelClass(Tree tree, CompilationUnitTree root) {
        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind()) || (tree.getKind() == Tree.Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree) tree).getExpression().getKind() == Tree.Kind.ERRONEOUS)) {
            for (Tree t : root.getTypeDecls()) {
                if (tree == t) {
                    return true;
                }
            }
        }
        return tree.getKind() == Kind.COMPILATION_UNIT;
    }

    private static boolean isAnnonInner(ElementHandle<TypeElement> elem) {
        String name = elem.getQualifiedName();
        int idx = name.lastIndexOf('.'); //NOI18N
        String simpleName = idx > -1 ? name.substring(idx + 1) : name;
        return simpleName.length() == 0 || Character.isDigit(simpleName.charAt(0));
    }

    private boolean isOfKindAndType(TypeMirror type, Element e, EnumSet<ElementKind> kinds, TypeMirror base, Scope scope, Trees trees, Types types) {
        if (type.getKind() != TypeKind.ERROR && kinds.contains(e.getKind())) {
            if (base == null) {
                return true;
            }
            if (types.isSubtype(type, base)) {
                return true;
            }
        }
        if ((e.getKind().isClass() || e.getKind().isInterface())
                && (kinds.contains(ANNOTATION_TYPE) || kinds.contains(CLASS) || kinds.contains(ENUM) || kinds.contains(INTERFACE))) {
            DeclaredType dt = (DeclaredType) e.asType();
            for (Element ee : e.getEnclosedElements()) {
                if (trees.isAccessible(scope, ee, dt) && isOfKindAndType(ee.asType(), ee, kinds, base, scope, trees, types)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAccessibleNonFinalType(Element e, Scope scope, Trees trees) {
        if (e.getKind().isClass() || e.getKind().isInterface()) {
            if (!e.getModifiers().contains(Modifier.FINAL)) {
                return true;
            }
            DeclaredType dt = (DeclaredType) e.asType();
            for (Element ee : e.getEnclosedElements()) {
                if (trees.isAccessible(scope, ee, dt) && containsAccessibleNonFinalType(ee, scope, trees)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<? extends TypeMirror> getSmartTypes(Env env) throws IOException {
        Set<? extends TypeMirror> smartTypes = env.getSmartTypes();
        if (smartTypes == null) {
            CompilationController controller = env.getController();
            controller.toPhase(JavaSource.Phase.RESOLVED);
            smartTypes = getSmartTypesImpl(env);
            if (smartTypes != null) {
                Iterator<? extends TypeMirror> it = smartTypes.iterator();
                TypeMirror err = null;
                if (it.hasNext()) {
                    err = it.next();
                    if (it.hasNext() || err.getKind() != TypeKind.ERROR) {
                        err = null;
                    }
                }
                if (err != null) {
                    HashSet<TypeMirror> st = new HashSet<>();
                    Types types = controller.getTypes();
                    TypeElement te = (TypeElement) ((DeclaredType) err).asElement();
                    if (te.getQualifiedName() == te.getSimpleName()) {
                        ClassIndex ci = controller.getClasspathInfo().getClassIndex();
                        for (ElementHandle<TypeElement> eh : ci.getDeclaredTypes(te.getSimpleName().toString(), ClassIndex.NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                            te = eh.resolve(controller);
                            if (te != null) {
                                st.add(types.erasure(te.asType()));
                            }
                        }
                    }
                    smartTypes = st;
                }
            }
            env.setSmartTypes(smartTypes);
        }
        return smartTypes;
    }

    @SuppressWarnings("fallthrough")
    private Set<? extends TypeMirror> getSmartTypesImpl(Env env) throws IOException {
        int offset = env.getOffset();
        final CompilationController controller = env.getController();
        TreeUtilities tu = controller.getTreeUtilities();
        TreePath path = tu.pathFor(offset);
        Tree lastTree = null;
        int dim = 0;
        while (path != null) {
            Tree tree = path.getLeaf();
            switch (tree.getKind()) {
                case VARIABLE:
                    TypeMirror type = controller.getTrees().getTypeMirror(path);
                    if (type == null) {
                        return null;
                    }
                    while (dim-- > 0) {
                        if (type.getKind() == TypeKind.ARRAY) {
                            type = ((ArrayType) type).getComponentType();
                        } else {
                            return null;
                        }
                    }
                    return type != null ? Collections.singleton(type) : null;
                case ASSIGNMENT:
                    type = controller.getTrees().getTypeMirror(new TreePath(path, ((AssignmentTree) tree).getVariable()));
                    if (type == null) {
                        return null;
                    }
                    TreePath parentPath = path.getParentPath();
                    if (parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION && type.getKind() == TypeKind.EXECUTABLE) {
                        type = ((ExecutableType) type).getReturnType();
                        while (dim-- > 0) {
                            if (type.getKind() == TypeKind.ARRAY) {
                                type = ((ArrayType) type).getComponentType();
                            } else {
                                return null;
                            }
                        }
                        if (type.getKind() == TypeKind.ARRAY) {
                            type = ((ArrayType) type).getComponentType();
                        }
                    }
                    return type != null ? Collections.singleton(type) : null;
                case RETURN:
                    TreePath methodOrLambdaPath = tu.getPathElementOfKind(EnumSet.of(Tree.Kind.METHOD, Tree.Kind.LAMBDA_EXPRESSION), path);
                    if (methodOrLambdaPath == null) {
                        return null;
                    }
                    if (methodOrLambdaPath.getLeaf().getKind() == Tree.Kind.METHOD) {
                        Tree retTree = ((MethodTree) methodOrLambdaPath.getLeaf()).getReturnType();
                        if (retTree == null) {
                            return null;
                        }
                        type = controller.getTrees().getTypeMirror(new TreePath(methodOrLambdaPath, retTree));
                        if (type == null && JavaSource.Phase.RESOLVED.compareTo(controller.getPhase()) > 0) {
                            controller.toPhase(Phase.RESOLVED);
                            type = controller.getTrees().getTypeMirror(new TreePath(methodOrLambdaPath, retTree));
                        }
                        return type != null ? Collections.singleton(type) : null;
                    } else {
                        type = controller.getTrees().getTypeMirror(methodOrLambdaPath);
                        if (type != null && type.getKind() == TypeKind.DECLARED) {
                            ExecutableType descType = controller.getTypeUtilities().getDescriptorType((DeclaredType) type);
                            if (descType != null) {
                                return Collections.singleton(descType.getReturnType());
                            }
                        }
                    }
                    break;
                case THROW:
                    TreePath methodPath = tu.getPathElementOfKind(Tree.Kind.METHOD, path);
                    if (methodPath == null) {
                        return null;
                    }
                    HashSet<TypeMirror> ret = new HashSet<>();
                    Trees trees = controller.getTrees();
                    for (ExpressionTree thr : ((MethodTree) methodPath.getLeaf()).getThrows()) {
                        type = trees.getTypeMirror(new TreePath(methodPath, thr));
                        if (type == null && JavaSource.Phase.RESOLVED.compareTo(controller.getPhase()) > 0) {
                            controller.toPhase(Phase.RESOLVED);
                            type = trees.getTypeMirror(new TreePath(methodPath, thr));
                        }
                        if (type != null) {
                            ret.add(type);
                        }
                    }
                    return ret;
                case TRY:
                    TryTree tt = (TryTree) tree;
                    BlockTree tryBlock = tt.getBlock();
                    SourcePositions sourcePositions = env.getSourcePositions();
                    if (tryBlock != null && sourcePositions.getStartPosition(env.getRoot(), tryBlock) <= offset) {
                        return null;
                    }
                    TypeElement te = controller.getElements().getTypeElement("java.lang.AutoCloseable"); //NOI18N
                    return te != null ? Collections.singleton(controller.getTypes().getDeclaredType(te)) : null;
                case IF:
                    IfTree iff = (IfTree) tree;
                    return iff.getCondition() == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                case WHILE_LOOP:
                    WhileLoopTree wl = (WhileLoopTree) tree;
                    return wl.getCondition() == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                case DO_WHILE_LOOP:
                    DoWhileLoopTree dwl = (DoWhileLoopTree) tree;
                    return dwl.getCondition() == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                case FOR_LOOP:
                    ForLoopTree fl = (ForLoopTree) tree;
                    Tree cond = fl.getCondition();
                    if (lastTree != null) {
                        if (cond instanceof ErroneousTree) {
                            Iterator<? extends Tree> itt = ((ErroneousTree) cond).getErrorTrees().iterator();
                            if (itt.hasNext()) {
                                cond = itt.next();
                            }
                        }
                        return cond == lastTree ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null;
                    }
                    sourcePositions = env.getSourcePositions();
                    CompilationUnitTree root = env.getRoot();
                    if (cond != null && sourcePositions.getEndPosition(root, cond) < offset) {
                        return null;
                    }
                    Tree lastInit = null;
                    for (Tree init : fl.getInitializer()) {
                        if (sourcePositions.getEndPosition(root, init) >= offset) {
                            return null;
                        }
                        lastInit = init;
                    }
                    String text;
                    if (lastInit == null) {
                        text = controller.getText().substring((int) sourcePositions.getStartPosition(root, fl), offset).trim();
                        int idx = text.indexOf('('); //NOI18N
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                        }
                    } else {
                        text = controller.getText().substring((int) sourcePositions.getEndPosition(root, lastInit), offset).trim();
                    }
                    return ";".equals(text) ? Collections.<TypeMirror>singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN)) : null; //NOI18N
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree efl = (EnhancedForLoopTree) tree;
                    Tree expr = efl.getExpression();
                    if (lastTree != null) {
                        if (expr instanceof ErroneousTree) {
                            Iterator<? extends Tree> itt = ((ErroneousTree) expr).getErrorTrees().iterator();
                            if (itt.hasNext()) {
                                expr = itt.next();
                            }
                        }
                        if (expr != lastTree) {
                            return null;
                        }
                    } else {
                        sourcePositions = env.getSourcePositions();
                        root = env.getRoot();
                        if (efl.getVariable() == null || sourcePositions.getEndPosition(root, efl.getVariable()) > offset) {
                            text = controller.getText().substring((int) sourcePositions.getStartPosition(root, efl), offset).trim();
                            int idx = text.indexOf('('); //NOI18N
                            if (idx >= 0) {
                                text = text.substring(idx + 1);
                            }
                        } else {
                            text = controller.getText().substring((int) sourcePositions.getEndPosition(root, efl.getVariable()), offset).trim();
                        }
                        if (!":".equals(text)) {
                            return null;
                        }
                    }
                    TypeMirror var = efl.getVariable() != null ? controller.getTrees().getTypeMirror(new TreePath(path, efl.getVariable())) : null;
                    return var != null ? Collections.singleton(var) : null;
                case SWITCH:
                    SwitchTree sw = (SwitchTree) tree;
                    if (sw.getExpression() != lastTree && sw.getExpression().getKind() != Tree.Kind.ERRONEOUS) {
                        return null;
                    }
                    ret = new HashSet<>();
                    Types types = controller.getTypes();
                    ret.add(controller.getTypes().getPrimitiveType(TypeKind.INT));
                    te = controller.getElements().getTypeElement("java.lang.Enum"); //NOI18N
                    if (te != null) {
                        ret.add(types.getDeclaredType(te));
                    }
                    if (controller.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                        te = controller.getElements().getTypeElement("java.lang.String"); //NOI18N
                        if (te != null) {
                            ret.add(types.getDeclaredType(te));
                        }
                    }
                    return ret;
                case SWITCH_EXPRESSION:
                    SwitchExpressionTree sew = (SwitchExpressionTree) tree;
                    if (sew.getExpression() != lastTree && sew.getExpression().getKind() != Tree.Kind.ERRONEOUS) {
                        return null;
                    }
                    ret = new HashSet<>();
                    types = controller.getTypes();
                    ret.add(controller.getTypes().getPrimitiveType(TypeKind.INT));
                    te = controller.getElements().getTypeElement("java.lang.Enum"); //NOI18N
                    if (te != null) {
                        ret.add(types.getDeclaredType(te));
                    }
                    if (controller.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                        te = controller.getElements().getTypeElement("java.lang.String"); //NOI18N
                        if (te != null) {
                            ret.add(types.getDeclaredType(te));
                        }
                    }
                    return ret;
                case METHOD_INVOCATION:
                    MethodInvocationTree mi = (MethodInvocationTree) tree;
                    sourcePositions = env.getSourcePositions();
                    root = env.getRoot();
                    List<Tree> argTypes = getArgumentsUpToPos(env, mi.getArguments(), (int) sourcePositions.getEndPosition(root, mi.getMethodSelect()), lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset, true);
                    if (argTypes != null) {
                        TypeMirror[] args = new TypeMirror[argTypes.size()];
                        int j = 0;
                        for (Tree t : argTypes) {
                            args[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                        }
                        TypeMirror[] targs = null;
                        if (!mi.getTypeArguments().isEmpty()) {
                            targs = new TypeMirror[mi.getTypeArguments().size()];
                            j = 0;
                            for (Tree t : mi.getTypeArguments()) {
                                TypeMirror ta = controller.getTrees().getTypeMirror(new TreePath(path, t));
                                if (ta == null) {
                                    return null;
                                }
                                targs[j++] = ta;
                            }
                        }
                        Tree mid = mi.getMethodSelect();
                        path = new TreePath(path, mid);
                        TypeMirror typeMirror = controller.getTrees().getTypeMirror(path);
                        final ExecutableType midTM = typeMirror != null && typeMirror.getKind() == TypeKind.EXECUTABLE ? (ExecutableType) typeMirror : null;
                        final ExecutableElement midEl = midTM == null ? null : (ExecutableElement) controller.getTrees().getElement(path);
                        switch (mid.getKind()) {
                            case MEMBER_SELECT: {
                                String name = ((MemberSelectTree) mid).getIdentifier().toString();
                                ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                                path = new TreePath(path, exp);
                                final TypeMirror tm = controller.getTrees().getTypeMirror(path);
                                final Element el = controller.getTrees().getElement(path);
                                final Trees trs = controller.getTrees();
                                if (el != null && tm.getKind() == TypeKind.DECLARED) {
                                    final boolean isStatic = el.getKind().isClass() || el.getKind().isInterface() || el.getKind() == TYPE_PARAMETER;
                                    final boolean isSuperCall = el.getKind().isField() && el.getSimpleName().contentEquals(SUPER_KEYWORD);
                                    final Scope scope = env.getScope();
                                    TypeElement enclClass = scope.getEnclosingClass();
                                    final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                        @Override
                                        public boolean accept(Element e, TypeMirror t) {
                                            return e.getKind() == METHOD && (!isStatic || e.getModifiers().contains(STATIC)) && trs.isAccessible(scope, e, (DeclaredType) (isSuperCall && enclType != null ? enclType : t));
                                        }
                                    };
                                    return getMatchingArgumentTypes(tm, controller.getElementUtilities().getMembers(tm, acceptor), name, args, targs, midEl, midTM, controller.getTypes(), controller.getTypeUtilities());
                                }
                                return null;
                            }
                            case IDENTIFIER: {
                                String name = ((IdentifierTree) mid).getName().toString();
                                final Scope scope = env.getScope();
                                final Trees trs = controller.getTrees();
                                final TypeElement enclClass = scope.getEnclosingClass();
                                final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic())) : false;
                                if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                        @Override
                                        public boolean accept(Element e, TypeMirror t) {
                                            return e.getKind() == CONSTRUCTOR && trs.isAccessible(scope, e, (DeclaredType) t);
                                        }
                                    };
                                    TypeMirror superclass = enclClass.getSuperclass();
                                    return getMatchingArgumentTypes(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, args, targs, midEl, midTM, controller.getTypes(), controller.getTypeUtilities());
                                }
                                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                    @Override
                                    public boolean accept(Element e, TypeMirror t) {
                                        return e.getKind() == METHOD && (!isStatic || e.getModifiers().contains(STATIC)) && trs.isAccessible(scope, e, (DeclaredType) t);
                                    }
                                };
                                return getMatchingArgumentTypes(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), THIS_KEYWORD.equals(name) ? INIT : name, args, targs, midEl, midTM, controller.getTypes(), controller.getTypeUtilities());
                            }
                        }
                    }
                    break;
                case NEW_CLASS:
                    NewClassTree nc = (NewClassTree) tree;
                    sourcePositions = env.getSourcePositions();
                    root = env.getRoot();
                    int idEndPos = (int) sourcePositions.getEndPosition(root, nc.getIdentifier());
                    if (idEndPos < 0) {
                        idEndPos = (int) sourcePositions.getStartPosition(root, nc);
                    }
                    if (idEndPos < 0 || idEndPos >= offset || controller.getText().substring(idEndPos, offset).indexOf('(') < 0) {
                        break;
                    }
                    argTypes = getArgumentsUpToPos(env, nc.getArguments(), idEndPos, lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset, true);
                    if (argTypes != null) {
                        trees = controller.getTrees();
                        TypeMirror[] args = new TypeMirror[argTypes.size()];
                        int j = 0;
                        for (Tree t : argTypes) {
                            args[j++] = trees.getTypeMirror(new TreePath(path, t));
                        }
                        TypeMirror[] targs = null;
                        if (!nc.getTypeArguments().isEmpty()) {
                            targs = new TypeMirror[nc.getTypeArguments().size()];
                            j = 0;
                            for (Tree t : nc.getTypeArguments()) {
                                TypeMirror ta = trees.getTypeMirror(new TreePath(path, t));
                                if (ta == null) {
                                    return null;
                                }
                                targs[j++] = ta;
                            }
                        }
                        Element elem = controller.getTrees().getElement(path);
                        ExecutableElement ncElem = elem != null && elem.getKind() == CONSTRUCTOR ? (ExecutableElement) elem : null;
                        TypeMirror ncTM = ncElem != null ? ncElem.asType() : null;
                        ExecutableType ncType = ncTM != null && ncTM.getKind() == TypeKind.EXECUTABLE ? (ExecutableType) ncTM : null;
                        Tree mid = nc.getIdentifier();
                        path = new TreePath(path, mid);
                        TypeMirror tm = trees.getTypeMirror(path);
                        if (tm != null && tm.getKind() == TypeKind.ERROR && path.getLeaf().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                            path = new TreePath(path, ((ParameterizedTypeTree) path.getLeaf()).getType());
                            tm = trees.getTypeMirror(path);
                        }
                        final Element el = controller.getTrees().getElement(path);
                        final Trees trs = controller.getTrees();
                        if (el != null && tm.getKind() == TypeKind.DECLARED) {
                            final Scope scope = env.getScope();
                            final boolean isAnonymous = nc.getClassBody() != null || el.getKind().isInterface() || el.getModifiers().contains(ABSTRACT);
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                @Override
                                public boolean accept(Element e, TypeMirror t) {
                                    return e.getKind() == CONSTRUCTOR && (trs.isAccessible(scope, e, (DeclaredType) t) || isAnonymous && e.getModifiers().contains(PROTECTED));
                                }
                            };
                            return getMatchingArgumentTypes(tm, controller.getElementUtilities().getMembers(tm, acceptor), INIT, args, targs, ncElem, ncType, controller.getTypes(), controller.getTypeUtilities());
                        }
                        return null;
                    }
                    break;
                case NEW_ARRAY:
                    NewArrayTree nat = (NewArrayTree) tree;
                    Tree arrayType = nat.getType();
                    if (arrayType == null) {
                        dim++;
                        break;
                    }
                    sourcePositions = env.getSourcePositions();
                    root = env.getRoot();
                    int typeEndPos = (int) sourcePositions.getEndPosition(root, arrayType);
                    if (typeEndPos > offset) {
                        break;
                    }
                    text = controller.getText().substring(typeEndPos, offset);
                    if (text.indexOf('{') >= 0) {
                        type = controller.getTrees().getTypeMirror(new TreePath(path, arrayType));
                        while (dim-- > 0) {
                            if (type.getKind() == TypeKind.ARRAY) {
                                type = ((ArrayType) type).getComponentType();
                            } else {
                                return null;
                            }
                        }
                        return type != null ? Collections.singleton(type) : null;
                    }
                    if (text.trim().endsWith("[")) //NOI18N
                    {
                        return Collections.singleton(controller.getTypes().getPrimitiveType(TypeKind.INT));
                    }
                    return null;
                case LAMBDA_EXPRESSION:
                    LambdaExpressionTree let = (LambdaExpressionTree) tree;
                    int pos = (int) env.getSourcePositions().getStartPosition(env.getRoot(), let.getBody());
                    if (offset <= pos && findLastNonWhitespaceToken(env, tree, offset).token().id() != JavaTokenId.ARROW
                            || lastTree != null && lastTree.getKind() == Tree.Kind.BLOCK) {
                        break;
                    }
                    type = controller.getTrees().getTypeMirror(path);
                    if (type != null && type.getKind() == TypeKind.DECLARED) {
                        ExecutableType descType = controller.getTypeUtilities().getDescriptorType((DeclaredType) type);
                        if (descType != null) {
                            return Collections.singleton(descType.getReturnType());
                        }
                    }
                    break;
                case CASE:
                    CaseTree ct = (CaseTree) tree;
                    ExpressionTree exp = ct.getExpression();
                    if (exp != null && env.getSourcePositions().getEndPosition(env.getRoot(), exp) >= offset) {
                        parentPath = path.getParentPath();
                        if (parentPath.getLeaf().getKind() == Tree.Kind.SWITCH) {
                            exp = ((SwitchTree) parentPath.getLeaf()).getExpression();
                            type = controller.getTrees().getTypeMirror(new TreePath(parentPath, exp));
                            return type != null ? Collections.singleton(type) : null;
                        }
                    }
                    return null;
                case ANNOTATION:
                    AnnotationTree ann = (AnnotationTree) tree;
                    pos = (int) env.getSourcePositions().getStartPosition(env.getRoot(), ann.getAnnotationType());
                    if (offset <= pos) {
                        break;
                    }
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), ann.getAnnotationType());
                    if (offset < pos) {
                        break;
                    }
                    text = controller.getText().substring(pos, offset).trim();
                    if ("(".equals(text) || text.endsWith("{") || text.endsWith(",")) { //NOI18N
                        TypeElement el = (TypeElement) controller.getTrees().getElement(new TreePath(path, ann.getAnnotationType()));
                        if (el != null) {
                            for (Element ee : el.getEnclosedElements()) {
                                if (ee.getKind() == METHOD && "value".contentEquals(ee.getSimpleName())) {
                                    type = ((ExecutableElement) ee).getReturnType();
                                    while (dim-- > 0) {
                                        if (type.getKind() == TypeKind.ARRAY) {
                                            type = ((ArrayType) type).getComponentType();
                                        } else {
                                            return null;
                                        }
                                    }
                                    if (type.getKind() == TypeKind.ARRAY) {
                                        type = ((ArrayType) type).getComponentType();
                                    }
                                    return type != null ? Collections.singleton(type) : null;
                                }
                            }
                        }
                    }
                    return null;
                case REMAINDER_ASSIGNMENT:
                case AND_ASSIGNMENT:
                case XOR_ASSIGNMENT:
                case OR_ASSIGNMENT:
                case LEFT_SHIFT_ASSIGNMENT:
                case RIGHT_SHIFT_ASSIGNMENT:
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    CompoundAssignmentTree cat = (CompoundAssignmentTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), cat.getVariable());
                    if (offset <= pos) {
                        break;
                    }
                    ret = new HashSet<>();
                    types = controller.getTypes();
                    ret.add(types.getPrimitiveType(TypeKind.BYTE));
                    ret.add(types.getPrimitiveType(TypeKind.CHAR));
                    ret.add(types.getPrimitiveType(TypeKind.INT));
                    ret.add(types.getPrimitiveType(TypeKind.LONG));
                    ret.add(types.getPrimitiveType(TypeKind.SHORT));
                    return ret;
                case LEFT_SHIFT:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case AND:
                case OR:
                case XOR:
                case REMAINDER:
                    BinaryTree bt = (BinaryTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), bt.getLeftOperand());
                    if (offset <= pos) {
                        break;
                    }
                case BITWISE_COMPLEMENT:
                    ret = new HashSet<>();
                    types = controller.getTypes();
                    ret.add(types.getPrimitiveType(TypeKind.BYTE));
                    ret.add(types.getPrimitiveType(TypeKind.CHAR));
                    ret.add(types.getPrimitiveType(TypeKind.INT));
                    ret.add(types.getPrimitiveType(TypeKind.LONG));
                    ret.add(types.getPrimitiveType(TypeKind.SHORT));
                    return ret;
                case CONDITIONAL_AND:
                case CONDITIONAL_OR:
                    bt = (BinaryTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), bt.getLeftOperand());
                    if (offset <= pos) {
                        break;
                    }
                case LOGICAL_COMPLEMENT:
                    return Collections.singleton(controller.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
                case PLUS:
                case EQUAL_TO:
                case NOT_EQUAL_TO:
                    bt = (BinaryTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), bt.getLeftOperand());
                    if (offset <= pos) {
                        break;
                    }
                    TypeMirror tm = controller.getTrees().getTypeMirror(new TreePath(path, bt.getLeftOperand()));
                    if (tm == null) {
                        return null;
                    }
                    if (tm.getKind().isPrimitive()) {
                        ret = new HashSet<>();
                        types = controller.getTypes();
                        ret.add(types.getPrimitiveType(TypeKind.BYTE));
                        ret.add(types.getPrimitiveType(TypeKind.CHAR));
                        ret.add(types.getPrimitiveType(TypeKind.DOUBLE));
                        ret.add(types.getPrimitiveType(TypeKind.FLOAT));
                        ret.add(types.getPrimitiveType(TypeKind.INT));
                        ret.add(types.getPrimitiveType(TypeKind.LONG));
                        ret.add(types.getPrimitiveType(TypeKind.SHORT));
                        return ret;
                    }
                    return Collections.singleton(tm);
                case PLUS_ASSIGNMENT:
                    cat = (CompoundAssignmentTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), cat.getVariable());
                    if (offset <= pos) {
                        break;
                    }
                    tm = controller.getTrees().getTypeMirror(new TreePath(path, cat.getVariable()));
                    if (tm == null) {
                        return null;
                    }
                    if (tm.getKind().isPrimitive()) {
                        ret = new HashSet<>();
                        types = controller.getTypes();
                        ret.add(types.getPrimitiveType(TypeKind.BYTE));
                        ret.add(types.getPrimitiveType(TypeKind.CHAR));
                        ret.add(types.getPrimitiveType(TypeKind.DOUBLE));
                        ret.add(types.getPrimitiveType(TypeKind.FLOAT));
                        ret.add(types.getPrimitiveType(TypeKind.INT));
                        ret.add(types.getPrimitiveType(TypeKind.LONG));
                        ret.add(types.getPrimitiveType(TypeKind.SHORT));
                        return ret;
                    }
                    return Collections.singleton(tm);
                case MULTIPLY_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                case MINUS_ASSIGNMENT:
                    cat = (CompoundAssignmentTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), cat.getVariable());
                    if (offset <= pos) {
                        break;
                    }
                    ret = new HashSet<>();
                    types = controller.getTypes();
                    ret.add(types.getPrimitiveType(TypeKind.BYTE));
                    ret.add(types.getPrimitiveType(TypeKind.CHAR));
                    ret.add(types.getPrimitiveType(TypeKind.DOUBLE));
                    ret.add(types.getPrimitiveType(TypeKind.FLOAT));
                    ret.add(types.getPrimitiveType(TypeKind.INT));
                    ret.add(types.getPrimitiveType(TypeKind.LONG));
                    ret.add(types.getPrimitiveType(TypeKind.SHORT));
                    return ret;
                case DIVIDE:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case MINUS:
                case MULTIPLY:
                    bt = (BinaryTree) tree;
                    pos = (int) env.getSourcePositions().getEndPosition(env.getRoot(), bt.getLeftOperand());
                    if (offset <= pos) {
                        break;
                    }
                case PREFIX_INCREMENT:
                case PREFIX_DECREMENT:
                case UNARY_PLUS:
                case UNARY_MINUS:
                    ret = new HashSet<>();
                    types = controller.getTypes();
                    ret.add(types.getPrimitiveType(TypeKind.BYTE));
                    ret.add(types.getPrimitiveType(TypeKind.CHAR));
                    ret.add(types.getPrimitiveType(TypeKind.DOUBLE));
                    ret.add(types.getPrimitiveType(TypeKind.FLOAT));
                    ret.add(types.getPrimitiveType(TypeKind.INT));
                    ret.add(types.getPrimitiveType(TypeKind.LONG));
                    ret.add(types.getPrimitiveType(TypeKind.SHORT));
                    return ret;
                case EXPRESSION_STATEMENT:
                    exp = ((ExpressionStatementTree) tree).getExpression();
                    if (exp.getKind() == Tree.Kind.PARENTHESIZED) {
                        text = controller.getText().substring((int) env.getSourcePositions().getStartPosition(env.getRoot(), exp), offset).trim();
                        if (text.endsWith(")")) //NOI18N
                        {
                            return null;
                        }
                    } else if (exp.getKind() == Tree.Kind.ERRONEOUS) {
                        Iterator<? extends Tree> it = ((ErroneousTree) exp).getErrorTrees().iterator();
                        if (it.hasNext()) {
                            Tree t = it.next();
                            if (t.getKind() == Tree.Kind.IDENTIFIER && YIELD_KEYWORD.contentEquals(((IdentifierTree) t).getName())) {
                                TreePath sPath = tu.getPathElementOfKind(Tree.Kind.SWITCH_EXPRESSION, path);
                                if (sPath != null) {
                                    path = sPath;
                                }
                            }
                        }
                    }
                    break;
                case YIELD:
                    TreePath sPath = tu.getPathElementOfKind(Tree.Kind.SWITCH_EXPRESSION, path);
                    if (sPath != null) {
                        path = sPath;
                    }
                    break;
                case BLOCK:
                    return null;
            }
            lastTree = tree;
            path = path.getParentPath();
        }
        return null;
    }

    private List<Pair<ExecutableElement, ExecutableType>> getMatchingExecutables(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
        List<Pair<ExecutableElement, ExecutableType>> ret = new ArrayList<>();
        for (Element e : elements) {
            if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                List<? extends VariableElement> params = ((ExecutableElement) e).getParameters();
                int parSize = params.size();
                boolean varArgs = ((ExecutableElement) e).isVarArgs();
                if (!varArgs && (parSize < argTypes.length)) {
                    continue;
                }
                ExecutableType eType = (ExecutableType) asMemberOf(e, type, types);
                if (parSize == 0) {
                    ret.add(Pair.of((ExecutableElement) e, eType));
                } else {
                    Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                    TypeMirror param = null;
                    for (int i = 0; i <= argTypes.length; i++) {
                        if (parIt.hasNext()) {
                            param = parIt.next();
                            if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
                                param = ((ArrayType) param).getComponentType();
                            }
                        } else if (!varArgs) {
                            break;
                        }
                        if (i == argTypes.length) {
                            ret.add(Pair.of((ExecutableElement) e, eType));
                            break;
                        }
                        if (argTypes[i] == null || !types.isAssignable(argTypes[i], param)) {
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private Set<TypeMirror> getMatchingArgumentTypes(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, TypeMirror[] typeArgTypes, ExecutableElement prototypeSym, ExecutableType prototype, Types types, TypeUtilities tu) {
        Set<TypeMirror> ret = new HashSet<>();
        List<TypeMirror> tatList = typeArgTypes != null ? Arrays.asList(typeArgTypes) : null;
        for (Element e : elements) {
            if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                List<? extends VariableElement> params = ((ExecutableElement) e).getParameters();
                int parSize = params.size();
                boolean varArgs = ((ExecutableElement) e).isVarArgs();
                if (!varArgs && (parSize <= argTypes.length)) {
                    continue;
                }
                ExecutableType meth = e == prototypeSym && prototype != null ? prototype : (ExecutableType) asMemberOf(e, type, types);
                Iterator<? extends TypeMirror> parIt = meth.getParameterTypes().iterator();
                TypeMirror param = null;
                Map<TypeVariable, TypeMirror> table = new HashMap<>();
                for (int i = 0; i <= argTypes.length; i++) {
                    if (parIt.hasNext()) {
                        param = parIt.next();
                    } else if (!varArgs) {
                        break;
                    }
                    if (tatList != null && param.getKind() == TypeKind.DECLARED && tatList.size() == meth.getTypeVariables().size()) {
                        param = tu.substitute(param, meth.getTypeVariables(), tatList);
                    }
                    if (i == argTypes.length) {
                        TypeMirror toAdd = null;
                        if (i < parSize) {
                            toAdd = param;
                        }
                        if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
                            toAdd = ((ArrayType) param).getComponentType();
                        }
                        while (toAdd != null && toAdd.getKind() == TypeKind.TYPEVAR) {
                            toAdd = ((TypeVariable) toAdd).getUpperBound();
                        }
                        if (toAdd != null && ret.add(toAdd)) {
                            TypeMirror toRemove = null;
                            for (TypeMirror tm : ret) {
                                if (tm != toAdd) {
                                    TypeMirror tmErasure = types.erasure(tm);
                                    TypeMirror toAddErasure = types.erasure(toAdd);
                                    if (types.isSubtype(toAddErasure, tmErasure)) {
                                        toRemove = toAdd;
                                        break;
                                    } else if (types.isSubtype(tmErasure, toAddErasure)) {
                                        toRemove = tm;
                                        break;
                                    }
                                }
                            }
                            if (toRemove != null && !toRemove.getKind().isPrimitive()
                                    && !"java.lang.String".equals(toRemove.toString()) && !"char[]".equals(toRemove.toString())) //NOI18N
                            {
                                ret.remove(toRemove);
                            }
                        }
                        break;
                    }
                    if (argTypes[i] == null) {
                        break;
                    }
                    if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
                        if (types.isAssignable(argTypes[i], param)) {
                            varArgs = false;
                        } else if (argTypes[i].getKind() != TypeKind.ERROR && !types.isAssignable(argTypes[i], ((ArrayType) param).getComponentType())) {
                            break;
                        }
                    } else if (argTypes[i].getKind() != TypeKind.ERROR && !types.isAssignable(argTypes[i], param)) {
                        if (tatList == null && param.getKind() == TypeKind.DECLARED && argTypes[i].getKind() == TypeKind.DECLARED
                                && types.isAssignable(types.erasure(argTypes[i]), types.erasure(param))) {
                            Iterator<? extends TypeMirror> argTypeTAs = ((DeclaredType) argTypes[i]).getTypeArguments().iterator();
                            for (Iterator<? extends TypeMirror> it = ((DeclaredType) param).getTypeArguments().iterator(); it.hasNext();) {
                                TypeMirror paramTA = it.next();
                                if (argTypeTAs.hasNext() && paramTA.getKind() == TypeKind.TYPEVAR) {
                                    table.put((TypeVariable) paramTA, argTypeTAs.next());
                                } else {
                                    break;
                                }
                            }
                            if (table.size() == meth.getTypeVariables().size()) {
                                tatList = new ArrayList<>(meth.getTypeVariables().size());
                                for (TypeVariable tv : meth.getTypeVariables()) {
                                    tatList.add(table.get(tv));
                                }
                            }
                            continue;
                        }
                        break;
                    }
                }
            }
        }
        return ret.isEmpty() ? null : ret;
    }
    
    private TypeMirror adjustType(Env env, TypeMirror original, Element element, TreePath path) {
        if (element instanceof VariableElement && !element.getKind().isField() && itemFactory instanceof TypeCastableItemFactory) {
            final Trees trees = env.getController().getTrees();
            final Types types = env.getController().getTypes();
            final TypeUtilities tu = env.getController().getTypeUtilities();
            Tree last = null;
            while (path != null) {
                if (path.getLeaf().getKind() == Tree.Kind.IF) {
                    IfTree ifTree = (IfTree) path.getLeaf();
                    if (ifTree.getThenStatement() == last) {
                        Tree cond = ifTree.getCondition();
                        while (cond.getKind() == Tree.Kind.PARENTHESIZED) {
                            cond = ((ParenthesizedTree)cond).getExpression();
                        }
                        if (cond.getKind() == Tree.Kind.INSTANCE_OF) {
                            InstanceOfTree instTree = (InstanceOfTree) cond;
                            if (element == trees.getElement(new TreePath(path, instTree.getExpression()))) {
                                TypeMirror tm = trees.getTypeMirror(new TreePath(path, instTree.getType()));
                                if (tm != null && tu.isCastable(original, tm)) {
                                    Boolean used = new ErrorAwareTreePathScanner<Boolean, Element>() {
                                        @Override
                                        public Boolean reduce(Boolean r1, Boolean r2) {
                                            return r1 == Boolean.TRUE ? r1 : r2;
                                        }                                        

                                        @Override
                                        public Boolean visitAssignment(AssignmentTree tree, Element e) {
                                            return e == trees.getElement(new TreePath(getCurrentPath(), tree.getVariable()))
                                                    ? Boolean.TRUE : super.visitAssignment(tree, e);
                                        }

                                        @Override
                                        public Boolean visitCompoundAssignment(CompoundAssignmentTree tree, Element e) {
                                            return e == trees.getElement(new TreePath(getCurrentPath(), tree.getVariable()))
                                                    ? Boolean.TRUE : super.visitCompoundAssignment(tree, e);
                                        }
                                    }.scan(new TreePath(path, ifTree.getThenStatement()), element);
                                    if (used != Boolean.TRUE) {
                                        if (original.getKind() == TypeKind.DECLARED) {
                                            return inferDeclaredType(types, (DeclaredType)original, tm);
                                        }
                                        return tm;
                                    }
                                }
                            }
                        }
                    }
                }
                last = path.getLeaf();
                path = path.getParentPath();
            }
        }
        return original;
    }
    
    private TypeMirror inferDeclaredType(Types types, DeclaredType original, TypeMirror type) {
        if (type != null && type.getKind() == TypeKind.DECLARED) {
            Element el = ((DeclaredType)type).asElement();
            if (el.getKind().isClass() || el.getKind().isInterface()) {
                List<? extends TypeParameterElement> typeParams = ((TypeElement)el).getTypeParameters();
                if (!typeParams.isEmpty() && !original.getTypeArguments().isEmpty()) {
                    for (TypeMirror typeArgument : ((DeclaredType)type).getTypeArguments()) {
                        if (typeArgument.getKind() == TypeKind.WILDCARD) {
                            typeArgument = ((WildcardType)typeArgument).getExtendsBound();
                        }
                        if (typeArgument == null || typeArgument.getKind() != TypeKind.TYPEVAR) {
                            return type;
                        }
                    }
                    if (el == original.asElement()) {
                        return original;
                    }
                    Map<Element, TypeMirror> map = new HashMap<>();
                    TypeMirror sup = ((TypeElement)el).getSuperclass();
                    TypeMirror infSup = inferDeclaredType(types, original, sup);
                    if (sup != infSup) {
                        Iterator<? extends TypeMirror> supTP = ((DeclaredType)sup).getTypeArguments().iterator();
                        Iterator<? extends TypeMirror> infTP = ((DeclaredType)infSup).getTypeArguments().iterator();
                        while (supTP.hasNext() && infTP.hasNext()) {
                            final TypeMirror next = supTP.next();
                            if (next.getKind() == TypeKind.TYPEVAR) {
                                map.put(((TypeVariable)next).asElement(), infTP.next());
                            }
                        }
                        assert !supTP.hasNext() && !infTP.hasNext();
                    }
                    for (TypeMirror iface : ((TypeElement)el).getInterfaces()) {
                        TypeMirror infIface = inferDeclaredType(types, original, iface);
                        if (iface != infIface) {
                            Iterator<? extends TypeMirror> ifaceTP = ((DeclaredType)iface).getTypeArguments().iterator();
                            Iterator<? extends TypeMirror> infTP = ((DeclaredType)infIface).getTypeArguments().iterator();
                            while (ifaceTP.hasNext() && infTP.hasNext()) {
                                final TypeMirror next = ifaceTP.next();
                                if (next.getKind() == TypeKind.TYPEVAR) {
                                    map.put(((TypeVariable)next).asElement(), infTP.next());
                                }
                            }
                            assert !ifaceTP.hasNext() && !infTP.hasNext();
                        }                        
                    }
                    TypeMirror[] targs = new TypeMirror[typeParams.size()];
                    int i = 0;
                    for (TypeParameterElement typeParam : typeParams) {
                        TypeMirror val = map.get(typeParam);
                        targs[i++] = val != null ? val : typeParam.getBounds().get(0);
                    }
                    return types.getDeclaredType((TypeElement)el, targs);
                }
            }
        }
        return type;
    }

    private boolean withinScope(Env env, TypeElement e) throws IOException {
        for (Element encl = env.getScope().getEnclosingClass(); encl != null; encl = encl.getEnclosingElement()) {
            if (e == encl) {
                return true;
            }
        }
        return false;
    }

    private boolean withinLabeledStatement(Env env) {
        TreePath path = env.getPath();
        while (path != null) {
            if (path.getLeaf().getKind() == Tree.Kind.LABELED_STATEMENT) {
                return true;
            }
            path = path.getParentPath();
        }
        return false;
    }

    private boolean withinModuleName(Env env) {
        TreePath path = env.getPath();
        Tree last = null;
        while (path != null) {
            Tree tree = path.getLeaf();
            if (last != null
                    && (tree.getKind() == Tree.Kind.EXPORTS && ((ExportsTree)tree).getModuleNames() != null && ((ExportsTree)tree).getModuleNames().contains(last)
                    || tree.getKind() == Tree.Kind.REQUIRES && ((RequiresTree)tree).getModuleName() == last)) {
                return true;
            }
            path = path.getParentPath();
            last = tree;
        }
        return false;
    }
    
    private boolean withinProvidesService(Env env) {
        TreePath path = env.getPath();
        Tree last = null;
        while (path != null) {
            Tree tree = path.getLeaf();
            if (last != null && tree.getKind() == Tree.Kind.PROVIDES && ((ProvidesTree)tree).getServiceName() == last) {
                return true;
            }
            path = path.getParentPath();
            last = tree;
        }
        return false;        
    }

    private boolean hasAccessibleInnerClassConstructor(Element e, Scope scope, Trees trees) {
        DeclaredType dt = (DeclaredType) e.asType();
        for (TypeElement inner : ElementFilter.typesIn(e.getEnclosedElements())) {
            if (trees.isAccessible(scope, inner, dt)) {
                DeclaredType innerType = (DeclaredType) inner.asType();
                for (ExecutableElement ctor : ElementFilter.constructorsIn(inner.getEnclosedElements())) {
                    if (trees.isAccessible(scope, ctor, innerType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean conflictsWithLocalMethods(Name name, TypeElement enclClass, List<ExecutableElement> methodsIn) {
        for (ExecutableElement local : methodsIn) {
            if (local.getEnclosingElement() == enclClass && name.contentEquals(local.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    private String fullName(Tree tree) {
        switch (tree.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) tree).getName().toString();
            case MEMBER_SELECT:
                String sname = fullName(((MemberSelectTree) tree).getExpression());
                return sname == null ? null : sname + '.' + ((MemberSelectTree) tree).getIdentifier();
            default:
                return null;
        }
    }

    private DeclaredType getDeclaredType(TypeElement e, HashMap<? extends Element, ? extends TypeMirror> map, Types types) {
        List<? extends TypeParameterElement> tpes = e.getTypeParameters();
        TypeMirror[] targs = new TypeMirror[tpes.size()];
        int i = 0;
        for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
            TypeParameterElement tpe = it.next();
            TypeMirror t = map.get(tpe);
            targs[i++] = t != null ? t : tpe.asType();
        }
        Element encl = e.getEnclosingElement();
        if ((encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement) encl).getTypeParameters().isEmpty()) {
            return types.getDeclaredType(getDeclaredType((TypeElement) encl, map, types), e, targs);
        }
        return types.getDeclaredType(e, targs);
    }

    private boolean startsWith(Env env, String theString) {
        String prefix = env.getPrefix();
        return startsWith(env, theString, prefix);
    }

    private boolean startsWith(Env env, String theString, String prefix) {
        return env.isCamelCasePrefix() ? Utilities.isCaseSensitive()
                ? Utilities.startsWithCamelCase(theString, prefix)
                : Utilities.startsWithCamelCase(theString, prefix) || Utilities.startsWith(theString, prefix)
                : Utilities.startsWith(theString, prefix);
    }

    private boolean withinBounds(Env env, TypeMirror type, List<? extends TypeMirror> bounds) {
        if (bounds != null) {
            Types types = env.getController().getTypes();
            for (TypeMirror bound : bounds) {
                if (!types.isSubtype(type, bound)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addVarTypeForLambdaParam(final Env env) throws IOException {
        if (env.getController().getSourceVersion().compareTo(RELEASE_11) < 0) {
            return;
        }
        results.add(itemFactory.createKeywordItem(VAR_KEYWORD, SPACE, anchorOffset, false));
    }

    /**
     *
     * @param env : env
     * @param tree : Lambda expression tree
     * @return true if first param of lambda expr is of 'var' type
     */
    private boolean isLambdaVarType(Env env, Tree tree) {

        if (tree.getKind() != Tree.Kind.LAMBDA_EXPRESSION) {
            return false;
        }
        LambdaExpressionTree let = (LambdaExpressionTree) tree;
        if (let.getParameters().isEmpty()) {
            return false;
        }

        boolean isFirstParamVarType = false;

        VariableTree firstParamTree = let.getParameters().get(0);
        int firstParamStartPos = (int) env.getSourcePositions().getStartPosition(env.getRoot(), firstParamTree);
        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(env, let, env.getOffset());
        ts.move(firstParamStartPos);
        ts.movePrevious();

        //TreeUtilities.isVarType() API can't be used as FirstParamTree might not be complete.
        while (ts.token().id() != JavaTokenId.COMMA && !isFirstParamVarType && ts.moveNext()) {
            isFirstParamVarType = ts.token().id() == JavaTokenId.VAR;
        }
        return isFirstParamVarType;

    }
    
    private static ElementKind simplifyElementKind(ElementKind kind) {
        if (ElementKind.BINDING_VARIABLE == kind) {
            return ElementKind.LOCAL_VARIABLE;
        } else if (ElementKind.RECORD == kind) {
            return ElementKind.CLASS;
        }
        return kind;
    }

}
