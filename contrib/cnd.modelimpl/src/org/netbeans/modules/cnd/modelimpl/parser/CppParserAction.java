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

/**
 *
 */
public interface CppParserAction extends CsmParserProvider.CsmParseCallback {
    
    public static int LABELED_STATEMENT__LABEL = 1;
    public static int LABELED_STATEMENT__CASE = 2;
    public static int LABELED_STATEMENT__CASE_COLON = 3;
    public static int LABELED_STATEMENT__DEFAULT = 4;
    public static int SELECTION_STATEMENT__IF = 5;
    public static int SELECTION_STATEMENT__IF_LPAREN = 6;
    public static int SELECTION_STATEMENT__IF_RPAREN = 7;
    public static int SELECTION_STATEMENT__ELSE = 8;
    public static int SELECTION_STATEMENT__SWITCH = 9;
    public static int SELECTION_STATEMENT__SWITCH_LPAREN = 10;
    public static int SELECTION_STATEMENT__SWITCH_RPAREN = 11;
    public static int CONDITION__EQUAL = 12;
    public static int ITERATION_STATEMENT__WHILE = 13;
    public static int ITERATION_STATEMENT__WHILE_LPAREN = 14;
    public static int ITERATION_STATEMENT__WHILE_RPAREN = 15;
    public static int ITERATION_STATEMENT__DO = 16;
    public static int ITERATION_STATEMENT__DO_WHILE = 17;
    public static int ITERATION_STATEMENT__DO_WHILE_LPAREN = 18;
    public static int ITERATION_STATEMENT__DO_WHILE_RPAREN = 19;
    public static int ITERATION_STATEMENT__FOR = 20;
    public static int ITERATION_STATEMENT__FOR_LPAREN = 21;
    public static int ITERATION_STATEMENT__FOR_COLON = 22;
    public static int ITERATION_STATEMENT__FOR_SEMICOLON = 23;
    public static int ITERATION_STATEMENT__FOR_RPAREN = 24;
    public static int JUMP_STATEMENT__BREAK = 25;
    public static int JUMP_STATEMENT__CONTINUE = 26;
    public static int JUMP_STATEMENT__RETURN = 27;
    public static int JUMP_STATEMENT__GOTO = 28;
    public static int SIMPLE_DECLARATION__COMMA2 = 29;
    public static int SIMPLE_DECLARATION__SEMICOLON = 30;
    public static int DECL_SPECIFIER__STORAGE_CLASS_SPECIFIER = 33;
    public static int DECL_SPECIFIER__FUNCTION_SPECIFIER = 34;
    public static int DECL_SPECIFIER__LITERAL_FRIEND = 35;
    public static int DECL_SPECIFIER__LITERAL_TYPEDEF = 36;
    public static int DECL_SPECIFIER__TYPE_SPECIFIER = 37;
    public static int DECL_SPECIFIER__LITERAL_CONSTEXPR = 38;
    public static int STORAGE_CLASS_SPECIFIER__REGISTER = 39;
    public static int STORAGE_CLASS_SPECIFIER__STATIC = 40;
    public static int STORAGE_CLASS_SPECIFIER__EXTERN = 41;
    public static int STORAGE_CLASS_SPECIFIER__MUTABLE = 42;
    public static int STORAGE_CLASS_SPECIFIER____THREAD = 43;
    public static int STORAGE_CLASS_SPECIFIER__THREAD_LOCAL = 44;
    public static int FUNCTION_SPECIFIER__INLINE = 45;
    public static int FUNCTION_SPECIFIER__VIRTUAL = 46;
    public static int FUNCTION_SPECIFIER__EXPLICIT = 47;
    public static int SIMPLE_TYPE_SPECIFIER__CHAR = 1;
    public static int SIMPLE_TYPE_SPECIFIER__WCHAR_T = 2;
    public static int SIMPLE_TYPE_SPECIFIER__CHAR16_T = 3;
    public static int SIMPLE_TYPE_SPECIFIER__CHAR32_T = 4;
    public static int SIMPLE_TYPE_SPECIFIER__BOOL = 5;
    public static int SIMPLE_TYPE_SPECIFIER__SHORT = 6;
    public static int SIMPLE_TYPE_SPECIFIER__INT = 7;
    public static int SIMPLE_TYPE_SPECIFIER__LONG = 8;
    public static int SIMPLE_TYPE_SPECIFIER__SIGNED = 9;
    public static int SIMPLE_TYPE_SPECIFIER__UNSIGNED = 10;
    public static int SIMPLE_TYPE_SPECIFIER__FLOAT = 11;
    public static int SIMPLE_TYPE_SPECIFIER__DOUBLE = 12;
    public static int SIMPLE_TYPE_SPECIFIER__VOID = 13;
    public static int SIMPLE_TYPE_SPECIFIER__AUTO = 14;
    public static int SIMPLE_TYPE_SPECIFIER__ID = 15;
    public static int SIMPLE_TYPE_SPECIFIER__SCOPE = 16;
    public static int SIMPLE_TYPE_SPECIFIER__BI_VA_LIST = 17;
    public static int DECLTYPE_SPECIFIER__LPAREN = 48;
    public static int DECLTYPE_SPECIFIER__RPAREN = 49;
    public static int QUALIFIED_NAMESPACE_SPECIFIER__SCOPE = 50;
    public static int QUALIFIED_NAMESPACE_SPECIFIER__IDENT = 51;
    public static int USING_DECLARATION__TYPENAME = 52;
    public static int USING_DECLARATION__SCOPE = 53;
    public static int USING_DIRECTIVE__SCOPE = 54;
    public static int USING_DIRECTIVE__IDENT = 55;
    public static int LINKAGE_SPECIFICATION__LCURLY = 56;
    public static int LINKAGE_SPECIFICATION__RCURLY = 57;
    public static int INIT_DECLARATOR_LIST__COMMA = 58;
    public static int NOPTR_DECLARATOR__LPAREN = 59;
    public static int NOPTR_DECLARATOR__RPAREN = 60;
    public static int NOPTR_DECLARATOR__LSQUARE = 61;
    public static int NOPTR_DECLARATOR__RSQUARE = 62;
    public static int FUNCTION_DECLARATOR__ELLIPSIS = 63;
    public static int NOPTR_ABSTRACT_DECLARATOR__LSQUARE = 64;
    public static int NOPTR_ABSTRACT_DECLARATOR__RSQUARE = 65;
    public static int NOPTR_ABSTRACT_DECLARATOR__LPAREN = 66;
    public static int NOPTR_ABSTRACT_DECLARATOR__RPAREN = 67;
    public static int GREEDY_NONPTR_DECLARATOR__LPAREN = 70;
    public static int GREEDY_NONPTR_DECLARATOR__RPAREN = 71;
    public static int GREEDY_NONPTR_DECLARATOR__LSQUARE = 72;
    public static int GREEDY_NONPTR_DECLARATOR__RSQUARE = 73;
    public static int PTR_OPERATOR__STAR = 74;
    public static int PTR_OPERATOR__AMPERSAND = 75;
    public static int PTR_OPERATOR__AND = 76;
    public static int PTR_OPERATOR__SCOPE = 77;
    public static int PTR_OPERATOR__STAR2 = 78;
    public static int CV_QUALIFIER__CONST = 79;
    public static int CV_QUALIFIER__VOLATILE = 80;
    public static int REF_QUALIFIER__AMPERSAND = 81;
    public static int REF_QUALIFIER__AND = 82;
    public static int DECLARATOR_ID__ELLIPSIS = 83;
    public static int PARAMETERS_AND_QUALIFIERS__LPAREN = 84;
    public static int PARAMETERS_AND_QUALIFIERS__RPAREN = 85;
    public static int PARAMETER_DECLARATION_CLAUSE__ELLIPSIS = 86;
    public static int PARAMETER_DECLARATION_CLAUSE__COMMA = 87;
    public static int PARAMETER_DECLARATION_CLAUSE__ELLIPSIS2 = 88;
    public static int PARAMETER_DECLARATION_LIST__COMMA = 89;
    public static int PARAMETER_DECLARATION__ASSIGNEQUAL = 90;
    public static int FUNCTION_DEFINITION_AFTER_DECLARATOR__ASSIGNEQUAL = 91;
    public static int FUNCTION_DEFINITION_AFTER_DECLARATOR__DELETE = 92;
    public static int FUNCTION_DEFINITION_AFTER_DECLARATOR__DEFAULT = 93;
    public static int INITIALIZER__LPAREN = 94;
    public static int INITIALIZER__RPAREN = 95;
    public static int BRACE_OR_EQUAL_INITIALIZER__ASSIGNEQUAL = 96;
    public static int INITIALIZER_LIST__COMMA = 97;
    public static int BRACED_INIT_LIST__COMMA = 98;
    public static int CLASS_VIRTUAL_SPECIFIER__FINAL = 99;
    public static int CLASS_VIRTUAL_SPECIFIER__EXPLICIT = 100;
    public static int MEMBER_SPECIFICATION__COLON = 101;
    public static int SIMPLE_MEMBER_DECLARATION__COMMA2 = 101;
    public static int SIMPLE_MEMBER_DECLARATION__SEMICOLON = 102;    
    public static int VIRT_SPECIFIER__OVERRIDE = 102;
    public static int VIRT_SPECIFIER__FINAL = 103;
    public static int VIRT_SPECIFIER__NEW = 104;
    public static int BASE_SPECIFIER_LIST__ELLIPSIS = 105;
    public static int BASE_SPECIFIER_LIST__COMMA = 106;
    public static int CLASS_OR_DECLTYPE__SCOPE = 108;
    public static int ACCESS_SPECIFIER__PRIVATE = 109;
    public static int ACCESS_SPECIFIER__PROTECTED = 110;
    public static int ACCESS_SPECIFIER__PUBLIC = 111;
    public static int MEM_INITIALIZER_LIST__ELLIPSIS = 112;
    public static int MEM_INITIALIZER_LIST__COMMA = 113;
    public static int MEM_INITIALIZER__LPAREN = 115;
    public static int MEM_INITIALIZER__RPAREN = 116;
    public static int OPERATOR_FUNCTION_ID__LESSTHAN = 117;
    public static int OPERATOR_FUNCTION_ID__GREATERTHAN = 118;
    public static int TEMPLATE_DECLARATION__EXPORT = 119;
    public static int TEMPLATE_DECLARATION__TEMPLATE = 120;
    public static int TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST = 121;
    public static int TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST = 122;
    public static int TEMPLATE_PARAMETER_LIST__COMMA = 123;
    public static int TYPE_PARAMETER__CLASS = 124;
    public static int TYPE_PARAMETER__CLASS_ASSIGNEQUAL = 125;
    public static int TYPE_PARAMETER__TYPENAME = 126;
    public static int TYPE_PARAMETER__TYPENAME_ASSIGNEQUAL = 127;
    public static int TYPE_PARAMETER__TEMPLATE_CLASS_ASSIGNEQUAL = 128;
    public static int SIMPLE_TEMPLATE_ID__TEMPLATE_ARGUMENT_LIST = 129;
    public static int SIMPLE_TEMPLATE_ID__END_TEMPLATE_ARGUMENT_LIST = 130;
    public static int SIMPLE_TEMPLATE_ID_NOCHECK__TEMPLATE_ARGUMENT_LIST = 131;
    public static int SIMPLE_TEMPLATE_ID_NOCHECK__END_TEMPLATE_ARGUMENT_LIST = 132;
    public static int SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST = 133;
    public static int SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST = 134;
    public static int TEMPLATE_ARGUMENT_LIST__ELLIPSIS = 135;
    public static int TEMPLATE_ARGUMENT_LIST__COMMA = 136;
    public static int EXPLICIT_INSTANTIATION__EXTERN = 137;
    public static int EXPLICIT_INSTANTIATION__TEMPLATE = 138;
    public static int HANDLER__LPAREN = 139;
    public static int HANDLER__RPAREN = 140;
    
