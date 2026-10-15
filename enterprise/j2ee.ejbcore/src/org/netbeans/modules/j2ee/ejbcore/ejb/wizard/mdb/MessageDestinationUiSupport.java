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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport.Context;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.JmsDestinationDefinition;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.javaee.resources.api.JmsDestination;
import org.netbeans.modules.javaee.specs.support.api.util.JndiNamespacesDefinition;
import org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel;
import org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Support for MessageDestinationPanel class.
 * <p>
 * This class contains only static methods.
 * @author Tomas Mysik
 */
public abstract class MessageDestinationUiSupport {

    /**
     * Get module and server message destinations.
     * <p>
     * <b>Destinations are fetched asynchronously.</b>
     * @param j2eeModuleProvider 
     * @return holder with both module and server message destinations.
     */
    public static DestinationsHolder getDestinations(final Project project, final J2eeModuleProvider j2eeModuleProvider) {
        assert j2eeModuleProvider != null;
        final DestinationsHolder holder = new DestinationsHolder();
        
        if (SwingUtilities.isEventDispatchThread()) {
            // fetch references & datasources asynchronously
            ProgressSupport.Action action = new ProgressSupport.BackgroundAction() {

                public void run(Context actionContext) {
                    String msg = NbBundle.getMessage(MessageDestinationUiSupport.class, "MSG_RetrievingDestinations");
                    actionContext.progress(msg);
                    try {
                        holder.setModuleDestinations(getProjectMessageDestinations(project, j2eeModuleProvider));
                        holder.setServerDestinations(j2eeModuleProvider.getConfigSupport().getServerMessageDestinations());
                    } catch (ConfigurationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };

            Collection<ProgressSupport.Action> asyncActions = Collections.singleton(action);
            ProgressSupport.invoke(asyncActions);
        } else {
            try {
                holder.setModuleDestinations(j2eeModuleProvider.getConfigSupport().getMessageDestinations());
                holder.setServerDestinations(j2eeModuleProvider.getConfigSupport().getServerMessageDestinations());
            } catch (ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return holder;
    }
 
    /**
     * Populate given combo box with given message destinations.
     * @param destinations message destinations for given combo box.
     * @param comboBox combo box to populate.
     * @param selectedItem combo box item to be selected. If it's <code>null</code> no item selection is done.
     */
    public static void populateDestinations(final Set<MessageDestination> destinations, final JComboBox comboBox,
            final MessageDestination selectedItem) {
        assert destinations != null;
        assert comboBox != null;
        
        comboBox.setRenderer(new MessageDestinationListCellRenderer());
        
        List<MessageDestination> sortedDestinations = new ArrayList<MessageDestination>(destinations);
        sortedDestinations.sort(new MessageDestinationComparator());
        
        comboBox.removeAllItems();
        for (MessageDestination d : sortedDestinations) {
            comboBox.addItem(d);
        }
        
        // select item?
        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        }
    }
    
    /**
     * Open the dialog for adding message destination. If the javaEE platform is EJB3.2+ it will return instance
     * of the (@link JmsDestinationDefinition} with flag for generation. Such destination has to be created as an
     * annotation inside the source file. Otherwise (for EJB3.1-) it will create static resource using the
     * {@link J2eeModuleProvider.ConfigSupport#createMessageDestination}.
     * @param project project where we are generating
     * @param j2eeModuleProvider Java EE module provider.
     * @param moduleDestinations module message destinations.
     * @param serverDestinations server message destinations.
     * @return created message destination or <code>null</code> if no message destination is created.
     */
    public static MessageDestination prepareMessageDestination(final Project project, final J2eeModuleProvider j2eeModuleProvider,
            final Set<MessageDestination> moduleDestinations, final Set<MessageDestination> serverDestinations) {
        assert j2eeModuleProvider != null;
        assert moduleDestinations != null;
        assert serverDestinations != null;
        
        // message destination names - create map for faster searching
        Map<String, MessageDestination.Type> destinations = new HashMap<String, MessageDestination.Type>();
        for (MessageDestination md : moduleDestinations) {
            destinations.put(md.getName(), md.getType());
        }
        for (MessageDestination md : serverDestinations) {
            destinations.put(md.getName(), md.getType());
        }

        J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        MessageDestinationPanel messageDestination = MessageDestinationPanel.newInstance(
                destinations, capabilities.isEjb32Supported(), JndiNamespacesDefinition.APPLICATION_NAMESPACE);
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                messageDestination,
                NbBundle.getMessage(MessageDestinationPanel.class, "LBL_AddMessageDestination"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(MessageDestinationPanel.class),
                null);
        NotificationLineSupport statusLine = dialogDescriptor.createNotificationLineSupport();
        messageDestination.setNotificationLine(statusLine);
        // initial invalidation
        dialogDescriptor.setValid(false);
        messageDestination.addPropertyChangeListener(MessageDestinationPanel.IS_VALID,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object newvalue = evt.getNewValue();
                        if (newvalue instanceof Boolean) {
                            dialogDescriptor.setValid(((Boolean) newvalue));
                        }
                    }
                }
        
        );
        
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        MessageDestination md = null;
        if (option == DialogDescriptor.OK_OPTION) {
            if (capabilities.isEjb32Supported()) {
                md = new JmsDestinationDefinition(
                        messageDestination.getDestinationName(),
                        messageDestination.getDestinationType(),
                        true);
            } else {
                md = createMessageDestination(
                        j2eeModuleProvider,
                        messageDestination.getDestinationName(),
                        messageDestination.getDestinationType());
            }
        }
        
        return md;
    }
    
    // this method has to be called asynchronously!
    private static MessageDestination createMessageDestination(final J2eeModuleProvider j2eeModuleProvider,
            final String destinationName, final MessageDestination.Type destinationType) {
        final MessageDestination[] messageDestinations = new MessageDestination[1];

        ProgressSupport.Action action = new ProgressSupport.BackgroundAction() {
            @Override
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(MessageDestinationUiSupport.class, "MSG_CreatingDestination");
                actionContext.progress(msg);
                try {
                    messageDestinations[0] = j2eeModuleProvider.getConfigSupport().createMessageDestination(destinationName, destinationType);
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            }
        };

        Collection<ProgressSupport.Action> asyncActions = Collections.singleton(action);
        ProgressSupport.invoke(asyncActions);

        return messageDestinations[0];
    }

    public static Set<MessageDestination> getProjectMessageDestinations(Project p, J2eeModuleProvider j2eeModuleProvider) {
        final Set<MessageDestination> allDestinations = new HashSet<MessageDestination>();

        try {
            // server specific, deployable destinations
            allDestinations.addAll(j2eeModuleProvider.getConfigSupport().getMessageDestinations());
        } catch (ConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

        // by project defined JNDI destinations
        try {
            JndiResourcesModelSupport.getModel(p).runReadAction(new MetadataModelAction<JndiResourcesModel, Void>() {
                @Override
                public Void run(JndiResourcesModel metadata) throws Exception {
                    for (final JmsDestination jmsDestination : metadata.getJmsDestinations()) {
                        Type type;
                        if ("jakarta.ejb.Topic".equals(jmsDestination.getClassName()) //NOI18N
                                || "javax.ejb.Topic".equals(jmsDestination.getClassName())) { //NOI18N
                            type = Type.TOPIC;
                        } else {
                            type = Type.QUEUE;
                        }
                        allDestinations.add(new JmsDestinationDefinition(jmsDestination.getName(), type, false));
                    }
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return allDestinations;
    }

    
    /**
     * Holder for message destinations (module- and server-).
     */
    public static class DestinationsHolder {
        private Set<MessageDestination> moduleDestinations;
        private Set<MessageDestination> serverDestinations;
        
        public DestinationsHolder() {
            super();
        }

        public synchronized void setModuleDestinations(final Set<MessageDestination> moduleDestinations) {
            this.moduleDestinations = moduleDestinations;
        }

        public synchronized void setServerDestinations(final Set<MessageDestination> serverDestinations) {
            this.serverDestinations = serverDestinations;
        }

        public synchronized Set<MessageDestination> getModuleDestinations() {
            if (moduleDestinations == null) {
                moduleDestinations = new HashSet<MessageDestination>();
            }
            return moduleDestinations;
        }

        public synchronized Set<MessageDestination> getServerDestinations() {
            if (serverDestinations == null) {
                serverDestinations = new HashSet<MessageDestination>();
            }
            return serverDestinations;
        }
    }
    
    // optional - create factory method for this class
    private static class MessageDestinationComparator implements Comparator<MessageDestination> {
        
        public int compare(MessageDestination md1, MessageDestination md2) {
            
            if (md1 == null) {
                return md2 == null ? 0 : -1;
            }
            
            if (md2 == null) {
                return 1;
            }
            
            String destName1 = md1.getName();
            String destName2 = md2.getName();
            if (destName1 == null) {
                return destName2 == null ? 0 : -1;
            }
            
            return destName2 == null ? 1 : destName1.compareToIgnoreCase(destName2);
        }
    }

    // optional - create factory method for this class
    private static class MessageDestinationListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof MessageDestination) {
                MessageDestination md = (MessageDestination) value;
                setText(md.getName());
                // tooltip
                String type = MessageDestination.Type.QUEUE.equals(md.getType()) ? "LBL_Queue" : "LBL_Topic"; // NOI18N
                StringBuilder sb = new StringBuilder(md.getName());
                sb.append(" ["); // NOI18N
                sb.append(NbBundle.getMessage(MessageDestinationUiSupport.class, type));
                sb.append("]"); // NOI18N
                setToolTipText(sb.toString());
            } else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            return this;
        }
    }
}
