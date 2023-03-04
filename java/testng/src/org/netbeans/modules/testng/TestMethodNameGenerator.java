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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.WorkingCopy;
import static java.lang.Boolean.TRUE;

/**
 * Utility class for generating a collection of unique names of test methods.
 *
 * @author  Marian Petras
 */
final class TestMethodNameGenerator {

    /** maximum number of parameter-type method name suffixes */
    private static final int MAX_SUFFIX_TYPES = 2;

    /**
     * collection of reserved type identifiers that should be left unchanged.
     * A method name may be reserved because it was assigned to some method
     * (value {@code Boolean.TRUE}) or because it was inherited from some parent
     * class (value {@code Boolean.FALSE}).
     */
    private Collection<String> reservedNames;

    private final WorkingCopy             workingCopy;
    private final List<ExecutableElement> srcMethods;
    private final TypeElement             tstClassElem;
    private final List<ExecutableElement> existingMethods;

    private final String[] testMethodNames;

    private TestMethodNameGenerator(final List<ExecutableElement> srcMethods,
                                    final TypeElement tstClassElem,
                                    WorkingCopy workingCopy) {
        this.srcMethods = srcMethods;
        this.tstClassElem = tstClassElem;
        this.workingCopy = workingCopy;

        existingMethods = (tstClassElem != null)
                          ? getExistingMethods(tstClassElem)
                          : Collections.<ExecutableElement>emptyList();
        reservedNames = new HashSet<String>((existingMethods.size() * 3 + 1) / 2);

        testMethodNames = new String[srcMethods.size()];
    }

    /**
     * Generates a list of unique names of test methods for the given source
     * methods. The names are generated such that they do not conflict
     * with names of methods that are already present in the test class
     * 
     * @param srcMethods  source methods for which test methods are about
     *                    to be created - names of these test methods will
     *                    be generated
     * @param tstClassElem  test class - the existing test class in which
     *                      new test classes are about to be generated,
     *                      or {@code null} if the test class does not exist yet
     * @param reservedMethodNames  list of reserved test method names
     *                             - these should not be avoided
     *                             by the test method name generator;
     *                             it may be {@code null} if there are no
     *                             reserved method names
     * @return  list of names for test methods for the given source methods;
     *          the names in the list are unique, they do not conflict
     *          with names of existing methods in the given test class (if any)
     *          and they are stored in the order of the source methods
     */
    static List<String> getTestMethodNames(
                                    final List<ExecutableElement> srcMethods,
                                    final TypeElement tstClassElem,
                                    final Collection<String> reservedMethodNames,
                                    final WorkingCopy workingCopy) {
        TestMethodNameGenerator inst
                = new TestMethodNameGenerator(srcMethods, tstClassElem, workingCopy);
        if (reservedMethodNames != null) {
            inst.reservedNames.addAll(reservedMethodNames);
        }
        return inst.getTestMethodNames();
    }

