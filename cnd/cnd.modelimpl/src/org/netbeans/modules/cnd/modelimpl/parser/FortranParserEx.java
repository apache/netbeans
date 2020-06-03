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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.netbeans.modules.cnd.antlr.TokenBuffer;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.FortranParser;
import org.netbeans.modules.cnd.modelimpl.parser.generated.FortranParser.program_return;
import org.openide.util.Exceptions;

/**
 *
 */
public class FortranParserEx {

    public static final int UNKNOWN_SOURCE_FORM = -1;
    public static final int FREE_FORM = 1;
    public static final int FIXED_FORM = 2;

    public List<Object> parsedObjects = new ArrayList<>();

    private FortranParser parser;

    private final int form;

    public program_return program() throws RecognitionException {
        return parser.program();
    }

    int getNumberOfSyntaxErrors() {
        return parser.getNumberOfSyntaxErrors();
    }

    public static class ProgramData {
        public String name;
        public int startOffset;
        public int endOffset;

        public List<Object> members = null;
    }

    public static class SubroutineData {
        public String name;
        public int startOffset;
        public int endOffset;

        public List<String> args = null;
    }

    public static class ModuleData {
        public String name;
        public int startOffset;
        public int endOffset;

        public List<Object> members = null;
    }

    public static class MyTokenSource implements TokenSource {

        TokenStream ts;

        public MyTokenSource(TokenStream ts) {
            this.ts = ts;
        }

        @Override
        public Token nextToken() {
            org.netbeans.modules.cnd.antlr.Token nextToken = null;
            try {
                nextToken = ts.nextToken();
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
            return (nextToken != null) ? new MyToken(nextToken) : null;
        }

        @Override
        public String getSourceName() {
            return "my token source"; // NOI18N
        }

    }

