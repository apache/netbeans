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

package org.netbeans.modules.junit;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 * Data structure that holds overview of class' members and their positions
 * within the class.
 * <p>To get an instance for a class, use static method
 * {@link #forClass}.</p>
 *
 * @author  Marian Petras
 */
final class ClassMap {
    
    private static final int SETUP_POS_INDEX = 0;
    private static final int TEARDOWN_POS_INDEX = 1;
    private static final int FIRST_METHOD_POS_INDEX = 2;
    private static final int LAST_INIT_POS_INDEX = 3;
    private static final int FIRST_NESTED_POS_INDEX = 4;
    private static final int BEFORE_POS_INDEX = 5;
    private static final int AFTER_POS_INDEX = 6;
    private static final int BEFORE_CLASS_POS_INDEX = 7;
    private static final int AFTER_CLASS_POS_INDEX = 8;
    
    private static final String JUNIT4_PKG_PREFIX = "org.junit.";                   //NOI18N
    private static final String JUNIT5_PKG_PREFIX = "org.junit.jupiter.api.";       //NOI18N

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    /**
     */
    private final List<String> signatures;
    /** */
    private final int[] positions;
    
    /** Creates a new instance of ClassMap */
    private ClassMap(List<String> signatures) {
        this.signatures = signatures;
    }
    
    {
        positions = new int[9];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = -1;
        }
    }
    
    /**
     * 
     * @exception  java.lang.IllegalArgumentException
     *             if any of the arguments is {@code null}
     *             or if the specified {@code ClassTree} is not a leaf
     *             of the specified {@code TreePath}
     */
    static ClassMap forClass(ClassTree cls, TreePath clsTreePath,
                             Trees trees) {
        if (cls == null) {
            throw new IllegalArgumentException("ClassTree: null");      //NOI18N
        }
        if (clsTreePath == null) {
            throw new IllegalArgumentException("TreePath: null");       //NOI18N
        }
        if (clsTreePath.getLeaf() != cls) {
            throw new IllegalArgumentException(
                  "given ClassTree is not leaf of the given TreePath"); //NOI18N
        }
        
        List<? extends Tree> members = cls.getMembers();
        if (members.isEmpty()) {
            return new ClassMap(new ArrayList<String>());
        }

        List<String> entries = new ArrayList<String>(members.size());
        ClassMap map = new ClassMap(entries);

        int index = 0;
        for (Tree member : members) {
            String signature;
            switch (member.getKind()) {
                case BLOCK:
                    signature = "* ";                                   //NOI18N
                    map.setLastInitializerIndex(index);
                    break;
                case VARIABLE:
                    signature = "- ";                                   //NOI18N
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    ClassTree clazz = (ClassTree) member;
                    signature = "[ " + clazz.getSimpleName();           //NOI18N
                    if (map.getFirstNestedClassIndex() == -1) {
                        map.setFirstNestedClassIndex(index);
                    }
                    break;
                case METHOD:
                    MethodTree method = (MethodTree) member;
                    boolean hasParams = !method.getParameters().isEmpty();
                    Name methodName = method.getName();
                    if (methodName.contentEquals("<init>")) {           //NOI18N
                        signature = hasParams ? "*+" : "* ";            //NOI18N
                        map.setLastInitializerIndex(index);
                    } else {
                        if (!hasParams) {
                            if ((map.getSetUpIndex() == -1)
                                    && methodName.contentEquals("setUp")) {     //NOI18N
                                map.setSetUpIndex(index);
                            }
                            if ((map.getTearDownIndex() == -1)
                                    && methodName.contentEquals("tearDown")) {  //NOI18N
                                map.setTearDownIndex(index);
                            }
                        }
                        signature = (hasParams ? "!+" : "! ")           //NOI18N
                                    + methodName.toString();
                        if (map.getFirstMethodIndex() == -1) {
                            map.setFirstMethodIndex(index);
                        }
                        
                        /* annotations: */
                        if (!method.getModifiers().getAnnotations().isEmpty()) {
                            TreePath methodTreePath = new TreePath(clsTreePath,
                                                                   method);
                            Element methodElement = trees.getElement(methodTreePath);
                            for (AnnotationMirror annMirror : methodElement.getAnnotationMirrors()) {
                                Element annElem = annMirror.getAnnotationType().asElement();
                                String fullName = ((TypeElement) annElem).getQualifiedName().toString();
                                if (fullName.startsWith(JUNIT5_PKG_PREFIX)) {
                                    String shortName = fullName.substring(JUNIT5_PKG_PREFIX.length());
                                    int posIndex;
                                    if (shortName.equals("BeforeEach")) {               //NOI18N
                                        posIndex = BEFORE_POS_INDEX;
                                    } else if (shortName.equals("AfterEach")) {         //NOI18N
                                        posIndex = AFTER_POS_INDEX;
                                    } else if (shortName.equals("BeforeAll")) {   //NOI18N
                                        posIndex = BEFORE_CLASS_POS_INDEX;
                                    } else if (shortName.equals("AfterAll")) {    //NOI18N
                                        posIndex = AFTER_CLASS_POS_INDEX;
                                    } else {
                                        continue;       //next annotation
                                    }

                                    if (map.positions[posIndex] == -1) {
                                        map.positions[posIndex] = index;
                                    }
                                } else if (fullName.startsWith(JUNIT4_PKG_PREFIX)) {
                                    String shortName = fullName.substring(JUNIT4_PKG_PREFIX.length());
                                    int posIndex;
                                    if (shortName.equals("Before")) {               //NOI18N
                                        posIndex = BEFORE_POS_INDEX;
                                    } else if (shortName.equals("After")) {         //NOI18N
                                        posIndex = AFTER_POS_INDEX;
                                    } else if (shortName.equals("BeforeClass")) {   //NOI18N
                                        posIndex = BEFORE_CLASS_POS_INDEX;
                                    } else if (shortName.equals("AfterClass")) {    //NOI18N
                                        posIndex = AFTER_CLASS_POS_INDEX;
                                    } else {
                                        continue;       //next annotation
                                    }
                                    
                                    if (map.positions[posIndex] == -1) {
                                        map.positions[posIndex] = index;
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    signature = "x ";                                   //NOI18N
            }
            entries.add(signature);
            index++;
        }
        
        return map;
    }
    
    /**
     */
    int getSetUpIndex() {
        return positions[SETUP_POS_INDEX];
    }
    
    /**
     */
    private void setSetUpIndex(int setUpIndex) {
        positions[SETUP_POS_INDEX] = setUpIndex;
    }
    
    /**
     */
    int getTearDownIndex() {
        return positions[TEARDOWN_POS_INDEX];
    }
    
    /**
     */
    private void setTearDownIndex(int tearDownIndex) {
        positions[TEARDOWN_POS_INDEX] = tearDownIndex;
    }
    
    /**
     * Returns an index of a method annotated with annotation
     * {@code org.junit.Before}. If there are more such methods
     * index of any of these methods may be returned.
     * 
     * @return  index of a {@code @Before}-annotated method,
     *          or {@code -1} if there is no such method
     */
    int getBeforeIndex() {
        return positions[BEFORE_POS_INDEX];
    }
    
    /**
     */
    private void setBeforeIndex(int index) {
        positions[BEFORE_POS_INDEX] = index;
    }
    
    /**
     * Returns an index of a method annotated with annotation
     * {@code org.junit.After}. If there are more such methods
     * index of any of these methods may be returned.
     * 
     * @return  index of a {@code @After}-annotated method,
     *          or {@code -1} if there is no such method
     */
    int getAfterIndex() {
        return positions[AFTER_POS_INDEX];
    }
    
    /**
     */
    private void setAfterIndex(int index) {
        positions[AFTER_POS_INDEX] = index;
    }
    
    /**
     * Returns an index of a method annotated with annotation
     * {@code org.junit.BeforeClass}. If there are more such methods
     * index of any of these methods may be returned.
     * 
     * @return  index of a {@code @BeforeClass}-annotated method,
     *          or {@code -1} if there is no such method
     */
    int getBeforeClassIndex() {
        return positions[BEFORE_CLASS_POS_INDEX];
    }
    
    /**
     */
    private void setBeforeClassIndex(int index) {
        positions[BEFORE_CLASS_POS_INDEX] = index;
    }
    
    /**
     * Returns an index of a method annotated with annotation
     * {@code org.junit.AfterClass}. If there are more such methods
     * index of any of these methods may be returned.
     * 
     * @return  index of a {@code @AfterClass}-annotated method,
     *          or {@code -1} if there is no such method
     */
    int getAfterClassIndex() {
        return positions[AFTER_CLASS_POS_INDEX];
    }
    
    /**
     */
    private void setAfterClassIndex(int index) {
        positions[AFTER_CLASS_POS_INDEX] = index;
    }
    
    /**
     */
    int getFirstMethodIndex() {
        return positions[FIRST_METHOD_POS_INDEX];
    }
    
    /**
     */
    private void setFirstMethodIndex(int firstMethodIndex) {
        positions[FIRST_METHOD_POS_INDEX] = firstMethodIndex;
    }
    
    /**
     */
    int getFirstNestedClassIndex() {
        return positions[FIRST_NESTED_POS_INDEX];
    }
    
    /**
     */
    private void setFirstNestedClassIndex(int firstNestedClassIndex) {
        positions[FIRST_NESTED_POS_INDEX] = firstNestedClassIndex;
    }
    
    /**
     */
    int getLastInitializerIndex() {
        return positions[LAST_INIT_POS_INDEX];
    }
    
    /**
     */
    private void setLastInitializerIndex(int lastInitializerIndex) {
        positions[LAST_INIT_POS_INDEX] = lastInitializerIndex;
    }
    
    /**
     */
    boolean containsSetUp() {
        return getSetUpIndex() != -1;
    }
    
    /**
     */
    boolean containsTearDown() {
        return getTearDownIndex() != -1;
    }
    
    /**
     */
    boolean containsBefore() {
        return getBeforeIndex() != -1;
    }
    
    /**
     */
    boolean containsAfter() {
        return getAfterIndex() != -1;
    }
    
    /**
     */
    boolean containsBeforeClass() {
        return getBeforeClassIndex() != -1;
    }
    
    /**
     */
    boolean containsAfterClass() {
        return getAfterClassIndex() != -1;
    }
    
    /**
     */
    boolean containsNoArgMethod(String name) {
        return findNoArgMethod(name) != -1;
    }
    
    /**
     */
    boolean containsMethods() {
        return getFirstMethodIndex() != -1;
    }
    
    /**
     */
    boolean containsInitializers() {
        return getLastInitializerIndex() != -1;
    }
    
    /**
     */
    boolean containsNestedClasses() {
        return getFirstNestedClassIndex() != -1;
    }
    
    /**
     */
    int findNoArgMethod(String name) {
        if (!containsMethods()) {
            return -1;
        }
        if (name.equals("setUp")) {                                     //NOI18N
            return getSetUpIndex();
        }
        if (name.equals("tearDown")) {                                  //NOI18N
            return getTearDownIndex();
        }
        
        return signatures.indexOf("! " + name);                         //NOI18N
    }
    
    /**
     */
    void addNoArgMethod(String name) {
        addNoArgMethod(name, size());
    }
    
    /**
     */
    void addNoArgMethod(String name, int index) {
        int currSize = size();
        
        if (index > currSize) {
            throw new IndexOutOfBoundsException("index: " + index       //NOI18N
                                               + ", size: " + currSize);//NOI18N
        }

        String signature = "! " + name;                                 //NOI18N
        if (index != currSize) {
            signatures.add(index, signature);
            shiftPositions(index, 1);
        } else {
            signatures.add(signature);                                  //NOI18N
        }
        
        if (name.equals("setUp")) {                                     //NOI18N
            setSetUpIndex(index);
        } else if (name.equals("tearDown")) {                           //NOI18N
            setTearDownIndex(index);
        }
        if (getFirstMethodIndex() == -1) {
            setFirstMethodIndex(index);
        }
    }

    /**
     */
    void addNoArgMethod(String name, String annotationName) {
        addNoArgMethod(name, annotationName, size());
    }
    
    /**
     */
    void addNoArgMethod(String name, String annotationName, int index) {
        addNoArgMethod(name, index);
        
        if (annotationName.equals(JUnit4TestGenerator.ANN_BEFORE)) {
            setBeforeIndex(index);
        } else if (annotationName.equals(JUnit4TestGenerator.ANN_AFTER)) {
            setAfterIndex(index);
        } else if (annotationName.equals(JUnit4TestGenerator.ANN_BEFORE_CLASS)) {
            setBeforeClassIndex(index);
        } else if (annotationName.equals(JUnit4TestGenerator.ANN_AFTER_CLASS)) {
            setAfterClassIndex(index);
        } 
    }
    
    /**
     */
    void removeNoArgMethod(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("negative index ("      //NOI18N
                                                + index + ')');
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("index: " + index       //NOI18N
                                               + ", size: " + size());  //NOI18N
        }
        
        String signature = signatures.get(index);
        
        if (!signature.startsWith("! ")) {                              //NOI18N
            throw new IllegalArgumentException(
                    "not a no-arg method at the given index ("          //NOI18N
                    + index + ')');
        }
        
        if (index == getSetUpIndex()) {
            setSetUpIndex(-1);
        } else if (index == getTearDownIndex()) {
            setTearDownIndex(-1);
        }
        if (index == getFirstMethodIndex()) {
            int currSize = size();
            if (index == (currSize - 1)) {
                setFirstMethodIndex(-1);
            } else {
                int newFirstMethodIndex = -1;
                int memberIndex = index + 1;
                for (String sign : signatures.subList(index + 1, currSize)) {
                    if (sign.startsWith("! ")) {
                        newFirstMethodIndex = memberIndex;
                        break;
                    }
                    memberIndex++;
                }
                setFirstMethodIndex(newFirstMethodIndex);
            }
        }
        shiftPositions(index + 1, -1);
    }
    
    /**
     * Returns names of all no-argument methods.
     * 
     * @return  list of names of no-argument methods
     *          in the order of the methods in the source code
     */
    List<String> getNoArgMethods() {
        if (!containsMethods()) {
            return Collections.<String>emptyList();
        }

        List<String> result = new ArrayList<String>(signatures.size());
        for (String signature : signatures) {
            if (signature.startsWith("! ")) {                           //NOI18N
                result.add(signature.substring(2));
            }
        }
        return result.isEmpty() ? Collections.<String>emptyList()
                                : result;
    }

    /**
     */
    void addNestedClass(String name) {
        int currSize = size();
        signatures.add("[ " + name);                                    //NOI18N

        if (getFirstNestedClassIndex() == -1) {
            setFirstNestedClassIndex(currSize);
        }
    }

    /**
     * Returns names of classes contained in the class corresponding to this
     * {@code ClassMap}. Both nested classes (static) and inner classes are
     * taken into account. Anonymous inner classes are ignored.
     *
     * @return  list of names of nested and inner classes in the order
     *          of the classes in the source code,
     */
    List<String> getNestedClasses() {
        int firstIndex = getFirstNestedClassIndex();
        if (firstIndex == -1) {
            return Collections.<String>emptyList();
        }

        assert signatures.get(firstIndex).charAt(0) == '[';

        String firstNestedClass = signatures.get(firstIndex).substring(2);

        final int size = size();
        if (firstIndex == (size - 1)) {
            return Collections.singletonList(firstNestedClass);
        }

        List<String> result = null;
        int startIndex = firstIndex + 1;
        Iterator<String> it = signatures.subList(startIndex, size).iterator();
        for (int index = startIndex; it.hasNext(); index++) {
            String signature = it.next();
            if (signature.charAt(0) == '[') {
                if (result == null) {
                    int initialCapacity = Math.min(4, size - index + 1);
                    result = new ArrayList<String>(initialCapacity);
                    result.add(firstNestedClass);
                }
                result.add(signature.substring(2));
            }
        }

        if (result == null) {
            result = Collections.singletonList(firstNestedClass);
        }
        return result;
    }

    /**
     * Returns indexes (positions) of classes contained in the class
     * corresponding to this {@code ClassMap}. Both nested classes (static)
     * and inner classes are taken into account. Anonymous inner classes are
     * ignored.
     *
     * @return  an array of indexes ({@code 0}-based) of all nested classes
     *          in the corresponding class, or an empty array if there are
     *          no nested classes
     */
    int[] getNestedClassIndexes() {
        int firstIndex = getFirstNestedClassIndex();
        if (firstIndex == -1) {
            return EMPTY_INT_ARRAY;
        }

        assert signatures.get(firstIndex).charAt(0) == '[';

        final int size = size();
        if (firstIndex == (size - 1)) {
            return new int[] {firstIndex};
        }

        int[] result = null;
        int count = 1;
        int startIndex = firstIndex + 1;
        Iterator<String> it = signatures.subList(startIndex, size).iterator();
        for (int index = startIndex; it.hasNext(); index++) {
            if (it.next().charAt(0) == '[') {
                if (result == null) {
                    result = new int[size - index + 1];
                }
                result[count++] = index;
            }
        }
        assert (count == 1) == (result == null);

        if (result == null) {
            result = new int[1];
        } else if (count < result.length) {
            int[] oldResult = result;
            result = new int[count];
            System.arraycopy(oldResult, 1, result, 1, count - 1);
        }
        result[0] = firstIndex;
        return result;
    }

    /**
     */
    int size() {
        return signatures.size();
    }
    
    /**
     */
    private void shiftPositions(int fromIndex,
                                int shiftSize) {
        for (int i = 0; i < positions.length; i++) {
            int pos = positions[i];
            if ((pos != -1) && (pos >= fromIndex)) {
                positions[i] = pos + shiftSize;
            }
        }
    }
    
}
