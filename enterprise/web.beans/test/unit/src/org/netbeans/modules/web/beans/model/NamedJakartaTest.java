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
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import static junit.framework.TestCase.assertEquals;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class NamedJakartaTest extends CommonTestCase {

    public NamedJakartaTest( String testName ) {
        super(testName, true);
    }

    public void testPlainNamed() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                " @Named "+
                "public class One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                " @Named(\"twoClass\") "+
                "public class Two extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                " @Named "+
                "public interface Iface {}" );


        TestUtilities.copyStringToFileObject(srcFO, "foo/CApital.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                " @Named "+
                "public class CApital {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Tree.java",
                "package foo; " +
                "import jakarta.inject.*; "+
                "public class Tree {" +
                " @Named int myField1; "+
                " @Named(\"field\") int myField2; "+
                " @Named void method(){} "+
                " @Named int getValue(){ return 0; } "+
                " @Named(\"stringGetter\") String getString(){ return null; } "+
                "}" );

        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                List<Element> namedElements = model.getNamedElements();
                Element one = null;
                Element two = null;
                Element iface = null;
                Element field1 = null;
                Element field2 = null;
                Element method = null;
                Element value = null;
                Element getter = null;
                for (Element element : namedElements) {
                    if ( element instanceof TypeElement ){
                        String fqn = ((TypeElement)element).getQualifiedName().toString();
                        if ( "foo.One".equals(fqn)){
                            one = element;
                            String name = model.getName(element);
                            assertEquals("one", name);
                        }
                        else if ( "foo.Two".equals( fqn )){
                            two = element;
                            String name = model.getName(element);
                            assertEquals("twoClass", name);
                        }
                        else if ( "foo.Iface".equals( fqn )){
                            iface = element;
                            String name = model.getName(element);
                            assertEquals("iface", name);
                        }
                        // #249438
                        else if ("foo.CApital".equals(fqn)) {
                            iface = element;
                            String name = model.getName(element);
                            assertEquals("CApital", name);
                        }
                    }
                    else if ( element instanceof VariableElement ){
                        String name = element.getSimpleName().toString();
                        if ( "myField1".equals( name )){
                            field1 = element;
                            name = model.getName(element);
                            assertEquals("myField1", name);
                        }
                        else if ( "myField2".equals(name )){
                            field2 = element;
                            name = model.getName(element);
                            assertEquals("field", name);
                        }
                    }
                    else if ( element instanceof ExecutableElement ){
                        String name = element.getSimpleName().toString();
                        if ( "method".equals( name )){
                            method = element;
                            name = model.getName(element);
                            assertEquals("method", name);
                        }
                        else if ( "getValue".equals(name )){
                            value = element;
                            name = model.getName(element);
                            assertEquals("value", name);
                        }
                        else if ( "getString".equals(name )){
                            getter = element;
                            name = model.getName(element);
                            assertEquals("stringGetter", name);
                        }
                    }
                }

                assertNotNull( one );
                assertNotNull( two );
                assertNotNull( iface );
                assertNotNull( field1 );
                assertNotNull( field2 );
                assertNotNull( method );
                assertNotNull( value );
                assertNotNull( getter );
                return null;
            }
        });

    }

    public void testStereotypeNamed() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                " @Stereotype1 "+
                "public class One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "@Stereotype2 "+
                "public class Three {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two {" +
                " @Stereotype1 int myField1; "+
                " @Stereotype2 int myField2; "+
                " @Stereotype1 void method(){} "+
                " @Stereotype2 int getValue(){ return 0; } "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+
                "@Retention(RUNTIME) "+
                "@Named "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
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
                "@Stereotype1 "+
                "public @interface Stereotype2 {}" );

        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                List<Element> namedElements = model.getNamedElements();
                Element one = null;
                Element three = null;
                Element field1 = null;
                Element field2 = null;
                Element method = null;
                Element value = null;
                for (Element element : namedElements) {
                    if ( element instanceof TypeElement ){
                        String fqn = ((TypeElement)element).getQualifiedName().toString();
                        if ( "foo.One".equals(fqn)){
                            one = element;
                            String name = model.getName(element);
                            assertEquals("one", name);
                        }
                        else if ( "foo.Three".equals( fqn )){
                            three = element;
                            String name = model.getName(element);
                            assertEquals("three", name);
                        }
                    }
                    else if ( element instanceof VariableElement ){
                        String name = element.getSimpleName().toString();
                        if ( "myField1".equals( name )){
                            field1 = element;
                            name = model.getName(element);
                            assertEquals("myField1", name);
                        }
                        else if ( "myField2".equals(name )){
                            field2 = element;
                            name = model.getName(element);
                            assertEquals("myField2", name);
                        }
                    }
                    else if ( element instanceof ExecutableElement ){
                        String name = element.getSimpleName().toString();
                        if ( "method".equals( name )){
                            method = element;
                            name = model.getName(element);
                            assertEquals("method", name);
                        }
                        else if ( "getValue".equals(name )){
                            value = element;
                            name = model.getName(element);
                            assertEquals("value", name);
                        }
                    }
                }

                assertNotNull( one );
                assertNotNull( three );
                assertNotNull( field1 );
                assertNotNull( field2 );
                assertNotNull( method );
                assertNotNull( value );
                return null;
                }
        });

    }

    public void testSpecializesNamed() throws IOException {

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                " @Named "+
                "public class One {" +
                " @Produces @Named(\"method\") void operation(){} "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                " @Specializes "+
                "public class Three extends One {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                " @Named(\"explicit\") "+
                "public class One1 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                " @Specializes "+
                "public class Two1 extends One1 {}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class Two extends One {" +
                " @Produces @Specializes void operation(){} "+
                "}" );

        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                List<Element> namedElements = model.getNamedElements();
                Element one = null;
                Element two = null;
                Element three = null;
                Element one1 = null;
                Element two1 = null;
                Element operation1 = null;
                Element operation2 = null;
                for (Element element : namedElements) {
                    if ( element instanceof TypeElement ){
                        String fqn = ((TypeElement)element).getQualifiedName().toString();
                        if ( "foo.One".equals(fqn)){
                            one = element;
                            String name = model.getName(element);
                            assertEquals("one", name);
                        }
                        if ( "foo.Two".equals(fqn)){
                            two = element;
                        }
                        else if ( "foo.Three".equals( fqn )){
                            three = element;
                            String name = model.getName(element);
                            assertEquals("three", name);
                        }
                        if ( "foo.One1".equals(fqn)){
                            one1 = element;
                            String name = model.getName(element);
                            assertEquals("explicit", name);
                        }
                        if ( "foo.Two1".equals(fqn)){
                            two1 = element;
                            String name = model.getName(element);
                            assertEquals("explicit", name);
                        }
                    }
                    else if ( element instanceof ExecutableElement ){
                        String name = element.getSimpleName().toString();
                        if ( "operation".equals( name )){
                            Element enclosingElement = element.getEnclosingElement();
                            if ( enclosingElement.getSimpleName().
                                    contentEquals("One")){
                                operation1 = element;
                                name = model.getName(element);
                                assertEquals("method", name);
                            }
                            else if ( enclosingElement.getSimpleName().
                                    contentEquals("Two")){
                                operation2 = element;
                                name = model.getName(element);
                                assertEquals("method", name);
                            }
                        }
                    }
                }

                assertNotNull( one );
                assertNull( two );
                assertNotNull( three );
                assertNotNull( one1 );
                assertNotNull( two1 );
                assertNotNull( operation1 );
                assertNotNull( operation2 );
                return null;
                }
        });

    }
}
