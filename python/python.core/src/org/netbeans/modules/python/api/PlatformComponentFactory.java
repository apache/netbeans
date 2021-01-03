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
package org.netbeans.modules.python.api;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class PlatformComponentFactory {

    /** Generally usable in conjuction with {@link #createComboWaitModel}. */
    private static final String DETECTING_VALUE =
            NbBundle.getMessage(PlatformComponentFactory.class, "PlatformComponentFactory.detetctingPlatforms");
    
    public static final Color INVALID_PLAF_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N

    private PlatformComponentFactory() {
        // don't allow instances
    }

    /**
     * Returns <code>JComboBox</code> initialized with {@link
     * PythonPlatformListModel} which contains all Python platform.
     */
    public static JComboBox getPythonPlatformsComboxBox() {
        final JComboBox plafComboBox = new JComboBox();
        plafComboBox.setRenderer(new PythonPlatformListRenderer());
        if (Util.isFirstPlatformTouch()) {
            plafComboBox.setModel(createComboWaitModel());
            plafComboBox.setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    PythonPlatformManager manager = PythonPlatformManager.getInstance();
                    manager.addVetoableChangeListener(new VetoableChangeListener() {
                        @Override
                        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                            PythonPlatformListModel model = new PythonPlatformListModel();
                            plafComboBox.setModel(model);
                            plafComboBox.setEnabled(model.getSize() > 0);
                        }
                    });
                    manager.autoDetect();
                }
            });
        } else {
            plafComboBox.setModel(new PythonPlatformListModel());
        }
        return plafComboBox;
    }

    public static PythonPlatform getPlatform(final JComboBox platforms) {
        Object value = platforms.getModel().getSelectedItem();
        return (value == DETECTING_VALUE) ? null : (PythonPlatform) value;
    }

    public static boolean isLoadingPlatforms(JComboBox platforms) {
        Object value = platforms.getModel().getSelectedItem();
        return value == DETECTING_VALUE;
    }

    /**
     * Returns <code>JList</code> initialized with {@link PythonPlatformListModel}
     * which contains all Python platform.
     */
    public static JList getPythonPlatformsList() {
        final JList plafList = new JList(createComboWaitModel());
        plafList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plafList.setCellRenderer(new PythonPlatformListRenderer());
        if (Util.isFirstPlatformTouch()) {
            plafList.setModel(createListWaitModel());
            plafList.setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    PythonPlatformManager manager = PythonPlatformManager.getInstance();
                    manager.addVetoableChangeListener(new VetoableChangeListener() {
                        @Override
                        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                            PythonPlatformListModel model = new PythonPlatformListModel();
                            plafList.setModel(model);
                            plafList.setEnabled(model.getSize() > 0);
                        }
                    });
                    manager.autoDetect();
                }
            });
        } else {
            plafList.setModel(new PythonPlatformListModel());
        }
        return plafList;
    }

    /**
     * Use this model in situation when you need to populate combo in the
     * background. The only item in this model is {@link #DETECTING_VALUE}.
     */
    public static ComboBoxModel createComboWaitModel() {
        return new DefaultComboBoxModel(new Object[]{ DETECTING_VALUE });
    }

    /**
     * Use this model in situation when you need to populate list in the
     * background. The only item in this model is {@link #DETECTING_VALUE}.
     */
    public static ListModel createListWaitModel() {
        DefaultListModel listModel = new DefaultListModel();
        listModel.addElement(DETECTING_VALUE);
        return listModel;
    }

    public static void addPlatformChangeListener(final JComboBox platforms, final PlatformChangeListener pcl) {
        platforms.addItemListener(pcl);
        platforms.addPropertyChangeListener(pcl);
    }

    public static void removePlatformChangeListener(final JComboBox platforms, final PlatformChangeListener pcl) {
        platforms.removeItemListener(pcl);
        platforms.removePropertyChangeListener(pcl);
    }

    public static abstract class PlatformChangeListener implements ItemListener, PropertyChangeListener {
        
        public abstract void platformChanged();

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                platformChanged();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // when the model has changed from "Detectin platform" to valid
            // platform model itemStateChanged is not fired(??)
            if (evt.getPropertyName().equals("model")) { // NOI18N
                platformChanged();
            }
        }
    }

    /**
     * Returns model containing all <em>currently</em> registered PythonPlatforms.
     * See also {@link PythonPlatform#getPlatforms}.
     * <p>Use in conjuction with {@link PythonPlatformListRenderer}</p>
     */
    public static class PythonPlatformListModel extends AbstractListModel
            implements ComboBoxModel {

        private static PythonPlatform[] getSortedPlatforms(PythonPlatform extra) {
            PythonPlatformManager manager = PythonPlatformManager.getInstance();
            List<String> platformNames = manager.getPlatformList();
            Set<PythonPlatform> _platforms = new HashSet<>();
            for (String platformName : platformNames) {
                _platforms.add(manager.getPlatform(platformName));
            }
            if (extra != null) {
                _platforms.add(extra);
            }

            PythonPlatform[] platforms = _platforms.toArray(new PythonPlatform[_platforms.size()]);
            Arrays.sort(platforms, new Comparator<PythonPlatform>() {
                @Override
                public int compare(PythonPlatform p1, PythonPlatform p2) {
                    String p1Name = p1.getName();
                    String p2Name = p2.getName();
                    if (p1Name == null) {
                        p1Name = "";
                    }
                    if (p2Name == null) {
                        p2Name = "";
                    }
                    int res = Collator.getInstance().compare(p1Name,p2Name);
                    if (res != 0) {
                        return res;
                    } else {
                        return System.identityHashCode(p1) - System.identityHashCode(p2);
                    }
                }
            });
            return platforms;
        }
        private PythonPlatform[] nbPlafs;
        private Object selectedPlaf;

        public PythonPlatformListModel() {
            this(null);
        }

        public PythonPlatformListModel(final PythonPlatform initiallySelected) {
            nbPlafs = getSortedPlatforms(initiallySelected);
            if (initiallySelected == null) {
                if (nbPlafs.length > 0) {
                    selectedPlaf = nbPlafs[0];
                }
            } else {
                selectedPlaf = initiallySelected;
            }
        }

        @Override
        public int getSize() {
            return nbPlafs.length;
        }

        @Override
        public Object getElementAt(int index) {
            return index < nbPlafs.length ? nbPlafs[index] : null;
        }

        @Override
        public void setSelectedItem(Object plaf) {
            assert plaf == null || plaf instanceof PythonPlatform;
            if (selectedPlaf != plaf) {
                selectedPlaf = plaf;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedPlaf;
        }

        void removePlatform(PythonPlatform plaf) {
            PythonPlatformManager.getInstance().removePlatform(plaf.getId());
            nbPlafs = getSortedPlatforms(null); // refresh
            fireContentsChanged(this, 0, nbPlafs.length - 1);
        }
    }
    
    /**
     * Render {@link PythonPlatform}.
     * <p>Use in conjuction with {@link PythonPlatformListModel}</p>
     */
    private static class PythonPlatformListRenderer extends JLabel implements ListCellRenderer, UIResource {

        public PythonPlatformListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            String label;
            if (value instanceof String) {
                label = (String) value;
            } else {
                PythonPlatform plaf = ((PythonPlatform) value);
                if (plaf == null || !plaf.isValid()) {
                    label = NbBundle.getMessage(PlatformComponentFactory.class, "PlatformComponentFactory.select.valid.platform");
                    setForeground(INVALID_PLAF_COLOR);
                } else {
                    label = plaf.getName();
                }
            }
            setText(label);

            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
}
