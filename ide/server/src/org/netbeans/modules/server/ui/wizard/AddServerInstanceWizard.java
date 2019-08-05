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

package org.netbeans.modules.server.ui.wizard;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 * @author Petr Hejl
 */
public class AddServerInstanceWizard extends WizardDescriptor {

    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    public static final String PROP_SERVER_INSTANCE_WIZARD = "ServInstWizard_server"; // NOI18N

    private static final String PROP_AUTO_WIZARD_STYLE = WizardDescriptor.PROP_AUTO_WIZARD_STYLE; // NOI18N

    private static final String PROP_CONTENT_DISPLAYED = WizardDescriptor.PROP_CONTENT_DISPLAYED; // NOI18N

    private static final String PROP_CONTENT_NUMBERED = WizardDescriptor.PROP_CONTENT_NUMBERED; // NOI18N

    private static final String PROP_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N

    private static final String PROP_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N

    private static final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE; // NOI18N

    private AddServerInstanceWizardIterator iterator;

    private ServerWizardPanel chooser;

    private static final Logger LOGGER = Logger.getLogger(AddServerInstanceWizard.class.getName()); // NOI18N
    
    private ServerRegistry registry;

    private AddServerInstanceWizard(ServerRegistry registry) {
        this(new AddServerInstanceWizardIterator(registry));
        this.registry = registry;
        
        putProperty(PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        putProperty(PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        putProperty(PROP_CONTENT_NUMBERED, Boolean.TRUE);

        if (registry.isCloud()) {
            setTitle(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ACIW_Title"));
            setTitleFormat(new MessageFormat(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ACIW_TitleFormat")));
        } else {
            setTitle(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Title"));
            setTitleFormat(new MessageFormat(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_TitleFormat")));
        }

        initialize();
    }

    // NEVER CALL this constructor directly!
    private AddServerInstanceWizard(AddServerInstanceWizardIterator iterator) {
        super(iterator);
        this.iterator = iterator;
    }
    
    public static ServerInstance showAddServerInstanceWizard() {
        return showAddServerInstanceWizard(ServerRegistry.getInstance());
    }

    public static ServerInstance showAddCloudInstanceWizard() {
        return showAddServerInstanceWizard(ServerRegistry.getCloudInstance());
    }

    private static ServerInstance showAddServerInstanceWizard(ServerRegistry registry) {
        Collection<? extends ServerWizardProvider> providers = Lookups.forPath(
                registry.getPath()).lookupAll(ServerWizardProvider.class);
        // this will almost never happen if this module will be autoload
        if (providers.isEmpty()) {
            // except we run in ergonomics mode and providers are not yet on
            // inspite there some are ready
            JRadioButton[] ready = listAvailableProviders(registry.getPath());
            
            if (registry.isCloud()) {
                String close = NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoCloudPlugins_Close");
                DialogDescriptor descriptor = new DialogDescriptor(
                        NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoCloudPlugins_Text"),
                        NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoCloudPlugins_Title"),
                        true,
                        new Object[] {close},
                        close,
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        null);
                DialogDisplayer.getDefault().notify(descriptor);
                return null;
            } else if (ready.length == 0) {
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
            } else {
                Action a = null;
                if (ready.length == 1) {
                    a = (Action)ready[0].getClientProperty("action"); // NOI18N
                } else {
                    AvailableProvidersPanel available = new AvailableProvidersPanel(ready);
                    DialogDescriptor descriptor = new DialogDescriptor(
                            available,
                            NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_NoServerPlugins_Title"),
                            true,
                            new Object[] {DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION },
                            null,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);

                    DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        a = (Action)available.getSelected().getClientProperty("action"); // NOI18N
                    }
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(a, 0, "noui")); // NOI18N
                } else {
                    return null;
                }
            }
        }

        AddServerInstanceWizard wizard = new AddServerInstanceWizard(registry);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        try {
            dialog.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(AddServerInstanceWizard.class, "ACSD_Add_Server_Instance"));
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }

        if (wizard.getValue() == WizardDescriptor.FINISH_OPTION) {
            Set instantiatedObjects = wizard.getInstantiatedObjects();
            if (instantiatedObjects != null && instantiatedObjects.size() > 0) {
                Object result = instantiatedObjects.iterator().next();
                if (result instanceof ServerInstance) {
                    return (ServerInstance) result;
                } else {
                    LOGGER.log(Level.WARNING, NbBundle.getMessage(
                            AddServerInstanceWizard.class, "MSG_WrongServerIntance", result)); // NOI18N
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

    @Override
    protected void updateState() {
        super.updateState();

        String[] contentData = getContentData();
        if (contentData != null) {
            putProperty(PROP_CONTENT_DATA, contentData);
            putProperty(PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(getContentSelectedIndex()));
        }
    }

    static JRadioButton[] listAvailableProviders(String path) {
        List<JRadioButton> res = new ArrayList<JRadioButton>();

        for (Action a : Utilities.actionsForPath(path+"/Actions")) { // NOI18N
            if (a == null) {
                continue;
            }
            Object msg = a.getValue("wizardMessage"); // NOI18N
            if (msg instanceof String) {
                JRadioButton button = new JRadioButton((String)msg);
                button.putClientProperty("action", a); // NOI18N
                res.add(button);
            }
        }

        return res.toArray(new JRadioButton[0]);
    }


    private ServerWizardPanel getChooser() {
        if (chooser == null) {
            chooser = new ServerWizardPanel(registry);
        }
        return chooser;
    }

    private String[] getContentData() {
        String[] firstContentData = getFirstPanelContentData(registry.isCloud());

        if (iterator.current().equals(getChooser())) {
            return firstContentData;
        } else {
            JComponent component = (JComponent) iterator.current().getComponent();
            String[] componentContentData = (String[]) component.getClientProperty(PROP_CONTENT_DATA);
            if (componentContentData == null) {
                return firstContentData;
            }

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
            JComponent component = (JComponent) iterator.current().getComponent();
            Integer componentIndex = (Integer) component.getClientProperty(PROP_CONTENT_SELECTED_INDEX);
            if (componentIndex != null) {
                return componentIndex.intValue() + 1;
            } else {
                return 1;
            }
        }
    }
    
    private static String[] getFirstPanelContentData(boolean cloud) {
        if (cloud) {
            return new String[] {
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ACIW_ChooseServer"),
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ACIW_Ellipsis")
                };
        } else {
            return new String[] {
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_ChooseServer"),
                    NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Ellipsis")
                };
        }
    }

    private static class AddServerInstanceWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {

        private final Map<ServerWizardProvider, InstantiatingIterator> iterators = new HashMap<ServerWizardProvider, InstantiatingIterator>();

        private WizardDescriptor.InstantiatingIterator iterator;

        private AddServerInstanceWizard wd;

        public boolean showingChooser = true;
        
        private ServerRegistry registry;

        public AddServerInstanceWizardIterator(ServerRegistry registry) {
            super();
            this.registry = registry;
        }

        public String name() {
            return null;
        }

        public WizardDescriptor.Panel current() {
            if (showingChooser) {
                return wd.getChooser();
            } else {
                if (iterator != null) {
                    return iterator.current();
                } else {
                    return null;
                }
            }
        }

        public boolean hasNext() {
            if (showingChooser) {
                return true;
            } else {
                if (iterator != null) {
                    return iterator.hasNext();
                } else {
                    return false;
                }
            }
        }

        public boolean hasPrevious() {
            return !showingChooser;
        }

        public void nextPanel() {
            if (iterator == null) {
                iterator = getServerIterator();
            } else {
                if (!showingChooser) {
                    iterator.nextPanel();
                }
            }
            showingChooser = false;
        }

        public void previousPanel() {
            if (iterator.hasPrevious()) {
                iterator.previousPanel();
            } else {
                showingChooser = true;
                iterator = null;
            }
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            wd = (AddServerInstanceWizard) wizard;

            // FYI: using wd.getChooser().getComponent() here was wrong as it forces
            // creation of panel too early: AddServerInstanceWizard constructor was not
            // yet finished and wizards and their panels were being created.
            wd.putProperty(PROP_CONTENT_DATA, getFirstPanelContentData(registry.isCloud()));
        }

        public Set instantiate() throws IOException {
            if (iterator != null) {
                return iterator.instantiate();
            } else {
                return null;
            }
        }

        private WizardDescriptor.InstantiatingIterator getServerIterator() {
            ServerWizardProvider server = getSelectedWizard();
            if (server == null) {
                return null;
            }

            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator)iterators.get(server);
            if (iterator != null) {
                return iterator;
            }


            iterator = server.getInstantiatingIterator();
            iterator.initialize(wd);
            iterators.put(server, iterator);
            return iterator;
        }

        public ServerWizardProvider getSelectedWizard() {
            return (ServerWizardProvider) wd.getProperty(PROP_SERVER_INSTANCE_WIZARD);
        }
    }
}
