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

import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceFactory;

/**
 *
 */
public class CppParserActionTracer extends CppParserActionImpl implements CppParserActionEx {
    private final TraceFactory.TraceWriter traceWriter;
    
    public CppParserActionTracer(CsmParserProvider.CsmParserParameters params, CXXParserActionEx wrapper) {
        super(params, wrapper);
        traceWriter = TraceFactory.getTraceWriter(this);
    }

    @Override
    public boolean type_specifier_already_present(TokenStream input) {
        return super.type_specifier_already_present(input);
    }

    @Override
    public boolean identifier_is(int kind, Token token) {
        return super.identifier_is(kind, token);
    }

    @Override
    public boolean top_level_of_template_arguments() {
        return super.top_level_of_template_arguments();
    }

    @Override
    public void enum_declaration(Token token) {
        printIn("enum_declaration", token); //NOI18N
        super.enum_declaration(token);
    }

    @Override
    public void end_enum_declaration(Token token) {
        super.end_enum_declaration(token);
        printOut("enum_declaration", token); //NOI18N
    }

    @Override
    public void enum_strongly_typed(Token token) {
        print("enum_strongly_typed", token); //NOI18N
        super.enum_strongly_typed(token);
    }

    @Override
    public void enum_name(Token token) {
        print("enum_name", token); //NOI18N
        super.enum_name(token);
    }

    @Override
    public void enum_body(Token token) {
        printIn("enum_body", token); //NOI18N
        super.enum_body(token);
    }

    @Override
    public void end_enum_body(Token token) {
        super.end_enum_body(token);
        printOut("enum_body", token); //NOI18N
    }

    @Override
    public void enumerator(Token token) {
        print("enumerator", token); //NOI18N
        super.enumerator(token);
    }

    @Override
    public void class_declaration(Token token) {
        printIn("class_declaration", token); //NOI18N
        super.class_declaration(token);
    }

    @Override
    public void end_class_declaration(Token token) {
        super.end_class_declaration(token);
        printOut("class_declaration", token); //NOI18N
    }

    @Override
    public void class_kind(Token token) {
        print("class_kind", token); //NOI18N
        super.class_kind(token);
    }
    
    @Override
    public void class_name(Token token) {
        printIn("class_name", token); //NOI18N
        super.class_name(token);
    }

    @Override
    public void end_class_name(Token token) {
        super.end_class_name(token);
        printOut("class_name", token); //NOI18N
    }

    @Override
    public void class_body(Token token) {
        printIn("class_body", token); //NOI18N
        super.class_body(token);
    }

    @Override
    public void end_class_body(Token token) {
        super.end_class_body(token);
        printOut("class_body", token); //NOI18N
    }

    @Override
    public void namespace_declaration(Token token) {
        printIn("namespace_declaration", token); //NOI18N
        super.namespace_declaration(token);
    }

    @Override
    public void namespace_name(Token token) {
        print("namespace_name", token); //NOI18N
        super.namespace_name(token);
    }

    @Override
    public void namespace_body(Token token) {
        printIn("namespace_body", token); //NOI18N
        super.namespace_body(token);
    }

    @Override
    public void end_namespace_body(Token token) {
        super.end_namespace_body(token);
        printOut("namespace_body", token); //NOI18N
    }

    @Override
    public void end_namespace_declaration(Token token) {
        super.end_namespace_declaration(token);
        printOut("namespace_declaration", token); //NOI18N
    }

    @Override
    public void compound_statement(Token token) {
        printIn("compound_statement", token); //NOI18N
        super.compound_statement(token);
    }

    @Override
    public void end_compound_statement(Token token) {
        super.end_compound_statement(token);
        printOut("compound_statement", token); //NOI18N
    }

    @Override
    public void simple_declaration(Token token) {
        printIn("simple_declaration", token); //NOI18N
        super.simple_declaration(token);
    }

    @Override
    public void simple_declaration(int kind, Token token) {
        print("simple_declaration", token); //NOI18N
        super.simple_declaration(kind, token);
    }

    @Override
    public void end_simple_declaration(Token token) {
        super.end_simple_declaration(token);
        printOut("simple_declaration", token); //NOI18N
    }

    @Override
    public void decl_specifier(int kind, Token token) {
        print("decl_specifier", token); //NOI18N
        super.decl_specifier(kind, token);
    }

    @Override
    public void simple_type_specifier(Token token) {
        printIn("simple_type_specifier", token); //NOI18N
        super.simple_type_specifier(token);
    }

    @Override
    public void simple_type_specifier(int kind, Token token) {
        print("simple_type_specifier", token); //NOI18N
        super.simple_type_specifier(kind, token);
    }

    @Override
    public void end_simple_type_specifier(Token token) {
        super.end_simple_type_specifier(token);
        printOut("simple_type_specifier", token); //NOI18N
    }

    @Override
    public void nested_name_specifier(Token token) {
        print("nested_name_specifier", token); //NOI18N
        super.nested_name_specifier(token);
    }

    @Override
    public void id(Token token) {
        print("id", token); //NOI18N
        super.id(token);
    }

    @Override
    public void simple_type_id(Token token) {
        print("simple_type_id", token); //NOI18N
        super.simple_type_id(token);
    }

    @Override
    public boolean isType(String name) {
        return super.isType(name);
    }

    @Override
    public void simple_template_id(Token token) {
        print("simple_template_id", token); //NOI18N
        super.simple_template_id(token);
    }

