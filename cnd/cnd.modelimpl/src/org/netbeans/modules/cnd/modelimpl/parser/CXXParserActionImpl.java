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

import java.util.Map;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 */
public class CXXParserActionImpl implements CXXParserActionEx {

    private final CppParserActionImpl orig;

    public CXXParserActionImpl(CsmParserProvider.CsmParserParameters params) {
        if (TraceFlags.TRACE_CPP_PARSER_ACTION) {
            orig = new CppParserActionTracer(params, this);
        } else {
            orig = new CppParserActionImpl(params, this);
        }
    }
    
    public void setParser(CXXParserEx parser) {
        orig.setParser(parser);
    }

    @Override
    public boolean type_specifier_already_present(TokenStream input) {        
        return orig.type_specifier_already_present(input);
    }    
    
    @Override
    public void enum_declaration(Token token) {
        orig.enum_declaration(convertToken(token));
    }

    @Override
    public void enum_strongly_typed(Token token) {
        orig.enum_strongly_typed(convertToken(token));
    }

    @Override
    public void enum_name(Token token) {
        orig.enum_name(convertToken(token));
    }

    @Override
    public void enum_body(Token token) {
        orig.enum_body(convertToken(token));
    }

    @Override
    public void enumerator(Token token) {
        orig.enumerator(convertToken(token));
    }

    @Override
    public void end_enum_body(Token token) {
        orig.end_enum_body(convertToken(token));
    }

    @Override
    public void end_enum_declaration(Token token) {
        orig.end_enum_declaration(convertToken(token));
    }

    @Override
    public void class_declaration(Token token) {
        orig.class_declaration(convertToken(token));
    }

    @Override
    public void class_kind(Token token) {
        orig.class_kind(convertToken(token));
    }

    @Override
    public void class_name(Token token) {
        orig.class_name(convertToken(token));
    }

    @Override
    public void class_body(Token token) {
        orig.class_body(convertToken(token));
    }

    @Override
    public void end_class_body(Token token) {
        orig.end_class_body(convertToken(token));
    }

    @Override
    public void end_class_declaration(Token token) {
        orig.end_class_declaration(convertToken(token));
    }

    @Override
    public void namespace_declaration(Token token) {
        orig.namespace_declaration(convertToken(token));
    }

    @Override
    public void namespace_name(Token token) {
        orig.namespace_name(convertToken(token));
    }

    @Override
    public void namespace_body(Token token) {
        orig.namespace_body(convertToken(token));
    }

    @Override
    public void end_namespace_body(Token token) {
        orig.end_namespace_body(convertToken(token));
    }

    @Override
    public void end_namespace_declaration(Token token) {
        orig.end_namespace_declaration(convertToken(token));
    }

    @Override
    public void compound_statement(Token token) {
        orig.compound_statement(convertToken(token));
    }

    @Override
    public void end_compound_statement(Token token) {
        orig.end_compound_statement(convertToken(token));
    }

    @Override
    public void id(Token token) {
        orig.id(convertToken(token));
    }

    @Override
    public void simple_type_id(Token token) {
        orig.simple_type_id(convertToken(token));
    }

    @Override
    public boolean isType(String name) {
        return orig.isType(name);
    }

    @Override
    public void pushFile(CsmFile file) {
        orig.pushFile(file);
    }

    @Override
    public CsmFile popFile() {
        return orig.popFile();
    }

    Map<Integer, CsmObject> getObjectsMap() {
        return orig.getObjectsMap();
    }

    public static org.netbeans.modules.cnd.antlr.Token convertToken(Token token) {
        return ParserProviderImpl.convertToken(token);
    }

    @Override
    public void simple_declaration(Token token) {
        orig.simple_declaration(convertToken(token));
    }

    @Override
    public void simple_declaration(int kind, Token token) {
        orig.simple_declaration(kind, convertToken(token));
    }
    
    @Override
    public void end_simple_declaration(Token token) {
        orig.end_simple_declaration(convertToken(token));
    }

    @Override
    public void simple_type_specifier(Token token) {
        orig.simple_type_specifier(convertToken(token));
    }

    @Override
    public void simple_type_specifier(int kind, Token token) {
        orig.simple_type_specifier(kind, convertToken(token));
    }

    @Override
    public void end_simple_type_specifier(Token token) {
        orig.end_simple_type_specifier(convertToken(token));
    }

