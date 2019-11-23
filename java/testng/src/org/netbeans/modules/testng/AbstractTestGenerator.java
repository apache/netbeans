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

package org.netbeans.modules.testng;

import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.netbeans.modules.testng.TestCreator.ACCESS_MODIFIERS;

/**
 * A base class for generators of TestNG test classes and test methods.
 *
 * @author  Marian Petras
 */
abstract class AbstractTestGenerator implements CancellableTask<WorkingCopy>{
    
    /**
     * name of the 'instance' variable in the generated test method skeleton
     *
     * @see  #RESULT_VAR_NAME
     * @see  #EXP_RESULT_VAR_NAME
     */
    private static final String INSTANCE_VAR_NAME = "instance";         //NOI18N
    /**
     * name of the 'result' variable in the generated test method skeleton
     *
     * @see  #EXP_RESULT_VAR_NAME
     */
    private static final String RESULT_VAR_NAME = "result";             //NOI18N
    /**
     * name of the 'expected result' variable in the generated test method
     * skeleton
     *
     * @see  #RESULT_VAR_NAME
     */
    private static final String EXP_RESULT_VAR_NAME = "expResult";      //NOI18N
    /**
     * base for artificial names of variables
     * (if there is no name to derive from)
     */
    private static final String ARTIFICAL_VAR_NAME_BASE = "arg";        //NOI18N
    /**
     * name of the stub test method
     */
    private static final String STUB_TEST_NAME = "testSomeMethod";      //NOI18N
    /**
     * name of the 'container' variable in the generated ejb test method skeleton
     */
    private static final String CONTAINER_VAR_NAME = "container";         //NOI18N

    /** */
    private static final EnumSet<Modifier> NO_MODIFIERS
            = EnumSet.noneOf(Modifier.class);
    
    /**
     * Returns {@code EnumSet} of all access modifiers.
     * 
     * @return  {@code EnumSet} of all access modifiers;
     *          it is guaranteed that the returned set always contains
     *          the same set of {@code Modifier}s, but the returned
     *          instance may not always be the same
     */
    protected static EnumSet<Modifier> accessModifiers() {
        /*
         * An alternative would be to create an instance of
         * unmodifiable Set<Modifier> (e.g. Collections.unmodifiableSet(...))
         * and always return this instance. But the instance would not be an
         * instance of (subclass of) EnumSet which would significantly slow down
         * many operations performed on it.
         */
        return EnumSet.copyOf(ACCESS_MODIFIERS);
    }
    
    /**
     * Returns an empty {@code EnumSet} of {@code Modifier}s.
     * 
     * @return  empty {@code EnumSet} of all {@code Modifier}s;
     *          it is guaranteed that the returned set is always empty
     *          but the returned instance may not always be the same
     */
    protected static EnumSet<Modifier> noModifiers() {
        /*
         * An alternative would be to create an instance of
         * unmodifiable Set<Modifier> (e.g. Collections.<Modifier>emptySet())
         * and always return that instance. But the instance would not be an
         * instance of (subclass of) EnumSet which would significantly slow down
         * many operations performed on it.
         */
        return EnumSet.copyOf(NO_MODIFIERS);
    }
    
    /** */
    protected final TestGeneratorSetup setup;

    private final List<ElementHandle<TypeElement>> srcTopClassElemHandles;

    private final List<String> suiteMembers;

    private final boolean isNewTestClass;

    private List<String>processedClassNames;

    /**
     * cached value of <code>TestNGSettings.getGenerateMainMethodBody()</code>
     */
    private String initialMainMethodBody;
    
    private volatile boolean cancelled = false;


    /**
     * Used when creating a new empty test class.
     */
    protected AbstractTestGenerator(TestGeneratorSetup setup) {
        this.setup = setup;
        this.srcTopClassElemHandles = null;
        this.suiteMembers = null;
        this.isNewTestClass = true;   //value not used
    }

    /**
     * Used when creating a test class for a given source class
     * or when creating a test suite.
     */
    protected AbstractTestGenerator(
                    TestGeneratorSetup setup,
                    List<ElementHandle<TypeElement>> srcTopClassHandles,
                    List<String>suiteMembers,
                    boolean isNewTestClass) {
        this.setup = setup;
        this.srcTopClassElemHandles = srcTopClassHandles;
        this.suiteMembers = suiteMembers;
        this.isNewTestClass = isNewTestClass;
    }

    /**
     */
    public void run(WorkingCopy workingCopy) throws IOException {
        
        workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);

        CompilationUnitTree compUnit = workingCopy.getCompilationUnit();
        List<ClassTree> tstTopClasses = TopClassFinder.findTopClasses(
                                            compUnit,
                                            workingCopy.getTreeUtilities());
        TreePath compUnitPath = new TreePath(compUnit);

        List<TypeElement> srcTopClassElems
                = resolveHandles(workingCopy, srcTopClassElemHandles);