    boolean type_specifier_already_present(TokenStream input);
    
    boolean identifier_is(int kind, Token token);
    
    boolean top_level_of_template_arguments();
    
    void enum_declaration(Token token);
    void enum_strongly_typed(Token token);
    void enum_name(Token token);
    void enum_body(Token token);
    void enumerator(Token token);
    void end_enum_body(Token token);
    void end_enum_declaration(Token token);

    void class_declaration(Token token);
    void class_kind(Token token);
    void class_name(Token token);
    void class_body(Token token);
    void end_class_body(Token token);
    void end_class_declaration(Token token);
    
    void namespace_declaration(Token token);
    void namespace_name(Token token);
    void namespace_body(Token token);    
    void end_namespace_body(Token token);
    void end_namespace_declaration(Token token);

    void compound_statement(Token token);
    void end_compound_statement(Token token);
    
    void simple_declaration(Token token);
    void simple_declaration(int kind, Token token);    
    void end_simple_declaration(Token token);
    
    void decl_specifier(int kind, Token token);
    
    void simple_type_specifier(Token token);
    void simple_type_specifier(int kind, Token token);
    void end_simple_type_specifier(Token token);
    void nested_name_specifier(Token token);
    
    void id(Token token);
    
    void simple_type_id(Token token);
    
