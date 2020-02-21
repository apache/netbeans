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

/**
 * Original C++ grammar.
 * C++ grammar. C++11 standard extensions.
 */

parser grammar CXXParser;

options {
    tokenVocab = APTTokenTypes;
    backtrack = false;
}

scope QualName {
    int qual;
    int type;
}

scope Declaration {
    declarator_type_t declarator;
    declaration_specifiers_t decl_specifiers;
    int type_specifiers_count;
}

@header {
package org.netbeans.modules.cnd.modelimpl.parser.generated;

import java.util.HashMap;
import org.netbeans.modules.cnd.modelimpl.parser.*;
}

@members {

    private /*final*/ CXXParserAction action;

    protected CXXParser(TokenStream input, CXXParserAction action) {
        super(input);
        assert action != null;
        this.action = action;
    }

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }

    class pCXX_grammar {
    }

    decl_kind blockscope_decl = null;
    decl_kind tparm_decl = null;
    decl_kind parm_decl = null;
    decl_kind field_decl = null;
    decl_kind object_decl = null;

    Object NULL = null;
    class decl_kind{
    }
    class type_specifier_t{
    }
    class name_specifier_t{
    }
    class declarator_type_t{
        public void init() {
        }
        public boolean is_function() {
            return true;
        }
        public void set_ident() {
        }
        public void set_ref() {
        }
        public void set_ptr(Object o1, Object o2) {
        }
        public void apply_array(Object o1) {
        }
        public void apply_parameters(Object o1) {
        }
        public void apply_ptr(Object o1) {
        }
        public void set_constructor(Object o1) {
        }

    }
    class declaration_specifiers_t{
        public void init(Object o) {
        }
        public void add_type(Object o1, Object o2) {
        }
        public void apply_specifier(Object o1, Object o2) {
        }
    }
    class qualifier_t{
    }
    class parameters_and_qualifiers_t{
    }
    class expression_t{
    }

    void println(Object o) {
    }
    void println(Object o1,Object o2) {
    }
    
    protected void sync_declaration_impl() {
    }

    protected void sync_member_impl() {
    }

    protected void sync_parameter_impl() {
    }

    protected void sync_statement_impl() {
    }

    pCXX_grammar CTX;

    static int IDT_CLASS_NAME=1;
    static int IDT_ENUM_NAME=2;
    static int IDT_TYPEDEF_NAME=4;
    static int IDT_TEMPLATE_NAME=8;
    static int IDT_OBJECT_NAME = 16;

    void init_declaration(pCXX_grammar ctx, decl_kind kind)
    {
//        $Declaration::declarator.init();
//        $Declaration::decl_specifiers.init(kind);
//        $Declaration::type_specifiers_count = 0;
    }

    boolean type_specifier_already_present(pCXX_grammar ctx)
    {
        boolean result = false;
//        if ($Declaration->size($Declaration) > 0) {
//            result = $Declaration::type_specifiers_count != 0;
//        }
//        trace("type_specifier_already_present()=",result);
        return result;
    }
    boolean identifier_is(int x) {
//        trace("identifier_is()=",true);
        return true;
    }
    boolean top_level_of_template_arguments() {
//        trace("top_level_of_template_arguments()=",true);
        return true;
    }
    boolean operator_is_template() {
//        trace("operator_is_template()=",true);
        return true;
    }

    void qual_setup() {
    }
    void qual_add_colon2() {
    }

    void store_type_specifier(type_specifier_t ts, pCXX_grammar ctx) {
//        $Declaration::type_specifiers_count++;
//        trace("store_type_specifier->", $Declaration::type_specifiers_count);
    }

    public boolean isTemplateTooDeep(int currentLevel, int maxLevel) {
        return isTemplateTooDeep(currentLevel, maxLevel, 0);
    }

    public static int TEMPLATE_PREVIEW_POS_LIMIT = 4096;
    public boolean isTemplateTooDeep(int currentLevel, int maxLevel, int startPos) {
        int level = currentLevel;
        int pos = startPos;            
        while(pos < TEMPLATE_PREVIEW_POS_LIMIT) {
            int token = input.LA(pos);
            pos++;
            if(token == EOF || token == 0) {
                break;
            }
            if(token == LCURLY || token == RCURLY) {
                break;
            }
            if(token == LESSTHAN) {
                level++;
            } else if(token == GREATERTHAN) {
                level--;
            } 
            if(level == 0) {
                return false;
            }
            if(level >= maxLevel) {
                return true;
            }
        }
        return false;
    }

}

compilation_unit: translation_unit;


/*START*/

// [gram.basic] 
translation_unit
@init                                                                           {if(state.backtracking == 0){action.translation_unit(input.LT(1));}}
    :
        sync_declaration 
        (
            declaration[object_decl]
            sync_declaration 
        )* EOF
    ;
finally                                                                         {if(state.backtracking == 0){action.end_translation_unit(input.LT(0));}}

// [gram.stmt]
/*
 * As per 2003 standard:
 * "An expression-statement with a function-style 
 * explicit type conversion as its leftmost 
 * subexpression can be indistinguishable from a declaration 
 * where the first declarator starts with a '('. 
 * In those cases the statement is a declaration."
 *
 * Resolve declaration vs expression conflict in favor of declaration.
 * (actually declaration synpred is a HUGE hammer, 
 * we should try find something else)
 */
statement
@init                                                                           {if(state.backtracking == 0){action.statement(input.LT(1));}}
    :
        // In standard there is no attribute_specifiers rule before declaration_statement
        // It's added here to avoid additional predicates
        attribute_specifiers?
        (
            labeled_statement
        |
            expression_or_declaration_statement
        |
            compound_statement[false]
        |
            selection_statement
        |
            iteration_statement
        |
            jump_statement
        |
            try_block
        )
    ;
finally                                                                         {if(state.backtracking == 0){action.end_statement(input.LT(0));}}

labeled_statement
@init                                                                           {if(state.backtracking == 0){action.labeled_statement(input.LT(1));}}
    :                                                                           
    (
        IDENT COLON                                                             {action.labeled_statement(action.LABELED_STATEMENT__LABEL, $IDENT, input.LT(0));}
        statement
    |
        LITERAL_case                                                            {action.labeled_statement(action.LABELED_STATEMENT__CASE, $LITERAL_case);}
        constant_expression 
        COLON                                                                   {action.labeled_statement(action.LABELED_STATEMENT__CASE_COLON, input.LT(0));}
        statement
    |
        LITERAL_default COLON                                                   {action.labeled_statement(action.LABELED_STATEMENT__DEFAULT, $LITERAL_default, input.LT(0));}
        statement
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_labeled_statement(input.LT(0));}}

expression_statement
@init                                                                           {if(state.backtracking == 0){action.expression_statement(input.LT(1));}}
    :                                                                           
        expression? SEMICOLON                                                   
    ;
finally                                                                         {if(state.backtracking == 0){action.end_expression_statement(input.LT(0));}}

expression_or_declaration_statement
    :
        (expression SEMICOLON) => 
                                                                                {action.expression_statement(input.LT(1));}
        expression SEMICOLON
                                                                                {action.end_expression_statement(input.LT(0));}
    |
        declaration_statement
    ;


compound_statement[boolean lazy]
@init                                                                           {if(state.backtracking == 0){action.compound_statement(input.LT(1));}}
    :
        {lazy}? skip_balanced_Curl
    |
        LCURLY 
        sync_statement 
        (
            statement
            sync_statement 
        )* 
        RCURLY
    ;
finally                                                                         {if(state.backtracking == 0){action.end_compound_statement(input.LT(0));}}

selection_statement
@init                                                                           {if(state.backtracking == 0){action.selection_statement(input.LT(1));}}
    :                                                                           
    (
        LITERAL_if                                                              {action.selection_statement(action.SELECTION_STATEMENT__IF, input.LT(0));}
        LPAREN                                                                  {action.selection_statement(action.SELECTION_STATEMENT__IF_LPAREN, input.LT(0));}
        condition 
        RPAREN                                                                  {action.selection_statement(action.SELECTION_STATEMENT__IF_RPAREN, input.LT(0));}
        statement 
        ( (LITERAL_else)=> 
            LITERAL_else                                                        {action.selection_statement(action.SELECTION_STATEMENT__ELSE, input.LT(0));}
            statement 
        )?
    |
        LITERAL_switch                                                          {action.selection_statement(action.SELECTION_STATEMENT__SWITCH, input.LT(0));}
        LPAREN                                                                  {action.selection_statement(action.SELECTION_STATEMENT__SWITCH_LPAREN, input.LT(0));}
        condition 
        RPAREN                                                                  {action.selection_statement(action.SELECTION_STATEMENT__SWITCH_RPAREN, input.LT(0));}
        statement
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_selection_statement(input.LT(0));}}

/*
 * The same expression-declaration ambiguity as in statement rule.
 */
condition
scope Declaration;
@init                                                                           {if(state.backtracking == 0){action.condition(input.LT(1));}}
    :                                                                           
    (
        (attribute_specifiers? type_specifier+ declarator EQUAL)=>
            condition_declaration
    |
        condition_expression
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_condition(input.LT(0));}}

condition_declaration
@init                                                                           {if(state.backtracking == 0){action.condition_declaration(input.LT(1));}}
    :
        attribute_specifiers?
        type_specifier+ declarator 
        EQUAL                                                                   {action.condition(action.CONDITION__EQUAL, input.LT(0));}
        assignment_expression
    ;
finally                                                                         {if(state.backtracking == 0){action.end_condition_declaration(input.LT(0));}}

condition_expression
@init                                                                           {if(state.backtracking == 0){action.condition_expression(input.LT(1));}}
    :
        expression
    ;
finally                                                                         {if(state.backtracking == 0){action.end_condition_expression(input.LT(0));}}

iteration_statement
@init                                                                           {if(state.backtracking == 0){action.iteration_statement(input.LT(1));}}
    :                                                                           
    (
        LITERAL_while                                                           {action.iteration_statement(action.ITERATION_STATEMENT__WHILE, input.LT(0));}
        LPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__WHILE_LPAREN, input.LT(0));}
        condition
        RPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__WHILE_RPAREN, input.LT(0));}
        statement
    |
        LITERAL_do                                                              {action.iteration_statement(action.ITERATION_STATEMENT__DO, input.LT(0));}
        statement 
        LITERAL_while                                                           {action.iteration_statement(action.ITERATION_STATEMENT__DO_WHILE, input.LT(0));}
        LPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__DO_WHILE_LPAREN, input.LT(0));}
        expression 
        RPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__DO_WHILE_RPAREN, input.LT(0));}
        SEMICOLON
    |
        LITERAL_for                                                             {action.iteration_statement(action.ITERATION_STATEMENT__FOR, input.LT(0));}
        LPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__FOR_LPAREN, input.LT(0));}
        (
            (for_range_declaration COLON) =>
            for_range_declaration 
            COLON                                                               {action.iteration_statement(action.ITERATION_STATEMENT__FOR_COLON, input.LT(0));}
            for_range_initializer
        |
            for_init_statement 
            condition? 
            SEMICOLON                                                           {action.iteration_statement(action.ITERATION_STATEMENT__FOR_SEMICOLON, input.LT(0));}
            expression? 
        )
        RPAREN                                                                  {action.iteration_statement(action.ITERATION_STATEMENT__FOR_RPAREN, input.LT(0));}
        statement
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_iteration_statement(input.LT(0));}}

/*
 * The same expression-declaration ambiguity as in statement rule.
 */
for_init_statement
@init                                                                           {if(state.backtracking == 0){action.for_init_statement(input.LT(1));}}
    :                                                                           
    (
        (simple_declaration[blockscope_decl])=>
            simple_declaration[blockscope_decl]
    |
        expression_statement
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_for_init_statement(input.LT(0));}}

for_range_declaration
@init                                                                           {if(state.backtracking == 0){action.for_range_declaration(input.LT(1));}}
    :
    attribute_specifiers? type_specifier+ declarator                            
    ;
finally                                                                         {if(state.backtracking == 0){action.end_for_range_declaration(input.LT(0));}}

for_range_initializer
@init                                                                           {if(state.backtracking == 0){action.for_range_initializer(input.LT(1));}}
    :                                                                           
    (
        expression 
    |   
        braced_init_list
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_for_range_initializer(input.LT(0));}}

jump_statement
@init                                                                           {if(state.backtracking == 0){action.jump_statement(input.LT(1));}}
    :                                                                           
    (
        LITERAL_break                                                           {action.jump_statement(action.JUMP_STATEMENT__BREAK, input.LT(0));}
        SEMICOLON
    |
        LITERAL_continue                                                        {action.jump_statement(action.JUMP_STATEMENT__CONTINUE, input.LT(0));}
        SEMICOLON
    |
        LITERAL_return                                                          {action.jump_statement(action.JUMP_STATEMENT__RETURN, input.LT(0));}
        (   
            expression?
        |   
            braced_init_list
        )               
        SEMICOLON
    |
        LITERAL_goto IDENT                                                      {action.jump_statement(action.JUMP_STATEMENT__GOTO, $LITERAL_goto, input.LT(0));}
        SEMICOLON
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_jump_statement(input.LT(0));}}

/*
 * simple_declaration has been split out of block_declaration so to have
 * an easier view of simple_declaration vs function_definition major conflict.
 */
declaration_statement
@init                                                                           {if(state.backtracking == 0){action.declaration_statement(input.LT(1));}}
    :
        simple_declaration[blockscope_decl]
    |
        block_declaration
    ;
finally                                                                         {if(state.backtracking == 0){action.end_declaration_statement(input.LT(0));}}

//[gram.dcl] 
/*
 * function_definition merged into one rule with simple_declaration (which in turn was taken out of block_declaration)
 */
declaration [decl_kind kind] 
@init                                                                           {if(state.backtracking == 0){action.declaration(input.LT(1));}}
    :
    (
        block_declaration
    |
        simple_declaration_or_function_definition[kind]
    |
        template_declaration[kind]
    |
        explicit_instantiation[kind]
    |
        explicit_specialization[kind]
    |
        linkage_specification[kind]
    |
        namespace_definition 
    |
        attribute_declaration
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_declaration(input.LT(0));}}

