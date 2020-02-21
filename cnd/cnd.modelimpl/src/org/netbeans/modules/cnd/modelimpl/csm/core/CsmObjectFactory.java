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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionInstantiation;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentDeclarations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentIncludes;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentInstantiations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentMacros;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences;
import org.netbeans.modules.cnd.modelimpl.content.file.ReferencesIndex;
import org.netbeans.modules.cnd.modelimpl.content.project.ClassifierContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainerNamespace;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainerProject;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.IncludedFileContainer;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplFunctionSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.FriendClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FriendFunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionInstantiationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.LambdaFunction;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceAliasImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterListImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypeAliasImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ModuleImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ProgramImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.SubroutineImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * objects factory
 */
/* XXX typo in interface name?
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.PersistentObjectFactory.class)
*/
public final class CsmObjectFactory extends AbstractObjectFactory implements PersistentFactory {
    
    private static final CsmObjectFactory instance = new CsmObjectFactory();

    private CsmObjectFactory() {
    }

    public static CsmObjectFactory instance() {
        return instance;
    }

    @Override
    protected short getHandler(Object object) {
        assert object != null;
        short aHandler;
        if (object instanceof LibProjectImpl) {
            aHandler = LIB_PROJECT_IMPL;
        } else if (object instanceof ProjectImpl) {
            aHandler = PROJECT_IMPL;
        } else if (object instanceof FileContainer) {
            aHandler = FILES_CONTAINER;
        } else if (object instanceof GraphContainer) {
            aHandler = GRAPH_CONTAINER;
        } else if (object instanceof FileImpl) {
            aHandler = FILE_IMPL;
        } else if (object instanceof FileComponentDeclarations) {
            aHandler = FILE_DECLARATIONS;
        } else if (object instanceof FileComponentMacros) {
            aHandler = FILE_MACROS;
        } else if (object instanceof FileComponentIncludes) {
            aHandler = FILE_INCLUDES;
        } else if (object instanceof FileComponentReferences) {
            aHandler = FILE_REFERENCES;
        } else if (object instanceof FileComponentInstantiations) {
            aHandler = FILE_INSTANTIATIONS;
//        } else if (object instanceof Unresolved.UnresolvedFile) {
//            aHandler = UNRESOLVED_FILE;
//        } else if (object instanceof Unresolved.UnresolvedClass) {
//            aHandler = UNRESOLVED_CLASS;
        } else if (object instanceof EnumImpl) {
            aHandler = ENUM_IMPL;
            if (object instanceof ForwardEnum) {
                aHandler = FORWARD_ENUM;
            }
        } else if (object instanceof ClassImpl) {
            if (object instanceof ClassImplFunctionSpecialization) {
                aHandler = CLASS_IMPL_FUNCTION_SPECIALIZATION;
            } else if (object instanceof ClassImplSpecialization) {
                aHandler = CLASS_IMPL_SPECIALIZATION;
            } else if (object instanceof ForwardClass) {
                aHandler = FORWARD_CLASS;
            } else {
                aHandler = CLASS_IMPL;
            }
        } else if (object instanceof TypedefImpl) {
            if (object instanceof ClassImpl.MemberTypedef) {
                aHandler = MEMBER_TYPEDEF;
            } else if (object instanceof ClassImpl.MemberTypeAliasImpl) {
                aHandler = MEMBER_TYPEALIAS;
            } else if (object instanceof TypeAliasImpl) {
                aHandler = TYPEALIAS_IMPL;
            } else {
                aHandler = TYPEDEF_IMPL;
            }
        } else if (object instanceof NamespaceImpl) {
            aHandler = NAMESPACE_IMPL;
        } else if (object instanceof NamespaceDefinitionImpl) {
            aHandler = NAMESPACE_DEF_IMPL;
        } else if (object instanceof NamespaceAliasImpl) {
            aHandler = NAMESPACE_ALIAS_IMPL;
        } else if (object instanceof UsingDeclarationImpl) {
            aHandler = USING_DECLARATION_IMPL;
        } else if (object instanceof UsingDirectiveImpl) {
            aHandler = USING_DIRECTIVE_IMPL;
        } else if (object instanceof ClassForwardDeclarationImpl) {
            if (object instanceof ClassImpl.ClassMemberForwardDeclaration) {
                aHandler = CLASS_MEMBER_FORWARD_DECLARATION;
            } else {
                aHandler = CLASS_FORWARD_DECLARATION_IMPL;
            }
        } else if (object instanceof EnumForwardDeclarationImpl) {
            if (object instanceof ClassImpl.EnumMemberForwardDeclaration) {
                aHandler = ENUM_MEMBER_FORWARD_DECLARATION;
            } else {
                aHandler = ENUM_FORWARD_DECLARATION_IMPL;
            }
        } else if (object instanceof FunctionImpl<?>) {
            // we have several FunctionImpl subclasses
            if (object instanceof FunctionImplEx<?>) {
                if (object instanceof FunctionDefinitionImpl<?>) {
                    // we have several FunctionDefinitionImpl subclasses
                    if (object instanceof DestructorDefinitionImpl) {
                        aHandler = DESTRUCTOR_DEF_IMPL;
                    } else if (object instanceof ConstructorDefinitionImpl) {
                        aHandler = CONSTRUCTOR_DEF_IMPL;
                    } else {
                        if (object instanceof CsmFriendFunction) {
                            aHandler = FRIEND_FUNCTION_DEF_IMPL;
                        } else {
                            aHandler = FUNCTION_DEF_IMPL;
                        }
                    }
                } else {
                    if (object instanceof CsmFriendFunction) {
                        aHandler = FRIEND_FUNCTION_IMPL_EX;
                    } else {
                        aHandler = FUNCTION_IMPL_EX;
                    }
                }
            } else if (object instanceof MethodImpl<?>) {
                // we have several MethodImpl subclusses
                if (object instanceof MethodDDImpl<?>) {
                    // we have two MethodDDImpl classses:
                    if (object instanceof DestructorDDImpl) {
                        aHandler = DESTRUCTOR_DEF_DECL_IMPL;
                    } else if (object instanceof ConstructorDDImpl) {
                        aHandler = CONSTRUCTOR_DEF_DECL_IMPL;
                    } else {
                        aHandler = METHOD_DEF_DECL_IMPL;
                    }
                } else if (object instanceof ConstructorImpl) {
                    aHandler = CONSTRUCTOR_IMPL;
                } else if (object instanceof DestructorImpl) {
                    aHandler = DESTRUCTOR_IMPL;
                } else if (object instanceof MethodImplSpecialization<?>) {
                    aHandler = METHOD_IMPL_SPECIALIZATION;
                } else {
                    aHandler = METHOD_IMPL;
                }
            } else if (object instanceof FunctionDDImpl<?>) {
                if (object instanceof CsmFriendFunction) {
                    aHandler = FRIEND_FUNCTION_DEF_DECL_IMPL;
                } else if (object instanceof LambdaFunction) {
                    aHandler = FUNCTION_LAMBDA_IMPL;
                } else {
                    aHandler = FUNCTION_DEF_DECL_IMPL;
                }
            } else {
                if (object instanceof CsmFriendFunction) {
                    aHandler = FRIEND_FUNCTION_IMPL;
                } else {
                    aHandler = FUNCTION_IMPL;
                }
            }
        } else if (object instanceof VariableImpl<?>) {
            // we have several VariableImpl subclasses
            if (object instanceof VariableDefinitionImpl) {
                aHandler = VARIABLE_DEF_IMPL;
            } else if (object instanceof FieldImpl) {
                aHandler = FIELD_IMPL;
            } else if (object instanceof ParameterImpl) {
                throw new IllegalArgumentException("instance of not persistable class " + object.getClass().getName() + object);  //NOI18N
            } else {
                aHandler = VARIABLE_IMPL;
            }
        } else if (object instanceof EnumeratorImpl) {
            aHandler = ENUMERATOR_IMPL;
        } else if (object instanceof IncludeImpl) {
            aHandler = INCLUDE_IMPL;
        } else if (object instanceof InheritanceImpl) {
            aHandler = INHERITANCE_IMPL;
        } else if (object instanceof ParameterListImpl<?,?>) {
            throw new IllegalArgumentException("instance of not persistable class " + object.getClass().getName() + object);  //NOI18N
        } else if (object instanceof MacroImpl) {
            aHandler = MACRO_IMPL;
        } else if (object instanceof FriendClassImpl) {
            aHandler = FRIEND_CLASS_IMPL;
        } else if (object instanceof DeclarationContainerProject) {
            aHandler = DECLARATION_CONTAINER_PROJECT;
        } else if (object instanceof DeclarationContainerNamespace) {
            aHandler = DECLARATION_CONTAINER_NAMESPACE;
        } else if (object instanceof ClassifierContainer) {
            aHandler = CLASSIFIER_CONTAINER;
        } else if (object instanceof IncludedFileContainer) {
            aHandler = INCLUDED_FILE_STORAGE;
        } else if (object instanceof TemplateParameterImpl) {
            aHandler = TEMPLATE_PARAMETER_IMPL;
        } else if (object instanceof Instantiation) {
            if (object instanceof Instantiation.Class) {
                aHandler = INSTANTIATION_CLASS;
            } else {
                throw new IllegalArgumentException("instance of unknown class " + object.getClass().getName());  //NOI18N            
            }
        } else if (object instanceof CsmFunctionInstantiation) {
            aHandler = FUNCTION_INSTANTIATION_IMPL;
        } else if (object instanceof ProgramImpl<?>) {
            aHandler = PROGRAM_IMPL;
        } else if (object instanceof SubroutineImpl<?>) {
            aHandler = SUBROUTINE_IMPL;
        } else if (object instanceof ModuleImpl) {
            aHandler = MODULE_IMPL;
        } else if (object instanceof ReferencesIndex) {
            aHandler = MODEL_INDEX;
        } else {
            throw new IllegalArgumentException("instance of unknown class " + object.getClass().getName());  //NOI18N
        }
        return aHandler;
    }