    /**
     * Determines names for test methods that are about to be generated.
     * 
     * @return  list of test method names, in the order corresponding to the
     *          order of {@linkplain #srcMethods source methods}
     */
    private List<String> getTestMethodNames() {
        if (tstClassElem != null) {
            collectExistingMethodNames(tstClassElem, reservedNames);
        }

        final int methodsCount = srcMethods.size();
        final String[] result = new String[methodsCount];

        final Map<String, Object> namesUsage
                = new HashMap<String, Object>(methodsCount * 3 + 1 / 2);
        final BitSet conflicting = new BitSet(methodsCount);
        int conflictingCount = 0;

        /*
         * Identify methods with overloaded names:
         */
        int index = -1;
        assert namesUsage.isEmpty();
        for (ExecutableElement srcMethod : srcMethods) {

            index++;

            String srcMethodName = srcMethod.getSimpleName().toString();
            String testMethodName = buildTestMethodName(srcMethodName);

            testMethodNames[index] = testMethodName;

            conflictingCount += registerTestMethodName(testMethodName,
                                                       index,
                                                       namesUsage,
                                                       conflicting);
        }
        namesUsage.clear();
        
        assert conflictingCount <= methodsCount;
        assert conflictingCount == conflicting.cardinality();

        int uniqueCount = methodsCount - conflictingCount;
        if (uniqueCount > 0) {

            /* fixate all unique method names... */

            for (index = conflicting.nextClearBit(0);   //for all unique...
                    (index >= 0) && (index < methodsCount);
                    index = conflicting.nextClearBit(index + 1)) {
                String name = testMethodNames[index];
                result[index] = name;
                reservedNames.add(name);    //fixate
            }
            
        }

        /* ... try to resolve the conflicting ones... */

        int[] paramsCount = null;
        Collection<TypeMirror> paramTypes = null;

        if (conflictingCount > 0) {

            /* will hold number of parameters of each source method */
            paramsCount = new int[srcMethods.size()];
            paramTypes = collectParamTypes(paramsCount);

            /* ROUND #2 - check conflicts of test methods of no-arg source methods */

            BitSet tested = findNoArgMethods(paramsCount, conflicting);
            BitSet noArgConflicting = new BitSet(srcMethods.size());

            final int testedCount = tested.cardinality();
            
            assert namesUsage.isEmpty();
            int noArgConflictingCount = 0;

            for (index = tested.nextSetBit(0);
                    index >= 0;
                    index = tested.nextSetBit(index + 1)) {

                noArgConflictingCount += registerTestMethodName(
                                                    testMethodNames[index],
                                                    index,
                                                    namesUsage,
                                                    noArgConflicting);
            }
            namesUsage.clear();

            assert noArgConflictingCount <= testedCount;
            assert noArgConflictingCount == noArgConflicting.cardinality();

            int noArgUniqueCount = methodsCount - conflictingCount;
            if (noArgUniqueCount > 0) {     /* among those for no-arg methods */

                /* fixate all unique names of test methods for no-arg source method: */

                BitSet noArgUnique = new BitSet(tested.size());
                noArgUnique.or(tested);
                noArgUnique.andNot(noArgConflicting);

                for (index = noArgUnique.nextSetBit(0);
                        index >= 0;
                        index = noArgUnique.nextSetBit(index + 1)) {
                    String name = testMethodNames[index];
                    result[index] = name;
                    reservedNames.add(name);   //fixate
                }
            }
            if (noArgConflictingCount > 0) {/* among those for no-arg methods */

                /* resolve conflicting names of test methods for no-arg source methods: */

                Map<String, Integer> usageNumbers
                        = new HashMap<String, Integer>((noArgConflictingCount + 1) * 3 / 2);

                noArgConflicting = tested;

                for (index = noArgConflicting.nextSetBit(0);
                        index >= 0;
                        index = noArgConflicting.nextSetBit(index + 1)) {

                    String simpleName = testMethodNames[index];
                    Integer oldValue = usageNumbers.get(simpleName);
                    int suffix = (oldValue == null)
                                 ? 0
                                 : oldValue.intValue();
                    String numberedName;
                    do {
                        suffix++;
                        numberedName = simpleName + suffix;
                    } while (reservedNames.contains(numberedName));
                    usageNumbers.put(simpleName, Integer.valueOf(suffix));

                    /* fixate immediately to ensure thare are really no conflicts */
                    result[index] = numberedName;
                    reservedNames.add(numberedName);    //fixate
                }

            }

            /*
             * OK, now we know that names of test methods for no-arg source
             * methods are resolved.
             */
            conflicting.andNot(noArgConflicting);
            conflictingCount -= noArgConflictingCount;
            uniqueCount += noArgConflictingCount;
            assert conflictingCount + uniqueCount == methodsCount;
        }

        String[] typeIdSuffixes = null;
        String[] parCntSuffixes = null;

        if (conflictingCount > 0) {

            /*
             * ROUND #3 - try to distinguish test method names by appending
             * identifiers of the source method parameter types or, if there
             * are too many parameters, by appending the number of parameters
             */

            BitSet tested = (BitSet) conflicting.clone();
            int testedCount = conflictingCount;
            conflicting.clear();
            conflictingCount = 0;

            assert paramsCount != null;
            assert paramTypes != null;

            /* only needed for methods with low number of arguments */
            TypeNameIdGenerator typeIdGenerator = null;

            if (!paramTypes.isEmpty()) {
                typeIdGenerator = TypeNameIdGenerator.createFor(
                                                        paramTypes,
                                                        workingCopy.getElements(),
                                                        workingCopy.getTypes());
            }

            assert namesUsage.isEmpty();
            String[] methodNames = new String[methodsCount];
            typeIdSuffixes = new String[methodsCount];
            parCntSuffixes = new String[methodsCount];
            for (index = tested.nextSetBit(0);
                    index >= 0;
                    index = tested.nextSetBit(index + 1)) {

                int parCount = paramsCount[index];
                String suffix;
                if (parCount > MAX_SUFFIX_TYPES) {
                    suffix = parCntSuffixes[index] = makeParamCountSuffix(parCount);
                } else {
                    List<? extends VariableElement> params
                            = srcMethods.get(index).getParameters();
                    StringBuilder buf = new StringBuilder(40);
                    for (int i = 0; i < parCount; i++) {
                        buf.append('_');
                        buf.append(typeIdGenerator.getParamTypeId(params.get(i).asType()));
                    }
                    suffix = typeIdSuffixes[index] = buf.toString();
                }

                String methodName = methodNames[index] = testMethodNames[index] + suffix;

                /* check whether it is duplicite: */
                conflictingCount += registerTestMethodName(
                                                    methodName,
                                                    index,
                                                    namesUsage,
                                                    conflicting);
            }
            namesUsage.clear();

            uniqueCount = testedCount - conflictingCount;

            if (uniqueCount > 0) {

                /* fixate all new unique names */

                BitSet unique = (BitSet) tested.clone();
                unique.andNot(conflicting);
                assert unique.cardinality() == uniqueCount;

                for (index = unique.nextSetBit(0);
                        index >= 0;
                        index = unique.nextSetBit(index + 1)) {
                    String methodName = methodNames[index];
                    result[index] = methodName;
                    reservedNames.add(methodName);      //fixate
                }
            }
        }

        if (conflictingCount > 0) {

            /*
             * ROUND #4 - try to distinguish test method names by appending
             * identifiers of the source method parameter types and their types,
             * or, if there are too many parameters, by only appending
             * the number of parameters
             */

            assert typeIdSuffixes != null;
            assert parCntSuffixes != null;

            BitSet tested = (BitSet) conflicting.clone();
            int testedCount = conflictingCount;
            conflicting.clear();
            conflictingCount = 0;

            assert namesUsage.isEmpty();
            String[] methodNames = new String[methodsCount];
            for (index = tested.nextSetBit(0);
                    index >= 0;
                    index = tested.nextSetBit(index + 1)) {

                int parCount = paramsCount[index];

                StringBuilder buf = new StringBuilder(60);
                buf.append(testMethodNames[index]);
                if (parCount <= MAX_SUFFIX_TYPES) {
                    assert typeIdSuffixes[index] != null;
                    buf.append(typeIdSuffixes[index]);
                }
                String parCntSuffix = parCntSuffixes[index];
                if (parCntSuffix == null) {
                    assert (parCount <= MAX_SUFFIX_TYPES);
                    parCntSuffix = parCntSuffixes[index] = makeParamCountSuffix(parCount);
                }
                buf.append(parCntSuffix);

                String methodName = methodNames[index] = buf.toString();

                /* check whether it is duplicite: */
                conflictingCount += registerTestMethodName(
                                                    methodName,
                                                    index,
                                                    namesUsage,
                                                    conflicting);
            }
            namesUsage.clear();

            uniqueCount = testedCount - conflictingCount;

            if (uniqueCount > 0) {

                /* fixate all new unique names */

                BitSet unique = (BitSet) tested.clone();
                unique.andNot(conflicting);
                assert unique.cardinality() == uniqueCount;

                for (index = unique.nextSetBit(0);
                        index >= 0;
                        index = unique.nextSetBit(index + 1)) {
                    String methodName = methodNames[index];
                    result[index] = methodName;
                    reservedNames.add(methodName);      //fixate
                }
            }
        }

        if (conflictingCount > 0) {


            /* ROUND #5 - append number of parameters + sequential number */

            Map<String, Integer> usageNumbers
                    = new HashMap<String, Integer>((conflictingCount * 3 + 1) / 2);

            assert parCntSuffixes != null;

            for (index = conflicting.nextSetBit(0);
                    index >= 0;
                    index = conflicting.nextSetBit(index + 1)) {

                String noNumMethodName = testMethodNames[index] + parCntSuffixes[index] + '_';
                Integer oldValue = usageNumbers.get(noNumMethodName);
                int suffix = (oldValue == null)
                             ? 0
                             : oldValue.intValue();
                String methodName;
                do {
                    suffix++;
                    methodName = noNumMethodName + suffix;
                } while (reservedNames.contains(methodName));
                usageNumbers.put(methodName, Integer.valueOf(suffix));

                /* fixate immediately to ensure thare are really no conflicts */
                result[index] = methodName;
                reservedNames.add(methodName);      //fixate
            }
        }

        return Arrays.asList(result);
    }

