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
package org.netbeans.jellytools.modules.editor;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.jellytools.MainWindowOperator;

/**
 * Generic support for Search and Replace bar in editor.
 *
 * @author jprox
 */
public abstract class EditorPanelOperator {

    private Class<? extends JPanel> panelClass;
    private JPanel panel;
    private boolean isPopupVisible;
    private List<JCheckBox> checkBoxesInPanel = new LinkedList<JCheckBox>();
    protected List<JButton> buttons = new LinkedList<JButton>();

    public EditorPanelOperator(Class<? extends JPanel> panelClass) {
        this.panelClass = panelClass;
    }
        
    protected JPanel openPanel(final EditorOperator editor) {
        invokeAction(editor);           
        return getOpenedPanel(editor);
    }

    protected abstract void invokeAction(final EditorOperator editorOperator);

    private JPanel findPanel(final Container comp) {
        if (comp.getClass().getName().equals(panelClass.getName())) {
            return (JPanel) comp;
        }
        Component[] coms = comp.getComponents();
        for (Component component : coms) {
            if (Container.class.isAssignableFrom(component.getClass())) {
                JPanel panel = findPanel((Container) component);
                if (panel != null) {
                    return panel;
                }
            }
        }
        return null;
    }

    private void expandPopup() {
        if (!isPopupVisible) {
            JButtonOperator jButtonOperator = new JButtonOperator(getExpandButton());
            jButtonOperator.doClick();
            new EventTool().waitNoEvent(250);
            isPopupVisible = true;
        }
    }
    
    private void closePopup() {
        if (isPopupVisible) {
            ContainerOperator containerOperator = new ContainerOperator(panel);
            containerOperator.pressMouse();
            new EventTool().waitNoEvent(100);
            containerOperator.releaseMouse();
            isPopupVisible = false;
        }
    }

    protected abstract JButton getExpandButton();

    private JPopupMenuOperator getPopupMenuOperator() {
        expandPopup();
        JPopupMenuOperator jPopupMenuOperator;
        jPopupMenuOperator = new JPopupMenuOperator(new ContainerOperator<JPanel>(panel));
        return jPopupMenuOperator;
    }
              
      
    protected JCheckBoxOperator getCheckbox(int number) {
        if (number<checkBoxesInPanel.size()) {
            return new JCheckBoxOperator(checkBoxesInPanel.get(number));
        } else {
            JPopupMenuOperator jpmo = getPopupMenuOperator();
            return new JCheckBoxOperator(jpmo, number - checkBoxesInPanel.size()+1);
        }
    }
    
    protected JButton getButton(int number) {
        return buttons.get(number);
    }
    

    public ContainerOperator<JPanel> getContainerOperator() {
        return new ContainerOperator<JPanel>(panel);
    }
    
    public boolean isVisible() {
        return panel.isVisible();
    }

    protected JPanel getOpenedPanel(EditorOperator editorOperator) {
        for (int i = 0; i < 10; i++) {
            this.panel =  findPanel((Container) editorOperator.getSource());
            if(panel!=null) break;
            new EventTool().waitNoEvent(200);
        }
        if(panel==null) return null;
        for (Component c : panel.getComponents()) {
            if (c instanceof JCheckBox) {
                checkBoxesInPanel.add((JCheckBox)c);
            } else if(c instanceof JButton) {
                buttons.add((JButton)c);
            }
        }      
        return panel;
    }

}
