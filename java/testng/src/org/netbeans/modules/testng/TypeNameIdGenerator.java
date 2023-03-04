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

package org.netbeans.modules.testng;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import static java.lang.Boolean.TRUE;
import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;

/**
 * Resolves brief names of types such that each type is assigned a unique string
 * identifier of that type. The goal is to keep the identifier easy for
 * understanding, short but still unique.
 *
 * @author  Marian Petras
 */
final class TypeNameIdGenerator {

    private static final String ARRAY_SUFFIX = "Arr";                   //NOI18N

    private final Elements elements;
    private final Types types;
    /**
     * mapping between full type names and the corresponding shorter string
     * identifiers
     */
    private final Map<String, String> qualNameToId;


    /**
     * Creates an instance of this generator with an empty
     * {@linkplain mapping table}. It must be filled by method
     * {@link generateMapping}.
     * 
     * @param  types  an instance of a utility class {code Types}
     */
    private TypeNameIdGenerator(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;

        qualNameToId = new HashMap<String, String>(20);
    }

    /**
     * Generates an instance of this class which will be able to translate
     * between parameter types and the corresponding unique string identifiers.
     * @param  paramTypes  parameter types that the instance must be able
     *                    to translate
     * @param  types  an instance of a utility class {code Types}
     * @return  instance of generator that is ready to translate any of the
     *          given type to a unique string identifier
     * @see  #getParamTypeId
     */
    static TypeNameIdGenerator createFor(Collection<TypeMirror> paramTypes,
                                         Elements elements,
                                         Types types) {
        final TypeNameIdGenerator inst = new TypeNameIdGenerator(elements,
                                                                 types);
        inst.generateMapping(paramTypes);
        return inst;
    }

