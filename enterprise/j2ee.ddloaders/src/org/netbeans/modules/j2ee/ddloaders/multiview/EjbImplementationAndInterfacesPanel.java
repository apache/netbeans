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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {

    private static final Logger LOGGER = Logger.getLogger(EjbImplementationAndInterfacesForm.class.getName());
    
    private EntityAndSessionHelper helper;
    private NonEditableDocument beanClassDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getEjbClass();
        }
    };
    private NonEditableDocument localComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocal();
        }
    };
    private NonEditableDocument localHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocalHome();
        }
    };
    private NonEditableDocument remoteComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getRemote();
        }
    };
    private NonEditableDocument remoteHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getHome();
        }
    };

    private static final String LINK_BEAN = "linkBean";
    private static final String LINK_LOCAL = "linkLocal";
    private static final String LINK_LOCAL_HOME = "linkLocalHome";
    private static final String LINK_REMOTE = "linkRemote";
    private static final String LINK_REMOTE_HOME = "linkRemoteHome";

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView,
            final EntityAndSessionHelper helper) {
        super(sectionNodeView);
        this.helper = helper;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);

        scheduleRefreshView();

        initLinkButton(getBeanClassLinkButton(), LINK_BEAN);
        initLinkButton(getLocalComponentLinkButton(), LINK_LOCAL);
        initLinkButton(getLocalHomeLinkButton(), LINK_LOCAL_HOME);
        initLinkButton(getRemoteComponentLinkButton(), LINK_REMOTE);
        initLinkButton(getRemoteHomeLinkButton(), LINK_REMOTE_HOME);
    }

    private void initLinkButton(AbstractButton button, String key) {
        LinkButton.initLinkButton(button, this, null, key);
    }

    @Override
    public void refreshView() {
        beanClassDocument.init();
        localComponentDocument.init();
        localHomeDocument.init();
        remoteComponentDocument.init();
        remoteHomeDocument.init();
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EntityAndSession) {
            scheduleRefreshView();
        }
    }

    @Override
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        String javaClass = null;
        if(LINK_BEAN.equals(ddProperty)) {
            javaClass = helper.getEjbClass();
        } else if(LINK_LOCAL.equals(ddProperty)) {
            javaClass = helper.getLocal();
        } else if(LINK_LOCAL_HOME.equals(ddProperty)) {
            javaClass = helper.getLocalHome();
        } else if(LINK_REMOTE.equals(ddProperty)) {
            javaClass = helper.getRemote();
        } else if (LINK_REMOTE_HOME.equals(ddProperty)) {
            javaClass = helper.getHome();
        } 
        
        if (javaClass == null || "".equals(javaClass.trim())) {
            LOGGER.log(Level.INFO, "Could not resolve class for ddProperty:" + ddProperty ); //NO18N
            return;
        }
        Utils.openEditorFor(helper.ejbJarFile, javaClass);
    }
}