    /**
     */
    private static final String makeParamCountSuffix(int paramCount) {
        return new StringBuilder(8)
               .append('_')
               .append(paramCount)
               .append("args")                                          //NOI18N
               .toString();
    }

    /**
     * 
     */
    private int registerTestMethodName(String testMethodName,
                                       int index,
                                       Map<String, Object>namesUsage,
                                       BitSet conflictingNamesIndices) {
        Object oldValue = namesUsage.put(testMethodName, Integer.valueOf(index));
        boolean nameConflict = (oldValue != null)
                               || (reservedNames != null)
                                  && (reservedNames.contains(testMethodName));

        assert !conflictingNamesIndices.get(index);

        int rv = 0;
        if (nameConflict) {
            if ((oldValue != null) && (oldValue != TRUE)) {
                /*
                 * (oldValue == Integer) ... conflict with another method name
                 *                           detected
                 * (oldValue == null) ...... conflict with a reserved method
                 *                           name detected
                 * (oldValue == TRUE) ...... name has been already known to be
                 *                           in conflict with some other name
                 */
                assert (oldValue.getClass() == Integer.class);
                int conflictingNameIndex = ((Integer) oldValue).intValue();
                assert !conflictingNamesIndices.get(conflictingNameIndex);
                conflictingNamesIndices.set(conflictingNameIndex);
                rv++;
            }
            conflictingNamesIndices.set(index);
            namesUsage.put(testMethodName, TRUE);
            rv++;
        }
        return rv;
    }