    /**
     * Generates mapping between full type names and their shorter (but still
     * unique) forms. The result is stored in field {@link #qualNameToId}
     * which is then used by method {@link #getParamTypeId}.
     * 
     * @param  paramTypes  parameter types for which a mapping should be
     *                     generated
     */
    private void generateMapping(final Collection<TypeMirror> paramTypes) {

        final int typesCount = paramTypes.size();
        int uniqueTypesCountExp = Math.max(5, typesCount / 3);
        List<String> fullTypeNames = new ArrayList<String>(uniqueTypesCountExp);
        List<String> briefTypeNames = new ArrayList<String>(uniqueTypesCountExp);

        Collection<String> processedTypes
                = new HashSet<String>((paramTypes.size() * 3 + 1) / 2);

        final Map<String, Object> briefTypeIdsUsage
                = new HashMap<String, Object>(typesCount * 3 / 2);
        final BitSet briefTypeIdsConflicting = new BitSet(typesCount);
        final BitSet briefTypeIdsRegistered = new BitSet(typesCount);
        int registeredTypeIdsCount = 0;
        int conflictingTypeIdsCount = 0;
        Collection<String> reservedTypeIds = new HashSet<String>(13);

        int index = 0;
        for (TypeMirror type : paramTypes) {

            final TypeKind kind = type.getKind();
            if ((kind != DECLARED) && (kind != ARRAY)) {
                continue;
            }

            String fullTypeName;
            String briefTypeName;
            if (kind == ARRAY) {
                int arrayDim = 0;
                StringBuilder fullTypeNameBuf = new StringBuilder(20);
                StringBuilder briefTypeNameBuf = new StringBuilder(26);
                TypeMirror compType = type;
                TypeKind compTypeKind;
                do {
                    arrayDim++;
                    fullTypeNameBuf.append('[');
                    briefTypeNameBuf.append(ARRAY_SUFFIX);
                    compType = ((ArrayType) compType).getComponentType();
                    compTypeKind = compType.getKind();
                } while (compTypeKind == ARRAY);

                if (compTypeKind == DECLARED) {
                    String compTypeFullName = getTypeFullName(compType);
                    fullTypeName = fullTypeNameBuf
                                   .append(compTypeFullName)
                                   .toString();
                    if (!processedTypes.add(fullTypeName)) {
                        /* this type has been already processed */
                        continue;
                    }
                    briefTypeName = briefTypeNameBuf
                                    .insert(0, getTypeBriefName(compTypeFullName))
                                    .toString();
                } else {
                    String typeId;
                    if (compTypeKind.isPrimitive()) {
                        typeId = getParamTypeId(type);
                    } else {
                        /*
                         * Might be getParamTypeId(type) as well
                         * - the result would be same. But this is faster.
                         */
                        typeId = getParamTypeId(compType);  //error, none, ...
                    }
                    conflictingTypeIdsCount += registerPrimitiveTypeIdUsage(
                                                       typeId,
                                                       briefTypeIdsUsage,
                                                       briefTypeIdsConflicting,
                                                       reservedTypeIds);
                    assert conflictingTypeIdsCount <= registeredTypeIdsCount;
                    continue;
                }
            } else {
                assert (kind == DECLARED);
                fullTypeName = getTypeFullName(type);
                if (!processedTypes.add(fullTypeName)) {
                    /* this type has been already processed */
                    continue;
                }
                briefTypeName = getTypeBriefName(fullTypeName);
            }

            fullTypeNames.add(fullTypeName);
            briefTypeNames.add(briefTypeName);

            briefTypeIdsRegistered.set(index);
            registeredTypeIdsCount++;
            conflictingTypeIdsCount += registerBriefTypeIdUsage(
                                                       briefTypeName,
                                                       index,
                                                       briefTypeIdsUsage,
                                                       briefTypeIdsConflicting,
                                                       reservedTypeIds);
            index++;
        }
        processedTypes.clear();
        processedTypes = null;

        assert conflictingTypeIdsCount <= registeredTypeIdsCount;
        assert conflictingTypeIdsCount == briefTypeIdsConflicting.cardinality();
        assert registeredTypeIdsCount == briefTypeIdsRegistered.cardinality();

        int uniqueTypeIdsCount = registeredTypeIdsCount - conflictingTypeIdsCount;
        if (uniqueTypeIdsCount > 0) {

            /* fixate all unique brief type Id's... */

            BitSet unique = (BitSet) briefTypeIdsRegistered.clone();
            unique.andNot(briefTypeIdsConflicting);

            for (index = unique.nextSetBit(0);   //for all unique...
                    index >= 0;
                    index = unique.nextSetBit(index + 1)) {
                String fullTypeName = fullTypeNames.get(index);
                String briefTypeName = briefTypeNames.get(index);
                reservedTypeIds.add(briefTypeName);             //fixate
                qualNameToId.put(fullTypeName, briefTypeName);  //add to result
            }
        }

        /* ... try to resolve the conflicting ones... */

        if (conflictingTypeIdsCount > 0) {

            /* ROUND #2 - try to use abbreviations of package names */

            BitSet conflicting = (BitSet) briefTypeIdsConflicting.clone();

            briefTypeIdsUsage.clear();
            briefTypeIdsRegistered.clear();
            briefTypeIdsConflicting.clear();
            registeredTypeIdsCount = 0;
            conflictingTypeIdsCount = 0;

            String[] longerTypeNames = new String[typesCount];

            for (index = conflicting.nextSetBit(0);
                    index >= 0;
                    index = conflicting.nextSetBit(index + 1)) {

                String fullTypeName = fullTypeNames.get(index);
                String briefTypeName = briefTypeNames.get(index);
                String longerTypeName = getLongerTypeId(fullTypeName, briefTypeName);

                longerTypeNames[index] = longerTypeName;

                briefTypeIdsRegistered.set(index);
                registeredTypeIdsCount++;
                conflictingTypeIdsCount += registerBriefTypeIdUsage(
                                                           longerTypeName,
                                                           index,
                                                           briefTypeIdsUsage,
                                                           briefTypeIdsConflicting,
                                                           reservedTypeIds);
            }

            assert conflictingTypeIdsCount <= registeredTypeIdsCount;
            assert conflictingTypeIdsCount == briefTypeIdsConflicting.cardinality();
            assert registeredTypeIdsCount == briefTypeIdsRegistered.cardinality();

            uniqueTypeIdsCount = registeredTypeIdsCount - conflictingTypeIdsCount;
            if (uniqueTypeIdsCount > 0) {

                /* fixate all unique longer type Id's... */

                BitSet unique = (BitSet) briefTypeIdsRegistered.clone();
                unique.andNot(briefTypeIdsConflicting);

                for (index = unique.nextSetBit(0);      //for all unique...
                        (index >= 0) && (index < typesCount);
                        index = unique.nextSetBit(index + 1)) {
                    String fullTypeName = fullTypeNames.get(index);
                    String longerTypeId = longerTypeNames[index];
                    reservedTypeIds.add(longerTypeId);           //fixate
                    qualNameToId.put(fullTypeName, longerTypeId);//add to result
                }
            }
        }

        /* ... try to resolve the remaining conflicts... */

        if (conflictingTypeIdsCount > 0) {

            /* ROUND #3 - use brief names + sequential number */

            Map<String, Integer> usageNumbers
                    = new HashMap<String, Integer>(conflictingTypeIdsCount * 3 / 2);

            BitSet conflicting = briefTypeIdsConflicting;

            for (index = conflicting.nextSetBit(0);
                    index >= 0;
                    index = conflicting.nextSetBit(index + 1)) {

                String briefTypeId = briefTypeNames.get(index);
                Integer oldValue = usageNumbers.get(briefTypeId);
                int suffix = (oldValue == null)
                             ? 0
                             : oldValue.intValue();
                String fullTypeName = fullTypeNames.get(index);
                String longestTypeId;
                do {
                    suffix++;
                    longestTypeId = briefTypeId + suffix;
                } while (reservedTypeIds.contains(longestTypeId));
                usageNumbers.put(briefTypeId, Integer.valueOf(suffix));

                /* fixate immediately to ensure thare are really no conflicts */
                reservedTypeIds.add(longestTypeId);             //fixate
                qualNameToId.put(fullTypeName, longestTypeId);  //add to result
            }
        }

        /* release data that is no longer necessary: */
        if (fullTypeNames != null) {
            fullTypeNames = null;
        }
        if (briefTypeNames != null) {
            briefTypeNames = null;
        }
        if (briefTypeIdsUsage != null) {
            briefTypeIdsUsage.clear();
        }
        if (briefTypeIdsRegistered != null) {
            briefTypeIdsRegistered.clear();
        }
        if (briefTypeIdsConflicting != null) {
            briefTypeIdsConflicting.clear();
        }
        if (reservedTypeIds != null) {
            reservedTypeIds.clear();
            reservedTypeIds = null;
        }
        if (paramTypes != null) {
            paramTypes.clear();
        }
    }

