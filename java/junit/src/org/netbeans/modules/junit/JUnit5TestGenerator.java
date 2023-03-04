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

import org.netbeans.modules.junit.api.JUnitVersion;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 *
 * @author  Marian Petras
 */
final class JUnit5TestGenerator extends AbstractTestGenerator {
    
    /** */
    static final String ANN_BEFORE_CLASS = "org.junit.jupiter.api.BeforeAll";           //NOI18N
    /** */
    static final String ANN_AFTER_CLASS = "org.junit.jupiter.api.AfterAll";             //NOI18N
    /** */
    static final String ANN_BEFORE = "org.junit.jupiter.api.BeforeEach";                //NOI18N
    /** */
    static final String ANN_AFTER = "org.junit.jupiter.api.AfterEach";                  //NOI18N
    /** */
    static final String ANN_TEST = "org.junit.jupiter.api.Test";                        //NOI18N
    /** */
    private static final String BEFORE_CLASS_METHOD_NAME = "setUpClass";//NOI18N
    /** */
    private static final String AFTER_CLASS_METHOD_NAME = "tearDownClass";//NOI18N
    /** */
    private static final String BEFORE_METHOD_NAME = "setUp";           //NOI18N
    /** */
    private static final String AFTER_METHOD_NAME = "tearDown";         //NOI18N
    
    /**
     */
    JUnit5TestGenerator(TestGeneratorSetup setup) {
        super(setup, JUnitVersion.JUNIT5);
    }
    
    /**
     */
    JUnit5TestGenerator(TestGeneratorSetup setup,
                        List<ElementHandle<TypeElement>> srcTopClassHandles,
                        List<String>suiteMembers,
                        boolean isNewTestClass) {
        super(setup, srcTopClassHandles, suiteMembers, isNewTestClass, JUnitVersion.JUNIT5);
    }
    
    
    /**
     */
    @Override
    protected ClassTree composeNewTestClass(WorkingCopy workingCopy,
                                            String name,
                                            List<? extends Tree> members) {
        final TreeMaker maker = workingCopy.getTreeMaker();
        ModifiersTree modifiers = maker.Modifiers(
                                      Collections.<Modifier>singleton(PUBLIC));
        return maker.Class(
                    modifiers,                                 //modifiers
                    name,                                      //name
                    Collections.<TypeParameterTree>emptyList(),//type params
                    null,                                      //extends
                    Collections.<ExpressionTree>emptyList(),   //implements
                    members);                                  //members
    }
    
    /**
     */
    @Override
    protected List<? extends Tree> generateInitMembers(WorkingCopy workingCopy) {
        if (!setup.isGenerateBefore() && !setup.isGenerateAfter()
                && !setup.isGenerateBeforeClass() && !setup.isGenerateAfterClass()) {
            return Collections.<Tree>emptyList();
        }

        List<MethodTree> result = new ArrayList<MethodTree>(4);
        if (setup.isGenerateBeforeClass()) {
            result.add(
                    generateInitMethod(BEFORE_CLASS_METHOD_NAME, ANN_BEFORE_CLASS, true, workingCopy));
        }
        if (setup.isGenerateAfterClass()) {
            result.add(
                    generateInitMethod(AFTER_CLASS_METHOD_NAME, ANN_AFTER_CLASS, true, workingCopy));
        }
        if (setup.isGenerateBefore()) {
            result.add(
                    generateInitMethod(BEFORE_METHOD_NAME, ANN_BEFORE, false, workingCopy));
        }
        if (setup.isGenerateAfter()) {
            result.add(
                    generateInitMethod(AFTER_METHOD_NAME, ANN_AFTER, false, workingCopy));
        }
        return result;
    }

    /**
     */
    @Override
    protected ClassTree generateMissingInitMembers(ClassTree tstClass,
                                                   TreePath tstClassTreePath,
                                                   WorkingCopy workingCopy) {
        if (!setup.isGenerateBefore() && !setup.isGenerateAfter()
                && !setup.isGenerateBeforeClass() && !setup.isGenerateAfterClass()) {
            return tstClass;
        }

        ClassMap classMap = ClassMap.forClass(tstClass, tstClassTreePath,
                                              workingCopy.getTrees());

        if ((!setup.isGenerateBefore() || classMap.containsBefore())
                && (!setup.isGenerateAfter() || classMap.containsAfter())
                && (!setup.isGenerateBeforeClass() || classMap.containsBeforeClass())
                && (!setup.isGenerateAfterClass() || classMap.containsAfterClass())) {
            return tstClass;
        }

        final TreeMaker maker = workingCopy.getTreeMaker();

        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);
        tstMembers.addAll(tstMembersOrig);