    /**
     * Collects names of accessible no-argument methods that are present
     * in the given class and its superclasses. Methods inherited from the
     * class's superclasses are taken into account, too.
     * 
     * @param  clazz  class whose methods' names should be collected
     * @param  reservedMethodNames  collection to which the method names
     *                              should be added
     */
    private void collectExistingMethodNames(TypeElement clazz,
                                            Collection<String> reservedMethodNames) {
        final Elements elements = workingCopy.getElements();
        List<? extends Element> allMembers = elements.getAllMembers(clazz);
        List<? extends ExecutableElement> methods = ElementFilter.methodsIn(allMembers);
        if (!methods.isEmpty()) {
            for (ExecutableElement method : methods) {
                if (method.getParameters().isEmpty()) {
                    reservedMethodNames.add(method.getSimpleName().toString());
                }
            }
        }
    }

    /**
     * Collects types of parameters used by source methods.
     * Methods without parameters and methods having more than
     * {@value #MAX_SUFFIX_TYPES} parameters are skipped.
     * This method also stores number of parameters of each method to the given
     * array.
     * 
     * @param  paramCount  an empty array of numbers - its length must be equal
     *                     to the number of {@link #srcMethods}
     * @return  types of parameters of methods having an overloaded name
     */
    private Collection<TypeMirror> collectParamTypes(int[] paramsCount) {
        Collection<TypeMirror> paramTypes = new ArrayList<TypeMirror>(
                                                             srcMethods.size());
        int index = -1;
        for (ExecutableElement srcMethod : srcMethods) {

            index++;

            List<? extends VariableElement> params = srcMethod.getParameters();
            if (params.isEmpty()) {
                paramsCount[index] = 0;
                continue;
            }

            final int parCount;
            paramsCount[index] = (parCount = params.size());

            if (parCount <= MAX_SUFFIX_TYPES) {
                for (int i = 0; i < parCount; i++) {
                    paramTypes.add(params.get(i).asType());
                }
            }
        }
        return !paramTypes.isEmpty() ? paramTypes
                                     : Collections.<TypeMirror>emptyList();
    }

    /**
     * Returns a list of methods contained directly in the given class.
     * 
     * @param  classElem  class whose methods should be returned
     * @return  list of methods in the given class
     */
    private static List<ExecutableElement> getExistingMethods(
                                    final TypeElement classElem) {
        List<? extends Element> elements = classElem.getEnclosedElements();
        if (elements.isEmpty()) {
            return Collections.<ExecutableElement>emptyList();
        }

        List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
        return !methods.isEmpty() ? methods
                                  : Collections.<ExecutableElement>emptyList();
    }

    private static String buildTestMethodName(String srcMethodName) {
        int length = srcMethodName.length();
        StringBuilder buf = new StringBuilder(length + 4);
        buf.append("test");                                             //NOI18N
        buf.append(Character.toUpperCase(srcMethodName.charAt(0)));
        if (length != 1) {
            buf.append(srcMethodName.substring(1));
        }
        return buf.toString();
    }

    /**
     * Finds indices of test methods which have conflicting names and test
     * no-argument source methods.
     * 
     * @param  paramsCount  array containing number of parameters of each source
     *                      method
     * @param  conflicting  bitmap - set bits determine indices of test methods
     *                      having conflicting names
     * @return  bitmap which holds information which of these conflicting test
     *          methods is a test for a no-argument source method
     */
    private static BitSet findNoArgMethods(int[] paramsCount, BitSet conflicting) {
        BitSet result = new BitSet(paramsCount.length);
        for (int index = 0; index < paramsCount.length; index++) {
            if ((paramsCount[index] == 0) && conflicting.get(index)) {
                result.set(index);
            }
        }
        return result;
    }

}
