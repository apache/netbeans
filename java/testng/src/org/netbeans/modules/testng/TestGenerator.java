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

import com.sun.source.tree.AnnotationTree;
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
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
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
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 *
 * @author  Marian Petras
 */
final class TestGenerator extends AbstractTestGenerator {
    
    /** */
    static final String ANN_BEFORE_CLASS = "org.testng.annotations.BeforeClass";     //NOI18N
    /** */
    static final String ANN_AFTER_CLASS = "org.testng.annotations.AfterClass";       //NOI18N
    /** */
    static final String ANN_BEFORE = "org.testng.annotations.BeforeMethod";                //NOI18N
    /** */
    static final String ANN_AFTER = "org.testng.annotations.AfterMethod";                  //NOI18N
    /** */
    static final String ANN_TEST = "org.testng.annotations.Test";                    //NOI18N
    /** */
//    private static final String ANN_RUN_WITH = "org.junit.runner.RunWith";//NOI18N
//    /** */
//    private static final String ANN_SUITE = "org.junit.runners.Suite";  //NOI18N
    /** */
    private static final String ANN_SUITE_MEMBERS = "SuiteClasses";     //NOI18N
    /** */
    private static final String BEFORE_CLASS_METHOD_NAME = "setUpClass";//NOI18N
    /** */
    private static final String AFTER_CLASS_METHOD_NAME = "tearDownClass";//NOI18N
    /** */
    private static final String BEFORE_METHOD_NAME = "setUpMethod";           //NOI18N
    /** */
    private static final String AFTER_METHOD_NAME = "tearDownMethod";         //NOI18N
    
    /**
     */
    TestGenerator(TestGeneratorSetup setup) {
        super(setup);
    }
    
