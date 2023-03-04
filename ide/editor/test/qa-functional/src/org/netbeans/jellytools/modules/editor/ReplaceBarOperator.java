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

import java.awt.event.KeyEvent;
import javax.swing.JButton;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.editor.search.ReplaceBar;

/**
 *
 * @author jprox
 */
public class ReplaceBarOperator extends EditorPanelOperator {

    private JTextComponentOperator replaseWithOp;
    private JButtonOperator replaceOp;
    private JButtonOperator replaceAllOp;    
    
    private EditorOperator editor;
    private SearchBarOperator sbo;

    @Override
    protected void invokeAction(EditorOperator editorOperator) {
        editorOperator.pushKey(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
        editor = editorOperator;
        

    }

    @Override
    protected JButton getExpandButton() {
        if (buttons.size() <= 3) {
            return null;
        }
        if (buttons.size() == 4) {
            return buttons.get(3);
        }
        return null;
    }

    public ReplaceBarOperator() {
        super(ReplaceBar.class);
    }

    public JTextComponentOperator replaceCombo() {
        if (replaseWithOp == null) {
            replaseWithOp = new JTextComponentOperator(getContainerOperator());
        }
        return replaseWithOp;
    }

    public JButtonOperator replaceButton() {
        if (replaceOp == null) {
            replaceOp = new JButtonOperator(getButton(0));
        }
        return replaceOp;
    }

    public JButtonOperator replaceAll() {
        if (replaceAllOp == null) {
            replaceAllOp = new JButtonOperator(getButton(1));
        }
        return replaceAllOp;
    }
    
    public JCheckBoxOperator replaceBackwardsCheckBox() {
        return getCheckbox(0);

    }

    public JCheckBoxOperator preserveCaseCheckBox() {
        return getCheckbox(1);
    }

    public static ReplaceBarOperator invoke(EditorOperator editorOperator) {
        ReplaceBarOperator rbo = new ReplaceBarOperator();
        rbo.openPanel(editorOperator);
        return rbo;
    }
    
    public SearchBarOperator getSearchBar() {
        if(sbo==null) {
            sbo = SearchBarOperator.getPanel(editor);
        }
        return sbo;
    }
    
    public JButtonOperator closeButton() {
        SearchBarOperator searchBar = getSearchBar();
        return searchBar.closeButton();
    }

    public void uncheckAll() {
        replaceBackwardsCheckBox().setSelected(false);
        preserveCaseCheckBox().setSelected(false);
        getSearchBar().uncheckAll();
    }
}
