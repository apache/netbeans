/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
