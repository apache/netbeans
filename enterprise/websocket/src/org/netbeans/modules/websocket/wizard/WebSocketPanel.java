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
package org.netbeans.modules.websocket.wizard;

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
class WebSocketPanel implements Panel<WizardDescriptor> {

    static final String URI = "websocket.uri";      // NOI18N

    WebSocketPanel( WizardDescriptor wizard ) {
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
    public WebSocketVisual getComponent() {
        if ( myComponent == null ){
            myComponent = new WebSocketVisual(myDescriptor);
        }
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.websocket.wizard.WebSocketPanel"); //NOI18N
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
            if (profile.isAtMost(Profile.JAVA_EE_6_FULL)) {
                setErrorMessage(NbBundle.getMessage(WebSocketPanel.class,
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

    private WebSocketVisual myComponent;
    private WizardDescriptor myDescriptor;

}