    @Override
    public void nested_name_specifier(Token token) {
        orig.nested_name_specifier(convertToken(token));
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
        orig.simple_template_id_nocheck(convertToken(token));
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
        orig.simple_template_id_nocheck(kind, convertToken(token));
    }
    
    @Override
    public void simple_template_id(Token token) {
        orig.simple_template_id_nocheck(convertToken(token));
    }

    @Override
    public void simple_template_id(int kind, Token token) {
        orig.simple_template_id_nocheck(kind, convertToken(token));
    }
    
    @Override
    public void decl_specifier(int kind, Token token) {
        orig.decl_specifier(kind, convertToken(token));
    }

    @Override
    public void simple_template_id_or_ident(Token token) {
        orig.simple_template_id_or_ident(convertToken(token));
    }

    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
        orig.simple_template_id_or_ident(kind, convertToken(token));
    }
    
    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
        orig.type_parameter(kind, convertToken(token), convertToken(token2), convertToken(token3));
    }
    
    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3, Token token4) {
        orig.type_parameter(kind, convertToken(token), convertToken(token2), convertToken(token3), convertToken(token4));
    }
    
    @Override
    public void elaborated_type_specifier(Token token) {
        orig.elaborated_type_specifier(convertToken(token));        
    }

    @Override
    public void using_declaration(Token token) {
        orig.using_declaration(convertToken(token));        
    }

    @Override
    public void using_declaration(int kind, Token token) {
        orig.using_declaration(kind, convertToken(token));
    }

    @Override
    public void end_using_declaration(Token semicolonToken) {
        orig.end_using_declaration(convertToken(semicolonToken));
    }
    
    @Override
    public void parameter_declaration_list() {
        orig.parameter_declaration_list();
    }

    @Override
    public void end_parameter_declaration_list() {
        orig.end_parameter_declaration_list();
    }
    
    @Override
    public void decl_specifiers(Token token) {
        orig.decl_specifiers(convertToken(token));
    }

    @Override
    public void end_decl_specifiers(Token token) {
        orig.end_decl_specifiers(convertToken(token));
    }
    
    @Override
    public boolean identifier_is(int kind, Token token) {
        return orig.identifier_is(kind, convertToken(token));
    }
    
    @Override
    public boolean top_level_of_template_arguments() {
        return orig.top_level_of_template_arguments();
    }

    @Override
    public void template_declaration(int kind, Token token) {
        orig.template_declaration(kind, convertToken(token));
    }
 
    @Override
    public void using_directive(Token usingToken, Token namespaceToken) {
        orig.using_directive(convertToken(usingToken), convertToken(namespaceToken));
    }

    @Override
    public void using_directive(int kind, Token token) {
        orig.using_directive(kind, convertToken(token));
    }

    @Override
    public void end_using_directive(Token semicolonToken) {
        orig.end_using_directive(convertToken(semicolonToken));
    }
    
    @Override
    public void namespace_alias_definition(Token namespaceToken, Token identToken, Token assignequalToken) {
        orig.namespace_alias_definition(convertToken(namespaceToken), convertToken(identToken), convertToken(assignequalToken));
    }

    @Override
    public void end_namespace_alias_definition(Token semicolonToken) {
        orig.end_namespace_alias_definition(convertToken(semicolonToken));
    }

    @Override
    public void qualified_namespace_specifier(int kind, Token token) {
        orig.qualified_namespace_specifier(kind, convertToken(token));
    }

    @Override
    public void greedy_declarator() {
        orig.greedy_declarator();
    }

    @Override
    public void end_greedy_declarator() {
        orig.end_greedy_declarator();
    }

    @Override
    public void declarator_id() {
        orig.declarator_id();
    }

    @Override
    public void end_declarator_id() {
        orig.end_declarator_id();
    }

    @Override public void translation_unit(Token token) {orig.translation_unit(convertToken(token));}
    @Override public void end_translation_unit(Token token) {orig.end_translation_unit(convertToken(token));}
    @Override public void statement(Token token) {orig.statement(convertToken(token));}
    @Override public void end_statement(Token token) {orig.end_statement(convertToken(token));}
    @Override public void labeled_statement(Token token) {orig.labeled_statement(convertToken(token));}
    @Override public void labeled_statement(int kind, Token token) {orig.labeled_statement(kind, convertToken(token));}
    @Override public void labeled_statement(int kind, Token token1, Token token2) {orig.labeled_statement(kind, convertToken(token1), convertToken(token2));}
    @Override public void end_labeled_statement(Token token) {orig.end_labeled_statement(convertToken(token));}
    @Override public void expression_statement(Token token) {orig.expression_statement(convertToken(token));}
    @Override public void end_expression_statement(Token token) {orig.end_expression_statement(convertToken(token));}
    @Override public void selection_statement(Token token) {orig.selection_statement(convertToken(token));}
    @Override public void selection_statement(int kind, Token token) {orig.selection_statement(kind, convertToken(token));}
    @Override public void end_selection_statement(Token token) {orig.end_selection_statement(convertToken(token));}
    @Override public void condition(Token token) {orig.condition(convertToken(token));}
    @Override public void condition(int kind, Token token) {orig.condition(kind, convertToken(token));}
    @Override public void end_condition(Token token) {orig.end_condition(convertToken(token));}
    @Override public void condition_declaration(Token token) {orig.condition_declaration(convertToken(token));}
    @Override public void end_condition_declaration(Token token) {orig.end_condition_declaration(convertToken(token));}
    @Override public void condition_expression(Token token) {orig.condition_expression(convertToken(token));}
    @Override public void end_condition_expression(Token token) {orig.end_condition_expression(convertToken(token));}
    @Override public void iteration_statement(Token token) {orig.iteration_statement(convertToken(token));}
    @Override public void iteration_statement(int kind, Token token) {orig.iteration_statement(kind, convertToken(token));}
    @Override public void end_iteration_statement(Token token) {orig.end_iteration_statement(convertToken(token));}
    @Override public void for_init_statement(Token token) {orig.for_init_statement(convertToken(token));}
    @Override public void end_for_init_statement(Token token) {orig.end_for_init_statement(convertToken(token));}
    @Override public void for_range_declaration(Token token) {orig.for_range_declaration(convertToken(token));}
    @Override public void end_for_range_declaration(Token token) {orig.end_for_range_declaration(convertToken(token));}
    @Override public void for_range_initializer(Token token) {orig.for_range_initializer(convertToken(token));}
    @Override public void end_for_range_initializer(Token token) {orig.end_for_range_initializer(convertToken(token));}
    @Override public void jump_statement(Token token) {orig.jump_statement(convertToken(token));}
    @Override public void jump_statement(int kind, Token token) {orig.jump_statement(kind, convertToken(token));}
    @Override public void jump_statement(int kind, Token token1, Token token2) {orig.jump_statement(kind, convertToken(token1), convertToken(token2));}
    @Override public void end_jump_statement(Token token) {orig.end_jump_statement(convertToken(token));}
    @Override public void declaration_statement(Token token) {orig.declaration_statement(convertToken(token));}
    @Override public void end_declaration_statement(Token token) {orig.end_declaration_statement(convertToken(token));}
    @Override public void declaration(Token token) {orig.declaration(convertToken(token));}
    @Override public void end_declaration(Token token) {orig.end_declaration(convertToken(token));}
    @Override public void block_declaration(Token token) {orig.block_declaration(convertToken(token));}
    @Override public void end_block_declaration(Token token) {orig.end_block_declaration(convertToken(token));}
    @Override public void id_expression(Token token) {orig.id_expression(convertToken(token));}
    @Override public void end_id_expression(Token token) {orig.end_id_expression(convertToken(token));}

    @Override public void tilde_class_name(Token token) {orig.tilde_class_name(convertToken(token));}
    @Override public void end_tilde_class_name(Token token) {orig.end_tilde_class_name(convertToken(token));}
    
    @Override public void alias_declaration(Token usingToken, Token identToken, Token assignequalToken) {orig.alias_declaration(convertToken(usingToken), convertToken(identToken), convertToken(assignequalToken));}
    @Override public void end_alias_declaration(Token token) {orig.end_alias_declaration(convertToken(token));}
    @Override public void function_specifier(int kind, Token token) {orig.function_specifier(kind, convertToken(token));}
    @Override public void type_specifier(Token token) {orig.type_specifier(convertToken(token));}
    @Override public void end_type_specifier(Token token) {orig.end_type_specifier(convertToken(token));}
    @Override public void trailing_type_specifier(Token token) {orig.trailing_type_specifier(convertToken(token));}
    @Override public void end_trailing_type_specifier(Token token) {orig.end_trailing_type_specifier(convertToken(token));}
    @Override public void decltype_specifier(Token token) {orig.decltype_specifier(convertToken(token));}
    @Override public void decltype_specifier(int kind, Token token) {orig.decltype_specifier(kind, convertToken(token));}
    @Override public void end_decltype_specifier(Token token) {orig.end_decltype_specifier(convertToken(token));}
    @Override public void end_elaborated_type_specifier(Token token) {orig.end_elaborated_type_specifier(convertToken(token));}
    @Override public void typename_specifier(Token token) {orig.typename_specifier(convertToken(token));}
    @Override public void end_typename_specifier(Token token) {orig.end_typename_specifier(convertToken(token));}
    @Override public void asm_definition(Token asmToken, Token lparenToken, Token stringToken, Token rparenToken, Token semicolonToken) {orig.asm_definition(convertToken(asmToken), convertToken(lparenToken), convertToken(stringToken), convertToken(rparenToken), convertToken(semicolonToken));}
    @Override public void linkage_specification(Token externToken, Token stringToken) {orig.linkage_specification(convertToken(externToken), convertToken(stringToken));}
    @Override public void linkage_specification(int kind, Token token) {orig.linkage_specification(kind, convertToken(token));}
    @Override public void end_linkage_specification(Token token) {orig.end_linkage_specification(convertToken(token));}
    @Override public void init_declarator_list(Token token) {orig.init_declarator_list(convertToken(token));}
    @Override public void init_declarator_list(int kind, Token token) {orig.init_declarator_list(kind, convertToken(token));}
    @Override public void end_init_declarator_list(Token token) {orig.end_init_declarator_list(convertToken(token));}
    @Override public void init_declarator(Token token) {orig.init_declarator(convertToken(token));}
    @Override public void end_init_declarator(Token token) {orig.end_init_declarator(convertToken(token));}
    @Override public void declarator(Token token) {orig.declarator(convertToken(token));}
    @Override public void end_declarator(Token token) {orig.end_declarator(convertToken(token));}
    @Override public void noptr_declarator(Token token) {orig.noptr_declarator(convertToken(token));}
    @Override public void noptr_declarator(int kind, Token token) {orig.noptr_declarator(kind, convertToken(token));}
    @Override public void end_noptr_declarator(Token token) {orig.end_noptr_declarator(convertToken(token));}
    @Override public void function_declarator(Token token) {orig.function_declarator(convertToken(token));}
    @Override public void end_function_declarator(Token token) {orig.end_function_declarator(convertToken(token));}
    @Override public void constructor_declarator(Token token) {orig.constructor_declarator(convertToken(token));}
    @Override public void end_constructor_declarator(Token token) {orig.end_constructor_declarator(convertToken(token));}
    @Override public void function_declarator(int kind, Token token) {orig.function_declarator(kind, convertToken(token));}
    @Override public void noptr_abstract_declarator(Token token) {orig.noptr_abstract_declarator(convertToken(token));}
    @Override public void noptr_abstract_declarator(int kind, Token token) {orig.noptr_abstract_declarator(kind, convertToken(token));}
    @Override public void end_noptr_abstract_declarator(Token token) {orig.end_noptr_abstract_declarator(convertToken(token));}
    @Override public void universal_declarator(Token token) {orig.universal_declarator(convertToken(token));}
    @Override public void end_universal_declarator(Token token) {orig.end_universal_declarator(convertToken(token));}
    @Override public void greedy_declarator(Token token) {orig.greedy_declarator(convertToken(token));}
    @Override public void end_greedy_declarator(Token token) {orig.end_greedy_declarator(convertToken(token));}
    @Override public void greedy_nonptr_declarator(Token token) {orig.greedy_nonptr_declarator(convertToken(token));}
    @Override public void greedy_nonptr_declarator(int kind, Token token) {orig.greedy_nonptr_declarator(kind, convertToken(token));}
    @Override public void end_greedy_nonptr_declarator(Token token) {orig.end_greedy_nonptr_declarator(convertToken(token));}
    @Override public void ptr_operator(Token token) {orig.ptr_operator(convertToken(token));}
    @Override public void ptr_operator(int kind, Token token) {orig.ptr_operator(kind, convertToken(token));}
    @Override public void end_ptr_operator(Token token) {orig.end_ptr_operator(convertToken(token));}
    @Override public void cv_qualifier(int kind, Token token) {orig.cv_qualifier(kind, convertToken(token));}
    @Override public void ref_qualifier(int kind, Token token) {orig.ref_qualifier(kind, convertToken(token));}
    @Override public void declarator_id(Token token) {orig.declarator_id(convertToken(token));}
    @Override public void declarator_id(int kind, Token token) {orig.declarator_id(kind, convertToken(token));}
    @Override public void end_declarator_id(Token token) {orig.end_declarator_id(convertToken(token));}
    @Override public void type_id(Token token) {orig.type_id(convertToken(token));}
    @Override public void end_type_id(Token token) {orig.end_type_id(convertToken(token));}
    @Override public void parameters_and_qualifiers(Token token) {orig.parameters_and_qualifiers(convertToken(token));}
    @Override public void parameters_and_qualifiers(int kind, Token token) {orig.parameters_and_qualifiers(kind, convertToken(token));}
    @Override public void end_parameters_and_qualifiers(Token token) {orig.end_parameters_and_qualifiers(convertToken(token));}
    @Override public void parameter_declaration_clause(Token token) {orig.parameter_declaration_clause(convertToken(token));}
    @Override public void parameter_declaration_clause(int kind, Token token) {orig.parameter_declaration_clause(kind, convertToken(token));}
    @Override public void end_parameter_declaration_clause(Token token) {orig.end_parameter_declaration_clause(convertToken(token));}
    @Override public void parameter_declaration_list(Token token) {orig.parameter_declaration_list(convertToken(token));}
    @Override public void end_parameter_declaration_list(int kind, Token token) {orig.end_parameter_declaration_list(kind, convertToken(token));}
    @Override public void end_parameter_declaration_list(Token token) {orig.end_parameter_declaration_list(convertToken(token));}
    @Override public void parameter_declaration(Token token) {orig.parameter_declaration(convertToken(token));}
    @Override public void parameter_declaration(int kind, Token token) {orig.parameter_declaration(kind, convertToken(token));}
    @Override public void end_parameter_declaration(Token token) {orig.end_parameter_declaration(convertToken(token));}
    @Override public void function_definition_after_declarator(Token token) {orig.function_definition_after_declarator(convertToken(token));}
    @Override public void function_definition_after_declarator(int kind, Token token) {orig.function_definition_after_declarator(kind, convertToken(token));}
    @Override public void end_function_definition_after_declarator(Token token) {orig.end_function_definition_after_declarator(convertToken(token));}
    @Override public void function_declaration(Token token) {orig.function_declaration(convertToken(token));}
    @Override public void end_function_declaration(Token token) {orig.end_function_declaration(convertToken(token));}
    @Override public void function_definition(Token token) {orig.function_definition(convertToken(token));}
    @Override public void end_function_definition(Token token) {orig.end_function_definition(convertToken(token));}
    @Override public void function_body(Token token) {orig.function_body(convertToken(token));}
    @Override public void end_function_body(Token token) {orig.end_function_body(convertToken(token));}
    @Override public void initializer(Token token) {orig.initializer(convertToken(token));}
    @Override public void initializer(int kind, Token token) {orig.initializer(kind, convertToken(token));}
    @Override public void end_initializer(Token token) {orig.end_initializer(convertToken(token));}
    @Override public void brace_or_equal_initializer(Token token) {orig.brace_or_equal_initializer(convertToken(token));}
    @Override public void brace_or_equal_initializer(int kind, Token token) {orig.brace_or_equal_initializer(kind, convertToken(token));}
    @Override public void end_brace_or_equal_initializer(Token token) {orig.end_brace_or_equal_initializer(convertToken(token));}
    @Override public void initializer_clause(Token token) {orig.initializer_clause(convertToken(token));}
    @Override public void end_initializer_clause(Token token) {orig.end_initializer_clause(convertToken(token));}
    @Override public void initializer_list(Token token) {orig.initializer_list(convertToken(token));}
    @Override public void initializer_list(int kind, Token token) {orig.initializer_list(kind, convertToken(token));}
    @Override public void end_initializer_list(Token token) {orig.end_initializer_list(convertToken(token));}
    @Override public void braced_init_list(Token token) {orig.braced_init_list(convertToken(token));}
    @Override public void braced_init_list(int kind, Token token) {orig.braced_init_list(kind, convertToken(token));}
    @Override public void end_braced_init_list(Token token) {orig.end_braced_init_list(convertToken(token));}
    @Override public void end_class_name(Token token) {orig.end_class_name(convertToken(token));}
    @Override public void optionally_qualified_name(Token token) {orig.optionally_qualified_name(convertToken(token));}
    @Override public void end_optionally_qualified_name(Token token) {orig.end_optionally_qualified_name(convertToken(token));}
    @Override public void class_head(Token token) {orig.class_head(convertToken(token));}
    @Override public void end_class_head(Token token) {orig.end_class_head(convertToken(token));}
    @Override public void class_virtual_specifier(int kind, Token token) {orig.class_virtual_specifier(kind, convertToken(token));}
    @Override public void member_specification(Token token) {orig.member_specification(convertToken(token));}
    @Override public void member_specification(int kind, Token token) {orig.member_specification(kind, convertToken(token));}
    @Override public void end_member_specification(Token token) {orig.end_member_specification(convertToken(token));}
    @Override public void member_declaration(Token token){orig.member_declaration(convertToken(token));}
    @Override public void member_declaration(int kind, Token token){orig.member_declaration(kind, convertToken(token));}
    @Override public void end_member_declaration(Token token){orig.end_member_declaration(convertToken(token));}    
    @Override public void member_bitfield_declarator(Token token){orig.member_bitfield_declarator(convertToken(token));}    
    @Override public void simple_member_declaration(Token token){orig.simple_member_declaration(convertToken(token));}
    @Override public void simple_member_declaration(int kind, Token token){orig.simple_member_declaration(kind, convertToken(token));}
    @Override public void end_simple_member_declaration(Token token){orig.end_simple_member_declaration(convertToken(token));}    
    @Override public void member_declarator(Token token) {orig.member_declarator(convertToken(token));}
    @Override public void end_member_declarator(Token token) {orig.end_member_declarator(convertToken(token));}
    @Override public void pure_specifier(Token token) {orig.pure_specifier(convertToken(token));}
    @Override public void end_pure_specifier(Token token) {orig.end_pure_specifier(convertToken(token));}
    @Override public void constant_initializer(Token token) {orig.constant_initializer(convertToken(token));}
    @Override public void end_constant_initializer(Token token) {orig.end_constant_initializer(convertToken(token));}
    @Override public void virt_specifier(int kind, Token token) {orig.virt_specifier(kind, convertToken(token));}
    @Override public void base_clause(Token token) {orig.base_clause(convertToken(token));}
    @Override public void end_base_clause(Token token) {orig.end_base_clause(convertToken(token));}
    @Override public void base_specifier_list(Token token) {orig.base_specifier_list(convertToken(token));}
    @Override public void base_specifier_list(int kind, Token token) {orig.base_specifier_list(kind, convertToken(token));}
    @Override public void end_base_specifier_list(Token token) {orig.end_base_specifier_list(convertToken(token));}
    @Override public void class_or_decltype(Token token) {orig.class_or_decltype(convertToken(token));}
    @Override public void class_or_decltype(int kind, Token token) {orig.class_or_decltype(kind, convertToken(token));}
    @Override public void end_class_or_decltype(Token token) {orig.end_class_or_decltype(convertToken(token));}
    @Override public void base_type_specifier(Token token) {orig.base_type_specifier(convertToken(token));}
    @Override public void end_base_type_specifier(Token token) {orig.end_base_type_specifier(convertToken(token));}
    @Override public void access_specifier(int kind, Token token) {orig.access_specifier(kind, convertToken(token));}
    @Override public void conversion_function_id(Token token) {orig.conversion_function_id(convertToken(token));}
    @Override public void end_conversion_function_id(Token token) {orig.end_conversion_function_id(convertToken(token));}
    @Override public void conversion_type_id(Token token) {orig.conversion_type_id(convertToken(token));}
    @Override public void end_conversion_type_id(Token token) {orig.end_conversion_type_id(convertToken(token));}
    @Override public void ctor_initializer(Token token) {orig.ctor_initializer(convertToken(token));}
    @Override public void end_ctor_initializer(Token token) {orig.end_ctor_initializer(convertToken(token));}
    @Override public void mem_initializer_list(Token token) {orig.mem_initializer_list(convertToken(token));}
    @Override public void mem_initializer_list(int kind, Token token) {orig.mem_initializer_list(kind, convertToken(token));}
    @Override public void end_mem_initializer_list(Token token) {orig.end_mem_initializer_list(convertToken(token));}
    @Override public void mem_initializer(Token token) {orig.mem_initializer(convertToken(token));}
    @Override public void mem_initializer(int kind, Token token) {orig.mem_initializer(kind, convertToken(token));}
    @Override public void end_mem_initializer(Token token) {orig.end_mem_initializer(convertToken(token));}
    @Override public void mem_initializer_id(Token token) {orig.mem_initializer_id(convertToken(token));}
    @Override public void end_mem_initializer_id(Token token) {orig.end_mem_initializer_id(convertToken(token));}
    @Override public void mem_operator_function_id(Token token) {orig.mem_operator_function_id(convertToken(token));}
    @Override public void operator_function_id(int kind, Token token) {orig.operator_function_id(kind, convertToken(token));}
    @Override public void end_operator_function_id(Token token) {orig.end_operator_function_id(convertToken(token));}
    @Override public void operator_id(Token token) {orig.operator_id(convertToken(token));}
    @Override public void end_operator_id(Token token) {orig.end_operator_id(convertToken(token));}
    @Override public void literal_operator_id(Token operatorToken, Token stringToken, Token identToken) {orig.literal_operator_id(convertToken(operatorToken), convertToken(stringToken), convertToken(identToken));}
    @Override public void template_declaration(Token token) {orig.template_declaration(convertToken(token));}
    @Override public void end_template_declaration(Token token) {orig.end_template_declaration(convertToken(token));}
    @Override public void template_parameter_list(Token token) {orig.template_parameter_list(convertToken(token));}
    @Override public void template_parameter_list(int kind, Token token) {orig.template_parameter_list(kind, convertToken(token));}
    @Override public void end_template_parameter_list(Token token) {orig.end_template_parameter_list(convertToken(token));}
    @Override public void template_parameter(Token token) {orig.template_parameter(convertToken(token));}
    @Override public void end_template_parameter(Token token) {orig.end_template_parameter(convertToken(token));}
    @Override public void type_parameter(int kind, Token token) {orig.type_parameter(kind, convertToken(token));}
    @Override public void template_argument_list(Token token) {orig.template_argument_list(convertToken(token));}
    @Override public void template_argument_list(int kind, Token token) {orig.template_argument_list(kind, convertToken(token));}
    @Override public void end_template_argument_list(Token token) {orig.end_template_argument_list(convertToken(token));}
    @Override public void template_argument(Token token) {orig.template_argument(convertToken(token));}
    @Override public void end_template_argument(Token token) {orig.end_template_argument(convertToken(token));}
    @Override public void explicit_instantiation(Token token) {orig.explicit_instantiation(convertToken(token));}
    @Override public void explicit_instantiation(int kind, Token token) {orig.explicit_instantiation(kind, convertToken(token));}
    @Override public void end_explicit_instantiation(Token token) {orig.end_explicit_instantiation(convertToken(token));}
    @Override public void explicit_specialization(Token templateToken, Token lessthenToken, Token greaterthenToken) {orig.explicit_specialization(convertToken(templateToken), convertToken(lessthenToken), convertToken(greaterthenToken));}
    @Override public void end_explicit_specialization(Token token) {orig.end_explicit_specialization(convertToken(token));}
    @Override public void try_block(Token token) {orig.try_block(convertToken(token));}
    @Override public void end_try_block(Token token) {orig.end_try_block(convertToken(token));}
    @Override public void function_try_block(Token token) {orig.function_try_block(convertToken(token));}
    @Override public void end_function_try_block(Token token) {orig.end_function_try_block(convertToken(token));}
    @Override public void handler(Token token) {orig.handler(convertToken(token));}
    @Override public void handler(int kind, Token token) {orig.handler(kind, convertToken(token));}
    @Override public void end_handler(Token token) {orig.end_handler(convertToken(token));}

    @Override
    public void assignment_expression(Token token) {
        orig.assignment_expression(convertToken(token));
    }

    @Override
    public void end_assignment_expression(Token token) {
        orig.end_assignment_expression(convertToken(token));
    }

    @Override
    public void expression(Token token) {
        orig.expression(convertToken(token));
    }

    @Override
    public void end_expression(Token token) {
        orig.end_expression(convertToken(token));
    }

    @Override
    public void constant_expression(Token token) {
        orig.constant_expression(convertToken(token));
    }

    @Override
    public void end_constant_expression(Token token) {
        orig.end_constant_expression(convertToken(token));
    }

    @Override public void skip_balanced_curlies(Token token) {orig.skip_balanced_curlies(convertToken(token));}    

    @Override
    public CsmFile getCurrentFile() {
        return orig.getCurrentFile();
    }
}