        if ((srcTopClassElems != null) && !srcTopClassElems.isEmpty()) {

            final ClassPath classPath =
                    workingCopy.getClasspathInfo()
                      .getClassPath(ClasspathInfo.PathKind.SOURCE);
            final FileObject fileObject = workingCopy.getFileObject();
            final String className =
                    classPath.getResourceName(fileObject, '.', false);

            // #188060
            assert className != null :
                    "Unknown class name. Test can't be generated. Bug #188060."
                    + "/nPlease, if possible, provide a sample project."
                    + "/n workingCopy=" + workingCopy 
                    + "/n workingCopy.getClasspathInfo()=" +
                                          workingCopy.getClasspathInfo()
                    + "/n classPath=" + classPath
                    + "/n fileObject=" + fileObject
                    + "/n classPath.findOwnerRoot(fileObject)=" +
                        classPath.findOwnerRoot(fileObject);

            /* Create/update a test class for each testable source class: */
            for (TypeElement srcTopClass : srcTopClassElems) {
                createOrUpdateTestClass(srcTopClass,
                                        tstTopClasses,
                                        className,
                                        compUnitPath,
                                        workingCopy);
            }
        } else if (suiteMembers != null) {          //test suite
            for (ClassTree tstClass : tstTopClasses) {
                TreePath tstClassTreePath = new TreePath(compUnitPath,
                                                         tstClass);
                ClassTree origTstTopClass = tstClass;
//                ClassTree tstTopClass = generateMissingSuiteClassMembers(
//                                                tstClass,
//                                                tstClassTreePath,
//                                                suiteMembers,
//                                                isNewTestClass,
//                                                workingCopy);
//                if (tstTopClass != origTstTopClass) {
//                    workingCopy.rewrite(origTstTopClass,
//                                        tstTopClass);
//                }
                classProcessed(tstClass);
            }
        } else if (srcTopClassElems == null) {      //new empty test class
            for (ClassTree tstClass : tstTopClasses) {
                ClassTree origTstTopClass = tstClass;
                ClassTree tstTopClass = generateMissingInitMembers(
                                                tstClass,
                                                new TreePath(compUnitPath,
                                                             tstClass),
                                                workingCopy);
                if (tstTopClass != origTstTopClass) {
                    workingCopy.rewrite(origTstTopClass,
                                        tstTopClass);
                }
            }
        }
    }
    
    /**
     * Creates or updates a test class for a given source class.
     * 
     * @param  srcTopClass  source class for which a test class should be
     *                      created or updated
     * @param  tstTopClasses  list of top-level classes that are present
     *                        in the test source code
     * @param  testClassName  desired name of the test class corresponding
     *                        to the given source class; if a test class of the
     *                        given name is not found, it is created
     * @param  compUnitPath  tree-path to the compilation unit of the test
     *                       source file
     * @param  workingCopy  working copy of the test file's structure
     */
    private void createOrUpdateTestClass(TypeElement srcTopClass,
                                         List<ClassTree> tstTopClasses,
                                         String testClassName,
                                         TreePath compUnitPath,
                                         WorkingCopy workingCopy) {
        List<ExecutableElement> srcMethods
                                = findTestableMethods(workingCopy, srcTopClass);
        boolean srcHasTestableMethods = !srcMethods.isEmpty();

        final String testClassSimpleName = TestUtil.getSimpleName(testClassName);

        ClassTree tstTopClass = findClass(testClassSimpleName, tstTopClasses);
        
        if (tstTopClass != null) {      //if the test class already exists
            TreePath tstTopClassTreePath = new TreePath(compUnitPath,
                                                        tstTopClass);

            ClassTree origTstTopClass = tstTopClass;
            if (srcHasTestableMethods) {
                tstTopClass = generateMissingTestMethods(
                                       srcTopClass,
                                       srcMethods,
                                       tstTopClass,
                                       tstTopClassTreePath,
                                       isNewTestClass,
                                       workingCopy);
            } else if (isNewTestClass) {
                tstTopClass = generateMissingInitMembers(
                                        tstTopClass,
                                        tstTopClassTreePath,
                                        workingCopy);
                // #175201
                tstTopClass = generateStubTestMethod(
                                       tstTopClass,
                                       STUB_TEST_NAME,
                                       workingCopy);

            }
            if (tstTopClass != origTstTopClass) {
                workingCopy.rewrite(origTstTopClass,
                                    tstTopClass);
            }
        } else {                        //it does not exist - it must be created
            if (srcHasTestableMethods) {
                tstTopClass = generateNewTestClass(workingCopy,
                                                   testClassSimpleName,
                                                   srcTopClass,
                                                   srcMethods);
                //PENDING - add the top class to the CompilationUnit

                //PENDING - generate suite method
            }
        }
    }

    /**
     * Returns default body for the test method. The generated body will
     * contains the following lines:
     * <pre><code>
     * // TODO review the generated test code and remove the default call to fail.
     * fail("The test case is a prototype.");
     * </code></pre>
     * @param maker the tree maker
     * @return an {@code ExpressionStatementTree} for the generated body.
     * @throws MissingResourceException
     * @throws IllegalStateException
     */
    @NbBundle.Messages({"TestCreator.variantMethods.defaultFailMsg=The test case is a prototype.",
        "TestCreator.variantMethods.defaultComment=TODO review the generated test code and remove the default call to fail."})
    private ExpressionStatementTree generateDefMethodBody(TreeMaker maker)
                        throws MissingResourceException, IllegalStateException {
//        String failMsg = NbBundle.getMessage(TestCreator.class,
//                                   "TestCreator.variantMethods.defaultFailMsg");
        String failMsg = Bundle.TestCreator_variantMethods_defaultFailMsg();
        MethodInvocationTree failMethodCall =
            maker.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                maker.Identifier("fail"),
                Collections.<ExpressionTree>singletonList(
                                                       maker.Literal(failMsg)));
        ExpressionStatementTree exprStatement =
            maker.ExpressionStatement(failMethodCall);
        if (setup.isGenerateMethodBodyComment()) {
//            Comment comment =
//                Comment.create(Comment.Style.LINE, -2, -2, -2,
//                               NbBundle.getMessage(AbstractTestGenerator.class,
//                                  "TestCreator.variantMethods.defaultComment"));
            Comment comment =
                Comment.create(Comment.Style.LINE, -2, -2, -2,
                               Bundle.TestCreator_variantMethods_defaultComment());
            maker.addComment(exprStatement, comment, true);
        }
        return exprStatement;
    }

    /**
     * Generates a new test class for the given source class.
     * 
     * @param  name  name of the class to be created
     * @param  srcClass  source class for which the test class should be created
     * @param  srcMethods  methods inside the source class for which
     *                     corresponding test methods should be created
     * @return  generated test class
     */
    private ClassTree generateNewTestClass(WorkingCopy workingCopy,
                                           String name,
                                           TypeElement srcClass,
                                           List<ExecutableElement> srcMethods) {

        String instanceClassName = null;
        ClassTree abstractClassImpl = null;
        if (srcClass.getModifiers().contains(ABSTRACT)
                && hasInstanceMethods(srcMethods)) {
            instanceClassName = getAbstractClassImplName(srcClass.getSimpleName());
            abstractClassImpl = generateAbstractClassImpl(srcClass,
                                                          instanceClassName,
                                                          workingCopy);
        }

        List<MethodTree> testMethods = generateTestMethods(srcClass,
                                                           instanceClassName,
                                                           srcMethods,
                                                           workingCopy);

        List<? extends Tree> members;
        if (abstractClassImpl == null) {
            members = testMethods;
        } else if (testMethods.isEmpty()) {
            members = Collections.singletonList(abstractClassImpl);
        } else {
            members = new ArrayList<Tree>(testMethods.size() + 1);
            ((List<Tree>) members).addAll(testMethods);
            ((List<Tree>) members).add(abstractClassImpl);
        }
        return composeNewTestClass(workingCopy, name, members);
    }

    /**
     * Generates a trivial non-abstract subclass of a given abstract class.
     *
     * @param  srcClass  abstract class for which an impl. class is to be made
     * @param  name  desired name of the subclass
     */
    private ClassTree generateAbstractClassImpl(TypeElement srcClass,
                                                CharSequence name,
                                                WorkingCopy workingCopy) {

        int membersCount = 0;
        MethodTree constructor = generateAbstractClassImplCtor(srcClass,
                                                               workingCopy);
        if (constructor != null) {
            membersCount++;
        }

        List<ExecutableElement> methods
                = ElementFilter.methodsIn(srcClass.getEnclosedElements());
        List<ExecutableElement> abstractMethods = findAbstractMethods(methods);
        if (!abstractMethods.isEmpty()) {
            membersCount += abstractMethods.size();
        }

        List<MethodTree> members;
        if (membersCount == 0) {
            members = Collections.emptyList();
        } else if (membersCount == 1) {
            if (constructor != null) {
                members = Collections.singletonList(constructor);
            } else {
                members = Collections.singletonList(
                        generateAbstractMethodImpl(abstractMethods.get(0),
                                                   workingCopy));
            }
        } else {
            members = new ArrayList<>(membersCount);
            if (constructor != null) {
                members.add(constructor);
            }
            for (ExecutableElement abstractMethod : abstractMethods) {
                members.add(generateAbstractMethodImpl(abstractMethod,
                                                       workingCopy));
            }

        }

        final TreeMaker maker = workingCopy.getTreeMaker();

        switch(srcClass.getKind()) {
            case INTERFACE:
            case ANNOTATION_TYPE:
                List<ExpressionTree> implemetnts =
                    new ArrayList<ExpressionTree>();
                implemetnts.add(maker.QualIdent(srcClass));
                return maker.Class(
                    maker.Modifiers(Collections.singleton(PUBLIC)),
                    name,                                         //name
                    Collections.<TypeParameterTree>emptyList(),   //type params
                    null,                                         //extends
                    implemetnts,                                  //implements
                    members);                                     //members
            default: // should be never happen. We'll generate a class anyway.
            case CLASS:
            case ENUM:
                return maker.Class(
                    maker.Modifiers(Collections.singleton(PUBLIC)),
                    name,                                         //name
                    Collections.<TypeParameterTree>emptyList(),   //type params
                    maker.QualIdent(srcClass),                    //extends
                    Collections.<ExpressionTree>emptyList(),      //implements
                    members);                                     //members
        }
    }

    /**
     * Makes a name for a simple implementation of an abstract class.
     * 
     * @param  baseName  name of the abstract class
     * @return  name for the simple class
     */
    private static String getAbstractClassImplName(CharSequence baseName) {
        return baseName + "Impl";                                       //NOI18N
    }

    /**
     * Generates a constructor for a simple subclass of a given abstract class,
     * if necessary.
     *
     * @param  srcClass  abstract class for which a subclass is being created
     * @param  name
     * @param  workingCopy  working copy of the class being created
     * @return  constructor for the class being generated,
     *          or {@code null} if no explicit constructor is necessary
     */
    private static MethodTree generateAbstractClassImplCtor(
                                                    TypeElement srcClass,
                                                    WorkingCopy workingCopy) {

        List<? extends VariableElement> superCtorParams;

        ExecutableElement superConstructor = findAccessibleConstructor(srcClass);
        if ((superConstructor == null)
            || (superCtorParams = superConstructor.getParameters()).isEmpty()
               && !throwsNonRuntimeExceptions(workingCopy, superConstructor)) {
            return null;
        }

        final TreeMaker maker = workingCopy.getTreeMaker();

        List<? extends VariableElement> defaultCtorParams
                = new ArrayList<>(superCtorParams);

        List<ExpressionTree> throwsList =
                (superConstructor != null)
                    ? generateThrowsList(superConstructor, workingCopy, maker, false)
                    : Collections.<ExpressionTree>emptyList();

        BlockTree body = maker.Block(
                Collections.singletonList(
                        maker.ExpressionStatement(
                                maker.MethodInvocation(
                                        Collections.<ExpressionTree>emptyList(),
                                        maker.Identifier("super"),      //NOI18N
                                        generateDefaultParamValues(
                                                defaultCtorParams, maker)))),
                false);

        return maker.Constructor(
                maker.Modifiers(Collections.singleton(PUBLIC)),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                throwsList,
                body);
    }

    /**
     * Generates default values (to be used in a method invocation)
     * for the given list of formal method parameters.
     *
     * @param  params  formal parameters to create default values for
     * @param  maker  tree maker to use for generating the default values
     * @return  list of default values for the given formal parameters
     */
    private static List<ExpressionTree> generateDefaultParamValues(
                                        List<? extends VariableElement> params,
                                        TreeMaker maker) {
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExpressionTree> result = new ArrayList<>(params.size());
        for (VariableElement param : params) {
            result.add(getDefaultValue(maker, param.asType()));
        }
        return result;
    }

    /**
     * Finds an non-private constructor in the given class.
     * It tries to find the following types of constructors, in this order:
     * <ol>
     *     <li>no-arg constructor</li>
     *     <li>one-argument constructor without throws-clause</li>
     *     <li>one-argument constructor with a throws-clause</li>
     *     <li>two-argument constructor without throws-clause</li>
     *     <li>two-argument constructor with a throws-clause</li>
     *     <li>etc.</li>
     * </ol>
     *
     * @param  clazz  class to find constructor in
     * @return  accessible (non-private) constructor,
     *          or {@code null} if none was found
     */
    private static ExecutableElement findAccessibleConstructor(TypeElement clazz) {
        List<ExecutableElement> ctors
                = ElementFilter.constructorsIn(clazz.getEnclosedElements());
        if (ctors.isEmpty()) {
            return null;
        }

        ExecutableElement best = null;
        int bestArgsCount = -1;
        boolean bestHasThrown = false;

        for (ExecutableElement ctor : ctors) {
            if (ctor.getModifiers().contains(PRIVATE)) {
                continue;
            }

            List args = ctor.getParameters();
            if (args.isEmpty()) {
                return ctor;        //this is the best constructor we could find
            }

            int argsCount = args.size();
            boolean hasThrown = !ctor.getThrownTypes().isEmpty();
            if ((best == null)
                    || (argsCount < bestArgsCount)
                    || (argsCount == bestArgsCount) && bestHasThrown && !hasThrown) {
                best = ctor;
                bestArgsCount = argsCount;
                bestHasThrown = hasThrown;
            }
        }
        return best;
    }

    /**
     * Generates a simple implementation of an abstract method declared
     * in a supertype.
     *
     * @param abstractMethod  the method whose implementation is to be generated
     */
    private static MethodTree generateAbstractMethodImpl(
                                            ExecutableElement abstractMethod,
                                            WorkingCopy workingCopy) {
        final TreeMaker maker = workingCopy.getTreeMaker();

        TypeMirror returnType = abstractMethod.getReturnType();
        List<? extends StatementTree> content;
        if (returnType.getKind() == TypeKind.VOID) {
            content = Collections.<StatementTree>emptyList();
        } else {
            content = Collections.singletonList(
                            maker.Return(getDefaultValue(maker, returnType)));
        }
        BlockTree body = maker.Block(content, false);

        return maker.Method(
                maker.Modifiers(Collections.singleton(PUBLIC)),
                abstractMethod.getSimpleName(),
                maker.Type(returnType),
                makeTypeParamsCopy(abstractMethod.getTypeParameters(), maker),
                makeParamsCopy(abstractMethod.getParameters(), maker),
                makeDeclaredTypesCopy((List<? extends DeclaredType>)
                                              abstractMethod.getThrownTypes(),
                                      maker),
                body,
                null);
    }

    /**
     * Makes a list of trees representing the given type parameter elements.
     */
    private static List<TypeParameterTree> makeTypeParamsCopy(
                                List<? extends TypeParameterElement> typeParams,
                                TreeMaker maker) {
        if (typeParams.isEmpty()) {
            return Collections.emptyList();
        }

        int size = typeParams.size();
        if (size == 1) {
            return Collections.singletonList(makeCopy(typeParams.get(0), maker));
        }

        List<TypeParameterTree> result = new ArrayList<>(size);
        for (TypeParameterElement typeParam : typeParams) {
            result.add(makeCopy(typeParam, maker));
        }
        return result;
    }

    private static List<VariableTree> makeParamsCopy(
                                        List<? extends VariableElement> params,
                                        TreeMaker maker) {
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        int size = params.size();
        if (size == 1) {
            return Collections.singletonList(makeCopy(params.get(0), maker));
        }

        List<VariableTree> result = new ArrayList<>(size);
        for (VariableElement param : params) {
            result.add(makeCopy(param, maker));
        }
        return result;
    }

    /**
     * Makes a tree representing the given {@code TypeParameterElement}.
     *
     * @param  typeParamElem  {@code TypeParameterElement} to make a copy of
     * @param  maker  tree maker to use when for creation of the copy
     * @return  {@code Tree} respresenting the given
     *          {@code TypeParameterElement}
     */
    private static TypeParameterTree makeCopy(TypeParameterElement typeParamElem,
                                              TreeMaker maker) {
        return maker.TypeParameter(
                typeParamElem.getSimpleName(),
                makeDeclaredTypesCopy((List<? extends DeclaredType>)
                                              typeParamElem.getBounds(),
                                      maker));
    }

    private static List<ExpressionTree> makeDeclaredTypesCopy(
                                          List<? extends DeclaredType> typeList,
                                          TreeMaker maker) {
        if (typeList.isEmpty()) {
            return Collections.emptyList();
        }

        int size = typeList.size();
        if (size == 1) {
            DeclaredType bound = typeList.get(0);
            return isRootObjectType(bound)                  //java.lang.Object
                   ? Collections.<ExpressionTree>emptyList()
                   : Collections.singletonList((ExpressionTree) maker.Type(bound));
        } else {
            List<ExpressionTree> result = new ArrayList<>(size);
            for (DeclaredType type : typeList) {
                result.add((ExpressionTree) maker.Type(type));
            }
            return result;
        }
    }

    /**
     * Checks whether the given type object represents type
     * {@code java.lang.Object}.
     * 
     * @param  type  type to be checked
     * @return  {@code true} if the passed type object represents type
     *          {@code java.lang.Object}, {@code false} otherwise
     */
    private static boolean isRootObjectType(DeclaredType type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }

        TypeElement elem = (TypeElement) type.asElement();
        return (elem.getKind() == ElementKind.CLASS)
               && (elem.getSuperclass().getKind() == TypeKind.NONE);
    }

    /**
     * Makes a tree representing the given {@code VariableElement}.
     *
     * @param  paramElem  {@code VariableElement} to make a copy of
     * @param  maker  tree maker to use when for creation of the copy
     * @return  {@code Tree} respresenting the given {@code VariableElement}
     */
    private static VariableTree makeCopy(VariableElement paramElem,
                                         TreeMaker maker) {
        return maker.Variable(
                    maker.Modifiers(Collections.<Modifier>emptySet()),
                    paramElem.getSimpleName(),
                    maker.Type(paramElem.asType()),
                    null);
    }

    /**
     * Finds abstract methods in the given class.
     *
     * @param  methods  method to find abstract methods among
     * @return  sublist of the passed list of methods, containing only
     *          abstract methods
     */
    private static List<ExecutableElement> findAbstractMethods(
                                              List<ExecutableElement> methods) {
        if (methods.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<ExecutableElement> result = null;
        int remainingCount = methods.size();
        for (ExecutableElement method : methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(ABSTRACT) && !modifiers.contains(STATIC)) {
                if (result == null) {
                    result = new ArrayList<>(remainingCount);
                }
                result.add(method);
            }
            remainingCount--;
        }

        if (result != null) {
            result.trimToSize();
        }
        return (result == null) ? Collections.<ExecutableElement>emptyList()
                                : result;
    }

    /**
     * Generates a new test class containing the given list of test methods.
     * 
     * @param  name  desired name of the test class
     * @param  members  desired content of the test class
     * @return  generated test class
     */
    protected abstract ClassTree composeNewTestClass(
                                        WorkingCopy workingCopy,
                                        String name,
                                        List<? extends Tree> members);

    /**
     */
    protected abstract List<? extends Tree> generateInitMembers(WorkingCopy workingCopy);

    /**
     * Generates missing set-up and tear-down methods in the given test class.
     * 
     * @param  tstClass  test class in which the methods should be generated
     * @return  a class tree with the missing methods added;
     *          or the passed class tree if no method was missing
     */
    protected abstract ClassTree generateMissingInitMembers(
                                                ClassTree tstClass,
                                                TreePath tstClassTreePath,
                                                WorkingCopy workingCopy);
    
    /**
     * Generates missing set-up and tear-down methods and adds them to the list
     * of class members.
     * 
     * @param  tstMembers  current list of test class members
     *                     - generated members will be added to it
     * @param  clsMap  index of the test class contents
     *                 - it will be updated if some members are added
     *                 to the list
     * @return  {@code true} if the list of members was modified,
     *          {@code false} otherwise
     */
    protected abstract boolean generateMissingInitMembers(
                                                 List<Tree> tstMembers,
                                                 ClassMap clsMap,
                                                 WorkingCopy workingCopy);
    
    /**
     * Finds position for the first init method.
     * 
     * @param  clsMap  index of the test class contents
     * @return  index where the first init method should be put,
     *          or {@code -1} if the method should be put to the end
     *          of the class
     */
    protected int getPlaceForFirstInitMethod(ClassMap clsMap) {
        int targetIndex;
        if (clsMap.containsMethods()) {
            targetIndex = clsMap.getFirstMethodIndex();
        } else if (clsMap.containsInitializers()) {
            targetIndex = clsMap.getLastInitializerIndex() + 1;
        } else if (clsMap.containsNestedClasses()) {
            targetIndex = clsMap.getFirstNestedClassIndex();
        } else {
            targetIndex = -1;        //end of the class
        }
        return targetIndex;
    }

    /**
     * 
     * @param  srcMethods  methods to create/update tests for
     * 
     */
    protected ClassTree generateMissingTestMethods(
                                    TypeElement srcClass,
                                    List<ExecutableElement> srcMethods,
                                    ClassTree tstClass,
                                    TreePath tstClassTreePath,
                                    boolean generateMissingInitMembers,
                                    WorkingCopy workingCopy) {
        if (srcMethods.isEmpty()) {
            return tstClass;
        }

        final Trees trees = workingCopy.getTrees();

        ClassMap clsMap = ClassMap.forClass(tstClass,
                                            tstClassTreePath,
                                            trees);

        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);
        tstMembers.addAll(tstMembersOrig);

        if (generateMissingInitMembers) {
            generateMissingInitMembers(tstMembers,
                                       clsMap,
                                       workingCopy);
        }
        generateMissingPostInitMethods(tstClassTreePath,
                                       tstMembers,
                                       clsMap,
                                       workingCopy);

        /* Generate test method names: */
