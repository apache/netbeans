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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * This test contains examples described in HOWTO - modify the source code.
 *
 * (Test will always pass, as it does not check the results. It is used
 * just for explanation.)
 *
 * @author Pavel Flaska
 */

public class TutorialTest extends GeneratorTestBase {
    
    /** Creates a new instance of TutorialTest */
    public TutorialTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TutorialTest.class);
//        suite.addTest(new TutorialTest("testFirstModification"));
//        suite.addTest(new TutorialTest("testAddMethod"));
//        suite.addTest(new TutorialTest("testAddAnnotation"));
//        suite.addTest(new TutorialTest("testForErno"));
//        suite.addTest(new TutorialTest("testMethodInvocation"));
//        suite.addTest(new TutorialTest("testNullLiteral"));
        return suite;
    }
    
    // Modifications demo:
    // adds "implements Externalizable" and its import to the source.
    public void testFirstModification() throws FileStateInvalidException,IOException {
        // First of all, we have to look for JavaSource we want to work with...
        // There are more ways to do it. For our demostration, we use 
        // straightforward solution, often used in tests. We omit details how
        // to obtain correct file object and java source and we expect 
        // successful behaviour of called methods.
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial1.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                // working copy is used for modify source. When all is
                // done, call commit() method on it to propagate changes
                // to original source.
                workingCopy.toPhase(Phase.RESOLVED);
                
                // CompilationUnitTree represents one java source file,
                // exactly as defined in  JLS, §7.3 Compilation Units.
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                // Get the tree maker. This is the core class used for
                // many modifications. It allows to add new members to class,
                // modify statements. You should be able to do anything
                // you need with your source.
                TreeMaker make = workingCopy.getTreeMaker();
                // Go through all the (§7.6) Top Level Type Declarations and
                // add the Externalizable interface to their declaration.
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        // Now, there are several way how to create interface
                        // identifier which we wants to add to the class declaration.
                        
                        // Simpliest, but not sufficient solution: Add the
                        // plain identifier. It generates source as you can
                        // see below, but when import is not available,
                        // identifier is not resolved and class will not
                        // compile.
                        // public class Tutorial1 {
                        // public class Tutorial1 implements Externalizable {
                        ExpressionTree implementsClause = make.Identifier("Externalizable");
                        

                        // We can solve described problem with specifying
                        // fully-qualified name. We can create again identifier
                        // tree. (Bear in mind, that you will never get such
                        // an identifier from the compiler staff - this identifier
                        // will be represented as chain of MemberSelectTree
                        // of "io" and "Externalizable" and IdentifierTree "java".
                        // Currently, it is compilable and correct, but one can
                        // consider to do it much more precisely. See below.
                        // public class Tutorial1 {
                        // public class Tutorial1 implements java.io.Externalizable {
                        implementsClause = make.Identifier("java.io.Externalizable");
                        
                        // Last one and probably the most often used solution.
                        // Use the resolved type, provide the fully-qualified name
                        // for this resolution. You should check, that element is
                        // available. Then, make QualIdent tree, which will be
                        // recognized during source code modification and engine
                        // will decide (in accordance with options) how to correctly
                        // generate. Often, import for your class will be added
                        // and simple name will be used in implments clause.
                        // public class Tutorial1 {
                        //
                        // import java.io.Externalizable;
                        // public class Tutorial1 implements Externalizable {
                        TypeElement element = workingCopy.getElements().getTypeElement("java.io.Externalizable");
                        implementsClause = make.QualIdent(element);
                        
                        // At this time, we want to add the created tree to correct
                        // place. We will use method addClassImplementsClause().
                        // Many of features uses these method, let's clarify
                        // names of the method:
                        // (add|insert|remove) prepend identified operation.
                        // (identifier)  identifies tree which will be modified,
                        // in our case it is ClassTree. The rest identifies the
                        // list which will be updated.
                        // See TreeMaker javadoc for details.
                        ClassTree modifiedClazz = make.addClassImplementsClause(clazz, implementsClause);
                        // As nodes in tree are immutable, all method return
                        // the same class type as provided in first paramter.
                        // If the method takes ClassTree parameter, it will
                        // return another class tree, which contains provided
                        // modification.
                        
                        // At the and, when you makes all the necessary changes,
                        // do not forget to replace original node with the new
                        // one.
                        
                        // TODO: changes can be chained, demonstrate!
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

        };

        // Now, we can start to process the changes. Because we want to modify
        // source, we have to use runModificationTask (see its javadoc).
        // At the end, we have to commit changes to propagate all the work
        // to the source file... This can fail, so ensure you correctly
        // handling exceptions. For testing reasons it is unimportant.
        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }

    // creates and adds method to the source-code
    public void testAddMethod() throws FileStateInvalidException, IOException {
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial1.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        
                        // Create the method. This is done again through the
                        // TreeMaker as in the previous method. Here is the
                        // code how will method look like:
                        //
                        //    public void writeExternal(final ObjectOutput arg0) throws IOException {
                        //        throw new UnsupportedOperationException("Not supported yet.");
                        //    }
                        
                        // create method modifier: public and no annotation
                        ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList()
                        );
                        
                        // create parameter:
                        // final ObjectOutput arg0
                        VariableTree parameter = make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "arg0", // name
                                make.Identifier("Object"), // parameter type
                                null // initializer - does not make sense in parameters.
                        );
                        
                        // prepare simple name to throws clause:
                        // 'throws IOException' and its import will be added
                        TypeElement element = workingCopy.getElements().getTypeElement("java.io.IOException");
                        ExpressionTree throwsClause = make.QualIdent(element);
                        
                        // create method. There are two basic options:
                        // 1)
                        // make.Method() with 'BlockTree body' parameter -
                        // body has to be created, here in example code
                        // empty body block commented out
                        // 2)
                        // make.Method() with 'String body' parameter -
                        // body is added as a text. Used in our example.
                        MethodTree newMethod = make.Method(
                            methodModifiers, // public
                            "writeExternal", // writeExternal
                            make.PrimitiveType(TypeKind.VOID), // return type "void"
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            Collections.<VariableTree>singletonList(parameter), // final ObjectOutput arg0
                            Collections.<ExpressionTree>singletonList(throwsClause), // throws 
                            "{ throw new UnsupportedOperationException(\"Not supported yet.\") }", // body text
                            // make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
                            null // default value - not applicable here, used by annotations
                        );

                        // and in the same way as interface was added to implements clause,
                        // add feature to the class:
                        ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    // creates and adds annotation to class modifiers
    public void testAddAnnotation() throws FileStateInvalidException, IOException {
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial1.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        
                        // create the copy of the annotation list:
                        List<? extends AnnotationTree> oldAnnList = clazz.getModifiers().getAnnotations();
                        List<AnnotationTree> modifiedAnnList = new ArrayList<AnnotationTree>(oldAnnList);
                        
                        // create the new annotation and add it to the end
                        // of the list:
                        // Note:
                        // use make.QualIdent(e) for correct import/FQN handling.
                        AnnotationTree newAnnotation = make.Annotation(
                                make.Identifier("Override"), 
                                Collections.<ExpressionTree>emptyList()
                        );
                        modifiedAnnList.add(newAnnotation);
                        
                        // create new class modifiers, flags aren't changed,
                        // annotation is added to the end of annotation list.
                        ModifiersTree classModifiers = make.Modifiers(
                            clazz.getModifiers().getFlags(),
                            modifiedAnnList
                        );
                        // rewrite the original modifiers with the new one:
                        workingCopy.rewrite(clazz.getModifiers(), classModifiers);
                    }
                }
            }

        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    // obtain, modify and replace the method body as a text
    public void testForJean() throws FileStateInvalidException, IOException {
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial2.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                // we know there is exactly one class, named Tutorial2.
                // (ommiting test for kind corectness!)
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // get the method, there is a default constructor and
                // exactly one method, named demoMethod().
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree body = method.getBody();
                
                // get SourcePositions instance for your working copy and
                // fetch out start and end position.
                SourcePositions sp = workingCopy.getTrees().getSourcePositions();
                int start = (int) sp.getStartPosition(cut, body);
                int end = (int) sp.getEndPosition(cut, body);
                // get body text from source text
                String bodyText = workingCopy.getText().substring(start, end);
                MethodTree modified = make.Method(
                        method.getModifiers(), // copy original values
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        bodyText.replace("{0}", "-tag-replace-"), // replace body with the new text
                        null // not applicable here
                );
                // rewrite the original modifiers with the new one:
                 workingCopy.rewrite(method, modified);
            }

        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    // obtain, modify and replace the method body as a text
    public void testForErno() throws FileStateInvalidException, IOException {
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial2.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                // we know there is exactly one class, named Tutorial2.
                // (ommiting test for kind corectness!)
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // get the method, there is a default constructor and
                // exactly one method, named demoMethod().
                VariableTree var = make.Variable(make.Modifiers(
                        Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList()),
                        "myField",
                        make.Identifier("MyClass"),
                        make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(), 
                            make.MemberSelect(make.Identifier("Something"), "getMyClass"),
                            Collections.<ExpressionTree>emptyList()
                        )
                );
                // rewrite the original modifiers with the new one:
                 ClassTree copy = make.addClassMember(clazz, var);
                 workingCopy.rewrite(clazz, copy);
            }

        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    // obtain, modify and replace the method body as a text
    public void testMethodInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                // make the outer invocation, i.e. "clone()"
                MethodInvocationTree cloneInvocation = make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.Identifier("clone"), 
                        Collections.<ExpressionTree>emptyList()
                );
                
                // encapsulate 'toString' identifier to outer invocation
                MemberSelectTree toStringSelIdent = make.MemberSelect(cloneInvocation, "toString");
                // make 'toString()' invocation
                MethodInvocationTree toStringInvocation = make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        toStringSelIdent,
                        Collections.<ExpressionTree>emptyList()
                );
                // make statement from created expression
                ExpressionStatementTree statement = make.ExpressionStatement(toStringInvocation);
                
                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }
            
        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(testFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }

    // obtain, modify and replace the method body as a text
    public void testNullLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
//        final String statementText = "new Runnable() { };";
//        final String statementText = "System.err.println(\"Not interested in.\");";
        final String statementText = "System.err.println(null);";
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                SourcePositions[] positions = new SourcePositions[1];
                final TreeUtilities treeUtils = workingCopy.getTreeUtilities();
                StatementTree body = treeUtils.parseStatement(statementText, positions);
                System.err.println(TreeMakerDemo.reverse(body));
            }
            
        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(testFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    // not important for tutorial reasons.
    // please ignore.
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ConstructorTest/ConstructorTest/";
    }

}
