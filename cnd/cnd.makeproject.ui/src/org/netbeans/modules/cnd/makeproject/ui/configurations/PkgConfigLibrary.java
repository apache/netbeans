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

/*
 * PkgConfigLibrary.java
 *
 * Created on Dec 1, 2010, 2:59:53 PM
 */

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class PkgConfigLibrary extends javax.swing.JPanel {
    private final MyListCellRenderer myListCellRenderer = new MyListCellRenderer();
    private final List<PackageConfiguration> avaliablePkgConfigs;
    private static final RequestProcessor RP = new RequestProcessor("PkgConfigLibrary init",1); //NOI18N

    /** Creates new form PkgConfigLibrary */
    public PkgConfigLibrary(final ExecutionEnvironment env, final MakeConfiguration conf, final JButton okButton) {
        initComponents();
	list.setCellRenderer(myListCellRenderer);
        avaliablePkgConfigs = new ArrayList<>();
        avaliablePkgConfigs.add(new Waiting());
        okButton.setEnabled(false);
        list.setModel(new AbstractListModel() {
            @Override
            public int getSize() {
                return avaliablePkgConfigs.size();
            }
            @Override
            public Object getElementAt(int i) {
                return avaliablePkgConfigs.get(i);
            }
        });
        filter.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateModel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateModel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateModel();
            }
        });
        RP.post(new Runnable() {

            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    Iterator<PackageConfiguration> iterator = avaliablePkgConfigs.iterator();
                    if (iterator.hasNext() && (iterator.next() instanceof Error)) {
                        okButton.setEnabled(false);
                    } else {
                        okButton.setEnabled(true);
                    }
                    list.setModel(new AbstractListModel() {
                        @Override
                        public int getSize() {
                            return avaliablePkgConfigs.size();
                        }
                        @Override
                        public Object getElementAt(int i) {
                            return avaliablePkgConfigs.get(i);
                        }
                    });

                } else {
                    if (HostInfoUtils.isHostInfoAvailable(env)) {
                        PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(env, conf);
                        TreeMap<String, PackageConfiguration> map = new TreeMap<>();
                        pkgConfig.getAvaliablePkgConfigs().forEach((conf) -> {
                            map.put(conf.getName(), conf);
                        });
                        avaliablePkgConfigs.clear();
                        avaliablePkgConfigs.addAll(map.values());
                        SwingUtilities.invokeLater(this);
                    } else {
                        avaliablePkgConfigs.clear();
                        avaliablePkgConfigs.add(new Error());
                        SwingUtilities.invokeLater(this);
                    }
                }
            }
        });
    }

    private void updateModel() {
        String pattern = filter.getText().trim().toLowerCase(Locale.getDefault());
        final List<PackageConfiguration> res = new ArrayList<>();
        avaliablePkgConfigs.forEach((conf) -> {
            if (conf.getName().toLowerCase(Locale.getDefault()).contains(pattern)){
                res.add(conf);
            } else if (conf.getDisplayName().toLowerCase(Locale.getDefault()).contains(pattern)){
                res.add(conf);
            }
        });
        list.setModel(new AbstractListModel() {
            @Override
            public int getSize() {
                return res.size();
            }
            @Override
            public Object getElementAt(int i) {
                return res.get(i);
            }
        });
    }

    public PackageConfiguration[] getPkgConfigLibs() {
    	Object[] selectedValues = list.getSelectedValues();
        PackageConfiguration[] selectedLibs = new PackageConfiguration[selectedValues.length];
        for (int i = 0; i < selectedValues.length; i++) {
            selectedLibs[i] = (PackageConfiguration)selectedValues[i];
        }
        return selectedLibs;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        filterLabel = new javax.swing.JLabel();
        filter = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        label.setLabelFor(scrollPane);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(PkgConfigLibrary.class, "PkgConfigLibrary.label.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(label, gridBagConstraints);

        scrollPane.setPreferredSize(new java.awt.Dimension(300, 300));
        scrollPane.setViewportView(list);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(scrollPane, gridBagConstraints);

        filterLabel.setLabelFor(filter);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(PkgConfigLibrary.class, "PkgConfigLibrary.filterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(filterLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(filter, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filter;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel label;
    private javax.swing.JList list;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    private static final class MyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    PackageConfiguration libraryItem = (PackageConfiguration)value;
            if (libraryItem instanceof Waiting) {
                label.setText(libraryItem.getName());
                label.setIcon(getWaitIcon());
            } else if (libraryItem instanceof Error) {
                label.setText(libraryItem.getName());
                label.setIcon(getErrorIcon());
            } else {
                label.setIcon(getLibraryIcon());
                label.setText(libraryItem.getName());
                String message = NbBundle.getMessage(PkgConfigLibrary.class, "PkgConfigLibrary.tooltip.text", //NOI18N
                        libraryItem.getDisplayName(), libraryItem.getVersion(), libraryItem.getLibs());
                label.setToolTipText(message);
            }
            return label;
        }
        private ImageIcon getLibraryIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/resources/stdLibrary.gif", false); //NOI18N
        }
        private ImageIcon getWaitIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/waitNode.gif", false); //NOI18N
        }
        private ImageIcon getErrorIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/exclamation.gif", false); //NOI18N
        }
    }

    private static final class Waiting implements PackageConfiguration {
        private static final String NAME = NbBundle.getMessage(PkgConfigLibrary.class, "Init_PkgConfigLibrary_List"); //NOI18N

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }

        @Override
        public String getVersion() {
            return ""; //NOI18N
        }

        @Override
        public Collection<String> getIncludePaths() {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getMacros() {
            return Collections.emptyList();
        }

        @Override
        public String getLibs() {
            return ""; //NOI18N
        }
    }

    private static final class Error implements PackageConfiguration {
        private static final String NAME = NbBundle.getMessage(PkgConfigLibrary.class, "Error_PkgConfigLibrary_List");

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }

        @Override
        public String getVersion() {
            return ""; //NOI18N
        }

        @Override
        public Collection<String> getIncludePaths() {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getMacros() {
            return Collections.emptyList();
        }

        @Override
        public String getLibs() {
            return ""; //NOI18N
        }
    }
}