        generateMissingInitMembers(tstMembers, classMap, workingCopy);

        ClassTree newClass = maker.Class(
                tstClass.getModifiers(),
                tstClass.getSimpleName(),
                tstClass.getTypeParameters(),
                tstClass.getExtendsClause(),
                (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
                tstMembers);
        return newClass;
    }
    
    /**
     */
    @Override
    protected boolean generateMissingInitMembers(List<Tree> tstMembers,
                                               ClassMap clsMap,
                                               WorkingCopy workingCopy) {
        boolean modified = false;
        
        if (setup.isGenerateBeforeClass() && !clsMap.containsBeforeClass()) {
            int targetIndex;
            if (clsMap.containsAfterClass()) {
                targetIndex = clsMap.getAfterClassIndex();
            } else {
                int beforeIndex = clsMap.getBeforeIndex();
                int afterIndex = clsMap.getAfterIndex();
                if ((beforeIndex != -1) && (afterIndex != -1)) {
                    targetIndex = Math.min(beforeIndex, afterIndex);
                } else {
                    targetIndex = Math.max(beforeIndex, afterIndex);
                }
            }
            addInitMethod(BEFORE_CLASS_METHOD_NAME,
                          ANN_BEFORE_CLASS,
                          true,
                          targetIndex,
                          tstMembers,
                          clsMap,
                          workingCopy);
            modified = true;
        }
        if (setup.isGenerateAfterClass() && !clsMap.containsAfterClass()) {
            int targetIndex;
            if (clsMap.containsBeforeClass()) {
                targetIndex = clsMap.getBeforeClassIndex() + 1;
            } else {
                int beforeIndex = clsMap.getBeforeIndex();
                int afterIndex = clsMap.getAfterIndex();
                if ((beforeIndex != -1) && (afterIndex != -1)) {
                    targetIndex = Math.min(beforeIndex, afterIndex);
                } else {
                    targetIndex = Math.max(beforeIndex, afterIndex);
                }
            }
            addInitMethod(AFTER_CLASS_METHOD_NAME,
                          ANN_AFTER_CLASS,
                          true,
                          targetIndex,
                          tstMembers,
                          clsMap,
                          workingCopy);
            modified = true;
        }
        if (setup.isGenerateBefore() && !clsMap.containsBefore()) {
            int targetIndex;
            if (clsMap.containsAfter()) {
                targetIndex = clsMap.getAfterIndex();
            } else {
                int beforeClassIndex = clsMap.getBeforeClassIndex();
                int afterClassIndex = clsMap.getAfterClassIndex();
                
                targetIndex = Math.max(beforeClassIndex, afterClassIndex);
                if (targetIndex != -1) {
                    targetIndex++;
                }
            }
            addInitMethod(BEFORE_METHOD_NAME,
                          ANN_BEFORE,
                          false,
                          targetIndex,
                          tstMembers,
                          clsMap,
                          workingCopy);
            modified = true;
        }
        if (setup.isGenerateAfter() && !clsMap.containsAfter()) {
            int targetIndex;
            if (clsMap.containsBefore()) {
                targetIndex = clsMap.getBeforeIndex() + 1;
            } else {
                int beforeClassIndex = clsMap.getBeforeClassIndex();
                int afterClassIndex = clsMap.getAfterClassIndex();
                targetIndex = Math.max(beforeClassIndex, afterClassIndex);
                if (targetIndex != -1) {
                    targetIndex++;
                }
            }
            addInitMethod(AFTER_METHOD_NAME,
                          ANN_AFTER,
                          false,
                          targetIndex,
                          tstMembers,
                          clsMap,
                          workingCopy);
            modified = true;
        }
        
        return modified;
    }

