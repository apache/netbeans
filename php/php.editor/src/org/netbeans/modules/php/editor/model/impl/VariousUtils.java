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
package org.netbeans.modules.php.editor.model.impl;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ArrowFunctionScope;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.CloneExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Radek Matous
 */
public final class VariousUtils {

    public static final String PRE_OPERATION_TYPE_DELIMITER = "@"; //NOI18N
    public static final String POST_OPERATION_TYPE_DELIMITER = ":"; //NOI18N
    public static final String POST_OPERATION_TYPE_DELIMITER_SUBS = "_POTD_"; //NOI18N
    // GH-6909 To avoid conflicting with member names, add "-type" suffix
    // because "-" can not be contained to member names
    public static final String CONSTRUCTOR_TYPE_PREFIX = "constuct-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String FUNCTION_TYPE_PREFIX = "fn-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String METHOD_TYPE_PREFIX = "mtd-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String STATIC_METHOD_TYPE_PREFIX = "static.mtd-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String FIELD_TYPE_PREFIX = "fld-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String STATIC_FIELD_TYPE_PREFIX = "static.fld-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String STATIC_CONSTANT_TYPE_PREFIX = "static.constant-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String VAR_TYPE_PREFIX = "var-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String ARRAY_TYPE_PREFIX = "array-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    public static final String TYPE_TYPE_PREFIX = "type-type" + POST_OPERATION_TYPE_DELIMITER; //NOI18N
    private static final Collection<String> SPECIAL_CLASS_NAMES = new LinkedList<>();
    private static final Collection<String> STATIC_CLASS_NAMES = new LinkedList<>();
    private static final String VAR_TYPE_COMMENT_PREFIX = "@var"; //NOI18N
    private static final String SPACES_AND_TYPE_DELIMITERS = "[| ]*"; //NOI18N
    private static final Pattern SEMI_TYPE_NAME_PATTERN = Pattern.compile("[" + PRE_OPERATION_TYPE_DELIMITER + POST_OPERATION_TYPE_DELIMITER + "]"); // NOI18N
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile("\\;"); // NOI18N
    private static final Pattern DOT_PATTERN = Pattern.compile("\\."); // NOI18N
    private static final Pattern TYPE_SEPARATOR_PATTERN = Pattern.compile("\\|"); // NOI18N
    private static final Pattern TYPE_SEPARATOR_INTERSECTION_PATTERN = Pattern.compile("\\&"); // NOI18N


    static {
        STATIC_CLASS_NAMES.add("self"); //NOI18N
        STATIC_CLASS_NAMES.add("static"); //NOI18N
        SPECIAL_CLASS_NAMES.add("parent"); //NOI18N
        SPECIAL_CLASS_NAMES.addAll(STATIC_CLASS_NAMES);
    }

    public static enum Kind {

        CONSTRUCTOR,
        FUNCTION,
        METHOD,
        STATIC_METHOD,
        FIELD,
        STATIC_FIELD,
        VAR;

        @Override
        public String toString() {
            String result;
            switch (this) {
                case CONSTRUCTOR:
                    result = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                    break;
                case FUNCTION:
                    result = VariousUtils.FUNCTION_TYPE_PREFIX;
                    break;
                case METHOD:
                    result = VariousUtils.METHOD_TYPE_PREFIX;
                    break;
                case STATIC_METHOD:
                    result = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                    break;
                case FIELD:
                    result = VariousUtils.FIELD_TYPE_PREFIX;
                    break;
                case STATIC_FIELD:
                    result = VariousUtils.STATIC_FIELD_TYPE_PREFIX;
                    break;
                case VAR:
                    result = VariousUtils.VAR_TYPE_PREFIX;
                    break;
                default:
                    result = super.toString();
            }
            return result;
        }
    };

    public static String encodeVariableName(final String name) {
        String result = name;
        if (name != null) {
            result = name.replace(POST_OPERATION_TYPE_DELIMITER, POST_OPERATION_TYPE_DELIMITER_SUBS);
        }
        return result;
    }

    public static String extractTypeFroVariableBase(VariableBase varBase) {
        return extractTypeFroVariableBase(varBase, Collections.<String, AssignmentImpl>emptyMap());
    }

    static String extractTypeFroVariableBase(VariableBase varBase, Map<String, AssignmentImpl> allAssignments) {
        ArrayDeque<VariableBase> stack = new ArrayDeque<>();
        String typeName = null;
        createVariableBaseChain(varBase, stack);
        while (!stack.isEmpty() && stack.peek() != null) {
            varBase = stack.pop();
            String tmpType = extractVariableTypeFromVariableBase(varBase, allAssignments);
            if (tmpType == null) {
                typeName = null;
                break;
            }
            if (typeName == null) {
                typeName = tmpType;
            } else {
                typeName += tmpType;
            }
        }
        return typeName; //extractVariableTypeFromVariableBase(varBase);
    }

    private VariousUtils() {
    }

    /**
     * First, try to get return type from function declaration; if not set,
     * try to get it from its PhpDoc.
     * @return return type from function declaration, can be {@code null}
     */
    @CheckForNull
    public static String getReturnType(Program root, FunctionDeclaration functionDeclaration) {
        Expression returnType = functionDeclaration.getReturnType();
        if (returnType != null) {
            String typeName;
            if (returnType instanceof UnionType) {
                typeName = getUnionType((UnionType) returnType);
            } else if (returnType instanceof IntersectionType){
                typeName = getIntersectionType((IntersectionType) returnType);
            } else {
                QualifiedName name = QualifiedName.create(returnType);
                assert name != null : returnType;
                typeName = name.toString();
            }
            if (Type.ARRAY.equals(typeName) || Type.SELF.equals(typeName)) {
                // For "array" type PHPDoc can contain more specific definition, i.e. MyClass[]
                // For "self" type PHPDoc can contain more specific definition, i.e. static or $this
                String typeFromPHPDoc = getReturnTypeFromPHPDoc(root, functionDeclaration);
                if (typeFromPHPDoc != null) {
                    return typeFromPHPDoc;
                }
            }
            if (returnType instanceof NullableType) {
                return CodeUtils.NULLABLE_TYPE_PREFIX + typeName;
            }
            return typeName;
        }
        return getReturnTypeFromPHPDoc(root, functionDeclaration);
    }

