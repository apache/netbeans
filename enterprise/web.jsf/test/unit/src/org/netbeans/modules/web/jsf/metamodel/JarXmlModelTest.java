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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.Factory;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class JarXmlModelTest extends CommonTestCase {

    public JarXmlModelTest( String testName ) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        List<URL> urls = new ArrayList<URL>(2);
        URL url = JarXmlModelTest.class.getResource("data/lib.jar");
        urls.add(  FileUtil.getArchiveRoot( url ));
        url = JarXmlModelTest.class.getResource("data/lib1.jar");
        urls.add(  FileUtil.getArchiveRoot( url) );
        addCompileRoots( urls );
    }
    
    public void testJarModels () throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                assertEquals( 6 , model.getModels().size());
                assertEquals( 6 , model.getFacesConfigs().size());

                assertNotNull(model.getModels().get(1).getModelSource().
                        getLookup().lookup(JsfModel.class));
                
                List<ManagedBean> beans = model.getElements( ManagedBean.class );
                assertEquals( 4 ,beans.size());
                boolean found = false;
                for (ManagedBean managedBean : beans) {
                    if ( "jar1ManagedBean".equals( managedBean.getManagedBeanName()))
                    {
                        found = true;
                    }
                }
                
                assertTrue( "Merged model should contain managed bean with name " +
                		"'jar1ManagedBean'", found);
                
                List<Factory> factories = model.getElements( Factory.class);
                assertEquals( 2 , factories.size() );
                boolean appFactoryFound = false;
                boolean renderKitFactoryFound = false; 
                for (Factory factory : factories) {
                    if ( factory.getApplicationFactories().size() >0 ){
                        appFactoryFound = true;
                        continue;
                    }
                    if ( factory.getRenderKitFactories().size() >0 ){
                        renderKitFactoryFound = true;
                        continue;
                    }
                }
                
                assertTrue( "Merged model should contain factory with " +
                		"'application-factory' child", appFactoryFound );
                assertTrue( "Merged model should contain factory with " +
                        "'render-kit-factory' child", renderKitFactoryFound );
                return null;
            }
        });
    }
    
    public void testChangedJarModels () throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                URL url = JarXmlModelTest.class.getResource("data/lib2.jar");
                SeveralXmlModelTest.PropListener l = new SeveralXmlModelTest.PropListener();
                model.addPropertyChangeListener(l);
                addCompileRoots( Collections.singletonList( 
                        new URL( "jar:"+url.toString()+"!/")));
                l.waitForModelUpdate();
                
                assertEquals( 7 ,  model.getModels().size());
                assertEquals( 7 ,  model.getFacesConfigs().size());
                boolean hasEmptyModel = false;
                for ( FacesConfig config : model.getFacesConfigs() ){
                    int size = config.getChildren().size();
                    if ( size == 0 ){
                        hasEmptyModel = true;
                    }
                }
                assertTrue( "added jar contians empty model. But it is not found", hasEmptyModel );
                return null;
            }
        });
    }

}
