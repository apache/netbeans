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
import javax.swing.JPanel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.editor.search.SearchBar;

/**
 *
 * @author jprox
 */
public class SearchBarOperator extends EditorPanelOperator {

    private JTextComponentOperator findOp;
    private JButtonOperator nextButtonOp;
    private JButtonOperator prevButtonOp;
    private JButtonOperator closeButtonOp;
    private JCheckBoxOperator match;
    private JCheckBoxOperator whole;
    private JCheckBoxOperator regular;
    private JCheckBoxOperator highlight;
    private JCheckBoxOperator wrap;
    
    private SearchBarOperator() {
        super(SearchBar.class);
    }

    @Override
    protected void invokeAction(EditorOperator editorOperator) {
        editorOperator.pushKey(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
    }

    @Override
    protected JButton getExpandButton() {
        if(buttons.size()<=3) return null;
        if(buttons.size()==4) return buttons.get(2);
        return null;
        
    }
    
    public static SearchBarOperator invoke(EditorOperator editorOperator) {
        SearchBarOperator sbo = new SearchBarOperator();
        sbo.openPanel(editorOperator);
        return sbo;
    }
    
    public static SearchBarOperator getPanel(EditorOperator editorOperator) {
        SearchBarOperator sbo = new SearchBarOperator();
        JPanel panel = sbo.getOpenedPanel(editorOperator);
        if(panel==null) throw new IllegalArgumentException("Panel is not found");
        return sbo;
    }
    
    public JTextComponentOperator findCombo() {
        if (findOp == null) {
            findOp = new JTextComponentOperator(getContainerOperator());
        }
        return findOp;
    }

    public JButtonOperator prevButton() {
        if (prevButtonOp == null) {
            prevButtonOp = new JButtonOperator(getButton(0));
        }
        return prevButtonOp;
    }

    public JButtonOperator nextButton() {
        if (nextButtonOp == null) {
            nextButtonOp = new JButtonOperator(getButton(1));
        }
        return nextButtonOp;
    }

    public JButtonOperator closeButton() {
        if (closeButtonOp == null) {
            closeButtonOp = new JButtonOperator(getButton(buttons.size()-1));
        }
        return closeButtonOp;
    }

    public JCheckBoxOperator matchCaseCheckBox() {
        return getCheckbox(0);

    }

    public JCheckBoxOperator highlightResultsCheckBox() {
        return getCheckbox(3);

    }

    public JCheckBoxOperator reqularExpressionCheckBox() {
        return getCheckbox(2);

    }

    public JCheckBoxOperator wholeWordsCheckBox() {
        return getCheckbox(1);

    }
    
    public JCheckBoxOperator wrapAroundCheckBox() {
        return getCheckbox(4);
    }

    void uncheckAll() {
        matchCaseCheckBox().setSelected(false);
        highlightResultsCheckBox().setSelected(false);
        reqularExpressionCheckBox().setSelected(false);
//        highlightResultsCheckBox().setSelected(false);
//        wrapAroundCheckBox().setSelected(false);
    }
    
}
