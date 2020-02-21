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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.support.KeyFactory.class)
public class KeyObjectFactory extends KeyFactory {
    
    /** Creates a new instance of KeyObjectFactory */
    public KeyObjectFactory() {
    }
    
    
    @Override
    public void writeKey(Key aKey, RepositoryDataOutput aStream) throws IOException {
        assert aKey instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)aKey, aStream);
    }
    
    @Override
    public Key readKey(RepositoryDataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out instanceof Key;
        // no reasone to cache declaration keys.
        boolean share = !(out instanceof OffsetableDeclarationKey);
        if (share) {
            Key shared = KeyManager.instance().getSharedKey((Key)out);
            assert shared != null;
            assert shared instanceof SelfPersistent;
            out = (SelfPersistent) shared;
        }
        return (Key)out;
    }
    
    @Override
    public void writeKeyCollection(Collection<Key> aCollection, RepositoryDataOutput aStream ) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aCollection.size();
        aStream.writeInt(collSize);
        
        Iterator <Key> iter = aCollection.iterator();
        
        while (iter.hasNext()) {
            Key aKey = iter.next();
            assert aKey != null;
            writeKey(aKey, aStream);
        }
    }
    
    @Override
    public void readKeyCollection(Collection<Key> aCollection, RepositoryDataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            Key aKey = readKey(aStream);
            assert aKey != null;
            aCollection.add(aKey);
        }
    }
    
    @Override
    protected short getHandler(Object object) {
        short aHandle ;
        if (object instanceof KeyDataPresentation) {
            aHandle = ((KeyDataPresentation)object).getHandler();
        } else {
            throw new IllegalArgumentException("The Key is an instance of the unknown final class " + object.getClass().getName());  // NOI18N
        }
        return aHandle;
    }
    
    @Override
    protected SelfPersistent createObject(short handler, RepositoryDataInput aStream) throws IOException {
        SelfPersistent aKey;
        boolean share = true;
        switch (handler) {
            case KEY_PROJECT_KEY:
                aKey = new ProjectKey(aStream);
                break;
            case KEY_NAMESPACE_KEY:
                aKey = new NamespaceKey(aStream);
                break;
            case KEY_FILE_KEY:
                aKey = new FileKey(aStream);
                break;
            case KEY_FILE_DECLARATIONS_KEY:
                aKey = new FileDeclarationsKey(aStream);
                break;
            case KEY_FILE_MACROS_KEY:
                aKey = new FileMacrosKey(aStream);
                break;
            case KEY_FILE_INCLUDES_KEY:
                aKey = new FileIncludesKey(aStream);
                break;
            case KEY_FILE_REFERENCES_KEY:
                aKey = new FileReferencesKey(aStream);
                break;
            case KEY_FILE_INSTANTIATIONS_KEY:
                aKey = new FileInstantiationsKey(aStream);
                break;
            case KEY_MACRO_KEY:
                aKey = new MacroKey(aStream);
                break;
            case KEY_INCLUDE_KEY:
                aKey = new IncludeKey(aStream);
                break;
            case KEY_INHERITANCE_PUBLIC_KEY:
                aKey = new InheritanceKey.PUBLIC(aStream);
                break;
            case KEY_INHERITANCE_PRIVATE_KEY:
                aKey = new InheritanceKey.PRIVATE(aStream);
                break;
            case KEY_INHERITANCE_PROTECTED_KEY:
                aKey = new InheritanceKey.PROTECTED(aStream);
                break;
            case KEY_INHERITANCE_NONE_KEY:
                aKey = new InheritanceKey.NONE(aStream);
                break;
            case KEY_PARAM_LIST_KEY:
                aKey = new ParamListKey(aStream);
                break;
            case KEY_ASM_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.ASM(aStream);
                break;
            case KEY_BUILT_IN_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.BUILT_IN(aStream);
                break;
            case KEY_CLASS_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.CLASS(aStream);
                break;
            case KEY_ENUM_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.ENUM(aStream);
                break;
            case KEY_FUNCTION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION(aStream);
                break;
            case KEY_NAMESPACE_DEFINITION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.NAMESPACE_DEFINITION(aStream);
                break;
            case KEY_STRUCT_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.STRUCT(aStream);
                break;
            case KEY_TEMPLATE_DECLARATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.TEMPLATE_DECLARATION(aStream);
                break;
            case KEY_UNION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.UNION(aStream);
                break;
            case KEY_VARIABLE_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.VARIABLE(aStream);
                break;
            case KEY_NAMESPACE_ALIAS_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.NAMESPACE_ALIAS(aStream);
                break;
            case KEY_ENUMERATOR_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.ENUMERATOR(aStream);
                break;
            case KEY_FUNCTION_DEFINITION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION_DEFINITION(aStream);
                break;
            case KEY_FUNCTION_LAMBDA_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION_LAMBDA(aStream);
                break;
            case KEY_FUNCTION_INSTANTIATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION_INSTANTIATION(aStream);
                break;
            case KEY_USING_DIRECTIVE_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.USING_DIRECTIVE(aStream);
                break;
            case KEY_TEMPLATE_PARAMETER_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.TEMPLATE_PARAMETER(aStream);
                break;
            case KEY_CLASS_FRIEND_DECLARATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.CLASS_FRIEND_DECLARATION(aStream);
                break;
            case KEY_TEMPLATE_SPECIALIZATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.TEMPLATE_SPECIALIZATION(aStream);
                break;
            case KEY_TYPEDEF_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.TYPEDEF(aStream);
                break;
            case KEY_TYPEALIAS_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.TYPEALIAS(aStream);
                break;
            case KEY_USING_DECLARATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.USING_DECLARATION(aStream);
                break;
            case KEY_VARIABLE_DEFINITION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.VARIABLE_DEFINITION(aStream);
                break;
            case KEY_CLASS_FORWARD_DECLARATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.CLASS_FORWARD_DECLARATION(aStream);
                break;
            case KEY_ENUM_FORWARD_DECLARATION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.ENUM_FORWARD_DECLARATION(aStream);
                break;
            case KEY_FUNCTION_FRIEND_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION_FRIEND(aStream);
                break;
            case KEY_FUNCTION_FRIEND_DEFINITION_KEY:
                share = false;
                aKey = new OffsetableDeclarationKey.FUNCTION_FRIEND_DEFINITION(aStream);
                break;
            case KEY_INSTANTIATION_KEY:
                share = false;
                aKey = new InstantiationKey(aStream);
                break;
            case KEY_PROJECT_DECLARATION_CONTAINER_KEY:
                aKey = new ProjectDeclarationContainerKey(aStream);
                break;
            case KEY_FILE_CONTAINER_KEY:
                aKey = new FileContainerKey(aStream);
                break;
            case KEY_GRAPH_CONTAINER_KEY:
                aKey = new GraphContainerKey(aStream);
                break;
            case KEY_NS_DECLARATION_CONTAINER_KEY:
                aKey = new NamespaceDeclarationContainerKey(aStream);
                break;
            case KEY_CLASSIFIER_CONTAINER_KEY:
                aKey = new ClassifierContainerKey(aStream);
                break;
            case KEY_INCLUDED_FILE_STORAGE_KEY:
                aKey = new IncludedFileStorageKey(aStream);
                break;
            case KEY_MODEL_INDEX_KEY:
                aKey = new ReferencesIndexKey(aStream);
                break;
            default:
                throw new IllegalArgumentException("Unknown hander was provided: " + handler);  // NOI18N
        }
        if (share) {
            Key shared = KeyManager.instance().getSharedKey((Key)aKey);
            assert shared != null;
            assert shared instanceof SelfPersistent;
            aKey = (SelfPersistent) shared;
        }

        return aKey;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // constants which defines the handle of a key in the stream
    
    private static final short FIRST_INDEX        = AbstractObjectFactory.LAST_INDEX + 1;
    
    public static final short KEY_PROJECT_KEY    = FIRST_INDEX;
    public static final short KEY_NAMESPACE_KEY  = KEY_PROJECT_KEY + 1;
    public static final short KEY_FILE_KEY       = KEY_NAMESPACE_KEY + 1;
    public static final short KEY_FILE_DECLARATIONS_KEY = KEY_FILE_KEY + 1;
    public static final short KEY_FILE_MACROS_KEY = KEY_FILE_DECLARATIONS_KEY + 1;
    public static final short KEY_FILE_INCLUDES_KEY = KEY_FILE_MACROS_KEY + 1;
    public static final short KEY_FILE_REFERENCES_KEY = KEY_FILE_INCLUDES_KEY + 1;
    public static final short KEY_FILE_INSTANTIATIONS_KEY = KEY_FILE_REFERENCES_KEY + 1;
    public static final short KEY_MACRO_KEY      = KEY_FILE_INSTANTIATIONS_KEY + 1;
    public static final short KEY_INCLUDE_KEY    = KEY_MACRO_KEY + 1;
    
    public static final short KEY_INHERITANCE_PUBLIC_KEY = KEY_INCLUDE_KEY + 1;
    public static final short KEY_INHERITANCE_PRIVATE_KEY = KEY_INHERITANCE_PUBLIC_KEY + 1;
    public static final short KEY_INHERITANCE_PROTECTED_KEY = KEY_INHERITANCE_PRIVATE_KEY + 1;
    public static final short KEY_INHERITANCE_NONE_KEY = KEY_INHERITANCE_PROTECTED_KEY + 1;
    
    public static final short KEY_PARAM_LIST_KEY  = KEY_INHERITANCE_NONE_KEY + 1;
    
    public static final short KEY_ASM_KEY = KEY_PARAM_LIST_KEY + 1;
    public static final short KEY_BUILT_IN_KEY = KEY_ASM_KEY + 1;
    public static final short KEY_CLASS_KEY = KEY_BUILT_IN_KEY + 1;
    public static final short KEY_ENUM_KEY = KEY_CLASS_KEY + 1;
    public static final short KEY_FUNCTION_KEY = KEY_ENUM_KEY + 1;
    public static final short KEY_NAMESPACE_DEFINITION_KEY = KEY_FUNCTION_KEY + 1;
    public static final short KEY_STRUCT_KEY = KEY_NAMESPACE_DEFINITION_KEY + 1;
    public static final short KEY_TEMPLATE_DECLARATION_KEY = KEY_STRUCT_KEY + 1;
    public static final short KEY_UNION_KEY = KEY_TEMPLATE_DECLARATION_KEY + 1;
    public static final short KEY_VARIABLE_KEY = KEY_UNION_KEY + 1;
    public static final short KEY_NAMESPACE_ALIAS_KEY = KEY_VARIABLE_KEY + 1;
    public static final short KEY_ENUMERATOR_KEY = KEY_NAMESPACE_ALIAS_KEY + 1;
    public static final short KEY_FUNCTION_DEFINITION_KEY = KEY_ENUMERATOR_KEY + 1;
    public static final short KEY_FUNCTION_LAMBDA_KEY = KEY_FUNCTION_DEFINITION_KEY + 1;
    public static final short KEY_FUNCTION_INSTANTIATION_KEY = KEY_FUNCTION_LAMBDA_KEY + 1;
    public static final short KEY_USING_DIRECTIVE_KEY = KEY_FUNCTION_INSTANTIATION_KEY + 1;
    public static final short KEY_TEMPLATE_PARAMETER_KEY = KEY_USING_DIRECTIVE_KEY + 1;
    public static final short KEY_CLASS_FRIEND_DECLARATION_KEY = KEY_TEMPLATE_PARAMETER_KEY + 1;
    public static final short KEY_TEMPLATE_SPECIALIZATION_KEY = KEY_CLASS_FRIEND_DECLARATION_KEY + 1;
    public static final short KEY_TYPEDEF_KEY = KEY_TEMPLATE_SPECIALIZATION_KEY + 1;
    public static final short KEY_TYPEALIAS_KEY = KEY_TYPEDEF_KEY + 1;
    public static final short KEY_USING_DECLARATION_KEY = KEY_TYPEALIAS_KEY + 1;
    public static final short KEY_VARIABLE_DEFINITION_KEY = KEY_USING_DECLARATION_KEY + 1;
    public static final short KEY_CLASS_FORWARD_DECLARATION_KEY = KEY_VARIABLE_DEFINITION_KEY + 1;
    public static final short KEY_ENUM_FORWARD_DECLARATION_KEY = KEY_CLASS_FORWARD_DECLARATION_KEY + 1;
    public static final short KEY_FUNCTION_FRIEND_KEY = KEY_ENUM_FORWARD_DECLARATION_KEY + 1;
    public static final short KEY_FUNCTION_FRIEND_DEFINITION_KEY = KEY_FUNCTION_FRIEND_KEY + 1;
    public static final short KEY_FUNCTION_TYPE_KEY = KEY_FUNCTION_FRIEND_DEFINITION_KEY + 1;
    
    public static final short KEY_INSTANTIATION_KEY = KEY_FUNCTION_TYPE_KEY + 1;
    
    public static final short KEY_PROJECT_DECLARATION_CONTAINER_KEY = KEY_INSTANTIATION_KEY + 1;
    public static final short KEY_FILE_CONTAINER_KEY = KEY_PROJECT_DECLARATION_CONTAINER_KEY + 1;
    public static final short KEY_GRAPH_CONTAINER_KEY = KEY_FILE_CONTAINER_KEY    + 1;
    public static final short KEY_NS_DECLARATION_CONTAINER_KEY = KEY_GRAPH_CONTAINER_KEY + 1;
    public static final short KEY_CLASSIFIER_CONTAINER_KEY = KEY_NS_DECLARATION_CONTAINER_KEY + 1;
    public static final short KEY_INCLUDED_FILE_STORAGE_KEY = KEY_CLASSIFIER_CONTAINER_KEY + 1;
    
    public static final short KEY_MODEL_INDEX_KEY = KEY_INCLUDED_FILE_STORAGE_KEY + 1;
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX    
    public static final short LAST_INDEX = KEY_MODEL_INDEX_KEY;
}
