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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.JmsDestinationDefinition;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageDestinationUiSupport;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.SendJMSMessageUiSupport.MdbHolder;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for SendJmsMessagePanel class.
 * <p>
 * This class contains only static methods.
 * @author Tomas Mysik
 * @see MessageDestinationUiSupport
 */
public abstract class SendJMSMessageUiSupport extends MessageDestinationUiSupport {

    /**
     * Get list of message-driven beans with all required properties.
     * @return list of message-driven beans.
     */
    public static List<MdbHolder> getMdbs(ChangeListener listener) {
        List<MdbHolder> mdbs = new ArrayList<>();

        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project p : openProjects) {
            if (EjbJar.getEjbJars(p).length > 0) {
                try {
                    Map<String, MessageDestination> drivens = getMdbs(p, listener);
                    populateMdbs(mdbs, drivens, p);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        return mdbs;
    }

    /**
     * Populate given combo box and text field with given message-driven beans.
     * @param mdbs message-driven beans for given combo box and text field.
     * @param comboBox combo box to populate.
     * @param textField text field to populate.
     */
    public static void populateMessageDrivenBeans(final List<MdbHolder> mdbs, final JComboBox comboBox,
            final JTextField textField) {
        assert mdbs != null;
        assert comboBox != null;
        assert textField != null;

        comboBox.setRenderer(new MdbHolderListCellRenderer());

        List<MdbHolder> sortedMdbs = new ArrayList<>(mdbs);
        sortedMdbs.sort(new MdbHolderComparator());

        comboBox.removeAllItems();
        textField.setText("");
        for (MdbHolder mdbHolder : sortedMdbs) {
            comboBox.addItem(mdbHolder);
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                populateMdbTextField((JComboBox) actionEvent.getSource(), textField);
            }
        });
        populateMdbTextField(comboBox, textField);
    }

    private static void populateMdbTextField(final JComboBox comboBox, final JTextField textField) {
        MdbHolder selectedItem = (MdbHolder) comboBox.getSelectedItem();
        if (selectedItem != null) {
            MessageDestination md = selectedItem.getMessageDestination();
            textField.setText(md != null ? md.getName() : "");
        }
    }

    private static void populateMdbs(List<MdbHolder> mdbs, final Map<String, MessageDestination> drivens, final Project project) {
        J2eeModuleProvider j2eeModuleProvider = getJ2eeModuleProvider(project);
        for (Map.Entry<String, MessageDestination> mdbEntry : drivens.entrySet()) {
            try {
                String mdbName = mdbEntry.getKey();
                MessageDestination destination = mdbEntry.getValue();

                // handle message destination deployment setup if any
                MessageDestination serverDestination = getServerMessageDestination(j2eeModuleProvider, destination.getName());
                destination = serverDestination != null ? serverDestination : destination;

                mdbs.add(new MdbHolder(mdbName, destination, project));
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
    }

    private static MessageDestination getServerMessageDestination(J2eeModuleProvider j2eeModuleProvider, String destinationName) throws ConfigurationException {
        // handle message destination deployment setup if any
        J2eeModuleProvider.ConfigSupport configSupport = j2eeModuleProvider.getConfigSupport();
        String serverDestName = configSupport.findMessageDestinationName(destinationName);

        if (serverDestName == null && destinationName != null) {
            serverDestName = destinationName;
        }

        if (serverDestName == null) {
            return null;
        }

        return configSupport.findMessageDestination(serverDestName);
    }

    private static J2eeModuleProvider getJ2eeModuleProvider(Project project) {
        return project.getLookup().lookup(J2eeModuleProvider.class);
    }

    // kay ejb-name and value is mapped-name
    private static Map<String, MessageDestination> getMdbs(Project project, final ChangeListener listener) throws IOException {

        Map<String, MessageDestination> mdbs = new HashMap<>();

        for (EjbJar ejbModule : EjbJar.getEjbJars(project)) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            MdbReadAction mdbReadAction = new MdbReadAction();
            HashMap<String, MessageDestination> mdbsInModule = new HashMap<>();

            // read current model data
            mdbsInModule.putAll(metadataModel.runReadAction(mdbReadAction));

            // read all data once processing/scanning is gone if requires (means not null listener)
            if (!metadataModel.isReady() && listener != null) {
                final MetadataModelReadHelper<EjbJarMetadata, Void> readHelper =
                        MetadataModelReadHelper.create(metadataModel,new MetadataModelAction<EjbJarMetadata, Void>() {
                    @Override
                    public Void run(EjbJarMetadata metadata) throws Exception {
                        return null;
                    }
                });
                readHelper.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (readHelper.getState() == MetadataModelReadHelper.State.FINISHED) {
                            listener.stateChanged(e);
                        }
                    }
                });
                readHelper.start();
            }

            mdbs.putAll(mdbsInModule);
        }

        return mdbs;
    }

