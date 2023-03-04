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
package org.netbeans.modules.web.beans.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.impl.model.results.DefinitionErrorResult;


/**
 * @author ads
 *
 */
public class ParametersTest extends CommonTestCase {

    public ParametersTest( String testName ) {
        super(testName);
    }
    
    public void testSimpleParameter() throws IOException, InterruptedException{
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
                "public @interface Binding1  {" +
                "    String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces String productionField = \"\"; "+
                " @Produces @foo.Binding2 int[] productionMethod() { return null; } "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.Binding1(value=\"a\") @foo.Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject void method1( @Binding2 SuperClass arg1 , " +
                "   @Binding1(\"a\") SuperClass arg2 ){} " +
                " @Produces boolean  method2( @Binding2 Two arg ){ return false;} "+
                " @Inject void method3( @Default SuperClass arg ){} "+
                " @Produces int method4( @Default String arg ){ return 0;} "+
                " @Inject void method5( @Binding2 int[] arg ){} " +
                " void method6( @Binding2 int[] arg ){} "+
                " @Inject void method7( SuperClass arg ){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start simple parameters test");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> parameters = 
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : parameters) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    Element enclosingElement = element.getEnclosingElement();
                    assert enclosingElement instanceof ExecutableElement;
                    ExecutableElement method = (ExecutableElement)enclosingElement;
                    names.add( method.getSimpleName()+ " " +element.getSimpleName());
                    if ( method.getSimpleName().contentEquals("method1")){
                        if ( element.getSimpleName().contentEquals("arg1")){
                            assertFindParameterResultInjectables(element, provider, "foo.One", "foo.Two");
                            assertFindParameterResultProductions(element, provider);
                        }
                        else if ( element.getSimpleName().contentEquals("arg2")){
                            assertFindParameterResultInjectables(element, provider, "foo.One");
                            assertFindParameterResultProductions(element, provider);
                        }
                    }
                    else if (method.getSimpleName().contentEquals("method2") ){
                        assertFindParameterResultInjectables(element, provider, "foo.Two");
                        assertFindParameterResultProductions(element, provider);
                    }
                    else if (method.getSimpleName().contentEquals("method3") ){
                        assertFindParameterResultInjectables(element, provider, "foo.SuperClass");
                        assertFindParameterResultProductions(element, provider);
                    }
                    else if (method.getSimpleName().contentEquals("method4") ){
                        DependencyInjectionResult result = provider.findParameterInjectable(element, null, new AtomicBoolean(false));
                        assertResultInjectables(result);
                        assertResultProductions(result, true, "productionField");
                    }
                    else if (method.getSimpleName().contentEquals("method5") ){
                        assertFindParameterResultInjectables(element, provider);
                        assertFindParameterResultProductions(element, provider, "productionMethod");
                    }
                    else if (method.getSimpleName().contentEquals("method6") ){
                        DependencyInjectionResult result = provider.findParameterInjectable(element, null, new AtomicBoolean(false));
                        /* Method has no any special annotation. It's argument is not injection point.*/
                        assertTrue( result instanceof DefinitionErrorResult );
                    }
                    else if (method.getSimpleName().contentEquals("method7") ){
                        assertFindParameterResultInjectables(element, provider, "foo.SuperClass");
                        assertFindParameterResultProductions(element, provider);
                    }
                }
                
                assert names.contains("method1 arg1");
                assert names.contains("method1 arg2");
                assert names.contains("method2 arg");
                assert names.contains("method3 arg");
                assert names.contains("method4 arg");
                assert names.contains("method6 arg");
                return null;
            }
        });
    }
    
    public void testDisposesParameter() throws IOException, InterruptedException{
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
                "public @interface Binding1  {" +
                "    String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces @Binding2 String getText(){ return null;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.Binding1(value=\"a\") @foo.Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class TestClass  {" +
                " @Produces @Binding2 int getIndex(){ return 0;} "+
                " @Produces @Binding1(\"a\") boolean isNull(){ return false;} "+
                " @Produces String get(){ return null;} "+
                
                " void clean(@Disposes @Binding2 int index , String text){} "+
                " void stopped(@Disposes @Binding1(\"a\") boolean isNull ){} "+
                " void close(@Disposes @Binding1(\"a\") SuperClass clazz){} "+
                " void inform(@Disposes @Binding2 String text){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start disposes parameters test");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> parameters = 
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : parameters) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    Element enclosingElement = element.getEnclosingElement();
                    assert enclosingElement instanceof ExecutableElement;
                    ExecutableElement method = (ExecutableElement)enclosingElement;
                    names.add( method.getSimpleName()+ " " +element.getSimpleName());
                    if ( method.getSimpleName().contentEquals("clean")){
                        if ( element.getSimpleName().contentEquals("index")){
                            assertFindParameterResultInjectables(element, provider);
                            assertFindParameterResultProductions(element, provider, "getIndex");
                        }
                        else if ( element.getSimpleName().contentEquals("text")){
                            assertFindParameterResultInjectables(element, provider);
                            assertFindParameterResultProductions(element, provider, "get");
                        }
                    }
                    else if (method.getSimpleName().contentEquals("stopped") ){
                        assertFindParameterResultInjectables(element, provider);
                        assertFindParameterResultProductions(element, provider, "isNull");
                    }
                    else if (method.getSimpleName().contentEquals("close") ){
                        assertFindParameterResultInjectables(element, provider);
                        assertFindParameterResultProductions(element, provider);
                    }
                    else if (method.getSimpleName().contentEquals("inform") ){
                        assertFindParameterResultInjectables(element, provider);
                        assertFindParameterResultProductions(element, provider);
                    }
                }
                
                assert names.contains("clean index");
                assert names.contains("clean text");
                assert names.contains("stopped isNull");
                assert names.contains("close clazz");
                assert names.contains("inform text");
                return null;
            }
        });
    }
    
    public void testObservesParameter() throws IOException, InterruptedException{
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
                "public @interface Binding1  {" +
                "    String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces @Default String getText(){ return null;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.Binding1(value=\"a\") @foo.Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SomeEvent.java",
                "package foo; " +
                "public class SomeEvent {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.event.*; "+
                "public class TestClass  {" +
                " void method1(@Observes @Binding2 SomeEvent , String text){} "+
                " void method2(@Observes @Binding1(\"a\") SomeEvent,  " +
                " @foo.Binding1(\"a\") @foo.Binding2 SuperClass clazz){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start observes parameters test");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> parameters = 
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : parameters) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    Element enclosingElement = element.getEnclosingElement();
                    assert enclosingElement instanceof ExecutableElement;
                    ExecutableElement method = (ExecutableElement)enclosingElement;
                    names.add( method.getSimpleName()+ " " +element.getSimpleName());
                    if ( method.getSimpleName().contentEquals("method1")){
                        if ( element.getSimpleName().contentEquals("text")){
                            assertFindParameterResultInjectables(element, provider);
                            assertFindParameterResultProductions(element, provider, "getText");
                            names.add("method1 text");
                        }
                    }
                    else if (method.getSimpleName().contentEquals("method2") ){
                        if ( element.getSimpleName().contentEquals("clazz")){
                            assertFindParameterResultInjectables(element, provider, "foo.One");
                            assertFindParameterResultProductions(element, provider);
                            names.add("method2 clazz");
                        }
                    }
                }
                
                assert names.contains("method1 text");
                assert names.contains("method2 clazz");
                return null;
            }
        });
    }
    
}
