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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class TypedTest extends CommonTestCase {

    public TypedTest( String testName ) {
        super(testName, false);
    }

    public void testCommon() throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Binding "+
                "@Typed( {Iface3.class} ) "+
                "public class One implements Iface3 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Typed( {Iface2.class} ) "+
                "@Binding "+
                "public class Two  extends One implements Iface2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "@Typed( { Iface3.class} ) "+
                "@Binding "+
                "public class Three extends Two {" +
                " @Produces @Typed({One.class}) @Binding Two productionFieldA=null; " +
                " @Produces @Typed({Object[].class}) @Binding String productionFieldB[]=null; " +
                " @Produces @Typed({Two[].class}) @Binding Three productionFieldC[]=null; " +
                " @Produces @Typed({Two.class}) @Binding Two productionMethodA() " +
                "{return null; } " +
                " @Produces @Typed({Integer.class}) @Binding Integer productionMethodB() " +
                "{return null; } " +
                " @Produces @Typed({Number.class}) @Binding Byte productionMethodC() " +
                "{return null; } " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface1.java",
                "package foo; " +
                "@Binding "+
                "public interface Iface1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface2.java",
                "package foo; " + 
                "import javax.enterprise.inject.Typed; "+
                "@Typed( {Iface1.class , Iface2.class} ) "+
                "@Binding "+
                "public interface Iface2 extends Iface1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface3.java",
                "package foo; " +
                "@Binding "+
                "public interface Iface3 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.inject.*; "+
                "public class TestClass  { " +
                " @Inject @foo.Binding Iface1 myFieldA; "+
                " @Inject @foo.Binding One myFieldB; "+
                " @Inject @foo.Binding String[] myFieldC; "+
                " @Inject @foo.Binding Two[] myFieldD; "+
                " @Inject @foo.Binding Two myFieldE; "+
                " @Inject @foo.Binding int myFieldF; "+
                " @Inject @foo.Binding Number myFieldG; "+
                "} ");
        
        inform("start restriction types test");
        
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
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider, "foo.Iface2", "foo.Iface1");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldB")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, 
                                provider, "productionFieldA");
                    }
                    else if ( element.getSimpleName().contentEquals("myFieldC")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, provider);
                    }
                    else  if ( element.getSimpleName().contentEquals("myFieldD")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductionsVar((VariableElement)element, 
                                provider, "productionFieldC");
                    }
                    else  if ( element.getSimpleName().contentEquals("myFieldE")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductions((VariableElement)element, 
                                provider, "productionMethodA");
                    }
                    else  if ( element.getSimpleName().contentEquals("myFieldF")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductions((VariableElement)element, 
                                provider, "productionMethodB");
                    }
                    else  if ( element.getSimpleName().contentEquals("myFieldG")){
                        assertFindVariableResultInjectables((VariableElement)element, 
                                provider);
                        assertFindVariableResultProductions((VariableElement)element, 
                                provider, "productionMethodC");
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
    
    public void testRawParametrized() throws MetadataModelException,
            IOException, InterruptedException
    {
        createQualifier("Binding1");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Binding1 "+
                "@Typed( {Object.class} ) "+
                "public class Generic<T> {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Binding1 "+
                "@Typed( {Generic.class} ) "+
                "public class Generic1<T> extends Generic<T> {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "public class Two1 extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic2.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Binding1 "+
                "@Typed( {Generic.class} ) "+
                "public class Generic2 extends Generic<One> {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic3.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Binding1 "+
                "@Typed( {Generic.class} ) "+
                "public class Generic3<T extends Two1> extends Generic<T> {}" );
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.util.*; "+
                "@Binding1 "+
                "public class Three<T> {" +
                " @Produces @Typed({Collection.class}) @Binding1 List productionFieldA=null; " +
                " @Produces @Typed({List.class}) @Binding1 LinkedList<T> productionFieldB=null; " +
                " @Produces @Typed({Set.class}) @Binding1 HashSet<T> productionMethodA() " +
                "{return null; } " +
                " @Produces @Typed({AbstractList.class}) @Binding1 ArrayList<T> productionMethodB() " +
                "{return null; } " +
                "}" );
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass1.java",
                "package foo; " +
                "import javax.inject.*; "+
                "import java.util.*; "+
                "public class TestClass1<T extends One>  { " +
                " @Inject @foo.Binding1 Generic myFieldA; "+
                " @Inject @foo.Binding1 Generic<? super Two> myFieldB; "+
                " @Inject @foo.Binding1 Generic<One1> myFieldC; "+
                " @Inject @foo.Binding1 List myFieldD; "+
                " @Inject @foo.Binding1 Set<? extends One> myFieldE; "+
                " @Inject @foo.Binding1 AbstractList<T> myFieldF; "+
                "} ");

        inform("start restriction types for generic case test");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass1");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                Set<String> names = new HashSet<String>();
                for (VariableElement element : injectionPoints) {
                    names.add(element.getSimpleName().toString());
                    if (element.getSimpleName().contentEquals("myFieldA")) {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider,"foo.Generic1");
                        assertFindVariableResultProductions(
                                (VariableElement) element, provider);
                    }
                    else if (element.getSimpleName().contentEquals("myFieldB"))
                    {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider,"foo.Generic1",
                                "foo.Generic2");
                        assertFindVariableResultProductions(
                                (VariableElement) element, provider);
                    }
                    else if (element.getSimpleName().contentEquals("myFieldC"))
                    {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider,"foo.Generic1");
                        assertFindVariableResultProductions(
                                (VariableElement) element, provider);
                    }
                    else if (element.getSimpleName().contentEquals("myFieldD"))
                    {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider);
                        assertFindVariableResultProductionsVar(
                                (VariableElement) element, provider,"productionFieldB");
                    }
                    else if (element.getSimpleName().contentEquals("myFieldE"))
                    {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider);
                        assertFindVariableResultProductions(
                                (VariableElement) element, provider,"productionMethodA");
                    }
                    else if (element.getSimpleName().contentEquals("myFieldF"))
                    {
                        assertFindVariableResultInjectables(
                                (VariableElement) element, provider);
                        assertFindVariableResultProductions(
                                (VariableElement) element, provider,"productionMethodB");
                    }
                }
                assert names.contains("myFieldA");
                assert names.contains("myFieldB");
                assert names.contains("myFieldC");
                assert names.contains("myFieldD");
                assert names.contains("myFieldE");
                assert names.contains("myFieldF");
                return null;
            }

        });
    }
}