    /**
     */
    TestGenerator(TestGeneratorSetup setup,
                        List<ElementHandle<TypeElement>> srcTopClassHandles,
                        List<String>suiteMembers,
                        boolean isNewTestClass) {
        super(setup, srcTopClassHandles, suiteMembers, isNewTestClass);
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
                    /*
                     * if (beforeIndex != -1)
                     *     targetIndex = beforeIndex;
                     * else if (afterIndex != -1)
                     *     targetIndex = afterIndex;
                     * else
                     *     targetIndex = -1;
                     */
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
                
                /*
                 * if ((beforeClassIndex != -1) && (afterClassIndex != -1))
                 *     targetIndex = Math.max(beforeClassIndex, afterClassIndex) + 1;
                 * else if (beforeClassIndex != -1)
                 *     targetIndex = beforeClassIndex + 1;
                 * else if (afterClassIndex != -1)
                 *     targetIndex = afterClassIndex + 1;
                 * else
                 *     targetIndex = -1
                 */
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
//    @Override
//    protected ClassTree finishSuiteClass(ClassTree tstClass,
//                                         TreePath tstClassTreePath,
//                                         List<Tree> tstMembers,
//                                         List<String> suiteMembers,
//                                         boolean membersChanged,
//                                         ClassMap classMap,
//                                         WorkingCopy workingCopy) {
//
//        ModifiersTree currModifiers = tstClass.getModifiers();
//        ModifiersTree modifiers = fixSuiteClassModifiers(tstClass,
//                                                         tstClassTreePath,
//                                                         currModifiers,
//                                                         suiteMembers,
//                                                         workingCopy);
//        if (!membersChanged) {
//            if (modifiers != currModifiers) {
//                workingCopy.rewrite(currModifiers, modifiers);
//            }
//            return tstClass;
//        }
//
//        return workingCopy.getTreeMaker().Class(
//                modifiers,
//                tstClass.getSimpleName(),
//                tstClass.getTypeParameters(),
//                tstClass.getExtendsClause(),
//                (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
//                tstMembers);
//    }
    
//    /**
//     * Keeps or modifies annotations and modifiers of the given suite class.
//     * Modifiers are modified such that the class is public.
//     * The list of annotations is modified such that it contains
//     * the following annotations:
//     * <pre><code>RunWith(Suite.class)
//     * @SuiteRunner.Suite({...})</code></pre>
//     * with members of the suite in place of the <code>{...}</code> list.
//     * 
//     * @param  tstClass  class whose modifiers and anntations are to be modified
//     * @param  tstClassTreePath  tree path to the class from the compilation unit
//     * @param  modifiers  current modifiers and annotations
//     * @param  suiteMembers  list of class names that should be contained
//     *                       in the test suite
//     * @return  {@code ModifiersTree} object containing the modified set
//     *          of class modifiers and annotations, or {@code null}
//     *          if no modifications were necessary
//     */
//    private ModifiersTree fixSuiteClassModifiers(ClassTree tstClass,
//                                                 TreePath tstClassTreePath,
//                                                 ModifiersTree modifiers,
//                                                 List<String> suiteMembers,
//                                                 WorkingCopy workingCopy) {
//        boolean flagsModified = false;
//        
//        Set<Modifier> currFlags = modifiers.getFlags();
//        Set<Modifier> flags = EnumSet.copyOf(currFlags);
//        flagsModified |= flags.remove(PRIVATE);
//        flagsModified |= flags.remove(PROTECTED);
//        flagsModified |= flags.add(PUBLIC);
//        if (!flagsModified) {
//            flags = currFlags;
//        }
//        
//        
//        boolean annotationListModified = false;
//        
//        List<? extends AnnotationTree> currAnnotations = modifiers.getAnnotations();
//        List<? extends AnnotationTree> annotations;
//        if (currAnnotations.isEmpty()) {
//            List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>(2);
//            newAnnotations.add(createRunWithSuiteAnnotation(workingCopy));
//            newAnnotations.add(createSuiteAnnotation(suiteMembers, workingCopy));
//            annotations = newAnnotations;
//            
//            annotationListModified = true;
//        } else {
//            Trees trees = workingCopy.getTrees();
//            Element classElement = trees.getElement(tstClassTreePath);
//            List<? extends AnnotationMirror> annMirrors
//                    = classElement.getAnnotationMirrors();
//            assert annMirrors.size() == currAnnotations.size();
//            
//            
//            int index = -1, runWithIndex = -1, suiteClassesIndex = -1;
//            for (AnnotationMirror annMirror : annMirrors) {
//                index++;
//                Element annElement = annMirror.getAnnotationType().asElement();
//                assert annElement instanceof TypeElement;
//                TypeElement annTypeElem = (TypeElement) annElement;
//                Name annFullName = annTypeElem.getQualifiedName();
//                
//                if ((runWithIndex == -1) && annFullName.contentEquals(ANN_RUN_WITH)) {
//                    runWithIndex = index;
//                } else if ((suiteClassesIndex == -1)
//                        && annFullName.contentEquals(ANN_SUITE + '.' + ANN_SUITE_MEMBERS)) {
//                    suiteClassesIndex = index;
//                }
//            }
//            
//            AnnotationTree runWithSuiteAnn;
//            if ((runWithIndex == -1) || !checkRunWithSuiteAnnotation(
//                                                annMirrors.get(runWithIndex),
//                                                workingCopy)) {
//                runWithSuiteAnn = createRunWithSuiteAnnotation(workingCopy);
//            } else {
//                runWithSuiteAnn = currAnnotations.get(runWithIndex);
//            }
//            
//            AnnotationTree suiteClassesAnn;
//            if ((suiteClassesIndex == -1) || !checkSuiteMembersAnnotation(
//                                                      annMirrors.get(suiteClassesIndex),
//                                                      suiteMembers,
//                                                      workingCopy)) {
//                suiteClassesAnn = createSuiteAnnotation(suiteMembers, workingCopy);
//            } else {
//                suiteClassesAnn = currAnnotations.get(suiteClassesIndex);
//            }
//            
//            if ((runWithIndex != -1) && (suiteClassesIndex != -1)) {
//                if (runWithSuiteAnn != currAnnotations.get(runWithIndex)) {
//                    workingCopy.rewrite(
//                            currAnnotations.get(runWithIndex),
//                            runWithSuiteAnn);
//                }
//                if (suiteClassesAnn != currAnnotations.get(suiteClassesIndex)) {
//                    workingCopy.rewrite(
//                            currAnnotations.get(suiteClassesIndex),
//                            suiteClassesAnn);
//                }
//                annotations = currAnnotations;
//            } else {
//                List<AnnotationTree> newAnnotations
//                        = new ArrayList<AnnotationTree>(currAnnotations.size() + 2);
//                if ((runWithIndex == -1) && (suiteClassesIndex == -1)) {
//                    
//                    /*
//                     * put the @RunWith(...) and @Suite.SuiteClasses(...)
//                     * annotations in front of other annotations
//                     */
//                    newAnnotations.add(runWithSuiteAnn);
//                    newAnnotations.add(suiteClassesAnn);
//                    if (!currAnnotations.isEmpty()) {
//                        newAnnotations.addAll(currAnnotations);
//                    }
//                } else {
//                    newAnnotations.addAll(currAnnotations);
//                    if (runWithIndex == -1) {
//                        assert suiteClassesIndex != 1;
//                        
//                        /*
//                         * put the @RunWith(...) annotation
//                         * just before the Suite.SuiteClasses(...) annotation
//                         */
//                        newAnnotations.add(suiteClassesIndex, runWithSuiteAnn);
//                    } else {
//                        assert runWithIndex != -1;
//                        
//                        /*
//                         * put the @Suite.SuiteClasses(...) annotation
//                         * just after the @RunWith(...) annotation
//                         */
//                        newAnnotations.add(runWithIndex + 1, suiteClassesAnn);
//                    }
//                }
//                annotations = newAnnotations;
//                
//                annotationListModified = true;
//            }
//        }
//        
//        if (!flagsModified && !annotationListModified) {
//            return modifiers;
//        }
//        
//        return workingCopy.getTreeMaker().Modifiers(flags, annotations);
//    }
    
//    /**
//     * Checks that the given annotation is of type
//     * <code>{@value #ANN_RUN_WITH}</code> and contains argument
//     * <code>{@value #ANN_SUITE}{@literal .class}</code>.
//     * 
//     * @param  annMirror  annotation to be checked
//     * @return  {@code true} if the annotation meets the described criteria,
//     *          {@code false} otherwise
//     */
//    private boolean checkRunWithSuiteAnnotation(AnnotationMirror annMirror,
//                                                WorkingCopy workingCopy) {
//        Map<? extends ExecutableElement,? extends AnnotationValue> annParams
//                = annMirror.getElementValues();
//        
//        if (annParams.size() != 1) {
//            return false;
//        }
//        
//        AnnotationValue annValue = annParams.values().iterator().next();
//        Name annValueClsName = getAnnotationValueClassName(annValue,
//                                                           workingCopy.getTypes());
//        return annValueClsName != null
//               ? annValueClsName.contentEquals(ANN_SUITE)
//               : false;
//    }
    
    /**
     * Checks that the given annotation is of type
     * <code>{@value #ANN_SUITE}.{@value #ANN_SUITE_MEMBERS}</code>
     * and contains the given list of classes as (the only) argument,
     * in the same order.
     * 
     * @param  annMirror  annotation to be checked
     * @param  suiteMembers  list of fully qualified class names denoting
     *                       content of the test suite
     * @return  {@code true} if the annotation meets the described criteria,
     *          {@code false} otherwise
     */
    private boolean checkSuiteMembersAnnotation(AnnotationMirror annMirror,
                                                List<String> suiteMembers,
                                                WorkingCopy workingCopy) {
        Map<? extends ExecutableElement,? extends AnnotationValue> annParams
                = annMirror.getElementValues();
        
        if (annParams.size() != 1) {
            return false;
        }
        
        AnnotationValue annValue = annParams.values().iterator().next();
        Object value = annValue.getValue();
        if (value instanceof java.util.List) {
            List<? extends AnnotationValue> items
                    = (List<? extends AnnotationValue>) value;
            
            if (items.size() != suiteMembers.size()) {
                return false;
            }
            
            Types types = workingCopy.getTypes();
            Iterator<String> suiteMembersIt = suiteMembers.iterator();
            for (AnnotationValue item : items) {
                Name suiteMemberName = getAnnotationValueClassName(item, types);
                if (suiteMemberName == null) {
                    return false;
                }
                if (!suiteMemberName.contentEquals(suiteMembersIt.next())) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
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
    
//    /**
//     * Creates annotation <code>@org.junit.runner.RunWith</code>.
//     * 
//     * @return  created annotation
//     */
//    private AnnotationTree createRunWithSuiteAnnotation(
//                                                WorkingCopy workingCopy) {
//        TreeMaker maker = workingCopy.getTreeMaker();
//        
//        /* @RunWith(Suite.class) */
//        return maker.Annotation(
//                getClassIdentifierTree(ANN_RUN_WITH, workingCopy),
//                Collections.<ExpressionTree>singletonList(
//                        maker.MemberSelect(
//                                getClassIdentifierTree(ANN_SUITE, workingCopy),
//                                "class")));                             //NOI18N
//    }
//    
//    /**
//     * Creates annotation
//     * <code>@org.junit.runners.Suite.SuiteClasses({...})</code>.
//     * 
//     * @param  suiteMembers  fully qualified names of classes to be included
//     *                       in the test suite
//     * @param  created annotation
//     */
//    private AnnotationTree createSuiteAnnotation(List<String> suiteMembers,
//                                                 WorkingCopy workingCopy) {
//        final TreeMaker maker = workingCopy.getTreeMaker();
//        
//        List<ExpressionTree> suiteMemberExpressions
//                = new ArrayList<ExpressionTree>(suiteMembers.size());
//        for (String suiteMember : suiteMembers) {
//            suiteMemberExpressions.add(
//                    maker.MemberSelect(
//                            getClassIdentifierTree(suiteMember, workingCopy),
//                            "class"));                                  //NOI18N
//        }
//        
//        /* @Suite.SuiteClasses({TestA.class, TestB.class, ...}) */
//        return maker.Annotation(
//                maker.MemberSelect(
//                        getClassIdentifierTree(ANN_SUITE, workingCopy),
//                        ANN_SUITE_MEMBERS),
//                Collections.singletonList(
//                        maker.NewArray(
//                                null,  //do not generate "new Class[]"
//                                Collections.<ExpressionTree>emptyList(),
//                                suiteMemberExpressions)));
//    }

}