sync_declaration
@init                                                                           {sync_declaration_impl();}
    :   // Deliberately match nothing, causing this rule always to be entered.
    ;

sync_member
@init                                                                           {sync_member_impl();}
    :   // Deliberately match nothing, causing this rule always to be entered.
    ;

sync_parameter
@init                                                                           {sync_parameter_impl();}
    :   // Deliberately match nothing, causing this rule always to be entered.
    ;

sync_statement
@init                                                                           {sync_statement_impl();}
    :   // Deliberately match nothing, causing this rule always to be entered.
    ;

block_declaration
@init                                                                           {if(state.backtracking == 0){action.block_declaration(input.LT(1));}}
    :                                                                           
    (
        asm_definition 
    |
        namespace_alias_definition 
    |
        using_declaration
    |
        using_directive 
    |
        static_assert_declaration
    |
        alias_declaration
//    |
//        opaque_enum_declaration
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_block_declaration(input.LT(0));}}

// IDs
id_expression
@init                                                                           {if(state.backtracking == 0){action.id_expression(input.LT(1));}}
    :                                                                           
    unqualified_or_qualified_id
    ;
finally                                                                         {if(state.backtracking == 0){action.end_id_expression(input.LT(0));}}

tilde_class_name
@init                                                                           {if(state.backtracking == 0){action.tilde_class_name(input.LT(1));}}
    :
    TILDE class_name
    ;
finally                                                                         {if(state.backtracking == 0){action.end_tilde_class_name(input.LT(0));}}

unqualified_or_qualified_id
@init {Token t = input.LT(1);}
    :
        (LITERAL_OPERATOR operator_id)=>
            operator_function_id
    |
        conversion_function_id
    |
        literal_operator_id
    |
        tilde_class_name
    |
        simple_template_id_or_IDENT
        (
            SCOPE 
            (
                LITERAL_template? 
                (
                    (LITERAL_OPERATOR operator_id)=>
                        operator_function_id
                |
                    conversion_function_id
                |
                    literal_operator_id
                |
                    tilde_class_name
                |
                    simple_template_id_or_IDENT_nested[t]
                )
            )
        )*
    |
        SCOPE (
            (simple_template_id_or_IDENT SCOPE) =>
            nested_name_specifier LITERAL_template? unqualified_id
        |
            operator_function_id
        |
            (LITERAL_OPERATOR STRING_LITERAL IDENT) =>
            literal_operator_id
        |
            simple_template_id_or_IDENT
        )
    ;




nested_simple_template_id_or_IDENT
@init {Token startToken = input.LT(1);}
    :
        simple_template_id_or_IDENT
        (
            SCOPE 
            (
                (LITERAL_template lookup_simple_template_id_nocheck SCOPE )=> 
                    LITERAL_template simple_template_id_nocheck
            |   
                simple_template_id_or_IDENT_nested[startToken]
            )
        )*
        
    ;

unqualified_id:
        (LITERAL_OPERATOR operator_id)=>
            operator_function_id
    |
        conversion_function_id
    |
        literal_operator_id
    |
        tilde_class_name
    |
        TILDE decltype_specifier
    |
        simple_template_id_or_IDENT
    ;

qualified_id:
        nested_name_specifier LITERAL_template? unqualified_id
    |
        SCOPE (
            // TODO: review temp predicate
            (simple_template_id_or_IDENT SCOPE) =>
            nested_name_specifier LITERAL_template? unqualified_id
        |
            // TODO: review temp predicate
            (LITERAL_OPERATOR STRING_LITERAL IDENT) =>
            literal_operator_id
        |
            operator_function_id
        |
            simple_template_id_or_IDENT
        )
    ;

/* original rule:
 *

nested_name_specifier:
        type-name SCOPE
    |
        namespace-name SCOPE
    |
        nested-name-specifier IDENT SCOPE
    |
        nested-name-specifier LITERAL_template? simple-template-id SCOPE
    ;

 * left-recursion removed here and LITERAL_template/IDENT ambiguity resolved
 */

nested_name_specifier returns [ name_specifier_t namequal ]
    :
        simple_template_id_or_IDENT
        SCOPE 
        (
            (LITERAL_template lookup_simple_template_id_nocheck SCOPE )=> 
                LITERAL_template simple_template_id_nocheck SCOPE
        |
            (IDENT SCOPE) =>
                IDENT                                                           {action.nested_name_specifier(input.LT(0));}
                SCOPE     
        |
            (lookup_simple_template_id SCOPE)=>
                simple_template_id SCOPE
        )*
    ;

lookup_nested_name_specifier:
        simple_template_id_or_IDENT SCOPE
//        (
//            IDENT SCOPE
//        |
//            LITERAL_template lookup_simple_template_id SCOPE
//        |
//            lookup_simple_template_id SCOPE
//        )*
    ;

//[gram.dcl]

alias_declaration:
    LITERAL_using IDENT ASSIGNEQUAL                                             {action.alias_declaration($LITERAL_using, $IDENT, $ASSIGNEQUAL);}
    type_id 
    SEMICOLON                                                                   {action.end_alias_declaration(input.LT(0));}
    ;

/*
 * original rule:

simle_declaration
        decl_specifier* (init_declarator (COMMA init_declarator)*)* SEMICOLON
    ;

 * construtor_declarator introduced into init_declarator part to resolve ambiguity
 * between single decl_specifier and the constructor name in a declarator (declarator_id) of constructor.
 *
 */
simple_declaration [decl_kind kind]
scope Declaration;
@init                                                                           {if(state.backtracking == 0){action.simple_declaration(input.LT(1));}}
    :
                                                                                {action.decl_specifiers(input.LT(1));}
        decl_specifier*                                                         {action.end_decl_specifiers(null/*input.LT(0)*/);}
        (
            SEMICOLON
        |
            (
                (constructor_declarator)=> constructor_declarator
            |
                init_declarator
            )
            // this is a continuation of init_declarator_list after constructor_declarator/init_declarator
            (
                COMMA                                                           {action.simple_declaration(action.SIMPLE_DECLARATION__COMMA2, input.LT(0));}
                init_declarator
            )* 
            SEMICOLON                                                           {action.simple_declaration(action.SIMPLE_DECLARATION__SEMICOLON, input.LT(0));}
        )
    ;
finally                                                                         {if(state.backtracking == 0){action.end_simple_declaration(input.LT(0));}}










/*
 * This is the above simple_declaration rule merged together with function definition
 * The idea is to avoid doing any lookaheads unless absolutely necessary (constructor declarator).
 * It requires certain duplication as common constructs in each branch of choice are a bit different
 * (see different init_declarator_list continuation sequences).
 */
simple_declaration_or_function_definition [decl_kind kind]
scope Declaration;
@init                                                                           {if(state.backtracking == 0){action.simple_declaration(input.LT(1));}}
    :
        gnu_attribute_or_extension_specifiers?
                                                                                {action.decl_specifiers(input.LT(1));}
        (decl_specifier gnu_attribute_specifiers?)*                             {action.end_decl_specifiers(null/*input.LT(0)*/);}
                                                                                
        (
            SEMICOLON                                                           {action.simple_declaration(action.SIMPLE_DECLARATION__SEMICOLON, input.LT(0));}
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this is a continuation of init_declarator_list after constructor_declarator
                    ( 
                        COMMA                                                   {action.simple_declaration(action.SIMPLE_DECLARATION__COMMA2, input.LT(0));}
                        init_declarator 
                    )* 
                    SEMICOLON                                                   {action.simple_declaration(action.SIMPLE_DECLARATION__SEMICOLON, input.LT(0));}
                |
                    function_definition_after_declarator[false, false, false]
                )
        |
            // greedy_declarator starts init_declarator
            greedy_declarator asm_statement?
            (
                { /*$greedy_declarator.type.is_function()*/ input.LA(1) != ASSIGNEQUAL }?
                    function_definition_after_declarator[false, false, false]
            |
                // this is a continuation of init_declarator_list 
                // after greedy_declarator
                initializer? 
                ( 
                    COMMA                                                       {action.simple_declaration(action.SIMPLE_DECLARATION__COMMA2, input.LT(0));}
                    init_declarator 
                )* 
                SEMICOLON                                                       {action.simple_declaration(action.SIMPLE_DECLARATION__SEMICOLON, input.LT(0));}
            )
        )    
    ;
finally                                                                         {if(state.backtracking == 0){action.end_simple_declaration(input.LT(0));}}

static_assert_declaration:
    LITERAL_static_assert LPAREN constant_expression COMMA STRING_LITERAL RPAREN SEMICOLON
    ;

attribute_declaration:
        cpp11_attribute_specifiers SEMICOLON
    ;

decl_specifier
    :
        storage_class_specifier                                                 {action.decl_specifier(action.DECL_SPECIFIER__STORAGE_CLASS_SPECIFIER, input.LT(0));}
    |
        function_specifier                                                      {action.decl_specifier(action.DECL_SPECIFIER__FUNCTION_SPECIFIER, input.LT(0));}
    |
        LITERAL_friend                                                          {action.decl_specifier(action.DECL_SPECIFIER__LITERAL_FRIEND, $LITERAL_friend);}
    |
        LITERAL_typedef                                                         {action.decl_specifier(action.DECL_SPECIFIER__LITERAL_TYPEDEF, $LITERAL_typedef);}
    |
        type_specifier                                                          {action.decl_specifier(action.DECL_SPECIFIER__TYPE_SPECIFIER, input.LT(0));}
    |
        LITERAL_constexpr                                                       {action.decl_specifier(action.DECL_SPECIFIER__LITERAL_CONSTEXPR, $LITERAL_constexpr);}
    ;

storage_class_specifier:
//        LITERAL_auto 
//    |
        LITERAL_register                                                        {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER__REGISTER, $LITERAL_register);}
    |
        LITERAL_static                                                          {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER__STATIC, $LITERAL_static);}
    |
        LITERAL_extern                                                          {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER__EXTERN, $LITERAL_extern);}
    |
        LITERAL_mutable                                                         {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER__MUTABLE, $LITERAL_mutable);}
    |
        LITERAL___thread                                                        {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER____THREAD, $LITERAL___thread);}
    |
        LITERAL_thread_local                                                    {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER__THREAD_LOCAL, $LITERAL_thread_local);}
    |
        LITERAL___hidden                                                        {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER___HIDDEN, $LITERAL___hidden);}
    |
        LITERAL___global                                                        {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER___GLOBAL, $LITERAL___global);}
    |
        LITERAL___symbolic                                                      {action.decl_specifier(action.STORAGE_CLASS_SPECIFIER___SYMBOLIC, $LITERAL___symbolic);}
    ;

function_specifier:
        LITERAL_inline 
    |
        LITERAL_virtual 
    |
        LITERAL_explicit 
    |
        LITERAL___inline   // compiler-specific
    ;

/*
 * original rule

type_specifier:
        simple_type_specifier 
    |
        class_specifier 
    |
        enum_specifier 
    |
        elaborated_type_specifier
    |
        cv_qualifier 
    ;

 * Ambiguity in LITERAL_class because of class_specifier vs elaborated_type_specifier conflict
 * Ambiguity in LITERAL_enum because of enum_specifier vs elaborated_type_specifier conflict
 *
 * Note, that (LITERAL_class SCOPE) sequence is not valid for class_specifier
 * Similarly (LITERAL_enum SCOPE) sequence is not valid for enum_specifier
 */

type_specifier returns [type_specifier_t ts]
@init                                                                           {if(state.backtracking == 0){action.type_specifier(input.LT(1));}}
    :
        // LITERAL_class SCOPE does not cover all the elaborated_type_specifier cases even with LITERAL_class
        (LITERAL_class SCOPE)=>
            trailing_type_specifier
    |
        // thus we need to make serious lookahead here to catch LCURLY
        (class_head LCURLY)=>
            class_specifier
    |
        // enum_specifier start sequence is simple
        (LITERAL_enum attribute_specifiers? IDENT? LCURLY)=>
            enum_specifier
    |
        trailing_type_specifier
    ;
finally                                                                         {if(state.backtracking == 0){action.end_type_specifier(input.LT(0));}}

trailing_type_specifier
@init                                                                           {if(state.backtracking == 0){action.trailing_type_specifier(input.LT(1));}}
    :   
        simple_type_specifier
    |
        elaborated_type_specifier
    |
        typename_specifier
    |
        cv_qualifier
    ;
finally                                                                         {if(state.backtracking == 0){action.end_trailing_type_specifier(input.LT(0));}}

simple_type_specifier returns [type_specifier_t ts_val]
scope QualName;
@init                                                                           {if(state.backtracking == 0){action.simple_type_specifier(input.LT(1));}}
    :
        LITERAL_char                                                            {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__CHAR, input.LT(0));}
    |
        LITERAL_wchar_t                                                         {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__WCHAR_T, input.LT(0));}
    |
        LITERAL_char16_t                                                        {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__CHAR16_T, input.LT(0));}
    |
        LITERAL_char32_t                                                        {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__CHAR32_T, input.LT(0));}
    |
        LITERAL_bool                                                            {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__BOOL, input.LT(0));}
    |
        LITERAL_short                                                           {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__SHORT, input.LT(0));}
    |
        LITERAL_int                                                             {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__INT, input.LT(0));}
    |
        LITERAL_long                                                            {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__LONG, input.LT(0));}
    |
        LITERAL_signed                                                          {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__SIGNED, input.LT(0));}
    |
        LITERAL_unsigned                                                        {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__UNSIGNED, input.LT(0));}
    |
        LITERAL_float                                                           {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__FLOAT, input.LT(0));}
    |
        LITERAL_double                                                          {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__DOUBLE, input.LT(0));}
    |
        LITERAL_void                                                            {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__VOID, input.LT(0));}
    |
        LITERAL_auto                                                            {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__AUTO, input.LT(0));}
    |
        LITERAL___builtin_va_list                                               {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__BI_VA_LIST, input.LT(0));}
    |
        decltype_specifier
    |
        /*
         * "at most one type-specifier is allowed in the complete decl-specifier-seq of a declaration..."
         * In particular (qualified)type_name is allowed only once.
         */
        { action.type_specifier_already_present(input) }? =>
                                                                                {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__ID, input.LT(0));}
