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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResultKind;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class ProgrammaticTest extends CommonTestCase {

    public ProgrammaticTest( String testName ) {
        super(testName);
    }
    
    public void testProgrammatic() throws IOException{
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding "+
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding "+
                "@Alternative "+
                "public class Three extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding Instance<One> myField1; "+
                " @Inject @Binding One myField2; "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
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
                        assertTrue ( "myField1 should be recognized as programmatic " +
                        		"injection point",model.isDynamicInjectionPoint(element));
                        DependencyInjectionResult injectables = model.lookupInjectables(element, null, new AtomicBoolean(false));
                        ResultKind kind = injectables.getKind();
                        assertEquals(ResultKind.INJECTABLES_RESOLVED, kind);
                        check(element, injectables, kind);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        assertFalse ( "myField2 should be recognized as programmatic " +
                                "injection point",model.isDynamicInjectionPoint(element));
                        DependencyInjectionResult injectables = model.lookupInjectables(element, null, new AtomicBoolean(false));
                        ResultKind kind = injectables.getKind();
                        assertEquals(ResultKind.RESOLUTION_ERROR, kind);
                        assertTrue( injectables instanceof DependencyInjectionResult.Error );
                        check(element, injectables, kind);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }


        });
    }
    
    private void check( VariableElement element, DependencyInjectionResult injectables,
            ResultKind kind )
    {
        assertTrue( injectables instanceof DependencyInjectionResult.ApplicableResult );
        assertTrue( injectables instanceof DependencyInjectionResult.ResolutionResult );
        Set<TypeElement> typeElements = 
            ((DependencyInjectionResult.ApplicableResult)injectables).getTypeElements();
        assertEquals("Incorrect number of eligible elemets are found",
                3, typeElements.size());
        for( TypeElement type : typeElements ){
            if ( type.getQualifiedName().contentEquals("foo.Three")){
               assertTrue(  "foo.Three element is enabled",
                       ((DependencyInjectionResult.ApplicableResult)injectables).isDisabled(element));
            }
        }
    }

}
