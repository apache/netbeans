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
package org.netbeans.modules.micronaut.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import static org.netbeans.modules.micronaut.expression.ExpressionTree.*;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageParser {

    public static final Pattern MEXP_PATTERN = Pattern.compile("#\\{(.*?)}");

    private final TokenSequence<?> ts;
    private String tokenType;
    private int lastPos;

    public MicronautExpressionLanguageParser(String text) {
        TokenHierarchy<String> th = TokenHierarchy.create(text, Language.find("text/x-micronaut-el"));
        this.ts = th.tokenSequence();
        next();
    }

    public ExpressionTree parse() {
        List<ExpressionTree> expressions = new ArrayList<>();
        while (!tokenType.isEmpty()) {
            int pos = ts.offset();
            expressions.add(expression());
            if (ts.offset() == pos) {
                next();
            }
        }
        return expressions.isEmpty() ? null : expressions.size() == 1 ? expressions.get(0) : new Erroneous(expressions, ts.offset(), ts.offset() + ts.token().length());
    }

    /**
     * Expression
     *  : TernaryExpression
     *  ;
     */
    private ExpressionTree expression() {
        return ternaryExpression();
    }

    /**
     * TernaryExpression
     *  : OrExpression
     *  | OrExpression '?' Expression ':' Expression
     *  ;
     */
    private ExpressionTree ternaryExpression() {
        ExpressionTree orExpression = orExpression();
        if ("keyword.control.ternary.qmark.mexp".equals(tokenType)) {
            next();
            ExpressionTree trueExpr = expression();
            ExpressionTree falseExpr;
            if ("keyword.control.ternary.colon.mexp".equals(tokenType)) {
                next();
                falseExpr = expression();
            } else {
                falseExpr = erroneous();
            }
            return new TernaryExpression(orExpression, trueExpr, falseExpr);
        } else if ("keyword.operator.elvis.mexp".equals(tokenType)) {
            next();
            ExpressionTree falseExpr = expression();
            return new BinaryExpression(Kind.ELVIS, orExpression, falseExpr);
        }
        return orExpression;
    }

    /**
     * OrExpression
     *   : AndExpression
     *   | OrExpression '||' AndExpression
     *   ;
     */
    private ExpressionTree orExpression() {
        ExpressionTree leftNode = andExpression();
        while ("keyword.operator.logical.or.mexp".equals(tokenType)) {
            next();
            leftNode = new BinaryExpression(Kind.OR, leftNode, andExpression());
        }
        return leftNode;
    }

    /**
     * AndExpression
     *   : EqualityExpression
     *   | AndExpression '&&' EqualityExpression
     *   ;
     */
    private ExpressionTree andExpression() {
        ExpressionTree leftNode = equalityExpression();
        while ("keyword.operator.logical.and.mexp".equals(tokenType)) {
            next();
            leftNode = new BinaryExpression(Kind.AND, leftNode, equalityExpression());
        }
        return leftNode;
    }

    /**
     * EqualityExpression
     *  : RelationalExpression
     *  | EqualityExpression '==' RelationalExpression
     *  | EqualityExpression '!=' RelationalExpression
     *  ;
     */
    private ExpressionTree equalityExpression() {
        ExpressionTree leftNode = relationalExpression();
        while ("keyword.operator.comparison.eq.mexp".equals(tokenType)) {
            String tokenText = ts.token().text().toString();
            next();
            leftNode = new BinaryExpression("==".equals(tokenText) ? Kind.EQUAL_TO : Kind.NOT_EQUAL_TO, leftNode, relationalExpression());
        }
        return leftNode;
    }

    /**
     * RelationalExpression
     *  : AdditiveExpression
     *  | RelationalExpression '&gt;' AdditiveExpression
     *  | RelationalExpression '&lt;' AdditiveExpression
     *  | RelationalExpression '&gt;=' AdditiveExpression
     *  | RelationalExpression '&lt;=' AdditiveExpression
     *  | RelationalExpression 'matches' StringLiteral
     *  | RelationalExpression 'instanceof' TypeReference
     *  ;
     */
    private ExpressionTree relationalExpression() {
        ExpressionTree leftNode = additiveExpression();
        while ("keyword.operator.comparison.rel.mexp".equals(tokenType) || "keyword.operator.instanceof.mexp".equals(tokenType)) {
            String tokenText = ts.token().text().toString();
            next();
            switch (tokenText) {
                case ">":
                    leftNode = new BinaryExpression(Kind.GREATER_THAN, leftNode, additiveExpression());
                    break;
                case "<":
                    leftNode = new BinaryExpression(Kind.LESS_THAN, leftNode, additiveExpression());
                    break;
                case ">=":
                    leftNode = new BinaryExpression(Kind.GREATER_THAN_EQUAL, leftNode, additiveExpression());
                    break;
                case "<=":
                    leftNode = new BinaryExpression(Kind.LESS_THAN_EQUAL, leftNode, additiveExpression());
                    break;
                case "matches":
                    leftNode = new BinaryExpression(Kind.MATCHES, leftNode, stringLiteral());
                    break;
                case "instanceof":
                    leftNode = new InstanceOf(leftNode, typeReference(true));
                    break;
            }
        }
        return leftNode;
    }

    /**
     * AdditiveExpression
     *  : MultiplicativeExpression
     *  | AdditiveExpression '+' MultiplicativeExpression
     *  | AdditiveExpression '-' MultiplicativeExpression
     *  ;
     */
    private ExpressionTree additiveExpression() {
        ExpressionTree leftNode = multiplicativeExpression();
        while ("keyword.operator.arithmetic.add.mexp".equals(tokenType)) {
            String tokenText = ts.token().text().toString();
            next();
            leftNode = new BinaryExpression("+".equals(tokenText) ? Kind.PLUS : Kind.MINUS, leftNode, multiplicativeExpression());
        }
        return leftNode;
    }

    /**
     * MultiplicativeExpression
     *  : PowExpression
     *  | MultiplicativeExpression '*' PowExpression
     *  | MultiplicativeExpression '/' PowExpression
     *  | MultiplicativeExpression 'div' PowExpression
     *  | MultiplicativeExpression '%' PowExpression
     *  | MultiplicativeExpression 'mod' PowExpression
     *  ;
     */
    private ExpressionTree multiplicativeExpression() {
        ExpressionTree leftNode = powExpression();
        while ("keyword.operator.arithmetic.mul.mexp".equals(tokenType)) {
            String tokenText = ts.token().text().toString();
            next();
            switch (tokenText) {
                case "*":
                    leftNode = new BinaryExpression(Kind.MULTIPLY, leftNode, powExpression());
                    break;
                case "/":
                case "div":
                    leftNode = new BinaryExpression(Kind.DIVIDE, leftNode, powExpression());
                    break;
                case "%":
                case "mod":
                    leftNode = new BinaryExpression(Kind.REMAINDER, leftNode, powExpression());
                    break;
            }
        }
        return leftNode;
    }

    /**
     * PowExpression
     *  : UnaryExpression
     *  | PowExpression '^' UnaryExpression
     *  ;
     */
    private ExpressionTree powExpression() {
        ExpressionTree leftNode = unaryExpression();
        while ("keyword.operator.arithmetic.pow.mexp".equals(tokenType)) {
            next();
            leftNode = new BinaryExpression(Kind.POWER, leftNode, unaryExpression());
        }
        return leftNode;
    }

    /**
     * UnaryExpression
     *  : '+' UnaryExpression
     *  | '-' UnaryExpression
     *  | '!' UnaryExpression
     *  | 'not' UnaryExpression
     *  | 'empty' UnaryExpression
     *  | PostfixExpression
     *  ;
     */
    private ExpressionTree unaryExpression() {
        String tokenText = ts.token().text().toString();
        int pos = tokenType.isEmpty() ? lastPos : ts.offset();
        switch (tokenText) {
            case "+":
                next();
                return new UnaryExpression(Kind.UNARY_PLUS, unaryExpression(), pos);
            case "-":
                next();
                return new UnaryExpression(Kind.UNARY_MINUS, unaryExpression(), pos);
            case "!":
            case "not":
                next();
                return new UnaryExpression(Kind.NOT, unaryExpression(), pos);
            case "empty":
                next();
                return new UnaryExpression(Kind.EMPTY, unaryExpression(), pos);
            default:
                return postfixExpression();
        }
    }

    /**
     * PostfixExpression
     *  : PrimaryExpression
     *  | PostfixExpression '.' MethodOrPropertyAccess
     *  | PostfixExpression '?.' MethodOrPropertyAccess
     *  | PostfixExpression ArrayAccess
     *  ;
     */
    private ExpressionTree postfixExpression() {
        ExpressionTree leftNode = primaryExpression();
        while ("punctuation.accessor.mexp".equals(tokenType)
                || "punctuation.accessor.optional.mexp".equals(tokenType)
                || "punctuation.bracket.square.beging.mexp".equals(tokenType)) {
            String tokenText = ts.token().text().toString();
            switch (tokenText) {
                case ".":
                case "?.":
                    next();
                    leftNode = methodOrPropertyAccess(leftNode);
                    break;
                default:
                    leftNode = arrayAccess(leftNode);
            }
        }
        return leftNode;
    }

    /**
     * PrimaryExpression
     *  : EvaluationContextAccess
     *  | EnvironmentAccess
     *  | BeanContextAccess
     *  | ThisAccess
     *  | TypeReference
     *  | ParenthesizedExpression
     *  | Literal
     *  ;
     */
    private ExpressionTree primaryExpression() {
        switch (tokenType) {
            case "punctuation.accessor.bean.mexp":
                return evaluationContextAccess(true);
            case "entity.name.function.mexp":
            case "variable.other.object.property.mexp":
                return evaluationContextAccess(false);
            case "support.function.environment.mexp":
                return environmentAccess();
            case "support.function.bean-context.mexp":
                return beanContextAccess();
            case "variable.language.this.mexp":
                return thisAccess();
            case "support.function.type-reference.mexp":
                return typeReference(true);
            case "punctuation.bracket.round.begin.mexp":
                return parenthesizedExpression();
            default:
                return literal();
        }
    }

    /**
     * ThisAccess
     *  : 'this'
     *  ;
     */
    private ExpressionTree thisAccess() {
        ThisAccess access = new ThisAccess(tokenType.isEmpty() ? lastPos : ts.offset(), ts.offset() + ts.token().length());
        next();
        return access;
    }

    /**
     * EvaluationContextAccess
     *  : '#' Identifier
     *  | '#' Identifier MethodArguments
     *  | Identifier
     *  | Identifier MethodArguments
     *  ;
     */
    private ExpressionTree evaluationContextAccess(boolean prefixed) {
        int start = tokenType.isEmpty() ? lastPos : ts.offset();
        if (prefixed) {
            next();
        }
        if ("entity.name.function.mexp".equals(tokenType)) {
            String identifier = ts.token().text().toString();
            next();
            List<? extends ExpressionTree> arguments = methodArguments();
            return new MethodCall(identifier, arguments, start, lastPos);
        }
        if ("variable.other.object.property.mexp".equals(tokenType)) {
            String identifier = ts.token().text().toString();
            next();
            return new PropertyAccess(identifier, start, lastPos);
        }
        return erroneous(start);
    }

    /**
     * BeanContextAccess
     *  : 'ctx' '[' TypeReference ']'
     *  ;
     */
    private ExpressionTree beanContextAccess() {
        int start = tokenType.isEmpty() ? lastPos : ts.offset();
        next();
        if (!"punctuation.bracket.square.beging.mexp".equals(tokenType)) {
            return erroneous(start);
        }
        next();
        TypeReference typeReference = typeReference("support.function.type-reference.mexp".equals(tokenType));
        if ("punctuation.bracket.square.end.mexp".equals(tokenType)) {
            BeanContextAccess access = new BeanContextAccess(typeReference, start, ts.offset() + ts.token().length());
            next();
            return access;
        }
        return erroneous(start, typeReference);
    }

    /**
     * EnvironmentAccess
     *  : 'env' '[' Expression ']'
     *  ;
     */
    private ExpressionTree environmentAccess() {
        int start = tokenType.isEmpty() ? lastPos : ts.offset();
        next();
        if (!"punctuation.bracket.square.beging.mexp".equals(tokenType)) {
            return erroneous(start);
        }
        next();
        ExpressionTree propertyName = expression();
        if ("punctuation.bracket.square.end.mexp".equals(tokenType)) {
            EnvironmentAccess access = new EnvironmentAccess(propertyName, start, ts.offset() + ts.token().length());
            next();
            return access;
        }
        return erroneous(start, propertyName);
    }

    /**
     * MethodOrFieldAccess
     *  : SimpleIdentifier
     *  | SimpleIdentifier MethodArguments
     *  ;
     */
    private ExpressionTree methodOrPropertyAccess(ExpressionTree callee) {
        if ("entity.name.function.mexp".equals(tokenType)) {
            String identifier = ts.token().text().toString();
            next();
            List<? extends ExpressionTree> arguments = methodArguments();
            return new MethodCall(callee, identifier, arguments, lastPos);
        }
        if ("variable.other.object.property.mexp".equals(tokenType)) {
            String identifier = ts.token().text().toString();
            next();
            return new PropertyAccess(callee, identifier, lastPos);
        }
        return erroneous(new PropertyAccess(callee, "", tokenType.isEmpty() ? ts.offset() + ts.token().length() : ts.offset()));
    }

    /**
     * ArrayAccess
     *  : '[' Expression ']'
     *  ;
     */
    private ExpressionTree arrayAccess(ExpressionTree callee) {
        next();
        ExpressionTree index = expression();
        if ("punctuation.bracket.square.end.mexp".equals(tokenType)) {
            ArrayAccess access = new ArrayAccess(callee, index, ts.offset() + ts.token().length());
            next();
            return access;
        }
        return erroneous(callee, index);
    }

    /**
     * MethodArguments
     *  : '(' MethodArgumentsList ')'
     *  ;
     */
    private List<? extends ExpressionTree> methodArguments() {
        next();
        List<? extends ExpressionTree> arguments = new ArrayList<>();
        if (!"punctuation.bracket.round.end.mexp".equals(tokenType)) {
            arguments = methodArgumentsList();
        }
        next();
        return arguments;
    }

    /**
     * MethodArgumentsList
     *  : Expression
     *  | MethodArgumentsList ',' Expression
     *  ;
     */
    private List<? extends ExpressionTree> methodArgumentsList() {
        List<ExpressionTree> arguments = new ArrayList<>();
        arguments.add(expression());
        while (!"punctuation.bracket.round.end.mexp".equals(tokenType)) {
            if ("punctuation.separator.arguments.mexp".equals(tokenType)) {
                next();
            }
            int pos = ts.offset();
            arguments.add(expression());
            if (pos == ts.offset() && !"punctuation.bracket.round.end.mexp".equals(tokenType)
                    && "punctuation.separator.arguments.mexp".equals(tokenType)) {
                next();
            }
        }
        return arguments;
    }

    /**
     * TypeReference
     *   : 'T' '(' ChainedIdentifier ')'
     *   | ChainedIdentifier
     *   ;
     */
    private TypeReference typeReference(boolean wrapped) {
        int start = tokenType.isEmpty() ? ts.offset() + ts.token().length() : ts.offset();
        if (wrapped) {
            next();
            if ("punctuation.bracket.round.begin.mexp".equals(tokenType)) {
                next();
            }
        }
        int typeStart = tokenType.isEmpty() ? ts.offset() + ts.token().length() : ts.offset();
        StringBuilder sb = new StringBuilder();
        while ("storage.type.java".equals(tokenType)) {
            sb.append(ts.token().text());
            next();
            if (!"punctuation.accessor.mexp".equals(tokenType)) {
                break;
            }
            sb.append('.');
            next();
        }
        if (wrapped && "punctuation.bracket.round.end.mexp".equals(tokenType)) {
            next();
        }
        return new TypeReference(sb.toString(), typeStart, start, Math.max(start, lastPos));
    }

    /**
     * ParenthesizedExpression
     *  : '(' Expression ')'
     *  ;
     */
    private ExpressionTree parenthesizedExpression() {
        int start = tokenType.isEmpty() ? lastPos : ts.offset();
        next();
        ExpressionTree parenthesizedExpression = expression();
        if ("punctuation.bracket.round.end.mexp".equals(tokenType)) {
            int end = ts.offset() + ts.token().length();
            next();
            return new ParenthesizedExpression(Kind.PARENTHESIZED, parenthesizedExpression, start, end);
        }
        return erroneous(start, parenthesizedExpression);
    }

    /**
     * Literal
     *  : NullLiteral
     *  | BoolLiteral
     *  | StringLiteral
     *  | IntLiteral
     *  | LongLiteral
     *  | FloatLiteral
     *  | DoubleLiteral
     *  | ErrorLiteral
     *  ;
     */
    private ExpressionTree literal() {
        switch (tokenType) {
            case "constant.language.null.mexp":
                return nullLiteral();
            case "constant.boolean.mexp":
                return boolLiteral();
            case "punctuation.definition.string.begin.mexp":
                return stringLiteral();
            case "constant.numeric.int.mexp":
                return intLiteral();
            case "constant.numeric.long.mexp":
                return longLiteral();
            case "constant.numeric.float.mexp":
                return floatLiteral();
            case "constant.numeric.double.mexp":
                return doubleLiteral();
            default:
                return erroneous();
        }
    }

    private ExpressionTree nullLiteral() {
        Literal literal = new Literal(Kind.NULL_LITERAL, null, ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree boolLiteral() {
        Literal literal = new Literal(Kind.BOOLEAN_LITERAL, Boolean.parseBoolean(ts.token().text().toString()), ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree stringLiteral() {
        int start = tokenType.isEmpty() ? lastPos : ts.offset();
        next();
        String value = "";
        if ("string.quoted.single.mexp".equals(tokenType)) {
            value = ts.token().text().toString();
            next();
        }
        if ("punctuation.definition.string.end.mexp".equals(tokenType)) {
            Literal literal = new Literal(Kind.STRING_LITERAL, value, start, ts.offset() + ts.token().length());
            next();
            return literal;
        }
        return erroneous(start);
    }

    private ExpressionTree intLiteral() {
        Literal literal = new Literal(Kind.INT_LITERAL, Integer.decode(ts.token().text().toString()), ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree longLiteral() {
        Literal literal = new Literal(Kind.LONG_LITERAL, Long.decode(ts.token().text().toString().replaceAll("([lL])", "")), ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree floatLiteral() {
        Literal literal = new Literal(Kind.FLOAT_LITERAL, Float.parseFloat(ts.token().text().toString()), ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree doubleLiteral() {
        Literal literal = new Literal(Kind.DOUBLE_LITERAL, Double.parseDouble(ts.token().text().toString()), ts.offset(), ts.offset() + ts.token().length());
        next();
        return literal;
    }

    private ExpressionTree erroneous(ExpressionTree... errors) {
        return erroneous(lastPos, errors);
    }

    private ExpressionTree erroneous(int pos, ExpressionTree... errors) {
        return new Erroneous(Arrays.asList(errors), pos, tokenType.isEmpty() ? ts.offset() + ts.token().length() : ts.offset());
    }

    private boolean next() {
        lastPos = ts.token() != null ? ts.offset() + ts.token().length() : 0;
        while (ts.moveNext()) {
            List<String> categories = (List<String>) ts.token().getProperty("categories");
            if (categories != null && categories.size() > 1) {
                String category = categories.get(categories.size() - 1);
                if (!category.startsWith("meta.")) {
                    tokenType = category;
                    return true;
                }
            }
        }
        tokenType = "";
        return false;
    }
}
