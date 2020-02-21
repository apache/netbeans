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
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;


/**
 * File and offset -based key for declarations
 */

/*package*/ abstract class OffsetableDeclarationKey extends OffsetableKey {
    
    /*package*/ static OffsetableDeclarationKey createOffsetableDeclarationKey(OffsetableDeclarationBase<?> obj) {
        switch (obj.getKind()) {
            case MACRO:
                throw new IllegalArgumentException();
            case ASM:
                return new OffsetableDeclarationKey.ASM(obj);
            case BUILT_IN:
                return new OffsetableDeclarationKey.BUILT_IN(obj);
            case CLASS:
                return new OffsetableDeclarationKey.CLASS(obj);
            case ENUM:
                return new OffsetableDeclarationKey.ENUM(obj);
            case FUNCTION:
                return new OffsetableDeclarationKey.FUNCTION(obj);
            case NAMESPACE_DEFINITION:
                return new OffsetableDeclarationKey.NAMESPACE_DEFINITION(obj);
            case STRUCT:
                return new OffsetableDeclarationKey.STRUCT(obj);
            case TEMPLATE_DECLARATION:
                return new OffsetableDeclarationKey.TEMPLATE_DECLARATION(obj);
            case UNION:
                return new OffsetableDeclarationKey.UNION(obj);
            case VARIABLE:
                return new OffsetableDeclarationKey.VARIABLE(obj);
            case NAMESPACE_ALIAS:
                return new OffsetableDeclarationKey.NAMESPACE_ALIAS(obj);
            case ENUMERATOR:
                return new OffsetableDeclarationKey.ENUMERATOR(obj);
            case FUNCTION_DEFINITION:
                return new OffsetableDeclarationKey.FUNCTION_DEFINITION(obj);
            case FUNCTION_LAMBDA:
                return new OffsetableDeclarationKey.FUNCTION_LAMBDA(obj);
            case FUNCTION_INSTANTIATION:
                return new OffsetableDeclarationKey.FUNCTION_INSTANTIATION(obj);
            case USING_DIRECTIVE:
                return new OffsetableDeclarationKey.USING_DIRECTIVE(obj);
            case TEMPLATE_PARAMETER:
                return new OffsetableDeclarationKey.TEMPLATE_PARAMETER(obj);
            case CLASS_FRIEND_DECLARATION:
                return new OffsetableDeclarationKey.CLASS_FRIEND_DECLARATION(obj);
            case TEMPLATE_SPECIALIZATION:
                return new OffsetableDeclarationKey.TEMPLATE_SPECIALIZATION(obj);
            case TYPEDEF:
                return new OffsetableDeclarationKey.TYPEDEF(obj);
            case TYPEALIAS:
                return new OffsetableDeclarationKey.TYPEALIAS(obj);
            case USING_DECLARATION:
                return new OffsetableDeclarationKey.USING_DECLARATION(obj);
            case VARIABLE_DEFINITION:
                return new OffsetableDeclarationKey.VARIABLE_DEFINITION(obj);
            case CLASS_FORWARD_DECLARATION:
                return new OffsetableDeclarationKey.CLASS_FORWARD_DECLARATION(obj);
            case ENUM_FORWARD_DECLARATION:
                return new OffsetableDeclarationKey.ENUM_FORWARD_DECLARATION(obj);
            case FUNCTION_FRIEND:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND(obj);
            case FUNCTION_FRIEND_DEFINITION:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND_DEFINITION(obj);
            case FUNCTION_TYPE:
                return new OffsetableDeclarationKey.FUNCTION_TYPE(obj);
        }
        throw new IllegalArgumentException();
    }

    /*package*/ static OffsetableDeclarationKey createUnnamedOffsetableDeclarationKey(OffsetableDeclarationBase<?> obj, int index) {
        switch (obj.getKind()) {
            case MACRO:
                throw new IllegalArgumentException();
            case ASM:
                return new OffsetableDeclarationKey.ASM(obj, index);
            case BUILT_IN:
                return new OffsetableDeclarationKey.BUILT_IN(obj, index);
            case CLASS:
                return new OffsetableDeclarationKey.CLASS(obj, index);
            case ENUM:
                return new OffsetableDeclarationKey.ENUM(obj, index);
            case FUNCTION:
                return new OffsetableDeclarationKey.FUNCTION(obj, index);
            case NAMESPACE_DEFINITION:
                return new OffsetableDeclarationKey.NAMESPACE_DEFINITION(obj, index);
            case STRUCT:
                return new OffsetableDeclarationKey.STRUCT(obj, index);
            case TEMPLATE_DECLARATION:
                return new OffsetableDeclarationKey.TEMPLATE_DECLARATION(obj, index);
            case UNION:
                return new OffsetableDeclarationKey.UNION(obj, index);
            case VARIABLE:
                return new OffsetableDeclarationKey.VARIABLE(obj, index);
            case NAMESPACE_ALIAS:
                return new OffsetableDeclarationKey.NAMESPACE_ALIAS(obj, index);
            case ENUMERATOR:
                return new OffsetableDeclarationKey.ENUMERATOR(obj, index);
            case FUNCTION_DEFINITION:
                return new OffsetableDeclarationKey.FUNCTION_DEFINITION(obj, index);
            case FUNCTION_LAMBDA:
                return new OffsetableDeclarationKey.FUNCTION_LAMBDA(obj, index);
            case FUNCTION_INSTANTIATION:
                return new OffsetableDeclarationKey.FUNCTION_INSTANTIATION(obj, index);
            case USING_DIRECTIVE:
                return new OffsetableDeclarationKey.USING_DIRECTIVE(obj, index);
            case TEMPLATE_PARAMETER:
                return new OffsetableDeclarationKey.TEMPLATE_PARAMETER(obj, index);
            case CLASS_FRIEND_DECLARATION:
                return new OffsetableDeclarationKey.CLASS_FRIEND_DECLARATION(obj, index);
            case TEMPLATE_SPECIALIZATION:
                return new OffsetableDeclarationKey.TEMPLATE_SPECIALIZATION(obj, index);
            case TYPEDEF:
                return new OffsetableDeclarationKey.TYPEDEF(obj, index);
            case TYPEALIAS:
                return new OffsetableDeclarationKey.TYPEALIAS(obj, index);
            case USING_DECLARATION:
                return new OffsetableDeclarationKey.USING_DECLARATION(obj, index);
            case VARIABLE_DEFINITION:
                return new OffsetableDeclarationKey.VARIABLE_DEFINITION(obj, index);
            case CLASS_FORWARD_DECLARATION:
                return new OffsetableDeclarationKey.CLASS_FORWARD_DECLARATION(obj, index);
            case ENUM_FORWARD_DECLARATION:
                return new OffsetableDeclarationKey.ENUM_FORWARD_DECLARATION(obj, index);
            case FUNCTION_FRIEND:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND(obj, index);
            case FUNCTION_FRIEND_DEFINITION:
                return new OffsetableDeclarationKey.FUNCTION_FRIEND_DEFINITION(obj, index);
            case FUNCTION_TYPE:
                return new OffsetableDeclarationKey.FUNCTION_TYPE(obj, index);
        }
        throw new IllegalArgumentException();
    }
    
    
    private OffsetableDeclarationKey(OffsetableDeclarationBase<?> obj) {
	super((FileImpl) obj.getContainingFile(), getSmartStartOffset(obj), getSmartEndOffset(obj), getName(obj));
	// we use name, because all other (FQN and UniqueName) could change
	// and name is fixed value
    }
    
    private OffsetableDeclarationKey(OffsetableDeclarationBase<?> obj, int index) {
	super((FileImpl) obj.getContainingFile(), getSmartStartOffset(obj), getSmartEndOffset(obj), Integer.toString(index));
	// we use index for unnamed objects
    }

    private OffsetableDeclarationKey(KeyDataPresentation presentation) {
        super(presentation);
    }
    
    private static CharSequence getName(OffsetableDeclarationBase<?> obj) {
        if (CsmKindUtilities.isFunction(obj) && obj instanceof FunctionImpl) {
            FunctionImpl fun = (FunctionImpl) obj;
            CharSequence funExtraSuffix = fun.getUIDExtraSuffix();
            if (funExtraSuffix != null) {
                StringBuilder sb = new StringBuilder(fun.getName()); 
                sb.append(funExtraSuffix);
                return sb.toString();
            }
        }  else if (CsmKindUtilities.isClass(obj) && obj instanceof ClassImpl) {
            ClassImpl cls = (ClassImpl) obj;
            CsmScope scope = cls.getScope();
            if (CsmKindUtilities.isOffsetable(scope)) {
                CsmOffsetable offsetableScope = (CsmOffsetable) scope;
                if (cls.getStartOffset() == offsetableScope.getStartOffset() 
                        && cls.getEndOffset() == offsetableScope.getEndOffset()) {
                    // This is a class defined via macro - it can be
                    // indistinguishable from it's scope if there are other classes
                    // with the same name. Example: #define MACRO struct A{struct B{struct A{};};};
                    StringBuilder sb = new StringBuilder(cls.getName()); 
                    sb.append(KeyUtilities.UID_INTERNAL_DATA_PREFIX);
                    sb.append(cls.getQualifiedName());
                    return sb.toString();
                }
            }
        }
        return obj.getName();
    }       
    
    private static int getSmartEndOffset(OffsetableDeclarationBase<?> obj) {
         return obj.getEndOffset();
    }
    
    private static int getSmartStartOffset(OffsetableDeclarationBase<?> obj) {
        // #132865 ClassCastException in Go To Type -
        // ensure that members and non-members has different keys
        // also make sure that function and fake function has different keys
        int result = obj.getStartOffset();
        if (obj instanceof ForwardClass) {
            result |= 0x80000000;
        } else if( obj instanceof CsmMember) {
            // do nothing
        } else if (FunctionImplEx.isFakeFunction(obj)) {
            result |= 0x80000000;
        } else {
            result |= 0x40000000;
        }
        return result;
    }

    @Override
    int getStartOffset() {
        return super.getStartOffset() & 0x3FFFFFFF;
    }
    
    /*package*/ OffsetableDeclarationKey(RepositoryDataInput aStream) throws IOException {
	super(aStream);
    }
    
    
    @Override
    public PersistentFactory getPersistentFactory() {
	return CsmObjectFactory.instance();
    }
    
    @Override
    public String toString() {
	String retValue;
	
	retValue = "OffsDeclKey: " + super.toString(); // NOI18N
	return retValue;
    }
    
    @Override
    public int getSecondaryDepth() {
	return super.getSecondaryDepth() + 1;
    }
    
    @Override
    public int getSecondaryAt(int level) {
	if (level == 0) {
	    return getHandler();
	}  else {
	    return super.getSecondaryAt(level - 1);
	}
    }
    
    static final class ASM extends OffsetableDeclarationKey {
        ASM(OffsetableDeclarationBase<?> obj) {super(obj);}
        ASM(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        ASM(KeyDataPresentation presentation) {super(presentation);}
        ASM(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.ASM);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_ASM_KEY;}
        
    }
    static final class BUILT_IN extends OffsetableDeclarationKey {
        BUILT_IN(OffsetableDeclarationBase<?> obj) {super(obj);}
        BUILT_IN(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        BUILT_IN(KeyDataPresentation presentation) {super(presentation);}
        BUILT_IN(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.BUILT_IN);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_BUILT_IN_KEY;}
    }
    static final class CLASS extends OffsetableDeclarationKey {
        CLASS(OffsetableDeclarationBase<?> obj) {super(obj);}
        CLASS(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        CLASS(KeyDataPresentation presentation) {super(presentation);}
        CLASS(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_CLASS_KEY;}
    }
    static final class ENUM extends OffsetableDeclarationKey {
        ENUM(OffsetableDeclarationBase<?> obj) {super(obj);}
        ENUM(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        ENUM(KeyDataPresentation presentation) {super(presentation);}
        ENUM(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_ENUM_KEY;}
    }
    static final class FUNCTION extends OffsetableDeclarationKey {
        FUNCTION(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_KEY;}
    }
    static final class NAMESPACE_DEFINITION extends OffsetableDeclarationKey {
        NAMESPACE_DEFINITION(OffsetableDeclarationBase<?> obj) {super(obj);}
        NAMESPACE_DEFINITION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        NAMESPACE_DEFINITION(KeyDataPresentation presentation) {super(presentation);}
        NAMESPACE_DEFINITION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.NAMESPACE_DEFINITION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_NAMESPACE_DEFINITION_KEY;}
    }
    static final class STRUCT extends OffsetableDeclarationKey {
        STRUCT(OffsetableDeclarationBase<?> obj) {super(obj);}
        STRUCT(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        STRUCT(KeyDataPresentation presentation) {super(presentation);}
        STRUCT(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_STRUCT_KEY;}
    }
    static final class TEMPLATE_DECLARATION extends OffsetableDeclarationKey {
        TEMPLATE_DECLARATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        TEMPLATE_DECLARATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        TEMPLATE_DECLARATION(KeyDataPresentation presentation) {super(presentation);}
        TEMPLATE_DECLARATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.TEMPLATE_DECLARATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_TEMPLATE_DECLARATION_KEY;}
    }
    static final class UNION extends OffsetableDeclarationKey {
        UNION(OffsetableDeclarationBase<?> obj) {super(obj);}
        UNION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        UNION(KeyDataPresentation presentation) {super(presentation);}
        UNION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.UNION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_UNION_KEY;}
    }
    static final class VARIABLE extends OffsetableDeclarationKey {
        VARIABLE(OffsetableDeclarationBase<?> obj) {super(obj);}
        VARIABLE(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        VARIABLE(KeyDataPresentation presentation) {super(presentation);}
        VARIABLE(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_VARIABLE_KEY;}
    }
    static final class NAMESPACE_ALIAS extends OffsetableDeclarationKey {
        NAMESPACE_ALIAS(OffsetableDeclarationBase<?> obj) {super(obj);}
        NAMESPACE_ALIAS(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        NAMESPACE_ALIAS(KeyDataPresentation presentation) {super(presentation);}
        NAMESPACE_ALIAS(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.NAMESPACE_ALIAS);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_NAMESPACE_ALIAS_KEY;}
    }
    static final class ENUMERATOR extends OffsetableDeclarationKey {
        ENUMERATOR(OffsetableDeclarationBase<?> obj) {super(obj);}
        ENUMERATOR(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        ENUMERATOR(KeyDataPresentation presentation) {super(presentation);}
        ENUMERATOR(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUMERATOR);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_ENUMERATOR_KEY;}
    }
    static final class FUNCTION_DEFINITION extends OffsetableDeclarationKey {
        FUNCTION_DEFINITION(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_DEFINITION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_DEFINITION(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_DEFINITION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_DEFINITION_KEY;}
    }
    static final class FUNCTION_LAMBDA extends OffsetableDeclarationKey {
        FUNCTION_LAMBDA(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_LAMBDA(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_LAMBDA(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_LAMBDA(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_LAMBDA);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_LAMBDA_KEY;}
    }
    static final class FUNCTION_INSTANTIATION extends OffsetableDeclarationKey {
        FUNCTION_INSTANTIATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_INSTANTIATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_INSTANTIATION(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_INSTANTIATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_INSTANTIATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_INSTANTIATION_KEY;}
    }
    static final class USING_DIRECTIVE extends OffsetableDeclarationKey {
        USING_DIRECTIVE(OffsetableDeclarationBase<?> obj) {super(obj);}
        USING_DIRECTIVE(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        USING_DIRECTIVE(KeyDataPresentation presentation) {super(presentation);}
        USING_DIRECTIVE(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.USING_DIRECTIVE);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_USING_DIRECTIVE_KEY;}
    }
    static final class TEMPLATE_PARAMETER extends OffsetableDeclarationKey {
        TEMPLATE_PARAMETER(OffsetableDeclarationBase<?> obj) {super(obj);}
        TEMPLATE_PARAMETER(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        TEMPLATE_PARAMETER(KeyDataPresentation presentation) {super(presentation);}
        TEMPLATE_PARAMETER(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.TEMPLATE_PARAMETER);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_TEMPLATE_PARAMETER_KEY;}
    }
    static final class CLASS_FRIEND_DECLARATION extends OffsetableDeclarationKey {
        CLASS_FRIEND_DECLARATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        CLASS_FRIEND_DECLARATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        CLASS_FRIEND_DECLARATION(KeyDataPresentation presentation) {super(presentation);}
        CLASS_FRIEND_DECLARATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_CLASS_FRIEND_DECLARATION_KEY;}
    }
    static final class TEMPLATE_SPECIALIZATION extends OffsetableDeclarationKey {
        TEMPLATE_SPECIALIZATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        TEMPLATE_SPECIALIZATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        TEMPLATE_SPECIALIZATION(KeyDataPresentation presentation) {super(presentation);}
        TEMPLATE_SPECIALIZATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.TEMPLATE_SPECIALIZATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_TEMPLATE_SPECIALIZATION_KEY;}
    }
    static final class TYPEDEF extends OffsetableDeclarationKey {
        TYPEDEF(OffsetableDeclarationBase<?> obj) {super(obj);}
        TYPEDEF(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        TYPEDEF(KeyDataPresentation presentation) {super(presentation);}
        TYPEDEF(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEDEF);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_TYPEDEF_KEY;}
    }
    static final class TYPEALIAS extends OffsetableDeclarationKey {
        TYPEALIAS(OffsetableDeclarationBase<?> obj) {super(obj);}
        TYPEALIAS(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        TYPEALIAS(KeyDataPresentation presentation) {super(presentation);}
        TYPEALIAS(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEALIAS);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_TYPEALIAS_KEY;}
    }
    static final class USING_DECLARATION extends OffsetableDeclarationKey {
        USING_DECLARATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        USING_DECLARATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        USING_DECLARATION(KeyDataPresentation presentation) {super(presentation);}
        USING_DECLARATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.USING_DECLARATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_USING_DECLARATION_KEY;}
    }
    static final class VARIABLE_DEFINITION extends OffsetableDeclarationKey {
        VARIABLE_DEFINITION(OffsetableDeclarationBase<?> obj) {super(obj);}
        VARIABLE_DEFINITION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        VARIABLE_DEFINITION(KeyDataPresentation presentation) {super(presentation);}
        VARIABLE_DEFINITION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE_DEFINITION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_VARIABLE_DEFINITION_KEY;}
    }
    static final class CLASS_FORWARD_DECLARATION extends OffsetableDeclarationKey {
        CLASS_FORWARD_DECLARATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        CLASS_FORWARD_DECLARATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        CLASS_FORWARD_DECLARATION(KeyDataPresentation presentation) {super(presentation);}
        CLASS_FORWARD_DECLARATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_CLASS_FORWARD_DECLARATION_KEY;}
    }
    static final class ENUM_FORWARD_DECLARATION extends OffsetableDeclarationKey {
        ENUM_FORWARD_DECLARATION(OffsetableDeclarationBase<?> obj) {super(obj);}
        ENUM_FORWARD_DECLARATION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        ENUM_FORWARD_DECLARATION(KeyDataPresentation presentation) {super(presentation);}
        ENUM_FORWARD_DECLARATION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_ENUM_FORWARD_DECLARATION_KEY;}
    }
    static final class FUNCTION_FRIEND extends OffsetableDeclarationKey {
        FUNCTION_FRIEND(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_FRIEND(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_FRIEND(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_FRIEND(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_FRIEND_KEY;}
    }
    static final class FUNCTION_FRIEND_DEFINITION extends OffsetableDeclarationKey {
        FUNCTION_FRIEND_DEFINITION(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_FRIEND_DEFINITION(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_FRIEND_DEFINITION(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_FRIEND_DEFINITION(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_FRIEND_DEFINITION_KEY;}
    }
    static final class FUNCTION_TYPE extends OffsetableDeclarationKey {
        FUNCTION_TYPE(OffsetableDeclarationBase<?> obj) {super(obj);}
        FUNCTION_TYPE(OffsetableDeclarationBase<?> obj, int index) {super(obj, index);}
        FUNCTION_TYPE(KeyDataPresentation presentation) {super(presentation);}
        FUNCTION_TYPE(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_TYPE);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_FUNCTION_TYPE_KEY;}
    }    
}
