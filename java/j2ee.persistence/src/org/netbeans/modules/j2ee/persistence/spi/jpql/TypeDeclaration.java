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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository;

/**
 * TODO: rewrite
 * @author sp153251
 */
public class TypeDeclaration implements ITypeDeclaration {

    /**
     * Determines whether the type declaration represents an array.
     */
    private boolean array;
    /**
     * The actual type that contains the generics, if any is present.
     */
    private Object genericType;
    /**
     * The cached {@link ITypeDeclaration ITypeDeclarations} representing the generics of the {@link
     * Type}.
     */
    private ITypeDeclaration[] genericTypes;
    /**
     * The external form of the Java type.
     */
    private final IType type;
    /**
     * The repository of {@link IType ITypes}.
     */
    private ITypeRepository typeRepository;
    
    private int dimensionality=-1;

    /**
     * Creates a new <code>TypeDeclaration</code>.
     *
     * @param typeRepository The repository of {@link IType ITypes}
     * @param type The external form of the Java type
     * @param genericType The actual type that contains the generics, if any is present
     * @param array Determines whether the type declaration represents an array
     */
    TypeDeclaration(ITypeRepository typeRepository,
            IType type,
            Object genericType,
            boolean array) {
        this.type = type;
        this.array = array;
        this.genericType = genericType;
        this.typeRepository = typeRepository;
    }
    
    TypeDeclaration(IType type, ITypeDeclaration[] genericTypes, int dimensionality){
        this.type = type;
        this.genericTypes = genericTypes;
        this.dimensionality = dimensionality;
        this.array = dimensionality>0;
    }

    private String buildArrayTypeName(String arrayTypeName) {

        StringBuilder sb = new StringBuilder();
        int index = arrayTypeName.indexOf('[');
        int dimens = (arrayTypeName.length() - index) / 2;
        String typeName = arrayTypeName.substring(0, index);

        while (--dimens >= 0) {
            sb.append("[");
        }

        String elementType = elementType(typeName);

        sb.append(elementType);
        sb.append(typeName);

        if (elementType.equals("L")) {
            sb.append(";");
        }

        return sb.toString();
    }

    private ITypeDeclaration[] buildParameterTypes() {

        List<ITypeDeclaration> parameterTypes = new ArrayList<>();

        // Example: Class<T>
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            for (java.lang.reflect.Type tp : parameterizedType.getActualTypeArguments()) {
                ITypeDeclaration typeParameter = buildTypeDeclaration(tp);
                parameterTypes.add(typeParameter);
            }
        } // T[]
        else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            parameterTypes.add(buildTypeDeclaration(genericArrayType.getGenericComponentType()));
        } // Example: Class
        else if (genericType.getClass() == Class.class) {
            ITypeDeclaration typeParameter = new TypeDeclaration(typeRepository, typeRepository.getType((Class<?>)genericType), null, ((Class<?>)genericType).isArray());
            parameterTypes.add(typeParameter);
        } // Example: <K, V>
        else if (genericType.getClass() == Class[].class) {
            for (Class<?> javaType : ((Class<?>[]) genericType)) {
                ITypeDeclaration typeParameter = new TypeDeclaration(typeRepository, typeRepository.getType(javaType), null, javaType.isArray());
                parameterTypes.add(typeParameter);
            }
        } // Example: <K, V>
        else if (genericType.getClass() == IType[].class) {
            for (IType tp : ((IType[]) genericType)) {
                ITypeDeclaration typeParameter = new TypeDeclaration(typeRepository, tp, null, false);
                parameterTypes.add(typeParameter);
            }
        }

        return parameterTypes.toArray(new ITypeDeclaration[0]);
    }

    private TypeDeclaration buildTypeDeclaration(Object genericType) {

        // <T1, ..., Tn>
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return buildTypeDeclaration(parameterizedType.getRawType());
        }

        // <T>
        if (genericType instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) genericType;
            for (java.lang.reflect.Type tp : typeVariable.getBounds()) {
                return buildTypeDeclaration(tp);
            }
            return new TypeDeclaration(typeRepository, typeRepository.getType((Object.class)), null, (Object.class).isArray());
        }

        // ?
        if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) genericType;
            for (java.lang.reflect.Type tp : wildcardType.getUpperBounds()) {
                return buildTypeDeclaration(tp);
            }
            return new TypeDeclaration(typeRepository, typeRepository.getType((Object.class)), null, (Object.class).isArray());
        }

        // T[]
        if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            String arrayTypeName = buildArrayTypeName(genericArrayType.toString());
            IType arrayType = typeRepository.getType(arrayTypeName);

            return new TypeDeclaration(
                    typeRepository,
                    arrayType,
                    genericArrayType.getGenericComponentType(),
                    true);
        }

        return new TypeDeclaration(typeRepository, typeRepository.getType(((Class<?>) genericType)), null, ((Class<?>) genericType).isArray());
    }

    private String elementType(String typeName) {

        if (typeName.equals("boolean")) {//NOI18N
            return "Z";
        }
        if (typeName.equals("byte") || typeName.equals("char") || typeName.equals("double") || typeName.equals("float") || typeName.equals("int") || typeName.equals("short")) {//NOI18N
            return (Character.toString(typeName.charAt(0))).toUpperCase();//just first char in upper case
        }
        if (typeName.equals("long")) {//NOI18N
            return "J";
        }

        return "L";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimensionality() {
        if(dimensionality <0){
            if (array) {
                String name = type.getName();
                dimensionality = 0;
                while (name.charAt(dimensionality) == '[') {
                    dimensionality++;
                }
            }
        }
        return dimensionality;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IType getType() {
        return type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeDeclaration[] getTypeParameters() {
        if (genericTypes == null) {
            if (genericType == null) {
                genericTypes = new ITypeDeclaration[0];
            } else {
                genericTypes = buildParameterTypes();
            }
        }
        return genericTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArray() {
        return array;
    }
}