    boolean isType(String name);
    

    void simple_template_id(Token token);
    void simple_template_id(int kind, Token token);
    
    void simple_template_id_or_ident(Token token);
    void simple_template_id_or_ident(int kind, Token token);

    void simple_template_id_nocheck(Token token);
    void simple_template_id_nocheck(int kind, Token token);
    
    void template_declaration(int kind, Token token);
    
    void type_parameter(int kind, Token token, Token token2, Token token3);
    void type_parameter(int kind, Token token, Token token2, Token token3, Token token4);
    
    void elaborated_type_specifier(Token token);
    
    void using_declaration(Token usingToken);
    void using_declaration(int kind, Token token);
    void end_using_declaration(Token semicolonToken);    
    
    void parameter_declaration_list();
    void end_parameter_declaration_list();
    
    void decl_specifiers(Token token);
    void end_decl_specifiers(Token token);
    
    
    void using_directive(Token usingToken, Token namespaceToken);
    void using_directive(int kind, Token token);
    void end_using_directive(Token semicolonToken);
    
    
    void namespace_alias_definition(Token namespaceToken, Token identToken, Token assignequalToken);
    void end_namespace_alias_definition(Token semicolonToken);
    void qualified_namespace_specifier(int kind, Token token);
        
    void greedy_declarator();
    void end_greedy_declarator();
    
