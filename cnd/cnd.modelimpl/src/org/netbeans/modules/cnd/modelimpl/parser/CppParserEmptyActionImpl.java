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
package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 */
public class CppParserEmptyActionImpl implements CppParserActionEx {
    private final Deque<CsmFile> files;

    CppParserEmptyActionImpl(CsmFile file) {
        files = new ArrayDeque<>();
        files.push(file);
    }
    
    CppParserEmptyActionImpl() {
        files = new ArrayDeque<>();
    }

    @Override
    public boolean type_specifier_already_present(TokenStream input) {        
        return true;
    }
    
    @Override
    public void enum_declaration(Token token) {
    }

    @Override
    public void enum_strongly_typed(Token token) {
    }

    @Override
    public void enum_name(Token token) {
    }

    @Override
    public void enum_body(Token token) {
    }

    @Override
    public void enumerator(Token token) {
    }

    @Override
    public void end_enum_body(Token token) {
    }

    @Override
    public void end_enum_declaration(Token token) {
    }

    @Override
    public void class_name(Token token) {
    }

    @Override
    public void class_body(Token token) {
    }

    @Override
    public void end_class_body(Token token) {
    }

    @Override
    public void namespace_body(Token token) {
    }

    @Override
    public void end_namespace_body(Token token) {
    }

    @Override
    public void compound_statement(Token token) {
    }

    @Override
    public void end_compound_statement(Token token) {
    }

    @Override
    public void id(Token token) {
    }

    @Override
    public boolean isType(String name) {
        return false;
    }

    @Override
    public void namespace_declaration(Token token) {
    }

    @Override
    public void end_namespace_declaration(Token token) {
    }

    @Override
    public void namespace_name(Token token) {
    }

    @Override
    public void class_declaration(Token token) {
    }

    @Override
    public void end_class_declaration(Token token) {
    }

    @Override
    public void class_kind(Token token) {
    }

    @Override
    public void simple_type_id(Token token) {
    }

    @Override
    public void pushFile(CsmFile file) {
        files.push(file);
    }

    @Override
    public CsmFile popFile() {
        CsmFile out = files.peek();
        files.pop();
        return out;
    }

    @Override
    public void simple_type_specifier(Token token) {
    }

    @Override
    public void simple_type_specifier(int kind, Token token) {
    }

    @Override
    public void end_simple_type_specifier(Token token) {
    }
    
    @Override
    public void nested_name_specifier(Token token) {
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
    }
    
    @Override
    public void simple_template_id(Token token) {
    }

    @Override
    public void simple_template_id(int kind, Token token) {
    }
    
    @Override
    public void simple_declaration(Token token) {
    }

    @Override
    public void end_simple_declaration(Token token) {
    }

    @Override
    public void decl_specifier(int kind, Token token) {
    }

