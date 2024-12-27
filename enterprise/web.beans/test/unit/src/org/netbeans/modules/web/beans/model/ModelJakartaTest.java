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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class ModelJakartaTest extends CommonTestCase {

    public ModelJakartaTest( String testName ) {
        super(testName, true);
    }

    public void testInjectionPointInitialization() throws MetadataModelException,
        IOException, InterruptedException
    {
        createQualifier("CustomBinding");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @foo.CustomBinding Object myFieldA = new Object();  "+
                " @Inject @foo.CustomBinding Object myFieldB ;  "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.CustomBinding " +
                "public class One  {}" );


        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<VariableElement> fields = ElementFilter.fieldsIn(
                        clazz.getEnclosedElements());
                Map<String,VariableElement> variables =
                    new HashMap<String, VariableElement>();
                for (VariableElement field : fields) {
                    variables.put(field.getSimpleName().toString(), field);
                }
                VariableElement fieldA = variables.get("myFieldA");
                assertNotNull( fieldA );
                DependencyInjectionResult result = model.lookupInjectables(fieldA,
                        null, new AtomicBoolean(false));
                assertEquals(DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED,
                        result.getKind());

                VariableElement fieldB = variables.get("myFieldB");
                assertNotNull( fieldB );
                result = model.lookupInjectables(fieldB, null, new AtomicBoolean(false));
                assertEquals(DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED,
                        result.getKind());
                return null;
            }

        });
    }

    public void testCommon() throws MetadataModelException, IOException,
        InterruptedException
    {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
		"import jakarta.enterprise.util.*; "+
                "import jakarta.inject.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.enterprise.util.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "@Inherited "+
                "public @interface CustomBinding  {" +
                "    String value(); "+
                "    @Nonbinding String comment() default \"\"; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class CustomClass  {" +
                " @Inject @foo.CustomBinding(value=\"a\") Object myFieldA;  "+
                " String myText[]; "+
                " @Inject @foo.CustomBinding(value=\"d\", comment=\"c\")  int myIndex; "+
                " Class<String> myClass; "+
                " @Inject @foo.CustomBinding(value=\"b\", comment=\"comment\")" +
                " foo.Generic<? extends Thread> myThread; "+
                " @Inject @foo.CustomBinding(value=\"c\")" +
                " foo.Generic<MyThread> myGen; "+
                " @Inject @foo.CustomBinding(value=\"e\") Thread myFieldB; "+
                " void method( Object param ){}"+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.CustomBinding(value=\"a\") " +
                "public class One  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two  extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "@foo.CustomBinding() " +
                "public class Three  { " +
                " @Produces @foo.CustomBinding(value=\"d\") " +
                "int productionField =1; " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@foo.CustomBinding(\"c\") "+
                "public class Generic<T extends foo.MyThread>  {" +
                " @Produces @foo.CustomBinding(value=\"e\") foo.MyThread getThread(){" +
                " return null; } "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@foo.CustomBinding(\"b\") "+
                "public class Generic1  extends Generic<MyThread>{}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/MyThread.java",
                "package foo; " +
                "public class MyThread extends Thread  {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/MyClass.java",
                "package foo; " +
                "public class MyClass extends Class<String>  {}" );

        inform("start common test");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints =
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                    else if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> params =
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : params) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }

                Set<String> names = new HashSet<String>();
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myFieldA")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.One", "foo.Two");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myGen")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Generic");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myIndex")){
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, provider, "productionField");
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldB")){
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductions((VariableElement)element, provider, "getThread");
                    }
                    else if ( element.getSimpleName().contentEquals("myThread")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Generic1");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                }

                assert names.contains("myFieldA");
                assert names.contains("myGen");
                assert names.contains("myIndex");
                assert names.contains("myFieldB");
                assert names.contains("myThread");

                return null;
            }

        });
    }

    public void testInjectable() throws MetadataModelException, IOException,
        InterruptedException
    {

        TestUtilities.copyStringToFileObject(srcFO, "foo/Fast.java",
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
                "public @interface Fast  {" +
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Slow.java",
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
                "public @interface Slow  {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Producer.java",
                "package foo; " +
                "import java.util.List; " +
                "public interface Producer  { " +
                "  List<String> getItems(); " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/SlowProducer.java",
                "package foo; " +
                "import java.util.List; " +
                "import jakarta.enterprise.inject.Default; " +
                "@Slow @Default " +
                "public class SlowProducer implements Producer { " +
                "  public List<String> getItems() { return null;} " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/FastProducer.java",
                "package foo; " +
                "import java.util.List; " +
                "import jakarta.enterprise.inject.Default; " +
                "@Fast @Default " +
                "public class FastProducer implements Producer { " +
                "  public List<String> getItems() { return null;} " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/User.java",
                "package foo; " +
                "import java.util.List; " +
                "import jakarta.inject.*; "+
                "import jakarta.enterprise.inject.Default; " +
                "public class User { " +
                "  @Inject @Slow @Default " +
                "  public Producer mySlowProducer; " +
                "  @Inject @Fast @Default " +
                "  public Producer myFastProducer; " +
                "  @Inject @Default " +
                "  public Producer myAmbiguousProducer; " +
                "}" );

        inform("start injectable test");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.User" );
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
                    if ( element.getSimpleName().contentEquals("mySlowProducer")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.SlowProducer");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("mySlowProducer")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.FastProducer");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myAmbiguousProducer")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.SlowProducer", "foo.FastProducer", "foo.Producer");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                }

                assert names.contains("mySlowProducer");
                assert names.contains("myFastProducer");
                assert names.contains("myAmbiguousProducer");

                return null;
            }

        });
    }



    public void testMixedBindings() throws MetadataModelException, IOException,
        InterruptedException
    {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import jakarta.enterprise.inject.*; "+
		"import jakarta.enterprise.util.*; "+
                "import java.lang.annotation.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.enterprise.util.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {" +
                "    String value(); "+
                "    @Nonbinding String comment() default \"\"; "+
                "}");

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
                "public @interface Binding2  {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding3.java",
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
                "public @interface Binding3  {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding4.java",
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
                "public @interface Binding4  {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface1.java",
                "package foo; " +
                "public interface Iface1 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface2.java",
                "package foo; " +
                "public interface Iface2 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface3.java",
                "package foo; " +
                "public interface Iface3 extends Iface1 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Clazz1 {" +
                " @Produces @foo.Binding1(\"b\") int productionField1 = 1; " +
                " @Produces @foo.Binding3 @foo.Binding2 String[] productionField2 = new String[0]; " +
                " @Produces @foo.Binding1(\"c\") @foo.Binding4 Clazz3 productionMethod() " +
                "{ return null; } " +
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "@foo.Binding1(\"a\") "+
                "public class Clazz2 extends Clazz1 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "@foo.Binding3 @foo.Binding2 "+
                "public class Clazz3 extends Clazz2 implements Iface1 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz4.java",
                "package foo; " +
                "@foo.Binding3 @foo.Binding1(\"a\") "+
                "public class Clazz4 implements Iface2, Iface3 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz5.java",
                "package foo; " +
                "@foo.Binding3 @foo.Binding1(\"b\") "+
                "public class Clazz5 implements Iface3 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz6.java",
                "package foo; " +
                "@foo.Binding1(\"a\") @foo.Binding2 @foo.Binding4 "+
                "public class Clazz6 extends Clazz2 {} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                "public class TestClass  { " +
                " @Inject @foo.Binding3 @foo.Binding1(\"a\") Iface1 myFieldA; "+
                " @Inject @foo.Binding2 Iface1 myFieldB; "+
                " @Inject @foo.Binding3 Clazz1 myFieldC; "+
                " @Inject @foo.Binding2 @foo.Binding4  Clazz2 myFieldD; "+
                " @Inject @foo.Binding1(\"b\") Integer myFieldE; "+
                " @Inject @foo.Binding3 @foo.Binding2 String[] myFieldF; "+
                " @Inject @foo.Binding1(\"c\") @foo.Binding4 Clazz1 myFieldG; "+
                "} ");

        inform("start mixed binding test");

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
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }

                Set<String> names = new HashSet<String>();
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myFieldA")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Clazz4");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldB")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Clazz3");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldC")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Clazz3");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldD")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Clazz6");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldE")){
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, provider, "productionField1");
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldF")){
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, provider, "productionField2");
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldG")){
                        assertFindVariableResultInjectables((VariableElement)element, provider);
                        assertFindVariableResultProductions((VariableElement)element, provider, "productionMethod");
                    }
                }

                assert names.contains("myFieldA");
                assert names.contains("myFieldB");
                assert names.contains("myFieldC");
                assert names.contains("myFieldD");
                assert names.contains("myFieldE");
                assert names.contains("myFieldF");
                assert names.contains("myFieldG");
                return null;
            }
        });

    }

}
