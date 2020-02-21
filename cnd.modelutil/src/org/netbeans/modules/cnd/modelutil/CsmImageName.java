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

package org.netbeans.modules.cnd.modelutil;

/**
 *
 */
public interface CsmImageName {
    static final String RESOURCE_BASE = "org/netbeans/modules/cnd/modelutil/resources/types16x16/"; // NOI18N

    static final String DEFAULT = RESOURCE_BASE+"defaultNode.png"; // NOI18N
    static final String DECLARATION_FILTER = RESOURCE_BASE+"declaration_filter.png"; // NOI18N
    static final String SCOPE_FILTER = RESOURCE_BASE+"scope_filter.png"; // NOI18N

    static final String NAMESPACE = RESOURCE_BASE+"namespace_16.png"; // NOI18N
    static final String CLASS = RESOURCE_BASE+"class_16.png"; // NOI18N
    static final String CLASS_FORWARD = RESOURCE_BASE+"class_forward_decl.png"; // NOI18N
    static final String STRUCT_FORWARD = RESOURCE_BASE+"struct_forward_decl.png"; // NOI18N
    static final String STRUCT = RESOURCE_BASE+"struct_16.png"; // NOI18N
    static final String UNION = RESOURCE_BASE+"union_16.png"; // NOI18N
    static final String ENUMERATION = RESOURCE_BASE+"enumeration_16.png"; // NOI18N
    static final String ENUMERATION_FWD = RESOURCE_BASE+"enumeration_fwd_16.png"; // NOI18N
    static final String ENUMERATOR = RESOURCE_BASE+"enumeration_item_16.png"; // NOI18N
    static final String TYPEDEF = RESOURCE_BASE+"typedef_16.png"; // NOI18N

    static final String FIELD_PUBLIC = RESOURCE_BASE+"fields.png"; // NOI18N
    static final String FIELD_PROTECTED = RESOURCE_BASE+"fields_protected.png"; // NOI18N
    static final String FIELD_PRIVATE = RESOURCE_BASE+"fields_private.png"; // NOI18N
    
    static final String FIELD_CONST_PUBLIC = RESOURCE_BASE+"const_public.png"; // NOI18N
    static final String FIELD_CONST_PROTECTED = RESOURCE_BASE+"const_protected.png"; // NOI18N
    static final String FIELD_CONST_PRIVATE = RESOURCE_BASE+"const_private.png"; // NOI18N

    static final String FIELD_ST_PUBLIC = RESOURCE_BASE+"fields_static.png"; // NOI18N
    static final String FIELD_ST_PROTECTED = RESOURCE_BASE+"fields_static_protected.png"; // NOI18N
    static final String FIELD_ST_PRIVATE = RESOURCE_BASE+"fields_static_private.png"; // NOI18N

    static final String FIELD_ST_CONST_PUBLIC = RESOURCE_BASE+"const_static_public.png"; // NOI18N
    static final String FIELD_ST_CONST_PROTECTED = RESOURCE_BASE+"const_static_protected.png"; // NOI18N
    static final String FIELD_ST_CONST_PRIVATE = RESOURCE_BASE+"const_static_private.png"; // NOI18N

    static final String CONSTRUCTOR_PUBLIC = RESOURCE_BASE+"constructors.png"; // NOI18N
    static final String CONSTRUCTOR_PROTECTED = RESOURCE_BASE+"constructors_protected.png"; // NOI18N
    static final String CONSTRUCTOR_PRIVATE = RESOURCE_BASE+"constructors_private.png"; // NOI18N

    static final String DESTRUCTOR_PUBLIC = RESOURCE_BASE+"deconstructors.png"; // NOI18N
    static final String DESTRUCTOR_PROTECTED = RESOURCE_BASE+"deconstructors_protected.png"; // NOI18N
    static final String DESTRUCTOR_PRIVATE = RESOURCE_BASE+"deconstructors_private.png"; // NOI18N

    static final String METHOD_PUBLIC = RESOURCE_BASE+"methods.png"; // NOI18N
    static final String METHOD_PROTECTED = RESOURCE_BASE+"methods_protected.png"; // NOI18N
    static final String METHOD_PRIVATE = RESOURCE_BASE+"methods_private.png"; // NOI18N

    static final String METHOD_ST_PUBLIC = RESOURCE_BASE+"methods_static.png"; // NOI18N
    static final String METHOD_ST_PROTECTED = RESOURCE_BASE+"methods_static_protected.png"; // NOI18N
    static final String METHOD_ST_PRIVATE = RESOURCE_BASE+"methods_static_private.png"; // NOI18N
    
    static final String FRIEND_METHOD = RESOURCE_BASE+"friend_method.png"; // NOI18N
    static final String FRIEND_OPERATOR = RESOURCE_BASE+"friend_operator.png"; // NOI18N
    static final String FRIEND_CLASS = RESOURCE_BASE+"friend_class.png"; // NOI18N

