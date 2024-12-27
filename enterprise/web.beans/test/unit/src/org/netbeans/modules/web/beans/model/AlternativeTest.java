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
package org.netbeans.modules.web.beans.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class AlternativeTest extends CommonTestCase {

    public AlternativeTest( String testName ) {
        super(testName, false);
    }

    public void testAlternativeDisabled() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml", 
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<alternatives>" +
                "</alternatives> " +
                "</beans>");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");

        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding1 "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "@Alternative "+
                "public class Three extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "@Binding1 @Binding2 "+
                "public class Two1 extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "@Stereotype1 "+
                "public class Four extends One1{}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Five.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 @Binding1 "+
                "@Stereotype2 "+
                "public class Five extends One1{}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "@Stereotype1 "+
                "public @interface Stereotype2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 One myField1; "+
                " @Inject @Binding1 One1 myField2; "+
                " @Inject @Binding1 @Binding2 One1 myField3; "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        check1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        check2( element , model);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        check3( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }
        });
    }
    
    public void testAlternativeEnabled() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml", 
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<alternatives>" +
                    "<class>foo.Three</class> "+
                    "<stereotype>foo.Stereotype1</stereotype> "+
                "</alternatives> " +
                "</beans>");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding1 "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 @Binding2 "+
                "@Alternative "+
                "public class Three extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "@Binding2 "+
                "@Stereotype3 "+
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 "+
                "@Stereotype1 "+
                "public class Four extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Five.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 @Binding1 "+
                "@Stereotype2 "+
                "public class Five extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 @Binding2 One myField1; "+
                " @Inject @Binding2 One1 myField2; "+
                " @Inject @Binding2 @Binding1 One1 myField3; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "@Stereotype1 "+
                "public @interface Stereotype2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype3.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype3 {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        checkEnabled1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkEnabled2( element , model);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        checkEnabled3( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }

        });
    }
    
    public void testMixedAlternativeStereotype() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml", 
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<alternatives>" +
                    "<class>foo.Three</class> "+
                    "<stereotype>foo.Stereotype1</stereotype> "+
                "</alternatives> " +
                "</beans>");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                " @Alternative "+
                " @Stereotype1 " +
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding1 "+
                " @Stereotype1 "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "@Alternative "+
                " @Stereotype1 "+
                "public class Three {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 One myField1; "+
                " @Inject @Binding1 Three myField2; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Stereotype "+
                "@Alternative "+
                "public @interface Stereotype2 {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        checkMixed1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkMixed2( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }

        });
    }
    
    public void testProductionAlternatives() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml", 
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<alternatives>" +
                    "<class>foo.One</class> "+
                    "<stereotype>foo.Stereotype1</stereotype> "+
                "</alternatives> " +
                "</beans>");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                " @Alternative "+
                "public class One {" +
                " @Alternative @Produces @Binding1 int myField1; "+
                " @Alternative @Stereotype2 @Produces @Binding1  String myField2; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class Two {" +
                " @Stereotype1 @Produces @Binding1 String myField1; "+
                " @Stereotype2 @Produces @Binding1 One myField2; "+
                " @Alternative @Produces @Binding1 Two myField3; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 int myField1; "+
                " @Inject @Binding1 String myField2; "+
                " @Inject @Binding1 One myField3; "+
                " @Inject @Binding1 Two myField4; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype2 {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        checkProduction1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkProduction2( element , model);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        checkProduction3( element , model);
                    }
                    else if ( element.getSimpleName().contentEquals("myField4")){
                        checkProduction4( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                assert names.contains("myField4");
                return null;
            }

        });
    }
    
    protected void checkMixed1( VariableElement element, WebBeansModel model ) {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement)injectable).getQualifiedName().toString();  
        
        assertEquals( "foo.Two", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 2 , typeElements.size());
        
        boolean oneFound = false;
        boolean twoFound = false;
        TypeElement one = null;
        TypeElement two = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.One".equals(typeName)){
                oneFound = true;
                one = typeElement;
            }
            if ( "foo.Two".equals( typeName)){
                twoFound = true;
                two = typeElement;
            }
        }
        
        assertTrue( "myField2 defined in class foo.One should be available " +
                "via ApplicableResult interface", oneFound );
        
        assertTrue( "myField1 defined in class foo.Two should be available " +
                "via ApplicableResult interface", twoFound );
        
        assertTrue ( "myField2 in foo.One is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( one ));
        assertTrue ( "myField1 in foo.Two is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( two ));
        
        assertTrue( "myField2 in foo.One should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(one));
        assertFalse( "myField1 in foo.Two should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(two));
    }
    
    protected void checkMixed2( VariableElement element, WebBeansModel model ) {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement)injectable).getQualifiedName().toString();  
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 1 , typeElements.size());
        
        assertEquals( "foo.Three", name );
        
        assertEquals(injectable,typeElements.iterator().next());
        
        assertTrue ( "foo.Three  is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( injectable ));
    }

    protected void checkProduction1( VariableElement element,
            WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        assertTrue( injectable instanceof VariableElement );
        String name = injectable.getSimpleName().toString();
        
        assertEquals( "myField1", name );

        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 1 , productions.size());
        assertEquals( 0 , typeElements.size());
        
        assertEquals(injectable,productions.iterator().next());
        
        assertTrue ( "myField1 defined in foo.One is not an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( injectable ));
    }
    
    protected void checkProduction2( VariableElement element,
            WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        assertTrue( injectable instanceof VariableElement );
        String name = injectable.getSimpleName().toString();
        
        assertEquals( "myField1", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 2 , productions.size());
        assertEquals( 0 , typeElements.size());
        
        boolean oneFound = false;
        boolean twoFound = false;
        Element one = null;
        Element two = null;
        for( Element field : productions ){
            String fieldName = field.getSimpleName().toString();
            if ( "myField2".equals(fieldName)){
                oneFound = true;
                one = field;
            }
            if ( "myField1".equals( fieldName)){
                twoFound = true;
                two = field;
            }
        }
        
        assertTrue( "myField2 defined in class foo.One should be available " +
        		"via ApplicableResult interface", oneFound );
        
        assertTrue( "myField1 defined in class foo.Two should be available " +
                "via ApplicableResult interface", twoFound );
        
        assertTrue ( "myField2 in foo.One is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( one ));
        assertTrue ( "myField1 in foo.Two is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( two ));
        
        assertTrue( "myField2 in foo.One should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(one));
        assertFalse( "myField1 in foo.Two should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(two));
    }
    
    protected void checkProduction3( VariableElement element,
            WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.RESOLUTION_ERROR, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 1 , productions.size());
        assertEquals( 0 , typeElements.size());
        
        Element resolved = productions.iterator().next();
        
        assertEquals( "myField2" , resolved.getSimpleName().toString());
        
        assertTrue ( "myField2 in foo.Two is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( resolved ));
        
        assertTrue( "myField2 should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(resolved));
        
    }
    
    protected void checkProduction4( VariableElement element,
            WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.RESOLUTION_ERROR, result.getKind());        
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 1 , productions.size());
        assertEquals( 0 , typeElements.size());
        
        Element resolved = productions.iterator().next();
        
        assertEquals( "myField3" , resolved.getSimpleName().toString());
        
        assertTrue ( "myField3 in foo.Two is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( resolved ));
        
        assertTrue( "myField3 should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(resolved));
    }

    private void check2( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Two1", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 3 , typeElements.size());
        
        boolean twoFound = false;
        boolean fourFound = false;
        boolean fiveFound = false;
        TypeElement two = null;
        TypeElement four = null;
        TypeElement five = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two1".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.Four".equals( typeName)){
                fourFound = true;
                four = typeElement;
            }
            if ( "foo.Five".equals( typeName)){
                fiveFound = true;
                five = typeElement;
            }
        }
        
        assertTrue( "foo.Two1 should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.Four should be available via ApplicableResult interface", 
                fourFound );
        assertTrue( "foo.Five should be available via ApplicableResult interface", 
                fiveFound );
        
        assertFalse ( "foo.Two1 is not an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( two ));
        assertTrue ( "foo.Four is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( four ));
        assertTrue ( "foo.Five is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( five ));
        
        assertFalse( "foo.Two1 should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(two));
        assertTrue( "foo.Four should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(four));
        assertTrue( "foo.Five should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(five));
        
    }
    
    private void check1( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Two", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 2 , typeElements.size());
        
        boolean twoFound = false;
        boolean threeFound = false;
        TypeElement two = null;
        TypeElement three = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.Three".equals( typeName)){
                threeFound = true;
                three = typeElement;
            }
        }
        assertTrue( "foo.Two should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.Three should be available via ApplicableResult interface", 
                threeFound );
        
        assertFalse( "foo.Two should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(two));
        assertTrue( "foo.Three should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(three));
        
        assertFalse ( "foo.Two is not an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( two ));
        assertTrue ( "foo.Three is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( three ));
    }
    
    private void check3( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Two1", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 2 , typeElements.size());
        
        boolean twoFound = false;
        boolean fiveFound = false;
        TypeElement two = null;
        TypeElement five = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two1".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.Five".equals( typeName)){
                fiveFound = true;
                five = typeElement;
            }
        }
        assertTrue( "foo.Two1 should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.Five should be available via ApplicableResult interface", 
                fiveFound );
        
        assertFalse( "foo.Two1 should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(two));
        assertTrue( "foo.Five should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(five));
        
        assertFalse ( "foo.Two1 is not an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( two ));
        assertTrue ( "foo.Five is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( five ));
    }
    
    private void checkEnabled1( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Three", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 1 , typeElements.size());
        
        assertEquals("foo.Three", 
                typeElements.iterator().next().getQualifiedName().toString() );
        
        assertFalse( "foo.Three should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(injectable));
        
        assertTrue ( "foo.Three is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( injectable ));
    }
    
    private void checkEnabled2( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.RESOLUTION_ERROR, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 3 , typeElements.size());
        
        boolean oneFound = false;
        boolean fourFound = false;
        boolean fiveFound = false;
        TypeElement one = null;
        TypeElement five = null;
        TypeElement four = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.One1".equals(typeName)){
                oneFound = true;
                one = typeElement;
            }
            if ( "foo.Four".equals( typeName)){
                fourFound = true;
                four = typeElement;
            }
            if ( "foo.Five".equals( typeName)){
                fiveFound = true;
                five = typeElement;
            }
        }
        
        assertTrue( "foo.One1 should be available via ApplicableResult interface", 
                oneFound );
        assertTrue( "foo.Four should be available via ApplicableResult interface", 
                fourFound );
        assertTrue( "foo.Five should be available via ApplicableResult interface", 
                fiveFound );
        
        assertFalse( "foo.Four should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(four));
        assertFalse( "foo.Five should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(five));
        assertTrue( "foo.One1 should be disabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(one));    
        
        assertTrue ( "foo.One1 is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( one ));
        assertTrue ( "foo.Four is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( four ));
        assertTrue ( "foo.Five is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( five ));
        
    }
    
    private void checkEnabled3( VariableElement element, WebBeansModel model )
    {
        DependencyInjectionResult result = model.lookupInjectables(element, null, new AtomicBoolean(false));
        
        assertNotNull( result );
        
        assertEquals( DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof DependencyInjectionResult.InjectableResult );
        assertTrue( result instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( result instanceof DependencyInjectionResult.ResolutionResult );
        
        Element injectable = ((DependencyInjectionResult.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Five", name );
        
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 1 , typeElements.size());
        
        assertEquals("foo.Five", 
                typeElements.iterator().next().getQualifiedName().toString() );
        
        assertFalse( "foo.Five should be enabled", 
                ((DependencyInjectionResult.ApplicableResult)result).isDisabled(injectable));
        
        assertTrue ( "foo.Five is an Alternative", 
                ((DependencyInjectionResult.ResolutionResult)result).isAlternative( injectable ));
    }
}
