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
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.wsitconf.api.DesignerListenerProvider;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Grebac
*/
public class MtomConfiguration  implements WSConfiguration{
  
    private Service service;
    private DataObject implementationFile;
    private Project project;
    
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
    private Binding binding;
    
    private ComponentListener cl;
    
    private boolean clAdded = false;
    
    private PropertyChangeListener configCreationListener = null;
    
    /** Creates a new instance of WSITWsConfiguration */

    public MtomConfiguration(final Service service, final FileObject implementationFile) {
        try {
            this.service = service;
            this.implementationFile = DataObject.find(implementationFile);
            this.project = FileOwnerQuery.getOwner(implementationFile);
            this.binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
            this.cl = new ComponentListener() {

                private void update() {
                    boolean enabled = TransportModelHelper.isMtomEnabled(binding);
                    for (PropertyChangeListener pcl : listeners) {
                        PropertyChangeEvent pce = new PropertyChangeEvent(MtomConfiguration.this, WSConfiguration.PROPERTY, null, enabled);
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
    
    private synchronized void addCListener(Binding binding) {
        if ((!clAdded) && (binding != null)) {
            binding.getModel().addComponentListener(cl);
            clAdded = true;
        }
    }                
                
    public Component getComponent() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(MtomConfiguration.class, "DesignConfigPanel.mtomCB.text");
    }

    public Image getIcon() {
        return ImageUtilities.loadImage
                ("org/netbeans/modules/websvc/wsitconf/resources/designer-mtom.gif"); // NOI18N   
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MtomConfiguration.class, "DesignConfigPanel.mtomCB.text");
    }
  
    public boolean isSet() {
        if (binding != null) {
            return TransportModelHelper.isMtomEnabled(binding);
        }
        return false;
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
                        binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, true, createdFiles);
                        if (binding == null) return;
                        if (!(TransportModelHelper.isMtomEnabled(binding) == enable)) {
                            TransportModelHelper.enableMtom(binding, enable);
                            WSITModelSupport.save(binding);
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
        if (binding != null) {
            binding.getModel().removeComponentListener(cl);
        }
    }

    public boolean isEnabled() {
        return true;
    }
}