    /**
     */
    private void addInitMethod(String methodName,
                               String annotationClassName,
                               boolean isStatic,
                               int targetIndex,
                               List<Tree> clsMembers,
                               ClassMap clsMap,
                               WorkingCopy workingCopy) {
        MethodTree initMethod = generateInitMethod(methodName,
                                                   annotationClassName,
                                                   isStatic,
                                                   workingCopy);
        
        if (targetIndex == -1) {
            targetIndex = getPlaceForFirstInitMethod(clsMap);
        }
        
        if (targetIndex != -1) {
            clsMembers.add(targetIndex, initMethod);
        } else {
            clsMembers.add(initMethod);
        }
        clsMap.addNoArgMethod(methodName, annotationClassName, targetIndex);
    }

    /**
     * Generates a set-up or a tear-down method.
     * The generated method will have no arguments, void return type
     * and a declaration that it may throw {@code java.lang.Exception}.
     * The method will have a declared protected member access.
     * The method contains call of the corresponding super method, i.e.
     * {@code super.setUp()} or {@code super.tearDown()}.
     *
     * @param  methodName  name of the method to be created
     * @return  created method
     * @see  http://junit.sourceforge.net/javadoc/junit/framework/TestCase.html
     *       methods {@code setUp()} and {@code tearDown()}
     */
    private MethodTree generateInitMethod(String methodName,
                                          String annotationClassName,
                                          boolean isStatic,
                                          WorkingCopy workingCopy) {
        Set<Modifier> methodModifiers
                = isStatic ? createModifierSet(PUBLIC, STATIC)
                           : Collections.<Modifier>singleton(PUBLIC);
        ModifiersTree modifiers = createModifiersTree(annotationClassName,
                                                      methodModifiers,
                                                      workingCopy);
        TreeMaker maker = workingCopy.getTreeMaker();
        BlockTree methodBody = maker.Block(
                Collections.<StatementTree>emptyList(),
                false);
        MethodTree method = maker.Method(
                modifiers,              // modifiers
                methodName,             // name
                maker.PrimitiveType(TypeKind.VOID),         // return type
                Collections.<TypeParameterTree>emptyList(), // type params
                Collections.<VariableTree>emptyList(),      // parameters
                Collections.<ExpressionTree>singletonList(
                        maker.Identifier("Exception")),     // throws...//NOI18N
                methodBody,
                null);                                      // default value
        return method;
    }
    
    /**
     */
    @Override
    protected void generateMissingPostInitMethods(TreePath tstClassTreePath,
                                                  List<Tree> tstMembers,
                                                  ClassMap clsMap,
                                                  WorkingCopy workingCopy) {
        /* no post-init methods */
    }
    
    /**
     */
    @Override
    protected MethodTree composeNewTestMethod(String testMethodName,
                                              BlockTree testMethodBody,
                                              List<ExpressionTree> throwsList,
                                              WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();
        return maker.Method(
                createModifiersTree(ANN_TEST,
                                    createModifierSet(PUBLIC),
                                    workingCopy),
                testMethodName,
                maker.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                throwsList,
                testMethodBody,
                null);          //default value - used by annotations
    }
    
    /**
     */
    @Override
    protected ClassTree finishSuiteClass(ClassTree tstClass,
                                         TreePath tstClassTreePath,
                                         List<Tree> tstMembers,
                                         List<String> suiteMembers,
                                         boolean membersChanged,
                                         ClassMap classMap,
                                         WorkingCopy workingCopy) {
        return tstClass;
    }
    
    
    /**
     * Returns fully qualified class name of a class given to an annotation
     * as (the only) argument.
     * 
     * @param  annValue  annotation value
     * @return  fully qualified name of a class represented by the given
     *          annotation value, or {@code null} if the annotation value
     *          does not represent a class
     */
    private Name getAnnotationValueClassName(AnnotationValue annValue,
                                             Types types) {
        Object value = annValue.getValue();
        if (value instanceof TypeMirror) {
            TypeMirror typeMirror = (TypeMirror) value;
            Element typeElement = types.asElement(typeMirror);
            if (typeElement.getKind() == ElementKind.CLASS) {
                return ((TypeElement) typeElement).getQualifiedName();
            }
        }
        return null;
    }
    
}
