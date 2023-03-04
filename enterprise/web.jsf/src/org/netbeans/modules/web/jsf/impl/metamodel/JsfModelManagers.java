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
package org.netbeans.modules.web.jsf.impl.metamodel;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel;


/**
 * @author ads
 *
 */
abstract class JsfModelManagers extends AbstractJsfModel {
    
    JsfModelManagers( AnnotationModelHelper helper ) {
        myHelper = helper;
        myBehaviorManager = helper.createPersistentObjectManager( 
                new ObjectProviders.BehaviorProvider( helper) );
        myComponentManager = helper.createPersistentObjectManager( 
                new ObjectProviders.ComponentProvider( helper) );
        myConverterManager = helper.createPersistentObjectManager( 
                new ObjectProviders.ConverterProvider( helper) );
        myManagedBeanManager = helper.createPersistentObjectManager( 
                new ObjectProviders.ManagedBeanProvider( helper) );
        myValidatorManager = helper.createPersistentObjectManager( 
                new ObjectProviders.ValidatorProvider( helper) );
        myRendererManager = helper.createPersistentObjectManager( 
                new ObjectProviders.RendererProvider( helper));
        myClientBehaviorManager = helper.createPersistentObjectManager(
                new ObjectProviders.ClientBehaviorProvider(helper));
        mySystemEventManager = helper.createPersistentObjectManager(
                new ObjectProviders.SystemEventListenerProvider(helper));
    }
    
    PersistentObjectManager<BehaviorImpl> getBeahviorManager(){
        return myBehaviorManager;
    }
    
    PersistentObjectManager<ComponentImpl> getComponentManager(){
        return myComponentManager;
    }
    
    PersistentObjectManager<ConverterImpl> getConverterManager(){
        return myConverterManager;
    }
    
    PersistentObjectManager<ManagedBeanImpl> getManagedBeanManager(){
        return myManagedBeanManager;
    }
    
    PersistentObjectManager<ValidatorImpl> getValidatorManager(){
        return myValidatorManager;
    }
    
    PersistentObjectManager<RendererImpl> getRendererManager(){
        return myRendererManager;
    }
    
    PersistentObjectManager<ClientBehaviorRendererImpl> getClientBehaviorManager(){
        return myClientBehaviorManager;
    }
    
    PersistentObjectManager<SystemEventListenerImpl> getSystemEventManager(){
        return mySystemEventManager;
    }
    
    AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private final PersistentObjectManager<BehaviorImpl> myBehaviorManager;
    private final PersistentObjectManager<ComponentImpl> myComponentManager;
    private final PersistentObjectManager<ConverterImpl> myConverterManager;
    private final PersistentObjectManager<ManagedBeanImpl> myManagedBeanManager;
    private final PersistentObjectManager<ValidatorImpl> myValidatorManager;
    private final PersistentObjectManager<RendererImpl> myRendererManager;
    private final PersistentObjectManager<ClientBehaviorRendererImpl> 
                                                        myClientBehaviorManager;
    private final PersistentObjectManager<SystemEventListenerImpl>
                                                        mySystemEventManager;
    private AnnotationModelHelper myHelper;
}
