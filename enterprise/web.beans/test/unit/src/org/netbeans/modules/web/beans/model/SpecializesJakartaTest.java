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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class SpecializesJakartaTest extends CommonTestCase {

    public SpecializesJakartaTest( String testName ) {
        super(testName, true);
    }

    public void testSimpleTypeSpecializes() throws IOException, InterruptedException{

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @CustomBinding One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@CustomBinding "+
                "public class Two  extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "public class Three extends Two {}" );

        inform("start simple specializes test");


        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        assert element.getSimpleName().contentEquals("myField");
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Two", "foo.Three");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                }
                return null;
            }
        });
    }

    public void testMergeBindingsSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
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
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @CustomBinding @Binding1 @Binding2 One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1 " +
                "public class One  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two  extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "@CustomBinding "+
                "public class Three extends Two {}" );

        inform("start merged specializes test");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        assert element.getSimpleName().contentEquals("myField");
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Three");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                }
                return null;
            }
        });
    }

    public void testDefaultSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
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
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @Default Two myField1; "+
                " @Inject Three myField2; "+
                " @Inject @Default @Binding2 @Binding1 One1 myField3; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Default " +
                "public class One  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two  extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "@Binding1 "+
                "public class One1  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two1  extends One1 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "public class Three  extends Two1 {}" );

        inform("start @Default specializes test");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                Set<String> names = new HashSet<String>();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        names.add( element.getSimpleName().toString());
                        if ( element.getSimpleName().contentEquals("myField1")){
                            assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Two");
                            assertFindVariableResultProductions((VariableElement)element, provider);
                        }
                        else if ( element.getSimpleName().contentEquals("myField2")){
                            assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Three");
                            assertFindVariableResultProductions((VariableElement)element, provider);
                        }
                        else if ( element.getSimpleName().contentEquals("myField3")){
                            assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Three");
                            assertFindVariableResultProductions((VariableElement)element, provider);
                        }
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }
        });
    }

    public void testSimpleProductionSpecializes() throws IOException, InterruptedException{

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @CustomBinding int myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class One  {" +
                " @CustomBinding @Produces int getIndex(){ return 0;} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes int getIndex(){return 0;} "+
                "}" );

        inform("start simple specializes test for production methods");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        assert element.getSimpleName().contentEquals("myField");
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductions((VariableElement)element, provider, "getIndex", "getIndex");
                    }
                }
                return null;
            }
        });
    }

    public void testMergeProductionSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
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
                "import jakarta.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @CustomBinding @Binding1 @Binding2 int myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class One  {" +
                " @Produces @CustomBinding int getIndex(){ return 0; } " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes @Binding1 int getIndex(){ return 0; } " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Three extends Two {" +
                " @Produces @Specializes @Binding2 int getIndex(){ return 0; } " +
                "}" );

        inform("start merged specializes test for production method");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction(
                new MetadataModelAction<WebBeansModel, Void>() {

                    public Void run( WebBeansModel model ) throws Exception {
                        TypeMirror mirror = model
                                .resolveType("foo.CustomClass");
                        Element clazz = ((DeclaredType) mirror).asElement();
                        List<? extends Element> children = clazz
                                .getEnclosedElements();
                        for (Element element : children) {
                            if (element instanceof VariableElement) {
                                assert element.getSimpleName().contentEquals(
                                        "myField");
                                assertFindVariableResultInjectables((VariableElement)element, provider);
                                assertFindVariableResultProductions((VariableElement)element, provider, "getIndex");
                            }
                        }
                        return null;
                    }
                });
    }

    public void testDefaultProductionSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
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
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @Default @Binding1 int myField1; "+
                " @Inject @Default @Binding2 @Binding1 boolean myField2; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class One  {" +
                " @Produces @Default int getIndex(){ return 0;} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes @Binding1 int getIndex(){ return 0;} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class One1  {" +
                " @Produces @Binding1 boolean isNull(){ return true;} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Two1  extends One1 {" +
                " @Produces @Specializes @Binding2 boolean isNull(){ return true;} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Three  extends Two1 {" +
                " @Produces @Specializes boolean isNull(){ return true;} "+
                "}" );

        inform("start @Default specializes test for production method");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                Set<String> names = new HashSet<String>();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        names.add( element.getSimpleName().toString());
                        if ( element.getSimpleName().contentEquals("myField1")){
                            assertFindVariableResultInjectables((VariableElement)element, provider);
                            assertFindVariableResultProductions((VariableElement)element, provider, "getIndex");
                        }
                        else if ( element.getSimpleName().contentEquals("myField2")){
                            assertFindVariableResultInjectables((VariableElement)element, provider);
                            assertFindVariableResultProductions((VariableElement)element, provider, "isNull");
                        }
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }
        });
    }

}