    public FortranParserEx(TokenStream ts, int form) {
        this.form = form;
        MyTokenSource myts = new MyTokenSource(ts);
        FortranTokenStream tokens = new FortranTokenStream(myts);
        tokens.fill();
        tokens.toString();
        parser = new FortranParser(tokens);
        try {
            FortranLexicalPrepass prepass = new FortranLexicalPrepass(tokens);
            prepass.setSourceForm(form);
            prepass.performPrepass();
            tokens.finalizeTokenStream();

//            int i = 0;
//            Token token = tokens.get(i);
//            while (token.getType() != -1) {
//                System.out.println(token.getType() + " " + token.getText() + " " + token.getLine() + " " + (token.getCharPositionInLine()));
//                i++;
//                token = tokens.get(i);
//            }
//            System.out.println(token.getType() + " " + token.getText() + " " + token.getLine() + " " + (token.getCharPositionInLine()));

        } catch (Throwable t) {
            System.out.println(t);
            t.printStackTrace(System.out);
        }

//        StringBuilder b = new StringBuilder();
//        for(Object t : tokens.getTokens()) {
//            Token token = (Token)t;
//            b.append(token.getType());
//            b.append(' ');
//            b.append(token.getText());
//            b.append(' ');
//            b.append(token.getLine());
//            b.append('\n');
//        }
        
        parser.inputStreams = new Stack<>();
        parser.action = new IFortranParserAction() {

            // Proogram

            ProgramData programData = null;

            @Override
            public void program_stmt(Token label, Token programKeyword, Token id, Token eos) {
                if (id != null && programKeyword instanceof MyToken && ((MyToken) programKeyword).t instanceof APTToken) {
                    programData = new ProgramData();
                    programData.name = id.getText();
                    programData.startOffset = ((APTToken) ((MyToken) programKeyword).t).getOffset();

                    programData.members = new ArrayList<>();
                    // System.out.println("program " + id);
                }
            }

            @Override
            public void end_program_stmt(Token label, Token endKeyword, Token programKeyword, Token id, Token eos) {

                if(programData != null) {
                    if(endKeyword instanceof APTToken) {
                        programData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        programData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(programData);
                }
                programData = null;

                if(subroutineData != null && subroutineData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;

                if(functionData != null && functionData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        functionData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;

                if(moduleData != null && moduleData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(moduleData);
                }
                moduleData = null;
            }

            // Subroutine

            SubroutineData subroutineData = null;

            @Override
            public void subroutine_stmt__begin() {
                subroutineData = new SubroutineData();
            }

            @Override
            public void subroutine_stmt(Token label, Token keyword, Token name, Token eos, boolean hasPrefix, boolean hasDummyArgList, boolean hasBindingSpec, boolean hasArgSpecifier) {
                if (subroutineData != null && name != null && name.getText() != null && keyword instanceof MyToken && ((MyToken) keyword).t instanceof APTToken) {
                    subroutineData.name = name.getText();
                    subroutineData.startOffset = ((APTToken) ((MyToken) keyword).t).getOffset();
                }
            }

            @Override
            public void end_subroutine_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
                if(subroutineData != null && subroutineData.name != null) {
                    if (keyword2 instanceof MyToken && ((MyToken) keyword2).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken) ((MyToken) keyword2).t).getEndOffset();
                    } else if(keyword1 instanceof MyToken && ((MyToken) keyword1).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)keyword1).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else if(programData != null && programData.members != null) {
                        programData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;
            }

            // Function

            SubroutineData functionData = null;

            @Override
            public void function_stmt__begin() {
                functionData = new SubroutineData();
            }

            @Override
            public void function_stmt(Token label, Token keyword, Token name, Token eos, boolean hasGenericNameList, boolean hasSuffix) {
                if (functionData != null && name != null && name.getText() != null && keyword instanceof MyToken && ((MyToken) keyword).t instanceof APTToken) {
                    functionData.name = name.getText();
                    functionData.startOffset = ((APTToken) ((MyToken) keyword).t).getOffset();
                }
            }

            @Override
            public void end_function_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
                if(functionData != null && functionData.name != null) {
                    if (keyword2 instanceof MyToken && ((MyToken) keyword2).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken) ((MyToken) keyword2).t).getEndOffset();
                    } else if(keyword1 instanceof MyToken && ((MyToken) keyword1).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)keyword1).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else if(programData != null && programData.members != null) {
                        programData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;
            }

            // Subroutine arguments

            @Override
            public void dummy_arg(Token dummy) {
                if(dummy!= null && subroutineData != null && subroutineData.args != null) {
                    subroutineData.args.add(dummy.getText());
                }
                if(dummy!= null && functionData != null && functionData.args != null) {
                    functionData.args.add(dummy.getText());
                }
            }

            @Override
            public void dummy_arg_list__begin() {
                if(subroutineData != null) {
                    if(subroutineData.args == null) {
                        subroutineData.args = new ArrayList<>();
                    }
                }
                if(functionData != null) {
                    if(functionData.args == null) {
                        functionData.args = new ArrayList<>();
                    }
                }
            }

            @Override
            public void dummy_arg_list(int count) {
            }

            // Function arguments

            @Override
            public void generic_name_list_part(Token ident) {
                if(ident!= null && subroutineData != null && subroutineData.args != null) {
                    subroutineData.args.add(ident.getText());
                }
                if(ident!= null && functionData != null && functionData.args != null) {
                    functionData.args.add(ident.getText());
                }
            }

            @Override
            public void generic_name_list__begin() {
                if(subroutineData != null) {
                    if(subroutineData.args == null) {
                        subroutineData.args = new ArrayList<>();
                    }
                }
                if(functionData != null) {
                    if(functionData.args == null) {
                        functionData.args = new ArrayList<>();
                    }
                }
            }

            @Override
            public void generic_name_list(int count) {
            }

            // Module

            ModuleData moduleData = null;

            @Override
            public void module() {

            }

            @Override
            public void module_stmt__begin() {
                moduleData = new ModuleData();
            }

            @Override
            public void module_stmt(Token label, Token moduleKeyword, Token id, Token eos) {
                if (moduleData != null && id != null && moduleKeyword instanceof MyToken && ((MyToken) moduleKeyword).t instanceof APTToken) {
                    moduleData.name = id.getText();
                    moduleData.startOffset = ((APTToken) ((MyToken) moduleKeyword).t).getOffset();
                    moduleData.members = new ArrayList<>();
                }
            }

            @Override
            public void end_module_stmt(Token label, Token endKeyword, Token moduleKeyword, Token id, Token eos) {

                if(subroutineData != null && subroutineData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else if(programData != null && programData.members != null) {
                        programData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;

                if(functionData != null && functionData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        functionData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else if(programData != null && programData.members != null) {
                        programData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;

                if(moduleData != null && moduleData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(moduleData);
                }
                moduleData = null;
            }

            @Override
            public void module_subprogram_part() {

            }

            @Override
            public void module_subprogram(boolean hasPrefix) {

            }




            @Override
            public void specification_part(int numUseStmts, int numImportStmts, int numDeclConstructs) {

            }

            @Override
            public void declaration_construct() {

            }

            @Override
            public void execution_part() {

            }

            @Override
            public void execution_part_construct() {

            }

            @Override
            public void internal_subprogram_part(int count) {

            }

            @Override
            public void internal_subprogram() {

            }

            @Override
            public void specification_stmt() {

            }

            @Override
            public void executable_construct() {

            }

            @Override
            public void action_stmt() {

            }

            @Override
            public void keyword() {

            }

            @Override
            public void name(Token id) {

            }

            @Override
            public void constant(Token id) {

            }

            @Override
            public void scalar_constant() {

            }

            @Override
            public void literal_constant() {

            }

            @Override
            public void int_constant(Token id) {

            }

            @Override
            public void char_constant(Token id) {

            }

            @Override
            public void intrinsic_operator() {

            }

            @Override
            public void defined_operator(Token definedOp, boolean isExtended) {

            }

            @Override
            public void extended_intrinsic_op() {

            }

            @Override
            public void label(Token lbl) {

            }

            @Override
            public void label_list__begin() {

            }

            @Override
            public void label_list(int count) {

            }

            @Override
            public void type_spec() {

            }

            @Override
            public void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon) {

            }

            @Override
            public void intrinsic_type_spec(Token keyword1, Token keyword2, int type, boolean hasKindSelector) {

            }

            @Override
            public void kind_selector(Token token1, Token token2, boolean hasExpression) {

            }

            @Override
            public void signed_int_literal_constant(Token sign) {

            }

            @Override
            public void int_literal_constant(Token digitString, Token kindParam) {

            }

            @Override
            public void kind_param(Token kind) {

            }

            @Override
            public void boz_literal_constant(Token constant) {

            }

            @Override
            public void signed_real_literal_constant(Token sign) {

            }

            @Override
            public void real_literal_constant(Token realConstant, Token kindParam) {

            }

            @Override
            public void complex_literal_constant() {

            }

            @Override
            public void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {

            }

            @Override
            public void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {

            }

            @Override
            public void char_selector(Token tk1, Token tk2, int kindOrLen1, int kindOrLen2, boolean hasAsterisk) {

            }

            @Override
            public void length_selector(Token len, int kindOrLen, boolean hasAsterisk) {

            }

            @Override
            public void char_length(boolean hasTypeParamValue) {

            }

            @Override
            public void scalar_int_literal_constant() {

            }

            @Override
            public void char_literal_constant(Token digitString, Token id, Token str) {

            }

            @Override
            public void logical_literal_constant(Token logicalValue, boolean isTrue, Token kindParam) {

            }

            @Override
            public void derived_type_def() {

            }

            @Override
            public void type_param_or_comp_def_stmt(Token eos, int type) {

            }

            @Override
            public void type_param_or_comp_def_stmt_list() {

            }

            @Override
            public void derived_type_stmt(Token label, Token keyword, Token id, Token eos, boolean hasTypeAttrSpecList, boolean hasGenericNameList) {

            }

            @Override
            public void type_attr_spec(Token keyword, Token id, int specType) {

            }

            @Override
            public void type_attr_spec_list__begin() {

            }

            @Override
            public void type_attr_spec_list(int count) {

            }

            @Override
            public void private_or_sequence() {

            }

            @Override
            public void end_type_stmt(Token label, Token endKeyword, Token typeKeyword, Token id, Token eos) {

            }

            @Override
            public void sequence_stmt(Token label, Token sequenceKeyword, Token eos) {

            }

            @Override
            public void type_param_decl(Token id, boolean hasInit) {

            }

            @Override
            public void type_param_decl_list__begin() {

            }

            @Override
            public void type_param_decl_list(int count) {

            }

            @Override
            public void type_param_attr_spec(Token kindOrLen) {

            }

            @Override
            public void component_def_stmt(int type) {

            }

            @Override
            public void data_component_def_stmt(Token label, Token eos, boolean hasSpec) {

            }

            @Override
            public void component_attr_spec(Token attrKeyword, int specType) {

            }

            @Override
            public void component_attr_spec_list__begin() {

            }

            @Override
            public void component_attr_spec_list(int count) {

            }

            @Override
            public void component_decl(Token id, boolean hasComponentArraySpec, boolean hasCoArraySpec, boolean hasCharLength, boolean hasComponentInitialization) {

            }

            @Override
            public void component_decl_list__begin() {

            }

            @Override
            public void component_decl_list(int count) {

            }

            @Override
            public void component_array_spec(boolean isExplicit) {

            }

            @Override
            public void deferred_shape_spec_list__begin() {

            }

            @Override
            public void deferred_shape_spec_list(int count) {

            }

            @Override
            public void component_initialization() {

            }

            @Override
            public void proc_component_def_stmt(Token label, Token procedureKeyword, Token eos, boolean hasInterface) {

            }

            @Override
            public void proc_component_attr_spec(Token attrSpecKeyword, Token id, int specType) {

            }

            @Override
            public void proc_component_attr_spec_list__begin() {

            }

            @Override
            public void proc_component_attr_spec_list(int count) {

            }

            @Override
            public void private_components_stmt(Token label, Token privateKeyword, Token eos) {

            }

            @Override
            public void type_bound_procedure_part(int count, boolean hasBindingPrivateStmt) {

            }

            @Override
            public void binding_private_stmt(Token label, Token privateKeyword, Token eos) {

            }

            @Override
            public void proc_binding_stmt(Token label, int type, Token eos) {

            }

            @Override
            public void specific_binding(Token procedureKeyword, Token interfaceName, Token bindingName, Token procedureName, boolean hasBindingAttrList) {

            }

            @Override
            public void generic_binding(Token genericKeyword, boolean hasAccessSpec) {

            }

            @Override
            public void binding_attr(Token bindingAttr, int attr, Token id) {

            }

            @Override
            public void binding_attr_list__begin() {

            }

            @Override
            public void binding_attr_list(int count) {

            }

            @Override
            public void final_binding(Token finalKeyword) {

            }

            @Override
            public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {

            }

            @Override
            public void type_param_spec(Token keyword) {

            }

            @Override
            public void type_param_spec_list__begin() {

            }

            @Override
            public void type_param_spec_list(int count) {

            }

            @Override
            public void structure_constructor(Token id) {

            }

            @Override
            public void component_spec(Token id) {

            }

            @Override
            public void component_spec_list__begin() {

            }

            @Override
            public void component_spec_list(int count) {

            }

            @Override
            public void component_data_source() {

            }

            @Override
            public void enum_def(int numEls) {

            }

            @Override
            public void enum_def_stmt(Token label, Token enumKeyword, Token bindKeyword, Token id, Token eos) {

            }

            @Override
            public void enumerator_def_stmt(Token label, Token enumeratorKeyword, Token eos) {

            }

            @Override
            public void enumerator(Token id, boolean hasExpr) {

            }

            @Override
            public void enumerator_list__begin() {

            }

            @Override
            public void enumerator_list(int count) {

            }

            @Override
            public void end_enum_stmt(Token label, Token endKeyword, Token enumKeyword, Token eos) {

            }

            @Override
            public void array_constructor() {

            }

            @Override
            public void ac_spec() {

            }

            @Override
            public void ac_value() {

            }

            @Override
            public void ac_value_list__begin() {

            }

            @Override
            public void ac_value_list(int count) {

            }

            @Override
            public void ac_implied_do() {

            }

            @Override
            public void ac_implied_do_control(boolean hasStride) {

            }

            @Override
            public void scalar_int_variable() {

            }

            @Override
            public void type_declaration_stmt(Token label, int numAttributes, Token eos) {

            }

            @Override
            public void declaration_type_spec(Token udtKeyword, int type) {

            }

            @Override
            public void attr_spec(Token attrKeyword, int attr) {

            }

            @Override
            public void entity_decl(Token id) {

            }

            @Override
            public void entity_decl_list__begin() {

            }

            @Override
            public void entity_decl_list(int count) {

            }

            @Override
            public void initialization(boolean hasExpr, boolean hasNullInit) {

            }

            @Override
            public void null_init(Token id) {

            }

            @Override
            public void access_spec(Token keyword, int type) {

            }

            @Override
            public void language_binding_spec(Token keyword, Token id, boolean hasName) {

            }

            @Override
            public void array_spec(int count) {

            }

            @Override
            public void array_spec_element(int type) {

            }

            @Override
            public void explicit_shape_spec(boolean hasUpperBound) {

            }

            @Override
            public void explicit_shape_spec_list__begin() {

            }

            @Override
            public void explicit_shape_spec_list(int count) {

            }

            @Override
            public void co_array_spec() {

            }

            @Override
            public void intent_spec(Token intentKeyword1, Token intentKeyword2, int intent) {

            }

            @Override
            public void access_stmt(Token label, Token eos, boolean hasList) {

            }

            @Override
            public void deferred_co_shape_spec() {

            }

            @Override
            public void deferred_co_shape_spec_list__begin() {

            }

            @Override
            public void deferred_co_shape_spec_list(int count) {

            }

            @Override
            public void explicit_co_shape_spec() {

            }

            @Override
            public void explicit_co_shape_spec_suffix() {

            }

            @Override
            public void access_id() {

            }

            @Override
            public void access_id_list__begin() {

            }

            @Override
            public void access_id_list(int count) {

            }

            @Override
            public void allocatable_stmt(Token label, Token keyword, Token eos, int count) {

            }

            @Override
            public void allocatable_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            @Override
            public void asynchronous_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void bind_stmt(Token label, Token eos) {

            }

            @Override
            public void bind_entity(Token entity, boolean isCommonBlockName) {

            }

            @Override
            public void bind_entity_list__begin() {

            }

            @Override
            public void bind_entity_list(int count) {

            }

            @Override
            public void data_stmt(Token label, Token keyword, Token eos, int count) {

            }

            @Override
            public void data_stmt_set() {

            }

            @Override
            public void data_stmt_object() {

            }

            @Override
            public void data_stmt_object_list__begin() {

            }

            @Override
            public void data_stmt_object_list(int count) {

            }

            @Override
            public void data_implied_do(Token id, boolean hasThirdExpr) {

            }

            @Override
            public void data_i_do_object() {

            }

            @Override
            public void data_i_do_object_list__begin() {

            }

            @Override
            public void data_i_do_object_list(int count) {

            }

            @Override
            public void data_stmt_value(Token asterisk) {

            }

            @Override
            public void data_stmt_value_list__begin() {

            }

            @Override
            public void data_stmt_value_list(int count) {

            }

            @Override
            public void scalar_int_constant() {

            }

            @Override
            public void hollerith_constant(Token hollerithConstant) {

            }

            @Override
            public void data_stmt_constant() {

            }

            @Override
            public void dimension_stmt(Token label, Token keyword, Token eos, int count) {

            }

            @Override
            public void dimension_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            @Override
            public void dimension_spec(Token dimensionKeyword) {

            }

            @Override
            public void intent_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void optional_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void parameter_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void named_constant_def_list__begin() {

            }

            @Override
            public void named_constant_def_list(int count) {

            }

            @Override
            public void named_constant_def(Token id) {

            }

            @Override
            public void pointer_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void pointer_decl_list__begin() {

            }

            @Override
            public void pointer_decl_list(int count) {

            }

            @Override
            public void pointer_decl(Token id, boolean hasSpecList) {

            }

            @Override
            public void protected_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void save_stmt(Token label, Token keyword, Token eos, boolean hasSavedEntityList) {

            }

            @Override
            public void saved_entity_list__begin() {

            }

            @Override
            public void saved_entity_list(int count) {

            }

            @Override
            public void saved_entity(Token id, boolean isCommonBlockName) {

            }

            @Override
            public void target_stmt(Token label, Token keyword, Token eos, int count) {

            }

            @Override
            public void target_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            @Override
            public void value_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void volatile_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void implicit_stmt(Token label, Token implicitKeyword, Token noneKeyword, Token eos, boolean hasImplicitSpecList) {

            }

            @Override
            public void implicit_spec() {

            }

            @Override
            public void implicit_spec_list__begin() {

            }

            @Override
            public void implicit_spec_list(int count) {

            }

            @Override
            public void letter_spec(Token id1, Token id2) {

            }

            @Override
            public void letter_spec_list__begin() {

            }

            @Override
            public void letter_spec_list(int count) {

            }

            @Override
            public void namelist_stmt(Token label, Token keyword, Token eos, int count) {

            }

            @Override
            public void namelist_group_name(Token id) {

            }

            @Override
            public void namelist_group_object(Token id) {

            }

            @Override
            public void namelist_group_object_list__begin() {

            }

            @Override
            public void namelist_group_object_list(int count) {

            }

            @Override
            public void equivalence_stmt(Token label, Token equivalenceKeyword, Token eos) {

            }

            @Override
            public void equivalence_set() {

            }

            @Override
            public void equivalence_set_list__begin() {

            }

            @Override
            public void equivalence_set_list(int count) {

            }

            @Override
            public void equivalence_object() {

            }

            @Override
            public void equivalence_object_list__begin() {

            }

            @Override
            public void equivalence_object_list(int count) {

            }

            @Override
            public void common_stmt(Token label, Token commonKeyword, Token eos, int numBlocks) {

            }

            @Override
            public void common_block_name(Token id) {

            }

            @Override
            public void common_block_object_list__begin() {

            }

            @Override
            public void common_block_object_list(int count) {

            }

            @Override
            public void common_block_object(Token id, boolean hasShapeSpecList) {

            }

            @Override
            public void variable() {

            }

            @Override
            public void designator(boolean hasSubstringRange) {

            }

            @Override
            public void designator_or_func_ref() {

            }

            @Override
            public void substring_range_or_arg_list() {

            }

            @Override
            public void substr_range_or_arg_list_suffix() {

            }

            @Override
            public void logical_variable() {

            }

            @Override
            public void default_logical_variable() {

            }

            @Override
            public void scalar_default_logical_variable() {

            }

            @Override
            public void char_variable() {

            }

            @Override
            public void default_char_variable() {

            }

            @Override
            public void scalar_default_char_variable() {

            }

            @Override
            public void int_variable() {

            }

            @Override
            public void substring(boolean hasSubstringRange) {

            }

            @Override
            public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {

            }

            @Override
            public void data_ref(int numPartRef) {

            }

            @Override
            public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {

            }

            @Override
            public void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous) {

            }

            @Override
            public void section_subscript_list__begin() {

            }

            @Override
            public void section_subscript_list(int count) {

            }

            @Override
            public void vector_subscript() {

            }

            @Override
            public void allocate_stmt(Token label, Token allocateKeyword, Token eos, boolean hasTypeSpec, boolean hasAllocOptList) {

            }

            @Override
            public void image_selector(int exprCount) {

            }

            @Override
            public void alloc_opt(Token allocOpt) {

            }

            @Override
            public void alloc_opt_list__begin() {

            }

            @Override
            public void alloc_opt_list(int count) {

            }

            @Override
            public void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec) {

            }

            @Override
            public void allocation_list__begin() {

            }

            @Override
            public void allocation_list(int count) {

            }

            @Override
            public void allocate_object() {

            }

            @Override
            public void allocate_object_list__begin() {

            }

            @Override
            public void allocate_object_list(int count) {

            }

            @Override
            public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {

            }

            @Override
            public void allocate_shape_spec_list__begin() {

            }

            @Override
            public void allocate_shape_spec_list(int count) {

            }

            @Override
            public void nullify_stmt(Token label, Token nullifyKeyword, Token eos) {

            }

            @Override
            public void pointer_object() {

            }

            @Override
            public void pointer_object_list__begin() {

            }

            @Override
            public void pointer_object_list(int count) {

            }

            @Override
            public void deallocate_stmt(Token label, Token deallocateKeyword, Token eos, boolean hasDeallocOptList) {

            }

            @Override
            public void dealloc_opt(Token id) {

            }

            @Override
            public void dealloc_opt_list__begin() {

            }

            @Override
            public void dealloc_opt_list(int count) {

            }

            @Override
            public void allocate_co_array_spec() {

            }

            @Override
            public void allocate_co_shape_spec(boolean hasExpr) {

            }

            @Override
            public void allocate_co_shape_spec_list__begin() {

            }

            @Override
            public void allocate_co_shape_spec_list(int count) {

            }

            @Override
            public void primary() {

            }

            @Override
            public void level_1_expr(Token definedUnaryOp) {

            }

            @Override
            public void defined_unary_op(Token definedOp) {

            }

            @Override
            public void power_operand(boolean hasPowerOperand) {

            }

            @Override
            public void power_operand__power_op(Token powerOp) {

            }

            @Override
            public void mult_operand(int numMultOps) {

            }

            @Override
            public void mult_operand__mult_op(Token multOp) {

            }

            @Override
            public void add_operand(Token addOp, int numAddOps) {

            }

            @Override
            public void add_operand__add_op(Token addOp) {

            }

            @Override
            public void level_2_expr(int numConcatOps) {

            }

            @Override
            public void power_op(Token powerKeyword) {

            }

            @Override
            public void mult_op(Token multKeyword) {

            }

            @Override
            public void add_op(Token addKeyword) {

            }

            @Override
            public void level_3_expr(Token relOp) {

            }

            @Override
            public void concat_op(Token concatKeyword) {

            }

            @Override
            public void rel_op(Token relOp) {

            }

            @Override
            public void and_operand(boolean hasNotOp, int numAndOps) {

            }

            @Override
            public void and_operand__not_op(boolean hasNotOp) {

            }

            @Override
            public void or_operand(int numOrOps) {

            }

            @Override
            public void equiv_operand(int numEquivOps) {

            }

            @Override
            public void equiv_operand__equiv_op(Token equivOp) {

            }

            @Override
            public void level_5_expr(int numDefinedBinaryOps) {

            }

            @Override
            public void level_5_expr__defined_binary_op(Token definedBinaryOp) {

            }

            @Override
            public void not_op(Token notOp) {

            }

            @Override
            public void and_op(Token andOp) {

            }

            @Override
            public void or_op(Token orOp) {

            }

            @Override
            public void equiv_op(Token equivOp) {

            }

            @Override
            public void expr() {

            }

            @Override
            public void defined_binary_op(Token binaryOp) {

            }

            @Override
            public void assignment_stmt(Token label, Token eos) {

            }

            @Override
            public void pointer_assignment_stmt(Token label, Token eos, boolean hasBoundsSpecList, boolean hasBRList) {

            }

            @Override
            public void data_pointer_object() {

            }

            @Override
            public void bounds_spec() {

            }

            @Override
            public void bounds_spec_list__begin() {

            }

            @Override
            public void bounds_spec_list(int count) {

            }

            @Override
            public void bounds_remapping() {

            }

            @Override
            public void bounds_remapping_list__begin() {

            }

            @Override
            public void bounds_remapping_list(int count) {

            }

            @Override
            public void proc_pointer_object() {

            }

            @Override
            public void where_stmt__begin() {

            }

            @Override
            public void where_stmt(Token label, Token whereKeyword) {

            }

            @Override
            public void where_construct(int numConstructs, boolean hasMaskedElsewhere, boolean hasElsewhere) {

            }

            @Override
            public void where_construct_stmt(Token id, Token whereKeyword, Token eos) {

            }

            @Override
            public void where_body_construct() {

            }

            @Override
            public void masked_elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {

            }

            @Override
            public void masked_elsewhere_stmt__end(int numBodyConstructs) {

            }

            @Override
            public void elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {

            }

            @Override
            public void elsewhere_stmt__end(int numBodyConstructs) {

            }

            @Override
            public void end_where_stmt(Token label, Token endKeyword, Token whereKeyword, Token id, Token eos) {

            }

            @Override
            public void forall_construct() {

            }

            @Override
            public void forall_construct_stmt(Token label, Token id, Token forallKeyword, Token eos) {

            }

            @Override
            public void forall_header() {

            }

            @Override
            public void forall_triplet_spec(Token id, boolean hasStride) {

            }

            @Override
            public void forall_triplet_spec_list__begin() {

            }

            @Override
            public void forall_triplet_spec_list(int count) {

            }

            @Override
            public void forall_body_construct() {

            }

            @Override
            public void forall_assignment_stmt(boolean isPointerAssignment) {

            }

            @Override
            public void end_forall_stmt(Token label, Token endKeyword, Token forallKeyword, Token id, Token eos) {

            }

            @Override
            public void forall_stmt__begin() {

            }

            @Override
            public void forall_stmt(Token label, Token forallKeyword) {

            }

            @Override
            public void block() {

            }

            @Override
            public void if_construct() {

            }

            @Override
            public void if_then_stmt(Token label, Token id, Token ifKeyword, Token thenKeyword, Token eos) {

            }

            @Override
            public void else_if_stmt(Token label, Token elseKeyword, Token ifKeyword, Token thenKeyword, Token id, Token eos) {

            }

            @Override
            public void else_stmt(Token label, Token elseKeyword, Token id, Token eos) {

            }

            @Override
            public void end_if_stmt(Token label, Token endKeyword, Token ifKeyword, Token id, Token eos) {

            }

            @Override
            public void if_stmt__begin() {

            }

            @Override
            public void if_stmt(Token label, Token ifKeyword) {

            }

            @Override
            public void case_construct() {

            }

            @Override
            public void select_case_stmt(Token label, Token id, Token selectKeyword, Token caseKeyword, Token eos) {

            }

            @Override
            public void case_stmt(Token label, Token caseKeyword, Token id, Token eos) {

            }

            @Override
            public void end_select_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {

            }

            @Override
            public void case_selector(Token defaultToken) {

            }

            @Override
            public void case_value_range() {

            }

            @Override
            public void case_value_range_list__begin() {

            }

            @Override
            public void case_value_range_list(int count) {

            }

            @Override
            public void case_value_range_suffix() {

            }

            @Override
            public void case_value() {

            }

            @Override
            public void associate_construct() {

            }

            @Override
            public void associate_stmt(Token label, Token id, Token associateKeyword, Token eos) {

            }

            @Override
            public void association_list__begin() {

            }

            @Override
            public void association_list(int count) {

            }

            @Override
            public void association(Token id) {

            }

            @Override
            public void selector() {

            }

            @Override
            public void end_associate_stmt(Token label, Token endKeyword, Token associateKeyword, Token id, Token eos) {

            }

            @Override
            public void select_type_construct() {

            }

            @Override
            public void select_type_stmt(Token label, Token selectConstructName, Token associateName, Token eos) {

            }

            @Override
            public void select_type(Token selectKeyword, Token typeKeyword) {

            }

            @Override
            public void type_guard_stmt(Token label, Token typeKeyword, Token isOrDefaultKeyword, Token selectConstructName, Token eos) {

            }

            @Override
            public void end_select_type_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {

            }

            @Override
            public void do_construct() {

            }

            @Override
            public void block_do_construct() {

            }

            @Override
            public void do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {

            }

            @Override
            public void label_do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {

            }

            @Override
            public void loop_control(Token whileKeyword, boolean hasOptExpr) {

            }

            @Override
            public void do_variable() {

            }

            @Override
            public void end_do() {

            }

            @Override
            public void end_do_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {

            }

            @Override
            public void do_term_action_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {

            }

            @Override
            public void cycle_stmt(Token label, Token cycleKeyword, Token id, Token eos) {

            }

            @Override
            public void exit_stmt(Token label, Token exitKeyword, Token id, Token eos) {

            }

            @Override
            public void goto_stmt(Token goKeyword, Token toKeyword, Token label, Token eos) {

            }

            @Override
            public void computed_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token eos) {

            }

            @Override
            public void assign_stmt(Token label1, Token assignKeyword, Token label2, Token toKeyword, Token name, Token eos) {

            }

            @Override
            public void assigned_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token name, Token eos) {

            }

