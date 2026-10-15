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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.testutilities.CdiTestUtilities;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class ScopeTest extends CommonTestCase {

    public ScopeTest( String testName ) {
        super(testName, false);
    }
    
    public void testInheritedScope() throws IOException{ 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@SessionScoped "+
                "public class SuperClass  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/ChildClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.ApplicationScoped; "+
                "@ApplicationScoped "+
                "public class ChildClass  extends SuperClass{ " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SubClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SubClass extends ChildClass{ " +
                "}" );
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.SuperClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.ChildClass" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.SubClass" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                return null;
            }
            
        });
    }
    
    public void testDependentScope() throws IOException{ 

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " + 
                "public class Clazz  { " +
                "}" );
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run(WebBeansModel model) throws Exception {
                TypeMirror mirror = model.resolveType("foo.Clazz");
                Element clazz = ((DeclaredType) mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.Dependent");
                return null;
            }
        });
    }
    
    public void testSterotypedScope() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class1.java",
                "package foo; " +
                "@Stereotype1 "+
                "public class Class1  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class2.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "@ApplicationScoped "+
                "public class Class2  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class3.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "public class Class3 extends Class2 { " +
                "}" );
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Class1" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.Class2" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.Class3" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                return null;
            }
        });
    }
    
    public void testCustomScope() throws IOException{ 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomScope.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.*; "+
                "@NormalScope "+
                "@Target({TYPE,METHOD,FIELD}) "+ 
                "@Retention(RUNTIME) "+
                "@Inherited "+
                "public @interface CustomScope {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "@CustomScope "+
                "public class Clazz  { " +
                "}" );
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Clazz" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "foo.CustomScope");
                return null;
            }
        });
    }
    
    public void testDefaultScope() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype3.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype "+
                "@ApplicationScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype3 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class1.java",
                "package foo; " +
                "@Stereotype1 "+
                "@Stereotype2 "+
                "public class Class1  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class2.java",
                "package foo; " +
                "@Stereotype1 "+
                "@Stereotype3 "+
                "public class Class2  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class3.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "@Stereotype3 "+
                "@ApplicationScoped "+
                "public class Class3  { " +
                "}" );
        
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Class1" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.Class3" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.Class2" );
                clazz = ((DeclaredType)mirror).asElement();
                boolean exception = false;
                
                try {
                    model.getScope(clazz);
                }
                catch(CdiException e ){
                    exception = true;
                }
                assertTrue( "Class2 has no explicit Scope but has different " +
                		"Stereotypes with different default Scopes. So it has no" +
                		" default scope and this is a problem", exception );
                return null;
            }
        });
    }

    private void checkScope(WebBeansModel model , Element element, 
            String fqn ){
        try {
            String scope = model.getScope(element);
            assertEquals("Not expected scope type", fqn, scope);
        }
        catch ( CdiException e ){
            throw new RuntimeException( e );
        }
    }
}
