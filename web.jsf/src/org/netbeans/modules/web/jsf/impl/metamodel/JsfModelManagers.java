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
