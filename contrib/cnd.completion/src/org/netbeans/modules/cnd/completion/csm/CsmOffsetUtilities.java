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

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmInitializerListContainer;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmObjectAttributeQuery;

/**
 * utilities method for working with offsets of Csm objects
 * and CsmOffsetable objects
 */
public class CsmOffsetUtilities {

    /** Creates a new instance of CsmOffsetUtils */
    private CsmOffsetUtilities() {
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            if (CsmKindUtilities.isNamespaceDefinition(obj)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) obj;
                // return false if we're not inside the namespace scope
                if (offset <= CsmObjectAttributeQuery.getDefault().getLeftBracketOffset(nsd)) {
                    return false;
                }
            }
            if (offset == offs.getEndOffset()) {
                if (CsmKindUtilities.isType(obj)) {
                    CsmType type = (CsmType)obj;
                    // we do not accept type if offset is after '*', '&' or '[]'
                    return !type.isPointer() && !type.isReference() && (type.getArrayDepth() == 0);
                } else if (endsWithBrace(offs)) {
                    // if we right after closed "}" it means we are out of scope object
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInObject(CsmObject outerObj, CsmObject innerObj) {
        if (!CsmKindUtilities.isOffsetable(outerObj) || !CsmKindUtilities.isOffsetable(innerObj)) {
            return false;
        }
        CsmOffsetable outer = (CsmOffsetable)outerObj;
        CsmOffsetable inner = (CsmOffsetable)innerObj;
        return outer.getContainingFile().equals(inner.getContainingFile()) &&
                outer.getStartOffset() <= inner.getStartOffset() &&
                inner.getEndOffset() <= outer.getEndOffset();
    }

    private static boolean endsWithBrace(CsmOffsetable obj) {
        if (!CsmKindUtilities.isScope(obj)) {
            // only scopes can end with '}'
            return false;
        }
        if (!CsmKindUtilities.isStatement(obj) || CsmKindUtilities.isCompoundStatement(obj)) {
            // non-statement scope always ends with '}'
            
            if(CsmKindUtilities.isFunctionDefinition(obj)) {
                CsmFunctionDefinition fun = (CsmFunctionDefinition) obj;
                if(fun.getBody().getStartOffset() == obj.getStartOffset() &&
                        fun.getBody().getEndOffset() == obj.getEndOffset()) {
                    return false;
                }
            }
            
            return true;
        }
        // special care is needed for scope statements
        CsmStatement stmt = (CsmStatement) obj;
        switch (stmt.getKind()) {
            case IF:
                // 'if' statement ends with '}' if its last branch ends with '}'
                CsmStatement elseBranch = ((CsmIfStatement)stmt).getElse();
                if (elseBranch != null) {
                    return CsmKindUtilities.isCompoundStatement(elseBranch);
                } else {
                    return CsmKindUtilities.isCompoundStatement(((CsmIfStatement)stmt).getThen());
                }
            case FOR:
            case RANGE_FOR:
            case WHILE:
                // loop statement ends with '}' if its body ends with '}'
                return CsmKindUtilities.isCompoundStatement(((CsmLoopStatement)stmt).getBody());
            default:
                return false;
        }
    }

    public static boolean isBeforeObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset < offs.getStartOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isAfterObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset > offs.getEndOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    // list is ordered by offsettable elements
    public static <T extends CsmObject> T findObject(Collection<T> list, CsmContext context, int offset) {
        assert (list != null) : "expect not null list";
        return findObject(list.iterator(), context, offset);
    }

    // list is ordered by offsettable elements
    public static <T extends CsmObject> T findObject(Iterator<T> it, CsmContext context, int offset) {
        assert (it != null) : "expect not null list";
        while (it.hasNext()) {
            T obj = it.next();
            assert (obj != null) : "can't be null declaration";
            if (CsmOffsetUtilities.isInObject((CsmObject)obj, offset)) {
                // we are inside csm element
                CsmContextUtilities.updateContextObject(obj, offset, context);
                return obj;
            }
        }
        return null;
    }
    
    public static boolean isInFunctionScope(final CsmFunction fun, final int offset) {
        boolean inScope = false;
        if (fun != null) {
            inScope = true;
            CsmFunctionParameterList paramList = fun.getParameterList();
            // check if offset is before parameters
            if (paramList != null) {
                if (CsmOffsetUtilities.isInObject(paramList, offset)) {
                    return true;
                }
                Collection<CsmParameter> params = paramList.getParameters();
                if (!params.isEmpty()) {
                    CsmParameter firstParam = params.iterator().next();
                    if (CsmOffsetUtilities.isBeforeObject(firstParam, offset)) {
                        return false;
                    }
                    // in function, but check that not in return type
                    // check if offset in return value
                    CsmType retType = fun.getReturnType();            
                    boolean isRetTypeDecltype = CsmContextUtilities.checkDecltype(retType); // NOI18N                    
                    return isRetTypeDecltype || !CsmOffsetUtilities.isInObject(retType, offset);
                }
            }
            // check initializer list for constructors
            if (CsmKindUtilities.isConstructor(fun)) {
                Collection<CsmExpression> izers = ((CsmInitializerListContainer) fun).getInitializerList();
                if (!izers.isEmpty()) {
                    CsmExpression firstIzer = izers.iterator().next();
                    if (CsmOffsetUtilities.isBeforeObject(firstIzer, offset)) {
                        return false;
                    }
                    return true;
                }
            }
            // for function definitions check body
            if (CsmKindUtilities.isFunctionDefinition(fun)) {
                CsmFunctionDefinition funDef = (CsmFunctionDefinition) fun;
                if (CsmOffsetUtilities.isBeforeObject(funDef.getBody(), offset)) {
                    if (CsmKindUtilities.isCastOperator(fun)) {
                        if (CsmOffsetUtilities.isBeforeObject(funDef.getReturnType(), offset)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            if (CsmKindUtilities.isFunctionDeclaration(fun)) {
                return false;
            }
        }
        return inScope;
    }

    public static boolean isInClassScope(final CsmClass clazz, final int offset) {
        return isInObject(clazz, offset) 
                && clazz.getLeftBracketOffset() < offset
                && offset < clazz.getEndOffset();
    }

    /**
     * Checks if two objects have same offsets. It usually means that
     * they are result of macro expansion.
     *
     * @param o1  first object
     * @param o2  second object
     * @return <code>true</code> if both arguments are offsetable and
     *          have equal start and end offsets, <code>false</code> otherwise
     */
    public static boolean sameOffsets(final CsmObject obj1, final CsmObject obj2) {
        if (CsmKindUtilities.isOffsetable(obj1) && CsmKindUtilities.isOffsetable(obj2)) {
            final CsmOffsetable ofs1 = (CsmOffsetable)obj1;
            final CsmOffsetable ofs2 = (CsmOffsetable)obj2;
            return ofs1.getStartOffset() == ofs2.getStartOffset()
                    && ofs1.getEndOffset() == ofs2.getEndOffset();
        } else {
            return false;
        }
    }

}