    void declarator_id();
    void end_declarator_id();
    
    
    void translation_unit(Token token);
    void end_translation_unit(Token token);
    void statement(Token token);
    void end_statement(Token token);
    void labeled_statement(Token token);
    void labeled_statement(int kind, Token token);
    void labeled_statement(int kind, Token token1, Token token2);
    void end_labeled_statement(Token token);
    void expression_statement(Token token);
    void end_expression_statement(Token token);
    void selection_statement(Token token);
    void selection_statement(int kind, Token token);
    void end_selection_statement(Token token);
    void condition(Token token);
    void condition(int kind, Token token);
    void end_condition(Token token);
    void condition_declaration(Token token);
    void end_condition_declaration(Token token);
    void condition_expression(Token token);
    void end_condition_expression(Token token);    
    void iteration_statement(Token token);
    void iteration_statement(int kind, Token token);
    void end_iteration_statement(Token token);
    void for_init_statement(Token token);
    void end_for_init_statement(Token token);
    void for_range_declaration(Token token);
    void end_for_range_declaration(Token token);
    void for_range_initializer(Token token);
    void end_for_range_initializer(Token token);
    void jump_statement(Token token);
    void jump_statement(int kind, Token token);
    void jump_statement(int kind, Token token1, Token token2);
    void end_jump_statement(Token token);
    void declaration_statement(Token token);
    void end_declaration_statement(Token token);
    void declaration(Token token);
    void end_declaration(Token token);
    void block_declaration(Token token);
    void end_block_declaration(Token token);
    void id_expression(Token token);
    void end_id_expression(Token token);
    
