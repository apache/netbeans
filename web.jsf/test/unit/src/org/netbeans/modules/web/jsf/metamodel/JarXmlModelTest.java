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
