/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