    void tilde_class_name(Token token);
    void end_tilde_class_name(Token token);
    
    void alias_declaration(Token usingToken, Token identToken, Token assignequalToken);    
    void end_alias_declaration(Token token);
    void function_specifier(int kind, Token token);
    void type_specifier(Token token);
    void end_type_specifier(Token token);
    void trailing_type_specifier(Token token);
    void end_trailing_type_specifier(Token token);
    void decltype_specifier(Token token);
    void decltype_specifier(int kind, Token token);
    void end_decltype_specifier(Token token);
    void end_elaborated_type_specifier(Token token);
    void typename_specifier(Token token);
    void end_typename_specifier(Token token);    
    void asm_definition(Token asmToken, Token lparenToken, Token stringToken, Token rparenToken, Token semicolonToken);
    void linkage_specification(Token externToken, Token stringToken);
    void linkage_specification(int kind, Token token);
    void end_linkage_specification(Token token);
    void init_declarator_list(Token token);
    void init_declarator_list(int kind, Token token);
    void end_init_declarator_list(Token token);
    void init_declarator(Token token);
    void end_init_declarator(Token token);
    void declarator(Token token);
    void end_declarator(Token token);
    void noptr_declarator(Token token);
    void noptr_declarator(int kind, Token token);
    void end_noptr_declarator(Token token);
    void function_declarator(Token token);
    void end_function_declarator(Token token);
    void constructor_declarator(Token token);
    void end_constructor_declarator(Token token);
    void function_declarator(int kind, Token token);
    void noptr_abstract_declarator(Token token);
    void noptr_abstract_declarator(int kind, Token token);
    void end_noptr_abstract_declarator(Token token);
    void universal_declarator(Token token);
    void end_universal_declarator(Token token);
    void greedy_declarator(Token token);
    void end_greedy_declarator(Token token);
    void greedy_nonptr_declarator(Token token);
    void greedy_nonptr_declarator(int kind, Token token);
    void end_greedy_nonptr_declarator(Token token);
    void ptr_operator(Token token);
    void ptr_operator(int kind, Token token);
    void end_ptr_operator(Token token);
    void cv_qualifier(int kind, Token token);
    void ref_qualifier(int kind, Token token);
    void declarator_id(Token token);
    void declarator_id(int kind, Token token);
    void end_declarator_id(Token token);
    void type_id(Token token);
    void end_type_id(Token token);
    void parameters_and_qualifiers(Token token);
    void parameters_and_qualifiers(int kind, Token token);
    void end_parameters_and_qualifiers(Token token);
    void parameter_declaration_clause(Token token);
    void parameter_declaration_clause(int kind, Token token);
    void end_parameter_declaration_clause(Token token);
    void parameter_declaration_list(Token token);
    void end_parameter_declaration_list(int kind, Token token);
    void end_parameter_declaration_list(Token token);
    void parameter_declaration(Token token);
    void parameter_declaration(int kind, Token token);
    void end_parameter_declaration(Token token);
    void function_definition_after_declarator(Token token);
    void function_definition_after_declarator(int kind, Token token);
    void end_function_definition_after_declarator(Token token);
    void function_declaration(Token token);
    void end_function_declaration(Token token);
    void function_definition(Token token);
    void end_function_definition(Token token);
    void function_body(Token token);
    void end_function_body(Token token);
    void initializer(Token token);
    void initializer(int kind, Token token);
    void end_initializer(Token token);
    void brace_or_equal_initializer(Token token);
    void brace_or_equal_initializer(int kind, Token token);
    void end_brace_or_equal_initializer(Token token);
    void initializer_clause(Token token);
    void end_initializer_clause(Token token);
    void initializer_list(Token token);
    void initializer_list(int kind, Token token);
    void end_initializer_list(Token token);
    void braced_init_list(Token token);
    void braced_init_list(int kind, Token token);
    void end_braced_init_list(Token token);
    void end_class_name(Token token);
    void optionally_qualified_name(Token token);
    void end_optionally_qualified_name(Token token);
    void class_head(Token token);
    void end_class_head(Token token);
    void class_virtual_specifier(int kind, Token token);
    void member_specification(Token token);
    void member_specification(int kind, Token token);
    void end_member_specification(Token token);
    void member_declaration(Token token);
    void member_declaration(int kind, Token token);
    void end_member_declaration(Token token);
    void simple_member_declaration(Token token);
    void simple_member_declaration(int kind, Token token);
    void end_simple_member_declaration(Token token);
    void member_declarator(Token token);
    void end_member_declarator(Token token);
    void member_bitfield_declarator(Token token);
    void pure_specifier(Token token);
    void end_pure_specifier(Token token);
    void constant_initializer(Token token);
    void end_constant_initializer(Token token);
    void virt_specifier(int kind, Token token);
    void base_clause(Token token);
    void end_base_clause(Token token);
    void base_specifier_list(Token token);
    void base_specifier_list(int kind, Token token);
    void end_base_specifier_list(Token token);
    void class_or_decltype(Token token);
    void class_or_decltype(int kind, Token token);
    void end_class_or_decltype(Token token);
    void base_type_specifier(Token token);
    void end_base_type_specifier(Token token);
    void access_specifier(int kind, Token token);
    void conversion_function_id(Token token);
    void end_conversion_function_id(Token token);
    void conversion_type_id(Token token);
    void end_conversion_type_id(Token token);
    void ctor_initializer(Token token);
    void end_ctor_initializer(Token token);
    void mem_initializer_list(Token token);
    void mem_initializer_list(int kind, Token token);
    void end_mem_initializer_list(Token token);
    void mem_initializer(Token token);
    void mem_initializer(int kind, Token token);
    void end_mem_initializer(Token token);
    void mem_initializer_id(Token token);
    void end_mem_initializer_id(Token token);
    void mem_operator_function_id(Token token);
    void operator_function_id(int kind, Token token);
    void end_operator_function_id(Token token);
    void operator_id(Token token);
    void end_operator_id(Token token);
    void literal_operator_id(Token operatorToken, Token stringToken, Token identToken);
    void template_declaration(Token token);
    void end_template_declaration(Token token);
    void template_parameter_list(Token token);
    void template_parameter_list(int kind, Token token);
    void end_template_parameter_list(Token token);
    void template_parameter(Token token);
    void end_template_parameter(Token token);
    void type_parameter(int kind, Token token);
    void template_argument_list(Token token);
    void template_argument_list(int kind, Token token);
    void end_template_argument_list(Token token);
    void template_argument(Token token);
    void end_template_argument(Token token);
    void explicit_instantiation(Token token);
    void explicit_instantiation(int kind, Token token);
    void end_explicit_instantiation(Token token);
    void explicit_specialization(Token templateToken, Token lessthenToken, Token greaterthenToken);
    void end_explicit_specialization(Token token);
    void try_block(Token token);
    void end_try_block(Token token);
    void function_try_block(Token token);
    void end_function_try_block(Token token);
    void handler(Token token);
    void handler(int kind, Token token);
    void end_handler(Token token);
    void assignment_expression(Token token);
    void end_assignment_expression(Token token);
    void expression(Token token);
    void end_expression(Token token);
    void constant_expression(Token token);
    void end_constant_expression(Token token);    
    
    void skip_balanced_curlies(Token token);
    
    CsmFile getCurrentFile();    
}
