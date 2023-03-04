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
package org.netbeans.modules.web.jsf.metamodel;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKit;
import org.netbeans.modules.web.jsf.api.metamodel.Behavior;
import org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.api.metamodel.FacesConverter;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.Renderer;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;
import org.netbeans.modules.web.jsf.api.metamodel.Validator;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class AnnotationsTest extends CommonTestCase  {

    public AnnotationsTest( String testName ) {
        super(testName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.metamodel.CommonTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URL url = FileUtil.getArchiveRoot(javax.faces.component.FacesComponent.class.getProtectionDomain().
                getCodeSource().getLocation());
        addCompileRoots( Collections.singletonList( url ));
    }

    public void testModel() throws IOException, InterruptedException {
        
        addTopLevelElements();
         
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                List<Component> components = model.getElements(Component.class);
                assertEquals( 1 ,  components.size());
                assertEquals( "foo.CustomComponent", components.get(0 ).getComponentClass() );
                assertEquals( "a", components.get(0 ).getComponentType() );
                
                List<Behavior> behaviors = model.getElements( Behavior.class);
                assertEquals( 1 ,  behaviors.size());
                assertEquals( "foo.CustomBehavior", behaviors.get(0 ).getBehaviorClass() );
                assertEquals( "b", behaviors.get(0 ).getBehaviorId() );
                
                List<FacesConverter> converters = model.getElements( FacesConverter.class);
                assertEquals( 1 ,  converters.size());
                assertEquals( "foo.CustomConverter", converters.get(0 ).getConverterClass() );
                assertEquals( "c", converters.get(0 ).getConverterId() );
                assertEquals( "java.lang.Integer", converters.get(0 ).getConverterForClass() );
                
                List<FacesManagedBean> beans = model.getElements( FacesManagedBean.class);
                assertEquals( 1 ,  beans.size());
                FacesManagedBean bean = beans.get( 0 );
                assertEquals("foo.CustomManagedBean", bean.getManagedBeanClass());
                assertEquals("managedBeanName",  bean.getManagedBeanName());
                assertTrue( bean.getEager());
                
                List<Validator> validators = model.getElements( Validator.class);
                assertEquals(1, validators.size());
                assertEquals("d", validators.get(0).getValidatorId());
                assertEquals("foo.CustomValidator",  validators.get(0).getValidatorClass());
                return null;
            }
        });
    }

    public void testXmlMergedWithAnnotations() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomRenderer.java",
                "package foo; " +
                "import javax.faces.render.*; " +
                "@FacesRenderer(componentFamily=\"compFam\",rendererType=\"type\"," +
                "renderKitId=\"kitId\") " +
                "public class CustomRenderer  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBehaviorRenderer.java",
                "package foo; " +
                "import javax.faces.render.*; " +
                "@FacesBehaviorRenderer(rendererType=\"behaviorType\"," +
                "renderKitId=\"kitId\") " +
                "public class CustomBehaviorRenderer  {" +
                "}"); 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomListener.java",
                "package foo; " +
                "import javax.faces.event.*; " +
                "@ListenerFor(systemEventClass=javax.faces.event.ComponentSystemEvent.class," +
                "sourceClass=java.lang.String.class) " +
                "public class CustomListener  implements ComponentSystemEventListener{ " +
                " void  processEvent(ComponentSystemEvent event) {} "+
                "}"); 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomSystemEventListener.java",
                "package foo; " +
                "import javax.faces.event.*; " +
                "@ListenerFor(systemEventClass=javax.faces.event.ComponentSystemEvent.class," +
                "sourceClass=java.lang.String.class) " +
                "public class CustomSystemEventListener  implements SystemEventListener{ " +
                " boolean   isListenerForSource(Object source) {" +
                " return false;"+
                "} "+
                " void  processEvent(ComponentSystemEvent event) {} "+
                "}"); 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/MultipleEventListener.java",
                "package foo; " +
                "import javax.faces.event.*; " +
                "@ListenersFor({"+
                "@ListenerFor(systemEventClass=javax.faces.event.PostAddToViewEvent.class," +
                "sourceClass=java.lang.Integer.class), " +
                "@ListenerFor(systemEventClass=javax.faces.event.PreValidateEvent.class)" +
                "})"+
                "public class MultipleEventListener  implements SystemEventListener{ " +
                " boolean   isListenerForSource(Object source) {" +
                " return false;"+
                "} "+
                " void  processEvent(ComponentSystemEvent event) {} "+
                "}"); 
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                List<RenderKit> kits = model.getElements(RenderKit.class);
                assertEquals( 1 ,  kits.size());

                RenderKit kit = kits.get( 0 );
                assertEquals( 2 , kit.getRenderers().size());
                assertEquals( 1, kit.getClientBehaviorRenderers().size());
                
                ClientBehaviorRenderer clientBehavior = kit.getClientBehaviorRenderers().get(0);
                assertEquals("foo.CustomBehaviorRenderer",  clientBehavior.getClientBehaviorRendererClass());
                assertEquals("behaviorType",  clientBehavior.getClientBehaviorRendererType());
                
                for (Renderer renderer : kit.getRenderers()) {
                    String family = renderer.getComponentFamily();
                    String clazz = renderer.getRendererClass();
                    String type = renderer.getRendererType();
                    if ( renderer instanceof FacesRenderer ){
                        assertEquals("mainComponentFamily", family);
                        assertEquals("mainRendererClass",  clazz);
                        assertEquals("mainRendererType",  type);
                    }
                    else {
                        assertEquals("compFam", family);
                        assertEquals("foo.CustomRenderer",  clazz);
                        assertEquals("type",  type);
                    }
                }
                
                assertEquals(3 , model.getElements( Application.class).get(0).
                        getSystemEventListeners().size());
        
                List<SystemEventListener> listeners = model.getElements( 
                        Application.class).get(0).getSystemEventListeners();
                boolean componentSystemEventFound = false;
                boolean postAddToViewEventFound = false;
                boolean beforeRenderEventFound = false;
                for (SystemEventListener systemEventListener : listeners) {
                    String clazz = systemEventListener.getSystemEventClass();
                    if ( clazz.equals("javax.faces.event.ComponentSystemEvent")){
                        componentSystemEventFound = true;
                        assertEquals(String.class.getCanonicalName(),  
                                systemEventListener.getSourceClass());
                        assertEquals("foo.CustomSystemEventListener", 
                                systemEventListener.getSystemEventListenerClass());
                    }
                    else if ( clazz.equals("javax.faces.event.PostAddToViewEvent")){
                        postAddToViewEventFound = true;
                        assertEquals("java.lang.Integer",  systemEventListener.
                                getSourceClass());
                        assertEquals("foo.MultipleEventListener",  
                                systemEventListener.getSystemEventListenerClass());
                    }
                    else if ( clazz.equals("javax.faces.event.PreValidateEvent")){
                        beforeRenderEventFound  = true;
                        assertEquals("java.lang.Void",  systemEventListener.
                                getSourceClass());
                        assertEquals("foo.MultipleEventListener",  
                                systemEventListener.getSystemEventListenerClass());
                    }
                    
                }
                assertTrue( "foo.CustomSystemEventListener class is not found ",
                        componentSystemEventFound);
                assertTrue( "foo.MultipleEventListener with " +
                		"PostAddToViewEvent event class is not found", 
                		postAddToViewEventFound );
                assertTrue( "foo.MultipleEventListener with " +
                        "PreValidateEvent event class is not found", 
                        beforeRenderEventFound );
                return null;
            }
        });
    }
    
    private void addTopLevelElements() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomComponent.java",
                "package foo; " +
                "import javax.faces.component.*; " +
                "@FacesComponent(value=\"a\") " +
                "public class CustomComponent  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBehavior.java",
                "package foo; " +
                "import javax.faces.component.behavior.*; " +
                "@FacesBehavior(value=\"b\") " +
                "public class CustomBehavior  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomConverter.java",
                "package foo; " +
                "import javax.faces.convert.*; " +
                "@FacesConverter(value=\"c\", forClass=java.lang.Integer.class) " +
                "public class CustomConverter  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomManagedBean.java",
                "package foo; " +
                "import javax.faces.bean.*; " +
                "@ManagedBean(name=\"managedBeanName\", eager=true) " +
                "public class CustomManagedBean  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomValidator.java",
                "package foo; " +
                "import javax.faces.validator.*; " +
                "@FacesValidator(value=\"d\") " +
                "public class CustomValidator  {" +
                "}");
    }
}