    @Override
    public void simple_template_id_or_ident(Token token) {
    }

    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
    }

    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
    }
    
    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3, Token token4) {
    }

    @Override
    public void elaborated_type_specifier(Token token) {
    }    
    
    @Override
    public void using_declaration(Token token) {
    }
    
    @Override
    public void parameter_declaration_list() {
    }

    @Override
    public void end_parameter_declaration_list() {
    }

    @Override
    public void decl_specifiers(Token token) {
    }

    @Override
    public void end_decl_specifiers(Token token) {
    }

    @Override
    public boolean identifier_is(int kind, Token token) {
        return true;
    }

    @Override
    public boolean top_level_of_template_arguments() {
        return true;
    }

    @Override
    public void template_declaration(int kind, Token token) {
    }

    @Override
    public void using_directive(Token usingToken, Token namespaceToken) {
    }

    @Override
    public void using_directive(int kind, Token token) {
    }

    @Override
    public void end_using_directive(Token semicolonToken) {
    }

    @Override
    public void using_declaration(int kind, Token token) {
    }

    @Override
    public void end_using_declaration(Token semicolonToken) {
    }

    @Override
    public void namespace_alias_definition(Token namespaceToken, Token identToken, Token assignequalToken) {
    }

    @Override
    public void end_namespace_alias_definition(Token semicolonToken) {
    }

    @Override
    public void qualified_namespace_specifier(int kind, Token token) {
    }

    @Override
    public void simple_declaration(int kind, Token token) {
    }

    @Override
    public void greedy_declarator() {
    }

    @Override
    public void end_greedy_declarator() {
    }

    @Override
    public void declarator_id() {
    }

    @Override
    public void end_declarator_id() {
    }
 
    @Override public void translation_unit(Token token) {}
    @Override public void end_translation_unit(Token token) {}
    @Override public void statement(Token token) {}
    @Override public void end_statement(Token token) {}
    @Override public void labeled_statement(Token token) {}
    @Override public void labeled_statement(int kind, Token token) {}
    @Override public void labeled_statement(int kind, Token token1, Token token2) {}
    @Override public void end_labeled_statement(Token token) {}
    @Override public void expression_statement(Token token) {}
    @Override public void end_expression_statement(Token token) {}
    @Override public void selection_statement(Token token) {}
    @Override public void selection_statement(int kind, Token token) {}
    @Override public void end_selection_statement(Token token) {}
    @Override public void condition(Token token) {}
    @Override public void condition(int kind, Token token) {}
    @Override public void end_condition(Token token) {}
    @Override public void condition_declaration(Token token) {}
    @Override public void end_condition_declaration(Token token) {}
    @Override public void condition_expression(Token token) {}
    @Override public void end_condition_expression(Token token) {}
    @Override public void iteration_statement(Token token) {}
    @Override public void iteration_statement(int kind, Token token) {}
    @Override public void end_iteration_statement(Token token) {}
    @Override public void for_init_statement(Token token) {}
    @Override public void end_for_init_statement(Token token) {}
    @Override public void for_range_declaration(Token token) {}
    @Override public void end_for_range_declaration(Token token) {}
    @Override public void for_range_initializer(Token token) {}
    @Override public void end_for_range_initializer(Token token) {}
    @Override public void jump_statement(Token token) {}
    @Override public void jump_statement(int kind, Token token) {}
    @Override public void jump_statement(int kind, Token token1, Token token2) {}
    @Override public void end_jump_statement(Token token) {}
    @Override public void declaration_statement(Token token) {}
    @Override public void end_declaration_statement(Token token) {}
    @Override public void declaration(Token token) {}
    @Override public void end_declaration(Token token) {}
    @Override public void block_declaration(Token token) {}
    @Override public void end_block_declaration(Token token) {}
    @Override public void id_expression(Token token) {}
    @Override public void end_id_expression(Token token) {}
    
    @Override public void tilde_class_name(Token token) {}
    @Override public void end_tilde_class_name(Token token) {}
    
    @Override public void alias_declaration(Token usingToken, Token identToken, Token assignequalToken) {}
    @Override public void end_alias_declaration(Token token) {}
    @Override public void function_specifier(int kind, Token token) {}
    @Override public void type_specifier(Token token) {}
    @Override public void end_type_specifier(Token token) {}
    @Override public void trailing_type_specifier(Token token) {}
    @Override public void end_trailing_type_specifier(Token token) {}
    @Override public void decltype_specifier(Token token) {}
    @Override public void decltype_specifier(int kind, Token token) {}
    @Override public void end_decltype_specifier(Token token) {}
    @Override public void end_elaborated_type_specifier(Token token) {}
    @Override public void typename_specifier(Token token) {}
    @Override public void end_typename_specifier(Token token) {}
    @Override public void asm_definition(Token asmToken, Token lparenToken, Token stringToken, Token rparenToken, Token semicolonToken) {}
    @Override public void linkage_specification(Token externToken, Token stringToken) {}
    @Override public void linkage_specification(int kind, Token token) {}
    @Override public void end_linkage_specification(Token token) {}
    @Override public void init_declarator_list(Token token) {}
    @Override public void init_declarator_list(int kind, Token token) {}
    @Override public void end_init_declarator_list(Token token) {}
    @Override public void init_declarator(Token token) {}
    @Override public void end_init_declarator(Token token) {}
    @Override public void declarator(Token token) {}
    @Override public void end_declarator(Token token) {}
    @Override public void noptr_declarator(Token token) {}
    @Override public void noptr_declarator(int kind, Token token) {}
    @Override public void end_noptr_declarator(Token token) {}
    @Override public void function_declarator(Token token) {}
    @Override public void end_function_declarator(Token token) {}
    @Override public void constructor_declarator(Token token) {}
    @Override public void end_constructor_declarator(Token token) {}
    @Override public void function_declarator(int kind, Token token) {}
    @Override public void noptr_abstract_declarator(Token token) {}
    @Override public void noptr_abstract_declarator(int kind, Token token) {}
    @Override public void end_noptr_abstract_declarator(Token token) {}
    @Override public void universal_declarator(Token token) {}
    @Override public void end_universal_declarator(Token token) {}
    @Override public void greedy_declarator(Token token) {}
    @Override public void end_greedy_declarator(Token token) {}
    @Override public void greedy_nonptr_declarator(Token token) {}
    @Override public void greedy_nonptr_declarator(int kind, Token token) {}
    @Override public void end_greedy_nonptr_declarator(Token token) {}
    @Override public void ptr_operator(Token token) {}
    @Override public void ptr_operator(int kind, Token token) {}
    @Override public void end_ptr_operator(Token token) {}
    @Override public void cv_qualifier(int kind, Token token) {}
    @Override public void ref_qualifier(int kind, Token token) {}
    @Override public void declarator_id(Token token) {}
    @Override public void declarator_id(int kind, Token token) {}
    @Override public void end_declarator_id(Token token) {}
    @Override public void type_id(Token token) {}
    @Override public void end_type_id(Token token) {}
    @Override public void parameters_and_qualifiers(Token token) {}
    @Override public void parameters_and_qualifiers(int kind, Token token) {}
    @Override public void end_parameters_and_qualifiers(Token token) {}
    @Override public void parameter_declaration_clause(Token token) {}
    @Override public void parameter_declaration_clause(int kind, Token token) {}
    @Override public void end_parameter_declaration_clause(Token token) {}
    @Override public void parameter_declaration_list(Token token) {}
    @Override public void end_parameter_declaration_list(int kind, Token token) {}
    @Override public void end_parameter_declaration_list(Token token) {}
    @Override public void parameter_declaration(Token token) {}
    @Override public void parameter_declaration(int kind, Token token) {}
    @Override public void end_parameter_declaration(Token token) {}
    @Override public void function_definition_after_declarator(Token token) {}
    @Override public void function_definition_after_declarator(int kind, Token token) {}
    @Override public void end_function_definition_after_declarator(Token token) {}
    @Override public void function_declaration(Token token) {}
    @Override public void end_function_declaration(Token token) {}
    @Override public void function_definition(Token token) {}
    @Override public void end_function_definition(Token token) {}
    @Override public void function_body(Token token) {}
    @Override public void end_function_body(Token token) {}
    @Override public void initializer(Token token) {}
    @Override public void initializer(int kind, Token token) {}
    @Override public void end_initializer(Token token) {}
    @Override public void brace_or_equal_initializer(Token token) {}
    @Override public void brace_or_equal_initializer(int kind, Token token) {}
    @Override public void end_brace_or_equal_initializer(Token token) {}
    @Override public void initializer_clause(Token token) {}
    @Override public void end_initializer_clause(Token token) {}
    @Override public void initializer_list(Token token) {}
    @Override public void initializer_list(int kind, Token token) {}
    @Override public void end_initializer_list(Token token) {}
    @Override public void braced_init_list(Token token) {}
    @Override public void braced_init_list(int kind, Token token) {}
    @Override public void end_braced_init_list(Token token) {}
    @Override public void end_class_name(Token token) {}
    @Override public void optionally_qualified_name(Token token) {}
    @Override public void end_optionally_qualified_name(Token token) {}
    @Override public void class_head(Token token) {}
    @Override public void end_class_head(Token token) {}
    @Override public void class_virtual_specifier(int kind, Token token) {}
    @Override public void member_specification(Token token) {}
    @Override public void member_specification(int kind, Token token) {}
    @Override public void end_member_specification(Token token) {}
    @Override public void member_declaration(Token token){}
    @Override public void member_declaration(int kind, Token token){}
    @Override public void end_member_declaration(Token token){}
    @Override public void simple_member_declaration(Token token){}
    @Override public void simple_member_declaration(int kind, Token token){}
    @Override public void end_simple_member_declaration(Token token){}
    @Override public void member_declarator(Token token) {}
    @Override public void end_member_declarator(Token token) {}
    @Override public void member_bitfield_declarator(Token token) {}
    @Override public void pure_specifier(Token token) {}
    @Override public void end_pure_specifier(Token token) {}
    @Override public void constant_initializer(Token token) {}
    @Override public void end_constant_initializer(Token token) {}
    @Override public void virt_specifier(int kind, Token token) {}
    @Override public void base_clause(Token token) {}
    @Override public void end_base_clause(Token token) {}
    @Override public void base_specifier_list(Token token) {}
    @Override public void base_specifier_list(int kind, Token token) {}
    @Override public void end_base_specifier_list(Token token) {}
    @Override public void class_or_decltype(Token token) {}
    @Override public void class_or_decltype(int kind, Token token) {}
    @Override public void end_class_or_decltype(Token token) {}
    @Override public void base_type_specifier(Token token) {}
    @Override public void end_base_type_specifier(Token token) {}
    @Override public void access_specifier(int kind, Token token) {}
    @Override public void conversion_function_id(Token token) {}
    @Override public void end_conversion_function_id(Token token) {}
    @Override public void conversion_type_id(Token token) {}
    @Override public void end_conversion_type_id(Token token) {}
    @Override public void ctor_initializer(Token token) {}
    @Override public void end_ctor_initializer(Token token) {}
    @Override public void mem_initializer_list(Token token) {}
    @Override public void mem_initializer_list(int kind, Token token) {}
    @Override public void end_mem_initializer_list(Token token) {}
    @Override public void mem_initializer(Token token) {}
    @Override public void mem_initializer(int kind, Token token) {}
    @Override public void end_mem_initializer(Token token) {}
    @Override public void mem_initializer_id(Token token) {}
    @Override public void end_mem_initializer_id(Token token) {}
    @Override public void mem_operator_function_id(Token token) {}
    @Override public void operator_function_id(int kind, Token token) {}
    @Override public void end_operator_function_id(Token token) {}
    @Override public void operator_id(Token token) {}
    @Override public void end_operator_id(Token token) {}
    @Override public void literal_operator_id(Token operatorToken, Token stringToken, Token identToken) {}
    @Override public void template_declaration(Token token) {}
    @Override public void end_template_declaration(Token token) {}
    @Override public void template_parameter_list(Token token) {}
    @Override public void template_parameter_list(int kind, Token token) {}
    @Override public void end_template_parameter_list(Token token) {}
    @Override public void template_parameter(Token token) {}
    @Override public void end_template_parameter(Token token) {}
    @Override public void type_parameter(int kind, Token token) {}
    @Override public void template_argument_list(Token token) {}
    @Override public void template_argument_list(int kind, Token token) {}
    @Override public void end_template_argument_list(Token token) {}
    @Override public void template_argument(Token token) {}
    @Override public void end_template_argument(Token token) {}
    @Override public void explicit_instantiation(Token token) {}
    @Override public void explicit_instantiation(int kind, Token token) {}
    @Override public void end_explicit_instantiation(Token token) {}
    @Override public void explicit_specialization(Token templateToken, Token lessthenToken, Token greaterthenToken) {}
    @Override public void end_explicit_specialization(Token token) {}
    @Override public void try_block(Token token) {}
    @Override public void end_try_block(Token token) {}
    @Override public void function_try_block(Token token) {}
    @Override public void end_function_try_block(Token token) {}
    @Override public void handler(Token token) {}
    @Override public void handler(int kind, Token token) {}
    @Override public void end_handler(Token token) {}

    @Override
    public void assignment_expression(Token token) {
    }

    @Override
    public void end_assignment_expression(Token token) {
    }

    @Override
    public void expression(Token token) {
    }

    @Override
    public void end_expression(Token token) {
    }

    @Override
    public void constant_expression(Token token) {
    }

    @Override
    public void end_constant_expression(Token token) {
    }

    @Override
    public void skip_balanced_curlies(Token token) {
    }

    @Override
    public CsmFile getCurrentFile() {
        return files.peek();
    }   
}
