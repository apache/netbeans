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
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
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
public class ObserversTest extends CommonTestCase {

    public ObserversTest(String testName){
        super( testName, false);
    }
    
    public void testSimple () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " +
                "public class EventObject { " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.inject.*; "+
                "import javax.enterprise.event.Event; "+
                "public class TestClass {" +
                " @Inject @foo.Binding Event<EventObject> event; " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.event.Observes; "+
                "public class Clazz {" +
                " public void eventObserver( @Observes @foo.Binding EventObject event ) {}" +
                "} ");
        
        inform("start simple observer test");
        
        MetadataModel<WebBeansModel> metaModel = createBeansModel() ;
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( ( VariableElement) element);
                    }
                }
                
                assertEquals(  1, injectionPoints.size());
                VariableElement var  = injectionPoints.get(0);
                assertEquals( var.getSimpleName().toString(), "event");
                List<ExecutableElement> observers = model.getObservers( var, 
                        (DeclaredType)mirror );
                assertEquals( "Should be exactly one observer method , but found " +
                        observers.size()  +" methods",  1, observers.size());
                ExecutableElement executableElement = observers.get(0);
                assertNotNull( executableElement );
                String name = executableElement.getSimpleName().toString();
                assertEquals(  "eventObserver" , name );
                return  null;
            }
        });
    }
    
    public void testCommon () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        createQualifier("Binding1");
        createQualifier("Binding2");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject.java",
                "package foo; " + 
                "public class SuperObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " + 
                "public interface Iface { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " + 
                "public class EventObject extends SuperObject implements Iface { " + 
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " + 
                "import javax.inject.*; "
                + "import javax.enterprise.event.Event; "
                + "public class TestClass {"
                + " @Inject @foo.Binding  @foo.Binding2 Event<EventObject> event; "
                + "} ");

        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz1 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 SuperObject event ) {}"
                + " public void method( @foo.Binding @foo.Binding2 EventObject event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz2 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 Iface event ) {}"
                + " public void notEventObserver( @Observes @foo.Binding @foo.Binding1 EventObject event ) {}"
                + "} ");

        inform("start common observer test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                assertEquals( 1, injectionPoints.size());
                VariableElement var = injectionPoints.get(0);
                assertEquals("event", var.getSimpleName().toString() );
                List<ExecutableElement> observers = model.getObservers(var,
                        (DeclaredType) mirror);
                assertEquals(
                        "Should be exactly two observer methods , but found "
                                + observers.size() + " methods",
                        2, observers.size());
                boolean foundSuper = false;
                boolean foundIface = false;
                for (ExecutableElement executableElement : observers) {
                    String name = executableElement.getSimpleName().toString();
                    assertEquals( "Found unexpected event observer method :"+
                            name ,name , "eventObserver");
                    TypeElement typeElement = model.getCompilationController().
                        getElementUtilities().enclosingTypeElement( executableElement );
                    String fqnType = typeElement.getQualifiedName().toString();
                    if ( "foo.Clazz1".equals(fqnType)){
                        foundSuper = true;
                    }
                    else if( "foo.Clazz2".equals( fqnType)){
                        foundIface = true;
                    }
                }
                assertTrue( "Observer method inside Clazz1 is not found", foundSuper );
                assertTrue( "Observer method inside Clazz2 is not found" , foundIface);
                return null;
            }
        });
    }
    
    public void testMembersQualifier () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
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
                "public @interface Binding1  {" +
                "    String value(); "+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
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
                "public @interface Binding2  {" +
                "    @Nonbinding String comment() default \"\"; "+
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject1.java",
                "package foo; " + 
                "public class SuperObject1 { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject2.java",
                "package foo; " + 
                "public class SuperObject2 extends SuperObject1 { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface1.java",
                "package foo; " + 
                "public interface Iface1 { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface2.java",
                "package foo; " + 
                "public interface Iface2 extends Iface1 { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " + 
                "public class EventObject extends SuperObject2 implements Iface2 { " + 
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " + 
                "import javax.inject.*; "
                + "import javax.enterprise.event.Event; "
                + "public class TestClass {"
                + " @Inject @foo.Binding2(comment=\"c\") @foo.Binding1(value=\"a\") Event<EventObject> event; "
                + " @Inject @foo.Binding @foo.Binding1(value=\"b\") Event<SuperObject1> event1; "
                + "} ");

        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz1 {"
                + " public void eventObserver( @Observes @foo.Binding " +
                		"@foo.Binding1(value=\"a\") @foo.Binding2(comment=\"other\")" +
                		" SuperObject1 event ) {}"
                + " public void eventObserver1( @Observes  " +
                		"@foo.Binding2(comment=\"any\") @foo.Binding1(value=\"a\") Iface1 event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz2 {"
                + " public void notEventObserver( @Observes @foo.Binding " +
                		"@foo.Binding2(comment=\"\") @foo.Binding1(value=\"b\") Iface2 event ) {}"
                + " public void notEventObserver2( @Observes @foo.Binding " +
                		"@foo.Binding1(value=\"a\") EventObject event ) {}"
                + " public void notEventObserver3( @Observes @foo.Binding " +
                    "@foo.Binding1(value=\"b\") Iface1 event ) {}"
                + "} ");

        inform("start observer test with binding members");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                assertEquals( 2, injectionPoints.size());
                VariableElement var1 = injectionPoints.get(0);
                VariableElement var2 = injectionPoints.get(1);
                String varName =var1.getSimpleName().toString();
                VariableElement var = "event".equals(varName) ? var1:var2;
                List<ExecutableElement> observers = model.getObservers(var,
                        (DeclaredType) mirror);
                assertEquals(
                        "Should be exactly two observer methods , but found "
                                + observers.size() + " methods",
                        2, observers.size());
                boolean foundSuper = false;
                boolean foundIface = false;
                for (ExecutableElement executableElement : observers) {
                    TypeElement typeElement = model.getCompilationController().
                        getElementUtilities().enclosingTypeElement( executableElement );
                    String fqnType = typeElement.getQualifiedName().toString();
                    assertEquals("Observer methods are defined only inside foo.Clazz1," +
                    		"but found : "+fqnType,"foo.Clazz1",fqnType);
                    String name = executableElement.getSimpleName().toString();
                    if ("eventObserver".equals(name)){
                        foundSuper = true;
                    }
                    else if( "eventObserver1".equals(name)){
                        foundIface = true;
                    }
                }
                assertTrue( "Observer method eventObserver inside Clazz1 is not found", foundSuper );
                assertTrue( "Observer method eventObserver1 inside Clazz1 is not found" , foundIface);
                return null;
            }
        });
    }
    
    public void testAny()throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
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
                "public @interface Binding1  {" +
                "    String value(); "+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject.java",
                "package foo; " + 
                "public class SuperObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " + 
                "public class Iface { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " + 
                "public class EventObject extends SuperObject implements Iface { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject1.java",
                "package foo; " + 
                "public class EventObject1  { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " + 
                "import javax.inject.*; "
                +"import javax.enterprise.inject.Any; "
                + "import javax.enterprise.event.Event; "
                + "public class TestClass {"
                + " @Inject @Any Event<EventObject> event; "
                + " @Inject Event<EventObject> event1; "
                + " @Inject @Binding1(value=\"a\") @Any Event<EventObject> event2; "
                + " @Inject @Binding @Any Event<EventObject1> event3; "
                + " @Inject @Binding Event<EventObject1> event4; "
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "+
                "import javax.enterprise.inject.Any; "
                + "public class Clazz1 {"
                + " public void eventObserver( @Observes SuperObject event ) {}"
                + " public void eventObserver1( @Observes  @Any Iface1 event ) {}"
                + " public void eventObserver2( @Observes  @Binding1(value=\"a\") EventObject event ) {}"
                + " public void eventObserver3( @Observes  @Binding1(value=\"a\") @Any EventObject event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "+
                "import javax.enterprise.inject.Any; "
                + "public class Clazz2 {"
                + " public void notEventObserver( @Observes @Any EventObject1 event ) {}"
                + " public void notEventObserver1( @Observes EventObject1 event ) {}"
                + "} ");
        
        inform("start @Any observer test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                assertEquals(5,  injectionPoints.size());
                for (VariableElement variableElement : injectionPoints) {
                    String name = variableElement.getSimpleName().toString();
                    if ( "event".equals(name)){
                        checkEvent( variableElement , model , (DeclaredType) mirror);
                    }
                    else if ( "event1".equals(name)){
                        checkEvent( variableElement , model , (DeclaredType) mirror);
                    }
                    else if ( "event2".equals(name)){
                        checkEvent2( variableElement , model , (DeclaredType) mirror);
                    }
                    else if ( "event3".equals(name)){
                        checkEvent3( variableElement , model , (DeclaredType) mirror);
                    }
                    else if ( "event4".equals(name)){
                        checkEvent3( variableElement , model , (DeclaredType) mirror);
                    }
                }
                return null;
            }


        });
        
    }
    
    private void checkEvent( VariableElement variableElement,
            WebBeansModel model , DeclaredType parent )
    {
        inform( "test " +variableElement.getSimpleName()+" field ");
        List<ExecutableElement> observers = model.getObservers(variableElement, parent);
        assertEquals(4,  observers.size());
        boolean observerFound = false;
        boolean observer1Found = false;
        boolean observer2Found = false;
        boolean observer3Found = false;
        for (ExecutableElement executableElement : observers) {
            TypeElement typeElement = model.getCompilationController().getElementUtilities().
                enclosingTypeElement( executableElement);
            String fqn = typeElement.getQualifiedName().toString();
            assertEquals( "foo.Clazz1", fqn);
            String name = executableElement.getSimpleName().toString();
            if ( "eventObserver".equals(name)){
                observerFound = true;
            }
            else if ( "eventObserver1".equals(name)){
                observer1Found = true;
            }
            else if ( "eventObserver2".equals(name)){
                observer2Found = true;
            }
            else if ( "eventObserver3".equals(name)){
                observer3Found = true;
            }
        }
        
        assertTrue ("Observer method 'foo.Clazz1.eventObserver' is not found", observerFound);
        assertTrue ("Observer method 'foo.Clazz1.eventObserver1' is not found", observer1Found);
        assertTrue ("Observer method 'foo.Clazz1.eventObserver2' is not found", observer2Found);
        assertTrue ("Observer method 'foo.Clazz1.eventObserver3' is not found", observer3Found);
    }
    
    private void checkEvent2( VariableElement variableElement,
            WebBeansModel model, DeclaredType parent )
    {
        inform( "test " +variableElement.getSimpleName()+" field ");
        List<ExecutableElement> observers = model.getObservers(variableElement, parent);
        assertEquals(2,  observers.size());
        boolean observer2Found = false;
        boolean observer3Found = false;
        for (ExecutableElement executableElement : observers) {
            TypeElement typeElement = model.getCompilationController().getElementUtilities().
                enclosingTypeElement( executableElement);
            String fqn = typeElement.getQualifiedName().toString();
            assertEquals( "foo.Clazz1", fqn);
            String name = executableElement.getSimpleName().toString();
            if ( "eventObserver2".equals(name)){
                observer2Found = true;
            }
            else if ( "eventObserver3".equals(name)){
                observer3Found = true;
            }
        }
        
        assertTrue ("Observer method 'foo.Clazz1.eventObserver2' is not found", observer2Found);
        assertTrue ("Observer method 'foo.Clazz1.eventObserver3' is not found", observer3Found);        
    }
    
    private void checkEvent3( VariableElement variableElement,
            WebBeansModel model, DeclaredType parent  )
    {
        inform( "test " +variableElement.getSimpleName()+" field ");       
        List<ExecutableElement> observers = model.getObservers(variableElement, parent);
        if ( observers.size() >0 ){
            ExecutableElement executableElement = observers.get( 0 );
            TypeElement typeElement = model.getCompilationController().getElementUtilities().
                enclosingTypeElement( executableElement);
            String fqn = typeElement.getQualifiedName().toString();
            assertTrue( "Found unexpected observer method '"+fqn+"."+
                    executableElement.getSimpleName(), false );
        }
    }
    
    public void testRawParameterizedAssignability()throws MetadataModelException, 
        IOException, InterruptedException 
    {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding.java",
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
                "public @interface Binding  {" +
                "    String value(); "+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " + 
                "public class EventObject extends SuperObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject.java",
                "package foo; " + 
                "public class SuperObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/ChildObject.java",
                "package foo; " + 
                "public class ChildObject extends EventObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " + 
                "import javax.inject.*; "
                +"import javax.enterprise.inject.Any; "
                + "import javax.enterprise.event.Event; "
                + "import java.util.List; "
                + "import java.util.Set; "
                + "import java.util.Collection; "
                + "public class TestClass {"
                + " @Inject @Binding(value=\"a\") Event<EventObject> event; "
                + " @Inject @Binding(value=\"b\") Event<List<String>> event1; "
                + " @Inject @Binding(value=\"c\") Event<List<EventObject>> event2; "
                + " @Inject @Binding(value=\"d\") Event<Collection<EventObject>> event3; "
                + " @Inject @Binding(value=\"e\") Event<Collection<EventObject>> event4; "
                + " @Inject @Binding(value=\"f\") Event<EventObject> event5; "
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Generic.java",
                "package foo; " + 
                "import javax.enterprise.event.Observes; "+
                "public class Generic<T extends SuperObject> { " + 
                " public void eventObserver( @Observes @Binding(value=\"a\") T t){} "+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " + 
                "import javax.enterprise.event.Observes; "+
                 "import java.util.List; "
                + "import java.util.Collection; "
                + "import java.util.Set; "
                +"public class Clazz { " + 
                " public void eventObserver( @Observes @Binding(value=\"b\") List list){} "+
                " public <T extends SuperObject> void eventObserver1( @Observes @Binding(value=\"c\") List<T> list){} "+
                " public void eventObserver2( @Observes @Binding(value=\"d\") Collection<? extends SuperObject> list){} "+
                " public void eventObserver3( @Observes @Binding(value=\"e\") Collection<? super ChildObject> list){} "+
                " public <T extends SuperObject> void eventObserver4( @Observes @Binding(value=\"f\") T t){} "+
                "} ");
        
        inform("start raw and parameterized assignability observer test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                assertEquals(6,  injectionPoints.size());
                for (VariableElement variableElement : injectionPoints) {
                    String name = variableElement.getSimpleName().toString();
                    if ( "event".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Generic" ,
                                "eventObserver");
                    }
                    else if ( "event1".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Clazz" ,
                                "eventObserver");
                    }
                    else if ( "event2".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Clazz" ,
                                "eventObserver1");
                    }
                    else if ( "event3".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Clazz" ,
                                "eventObserver2");
                    }
                    else if ( "event4".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Clazz" ,
                                "eventObserver3");
                    }
                    else if ( "event5".equals(name)){
                        commonAssagnabilityCheck(variableElement, model, 
                                (DeclaredType) mirror, "foo.Clazz" ,
                                "eventObserver4");
                    }
                }
                return null;
            }

        });
    }
    
    private void commonAssagnabilityCheck( VariableElement variableElement,
            WebBeansModel model, DeclaredType parent , String enclosingClassName ,
            String methodName )
    {
        inform( "assignability test for " +variableElement.getSimpleName()+" field ");       
        List<ExecutableElement> observers = model.getObservers(variableElement, parent);
        assertEquals(1,  observers.size());
        ExecutableElement executableElement = observers.get(0);
        assertNotNull( executableElement );
        
        String foundMethod = executableElement.getSimpleName().toString();
        assertEquals("Expected "+methodName+" observer but found :"+foundMethod,
                methodName, foundMethod);
        
        TypeElement methodClass = model.getCompilationController().getElementUtilities().
            enclosingTypeElement( executableElement);
        
        
        String name = methodClass.getQualifiedName().toString();
        assertEquals( "Enclosing type of observer method "+methodName+" should " +
        		"be "+enclosingClassName+" but found : "+name ,enclosingClassName , name);
    }
    
    
}
