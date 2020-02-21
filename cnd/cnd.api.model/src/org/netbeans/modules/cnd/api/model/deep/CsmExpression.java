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

package org.netbeans.modules.cnd.api.model.deep;

import java.util.List;

import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;

/**
 * Represents expression
 */
public interface CsmExpression extends CsmOffsetable, CsmScopeElement {

    //TODO: check in accordance to C++ standard
    public enum Kind {
        
        //
        // Primary expressions
        //
        
        // TODO: perhaps it isn't worth making separate kinds for different literals
        INTEGER_LITERAL,
        CHAR_LITERAL,
        FLOAT_LITERAL,
        STRING_LITERAL,
        BOOLEAN_LITERAL,
        
        THIS,
        PRIMARY_BRACKETED,
        
        REFERENCE,
        
        
        //
        // Postfix expressions
        //
        SUBSCRIPT,
        FUNCTIONCALL,
        // TODO: what is " postfix-expression ( expression-list ) " ?
        // TODO: add 
        SIMPLETYPE_INT,
        SIMPLETYPE_SHORT,
        SIMPLETYPE_DOUBLE,
        SIMPLETYPE_FLOAT,
        SIMPLETYPE_CHAR,
        SIMPLETYPE_WCHART,
        SIMPLETYPE_SIGNED,
        SIMPLETYPE_UNSIGNED,
        SIMPLETYPE_BOOL,
        SIMPLETYPE_LONG,
        
        TYPENAME_IDENTIFIER,
        TYPENAME_TEMPLATEID,
        
        DOT_IDEXPRESSION,
        ARROW_IDEXPRESSION,
        DOT_TEMPL_IDEXPRESS,
        ARROW_TEMPL_IDEXP,

        DOT_DESTRUCTOR,
        ARROW_DESTRUCTOR,

        POST_INCREMENT,
        POST_DECREMENT,
        
        DYNAMIC_CAST,
        REINTERPRET_CAST,
        STATIC_CAST,
        CONST_CAST,
        TYPEID_EXPRESSION,
        TYPEID_TYPEID,
        
        //
        // Unary expressions
        //
        PRE_INCREMENT,
        PRE_DECREMENT,
        STAR_CASTEXPRESSION,
        AMPSND_CASTEXPRESSION,
        PLUS_CASTEXPRESSION,
        MINUS_CASTEXPRESSION,
        NOT_CASTEXPRESSION,
        TILDE_CASTEXPRESSION,
        SIZEOF_UNARYEXPRESSION,
        SIZEOF_TYPEID,
        
        NEW_NEWTYPEID,
        NEW_TYPEID,
        DELETE_CASTEXPRESSION,
        DELETE_VECTORCASTEXPRESSION,
        
        CASTEXPRESSION,
        PM_DOTSTAR,
        PM_ARROWSTAR,
        MULTIPLICATIVE_MULTIPLY,
        MULTIPLICATIVE_DIVIDE,
        MULTIPLICATIVE_MODULUS,
        ADDITIVE_PLUS,
        ADDITIVE_MINUS,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        
        //
        // Relational
        //
        LESSTHAN,
        GREATERTHAN,
        LESSTHANEQUALTO,
        GREATERTHANEQUALTO,
        
        //
        // Equality
        //
        EQUALS,
        NOTEQUALS,
        
        BITAND,
        EXCLUSIVEOR,
        INCLUSIVEOR,
        LOGICAL_AND,
        LOGICAL_OR,
        CONDITIONAL,
        THROW,
        ASSIGNMENT_NORMAL,
        ASSIGNMENT_PLUS,
        ASSIGNMENT_MINUS,
        ASSIGNMENT_MULT,
        ASSIGNMENT_DIV,
        ASSIGNMENT_MOD,
        ASSIGNMENT_LSHIFT,
        ASSIGNMENT_RSHIFT,
        ASSIGNMENT_AND,
        ASSIGNMENT_OR,
        ASSIGNMENT_XOR,
        LIST
        
    }
    
    /**
     * Gets this expression kind
     */
    Kind getKind();
    
    
    /**
     * Gets parent expression or null if this is no parent expression
     */
    CsmExpression getParent();
    
    
    /**
     * Gets this expression operands
     */
    List<CsmExpression> getOperands();


    List<CsmStatement> getLambdas();
        
    /** 
     * Gets this object's expanded text. 
     * (if there are macroses inside expression or the whole expression is from macros) 
     */
    CharSequence getExpandedText();
}
