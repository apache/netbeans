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

package org.netbeans.modules.j2ee.deployment.impl.ui.wizard;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class AddServerInstanceWizard extends WizardDescriptor {
    public final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    public final static String PROP_SERVER = "ServInstWizard_server"; // NOI18N

    private final static String PROP_AUTO_WIZARD_STYLE = WizardDescriptor.PROP_AUTO_WIZARD_STYLE; // NOI18N
    private final static String PROP_CONTENT_DISPLAYED = WizardDescriptor.PROP_CONTENT_DISPLAYED; // NOI18N
    private final static String PROP_CONTENT_NUMBERED = WizardDescriptor.PROP_CONTENT_NUMBERED; // NOI18N
    private final static String PROP_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    private final static String PROP_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N
    private final static String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE; // NOI18N

    private AddServerInstanceWizardIterator iterator;
    private ServerChooserPanel chooser;
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.j2ee.deployment"); // NOI18N

    private AddServerInstanceWizard(Map<String, String> props) {
        this(new AddServerInstanceWizardIterator());
        
        putProperty(PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        putProperty(PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        putProperty(PROP_CONTENT_NUMBERED, Boolean.TRUE);
        for (Entry<String, String> entry : props.entrySet()) {
            putProperty(entry.getKey(), entry.getValue());
        }
        
        setTitle(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Title"));
        setTitleFormat(new MessageFormat(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_TitleFormat")));
        
        initialize();
    }
    
    private AddServerInstanceWizard(AddServerInstanceWizardIterator iterator) {
        super(iterator);
        this.iterator = iterator;
    }
    
    
    public static String showAddServerInstanceWizard(Map<String, String> props) {
        Collection<Server> allServers = ServerRegistry.getInstance().getServers();
        for (java.util.Iterator<Server> it = allServers.iterator(); it.hasNext();) {
            Server server = it.next();
            OptionalDeploymentManagerFactory factory = server.getOptionalFactory();
            if (factory == null || factory.getAddInstanceIterator() == null) {
                it.remove();
            }
        }
        if (allServers.isEmpty()) {
            // display the warning dialog - no server plugins
            String close = NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoServerPlugins_Close");
            DialogDescriptor descriptor = new DialogDescriptor(
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoServerPlugins_Text"),
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoServerPlugins_Title"),
                    true,
                    new Object[] {close},
                    close,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);

            // TODO invoke plugin manager once API to do that will be available
            DialogDisplayer.getDefault().notify(descriptor);
            return null;
        }
            
        AddServerInstanceWizard wizard = new AddServerInstanceWizard(props);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        try {
            dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerInstanceWizard.class, "ACSD_Add_Server_Instance"));
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        if (wizard.getValue() == WizardDescriptor.FINISH_OPTION) {
            Set instantiatedObjects = wizard.getInstantiatedObjects();
            if (instantiatedObjects != null && instantiatedObjects.size() > 0) {
                Object result = instantiatedObjects.iterator().next();
                if (result instanceof InstanceProperties) {
                    return ((InstanceProperties) result).getProperty(InstanceProperties.URL_ATTR);
                } else {
                    LOGGER.warning(wizard.iterator.getSelectedServer() + "'s add server instance wizard iterator should return " + // NOI18N
                            "a Set containing new server instance InstanceProperties object as a result of the " + // NOI18N
                            "WizardDescriptor.InstantiatingIterator.instantiate() method."); // NOI18N
                    // there is an error in the server plugin, cannot return the added instance
                    return null;
                }
            }
        }
        // the wizard was cancelled
        return null;
    }
    
    public void setErrorMessage(String message) {
        putProperty(PROP_ERROR_MESSAGE, message);
    }
    
    protected void updateState() {
        super.updateState();
        
        String[] contentData = getContentData();
        if (contentData != null) {
            putProperty(PROP_CONTENT_DATA, contentData);
            putProperty(PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(getContentSelectedIndex()));
        }
    }

    private ServerChooserPanel getChooser() {
        if (chooser == null)
            chooser = new ServerChooserPanel();

        return chooser;
    }
    
    private String[] getContentData() {
        JComponent first;
        String[] firstContentData;
        
        first = (JComponent)getChooser().getComponent();
        firstContentData = (String[])first.getClientProperty(PROP_CONTENT_DATA);
        
        if (iterator.current().equals(getChooser())) {
            return firstContentData;
        } else {
            JComponent component = (JComponent)iterator.current().getComponent();
            String[] componentContentData = (String[])component.getClientProperty(PROP_CONTENT_DATA);
            if (componentContentData == null)
                return firstContentData;
            
            String[] contentData = new String[componentContentData.length + 1];
            contentData[0] = firstContentData[0];
            System.arraycopy(componentContentData, 0, contentData, 1, componentContentData.length);
            return contentData;
        }
    }

    private int getContentSelectedIndex() {
        if (iterator.current().equals(getChooser())) {
            return 0;
        } else {
            JComponent component = (JComponent)iterator.current().getComponent();
            Integer componentIndex = (Integer)component.getClientProperty(PROP_CONTENT_SELECTED_INDEX);
            if (componentIndex != null)
                return componentIndex.intValue() + 1;
            else
                return 1;
        }
    }
    
    private static class AddServerInstanceWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
        private AddServerInstanceWizard wd;
        public boolean showingChooser;
        private WizardDescriptor.InstantiatingIterator iterator;
        private HashMap iterators;
        
        public AddServerInstanceWizardIterator() {
            showingChooser = true;
            iterators = new HashMap();
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public WizardDescriptor.Panel current() {
            if (showingChooser)
                return wd.getChooser();
            else
                if (iterator != null)
                    return iterator.current();
                else
                    return null;
        }
        
        public boolean hasNext() {
            if (showingChooser)
                return true;
            else
                if (iterator != null)
                    return iterator.hasNext();
                else
                    return false;
        }
        
        public boolean hasPrevious() {
            if (showingChooser)
                return false;
            else
                return true;
        }
        
        public String name() {
            return null;
        }
        
        public void nextPanel() {
            if (iterator == null)
                iterator = getServerIterator();
            else {
                if (!showingChooser)
                    iterator.nextPanel();
            }
            showingChooser = false;
        }
        
        public void previousPanel() {
            if (iterator.hasPrevious())
                iterator.previousPanel();
            else {
                showingChooser = true;
                iterator = null;
            }
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            wd = (AddServerInstanceWizard)wizard;
            
            JComponent chooser = (JComponent)wd.getChooser().getComponent();
            chooser.putClientProperty(PROP_CONTENT_DATA, new String[] { 
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_ChooseServer"),
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Ellipsis")
            });
        }

        public java.util.Set instantiate() throws java.io.IOException {
            if (iterator != null) {
                return iterator.instantiate();
            }
            else
                return null;
        }
        
        private WizardDescriptor.InstantiatingIterator getServerIterator() {
            Server server = getSelectedServer();
            if (server == null)
                return null;
            
            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator)iterators.get(server);
            if (iterator != null)
                return iterator;
            
            OptionalDeploymentManagerFactory factory = server.getOptionalFactory();
            if (factory != null) {
                iterator = factory.getAddInstanceIterator();
                iterator.initialize(wd);
                iterators.put(server, iterator);
                return iterator;
            }
            else
                return null;
        }
        
        public Server getSelectedServer() {
            return (Server)wd.getProperty(PROP_SERVER);
        }
    }  
}