    @Override
    protected SelfPersistent createObject(short handler, RepositoryDataInput stream) throws IOException {
        SelfPersistent obj;

        switch (handler) {
            case PROJECT_IMPL:
                obj = new ProjectImpl(stream);
                break;

            case LIB_PROJECT_IMPL:
                obj = new LibProjectImpl(stream);
                break;

            case FILES_CONTAINER:
                obj = new FileContainer(stream);
                break;

            case GRAPH_CONTAINER:
                obj = new GraphContainer(stream);
                break;

            case FILE_IMPL:
                obj = new FileImpl(stream);
                break;

            case FILE_DECLARATIONS:
                obj = new FileComponentDeclarations(stream);
                break;

            case FILE_MACROS:
                obj = new FileComponentMacros(stream);
                break;

            case FILE_INCLUDES:
                obj = new FileComponentIncludes(stream);
                break;

            case FILE_REFERENCES:
                obj = new FileComponentReferences(stream);
                break;
                
            case FILE_INSTANTIATIONS:
                obj = new FileComponentInstantiations(stream);
                break;

                //            case UNRESOLVED_FILE:
//                obj = new Unresolved.UnresolvedFile(stream);
//                break;
//                
//            case UNRESOLVED_CLASS:
//                obj = new Unresolved.UnresolvedClass(stream);
//                break;

            case ENUM_IMPL:
                obj = new EnumImpl(stream);
                break;

            case CLASS_IMPL:
                obj = new ClassImpl(stream);
                break;

            case CLASS_IMPL_SPECIALIZATION:
                obj = new ClassImplSpecialization(stream);
                break;

            case CLASS_IMPL_FUNCTION_SPECIALIZATION:
                obj = new ClassImplFunctionSpecialization(stream);
                break;

            case FORWARD_CLASS:
                obj = new ForwardClass(stream);
                break;

            case FORWARD_ENUM:
                obj = new ForwardEnum(stream);
                break;

            case TYPEDEF_IMPL:
                obj = new TypedefImpl(stream);
                break;
                
            case TYPEALIAS_IMPL:
                obj = new TypeAliasImpl(stream);
                break;

            case MEMBER_TYPEDEF:
                obj = new ClassImpl.MemberTypedef(stream);
                break;
                
            case MEMBER_TYPEALIAS:
                obj = new ClassImpl.MemberTypeAliasImpl(stream);
                break;

            case NAMESPACE_IMPL:
                obj = new NamespaceImpl(stream);
                break;

            case NAMESPACE_DEF_IMPL:
                obj = new NamespaceDefinitionImpl(stream);
                break;

            case NAMESPACE_ALIAS_IMPL:
                obj = new NamespaceAliasImpl(stream);
                break;

            case USING_DECLARATION_IMPL:
                obj = new UsingDeclarationImpl(stream);
                break;

            case USING_DIRECTIVE_IMPL:
                obj = new UsingDirectiveImpl(stream);
                break;

            case CLASS_FORWARD_DECLARATION_IMPL:
                obj = new ClassForwardDeclarationImpl(stream);
                break;

            case ENUM_FORWARD_DECLARATION_IMPL:
                obj = new EnumForwardDeclarationImpl(stream);
                break;

            case CLASS_MEMBER_FORWARD_DECLARATION:
                obj = new ClassImpl.ClassMemberForwardDeclaration(stream);
                break;

            case ENUM_MEMBER_FORWARD_DECLARATION:
                obj = new ClassImpl.EnumMemberForwardDeclaration(stream);
                break;

            case FUNCTION_IMPL:
                obj = new FunctionImpl(stream);
                break;

            case FUNCTION_IMPL_EX:
                obj = new FunctionImplEx(stream);
                break;

            case FUNCTION_INSTANTIATION_IMPL:
                obj = new FunctionInstantiationImpl(stream);
                break;
                
            case DESTRUCTOR_DEF_IMPL:
                obj = new DestructorDefinitionImpl(stream);
                break;

            case CONSTRUCTOR_DEF_IMPL:
                obj = new ConstructorDefinitionImpl(stream);
                break;

            case CONSTRUCTOR_DEF_DECL_IMPL:
                obj = new ConstructorDDImpl(stream);
                break;

            case FUNCTION_DEF_IMPL:
                obj = new FunctionDefinitionImpl(stream);
                break;

            case DESTRUCTOR_DEF_DECL_IMPL:
                obj = new DestructorDDImpl(stream);
                break;

            case METHOD_DEF_DECL_IMPL:
                obj = new MethodDDImpl(stream);
                break;

            case CONSTRUCTOR_IMPL:
                obj = new ConstructorImpl(stream);
                break;

            case DESTRUCTOR_IMPL:
                obj = new DestructorImpl(stream);
                break;

            case METHOD_IMPL:
                obj = new MethodImpl(stream);
                break;

            case METHOD_IMPL_SPECIALIZATION:
                obj = new MethodImplSpecialization(stream);
                break;

            case FUNCTION_DEF_DECL_IMPL:
                obj = new FunctionDDImpl(stream);
                break;

            case FUNCTION_LAMBDA_IMPL:
                obj = new LambdaFunction(stream);
                break;

            case VARIABLE_DEF_IMPL:
                obj = new VariableDefinitionImpl(stream);
                break;

            case FIELD_IMPL:
                obj = new FieldImpl(stream);
                break;

            case VARIABLE_IMPL:
                obj = new VariableImpl(stream);
                break;

            case ENUMERATOR_IMPL:
                obj = new EnumeratorImpl(stream);
                break;

            case INCLUDE_IMPL:
                obj = new IncludeImpl(stream);
                break;

            case INHERITANCE_IMPL:
                obj = new InheritanceImpl(stream);
                break;

            case MACRO_IMPL:
                obj = new MacroImpl(stream);
                break;

            case FRIEND_CLASS_IMPL:
                obj = new FriendClassImpl(stream);
                break;

            case FRIEND_FUNCTION_IMPL:
                obj = new FriendFunctionImpl(stream);
                break;

            case FRIEND_FUNCTION_IMPL_EX:
                obj = new FriendFunctionImplEx(stream);
                break;

            case FRIEND_FUNCTION_DEF_IMPL:
                obj = new FriendFunctionDefinitionImpl(stream);
                break;

            case FRIEND_FUNCTION_DEF_DECL_IMPL:
                obj = new FriendFunctionDDImpl(stream);
                break;

            case DECLARATION_CONTAINER_PROJECT:
                obj = new DeclarationContainerProject(stream);
                break;

            case DECLARATION_CONTAINER_NAMESPACE:
                obj = new DeclarationContainerNamespace(stream);
                break;

            case CLASSIFIER_CONTAINER:
                obj = new ClassifierContainer(stream);
                break;

            case INCLUDED_FILE_STORAGE:
                obj = new IncludedFileContainer(stream);
                break;

            case TEMPLATE_PARAMETER_IMPL:
                obj = new TemplateParameterImpl(stream);
                break;

            case INSTANTIATION_CLASS:
                obj = new Instantiation.Class(stream);
                break;
                
            case PROGRAM_IMPL:
                obj = new ProgramImpl(stream);
                break;

            case SUBROUTINE_IMPL:
                obj = new SubroutineImpl(stream);
                break;

            case MODULE_IMPL:
                obj = new ModuleImpl(stream);
                break;

            case MODEL_INDEX:
                obj = ReferencesIndex.create(stream);
                break;
            default:
                throw new IllegalArgumentException("Unknown handler " + handler);  //NOI18N
        }
        return obj;
    }

