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

package org.netbeans.modules.websvc.wsitconf.design;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.wsitconf.api.DesignerListenerProvider;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.DefaultSettings;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Grebac
 */
public class SecurityConfiguration implements WSConfiguration {
  
    private Service service;
    private DataObject implementationFile;
    private Project project;
    
    private ServiceModel serviceModel;
    private ServiceChangeListener scl;

    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private Binding binding;
    
    private ComponentListener cl;
    private boolean clAdded = false;
    
    private PropertyChangeListener configCreationListener = null;
    
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
    
    /** Creates a new instance of WSITWsConfiguration */

    public SecurityConfiguration(final Service service, final FileObject implementationFile) {
        try {
            this.service = service;
            this.implementationFile = DataObject.find(implementationFile);
            this.project = FileOwnerQuery.getOwner(implementationFile);
            this.serviceModel = ServiceModel.getServiceModel(implementationFile);
            addServiceChangeListener();
            this.cl = new ComponentListener() {

                private void update() {
                    boolean enabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
                    for (PropertyChangeListener pcl : listeners) {
                        PropertyChangeEvent pce = new PropertyChangeEvent(SecurityConfiguration.this, WSConfiguration.PROPERTY, null, enabled);
                        pcl.propertyChange(pce);
                    }
                }

                public void valueChanged(ComponentEvent evt) {
                    update();
                }

                public void childrenAdded(ComponentEvent evt) {
                    update();
                }

                public void childrenDeleted(ComponentEvent evt) {
                    update();
                }
            };
            this.binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
            if (binding != null) {
                addCListener(binding);
            } else {
                configCreationListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
                        addCListener(binding);
                        cl.valueChanged(null);
                    }
                };
                DesignerListenerProvider.registerListener(configCreationListener);
            }
         } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Component getComponent() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(SecurityConfiguration.class, "DesignConfigPanel.Security");
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/wsitconf/resources/designer-security.gif");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SecurityConfiguration.class, "DesignConfigPanel.Security");
    }
  
    private void addServiceChangeListener() {        
        if (scl == null) {
            scl = new  WsitServiceChangeListener(service, implementationFile, project);
        }
        if ((scl != null) && (serviceModel != null)) {
            serviceModel.addServiceChangeListener(scl);
        }
    }
    
    private synchronized void addCListener(Binding binding) {
        if ((!clAdded) && (binding != null)) {
            binding.getModel().addComponentListener(cl);
            clAdded = true;
        }
    }
    
    public boolean isSet() {
        boolean set = false;
        this.binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, false, null);
        addCListener(binding);
        if (binding != null) {
            set = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        }
        return set;
    }
        
    public void set() {
        switchIt(true);
    }

    public void unset() {
        switchIt(false);
    }

    private void switchIt(final boolean enable) {
       final ConfigRunnable r = new ConfigRunnable();        
       SwingUtilities.invokeLater(r);
       RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    if (enable) {
                        binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, true, null);
                        if (binding == null) return;

                        addCListener(binding);

                        if (!(SecurityPolicyModelHelper.isSecurityEnabled(binding))) {
                            // default profile with the easiest setup
                            SecurityProfile secProf = SecurityProfileRegistry.getDefault().getProfile(ComboConstants.PROF_MUTUALCERT);
                            secProf.profileSelected(binding, true, ConfigVersion.getDefault());
                            if (!RMModelHelper.getInstance(ConfigVersion.getDefault()).isRMEnabled(binding)) {
                                if (secProf instanceof SecureConversationFeature) {
                                    ((SecureConversationFeature)secProf).enableSecureConversation(binding, true);
                                }
                            }
                            DefaultSettings.fillDefaults(project, false,true);
                            ProfilesModelHelper.setServiceDefaults(ComboConstants.PROF_MUTUALCERT, binding, project);
                            WSITModelSupport.save(binding);
                        }
                    } else {
                        if (binding == null) return;
                        if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
                            addCListener(binding);
                            SecurityPolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(binding)).disableSecurity(binding, true);
                            WSITModelSupport.save(binding);
                        }
                    }
                } finally {
                    r.stop();
                }
            }
        });
    }
    
    public void registerListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void unregisterListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    protected void finalize() {
        if ((scl != null) && (serviceModel != null)) {
            serviceModel.removeServiceChangeListener(scl);
        }
        if ((binding != null) && (clAdded)) {
            binding.getModel().removeComponentListener(cl);
        }
    }

    public boolean isEnabled() {
        boolean enabled =  false;
        if ((implementationFile == null) || (!implementationFile.isValid())) {
            return false;
        }
        Node n = implementationFile.getNodeDelegate();
        JaxWsModel model = project.getLookup().lookup(JaxWsModel.class);
        enabled = !SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(n, model);
        return enabled;
    }
}
