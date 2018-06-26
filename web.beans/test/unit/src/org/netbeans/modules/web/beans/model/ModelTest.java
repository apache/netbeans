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
public class ModelTest extends CommonTestCase {

    public ModelTest( String testName ) {
        super(testName);
    }
    
    public void testInjectionPointInitialization() throws MetadataModelException, 
        IOException, InterruptedException 
    {
        createQualifier("CustomBinding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                assertEquals(DependencyInjectionResult.ResultKind.DEFINITION_ERROR,  
                        result.getKind());
                assertTrue( result instanceof DependencyInjectionResult.Error);
                
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
                "import javax.enterprise.inject.*; "+
		"import javax.enterprise.util.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.enterprise.util.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "@foo.CustomBinding() " +
                "public class Three  { " +
                " @Produces @foo.CustomBinding(value=\"d\") " +
                "int productionField =1; " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@foo.CustomBinding(\"c\") "+
                "public class Generic<T extends foo.MyThread>  {" +
                " @Produces @foo.CustomBinding(value=\"e\") foo.MyThread getThread(){" +
                " return null; } "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.Default; " +
                "@Slow @Default " +
                "public class SlowProducer implements Producer { " +
                "  public List<String> getItems() { return null;} " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/FastProducer.java",
                "package foo; " +
                "import java.util.List; " +
                "import javax.enterprise.inject.Default; " +
                "@Fast @Default " +
                "public class FastProducer implements Producer { " +
                "  public List<String> getItems() { return null;} " +
                "}" );

        TestUtilities.copyStringToFileObject(srcFO, "foo/User.java",
                "package foo; " +
                "import java.util.List; " +
                "import javax.inject.*; "+
                "import javax.enterprise.inject.Default; " +
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
                "import javax.enterprise.inject.*; "+
		"import javax.enterprise.util.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "import javax.enterprise.util.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
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
                "import javax.enterprise.inject.*; "+
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
                "import javax.inject.*; "+
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
