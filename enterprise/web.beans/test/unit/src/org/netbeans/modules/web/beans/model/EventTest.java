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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class EventTest extends CommonTestCase {

    public EventTest(String testName ){
        super( testName);
    }
    
    public void testSimple () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " +
                "public class EventObject { " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.inject.*; "+
                "import javax.enterprise.event.Event; "+
                "public class Clazz {" +
                " @Inject @foo.Binding Event<EventObject> event; " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.event.Observes; "+
                "public class TestClass {" +
                " public void eventObserver( @Observes @foo.Binding EventObject event ) {}" +
                "} ");
        
        inform("start simple event test");
        
        MetadataModel<WebBeansModel> metaModel = createBeansModel() ;
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<ExecutableElement> methods = ElementFilter.methodsIn( children );
                
                assertEquals(  1, methods.size());
                ExecutableElement method  = methods.get(0);
                assertEquals( method.getSimpleName().toString(), "eventObserver");
                List<VariableElement> events = model.getEventInjectionPoints( method, 
                        (DeclaredType)mirror );
                assertEquals( "Should be exactly one event injection , but found " +
                        events.size()  +" events",  1, events.size());
                VariableElement var = events.get(0);
                assertNotNull( var );
                String name = var.getSimpleName().toString();
                assertEquals(  "event" , name );
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

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " + 
                "import javax.inject.*; "
                + "import javax.enterprise.event.Event; "
                + "public class Clazz {"
                + " @Inject @foo.Binding  @foo.Binding2 Event<EventObject> event; "
                + "} ");

        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class TestClass1 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 SuperObject event ) {}"
                + " public void method( @foo.Binding @foo.Binding2 EventObject event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class TestClass2 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 Iface event ) {}"
                + " public void notEventObserver( @Observes @foo.Binding @foo.Binding1 EventObject event ) {}"
                + "} ");

        inform("start common event test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                commonCheck(model, "foo.TestClass1", false);
                commonCheck(model, "foo.TestClass2", true);
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

        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " + 
                "import javax.inject.*; "
                + "import javax.enterprise.event.Event; "
                + "public class Clazz {"
                + " @Inject @foo.Binding2(comment=\"c\") @foo.Binding1(value=\"a\") Event<EventObject> event; "
                + " @Inject @foo.Binding @foo.Binding1(value=\"b\") Event<SuperObject1> event1; "
                + "} ");

        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class TestClass1 {"
                + " public void eventObserver( @Observes @foo.Binding " +
                        "@foo.Binding1(value=\"a\") @foo.Binding2(comment=\"other\")" +
                        " SuperObject1 event ) {}"
                + " public void eventObserver1( @Observes  " +
                        "@foo.Binding2(comment=\"any\") @foo.Binding1(value=\"a\") Iface1 event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class TestClass2 {"
                + " public void notEventObserver( @Observes @foo.Binding " +
                        "@foo.Binding2(comment=\"\") @foo.Binding1(value=\"b\") Iface2 event ) {}"
                + " public void notEventObserver2( @Observes @foo.Binding " +
                        "@foo.Binding1(value=\"a\") EventObject event ) {}"
                + " public void notEventObserver3( @Observes @foo.Binding " +
                    "@foo.Binding1(value=\"b\") Iface1 event ) {}"
                + "} ");

        inform("start event test with binding members");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                unmatchedObserversCheck(model, 3);
                
                bindingMembersCheck( model );
 
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
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " + 
                "import javax.inject.*; "
                +"import javax.enterprise.inject.Any; "
                + "import javax.enterprise.event.Event; "
                + "public class Clazz {"
                + " @Inject @Any Event<EventObject> event; "
                + " @Inject Event<EventObject> event1; "
                + " @Inject @Binding1(value=\"a\") @Any Event<EventObject> event2; "
                + " @Inject @Binding @Any Event<EventObject1> event3; "
                + " @Inject @Binding Event<EventObject1> event4; "
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "+
                "import javax.enterprise.inject.Any; "
                + "public class TestClass1 {"
                + " public void eventObserver( @Observes SuperObject event ) {}"
                + " public void eventObserver1( @Observes  @Any Iface1 event ) {}"
                + " public void eventObserver2( @Observes  @Binding1(value=\"a\") EventObject event ) {}"
                + " public void eventObserver3( @Observes  @Binding1(value=\"a\") @Any EventObject event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/TestClass2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "+
                "import javax.enterprise.inject.Any; "
                + "public class TestClass2 {"
                + " public void notEventObserver( @Observes @Any EventObject1 event ) {}"
                + " public void notEventObserver1( @Observes EventObject1 event ) {}"
                + "} ");
        
        inform("start @Any event test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                unmatchedObserversCheck(model, 2);
               
                anyEventsCheck( model );
                return null;
            }

        });
        
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
    
    TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
            "package foo; " + 
            "import javax.inject.*; "
            +"import javax.enterprise.inject.Any; "
            + "import javax.enterprise.event.Event; "
            + "import java.util.List; "
            + "import java.util.Set; "
            + "import java.util.Collection; "
            + "public class Clazz {"
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
    
    TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
            "package foo; " + 
            "import javax.enterprise.event.Observes; "+
             "import java.util.List; "
            + "import java.util.Collection; "
            + "import java.util.Set; "
            +"public class TestClass { " + 
            " public void eventObserver( @Observes @Binding(value=\"b\") List list){} "+
            " public <T extends SuperObject> void eventObserver1( @Observes @Binding(value=\"c\") List<T> list){} "+
            " public void eventObserver2( @Observes @Binding(value=\"d\") Collection<? extends SuperObject> list){} "+
            " public void eventObserver3( @Observes @Binding(value=\"e\") Collection<? super ChildObject> list){} "+
            " public <T extends SuperObject> void eventObserver4( @Observes @Binding(value=\"f\") T  t){} "+
            "} ");
    
    inform("start raw and parameterized assignability event test");

    MetadataModel<WebBeansModel> metaModel = createBeansModel();
    metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

        public Void run( WebBeansModel model ) throws Exception {
            TypeMirror mirror = model.resolveType("foo.TestClass");
            Element clazz = ((DeclaredType) mirror).asElement();
            List<? extends Element> children = clazz.getEnclosedElements();
            List<ExecutableElement> methods = ElementFilter.methodsIn( children );
            
            assertEquals(5, methods.size());
            Map<String, String> observer2Field = new HashMap<String, String>();
            for ( int i=0 ; i<5; i++){
                int eventIndex = i+1;
                String observer = null;
                if ( i==0 ){
                    observer = "eventObserver";
                }
                else {
                    observer = "eventObserver"+i;
                }
                observer2Field.put(observer, "event"+eventIndex);
            }
            
            for (ExecutableElement method : methods) {
                Name simpleName = method.getSimpleName();
                List<VariableElement> eventInjectionPoints = 
                    model.getEventInjectionPoints( method , (DeclaredType) mirror);
                assertEquals( "Found "+eventInjectionPoints.size()+" event " +
                		"injections for '"+simpleName+"' method", 1,  
                		eventInjectionPoints.size());
                VariableElement variableElement = eventInjectionPoints.get(0);
                TypeElement enclosingType = model.getCompilationController().getElementUtilities().
                    enclosingTypeElement( variableElement);
                assertEquals("Unexpected enclosing type for event injection :"+
                        variableElement.getSimpleName(), "foo.Clazz",  
                        enclosingType.getQualifiedName().toString());
                String varName  = variableElement.getSimpleName().toString();
                assertEquals( "Observer '"+simpleName+"' has wrong event injectable", 
                        observer2Field.get( simpleName.toString()), varName );
            }
            
            
            mirror = model.resolveType("foo.Generic");
            clazz = ((DeclaredType) mirror).asElement();
            children = clazz.getEnclosedElements();
            methods = ElementFilter.methodsIn( children );
            assertEquals(1,  methods.size());
            ExecutableElement method = (ExecutableElement)methods.get(0 );
            
            List<VariableElement> eventInjectionPoints = 
                    model.getEventInjectionPoints(method, (DeclaredType) mirror);
            assertEquals("Expected only one event injection for observer method " +
            		"'foo.Generic.eventObserver'", 1,  eventInjectionPoints.size());
            VariableElement variableElement = eventInjectionPoints.get(0);
            TypeElement enclosingType = model.getCompilationController().getElementUtilities().
                enclosingTypeElement( variableElement);
            assertEquals("Unexpected enclosing type for event injection :"+
                    variableElement.getSimpleName(), "foo.Clazz",  
                    enclosingType.getQualifiedName().toString());
            String varName  = variableElement.getSimpleName().toString();
            assertEquals( "Observer 'foo.Generic.eventObserver' has wrong event injectable", 
                "event", varName );
            
            return null;
        }

    });
    }
    
    private void anyEventsCheck( WebBeansModel model ) {
        TypeMirror mirror = model.resolveType("foo.TestClass1");
        Element clazz = ((DeclaredType) mirror).asElement();
        List<? extends Element> children = clazz.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn( children );
        assertEquals(4, methods.size());
        for (ExecutableElement method : methods) {
            Name simpleName = method.getSimpleName();
            List<VariableElement> eventInjectionPoints = 
                model.getEventInjectionPoints( method , (DeclaredType) mirror);
            Set<String> fields = new HashSet<String>();
            for (VariableElement variableElement : eventInjectionPoints) {
                String name = variableElement.getSimpleName().toString();
                fields.add( name );
                TypeElement containingClass = model.getCompilationController().getElementUtilities().
                    enclosingTypeElement( variableElement);
                assertEquals("Event injection points are expected to be in 'foo.Clazz';" +
                		"but found injection point '"+name +"' in the class '"
                		+containingClass.getQualifiedName(), "foo.Clazz",  
                		containingClass.getQualifiedName().toString());
            }
            
            if ( "eventObserver".contentEquals(simpleName)||
                    "eventObserver1".contentEquals(simpleName))
            {
                assertEquals("Expected exactly 2 event injections for observer "+
                        simpleName+" but found :" +fields.size(), 2,  fields.size());
                assertTrue( "Expected 'event' field as injection point", fields.contains("event"));
                assertTrue( "Expected 'event1' field as injection point", fields.contains("event1"));
            }
            else if ( "eventObserver2".contentEquals(simpleName) ||
                    "eventObserver3".contentEquals(simpleName))
            {
                assertEquals("Expected exactly 3 event injections for observer "+
                        simpleName+" but found :" +fields.size(), 3,  fields.size());
                assertTrue( "Expected 'event' field as injection point", fields.contains("event"));
                assertTrue( "Expected 'event1' field as injection point", fields.contains("event1"));
                assertTrue( "Expected 'event2' field as injection point", fields.contains("event2"));
            }
        }
    }

    private void unmatchedObserversCheck( WebBeansModel model , int size ) {
        TypeMirror mirror = model.resolveType("foo.TestClass2");
        Element clazz = ((DeclaredType) mirror).asElement();
        List<? extends Element> children = clazz.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn( children );
        assertEquals(size, methods.size());
        for (ExecutableElement method : methods) {
            List<VariableElement> eventInjectionPoints = model.
                getEventInjectionPoints(method, (DeclaredType) mirror);
            if ( eventInjectionPoints.size() >0 ){
                VariableElement variableElement = eventInjectionPoints.get(0);
                TypeElement containingType = model.getCompilationController().getElementUtilities().
                    enclosingTypeElement(variableElement);
                assertTrue( "Found unexpected observer 'foo.TestClass2."+
                        method.getSimpleName()+"' for event injection" +
                                " point :'"+containingType.getQualifiedName()
                                +"."+variableElement.getSimpleName()+"'", false );
            }
        }
    }
    
    private void bindingMembersCheck( WebBeansModel model ) {
        TypeMirror mirror = model.resolveType("foo.TestClass1");
        Element clazz = ((DeclaredType) mirror).asElement();
        List<? extends Element> children = clazz.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn( children );
        assertEquals(2, methods.size());
        for (ExecutableElement method : methods) {
            Name simpleName = method.getSimpleName();
            List<VariableElement> eventInjectionPoints = model.
                getEventInjectionPoints( method, (DeclaredType) mirror);
            assertEquals("Observer "+simpleName+" matches "+eventInjectionPoints.size()
                    +" events. But should match exactly one", 1, eventInjectionPoints.size());
            VariableElement variableElement = eventInjectionPoints.get(0);
            TypeElement containingType = model.getCompilationController().
                getElementUtilities().enclosingTypeElement( variableElement);
            Name varName = variableElement.getSimpleName();
            assertEquals("Event injection point should be inside class foo.Clazz," +
            		"but found inside "+ containingType.getQualifiedName(), 
            		"foo.Clazz",  containingType.getQualifiedName().toString());
            assertEquals("Observer method "+simpleName+" should match to" +
                		" event field 'event', but found :"+varName, "event",  varName.toString());
        }
        
    }

    private void commonCheck( WebBeansModel model , String className, boolean 
            twoObservers) 
    {
        TypeMirror mirror = model.resolveType(className);
        Element clazz = ((DeclaredType) mirror).asElement();
        List<? extends Element> children = clazz.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn( children );

        assertEquals( 2, methods.size());
        ExecutableElement observer= null;
        List<ExecutableElement> observerMethods = getObserverMethods(model , methods);
        if ( twoObservers ){
            assertEquals( 2,  observerMethods.size());
            ExecutableElement method1 = observerMethods.get( 0 );
            ExecutableElement method2 = observerMethods.get( 1 );
            ExecutableElement nonMatchedObserver = null;
            if ( method1.getSimpleName().contentEquals("eventObserver")){
                observer = method1;
                nonMatchedObserver = method2;
            }
            else {
                observer =method2;
                nonMatchedObserver = method1;
            }
            checkInjectionEvent(model, mirror, observer);
            List<VariableElement> eventInjectionPoints = model.getEventInjectionPoints( 
                    nonMatchedObserver, (DeclaredType) mirror);
            if ( eventInjectionPoints.size() >0 ){ 
                assertTrue( "Found unexpected observer '"+className+"."+
                        nonMatchedObserver.getSimpleName()+" for event :"+
                        eventInjectionPoints.get(0).getSimpleName() , false);
            }
        }
        else {
            assertEquals( 1,  observerMethods.size());
            observer = observerMethods.get(0);
            assertEquals("eventObserver",  observer.getSimpleName().toString());
            checkInjectionEvent(model, mirror, observer);
        }
    }

    private void checkInjectionEvent( WebBeansModel model, TypeMirror mirror,
            ExecutableElement observer )
    {
        List<VariableElement> injections = model.getEventInjectionPoints( observer,
                (DeclaredType) mirror);
        assertEquals(
                "Should be exactly one event injection , but found "
                        + injections.size() + " events",
                1, injections.size());
        VariableElement variableElement = injections.get( 0 );
        assertNotNull( variableElement );
        assertEquals("event",  variableElement.getSimpleName().toString());
        TypeElement type = model.getCompilationController().getElementUtilities().
            enclosingTypeElement( variableElement);
        assertNotNull( type );
        assertEquals("foo.Clazz",  type.getQualifiedName().toString());
    }
    
    private List<ExecutableElement> getObserverMethods(WebBeansModel model,
            List<ExecutableElement> methods){
        List<ExecutableElement> result = new ArrayList<ExecutableElement>( methods.size());
        for (ExecutableElement method : methods) {
            if ( model.getObserverParameter( method ) != null ){
                result.add( method );
            }
        }
        return result;
    }
}
