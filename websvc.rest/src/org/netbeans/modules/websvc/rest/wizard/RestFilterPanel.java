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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
