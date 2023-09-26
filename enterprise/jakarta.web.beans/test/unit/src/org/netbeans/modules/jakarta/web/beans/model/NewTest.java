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
package org.netbeans.modules.jakarta.web.beans.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
public class NewTest extends CommonTestCase {

    public NewTest( String testName ) {
        super(testName);
    }

    public void testNew() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "public class SuperClass  { " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One extends SuperClass {}" );

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

        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "import jakarta.inject.*; "+
                "public class TestClass  {" +
                " @Inject @New One myField1; "+
                " @Inject @New(Two.class) SuperClass myField2; "+
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import jakarta.enterprise.inject.*; "+
                "@Binding1 "+
                "public class Two extends SuperClass {}" );

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
                    if ( element.getSimpleName().contentEquals("myField1")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.One");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        assertFindVariableResultInjectables((VariableElement)element, provider, "foo.Two");
                        assertFindVariableResultProductions((VariableElement)element, provider);
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }
        });

    }

}
