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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 */
public class PlatformSpecificProp extends BooleanNodeProp  implements PropertyChangeListener{
    private PropertyEditor editor;
    private final BooleanConfiguration value;
    private String platform;
    private final MakeConfiguration makeConfiguration;

    public PlatformSpecificProp(MakeConfiguration makeConfiguration, BooleanConfiguration value, boolean canWrite, String id, String name, String hint) {
        super(value, canWrite, id, name, hint);
        this.value = value;
        this.makeConfiguration = makeConfiguration;
        makeConfiguration.getDevelopmentHost().addPropertyChangeListener(this);
        platform = Platforms.getPlatform(makeConfiguration.getDevelopmentHost().getBuildPlatform()).getDisplayName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof  DevelopmentHostConfiguration) {
            platform = Platforms.getPlatform(makeConfiguration.getDevelopmentHost().getBuildPlatform()).getDisplayName();
            PropertyEditor ed = getPropertyEditor();
            if (ed instanceof PropertyEditorSupport) {
                ((PropertyEditorSupport) ed).firePropertyChange();
            }
        }
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (editor == null) {
            editor = new PropertyEditorImpl();
        }
        return editor;
    }

    private class PropertyEditorImpl extends PropertyEditorSupport implements ExPropertyEditor {
        private final JCheckBox checkBox = new JCheckBox();
        private PropertyEnv propenv;

        public PropertyEditorImpl() {
        }

        @Override
        public void setValue(Object obj) {
            if (obj instanceof Boolean) {
                Boolean v = (Boolean)obj;
                checkBox.setSelected(v);
            }
        }

        @Override
        public Object getValue() {
            return checkBox.isSelected();
        }

        @Override
        public boolean isPaintable() {
            return true;
        }

        @Override
        public void firePropertyChange() {
            super.firePropertyChange();
        }

        @Override
        public void paintValue(Graphics g, Rectangle box) {
            checkBox.setSize(box.width, box.height);
            checkBox.doLayout();
            checkBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            if (checkBox.isSelected()) {
                checkBox.setText(getTags()[1]);
            } else {
                checkBox.setText(getTags()[0]);
            }
            Graphics gr = g.create(box.x, box.y, box.width, box.height);
            checkBox.setOpaque(false);
            checkBox.paint(gr);
            gr.dispose();
        }

        @Override
        public String getAsText() {
            if (checkBox.isSelected()) {
                return getTags()[1];
            } else {
                return getTags()[0];
            }
        }

        @Override
        public String[] getTags() {
            String[] out = new String[] {
                NbBundle.getMessage(PlatformSpecificProp.class, "PlatformSpecificFalse"),
                NbBundle.getMessage(PlatformSpecificProp.class, "PlatformSpecificTrue", platform)};
            return out;
        }

        @Override
        public Component getCustomEditor() {
            throw new IllegalStateException();
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            propenv = env;
        }
    }

}