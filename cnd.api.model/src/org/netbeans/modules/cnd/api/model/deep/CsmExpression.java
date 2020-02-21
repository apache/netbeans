/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
