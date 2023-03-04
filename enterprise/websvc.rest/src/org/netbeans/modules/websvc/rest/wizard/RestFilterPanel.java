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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.io.IOException;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class RestFilterPanel implements Panel<WizardDescriptor> {
    
    static final String HTTP_METHODS = "http-methods";          // NOI18N
    static final String ORIGIN = "origin";                                  // NOI18N
    static final String HEADERS = "headers";                            // NOI18N

    RestFilterPanel( WizardDescriptor wizard ) {
        myDescriptor = wizard;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    @Override
    public Component getComponent() {
        if ( myComponent == null ){
            myComponent = new RestFilterPanelVisual(myDescriptor);
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
        RestSupport support  = project.getLookup().lookup(RestSupport.class);
        if (support != null) {
            if (support.isEE7() || support.hasJersey2(true)) {
                if (!support.isRestSupportOn()) {
                    // TODO: how is user supposed to "enable" Jax-RS? could IDE do it automatically?
                    myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                            NbBundle.getMessage(RestFilterPanel.class, 
                            "ERR_NoRestConfig"));                   // NOI18N 
                    return false;
                }
                else {
                    return true;
                }
            }
            
            // allow to create CORS filter on Java EE6 server with Jersey 1
            if (support.isEE6() && support.hasJersey1(true) && 
                    RestSupport.CONFIG_TYPE_IDE.equals(support.getProjectProperty(RestSupport.PROP_REST_CONFIG_TYPE))) {
                if (!support.isRestSupportOn()) {
                    // TODO: how is user supposed to "enable" Jax-RS? could IDE do it automatically?
                    myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                            NbBundle.getMessage(RestFilterPanel.class, 
                            "ERR_NoRestConfig"));                   // NOI18N 
                    return false;
                } else {
                    return true;
                }
            }
                    
            Object object  = null;
            try {
                object = MiscUtilities.getRestServletMapping(support.getWebApp());
            } catch(IOException e ){
                // just keep object with null value
            }
            if (object == null) {
                if ( support.isRestSupportOn() ){
                    myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(RestFilterPanel.class, 
                        "ERR_NoJerseyConfig"));                      // NOI18N
                } else {
                    myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                            NbBundle.getMessage(RestFilterPanel.class, 
                            "ERR_NoRestConfig"));                   // NOI18N
                }
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    @Override
    public void readSettings( WizardDescriptor wizard ) {
        myDescriptor = wizard;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    @Override
    public void storeSettings( WizardDescriptor descriptor ) {
        if ( myComponent != null ){
            myComponent.store( descriptor );
        }
    }
    
    private RestFilterPanelVisual myComponent;
    private WizardDescriptor myDescriptor; 
}