            @Override
            public void stmt_label_list() {

            }

            @Override
            public void pause_stmt(Token label, Token pauseKeyword, Token constant, Token eos) {

            }

            @Override
            public void arithmetic_if_stmt(Token label, Token ifKeyword, Token label1, Token label2, Token label3, Token eos) {

            }

            @Override
            public void continue_stmt(Token label, Token continueKeyword, Token eos) {

            }

            @Override
            public void stop_stmt(Token label, Token stopKeyword, Token eos, boolean hasStopCode) {

            }

            @Override
            public void stop_code(Token digitString) {

            }

            @Override
            public void scalar_char_constant() {

            }

            @Override
            public void io_unit() {

            }

            @Override
            public void file_unit_number() {

            }

            @Override
            public void open_stmt(Token label, Token openKeyword, Token eos) {

            }

            @Override
            public void connect_spec(Token id) {

            }

            @Override
            public void connect_spec_list__begin() {

            }

            @Override
            public void connect_spec_list(int count) {

            }

            @Override
            public void close_stmt(Token label, Token closeKeyword, Token eos) {

            }

            @Override
            public void close_spec(Token closeSpec) {

            }

            @Override
            public void close_spec_list__begin() {

            }

            @Override
            public void close_spec_list(int count) {

            }

            @Override
            public void read_stmt(Token label, Token readKeyword, Token eos, boolean hasInputItemList) {

            }