//    private static void putMissingMDBs(Map<String, MessageDestination> result, HashMap<String, MessageDestination> mdbsInModule) {
//        for (Map.Entry<String, MessageDestination> entry : mdbsInModule.entrySet()) {
//            MessageDestination dest = result.get(entry.getKey());
//            if (dest == null) {
//                result.put(entry.getKey(), entry.getValue());
//            } else {
//                MessageDestination moduleDest = entry.getValue();
//                if (dest.getName() != null && !dest.getName().equals(moduleDest.getName())) {
//                    result.put(entry.getKey(), entry.getValue());
//                }
//            }
//        }
//    }

    private static class MdbReadAction implements MetadataModelAction<EjbJarMetadata, Map<String, MessageDestination>> {

        @Override
        public Map<String, MessageDestination> run(EjbJarMetadata metadata) throws Exception {
            Map<String, MessageDestination> result = new HashMap<>();
            EnterpriseBeans eb = metadata.getRoot().getEnterpriseBeans();
            if (eb == null) {
                return Collections.<String, MessageDestination>emptyMap();
            }

            MessageDriven[] messageDrivens = eb.getMessageDriven();
            for (MessageDriven mdb : messageDrivens) {
                result.put(mdb.getEjbName(), findMsgDest(mdb));
            }
            return result;
        }
    }

    // fix for 162899
    // not very nice solution, but the interface
    // org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven is not nice too.
    private static MessageDestination findMsgDest(MessageDriven mdb) {
        try {
            // try EJB 3.0 style first
            String destName = mdb.getMappedName();
            MessageDestination.Type destType = MessageDestination.Type.QUEUE;
            ActivationConfig activationConfig = mdb.getActivationConfig();
            if (activationConfig != null) {
                for (ActivationConfigProperty property : activationConfig.getActivationConfigProperty()) {
                    if (null != property.getActivationConfigPropertyName()) {
                        switch (property.getActivationConfigPropertyName()) {
                            case "destinationLookup":   //NOI18N
                                destName = property.getActivationConfigPropertyValue();
                                break;
                            case "destinationType":     //NOI18N
                                destType = "javax.jms.Topic".equals(property.getActivationConfigPropertyValue()) ? //NOI18N
                                        MessageDestination.Type.TOPIC : MessageDestination.Type.QUEUE;
                                break;
                        }
                    }
                }
            }

            return new JmsDestinationDefinition(destName, destType, false);
        } catch (VersionNotSupportedException e) {
            Logger.getLogger(SendJMSMessageUiSupport.class.getName()).info("Older than EJB 3.0 is not supported any more.");
            return null;
        }
    }

    /**
     * Holder for message-driven bean and its properties.
     */
    public static class MdbHolder {
    
        private final String mdbEjbName;
        private final MessageDestination messageDestination;
        private final Project project;

        /** Constructor with all properties. */
        public MdbHolder(String mdbEjbName, final MessageDestination messageDestination, final Project project) {
            assert mdbEjbName != null;
//            assert messageDestination != null;
            assert project != null;

            this.mdbEjbName = mdbEjbName;
            this.messageDestination = messageDestination;
            this.project = project;
        }

        public MessageDestination getMessageDestination() {
            return messageDestination;
        }

        public Project getProject() {
            return project;
        }

        public String getMdbEjbName() {
            return mdbEjbName;
        }

        public String getProjectName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getName());
            sb.append(" [");
            sb.append(getMdbEjbName());
            sb.append(" (");
            sb.append(messageDestination != null ? messageDestination.getType().toString() : "");
            sb.append("), ");
            sb.append(getProjectName());
            sb.append("]");
            return sb.toString();
        }
    }

    // optional - create factory method for this class
    private static class MdbHolderComparator implements Comparator<MdbHolder> {

        @Override
        public int compare(MdbHolder mdbHolder1, MdbHolder mdbHolder2) {

            if (mdbHolder1 == null) {
                return mdbHolder2 == null ? 0 : -1;
            }

            if (mdbHolder2 == null) {
                return 1;
            }

            String name1 = mdbHolder1.getMdbEjbName();
            String name2 = mdbHolder2.getMdbEjbName();
            if (name1 == null) {
                return name2 == null ? 0 : -1;
            }

            return name2 == null ? 1 : name1.compareToIgnoreCase(name2);
        }
    }

    // optional - create factory method for this class
    private static class MdbHolderListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof MdbHolder) {
                MdbHolder mdbHolder = (MdbHolder) value;

                // "ejbName (projectName)"
                setText(mdbHolder.getMdbEjbName() + " (" + mdbHolder.getProjectName() + ")"); //NOI18N

                // tooltip
                MessageDestination messageDestination = mdbHolder.getMessageDestination();
                if (messageDestination == null) {
                    setToolTipText(""); // NOI18N
                } else {
                    String type = MessageDestination.Type.QUEUE.equals(messageDestination.getType()) ? "LBL_Queue" : "LBL_Topic"; // NOI18N
                    StringBuilder sb = new StringBuilder(mdbHolder.getProjectName());
                    sb.append(" : "); // NOI18N
                    sb.append(mdbHolder.getMdbEjbName());
                    sb.append(" ["); // NOI18N
                    sb.append(NbBundle.getMessage(MessageDestinationUiSupport.class, type));
                    sb.append("]"); // NOI18N
                    setToolTipText(sb.toString());
                }
            } else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            return this;
        }
    }
}
