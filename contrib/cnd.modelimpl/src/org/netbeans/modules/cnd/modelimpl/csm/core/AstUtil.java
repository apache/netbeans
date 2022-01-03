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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintStream;
import org.netbeans.modules.cnd.antlr.ASTVisitor;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableFakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.TokenBasedAST;
import org.netbeans.modules.cnd.modelimpl.parser.TokenBasedFakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.openide.util.CharSequences;

/**
 * Miscellaneous AST-related static utility functions
 */
public class AstUtil {

    private AstUtil() {
    }

    public static boolean isEmpty(AST ast, boolean hasFakeChild) {
	if( isEmpty(ast) ) {
	    return true;
	}
	else {
	    return hasFakeChild ? isEmpty(ast.getFirstChild()) : false;
	}
    }

    private static boolean isEmpty(AST ast) {
	return (ast == null || ast.getType() == CPPTokenTypes.EOF);
    }

    public static boolean isElaboratedKeyword(AST ast) {
        if (ast != null) {
            return ast.getType() == CPPTokenTypes.LITERAL_struct ||
                   ast.getType() == CPPTokenTypes.LITERAL_class ||
                   ast.getType() == CPPTokenTypes.LITERAL_union ||
                   ast.getType() == CPPTokenTypes.LITERAL_enum;
        }
        return false;
    }
    
    public static CharSequence getRawNameInChildren(AST ast) {
        return getRawName(findIdToken(ast));
    }

    public static CharSequence[] toRawName(CharSequence rawName) {
        if (rawName == null) {
            return null;
        }
        String[] split = rawName.toString().split("\\."); //NOI18N
        CharSequence[] res = new CharSequence[split.length];
        for(int i = 0; i < split.length; i++) {
            res[i] = CharSequences.create(split[i]);
        }
        return res;
    }
    