//        { !type_specifier_already_present(CTX) }? =>
            (
                SCOPE {{ qual_add_colon2(); }}                                  {action.simple_type_specifier(action.SIMPLE_TYPE_SPECIFIER__SCOPE, input.LT(0));}
            )?
            /* note that original rule does not allow empty nested_name_specifier for the LITERAL_template alternative */
            (
                (lookup_nested_name_specifier)=>
                    nested_name_specifier 
                    (simple_template_id_or_IDENT                                //{action.simple_type_specifier(input.LT(0));}
                    | LITERAL_template simple_template_id)
            |
                simple_template_id_or_IDENT                                     //{action.simple_type_specifier(input.LT(0));}
            )
    ;
finally                                                                         {if(state.backtracking == 0){action.end_simple_type_specifier(input.LT(0));}}

lookup_type_name:
        IDENT { action.identifier_is(IDT_CLASS_NAME|IDT_ENUM_NAME|IDT_TYPEDEF_NAME, $IDENT) }?
    ;

/*
 * original rule:
 *
elaborated_type_specifier:
        class_key SCOPE? nested_name_specifier? IDENT 
    |
        class_key SCOPE? nested_name_specifier? LITERAL_template? simple_template_id 
    |
        LITERAL_enum SCOPE? nested_name_specifier? IDENT 
    |
        LITERAL_typename SCOPE? nested_name_specifier IDENT 
    |
        LITERAL_typename SCOPE? nested_name_specifier LITERAL_template? simple_template_id 
    ;
* Ambiguity introduced by IDENT SCOPE IDENT sequence in a context of
* elaborated_type_specifier going right before declarators in simple declaration.
* Resolved by factoring out nested_name_specifier construct in 'class' situation.
* Resolved by specifically predicating IDENT SCOPE in 'enum' situation.
*/

decltype_specifier
@init                                                                           {if(state.backtracking == 0){action.decltype_specifier(input.LT(1));}}
    :
    (literal_decltype | literal_typeof)                                         
    LPAREN                                                                      {action.decltype_specifier(action.DECLTYPE_SPECIFIER__LPAREN, input.LT(0));}
    expression 
    RPAREN                                                                      {action.decltype_specifier(action.DECLTYPE_SPECIFIER__RPAREN, input.LT(0));}
                                                                                
    ;
finally                                                                         {if(state.backtracking == 0){action.end_decltype_specifier(input.LT(0));}}

elaborated_type_specifier
@init                                                                           {if(state.backtracking == 0){action.elaborated_type_specifier(input.LT(1));}}
    :                                                                           //{action.elaborated_type_specifier(input.LT(1));}
    (
        class_key attribute_specifiers? SCOPE?         
        (
            (IDENT SCOPE) =>
                nested_name_specifier (simple_template_id_or_IDENT | LITERAL_template simple_template_id_nocheck)
        |
            (
                simple_template_id_or_IDENT                                     //{action.elaborated_type_specifier(input.LT(0));}
            | 
                LITERAL_template simple_template_id_nocheck
            )
        )
    |
        LITERAL_enum SCOPE? (
            (IDENT SCOPE)=>
                nested_name_specifier IDENT
        |
            (IDENT)=>
                IDENT
        )
    )                                                                           //{action.end_elaborated_type_specifier(input.LT(0));}
    ;
finally                                                                         {if(state.backtracking == 0){action.end_elaborated_type_specifier(input.LT(0));}}
    
// In C++0x this is factored out already
typename_specifier
@init                                                                           {if(state.backtracking == 0){action.typename_specifier(input.LT(1));}}
    :
        LITERAL_typename SCOPE? nested_name_specifier ( simple_template_id_or_IDENT  | LITERAL_template simple_template_id_nocheck )
    ;
finally                                                                         {if(state.backtracking == 0){action.end_typename_specifier(input.LT(0));}}

/*
 * original rule (not needed now):
enum_name:
        IDENT
    ;
 *
 */
enum_specifier:
//        LITERAL_enum IDENT? LCURLY enumerator_list? RCURLY
                                                                                {action.enum_declaration(input.LT(1));}
        enum_head 
        (
            LCURLY                                                              {action.enum_body($LCURLY);}
            (enumerator_list COMMA?)? 
            RCURLY                                                              {action.end_enum_body($RCURLY);}
                                                                                {action.end_enum_declaration($RCURLY);}
        |
            SEMICOLON                                                           {action.end_enum_declaration($SEMICOLON);}
        )
    ;
enum_head:
    enum_key attribute_specifiers?
    (
        nested_name_specifier IDENT                                             {action.enum_name(input.LT(0));}
    |
        (   
            IDENT                                                               {action.enum_name(input.LT(0));}
        )?
    )
    enum_base?
    ;
//opaque_enum_declaration:
//    enum_key IDENT enum_base? SEMICOLON
//    ;
enum_key:
    LITERAL_enum // (LITERAL_class | LITERAL_struct)?
    ;
enum_base:
    COLON type_specifier+
    ;
enumerator_list:
        enumerator_definition (COMMA enumerator_definition)* 
    ;
enumerator_definition:
        enumerator 
    |
        enumerator ASSIGNEQUAL constant_expression 
    ;

enumerator:
        IDENT                                                                   {action.enumerator($IDENT);}
    ;

/*
 * original rules (not needed now):

namespace_name:
        original_namespace_name 
    |
        namespace_alias 
    ;

original_namespace_name:
        IDENT
    ;
 *
 */

/*
 * original rules:

namespace_definition:
        named_namespace_definition
    |
        unnamed_namespace_definition
    ;

named_namespace_definition:
        original_namespace_definition
    |
        extension_namespace_definition
    ;
original_namespace_definition:
        LITERAL_namespace IDENT LCURLY namespace_body RCURLY
    ;
extension_namespace_definition:
        LITERAL_namespace original_namespace_name LCURLY namespace_body RCURLY
    ;

unnamed_namespace_definition:
        LITERAL_namespace LCURLY namespace_body RCURLY
    ;

 * This is all unnecessarily complicated. We can easily handle it by one single rule:
 */
namespace_definition:
        LITERAL_inline?
        LITERAL_namespace                                                       {action.namespace_declaration($LITERAL_namespace);}
        (   
            IDENT                                                               {action.namespace_name($IDENT);}
        )?
        gnu_attribute_or_extension_specifiers?
        LCURLY                                                                  {action.namespace_body($LCURLY);}
        sync_declaration
        (
            declaration[object_decl]
            sync_declaration
        )*
        RCURLY                                                                  {action.end_namespace_body($RCURLY);} 
                                                                                {action.end_namespace_declaration($RCURLY);} 
    ;

namespace_body:
        declaration[object_decl] *
    ;

namespace_alias:
        IDENT
    ;

namespace_alias_definition:
        LITERAL_namespace IDENT ASSIGNEQUAL                                     {action.namespace_alias_definition($LITERAL_namespace, $IDENT, $ASSIGNEQUAL);}
        qualified_namespace_specifier 
        SEMICOLON                                                               {action.end_namespace_alias_definition($SEMICOLON);}
    ;

qualified_namespace_specifier:
        (
            SCOPE                                                               {action.qualified_namespace_specifier(action.QUALIFIED_NAMESPACE_SPECIFIER__SCOPE, $SCOPE);}
        )? 
        nested_name_specifier? 
        IDENT                                                                   {action.qualified_namespace_specifier(action.QUALIFIED_NAMESPACE_SPECIFIER__IDENT, $IDENT);}
    ;

/*
 * original rule:

using-declaration:
        LITERAL_using LITERAL_typename? SCOPE? nested_name_specifier unqualified_id SEMICOLON
     |
        LITERAL_using SCOPE unqualified_id SEMICOLON
     ;

 * Ambiguity in SCOPE between two alternatives resolved by collapsing them into one.
 * Note that new rule allows LITERAL_using unqualified_id w/o SCOPE, not allowed before.
 * It should be ruled out after the parsing.
 */
using_declaration
     : 
        LITERAL_using                                                           {action.using_declaration($LITERAL_using);}
        (
            LITERAL_typename                                                    {action.using_declaration(action.USING_DECLARATION__TYPENAME, $LITERAL_typename);}
        )?
        (
            SCOPE                                                               {action.using_declaration(action.USING_DECLARATION__SCOPE, $SCOPE);}
        )?
        // TODO: review temp predicate
        ((simple_template_id_or_IDENT SCOPE) => nested_name_specifier)? 
        unqualified_id
        SEMICOLON                                                               {action.end_using_declaration($SEMICOLON);}
    ;

using_directive:
        LITERAL_using LITERAL_namespace                                         {action.using_directive($LITERAL_using, $LITERAL_namespace);}
        (
            SCOPE                                                               {action.using_directive(action.USING_DIRECTIVE__SCOPE, $SCOPE);}
        )? 
        nested_name_specifier? 
        IDENT                                                                   {action.using_directive(action.USING_DIRECTIVE__IDENT, $IDENT);}
        SEMICOLON                                                               {action.end_using_directive($SEMICOLON);}
    ;


asm_statement:
        literal_asm LPAREN adjacent_string_literals? RPAREN
    ;

asm_definition:
        asm_statement SEMICOLON                      //{action.asm_definition($LITERAL_asm, $LPAREN, $STRING_LITERAL, $RPAREN, $SEMICOLON);}
    ;

linkage_specification [decl_kind kind]:
        LITERAL_extern STRING_LITERAL                                           {action.linkage_specification($LITERAL_extern, $STRING_LITERAL);}
        (
            LCURLY                                                              {action.linkage_specification(action.LINKAGE_SPECIFICATION__LCURLY, input.LT(0));}
            sync_declaration
            (
                declaration[kind]
                sync_declaration
            )*
            RCURLY                                                              {action.linkage_specification(action.LINKAGE_SPECIFICATION__RCURLY, input.LT(0));}
    |
            declaration[kind]
        )                                                                       {action.end_linkage_specification(input.LT(0));}
    ;

attribute_specifiers:
        (attribute_specifier | gnu_attribute_or_extension_specifier)+
    ;

cpp11_attribute_specifiers:
        attribute_specifier+
    ;

attribute_specifier:
        LSQUARE LSQUARE attribute_list RSQUARE RSQUARE
    |
        aligment_specifier
    ;

aligment_specifier:
        LITERAL_alignas LPAREN
        (
            (type_id)=> type_id
        |
            assignment_expression
        )
        ELLIPSIS? RPAREN
    ;

attribute_list:
        (attribute ELLIPSIS? (COLON (attribute ELLIPSIS?)?)?)?
    ;

attribute:
        attribute_token attribute_argument_clouse?
    ;

attribute_token:
        IDENT
    |
        attribute_scoped_token
    ;

attribute_scoped_token:
        attribute_namespace SCOPE IDENT
    ;

attribute_namespace:
        IDENT
    ;

attribute_argument_clouse:
        LPAREN balanced_tokens RPAREN
    ;

balanced_tokens:
        balanced_token+
    ;

balanced_token:
        LPAREN balanced_tokens RPAREN
    |
        LSQUARE balanced_tokens RSQUARE
    |
        LCURLY balanced_tokens RCURLY
    |
        ~(RCURLY | LCURLY | LSQUARE | RSQUARE | LPAREN | RPAREN)
    ;

gnu_attribute_or_extension_specifiers:
        gnu_attribute_or_extension_specifier+
    ;

gnu_attribute_or_extension_specifier:
        gnu_attribute_specifier
    |
        LITERAL___extension__
    ;

gnu_attribute_specifiers:
        gnu_attribute_specifier+
    ;

gnu_attribute_specifier:
        LITERAL___attribute__ LPAREN balanced_tokens RPAREN
    ;

init_declarator_list
@init                                                                           {if(state.backtracking == 0){action.init_declarator_list(input.LT(1));}}
    :                                                                       
        init_declarator
        (
            COMMA                                                               {action.init_declarator_list(action.INIT_DECLARATOR_LIST__COMMA, input.LT(0));}
            init_declarator
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_init_declarator_list(input.LT(0));}}

/*
 * As per 2003 standard:
 * Ambiguity happens "between a function declaration with a redundant set of parentheses
 * around a parameter name and an object declaration with a function-style cast as the initializer."
 *
 * Thus declarator (which can end in parameters_and_qualifiers) conflicts with "()"-initializer.
 * "the resolution is to consider any construct that could possibly be a declaration a declaration".
 * Thus we take parameters_and_qualifiers as far as possible.
 *
 */
init_declarator
@init                                                                           {if(state.backtracking == 0){action.init_declarator(input.LT(1));}}
    :                                                                           
        greedy_declarator asm_statement? initializer?                                          
    ;
finally                                                                         {if(state.backtracking == 0){action.end_init_declarator(input.LT(0));}}

/*
 * original rule (naming as per C++0X)
declarator:
    ptr_declarator
    ;
ptr_declarator:
        noptr_declarator
    |
        ptr_operator ptr_declarator
    ;
noptr_declarator:
        declarator_id
    |
        noptr_declarator parameters-and-qualifiers
    |
        noptr_declarator LSQUARE constant_expression? RSQUARE
    |
        LPAREN ptr_declarator RPAREN
    ;
 * Ambiguity on nested_name qualifier is caused by ptr_operator vs declarator_id (of direct declarator).
 * It qualifies either STAR (for ptr_operator) or type_name (for declarator_id).
 * Resolve by syntactically probing ptr_operator first.
 */



declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.declarator(input.LT(1));}}
    :                                                                           
    (
        (ptr_operator)=>
            ptr_operator literal_restrict? nested=declarator
//                {{ type = $nested.type;
//                   type.apply_ptr($ptr_operator.type);
//                }}
    |
        noptr_declarator 
//            {{ type = $noptr_declarator.type; }}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_declarator(input.LT(0));}}

// is quite unpretty because of left recursion removed here
noptr_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.noptr_declarator(input.LT(1));}}
    :                                                                           
        (
            declarator_id attribute_specifiers?
//                {{ type = $declarator_id.type; }}
        |
            LPAREN                                                              {action.noptr_declarator(action.NOPTR_DECLARATOR__LPAREN, input.LT(0));}
            gnu_attribute_specifier*
            declarator 
            RPAREN                                                              {action.noptr_declarator(action.NOPTR_DECLARATOR__RPAREN, input.LT(0));}
//                {{ type = $declarator.type; }}
        ) // continued
        (
            parameters_and_qualifiers
//                {{ type.apply_parameters($parameters_and_qualifiers.pq); }}
         |
            LSQUARE                                                             {action.noptr_declarator(action.NOPTR_DECLARATOR__LSQUARE, input.LT(0));}
            constant_expression? 
            RSQUARE                                                             {action.noptr_declarator(action.NOPTR_DECLARATOR__RSQUARE, input.LT(0));}
            attribute_specifiers?
//                {{ type.apply_array($constant_expression.expr); }}
        )*
        trailing_return_type?                                                   
    ;
finally                                                                         {if(state.backtracking == 0){action.end_noptr_declarator(input.LT(0));}}

trailing_return_type:
    LITERAL_POINTERTO trailing_type_specifier+ 
    ((abstract_declarator) => abstract_declarator)? // review: predicate to avoid ambiguity around ELLIPSIS
    ;
/*
 *   This rule was crafted in order to resolve ambiguity between decl_specifier (type_specifier)
 * and constructor declaration (which has declarator_id == class name).
 * For that we create a special "constructor-declarator", which is a function declarator *BUT* without a
 * leading class name.
 */
function_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.function_declarator(input.LT(1));}}
    :                                                                           
    (
        (constructor_declarator)=>
            constructor_declarator //{{ type = $constructor_declarator.type; }}
    |
        declarator //{{ type = $declarator.type; }}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_function_declarator(input.LT(0));}}

constructor_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.constructor_declarator(input.LT(1));}}
    :                                                                           
        parameters_and_qualifiers                                               
            //{{ type.set_constructor($parameters_and_qualifiers.pq); }}
    ;
finally                                                                         {if(state.backtracking == 0){action.end_constructor_declarator(input.LT(0));}}

/*

abstract_declarator:
        ptr_abstract_declarator
    ;
ptr_abstract_declarator:
        noptr_abstract_declarator
    |
        ptr_operator ptr_abstract_declarator?
    ;

noptr_abstract_declarator:
        noptr_abstract_declarator? parameters_and_qualifiers
    |
        noptr_abstract_declarator? LSQUARE constant_expression RSQUARE
    |
        ( ptr_abstract_declarator )
    ;
*/

abstract_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.function_declarator(input.LT(1));}}
    :                                                                           
    (
        noptr_abstract_declarator 
        //{{ type = $noptr_abstract_declarator.type; }}
        trailing_return_type?
    |
        ptr_operator literal_restrict? ((abstract_declarator) => abstract_declarator)? // review: predicate to avoid ambiguity around ELLIPSIS
//            {{ type = $decl.type;
//               type.apply_ptr($ptr_operator.type);
//            }}
    |
        ELLIPSIS                                                                {action.function_declarator(action.FUNCTION_DECLARATOR__ELLIPSIS, input.LT(0));}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_function_declarator(input.LT(0));}}

noptr_abstract_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.noptr_abstract_declarator(input.LT(1));}}
    :                                                                           
    (
        (LPAREN abstract_declarator RPAREN)=>
            LPAREN                                                              {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__LPAREN, input.LT(0));}
            abstract_declarator 
            RPAREN                                                              {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__RPAREN, input.LT(0));}
            ( 
                parameters_and_qualifiers 
            | 
                LSQUARE                                                         {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__LSQUARE, input.LT(0));}
                constant_expression? 
                RSQUARE                                                         {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__RSQUARE, input.LT(0));}
        )*
    |
        ( 
            parameters_and_qualifiers 
    |
            LSQUARE                                                             {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__LSQUARE, input.LT(0));}
            constant_expression? 
            RSQUARE                                                             {action.noptr_abstract_declarator(action.NOPTR_ABSTRACT_DECLARATOR__RSQUARE, input.LT(0));}
        )+
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_noptr_abstract_declarator(input.LT(0));}}

universal_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.universal_declarator(input.LT(1));}}
    :                                                                           
    (options { backtrack = true; }:
        declarator //{ type = $declarator.type; }
    |
        abstract_declarator //{ type = $abstract_declarator.type; }
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_universal_declarator(input.LT(0));}}

greedy_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.greedy_declarator(input.LT(1));}}
    :   
    (
        (ptr_operator)=>
            ptr_operator gnu_attribute_specifier* decl=greedy_declarator
    |
        greedy_nonptr_declarator //{{ type = $greedy_nonptr_declarator.type; }}
//            {{ type = $decl.type;
//               type.apply_ptr($ptr_operator.type);
//            }}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_greedy_declarator(input.LT(0));}}

/*
 * This is to resolve ambiguity between declarator and subsequent (expression) initializer in init_declarator.
 * Eat as much parameter sets as possible.
 */
greedy_nonptr_declarator returns [declarator_type_t type]
@init                                                                           {if(state.backtracking == 0){action.greedy_nonptr_declarator(input.LT(1));}}
    :                                                                           
    (
        (
            declarator_id attribute_specifiers?
                //{{ type = $declarator_id.type; }}
        |
            LPAREN                                                              {action.greedy_nonptr_declarator(action.GREEDY_NONPTR_DECLARATOR__LPAREN, input.LT(0));}
            gnu_attribute_specifier*
            greedy_declarator 
            RPAREN                                                              {action.greedy_nonptr_declarator(action.GREEDY_NONPTR_DECLARATOR__RPAREN, input.LT(0));}
                //{{ type = $greedy_declarator.type; }}
        ) // continued
        (
            (parameters_and_qualifiers)=>
                parameters_and_qualifiers
                //{{ type.apply_parameters($parameters_and_qualifiers.pq); }}
        |
            LSQUARE                                                             {action.greedy_nonptr_declarator(action.GREEDY_NONPTR_DECLARATOR__LSQUARE, input.LT(0));}
            constant_expression? 
            RSQUARE                                                             {action.greedy_nonptr_declarator(action.GREEDY_NONPTR_DECLARATOR__RSQUARE, input.LT(0));}
                //{{ type.apply_array($constant_expression.expr); }}
        )*
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_greedy_nonptr_declarator(input.LT(0));}}

ptr_operator returns [ declarator_type_t type ]
@init                                                                           {if(state.backtracking == 0){action.ptr_operator(input.LT(1));}}
    :                                                                           
    (
        STAR                                                                    {action.ptr_operator(action.PTR_OPERATOR__STAR, input.LT(0));}
        cv_qualifier*
            //{{ type.set_ptr(NULL, $cv_qualifier.qual); }}
    |
        AMPERSAND                                                               {action.ptr_operator(action.PTR_OPERATOR__AMPERSAND, input.LT(0));}
            //{{ type.set_ref(); }}
    |
        AND                                                                     {action.ptr_operator(action.PTR_OPERATOR__AND, input.LT(0));}
    |
        (
            SCOPE                                                               {action.ptr_operator(action.PTR_OPERATOR__SCOPE, input.LT(0));}
        )? 
        nested_name_specifier 
        STAR                                                                    {action.ptr_operator(action.PTR_OPERATOR__STAR2, input.LT(0));}
        cv_qualifier*
//           {{ type.set_ptr(& $nested_name_specifier.namequal, $cv_qualifier.qual); }}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_ptr_operator(input.LT(0));}}

cv_qualifier returns [ qualifier_t qual ]:
        literal_const                                                           {action.cv_qualifier(action.CV_QUALIFIER__CONST, input.LT(0));}
        //{{ qual = LITERAL_const; }}
    |
        LITERAL_volatile                                                        {action.cv_qualifier(action.CV_QUALIFIER__VOLATILE, input.LT(0));}
        //{{ qual = LITERAL_volatile; }}
    ;

ref_qualifier:
        AMPERSAND                                                               {action.ref_qualifier(action.REF_QUALIFIER__AMPERSAND, input.LT(0));}
    |
        AND                                                                     {action.ref_qualifier(action.REF_QUALIFIER__AND, input.LT(0));}
    ;

/*
 * original rule:

    |
        SCOPE? nested_name_specifier? type_name 

 * This alternative deleted, as it actually is contained in id_expression
 */

declarator_id returns [ declarator_type_t type ] 
@init                                                                           {if(state.backtracking == 0){action.declarator_id(input.LT(1));}}
    :                                                                           
        (
            ELLIPSIS                                                            {action.declarator_id(action.DECLARATOR_ID__ELLIPSIS, input.LT(0));}
        )? 
        id_expression //{{ type.set_ident(); }}                       
                                                                                
    ;
finally                                                                         {if(state.backtracking == 0){action.end_declarator_id(input.LT(0));}}

/*
 * from 8.2 Ambiguity resolution:
 * "any construct that could possibly be a type-id in its syntactic context
 * shall be considered a type-id"
 */
type_id
@init                                                                           {if(state.backtracking == 0){action.type_id(input.LT(1));}}
    :                                            
        gnu_attribute_or_extension_specifiers?
        (type_specifier gnu_attribute_specifiers?)+ 
        ((abstract_declarator) => abstract_declarator)? // review: predicate to avoid ambiguity around ELLIPSIS
    ;
finally                                                                         {if(state.backtracking == 0){action.end_type_id(input.LT(0));}}

parameters_and_qualifiers returns [ parameters_and_qualifiers_t pq ]
@init                                                                           {if(state.backtracking == 0){action.parameters_and_qualifiers(input.LT(1));}}
    :                                                                           
        parameters
        attribute_specifiers?
        cv_qualifier* 
        ref_qualifier? 
        exception_specification?                                                
    ;
finally                                                                         {if(state.backtracking == 0){action.end_parameters_and_qualifiers(input.LT(0));}}

parameters
scope Declaration; /* need it zero'ed to handle hoisted type_specifier predicate */
@init                                                                           {if(state.backtracking == 0){action.parameters_and_qualifiers(action.PARAMETERS_AND_QUALIFIERS__LPAREN, input.LT(1));}}
    :                                                                           
        LPAREN
                                                                                {if(state.backtracking == 0){action.parameter_declaration_clause(input.LT(1));}}
        (
            ELLIPSIS?
        |
            (                                                                   {if(state.backtracking == 0){action.parameter_declaration_list(input.LT(1));}}
                sync_parameter
                parameter_declaration[parm_decl] 
                (
                    COMMA                                                       {action.end_parameter_declaration_list(action.PARAMETER_DECLARATION_LIST__COMMA, input.LT(0));}
                    parameter_declaration[parm_decl]
                    sync_parameter
                )*                                                              
                                                                                {if(state.backtracking == 0){action.end_parameter_declaration_list(input.LT(0));}}
            )
            (
                COMMA                                                               {action.parameter_declaration_clause(action.PARAMETER_DECLARATION_CLAUSE__COMMA, input.LT(0));}
                ELLIPSIS                                                            {action.parameter_declaration_clause(action.PARAMETER_DECLARATION_CLAUSE__ELLIPSIS2, input.LT(0));}
            )?
        )
        RPAREN                                                                  {if(state.backtracking == 0){action.end_parameter_declaration_clause(input.LT(0));}}
    ;
finally                                                                         {if(state.backtracking == 0){action.parameters_and_qualifiers(action.PARAMETERS_AND_QUALIFIERS__RPAREN, input.LT(0));}}

parameter_declaration_clause
scope Declaration; /* need it zero'ed to handle hoisted type_specifier predicate */
@init                                                                           {if(state.backtracking == 0){action.parameter_declaration_clause(input.LT(1));}}
    :                                                                           
    (
        (
            ELLIPSIS                                                            {action.parameter_declaration_clause(action.PARAMETER_DECLARATION_CLAUSE__ELLIPSIS, input.LT(0));}
        )?
    |
        parameter_declaration_list 
        (
            COMMA                                                               {action.parameter_declaration_clause(action.PARAMETER_DECLARATION_CLAUSE__COMMA, input.LT(0));}
            ELLIPSIS                                                            {action.parameter_declaration_clause(action.PARAMETER_DECLARATION_CLAUSE__ELLIPSIS2, input.LT(0));}
        )?
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_parameter_declaration_clause(input.LT(0));}}

parameter_declaration_list
@init                                                                           {if(state.backtracking == 0){action.parameter_declaration_list(input.LT(1));}}
    :                                                                           
        parameter_declaration[parm_decl] 
        (
            COMMA                                                               {action.end_parameter_declaration_list(action.PARAMETER_DECLARATION_LIST__COMMA, input.LT(0));}
            parameter_declaration[parm_decl]
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_parameter_declaration_list(input.LT(0));}}

parameter_declaration [decl_kind kind]
scope Declaration;
@init                                                                           {if(state.backtracking == 0){action.parameter_declaration(input.LT(1));}}
    :                                                                           
        attribute_specifiers?                                                                                                                                                               
                                                                                {action.decl_specifiers(input.LT(1));}
        (decl_specifier attribute_specifiers?)                                  
        ( decl_specifier attribute_specifiers? )*                               {action.end_decl_specifiers(null/*input.LT(0)*/);}

        universal_declarator? 
        (
            ASSIGNEQUAL                                                         {action.parameter_declaration(action.PARAMETER_DECLARATION__ASSIGNEQUAL, input.LT(0));}
            assignment_expression
        )?                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_parameter_declaration(input.LT(0));}}

/*
 * original rule:

function_definition:
        decl_specifier* declarator ctor_initializer? function_body 
    |
        decl_specifier* declarator function_try_block
    ;

 * Factoring out a sequence that follows declarator, as it helps disambiguating in context when
 * function_definition conflicts because of decl_specifier
 */
