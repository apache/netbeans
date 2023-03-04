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

package org.netbeans.modules.web.jsf.api.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.util.Lookup;

/**
 * Actually this class does cache nothing, but grabs the data from beans model
 * which is supposed to do the caching.
 *
 * @author Petr Pisl
 * @author ads
 */
public class JSFBeanCache {
    
    public static List<FacesManagedBean> getBeans(Project project) {
        //unit testing, tests can provider a fake beans provider >>>
        JsfBeansProvider beansProvider = Lookup.getDefault().lookup(JsfBeansProvider.class);
        if(beansProvider != null) {
            return beansProvider.getBeans(project);
        }
        //<<<

        final List<FacesManagedBean> beans = new ArrayList<FacesManagedBean>();
        MetadataModel<JsfModel> model = JSFUtils.getModel(project);
        if ( model == null){
            return beans;
        }
        try {
            model.runReadAction( new MetadataModelAction<JsfModel, Void>() {

                public Void run( JsfModel model ) throws Exception {
                    List<FacesManagedBean> managedBeans = model.getElements( 
                            FacesManagedBean.class);
                    beans.addAll( managedBeans );
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            LOG.log( Level.WARNING , e.getMessage(), e );
        }
        catch (IOException e) {
            LOG.log( Level.WARNING , e.getMessage(), e );
        }
        return beans;
    }
    
    private static final Logger LOG = Logger.getLogger( 
            JSFBeanCache.class.getCanonicalName() );


    //for unit tests>>>
    public static interface JsfBeansProvider {

        public List<FacesManagedBean> getBeans(Project project);

    }
    //<<<
    
}