            @Override
            public void write_stmt(Token label, Token writeKeyword, Token eos, boolean hasOutputItemList) {

            }

            @Override
            public void print_stmt(Token label, Token printKeyword, Token eos, boolean hasOutputItemList) {

            }

            @Override
            public void io_control_spec(boolean hasExpression, Token keyword, boolean hasAsterisk) {

            }

            @Override
            public void io_control_spec_list__begin() {

            }

            @Override
            public void io_control_spec_list(int count) {

            }

            @Override
            public void format() {

            }

            @Override
            public void input_item() {

            }

            @Override
            public void input_item_list__begin() {

            }

            @Override
            public void input_item_list(int count) {

            }

            @Override
            public void output_item() {

            }

            @Override
            public void output_item_list__begin() {

            }

            @Override
            public void output_item_list(int count) {

            }

            @Override
            public void io_implied_do() {

            }

            @Override
            public void io_implied_do_object() {

            }

            @Override
            public void io_implied_do_control() {

            }

            @Override
            public void dtv_type_spec(Token typeKeyword) {

            }

            @Override
            public void wait_stmt(Token label, Token waitKeyword, Token eos) {

            }

            @Override
            public void wait_spec(Token id) {

            }

            @Override
            public void wait_spec_list__begin() {

            }

            @Override
            public void wait_spec_list(int count) {

            }

