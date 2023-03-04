/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository;

/**
 *
 * @author sp153251
 */
public class Constructor implements IConstructor{
    private final ExecutableElement nbConstructor;
    private final java.lang.reflect.Constructor <?> jConstructor;
    private final IType owner;
    private ITypeDeclaration[] parameterTypes;

    public Constructor(IType owner, ExecutableElement constructor){
        assert constructor.getKind() == ElementKind.CONSTRUCTOR;
        this.nbConstructor = constructor;
        this.owner = owner;
        this.jConstructor = null;
    }
    
    public Constructor(IType owner, java.lang.reflect.Constructor<?> constructor) {
        this.owner = owner;
        this.jConstructor = constructor;
        this.nbConstructor = null;
    }
    
    @Override
    public ITypeDeclaration[] getParameterTypes() {
        if(parameterTypes == null){
            if(nbConstructor != null){
                List<? extends VariableElement> params = nbConstructor.getParameters();
                parameterTypes = new ITypeDeclaration[params.size()];
                for(int i=0; i<params.size(); i++){
                    VariableElement param = params.get(i);
                    parameterTypes[i] = typeToTypeDeclaration(param.asType());
                }
            } else {
                Class<?>[] types = jConstructor.getParameterTypes();
                java.lang.reflect.Type[] genericTypes = jConstructor.getGenericParameterTypes();
                parameterTypes = new ITypeDeclaration[types.length];
                ITypeRepository typeRepository = ((Type)owner).getTypeRepository();

                for (int index = 0, count = types.length; index < count; index++) {
                    IType type = typeRepository.getType(types[index]);
                    parameterTypes[index] = new TypeDeclaration(typeRepository, type, genericTypes[index], types[index].isArray());
                }
            }
        }
        return parameterTypes;
    }
 
    private TypeDeclaration typeToTypeDeclaration(TypeMirror tMirror){
        int dimension = 0;
        TypeMirror aType =  tMirror;
        ITypeDeclaration[] generics = null;
        if(tMirror.getKind() == TypeKind.ARRAY){
            for(;aType.getKind() == TypeKind.ARRAY; aType =  ((ArrayType)tMirror).getComponentType()) {
                dimension++;
            }
        }
        if(aType.getKind() == TypeKind.DECLARED){
            DeclaredType dType = (DeclaredType) aType;
            List<? extends TypeMirror> parameters = dType.getTypeArguments();
            if( parameters!=null && !parameters.isEmpty()){
                generics = new ITypeDeclaration[parameters.size()];
                int i=0;
                for(TypeMirror gType: parameters){
                    generics[i] = typeToTypeDeclaration(gType);
                    i++;
                }
            }
        }
        return new TypeDeclaration(owner, generics, dimension);
    }
}
