/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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