            @Override
            public void backspace_stmt(Token label, Token backspaceKeyword, Token eos, boolean hasPositionSpecList) {

            }

            @Override
            public void endfile_stmt(Token label, Token endKeyword, Token fileKeyword, Token eos, boolean hasPositionSpecList) {

            }

            @Override
            public void rewind_stmt(Token label, Token rewindKeyword, Token eos, boolean hasPositionSpecList) {

            }

            @Override
            public void position_spec(Token id) {

            }

            @Override
            public void position_spec_list__begin() {

            }

            @Override
            public void position_spec_list(int count) {

            }

            @Override
            public void flush_stmt(Token label, Token flushKeyword, Token eos, boolean hasFlushSpecList) {

            }

            @Override
            public void flush_spec(Token id) {

            }

            @Override
            public void flush_spec_list__begin() {

            }

            @Override
            public void flush_spec_list(int count) {

            }

            @Override
            public void inquire_stmt(Token label, Token inquireKeyword, Token id, Token eos, boolean isType2) {

            }

            @Override
            public void inquire_spec(Token id) {

            }

            @Override
            public void inquire_spec_list__begin() {

            }

            @Override
            public void inquire_spec_list(int count) {

            }

            @Override
            public void format_stmt(Token label, Token formatKeyword, Token eos) {

            }