    public static CharSequence getRawName(AST token) {
        StringBuilder l = new StringBuilder();
        for( ; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.IDENT:
                    if (l.length()>0) {
                        l.append('.');
                    }
                    l.append(AstUtil.getText(token));
                    break;
                case CPPTokenTypes.SCOPE:
                    break;
                default:
                    //TODO: process templates
                    break;
            }
        }
        return NameCache.getManager().getString(CharSequences.create(l));
    }

    private static AST findIdToken(AST ast) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.IDENT ) {
                return token;
            }
            else if( token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                return token.getFirstChild();
            }
        }
        return null;
    }

    public static CharSequence findId(AST ast) {
        return findId(ast, -1);
    }

    /**
     * Finds ID (either CPPTokenTypes.CSM_QUALIFIED_ID or CPPTokenTypes.ID)
     * in direct children of the given AST tree
     *
     * @param ast tree to search ID in
     *
     * @param limitingTokenType type of token that, if being found, stops search
     *        -1 means that there is no such token.
     *        This parameter allows, for example, searching until "}" is encountered
     * @return found id
     */
    public static CharSequence findId(AST ast, int limitingTokenType) {
	return findId(ast, limitingTokenType, false);
    }

    /**
     * Finds ID (either CPPTokenTypes.CSM_QUALIFIED_ID or CPPTokenTypes.ID)
     * in direct children of the given AST tree
     *
     * @param ast tree to search ID in
     *
     * @param limitingTokenType type of token that, if being found, stops search
     *        -1 means that there is no such token.
     *        This parameter allows, for example, searching until "}" is encountered
     * @param qualified flag indicating if full qualified id is needed
     * @return id
     */
    public static CharSequence findId(AST ast, int limitingTokenType, boolean qualified) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            if( type == limitingTokenType && limitingTokenType >= 0 ) {
                return null;
            }
            else if( type == CPPTokenTypes.IDENT ) {
                return AstUtil.getText(token);
            }
            else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		if( qualified ) {
		    return AstUtil.getText(token);
		}
                AST last = getLastChild(token);
                if( last != null) {
                    if( last.getType() == CPPTokenTypes.IDENT ) {
                        return AstUtil.getText(last);
                    }
                    else {
                        AST first = token.getFirstChild();
                        if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
                            StringBuilder sb = new StringBuilder(AstUtil.getText(first));
                            sb.append(' ');
                            AST next = first.getNextSibling();
                            if( next != null ) {
                                sb.append(AstUtil.getText(next));
                            }
                            return sb;
                        } else if (first.getType() == CPPTokenTypes.IDENT){
                            return AstUtil.getText(first);
                        }
                    }
                }
            }
        }
        return "";
    }

    public static CharSequence getText(AST ast) {
        if (ast instanceof FakeAST) {
            return ((FakeAST)ast).getTextID();
        } else if (ast instanceof CsmAST) {
            return ((CsmAST)ast).getTextID();
        }
        return ast.getText();
    }

    public static AST findMethodName(AST ast){
        AST type = ast.getFirstChild(); // type
        AST qn = null;
        int i = 0;
        while(type != null){
            switch(type.getType()){
                case CPPTokenTypes.LESSTHAN:
                    i++;
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.GREATERTHAN:
                    i--;
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_TYPE_BUILTIN:
                case CPPTokenTypes.CSM_TYPE_ATOMIC:
                case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    type = type.getNextSibling();
                    if (i == 0){
                        qn = type;
                    }
                    continue;
                case CPPTokenTypes.IDENT:
                    if (i == 0 && qn == null) {
                        qn = type;
                    }
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                    if (i == 0) {
                        qn = type;
                    }
                    type = type.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
                case CPPTokenTypes.CSM_TRY_CATCH_STATEMENT_LAZY:
                case CPPTokenTypes.COLON:
                case CPPTokenTypes.POINTERTO:
                    break;
                default:
                    type = type.getNextSibling();
                    continue;
            }
            break;
        }
        return qn;
    }
    
    public static AST findTypeNode(AST ast) {
        AST typeAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_TYPE_BUILTIN);
        if (typeAst == null) {
            typeAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_TYPE_COMPOUND);
            if (typeAst == null) {
                typeAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_TYPE_DECLTYPE);
                if (typeAst == null) {
                    typeAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_TYPE_ATOMIC);
                }
            }
        }        
        return typeAst;
    }
    
    public static boolean isTypeNode(AST ast) {
        return ast != null && 
            (ast.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
             ast.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
             ast.getType() == CPPTokenTypes.CSM_TYPE_DECLTYPE ||
             ast.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC);
    }

    public static boolean hasChildOfType(AST ast, int type) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasChild(AST ast, AST child) {
        if (ast != null && child != null) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                if (token == child) {
                    return true;
                }
            }
        }
        return false;
    }

    public static AST findChildOfType(AST ast, int type) {
        return findChildOfType(ast, type, null);
    }
    
    public static AST findChildOfType(AST ast, int type, AST stopToken) {
        for( AST token = ast.getFirstChild(); token != null && token != stopToken; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return token;
            }
        }
        return null;
    }    

    public static AST findSiblingOfType(AST ast, int type) {
        return findSiblingOfType(ast, type, null);
    }
    
    public static AST findSiblingOfType(AST ast, int type, AST stopToken) {
        for( AST token = ast; token != null && token != stopToken; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                return token;
            }
        }
        return null;
    }    

    public static AST findLastSiblingOfType(AST ast, int type) {
        AST result = null;
        for( AST token = ast; token != null; token = token.getNextSibling() ) {
            if( token.getType() == type ) {
                result = token;
            }
        }
        return result;
    }
    
    public static AST skipTokens(AST ast, int...tokens) {
        AST next = ast;
        while (next != null && arrayContains(next.getType(), tokens)) {
            next = next.getNextSibling();
        }
        return next;
    }

    public static AST getLastChild(AST token) {
        if( token == null ) {
            return null;
        }
        AST child = token.getFirstChild();
        if( child != null ) {
            while( child.getNextSibling() != null ) {
                child = child.getNextSibling();
            }
            return child;
        }
        return null;
    }

    public static AST getLastChildRecursively(AST token) {
        if( token == null ) {
            return null;
        }
        if( token.getFirstChild() == null ) {
            return token;
        }
        else {
            AST child = getLastChild(token);
            return getLastChildRecursively(child);
        }
    }
    
    public static AST getLastNonEOFChildRecursively(AST token) {
        if( token == null ) {
            return null;
        }
        AST child = token.getFirstChild();
        if(child == null) {
            return token;
        } else {
            AST lastChild = getLastNonEOFChildRecursively(child);
            while( child.getNextSibling() != null) {
                child = child.getNextSibling();
                AST lastChild2 = getLastNonEOFChildRecursively(child);
                if(lastChild2.getType() != Token.EOF_TYPE && lastChild2 instanceof CsmAST) {
                    lastChild = lastChild2;
                }
            }
            return lastChild;
        }
    }    

    public static OffsetableAST getFirstOffsetableAST(AST node) {
        if( node != null ) {
            if( node instanceof OffsetableAST ) {
                return (OffsetableAST) node;
            }
            else {
                return getFirstOffsetableAST(node.getFirstChild());
            }
        }
        return null;
    }
    
    public static TokenBasedAST getFirstTokenBasedAST(AST node) {
        if (node != null) {
            if (node instanceof TokenBasedAST) {
                return (TokenBasedAST) node;
            } else {
                return getFirstTokenBasedAST(node.getFirstChild());
            }
        }
        return null;
    }

    public static String toString(AST ast) {
        final StringBuilder out = new StringBuilder();
        ASTVisitor impl = new ASTVisitor() {

            @Override
            public void visit(AST node) {
                print(node, out);
                for (AST node2 = node; node2 != null; node2 = node2.getNextSibling()) {
                    if (node2.getFirstChild() != null) {
                        out.append('>');
                        visit(node2.getFirstChild());
                        out.append('<');
                    }
                }
            }
        };
        impl.visit(ast);
        return out.toString();
    }

    public static void toStream(AST ast, final PrintStream ps) {
        ASTVisitor impl = new ASTVisitor() {
            
            private int depth = 0;
            
            @Override
            public void visit(AST node) {
                final boolean hasChildren = node.getFirstChild() != null;
                indent(ps, depth);
                if (hasChildren) {
                    ps.print('>');
                }
                print(node, ps);
                ps.println();
                if (hasChildren) {
                    ++depth;
                    for (AST node2 = node.getFirstChild(); node2 != null; node2 = node2.getNextSibling()) {
                        visit(node2);
                    }
                    --depth;
                    indent(ps, depth);
                    ps.print('<');
                    ps.println();
                }
            }
        };
        for (AST node = ast; node != null; node = node.getNextSibling()) {
            impl.visit(node);
        }
    }

    /**
     * Creates an AST with node <code>n1</code> as root and node <code>n2</code>
     * as its single child, discarding all other children and siblings of
     * both nodes. This function creates copies of nodes, original nodes
     * are not changed.
     *
     * @param n1  root node
     * @param n2  child node
     * @return AST consisting of two given nodes
     */
    public static AST createAST(AST n1, AST n2) {
        AST root = new CsmAST();
        root.initialize(n1);
        AST child = new CsmAST();
        child.initialize(n2);
        root.addChild(child);
        return root;
    }

    private static void print(AST ast, PrintStream ps) {
        ps.print('[');
        ps.print(ast.getText());
        ps.print('(');
        ps.print(ast.getType());
        ps.print(')');
        ps.print(ast.getLine());
        ps.print(':');
        ps.print(ast.getColumn());
        ps.print(']');
        //ps.print('\n');
    }
    
    private static void print(AST ast, StringBuilder out) {
        out.append('[');
        out.append(ast.getText());
        out.append('(');
        out.append(ast.getType());
        out.append(')');
        out.append(ast.getLine());
        out.append(':');
        out.append(ast.getColumn());
        out.append(']');
        //out.append('\n');
    }
    
    private static void printTextOnly(AST ast, PrintStream ps) {
        ps.print('[');
        ps.print(ast.getText());
        ps.print(']');
    }
    
    private static void indent(PrintStream ps, int depth) {
        for (int i = 0; i < depth; ++i) {
            ps.print("  "); // NOI18N
        }
    }

    public static String getOffsetString(AST ast) {
        if (ast == null) {
            return "<null>"; // NOI18N
        }
        OffsetableAST startAst = getFirstOffsetableAST(ast);
        AST endAst = getLastChildRecursively(ast);
        if (startAst != null && endAst != null) {
            StringBuilder sb = new StringBuilder();// NOI18N
            sb.append("[").append(startAst.getLine());// NOI18N
            sb.append(":").append(startAst.getColumn());// NOI18N
            sb.append("-").append(endAst.getLine());// NOI18N
            sb.append(":").append(endAst.getColumn());// NOI18N
            sb.append("]"); //NOI18N
            return sb.toString();
        }
        return "<no csm nodes>"; // NOI18N
    }
    
    public static boolean visitAST(ASTTokenVisitor visitor, AST ast) {
        if (ast != null) {
            switch (visitor.visit(ast)) {
                case ABORT:
                    return false;
                case SKIP_SUBTREE:
                    return true;
                case CONTINUE:
                    for (AST insideToken = ast.getFirstChild(); insideToken != null; insideToken = insideToken.getNextSibling()) {
                        if (!visitAST(visitor, insideToken)) {
                            return false;
                        }
                    }                        
            }
        }
        return true;
    }       
    
    /**
     * 
     * @param ast
     * @return true if ast has macro expanded tokens
     */
    public static boolean hasExpandedTokens(AST ast) {
        ASTExpandedTokensChecker checker = new ASTExpandedTokensChecker();
        visitAST(checker, ast);
        return checker.HasExpanded();
    }    
    
    /**
     * Clones AST until stop node is reached
     * @param source
     * @param stopNode - the last AST node to be cloned
     * @return "cloned" AST
     */
    public static AST cloneAST(AST source, AST stopNode) {
        return cloneAST(source, stopNode, true);
    }
    
    /**
     * Clones AST until stop node is reached
     * @param source
     * @param stopNode
     * @param includeLast - true if stopNode should be included
     * @return "cloned" AST
     */    
    public static AST cloneAST(AST source, AST stopNode, boolean includeLast) {
        if (source == null) {
            return null;
        }
        
        AST firstClonedNode = createFakeClone(source);
        AST currentClonedAST = firstClonedNode;
        AST prevClonedAST = null;
        
        while (source != null) {
            currentClonedAST.initialize(source);
            currentClonedAST.setFirstChild(source.getFirstChild());
            if (prevClonedAST != null) {
                prevClonedAST.setNextSibling(currentClonedAST);
            }
            if (source == stopNode) {
                break;
            }
            source = source.getNextSibling();
            prevClonedAST = currentClonedAST;
            currentClonedAST = createFakeClone(source);
        }
        
        if (!includeLast) {
            if (prevClonedAST == null) {
                return null;
            } else {
                prevClonedAST.setNextSibling(null);
            }
        }
        
        return firstClonedNode;
    }
    
    public static AST createFakeClone(AST ast) {
        if (ast instanceof TokenBasedAST) {
            return new TokenBasedFakeAST();
        } else if (ast instanceof OffsetableAST) {
            return new OffsetableFakeAST();
        }
        return new FakeAST();
    }       
    
    public static interface ASTTokenVisitor {
        
        public static enum Action {
            CONTINUE,
            SKIP_SUBTREE,
            ABORT
        }
        
        /**
         * Called on enter
         * @param token 
         * @return what action to perform
         */
        Action visit(AST token);
        
    }                 
    
    public static class ASTExpandedTokensChecker implements ASTTokenVisitor {
    
        private boolean expanded;

        @Override
        public Action visit(AST token) {
            if (token instanceof TokenBasedAST) {
                TokenBasedAST tokenBasedAST = (TokenBasedAST) token;
                if (APTUtils.isMacroExpandedToken(tokenBasedAST.getToken())) {
                    expanded = true;
                    return Action.ABORT;
                }
            }
            return Action.CONTINUE;
        }        

        public boolean HasExpanded() {
            return expanded;
        }
    }    
    
    public static class ASTTokensStringizer implements ASTTokenVisitor {
        
        private final boolean insertSpaces;
        
        protected int numStringizedTokens = 0;
    
        protected final StringBuilder sb = new StringBuilder();

        public ASTTokensStringizer() {
            this(false);
        }

        public ASTTokensStringizer(boolean insertSpaces) {
            this.insertSpaces = insertSpaces;
        }

        @Override
        public Action visit(AST token) {
            if (token.getFirstChild() == null) {
                if (insertSpaces && sb.length() > 0) {
                    sb.append(" "); // NOI18N
                }
                sb.append(token.getText());
                numStringizedTokens++;
            }
            return Action.CONTINUE;
        }
        
        public String getText() {
            return sb.toString();
        }

        public int getNumberOfStringizedTokens() {
            return numStringizedTokens;
        }     
    }
    
    private static boolean arrayContains(int value, int...array) {
        for (int elem : array) {
            if (value == elem) {
                return true;
            }
        }
        return false;
    }
}

 