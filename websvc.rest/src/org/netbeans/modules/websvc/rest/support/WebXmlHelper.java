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