//        TypeElement tstClassElem
//                = (TypeElement) trees.getElement(tstClassTreePath);
        List<String> testMethodNames
                = TestMethodNameGenerator.getTestMethodNames(srcMethods,
// passing null's to get the names as for newly created class to avoid creating all test methods every time we generating the tests
                                                             null, null,
                                                             workingCopy);

        Iterator<ExecutableElement> srcMethodsIt = srcMethods.iterator();
        Iterator<String> tstMethodNamesIt = testMethodNames.iterator();

        CharSequence instanceClassName = null;
        ClassTree newAbstractClassImpl = null;
        if (srcClass.getModifiers().contains(ABSTRACT)
                && hasInstanceMethods(srcMethods)) {
            String prefInstanceClassName
                    = getAbstractClassImplName(srcClass.getSimpleName());
            instanceClassName = findAbstractClassImplName(srcClass,
                                                          tstClass,
                                                          tstClassTreePath,
                                                          clsMap,
                                                          prefInstanceClassName,
                                                          trees,
                                                          workingCopy.getTypes());
            if (instanceClassName == null) {
                instanceClassName = prefInstanceClassName;
                newAbstractClassImpl = generateAbstractClassImpl(srcClass,
                                                                 instanceClassName,
                                                                 workingCopy);
            }
        }

        while (srcMethodsIt.hasNext()) {
            assert tstMethodNamesIt.hasNext();

            ExecutableElement srcMethod = srcMethodsIt.next();
            String testMethodName = tstMethodNamesIt.next();
            int testMethodIndex = clsMap.findNoArgMethod(testMethodName);
            if (testMethodIndex != -1) {
                continue;       //corresponding test method already exists
            }

            MethodTree newTestMethod = generateTestMethod(
                            srcClass,
                            srcMethod,
                            testMethodName,
                            instanceClassName,
                            workingCopy);

            tstMembers.add(newTestMethod);
            clsMap.addNoArgMethod(newTestMethod.getName().toString());
        }
        assert !tstMethodNamesIt.hasNext();

        if (newAbstractClassImpl != null) {
            tstMembers.add(newAbstractClassImpl);
            clsMap.addNestedClass(instanceClassName.toString());
        }

        if (tstMembers.size() == tstMembersOrig.size()) {  //no test method added
            return tstClass;
        }

        ClassTree newClass = workingCopy.getTreeMaker().Class(
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
    protected abstract void generateMissingPostInitMethods(
                                                TreePath tstClassTreePath,
                                                List<Tree> tstMembers,
                                                ClassMap clsMap,
                                                WorkingCopy workingCopy);
    
    /**
     * Generates test methods for the given source methods.
     * The created test methods will be put to a newly created test class.
     * The test class does not exist at the moment this method is called.
     * 
     * @param  srcClass  source class containing the source methods
     * @param  instanceClsName  name of a class whose instance should be created
     *                          if appropriate
     * @param  srcMethods  source methods the test methods should be created for
     */
    private List<MethodTree> generateTestMethods(
                                       TypeElement srcClass,
                                       CharSequence instanceClsName,
                                       List<ExecutableElement> srcMethods,
                                       WorkingCopy workingCopy) {
        if (srcMethods.isEmpty()) {
            return Collections.<MethodTree>emptyList();
        }

        List<String> testMethodNames
                = TestMethodNameGenerator.getTestMethodNames(srcMethods,
                                                             null,
                                                             null,   //reserved
                                                             workingCopy);

        Iterator<ExecutableElement> srcMethodsIt = srcMethods.iterator();
        Iterator<String> tstMethodNamesIt = testMethodNames.iterator();

        List<MethodTree> testMethods = new ArrayList<MethodTree>(srcMethods.size());
        while (srcMethodsIt.hasNext()) {
            assert tstMethodNamesIt.hasNext();

            ExecutableElement srcMethod = srcMethodsIt.next();
            String testMethodName = tstMethodNamesIt.next();

            testMethods.add(
                    generateTestMethod(srcClass,
                                       srcMethod,
                                       testMethodName,
                                       instanceClsName,
                                       workingCopy));
        }
        assert !tstMethodNamesIt.hasNext();
        return testMethods;
    }

    /**
     * Generates a test methods for the given source method.
     * 
     * @param  srcClass  source class - parent of the source method
     * @param  srcMethod  source method for which the test method should be
     *                    created
     * @param  useNoArgConstrutor  whether a no-argument constructor should be
     *                             used in the default test method body;
     *                             it should not be {@code true} unless
     *                             the source class contains an accessible
     *                             no-argument constructor
     * @param  instanceClsName  name of a class of which a constructor should be
     *                          createc if {@code useNoArgConstrutor} is
     *                          {@code true}
     * @return  the generated test method
     */
    @NbBundle.Messages({"# {0} - method",
        "# {1} - class",
        "TestCreator.variantMethods.JavaDoc.comment=Test of {0} method, of class {1}."})
    protected MethodTree generateTestMethod(TypeElement srcClass,
                                          ExecutableElement srcMethod,
                                          String testMethodName,
                                          CharSequence instanceClsName,
                                          WorkingCopy workingCopy) {
        final TreeMaker maker = workingCopy.getTreeMaker();

        MethodTree method = composeNewTestMethod(
                testMethodName,
                generateTestMethodBody(srcClass,
                                       srcMethod,
                                       instanceClsName,
                                       workingCopy),
                generateThrowsList(srcMethod, workingCopy, maker, isClassEjb31Bean(workingCopy, srcClass)),
                workingCopy);

        if (setup.isGenerateMethodJavadoc()) {
//            String commentText = NbBundle.getMessage(
//                    TestCreator.class,
//                    "TestCreator.variantMethods.JavaDoc.comment",       //NOI18N
//                    srcMethod.getSimpleName().toString(),
//                    srcClass.getSimpleName().toString());
            String commentText = Bundle.TestCreator_variantMethods_JavaDoc_comment(
                    srcMethod.getSimpleName().toString(),
                    srcClass.getSimpleName().toString());
            Comment javadoc = Comment.create(Comment.Style.JAVADOC,
                                             -2, -2, -2,
                                             commentText);
            maker.addComment(method, javadoc, true);
        }

        return method;
    }

    protected ClassTree generateStubTestMethod(ClassTree tstClass,
                                               String testMethodName,
                                               WorkingCopy workingCopy) {
        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 4);
        tstMembers.addAll(tstMembersOrig);

        List<ExpressionTree> throwsList = Collections.emptyList();
        MethodTree method = composeNewTestMethod(
                STUB_TEST_NAME,
                generateStubTestMethodBody(workingCopy),
                throwsList,
                workingCopy);

        tstMembers.add(method);

        ClassTree newClass = workingCopy.getTreeMaker().Class(
                tstClass.getModifiers(),
                tstClass.getSimpleName(),
                tstClass.getTypeParameters(),
                tstClass.getExtendsClause(),
                (List<? extends ExpressionTree>) tstClass.getImplementsClause(),
                tstMembers);
        return newClass;
    }

    /**
     * Generates a {@code throws}-clause of a method or constructor such that
     * it declares all non-runtime exceptions and errors that are declared
     * in the {@code throws}-clause of the given executable element (method or
     * constructor).
     *
     * @param  execElem  executable element (method or constructor) that serves
     *                   as the model for the generated throws-list
     * @param  compInfo  {@code CompilationInfo} to be used for obtaining
     *                   information about the given executable element
     * @param  maker  maker to be used for construction of the resulting list
     * @return  list of {@code ExpressionTree}s representing the throws-list
     *          that can be used for construction of a method with the same
     *          throws-clause as the given executable element
     */
    private static List<ExpressionTree> generateThrowsList(
                                                    ExecutableElement execElem,
                                                    CompilationInfo compInfo,
                                                    TreeMaker maker,
                                                    boolean forceThrowsClause) {
        if (forceThrowsClause || throwsNonRuntimeExceptions(compInfo, execElem)) {
            return Collections.<ExpressionTree>singletonList(
                                        maker.Identifier("Exception")); //NOI18N
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     */
    protected abstract MethodTree composeNewTestMethod(
                                            String testMethodName,
                                            BlockTree testMethodBody,
                                            List<ExpressionTree> throwsList,
                                            WorkingCopy workingCopy);

    /**
     */
//    private ClassTree generateMissingSuiteClassMembers(
//                                            ClassTree tstClass,
//                                            TreePath tstClassTreePath,
//                                            List<String> suiteMembers,
//                                            boolean isNewTestClass,
//                                            WorkingCopy workingCopy) {
//        final TreeMaker maker = workingCopy.getTreeMaker();
//
//        List<? extends Tree> tstMembersOrig = tstClass.getMembers();
//        List<Tree> tstMembers = new ArrayList<Tree>(tstMembersOrig.size() + 2);
//        tstMembers.addAll(tstMembersOrig);
//        boolean membersChanged = false;
//
//        ClassMap classMap = ClassMap.forClass(tstClass,
//                                              tstClassTreePath,
//                                              workingCopy.getTrees());
//
//        if (isNewTestClass) {
//            membersChanged |= generateMissingInitMembers(tstMembers,
//                                                         classMap,
//                                                         workingCopy);
//        }
//
//        return finishSuiteClass(tstClass,
//                                tstClassTreePath,
//                                tstMembers,
//                                suiteMembers,
//                                membersChanged,
//                                classMap,
//                                workingCopy);
//    }
    
    /**
     */
//    protected abstract ClassTree finishSuiteClass(
//                                        ClassTree tstClass,
//                                        TreePath tstClassTreePath,
//                                        List<Tree> tstMembers,
//                                        List<String> suiteMembers,
//                                        boolean membersChanged,
//                                        ClassMap classMap,
//                                        WorkingCopy workingCopy);
    
    // <editor-fold defaultstate="collapsed" desc=" disabled code ">
//        /**
//         */
//        private void addMainMethod(final ClassTree classTree) {
//            MethodTree mainMethod = createMainMethod(maker);
//            if (mainMethod != null) {
//                maker.addClassMember(classTree, mainMethod);
//            }
//        }
//
//        /**
//         */
//        private void fillTestClass(JavaClass srcClass, JavaClass tstClass) {
//            
//            fillGeneral(tstClass);
//
//            List innerClasses = TestUtil.filterFeatures(srcClass,
//                                                        JavaClass.class);
//
//            /* Create test classes for inner classes: */
//            for (Iterator i = innerClasses.iterator(); i.hasNext(); ) {
//                JavaClass innerCls = (JavaClass) i.next();
//
//                if (!isClassTestable(innerCls).isTestable()) {
//                    continue;
//                }
//                    
//                /*
//                 * Check whether the test class for the inner class exists
//                 * and create one if it does not exist:
//                 */
//                String innerTestClsName
//                        = TestUtil.getTestClassName(innerCls.getSimpleName());
//                JavaClass innerTestCls
//                        = TestUtil.getClassBySimpleName(tstClass,
//                                                        innerTestClsName);
//                if (innerTestCls == null) {
//                    innerTestCls = tgtPkg.getJavaClass().createJavaClass();
//                    innerTestCls.setSimpleName(
//                            tstClass.getName() + '.' + innerTestClsName);
//                    tstClass.getFeatures().add(innerTestCls);
//                }
//
//                /* Process the tested inner class: */
//                fillTestClass(innerCls, innerTestCls);
//
//                /* Make the inner test class testable with JUnit: */
//                innerTestCls.setModifiers(innerTestCls.getModifiers() | Modifier.STATIC);
//            }
//
//            /* Add the suite() method (only if we are supposed to do so): */
//            if (generateSuiteClasses && !hasSuiteMethod(tstClass)) {
//                tstClass.getFeatures().add(createTestClassSuiteMethod(tstClass));
//            }
//
//            /* Create missing test methods: */
//            List srcMethods = TestUtil.filterFeatures(srcClass, Method.class);
//            for (Iterator i = srcMethods.iterator(); i.hasNext(); ) {
//                Method sm = (Method) i.next();
//                if (isMethodAcceptable(sm) &&
//                        tstClass.getMethod(createTestMethodName(sm.getName()),
//                                          Collections.EMPTY_LIST,
//                                          false)
//                        == null) {
//                    Method tm = createTestMethod(srcClass, sm);
//                    tstClass.getFeatures().add(tm);
//                }
//            }
//
//            /* Create abstract class implementation: */
//            if (!skipAbstractClasses
//                    && (Modifier.isAbstract(srcClass.getModifiers())
//                        || srcClass.isInterface())) {
//                createAbstractImpl(srcClass, tstClass);
//            }
//        }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" disabled code ">
//        /**
//         */
//        private Constructor createTestConstructor(String className) {
//            Constructor constr = tgtPkg.getConstructor().createConstructor(
//                               className,               // name
//                               Collections.EMPTY_LIST,  // annotations
//                               Modifier.PUBLIC,         // modifiers
//                               null,                    // Javadoc text
//                               null,                    // Javadoc - object
//                               null,                    // body - object
//                               "super(testName);\n",    // body - text  //NOI18N
//                               Collections.EMPTY_LIST,  // type parameters
//                               createTestConstructorParams(),  // parameters
//                               null);                   // exception names
//            return constr;
//        }
//
//        /**
//         */
//        private List/*<Parameter>*/ createTestConstructorParams() {
//            Parameter param = tgtPkg.getParameter().createParameter(
//                                "testName",             // parameter name
//                                Collections.EMPTY_LIST, // annotations
//                                false,                  // not final
//                                TestUtil.getTypeReference(   // type
//                                        tgtPkg, "String"),              //NOI18N
//                                0,                      // dimCount
//                                false);                 // is not var.arg.
//            return Collections.singletonList(param);
//        }
    // </editor-fold>

    /**
     * Creates a public static {@code main(String[])} method
     * with the body taken from settings.
     *
     * @param  maker  {@code TreeMaker} to use for creating the method
     * @return  created {@code main(...)} method,
     *          or {@code null} if the method body would be empty
     */
    private MethodTree createMainMethod(TreeMaker maker) {
        String initialMainMethodBody = getInitialMainMethodBody();
        if (initialMainMethodBody.length() == 0) {
            return null;
        }

        ModifiersTree modifiers = maker.Modifiers(
                createModifierSet(Modifier.PUBLIC, Modifier.STATIC));
        VariableTree param = maker.Variable(
                        maker.Modifiers(Collections.<Modifier>emptySet()),
                        "argList",                                      //NOI18N
                        maker.Identifier("String[]"),                   //NOI18N
                        null);            //initializer - not used in params
        MethodTree mainMethod = maker.Method(
              modifiers,                            //public static
              "main",                               //method name "main"//NOI18N
              maker.PrimitiveType(TypeKind.VOID),   //return type "void"
              Collections.<TypeParameterTree>emptyList(),     //type params
              Collections.<VariableTree>singletonList(param), //method param
              Collections.<ExpressionTree>emptyList(),        //throws-list
              '{' + initialMainMethodBody + '}',    //body text
              null);                                //only for annotations

        return mainMethod;
    }

    /**
     * Generates a default body of a test method.
     * 
     * @param  srcClass  class - parent of the tested source method
     * @param  srcMethod  source method which should be tested by the test
     * @param  useNoArgConstrutor  whether a no-argument constructor should be
     *                             used in the body;
     *                             it should not be {@code true} unless
     *                             the source class contains an accessible
     *                             no-argument constructor
     * @param  instanceClsName  name of a class of instance - only used if
     *                          {@code useNoArgConstrutor} is {@code true}
     */
    protected BlockTree generateTestMethodBody(TypeElement srcClass,
                                               ExecutableElement srcMethod,
                                               CharSequence instanceClsName,
                                               WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();

        ExecutableType srcMethodExType = (ExecutableType)srcMethod.asType();
        try {
            srcMethodExType = (ExecutableType)workingCopy.getTypes().asMemberOf((DeclaredType)srcClass.asType(), srcMethod);
        } catch (IllegalArgumentException iae) {
        }


        boolean isStatic = srcMethod.getModifiers().contains(Modifier.STATIC);
        List<StatementTree> statements = new ArrayList<StatementTree>(8);

        if (setup.isGenerateDefMethodBody()) {
            StatementTree sout = generateSystemOutPrintln(
                                    maker,
                                    srcMethod.getSimpleName().toString());
            List<VariableTree> paramVariables = generateParamVariables(
                                    workingCopy,
                                    srcMethodExType,
                                    getTestSkeletonVarNames(srcMethod.getParameters()));
            statements.add(sout);
            statements.addAll(paramVariables);

            if (!isStatic) {

                boolean useNoArgConstructor = hasAccessibleNoArgConstructor(srcClass);

                if (isClassEjb31Bean(workingCopy, srcClass) && isMethodInContainerLookup(srcClass, srcMethod)) {
                    statements.addAll(generateEJBLookupCode(maker, srcClass, srcMethod));
                } else {
                    VariableTree instanceVarInit = maker.Variable(
                        maker.Modifiers(Collections.<Modifier>emptySet()),
                        INSTANCE_VAR_NAME,
                        maker.QualIdent(srcClass),
                        useNoArgConstructor
                            ? generateNoArgConstructorCall(maker,
                                                            srcClass,
                                                            instanceClsName)
                             : maker.Literal(null));
                    statements.add(instanceVarInit);
                }
            }

            MethodInvocationTree methodCall = maker.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),    //type args.
                    maker.MemberSelect(
                            isStatic ? maker.QualIdent(srcClass)
                                     : maker.Identifier(INSTANCE_VAR_NAME),
                            srcMethod.getSimpleName()),
                    createIdentifiers(maker, paramVariables));

            TypeMirror retType = srcMethodExType.getReturnType();
            TypeKind retTypeKind = retType.getKind();

            switch(retTypeKind){
                case VOID:
                case ERROR: {
                    StatementTree methodCallStmt = maker.ExpressionStatement(methodCall);
                    statements.add(methodCallStmt);
                    break;
                }
                case TYPEVAR:{
                    retType = getSuperType(workingCopy, retType);
                    retTypeKind = retType.getKind();
                }
                default:
                    retType = workingCopy.getTypes().erasure(retType);
                    retTypeKind = retType.getKind();
                    Tree retTypeTree = maker.Type(retType);
                    VariableTree expectedValue = maker.Variable(
                            maker.Modifiers(NO_MODIFIERS),
                            EXP_RESULT_VAR_NAME,
                            retTypeTree,
                            getDefaultValue(maker, retType));
                    VariableTree actualValue = maker.Variable(
                            maker.Modifiers(NO_MODIFIERS),
                            RESULT_VAR_NAME,
                            retTypeTree,
                            methodCall);

                    List<ExpressionTree> comparisonArgs = new ArrayList<ExpressionTree>(2);
                    comparisonArgs.add(maker.Identifier(actualValue.getName().toString()));
                    comparisonArgs.add(maker.Identifier(expectedValue.getName().toString()));
                    if ((retTypeKind == TypeKind.DOUBLE) || (retTypeKind == TypeKind.FLOAT)){
                        comparisonArgs.add(maker.Identifier(new Double(0).toString()));
                    }

                    MethodInvocationTree comparison = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),    //type args.
                            maker.Identifier("assertEquals"),               //NOI18N
                            comparisonArgs);
                    StatementTree comparisonStmt = maker.ExpressionStatement(
                            comparison);

                    statements.add(expectedValue);
                    statements.add(actualValue);
                    statements.add(comparisonStmt);
            }

            // close EJBContainer if searching in container lookup was called
            if (isClassEjb31Bean(workingCopy, srcClass) && isMethodInContainerLookup(srcClass, srcMethod)) {
                statements.add(generateEJBCleanUpCode(maker));
            }
        }

        //PENDING - source code hints