            @Override
            public void format_specification(boolean hasFormatItemList) {

            }

            @Override
            public void format_item(Token descOrDigit, boolean hasFormatItemList) {

            }

            @Override
            public void format_item_list__begin() {

            }

            @Override
            public void format_item_list(int count) {

            }

            @Override
            public void v_list_part(Token plus_minus, Token digitString) {

            }

            @Override
            public void v_list__begin() {

            }

            @Override
            public void v_list(int count) {

            }

            @Override
            public void main_program__begin() {

            }

            @Override
            public void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart) {

            }

            @Override
            public void ext_function_subprogram(boolean hasPrefix) {

            }

            @Override
            public void use_stmt(Token label, Token useKeyword, Token id, Token onlyKeyword, Token eos, boolean hasModuleNature, boolean hasRenameList, boolean hasOnly) {

            }

            @Override
            public void module_nature(Token nature) {

            }

            @Override
            public void rename(Token id1, Token id2, Token op1, Token defOp1, Token op2, Token defOp2) {

            }

            @Override
            public void rename_list__begin() {

            }

            @Override
            public void rename_list(int count) {

            }

            @Override
            public void only() {

            }

            @Override
            public void only_list__begin() {

            }

            @Override
            public void only_list(int count) {

            }

            @Override
            public void block_data() {

            }

