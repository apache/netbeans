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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
