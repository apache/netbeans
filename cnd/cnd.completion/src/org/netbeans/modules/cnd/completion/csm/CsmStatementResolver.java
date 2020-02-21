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

package org.netbeans.modules.cnd.completion.csm;

import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MutableObject;

/**
 * utility class
 * used to find innermost statement inside CsmDeclaration and it's
 * context chain
 */
public class CsmStatementResolver {

    /** Creates a new instance of CsmStatementResolver */
    private CsmStatementResolver() {
    }

    /*
     * finds inner object for given offset and update context
     */
    public static boolean findInnerObject(CsmStatement stmt, int offset, CsmContext context) {
        if( stmt == null ) {
            if (CsmUtilities.DEBUG) {
                System.out.println("STATEMENT is null"); //NOI18N
            }
            return false;
        }
        if (!CsmOffsetUtilities.isInObject(stmt, offset)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("Offset " + offset+ " is not in statement " + stmt); //NOI18N
            }
            return false;
        }
        // update context of passed statements
        CsmContextUtilities.updateContext(stmt, offset, context);

        CsmStatement.Kind kind = stmt.getKind();
        boolean found = true;
        switch (kind) {
            case COMPOUND:
                found = findInnerCompound((CsmCompoundStatement) stmt, offset, context);
                break;
            case IF:
                found = findInnerIf((CsmIfStatement) stmt, offset, context);
                break;
            case TRY_CATCH:
                found = findInnerTry((CsmTryCatchStatement) stmt, offset, context);
                break;
            case CATCH:
                found = findInnerCatch((CsmExceptionHandler) stmt, offset, context);
                break;
            case DECLARATION:
                found = findInnerDeclaration((CsmDeclarationStatement) stmt, offset, context);
                break;
            case WHILE:
            case DO_WHILE:
                found = findInnerWhile((CsmLoopStatement) stmt, offset, context);
                break;
            case FOR:
                found = findInnerFor((CsmForStatement) stmt, offset, context);
                break;
            case RANGE_FOR:
                found = findInnerRange((CsmRangeForStatement) stmt, offset, context);
                break;
            case SWITCH:
                found = findInnerSwitch((CsmSwitchStatement) stmt, offset, context);
                break;
            case EXPRESSION:
                found = findInnerExpression(((CsmExpressionStatement) stmt).getExpression(), offset, context);
                break;
            case RETURN:
                found = findInnerExpression(((CsmReturnStatement) stmt).getReturnExpression(), offset, context);
                break;
            case BREAK:
            case CASE:
            case CONTINUE:
            case DEFAULT:
            case GOTO:
            case LABEL:
                break;
            default:
                if (CsmUtilities.DEBUG) {
                    System.out.println("unexpected statement kind"); //NOI18N
                }
                break;
        }
        return found;
    }

    private static boolean findInnerCompound(CsmCompoundStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (stmt != null) {
            for (CsmStatement curSt : stmt.getStatements()) {
                if (findInnerObject(curSt, offset, context)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean findInnerTry(CsmTryCatchStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";
        if (findInnerObject(stmt.getTryStatement(), offset, context)) {
            return true;
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            if (findInnerObject(handler, offset, context)) {
                return true;
            }
        }
        return false;
    }

    private static boolean findInnerCatch(CsmExceptionHandler stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";
        return findInnerCompound((CsmCompoundStatement) stmt, offset, context);
    }

    private static boolean findInnerIf(CsmIfStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called";

        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in CONDITION of if statement "); //NOI18N
            }
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            findInnerExpression(stmt.getCondition().getExpression(), offset, context);
            return true;
        }
        if (findInnerObject(stmt.getThen(), offset, context)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in THEN: "); //NOI18N
            }
            return true;
        }
        if (findInnerObject(stmt.getElse(), offset, context)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in ELSE: "); //NOI18N
            }
            return true;
        }
        return false;
    }

    private static boolean findInnerDeclaration(CsmDeclarationStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in declaration statement when called"; //NOI18N
        List<CsmDeclaration> decls = stmt.getDeclarators();
        CsmDeclaration decl = CsmOffsetUtilities.findObject(decls, context, offset);
        if (decl != null && (decls.size() == 1 || !CsmOffsetUtilities.sameOffsets(stmt, decl))) {
            if (CsmUtilities.DEBUG) {
                System.out.println("we have declarator " + decl); //NOI18N
            }
            if (CsmKindUtilities.isTypedef(decl) || CsmKindUtilities.isTypeAlias(decl)) {
                CsmClassifier classifier = ((CsmTypedef)decl).getType().getClassifier();
                if (CsmOffsetUtilities.isInObject(decl, classifier) && !CsmOffsetUtilities.sameOffsets(decl, classifier)) {
                    decl = classifier;
                }
            }
            if (CsmKindUtilities.isEnum(decl)) {
                findInnerEnum((CsmEnum)decl, offset, context);
            } else if (CsmKindUtilities.isClass(decl)) {
                findInnerClass((CsmClass)decl, offset, context);
            } else  if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;

                // check if offset in parameters
                CsmFunctionParameterList paramList = fun.getParameterList();
                if (paramList != null) {
                    CsmParameter param = CsmOffsetUtilities.findObject(paramList.getParameters(), context, offset);
                    if (CsmOffsetUtilities.isInObject(paramList, offset) || (param != null && !CsmOffsetUtilities.sameOffsets(fun, param))) {
                        context.add(fun);
                        if (param != null) {
                            CsmType type = param.getType();
                            if (!CsmOffsetUtilities.sameOffsets(param, type)
                                    && CsmOffsetUtilities.isInObject(type, offset)) {
                                context.setLastObject(type);
                            } else {
                                context.setLastObject(param);
                            }
                        }
                    }
                }
                    
                if (CsmKindUtilities.isFunctionDefinition(fun) || CsmKindUtilities.isLambda(fun)) {
                    CsmFunctionDefinition funDef = (CsmFunctionDefinition)fun;
                    CsmCompoundStatement body = funDef.getBody();
                    if ((!CsmOffsetUtilities.sameOffsets(funDef, body) || body.getStartOffset() != body.getEndOffset()) && CsmOffsetUtilities.isInObject(body, offset)) {
                        CsmContextUtilities.updateContext(fun, offset, context);
                        // offset is in body, try to find inners statement
                        if (CsmStatementResolver.findInnerObject(body, offset, context)) {
                            CsmContextUtilities.updateContext(body, offset, context);
                            // if found exact object => return it, otherwise return last found scope
                            CsmObject found = context.getLastObject();
                            if (!CsmOffsetUtilities.sameOffsets(body, found)) {
                                context.setLastObject(found);
                                return true;
                            }
                        }
                    }
                }   
            } else if (CsmKindUtilities.isVariable(decl)) {
                findInnerExpression(((CsmVariable)decl).getInitialValue(), offset, context);
            }
            return true;
        }
        return false;
    }

    private static boolean findInnerEnum(CsmEnum enumm, int offset, CsmContext context) {
        CsmContextUtilities.updateContext(enumm, offset, context);
        CsmEnumerator enumerator = CsmOffsetUtilities.findObject(enumm.getEnumerators(), context, offset);
        if (enumerator != null && !CsmOffsetUtilities.sameOffsets(enumm, enumerator)) {
            CsmContextUtilities.updateContext(enumerator, offset, context);
        }
        return true;
    }

    private static boolean findInnerClass(CsmClass clazz, int offset, CsmContext context) {
        CsmContextUtilities.updateContext(clazz, offset, context);
        CsmMember member = CsmOffsetUtilities.findObject(clazz.getMembers(), context, offset);
        if (!CsmOffsetUtilities.sameOffsets(clazz, member)) {
            if (CsmKindUtilities.isClass(member)) {
                findInnerClass((CsmClass)member, offset, context);
            } else if (CsmKindUtilities.isFunctionDefinition(member)) {
                CsmContextUtilities.updateContext(member, offset, context);
                CsmCompoundStatement body = ((CsmFunctionDefinition)member).getBody();
                if (!CsmOffsetUtilities.sameOffsets(member, body)) {
                    findInnerObject(body, offset, context);
                }
            }
        }
        return true;
    }

    private static boolean findInnerWhile(CsmLoopStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in condition of loop statement isPostCheck()=" + stmt.isPostCheck()); //NOI18N
            }
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            findInnerExpression(stmt.getCondition().getExpression(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    private static boolean findInnerFor(CsmForStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (findInnerObject(stmt.getInitStatement(), offset, context)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in INIT of for statement"); //NOI18N
            }
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getIterationExpression(), offset)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in ITERATION  of for statement"); //NOI18N
            }
            CsmExpression iterationExpression = stmt.getIterationExpression();
            CsmContextUtilities.updateContextObject(iterationExpression, offset, context);
            
            if(findInnerExpression(iterationExpression, offset, context)) {
                return true;
            }
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            if (CsmUtilities.DEBUG) {
                System.out.println("in CONDITION of for statement "); //NOI18N
            }
            CsmCondition condition = stmt.getCondition();
            CsmContextUtilities.updateContextObject(condition, offset, context);
            if(findInnerExpression(condition.getExpression(), offset, context)) {
                return true;
            }
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    private static boolean findInnerRange(CsmRangeForStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (findInnerObject(stmt.getDeclaration(), offset, context)) {
            return true;
        }
        if (CsmOffsetUtilities.isInObject(stmt.getInitializer(), offset)) {
            CsmExpression initializerExpression = stmt.getInitializer();
            CsmContextUtilities.updateContextObject(initializerExpression, offset, context);
            
            if(findInnerExpression(initializerExpression, offset, context)) {
                return true;
            }
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }
    
    private static boolean findInnerSwitch(CsmSwitchStatement stmt, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(stmt, offset)) : "we must be in statement when called"; //NOI18N
        if (!CsmOffsetUtilities.sameOffsets(stmt, stmt.getCondition())
                && CsmOffsetUtilities.isInObject(stmt.getCondition(), offset)) {
            CsmContextUtilities.updateContextObject(stmt.getCondition(), offset, context);
            return true;
        }
        return findInnerObject(stmt.getBody(), offset, context);
    }

    /*package*/ static boolean findInnerExpression(CsmExpression expr, int offset, CsmContext context) {
        if(expr != null) {
            for (CsmStatement csmStatement : expr.getLambdas()) {
                CsmDeclarationStatement lambda = (CsmDeclarationStatement)csmStatement;
                if ((!CsmOffsetUtilities.sameOffsets(expr, lambda) || lambda.getStartOffset() != lambda.getEndOffset()) && CsmOffsetUtilities.isInObject(lambda, offset)) {
                    // offset is in body, try to find inners statement
                    if (CsmStatementResolver.findInnerObject(lambda, offset, context)) {
                        // if found exact object => return it, otherwise return last found scope
                        CsmObject found = context.getLastObject();
                        if (!CsmOffsetUtilities.sameOffsets(lambda, found)) {
                            CsmContextUtilities.updateContextObject(found, offset, context);
                            return true;
                        }
                    }
                }
            }            
        }
        return false;        
    }
    
    public static <T extends CsmStatement> T findStatement(CsmStatement root, Class<T> stmtClass) {
        final MutableObject<T> result = new MutableObject<>();
        walkImpl(root, (stmt) -> {
            if (stmtClass.isAssignableFrom(stmt.getClass())) {
                result.value = (T) stmt;
                return StatementsWalker.Action.STOP;
            }
            return StatementsWalker.Action.CONTINUE;
        });
        return result.value;
    }

    private static StatementsWalker.Action walkImpl(CsmStatement stmt, StatementsWalker walker) {
        if (stmt == null) {
            return StatementsWalker.Action.CONTINUE;
        }
        CsmStatement.Kind kind = stmt.getKind();
        StatementsWalker.Action act = walker.visit(stmt);
        if (needStop(act)) {
            return act;
        }
        act = StatementsWalker.Action.CONTINUE;
        switch (kind) {
            case COMPOUND:
                act = walkCompound((CsmCompoundStatement) stmt, walker);
                break;
            case IF:
                act = walkIf((CsmIfStatement) stmt, walker);
                break;
            case TRY_CATCH:
                act = walkTry((CsmTryCatchStatement) stmt, walker);
                break;
            case CATCH:
                act = walkCatch((CsmExceptionHandler) stmt, walker);
                break;
            case DECLARATION:
                act = walkDeclaration((CsmDeclarationStatement) stmt, walker);
                break;
            case WHILE:
            case DO_WHILE:
                act = walkWhile((CsmLoopStatement) stmt, walker);
                break;
            case FOR:
                act = walkFor((CsmForStatement) stmt, walker);
                break;
            case RANGE_FOR:
                act = walkRange((CsmRangeForStatement) stmt, walker);
                break;
            case SWITCH:
                act = walkSwitch((CsmSwitchStatement) stmt, walker);
                break;
            case EXPRESSION:
            case RETURN:
            case BREAK:
            case CASE:
            case CONTINUE:
            case DEFAULT:
            case GOTO:
            case LABEL:
                break;
            default:
                if (CsmUtilities.DEBUG) {
                    System.out.println("unexpected statement kind"); //NOI18N
                }
                break;
        }
        // Stop on 'full stop'
        if (act == StatementsWalker.Action.STOP) {
            return act;
        }
        // Continue on 'stop branch' or 'continue'
        return StatementsWalker.Action.CONTINUE;
    }
    
    private static StatementsWalker.Action walkCompound(CsmCompoundStatement stmt, StatementsWalker walker) {
        if (stmt != null) {
            for (CsmStatement curSt : stmt.getStatements()) {
                StatementsWalker.Action act = walkImpl(curSt, walker);
                if (needStop(act)) {
                    return act;
                }
            }
        }
        return StatementsWalker.Action.CONTINUE;
    }

    private static StatementsWalker.Action walkTry(CsmTryCatchStatement stmt, StatementsWalker walker) {
        StatementsWalker.Action act = walkImpl(stmt.getTryStatement(), walker);
        if (needStop(act)) {
            return act;
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            act = walkImpl(handler, walker);
            if (needStop(act)) {
                return act;
            }
        }
        return StatementsWalker.Action.CONTINUE;
    }

    private static StatementsWalker.Action walkCatch(CsmExceptionHandler stmt, StatementsWalker walker) {
        return walkCompound((CsmCompoundStatement) stmt, walker);
    }

    private static StatementsWalker.Action walkIf(CsmIfStatement stmt, StatementsWalker walker) {
        StatementsWalker.Action act = walkImpl(stmt.getThen(), walker);
        if (needStop(act)) {
            return act;
        }
        return walkImpl(stmt.getElse(), walker);
    }

    private static StatementsWalker.Action walkDeclaration(CsmDeclarationStatement stmt, StatementsWalker walker) {
        return StatementsWalker.Action.CONTINUE;
    }

    private static StatementsWalker.Action walkWhile(CsmLoopStatement stmt, StatementsWalker walker) {
        return walkImpl(stmt.getBody(), walker);
    }

    private static StatementsWalker.Action walkFor(CsmForStatement stmt, StatementsWalker walker) {
        StatementsWalker.Action act = walkImpl(stmt.getInitStatement(), walker);
        if (needStop(act)) {
            return act;
        }
        return walkImpl(stmt.getBody(), walker);
    }

    private static StatementsWalker.Action walkRange(CsmRangeForStatement stmt, StatementsWalker walker) {
        StatementsWalker.Action act = walkImpl(stmt.getDeclaration(), walker);
        if (needStop(act)) {
            return act;
        }
        return walkImpl(stmt.getBody(), walker);
    }
    
    private static StatementsWalker.Action walkSwitch(CsmSwitchStatement stmt, StatementsWalker walker) {
        return walkImpl(stmt.getBody(), walker);
    }
    
    private static boolean needStop(StatementsWalker.Action act) {
        return act == StatementsWalker.Action.STOP_BRANCH
                || act == StatementsWalker.Action.STOP;
    }
    
    @FunctionalInterface
    private static interface StatementsWalker {
        
        Action visit(CsmStatement stmt);
        
        public static enum Action {
            CONTINUE,
            STOP_BRANCH,
            STOP
        }
    }
}