            @Override
            public void block_data_stmt__begin() {

            }

            @Override
            public void block_data_stmt(Token label, Token blockKeyword, Token dataKeyword, Token id, Token eos) {

            }

            @Override
            public void end_block_data_stmt(Token label, Token endKeyword, Token blockKeyword, Token dataKeyword, Token id, Token eos) {

            }

            @Override
            public void interface_block() {

            }

            @Override
            public void interface_specification() {

            }

            @Override
            public void interface_stmt__begin() {

            }

            @Override
            public void interface_stmt(Token label, Token abstractToken, Token keyword, Token eos, boolean hasGenericSpec) {

            }

            @Override
            public void end_interface_stmt(Token label, Token kw1, Token kw2, Token eos, boolean hasGenericSpec) {

            }

            @Override
            public void interface_body(boolean hasPrefix) {

            }

            @Override
            public void procedure_stmt(Token label, Token module, Token procedureKeyword, Token eos) {

            }

            @Override
            public void generic_spec(Token keyword, Token name, int type) {

            }

            @Override
            public void dtio_generic_spec(Token rw, Token format, int type) {

            }

            @Override
            public void import_stmt(Token label, Token importKeyword, Token eos, boolean hasGenericNameList) {

            }

            @Override
            public void external_stmt(Token label, Token externalKeyword, Token eos) {

            }

