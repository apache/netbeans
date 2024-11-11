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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class InterceptorBindingsTest extends CommonTestCase {

    public InterceptorBindingsTest( String testName ) {
        super(testName);
    }

    public void testClassInterceptorBindings() throws IOException{
        
        createInterceptorBinding("IBinding1");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "@IBinding1 "+
                "public @interface IBinding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding3.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "@IBinding1 "+
                "@Inherited "+
                "public @interface IBinding3  {} ");
        
        createInterceptorBinding("IBinding4");
        
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
                "@Stereotype "+
                "@Inherited "+
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
                "@IBinding4 "+
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
                "@Stereotype "+
                "@Stereotype2 "+
                "public @interface Stereotype3 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding5.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "@Stereotype2 "+
                "public @interface IBinding5  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding3 @Stereotype1 "+
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@IBinding2 @Stereotype2 "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "@Stereotype3 "+
                "public class Three extends Two {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "@IBinding4 "+
                "public class Four extends Three {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Five.java",
                "package foo; " +
                "@IBinding5 "+
                "public class Five {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Six.java",
                "package foo; " +
                "@Stereotype3 "+
                "public class Six {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkInterceptorBindings(model, "foo.One" , "foo.IBinding3",
                        "foo.IBinding1");
                
                checkInterceptorBindings(model, "foo.Two" , "foo.IBinding2",
                    "foo.IBinding4", "foo.IBinding3", "foo.IBinding1");
                
                checkInterceptorBindings(model, "foo.Three" , "foo.IBinding4", 
                        "foo.IBinding3", "foo.IBinding1");
                
                checkInterceptorBindings(model, "foo.Four" , "foo.IBinding4", 
                        "foo.IBinding3", "foo.IBinding1");
                
                checkInterceptorBindings(model, "foo.Five" , "foo.IBinding5");
                
                checkInterceptorBindings(model, "foo.Six" , "foo.IBinding4");
                
                return null;
            }

        });
    }
    
    public void testMethodInterceptorBindings() throws IOException{
        
        createInterceptorBinding("IBinding1");
        createInterceptorBinding("IBinding2");
        createInterceptorBinding("IBinding3");
        
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
                "@Stereotype "+
                "@IBinding2 "+
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
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding1 "+
                "public class One {" +
                " @IBinding3 void method1(){} "+
                " @Stereotype2 void method2(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Stereotype2 "+
                "public class Two {" +
                " @IBinding1 void method(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "public class Three {" +
                " @IBinding1 void method(){} "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkMethodInterceptorBindings(model, "foo.One", "method1", 
                        "foo.IBinding1","foo.IBinding3");
                
                checkMethodInterceptorBindings(model, "foo.One", "method2", 
                        "foo.IBinding1","foo.IBinding2");
                
                checkMethodInterceptorBindings(model, "foo.Two", "method", 
                        "foo.IBinding1","foo.IBinding2");
                
                checkMethodInterceptorBindings(model, "foo.Three", "method", 
                        "foo.IBinding1");
                return null;
            }

        });
    }
    
    private void checkMethodInterceptorBindings( WebBeansModel model , 
            String typeElement, String methodName, String... annotationFqns) 
    {
        TypeMirror mirror = model.resolveType( typeElement );
        Element clazz = ((DeclaredType)mirror).asElement();
        List<ExecutableElement> methods = ElementFilter.methodsIn( 
                clazz.getEnclosedElements());
        ExecutableElement element = null;
        for (ExecutableElement method : methods) {
            String name = method.getSimpleName().toString();
            if ( name.equals( methodName)){
                element = method;
                break;
            }
        }
        
        assertNotNull( element );
        checkInterceptorBindings(model, element, annotationFqns);
    }

    private void checkInterceptorBindings( WebBeansModel model, Element element,
            String... annotationFqns )
    {
        Collection<AnnotationMirror> interceptorBindings = 
            model.getInterceptorBindings(element);
        
        Set<String> fqns = getIBindingFqns(interceptorBindings);
        
        Set<String> expected = new HashSet<String>( Arrays.asList( annotationFqns));
        expected.removeAll( fqns );
        if ( expected.size() >0 ){
            StringBuilder builder = new StringBuilder();
            for (String fqn : expected) {
                builder.append(fqn);
                builder.append(", ");
            }
            assertFalse( "Elements "+builder+" are not found ", true);
        }
        
        List<String> expectedList = Arrays.asList( annotationFqns);
        fqns.removeAll( expectedList );
        if ( fqns.size() >0 ){
            StringBuilder builder = new StringBuilder();
            for (String fqn : fqns) {
                builder.append(fqn);
                builder.append(", ");
            }
            assertFalse( "Interceptor bindings  "+builder+" are found but not expected", true);
        }
    }
        
    private void checkInterceptorBindings( WebBeansModel model , 
            String typeElement, String... annotationFqns) 
    {
        TypeMirror mirror = model.resolveType( typeElement );
        Element clazz = ((DeclaredType)mirror).asElement();
        
        checkInterceptorBindings(model, clazz, annotationFqns);
    }

    private Set<String> getIBindingFqns(
            Collection<AnnotationMirror> interceptorBindings )
    {
        Set<String> fqns = new HashSet<String>();
        for (AnnotationMirror annotationMirror : interceptorBindings) {
            Element iBinding = annotationMirror.getAnnotationType().asElement();
            if ( iBinding instanceof TypeElement ){
                fqns.add( ((TypeElement)iBinding).getQualifiedName().toString());
            }
        }
        return fqns;
    }
}
