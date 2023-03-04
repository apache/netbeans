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
package org.netbeans.modules.php.editor.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * @author Radek Matous
 */
public class ParameterInfoSupport {
    private ModelVisitor modelVisitor;
    private int offset;

    ParameterInfoSupport(ModelVisitor modelVisitor, int offset) {
        this.modelVisitor = modelVisitor;
        this.offset = offset;
    }

    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE);


    private enum State {
       START, METHOD, INVALID, VARBASE, DOLAR, PARAMS, REFERENCE, STATIC_REFERENCE, FUNCTION, FIELD, VARIABLE, CLASSNAME, STOP
    };

    public ParameterInfo getParameterInfo() {
        ParameterInfo retval = parametersNodeImpl(offset, modelVisitor.getCompilationInfo());
        if (retval == ParameterInfo.NONE) {
            retval = parametersTokenImpl();
        }
        return retval;
    }

    private ParameterInfo parametersTokenImpl() {
        FileScope modelScope = modelVisitor.getFileScope();
        VariableScope nearestVariableScope = modelVisitor.getNearestVariableScope(offset);

        if (modelScope == null || nearestVariableScope == null) {
            return ParameterInfo.NONE;
        }
        TokenHierarchy<?> tokenHierarchy = modelVisitor.getCompilationInfo().getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(tokenHierarchy, offset);

        if (moveToOffset(tokenSequence, offset)) {
            return ParameterInfo.NONE;
        }

        int commasCount = 0;
        int anchor = -1;
        State state = State.PARAMS;
        int leftBraces = 0;
        int rightBraces = 1;
        StringBuilder metaAll = new StringBuilder();
        while (!state.equals(State.INVALID) && !state.equals(State.STOP) && tokenSequence.movePrevious() && skipWhitespaces(tokenSequence)) {
            Token<PHPTokenId> token = tokenSequence.token();
            if (!CTX_DELIMITERS.contains(token.id())) {
                switch (state) {
                    case METHOD:
                    case START:
                        state = (state.equals(State.METHOD)) ? State.STOP : State.INVALID;
                        // state = State.INVALID;
                        if (isReference(token)) {
                            metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.REFERENCE;
                        } else if (isStaticReference(token)) {
                            metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.STATIC_REFERENCE;
                        } else if (state.equals(State.STOP)) {
                            metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FUNCTION_TYPE_PREFIX);
                        }
                        break;
                    case REFERENCE:
                        state = State.INVALID;
                        if (isRightBracket(token)) {
                            rightBraces++;
                            state = State.PARAMS;
                        } else if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.FIELD;
                        } else if (isVariable(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.VARBASE;
                        }
                        break;
                    case STATIC_REFERENCE:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isSelf(token)) {
                            metaAll.insert(0, buildStaticClassName(nearestVariableScope, token.text().toString()));
                            //TODO: maybe rather introduce its own State
                            state = State.CLASSNAME;
                        }
                        break;
                    case PARAMS:
                        state = State.INVALID;
                        if (isWhiteSpace(token)) {
                            state = State.PARAMS;
                        } else if (isComma(token)) {
                            if (metaAll.length() == 0) {
                                commasCount++;
                            }
                            state = State.PARAMS;
                        } else if (isVariable(token)) {
                            state = State.PARAMS;
                        } else if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        } else if (isLeftBracket(token)) {
                            leftBraces++;
                        } else if (isRightBracket(token)) {
                            rightBraces++;
                        } else {
                            state = State.PARAMS;
                        }
                        if (leftBraces == rightBraces) {
                            state = State.FUNCTION;
                        }
                        break;
                    case FUNCTION:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            if (anchor == -1) {
                                anchor = tokenSequence.offset() + token.text().length();
                            }
                            state = State.METHOD;
                        }
                        break;
                    case FIELD:
                        state = State.INVALID;
                        if (isReference(token)) {
                            metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FIELD_TYPE_PREFIX);
                            state = State.REFERENCE;
                        }
                        break;
                    case VARBASE:
                        if (isStaticReference(token)) {
                            state = State.STATIC_REFERENCE;
                            break;
                        }
                    case VARIABLE:
                        metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.VAR_TYPE_PREFIX);
                        state = State.STOP;
                        break;
                    case CLASSNAME:
                        //TODO: self, parent not handled yet
                        //TODO: maybe rather introduce its own State for self, parent
                        state = State.STOP;
                        break;
                    default:
                        //no-op
                }
            } else {
                if (state.equals(State.METHOD)) {
                    state = State.STOP;
                    PHPTokenId id = token.id();
                    if (id != null && PHPTokenId.PHP_NEW.equals(id)) {
                        metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.CONSTRUCTOR_TYPE_PREFIX);
                    } else {
                        metaAll.insert(0, VariousUtils.PRE_OPERATION_TYPE_DELIMITER + VariousUtils.FUNCTION_TYPE_PREFIX);
                    }
                    break;
                }
            }

        }
        if (state.equals(State.STOP)) {
            String typeName = metaAll.toString();
            ArrayDeque<? extends ModelElement> elemenst = VariousUtils.getElements(modelScope, nearestVariableScope, typeName, offset);
            if (!elemenst.isEmpty()) {
                ModelElement element = elemenst.peek();
                if (element instanceof FunctionScope) {
                    return new ParameterInfo(toParamNames((FunctionScope) element), commasCount, anchor);
                }
            }
        }

        return ParameterInfo.NONE;
    }



    private static String buildStaticClassName(Scope scp, String staticClzName) {
        if (scp instanceof MethodScope) {
            MethodScope msi = (MethodScope) scp;
            ClassScope csi = (ClassScope) msi.getInScope();
            switch (staticClzName) {
                case "self": //NOI18N
                    staticClzName = csi.getName();
                    break;
                case "parent": //NOI18N
                    ClassScope clzScope = ModelUtils.getFirst(csi.getSuperClasses());
                    if (clzScope != null) {
                        staticClzName = clzScope.getName();
                    }
                    break;
                default:
                    // no-op
            }
        }
        return staticClzName;
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

    private static boolean isComma(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ","); // NOI18N
    }

    private static boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OBJECT_OPERATOR);
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

    private static boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }

    private static ParameterInfo parametersNodeImpl(final int caretOffset, final ParserResult info) {
        final ParameterInfo[] retval = new ParameterInfo[1];
        retval[0] = ParameterInfo.NONE;
        DefaultVisitor visitor = new DefaultVisitor() {

            @Override
            public void scan(ASTNode node) {
                if (node != null) {
                    OffsetRange range = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                    if (range.containsInclusive(caretOffset)) {
                        super.scan(node);
                    }
                }
            }

            @Override
            public void visit(ClassInstanceCreation node) {
                ASTNodeInfo<ClassInstanceCreation> nodeInfo = ASTNodeInfo.create(node);
                retval[0] = createParameterInfo(nodeInfo, node.ctorParams());
                super.visit(node);
            }


            @Override
            public void visit(FunctionInvocation node) {
                ASTNodeInfo<FunctionInvocation> nodeInfo = ASTNodeInfo.create(node);
                retval[0] = createParameterInfo(nodeInfo, node.getParameters());
                super.visit(node);
            }

            private ParameterInfo createParameterInfo(ASTNodeInfo nodeInfo, List<Expression> parameters) {
                int idx;
                ASTNode node = nodeInfo.getOriginalNode();
                int anchor  = nodeInfo.getRange().getEnd();
                OffsetRange offsetRange = new OffsetRange(anchor, node.getEndOffset());
                if (offsetRange.containsInclusive(caretOffset)) {
                    idx = 0;
                    for (int i = 0; i < parameters.size(); i++) {
                        Expression expression = parameters.get(i);
                        offsetRange = new OffsetRange(expression.getStartOffset(), expression.getEndOffset());
                        if (offsetRange.containsInclusive(caretOffset)) {
                            idx = i;
                        } else {
                            offsetRange = new OffsetRange(expression.getEndOffset(), node.getEndOffset());
                            if (offsetRange.containsInclusive(caretOffset)) {
                                idx = i + 1;
                            }
                        }
                    }
                    final Model model = ((PHPParseResult) info).getModel();
                    OccurencesSupport occurencesSupport = model.getOccurencesSupport((nodeInfo.getRange().getStart() + anchor) / 2);
                    Occurence occurence = occurencesSupport.getOccurence();
                    if (occurence != null) {
                        Collection<? extends PhpElement> allDeclarations = occurence.getAllDeclarations();
                        if (allDeclarations.size() > 0) {
                            PhpElement declaration = allDeclarations.iterator().next();
                            final boolean oneDeclaration = occurence.getAllDeclarations().size() == 1;
                            if (declaration instanceof FunctionScope && oneDeclaration) {
                                List<String> paramNames = toParamNames((FunctionScope) declaration);
                                return paramNames.isEmpty() ? ParameterInfo.NONE : new ParameterInfo(paramNames, idx, anchor);
                            } else if (declaration instanceof BaseFunctionElement && oneDeclaration) {
                                List<String> paramNames = toParamNames((BaseFunctionElement) declaration);
                                return paramNames.isEmpty() ? ParameterInfo.NONE : new ParameterInfo(paramNames, idx, anchor);
                            } else if (declaration instanceof ClassElement && oneDeclaration) {
                                List<String> paramNames = toParamNames((ClassElement) declaration);
                                return paramNames.isEmpty() ? ParameterInfo.NONE : new ParameterInfo(paramNames, idx, anchor);
                            }

                        }
                    }
                }
                return ParameterInfo.NONE;
            }
        };
        Program root = Utils.getRoot(info);
        if (root != null) {
            visitor.scan(root);
        }
        return retval[0];
    }

    @NonNull
    private static List<String> toParamNames(FunctionScope functionScope) {
        List<String> paramNames = new ArrayList<>();
        List<? extends ParameterElement> parameters = functionScope.getParameters();
        for (ParameterElement parameter : parameters) {
            paramNames.add(parameter.asString(OutputType.SHORTEN_DECLARATION));
        }
        return paramNames;
    }
    @CheckForNull
    private static List<String> toParamNames(BaseFunctionElement functionElement) {
        List<String> paramNames = new ArrayList<>();
        List<? extends ParameterElement> parameters = functionElement.getParameters();
        for (ParameterElement parameter : parameters) {
            paramNames.add(parameter.asString(OutputType.SHORTEN_DECLARATION));
        }
        return paramNames;
    }
    @CheckForNull
    private static List<String> toParamNames(ClassElement clzElement) {
        List<String> paramNames = new ArrayList<>();
        ElementQuery elementQuery = clzElement.getElementQuery();
        if (elementQuery instanceof ElementQuery.Index) {
            ElementQuery.Index index = (Index) elementQuery;
            Iterator<MethodElement> iterator = index.getConstructors(clzElement).iterator();
            MethodElement constructor = iterator.hasNext() ? iterator.next() : null;
            if (constructor != null) {
                List<? extends ParameterElement> parameters = constructor.getParameters();
                for (ParameterElement parameter : parameters) {
                    paramNames.add(parameter.asString(OutputType.SHORTEN_DECLARATION));
                }
            }
        }
        return paramNames;
    }
}