    /**
     * Registers usage of a primitive type or a primitive type array.
     * 
     * @param  typeId  typeId of the primitive type
     * @param  typeIdUsage  registry of usages of the given type - it is used
     *                      for detection of multiple usages of the same type Id
     * @param  conflictingTypesIndices  bitset to which detected conflicting
     *                                  types should be registered
     * @return  number of newly detected types having conflicting names
     *          ({@code 0} or {@code 1})
     */
    private int registerPrimitiveTypeIdUsage(String typeId,
                                             Map<String, Object> typeIdUsage,
                                             BitSet conflictingTypesIndices,
                                             Collection<String> reservedTypeIds) {
        if (!reservedTypeIds.add(typeId)) {  //this typeId is already registered
            return 0;
        }

        int rv = 0;

        /* check whether the type ID is overloaded: */
        Object oldValue = typeIdUsage.get(typeId);
        if ((oldValue != null) && (oldValue != TRUE)) {
            assert (oldValue.getClass() == Integer.class);
            int conflictingTypeIndex = ((Integer) oldValue).intValue();
            assert !conflictingTypesIndices.get(conflictingTypeIndex);
            conflictingTypesIndices.set(conflictingTypeIndex);
            typeIdUsage.put(typeId, TRUE);
            rv++;
        }

        return rv;
    }

