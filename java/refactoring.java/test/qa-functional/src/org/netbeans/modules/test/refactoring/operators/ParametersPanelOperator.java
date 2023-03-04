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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class ParametersPanelOperator extends NbDialogOperator {

    public ParametersPanelOperator(String name) {
        super(name);
    }
    private JButtonOperator _btBack;
    private JButtonOperator _btPreview;
    JButtonOperator _btRefactor;
    private JCheckBoxOperator _chbxOpenInNewTab;

    public JButtonOperator btBack() {
        if (_btBack == null) {
            _btBack = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Back"));  // < Back
        }
        return _btBack;
    }

    public JButtonOperator btPreview() {
        if (_btPreview == null) {
            _btPreview = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_PreviewAll"));  //Preview
        }
        return _btPreview;
    }

    public JButtonOperator btRefactor() {
        if (_btRefactor == null) {
            _btRefactor = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish"));  // Refactor
        }
        return _btRefactor;
    }

    public JCheckBoxOperator cbxOpenInNewTab() {
        if (_chbxOpenInNewTab == null) {
            _chbxOpenInNewTab = new JCheckBoxOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "ParametersPanel.openInNewTab.text"));  // Open In New Tab
        }
        return _chbxOpenInNewTab;
    }

    /**
     * Pushes "< Back" button.
     */
    public void back() {
        btBack().push();
    }

    /**
     * Pushes "Preview" button.
     */
    public void preview() {
        btPreview().push();
    }

    /**
     * Pushes "Refactor" button.
     */
    public void refactor() {
        btRefactor().push();
    }
}
