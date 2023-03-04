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
package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author PeterLiu
 */
public class WebXmlHelper {
    
    public static final String PERSISTENCE_FACTORY  = "persistence-factory";    //NOI81N

    private static final String PERSISTENCE_UNIT_REF_PREFIX = "persistence/";       //NOI81N

    private static final String PERSISTENCE_UNIT_REF_TAG = "persistence-unit-ref";      //NOI18N

    private static final String PERSISTENCE_UNIT_REF_NAME_TAG = "persistence-unit-ref-name";        //NOI18N

    private static final String PERSISTENCE_UNIT_NAME_TAG = "persistence-unit-name";    //NOI18N

    private static final String PERSISTENCE_CONTEXT_REF_TAG = "persistence-context-ref";      //NOI18N

    private static final String PERSISTENCE_CONTEXT_REF_NAME_TAG = "persistence-context-ref-name";        //NOI18N
    
    private static final String RESOURCE_REF_TAG = "resource-ref";      //NOI18N
    
    private static final String RESOURCE_REF_NAME_TAG = "res-ref-name";        //NOI18N
    
    private static final String RESOURCE_TYPE_TAG = "res-type";        //NOI18N
    
    private static final String RESOURCE_AUTH_TAG = "res-auth";       //NOI18N
    
    private static final String USER_TRANSACTION = "UserTransaction";        //NOI18N
 
    private static final String USER_TRANSACTION_CLASS = "javax.transaction.UserTransaction";   //NOI18N   

    private static final String CONTAINER = "Container";        //NOI18N
    
    private Project project;
    private String puName;
    private DOMHelper helper;

    public WebXmlHelper(Project project, String puName) {
        this.project = project;
        this.puName = puName;
    }

    public void configure() {
        FileObject fobj = getWebXml(project);

        if (fobj == null)  return;
        
        helper = new DOMHelper(fobj);
     
        addPersistenceUnitRef();
        
        /*
         * Fix for BZ#190237 -  RESTful WS from entity classes do not deploy on WebLogic
         */
        if (RestUtils.hasJTASupport(project)&& !RestUtils.hasProfile(project, 
                Profile.JAVA_EE_5, Profile.JAVA_EE_6_FULL, Profile.JAVA_EE_6_WEB)) 
        {
            addUserTransactionResourceRef();
        }
        helper.save();
    }

    private void addPersistenceUnitRef() {
        Element refElement = helper.findElement(PERSISTENCE_UNIT_REF_NAME_TAG, 
                PERSISTENCE_FACTORY);

        if (refElement != null) {
            return;
        }
        
        refElement = helper.createElement(PERSISTENCE_UNIT_REF_TAG);
        refElement.appendChild(helper.createElement(PERSISTENCE_UNIT_REF_NAME_TAG, 
                PERSISTENCE_FACTORY));
        refElement.appendChild(helper.createElement(PERSISTENCE_UNIT_NAME_TAG, puName));

        helper.appendChild(refElement);
    }

    private void addPersistenceContextRef() {
        String refName = PERSISTENCE_UNIT_REF_PREFIX + puName;
        Element refElement = helper.findElement(PERSISTENCE_CONTEXT_REF_NAME_TAG, refName);

        if (refElement != null) {
            return;
        }

        refElement = helper.createElement(PERSISTENCE_CONTEXT_REF_TAG);
        refElement.appendChild(helper.createElement(PERSISTENCE_CONTEXT_REF_NAME_TAG, refName));
        refElement.appendChild(helper.createElement(PERSISTENCE_UNIT_NAME_TAG, puName));

        helper.appendChild(refElement);
    }

    private void addUserTransactionResourceRef() {
        Element refElement = helper.findElement(RESOURCE_REF_NAME_TAG, USER_TRANSACTION);

        if (refElement != null) {
            return;
        }

        refElement = helper.createElement(RESOURCE_REF_TAG);
        refElement.appendChild(helper.createElement(RESOURCE_REF_NAME_TAG, USER_TRANSACTION));
        refElement.appendChild(helper.createElement(RESOURCE_TYPE_TAG, USER_TRANSACTION_CLASS));
        refElement.appendChild(helper.createElement(RESOURCE_AUTH_TAG, CONTAINER));

        helper.appendChild(refElement);
    }
    
    private FileObject getWebXml(Project project) {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            return wm.getDeploymentDescriptor();
        }
        return null;
    }
}
