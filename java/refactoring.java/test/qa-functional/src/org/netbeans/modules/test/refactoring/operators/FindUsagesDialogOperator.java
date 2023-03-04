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
package org.netbeans.modules.test.refactoring.operators;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.ComboBoxModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class FindUsagesDialogOperator extends ParametersPanelOperator {

    private JButtonOperator _btFind;
    private JLabelOperator _lblUsagesOf;
    private JCheckBoxOperator _chbxSearchInComments;
    private JRadioButtonOperator _rbtFindUsages;
    private JRadioButtonOperator _rbtFindDirectSubtypes;
    private JRadioButtonOperator _rbtFindAllSubtypes;
    private JRadioButtonOperator _rbtFindOverridding;
    private JRadioButtonOperator _rbtFindMethodUsage;
    private JComboBoxOperator _cbxScope;

    public FindUsagesDialogOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_FindUsagesDialog"));
    }

    public JButtonOperator getFind() {
        if (_btFind == null) {
            _btFind = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Find"));  // Find
        }
        return _btFind;
    }

    public JLabelOperator getLabel() {
        if (_lblUsagesOf == null) {
            _lblUsagesOf = new JLabelOperator(this);
        }
        return _lblUsagesOf;
    }

    public JRadioButtonOperator getFindAllSubtypes() {
        if (_rbtFindAllSubtypes == null) {
            _rbtFindAllSubtypes = new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_FindAllSubtypes")); // Find All Subtypes
        }
        return _rbtFindAllSubtypes;
    }

    public JRadioButtonOperator getFindDirectSubtypesOnly() {
        if (_rbtFindDirectSubtypes == null) {
            _rbtFindDirectSubtypes = new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_FindDirectSubtypesOnly")); // Find Direct Subtypes Only
        }
        return _rbtFindDirectSubtypes;
    }

    public JRadioButtonOperator getFindUsages() {
        if (_rbtFindUsages == null) {
            _rbtFindUsages = new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_FindUsages")); // Find Usages
        }
        return _rbtFindUsages;
    }

    public JCheckBoxOperator getSearchInComments() {
        if (_chbxSearchInComments == null) {
            _chbxSearchInComments = new JCheckBoxOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_SearchInComents")); // Search in Comments
        }
        return _chbxSearchInComments;
    }

    public JComboBoxOperator getScope() {
        if (_cbxScope == null) {
            _cbxScope = new JComboBoxOperator(this);
        }
        return _cbxScope;
    }

    public JRadioButtonOperator getFindUsagesAndOverridingMethods() {
        if (_rbtFindMethodUsage == null) {
            _rbtFindMethodUsage = new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_FindUsagesOverridingMethods")); // Find Usages and Overriding Methods
        }
        return _rbtFindMethodUsage;
    }

    public JRadioButtonOperator getFindOverriddingMethods() {
        if (_rbtFindOverridding == null) {
            _rbtFindOverridding = new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_FindOverridingMethods")); // Find Overriding Methods
        }
        return _rbtFindOverridding;
    }

    /**
     * Pushes "Find" button.
     */
    public void find() {
        getFind().pushNoBlock();
    }

    /**
     * Select the scope
     *
     * @param projectName The name of project or null if find should be
     * performed on all projects
     */
    public void setScope(String projectName) {
        JComboBoxOperator scopeOperator = getScope();
        String select_item;
        if (projectName == null) {
            select_item = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.scope.Bundle", "LBL_AllProjects");
        } else {
            select_item = projectName;
        }

        ComboBoxModel model = scopeOperator.getModel();
        int index = -1;
        String dn;
        for (int i = 0; i < model.getSize()-1; i++) { /// -1 ... it's custom and it fails
            dn = ((org.netbeans.modules.refactoring.spi.impl.DelegatingScopeProvider) model.getElementAt(i)).getDisplayName();
            if (dn.indexOf(select_item) != -1) {
                index = i;
            }
        }
        scopeOperator.selectItem(index);
    }

    public String getSelectedScopeItem() {
        return ((org.netbeans.modules.refactoring.spi.impl.DelegatingScopeProvider) getScope().getSelectedItem()).getDisplayName();
    }
}
