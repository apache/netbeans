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

package org.netbeans.modules.cnd.modelimpl.trace;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;

/**
 * pre-integration tests for parser
 */
public class FileModelTest extends TraceModelTestBase {

    public FileModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
//        System.setProperty("apt.use.clank", "true");
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_01() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_02() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }
    
    public void testDeclSpec() throws Exception {
        // IZ#132136: code completion for C++ and Qt does not work under Windows
        performTest("declspec.cc");
    }
    
    public void testTemplateFunctionInTemplateClass() throws Exception {
        // IZ#
        performTest("template_fun_in_template_class.cc");
    }
    
    public void testIncludeMacroExpansion() throws Exception {
        // IZ#124635
        performTest("include_macro_expanding.cc");
    }
    
    public void testParserRecover() throws Exception {
        performTest("parser_recover.cc");
    }
    
    public void testBitFields() throws Exception {
        performTest("bitFields.cc");
    }
    
    public void testFunWithoutRetTypeInClassBody() throws Exception {
        performTest("constructors_and_fun_no_ret_types.cc");
    }
    
    public void testStackOverflowOnCastExpression() throws Exception {
        // IZ#115549 StackOverflowError on parsing long expressions
        performTest("stackoverflow.cc");        
    }
    
    public void testIncompleteString() throws Exception {
        // Clank: to fix this case in Clank mode I had to modify:
        // Lexer.LexCharConstant
        // Lexer.LexStringLiteral
        // and return token with real Kind even for incomplete tokens
        // instead of tok.TokenKind.unknown
        performTest("incomplete_string.cc");        
    }
    
    public void testPreProcDefinedKeyword() throws Exception {
        performTest("preproc_defined_keyword.cc");        
    }

    public void testPreProcTrueKeywordID_Studio() throws Exception {
        // "true" in preprocessor for C++ mode was fixed in OSS (21194403)
        // hack was removed as rev http://hg.netbeans.org/cnd-main/rev/9c543f5a42d3
        if (false) {
            // clank doesn't have this specific of Studio compiler
            performTest("check.true.studio.cc");
        }
    }
    
    public void testPreProcTrueKeywordIDCPP() throws Exception {
        performTest("check.true.cc");        
    }
    
    public void testPreProcTrueKeywordIDC() throws Exception {
        performTest("check.true.c");        
    }
    
    public void testFriendsDeclaration() throws Exception {
        performTest("friend.cc"); // NOI18N
    }
    
    public void testCNavigation() throws Exception {
        performTest("cnav.c"); // NOI18N
    }
    
    public void testDummy() throws Exception {
        performTest("dummy.cc"); // NOI18N
    }
    
    public void testExpandDollar() throws Exception {
        // IZ#208064: jQuery selector-function semulation fails
        performTest("expand_dollar.cc"); // NOI18N
    }
    
    public void testDefineMacro() throws Exception {
        performTest("define_macro.cc"); // NOI18N
    }
    
    public void testIncludeCorrectness() throws Exception {
        performTest("test_include_correcteness.cc"); // NOI18N
    }   
    
    public void testTemplateExplicitInstantiation() throws Exception {
        performTest("template_explicit_instantiation.cc"); // NOI18N
    }
    
    public void testIntStaticField() throws Exception {
	performTest("int_static_field.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop1() throws Exception {
	performTest("infinite1.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop2() throws Exception {
	performTest("infinite2.cc"); // NOI18N
    }
    
    public void testResolverInfiniteLoop3() throws Exception {
	performTest("infinite3.cc"); // NOI18N
    }

    public void testCdeclAndPointerReturnType() throws Exception {
	performTest("cdecl_and_poniter_return_type.cc"); // NOI18N
    }
    
    public void testNestedClassesAndEnums_1() throws Exception {
	performTest("nested_classes_and_enums_1.cc"); // NOI18N
    }
    
    public void testFunctionPointerAsParameterType () throws Exception {
	performTest("function_pointer_as_param_type.cc"); // NOI18N
    }

    public void testFunctionPointerAsVariableType () throws Exception {
	performTest("function_pointer_as_var_type.cc"); // NOI18N
    }

    public void testFunctionPointerMisc() throws Exception {
	performTest("function_pointer_misc.cc"); // NOI18N
    }
	
    public void testUsingExtern() throws Exception {
        performTest("using_extern.h");
    }     

    public void testPartialSpeciazationsAndOperatorLess() throws Exception {
        performTest("partial_specializations.cc");
    }     
    
    public void testFuncDeclPrefixAttributes() throws Exception {
        performTest("func_decl_prefix_attributes.cc");
    }
    
    public void testVariableDefinition() throws Exception {
        performTest("variable_definition.cc"); // NOI18N
    }

    public void testFunctionPointerAsReturnType () throws Exception {
        performTest("function_pointer_as_return_type.cc"); // NOI18N
    }  
    
    public void testFunctionPointerAsTypeCast() throws Exception {
        performTest("function_pointer_as_type_cast.cc"); // NOI18N
    }
    
    public void testFunExpandedUnnamedParams() throws Exception {
        performTest("function_expanded_unnamed_params.cc"); // NOI18N
    }
    
    public void testErrorDirective() throws Exception {
	performTest("error_directive.cc"); // NOI18N
    }
    
    public void testEmptyLongHex() throws Exception {
	performTest("empty_long_hex.c"); // NOI18N
    }
    
    public void testUnresolvedPersistence() throws Exception {
        performTest("unresolved_persistence.cc"); // NOI18N
    }

    public void test0x01() throws Exception {
        performTest("0x01.c"); // NOI18N
    }
    
//  disable this test because it's OS-locale dependent and can fail where
//  russian is not installed    
//    public void test0x16() throws Exception {
//        performTest("0x16.cc"); // NOI18N
//    }
    
    public void testPreProcExpressionAndEmptyBodyMacro() throws Exception {
        performTest("ppExpressionAndEmptyBodyMacro.cc"); //NOI18N
    }
    
    public void testPPDirectiveExtraTokens() throws Exception {
        //#201806  -  extra tokens at end of #endif directive breaks code model 
        performTest("iz201806.cc");
    }
    
    public void testExprAfterIf() throws Exception {
        performTest("lparenAfterPPKwds.cc"); // NOI18N
    }

    public void testNamespaceAttribute() throws Exception {
        performTest("namespace_attrib.cc"); // NOI18N
    }
    
    public void testTypedefEnumInClassScope() throws Exception {
        performTest("typedef_enum_in_class_scope.cc"); // NOI18N
    }
    
    public void testNamedMemberEnumTypedef() throws Exception {
        performTest("named_member_enum_typedef.cc"); // NOI18N
    }
    
    public void testTypedefInsideFunc() throws Exception {
        performTest("typedef_inside_func.cc"); // NOI18N
    }
    
    public void testAttributesAlignedClass() throws Exception {
        performTest("attributes_aligned_class.cc"); // NOI18N
    }
    
    public void testStaticStruct() throws Exception {
        performTest("static_struct.cc"); // NOI18N
    }

    public void testInlineDtorDefinitionName() throws Exception {
        performTest("inline_dtor_definition_name.cc"); // NOI18N
    }
        
    public void testThrowConst() throws Exception {
        performTest("throw_const.cc"); // NOI18N
    }
    
    public void testTemplateDtorDefinition() throws Exception {
        performTest("template_dtor_definition.cc"); // NOI18N
    }
    
    public void testKAndRParams() throws Exception {
        performTest("k_and_r_params.c"); // NOI18N
    }
    
    public void testFunctionsAndVariables() throws Exception {
        performTest("functions_and_variables.cc"); // NOI18N
    }

    public void testStaticFunction() throws Exception {
        performTest("static_function.cc"); // NOI18N
    }

    public void testTypename() throws Exception {
        // IZ 131012 : missed declaration with "typename" keyword
        performTest("typename.cc"); // NOI18N
    }
    
    public void testArray() throws Exception {
        // IZ 130678 : incorrect offsets for type of array delcaration
        performTest("array.cc"); // NOI18N
    }
    
    public void testTemplateDestrucror() throws Exception {
        // IZ 131407 : parser doesn't handle specialized destructor
        performTest("template_destructor.cc"); // NOI18N
    }
    
    public void testConversionOperator() throws Exception {
        // IZ 137468 : grammar does not support conversion operator invocation
        performTest("conversion_operator.cc"); // NOI18N
    }

    public void testClassQualifiers() throws Exception {
        // IZ 136821 : Keyword volatile breakes classifier content
        performTest("class_qualifiers.cc"); // NOI18N
    }

    public void testExtensions() throws Exception {
        // IZ 137118 : IDE highlights GTK_WIDGET_SET_FLAGS and GTK_CAN_DEFAULT macros
        performTest("extensions.cc"); // NOI18N
    }

    public void testClassTemplateMethodCall() throws Exception {
        // IZ 137531 : IDE highlights db.template cursor<T> line as error
        performTest("class_template_method_call.cc"); // NOI18N
    }

    public void testGccAttribute() throws Exception {
        // IZ 136947 : IDE highlights code with 'typedef' as wrong
        performTest("gcc_attribute.c"); // NOI18N
    }

    public void testComplex() throws Exception {
        // IZ 136729 : Code model is broken by _Complex keyword
        performTest("complex.c"); // NOI18N
    }

    public void testAttributeInConstructor() throws Exception {
        // IZ 136239 : C++ grammar does not allow attributes after constructor
        performTest("attribute_in_constructor.cc"); // NOI18N
    }

    public void testCastOperator() throws Exception {
        // IZ 137094 : grammar do not support parenthesis in cast
        performTest("cast.cc"); // NOI18N
    }

    public void testIZ138320() throws Exception {
        // IZ 138320 : IDE doesn't recognize 'class P = V const *' line in template
        performTest("IZ138320.cc"); // NOI18N
    }

    public void testIZ138551() throws Exception {
        // IZ 138551 : parser fails on "template class A::B<1>;"
        performTest("IZ138551.cc"); // NOI18N
    }

    public void testIZ144276() throws Exception {
        CsmCacheManager.enter();
        try {
            // IZ 144276 : StackOverflowError on typedef C::C C;
            performTest("IZ144276.cc"); // NOI18N
            for(CsmProject p : getModel().projects()){
                for(CsmFile f : p.getAllFiles()){
                    for (CsmDeclaration d : f.getDeclarations()){
                        if (CsmKindUtilities.isTypedef(d) || CsmKindUtilities.isTypeAlias(d)) {
                            CsmType t = ((CsmTypedef)d).getType();
                            if (t != null) {
                                t.isTemplateBased();
                                CsmClassifier c = t.getClassifier();
                                if (c != null) {
                                    CsmBaseUtilities.getOriginalClassifier(c,f);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            CsmCacheManager.leave();
        }
    }

    public void testArrayCast() throws Exception {
        // IZ 138899 : parser fails on conversion "(int(*)[4][4])"
        performTest("array_cast.cc");
    }

    public void testStringizeMacro() throws Exception {
        if (APTTraceFlags.USE_CLANK) {
          // this is the test for non-clank mode only
          return;
        }
        // IZ 137465 : wrong macro expansion for #x
        performPreprocessorTest("stringize_macro.cc"); // NOI18N
    }

    public void testTemplateParams() throws Exception {
        // IZ 138551 : parser fails on "template class A::B<1>;"
        performTest("templateParams.h"); // NOI18N
    }

    public void testTemplateMethodCall() throws Exception {
        // IZ 138962 : Passer fails on template method calls
        performTest("template_method_call.cc"); // NOI18N
    }

    public void testExpressions() throws Exception {
        // IZ 138962 : Passer fails on template method calls
        performTest("expressions.cc"); // NOI18N
    }

    public void testFunctionPointerAsTemplateParameter() throws Exception {
        performTest("function_pointer_as_template_parameter.cc"); // NOI18N
    }

    public void test10000parameters() throws Exception {
        performTest("10000parameters.c"); // NOI18N
    }
    
    public void testTypedefPointerToStaticMember() throws Exception {
        // IZ 138325 : IDE highlights 'typedef R (T::*F);' line as wrong
        performTest("typedef_pointer_to_static_member.cc"); // NOI18N
    }

    public void testEmptyArrayInitializer() throws Exception {
        // IZ 140082 : parser fails on "int empty[] = {}"
        performTest("empty_array_initializer.cc"); // NOI18N
    }

    public void testTemplatePointerToMethod() throws Exception {
        // IZ 140559 : parser fails on code from boost
        performTest("template_pointer_to_method.cc"); // NOI18N
    }

    public void testResolverNs_1() throws Exception {
        // IZ 140704 A constant in namespace is highlighted as an unresolved id
        performTest("resolver_ns_general.cc"); // NOI18N
    }

    public void testResolverNs_2() throws Exception {
        // IZ 140704 A constant in namespace is highlighted as an unresolved id
        performTest("resolver_ns_using_declaration.cc"); // NOI18N
    }

    public void testResolverUsingDeclarationInClass() throws Exception {
        performTest("resolver_using_declaration_in_class.cc"); // NOI18N
    }

    public void testTwoBranches() throws Exception {
        // iz #142110 For a header file, that is included with different
        // preprocessor states, code model should include the most complete data
        performTest("branches_1.cc"); // NOI18N
    }

    public void testMacrodef() throws Exception {
        performTest("macrodef.cc"); // NOI18N
    }

    public void testClassBodyIncluded() throws Exception {
        performTest("class_body_included.cc"); // NOI18N
    }

    public void testResolverClassString() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }

    public void testResolverTypedefString() throws Exception {
        performTest("resolver_typedef_string.cc"); // NOI18N
    }

    public void testTemplateInnerClassDtorDefinition() throws Exception {
        performTest("template_inner_class_dtor_definition.cc"); // NOI18N
    }

    // #143611 If a class inherits some template *specialization*, unresolved IDs appear
    public void testTemplateSpecializationInheritance_1() throws Exception {
        performTest("template_spec_inherited_1.cc"); // NOI18N
    }
    
    // #144156 Template specialization functions: incorrect navigation between definitions and declarations
    public void testTemplateFunctionSpecialization() throws Exception {
        performTest("template_fun_spec.cc"); // NOI18N
    }

    // #144968 A lot of parser errors in boost: instances.hpp
    public void testIZ144968() throws Exception {
        performTest("IZ144968.cc"); // NOI18N
    }
    
    // #144009 wrong error highlighting for inline structure
    public void testIZ144009() throws Exception {
        performTest("IZ144009.cc"); // NOI18N
    }   

    // #145963 can't resolve template class implementations
    public void testLocalVariables() throws Exception {
        performTest("local_variables.cc"); // NOI18N
    }   
    
    // #146150 unexpected token: ; message appears on extern int errno; line
    public void testIZ146150() throws Exception {
        performTest("IZ146150.cc"); // NOI18N
    }   

    // #146150 unexpected token: ; message appears on extern int errno; line
    public void testMethodsWithFunctionAsReturnType() throws Exception {
        performTest("methods_with_function_as_return_type.cc"); // NOI18N
    }   

    // #145071 : forward declarations marked as error
    public void testForwardDeclarations() throws Exception {        
        performTest("forward_declarations.cc"); // NOI18N
    }

    // #146966 : parser fails on recognizing some operator's definitions
    public void testTemplateTypeCastOperators() throws Exception {        
        performTest("template_type_cast_operators.cc"); // NOI18N        
    }
    
    // IZ#136731 : No hyper link on local extern function
    public void testFunctionDeclarationAsParameter() throws Exception {      
        performTest("function_declaration_as_parameter.cc"); // NOI18N        
    }

    // IZ#149412 : parser doesn't recover after specific structure initialization
    public void testStructInitializer() throws Exception {      
        performTest("struct_initializer.c"); // NOI18N        
    }

    // IZ#149412 : parser doesn't recover after specific structure initialization
    public void testVariableInitializer() throws Exception {      
        performTest("variable_initializer.cc"); // NOI18N        
    }

    // IZ#149499 : parser does not support omitting first branch of ? : operator
    public void testTernaryOperator() throws Exception {      
        performTest("ternary_operator.cc"); // NOI18N        
    }
    
    // IZ#149483 : parser fails on unsigned const char
    public void testConst() throws Exception {      
        performTest("const.cc"); // NOI18N        
    }    
    
    // Bug#217470
    public void testElaboratedTypeForwards() throws Exception {
        performTest("elaboratedTypeForwards.cpp");
    }
    
    public void testBug233263() throws Exception {
        performTest("bug233263.cpp");
    }
    
    public void testBug234768() throws Exception {
        // Bug 234768 - Cpp11 classes/enums could not be defined inside type aliases
        performTest("bug234768.cpp");
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends TraceModelTestBase {
	
        public Failed(String testName) {
            super(testName);
        }

//        public void testParserRecover() throws Exception {
//            performTest("parser_recover.cc");
//        }

        public void testPreProcDefinedKeyword() throws Exception {
            performTest("preproc_defined_keyword.cc");        
        }

	@Override
	protected void setUp() throws Exception {
	    System.setProperty("parser.report.errors", "true");
	    super.setUp();
	}
	
        @Override
	protected Class<?> getTestCaseDataClass() {
	    return FileModelTest.class;
	}
	
        @Override
	protected void postSetUp() {
	    // init flags needed for file model tests
	    getTraceModel().setDumpModel(true);
	    getTraceModel().setDumpPPState(true);
	}
   }
    
}



