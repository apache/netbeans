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
package org.netbeans.api.java.platform;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.openide.util.WeakListeners;

public final class PlatformsCustomizer {

    /**
     * The name of the platform unique id property in registered NetBeans
     */
    public static final String PROP_PLATFORM_ID = "platform.ant.name"; //NOI18N

    private static final Specification ALL_JAVA2SE = new Specification("J2SE", null); //NOI18N

    private PlatformsCustomizer() {

    }

    /**
     * Shows platforms customizer.
     *
     * @param platform which should be selected, may be null
     * @return boolean for future extension, currently always true
     */
    public static boolean showCustomizer(JavaPlatform platform) {
        org.netbeans.modules.java.platform.ui.PlatformsCustomizer customizer
                = new org.netbeans.modules.java.platform.ui.PlatformsCustomizer(platform);
        javax.swing.JButton close = new javax.swing.JButton(NbBundle.getMessage(PlatformsCustomizer.class, "CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformsCustomizer.class, "AD_Close"));
        DialogDescriptor descriptor = new DialogDescriptor(customizer, NbBundle.getMessage(PlatformsCustomizer.class,
                "TXT_PlatformsManager"), true, new Object[]{close}, close, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(PlatformsCustomizer.class), null); // NOI18N
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return true;
    }

    /**
     * Returns an initialized ComboBox displaying all the installed JavaSE
     * platforms.
     *
     * @since 1.51
     * @see PlatformComboBoxModel#PlatformComboBoxModel()
     *
     * @return a combo box initialized with a suitable model and renderer to
     * display the installed Java Platforms.
     */
    public static JComboBox<JavaPlatform> createPlatformComboBox() {
        return createPlatformComboBox(createPlatformModel());
    }

    /**
     * Utility method to create a combo box with Java platforms.
     *
     * @since 1.51
     * @param model The model for the Java Platform ComboBox.
     * @return a combo box initialized with the given model and a suitable
     * renderer to display the installed Java Platforms.
     */
    public static JComboBox<JavaPlatform> createPlatformComboBox(ComboBoxModel<JavaPlatform> model) {
        JComboBox<JavaPlatform> ret = new JComboBox<>(model);
        ret.setRenderer(new PlatformRenderer());
        return ret;
    }

    /**
     * Create a model containing all JavaSE platforms registered in the IDE.
     * @return the ComboBoxModel
     * @since 1.51
     */
    public static ComboBoxModel<JavaPlatform> createPlatformModel() {
        return createPlatformModel(ALL_JAVA2SE);
    }

    /**
     * Create a model containing Java platforms registered in the IDE that
     * matches with the provided {@link Specification}.
     * Returned model can accept a JavaPlatform or any object whose toString()
     * method returns a registered Java Platform ID as selected item. This
     * platform id can be obtained from a Java platform by getting its
     * {@link #PROP_PLATFORM_ID} property.
     *
     * @param spec filter for the Java Platforms
     * @return the ComboBoxModel
     * @since 1.51
     */
    public static ComboBoxModel<JavaPlatform> createPlatformModel(Specification spec) {
        return new PlatformComboBoxModel(spec);
    }

    private static class PlatformComboBoxModel extends AbstractListModel<JavaPlatform> implements ComboBoxModel<JavaPlatform>, PropertyChangeListener {

        private static final Logger LOG = Logger.getLogger(PlatformComboBoxModel.class.getName());

        private final Specification specification;
        private JavaPlatform[] data;
        private Object sel;

        @SuppressWarnings("LeakingThisInConstructor")
        public PlatformComboBoxModel(Specification specification) {
            this.specification = specification;
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        public int getSize() {
            return data.length;
        }

        @Override
        public JavaPlatform getElementAt(int index) {
            return data[index];
        }

        @Override
        public void setSelectedItem(Object anItem) {
            boolean changed = sel != anItem;
            sel = anItem;
            if (changed) {
                fireContentsChanged(this, 0, data.length);
            }
        }

        @Override
        public Object getSelectedItem() {
            return sel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String current = sel instanceof JavaPlatform ? ((JavaPlatform) sel).getProperties().get(PROP_PLATFORM_ID) : sel.toString();
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            JavaPlatform found = null;
            for (int i = 0; i < data.length; i++) {
                JavaPlatform pf = data[i];
                if (current.equals(pf.getProperties().get(PROP_PLATFORM_ID))) {
                    found = pf;
                    break;
                }
            }
            setSelectedItem(found != null ? found : current);
        }

        private void getPlatforms(JavaPlatformManager jpm) {
            data = jpm.getPlatforms(null, specification);
            if (LOG.isLoggable(Level.FINE)) {
                for (JavaPlatform jp : data) {
                    LOG.log(Level.FINE, "Adding JavaPlaform: {0}", jp.getDisplayName()); //NOI18N
                }
            }
        }

    }

    private static class PlatformRenderer extends JLabel implements ListCellRenderer, UIResource {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public PlatformRenderer() {
            setOpaque(true);
        }

        @Override
        @NbBundle.Messages({"# {0} - platformId", "LBL_MissingPlatform=Missing platform: {0}"})
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            if (value instanceof JavaPlatform) {
                JavaPlatform jp = (JavaPlatform) value;
                setText(jp.getDisplayName());
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
            } else {
                if (value == null) {
                    setText("");
                } else {
                    setText(Bundle.LBL_MissingPlatform(value));
                    setForeground(UIManager.getColor("nb.errorForeground")); //NOI18N
                }
            }
            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    } // end of PlatformsRenderer

}