function_definition_after_declarator[boolean class_late_binding, boolean member_declaration, boolean standalone]
@init                                                                           {if(state.backtracking == 0 && !standalone && (!class_late_binding || !member_declaration)){action.function_definition_after_declarator(input.LT(1));}}
    :
        (ASSIGNEQUAL) => 
        ASSIGNEQUAL                                                             {action.function_definition_after_declarator(action.FUNCTION_DEFINITION_AFTER_DECLARATOR__ASSIGNEQUAL, input.LT(0));}
        (
            LITERAL_delete                                                      {action.function_definition_after_declarator(action.FUNCTION_DEFINITION_AFTER_DECLARATOR__DELETE, input.LT(0));}
        | 
            LITERAL_default                                                     {action.function_definition_after_declarator(action.FUNCTION_DEFINITION_AFTER_DECLARATOR__DEFAULT, input.LT(0));}
        ) 
        SEMICOLON
    |
        ({!standalone && !class_late_binding && member_declaration}?
            ((COLON) => COLON                                                   {if(state.backtracking == 0){action.skip_balanced_curlies(input.LT(0));}}
                (
                    ~(RCURLY | LCURLY)                                          {if(state.backtracking == 0){action.skip_balanced_curlies(input.LT(0));}}
                )*
            )?
            skip_balanced_Curl
        |
            (
                ctor_initializer? function_body[class_late_binding]
            |
                function_try_block[class_late_binding]
            )
        )
    ;
finally                                                                         {if(state.backtracking == 0 && !standalone && (!class_late_binding || !member_declaration)){action.end_function_definition_after_declarator(input.LT(0));}}

/*
 * We have a baaad conflict caused by declaration w/o decl_specifier,
 * that is w/o return type specification.
 *
 * In old K&R C times this was an "implicit int" declaration.
 * Currently we allow only constructors/destructors to have no return type
 * (and surely it does not mean "implicit int").
 *
 * However constructor's name conflicts with type_specifier of an ordinary declaration.
 *
 * This conflict rises for any function declaration
 */

function_declaration [decl_kind kind]
scope Declaration;
@init                                                                           {if(state.backtracking == 0){action.function_declaration(input.LT(1));}}
    :                                                                           
        decl_specifier* function_declarator                                     
    ;
finally                                                                         {if(state.backtracking == 0){action.end_function_declaration(input.LT(0));}}

//function_definition [decl_kind kind]
//    :                                                                           {action.function_definition(input.LT(1));}
//        function_declaration[kind] function_definition_after_declarator         {action.end_function_definition(input.LT(0));}
//    ;

function_body[boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.function_body(input.LT(1));}}
    :
        compound_statement[class_late_binding] 
    ;
finally                                                                         {if(state.backtracking == 0){action.end_function_body(input.LT(0));}}

initializer
@init                                                                           {if(state.backtracking == 0){action.initializer(input.LT(1));}}
    :                                                                           
    (
        brace_or_equal_initializer
    |
        LPAREN                                                                  {action.initializer(action.INITIALIZER__LPAREN, input.LT(0));}
        expression_list 
        RPAREN                                                                  {action.initializer(action.INITIALIZER__RPAREN, input.LT(0));}
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_initializer(input.LT(0));}}

brace_or_equal_initializer
@init                                                                           {if(state.backtracking == 0){action.brace_or_equal_initializer(input.LT(1));}}
    :                                                                           
    (
        ASSIGNEQUAL                                                             {action.brace_or_equal_initializer(action.BRACE_OR_EQUAL_INITIALIZER__ASSIGNEQUAL, input.LT(0));}
        initializer_clause 
    |
        braced_init_list
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_brace_or_equal_initializer(input.LT(0));}}

initializer_clause
@init                                                                           {if(state.backtracking == 0){action.initializer_clause(input.LT(1));}}
    : 
    (
        assignment_expression 
    |
        braced_init_list
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_initializer_clause(input.LT(0));}}

initializer_list
@init                                                                           {if(state.backtracking == 0){action.initializer_list(input.LT(1));}}
        :                                                                       
        initializer_clause 
        (   
            COMMA                                                               {action.initializer_list(action.INITIALIZER_LIST__COMMA, input.LT(0));}
            initializer_clause 
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_initializer_list(input.LT(0));}}

braced_init_list
@init                                                                           {if(state.backtracking == 0){action.braced_init_list(input.LT(1));}}
    :                                                                           
    LCURLY 
    (
        initializer_list 
        (
            COMMA                                                               {action.braced_init_list(action.BRACED_INIT_LIST__COMMA, input.LT(0));}
        )?
    )? 
    RCURLY                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_braced_init_list(input.LT(0));}}

//[gram.class] 
class_name
@init                                                                           {if(state.backtracking == 0){action.class_name(input.LT(1));}}
    :                                                                           
        simple_template_id_or_IDENT                                             
    ;
finally                                                                         {if(state.backtracking == 0){action.end_class_name(input.LT(0));}}

class_specifier
@init                                                                           {if(state.backtracking == 0){action.class_declaration(input.LT(1));}}
    :
        class_head 
        LCURLY                                                                  {action.class_body($LCURLY);}
        sync_member
        (
            member_specification[false]
            sync_member
        )*
        RCURLY                                                                  {action.end_class_body($RCURLY);}
    ;
finally                                                                         {if(state.backtracking == 0){action.end_class_declaration(input.LT(0));}}

/*
 * Original rule:

class_head:
        class_key IDENT? base_clause? 
    |
        class_key nested_name_specifier IDENT base_clause? 
    |
        class_key nested_name_specifier? simple_template_id base_clause? 
    ;

*  Ambiguity due to nested_name_specifier usage
*/
optionally_qualified_name
@init                                                                           {if(state.backtracking == 0){action.optionally_qualified_name(input.LT(1));}}
    :                                                                           
        // TODO: review temp predicate
        (((lookup_simple_template_id | IDENT) SCOPE) => nested_name_specifier)? 
        simple_template_id_or_IDENT                                             //{action.class_name(input.LT(0));}
    ;
finally                                                                         {if(state.backtracking == 0){action.end_optionally_qualified_name(input.LT(0));}}

class_head
@init                                                                           {if(state.backtracking == 0){action.class_head(input.LT(1));}}
    :                                                                           
        class_key 
        attribute_specifiers?
        optionally_qualified_name? 
        class_virtual_specifier* 
        base_clause?                                                            
    ;
finally                                                                         {if(state.backtracking == 0){action.end_class_head(input.LT(0));}}

class_virtual_specifier:
        LITERAL_final                                                           {action.class_virtual_specifier(action.CLASS_VIRTUAL_SPECIFIER__FINAL, input.LT(0));}
    |   
        LITERAL_explicit                                                        {action.class_virtual_specifier(action.CLASS_VIRTUAL_SPECIFIER__EXPLICIT, input.LT(0));}
    ;

class_key:
        LITERAL_class                                                           {action.class_kind($LITERAL_class);}
    |
        LITERAL_struct                                                          {action.class_kind($LITERAL_struct);}
    |
        LITERAL_union                                                           {action.class_kind($LITERAL_union);}
    ;

member_specification[boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.member_specification(input.LT(1));}}
    :
        access_specifier 
        COLON                                                                   {action.member_specification(action.MEMBER_SPECIFICATION__COLON, input.LT(0));}
    |
        member_declaration[field_decl, class_late_binding]
    ;
finally                                                                         {if(state.backtracking == 0){action.end_member_specification(input.LT(0));}}


/*
 * original rule (part that was rewritten)

 member_declaration:
        decl_specifier* member_declarator_list? SEMICOLON
    |
        function_definition SEMICOLON?
    |
        SCOPE? nested_name_specifier LITERAL_template? unqualified_id SEMICOLON
    |

member_declarator:
        declarator constant_initializer?
    |
        IDENT? COLON constant_expression
    ;

 *
 * (optional SEMICOLON? deleted after function_defition, as the first alternative takes care of it already)
 * Conflict on decl_specifier between first alternative and second one (function_definition) resolved
 * by factorizing on common parts of the first member_declarator (decl_specifier* declarator).
 * It was pretty involved, and besides member_declaration also affecting 3 other rules.
 *
 * Another conflict is between first set of alternatives and access declaration.
 * Access declaration is being subsumed by member declaration with absent decl_specifier.
 * There needs to be a special semantic check for "access declaration" when handling results of member declaration.
 */
member_declaration [decl_kind kind, boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.member_declaration(input.LT(1));}}
    :
        attribute_specifiers? simple_member_declaration_or_function_definition[kind, class_late_binding]
    |
        /* this is likely to be covered by decl_specifier/declarator part of member_declarator
            SCOPE? nested_name_specifier LITERAL_template? unqualified_id SEMICOLON
    |
        */

        using_declaration
    |
        template_declaration[kind]
    |
        static_assert_declaration
    |
        alias_declaration
    ;
finally                                                                         {if(state.backtracking == 0){action.end_member_declaration(input.LT(0));}}

simple_member_declaration_or_function_definition[decl_kind kind, boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.simple_member_declaration(input.LT(1));}}
    :
                                                                                {action.decl_specifiers(input.LT(1));}
        decl_specifier*                                                         {action.end_decl_specifiers(null/*input.LT(0)*/);}
        (
            (IDENT? COLON)=>
                member_bitfield_declarator 
                ( 
                    COMMA                                                       {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__COMMA2, input.LT(0));}
                    member_declarator 
                )* 
                SEMICOLON                                                       {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__SEMICOLON, input.LT(0));}
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this was member_declarator_list
                    ( 
                        COMMA                                                   {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__COMMA2, input.LT(0));}
                        member_declarator 
                    )* 
                    SEMICOLON                                                   {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__SEMICOLON, input.LT(0));}
                |
                    function_definition_after_declarator[class_late_binding, true, false]
                )
        |
            declarator
            (
                { /*$declarator.type.is_function()*/ (input.LA(1) != ASSIGNEQUAL && (input.LA(1) != COLON || input.LA(0) == RPAREN)) }?
                    function_definition_after_declarator[class_late_binding, true, false]
            |
                // this was member_declarator_list
                constant_initializer? 
                ( 
                    COMMA                                                       {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__COMMA2, input.LT(0));}
                    member_declarator 
                )* 
                SEMICOLON                                                       {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__SEMICOLON, input.LT(0));}
            )
        |
            SEMICOLON                                                           {action.simple_member_declaration(action.SIMPLE_MEMBER_DECLARATION__SEMICOLON, input.LT(0));}
        )
    ;
finally                                                                         {if(state.backtracking == 0){action.end_simple_member_declaration(input.LT(0));}}

member_bitfield_declarator
    :
        (
            IDENT                                                               {if(state.backtracking == 0){action.member_bitfield_declarator(input.LT(0));}}
        )? 
        virt_specifier* 
        COLON 
        constant_expression
    ;

member_declarator
@init                                                                           {if(state.backtracking == 0){action.member_declarator(input.LT(1));}}
    :                                                                           
    (
        declarator virt_specifier* brace_or_equal_initializer?
    |
        member_bitfield_declarator
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_member_declarator(input.LT(0));}}

/*
 * original rule:

member_declarator_list:
        member_declarator ( COMMA member_declarator )*
    ;

 *
 * No longer needed as this list was inserted into member_declaration rule in order to
 * factorize first member_declaration entry.
 */

// = 0 (not used, as it conflicts with constant_initializer
pure_specifier
@init                                                                           {if(state.backtracking == 0){action.pure_specifier(input.LT(1));}}
    :                                                                           
        ASSIGNEQUAL literal                                                     
    ;
finally                                                                         {if(state.backtracking == 0){action.end_pure_specifier(input.LT(0));}}

constant_initializer
@init                                                                           {if(state.backtracking == 0){action.constant_initializer(input.LT(1));}}
    :                                                                           
        ASSIGNEQUAL constant_expression                                         
    ;
finally                                                                         {if(state.backtracking == 0){action.end_constant_initializer(input.LT(0));}}

virt_specifier:
        LITERAL_override                                                        {action.virt_specifier(action.VIRT_SPECIFIER__OVERRIDE, input.LT(0));}
    |
        LITERAL_final                                                           {action.virt_specifier(action.VIRT_SPECIFIER__FINAL, input.LT(0));}
    |
        LITERAL_new                                                             {action.virt_specifier(action.VIRT_SPECIFIER__NEW, input.LT(0));}
    ;

// [gram.class.derived] 
base_clause
@init                                                                           {if(state.backtracking == 0){action.base_clause(input.LT(1));}}
    :                                                                           
        COLON base_specifier_list                                               
    ;
finally                                                                         {if(state.backtracking == 0){action.end_base_clause(input.LT(0));}}

base_specifier_list
@init                                                                           {if(state.backtracking == 0){action.base_specifier_list(input.LT(1));}}
        :                                                                       
        base_specifier 
        (
            ELLIPSIS                                                            {action.base_specifier_list(action.BASE_SPECIFIER_LIST__ELLIPSIS, input.LT(0));}
        )? 
        ( 
            COMMA                                                               {action.base_specifier_list(action.BASE_SPECIFIER_LIST__COMMA, input.LT(0));}
            base_specifier 
            (
                ELLIPSIS                                                        {action.base_specifier_list(action.BASE_SPECIFIER_LIST__ELLIPSIS, input.LT(0));}
            )? 
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_base_specifier_list(input.LT(0));}}

base_specifier:
    attribute_specifiers?
    (
        base_type_specifier
    |
        LITERAL_virtual access_specifier? base_type_specifier
    |
        access_specifier LITERAL_virtual? base_type_specifier
    )
    ;
class_or_decltype
@init                                                                           {if(state.backtracking == 0){action.class_or_decltype(input.LT(1));}}
    :                                                                           
    (
        (
            SCOPE                                                               {action.class_or_decltype(action.CLASS_OR_DECLTYPE__SCOPE, input.LT(0));}
        )? 
        nested_simple_template_id_or_IDENT
    |
        decltype_specifier
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_class_or_decltype(input.LT(0));}}

base_type_specifier
@init                                                                           {if(state.backtracking == 0){action.base_type_specifier(input.LT(1));}}
    :                                                                           
        class_or_decltype                                                       
    ;
finally                                                                         {if(state.backtracking == 0){action.end_base_type_specifier(input.LT(0));}}

access_specifier:
        LITERAL_private                                                         {action.access_specifier(action.ACCESS_SPECIFIER__PRIVATE, input.LT(0));}
    |
        LITERAL_protected                                                       {action.access_specifier(action.ACCESS_SPECIFIER__PROTECTED, input.LT(0));}
    |
        LITERAL_public                                                          {action.access_specifier(action.ACCESS_SPECIFIER__PUBLIC, input.LT(0));}
    ;

