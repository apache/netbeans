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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class CdiAnalysisTest extends BaseAnalisysTestCase {

    /**
     * @param testName
     */
    public CdiAnalysisTest( String testName ) {
        super(testName, false);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.BaseAnalisysTestCase#createTask()
     */
    @Override
    protected CdiAnalysisTestTask createTask() {
        return new CdiAnalysisTestTask();
    }
    
    /*
     * TypedClassAnalizer
     */
    public void testTypedClass() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                "@Typed({List.class}) "+
                " public class Clazz { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Typed({Comparable.class}) "+
                " public class Clazz1 implements Comparable<String> { "+
                " public int comapreTo( String str ) {"+
                "   return 0; "+
                " }"+
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
     * AnnotationsAnalyzer(ClassAnalyzer) checkDecoratorInterceptor
     */
    public void testAnnotationsDecoratorInterceptor() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.interceptor.Interceptor; "+
                "@Decorator "+
                "@Interceptor "+
                " public class Clazz { "+
                "}");
        
        FileObject goodFile =TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "@Interceptor "+
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
    
    /*
     * AnnotationsAnalyzer(ClassAnalyzer) checkDelegateInjectionPoint
     */
    public void testDecoratorDelegate() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.decorator.Decorator; "+
                "import javax.inject.Inject; "+
                "@Decorator "+
                " public class Clazz { "+
                " @Inject int injectionPoint; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                " public class Clazz1 { "+
                " public Clazz1( @Delegate Object arg ){ "+
                " }"+
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
     * AnnotationsAnalyzer(ClassAnalyzer) checkProducerFields
     */
    public void testDecoratorProducerField() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.enterprise.inject.Produces; "+
                "@Decorator "+
                " public class Clazz { "+
                " @Produces int production; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                "@Decorator "+
                " public class Clazz1 { "+
                "  @Inject @Delegate Object injection; "+
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
     * AnnotationsAnalyzer(ClassAnalyzer) checkMethods
     */
    public void testInterceptorMethods() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Produces; "+
                "@Interceptor "+
                " public class Clazz { "+
                " @Produces int production(){ return 0 } ; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Produces; "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "  int method(){ return 0;} "+
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
     * AnnotationsAnalyzer(ClassAnalyzer) checkSession
     */
    public void testInterceptorSessionBeans() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.ejb.Singleton; "+
                "@Interceptor "+
                "@Singleton "+
                " public class Clazz { "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "@Interceptor "+
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
    
    /*
     * AnnotationsAnalyzer(ClassAnalyzer) checkNamed , checkAlternatives, checkSpecializes
     */
    public void testInterceptorAlternativeNamed() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                " import javax.inject.Named; "+
                "@Named "+
                "@Interceptor "+
                " public class Clazz { "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Alternative; "+
                "@Alternative "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Specializes; "+
                "@Interceptor "+
                "@Specializes "+
                " public class Clazz2 { "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz");
                assertEquals( 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz1");
                assertEquals( 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz2");
                assertEquals( 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile2 , processor);
    }
    
    /*
     * CtorsAnalyzer
     */
    public void testInitializerCtors() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz { "+
                " @Inject public Clazz( int i){} "+
                " @Inject public Clazz( String str ){} "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz1 { "+
                " @Inject public Clazz1( int i){} "+
                " public Clazz1( Stirng str ){} "+
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
     * TypedFieldAnalyzer
     */
    public void testTypedField() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                " public class Clazz { "+
                " @Typed({List.class}) Object field; "+
                " int field1; "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import java.util.List; "+
                "import java.util.Collection; "+
                " public class Clazz1  { "+
                " @Typed({Collection.class}) List<String> field; "+
                " int field1; "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "field");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * DelegateFieldAnalizer 
     */
    public void testDelegateField() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface Iface  { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                " @Decorator "+
                " public class Clazz  implements Iface { "+
                " @Inject @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                " @Decorator "+
                " public class Clazz1  implements Iface  { "+
                " @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                " public class Clazz2  implements Iface  { "+
                " @Inject @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile3 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                " @Decorator "+
                " public class Clazz3 implements Iface { "+
                " @Inject @Delegate Object delegateInjection; "+
                " int field1; "+
                "}");
        
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "delegateInjection",
                        true);
                Set<Element> elements = result.getErrors().keySet();
                assertEquals( "Exactly two errors should be detected" , 
                        2, elements.size());
                boolean clazzFound = false;
                for (Element element : elements) {
                    if ( element instanceof  TypeElement ){
                        String fqn = ((TypeElement)element).
                                getQualifiedName().toString();
                        if ( fqn.equals("foo.Clazz1")){
                            clazzFound = true;
                        }
                    }
                }
                assertTrue("foo.Clazz1 should be marked with an error ",clazzFound);
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz2", "delegateInjection");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz3", "delegateInjection");
            }
            
        };
        runAnalysis(errorFile3 , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    /*
     * ProducerFieldAnalyzer : checkSessionBean
     */
    public void testProductionFieldInSession() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.ejb.Singleton; "+
                "@Singleton "+
                " public class Clazz { "+
                " @Produces int production; "+
                " int field1; "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.ejb.Singleton; "+
                "@Singleton "+
                " public class Clazz1  { "+
                " static @Produces int production; "+
                " int field1; "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "production");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ProducerFieldAnalyzer : checkType
     */
    public void testProductionFieldType() throws IOException{
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz  { "+
                " static @Produces Class<String> production; "+
                " int field1; "+
                "}");
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz1<T> { "+
                " @Produces T production; "+
                " int field1; "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz2 { "+
                " @Produces Class<? extends String> production; "+
                " int field1; "+
                "}");
        
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "production");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz2", "production");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * TypedMethodAnalyzer
     */
    public void testTypedMethod() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                " public class Clazz { "+
                " @Typed({List.class}) Object method(){ return null; } "+
                " int operation(){ return 0; } "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import java.util.List; "+
                "import java.util.Collection; "+
                " public class Clazz1  { "+
                " @Typed({Collection.class}) List<String> method(){ return null; }; "+
                " int operation(){ return 0; }  "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz", "method");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * AnnotationsAnalyzer : combinations of various CDI annotations: inject, producer, observer, disposes
     */
    public void testMethodAnnotations() throws IOException {
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz.java", "package foo; "
                        + "import javax.inject.Inject; "
                        + "import javax.enterprise.event.Observes; "
                        + "import javax.enterprise.inject.Produces; "
                        +" import javax.enterprise.inject.Disposes; "
                        + " public class Clazz { "
                        + " @Inject int initializer( int arg ) { return 0; } " 
                        + " @Produces String production(){return null; } ; "
                        + " void observer( @Observes String event ){} ; "
                        + " void disposer( @Disposes int arg ){} "
                        + "}");
        
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz1.java", 
                "package foo; "
                + "import javax.inject.Inject; "
                + "import javax.enterprise.inject.Produces; "
                + " public class Clazz1 { "
                + " @Inject @Produces int badProduction( int arg ){ return 0; } "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz2.java", 
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "import javax.enterprise.inject.Produces; "
                + " public class Clazz2 { "
                + " @Produces int badProduction( @Observes String event) { return 0; }"+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz3.java", 
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                +" import javax.enterprise.inject.Disposes; "
                + " public class Clazz3 { "
                + " int badObserver( @Disposes @Observes String event) { return 0; }"+
                " void  method(){}  "
                + "}");

        
        ResultProcessor processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "badProduction");
            }

        };
        runAnalysis(errorFile, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "badProduction");
            }

        };
        runAnalysis(errorFile1, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "badObserver");
            }

        };
        runAnalysis(errorFile2, processor);


        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
    
    /*
     * AnnotationsAnalyzer: checkAbstractMethod
     */
    public void testAbstractMethod() throws IOException {
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz.java", "package foo; "
                        + "import javax.inject.Inject; "
                        + "import javax.enterprise.inject.Produces; "
                        + " import javax.enterprise.inject.Disposes; "
                        + " public class Clazz { "
                        + " @Produces String production(){return null; } ; "
                        + " void disposer( @Disposes int arg ){} " + "}");

        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz1.java", "package foo; "
                        + "import javax.inject.Inject; "
                        + "import javax.enterprise.inject.Produces; "
                        + " public class Clazz1 { "
                        + " @Produces  abstract int badProduction( int arg ); "
                        + " void  method(){}  " + "}");

        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz2.java", "package foo; "
                        + " import javax.enterprise.inject.Disposes; "
                        + " public class Clazz2 { "
                        + " abstract int badDisposer( @Disposes String arg);"
                        + " void  method(){}  " + "}");

        ResultProcessor processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "badProduction");
            }

        };
        runAnalysis(errorFile, processor);

        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "badDisposer");
            }

        };
        runAnalysis(errorFile1, processor);

        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
    
  /*
   * AnnotationsAnalyzer: checkBusinessMethod
   */
    public void testBusinessAnnotations() throws IOException {
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
            "foo/Clazz.java", "package foo; "
                    + "import javax.inject.Inject; "
                    + "import javax.enterprise.inject.Produces; "
                    +" import javax.enterprise.inject.Disposes; "
                    + "import javax.enterprise.event.Observes; "
                    + "import javax.ejb.Stateful; "
                    + " @Stateful "
                    + " public class Clazz { "
                    + " public @Produces String production(){return null; } ; "
                    + " public void disposer( @Disposes int arg ){} "
                    + " public void observer( @Observes String event  ){} "
                    + "}");
    
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz1.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                + "import javax.enterprise.inject.Produces; "
                + " @Stateful "
                + " public class Clazz1 { "
                + " @Produces  int notBusiness( int arg ){ return 0;}  "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz2.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                +" import javax.enterprise.inject.Disposes; "
                + " @Stateful "
                + " public class Clazz2 { "
                + " public final void notBusiness( @Disposes String arg){ } "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz3.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                + "import javax.enterprise.event.Observes; "
                + "import javax.ejb.PostActivate; "
                + " @Stateful "
                + " public class Clazz3 { "
                + " @PostActivate public void lifecycle( @Observes String event){ } "+
                " void  method(){}  "
                + "}");
      
        
        ResultProcessor processor = new ResultProcessor() {
      
            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "notBusiness");
            }
      
        };
        runAnalysis(errorFile, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "notBusiness");
            }

        };
        runAnalysis(errorFile1, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "lifecycle");
            }

        };
        runAnalysis(errorFile2, processor);
    
        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
    
    /*
     * AnnotationsAnalyzer: initializers check
     */
    public void testInitializers() throws IOException{
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz { "+
                " @Inject public initMethod( int i){} "+
                "}");
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz1 { "+
                " @Inject abstract public void badInit(); "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz2 { "+
                " @Inject static public void badInit() {}  "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz3 { "+
                " @Inject public <T> void badInit( Class<T> clazz ) {}  "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "badInit");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "badInit");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "badInit");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * DelegateMethodAnalyzer: checkMethodDefinition, checkClassDefinition, checkDelegateType
     */
    public void testDelegateMethod() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface  Iface { "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Decorated.java",
                "package foo; " +
                " public class  Decorated implements Iface { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Delegate; "+
                "import javax.decorator.Decorator; "+
                "import javax.inject.Inject; "+
                " @Decorator "+
                " public class Clazz implements Iface { "+
                " @Inject  public void initMethod( @Delegate Iface obj ){} "+
                "}");
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                "import javax.decorator.Delegate; "+
                "import javax.decorator.Decorator; "+
                " @Decorator "+
                " public class Clazz1 implements Iface { "+
                " public void badDelegate( @Delegate Iface obj ){} "+
                "}");
        
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                "import javax.decorator.Delegate; "+
                " public class Clazz2 implements Iface { "+
                " @Inject public void badDelegate( @Delegate Iface obj ){} "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                " @Decorator "+
                " public class Clazz3 implements Iface { "+
                " @Inject public void badDelegate( @Delegate Object clazz ) {}  "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                Map<Element, String> errors = result.getErrors();
                boolean classFound = false;
                boolean parameterFound = false;
                assertEquals( "Expected exactly two error elements" , 2, errors.keySet().size());
                for( Element element : errors.keySet() ){
                    if ( element instanceof TypeElement ) {
                        classFound = ((TypeElement)element).getQualifiedName().
                            contentEquals("foo.Clazz1");
                    }
                    else if ( element instanceof VariableElement ){
                        parameterFound = element.getSimpleName().contentEquals("obj");
                        if ( parameterFound ){
                            parameterFound = element.getEnclosingElement().getSimpleName().
                            contentEquals("badDelegate");
                        }
                    }
                }
                assertTrue( "Clazz1 is expected as error element context",classFound );
                assertTrue( "parameter 'obj' of method 'badDelegate'  is expected " +
                		"as error element context",classFound );
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz2", "badDelegate", "obj");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkParamElement(result, "foo.Clazz3", "badDelegate", "clazz");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ProducerMethodAnalyzer : checkType, checkSpecializes
     */
    public void testProducerMethod() throws IOException{
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz  { "+
                " static @Produces Class<String> productionMethod(){ return null; } "+
                " void  operation(){} "+
                "}");
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz1<T> { "+
                " @Produces T productionMethod(){ return null; } "+
                " void  operation(){} "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz2 { "+
                " @Produces Class<? extends String> productionMethod(){ return null; } "+
                " void  operation(){} "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.enterprise.inject.Specializes; "+
                " public class Clazz3 { "+
                " static @Specializes @Produces String productionMethod(){ return null; } "+
                " @Produces String productionMethod1(){ return null; } "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class SuperClass { "+
                " String nonProduction(){ return null; } "+
                " @Produces String superProduction(){ return null; } "+
                "}");
        
        FileObject errorFile3 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.enterprise.inject.Specializes; "+
                " public class Clazz4 extends SuperClass{ "+
                " @Specializes @Produces String nonProduction(){ return null; } "+
                " @Specializes @Produces String superProduction(){ return null; } "+
                "}");
        
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "productionMethod");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "productionMethod");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "productionMethod");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz4", "nonProduction");
            }
            
        };
        runAnalysis(errorFile3 , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * org.netbeans.modules.web.beans.analysis.analyzer.CtorAnalyzer
     */
    public void testCtor() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Disposes; "+
                " public class Clazz { "+
                " public Clazz( int i){} "+
                " public Clazz( @Disposes String str ){} "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.event.Observes; "+
                " public class Clazz1 { "+
                " public Clazz1( int i){} "+
                " public Clazz1( @Observes String str ){} "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz2 { "+
                " public Clazz2( Stirng str ){} "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkCtor(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkCtor(result, "foo.Clazz1");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ScopeAnalyzer
     */
    public void testScope() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Scope1.java",
                "package foo; " +
                " import javax.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.ANNOTATION_TYPE}) "+
                " @Scope "+
                " public @interface Scope1 { "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Scope2.java",
                "package foo; " +
                " import javax.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope2 { "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Scope3.java",
                "package foo; " +
                " import javax.inject.Scope; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Scope "+
                " public @interface Scope3 { "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Scope1");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Scope2");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * QualifierAnalyzer 
     */
    public void testQualifier() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier1.java",
                "package foo; " +
                " import javax.inject.Qualifier; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.ANNOTATION_TYPE}) "+
                " @Qualifier "+
                " public @interface Qualifier1 { "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier2.java",
                "package foo; " +
                " import javax.inject.Qualifier; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, ElementType.TYPE}) "+
                " @Qualifier "+
                " public @interface Qualifier2 { "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier3.java",
                "package foo; " +
                " import javax.inject.Qualifier; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Target({ElementType.FIELD, ElementType.PARAMETER}) "+
                " @Qualifier "+
                " public @interface Qualifier3 { "+
                "}");
        
        /*
         * Create a good class files
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier4.java",
                "package foo; " +
                " import javax.inject.Qualifier; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
                " @Qualifier "+
                " public @interface Qualifier4 { "+
                "}");
        
        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier5.java",
                "package foo; " +
                " import javax.inject.Qualifier; "+
                " import java.lang.annotation.Retention; "+
                " import java.lang.annotation.RetentionPolicy; "+
                " import java.lang.annotation.Target; " +
                " import java.lang.annotation.ElementType; "+
                " @Retention(RetentionPolicy.RUNTIME) "+
                " @Target({ElementType.FIELD, ElementType.PARAMETER}) "+
                " @Qualifier "+
                " public @interface Qualifier5 { "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Qualifier1");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Qualifier2");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Qualifier3");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }
    
   /*
   * QualifierAnalyzer checkMembers()
   */
  public void testQualifierMembers() throws IOException{
      FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier1.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              " @Qualifier "+
              " public @interface Qualifier1 { "+
              " String value(); "+
              " String[] comments(); "+
              "}");
      
      FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier2.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              " @Qualifier "+
              " public @interface Qualifier2 { "+
              " String value(); "+
              " Qualifier1 qualifier(); "+
              "}");
      
      FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Qualifier3.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " import javax.enterprise.util.Nonbinding; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              " @Qualifier "+
              " public @interface Qualifier3 { "+
              " String value(); "+
              " @Nonbinding "+
              " Qualifier1 qualifier(); "+
              " @Nonbinding "+
              " String[] comments(); "+
              "}");
      
      ResultProcessor processor = new ResultProcessor (){

          @Override
          public void process( TestProblems result ) {
              checkTypeElement(result.getWarings(), "foo.Qualifier1");
              assertEquals( 0 , result.getErrors().size());
          }
          
      };
      runAnalysis(errorFile , processor);
      
      processor = new ResultProcessor (){

          @Override
          public void process( TestProblems result ) {
              checkTypeElement(result.getWarings(), "foo.Qualifier2");
              assertEquals( 0 , result.getErrors().size());
          }
          
      };
      runAnalysis(errorFile1 , processor);
      
      runAnalysis( goodFile, NO_ERRORS_PROCESSOR);
  }
  
  /*
   * InterceptorBindingMembersAnalyzer checkMembers()
   */
  public void testIBindingMembers() throws IOException{
      FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding1.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " import javax.interceptor.*; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              "@InterceptorBinding " +
              " public @interface IBinding1 { "+
              " String value(); "+
              " String[] comments(); "+
              "}");
      
      FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " import javax.interceptor.*; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              "@InterceptorBinding " +
              " public @interface IBinding2 { "+
              " String value(); "+
              " IBinding1 qualifier(); "+
              "}");
      
      FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding3.java",
              "package foo; " +
              " import javax.inject.Qualifier; "+
              " import java.lang.annotation.Retention; "+
              " import java.lang.annotation.RetentionPolicy; "+
              " import java.lang.annotation.Target; " +
              " import java.lang.annotation.ElementType; "+
              " import javax.enterprise.util.Nonbinding; "+
              " import javax.interceptor.*; "+
              " @Retention(RetentionPolicy.RUNTIME) "+
              " @Target({ElementType.METHOD,ElementType.FIELD, " +
                "ElementType.PARAMETER, ElementType.TYPE}) "+
              "@InterceptorBinding " +
              " public @interface IBinding3 { "+
              " String value(); "+
              " @Nonbinding "+
              " Qualifier1 qualifier(); "+
              " @Nonbinding "+
              " String[] comments(); "+
              "}");
      
      ResultProcessor processor = new ResultProcessor (){

          @Override
          public void process( TestProblems result ) {
              checkTypeElement(result.getWarings(), "foo.IBinding1");
              assertEquals( 0 , result.getErrors().size());
          }
          
      };
      runAnalysis(errorFile , processor);
      
      processor = new ResultProcessor (){

          @Override
          public void process( TestProblems result ) {
              checkTypeElement(result.getWarings(), "foo.IBinding2");
              assertEquals( 0 , result.getErrors().size());
          }
          
      };
      runAnalysis(errorFile1 , processor);
      
      runAnalysis( goodFile, NO_ERRORS_PROCESSOR);
  }
    
}