    /**
     * Registers usage of a given type.
     * 
     * @param  briefTypeName  brief name of the type
     * @param  index  index of the type in the list of types
     * @param  typeIdUsage  registry of usages of the given type - it is used
     *                      for detection of multiple usages of the same type Id
     * @param  conflictingTypesIndices  bitset to which detected conflicting
     *                                  types should be registered
     * @return  number of newly detected types having conflicting names
     *          ({@code 0}, {@code 1} or {@code 2})
     */
    private int registerBriefTypeIdUsage(String typeId,
                                         int index,
                                         Map<String, Object> typeIdUsage,
                                         BitSet conflictingTypesIndices,
                                         Collection<String> reservedTypeIds) {
        Object oldValue = typeIdUsage.put(typeId, Integer.valueOf(index));
        boolean nameConflict = (oldValue != null)
                               || (reservedTypeIds != null)
                                  && (reservedTypeIds.contains(typeId));

        assert !conflictingTypesIndices.get(index);

        int rv = 0;
        if (nameConflict) {
            if ((oldValue != null) && (oldValue != TRUE)) {
                /*
                 * (oldValue == Integer) ... conflict with another brief Id
                 *                           detected
                 * (oldValue == null) ...... conflict with a reserved type Id
                 *                           detected
                 * (oldValue == TRUE) ...... name has been already known to be
                 *                           in conflict with some other type
                 */
                assert (oldValue.getClass() == Integer.class);
                int conflictingTypeIndex = ((Integer) oldValue).intValue();
                assert !conflictingTypesIndices.get(conflictingTypeIndex);
                conflictingTypesIndices.set(conflictingTypeIndex);
                rv++;
            }
            conflictingTypesIndices.set(index);
            typeIdUsage.put(typeId, TRUE);
            rv++;
        }
        return rv;
    }

