/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeListener;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class JaxRsFilterPanel implements Panel<WizardDescriptor> {

    static final String CLIENT_FILTER = "client.filter";        // NOI18N
    static final String SERVER_FILTER = "server.filter";        // NOI18N

    static final String REQUEST = "request.filter";             // NOI18N
    static final String RESPONSE = "response.filter";            // NOI18N

    static final String PRE_MATCHING = "prematching.filter";    // NOI18N
    static final String PROVIDER = "provider.filter";           // NOI18N

    JaxRsFilterPanel( WizardDescriptor wizard ) {
        myDescriptor = wizard;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
        getComponent().addChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    @Override
    public JaxRsFilterVisual getComponent() {
        if ( myComponent == null ){
            myComponent = new JaxRsFilterVisual(myDescriptor);
        }
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    @Override
    public boolean isValid() {
        Project project = Templates.getProject(myDescriptor);
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            Profile profile = webModule.getJ2eeProfile();
            if ( !Profile.JAKARTA_EE_9_FULL.equals(profile) &&
                    !Profile.JAKARTA_EE_9_WEB.equals(profile) &&
                    !Profile.JAKARTA_EE_8_FULL.equals(profile) &&
                    !Profile.JAKARTA_EE_8_WEB.equals(profile) &&
                    !Profile.JAVA_EE_8_FULL.equals(profile) &&
                    !Profile.JAVA_EE_8_WEB.equals(profile) &&
                    !Profile.JAVA_EE_7_FULL.equals(profile) &&
                    !Profile.JAVA_EE_7_WEB.equals(profile))
            {
                setErrorMessage(NbBundle.getMessage(JaxRsFilterPanel.class,
                        "MSG_NoJEE7Profile"));          // NOI18N
                return false;
            }
        }

        String msg = getComponent().getError();
        setErrorMessage(msg);
        return msg==null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    @Override
    public void readSettings( WizardDescriptor descriptor ) {
        getComponent().readSettings(descriptor);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
        getComponent().removeChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    @Override
    public void storeSettings( WizardDescriptor descriptor ) {
        getComponent().storeSettings(descriptor);
    }

    private void setErrorMessage(String message) {
        myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    private JaxRsFilterVisual myComponent;
    private WizardDescriptor myDescriptor;

}