    @Override
    public void simple_template_id(int kind, Token token) {
        print("simple_template_id", token); //NOI18N
        super.simple_template_id(kind, token);
    }

    @Override
    public void simple_template_id_or_ident(Token token) {
        print("simple_template_id_or_ident", token); //NOI18N
        super.simple_template_id_or_ident(token);
    }

    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
        print("simple_template_id_or_ident", token); //NOI18N
        super.simple_template_id_or_ident(kind, token);
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
        print("simple_template_id_nocheck", token); //NOI18N
        super.simple_template_id_nocheck(token);
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
        print("simple_template_id_nocheck", token); //NOI18N
        super.simple_template_id_nocheck(kind, token);
    }

    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
        print("type_parameter", token, token2, token3); //NOI18N
        super.type_parameter(kind, token, token2, token3);
    }

    @Override
    public void type_parameter(int kind, Token token) {
        print("type_parameter", token); //NOI18N
        super.type_parameter(kind, token);
    }

    @Override
    public void elaborated_type_specifier(Token token) {
        printIn("elaborated_type_specifier", token); //NOI18N
        super.elaborated_type_specifier(token);
    }

    @Override
    public void using_declaration(Token token) {
        printIn("using_declaration", token); //NOI18N
        super.using_declaration(token);
    }

    @Override
    public void using_declaration(int kind, Token token) {
        print("using_declaration", token); //NOI18N
        super.using_declaration(kind, token);
    }

    @Override
    public void end_using_declaration(Token token) {
        super.end_using_declaration(token);
        printOut("using_declaration", token); //NOI18N
    }

    @Override
    public void parameter_declaration_list() {
        printIn("parameter_declaration_list"); //NOI18N
        super.parameter_declaration_list();
    }

    @Override
    public void end_parameter_declaration_list() {
        super.end_parameter_declaration_list();
        printOut("parameter_declaration_list"); //NOI18N
    }

    @Override
    public void decl_specifiers(Token token) {
        printIn("decl_specifiers", token); //NOI18N
        super.decl_specifiers(token);
    }

    @Override
    public void end_decl_specifiers(Token token) {
        super.end_decl_specifiers(token);
        printOut("decl_specifiers", token); //NOI18N
    }

    @Override
    public void using_directive(Token token, Token namespaceToken) {
        printIn("using_directive", token, namespaceToken); //NOI18N
        super.using_directive(token, namespaceToken);
    }

    @Override
    public void using_directive(int kind, Token token) {
        print("using_directive", token); //NOI18N
        super.using_directive(kind, token);
    }

    @Override
    public void end_using_directive(Token token) {
        super.end_using_directive(token);
        printOut("using_directive", token); //NOI18N
    }

    @Override
    public void namespace_alias_definition(Token token, Token identToken, Token assignequalToken) {
        printIn("namespace_alias_definition", token, identToken, assignequalToken); //NOI18N
        super.namespace_alias_definition(token, identToken, assignequalToken);
    }

    @Override
    public void end_namespace_alias_definition(Token token) {
        super.end_namespace_alias_definition(token);
        printOut("namespace_alias_definition", token); //NOI18N
    }

    @Override
    public void qualified_namespace_specifier(int kind, Token token) {
        print("qualified_namespace_specifier", token); //NOI18N
        super.qualified_namespace_specifier(kind, token);
    }

    @Override
    public void greedy_declarator() {
        printIn("greedy_declarator"); //NOI18N
        super.greedy_declarator();
    }

    @Override
    public void end_greedy_declarator() {
        super.end_greedy_declarator();
        printOut("greedy_declarator"); //NOI18N
    }

    @Override
    public void declarator_id() {
        printIn("declarator_id"); //NOI18N
        super.declarator_id();
    }

    @Override
    public void end_declarator_id() {
        super.end_declarator_id();
        printOut("declarator_id"); //NOI18N
    }

    @Override
    public void translation_unit(Token token) {
        printIn("translation_unit", token); //NOI18N
        super.translation_unit(token);
    }

    @Override
    public void end_translation_unit(Token token) {
        super.end_translation_unit(token);
        printOut("translation_unit", token); //NOI18N
    }

    @Override
    public void statement(Token token) {
        printIn("statement", token); //NOI18N
        super.statement(token);
    }

    @Override
    public void end_statement(Token token) {
        super.end_statement(token);
        printOut("statement", token); //NOI18N
    }

    @Override
    public void labeled_statement(Token token) {
        printIn("labeled_statement", token); //NOI18N
        super.labeled_statement(token);
    }

    @Override
    public void labeled_statement(int kind, Token token) {
        print("labeled_statement", token); //NOI18N
        super.labeled_statement(kind, token);
    }

    @Override
    public void labeled_statement(int kind, Token token1, Token token2) {
        print("labeled_statement", token1, token2); //NOI18N
        super.labeled_statement(kind, token1, token2);
    }

    @Override
    public void end_labeled_statement(Token token) {
        super.end_labeled_statement(token);
        printOut("labeled_statement", token); //NOI18N
    }

    @Override
    public void expression_statement(Token token) {
        printIn("expression_statement", token); //NOI18N
        super.expression_statement(token);
    }

    @Override
    public void end_expression_statement(Token token) {
        super.end_expression_statement(token);
        printOut("expression_statement", token); //NOI18N
    }

    @Override
    public void selection_statement(Token token) {
        printIn("selection_statement", token); //NOI18N
        super.selection_statement(token);
    }

    @Override
    public void selection_statement(int kind, Token token) {
        print("selection_statement", token); //NOI18N
        super.selection_statement(kind, token);
    }

    @Override
    public void end_selection_statement(Token token) {
        super.end_selection_statement(token);
        printOut("selection_statement", token); //NOI18N
    }

    @Override
    public void condition(Token token) {
        printIn("condition", token); //NOI18N
        super.condition(token);
    }

    @Override
    public void condition(int kind, Token token) {
        print("condition", token); //NOI18N
        super.condition(kind, token);
    }

    @Override
    public void end_condition(Token token) {
        super.end_condition(token);
        printOut("condition", token); //NOI18N
    }

    @Override
    public void condition_declaration(Token token) {
        printIn("condition_declaration", token); //NOI18N
        super.condition_declaration(token);
    }

    @Override
    public void end_condition_declaration(Token token) {
        super.end_condition_declaration(token);
        printOut("condition_declaration", token); //NOI18N
    }

    @Override
    public void condition_expression(Token token) {
        printIn("condition_expression", token); //NOI18N
        super.condition_expression(token);
    }

    @Override
    public void end_condition_expression(Token token) {
        super.end_condition_expression(token);
        printOut("condition_expression", token); //NOI18N
    }

    @Override
    public void iteration_statement(Token token) {
        printIn("iteration_statement", token); //NOI18N
        super.iteration_statement(token);
    }

    @Override
    public void iteration_statement(int kind, Token token) {
        print("iteration_statement", token); //NOI18N
        super.iteration_statement(kind, token);
    }

    @Override
    public void end_iteration_statement(Token token) {
        super.end_iteration_statement(token);
        printOut("iteration_statement", token); //NOI18N
    }

    @Override
    public void for_init_statement(Token token) {
        printIn("for_init_statement", token); //NOI18N
        super.for_init_statement(token);
    }

    @Override
    public void end_for_init_statement(Token token) {
        super.end_for_init_statement(token);
        printOut("for_init_statement", token); //NOI18N
    }

    @Override
    public void for_range_declaration(Token token) {
        printIn("for_range_declaration", token); //NOI18N
        super.for_range_declaration(token);
    }

    @Override
    public void end_for_range_declaration(Token token) {
        super.end_for_range_declaration(token);
        printOut("for_range_declaration", token); //NOI18N
    }

    @Override
    public void for_range_initializer(Token token) {
        printIn("for_range_initializer", token); //NOI18N
        super.for_range_initializer(token);
    }

    @Override
    public void end_for_range_initializer(Token token) {
        super.end_for_range_initializer(token);
        printOut("for_range_initializer", token); //NOI18N
    }

    @Override
    public void jump_statement(Token token) {
        printIn("jump_statement", token); //NOI18N
        super.jump_statement(token);
    }

    @Override
    public void jump_statement(int kind, Token token) {
        print("jump_statement", token); //NOI18N
        super.jump_statement(kind, token);
    }

    @Override
    public void jump_statement(int kind, Token token1, Token token2) {
        print("jump_statement", token1, token2); //NOI18N
        super.jump_statement(kind, token1, token2);
    }

    @Override
    public void end_jump_statement(Token token) {
        super.end_jump_statement(token);
        printOut("jump_statement", token); //NOI18N
    }

    @Override
    public void declaration_statement(Token token) {
        printIn("declaration_statement", token); //NOI18N
        super.declaration_statement(token);
    }

    @Override
    public void end_declaration_statement(Token token) {
        super.end_declaration_statement(token);
        printOut("declaration_statement", token); //NOI18N
    }

    @Override
    public void declaration(Token token) {
        printIn("declaration", token); //NOI18N
        super.declaration(token);
    }

    @Override
    public void end_declaration(Token token) {
        super.end_declaration(token);
        printOut("declaration", token); //NOI18N
    }

    @Override
    public void block_declaration(Token token) {
        printIn("block_declaration", token); //NOI18N
        super.block_declaration(token);
    }

    @Override
    public void end_block_declaration(Token token) {
        super.end_block_declaration(token);
        printOut("block_declaration", token); //NOI18N
    }

    @Override
    public void id_expression(Token token) {
        printIn("id_expression", token); //NOI18N
        super.id_expression(token);
    }

    @Override
    public void end_id_expression(Token token) {
        super.end_id_expression(token);
        printOut("id_expression", token); //NOI18N
    }
    
    @Override
    public void tilde_class_name(Token token) {
        printIn("tilde_class_name", token); //NOI18N
        super.tilde_class_name(token);
    }
    
    @Override
    public void end_tilde_class_name(Token token) {
        super.end_tilde_class_name(token);
        printOut("tilde_class_name", token); //NOI18N
    }

    @Override
    public void alias_declaration(Token usingToken, Token identToken, Token assignequalToken) {
        printIn("alias_declaration", usingToken, identToken, assignequalToken); //NOI18N
        super.alias_declaration(usingToken, identToken, assignequalToken);
    }

    @Override
    public void end_alias_declaration(Token token) {
        super.end_alias_declaration(token);
        printOut("alias_declaration", token); //NOI18N
    }

    @Override
    public void function_specifier(int kind, Token token) {
        print("function_specifier", token); //NOI18N
        super.function_specifier(kind, token);
    }

    @Override
    public void type_specifier(Token token) {
        printIn("type_specifier", token); //NOI18N
        super.type_specifier(token);
    }

    @Override
    public void end_type_specifier(Token token) {
        super.end_type_specifier(token);
        printOut("type_specifier", token); //NOI18N
    }

    @Override
    public void trailing_type_specifier(Token token) {
        printIn("trailing_type_specifier", token); //NOI18N
        super.trailing_type_specifier(token);
    }

    @Override
    public void end_trailing_type_specifier(Token token) {
        super.end_trailing_type_specifier(token);
        printOut("trailing_type_specifier", token); //NOI18N
    }

    @Override
    public void decltype_specifier(Token token) {
        printIn("decltype_specifier", token); //NOI18N
        super.decltype_specifier(token);
    }

    @Override
    public void decltype_specifier(int kind, Token token) {
        print("decltype_specifier", token); //NOI18N
        super.decltype_specifier(kind, token);
    }

    @Override
    public void end_decltype_specifier(Token token) {
        super.end_decltype_specifier(token);
        printOut("decltype_specifier", token); //NOI18N
    }

    @Override
    public void end_elaborated_type_specifier(Token token) {
        super.end_elaborated_type_specifier(token);
        printOut("elaborated_type_specifier", token); //NOI18N
    }

    @Override
    public void typename_specifier(Token token) {
        printIn("typename_specifier", token); //NOI18N
        super.typename_specifier(token);
    }

    @Override
    public void end_typename_specifier(Token token) {
        super.end_typename_specifier(token);
        printOut("typename_specifier", token); //NOI18N
    }

    @Override
    public void asm_definition(Token asmToken, Token lparenToken, Token stringToken, Token rparenToken, Token semicolonToken) {
        print("asm_definition", lparenToken, stringToken, rparenToken, semicolonToken); //NOI18N
        super.asm_definition(asmToken, lparenToken, stringToken, rparenToken, semicolonToken);
    }

    @Override
    public void linkage_specification(Token externToken, Token stringToken) {
        printIn("linkage_specification", externToken, stringToken); //NOI18N
        super.linkage_specification(externToken, stringToken);
    }

    @Override
    public void linkage_specification(int kind, Token token) {
        print("linkage_specification", token); //NOI18N
        super.linkage_specification(kind, token);
    }

    @Override
    public void end_linkage_specification(Token token) {
        super.end_linkage_specification(token);
        printOut("linkage_specification", token); //NOI18N
    }

    @Override
    public void init_declarator_list(Token token) {
        printIn("init_declarator_list", token); //NOI18N
        super.init_declarator_list(token);
    }

    @Override
    public void init_declarator_list(int kind, Token token) {
        print("init_declarator_list", token); //NOI18N
        super.init_declarator_list(kind, token);
    }

    @Override
    public void end_init_declarator_list(Token token) {
        super.end_init_declarator_list(token);
        printOut("init_declarator_list", token); //NOI18N
    }

    @Override
    public void init_declarator(Token token) {
        printIn("init_declarator", token); //NOI18N
        super.init_declarator(token);
    }

    @Override
    public void end_init_declarator(Token token) {
        super.end_init_declarator(token);
        printOut("init_declarator", token); //NOI18N
    }

    @Override
    public void declarator(Token token) {
        printIn("declarator", token); //NOI18N
        super.declarator(token);
    }

    @Override
    public void end_declarator(Token token) {
        super.end_declarator(token);
        printOut("declarator", token); //NOI18N
    }

    @Override
    public void noptr_declarator(Token token) {
        printIn("noptr_declarator", token); //NOI18N
        super.noptr_declarator(token);
    }

    @Override
    public void noptr_declarator(int kind, Token token) {
        print("noptr_declarator", token); //NOI18N
        super.noptr_declarator(kind, token);
    }

    @Override
    public void end_noptr_declarator(Token token) {
        super.end_noptr_declarator(token);
        printOut("noptr_declarator", token); //NOI18N
    }

    @Override
    public void function_declarator(Token token) {
        printIn("function_declarator", token); //NOI18N
        super.function_declarator(token);
    }

    @Override
    public void end_function_declarator(Token token) {
        super.end_function_declarator(token);
        printOut("function_declarator", token); //NOI18N
    }

    @Override
    public void constructor_declarator(Token token) {
        printIn("constructor_declarator", token); //NOI18N
        super.constructor_declarator(token);
    }

    @Override
    public void end_constructor_declarator(Token token) {
        super.end_constructor_declarator(token);
        printOut("constructor_declarator", token); //NOI18N
    }

    @Override
    public void function_declarator(int kind, Token token) {
        print("function_declarator", token); //NOI18N
        super.function_declarator(kind, token);
    }

    @Override
    public void noptr_abstract_declarator(Token token) {
        printIn("noptr_abstract_declarator", token); //NOI18N
        super.noptr_abstract_declarator(token);
    }

    @Override
    public void noptr_abstract_declarator(int kind, Token token) {
        print("noptr_abstract_declarator", token); //NOI18N
        super.noptr_abstract_declarator(kind, token);
    }

    @Override
    public void end_noptr_abstract_declarator(Token token) {
        super.end_noptr_abstract_declarator(token);
        printOut("noptr_abstract_declarator", token); //NOI18N
    }

    @Override
    public void universal_declarator(Token token) {
        printIn("universal_declarator", token); //NOI18N
        super.universal_declarator(token);
    }

    @Override
    public void end_universal_declarator(Token token) {
        super.end_universal_declarator(token);
        printOut("universal_declarator", token); //NOI18N
    }

    @Override
    public void greedy_declarator(Token token) {
        printIn("greedy_declarator", token); //NOI18N
        super.greedy_declarator(token);
    }

    @Override
    public void end_greedy_declarator(Token token) {
        super.end_greedy_declarator(token);
        printOut("greedy_declarator", token); //NOI18N
    }

    @Override
    public void greedy_nonptr_declarator(Token token) {
        printIn("greedy_nonptr_declarator", token); //NOI18N
        super.greedy_nonptr_declarator(token);
    }

    @Override
    public void greedy_nonptr_declarator(int kind, Token token) {
        print("greedy_nonptr_declarator", token); //NOI18N
        super.greedy_nonptr_declarator(kind, token);
    }

    @Override
    public void end_greedy_nonptr_declarator(Token token) {
        super.end_greedy_nonptr_declarator(token);
        printOut("greedy_nonptr_declarator", token); //NOI18N
    }

    @Override
    public void ptr_operator(Token token) {
        printIn("ptr_operator", token); //NOI18N
        super.ptr_operator(token);
    }

    @Override
    public void ptr_operator(int kind, Token token) {
        print("ptr_operator", token); //NOI18N
        super.ptr_operator(kind, token);
    }

    @Override
    public void end_ptr_operator(Token token) {
        super.end_ptr_operator(token);
        printOut("ptr_operator", token); //NOI18N
    }

    @Override
    public void cv_qualifier(int kind, Token token) {
        print("cv_qualifier", token); //NOI18N
        super.cv_qualifier(kind, token);
    }

    @Override
    public void ref_qualifier(int kind, Token token) {
        print("ref_qualifier", token); //NOI18N
        super.ref_qualifier(kind, token);
    }

    @Override
    public void declarator_id(Token token) {
        printIn("declarator_id", token); //NOI18N
        super.declarator_id(token);
    }

    @Override
    public void declarator_id(int kind, Token token) {
        print("declarator_id", token); //NOI18N
        super.declarator_id(kind, token);
    }

    @Override
    public void end_declarator_id(Token token) {
        super.end_declarator_id(token);
        printOut("declarator_id", token); //NOI18N
    }

    @Override
    public void type_id(Token token) {
        printIn("type_id", token); //NOI18N
        super.type_id(token);
    }

    @Override
    public void end_type_id(Token token) {
        super.end_type_id(token);
        printOut("type_id", token); //NOI18N
    }

    @Override
    public void parameters_and_qualifiers(Token token) {
        printIn("parameters_and_qualifiers", token); //NOI18N
        super.parameters_and_qualifiers(token);
    }

    @Override
    public void parameters_and_qualifiers(int kind, Token token) {
        print("parameters_and_qualifiers", token); //NOI18N
        super.parameters_and_qualifiers(kind, token);
    }

    @Override
    public void end_parameters_and_qualifiers(Token token) {
        super.end_parameters_and_qualifiers(token);
        printOut("parameters_and_qualifiers", token); //NOI18N
    }

    @Override
    public void parameter_declaration_clause(Token token) {
        printIn("parameter_declaration_clause", token); //NOI18N
        super.parameter_declaration_clause(token);
    }

    @Override
    public void parameter_declaration_clause(int kind, Token token){
        print("parameter_declaration_clause", token); //NOI18N
        super.parameter_declaration_clause(kind, token);
    }

    @Override
    public void end_parameter_declaration_clause(Token token) {
        super.end_parameter_declaration_clause(token);
        printOut("parameter_declaration_clause", token); //NOI18N
    }

    @Override
    public void parameter_declaration_list(Token token) {
        printIn("parameter_declaration_list", token); //NOI18N
        super.parameter_declaration_list(token);
    }

    @Override
    public void end_parameter_declaration_list(int kind, Token token) {
        print("end_parameter_declaration_list", token); //NOI18N
        super.end_parameter_declaration_list(kind, token);
    }

    @Override
    public void end_parameter_declaration_list(Token token) {
        super.end_parameter_declaration_list(token);
        printOut("parameter_declaration_list", token); //NOI18N
    }

    @Override
    public void parameter_declaration(Token token) {
        printIn("parameter_declaration", token); //NOI18N
        super.parameter_declaration(token);
    }
    
    @Override
    public void parameter_declaration(int kind, Token token) {
        print("parameter_declaration", token); //NOI18N
        super.parameter_declaration(kind, token);
    }

    @Override
    public void end_parameter_declaration(Token token) {
        super.end_parameter_declaration(token);
        printOut("parameter_declaration", token); //NOI18N
    }

    @Override
    public void function_definition_after_declarator(Token token){
        printIn("function_definition_after_declarator", token); //NOI18N
        super.function_definition_after_declarator(token);
    }

    @Override
    public void function_definition_after_declarator(int kind, Token token) {
        print("function_definition_after_declarator", token); //NOI18N
        super.function_definition_after_declarator(kind, token);
    }

    @Override
    public void end_function_definition_after_declarator(Token token) {
        super.end_function_definition_after_declarator(token);
        printOut("function_definition_after_declarator", token); //NOI18N
    }

    @Override
    public void function_declaration(Token token) {
        printIn("function_declaration", token); //NOI18N
        super.function_declaration(token);
    }

    @Override
    public void end_function_declaration(Token token) {
        super.end_function_declaration(token);
        printOut("function_declaration", token); //NOI18N
    }

    @Override
    public void function_definition(Token token) {
        printIn("function_definition", token); //NOI18N
        super.function_definition(token);
    }

    @Override
    public void end_function_definition(Token token) {
        super.end_function_definition(token);
        printOut("function_definition", token); //NOI18N
    }

    @Override
    public void function_body(Token token) {
        printIn("function_body", token); //NOI18N
        super.function_body(token);
    }

    @Override
    public void end_function_body(Token token) {
        super.end_function_body(token);
        printOut("function_body", token); //NOI18N
    }

    @Override
    public void initializer(Token token) {
        printIn("initializer", token); //NOI18N
        super.initializer(token);
    }

    @Override
    public void initializer(int kind, Token token) {
        print("initializer", token); //NOI18N
        super.initializer(kind, token);
    }

    @Override
    public void end_initializer(Token token) {
        super.end_initializer(token);
        printOut("initializer", token); //NOI18N
    }

    @Override
    public void brace_or_equal_initializer(Token token) {
        printIn("brace_or_equal_initializer", token); //NOI18N
        super.brace_or_equal_initializer(token);
    }

    @Override
    public void brace_or_equal_initializer(int kind, Token token) {
        print("brace_or_equal_initializer", token); //NOI18N
        super.brace_or_equal_initializer(kind, token);
    }

    @Override
    public void end_brace_or_equal_initializer(Token token) {
        super.end_brace_or_equal_initializer(token);
        printOut("brace_or_equal_initializer", token); //NOI18N
    }

    @Override
    public void initializer_clause(Token token) {
        printIn("initializer_clause", token); //NOI18N
        super.initializer_clause(token);
   }

    @Override
    public void end_initializer_clause(Token token) {
        super.end_initializer_clause(token);
        printOut("initializer_clause", token); //NOI18N
    }

    @Override
    public void initializer_list(Token token) {
        printIn("initializer_list", token); //NOI18N
        super.initializer_list(token);
    }

    @Override
    public void initializer_list(int kind, Token token) {
        print("initializer_list", token); //NOI18N
        super.initializer_list(kind, token);
    }

    @Override
    public void end_initializer_list(Token token) {
        super.end_initializer_list(token);
        printOut("initializer_list", token); //NOI18N
    }

    @Override
    public void braced_init_list(Token token) {
        printIn("braced_init_list", token); //NOI18N
        super.braced_init_list(token);
    }

    @Override
    public void braced_init_list(int kind, Token token) {
        print("braced_init_list", token); //NOI18N
        super.braced_init_list(kind, token);
    }

    @Override
    public void end_braced_init_list(Token token) {
        super.end_braced_init_list(token);
        printOut("braced_init_list", token); //NOI18N
    }

    @Override
    public void optionally_qualified_name(Token token) {
        printIn("optionally_qualified_name", token); //NOI18N
        super.optionally_qualified_name(token);
    }

    @Override
    public void end_optionally_qualified_name(Token token) {
        super.end_optionally_qualified_name(token);
        printOut("optionally_qualified_name", token); //NOI18N
    }

    @Override
    public void class_head(Token token) {
        printIn("class_head", token); //NOI18N
        super.class_head(token);
    }

    @Override
    public void end_class_head(Token token) {
        super.end_class_head(token);
        printOut("class_head", token); //NOI18N
    }

    @Override
    public void class_virtual_specifier(int kind, Token token) {
        print("class_virtual_specifier", token); //NOI18N
        super.class_virtual_specifier(kind, token);
    }

    @Override
    public void member_specification(Token token) {
        printIn("member_specification", token); //NOI18N
        super.member_specification(token);
    }

    @Override
    public void member_specification(int kind, Token token) {
        print("member_specification", token); //NOI18N
        super.member_specification(kind, token);
    }

    @Override
    public void end_member_specification(Token token) {
        super.end_member_specification(token);
        printOut("member_specification", token); //NOI18N
    }

    @Override
    public void member_declaration(Token token) {
        printIn("member_declaration", token); //NOI18N
        super.member_declaration(token);
    }

    @Override
    public void member_declaration(int kind, Token token) {
        print("member_declaration", token); //NOI18N
        super.member_declaration(kind, token);
    }

    @Override
    public void end_member_declaration(Token token) {
        super.end_member_declaration(token);
        printOut("member_declaration", token); //NOI18N
    }

    @Override
    public void simple_member_declaration(Token token) {
        printIn("simple_member_declaration", token); //NOI18N
        super.simple_member_declaration(token);
    }

    @Override
    public void simple_member_declaration(int kind, Token token) {
        print("simple_member_declaration", token); //NOI18N
        super.simple_member_declaration(kind, token);
    }

    @Override
    public void end_simple_member_declaration(Token token) {
        super.end_simple_member_declaration(token);
        printOut("simple_member_declaration", token); //NOI18N
    }

    @Override
    public void member_declarator(Token token) {
        printIn("member_declarator", token); //NOI18N
        super.member_declarator(token);
    }

    @Override
    public void end_member_declarator(Token token) {
        super.end_member_declarator(token);
        printOut("member_declarator", token); //NOI18N
    }
    
    @Override 
    public void member_bitfield_declarator(Token token) {
        super.end_member_declarator(token);
        printOut("member_bitfield_declarator", token); //NOI18N
    }

    @Override
    public void pure_specifier(Token token) {
        printIn("pure_specifier", token); //NOI18N
        super.pure_specifier(token);
    }

    @Override
    public void end_pure_specifier(Token token) {
        super.end_pure_specifier(token);
        printOut("pure_specifier", token); //NOI18N
    }

    @Override
    public void constant_initializer(Token token) {
        printIn("constant_initializer", token); //NOI18N
        super.constant_initializer(token);
    }

    @Override
    public void end_constant_initializer(Token token) {
        super.end_constant_initializer(token);
        printOut("constant_initializer", token); //NOI18N
    }

    @Override
    public void virt_specifier(int kind, Token token) {
        print("virt_specifier", token); //NOI18N
        super.virt_specifier(kind, token);
    }

    @Override
    public void base_clause(Token token) {
        printIn("base_clause", token); //NOI18N
        super.base_clause(token);
    }

    @Override
    public void end_base_clause(Token token) {
        super.end_base_clause(token);
        printOut("base_clause", token); //NOI18N
    }

    @Override
    public void base_specifier_list(Token token) {
        printIn("base_specifier_list", token); //NOI18N
        super.base_specifier_list(token);
    }

    @Override
    public void base_specifier_list(int kind, Token token) {
        print("base_specifier_list", token); //NOI18N
        super.base_specifier_list(kind, token);
    }

    @Override
    public void end_base_specifier_list(Token token) {
        super.end_base_specifier_list(token);
        printOut("base_specifier_list", token); //NOI18N
    }

    @Override
    public void class_or_decltype(Token token) {
        printIn("class_or_decltype", token); //NOI18N
        super.class_or_decltype(token);
    }

    @Override
    public void class_or_decltype(int kind, Token token) {
        print("class_or_decltype", token); //NOI18N
        super.class_or_decltype(kind, token);
    }

    @Override
    public void end_class_or_decltype(Token token) {
        super.end_class_or_decltype(token);
        printOut("class_or_decltype", token); //NOI18N
    }

    @Override
    public void base_type_specifier(Token token) {
        printIn("base_type_specifier", token); //NOI18N
        super.base_type_specifier(token);
    }

    @Override
    public void end_base_type_specifier(Token token) {
        super.end_base_type_specifier(token);
        printOut("base_type_specifier", token); //NOI18N
    }

    @Override
    public void access_specifier(int kind, Token token) {
        print("access_specifier", token); //NOI18N
        super.access_specifier(kind, token);
    }

    @Override
    public void conversion_function_id(Token token) {
        printIn("conversion_function_id", token); //NOI18N
        super.conversion_function_id(token);
    }

    @Override
    public void end_conversion_function_id(Token token) {
        super.end_conversion_function_id(token);
        printOut("conversion_function_id", token); //NOI18N
    }

    @Override
    public void conversion_type_id(Token token) {
        printIn("conversion_type_id", token); //NOI18N
        super.conversion_type_id(token);
    }

    @Override
    public void end_conversion_type_id(Token token) {
        super.end_conversion_type_id(token);
        printOut("conversion_type_id", token); //NOI18N
    }

    @Override
    public void ctor_initializer(Token token) {
        printIn("ctor_initializer", token); //NOI18N
        super.ctor_initializer(token);
    }

    @Override
    public void end_ctor_initializer(Token token) {
        super.end_ctor_initializer(token);
        printOut("ctor_initializer", token); //NOI18N
    }

    @Override
    public void mem_initializer_list(Token token) {
        printIn("mem_initializer_list", token); //NOI18N
        super.mem_initializer_list(token);
    }

    @Override
    public void mem_initializer_list(int kind, Token token) {
        print("mem_initializer_list", token); //NOI18N
        super.mem_initializer_list(kind, token);
    }

    @Override
    public void end_mem_initializer_list(Token token) {
        super.end_mem_initializer_list(token);
        printOut("mem_initializer_list", token); //NOI18N
    }

    @Override
    public void mem_initializer(Token token) {
        printIn("mem_initializer", token); //NOI18N
        super.mem_initializer(token);
    }

    @Override
    public void mem_initializer(int kind, Token token) {
        print("mem_initializer", token); //NOI18N
        super.mem_initializer(kind, token);
    }

    @Override
    public void end_mem_initializer(Token token) {
        super.end_mem_initializer(token);
        printOut("mem_initializer", token); //NOI18N
    }

    @Override
    public void mem_initializer_id(Token token) {
        printIn("mem_initializer_id", token); //NOI18N
        super.mem_initializer_id(token);
    }

    @Override
    public void end_mem_initializer_id(Token token) {
        super.end_mem_initializer_id(token);
        printOut("mem_initializer_id", token); //NOI18N
    }

    @Override
    public void mem_operator_function_id(Token token) {
        printIn("mem_operator_function_id", token); //NOI18N
        super.mem_operator_function_id(token);
    }

    @Override
    public void operator_function_id(int kind, Token token) {
        print("operator_function_id", token); //NOI18N
        super.operator_function_id(kind, token);
    }

    @Override
    public void end_operator_function_id(Token token) {
        super.end_operator_function_id(token);
        printOut("mem_operator_function_id", token); //NOI18N
    }

    @Override
    public void operator_id(Token token) {
        printIn("operator_id", token); //NOI18N
        super.operator_id(token);
    }

    @Override
    public void end_operator_id(Token token) {
        super.end_operator_id(token);
        printOut("operator_id", token); //NOI18N
    }

    @Override
    public void literal_operator_id(Token operatorToken, Token stringToken, Token identToken) {
        print("literal_operator_id", operatorToken, stringToken, identToken); //NOI18N
        super.literal_operator_id(operatorToken, stringToken, identToken);
    }

    @Override
    public void template_declaration(Token token) {
        printIn("template_declaration", token); //NOI18N
        super.template_declaration(token);
    }

    @Override
    public void template_declaration(int kind, Token token) {
        print("template_declaration", token); //NOI18N
        super.template_declaration(kind, token);
    }

    @Override
    public void end_template_declaration(Token token) {
        super.end_template_declaration(token);
        printOut("template_declaration", token); //NOI18N
    }

    @Override
    public void template_parameter_list(Token token) {
        printIn("template_parameter_list", token); //NOI18N
        super.template_parameter_list(token);
    }

    @Override
    public void template_parameter_list(int kind, Token token) {
        print("template_parameter_list", token); //NOI18N
        super.template_parameter_list(kind, token);
    }

    @Override
    public void end_template_parameter_list(Token token) {
        super.end_template_parameter_list(token);
        printOut("template_parameter_list", token); //NOI18N
    }

    @Override
    public void template_parameter(Token token) {
        printIn("template_parameter", token); //NOI18N
        super.template_parameter(token);
    }

    @Override
    public void end_template_parameter(Token token) {
        super.end_template_parameter(token);
        printOut("template_parameter", token); //NOI18N
    }

    @Override
    public void template_argument_list(Token token) {
        printIn("template_argument_list", token); //NOI18N
        super.template_argument_list(token);
    }

    @Override
    public void template_argument_list(int kind, Token token) {
        print("template_argument_list", token); //NOI18N
        super.template_argument_list(token);
    }

    @Override
    public void end_template_argument_list(Token token) {
        super.end_template_argument_list(token);
        printOut("template_argument_list", token); //NOI18N
    }

    @Override
    public void template_argument(Token token) {
        printIn("template_argument", token); //NOI18N
        super.template_argument(token);
    }

    @Override
    public void end_template_argument(Token token) {
        super.end_template_argument(token);
        printOut("template_argument", token); //NOI18N
    }

    @Override
    public void explicit_instantiation(Token token) {
        printIn("explicit_instantiation", token); //NOI18N
        super.explicit_instantiation(token);
    }

    @Override
    public void explicit_instantiation(int kind, Token token) {
        print("explicit_instantiation", token); //NOI18N
        super.explicit_instantiation(kind, token);
    }

    @Override
    public void end_explicit_instantiation(Token token) {
        super.end_explicit_instantiation(token);
        printOut("explicit_instantiation", token); //NOI18N
    }

    @Override
    public void explicit_specialization(Token templateToken, Token lessthenToken, Token greaterthenToken) {
        printIn("explicit_specialization", templateToken, lessthenToken, greaterthenToken); //NOI18N
        super.explicit_specialization(templateToken, lessthenToken, greaterthenToken);
    }

    @Override
    public void end_explicit_specialization(Token token) {
        super.end_explicit_specialization(token);
        printOut("explicit_specialization", token); //NOI18N
    }

    @Override
    public void try_block(Token token) {
        printIn("try_block", token); //NOI18N
        super.try_block(token);
    }

    @Override
    public void end_try_block(Token token) {
        super.end_try_block(token);
        printOut("try_block", token); //NOI18N
    }

    @Override
    public void function_try_block(Token token) {
        printIn("function_try_block", token); //NOI18N
        super.function_try_block(token);
    }

    @Override
    public void end_function_try_block(Token token) {
        super.end_function_try_block(token);
        printOut("function_try_block", token); //NOI18N
    }

    @Override
    public void handler(Token token) {
        printIn("handler", token); //NOI18N
        super.handler(token);
    }

    @Override
    public void handler(int kind, Token token) {
        print("handler", token); //NOI18N
        super.handler(kind, token);
    }

    @Override
    public void end_handler(Token token) {
        super.end_handler(token);
        printOut("handler", token); //NOI18N
    }

    @Override
    public void assignment_expression(Token token) {
        printIn("assignment_expression", token); //NOI18N
        super.assignment_expression(token);
    }

    @Override
    public void end_assignment_expression(Token token) {
        super.end_assignment_expression(token);
        printOut("assignment_expression", token); //NOI18N
    }

    @Override
    public void expression(Token token) {
        printIn("expression", token); //NOI18N
        super.expression(token);
    }

    @Override
    public void end_expression(Token token) {
        super.end_expression(token);
        printOut("expression", token); //NOI18N
    }

    @Override
    public void constant_expression(Token token) {
        printIn("constant_expression", token); //NOI18N
        super.constant_expression(token);
    }

    @Override
    public void end_constant_expression(Token token) {
        super.end_constant_expression(token);
        printOut("constant_expression", token); //NOI18N
    }

    @Override
    public void skip_balanced_curlies(Token token) {
        super.skip_balanced_curlies(token);
    }    

    @Override
    public void pushFile(CsmFile file) {
        super.pushFile(file);
    }

    @Override
    public CsmFile popFile() {
        return super.popFile();
    }
    
    private void printIn(String message, Token ... token) {
        traceWriter.printIn(message, token);
    }

    private void printOut(String message, Token ... token) {
        traceWriter.printOut(message, token);
    }

    private void print(String message, Token ... token) {
        traceWriter.print(message, token);
    }
}