    /**
     * Returns a short but unique parameter type identifier for the given type.
     * 
     * @param  type  type for which a unique id is requested
     * @return  unique type id for the given type
     */
    String getParamTypeId(TypeMirror type) {
        if (type == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        final TypeKind kind = type.getKind();
        if ((kind != DECLARED) && (kind != ARRAY)) {

            if (kind.isPrimitive()) {
                return type.toString();
            }

            switch (kind) {
                case ERROR:
                    return "ErrorType";                                 //NOI18N
                case NONE:
                    assert false;
                    return "NoType";                                    //NOI18N
                case VOID:
                    assert false;
                    return "VoidType";                                  //NOI18N
                case NULL:
                    assert false;
                    return "NullType";                                  //NOI18N
                case EXECUTABLE:
                case PACKAGE:
                case WILDCARD:
                    assert false;
                    return null;
                case TYPEVAR:
                    return "GenericType";                               //NOI18N
                default:    // including OTHER
                    return "UnknownType";                               //NOI18N
            }
        }

        String fullTypeName;
        if (kind == ARRAY) {
            int arrayDim = 0;
            StringBuilder fullTypeNameBuf = new StringBuilder(20);
            TypeMirror compType = type;
            TypeKind compTypeKind;
            do {
                arrayDim++;
                fullTypeNameBuf.append('[');
                compType = ((ArrayType) compType).getComponentType();
                compTypeKind = compType.getKind();
            } while (compTypeKind == ARRAY);

            if (compTypeKind == DECLARED) {
                fullTypeName = fullTypeNameBuf
                               .append(getTypeFullName(compType))
                               .toString();
            } else if (compTypeKind.isPrimitive()) {
                StringBuilder paramTypeIdBuf = new StringBuilder(17);
                paramTypeIdBuf.append(compType.toString());
                for (int i = 0; i < arrayDim; i++) {
                    paramTypeIdBuf.append(ARRAY_SUFFIX);
                }
                return paramTypeIdBuf.toString();
            } else {
                return getParamTypeId(compType);    //error, none, void, ...
            }
        } else {
            assert (kind == DECLARED);
            fullTypeName = getTypeFullName(type);
        }

        String id = qualNameToId.get(fullTypeName);
        if (id == null) {
            throw new IllegalArgumentException("unknown type");         //NOI18N
        }
        return id;
    }

    /**
     * Generates a string describing the given type. If the type is
     * parameterized, its erasure is used instead.
     * 
     * @param  type  type for which a string identifier is to be generated
     * @return  generated string identifier for the given type
     */
    private String getTypeFullName(TypeMirror type) {
        assert type.getKind() == DECLARED;
        DeclaredType typeErasure = (DeclaredType) types.erasure(type);
        TypeElement typeErasureElem = (TypeElement) typeErasure.asElement();
        return elements.getBinaryName(typeErasureElem).toString();
    }

    /**
     * Returns a brief version of a given type name.
     * 
     * @param  typeFullName  full type name
     * @return  brief version of the type, i.e. package name is stripped
     */
    private static String getTypeBriefName(String typeFullName) {
        int dotIndex = typeFullName.lastIndexOf('.');
        String result = (dotIndex == -1) ? typeFullName
                                         : typeFullName.substring(dotIndex + 1);
        int dollarIndex = result.lastIndexOf('$');
        if (dollarIndex != -1) {        //nested class
            StringBuilder buf = new StringBuilder(result);
            do {
                buf.deleteCharAt(dollarIndex);
                dollarIndex = result.lastIndexOf('$', dollarIndex - 1);
            } while (dollarIndex != -1);
            result = buf.toString();
        }
        return result;
    }

    /**
     * Returns a longer type Id of the given type.
     * The longer type Id is made of brief type Id by prepending an abbreviation
     * of the type's package name.
     * The package name abbreviation is a sequence of first characters of parts
     * of the package name. An abbreviation of a type that has an empty package
     * name (i.e. belonging to the default package), is an empty string.
     * <p>
     * Example:<br />
     * <code>getLongerTypeId(...)</code>
     * for type <code><u>j</u>ava.<u>l</u>ang.<u>String</u></code>
     * is <code>&quot;jlString&quot;</code>.
     * </p>
     * 
     * @param  typeFullName  full type Id of the type whose longer type Id
     *                       is to be returned
     * @param  briefTypeId  brief type Id of the type
     * @return  longer type Id of the given type;
     *          if the given type belongs to the default package
     *          (i.e. the package name is empty), the same (instance of) string
     *          is returned
     */
    private static String getLongerTypeId(String typeFullName,
                                          String briefTypeId) {
        if (typeFullName.charAt(0) == '[') {        //it's an array type
            int startIndex = 0;
            do {
                startIndex++;
            } while (typeFullName.charAt(startIndex) == '[');
            typeFullName = typeFullName.substring(startIndex);
        }

        int lastDot = typeFullName.lastIndexOf('.');
        if (lastDot == -1) {
            return briefTypeId;
        }

        String pkgName = typeFullName.substring(0, lastDot);
        StringBuilder buf = new StringBuilder(10);
        int nextDot = -1;
        do {
            int pkgPartStart = nextDot + 1;
            buf.append(pkgName.charAt(pkgPartStart));
            nextDot = pkgName.indexOf('.', pkgPartStart);
            assert (nextDot != pkgPartStart);
        } while (nextDot != -1);
        return buf.append(briefTypeId).toString();
    }

}
