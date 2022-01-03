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

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.KeyPresentationFactory;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.KeyPresentationFactory.class)
public class KeyPresentationFactoryImpl implements KeyPresentationFactory {

    @Override
    public Key create(KeyDataPresentation presentation) {
        switch (presentation.getHandler()) {
            case KeyObjectFactory.KEY_INCLUDED_FILE_STORAGE_KEY:
                return new IncludedFileStorageKey(presentation);
            case KeyObjectFactory.KEY_CLASSIFIER_CONTAINER_KEY:
                return new ClassifierContainerKey(presentation);
            case KeyObjectFactory.KEY_FILE_CONTAINER_KEY:
                return new FileContainerKey(presentation);
            case KeyObjectFactory.KEY_FILE_DECLARATIONS_KEY:
                return new FileDeclarationsKey(presentation);
            case KeyObjectFactory.KEY_FILE_INCLUDES_KEY:
                return new FileIncludesKey(presentation);
            case KeyObjectFactory.KEY_FILE_KEY:
                return new FileKey(presentation);
            case KeyObjectFactory.KEY_FILE_MACROS_KEY:
                return new FileMacrosKey(presentation);
            case KeyObjectFactory.KEY_FILE_REFERENCES_KEY:
                return new FileReferencesKey(presentation);
            case KeyObjectFactory.KEY_FILE_INSTANTIATIONS_KEY:
                return new FileInstantiationsKey(presentation);
            case KeyObjectFactory.KEY_GRAPH_CONTAINER_KEY:
                return new GraphContainerKey(presentation);
            case KeyObjectFactory.KEY_NS_DECLARATION_CONTAINER_KEY:
                return new NamespaceDeclarationContainerKey(presentation);
            case KeyObjectFactory.KEY_NAMESPACE_KEY:
                return new NamespaceKey(presentation);
            case KeyObjectFactory.KEY_PROJECT_DECLARATION_CONTAINER_KEY:
                return new ProjectDeclarationContainerKey(presentation);
            case KeyObjectFactory.KEY_PROJECT_KEY:
                return new ProjectKey(presentation);
            case KeyObjectFactory.KEY_INCLUDE_KEY:
                return new IncludeKey(presentation);
            case KeyObjectFactory.KEY_INSTANTIATION_KEY:
                return new InstantiationKey(presentation);
            case KeyObjectFactory.KEY_INHERITANCE_PRIVATE_KEY:
                return new InheritanceKey.PRIVATE(presentation);
            case  KeyObjectFactory.KEY_INHERITANCE_PROTECTED_KEY:
                return new InheritanceKey.PROTECTED(presentation);
            case KeyObjectFactory.KEY_INHERITANCE_PUBLIC_KEY:
                return new InheritanceKey.PUBLIC(presentation);
            case KeyObjectFactory.KEY_INHERITANCE_NONE_KEY:
                return new InheritanceKey.NONE(presentation);
            case KeyObjectFactory.KEY_PARAM_LIST_KEY:
                return new ParamListKey(presentation);
            case KeyObjectFactory.KEY_MACRO_KEY:
                return new MacroKey(presentation);
            case KeyObjectFactory.KEY_ASM_KEY:
                return new OffsetableDeclarationKey.ASM(presentation);
            case KeyObjectFactory.KEY_BUILT_IN_KEY:
                return new OffsetableDeclarationKey.BUILT_IN(presentation);
            case KeyObjectFactory.KEY_CLASS_KEY:
                return new OffsetableDeclarationKey.CLASS(presentation);
            case KeyObjectFactory.KEY_ENUM_KEY:
                return new OffsetableDeclarationKey.ENUM(presentation);
            case KeyObjectFactory.KEY_FUNCTION_KEY:
                return new OffsetableDeclarationKey.FUNCTION(presentation);
            case KeyObjectFactory.KEY_NAMESPACE_DEFINITION_KEY:
                return new OffsetableDeclarationKey.NAMESPACE_DEFINITION(presentation);
            case KeyObjectFactory.KEY_STRUCT_KEY:
                return new OffsetableDeclarationKey.STRUCT(presentation);
            case KeyObjectFactory.KEY_TEMPLATE_DECLARATION_KEY:
                return new OffsetableDeclarationKey.TEMPLATE_DECLARATION(presentation);
            case KeyObjectFactory.KEY_UNION_KEY:
                return new OffsetableDeclarationKey.UNION(presentation);
            case KeyObjectFactory.KEY_VARIABLE_KEY:
                return new OffsetableDeclarationKey.VARIABLE(presentation);
            case KeyObjectFactory.KEY_NAMESPACE_ALIAS_KEY:
                return new OffsetableDeclarationKey.NAMESPACE_ALIAS(presentation);
            case KeyObjectFactory.KEY_ENUMERATOR_KEY:
                return new OffsetableDeclarationKey.ENUMERATOR(presentation);
            case KeyObjectFactory.KEY_FUNCTION_DEFINITION_KEY:
                return new OffsetableDeclarationKey.FUNCTION_DEFINITION(presentation);
            case KeyObjectFactory.KEY_FUNCTION_LAMBDA_KEY:
                return new OffsetableDeclarationKey.FUNCTION_LAMBDA(presentation);
            case KeyObjectFactory.KEY_FUNCTION_INSTANTIATION_KEY:
                return new OffsetableDeclarationKey.FUNCTION_INSTANTIATION(presentation);
            case KeyObjectFactory.KEY_USING_DIRECTIVE_KEY:
                return new OffsetableDeclarationKey.USING_DIRECTIVE(presentation);
            case KeyObjectFactory.KEY_TEMPLATE_PARAMETER_KEY:
                return new OffsetableDeclarationKey.TEMPLATE_PARAMETER(presentation);
            case KeyObjectFactory.KEY_CLASS_FRIEND_DECLARATION_KEY:
                return new OffsetableDeclarationKey.CLASS_FRIEND_DECLARATION(presentation);
            case KeyObjectFactory.KEY_TEMPLATE_SPECIALIZATION_KEY:
                return new OffsetableDeclarationKey.TEMPLATE_SPECIALIZATION(presentation);
            case KeyObjectFactory.KEY_TYPEDEF_KEY:
                return new OffsetableDeclarationKey.TYPEDEF(presentation);
            case KeyObjectFactory.KEY_TYPEALIAS_KEY:
                return new OffsetableDeclarationKey.TYPEALIAS(presentation);
            case KeyObjectFactory.KEY_USING_DECLARATION_KEY:
                return new OffsetableDeclarationKey.USING_DECLARATION(presentation);
            case KeyObjectFactory.KEY_VARIABLE_DEFINITION_KEY:
                return new OffsetableDeclarationKey.VARIABLE_DEFINITION(presentation);
            case KeyObjectFactory.KEY_CLASS_FORWARD_DECLARATION_KEY:
                return new OffsetableDeclarationKey.CLASS_FORWARD_DECLARATION(presentation);
            case KeyObjectFactory.KEY_ENUM_FORWARD_DECLARATION_KEY:
                return new OffsetableDeclarationKey.ENUM_FORWARD_DECLARATION(presentation);
            case KeyObjectFactory.KEY_FUNCTION_FRIEND_KEY:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND(presentation);
            case KeyObjectFactory.KEY_FUNCTION_FRIEND_DEFINITION_KEY:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND_DEFINITION(presentation);
            case KeyObjectFactory.KEY_FUNCTION_TYPE_KEY:
                return new OffsetableDeclarationKey.FUNCTION_TYPE(presentation);
        }
        return null;
    }
    
}
