/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.search.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.util.WeakListeners;

// suspending the use of EditorActionRegistration due to #167063

//    @EditorActionRegistration(name = BaseKit.toggleHighlightSearchAction,
@EditorActionRegistration(name = BaseKit.toggleHighlightSearchAction, iconResource = "org/netbeans/modules/editor/search/resources/toggle_highlight.png") // NOI18N
public class ToggleHighlightSearchAction extends AbstractEditorAction {
    static final long serialVersionUID = 4603809175771743200L;


    public ToggleHighlightSearchAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        JToggleButton b = new MyGaGaButton();
        b.setModel(new HighlightButtonModel());
        b.setAction(this);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Boolean cur = (Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
            if (cur == null || cur.booleanValue() == false) {
                cur = Boolean.TRUE;
            } else {
                cur = Boolean.FALSE;
            }
            EditorFindSupport.getInstance().putFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, cur);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        JToggleButton b = new MyGaGaButton();
        b.setModel(new HighlightButtonModel());
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        b.setAction(this);
        return b;
    }

    private static final class HighlightButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {

        public HighlightButtonModel() {
            EditorFindSupport efs = EditorFindSupport.getInstance();
            efs.addPropertyChangeListener(WeakListeners.propertyChange(this, efs));
            propertyChange(null);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
                Boolean value = (Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
                setSelected(value == null ? false : value.booleanValue());
            }
        }
    } // End of HighlightButtonModel class

    private static final class MyGaGaButton extends JToggleButton implements ChangeListener {

        public MyGaGaButton() {
        }

        @Override
        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }
            super.setModel(model);
            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        @Override
        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        @Override
        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    }

} // End of ToggleHighlightSearchAction class
