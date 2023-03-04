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
package org.netbeans.modules.analysis.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventObject;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.analysis.Configuration;
import org.netbeans.modules.analysis.ConfigurationsManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "CTL_NewConfig=New...",
    "ConfigDefaultName=newConfig",
    "CTL_Rename=Rename...",
    "CTL_Delete=Delete",
    "CTL_Duplicate=Duplicate",
    "MSG_ReallyDeleteConfig=Really want to delete {0}",
    "DeleteConfigTitle=Delete Configuration"
})
public class ConfigurationsComboModel extends AbstractListModel implements ComboBoxModel, ChangeListener {

    private New aNew = new New(Bundle.CTL_NewConfig(), Bundle.ConfigDefaultName());
    private Delete delete = new Delete();
    private New duplicate = new New(Bundle.CTL_Duplicate(), null);
    private Rename rename = new Rename();
    private Object selected;
    private Configuration lastSelected;
    private boolean canModify;
            Confirmable currentActiveItem;

    public ConfigurationsComboModel(boolean canModify) {
        selected = getSize() == 0 ? null :getElementAt(0);
        lastSelected = (Configuration) selected;
        this.canModify = canModify;
        ConfigurationsManager.getDefault().addChangeListener(WeakListeners.change(this, ConfigurationsManager.getDefault()));
    }

    @Override
    public int getSize() {
        return ConfigurationsManager.getDefault().size() + (canModify ? 4 : 0);
    }

    @Override
    public Object getElementAt(int i) {
        if (canModify) {
            if (i == getSize() - 4) {
                return aNew;
            } else if (i == getSize() - 3) {
                return duplicate;
            } else if (i == getSize() - 2) {
                return rename;
            } else if (i == getSize() - 1) {
                return delete;
            }
        }
        return ConfigurationsManager.getDefault().getConfiguration(i);
    }

    public boolean canModify() {
        return canModify;
    }

    @Override
    public void setSelectedItem(Object o) {
        setLastSelected(selected);
        selected = o;
        fireContentsChanged(this, -1, -1);
    }

    private void setLastSelected(Object o) {
        if (o instanceof Configuration) {
            lastSelected = (Configuration) o;
        }
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        fireContentsChanged(this, -1, -1);
    }

    private class New implements ActionListener, FocusListener, KeyListener, PopupMenuListener, Confirmable {

        private final String actionName;
        private final String configName;

        public New(String actionName, String configName) {
            this.actionName = actionName;
            this.configName = configName;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            currentActiveItem = this;
            
            JComboBox combo = (JComboBox) ae.getSource();
            combo.setEditable(true);
            combo.getEditor().getEditorComponent().addFocusListener(this);
            combo.getEditor().getEditorComponent().addKeyListener(this);
            combo.addPopupMenuListener(this);
            combo.setSelectedItem(configName == null ? lastSelected + "1" : configName);
        }

        @Override
        public String toString() {
            return actionName;
        }

        @Override
        public void focusGained(FocusEvent fe) {
        }

        @Override
        public void focusLost(FocusEvent fe) {
            confirm(fe);
        }

        public void confirm(EventObject fe) {
            JTextField tf = (JTextField) fe.getSource();
            JComboBox combo = (JComboBox) tf.getParent();
            if (combo==null)
                return;
            if (fe instanceof FocusEvent) {
                combo.getEditor().getEditorComponent().removeFocusListener(this);
            } else {
                combo.getEditor().getEditorComponent().removeKeyListener(this);
            }
            Configuration config = configName==null ? 
                    ConfigurationsManager.getDefault().duplicate(lastSelected, tf.getText(), tf.getText()):
                    ConfigurationsManager.getDefault().create(tf.getText(), tf.getText());
            combo.setSelectedItem(config);
            combo.setEditable(false);
            currentActiveItem = null;
        }

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER || ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                confirm(ke);
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox combo = (JComboBox) e.getSource();
            confirm(new EventObject(combo.getEditor().getEditorComponent()));
            combo.removePopupMenuListener(this);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private class Rename implements ActionListener, FocusListener, KeyListener, PopupMenuListener, Confirmable {

        @Override
        public void actionPerformed(ActionEvent ae) {
            currentActiveItem = this;
            
            JComboBox combo = (JComboBox) ae.getSource();
            combo.setEditable(true);
            JTextField editorComponent = (JTextField) combo.getEditor().getEditorComponent();
            editorComponent.addFocusListener(this);
            editorComponent.addKeyListener(this);
            combo.setSelectedItem(lastSelected);
            combo.addPopupMenuListener(this);
        }

        @Override
        public String toString() {
            return Bundle.CTL_Rename();
        }

        @Override
        public void focusGained(FocusEvent fe) {
        }

        @Override
        public void focusLost(FocusEvent fe) {
            confirm(fe);
        }

        public void confirm(EventObject fe) {
            JTextField tf = (JTextField) fe.getSource();
            JComboBox combo = (JComboBox) tf.getParent();
            if (combo==null)
                return;
            if (fe instanceof FocusEvent) {
                combo.getEditor().getEditorComponent().removeFocusListener(this);
            } else {
                combo.getEditor().getEditorComponent().removeKeyListener(this);
            }
            Configuration config = lastSelected;
            config.setDisplayName(tf.getText());
            combo.setSelectedItem(config);
            combo.setEditable(false);
            currentActiveItem = null;
        }

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyChar() == KeyEvent.VK_ENTER || ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                confirm(ke);
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }
       
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox combo = (JComboBox) e.getSource();
            confirm(new EventObject(combo.getEditor().getEditorComponent()));
            combo.removePopupMenuListener(this);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        
    }

    private class Delete implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ConfigurationsManager.getDefault().size() == 1) {
                setSelectedItem(getElementAt(0));
                return;
            }
            final JComboBox combo = (JComboBox) ae.getSource();
            combo.setSelectedItem(lastSelected);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (JOptionPane.showConfirmDialog(combo, 
                    Bundle.MSG_ReallyDeleteConfig(lastSelected),
                    Bundle.DeleteConfigTitle(),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        ConfigurationsManager.getDefault().remove(lastSelected);
                        setSelectedItem(getElementAt(0));
                    }
                }
            });
        }

        @Override
        public String toString() {
            return Bundle.CTL_Delete();
        }
    }
    
    interface Confirmable {
        public void confirm(EventObject fe);
    }
}
