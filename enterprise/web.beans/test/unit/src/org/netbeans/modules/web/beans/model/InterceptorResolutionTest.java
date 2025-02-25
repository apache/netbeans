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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class InterceptorResolutionTest extends CommonTestCase {

    public InterceptorResolutionTest( String testName ) {
        super(testName, false);
    }

    public void testSimpleInterceptorCase() throws IOException{
        createInterceptorBinding("IBinding1");
        createInterceptorBinding("IBinding2");
        createInterceptorBinding("IBinding3");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding1 "+
                "public class One {" +
                " void method1(){} "+
                " @IBinding2 @IBinding3 void method2(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1 "+
                "public class Iceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor2.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding3 "+
                "public class Iceptor2 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor3.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding2  @IBinding1 "+
                "public class Iceptor3 {" +
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkInterceptors(model, "foo.One", "foo.Iceptor1");
                
                checkMethodInterceptors(model, "foo.One", "method1", "foo.Iceptor1");
                
                checkMethodInterceptors(model, "foo.One", "method2", "foo.Iceptor1",
                        "foo.Iceptor2", "foo.Iceptor3");
                
                return null;
            }
            
        });
    }
    
    public void testInterceptorNonbindingMembers() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "import javax.enterprise.util.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface IBinding1  {" +
                " String value() ;"+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "import javax.enterprise.util.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface IBinding2  {" +
                " String value() ;"+
                " @Nonbinding String comment() defualt \"\" ; "+
                "} ");
        createInterceptorBinding("IBinding3");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding1(\"d\") "+
                "public class One {" +
                " void method1(){} "+
                " @IBinding2(\"a\") @IBinding3 void method2(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1(\"e\") "+
                "public class Iceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor2.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding3 @IBinding2(value=\"a\",comment=\"c\")"+
                "public class Iceptor2 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor3.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding2(\"b\")  @IBinding1 "+
                "public class Iceptor3 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor4.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1(\"d\") "+
                "public class Iceptor4 {" +
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkInterceptors(model, "foo.One", "foo.Iceptor4");
                
                checkMethodInterceptors(model, "foo.One", "method1", "foo.Iceptor4");
                
                checkMethodInterceptors(model, "foo.One", "method2", "foo.Iceptor2",
                        "foo.Iceptor4");
                
                return null;
            }
            
        });
    }
    
    public void testEnabledInterceptor() throws IOException{
        createInterceptorBinding("IBinding1");
        createInterceptorBinding("IBinding2");
        
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml",
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<interceptors> "+
                    "<class>foo.Iceptor2</class>"+
                    "<class>foo.Iceptor3</class>"+
                "</interceptors> " +
                "</beans>");

        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding1 "+
                "public class One {" +
                " @IBinding2 void method1(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1 "+
                "public class Iceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor2.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding2 "+
                "public class Iceptor2 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor3.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding2 @IBinding1 "+
                "public class Iceptor3 {" +
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkMethodEnabledInterceptors(model, "foo.One", "method1", 
                        new String[]{ "foo.Iceptor2" , "foo.Iceptor3"}, "foo.Iceptor1");
                
                return null;
            }
            
        });
    }
    
    public void testDeclaredInterceptor() throws IOException{
        createInterceptorBinding("IBinding1");
        
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml",
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<interceptors> "+
                "</interceptors> " +
                "</beans>");

        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptors({DeclaredIceptor1.class, DeclaredIceptor2.class}) "+
                "public class One {" +
                " @IBinding1 @Interceptors({DeclaredIceptor3.class}) void method(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1 "+
                "public class Iceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/DeclaredIceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "public class DeclaredIceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/DeclaredIceptor2.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "public class DeclaredIceptor2 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/DeclaredIceptor3.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "public class DeclaredIceptor3 {" +
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkInterceptors(model, "foo.One",  false, "foo.DeclaredIceptor1", 
                        "foo.DeclaredIceptor2" );
                
                checkMethodInterceptors(model, "foo.One", "method", false   , 
                        "foo.DeclaredIceptor1", "foo.DeclaredIceptor2", 
                        "foo.DeclaredIceptor3");
                
                return null;
            }
            
        });
    }
    
    public void testInterceptorMixedCases() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "import javax.enterprise.util.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface IBinding1  {" +
                " String value() ;"+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/IBinding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "import javax.enterprise.util.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "@IBinding1(\"a\") "+
                "public @interface IBinding2  {" +
                "} ");
        createInterceptorBinding("IBinding3");
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
                "@IBinding4 "+
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
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@IBinding3 "+
                "public class One {" +
                " @IBinding2 void method1(){} "+
                " @Stereotype2 @IBinding1(\"c\") void method2(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Stereotype1 "+
                "public class Two {" +
                " @IBinding1(\"b\") void method(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor1.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding1(\"a\") "+
                "public class Iceptor1 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor2.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding3 @IBinding4 @IBinding1(\"d\")"+
                "public class Iceptor2 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor3.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@IBinding4  @IBinding1(\"b\") "+
                "public class Iceptor3 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor4.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@Stereotype2 @IBinding1(\"b\") "+
                "public class Iceptor4 {" +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iceptor5.java",
                "package foo; " +
                "import javax.interceptor.*; "+
                "@Interceptor "+
                "@Stereotype1 @IBinding1(\"c\") "+
                "public class Iceptor5 {" +
                "}" );
        
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkInterceptors(model, "foo.One" );
                
                checkMethodInterceptors(model, "foo.One", "method1", "foo.Iceptor1");
                
                checkMethodInterceptors(model, "foo.One", "method2", "foo.Iceptor5");
                
                checkMethodInterceptors(model, "foo.Two", "method", "foo.Iceptor3",
                        "foo.Iceptor4" );
                
                return null;
            }
            
        });
    }
    
    private void checkInterceptors(WebBeansModel model , String className , 
            String... interceptorFqns)
    {
        checkInterceptors(model, className, true , interceptorFqns);
    }
    
    private void checkInterceptors(WebBeansModel model , String className , 
            boolean resolved, String... interceptorFqns)
    {
        TypeMirror type = model.resolveType( className );
        Element clazz = model.getCompilationController().getTypes().asElement( type );
        
        checkInterceptors(model, clazz, resolved , interceptorFqns);
    }
    
    private InterceptorsResult checkMethodInterceptors(WebBeansModel model , 
            String className , String methodName, String... interceptorFqns)
    {
        return checkMethodInterceptors(model, className, methodName, 
                true, interceptorFqns);
    }
    
    private InterceptorsResult checkMethodInterceptors(WebBeansModel model , 
            String className , String methodName, boolean resolved ,
            String... interceptorFqns)
    {
        ExecutableElement element = getMethod(model, className, methodName);
        
        assertNotNull( element );
        return checkInterceptors(model, element, resolved, interceptorFqns);
    }

    private ExecutableElement getMethod( WebBeansModel model, String className,
            String methodName )
    {
        TypeMirror type = model.resolveType( className );
        Element clazz = model.getCompilationController().getTypes().asElement( type );
        
        List<ExecutableElement> methods = ElementFilter.methodsIn( 
                clazz.getEnclosedElements() );
        ExecutableElement element = null;
        for (ExecutableElement executableElement : methods) {
            String name = executableElement.getSimpleName().toString();
            if ( name.equals(methodName )){
                element = executableElement;
                break;
            }
        }
        return element;
    }
    
    private void checkMethodEnabledInterceptors(WebBeansModel model , 
            String className , String methodName, String[] enabledInterceptors,
            String...  disabledInterceptors )
    {
        List<String> list = new LinkedList<String>( Arrays.asList( enabledInterceptors ));
        list.addAll( Arrays.asList( disabledInterceptors ));
        InterceptorsResult result = checkMethodInterceptors(model, className, 
                methodName, list.toArray( new String[0] ));
        
        Set<String> disabled = new HashSet<String>();
        Set<String> enabled =  new HashSet<String>();
        List<TypeElement> allInterceptors = result.getAllInterceptors();
        for (TypeElement typeElement : allInterceptors) {
            if ( result.isDisabled(typeElement)){
                disabled.add( typeElement.getQualifiedName().toString());
            }
            else {
                enabled.add( typeElement.getQualifiedName().toString());
            }
        }

        Set<String> requiredEnabled = new HashSet<String>( Arrays.asList( enabledInterceptors));
        compareCollections(enabled, requiredEnabled, "Not found enabled interceptors :");
        compareCollections(Arrays.asList( enabledInterceptors), 
                enabled, "These interceptos are unexpectedly enabled :");
        
        Set<String> requiredDisabled = new HashSet<String>( Arrays.asList( disabledInterceptors));
        compareCollections(disabled, requiredDisabled, "Not found disabled interceptors :");
        compareCollections(Arrays.asList( disabledInterceptors), 
                disabled, "These interceptos are unexpectedly disabled :");
    }
    
    private void compareCollections( Collection<String> actual,Set<String> required ,
            String errorMessage )
    {
        required.removeAll( actual );
        if ( !required.isEmpty()){
            StringBuilder builder = new StringBuilder();
            for (String fqn : required) {
                builder.append( fqn );
                builder.append(" , ");
            }
            assertFalse( errorMessage+ builder.toString() ,true );
        }
    }
    
    private InterceptorsResult checkInterceptors( WebBeansModel model, Element element,
            String... interceptorFqns  )
    {
        return checkInterceptors(model, element, true , interceptorFqns);
    }

    private InterceptorsResult checkInterceptors( WebBeansModel model, Element element,
             boolean resolved , String... interceptorFqns )
    {
        InterceptorsResult result = model.getInterceptors(element);
        Collection<TypeElement> interceptors = null;
        if ( resolved ){
            interceptors = result.getResolvedInterceptors();
        }
        else {
            interceptors = result.getDeclaredInterceptors();
        }
        Set<String> foundIceptors = new HashSet<String>();
        for (TypeElement typeElement : interceptors) {
            String fqn = typeElement.getQualifiedName().toString();
            foundIceptors.add( fqn );
        }
        Set<String> requiredFqns = new HashSet<String>( Arrays.asList( interceptorFqns));
        
        requiredFqns.removeAll( foundIceptors );
        if ( !requiredFqns.isEmpty() ){
            StringBuilder builder = new StringBuilder();
            for( String fqn : requiredFqns ){
                builder.append( fqn );
                builder.append(", ");
            }
            assertFalse("Interceptors "+builder+" are exepcted but not found", true );
        }
        
        foundIceptors.removeAll(Arrays.asList( interceptorFqns));
        if ( !foundIceptors.isEmpty() ){
            StringBuilder builder = new StringBuilder();
            for( String fqn : foundIceptors ){
                builder.append( fqn );
                builder.append(", ");
            }
            assertFalse("Interceptors "+builder+" found but not expected", true );
        }
        return result;
    }
}