    /**
     * Get the types separated by "|".
     *
     * @param unionType
     * @return types separated by "|"
     */
    public static String getUnionType(UnionType unionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : unionType.getTypes()) {
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR);
            }
            // GH-4725: PHP 8.2 Disjunctive Normal Form Types
            // e.g. (X&Y)|(A&B)
            if (type instanceof IntersectionType) {
                IntersectionType intersectionType = (IntersectionType) type;
                sb.append("(").append(getIntersectionType(intersectionType)).append(")"); // NOI18N
                continue;
            }
            QualifiedName name = QualifiedName.create(type);
            assert name != null : type;
            sb.append(name.toString());
        }
        return sb.toString();
    }

    /**
     * Get the types separated by "&".
     *
     * @param intesectionType
     * @return types separated by "&"
     */
    public static String getIntersectionType(IntersectionType intesectionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : intesectionType.getTypes()) {
            QualifiedName name = QualifiedName.create(type);
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR_INTERSECTION);
            }
            assert name != null : type;
            sb.append(name.toString());
        }
        return sb.toString();
    }

    @CheckForNull
    public static String getDeclaredType(Expression declaredType) {
        if (declaredType != null) {
            if (declaredType instanceof UnionType) {
                return getUnionType((UnionType) declaredType);
            }
            if (declaredType instanceof IntersectionType) {
                return getIntersectionType((IntersectionType) declaredType);
            }
            boolean isNullableType = declaredType instanceof NullableType;
            QualifiedName fieldTypeName = QualifiedName.create(declaredType);
            if (fieldTypeName != null) {
                return (isNullableType ? CodeUtils.NULLABLE_TYPE_PREFIX : "") + fieldTypeName.toString(); // NOI18N
            }
        }
        return null;
    }

    public static List<Pair<QualifiedName, Boolean/* isNullableType */>> getParamTypesFromUnionTypes(UnionType unionType) {
        List<Pair<QualifiedName, Boolean>> types = new ArrayList<>();
        for (QualifiedName type : QualifiedName.create(unionType)) {
            types.add(Pair.of(type, false));
        }
        return types;
    }

    public static List<Pair<QualifiedName, Boolean/* isNullableType */>> getParamTypesFromIntersectionTypes(IntersectionType intersectionType) {
        List<Pair<QualifiedName, Boolean>> types = new ArrayList<>();
        for (Expression type : intersectionType.getTypes()) {
            QualifiedName name = QualifiedName.create(type);
            if (name != null) {
                types.add(Pair.of(name, false));
            }
        }
        return types;
    }

    public static String getReturnTypeFromPHPDoc(Program root, FunctionDeclaration functionDeclaration) {
        return getTypeFromPHPDoc(root, functionDeclaration, PHPDocTag.Type.RETURN);
    }

    public static String getFieldTypeFromPHPDoc(Program root, SingleFieldDeclaration field) {
        return getTypeFromPHPDoc(root, field, PHPDocTag.Type.VAR);
    }

    public static String getDeprecatedDescriptionFromPHPDoc(Program root, ASTNode node) {
        return getDescriptionFromPHPDoc(root, node, PHPDocTag.Type.DEPRECATED);
    }

    public static boolean isDeprecatedFromPHPDoc(Program root, ASTNode node) {
        return getDeprecatedDescriptionFromPHPDoc(root, node) != null;
    }

    public static Map<String, Pair<String, List<Pair<QualifiedName, Boolean>>>> getParamTypesFromPHPDoc(Program root, ASTNode node) {
        Map<String, Pair<String, List<Pair<QualifiedName, Boolean>>>> retval = new HashMap<>();
        Comment comment = Utils.getCommentForNode(root, node);

        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;

            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind().equals(PHPDocTag.Type.PARAM)) {
                    List<Pair<QualifiedName, Boolean>> allTypes = new ArrayList<>();
                    PHPDocVarTypeTag paramTag = (PHPDocVarTypeTag) tag;
                    for (PHPDocTypeNode type : paramTag.getTypes()) {
                        String typeName = type.getValue();
                        boolean isNullableType = CodeUtils.isNullableType(typeName);
                        if (isNullableType) {
                            typeName = typeName.substring(1);
                        }
                        allTypes.add(Pair.of(QualifiedName.create(typeName), isNullableType));
                    }
                    String value = paramTag.getValue().trim(); // e.g. (X&Y)|Z $variable
                    String[] split = CodeUtils.WHITE_SPACES_PATTERN.split(value);
                    String rawType = ""; // NOI18N
                    if (split.length > 0) {
                        rawType = split[0];
                    }
                    Pair<String, List<Pair<QualifiedName, Boolean>>> types = Pair.of(rawType, allTypes);
                    retval.put(paramTag.getVariable().getValue(), types);
                }
            }
        }
        return retval;
    }

    public static String getTypeFromPHPDoc(Program root, ASTNode node, PHPDocTag.Type tagType) {
        Comment comment = Utils.getCommentForNode(root, node);

        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;

            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind().equals(tagType)) {
                    String[] parts = CodeUtils.WHITE_SPACES_PATTERN.split(tag.getValue().trim(), 2);

                    if (parts.length > 0) {
                        String type = SEMICOLON_PATTERN.split(parts[0], 2)[0];
                        return type;
                    }

                    break;
                }
            }
        } else if ((comment instanceof PHPVarComment) && PHPDocTag.Type.VAR == tagType) {
            // GH-6359
            // /** @var Type $field */
            // private $field;
            PHPVarComment varComment = (PHPVarComment) comment;
            PHPDocVarTypeTag tag = varComment.getVariable();
            String[] parts = CodeUtils.WHITE_SPACES_PATTERN.split(tag.getValue().trim(), 3); // 3: @var Type $field
            if (parts.length > 1) {
                return parts[1];
            }
        }

        return null;
    }

    public static String getDescriptionFromPHPDoc(Program root, ASTNode node, PHPDocTag.Type tagType) {
        Comment comment = Utils.getCommentForNode(root, node);
        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;
            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind().equals(tagType)) {
                    return tag.getValue().trim();
                }
            }
        }
        return null;
    }

    @CheckForNull
    static String extractVariableTypeFromAssignment(Assignment assignment, Map<String, AssignmentImpl> allAssignments) {
        Expression expression = assignment.getRightHandSide();
        return extractVariableTypeFromExpression(expression, allAssignments);
    }

    static String extractVariableTypeFromExpression(Expression expression, Map<String, AssignmentImpl> allAssignments) {
        if (expression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) expression, allAssignments);
        } else if (expression instanceof Reference) {
            Reference ref = (Reference) expression;
            expression = ref.getExpression();
        }
        if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
            final ClassName className = classInstanceCreation.getClassName();
            Expression name = className.getName();
            if (name instanceof NamespaceName) {
                QualifiedName qn = QualifiedName.create(name);
                assert qn != null : name;
                return qn.toString();
            }
            return CodeUtils.extractClassName(className);
        } else if (expression instanceof ArrayCreation) {
            return Type.ARRAY;
        } else if (expression instanceof VariableBase) {
            return extractTypeFroVariableBase((VariableBase) expression, allAssignments); //extractVariableTypeFromVariableBase(varBase);
        } else if (expression instanceof Scalar) {
            Scalar scalar = (Scalar) expression;
            Scalar.Type scalarType = scalar.getScalarType();
            if (scalarType.equals(Scalar.Type.STRING)) {
                String stringValue = scalar.getStringValue().toLowerCase();
                if (stringValue.equals("false") || stringValue.equals("true")) { //NOI18N
                    return Type.BOOL;
                }
                if (stringValue.equals(Type.NULL)) {
                    return Type.NULL;
                }
            }
            return scalarType.toString().toLowerCase();
        } else if (expression instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) expression;
            OperatorType operator = infixExpression.getOperator();
            if (operator.equals(OperatorType.CONCAT)) {
                return Type.STRING.toString().toLowerCase();
            }
        } else if (expression instanceof CloneExpression) {
            CloneExpression cloneExpression = (CloneExpression) expression;
            return extractVariableTypeFromExpression(cloneExpression.getExpression(), allAssignments);
        }
        return null;
    }

    public static String replaceVarNames(String semiTypeName, Map<String, String> var2Type) {
        StringBuilder retval = new StringBuilder();
        String[] fragments = SEMI_TYPE_NAME_PATTERN.split(semiTypeName);
        for (int i = 0; i < fragments.length; i++) {
            String frag = fragments[i];
            if (frag.trim().length() == 0) {
                continue;
            }
            if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                if (i + 1 < fragments.length) {
                    String varName = fragments[++i];
                    String type = var2Type.get(varName);
                    if (type != null) {
                        retval.append(type);
                        continue;
                    }
                }
                return null;
            }
            Kind[] values = VariousUtils.Kind.values();
            boolean isPrefix = false;
            for (Kind kind : values) {
                if (kind.toString().startsWith(frag)) {
                    isPrefix = true;
                    break;
                }
            }
            if (isPrefix) {
                retval.append(PRE_OPERATION_TYPE_DELIMITER);
                retval.append(frag);
                retval.append(POST_OPERATION_TYPE_DELIMITER);
            } else {
                retval.append(frag);
            }
        }
        return retval.toString();
    }

    public static Collection<? extends VariableName> getAllVariables(VariableScope varScope, String semiTypeName) {
        List<VariableName> retval = new ArrayList<>();
        String[] fragments = SEMI_TYPE_NAME_PATTERN.split(semiTypeName);
        for (int i = 0; i < fragments.length; i++) {
            String frag = fragments[i];
            if (frag.trim().length() == 0) {
                continue;
            }
            if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                if (i + 1 < fragments.length) {
                    String varName = fragments[++i];
                    VariableName var = varName != null ? ModelUtils.getFirst(varScope.getDeclaredVariables(), varName) : null;
                    if (var != null) {
                        retval.add(var);
                    } else {
                        return Collections.emptyList();
                    }
                }
            }
        }
        return retval;
    }
    private static Set<String> recursionDetection = new HashSet<>(); //#168868
    //TODO: needs to be improved to properly return more types

    public static Collection<? extends TypeScope> getType(
            final VariableScope varScope,
            String semiTypeName,
            int offset,
            boolean justDispatcher) {
        if (varScope instanceof ArrowFunctionScope) {
            return getArrowFunctionScopeType((ArrowFunctionScope) varScope, semiTypeName, offset, justDispatcher);
        }
        return getType(varScope, semiTypeName, offset, justDispatcher, Collections.emptyList());
    }

    private static Collection<? extends TypeScope> getArrowFunctionScopeType(
            final ArrowFunctionScope varScope,
            String semiTypeName,
            int offset,
            boolean justDispatcher) {
        assert varScope instanceof ArrowFunctionScope;
        Collection<? extends TypeScope> types = Collections.emptyList();
        Scope inScope = varScope;
        // for nested arrow functions
        while (types.isEmpty()
                && (inScope instanceof FunctionScopeImpl || inScope instanceof NamespaceScopeImpl)) {
            types = getType((VariableScope) inScope, semiTypeName, offset, justDispatcher, Collections.emptyList());
            inScope = inScope.getInScope();
            if (inScope == null) {
                break;
            }
        }
        return types;
    }

    public static Collection<? extends TypeScope> getType(
            final VariableScope varScope,
            String semiTypeName,
            int offset,
            boolean justDispatcher,
            Collection<? extends TypeScope> callerTypes) {
        Collection<? extends TypeScope> recentTypes = Collections.emptyList();
        Collection<? extends TypeScope> oldRecentTypes;
        Stack<VariableName> fldVarStack = new Stack<>();

        if (semiTypeName != null && semiTypeName.contains(PRE_OPERATION_TYPE_DELIMITER)) {
            String operation = null;
            String[] fragments = SEMI_TYPE_NAME_PATTERN.split(semiTypeName);
            int len = (justDispatcher) ? fragments.length - 1 : fragments.length;
            for (int i = 0; i < len; i++) {
                oldRecentTypes = recentTypes;
                String frag = fragments[i].trim();
                if (frag.length() == 0) {
                    continue;
                }
                String operationPrefix = (frag.endsWith(POST_OPERATION_TYPE_DELIMITER)) ? frag : String.format("%s%s", frag, POST_OPERATION_TYPE_DELIMITER);
                if (VariousUtils.METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_FIELD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.STATIC_FIELD_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_CONSTANT_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.STATIC_CONSTANT_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.ARRAY_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.ARRAY_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else if (VariousUtils.TYPE_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.TYPE_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0 : frag;
                        recentTypes = IndexScopeImpl.getTypes(QualifiedName.create(frag), varScope);
                    } else if (operation.startsWith(VariousUtils.TYPE_TYPE_PREFIX)) {
                        recentTypes = IndexScopeImpl.getTypes(QualifiedName.create(frag), varScope);
                        // !!! THIS IS A HACK !!!
                        // varScope.getDeclaredVariables() method invokes lazy scan of methods, so proper variables are assigned
                        // to proper elements. Without this hack CC doesn't work in issue 226071 for first invocation, just for the second.
                        // It works for "non static CC: $this->a^", becuase when VAR_TYPE_PREFIX is used (for $this), then there is
                        // "VariableName var = ModelUtils.getFirst(varScope.getDeclaredVariables(), varName);" invoked for fetching
                        // variable name. In static context, we don't need variable name, but we need fully initialized scope as well.
                        varScope.getDeclaredVariables();
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        //new FooImpl()-> not allowed in php
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        QualifiedName fullyQualifiedName = getFullyQualifiedName(createQuery(frag, varScope), offset, varScope);
                        newRecentTypes.addAll(IndexScopeImpl.getClasses(fullyQualifiedName, varScope));
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        for (TypeScope tScope : oldRecentTypes) {
                            Collection<? extends MethodScope> inheritedMethods = IndexScopeImpl.getMethods(tScope, frag, varScope, PhpModifiers.ALL_FLAGS);
                            for (MethodScope meth : inheritedMethods) {
                                newRecentTypes.addAll(meth.getReturnTypes(true, Collections.singleton(tScope)));
                            }
                        }
                        recentTypes = filterSuperTypes(newRecentTypes);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        FunctionScope fnc = ModelUtils.getFirst(IndexScopeImpl.getFunctions(QualifiedName.create(frag), varScope));
                        if (fnc != null) {
                            newRecentTypes.addAll(fnc.getReturnTypes(true, recentTypes));
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(STATIC_FIELD_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        final Collection<? extends TypeScope> types;
                        final String fieldName;
                        String[] frgs = DOT_PATTERN.split(frag);
                        if (frgs.length == 1) {
                            // uniform variable syntax
                            fieldName = frag;
                            types = oldRecentTypes;
                        } else {
                            assert frgs.length == 2 : semiTypeName;
                            fieldName = frgs[1];
                            String clsName = frgs[0];
                            assert clsName != null : frag;
                            QualifiedName fullyQualifiedName = getFullyQualifiedName(createQuery(clsName, varScope), offset, varScope);
                            types = IndexScopeImpl.getTypes(fullyQualifiedName, varScope);
                        }
                        for (TypeScope type : types) {
                            Collection<? extends FieldElement> fields = IndexScopeImpl.getFields(type, fieldName, varScope, PhpModifiers.ALL_FLAGS);
                            for (FieldElement field : fields) {
                                newRecentTypes.addAll(field.getTypes(offset));
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(STATIC_CONSTANT_TYPE_PREFIX)) {
                        // const or case: const CONSTANT = 1;, case CASE1;
                        // Name::CONSTANT; Name::CASE1;
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        final Collection<? extends TypeScope> types;
                        final String constantName;
                        String[] frgs = DOT_PATTERN.split(frag);
                        if (frgs.length == 1) {
                            // uniform variable syntax
                            constantName = frag;
                            types = oldRecentTypes;
                        } else {
                            assert frgs.length == 2 : semiTypeName;
                            constantName = frgs[1];
                            String clsName = frgs[0];
                            assert clsName != null : frag;
                            QualifiedName fullyQualifiedName = getFullyQualifiedName(createQuery(clsName, varScope), offset, varScope);
                            types = IndexScopeImpl.getTypes(fullyQualifiedName, varScope);
                        }
                        for (TypeScope type : types) {
                            List<? extends CaseElement> enumCases = IndexScopeImpl.getEnumCases(QualifiedName.create(constantName), type);
                            for (CaseElement enumCase : enumCases) {
                                Scope inScope = enumCase.getInScope();
                                if (inScope instanceof TypeScope) {
                                    newRecentTypes.add((TypeScope) inScope);
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        final Collection<? extends TypeScope> types;
                        final String methodName;
                        String[] frgs = DOT_PATTERN.split(frag);
                        if (frgs.length == 1) {
                            // uniform variable syntax
                            methodName = frag;
                            types = oldRecentTypes;
                        } else {
                            assert frgs.length == 2 : frag;
                            methodName = frgs[1];
                            String typeName = frgs[0];
                            assert typeName != null : frag;
                            QualifiedName fullyQualifiedName = getFullyQualifiedName(createQuery(typeName, varScope), offset, varScope);
                            types = IndexScopeImpl.getTypes(fullyQualifiedName, varScope);
                        }
                        for (TypeScope type : types) {
                            Collection<? extends MethodScope> inheritedMethods = IndexScopeImpl.getMethods(type, methodName, varScope, PhpModifiers.ALL_FLAGS);
                            for (MethodScope meth : inheritedMethods) {
                                if (callerTypes.isEmpty()) {
                                    newRecentTypes.addAll(meth.getReturnTypes(true, types));
                                } else {
                                    // #269108
                                    newRecentTypes.addAll(meth.getReturnTypes(true, callerTypes));
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)
                            || (operation.startsWith(VariousUtils.ARRAY_TYPE_PREFIX))) {
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        String varName = frag;
                        VariableName var = getVariableName(varScope, varName);
                        if (var != null) {
                            if (i + 2 < len && VariousUtils.FIELD_TYPE_PREFIX.startsWith(fragments[i + 1])) {
                                fldVarStack.push(var);
                            }
                            final String checkName = var.getName() + String.valueOf(offset);
                            boolean added = recursionDetection.add(checkName);
                            try {
                                if (added) {
                                    boolean isArray = operation.startsWith(VariousUtils.ARRAY_TYPE_PREFIX);
                                    if (isArray) {
                                        newRecentTypes.addAll(var.getArrayAccessTypes(offset));
                                    } else {
                                        newRecentTypes.addAll(var.getTypes(offset));
                                    }
                                }
                            } finally {
                                recursionDetection.remove(checkName);
                            }
                        }

                        if (newRecentTypes.isEmpty()) {
                            if (varScope instanceof MethodScope) { //NOI18N
                                MethodScope mScope = (MethodScope) varScope;
                                if ((frag.equals("this") || frag.equals("$this"))) { //NOI18N
                                    final Scope inScope = mScope.getInScope();
                                    if (inScope instanceof ClassScope) {
                                        String clsName = ((ClassScope) inScope).getName();
                                        newRecentTypes.addAll(IndexScopeImpl.getClasses(QualifiedName.create(clsName), varScope));
                                    } else if (inScope instanceof EnumScope) {
                                        String enumName = ((EnumScope) inScope).getName();
                                        newRecentTypes.addAll(IndexScopeImpl.getEnums(QualifiedName.create(enumName), varScope));
                                    } else if (inScope instanceof TraitScope) {
                                        String traitName = ((TraitScope) inScope).getName();
                                        newRecentTypes.addAll(IndexScopeImpl.getTraits(QualifiedName.create(traitName), varScope));
                                    }
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;

                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        VariableName var = fldVarStack.isEmpty() ? null : fldVarStack.pop();
                        Set<TypeScope> newRecentTypes = new HashSet<>();
                        String fldName = frag;
                        if (!fldName.startsWith("$")) { //NOI18N
                            fldName = "$" + fldName; //NOI18N
                        }
                        for (TypeScope type : oldRecentTypes) {
                            Collection<? extends FieldElement> inheritedFields = IndexScopeImpl.getFields(type, fldName, varScope, PhpModifiers.ALL_FLAGS);
                            for (FieldElement fieldElement : inheritedFields) {
                                Collection<? extends TypeScope> fieldTypes = fieldElement.getTypes(offset);
                                if (fieldTypes.isEmpty() && var != null) {
                                    final Collection<? extends TypeScope> varFieldTypes = var.getFieldTypes(fieldElement, offset);
                                    if (varFieldTypes.isEmpty() && (fieldElement instanceof FieldElementImpl)) {
                                        newRecentTypes.addAll(((FieldElementImpl) fieldElement).getDefaultTypes());
                                    } else {
                                        newRecentTypes.addAll(varFieldTypes);
                                    }
                                } else {
                                    newRecentTypes.addAll(fieldTypes);
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
        } else if (semiTypeName != null) {
            String typeName = CodeUtils.removeNullableTypePrefix(semiTypeName);
            List<String> typeNames = StringUtils.explode(typeName, Type.SEPARATOR);
            final List<QualifiedName> qualifiedNames = new ArrayList<>();
            for (String name : typeNames) {
                QualifiedName qn = QualifiedName.create(name);
                String translatedName = translateSpecialClassName(varScope, qn.getName());
                QualifiedNameKind kind = QualifiedNameKind.resolveKind(translatedName);
                // fully qualified name may be returned if qualified name is "parent"
                if (kind == QualifiedNameKind.UNQUALIFIED) {
                    qn = qn.toNamespaceName().append(translatedName);
                } else {
                    qn = QualifiedName.create(translatedName);
                }
                if (name.startsWith("\\")) { // NOI18N
                    qn = qn.toFullyQualified();
                } else {
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(varScope);
                    if (namespaceScope != null) {
                        Collection<QualifiedName> possibleFQN = getPossibleFQN(qn, offset, namespaceScope);
                        if (!possibleFQN.isEmpty()) {
                            qn = ModelUtils.getFirst(possibleFQN);
                        }
                    }
                }
                qualifiedNames.add(qn);
            }
            final IndexScope indexScope = ModelUtils.getIndexScope(varScope);
            final ArrayList<TypeScope> typeScopes = new ArrayList<>();
            qualifiedNames.forEach(name -> typeScopes.addAll(indexScope.findTypes(name)));
            return typeScopes;
        }

        return recentTypes;
    }

    @CheckForNull
    private static VariableName getVariableName(final VariableScope varScope, String varName) {
        VariableName var = ModelUtils.getFirst(varScope.getDeclaredVariables(), varName);
        // NETBEANS-2992
        // when $this is used in anonymous function, check the parent scope
        if (var == null
                && ModelUtils.isAnonymousFunction(varScope)
                && varName.equals("$this")) { // NOI18N
            Scope inScope = varScope.getInScope();
            while (ModelUtils.isAnonymousFunction(inScope)) {
                inScope = inScope.getInScope();
            }
            if (inScope instanceof VariableScope) {
                var = ModelUtils.getFirst(((VariableScope) inScope).getDeclaredVariables(), varName);
            }
        }
        return var;
    }

    private static Collection<TypeScope> filterSuperTypes(final Collection<? extends TypeScope> typeScopes) {
        final Collection<TypeScope> result = new HashSet<>();
        if (typeScopes.size() > 1) {
            result.addAll(filterPossibleSuperTypes(typeScopes));
        } else {
            result.addAll(typeScopes);
        }
        return result;
    }

    private static Collection<TypeScope> filterPossibleSuperTypes(final Collection<? extends TypeScope> typeScopes) {
        final Collection<TypeScope> result = new HashSet<>();
        for (TypeScope typeScope : typeScopes) {
            if (!isSuperTypeOf(typeScope, typeScopes)) {
                result.add(typeScope);
            }
        }
        return result;
    }

    private static boolean isSuperTypeOf(final TypeScope superType, final Collection<? extends TypeScope> typeScopes) {
        boolean result = false;
        for (TypeScope typeScope : typeScopes) {
            if (superType.isSuperTypeOf(typeScope)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static QualifiedName createQuery(String semiTypeName, final Scope scope) {
        QualifiedName result;
        final QualifiedName query = QualifiedName.create(semiTypeName);
        final String translatedName = translateSpecialClassName(scope, query.getName());
        if (translatedName != null) {
            if (translatedName.startsWith("\\")) { //NOI18N
                result = QualifiedName.create(translatedName);
            } else {
                result = query.toNamespaceName(query.getKind().isFullyQualified()).append(translatedName);
            }
        } else {
            result = query.toNamespaceName(query.getKind().isFullyQualified());
        }
        return result;
    }

    public static ArrayDeque<? extends ModelElement> getElements(FileScope topScope, final VariableScope varScope, String semiTypeName, int offset) {
        ArrayDeque<ModelElement> emptyStack = new ArrayDeque<>();
        ArrayDeque<ModelElement> retval = new ArrayDeque<>();
        ArrayDeque<Collection<? extends TypeScope>> stack = new ArrayDeque<>();

        TypeScope type;
        if (semiTypeName != null && semiTypeName.contains(PRE_OPERATION_TYPE_DELIMITER)) {
            String operation = null;
            String[] fragments = SEMI_TYPE_NAME_PATTERN.split(semiTypeName);
            int len = fragments.length;
            for (int i = 0; i < len; i++) {
                String frag = fragments[i];
                if (frag.trim().length() == 0) {
                    continue;
                }
                String operationPrefix = (frag.endsWith(POST_OPERATION_TYPE_DELIMITER)) ? frag : String.format("%s%s", frag, POST_OPERATION_TYPE_DELIMITER);
                if (VariousUtils.METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;

                        Collection<? extends TypeScope> types = IndexScopeImpl.getTypes(QualifiedName.create(frag), topScope);

                        if (!types.isEmpty()) {
                            stack.push(types);
                        }
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        Collection<? extends TypeScope> types = stack.isEmpty() ? null : stack.pop();
                        if (types == null || types.isEmpty()) {
                            return emptyStack;
                        }
                        TypeScope cls = ModelUtils.getFirst(types);
                        if (cls == null) {
                            return emptyStack;
                        }
                        final Collection<? extends MethodScope> methods = IndexScopeImpl.getMethods(cls, frag, topScope, PhpModifiers.ALL_FLAGS);
                        MethodScope meth = ModelUtils.getFirst(methods);
                        if (methods.isEmpty()) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        types = meth.getReturnTypes(true, types);
                        if (types == null || types.isEmpty()) {
                            break;
                        }
                        stack.push(types);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        FunctionScope fnc = ModelUtils.getFirst(IndexScopeImpl.getFunctions(QualifiedName.create(frag), topScope));
                        if (fnc == null) {
                            break;
                        } else {
                            retval.push(fnc);
                        }
                        Collection<? extends TypeScope> recentTypes = stack.isEmpty() ? Collections.<TypeScope>emptyList() : stack.peek();
                        final Collection<? extends TypeScope> returnTypes = fnc.getReturnTypes(true, recentTypes);
                        type = ModelUtils.getFirst(returnTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(returnTypes);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        ClassScope cls = ModelUtils.getFirst(IndexScopeImpl.getClasses(QualifiedName.create(frag), topScope));
                        if (cls == null) {
                            break;
                        } else {
                            MethodScope meth = ModelUtils.getFirst(IndexScopeImpl.getMethods(cls, "__construct", topScope, PhpModifiers.ALL_FLAGS)); //NOI18N
                            if (meth != null) {
                                retval.push(meth);
                            } else {
                                return emptyStack;
                            }
                        }
                        stack.push(Collections.singletonList(cls));
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        String[] frgs = DOT_PATTERN.split(frag);
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(IndexScopeImpl.getClasses(QualifiedName.create(clsName), topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(IndexScopeImpl.getMethods(cls, frgs[1], topScope, PhpModifiers.ALL_FLAGS));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        Collection<? extends TypeScope> recentTypes = stack.isEmpty() ? Collections.<TypeScope>emptyList() : stack.peek();
                        final Collection<? extends TypeScope> returnTypes = meth.getReturnTypes(true, recentTypes);
                        type = ModelUtils.getFirst(returnTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(returnTypes);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        type = null;
                        if (varScope instanceof MethodScope) { //NOI18N
                            MethodScope mScope = (MethodScope) varScope;
                            if ((frag.equals("this") || frag.equals("$this"))) { //NOI18N
                                type = (ClassScope) mScope.getInScope();
                            }
                            if (type != null) {
                                stack.push(Collections.singletonList(type));
                                operation = null;
                            }
                        } else if (varScope instanceof NamespaceScope) {
                            NamespaceScope nScope = (NamespaceScope) varScope;
                            VariableName varName = ModelUtils.getFirst(nScope.getDeclaredVariables(), frag);
                            if (varName != null) {
                                final Collection<? extends TypeScope> types = varName.getTypes(offset);
                                type = ModelUtils.getFirst(types);
                                if (type != null) {
                                    stack.push(types);
                                    operation = null;
                                }
                            }
                        }
                        if (type == null) {
                            List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), frag);
                            if (!variables.isEmpty()) {
                                VariableName varName = ModelUtils.getFirst(variables);
                                final Collection<? extends TypeScope> types = varName != null ? varName.getTypes(offset) : null;
                                type = types != null ? ModelUtils.getFirst(types) : null;
                                if (varName != null) {
                                    retval.push(varName);
                                }
                                if (type != null) {
                                    stack.push(types);
                                } else {
                                    break;
                                }
                                operation = null;
                            }
                        }
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        Collection<? extends TypeScope> types = stack.isEmpty() ? null : stack.pop();
                        if (types == null || types.isEmpty()) {
                            return emptyStack;
                        }
                        TypeScope cls = ModelUtils.getFirst(types);
                        if (cls == null || !(cls instanceof ClassScope)) {
                            return emptyStack;
                        }
                        FieldElement fieldElement = ModelUtils.getFirst(IndexScopeImpl.getFields((ClassScope) cls,
                                !frag.startsWith("$") ? String.format("%s%s", "$", frag) : frag, topScope, PhpModifiers.ALL_FLAGS)); //NOI18N
                        if (fieldElement == null) {
                            return emptyStack;
                        } else {
                            retval.push(fieldElement);
                        }
                        final Collection<? extends TypeScope> fieldTypes = fieldElement.getTypes(offset);
                        type = ModelUtils.getFirst(fieldTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(fieldTypes);
                        operation = null;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
        }
        return retval;
    }

    private static void createVariableBaseChain(VariableBase node, ArrayDeque<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation) node).getDispatcher(), stack);
        } else if (node instanceof FieldAccess) {
            createVariableBaseChain(((FieldAccess) node).getDispatcher(), stack);
        } else if (node instanceof StaticDispatch) {
            Expression dispatcher = ((StaticDispatch) node).getDispatcher();
            if (dispatcher instanceof VariableBase) {
                createVariableBaseChain((VariableBase) dispatcher, stack);
            }
        }
    }

    private static String extractVariableTypeFromVariableBase(VariableBase varBase, Map<String, AssignmentImpl> allAssignments) {
        if (varBase instanceof AnonymousObjectVariable) {
            AnonymousObjectVariable aov = (AnonymousObjectVariable) varBase;
            Expression clsName = aov.getName();
            assert clsName instanceof ClassInstanceCreation || clsName instanceof CloneExpression : clsName.getClass().getName();
            if (clsName instanceof CloneExpression) {
                CloneExpression ce = (CloneExpression) clsName;
                clsName = ce.getExpression();
                if (clsName instanceof AnonymousObjectVariable) {
                    clsName = ((AnonymousObjectVariable) clsName).getName();
                }
            }
            if (clsName instanceof ClassInstanceCreation) {
                ClassInstanceCreation cis = (ClassInstanceCreation) clsName;
                String className = CodeUtils.extractClassName(cis.getClassName());
                return PRE_OPERATION_TYPE_DELIMITER + CONSTRUCTOR_TYPE_PREFIX + className;
            }
        } else if (varBase instanceof Variable) {
            String varName = CodeUtils.extractVariableName((Variable) varBase);
            AssignmentImpl assignmentImpl = allAssignments.get(varName);
            if (assignmentImpl != null) {
                String semiTypeName = assignmentImpl.typeNameFromUnion();
                if (semiTypeName != null) {
                    return semiTypeName;
                }
            }
            return PRE_OPERATION_TYPE_DELIMITER + VAR_TYPE_PREFIX + varName;
        } else if (varBase instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) varBase;
            String fname = CodeUtils.extractFunctionName(functionInvocation);
            return PRE_OPERATION_TYPE_DELIMITER + FUNCTION_TYPE_PREFIX + fname;
        } else if (varBase instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) varBase;
            String className = null;
            Expression dispatcher = staticMethodInvocation.getDispatcher();
            if (dispatcher instanceof Identifier || dispatcher instanceof NamespaceName) {
                className = CodeUtils.extractQualifiedName(dispatcher);
            }
            String methodName = CodeUtils.extractFunctionName(staticMethodInvocation.getMethod());
            if (methodName != null) {
                if (className != null) {
                    return PRE_OPERATION_TYPE_DELIMITER + STATIC_METHOD_TYPE_PREFIX + className + '.' + methodName;
                }
                return PRE_OPERATION_TYPE_DELIMITER + STATIC_METHOD_TYPE_PREFIX + methodName;
            }
        } else if (varBase instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) varBase;
            String methodName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            if (methodName != null) {
                return PRE_OPERATION_TYPE_DELIMITER + METHOD_TYPE_PREFIX + methodName;
            }
        } else if (varBase instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) varBase;
            String filedName = CodeUtils.extractVariableName(fieldAccess.getField());
            if (filedName != null) {
                return PRE_OPERATION_TYPE_DELIMITER + FIELD_TYPE_PREFIX + encodeVariableName(filedName);
            }
        } else if (varBase instanceof StaticFieldAccess) {
            StaticFieldAccess fieldAccess = (StaticFieldAccess) varBase;
            String clsName = CodeUtils.extractUnqualifiedName(fieldAccess.getDispatcher());
            String fldName = CodeUtils.extractVariableName(fieldAccess.getField());
            if (fldName != null) {
                if (clsName != null) {
                    return PRE_OPERATION_TYPE_DELIMITER + STATIC_FIELD_TYPE_PREFIX + clsName + '.' + fldName;
                }
                return PRE_OPERATION_TYPE_DELIMITER + STATIC_FIELD_TYPE_PREFIX + fldName;
            }
        } else if (varBase instanceof StaticConstantAccess) {
            StaticConstantAccess constantAccess = (StaticConstantAccess) varBase;
            if (!constantAccess.isDynamicName()) {
                String clsName = CodeUtils.extractUnqualifiedName(constantAccess.getDispatcher());
                String constName = CodeUtils.extractQualifiedName(constantAccess.getConstant());
                if (constName != null) {
                    if (clsName != null) {
                        return PRE_OPERATION_TYPE_DELIMITER + STATIC_CONSTANT_TYPE_PREFIX + clsName + '.' + constName;
                    }
                    return PRE_OPERATION_TYPE_DELIMITER + STATIC_CONSTANT_TYPE_PREFIX + constName;
                }
            }
        }

        return null;
    }

    public static String resolveFileName(Include include) {
        Expression e = include.getExpression();

        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }

        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;

            if (Scalar.Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName;
                return fileName;
            }
        }

        return null;
    }

    /**
     * @param sourceFile needs to be data file (not folder)
     */
    public static FileObject resolveInclude(FileObject sourceFile, Include include) {
        Parameters.notNull("sourceFile", sourceFile); //NOI18N
        if (sourceFile.isFolder()) {
            throw new IllegalArgumentException(FileUtil.getFileDisplayName(sourceFile));
        }
        return resolveInclude(sourceFile, resolveFileName(include));
    }

    public static FileObject resolveInclude(FileObject sourceFile, String fileName) {
        FileObject retval = null;
        if (fileName != null) {
            File absoluteFile = new File(fileName);
            if (absoluteFile.exists()) {
                retval = FileUtil.toFileObject(FileUtil.normalizeFile(absoluteFile));
            } else {
                FileObject parent = sourceFile.getParent();
                if (parent != null) {
                    retval = PhpSourcePath.resolveFile(parent, fileName);
                }
            }
        }
        return retval;
    }
    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE,
            PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHP_COMMENT_START,
            PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHP_COMMENT,
            PHPTokenId.PHP_LINE_COMMENT);

    public enum State {
        START, METHOD, INVALID, VARBASE, DOLAR, PARAMS, ARRAYREFERENCE, REFERENCE,
        STATIC_REFERENCE, FUNCTION, FIELD, VARIABLE, ARRAY_FIELD, ARRAY_VARIABLE, CLASSNAME, STOP, IDX
    };

    @org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH"})
    public static String getSemiType(TokenSequence<PHPTokenId> tokenSequence, State state, VariableScope varScope) {
        int commasCount = 0;
        String possibleClassName = ""; //NOI18N
        int anchor = -1;
        int leftBraces = 0;
        int rightBraces = State.PARAMS.equals(state) ? 1 : 0;
        int arrayBrackets = 0;
        String className = null;
        String fieldName = null;
        CloneExpressionInfo cloneInfo = new CloneExpressionInfo();
        StringBuilder metaAll = new StringBuilder();
        while (!state.equals(State.INVALID) && !state.equals(State.STOP) && tokenSequence.movePrevious() && skipWhitespaces(tokenSequence)) {
            Token<PHPTokenId> token = tokenSequence.token();
            if (!CTX_DELIMITERS.contains(token.id()) || isVarTypeComment(token)) {
                switch (state) {
                    case METHOD:
                    case START:
                        state = (state.equals(State.METHOD)) ? State.STOP : State.INVALID;
                        // state = State.INVALID;
                        if (isReference(token)) {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.REFERENCE;
                            cloneInfo.setReference(state);
                        } else if (isStaticReference(token)) {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.STATIC_REFERENCE;
                            cloneInfo.setReference(state);
                        } else if (state.equals(State.STOP)) {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FUNCTION_TYPE_PREFIX);
                        }
                        break;
                    case IDX:
                        if (isLeftArryBracket(token)) {
                            arrayBrackets--;
                            if (arrayBrackets == 0) {
                                state = State.ARRAYREFERENCE;
                            }
                        } else if (isRightArryBracket(token)) {
                            arrayBrackets++;
                        } else if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        }
                        break;
                    case ARRAYREFERENCE:
                    case REFERENCE:
                        boolean isArray = state.equals(State.ARRAYREFERENCE);
                        state = State.INVALID;
                        if (isRightBracket(token)) {
                            rightBraces++;
                            state = State.PARAMS;
                            cloneInfo.setEndOffset(tokenSequence.offset());
                        } else if (isRightArryBracket(token)) {
                            arrayBrackets++;
                            state = State.IDX;
                        } else if (isString(token)) {
                            fieldName = token.text().toString();
                            state = isArray ? State.ARRAY_FIELD : State.FIELD;
                        } else if (isVariable(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = isArray ? State.ARRAY_VARIABLE : State.VARBASE;
                        }
                        break;
                    case STATIC_REFERENCE:
                        state = State.INVALID;
                        if (isString(token)) {
                            className = token.text().toString();
                            state = State.CLASSNAME;
                        } else if (isSelf(token) || isParent(token) || isStatic(token)) {
                            className = translateSpecialClassName(varScope, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isRightBracket(token)) {
                            rightBraces++;
                            state = State.PARAMS;
                            cloneInfo.setEndOffset(tokenSequence.offset());
                        } else if (isRightArryBracket(token)) {
                            arrayBrackets++;
                            state = State.IDX;
                        } else if (isVariable(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.VARBASE;
                        }
                        break;
                    case PARAMS:
                        if (isWhiteSpace(token)) {
                            state = State.PARAMS;
                        } else if (isComma(token)) {
                            if (metaAll.length() == 0) {
                                commasCount++;
                            }
                        } else if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        } else if (isLeftBracket(token)) {
                            leftBraces++;
                        } else if (isRightBracket(token)) {
                            rightBraces++;
                        } else if (isString(token)) {
                            possibleClassName = fetchPossibleClassName(tokenSequence);
                        }
                        if (leftBraces == rightBraces) {
                            state = State.FUNCTION;
                        }
                        // NETBEANS-501
                        if (PHPTokenId.PHP_CLONE == token.id()
                                && cloneInfo.getEndOffset() != -1
                                && cloneInfo.getReference() != null) {
                            tokenSequence.move(cloneInfo.getEndOffset());
                            tokenSequence.moveNext();
                            state = cloneInfo.getReference();
                            rightBraces--;
                        }
                        break;
                    case FUNCTION:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            if (anchor == -1) {
                                anchor = tokenSequence.offset();
                            }
                            state = State.METHOD;
                        }
                        break;
                    case ARRAY_FIELD: // no break
                    case FIELD: // field or enum case
                        state = State.INVALID;
                        if (isStaticReference(token)) {
                            // ::ENUM_CASE
                            state = State.STATIC_REFERENCE;
                            break;
                        }
                        assert fieldName != null;
                        metaAll.insert(0, fieldName);
                        fieldName = null;
                        if (isReference(token)) {
                            // ->fieldName
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FIELD_TYPE_PREFIX);
                            state = State.REFERENCE;
                        }
                        break;
                    case VARBASE:
                        if (isStaticReference(token)) {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FIELD_TYPE_PREFIX);
                            state = State.STATIC_REFERENCE;
                            break;
                        } else {
                            state = State.VARIABLE;
                        } // no break
                    case ARRAY_VARIABLE: // no break
                    case VARIABLE:
                        if (state.equals(State.ARRAY_VARIABLE)) {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.ARRAY_TYPE_PREFIX);
                        } else {
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.VAR_TYPE_PREFIX);
                        } // no break
                    case CLASSNAME:
                        //TODO: self, parent not handled yet
                        //TODO: maybe rather introduce its own State for self, parent
                        if (isStaticReference(token)) {
                            // CLASS_NAME::ENUM_CASE
                            state = State.STATIC_REFERENCE;
                            break;
                        }
                        if (className != null) {
                            metaAll.insert(0, className);
                            className = null;
                        }
                        if (isNamespaceSeparator(token)) {
                            if (tokenSequence.movePrevious()) {
                                metaAll.insert(0, token.text().toString());
                                token = tokenSequence.token();
                                if (isString(token)) {
                                    metaAll.insert(0, token.text().toString());
                                    break;
                                }
                                metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.TYPE_TYPE_PREFIX);
                            }
                        } else if (isReference(token)) {
                            // ->fieldName
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FIELD_TYPE_PREFIX);
                            state = State.REFERENCE;
                            break;
                        } else {
                            metaAll = transformToFullyQualifiedType(metaAll, tokenSequence, varScope);
                            metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.TYPE_TYPE_PREFIX);
                        }
                        state = State.STOP;
                        break;
                    default:
                    //no-op
                }
            } else {
                if (state.equals(State.VARBASE)) {
                    metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.VAR_TYPE_PREFIX);
                    state = State.STOP;
                    break;
                } else if (state.equals(State.CLASSNAME)) {
                    if (className != null) {
                        metaAll.insert(0, className);
                        className = null;
                    }
                    if (!metaAll.toString().startsWith("\\")) { //NOI18N
                        if (tokenSequence.moveNext()) { // return to last valid token
                            metaAll = transformToFullyQualifiedType(metaAll, tokenSequence, varScope);
                        }
                    }
                    metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.TYPE_TYPE_PREFIX);
                    state = State.STOP;
                    break;
                } else if (state.equals(State.METHOD)) {
                    state = State.STOP;
                    PHPTokenId id = token.id();
                    if (id != null && PHPTokenId.PHP_NEW.equals(id)) {
                        metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.CONSTRUCTOR_TYPE_PREFIX);
                    } else {
                        metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FUNCTION_TYPE_PREFIX);
                    }
                    break;
                } else if (state.equals(State.PARAMS) && !possibleClassName.isEmpty() && token.id() != null
                        && PHPTokenId.PHP_NEW.equals(token.id()) && (rightBraces - 1 == leftBraces) && isPossibleAnonymousObjectCall(tokenSequence)) {
                    state = State.STOP;
                    metaAll.insert(0, PRE_OPERATION_TYPE_DELIMITER + VariousUtils.CONSTRUCTOR_TYPE_PREFIX + possibleClassName);
                    break;
                }
            }
        }
        if (state.equals(State.STOP)) {
            String retval = metaAll.toString();
            if (retval != null) {
                return retval;
            }
        }
        return null;
    }

    private static boolean isPossibleAnonymousObjectCall(final TokenSequence<PHPTokenId> tokenSequence) {
        boolean result = true;
        boolean skippedOpenParenthesis = tokenSequence.movePrevious();
        if (skippedOpenParenthesis) {
            boolean posibleMethodTokenNameExists = tokenSequence.movePrevious();
            if (posibleMethodTokenNameExists) {
                Token<PHPTokenId> token = tokenSequence.token();
                if (isString(token) || isComma(token)) {
                    result = false;
                }
                tokenSequence.moveNext();
            }
            tokenSequence.moveNext();
        }
        return result;
    }

    private static boolean isVarTypeComment(final Token<PHPTokenId> token) {
        boolean result = false;
        if (token != null) {
            CharSequence tokenText = token.text();
            if (PHPTokenId.PHP_COMMENT.equals(token.id()) && tokenText != null
                    && tokenText.toString().trim().startsWith(VAR_TYPE_COMMENT_PREFIX)) {
                result = true;
            }
        }
        return result;
    }

    private static String fetchPossibleClassName(final TokenSequence<PHPTokenId> tokenSequence) {
        String result = isString(tokenSequence.token()) ? tokenSequence.token().text().toString() : ""; //NOI18N
        while (tokenSequence.movePrevious() && (isString(tokenSequence.token()) || isNamespaceSeparator(tokenSequence.token()))) {
            result = tokenSequence.token().text().toString() + result;
        }
        tokenSequence.moveNext();
        return result;
    }

    private static StringBuilder transformToFullyQualifiedType(final StringBuilder metaAll, final TokenSequence<PHPTokenId> tokenSequence, final Scope varScope) {
        StringBuilder result = metaAll;
        String currentMetaAll = metaAll.toString();
        int indexOfType = currentMetaAll.indexOf(PRE_OPERATION_TYPE_DELIMITER);
        if (indexOfType != -1) {
            String lastType = currentMetaAll.substring(0, indexOfType);
            if (!lastType.trim().isEmpty()) {
                String qualifiedTypeName = qualifyTypeNames(lastType, tokenSequence.offset(), varScope);
                result = new StringBuilder(qualifiedTypeName + currentMetaAll.substring(indexOfType));
            }
        }
        return result;
    }

    // XXX
    public static String getVariableName(String semiType) {
        if (semiType != null) {
            String prefix = PRE_OPERATION_TYPE_DELIMITER + VariousUtils.VAR_TYPE_PREFIX;
            if (semiType.startsWith(prefix)) {
                return semiType.substring(prefix.length(), semiType.lastIndexOf(PRE_OPERATION_TYPE_DELIMITER));
            }
        }
        return null;
    }

    private static boolean skipWhitespaces(TokenSequence<PHPTokenId> tokenSequence) {
        Token<PHPTokenId> token = tokenSequence.token();
        while (token != null && isWhiteSpace(token)) {
            boolean retval = tokenSequence.movePrevious();
            token = tokenSequence.token();
            if (!retval) {
                return false;
            }
        }
        return true;
    }

    private static String translateSpecialClassName(Scope scp, String clsName) {
        TypeScope typeScope = null;
        if (scp instanceof ClassScope || scp instanceof TraitScope || scp instanceof EnumScope) {
            typeScope = (TypeScope) scp;
        } else if (scp instanceof MethodScope) {
            MethodScope msi = (MethodScope) scp;
            Scope inScope = msi.getInScope();
            if (inScope instanceof ClassScope || inScope instanceof TraitScope || inScope instanceof EnumScope) {
                typeScope = (TypeScope) inScope;
            }
        }
        if (typeScope != null) {
            switch (clsName) {
                case "self": //NOI18N
                case "this": //NOI18N
                case "static": //NOI18N
                    clsName = typeScope.getName();
                    break;
                case "parent": //NOI18N
                    if (typeScope instanceof ClassScope) {
                        ClassScope classScope = (ClassScope) typeScope;
                        QualifiedName fullyQualifiedName = ModelUtils.getFirst(classScope.getPossibleFQSuperClassNames());
                        if (fullyQualifiedName != null) {
                            clsName = fullyQualifiedName.toString();
                        } else {
                            ClassScope clzScope = ModelUtils.getFirst(classScope.getSuperClasses());
                            if (clzScope != null) {
                                clsName = clzScope.getName();
                            }
                        }
                    }
                    break;
                default:
                // no-op
            }
        }
        return clsName;
    }

    private static boolean moveToOffset(TokenSequence<PHPTokenId> tokenSequence, final int offset) {
        return tokenSequence == null || tokenSequence.move(offset) < 0;
    }

    private static boolean isDolar(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "$"); // NOI18N
    }

    private static boolean isLeftBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "("); // NOI18N
    }

    private static boolean isRightBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ")"); // NOI18N
    }

    private static boolean isRightArryBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "]"); // NOI18N
    }

    private static boolean isLeftArryBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "["); // NOI18N
    }

    private static boolean isComma(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ","); // NOI18N
    }

    private static boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OBJECT_OPERATOR)
                || token.id().equals(PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR);
    }

    private static boolean isNamespaceSeparator(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_NS_SEPARATOR);
    }

    private static boolean isWhiteSpace(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.WHITESPACE);
    }

    private static boolean isStaticReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM);
    }

    private static boolean isVariable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_VARIABLE);
    }

    private static boolean isSelf(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_SELF);
    }

    private static boolean isStatic(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STATIC);
    }

    private static boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }

    public static Collection<? extends TypeScope> getStaticTypeName(Scope inScope, String staticTypeName) {
        TypeScope csi = null;
        if (inScope instanceof MethodScope) {
            MethodScope msi = (MethodScope) inScope;
            Scope methodInScope = msi.getInScope();
            if (methodInScope instanceof ClassScope) {
                csi = (ClassScope) methodInScope;
            } else if (methodInScope instanceof TraitScope) {
                csi = (TraitScope) methodInScope;
            } else if (methodInScope instanceof EnumScope) {
                csi = (EnumScope) methodInScope;
            }
        }
        if (inScope instanceof TypeScope) {
            // e.g. const EXAMPLE = self::UNDEFINED;
            csi = (TypeScope) inScope;
        }
        if (csi != null) {
            if (Type.SELF.equalsIgnoreCase(staticTypeName) || Type.STATIC.equalsIgnoreCase(staticTypeName)) {
                return Collections.singletonList(csi);
            } else if (Type.PARENT.equalsIgnoreCase(staticTypeName) && (csi instanceof ClassScope)) {
                return ((ClassScope) csi).getSuperClasses();
            }
        }
        return IndexScopeImpl.getTypes(QualifiedName.create(staticTypeName), inScope);
    }

    public static QualifiedName getPreferredName(QualifiedName fullName, NamespaceScope contextNamespace) {
        Collection<QualifiedName> allNames = getAllNames(fullName, contextNamespace);
        int segmentCount = Integer.MAX_VALUE;
        QualifiedName retval = null;
        for (QualifiedName qualifiedName : allNames) {
            int size = qualifiedName.getSegments().size();
            if (size < segmentCount) {
                retval = qualifiedName;
                segmentCount = size;
            }
        }
        return retval;
    }

    public static Collection<QualifiedName> getAllNames(QualifiedName fullName, NamespaceScope contextNamespace) {
        Set<QualifiedName> namesProposals = new HashSet<>();
        namesProposals.addAll(getRelatives(contextNamespace, fullName));
        namesProposals.add(fullName.toFullyQualified());
        return namesProposals;
    }

    public static Collection<QualifiedName> getRelativesToUses(NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<>();
        Collection<? extends UseScope> declaredUses = contextNamespace.getAllDeclaredSingleUses();
        for (UseScope useElement : declaredUses) {
            QualifiedName proposedName = QualifiedName.getSuffix(fullName, QualifiedName.create(useElement.getName()), true);
            if (proposedName != null) {
                AliasedName aliasedName = useElement.getAliasedName();
                if (aliasedName != null) {
                    String nameWithoutAlias = proposedName.toString();
                    int indexOfNsSeparator = nameWithoutAlias.indexOf("\\"); //NOI18N
                    String newName = indexOfNsSeparator == -1
                            ? aliasedName.getAliasName()
                            : aliasedName.getAliasName() + nameWithoutAlias.substring(indexOfNsSeparator);
                    proposedName = QualifiedName.create(newName);
                }
                namesProposals.add(proposedName);
            }
        }
        return namesProposals;
    }

    public static Collection<QualifiedName> getRelativesToNamespace(NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<>();
        QualifiedName proposedName = QualifiedName.getSuffix(fullName, QualifiedName.create(contextNamespace), false);
        if (proposedName != null) {
            namesProposals.add(proposedName);
        }
        return namesProposals;
    }

    public static Collection<QualifiedName> getRelatives(NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<>();
        namesProposals.addAll(getRelativesToNamespace(contextNamespace, fullName));
        namesProposals.addAll(getRelativesToUses(contextNamespace, fullName));
        return namesProposals;
    }

    public static Collection<QualifiedName> getComposedNames(QualifiedName name, NamespaceScope contextNamespace) {
        Collection<? extends UseScope> declaredUses = contextNamespace.getAllDeclaredSingleUses();
        Set<QualifiedName> namesProposals = new HashSet<>();
        if (!name.getKind().isFullyQualified()) {
            QualifiedName proposedName = QualifiedName.create(contextNamespace).append(name).toFullyQualified();
            if (proposedName != null) {
                namesProposals.add(proposedName);
            }
            for (UseScope useElement : declaredUses) {
                final QualifiedName useQName = QualifiedName.create(useElement.getName());
                proposedName = useQName.toNamespaceName().append(name).toFullyQualified();
                if (proposedName != null) {
                    namesProposals.add(proposedName);
                }
                if (!useQName.getName().equalsIgnoreCase(name.getName())) {
                    proposedName = useQName.append(name).toFullyQualified();
                    if (proposedName != null) {
                        namesProposals.add(proposedName);
                    }
                }
            }
        }
        namesProposals.add(name);
        return namesProposals;
    }

    /**
     * This method is trying to guess the full qualified name from a name. Names
     * are resolved following these resolution rules like in the php runtime:
     *
     * 1. Calls to fully qualified functions, classes or constants are resolved
     * at compile-time. For instance new \A\B resolves to class A\B. 2. All
     * unqualified and qualified names (not fully qualified names) are
     * translated during compilation according to current import rules. For
     * example, if the namespace A\B\C is imported as C, a call to C\D\e() is
     * translated to A\B\C\D\e(). 3. Inside a namespace, all qualified names not
     * translated according to import rules have the current namespace
     * prepended. For example, if a call to C\D\e() is performed within
     * namespace A\B, it is translated to A\B\C\D\e(). 4. Unqualified class
     * names are translated during compilation according to current import rules
     * (full name substituted for short imported name). In example, if the
     * namespace A\B\C is imported as C, new C() is translated to new A\B\C().
     * 5. Inside namespace (say A\B), calls to unqualified functions are
     * resolved at run-time. Here is how a call to function foo() is resolved:
     * 1. It looks for a function from the current namespace: A\B\foo(). 2. It
     * tries to find and call the global function foo(). 6. Inside namespace
     * (say A\B), calls to unqualified or qualified class names (not fully
     * qualified class names) are resolved at run-time. Here is how a call to
     * new C() or new D\E() is resolved. For new C(): 1. It looks for a class
     * from the current namespace: A\B\C. 2. It attempts to autoload A\B\C. For
     * new D\E(): 1. It looks for a class by prepending the current namespace:
     * A\B\D\E. 2. It attempts to autoload A\B\D\E. To reference any global
     * class in the global namespace, its fully qualified name new \C() must be
     * used.
     *
     * @param name the qualified name that should be resolved according the
     * mentioned rules.
     * @param nameOffset Offset of the name that should be resolved. The
     * resolving full qualified names depends on the location of imports (use
     * declaration).
     * @param contextNamespace Namespace where is the qualified name located
     * @return collection of full qualified names that fits the input name in
     * the name space context. Usually the method returns just one, but it can
     * return, if is not clear whether the name belongs to the defined namespace
     * or to the default one.
     */
    public static Collection<QualifiedName> getPossibleFQN(QualifiedName name, int nameOffset, NamespaceScope contextNamespace) {
        Set<QualifiedName> namespaces = new HashSet<>();
        boolean resolved = false;
        if (name.getKind().isFullyQualified()) {
            namespaces.add(name);
            resolved = true;
        } else {
            Collection<? extends UseScope> uses = contextNamespace.getAllDeclaredSingleUses();
            if (uses.size() > 0) {
                for (UseScope useDeclaration : contextNamespace.getAllDeclaredSingleUses()) {
                    if (useDeclaration.getOffset() < nameOffset) {
                        String firstNameSegment = name.getSegments().getFirst();
                        QualifiedName returnName;
                        if ((useDeclaration.getAliasedName() != null
                                && firstNameSegment.equals(useDeclaration.getAliasedName().getAliasName()))) {
                            returnName = useDeclaration.getAliasedName().getRealName();
                        } else {
                            returnName = QualifiedName.create(useDeclaration.getName());
                            if (!firstNameSegment.equals(returnName.getSegments().getLast())) {
                                returnName = null;
                            }
                        }
                        if (returnName != null) {
                            for (int i = 1; i < name.getSegments().size(); i++) {
                                returnName = returnName.append(name.getSegments().get(i));
                            }
                            namespaces.add(returnName.toFullyQualified());
                            resolved = true;
                        }
                    }
                }
            }
        }
        if (!resolved) {
            if (name.getKind().isUnqualified()) {
                namespaces.add(contextNamespace.getNamespaceName().append(name).toFullyQualified());
            } else {
                // the name is qualified -> append the name to the namespace name
                namespaces.add(QualifiedName.create(contextNamespace).append(name).toFullyQualified());
            }
        }
        return namespaces;
    }

    public static boolean isAliased(final QualifiedName qualifiedName, final int offset, final Scope inScope) {
        boolean result = false;
        if (!qualifiedName.getKind().isFullyQualified() && !isSpecialClassName(qualifiedName.getName())) {
            result = isAliasedClassName(qualifiedName.getSegments().getFirst(), offset, inScope);
        }
        return result;
    }

    public static boolean isAlias(final QualifiedName unqualifiedName, final int offset, final Scope inScope) {
        boolean result = false;
        if (unqualifiedName.getKind().isUnqualified() && !isSpecialClassName(unqualifiedName.getName())) {
            result = isAliasedClassName(unqualifiedName.getSegments().getFirst(), offset, inScope);
        }
        return result;
    }

    private static boolean isAliasedClassName(final String className, final int offset, final Scope inScope) {
        boolean result = false;
        Scope scope = inScope;
        while (scope != null && !(scope instanceof NamespaceScope)) {
            scope = scope.getInScope();
        }
        if (scope != null) {
            result = isAlias(className, offset, (NamespaceScope) scope);
        }
        return result;
    }

    private static boolean isAlias(final String name, final int offset, final NamespaceScope namespaceScope) {
        boolean result = false;
        for (UseScope useElement : namespaceScope.getAllDeclaredSingleUses()) {
            if (useElement.getOffset() < offset) {
                AliasedName aliasName = useElement.getAliasedName();
                if (aliasName != null) {
                    if (name.equals(aliasName.getAliasName())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    @CheckForNull
    public static AliasedName getAliasedName(final QualifiedName qualifiedName, final int offset, final Scope inScope) {
        AliasedName result = null;
        if (!qualifiedName.getKind().isFullyQualified() && !isSpecialClassName(qualifiedName.getName())) {
            Scope scope = inScope;
            while (scope != null && !(scope instanceof NamespaceScope)) {
                scope = scope.getInScope();
            }
            if (scope != null) {
                NamespaceScope namespaceScope = (NamespaceScope) scope;
                String firstSegmentName = qualifiedName.getSegments().getFirst();
                for (UseScope useElement : namespaceScope.getAllDeclaredSingleUses()) {
                    if (useElement.getOffset() < offset) {
                        AliasedName aliasName = useElement.getAliasedName();
                        if (aliasName != null) {
                            if (firstSegmentName.equals(aliasName.getAliasName())) {
                                result = aliasName;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static QualifiedName getFullyQualifiedName(QualifiedName qualifiedName, int offset, Scope inScope) {
        return TypeNameResolverImpl.forFullyQualifiedName(inScope, offset).resolve(qualifiedName);
    }

    /**
     * Resolves fully qualified type names from their simple names.
     *
     * @param typeNames Type names in to format: string|ClassName|null
     * @param offset Offset, where the type is resolved.
     * @param inScope Scope, where the type is resolved.
     * @return Fully qualified type names in the format:
     * string|\Foo\ClassName|null
     */
    public static String qualifyTypeNames(String typeNames, int offset, Scope inScope) {
        // GH-4725: PHP 8.2 Disjunctive Normal Form Types
        // e.g. (X&Y)|(A&B)|Countable
        StringBuilder sb = new StringBuilder();
        if (typeNames != null) {
            if (typeNames.contains("(")) { // NOI18N
                String[] split = TYPE_SEPARATOR_PATTERN.split(typeNames);
                for (String type : split) {
                    if (sb.length() > 0) {
                        sb.append(Type.SEPARATOR);
                    }
                    String typeName = type.replace("(", "").replace(")", ""); // NOI18N
                    boolean isIntersectionType = typeName.contains(Type.SEPARATOR_INTERSECTION);
                    if (isIntersectionType) {
                        sb.append("(").append(qualifyUnionOrIntersectionTypeNames(typeName, offset, inScope)).append(")"); // NOI18N
                    } else {
                        sb.append(qualifyUnionOrIntersectionTypeNames(typeName, offset, inScope));
                    }
                }
            } else {
                sb.append(qualifyUnionOrIntersectionTypeNames(typeNames, offset, inScope));
            }
        }
        return sb.toString();
    }

    private static String qualifyUnionOrIntersectionTypeNames(String typeNames, int offset, Scope inScope) {
        StringBuilder retval = new StringBuilder();
        if (typeNames != null) {
            if (!typeNames.matches(SPACES_AND_TYPE_DELIMITERS)) {
                boolean isIntersection = typeNames.contains(Type.SEPARATOR_INTERSECTION);
                String[] types = isIntersection
                        ? TYPE_SEPARATOR_INTERSECTION_PATTERN.split(typeNames)
                        : TYPE_SEPARATOR_PATTERN.split(typeNames);
                for (String typeName : types) {
                    String typeRawPart = typeName;
                    if (CodeUtils.isNullableType(typeRawPart)) {
                        retval.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                        typeRawPart = typeRawPart.substring(1);
                    }
                    String typeArrayPart = ""; //NOI18N
                    int indexOfArrayDelim = typeName.indexOf('[');
                    if (indexOfArrayDelim != -1) {
                        typeRawPart = typeName.substring(0, indexOfArrayDelim);
                        typeArrayPart = typeName.substring(indexOfArrayDelim);
                    }
                    if ("$this".equals(typeName)) { //NOI18N
                        // #239987
                        retval.append("\\this").append(Type.getTypeSeparator(isIntersection)); //NOI18N
                    } else if (!typeRawPart.startsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR) && !Type.isPrimitive(typeRawPart)) {
                        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(typeRawPart), offset, inScope);
                        retval.append(fullyQualifiedName.toString().startsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR)
                                ? "" //NOI18N
                                : NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
                        retval.append(fullyQualifiedName.toString()).append(typeArrayPart).append(Type.getTypeSeparator(isIntersection));
                    } else {
                        retval.append(typeRawPart).append(typeArrayPart).append(Type.getTypeSeparator(isIntersection));
                    }
                }
                assert retval.length() - Type.getTypeSeparator(isIntersection).length() >= 0 : "retval:" + retval + "# typeNames:" + typeNames; //NOI18N
                retval = new StringBuilder(retval.toString().substring(0, retval.length() - Type.getTypeSeparator(isIntersection).length()));
            }
        }
        return retval.toString();
    }

    /**
     * Check if a className is "self", "static", or "parent".
     *
     * @param className
     * @return
     */
    public static boolean isSpecialClassName(final String className) {
        return SPECIAL_CLASS_NAMES.contains(className.toLowerCase());
    }

    /**
     * Check if a className is "self" or "static".
     *
     * @param className
     * @return
     */
    public static boolean isStaticClassName(String className) {
        return className != null && STATIC_CLASS_NAMES.contains(className.toLowerCase());
    }

    public static boolean isSemiType(String typeName) {
        return typeName != null && typeName.contains(PRE_OPERATION_TYPE_DELIMITER);
    }

    public static List<String> getAllTypeNames(String declaredTypes) {
        if (!StringUtils.hasText(declaredTypes)) {
            return Collections.emptyList();
        }
        List<String> typeNames = new ArrayList<>();
        // e.g. (X&Y)|Z
        for (String typeName : CodeUtils.SPLIT_TYPES_PATTERN.split(declaredTypes.trim())) {
            if (!typeName.isEmpty() && !VariousUtils.isSemiType(typeName)) {
                typeNames.add(typeName);
            }
        }
        return typeNames;
    }

    //~ inner class
    private static class CloneExpressionInfo {

        private int endOffset = -1;
        private State reference = null;

        public int getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public State getReference() {
            return reference;
        }

        public void setReference(State reference) {
            this.reference = reference;
        }
    }
}