    static final String FUNCTION_GLOBAL = RESOURCE_BASE+"global_function.png"; // NOI18N
    static final String FUNCTION_DECLARATION_GLOBAL = RESOURCE_BASE+"global_function_decl.png"; // NOI18N
    static final String FUNCTION_ST_GLOBAL = RESOURCE_BASE+"static_function.png"; // NOI18N

    static final String OPERATOR_PUBLIC = RESOURCE_BASE+"operator_public.png"; // NOI18N
    static final String OPERATOR_PROTECTED = RESOURCE_BASE+"operator_protected.png"; // NOI18N
    static final String OPERATOR_PRIVATE = RESOURCE_BASE+"operator_privat.png"; // NOI18N

    static final String OPERATOR_ST_PUBLIC = RESOURCE_BASE+"operator_static.png"; // NOI18N
    static final String OPERATOR_ST_PROTECTED = RESOURCE_BASE+"operator_static_protected.png"; // NOI18N
    static final String OPERATOR_ST_PRIVATE = RESOURCE_BASE+"operator_static_privat.png"; // NOI18N
    
    static final String OPERATOR_GLOBAL = RESOURCE_BASE+"global_operator.png"; // NOI18N
    static final String OPERATOR_ST_GLOBAL = RESOURCE_BASE+"global_operator_static.png"; // NOI18N

    static final String VARIABLE_GLOBAL = RESOURCE_BASE+"global_variable.png"; // NOI18N
    static final String VARIABLE_EX_GLOBAL = RESOURCE_BASE+"extern_variable_16.png"; // NOI18N
    static final String VARIABLE_CONST_GLOBAL = RESOURCE_BASE+"global_constant.png"; // NOI18N

    static final String VARIABLE_LOCAL = RESOURCE_BASE+"local_variable.png"; // NOI18N
    static final String VARIABLE_ST_LOCAL = RESOURCE_BASE+"local_variable.png"; // NOI18N

    static final String VARIABLE_CONST_LOCAL = RESOURCE_BASE+"local_constant.png"; // NOI18N
    static final String VARIABLE_CONST_ST_LOCAL = RESOURCE_BASE+"local_constant.png"; // NOI18N

    static final String VARIABLE_FILE_LOCAL = RESOURCE_BASE+"static_variable.png"; // NOI18N
    static final String VARIABLE_CONST_FILE_LOCAL = RESOURCE_BASE+"global_constant.png"; // NOI18N
    static final String VARIABLE_CONST_ST_FILE_LOCAL = RESOURCE_BASE+"global_constant.png"; // NOI18N
    
    static final String MACRO = RESOURCE_BASE+"code_macro_16.png"; // NOI18N
    static final String SYSTEM_MACRO = RESOURCE_BASE+"system_macro_16.png"; // NOI18N
    static final String ERROR = RESOURCE_BASE+"code_error_16.png"; // NOI18N
    static final String INCLUDE_USER = RESOURCE_BASE+"include_user_16.png"; // NOI18N
    static final String INCLUDE_SYSTEM = RESOURCE_BASE+"include_sys_16.png"; // NOI18N
    static final String INCLUDE_SYS_FOLDER = RESOURCE_BASE+"include_sys_folder_16.png"; // NOI18N
    static final String INCLUDE_USR_FOLDER = RESOURCE_BASE+"include_usr_folder_16.png"; // NOI18N
    static final String NAMESPACE_ALIAS = RESOURCE_BASE+"namespace_alias.png"; // NOI18N
    static final String USING = RESOURCE_BASE+"using_namespace_16.png"; // NOI18N
    static final String USING_DECLARATION =  RESOURCE_BASE+"using_declaration_16.png"; // NOI18N
    static final String C_CPP_KEYWORD = RESOURCE_BASE+"keyword.png"; // NOI18N
    
    static final String PROJECT =  RESOURCE_BASE+"project.png"; // NOI18N
    static final String PROJECT_OPENED =  RESOURCE_BASE+"project_open.png"; // NOI18N
    static final String LIB_PROJECT =  RESOURCE_BASE+"libraries.png"; // NOI18N
    static final String LIB_PROJECT_OPENED =  RESOURCE_BASE+"libraries.png"; // NOI18N
    
    static final String HEADER_FILE = RESOURCE_BASE+"header.gif"; // NOI18N
    static final String CPP_SOUCE_FILE = RESOURCE_BASE+"cppsource.gif"; // NOI18N
    static final String C_SOUCE_FILE = RESOURCE_BASE+"csource.gif"; // NOI18N
    static final String FORTRAN_SOUCE_FILE = RESOURCE_BASE+"fortran.gif"; // NOI18N

    static final String TEMPLATE_PARAMETER =  RESOURCE_BASE+"template_parameter_16.png"; // NOI18N

    static final String MODULE =  RESOURCE_BASE+"methods_container.png"; // NOI18N
}
