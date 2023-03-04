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

package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
;
import org.netbeans.test.subversion.operators.actions.RelocateAction;

/**
 *
 * @author peterpis
 */
public class RelocateOperator extends NbDialogOperator {

    private JLabelOperator _lblWC;
    private JLabelOperator _lblCurrentURL;
    private JLabelOperator _lblNewURL;
    private JTextFieldOperator _tfWC;
    private JTextFieldOperator _tfCurrentURL;
    private JTextFieldOperator _tfNewURL;
    private JButtonOperator _btnRelocate;
    private JButtonOperator _btnCancel;
    private JButtonOperator _btnHelp;

    public RelocateOperator() {
        super("Relocate");
    }

    public static RelocateOperator invoke(Node node) {
        new RelocateAction().perform(node);
        return new RelocateOperator();
    }


    public JLabelOperator lblWC() {
        if (_lblWC == null) {
            _lblWC = new JLabelOperator(this, "Working Copy:");
        }
        return _lblWC;
    }

    public JLabelOperator lblCurrentURL() {
        if (_lblCurrentURL == null) {
            _lblCurrentURL = new JLabelOperator(this, "Current URL:");
        }
        return _lblCurrentURL;
    }

    public JLabelOperator lblNewURL() {
        if (_lblNewURL == null) {
            _lblNewURL = new JLabelOperator(this, "New URL:");
        }
        return _lblNewURL;
    }

    public JTextFieldOperator tfWC() {
        if (_tfWC == null) {
            _tfWC = new JTextFieldOperator(this, 0);
        }
        return _tfWC;
    }

    public JTextFieldOperator tfCurrentURL() {
        if (_tfCurrentURL == null) {
            _tfCurrentURL = new JTextFieldOperator(this, 1);
        }
        return _tfCurrentURL;
    }

    public JTextFieldOperator tfNewURL() {
        if (_tfNewURL == null) {
            _tfNewURL = new JTextFieldOperator(this, 2);
        }
        return _tfNewURL;
    }

    public void typeText(String text) {
        tfNewURL().typeText(text);
    }

    public JButtonOperator btnRelocate() {
        if (_btnRelocate == null) {
            _btnRelocate = new JButtonOperator(this, "Relocate");
        }
        return _btnRelocate;
    }

    public JButtonOperator btnCancel() {
        if (_btnCancel == null) {
            _btnCancel = new JButtonOperator(this, "Cancel");
        }
        return _btnCancel;
    }

    public JButtonOperator btnHelp() {
        if (_btnHelp == null) {
            _btnHelp = new JButtonOperator(this, "Help");
        }
        return _btnHelp;
    }

    public void verify() {
        lblWC();
        lblCurrentURL();
        lblNewURL();
        tfWC();
        tfCurrentURL();
        tfNewURL();
    }
}
