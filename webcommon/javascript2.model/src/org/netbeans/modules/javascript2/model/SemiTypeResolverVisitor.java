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
package org.netbeans.modules.javascript2.model;

import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.TernaryNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.Lexer;
import com.oracle.js.parser.Token;
import com.oracle.js.parser.TokenType;
import static com.oracle.js.parser.TokenType.ADD;
import static com.oracle.js.parser.TokenType.DECPOSTFIX;
import static com.oracle.js.parser.TokenType.DECPREFIX;
import static com.oracle.js.parser.TokenType.INCPOSTFIX;
import static com.oracle.js.parser.TokenType.INCPREFIX;
import static com.oracle.js.parser.TokenType.NEW;
import static com.oracle.js.parser.TokenType.NOT;
import static com.oracle.js.parser.TokenType.SUB;
import com.oracle.js.parser.ir.JoinPredecessorExpression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class SemiTypeResolverVisitor extends PathNodeVisitor {

    private static final Logger LOGGER = Logger.getLogger(SemiTypeResolverVisitor.class.getName());

    public static final String ST_START_DELIMITER = "@"; //NOI18N
    public static final String ST_THIS = "@this;"; //NOI18N
    public static final String ST_VAR = "@var;"; //NOI18N
    public static final String ST_EXP = "@exp;"; //NOI18N
    public static final String ST_PRO = "@pro;"; //NOI18N
    public static final String ST_CALL = "@call;"; //NOI18N
    public static final String ST_NEW = "@new;"; //NOI18N
    public static final String ST_ARR = "@arr;"; //NOI18N
    public static final String ST_ANONYM = "@anonym;"; //NOI18N
    public static final String ST_WITH = "@with;"; //NOI18N

    private static final TypeUsage BOOLEAN_TYPE = new TypeUsage(Type.BOOLEAN, -1, true);
    private static final TypeUsage STRING_TYPE = new TypeUsage(Type.STRING, -1, true);
    private static final TypeUsage NUMBER_TYPE = new TypeUsage(Type.NUMBER, -1, true);
    private static final TypeUsage ARRAY_TYPE = new TypeUsage(Type.ARRAY, -1, true);
    private static final TypeUsage REGEXP_TYPE = new TypeUsage(Type.REGEXP, -1, true);
    private static final TypeUsage UNDEFINED_TYPE = new TypeUsage(Type.UNDEFINED, -1, true);

    private Map<String, TypeUsage> result;

    private List<String> exp;

    private int typeOffset;

    private final FinderOffsetTypeVisitor offsetVisitor;
    private ModelBuilder builder;

    public SemiTypeResolverVisitor() {
        offsetVisitor = new FinderOffsetTypeVisitor();
    }

    public Set<TypeUsage> getSemiTypes(Node expression, ModelBuilder builder) {
        this.builder = builder;
        exp = new ArrayList<>();
        result = new HashMap<>();
        reset();
        expression.accept(this);
        add(exp, typeOffset == -1 ? offsetVisitor.findOffset(expression) : typeOffset, false);
        return new HashSet<>(result.values());
    }

    private void reset() {
        exp.clear();
        typeOffset = -1;
        //visitedIndexNode = false;  // we are not able to count arrays now
    }

    private void add(List<String> exp, int offset, boolean resolved) {
        if (/*visitedIndexNode ||*/ exp.isEmpty()
                || (exp.size() == 1 && exp.get(0).startsWith(ST_START_DELIMITER) && !exp.get(0).startsWith(ST_ANONYM)
                && !ST_THIS.equals(exp.get(0)))) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!exp.get(0).startsWith(ST_START_DELIMITER)) {
            if (exp.size() == 1) {
                sb.append(ST_VAR);
            } else {
                sb.append(ST_EXP);
            }
        }
        for (String part : exp) {
            sb.append(part);
        }
        String type = sb.toString();
        if (!result.containsKey(type)) {
            result.put(type, new TypeUsage(type, offset, resolved));
        }
    }

    private void add(TypeUsage type) {
        if (!result.containsKey(type.getType())) {
            result.put(type.getType(), type);
        }
    }

    @Override
    public Node leaveAccessNode(AccessNode accessNode) {
        if (!exp.isEmpty()) {
            String type = exp.get(exp.size() - 1);
            String preType = exp.size() > 1 ? exp.get(exp.size() - 2) : "";
            if (!ST_THIS.equals(type) && !(preType.startsWith(ST_START_DELIMITER)) && !type.startsWith(ST_ANONYM)) {
                exp.add(exp.size() - 1, ST_PRO);
            }
        }
        exp.add(ST_PRO);
        exp.add(accessNode.getProperty());
        return super.leaveAccessNode(accessNode);
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        addToPath(callNode);
        if (!(callNode.getFunction() instanceof FunctionNode)) {
            callNode.getFunction().accept(this);
        }
        if (exp.size() == 2 && ST_NEW.equals(exp.get(0))) {
            return false;
        }
        if (callNode.getFunction() instanceof AccessNode) {
            int size = exp.size();
            if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
                exp.remove(size - 2);
            }
        }
        else if (callNode.getFunction() instanceof FunctionNode) {
            FunctionNode function = (FunctionNode) callNode.getFunction();
            String name = builder.getFunctionName(function);
//            String name = function.getIdent().getName();
            add(new TypeUsage(ST_CALL + name, function.getStart(), false));
            return false;
        }
        if (exp.isEmpty()) {
            exp.add(ST_CALL);
        } else {
            exp.add(exp.size() - 1, ST_CALL);
        }
        return false;
    }

    @Override
    public Node leaveCallNode(CallNode callNode) {
        if (callNode.getFunction() instanceof AccessNode) {
            int size = exp.size();
            if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
                exp.remove(size - 2);
            }
        }
        exp.add(exp.size() - 1, ST_CALL);
        return super.leaveCallNode(callNode);
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        switch (Token.descType(unaryNode.getToken())) {
            case NEW:
                exp.add(ST_NEW);
                SimpleNameResolver snr = new SimpleNameResolver();
                exp.add(snr.getFQN(unaryNode.getExpression(), builder));
                typeOffset = snr.getTypeOffset();
                return false;
            case NOT:
                add(BOOLEAN_TYPE);
                return false;
            case ADD:
            case SUB:
            case DECPREFIX:
            case DECPOSTFIX:
            case INCPREFIX:
            case INCPOSTFIX:
                add(NUMBER_TYPE);
                return false;
            default:
                return super.enterUnaryNode(unaryNode);
        }
    }


    @Override
    public Node leaveUnaryNode(UnaryNode uNode) {
        if (Token.descType(uNode.getToken()) == TokenType.NEW) {
            int size = exp.size();
            if (size > 1 && ST_CALL.equals(exp.get(size - 2))) {
                exp.remove(size - 2);
            }
            typeOffset = uNode.getExpression().getStart();
            if (!exp.isEmpty()) {
                exp.add(exp.size() - 1, ST_NEW);
            } else {
                exp.add(ST_NEW);
            }
        }
        return super.leaveUnaryNode(uNode);
    }

    @Override
    public boolean enterIdentNode(IdentNode iNode) {
        String name = iNode.getPropertyName();
        if (ModelUtils.THIS.equals(name)) {  //NOI18N
            exp.add(ST_THIS);
        } else if (Type.UNDEFINED.equals(name)){
            add(UNDEFINED_TYPE);
        } else {
            if (getPath().isEmpty()) {
                exp.add(ST_VAR);
            }
            exp.add(name);
        }
        return false;
    }

    @Override
    public boolean enterLiteralNode(LiteralNode lNode) {
        Object value = lNode.getObject();
        TypeUsage type = null;
        if (value instanceof Boolean) {
            type = BOOLEAN_TYPE;
        } else if (value instanceof String) {
            type = STRING_TYPE;
        } else if (value instanceof Integer
                || value instanceof Float
                || value instanceof Double) {
            type = NUMBER_TYPE;
        } else if (lNode instanceof LiteralNode.ArrayLiteralNode) {
            type = ARRAY_TYPE;
        } else if (value instanceof Lexer.RegexToken) {
            type = REGEXP_TYPE;
        }

        if (type != null) {
            if (getPath().size() > 1 && getPreviousFromPath(2) instanceof CallNode) {
                exp.add(type.getType());
            } else {
                add(type);
            }
        }
        return false;
    }

    @Override
    public boolean enterTernaryNode(TernaryNode ternaryNode) {
        ternaryNode.getTrueExpression().accept(this);
        add(exp, offsetVisitor.findOffset(ternaryNode.getTrueExpression()), false);
        reset();
        Node third = ternaryNode.getFalseExpression();
        third.accept(this);
        int typeStart = offsetVisitor.findOffset(third);
        add(exp, typeStart, false);
        reset();
        return false;
    }

    @Override
    public boolean enterObjectNode(ObjectNode objectNode) {
        int size = getPath().size();
        if (size > 0 && getPath().get(size - 1) instanceof AccessNode) {
            exp.add(ST_ANONYM + objectNode.getStart());
        } else {
            add(new TypeUsage(ST_ANONYM + objectNode.getStart(), objectNode.getStart(), false));
        }
        return false;
    }

    @Override
    public boolean enterIndexNode(IndexNode indexNode) {
        addToPath(indexNode);
        indexNode.getBase().accept(this);
        int size = exp.size();
        if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
            exp.remove(size - 2);
        }
        if (exp.isEmpty()) {
            exp.add(ST_ARR);
        } else {
            boolean propertyAccess = false;
            if (indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode lNode = (LiteralNode)indexNode.getIndex();
                if (lNode.isString()) {
                    exp.add(ST_PRO);
                    exp.add(lNode.getPropertyName());
                    propertyAccess = true;
                }
            }
            if (!propertyAccess) {
                exp.add(exp.size() - 1, ST_ARR);
            }
        }
        //add(exp, indexNode.getStart(), false);
        //reset();
        return false;
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        if (!binaryNode.isAssignment()) {
            if (isResultString(binaryNode)) {
                add(STRING_TYPE);
                return false;
            }
            if (isResultNumber(binaryNode)) {
                add(NUMBER_TYPE);
                return false;
            }
            TokenType tokenType = binaryNode.tokenType();
            if (tokenType == TokenType.EQ || tokenType == TokenType.EQ_STRICT
                    || tokenType == TokenType.NE || tokenType == TokenType.NE_STRICT
                    || tokenType == TokenType.GE || tokenType == TokenType.GT
                    || tokenType == TokenType.LE || tokenType == TokenType.LT
                    || tokenType == TokenType.AND ) {
                if (getPath().isEmpty()) {
                    add(BOOLEAN_TYPE);
                }
                return false;
            }
            binaryNode.lhs().accept(this);
            add(exp, offsetVisitor.findOffset(binaryNode.lhs()), false);
            reset();
            binaryNode.rhs().accept(this);
            add(exp, offsetVisitor.findOffset(binaryNode.rhs()), false);
            reset();
            return false;
        }
        if (binaryNode.rhs() instanceof FunctionNode) {
            binaryNode.lhs().accept(this);
            return false;
        }
        if (binaryNode.isAssignment()) {
            binaryNode.rhs().accept(this);
            return false;
        }
        return super.enterBinaryNode(binaryNode);
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        List<? extends Node> path = getPath();
        boolean functionType = true;
        if (!path.isEmpty()) {
            Node lastNode = path.get(path.size() - 1);
            functionType = !(lastNode instanceof CallNode);
        }
        if (functionType) {
            add(new TypeUsage(Type.FUNCTION, functionNode.getStart(), true));
        }
        return false;
    }

    private boolean isResultString(BinaryNode binaryNode) {
        boolean bResult = false;
        TokenType tokenType = binaryNode.tokenType();
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (tokenType == TokenType.ADD
                && ((lhs instanceof LiteralNode && ((LiteralNode) lhs).isString())
                || (rhs instanceof LiteralNode && ((LiteralNode) rhs).isString()))) {
            bResult = true;
        } else {
            if (lhs instanceof JoinPredecessorExpression) {
                lhs = ((JoinPredecessorExpression)lhs).getExpression();
            }
            if (rhs instanceof JoinPredecessorExpression) {
                rhs = ((JoinPredecessorExpression)rhs).getExpression();
            }
            if (lhs instanceof BinaryNode) {
                bResult = isResultString((BinaryNode) lhs);
            } else if (rhs instanceof BinaryNode) {
                bResult = isResultString((BinaryNode) rhs);
            }
        }
        return bResult;
    }

    private boolean isResultNumber(BinaryNode binaryNode) {
        boolean bResult = false;
        TokenType tokenType = binaryNode.tokenType();
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if ((tokenType == TokenType.BIT_OR || tokenType == TokenType.BIT_AND)
                && ((lhs instanceof LiteralNode && ((LiteralNode) lhs).isNumeric())
                || (rhs instanceof LiteralNode && ((LiteralNode) rhs).isNumeric()))) {
            bResult = true;
        } else if (tokenType == TokenType.DIV || tokenType == TokenType.MUL
                || tokenType == TokenType.SUB ){
            bResult = true;
        } else {
            if (lhs instanceof BinaryNode) {
                bResult = isResultNumber((BinaryNode) lhs);
            } else if (rhs instanceof BinaryNode) {
                bResult = isResultNumber((BinaryNode) rhs);
            }
        }
        return bResult;
    }

    private static class SimpleNameResolver extends PathNodeVisitor {
        private List<String> exp = new ArrayList<>();
        private int typeOffset = -1;
        private ModelBuilder builder;

        public String getFQN(Node expression, ModelBuilder builder) {
            exp.clear();
            this.builder = builder;
            expression.accept(this);
            StringBuilder sb = new StringBuilder();
            for(String part : exp){
                sb.append(part);
                sb.append('.');
            }
            if (sb.length() == 0) {
                LOGGER.log(Level.FINE, "New operator withouth name: {0}", expression.toString()); //NOI18N
                return null;
            }
            return sb.toString().substring(0, sb.length() - 1);
        }

        public int getTypeOffset() {
            return typeOffset;
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            if (typeOffset == -1) {
                typeOffset = accessNode.getFinish() - accessNode.getProperty().length();
            }
            accessNode.getBase().accept(this);
            exp.add(accessNode.getProperty());
            return false;
        }


        @Override
        public boolean enterCallNode(CallNode callNode) {
            callNode.getFunction().accept(this);
            return false;
        }

        @Override
        public boolean enterFunctionNode(FunctionNode functionNode) {
            String name = builder.getFunctionName(functionNode);
            exp.add(name);
            if (typeOffset == -1) {
                typeOffset = functionNode.getIdent().getStart();
            }
            return false;
        }


        @Override
        public boolean enterIndexNode(IndexNode indexNode) {
            indexNode.getBase().accept(this);
            return false;
        }



        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            exp.add(identNode.getName());
            if (typeOffset == -1) {
                typeOffset = identNode.getStart();
            }
            return super.enterIdentNode(identNode);
        }

// TRUFFLE
//        @Override
//        public Node enter(ReferenceNode referenceNode) {
//            referenceNode.getReference().accept(this);
//            return null;
//        }
    }

    private static class FinderOffsetTypeVisitor extends NodeVisitor {
        private int typeOffset = -1;

        public FinderOffsetTypeVisitor() {
            super(new LexicalContext());
        }

        int findOffset (Node expression) {
            expression.accept(this);
            return typeOffset;
        }

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            typeOffset = identNode.getStart();
            return false;
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            typeOffset = accessNode.getStart();
            return false;
        }
    }
}
