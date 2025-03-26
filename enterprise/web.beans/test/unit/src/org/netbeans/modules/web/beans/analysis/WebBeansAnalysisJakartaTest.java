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
package org.netbeans.modules.web.beans.analysis;

import java.io.IOException;

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class WebBeansAnalysisJakartaTest extends BaseAnalisysTestCase {


    public WebBeansAnalysisJakartaTest(String testName) {
        super(testName, true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.BaseAnalisysTestCase#createTask()
     */
    @Override
    protected WebBeansAnalysisTestTask createTask() {
        return new WebBeansAnalysisTestTask( getUtilities() );
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - ManagedBeansAnalizer
    //
    //=======================================================================
    /*
     * ManagedBeansAnalizer.checkCtor
     */
    public void testManagedBeansCtor() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz { "+
                " private Clazz(){} "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz1 { "+
                " public Clazz1( int i ){} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz2  { "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " @Qualifier1 "+
                " public class Clazz3  { "+
                " @Inject public Clazz3( String str ){} "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz");
                assertEquals( "Found unexpected errors", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz1");
                assertEquals( "Found unexpected errors", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    /*
     * ManagedBeansAnalizer.checkInner
     */
    public void testManagedBeansInner() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " public class Clazz { "+
                " @Qualifier1 "+
                " class Inner{} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " public class Clazz1  { "+
                " @Qualifier1 "+
                " static class Inner{} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result , "foo.Clazz.Inner");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ManagedBeansAnalizer.checkAbstract
     */
    public void testManagedBeansAbstract() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Qualifier1 "+
                " public abstract class Clazz { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.inject.Inject; "+
                " @Qualifier1 "+
                " @Decorator "+
                " public abstract class Clazz1  { "+
                " @Inject public Clazz1( @Qualifier1 @Delegate Clazz arg ){ "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result.getWarings() , "foo.Clazz");
                assertEquals( "Unxepected error found", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ManagedBeansAnalizer.checkImplementsExtension
     */
    public void testManagedBeansImplementsExtension() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.enterprise.inject.spi.Extension "+
                " @Qualifier1 "+
                " public class Clazz implements Extension { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz1  { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result.getWarings() , "foo.Clazz");
                assertEquals( "Unxepected error found", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - ScopedBeanAnalyzer
    //
    //=======================================================================

    /*
     * ScopedBeanAnalyzer.checkProxiability
     */
    public void testScopedProxiability() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope2.java",
                "package foo; " +
                " import jakarta.enterprise.context.NormalScope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @NormalScope "+
                " public @interface Scope2 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Scope2 "+
                " public final class Clazz { "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @Scope2 "+
                " public class Clazz1 { "+
                " public final void op(){} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " @Scope1 "+
                " public final class Clazz2  { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result , "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result.getWarings() , "foo.Clazz1", "op");
                assertEquals( "Unxepected error found", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ScopedBeanAnalyzer.checkPublicField
     */
    public void testScopedPublicField() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Scope1 "+
                " public class Clazz { "+
                " public int field; "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " public class Clazz1  { "+
                " public int field; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ScopedBeanAnalyzer.checkParameterizedBean
     */
    public void testScopedParameterizedBean() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Scope1 "+
                " public class Clazz<T> { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " public class Clazz1<T>  { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ScopedBeanAnalyzer.checkInterceptorDecorator
     */
    public void testScopedDecoratorInterceptor() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface Iface { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.interceptor.Interceptor; "+
                " @Scope1 "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.inject.Inject; "+
                " @Scope1 "+
                "@Decorator "+
                " public class Clazz2 implements Iface{ "+
                " @Delegate @Inject Iface delegate; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz1");
                assertEquals( 0, result.getErrors().size() );
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz2");
                assertEquals( 0, result.getErrors().size() );
            }

        };
        runAnalysis(errorFile1 , processor);

    }

    /*
     * ScopedBeanAnalyzer.checkPassivationCapable
     */
    public void testPassivation() throws IOException {
        getUtilities().initEnterprise();
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.enterprise.context.NormalScope; " +
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @NormalScope(passivating=true) "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.interceptor.Interceptor; "+
                " @Scope1 "+
                " public class Clazz1 { "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " @SessionScoped "+
                " public class Clazz2 { "+
                "}");

        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/SessionClazz.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.ejb.Singleton; "+
                " @SessionScoped "+
                " @Singleton "+
                " public class SessionClazz { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import java.io.Serializable; "+
                " @SessionScoped "+
                " public class Clazz3 implements Serializable { "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                " import jakarta.enterprise.context.RequestScoped; "+
                " @RequestScoped "+
                " public class Clazz4{ "+
                "}");

        FileObject goodFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/StatefulClazz.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import java.io.Serializable; "+
                " import jakarta.ejb.Stateful; "+
                " @SessionScoped "+
                " @Stateful "+
                " public class StatefulClazz  { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result, "foo.Clazz1");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result,"foo.Clazz2");
            }

        };
        runAnalysis(errorFile1 , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result,"foo.SessionClazz");
            }

        };
        runAnalysis(errorFile2 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile2, NO_ERRORS_PROCESSOR);
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - SessionBeanAnalyzer
    //
    //=======================================================================

    public void testSessionBean() throws IOException {
        getUtilities().initEnterprise();

        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.ejb.Singleton; "+
                " @Scope1 "+
                " @Singleton "+
                " public class Clazz { "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);
        errorFile.delete();

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.ejb.Stateless; "+
                " @Scope1 "+
                " @Stateless "+
                " public class Clazz1 { "+
                "}");
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz1");
            }

        };
        runAnalysis(errorFile1 , processor);
        errorFile1.delete();

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.enterprise.context.ApplicationScoped; "+
                "import jakarta.ejb.Singleton; "+
                " @Singleton "+
                " @ApplicationScoped "+
                " public class Clazz2  { "+
                "}");
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        goodFile.delete();

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import jakarta.ejb.Stateless; "+
                " @Stateless "+
                " public class Clazz3  { "+
                "}");
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
        goodFile1.delete();
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - InterceptedBeanAnalyzer
    //
    //=======================================================================

    public void testInterceptedBean() throws IOException {
        getUtilities().createInterceptorBinding("IBinding");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @IBinding "+
                " public class Clazz { "+
                " public final void method(){} "+
                " private final void privateMethod(){} "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @IBinding "+
                " public final class Clazz1 { "+
                " static void staticMethod(){} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " @IBinding "+
                " public class Clazz2  { "+
                " private final void privateMethod(){} "+
                " final static void staticMethod(){} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz1");
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - NamedModelAnalyzer
    //
    //=======================================================================

    public void testNamed() throws IOException {

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                " import jakarta.inject.Named; "+
                "@Named "+
                " public class SuperClass { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " import jakarta.inject.Named; "+
                " import jakarta.enterprise.inject.Specializes; "+
                "@Named "+
                "@Specializes "+
                " public class Clazz extends SuperClass { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass1.java",
                "package foo; " +
                " public class SuperClass1  { "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " import jakarta.inject.Named; "+
                " import jakarta.enterprise.inject.Specializes; "+
                "@Named "+
                "@Specializes "+
                " public class Clazz1 extends SuperClass1 { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    //=======================================================================
    //
    //  ClassModelAnalyzer - DeclaredIBindingsAnalyzer
    //
    //=======================================================================

    public void testIBindingsDuplication() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface IBinding1  {" +
                "    String value(); "+
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "@IBinding1(\"a\") "+
                "public @interface IBinding2  {} ");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @IBinding1(\"b\") @IBinding2 " +
                " public class Clazz { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @IBinding1(\"a\") @IBinding2 " +
                " public class Clazz1 { "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    //=======================================================================
    //
    //  FieldModelAnalyzer - ScopedFieldAnalyzer
    //
    //=======================================================================
    public void testScopedProducerField() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz<T>  { "+
                " @Produces @Scope1 T producerField; "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz1<T>  { "+
                " @Produces T producerField; "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " public class Clazz2<T>  { "+
                " private T field; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "producerField");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    /*
     * ScopedFieldAnalyzer.checkPassivationCapable
     */
    public void testPassivationProductionField() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FinalClass.java",
                "package foo; " +
                " public final class FinalClass { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz1 { "+
                " @Produces @SessionScoped FinalClass field; "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz2  { "+
                " @Produces @SessionScoped int field; "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                " import jakarta.enterprise.context.RequestScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz3  { "+
                " @Produces @RequestScoped FinalClass field; "+
                "}");

        FileObject goodFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                " import java.io.Serializable; "+
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz4  { "+
                " @Produces @SessionScoped Serializable field; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "field");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile2, NO_ERRORS_PROCESSOR);
    }

    //=======================================================================
    //
    //  FieldModelAnalyzer - InjectionPointAnalyzer
    //
    //=======================================================================

    /*
     * InjectionPointAnalyzer.checkInjectionPointMetadata
     */
    public void testInjectionPointMetadata() throws IOException {
        getUtilities().createQualifier("Qualifier1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.enterprise.inject.spi.InjectionPoint; "+
                "import jakarta.inject.Inject; "+
                " @Scope1 "+
                " public class Clazz  { "+
                " @Inject InjectionPoint injectionMeta; "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.spi.InjectionPoint; "+
                "import jakarta.inject.Inject; "+
                " public class Clazz1  { "+
                " @Inject InjectionPoint injectionMeta; "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.spi.InjectionPoint; "+
                "import jakarta.inject.Inject; "+
                " @Scope1 "+
                " public class Clazz2  { "+
                " @Inject @Qualifier1 InjectionPoint injectionMeta; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "injectionMeta");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, WARNINGS_PROCESSOR );
        runAnalysis( goodFile1, WARNINGS_PROCESSOR );
    }

    /*
     * InjectionPointAnalyzer.checkNamed
     */
    public void testNamedInjectionPoint() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanType.java",
                "package foo; " +
                " @Scope1 "+
                " public class BeanType  { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " import jakarta.inject.Named; "+
                " public class Clazz  { "+
                " @Inject @Named BeanType injectionPoint; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result.getWarings(), "foo.Clazz", "injectionPoint");
                assertEquals( 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

    }

    /*
     * InjectionPointAnalyzer.checkResult : typesafe resolution checks
     */
    public void testInjectableResult() throws IOException {
        getUtilities().createQualifier("Qualifier1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface Iface  { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/ImplClass.java",
                "package foo; " +
                " public class ImplClass  implements Iface { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz { "+
                " @Inject @Qualifier1 Iface injectioPoint; "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz1 { "+
                " @Inject Iface injectioPoint; "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.enterprise.context.spi.Context; "+
                " public class Clazz2 { "+
                " @Inject Context injectioPoint; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result.getWarings(), "foo.Clazz", "injectioPoint");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    /*
     * InjectionPointAnalyzer.checkResult : defenition errors ( DefinitionErrorResult impl )
     */
    public void testDefinitionErrorResult() throws IOException {
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz { "+
                " @Inject int injectioPoint=0; "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz1 { "+
                " static @Inject int injectioPoint; "+
                "}");

        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz2 { "+
                " final @Inject int injectioPoint; "+
                "}");

        FileObject errorFile3 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz3 { "+
                " @Produces @Inject int injectioPoint; "+
                "}");

        FileObject errorFile4 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz4<T> { "+
                " @Inject T injectioPoint; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "injectioPoint");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "injectioPoint");
            }

        };
        runAnalysis(errorFile1 , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz2", "injectioPoint");
            }

        };
        runAnalysis(errorFile2 , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz3", "injectioPoint");
            }

        };
        runAnalysis(errorFile3 , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz4", "injectioPoint");
            }

        };
        runAnalysis(errorFile4 , processor);
    }

    /*
     * InjectionPointAnalyzer.analyzeDecoratedBeans
     */
    public void testDecoratedBean() throws IOException {
        getUtilities().createQualifier("Qualifier1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface1.java",
                "package foo; " +
                " public interface Iface1 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public final class DecoratedBean1 implements Iface1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz implements Iface1 { "+
                " @Qualifier1 @Inject @Delegate Iface1 delegate; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface2.java",
                "package foo; " +
                " public interface Iface2 { "+
                " void method();"+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean2.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class DecoratedBean2 implements Iface2 { "+
                " public final void method() {  } "+
                " private final void op() { } "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz1 implements Iface2 { "+
                " @Qualifier1 @Inject @Delegate Iface2 delegate; "+
                " public void method() {  } "+
                " private final void op() {  } "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface3.java",
                "package foo; " +
                " public interface Iface3 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean3.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " @Qualifier1 "+
                " public class DecoratedBean3 implements Iface3 { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz2 implements Iface3 { "+
                " @Qualifier1 @Inject @Delegate Iface3 delegate; "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "delegate");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "delegate");
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );

    }

    //=======================================================================
    //
    //  MethodModelAnalyzer - ScopedMethodAnalyzer
    //
    //=======================================================================
    public void testScopedProducerMethod() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz<T>  { "+
                " @Produces @Scope1 T producerMethod(){ return null; } "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz1<T>  { "+
                " @Produces T producerMethod(){ return null; } "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " public class Clazz2<T>  { "+
                " private T method(){ return null; } "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz", "producerMethod");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    /*
     * ScopedMethodAnalyzer.checkPassivationCapable
     */
    public void testPassivationProductionMethod() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FinalClass.java",
                "package foo; " +
                " public final class FinalClass { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz1 { "+
                " @Produces @SessionScoped FinalClass method(){ return null; } "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz2  { "+
                " @Produces @SessionScoped int method(){ return 0; } "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                " import jakarta.enterprise.context.RequestScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz3  { "+
                " @Produces @RequestScoped FinalClass method(){ return null; } "+
                "}");

        FileObject goodFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                " import java.io.Serializable; "+
                " import jakarta.enterprise.context.SessionScoped; "+
                " import jakarta.enterprise.inject.Produces; "+
                " public class Clazz4  { "+
                " @Produces @SessionScoped Serializable method(){ return null; } "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "method");
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR);
        runAnalysis( goodFile2, NO_ERRORS_PROCESSOR);
    }

    //=======================================================================
    //
    //  MethodModelAnalyzer - InjectionPointParameterAnalyzer
    //
    //=======================================================================

    /*
     * InjectionPointParameterAnalyzer.checkResult : typesafe resolution checks
     */
    public void testInjectableParamResult() throws IOException {
        getUtilities().createQualifier("Qualifier1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface Iface  { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/ImplClass.java",
                "package foo; " +
                " public class ImplClass  implements Iface { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz { "+
                " @Inject void method(@Qualifier1 Iface injectionPoint ){} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " public class Clazz1 { "+
                " @Inject void method(Iface injectioPoint){} "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.enterprise.context.spi.Context; "+
                " public class Clazz2 { "+
                " @Inject void method( Context injectioPoint) {} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement( result.getWarings(), "foo.Clazz", "method",
                        "injectionPoint");
                assertEquals( "Found unexpected errors", 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }

    /*
     * InjectionPointParameterAnalyzer.checkResult : defenition errors ( DefinitionErrorResult impl )
     */
    public void testParamDefinitionErrorResult() throws IOException {

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.enterprise.inject.Produces; "+
                " public class Clazz1<T> { "+
                " @Inject void method( T injectioPoint ){} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz1", "method", "injectioPoint");
            }

        };
        runAnalysis(errorFile , processor);

    }

    /*
     * InjectionPointParameterAnalyzer.checkName
     */
    public void testNamedParameter() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanType.java",
                "package foo; " +
                " public class BeanType { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " import jakarta.inject.Named; "+
                " public class Clazz1 { "+
                " @Inject void method( @Named BeanType injectionPoint ){} "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " import jakarta.inject.Named; "+
                " public class Clazz2 { "+
                " @Inject void method( @Named(\"paramName\") BeanType injectionPoint ){} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz1", "method", "injectionPoint");
                checkParamElement(result.getWarings(), "foo.Clazz1", "method",
                        "injectionPoint");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result.getWarings(), "foo.Clazz2", "method",
                        "injectionPoint");
                assertEquals( 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile1 , processor);

    }

    /*
     * InjectionPointAnalyzer.analyzeDecoratedBeans
     */
    public void testParamDecoratedBean() throws IOException {
        getUtilities().createQualifier("Qualifier1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface1.java",
                "package foo; " +
                " public interface Iface1 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public final class DecoratedBean1 implements Iface1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz implements Iface1 { "+
                " @Inject void init( @Qualifier1 @Delegate Iface1 delegate){} "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface2.java",
                "package foo; " +
                " public interface Iface2 { "+
                " void method();"+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean2.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class DecoratedBean2 implements Iface2 { "+
                " public final void method() {  } "+
                " private final void op() { } "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz1 implements Iface2 { "+
                " @Inject void init ( @Qualifier1 @Delegate Iface2 delegate) {} "+
                " public void method() {  } "+
                " private final void op() {  } "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface3.java",
                "package foo; " +
                " public interface Iface3 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/DecoratedBean3.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                " @Qualifier1 "+
                " public class DecoratedBean3 implements Iface3 { "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import jakarta.inject.Inject; "+
                "import jakarta.decorator.Delegate; "+
                "import jakarta.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz2 implements Iface3 { "+
                " @Inject void init( @Qualifier1 @Delegate Iface3 delegate){} "+
                "}");

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz", "init", "delegate");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz1", "init", "delegate");
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );

    }

    //=======================================================================
    //
    //  MethodModelAnalyzer - InterceptedMethodAnalyzer
    //
    //=======================================================================
    public void testInterceptedMethod() throws IOException {
        getUtilities().initEnterprise();
        getUtilities().createInterceptorBinding("IBinding1");

        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+
                "public @interface IBinding2  {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/ICeptor.java",
                "package foo; " +
                " import jakarta.interceptor.Interceptor; "+
                " @Interceptor @IBinding1 "+
                " public class ICeptor { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/ICeptor1.java",
                "package foo; " +
                " import jakarta.interceptor.Interceptor; "+
                " @Interceptor @IBinding2 "+
                " public class ICeptor1 { "+
                "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import jakarta.ejb.PostActivate; "+
                " public class Clazz { "+
                " @IBinding1 "+
                " @PostActivate "+
                " public void method(){} "+
                "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " public class Clazz1 { "+
                " @IBinding1 "+
                " public final void method(){} "+
                "}");

        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " public final class Clazz2 { "+
                " @IBinding1 "+
                " public void method(){} "+
                "}");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import jakarta.ejb.PrePassivate; "+
                " @IBinding2 "+
                " public class Clazz3 { "+
                " @PrePassivate "+
                " public void method(){} "+
                " @IBinding1 "+
                " private final void method1(){} "+
                "}");

        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                " public final class Clazz4 { "+
                " public void method(){} "+
                "}");

        FileObject goodFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/ICeptor2.java",
                "package foo; " +
                " import jakarta.interceptor.Interceptor; "+
                " @Interceptor @IBinding2 "+
                " public final class ICeptor2 { "+
                " public final void method(){} "+
                "}");


        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result.getWarings(), "foo.Clazz", "method");
                checkMethodElement(result, "foo.Clazz", "method");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "method");
                assertEquals( "Unexpected warings found", 0, result.getWarings().size());
            }

        };
        runAnalysis(errorFile1 , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "method");
                assertEquals( "Unexpected warings found", 0, result.getWarings().size());
            }

        };
        runAnalysis(errorFile2 , processor);

        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
        runAnalysis(goodFile1, NO_ERRORS_PROCESSOR);
        runAnalysis(goodFile2, NO_ERRORS_PROCESSOR);
    }

    //=======================================================================
    //
    //  AnnotationModelAnalyzer - StereotypeAnalyzer
    //
    //=======================================================================

    /*
     * StereotypeAnalyzer.analyzeScope
     */
    public void testScopedStereotype() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Scope2.java",
                "package foo; " +
                " import jakarta.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope2 { "+
                "}");

        FileObject errorFile =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Scope1 @Scope2 "+
                "public @interface Stereotype1 {}" );

        FileObject errorFile1 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Stereotype3 @Stereotype4 "+
                "public @interface Stereotype2 {}" );

        FileObject goodFile =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype3.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Scope1 "+
                "public @interface Stereotype3 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype4.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Scope2 "+
                "public @interface Stereotype4 {}" );

        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype1");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype2");
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }

    /*
     * StereotypeAnalyzer. checkName, checkDefinition, checkInterceptorBindings, checkTransitiveStereotypes
     */
    public void testStereotype() throws IOException {
        getUtilities().createInterceptorBinding("IBinding1");
        FileObject errorFile =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.Named; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Named(\"name\") " +
                "public @interface Stereotype1 {}" );

        FileObject errorFile1 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.SOURCE; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(SOURCE) "+
                "@Stereotype "+
                "public @interface Stereotype2 {}" );

        FileObject errorFile2 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype3.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "public @interface Stereotype3 {}" );

        FileObject errorFile3 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype4.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @IBinding1 "+
                "public @interface Stereotype4 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/TypeStereotype.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.Named; "+
                "@Target({TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "public @interface TypeStereotype {}" );

        FileObject errorFile4 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype5.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @TypeStereotype "+
                "public @interface Stereotype5 {}" );

        FileObject goodFile =  TestUtilities.copyStringToFileObject(srcFO, "foo/GoodStereotype1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.Named; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Named " +
                "public @interface GoodStereotype1 {}" );

        FileObject goodFile1 =  TestUtilities.copyStringToFileObject(srcFO, "foo/GoodStereotype2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.Named; "+
                "@Target({TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                " @Named " +
                " @IBinding1 "+
                "public @interface GoodStereotype2 {}" );

        ResultProcessor processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype1");
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype2");
            }

        };
        runAnalysis(errorFile1 , processor);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype3");
            }

        };
        runAnalysis(errorFile2 , processor);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype4");
            }

        };
        runAnalysis(errorFile3 , processor);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Stereotype5");
            }

        };
        runAnalysis(errorFile4 , processor);

        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
        runAnalysis(goodFile1, NO_ERRORS_PROCESSOR);
    }

    /*
     * StereotypeAnalyzer. checkQualifiers, checkTyped
     */
    public void testQualifiedTypedStereotype() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "@Qualifier1 "+
                "public @interface Stereotype1 {}" );

        FileObject errorFile1 =  TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.enterprise.inject.Typed; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "@Typed({}) "+
                "public @interface Stereotype2 {}" );

        ResultProcessor processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Stereotype1");
                assertEquals( 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile , processor);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Stereotype2");
                assertEquals( 0, result.getErrors().size());
            }

        };
        runAnalysis(errorFile1 , processor);

    }

    //=======================================================================
    //
    //  AnnotationModelAnalyzer - InterceptorBindingAnalyzer
    //
    //=======================================================================

    public void testInterceptorBinding() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/TypeBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+
                "public @interface TypeBinding  {} ");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.SOURCE; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(SOURCE) "+
                "@Target({TYPE, METHOD}) "+
                "public @interface IBinding1  {} ");

        FileObject goodFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD}) "+
                "public @interface IBinding2  {} ");

        FileObject errorFile3 = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding3.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({TYPE, METHOD}) "+
                " @TypeBinding "+
                "public @interface IBinding3  {} ");

        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding4.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+
                " @TypeBinding "+
                "public @interface IBinding4  {} ");

        ResultProcessor processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.IBinding1");
            }

        };
        runAnalysis(errorFile1 , processor);

        runAnalysis(goodFile2 , NO_ERRORS_PROCESSOR);

        processor = new ResultProcessor (){
            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.IBinding3");
            }

        };
        runAnalysis(errorFile3 , processor);

        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
}