// [gram.special] 
conversion_function_id
@init                                                                           {if(state.backtracking == 0){action.conversion_function_id(input.LT(1));}}
    :                                                                           
        LITERAL_OPERATOR conversion_type_id                                     
    ;
finally                                                                         {if(state.backtracking == 0){action.end_conversion_function_id(input.LT(0));}}

/*
 * original rule:

conversion_type_id:
        type_specifier+ conversion_declarator?
    ;
conversion_declarator:
        ptr_operator+
    ;

 * As per 2003 standard:
 * "The conversion-type-id in a conversion-function-id is the longest possible sequence
 *  of conversion-declarators... This prevents ambiguities between the declarator operator *
 *  and its expression counterparts."
 *
 * Resolve by folding and adding a synpred.
 */
conversion_type_id
@init                                                                           {if(state.backtracking == 0){action.conversion_type_id(input.LT(1));}}
    :                                                                           
        type_specifier
        (type_specifier)*
        ((ptr_operator)=> ptr_operator)*                                        
    ;
finally                                                                         {if(state.backtracking == 0){action.end_conversion_type_id(input.LT(0));}}

ctor_initializer
@init                                                                           {if(state.backtracking == 0){action.ctor_initializer(input.LT(1));}}
    :                                                                           
        COLON mem_initializer_list                                              
    ;
finally                                                                         {if(state.backtracking == 0){action.end_ctor_initializer(input.LT(0));}}

mem_initializer_list
@init                                                                           {if(state.backtracking == 0){action.mem_initializer_list(input.LT(1));}}
    :                                                                           
        mem_initializer 
        (
            ELLIPSIS                                                            {action.mem_initializer_list(action.MEM_INITIALIZER_LIST__ELLIPSIS, input.LT(0));}
        )? 
        ( 
            COMMA                                                               {action.mem_initializer_list(action.MEM_INITIALIZER_LIST__COMMA, input.LT(0));}
            mem_initializer 
            (
                ELLIPSIS                                                        {action.mem_initializer_list(action.MEM_INITIALIZER_LIST__ELLIPSIS, input.LT(0));}
            )? 
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_mem_initializer_list(input.LT(0));}}

mem_initializer
@init                                                                           {if(state.backtracking == 0){action.mem_initializer(input.LT(1));}}
    :                                                                           
        mem_initializer_id 
        (
            LPAREN                                                              {action.mem_initializer(action.MEM_INITIALIZER__LPAREN, input.LT(0));}
            expression_list? 
            RPAREN                                                              {action.mem_initializer(action.MEM_INITIALIZER__RPAREN, input.LT(0));}
        |
            braced_init_list
        )                                                                       
    ;
finally                                                                         {if(state.backtracking == 0){action.end_mem_initializer(input.LT(0));}}

/*
 * original rule:
mem_initializer_id:
        SCOPE? nested_name_specifier? class_name 
    |
        IDENT 
    ;
 * Ambiguity resolved by removing special class_name case
 */
mem_initializer_id
@init                                                                           {if(state.backtracking == 0){action.mem_initializer_id(input.LT(1));}}
    :                                                                           
        class_or_decltype                                                       
    ;
finally                                                                         {if(state.backtracking == 0){action.end_mem_initializer_id(input.LT(0));}}

// [gram.over] 
operator_function_id
@init                                                                           {if(state.backtracking == 0){action.mem_operator_function_id(input.LT(1));}}
        :
        LITERAL_OPERATOR 
        operator_id 
        ( { operator_is_template() }?=> 
            LESSTHAN                                                            {action.operator_function_id(action.OPERATOR_FUNCTION_ID__LESSTHAN, input.LT(0));}
            template_argument_list? 
            GREATERTHAN                                                         {action.operator_function_id(action.OPERATOR_FUNCTION_ID__GREATERTHAN, input.LT(0));}
        )?                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_operator_function_id(input.LT(0));}}

/*
 * Ambiguity between operator new/delete and operator new/delete[] resolved towards the latter.
 */
operator_id returns [int id]
@init                                                                           {if(state.backtracking == 0){action.operator_id(input.LT(1));}}
    :                                                                           
    (
        (LITERAL_new LSQUARE RSQUARE)=>
            LITERAL_new LSQUARE RSQUARE |
        (LITERAL_delete LSQUARE RSQUARE)=>
            LITERAL_delete LSQUARE RSQUARE |
        LITERAL_new | LITERAL_delete |
        PLUS | MINUS | STAR | DIVIDE | MOD | BITWISEXOR | AMPERSAND | BITWISEOR | TILDE |
        NOT | ASSIGNEQUAL | LESSTHAN | GREATERTHAN | PLUSEQUAL | MINUSEQUAL | TIMESEQUAL | DIVIDEEQUAL | MODEQUAL |
        BITWISEXOREQUAL | BITWISEANDEQUAL | BITWISEOREQUAL | SHIFTLEFT | shiftright_literal | SHIFTRIGHTEQUAL | SHIFTLEFTEQUAL | EQUAL | NOTEQUAL |
        LESSTHANOREQUALTO | GREATERTHANOREQUALTO | AND | OR | PLUSPLUS | MINUSMINUS | COMMA | POINTERTOMBR | POINTERTO | 
        LPAREN RPAREN | LSQUARE RSQUARE 
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_operator_id(input.LT(0));}}

literal_operator_id
    :
        LITERAL_OPERATOR STRING_LITERAL IDENT                                   {action.literal_operator_id($LITERAL_OPERATOR, $STRING_LITERAL, $IDENT);}
    ;