    @Override
    public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
        SelfPersistent persistentObj = (SelfPersistent) obj;
        super.writeSelfPersistent(persistentObj, out);
    }

    @Override
    public Persistent read(RepositoryDataInput in) throws IOException {
        SelfPersistent persistentObj = super.readSelfPersistent(in);
        assert persistentObj == null || persistentObj instanceof Persistent;
        return (Persistent) persistentObj;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    // handlers to identify different classes of projects
    
    private static final short FIRST_INDEX                    = UIDObjectFactory.LAST_INDEX + 1;
    
    private static final short PROJECT_IMPL                   = FIRST_INDEX;
    private static final short LIB_PROJECT_IMPL               = PROJECT_IMPL + 1;    
    private static final short FILES_CONTAINER                = LIB_PROJECT_IMPL + 1;
    private static final short GRAPH_CONTAINER                = FILES_CONTAINER + 1;
    private static final short DECLARATION_CONTAINER_PROJECT  = GRAPH_CONTAINER + 1;
    private static final short DECLARATION_CONTAINER_NAMESPACE= DECLARATION_CONTAINER_PROJECT + 1;
    private static final short CLASSIFIER_CONTAINER           = DECLARATION_CONTAINER_NAMESPACE + 1;
    private static final short INCLUDED_FILE_STORAGE          = CLASSIFIER_CONTAINER + 1;
    private static final short FILE_IMPL                      = INCLUDED_FILE_STORAGE + 1;
    private static final short FILE_DECLARATIONS              = FILE_IMPL + 1;
    private static final short FILE_MACROS                    = FILE_DECLARATIONS + 1;
    private static final short FILE_INCLUDES                  = FILE_MACROS + 1;
    private static final short FILE_REFERENCES                = FILE_INCLUDES + 1;
    private static final short FILE_INSTANTIATIONS            = FILE_REFERENCES + 1;
    private static final short ENUM_IMPL                      = FILE_INSTANTIATIONS + 1;
    private static final short FORWARD_ENUM                   = ENUM_IMPL + 1;
    private static final short CLASS_IMPL_SPECIALIZATION      = FORWARD_ENUM + 1;
    private static final short CLASS_IMPL_FUNCTION_SPECIALIZATION = CLASS_IMPL_SPECIALIZATION + 1;
    private static final short FORWARD_CLASS                  = CLASS_IMPL_FUNCTION_SPECIALIZATION + 1;
    private static final short CLASS_IMPL                     = FORWARD_CLASS + 1;
//    private static final int UNRESOLVED_FILE                = CLASS_IMPL + 1;
//    private static final int UNRESOLVED_CLASS               = UNRESOLVED_FILE + 1;
//    private static final int TYPEDEF_IMPL                   = UNRESOLVED_CLASS + 1;
    private static final short TYPEDEF_IMPL                   = CLASS_IMPL + 1;
    private static final short TYPEALIAS_IMPL                 = TYPEDEF_IMPL + 1;
    private static final short MEMBER_TYPEDEF                 = TYPEALIAS_IMPL + 1;
    private static final short MEMBER_TYPEALIAS               = MEMBER_TYPEDEF + 1;
    private static final short NAMESPACE_IMPL                 = MEMBER_TYPEALIAS + 1;
    private static final short NAMESPACE_DEF_IMPL             = NAMESPACE_IMPL + 1;
    private static final short NAMESPACE_ALIAS_IMPL           = NAMESPACE_DEF_IMPL + 1;
    private static final short USING_DECLARATION_IMPL         = NAMESPACE_ALIAS_IMPL + 1;
    private static final short USING_DIRECTIVE_IMPL           = USING_DECLARATION_IMPL + 1;
    private static final short ENUM_FORWARD_DECLARATION_IMPL  = USING_DIRECTIVE_IMPL + 1;
    private static final short ENUM_MEMBER_FORWARD_DECLARATION= ENUM_FORWARD_DECLARATION_IMPL + 1;
    private static final short CLASS_FORWARD_DECLARATION_IMPL = ENUM_MEMBER_FORWARD_DECLARATION + 1;
    private static final short CLASS_MEMBER_FORWARD_DECLARATION = CLASS_FORWARD_DECLARATION_IMPL + 1;   
    private static final short FRIEND_CLASS_IMPL              = CLASS_MEMBER_FORWARD_DECLARATION + 1;
                
    // functions
    private static final short FUNCTION_IMPL                  = FRIEND_CLASS_IMPL + 1;
    private static final short FUNCTION_IMPL_EX               = FUNCTION_IMPL + 1;
    private static final short FUNCTION_INSTANTIATION_IMPL    = FUNCTION_IMPL_EX + 1;

    //// function definitons 
    private static final short DESTRUCTOR_DEF_IMPL            = FUNCTION_INSTANTIATION_IMPL + 1;
    private static final short CONSTRUCTOR_DEF_IMPL           = DESTRUCTOR_DEF_IMPL + 1;
    private static final short FUNCTION_DEF_IMPL              = CONSTRUCTOR_DEF_IMPL + 1;

    //// friends
    private static final short FRIEND_FUNCTION_IMPL           = FUNCTION_DEF_IMPL + 1;
    private static final short FRIEND_FUNCTION_IMPL_EX        = FRIEND_FUNCTION_IMPL + 1;
    private static final short FRIEND_FUNCTION_DEF_IMPL       = FRIEND_FUNCTION_IMPL_EX + 1;
    private static final short FRIEND_FUNCTION_DEF_DECL_IMPL  = FRIEND_FUNCTION_DEF_IMPL + 1;
    
    //// methods
    private static final short CONSTRUCTOR_DEF_DECL_IMPL      = FRIEND_FUNCTION_DEF_DECL_IMPL + 1;
    private static final short DESTRUCTOR_DEF_DECL_IMPL       = CONSTRUCTOR_DEF_DECL_IMPL + 1;
    private static final short METHOD_DEF_DECL_IMPL           = DESTRUCTOR_DEF_DECL_IMPL + 1;
    private static final short CONSTRUCTOR_IMPL               = METHOD_DEF_DECL_IMPL + 1;
    private static final short DESTRUCTOR_IMPL                = CONSTRUCTOR_IMPL + 1;
    private static final short METHOD_IMPL                    = DESTRUCTOR_IMPL + 1;
    private static final short METHOD_IMPL_SPECIALIZATION     = METHOD_IMPL + 1;
    
    private static final short FUNCTION_DEF_DECL_IMPL         = METHOD_IMPL_SPECIALIZATION + 1;
    private static final short FUNCTION_LAMBDA_IMPL           = FUNCTION_DEF_DECL_IMPL + 1;
    // end of functions
    
    // variables
    private static final short VARIABLE_IMPL                  = FUNCTION_LAMBDA_IMPL + 1;
    private static final short VARIABLE_DEF_IMPL              = VARIABLE_IMPL + 1;
    private static final short FIELD_IMPL                     = VARIABLE_DEF_IMPL + 1;
    
    private static final short ENUMERATOR_IMPL                = FIELD_IMPL + 1;

    private static final short INCLUDE_IMPL                   = ENUMERATOR_IMPL + 1;
    private static final short INHERITANCE_IMPL               = INCLUDE_IMPL + 1;
    private static final short MACRO_IMPL                     = INHERITANCE_IMPL + 1;
    private static final short TEMPLATE_PARAMETER_IMPL        = MACRO_IMPL + 1;

    // instantiations
    private static final short INSTANTIATION_CLASS            = TEMPLATE_PARAMETER_IMPL + 1;

    // fortran

    private static final short PROGRAM_IMPL                  = INSTANTIATION_CLASS + 1;
    private static final short SUBROUTINE_IMPL               = PROGRAM_IMPL + 1;
    private static final short MODULE_IMPL                   = SUBROUTINE_IMPL + 1;

    private static final short MODEL_INDEX                   = MODULE_IMPL + 1;
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX        
    public static final short LAST_INDEX = MODEL_INDEX;
}
