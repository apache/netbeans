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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class SeveralXmlModelTest extends CommonTestCase {

    public SeveralXmlModelTest( String testName ) {
        super(testName);
    }
    
    public void testSeveralModelInSrc() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                assertEquals( 3 , model.getModels().size());
                assertNotNull( model.getMainConfig() );
                assertEquals( 3 ,  model.getFacesConfigs().size());
                
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 2 , applications.size());
                Application withSystemEventListener= null;
                for (Application application : applications) {
                    if ( application.getSystemEventListeners().size() == 1){
                        withSystemEventListener = application;
                    }
                }
                
                assertNotNull( "one application should have " +
                		"child system event listener", withSystemEventListener );
                Application empty = 
                    applications.get( 0 ).equals( withSystemEventListener) ? 
                            applications.get( 1 ) : applications.get(0);
                assertEquals( "one application should be empty", 0, empty.getChildren().size());
                return null;
            }
        });
    }
    
    public void testRemoveModelInSrc() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                PropListener l = new PropListener();
                model.addPropertyChangeListener(l);
                srcFO.getFileObject("META-INF/two.faces-config.xml").delete();
                l.waitForModelUpdate();
                assertEquals( 2 ,  model.getModels().size());
                assertEquals( 2 , model.getFacesConfigs().size());
                
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 1 , applications.size());
                return null;
            }
        });
    }

    public void testAddModelInSrc() throws IOException, InterruptedException{
        FileObject fileObject = srcFO.getFileObject("META-INF/one.faces-config.xml");
        if ( fileObject!= null ){
            fileObject.delete();
        }
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                assertEquals( 2 ,  model.getModels().size());
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 1 , applications.size());

                PropListener l = new PropListener();
                model.addPropertyChangeListener(l);
                TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                        getFileContent("data/faces-config.xml"));
                l.waitForModelUpdate();
                
                assertEquals( 3 ,  model.getModels().size());
                assertEquals( 3 , model.getFacesConfigs().size());
                
                applications = model.getElements( Application.class);
                assertEquals( 2 , applications.size());
                return null;
            }
        });
    }

    public void testPrettyFacesModel() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/faces-config-prettyFaces.xml", getFileContent("data/faces-config-prettyFaces.xml"));
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel, Void>() {
            @Override
            public Void run(JsfModel model) throws Exception {
                assertEquals(0, model.getModels().size());

                PropListener l = new PropListener();
                model.addPropertyChangeListener(l);
                TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml", getFileContent("data/faces-config.xml"));
                l.waitForModelUpdate();

                assertEquals(1, model.getModels().size());
                assertEquals(1, model.getFacesConfigs().size());

                List<Application> applications = model.getElements(Application.class);
                assertEquals(1, applications.size());
                return null;
            }
        });
    }

    public void testModelBeanCompletion() throws Exception {
        FileObject fileObject = srcFO.getFileObject("META-INF/one.faces-config.xml");
        if (fileObject != null) {
            fileObject.delete();
        }
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));

        MetadataModel<JsfModel> jsfModel = createJsfModel();

        jsfModel.runReadAction(new MetadataModelAction<JsfModel, Void>() {
            public Void run(JsfModel model) throws Exception {
                List<FacesManagedBean> elements = model.getElements(FacesManagedBean.class);
                assertEquals(1 , elements.size());
                return null;
            }
        });

        // wait threshold for next possible XDM model update invokation
        Thread.sleep(AbstractModelFactory.DELAY_DIRTY);
        jsfModel.runReadAction(new MetadataModelAction<JsfModel, Void>() {
            public Void run(JsfModel model) throws Exception {
                // change content of the file and put there MDB without specified class
                PropListener l = new PropListener();
                model.getFacesConfigs().get(0).getModel().addPropertyChangeListener(l);
                TestUtilities.copyStringToFileObject(srcFO, "META-INF/faces-config.xml",
                        getFileContent("data/three.faces-config.xml"));
                l.waitForModelUpdate();
                // ManagedBeans without specified class shouldn't be returned
                List<FacesManagedBean> elements = model.getElements(FacesManagedBean.class);
                assertEquals(0 , elements.size());
                return null;
            }
        });
    }

    /**
     * File change events (which cause reload of list of configuration files) are
     * fired in separate thread and to synchronize on delivery of these events
     * we wait on a property change event.
     */
    static class PropListener implements PropertyChangeListener {

        private boolean modelUpdated = false;

        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            modelUpdated = true;
        }

        public void waitForModelUpdate() throws InterruptedException {
            while (!isModelUpdated()) {
                Thread.sleep(100);
            }
        }

        public synchronized boolean isModelUpdated() {
            return modelUpdated;
        }
        
    }
}
