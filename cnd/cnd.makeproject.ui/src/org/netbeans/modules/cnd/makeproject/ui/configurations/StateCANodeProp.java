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
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JCheckBox;
import org.openide.nodes.PropertySupport;

/**
 *
 */
public final class StateCANodeProp extends PropertySupport<StateCA> {
    private static final JCheckBox checkBox = new JCheckBox();
    private final StateCA value;
    private PropertyEditor editor;

    public StateCANodeProp(StateCA value, String displayName, String hint) {
        super("CodeAssistance", StateCA.class, displayName,hint, true, false); //NOI18N
        this.value = value;
    }

    @Override
    public StateCA getValue() {
        return value;
    }

    @Override
    public void setValue(StateCA v) {
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (editor == null) {
            editor = new PropertyEditorImpl();
        }
        return editor;
    }

    private class PropertyEditorImpl implements PropertyEditor {

        public PropertyEditorImpl() {
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public boolean isPaintable() {
            return true;
        }

        @Override
        public void paintValue(Graphics g, Rectangle box) {
            checkBox.setForeground(g.getColor());
            checkBox.setFont(g.getFont());
            g.translate(box.x, box.y);
            StateCA state = (StateCA) getValue();
            checkBox.setText(state.toString());
            checkBox.setEnabled(false);
            switch (state) {
                case ParsedSource:
                case ExtraParsedSource:
                case IncludedHeader:
                case ParsedOrphanHeader:
                    checkBox.setSelected(true);
                    break;
                case ExcludedHeader:
                case ExcludedSource:
                case NotYetParsed:
                    checkBox.setSelected(false);
                    break;
                default:
                    assert false : "unexpected " + state;
            }
            checkBox.setSize(box.width, box.height);
            checkBox.paint(g);
            g.translate(-box.x, -box.y);
        }

        @Override
        public String getJavaInitializationString() {
            return ""; //NOI18N
        }

        @Override
        public String getAsText() {
            return value.toString();
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
        }

        @Override
        public String[] getTags() {
            StateCA[] values = StateCA.values();
            String[] out = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                out[i] = values[i].toString();
            }
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
    }
    
}
