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
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
public class DecoratorJakartaTest extends CommonTestCase {

    public DecoratorJakartaTest( String testName ) {
        super(testName, true);
    }

    public void testSimple() throws IOException, InterruptedException {
        createQualifier("Binding");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject SimpleTest1( @Delegate One param ) {} "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding "+
                "public class Two extends One {}");

        inform("start simple  decorators tests");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" , "foo.SimpleTest1" );

                checkDecorator(model, "foo.Two" , "foo.SimpleTest");

                return null;
            }

        });
    }

    public void testMultipleQualifiers() throws IOException, InterruptedException {
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
                "import jakarta.enterprise.util.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {" +
                "    String value(); "+
                "    @Nonbinding String comment() default \"\"; "+
                "}");
        createQualifier("Binding2");
        createQualifier("Binding3");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding1(value=\"a\", comment=\"a\") @Binding2 One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject @Delegate @Binding2 One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1(value=\"a\", comment=\"b\") "+
                "public class One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Binding2 "+
                "@Specializes "+
                "public class Two extends One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Binding2 @Binding3 "+
                "public class Three extends One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Binding1(value=\"b\", comment=\"a\") @Binding3 "+
                "public class Four extends One {}");

        inform("start multiple decorators tests");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" );

                checkDecorator(model, "foo.Two" , "foo.SimpleTest", "foo.SimpleTest1");

                checkDecorator(model, "foo.Three" ,  "foo.SimpleTest1");

                checkDecorator(model, "foo.Four" );

                return null;
            }

        });
    }

    /*
     * @Default, @New , @Any
     */
    public void testCornerCasesQualifiers() throws IOException, InterruptedException {
        createQualifier("Binding1");
        createQualifier("Binding2");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding1 @Default One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject @Delegate One myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest2.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest2 extends One {"+
                " @Inject @Delegate @Any Three myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest3.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "import jakarta.decorator.Decorator; "+
                "import jakarta.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest3 extends One {"+
                " @Inject @Delegate @New Four myField; "+
                "}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1 "+
                "public class One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Specializes "+
                "public class Two extends One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "public class Three extends One {}");

        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "@Default @Named "+
                "public class Four extends One {}");

        inform("start multiple decorators tests");

        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();

        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" );

                checkDecorator(model, "foo.Two" , "foo.SimpleTest", "foo.SimpleTest1");

                checkDecorator(model, "foo.Three" ,  "foo.SimpleTest1", "foo.SimpleTest2");

                checkDecorator(model, "foo.Four" ,  "foo.SimpleTest1", "foo.SimpleTest3");

                return null;
            }

        });
    }

    private void checkDecorator( WebBeansModel model, String className ,
            String... decorators )
    {
        TypeMirror mirror = model.resolveType( className);
        Element clazz = ((DeclaredType)mirror).asElement();
        TypeElement type = (TypeElement)clazz;

        Collection<TypeElement> decoratorElements = model.getDecorators( type );
        assertEquals( "Class "+className+" should have exactly "+
                decorators.length+" decorator(s)", decorators.length ,
                decoratorElements.size());
        Set<String> set =new HashSet<String>();
        set.addAll( Arrays.asList( decorators ));

        Set<String> fqns = new HashSet<String>();
        for (TypeElement typeElement : decoratorElements) {
            fqns.add( typeElement.getQualifiedName().toString());
        }
        assertTrue( fqns.containsAll( set ));
    }

}
