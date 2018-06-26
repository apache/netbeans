/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