// [gram.temp] 
template_declaration [decl_kind kind]
@init                                                                           {if(state.backtracking == 0){action.template_declaration(input.LT(1));}}
    :                                                                           
        (
            LITERAL_export                                                      {action.template_declaration(action.TEMPLATE_DECLARATION__EXPORT, $LITERAL_export);}
        )? 
        LITERAL_template                                                        {action.template_declaration(action.TEMPLATE_DECLARATION__TEMPLATE, $LITERAL_template);}
        LESSTHAN                                                                {action.template_declaration(action.TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
        template_parameter_list 
        GREATERTHAN                                                             {action.template_declaration(action.TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
        declaration[kind]                                                       
    ;
finally                                                                         {if(state.backtracking == 0){action.end_template_declaration(input.LT(0));}}

template_parameter_list
@init                                                                           {if(state.backtracking == 0){action.template_parameter_list(input.LT(1));}}
    :
        template_parameter                                                      
        ( 
            COMMA                                                               {action.template_parameter_list(action.TEMPLATE_PARAMETER_LIST__COMMA, input.LT(0));}
            template_parameter 
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_template_parameter_list(input.LT(0));}}

/*
 * Ambiguity resolution for LITERAL_class {IDENT,GREATERTHAN,COMMA,ASSIGNEQUAL} conflict between type_parameter
 * and type_specifier, which starts parameter_declaration.
 * To resolve this ambiguity just make an additional type_parameter syntactically predicated
 * with this fixed lookahead.
 *
 * Note that COMMA comes from template_parameter_list rule and GREATERTHAN comes even further from 
 * template_declaration rule
*/
template_parameter
@init                                                                           {if(state.backtracking == 0){action.template_parameter(input.LT(1));}}
    :                                                                           
    (
    (LITERAL_class ( IDENT | GREATERTHAN | COMMA | ASSIGNEQUAL ) )=>
        type_parameter
    |
        // this should map the rest of type_parameter that starts differently from above
        type_parameter
    |
        parameter_declaration[tparm_decl]
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_template_parameter(input.LT(0));}}

type_parameter:
        LITERAL_class ELLIPSIS? IDENT?                                          {action.type_parameter(action.TYPE_PARAMETER__CLASS, $LITERAL_class, $ELLIPSIS, $IDENT);}
    |
        LITERAL_class IDENT? ASSIGNEQUAL type_id                                {action.type_parameter(action.TYPE_PARAMETER__CLASS_ASSIGNEQUAL, $LITERAL_class, $IDENT, $ASSIGNEQUAL);}
    |
        LITERAL_typename ELLIPSIS? IDENT?                                       {action.type_parameter(action.TYPE_PARAMETER__TYPENAME, $LITERAL_typename, $ELLIPSIS, $IDENT);}
    |
        LITERAL_typename IDENT? ASSIGNEQUAL type_id                             {action.type_parameter(action.TYPE_PARAMETER__TYPENAME_ASSIGNEQUAL, $LITERAL_typename, $IDENT, $ASSIGNEQUAL);}
    |
        template_parameter_type
        LITERAL_class ELLIPSIS? IDENT? (ASSIGNEQUAL id_expression)?             {action.type_parameter(action.TYPE_PARAMETER__TEMPLATE_CLASS_ASSIGNEQUAL, $LITERAL_class, $ELLIPSIS, $IDENT, $ASSIGNEQUAL);}
    ;

template_parameter_type
@init                                                                           {if(state.backtracking == 0){action.template_declaration(input.LT(1));}}
    :                                                                           
        LITERAL_template                                                        {action.template_declaration(action.TEMPLATE_DECLARATION__TEMPLATE, $LITERAL_template);}
        LESSTHAN                                                                {action.template_declaration(action.TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
        template_parameter_list 
        GREATERTHAN                                                             {action.template_declaration(action.TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
;
finally                                                                         {if(state.backtracking == 0){action.end_template_declaration(input.LT(0));}}

simple_template_id
    :
        IDENT                                                                   {action.simple_template_id($IDENT);}
        LESSTHAN { (action.identifier_is(IDT_TEMPLATE_NAME, $IDENT)) }?         {action.simple_template_id(action.SIMPLE_TEMPLATE_ID__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
        template_argument_list? 
        GREATERTHAN                                                             {action.simple_template_id(action.SIMPLE_TEMPLATE_ID__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
    ;
lookup_simple_template_id
    :
        IDENT LESSTHAN { (action.identifier_is(IDT_TEMPLATE_NAME, $IDENT)) }?
            look_after_tmpl_args
    ;

simple_template_id_nocheck
    :
        IDENT                                                                   {action.simple_template_id_nocheck($IDENT);}
        LESSTHAN                                                                {action.simple_template_id_nocheck(action.SIMPLE_TEMPLATE_ID_NOCHECK__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
        template_argument_list? 
        GREATERTHAN                                                             {action.simple_template_id_nocheck(action.SIMPLE_TEMPLATE_ID_NOCHECK__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
    ;
lookup_simple_template_id_nocheck
    :
        IDENT LESSTHAN look_after_tmpl_args
    ;

simple_template_id_or_IDENT
    :
        ( (IDENT LESSTHAN) => 
                ( { action.identifier_is(IDT_TEMPLATE_NAME, input.LT(1)) }? =>
                IDENT                                                           {action.simple_template_id_or_ident(input.LT(0));}
                LESSTHAN                                                        {action.simple_template_id_or_ident(action.SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
                template_argument_list?
                GREATERTHAN                                                     {action.simple_template_id_or_ident(action.SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
            |   
                IDENT                                                           {action.simple_template_id_or_ident(input.LT(0));}
            )
        |   
            IDENT                                                               {action.simple_template_id_or_ident(input.LT(0));}
        )
    ;

simple_template_id_or_IDENT_nested [Token t]
    :
        ( (IDENT LESSTHAN) => 
                ( { action.identifier_is(IDT_TEMPLATE_NAME, t) }? =>
                IDENT                                                           {action.simple_template_id_or_ident(input.LT(0));}
                LESSTHAN                                                        {action.simple_template_id_or_ident(action.SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST, $LESSTHAN);}
                template_argument_list?
                GREATERTHAN                                                     {action.simple_template_id_or_ident(action.SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST, $GREATERTHAN);}
            |   
                IDENT                                                           {action.simple_template_id_or_ident(input.LT(0));}
            )
        |   
            IDENT                                                               {action.simple_template_id_or_ident(input.LT(0));}
        )
    ;

lookup_simple_template_id_or_IDENT
    :
        IDENT
        ( { (identifier_is(IDT_TEMPLATE_NAME)) }?=>
            LESSTHAN look_after_tmpl_args
        )?
    ;

/*
 * original rule:
template_name:
        IDENT
    ;
 * not needed
 */

template_argument_list
@init                                                                           {if(state.backtracking == 0){action.template_argument_list(input.LT(1));}}
    :                                                                           
        template_argument 
        (
            ELLIPSIS                                                            {action.template_argument_list(action.TEMPLATE_ARGUMENT_LIST__ELLIPSIS, input.LT(0));}
        )? 
        ( 
            COMMA                                                               {action.template_argument_list(action.TEMPLATE_ARGUMENT_LIST__COMMA, input.LT(0));}
            template_argument 
            (
                ELLIPSIS                                                        {action.template_argument_list(action.TEMPLATE_ARGUMENT_LIST__ELLIPSIS, input.LT(0));}
            )? 
        )*                                                                      
    ;
finally                                                                         {if(state.backtracking == 0){action.end_template_argument_list(input.LT(0));}}

template_argument
@init                                                                           {if(state.backtracking == 0){action.template_argument(input.LT(1));}}
    :                                                                           
    (
        {(isTemplateTooDeep(1, 10))}? 
        (~(GREATERTHAN | LESSTHAN | RCURLY | LCURLY))* 
        (
            lazy_template 
            (~(GREATERTHAN | LESSTHAN | RCURLY | LCURLY | COMMA | ELLIPSIS))*
        )+
    |
        // id_exression is included into assignment_expression, thus we need to explicitly rule it up
        (id_expression ELLIPSIS? (COMMA | GREATERTHAN))=> id_expression
    |
        (type_id)=> type_id
    |
        assignment_expression
    )                                                                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_template_argument(input.LT(0));}}

explicit_instantiation [decl_kind kind]
@init                                                                           {if(state.backtracking == 0){action.explicit_instantiation(input.LT(1));}}
    :                                                                           
        (
            LITERAL_extern                                                      {action.explicit_instantiation(action.EXPLICIT_INSTANTIATION__EXTERN, input.LT(0));}
        )? 
        LITERAL_template                                                        {action.explicit_instantiation(action.EXPLICIT_INSTANTIATION__TEMPLATE, input.LT(0));}
        declaration[kind]                                                       
    ;
finally                                                                         {if(state.backtracking == 0){action.end_explicit_instantiation(input.LT(0));}}

explicit_specialization [decl_kind kind]
    :                                                                           
        LITERAL_template LESSTHAN GREATERTHAN                                   {action.explicit_specialization($LITERAL_template, $LESSTHAN, $GREATERTHAN);}
        declaration[kind]                                                       {action.end_explicit_specialization(input.LT(0));}
    ;

// [gram.except] 
try_block
@init                                                                           {if(state.backtracking == 0){action.try_block(input.LT(1));}}
    :                                                                           
        LITERAL_try compound_statement[false] 
        handler[false]+                                                         
    ;
finally                                                                         {if(state.backtracking == 0){action.end_try_block(input.LT(0));}}

function_try_block[boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.function_try_block(input.LT(1));}}
    :                                                                           
        LITERAL_try ctor_initializer? function_body[class_late_binding] 
        handler[class_late_binding]+                                     
    ;
finally                                                                         {if(state.backtracking == 0){action.end_function_try_block(input.LT(0));}}

handler[boolean class_late_binding]
@init                                                                           {if(state.backtracking == 0){action.handler(input.LT(1));}}
    :                                                                           
        LITERAL_catch 
        LPAREN                                                                  {action.handler(action.HANDLER__LPAREN, input.LT(0));}
        exception_declaration 
        RPAREN                                                                  {action.handler(action.HANDLER__RPAREN, input.LT(0));}
        compound_statement[class_late_binding]                           
    ;
finally                                                                         {if(state.backtracking == 0){action.end_handler(input.LT(0));}}

/*
 * original rule:
exception_declaration:
        type_specifier+ declarator
    |
        type_specifier+ abstract_declarator?
    |
        ELLIPSIS
    ;

 * Ambiguity in declarator vs abstract_declarator resolved by moving it into universal_declarator
 */
exception_declaration
scope Declaration;
@init { init_declaration(CTX, blockscope_decl); }
    :
        attribute_specifiers? type_specifier+ universal_declarator?
    |
        ELLIPSIS
    ;
throw_expression:
        LITERAL_throw assignment_expression? 
    ;
exception_specification:
        dynamic_exception_specification gnu_attribute_or_extension_specifiers?
    |
        noexcept_specification gnu_attribute_or_extension_specifiers?
    ;
dynamic_exception_specification:
        LITERAL_throw LPAREN type_id_list? RPAREN 
    ;
type_id_list:
        type_id ELLIPSIS? ( COMMA type_id ELLIPSIS? )*
    ;
noexcept_specification:
        LITERAL_noexcept //(LPAREN constant_expression RPAREN)?
    ;

// EXPRESSIONS
// [gram.expr]
primary_expression:
        literal
    |
        LITERAL_this
    |
        LPAREN expression RPAREN 
    |
        id_expression 
    |
        lambda_expression
    ;

lambda_expression:
        lambda_introduser lambda_declarator? compound_statement[false]
    ;

lambda_introduser:
        LSQUARE lambda_capture? RSQUARE
    ;
lambda_capture:
        capture_default (COMMA capture_list)?
    |
        capture_list
    ;
capture_default:
        AMPERSAND
    |
        ASSIGNEQUAL
    ;
capture_list:
        capture ELLIPSIS? (COMMA capture ELLIPSIS?)*
    ;
capture:
        IDENT
    |
        AMPERSAND IDENT
    |
        LITERAL_this
    ;
lambda_declarator:
        parameters LITERAL_mutable? exception_specification? trailing_return_type?
    ;

/*
 * original rule:
postfix_expression:
        primary_expression
    |
        postfix_expression LSQUARE expression RSQUARE
    |
        postfix_expression LPAREN expression_list? RPAREN
    |
        simple_type_specifier LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier IDENT LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier LITERAL_template? template_id LPAREN expression_list? RPAREN
    |
        postfix_expression DOT LITERAL_template? id_expression
    |
        postfix_expression POINTERTO LITERAL_template? id_expression
    |
        postfix_expression DOT pseudo_destructor_name
    |
        postfix_expression POINTERTO pseudo_destructor_name
    |
        postfix_expression PLUSPLUS
    |
        postfix_expression MINUSMINUS
    |
        dynamic_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        static_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        reinterpret_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        const_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        typeid LPAREN expression RPAREN
    |
        typeid LPAREN type_id RPAREN
    ;
/*
 * Left recursion removed by moving non-recursive into basic_postfix_expression and applying "recursive"
 * parts by a loop on top of it.
 *
 * "pseudo-destructor-name" thing is heavily conflicting with id_expression,
 * so it does not make any sense to introduce. This means that id_expression should
 * allow everything pseudo-destructor-name allows, and then be semantically checked later.
 */
postfix_expression:
        basic_postfix_expression
        (
            LSQUARE expression RSQUARE
        |
            LPAREN expression_list? RPAREN
        |
            DOT
            (
                LITERAL_template? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            POINTERTO
            (
                LITERAL_template? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            PLUSPLUS
        |
            MINUSMINUS
        )*
    ;

basic_postfix_expression:
        primary_expression
    |
        simple_type_specifier LPAREN expression_list? RPAREN
    |
        LITERAL_typename SCOPE? nested_name_specifier (
            IDENT LPAREN expression_list? RPAREN
        |
            LITERAL_template? simple_template_id LPAREN expression_list? RPAREN
        )
    |
        LITERAL_dynamic_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_static_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_reinterpret_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        LITERAL_const_cast LESSTHAN type_id GREATERTHAN LPAREN expression RPAREN
    |
        // AMB
        // expression and type_id conflict in "simple_type_specifier"
        // rule up type_id, as it should be easier to check
        LITERAL_typeid LPAREN ( (type_id)=> type_id |  expression ) RPAREN
    ;

expression_list:
        initializer_list
    ;
/*
 * original rule:
pseudo_destructor_name:
        SCOPE? nested_name_specifier? type_name SCOPE TILDE type_name
    |
        SCOPE? nested_name_specifier LITERAL_template simple_template_id SCOPE TILDE type_name
    |
        SCOPE? nested_name_specifier? TILDE type_name
    ;

 * A healthy dose of left-factoring solves the issue.
 *
 * This rule is not used anymore

pseudo_destructor_name:
        SCOPE?
        (
            nested_name_specifier? LITERAL_template? IDENT SCOPE TILDE IDENT
        |
            nested_name_specifier LITERAL_template simple_template_id SCOPE TILDE IDENT
        )
    ;
 *
 */

/*
 * ambiguity between postfix_expression and new/delete_expression caused by presence of
 * id_expression in a former alternative and is problematic to resolve.
 * For now just synpred on new/delete. Reconsider if it appears to be costly.
 *
 * As per 2003 standard:
 * "There is an ambiguity in the unary-expression ~X(), where X is a class-name.
 * The ambiguity is resolved in favor of treating ~ as a unary complement rather than
 * treating ~X as referring to a destructor."
 */
unary_expression:
       (TILDE cast_expression)=>
             TILDE cast_expression
    |
        (new_expression)=>
            new_expression
    |
        (delete_expression)=>
            delete_expression
    |
        (type_trait_literal)=>
            type_trait_expression
    |
        postfix_expression
    |
        PLUSPLUS cast_expression
    |
        MINUSMINUS cast_expression
    |
        unary_operator_but_not_TILDE cast_expression
    |
        noexcept_expression
    ;

unary_operator:
        unary_operator_but_not_TILDE | TILDE
    ;
unary_operator_but_not_TILDE:
        STAR | AMPERSAND | PLUS | MINUS | NOT
    ;

/*
 * original rule:

new_expression:
        SCOPE? LITERAL_new new_placement? new_type_id new_initializer? 
    |
        SCOPE? LITERAL_new new_placement? LPAREN type_id RPAREN new_initializer? 
    ;

 *
 * Complication appears due to the optional new_placement and (type_id).
 * Unhealthy dose of left-factoring solves this issue.
 */
new_expression:
        SCOPE? LITERAL_new
        (
            new_placement ( new_type_id | LPAREN type_id RPAREN )
        |
            (LPAREN type_id RPAREN)=>
                LPAREN type_id RPAREN
        |
            new_type_id
        ) new_initializer?
    ;

new_placement:
        LPAREN expression_list RPAREN 
    ;

/*
 * As per 2003 standard:
 * "The new-type-id in a new-expression is the longest possible sequence of new-declarators"
 *
 * As all the ambiguities in new_type_id seem to come from new_declarator's ptr_operator
 * force it by synpreds.
 *
 * Is this resolution correct??
 *  new (int(*p)) int; // new-placement expression
 */
new_type_id:
        type_specifier
        (type_specifier)*
        ((LSQUARE | ptr_operator)=>
            new_declarator)?
    ;

new_declarator:
        (ptr_operator)=>
            ptr_operator new_declarator
    |
        direct_new_declarator
    ;

direct_new_declarator:
        LSQUARE expression RSQUARE ( LSQUARE constant_expression RSQUARE )*
    ;

new_initializer:
        LPAREN expression_list? RPAREN
    |
        braced_init_list
    ;
delete_expression:
        SCOPE? LITERAL_delete 
        (
            (LSQUARE RSQUARE) => LSQUARE RSQUARE cast_expression
        |
            cast_expression
        )
    ;
noexcept_expression:
        LITERAL_noexcept LPAREN expression RPAREN
    ;
cast_expression :
        (LPAREN type_id RPAREN)=>
            LPAREN type_id RPAREN cast_expression
    |
        unary_expression
    ;

pm_expression :
        cast_expression ( DOTMBR cast_expression | POINTERTOMBR cast_expression ) *
    ;

multiplicative_expression:
        pm_expression
        (
            STAR pm_expression
        |
            DIVIDE pm_expression
        |
            MOD pm_expression
        )*
    ;

additive_expression:
        multiplicative_expression ( PLUS multiplicative_expression | MINUS multiplicative_expression )*
    ;

shift_expression:
        additive_expression 
        (
            (
                SHIFTLEFT 
            | 
                shiftright_literal
            )
            additive_expression
        )*
    ;

/*
 * GREATERTHAN ambiguity (GREATERTHAN in relational expression vs GREATERTHAN closing template arguments list) is one of
 * C++ dumbest ambiguities. Resolve it by tracking whether expression is a top-level expression (e.g. not
 * parenthesized) and parsed in a context of template argument - then do not accept is as a continuation of
 * relational expression.
 */
relational_expression:
        shift_expression
        ( 
            { !action.top_level_of_template_arguments() }?=>
            GREATERTHAN shift_expression
          |
            LESSTHAN shift_expression
          |
            LESSTHANOREQUALTO shift_expression
          |
            GREATERTHANOREQUALTO shift_expression
        )*
    ;
equality_expression:
        relational_expression ( EQUAL relational_expression | NOTEQUAL relational_expression)*
    ;
and_expression:
        equality_expression ( AMPERSAND equality_expression )*
    ;
exclusive_or_expression:
        and_expression ( BITWISEXOR and_expression )*
    ;
inclusive_or_expression:
        exclusive_or_expression ( BITWISEOR exclusive_or_expression )*
    ;
logical_and_expression:
        inclusive_or_expression ( AND inclusive_or_expression )*
    ;
logical_or_expression:
        logical_and_expression ( OR logical_and_expression )*
    ;
conditional_expression:
        logical_or_expression (QUESTIONMARK expression COLON assignment_expression)?
    |
        QUESTIONMARK expression COLON assignment_expression
    ;
/*
 * These are the example of "precedence climbing" implementation
 *

binary_operator returns [ int prec]:
        PLUS| MINUS |
        STAR | DIVIDE | MOD | BITWISEXOR | AMPERSAND | BITWISEOR |
        NOT | LESSTHAN | GREATERTHAN |
        SHIFTLEFT | SHIFTRIGHT |
        EQUAL | NOTEQUAL | LESSTHANOREQUALTO | GREATERTHANOREQUALTO | AND | OR
    ;

fast_expression:
        climbing_expression[0]
    ;

climbing_expression [int prio]:
        primary_climbing
        ((binary_operator { $binary_operator.prec >= prio }? )=>
         binary_operator  climbing_expression[$binary_operator.prec+1])?
    ;

primary_climbing:
        unary_operator climbing_expression[$unary_operator.prec]
    ;
*/

/*
 * original rule:

assignment_expression:
        conditional_expression 
    |
        logical_or_expression assignment_operator assignment_expression 
    |

 * Ambiguity on logical_or_expression in assignment vs conditional_expression.
 * Resolved by unpretty rule-splitting and left-factoring.
 */
assignment_expression
@init                                                                           {if(state.backtracking == 0){action.assignment_expression(input.LT(1));}}
    :
        // this is taken from conditional_expression
        QUESTIONMARK expression COLON assignment_expression
    |
        logical_or_expression (
            // this is taken from conditional_expression
            (QUESTIONMARK expression COLON assignment_expression)?
        |
            assignment_operator assignment_expression
        )
    |
        throw_expression
    ;
finally                                                                         {if(state.backtracking == 0){action.end_assignment_expression(input.LT(0));}}

assignment_operator:
        ASSIGNEQUAL | TIMESEQUAL | DIVIDEEQUAL | MODEQUAL | PLUSEQUAL | MINUSEQUAL | SHIFTRIGHTEQUAL | SHIFTLEFTEQUAL |
        BITWISEANDEQUAL | BITWISEXOREQUAL | BITWISEOREQUAL
    ;

expression
@init                                                                           {if(state.backtracking == 0){action.expression(input.LT(1));}}
    :
        assignment_expression ( COMMA assignment_expression )*
    ;
finally                                                                         {if(state.backtracking == 0){action.end_expression(input.LT(0));}}

constant_expression returns [ expression_t expr ]
@init                                                                           {if(state.backtracking == 0){action.constant_expression(input.LT(1));}}
    :
        conditional_expression
    ;
finally                                                                         {if(state.backtracking == 0){action.end_constant_expression(input.LT(0));}}


type_trait_expression
    :
        type_trait_literal 
        (
            (LPAREN type_id RPAREN)=>
                LPAREN type_id RPAREN
        |
            unary_expression
        )
    ;

type_trait_literal
    :
        LITERAL_sizeof | compiler_specific_type_trait_literal
    ;

compiler_specific_type_trait_literal
    :        
        LITERAL___is_pod | LITERAL___has_nothrow_assign | LITERAL___has_nothrow_copy | LITERAL___has_nothrow_constructor |
        LITERAL___has_trivial_assign | LITERAL___has_trivial_copy | LITERAL___has_trivial_destructor | LITERAL___has_virtual_destructor |
        LITERAL___is_abstract | LITERAL___is_empty | LITERAL___is_literal_type | LITERAL___is_polymorphic |
        LITERAL___is_standard_layout | LITERAL___is_trivial | LITERAL___is_union | LITERAL___underlying_type | 
        LITERAL___is_class
    ;

shiftright_literal
    :
        SHIFTRIGHT
    |
        // check if we have special split SHIFTRIGHT token (it has empty name as marker)
        {input.LA(1) == GREATERTHAN && input.LT(1).getText().equals("")}?=> 
            // in this case first token has empty text and the second token is a FilterToken with the link to original and text from original
            GREATERTHAN GREATERTHAN // if hook is expected to be called here => don't forget to pass SHIFT instead of two ">" tokens
    ;

// [gram.lex]

literal
    :
    DECIMALINT|HEXADECIMALINT|FLOATONE|CHAR_LITERAL|adjacent_string_literals|NUMBER|OCTALINT|LITERAL_true|LITERAL_false|compiler_specific_literal
    ;

adjacent_string_literals
    :
        STRING_LITERAL+
    ;

compiler_specific_literal
    :
        LITERAL___null
    ;

// lookahead stuff
// token list arg_syms from parseutil.cc, to implement look_after_tmpl_args

// $<Look ahead

lookahead_tokenset_arg_syms
    :
        IDENT|DECIMALINT|HEXADECIMALINT|FLOATONE|CHAR_LITERAL|STRING_LITERAL|NUMBER|OCTALINT|
        PLUS|MINUS|STAR|AMPERSAND|LITERAL_sizeof|TILDE|
        NOT|PLUSPLUS|MINUSMINUS|LITERAL_OPERATOR|LITERAL_new|LITERAL_delete|
        LITERAL_this|
        LITERAL_void|LITERAL_char|LITERAL_short|LITERAL_long|LITERAL_float|LITERAL_double|LITERAL_signed|LITERAL_unsigned|LITERAL_int|
        DIVIDE|SHIFTLEFT|SHIFTRIGHT|BITWISEOR|AND|OR|BITWISEXOR|
        EQUAL|LESSTHANOREQUALTO|GREATERTHANOREQUALTO|NOTEQUAL|
        ASSIGNEQUAL|BITWISEANDEQUAL|DIVIDEEQUAL|SHIFTLEFTEQUAL|SHIFTRIGHTEQUAL|MINUSEQUAL|PLUSEQUAL|
        MODEQUAL|TIMESEQUAL|BITWISEOREQUAL|BITWISEXOREQUAL|DOT|MOD|
        POINTERTO|QUESTIONMARK|COLON|SCOPE|DOTMBR|POINTERTOMBR|COMMA|ELLIPSIS|
        LITERAL_typedef|LITERAL_extern|LITERAL_static|LITERAL_auto|LITERAL_register|LITERAL___thread|
        literal_const|LITERAL_volatile|LITERAL_struct|LITERAL_union|LITERAL_class|LITERAL_enum|LITERAL_typename|
        LITERAL___offsetof|LITERAL___alignof|LITERAL_throw|LITERAL_wchar_t|LITERAL_typeid|
        LITERAL_const_cast|LITERAL_static_cast|LITERAL_dynamic_cast|LITERAL_reinterpret_cast|
        LITERAL_bool|LITERAL_true|LITERAL_false|
        LITERAL___global|LITERAL___symbolic|LITERAL___hidden|LITERAL___declspec|
        LITERAL___attribute__|LITERAL___typeof__|
        IS_ENUM|IS_UNION|IS_CLASS|IS_POD|IS_ABSTRACT|HAS_VIRT_DESTR|IS_EMPTY|IS_BASEOF|IS_POLYMORPH
    ;

look_after_tmpl_args
scope {
    int level;
}
@init{ 
    $look_after_tmpl_args::level = 0;
    int npar = 0;
    int nbrac = 0;
}
    :
        (
            // this gets us out if GREATERTHAN is met when level == 0
            (GREATERTHAN {
                    ($look_after_tmpl_args::level > 0)
                  }? )=>
            GREATERTHAN
                {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level--;
                            println("level-- (", $look_after_tmpl_args::level);
                        }
                }}
        |
            LESSTHAN {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level++;
                            println("level++ (", $look_after_tmpl_args::level);
                    }
                }}
        |
            LPAREN {{ npar++; }}
        |
            RPAREN {{ if (npar > 0) npar--; }}
        |
            LSQUARE {{ nbrac++; }}
        |
            RSQUARE {{ if (nbrac > 0) nbrac--; }}
        |
            lookahead_tokenset_arg_syms
        )* GREATERTHAN
    ;

skip_balanced_Curl
            :
            LCURLY                                                              {if(state.backtracking == 0){action.skip_balanced_curlies(input.LT(0));}}
            (options {greedy=false;}:
                skip_balanced_Curl | ~(RCURLY | LCURLY)                         {if(state.backtracking == 0){action.skip_balanced_curlies(input.LT(0));}}
            )*
            RCURLY                                                              {if(state.backtracking == 0){action.skip_balanced_curlies(input.LT(0));}}
        ;



lazy_template
    :
        LESSTHAN
        (
            (   ~(GREATERTHAN | LESSTHAN | RCURLY | LCURLY)
            |   lazy_template
            )*
        )
        GREATERTHAN
    ;

protected
literal_asm : LITERAL_asm | LITERAL__asm | LITERAL___asm|LITERAL___asm__;

protected
literal_cdecl : LITERAL__cdecl | LITERAL___cdecl;

protected
literal_const : LITERAL_const | LITERAL___const | LITERAL___const__;

protected
literal_declspec : LITERAL__declspec | LITERAL___declspec;

protected
literal_far : LITERAL__far | LITERAL___far;

protected
literal_inline : LITERAL_inline | LITERAL__inline | LITERAL___inline | LITERAL___inline__ | LITERAL___forceinline;

protected
literal_int64 : LITERAL__int64 | LITERAL___int64;

protected
literal_signed: LITERAL_signed | LITERAL___signed | LITERAL___signed__;

protected
literal_unsigned: LITERAL_unsigned | LITERAL___unsigned__;

protected
literal_near : LITERAL__near | LITERAL___near;

protected
literal_pascal : LITERAL_pascal | LITERAL__pascal | LITERAL___pascal;

protected
literal_stdcall : LITERAL__stdcall | LITERAL___stdcall;

protected
literal_clrcall : LITERAL___clrcall;

protected
literal_volatile : LITERAL_volatile | LITERAL___volatile | LITERAL___volatile__;

protected
literal_typeof : LITERAL_typeof | LITERAL___typeof | LITERAL___typeof__ ;

protected
literal_restrict : LITERAL_restrict | LITERAL___restrict | LITERAL___restrict__;

protected
literal_complex : LITERAL__Complex | LITERAL___complex__ | LITERAL___complex;

protected
literal_attribute : LITERAL___attribute | LITERAL___attribute__;

protected
literal_try : LITERAL_try | LITERAL___try;

protected
literal_finally : LITERAL___finally;

protected
literal_decltype : LITERAL_decltype | LITERAL___decltype;

// $>
// ==============
//

// LITERAL_template: 'template';
// COLON: ':'; SCOPE: '::';

// DOT: '.'; ELLIPSIS: '...';
// MINUS: '-'; PLUS: '+'; MINUSMINUS: '--'; PLUSPLUS: '++'; PLUSEQUAL: '+='; MINUSEQUAL: '-=';

// POINTERTO: '->'; 
// STAR: '*'; TIMESEQUAL: '*='; DOTMBR: '.*'; POINTERTOMBR: '->*';
// DIVIDE: '/'; DIVIDEEQUAL: '/=';
// MOD: '%'; MODEQUAL: '%=';
// NOT: '!';
// ASSIGNEQUAL: '=';
// EQUAL: '=='; LESSTHANOREQUALTO: '<='; GREATERTHANOREQUALTO: '>='; NOTEQUAL: '!=';
// AMPERSAND: '&'; AND: '&&'; BITWISEANDEQUAL: '&=';
// BITWISEOR: '|'; OR: '||'; BITWISEOREQUAL: '|=';
// BITWISEXOREQUAL: '^='; BITWISEXOR: '^'; 
// SHIFTLEFT: '<<'; SHIFTRIGHT: '>>'; SHIFTLEFTEQUAL: '<<='; SHIFTRIGHTEQUAL: '>>=';
// LITERAL_this: 'this';
// LITERAL_typename: 'typename';
// LITERAL_typeid: 'typeid';
// LPAREN: '('; RPAREN: ')';
// LSQUARE: '['; RSQUARE: ']';
// LCURLY: '{'; RCURLY: '}';
// LESSTHAN: '<'; GREATERTHAN: '>';

// LITERAL_char: 'char';
// LITERAL_wchar_t: 'wchar_t';
// LITERAL_bool: 'bool'; 
// LITERAL_true: 'true';
// LITERAL_false: 'false';
// LITERAL_short: 'short';
// LITERAL_int: 'int';
// LITERAL_long: 'long';
// LITERAL_signed: 'signed';
// LITERAL_unsigned: 'unsigned';
// LITERAL_float: 'float';
// LITERAL_double: 'double';
// LITERAL_void: 'void';

// LITERAL_enum: 'enum'; LITERAL_class: 'class'; LITERAL_struct: 'struct'; LITERAL_union: 'union';

// LITERAL_dynamic_cast: 'dynamic_cast';
// LITERAL_static_cast: 'static_cast';
// LITERAL_reinterpret_cast: 'reinterpret_cast';
// LITERAL_const_cast: 'const_cast';

// COMMA: ',';
// TILDE: '~';

// LITERAL_new: 'new'; LITERAL_delete: 'delete';
// LITERAL_namespace: 'namespace'; LITERAL_using: 'using';

// LITERAL_OPERATOR: 'operator';

// LITERAL_friend: 'friend';
// LITERAL_typedef: 'typedef';
// LITERAL_auto: 'auto';
// LITERAL_register: 'register';
// LITERAL_static: 'static';
// LITERAL_extern: 'extern';
// LITERAL_mutable: 'mutable';
// LITERAL_inline: 'inline';
// LITERAL_virtual: 'virtual';
// LITERAL_explicit: 'explicit';
// LITERAL_export: 'export';
// LITERAL_private: 'private';
// LITERAL_protected: 'protected';
// LITERAL_public: 'public';
// SEMICOLON: ';';

// LITERAL_try: 'try'; LITERAL_catch: 'catch'; LITERAL_throw: 'throw';

// LITERAL_const: 'const'; LITERAL_volatile: 'volatile';
// LITERAL_asm: 'asm';
// LITERAL_break: 'break'; LITERAL_continue: 'continue'; LITERAL_return: 'return';

// LITERAL_goto: 'goto';
// LITERAL_for: 'for'; LITERAL_while: 'while'; LITERAL_do: 'do';
// LITERAL_if: 'if'; LITERAL_else: 'else';
// LITERAL_switch: 'switch'; LITERAL_case: 'case'; LITERAL_default: 'default';

// QUESTIONMARK: '?'; LITERAL_sizeof: 'sizeof';
// LITERAL___offsetof: '__offsetof';
// LITERAL___thread: '__thread';

// LITERAL___global: '__global';
// LITERAL___symbolic: '__symbolic';
// LITERAL___hidden: '__hidden';

// LITERAL___declspec: '__declspec';
// LITERAL___attribute__: '__attribute__';
// LITERAL___typeof__: '__typeof__';
// LITERAL___alignof: '__alignof';

// LITERAL__Pragma: '_Pragma';

// HAS_TRIVIAL_DESTR: '__oracle_has_trivial_destructor';
// HAS_VIRTUAL_DESTR: '__oracle_has_virtual_destructor';
// IS_ENUM: '__oracle_is_enum';
// IS_UNION: '__oracle_is_union';
// IS_CLASS: '__oracle_is_class';
// IS_POD: '__oracle_is_pod';
// IS_ABSTRACT: '__oracle_is_abstract';
// IS_EMPTY: '__oracle_is_empty';
// IS_POLYMORPH: '__oracle_is_polymorphic';
// IS_BASEOF: '__oracle_is_base_of';

// CHAR_LITERAL
//     :   '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
//     ;

// STRING
//     :  '"' STRING_GUTS '"'
//     ;

// fragment
// STRING_GUTS :	( EscapeSequence | ~('\\'|'"') )* ;

// fragment
// HEX_LITERAL : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

// fragment
// DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

// fragment
// OCTAL_LITERAL : '0' ('0'..'7')+ IntegerTypeSuffix? ;

// fragment
// HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

// fragment
// IntegerTypeSuffix
//     :	('l'|'L')
//     |	('u'|'U')  ('l'|'L')?
//     ;

// fragment
// Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// fragment
// FloatTypeSuffix : ('f'|'F'|'d'|'D') ;

// fragment
// EscapeSequence
//     :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
//     |   OctalEscape
//     ;

// fragment
// OctalEscape
//     :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7')
//     ;

// fragment
// UnicodeEscape
//     :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
//     ;

// WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
//     ;

// COMMENT
//     :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
//     ;

// LINE_COMMENT
//     : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
//     ;

// LITERAL:
//         HEX_LITERAL
//     |
//         DECIMAL_LITERAL
//     |
//         OCTAL_LITERAL
//     ;

// IDENT
//     :	LETTER (LETTER|'0'..'9')*
//     ;
	
// fragment
// LETTER
//     :	'$'
//     |	'A'..'Z'
//     |	'a'..'z'
//     |	'_'
//     ;

/*END*/