            @Override
            public void procedure_declaration_stmt(Token label, Token procedureKeyword, Token eos, boolean hasProcInterface, int count) {

            }

            @Override
            public void proc_interface(Token id) {

            }

            @Override
            public void proc_attr_spec(Token attrKeyword, Token id, int spec) {

            }

            @Override
            public void proc_decl(Token id, boolean hasNullInit) {

            }

            @Override
            public void proc_decl_list__begin() {

            }

            @Override
            public void proc_decl_list(int count) {

            }

            @Override
            public void intrinsic_stmt(Token label, Token intrinsicToken, Token eos) {

            }

            @Override
            public void function_reference(boolean hasActualArgSpecList) {

            }

            @Override
            public void call_stmt(Token label, Token callKeyword, Token eos, boolean hasActualArgSpecList) {

            }

            @Override
            public void procedure_designator() {

            }

            @Override
            public void actual_arg_spec(Token keyword) {

            }

            @Override
            public void actual_arg_spec_list__begin() {

            }

            @Override
            public void actual_arg_spec_list(int count) {

            }

            @Override
            public void actual_arg(boolean hasExpr, Token label) {

            }

            @Override
            public void function_subprogram(boolean hasExePart, boolean hasIntSubProg) {

            }

            @Override
            public void proc_language_binding_spec() {

            }

            @Override
            public void prefix(int specCount) {

            }

            @Override
            public void t_prefix(int specCount) {

            }

            @Override
            public void prefix_spec(boolean isDecTypeSpec) {

            }

            @Override
            public void t_prefix_spec(Token spec) {

            }

            @Override
            public void suffix(Token resultKeyword, boolean hasProcLangBindSpec) {

            }

            @Override
            public void result_name() {

            }

            @Override
            public void entry_stmt(Token label, Token keyword, Token id, Token eos, boolean hasDummyArgList, boolean hasSuffix) {

            }

            @Override
            public void return_stmt(Token label, Token keyword, Token eos, boolean hasScalarIntExpr) {

            }

            @Override
            public void contains_stmt(Token label, Token keyword, Token eos) {

            }

            @Override
            public void stmt_function_stmt(Token label, Token functionName, Token eos, boolean hasGenericNameList) {

            }

            @Override
            public void end_of_stmt(Token eos) {

            }

            @Override
            public void start_of_file(String fileName) {

            }

            @Override
            public void end_of_file() {

            }

            @Override
            public void cleanUp() {

            }
        };
    }

    static public class MyToken implements org.antlr.runtime.Token {

        org.netbeans.modules.cnd.antlr.Token t;
        int chanel = 0;
        int type = 0;
        CharStream stream = null;

        public MyToken(org.netbeans.modules.cnd.antlr.Token t) {
//            if(t.getType() == APTTokenTypes.EOF) {
//                t = APTUtils.EOF_TOKEN2;
//            }
            this.t = t;
            type = t.getType();
        }

        @Override
        public String getText() {
            return t.getText();
        }

        @Override
        public void setText(String arg0) {
            t.setText(arg0);
        }

        @Override
        public int getType() {
            return t.getType() != APTTokenTypes.EOF ? type : APTUtils.EOF_TOKEN2.getType();
        }

        @Override
        public void setType(int arg0) {
            type = arg0;
        }

        @Override
        public int getLine() {
            return t.getLine();
        }

        @Override
        public void setLine(int arg0) {
            t.setLine(arg0);
        }

        @Override
        public int getCharPositionInLine() {
            return t.getColumn() - 1;
        }

        @Override
        public void setCharPositionInLine(int arg0) {
            t.setColumn(arg0);
        }

        @Override
        public int getChannel() {
            return t.getType() == APTTokenTypes.CONTINUE_CHAR ? 99 : chanel;
        }

        @Override
        public void setChannel(int arg0) {
            chanel = arg0;
        }

        @Override
        public int getTokenIndex() {
            //
            return 0;
        }

        @Override
        public void setTokenIndex(int arg0) {
            //
        }

        @Override
        public CharStream getInputStream() {
            return stream;
        }

        @Override
        public void setInputStream(CharStream s) {
            stream = s;
        }

        @Override
        public String toString() {
            return t.toString();
        }

    }


    static public class MyTokenStream implements org.antlr.runtime.TokenStream {
        TokenBuffer tb;

        int lastMark;

        public MyTokenStream(TokenBuffer tb) {
            this.tb = tb;
        }

        @Override
        public Token LT(int arg0) {

            if (arg0 < 0) {
                arg0++; // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
                if ((tb.index() + arg0 - 1) < 0) {
                    return new MyToken(tb.LT(APTTokenTypes.EOF));
                }
            }

            return new MyToken(tb.LT(arg0));
        }

        @Override
        public void consume() {
            tb.consume();
        }

        @Override
        public int LA(int arg0) {
            int la = tb.LA(arg0);
            return (la != 1)?la:-1;
        }

        @Override
        public int mark() {
            lastMark = tb.index();
            return tb.mark();
        }

        @Override
        public int index() {
            return tb.index();
        }

        @Override
        public void rewind(int arg0) {
            tb.rewind(arg0);
        }

        @Override
        public void rewind() {
            tb.mark();
            tb.rewind(lastMark);
        }

        @Override
        public void seek(int arg0) {
            tb.seek(arg0);
        }

        @Override
        public Token get(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public TokenSource getTokenSource() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String toString(int arg0, int arg1) {
            return tb.toString();
        }

        @Override
        public String toString(Token arg0, Token arg1) {
            return tb.toString();
        }

        @Override
        public void release(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String getSourceName() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int range() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }


}
