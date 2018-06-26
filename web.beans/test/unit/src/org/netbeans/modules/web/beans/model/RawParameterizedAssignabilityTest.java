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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
public class RawParameterizedAssignabilityTest extends CommonTestCase {

    public RawParameterizedAssignabilityTest( String testName ) {
        super(testName);
    }

    public void testGenerics() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {" +
                " String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class CustomClass<E extends Two>  {" +
                " @Inject @CustomBinding(\"a\") Generic myField1; "+
                " @Inject @CustomBinding(\"b\") Generic myField2; "+
                " @Inject @CustomBinding(\"b\") Generic<One> myField3; "+
                " @Inject @CustomBinding(\"c\") Generic<? extends One> myField4; "+
                " @Inject @CustomBinding(\"b\") Generic<? super Three> myField5; "+
                " @Inject @CustomBinding(\"d\") Generic4<? extends One> myField6; "+
                " @Inject @CustomBinding(\"e\") Generic5<? super Two> myField7; "+
                " @Inject @CustomBinding(\"d\") Generic4<Two> myField8; "+
                " @Inject @CustomBinding(\"d\") Generic4<E> myField9; "+
                " @Inject @CustomBinding(\"d\") Generic<E> myField10; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " +
                " @CustomBinding(\"a\") "+ 
                "public class Generic<T>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic1.java",
                "package foo; " +
                " @CustomBinding(\"b\") "+ 
                "public class Generic1 extends Generic<Object>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "public class Three extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic2.java",
                "package foo; " +
                " @CustomBinding(\"b\") "+ 
                "public class Generic2 extends Generic<Two>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic3.java",
                "package foo; " +
                " @CustomBinding(\"c\") "+ 
                "public class Generic3 extends Generic<Three>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic4.java",
                "package foo; " +
                " @CustomBinding(\"d\") "+ 
                "public class Generic4<T extends One>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic5.java",
                "package foo; " +
                " @CustomBinding(\"e\") "+ 
                "public class Generic5<T super One>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic6.java",
                "package foo; " +
                " @CustomBinding(\"a\") "+ 
                "public class Generic6<T> extends Generic<T>  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic7.java",
                "package foo; " +
                " @CustomBinding(\"d\") "+ 
                "public class Generic7<T extends One> extends Generic<T> {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic8.java",
                "package foo; " +
                " @CustomBinding(\"d\") "+ 
                "public class Generic8<T extends Three> extends Generic<T> {}");
        
        inform("start generic types tests");
        
        
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
                    if (element instanceof VariableElement) {
                        injectionPoints.add( (VariableElement)element );
                    }
                }
                Set<String> names = new HashSet<String>();
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic",
                                "foo.Generic6");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic1");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic2");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField4")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic3");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField5")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic1");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField6")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic4");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField7")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic5");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField8")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic4");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField9")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic4");
                        assertFindVariableResultProductions(element, provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField10")){
                        assertFindVariableResultInjectables(element, provider, "foo.Generic7");
                        assertFindVariableResultProductions(element, provider);
                    }
                }

                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                assert names.contains("myField4");
                assert names.contains("myField5");
                assert names.contains("myField6");
                assert names.contains("myField7");
                assert names.contains("myField8");
                assert names.contains("myField9");
                assert names.contains("myField10");
                
                return null;
            }
            });
        }
    
    public void testProductions() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {" +
                " String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.util.*; "+
                "public class CustomClass<E extends One>  {" +
                " @Inject @CustomBinding(\"a\") Class<One> myField1; "+
                " @Inject @CustomBinding(\"b\") List<One> myField2; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import java.util.*; "+
                "public class Generic<T>  {" +
                " @Produces @CustomBinding(\"a\")  Class<T> getClass(){ " +
                "   return null; " +
                "}"+
                " @Produces @CustomBinding(\"b\") List<T> myList; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class Generic1 extends Generic<Two>  {" +
                "}");
        
    
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
                    if (element instanceof VariableElement) {
                        injectionPoints.add( (VariableElement)element );
                    }
                }
                Set<String> names = new HashSet<String>();
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    // Productions are not inherited by child classes.
                    if ( element.getSimpleName().contentEquals("myField1")){
                        assertFindVariableResultInjectables(element, provider);
                        assertFindAllProductions(element, provider, "getClass",
                                "foo.Generic" );
                    }
                    if ( element.getSimpleName().contentEquals("myField2")){
                        assertFindVariableResultInjectables(element, provider);
                        assertFindAllProductions(element, provider,
                                "myList", "foo.Generic");
                    }
                }

                assert names.contains("myField1");
                assert names.contains("myField2");
                
                return null;
            }
            });
    }
    
}