//            if (generateSourceCodeHints) {
//                // generate comments to bodies
//                if (needsEmptyLine) {
//                    newBody.append('\n');
//                    needsEmptyLine = false;
//                }
//                newBody.append(NbBundle.getMessage(
//                    TestCreator.class,
//                    generateDefMethodBody
//                           ? "TestCreator.variantMethods.defaultComment"//NOI18N
//                           : "TestCreator.variantMethods.onlyComment")) //NOI18N
//                       .append('\n');
//            }

        if (setup.isGenerateDefMethodBody()) {
            ExpressionStatementTree exprStatementTree =
                                             generateDefMethodBody(maker);
            statements.add(exprStatementTree);
        }

        return maker.Block(statements, false);
    }

    protected BlockTree generateStubTestMethodBody(WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();
        List<StatementTree> statements = new ArrayList<StatementTree>(8);


        if (setup.isGenerateDefMethodBody()) {
            ExpressionStatementTree exprStatementTree =
                                             generateDefMethodBody(maker);
            statements.add(exprStatementTree);
        }

        return maker.Block(statements, false);
    }

    /**
     */
    private StatementTree generateSystemOutPrintln(TreeMaker maker,
                                                   String arg) {
        MethodInvocationTree methodInvocation = maker.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),        //type args
                maker.MemberSelect(
                        maker.MemberSelect(
                                maker.Identifier("System"), "out"), "println"),//NOI18N
                Collections.<LiteralTree>singletonList(
                        maker.Literal(arg)));                   //args.
        return maker.ExpressionStatement(methodInvocation);
    }

    /**
     */
    private List<VariableTree> generateParamVariables(
                                            WorkingCopy workingCopy,
                                            ExecutableType srcMethod,
                                            String[] varNames) {
        TreeMaker maker = workingCopy.getTreeMaker();
        List<? extends TypeMirror> params = srcMethod.getParameterTypes();
        if ((params == null) || params.isEmpty()) {
            return Collections.<VariableTree>emptyList();
        }

        Set<Modifier> noModifiers = Collections.<Modifier>emptySet();
        List<VariableTree> paramVariables = new ArrayList<VariableTree>(params.size());
        int index = 0;
        for (TypeMirror param : params) {
            if (param.getKind() == TypeKind.TYPEVAR){
                param = getSuperType(workingCopy, param);
            }
            paramVariables.add(
                    maker.Variable(maker.Modifiers(noModifiers),
                                   varNames[index++],
                                   maker.Type(param),
                                   getDefaultValue(maker, param)));
        }
        return paramVariables;
    }

    /**
     * Returns supertype of the provided type or provided type if no supertypes were found
     */

    private TypeMirror getSuperType(WorkingCopy workingCopy, TypeMirror type){
        List<? extends TypeMirror> superTypes = workingCopy.getTypes().directSupertypes(type);
        if (!superTypes.isEmpty()){
            return superTypes.get(0);
        }
        return type;
    }
    /**
     */
    private List<IdentifierTree> createIdentifiers(
                                            TreeMaker maker,
                                            List<VariableTree> variables) {
        List<IdentifierTree> identifiers;
        if (variables.isEmpty()) {
            identifiers = Collections.<IdentifierTree>emptyList();
        } else {
            identifiers = new ArrayList<IdentifierTree>(variables.size());
            for (VariableTree var : variables) {
                identifiers.add(maker.Identifier(var.getName().toString()));
            }
        }
        return identifiers;
    }

    /**
     * Builds list of variable names for use in a test method skeleton.
     * By default, names of variables are same as names of tested method's
     * declared parameters. There are three variable names reserved
     * for variables holding the instance the tested method will be called on,
     * the expected result and the actual result returned
     * by the tested method. This method resolves a potential conflict
     * if some of the tested method's parameter's name is one of these
     * reserved names - in this case, the variable name used is a slight
     * modification of the declared parameter's name. The method also resolves
     * cases that some or all parameters are without name - in this case,
     * an arbitrary name is assigned to each of these unnamed parameters.
     * The goal is to ensure that all of the used variable names are unique.
     *
     * @param  sourceMethodParams 
     *                  list of tested method's parameters (items are of type
     *                  <code>org.netbeans.jmi.javamodel.TypeParameter</code>)
     * @return  variable names used for default values of the tested method's
     *          parameters (the reserved variable names are not included)
     */
    private String[] getTestSkeletonVarNames(
            final List<? extends VariableElement> sourceMethodParams) {

        /* Handle the trivial case: */
        if (sourceMethodParams.isEmpty()) {
            return new String[0];
        }

        final int count = sourceMethodParams.size();
        String[] varNames = new String[count];
        boolean[] conflicts = new boolean[count];
        boolean issueFound = false;

        Set<String> varNamesSet = new HashSet<String>((int) ((count + 2) * 1.4));
        varNamesSet.add(INSTANCE_VAR_NAME);
        varNamesSet.add(RESULT_VAR_NAME);
        varNamesSet.add(EXP_RESULT_VAR_NAME);

        Iterator<? extends VariableElement> it = sourceMethodParams.iterator();
        for (int i = 0; i < count; i++) {
            String paramName = it.next().getSimpleName().toString();
            varNames[i] = paramName;

            if (paramName == null) {
                issueFound = true;
            } else if (!varNamesSet.add(paramName)) {
                conflicts[i] = true;
                issueFound = true;
            } else {
                conflicts[i] = false;
            }
        }

        if (issueFound) {
            for (int i = 0; i < count; i++) {
                String paramName;
                if (varNames[i] == null) {
                    paramName = ARTIFICAL_VAR_NAME_BASE + i;
                    if (varNamesSet.add(paramName)) {
                        varNames[i] = paramName;
                        continue;
                    } else {
                        conflicts[i] = true;
                    }
                }
                if (conflicts[i]) {
                    String paramNamePrefix = varNames[i] + '_';

                    int index = 2;
                    while (!varNamesSet.add(
                                    paramName = (paramNamePrefix + (index++))));
                    varNames[i] = paramName;
                }
            }
        }

        return varNames;
    }

    /**
     */
    private static ExpressionTree getDefaultValue(TreeMaker maker,
                                           TypeMirror type) {
        ExpressionTree defValue;
        TypeKind typeKind = type.getKind();
        if (typeKind.isPrimitive()) {
            switch (typeKind) {
                case BOOLEAN:
                    defValue = maker.Literal(Boolean.FALSE);
                    break;
                case CHAR:
                    defValue = maker.Literal(new Character(' '));
                    break;
                case BYTE:
                    defValue = maker.Literal(new Byte((byte) 0));
                    break;
                case SHORT:
                    defValue = maker.Literal(new Short((short) 0));
                    break;
                case INT:
                    defValue = maker.Literal(new Integer(0));
                    break;
                case FLOAT:
                    defValue = maker.Literal(new Float(0.0F));
                    break;
                case LONG:
                    defValue = maker.Literal(new Long(0L));
                    break;
                case DOUBLE:
                    defValue = maker.Literal(new Double(0.0));
                    break;
                default:
                    assert false : "unknown primitive type";            //NOI18N
                    defValue = maker.Literal(new Integer(0));
                    break;
            }
        } else if ((typeKind == TypeKind.DECLARED)
                   && type.toString().equals("java.lang.String")) {     //NOI18N
            defValue = maker.Literal("");                               //NOI18N
        } else {
            defValue = maker.Literal(null);
        }
        return defValue;
    }

    /**
     */
    private ExpressionTree generateNoArgConstructorCall(TreeMaker maker,
                                                        TypeElement cls,
                                                        CharSequence instanceClsName) {
        return maker.NewClass(
                null,                                   //enclosing instance
                Collections.<ExpressionTree>emptyList(),//type arguments
                instanceClsName != null
                        ? maker.Identifier(instanceClsName)
                        : maker.QualIdent(cls),         //class identifier
                Collections.<ExpressionTree>emptyList(),//arguments list
                null);                                  //class body
    }

    /**
     */
    private List<ExecutableElement> findTestableMethods(WorkingCopy wc, TypeElement classElem) {
        boolean isEJB = isClassEjb31Bean(wc, classElem);
        final Types types = wc.getTypes();

        Iterable<? extends Element> elements;
        if (isEJB){
            final TypeMirror typeObject = wc.getElements().getTypeElement("java.lang.Object").asType(); //NOI18N
            ElementAcceptor acceptor = new ElementAcceptor(){
                public boolean accept(Element e, TypeMirror type) {
                    return !types.isSameType(typeObject, e.getEnclosingElement().asType());
                }
            };
            elements = wc.getElementUtilities().getMembers(classElem.asType(), acceptor);
        } else {
            elements = classElem.getEnclosedElements();
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(elements);

        if (methods.isEmpty()) {
            return Collections.<ExecutableElement>emptyList();
        }

        List<ExecutableElement> testableMethods = null;

        int skippedCount = 0;
        for (ExecutableElement method : methods) {
            if (isTestableMethod(method) &&
                    (!isEJB || (isEJB && isTestableEJBMethod(method)))) {
                if (testableMethods == null) {
                    testableMethods = new ArrayList<ExecutableElement>(
                                             methods.size() - skippedCount);
                }
                testableMethods.add(method);
            } else {
                skippedCount++;
            }
        }

        return (testableMethods != null)
               ? testableMethods
               : Collections.<ExecutableElement>emptyList();
    }

    /**
     */
    private boolean isTestableMethod(ExecutableElement method) {
        if (method.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException();
        }

        return setup.isMethodTestable(method);
    }

    private boolean isTestableEJBMethod(ExecutableElement method){
        Set<Modifier> modifiers = method.getModifiers();
        
        return !(modifiers.isEmpty() || !EnumSet.copyOf(modifiers).removeAll(ACCESS_MODIFIERS)) &&
               !modifiers.contains(Modifier.PROTECTED);
    }
    /**
     * Finds a non-abstract, direct or indirect subclass of a given source class
     * among nested classes of the given test class. Both static nested classes
     * and inner classes are taken into account. Anonymous inner classes and
     * classes defined inside code blocks are ignored. If there are multiple
     * nested/inner classes the one having the given preferred name, if any,
     * is chosen.
     * 
     * @param  srcClass  abstract class the subclass of which is to be found
     * @param  tstClass  test class to search
     * @param  tstClassPath  tree-path to the test class
     * @param  tstClassMap  content index of the test class
     * @param  preferredName  preferred name of the nested/inner class
     * @return  name of the found nested class, or {@code null} if non was found
     */
    private static CharSequence findAbstractClassImplName(TypeElement srcClass,
                                                          ClassTree tstClass,
                                                          TreePath tstClassPath,
                                                          ClassMap tstClassMap,
                                                          String preferredName,
                                                          Trees trees,
                                                          Types types) {
        if (!tstClassMap.containsNestedClasses()) {
            return null;
        }

        boolean mayContainPreferred = tstClassMap.getNestedClasses()
                                      .contains(preferredName);

        List<? extends Tree> tstClassMembers = tstClass.getMembers();
        TypeMirror srcClassType = null;
        Name firstFound = null;
        for (int index : tstClassMap.getNestedClassIndexes()) {
            Tree member = tstClassMembers.get(index);
            assert TreeUtilities.CLASS_TREE_KINDS.contains(member.getKind());
            ClassTree nestedClass = (ClassTree) member;
            if (nestedClass.getModifiers().getFlags().contains(ABSTRACT)) {
                continue;
            }

            TreePath nestedClassPath = new TreePath(tstClassPath, nestedClass);
            TypeMirror nestedClassType = trees.getElement(nestedClassPath).asType();
            if (srcClassType == null) {
                srcClassType = srcClass.asType();
            }
            if (types.isSubtype(nestedClassType, srcClassType)) {
                Name name = nestedClass.getSimpleName();
                if (!mayContainPreferred || name.contentEquals(preferredName)) {
                    return name;
                } else if (firstFound == null) {
                    firstFound = name;
                }
            }

        }
        return firstFound;      //may be null
    }

    /**
     * Checks whether there is an instance (non-static) method among the given
     * methods.
     * 
     * @param  methods  methods to probe
     * @return  {@code true} if there is at least one non-static method in the
     *          given list of methods, {@code false} otherwise
     */
    private static boolean hasInstanceMethods(List<ExecutableElement> methods) {
        if (methods.isEmpty()) {
            return false;
        }

        for (ExecutableElement method : methods) {
            if (!method.getModifiers().contains(STATIC)) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    protected boolean hasAccessibleNoArgConstructor(TypeElement srcClass) {
        boolean answer;

        List<ExecutableElement> constructors
             = ElementFilter.constructorsIn(srcClass.getEnclosedElements());

        if (constructors.isEmpty()) {
            answer = true;  //no explicit constructor -> synthetic no-arg. constructor
        } else {
            answer = false;
            for (ExecutableElement constructor : constructors) {
                if (constructor.getParameters().isEmpty()) {
                    answer = !constructor.getModifiers().contains(Modifier.PRIVATE);
                    break;
                }
            }
        }
        return answer;
    }

    /**
     */
    private static boolean throwsNonRuntimeExceptions(CompilationInfo compInfo,
                                                      ExecutableElement method) {
        List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
        if (thrownTypes.isEmpty()) {
            return false;
        }

        String runtimeExcName = "java.lang.RuntimeException";           //NOI18N
        TypeElement runtimeExcElement = compInfo.getElements()
                                        .getTypeElement(runtimeExcName);
        if (runtimeExcElement == null) {
            Logger.getLogger("testng").log(                              //NOI18N
                    Level.WARNING,
                    "Could not find TypeElement for "                   //NOI18N
                            + runtimeExcName);
            return true;
        }

        Types types = compInfo.getTypes();
        TypeMirror runtimeExcType = runtimeExcElement.asType();
        for (TypeMirror exceptionType : thrownTypes) {
            if (!types.isSubtype(exceptionType, runtimeExcType)) {
                return true;
            }
        }

        return false;
    }

    /**
     */
    private <T extends Element> List<T> resolveHandles(
                                            CompilationInfo compInfo,
                                            List<ElementHandle<T>> handles) {
        if (handles == null) {
            return null;
        }
        if (handles.isEmpty()) {
            return Collections.<T>emptyList();
        }

        List<T> elements = new ArrayList<T>(handles.size());
        for (ElementHandle<T> handle : handles) {
            T element = handle.resolve(compInfo);
            if (element != null) {
                elements.add(element);
            } else {
                ErrorManager.getDefault().log(
                        ErrorManager.WARNING,
                        "TestNG: Could not resolve element handle "      //NOI18N
                                + handle.getBinaryName());
            }
        }
        return elements;
    }

    /**
     * Stops this creator - cancels creation of a test class.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     */
    private void classProcessed(ClassTree cls) {
        if (processedClassNames == null) {
            processedClassNames = new ArrayList<String>(4);
        }
        processedClassNames.add(cls.getSimpleName().toString());
    }

    /**
     */
    List<String> getProcessedClassNames() {
        return processedClassNames != null
               ? processedClassNames
               : Collections.<String>emptyList();
    }

    /* private methods */
    
//XXX: retouche
//    /**
//     *
//     * @param cls JavaClass to generate the comment to.
//     */
//    private static void addClassBodyComment(JavaClass cls) {
//        int off = cls.getEndOffset() - 1;        
//        String theComment1 = NbBundle.getMessage(TestCreator.class,
//                                                 CLASS_COMMENT_LINE1);
//        String theComment2 = NbBundle.getMessage(TestCreator.class,
//                                                 CLASS_COMMENT_LINE2);
//        String indent = getIndentString();
//        DiffElement diff = new DiffElement(
//                off,
//                off,
//                indent + theComment1 + '\n'
//                + indent + theComment2 + '\n' + '\n');
//        ((ResourceImpl) cls.getResource()).addExtDiff(diff);
//    }
    
    /**
     */
    private String getInitialMainMethodBody() {
        if (initialMainMethodBody == null) {
            initialMainMethodBody = TestNGSettings.getDefault()
                                    .getGenerateMainMethodBody();
            if (initialMainMethodBody == null) {
                /*
                 * set it to a non-null value so that this method does not try
                 * to load it from the settings next time
                 */
                initialMainMethodBody = "";                             //NOI18N
            }
        }
        return initialMainMethodBody;
    }
    
    /**
     */
    protected ModifiersTree createModifiersTree(String annotationClassName,
                                                Set<Modifier> modifiers,
                                                WorkingCopy workingCopy) {
        TreeMaker maker = workingCopy.getTreeMaker();
        AnnotationTree annotation = maker.Annotation(
                getClassIdentifierTree(annotationClassName, workingCopy),
                Collections.<ExpressionTree>emptyList());
        return maker.Modifiers(modifiers,
                               Collections.<AnnotationTree>singletonList(
                                                                   annotation));
    }

    /** */
    private Map<String, ExpressionTree> classIdentifiers;

    /**
     */
    protected ExpressionTree getClassIdentifierTree(String className,
                                                    WorkingCopy workingCopy) {
        ExpressionTree classIdentifier;
        if (classIdentifiers == null) {
            classIdentifier = null;
            classIdentifiers = new HashMap<String, ExpressionTree>(13);
        } else {
            classIdentifier = classIdentifiers.get(className);
        }
        if (classIdentifier == null) {
            TypeElement typeElement
                    = getElemForClassName(className, workingCopy.getElements());
            TreeMaker maker = workingCopy.getTreeMaker();
            classIdentifier = (typeElement != null)
                               ? maker.QualIdent(typeElement)
                               : maker.Identifier(className);
            classIdentifiers.put(className, classIdentifier);
        }
        return classIdentifier;
    }

    /**
     */
    protected static TypeElement getElemForClassName(String className,
                                                     Elements elements) {
        TypeElement elem = elements.getTypeElement(className);
        if (elem == null) {
            ErrorManager.getDefault().log(
                    ErrorManager.ERROR,
                    "Could not find TypeElement for " + className);     //NOI18N
        }
        return elem;
    }

    /**
     * Creates a {@code Set} of {@code Modifier}s from the given list
     * of modifiers.
     * 
     * @param  modifiers  modifiers that should be contained in the set
     * @return  set containing exactly the given modifiers
     */
    static Set<Modifier> createModifierSet(Modifier... modifiers) {
        EnumSet<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
        for (Modifier m : modifiers) {
            modifierSet.add(m);
        }
        return modifierSet;
    }

    private static boolean isClassEjb31Bean(WorkingCopy wc, TypeElement srcClass) {
        ClassPath cp = wc.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
        if (cp == null || cp.findResource("javax/ejb/embeddable/EJBContainer.class") == null) {
            // if EJBContainer class is not available on classpath then it is not EJB 3.1
            return false;
        }
        List<? extends AnnotationMirror> annotations = wc.getElements().getAllAnnotationMirrors(srcClass);
        for (AnnotationMirror am : annotations) {
            String annotation = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
            if (annotation.equals("javax.ejb.Singleton") || // NOI18N
                annotation.equals("javax.ejb.Stateless") || // NOI18N
                annotation.equals("javax.ejb.Stateful")) { // NOI18N
                // class is an EJB
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether is given method declared by no-interface Bean or by interface annotated by
     * {@code @javax.ejb.Remote} or {@code @javax.ejb.Local}
     *
     * @param srcClass class for which are generated test cases
     * @param srcMethod method of interest
     * @return {@code true} if the bean is no-interface or method is declared by
     * respectively annotated interface, {@code false} otherwise
     */
    private static boolean isMethodInContainerLookup(TypeElement srcClass, ExecutableElement srcMethod) {
        // check for no-interface LocalBean
        List<? extends AnnotationMirror> annotations = srcClass.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotations) {
            String annotation = ((TypeElement)annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString();
            if (annotation.equals("javax.ejb.LocalBean"))   // NOI18N
                return true;
        }
        // check if the class has empty implements clause or given method is declared by @Remote, @Local interface
        List<? extends TypeMirror> interfaces = srcClass.getInterfaces();
        if (interfaces.isEmpty()
                || areAllowedInterfacesForLocalBean(interfaces)
                || getEjbInterfaceDeclaringMethod(srcMethod, interfaces) != null) {
            return true;
        }
        return false;
    }

    /**
     * Checks {@code List} of interfaces if are all of them allowed for LocalBean,
     * means if all interfaces are {@code java.io.Serializable}, {@code java.io.Externalizable} or
     * from package {@code javax.ejb}
     * 
     * @param interfaces {@code List} of interfaces which should be checked
     * @return {@code true} if all interfaces are allowed for LocalBean, {@code false} otherwise
     */
    private static boolean areAllowedInterfacesForLocalBean(List<? extends TypeMirror> interfaces) {
        for (TypeMirror typeMirror : interfaces) {
            if (typeMirror instanceof DeclaredType) {
                TypeElement interfaceElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
                String interfaceClassName = interfaceElement.getQualifiedName().toString();
                if (!interfaceClassName.equals("java.io.Serializable")          //NOI18N
                        && !interfaceClassName.equals("java.io.Externalizable") //NOI18N
                        && !interfaceClassName.startsWith("javax.ejb.")) {      //NOI18N
                   return false;
                }
            }
        }
        return true;
    }

    private List<VariableTree> generateEJBLookupCode(TreeMaker maker, TypeElement srcClass, ExecutableElement srcMethod) {
        final String ejbContainerPackage = "javax.ejb.embeddable.EJBContainer"; // NOI18N
        List<VariableTree> trees = new ArrayList<VariableTree>();

        // TODO: there are probably better ways how to generate code below:
        IdentifierTree container = maker.Identifier(ejbContainerPackage); 
        MethodInvocationTree invocation = maker.MethodInvocation(
            Collections.<ExpressionTree>emptyList(),
            maker.MemberSelect(container, "createEJBContainer"), // NOI18N
            Collections.<ExpressionTree>emptyList()
        );
        VariableTree containerVarInit = maker.Variable(
            maker.Modifiers(Collections.<Modifier>emptySet()),
            CONTAINER_VAR_NAME,
            maker.QualIdent(ejbContainerPackage), 
            invocation
            );
        trees.add(containerVarInit);

        String className = getBeanInterfaceOrImplementationClassName(srcClass, srcMethod);
        IdentifierTree bean = maker.Identifier("(" + className + ")" + CONTAINER_VAR_NAME); // NOI18N
        MethodInvocationTree contextInvocation = maker.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                maker.MemberSelect(bean, "getContext"), // NOI18N
                Collections.<ExpressionTree>emptyList()
        );
        contextInvocation = maker.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                maker.MemberSelect(contextInvocation, "lookup"), // NOI18N
                Collections.<ExpressionTree>singletonList(maker.Literal("java:global/classes/"+srcClass.getSimpleName())) // NOI18N
        );
        VariableTree beanVarInit = maker.Variable(
            maker.Modifiers(Collections.<Modifier>emptySet()),
            INSTANCE_VAR_NAME,
            maker.QualIdent(className),
            contextInvocation
            );
        trees.add(beanVarInit);

        return trees;
    }

    /**
     * Get name of the implementation class or interface which should be used
     * for searching in server container context.
     *
     * @param srcClass class for which are generated TestNG tests
     * @param srcMethod currently generated method 
     * @return {@code String} which should be used for searching in context
     */
    private static String getBeanInterfaceOrImplementationClassName(TypeElement srcClass, ExecutableElement srcMethod) {
        String interfaceClassName = getEjbInterfaceDeclaringMethod(srcMethod, srcClass.getInterfaces());
        if (interfaceClassName != null) {
            return interfaceClassName;
        } else {
            return srcClass.getSimpleName().toString();
        }
    }

    /**
     * Gets interface classname which is annotated as Remote or Local and declares 
     * given method or {@code null} if no such interface was found
     *
     * @param srcMethod method which should be declared
     * @param interfaces {@code List} of interfaces to be scanned for {@code @Local},
     * {@code @Remote} annotation and method declaration
     * @return interface classname when was found satisfactory interface, {@code null} otherwise
     */
    private static String getEjbInterfaceDeclaringMethod(ExecutableElement srcMethod, List<? extends TypeMirror> interfaces) {
        for (TypeMirror typeMirror : interfaces) {
            if (typeMirror instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                TypeElement interfaceElement = (TypeElement) declaredType.asElement();
                if (isLocalOrRemoteInterface(interfaceElement)
                        && isMethodDeclaredByInterface(interfaceElement, srcMethod)) {
                    return interfaceElement.getSimpleName().toString();
                }
            }
        }
        return null;
    }

    /**
     * Checks whether is interface annotated as {@code @javax.ejb.Remote} or {@code @javax.ejb.Local}
     *
     * @param trgInterface interface which should be annotated
     * @return {@code true} if the interface is annotated, {@code false} otherwise
     */
    private static boolean isLocalOrRemoteInterface(TypeElement trgInterface) {
        List<? extends AnnotationMirror> annotations = trgInterface.getAnnotationMirrors();
        for (AnnotationMirror am : annotations) {
            String annotation = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
            if (annotation.equals("javax.ejb.Local") ||  // NOI18N
                annotation.equals("javax.ejb.Remote")) { // NOI18N
                // interface is @Local or @Remote
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the interface declare given method
     *
     * @param trgInterface interface to be declaring the method
     * @param srcMethod method to be declared
     * @return {@code true} if the method is declared by the interface, {@code false} otherwise
     */
    private static boolean isMethodDeclaredByInterface(TypeElement trgInterface, ExecutableElement srcMethod) {
        List<? extends Element> enclosedElements = trgInterface.getEnclosedElements();
        List<? extends VariableElement> methodParameters = srcMethod.getParameters();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement instanceof ExecutableElement) {
                ExecutableElement exElement = (ExecutableElement) enclosedElement;
                List<? extends VariableElement> elementParameters = exElement.getParameters();
                if (srcMethod.getSimpleName() != exElement.getSimpleName()
                        || (methodParameters.size() != elementParameters.size())) {
                    continue;
                }
                for (int i = 0; i < methodParameters.size(); i++) {
                    if (!((VariableElement) methodParameters.get(i)).asType().toString().equals(
                            ((VariableElement) elementParameters.get(i)).asType().toString())) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private StatementTree generateEJBCleanUpCode(TreeMaker maker) {
        IdentifierTree container = maker.Identifier(CONTAINER_VAR_NAME);
        MethodInvocationTree invocation = maker.MethodInvocation(
            Collections.<ExpressionTree>emptyList(),
            maker.MemberSelect(container, "close"), // NOI18N
            Collections.<ExpressionTree>emptyList()
        );
        return maker.ExpressionStatement(invocation);
    }

    /**
     * Finds a class with the specified simple name in the specified list of
     * classes.
     * @param simpleName the simple class name.
     * @param classes the list of the {@code ClassTree}s.
     * @return a {@code ClassTree} of the class if the specified simple class
     *         name is not {@code null}, and a class with that name exists,
     *         otherwise {@code null}.
     */
    private ClassTree findClass(String simpleName, List<ClassTree> classes) {
        if(simpleName == null) { // #180480
            return null;
        }
        for (ClassTree cls : classes) {
            if (cls.getSimpleName().contentEquals(simpleName)) {
                return cls;     // class exists
            }
        }
        return null;
    }

}
