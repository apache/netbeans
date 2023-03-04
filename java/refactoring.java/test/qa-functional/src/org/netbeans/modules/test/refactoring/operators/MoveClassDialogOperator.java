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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class MoveClassDialogOperator extends ParametersPanelOperator{
    private JComboBoxOperator _cbxProjects;
    private JComboBoxOperator _cbxLocation;
    private JComboBoxOperator _cbxToPackage;
    private JLabelOperator _lblTitle;
    private JLabelOperator _lblError;


    public MoveClassDialogOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle","LBL_MoveClass")); // Move Class
    }

    public JComboBoxOperator getProjectsCombo() {
        if(_cbxProjects==null) {
            _cbxProjects = new JComboBoxOperator(this, 0);
        }
        return _cbxProjects;
    }


    public JComboBoxOperator getLocationCombo() {
        if(_cbxLocation==null) {
            _cbxLocation = new JComboBoxOperator(this, 1);
        }
        return _cbxLocation;
    }

    public JComboBoxOperator getPackageCombo() {
        if(_cbxToPackage==null) {
            _cbxToPackage = new JComboBoxOperator(this, 2);
        }
        return _cbxToPackage;
    }

    public JLabelOperator getTitleLabel() {
        if(_lblTitle==null) {
            _lblTitle = new JLabelOperator(this,0);
        }
        return _lblTitle;
    }

    public JLabelOperator getError() {
        if(_lblError==null) {
            _lblError = new JLabelOperator(this,1);
        }
        return _lblError;
    }
}
