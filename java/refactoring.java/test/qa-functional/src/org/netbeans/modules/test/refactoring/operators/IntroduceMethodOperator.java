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

import java.awt.Component;
import javax.swing.JLabel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * <p> @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceMethodOperator extends ParametersPanelOperator {

    private JTextFieldOperator textField;
    private JCheckBoxOperator alsoReplace;
    private JCheckBoxOperator property;
    private JLabelOperator error;
    private JRadioButtonOperator radPublic;
    private JRadioButtonOperator radProtected;
    private JRadioButtonOperator radDefault;
    private JRadioButtonOperator radPrivate;

    public IntroduceMethodOperator() {
        super("Introduce method");
    }

    public JTextFieldOperator getNewName() {
        if (textField == null) {
            textField = new JTextFieldOperator(this);
        }
        return textField;
    }

    public JCheckBoxOperator getAlsoReplace() {
        if (alsoReplace == null) {
            alsoReplace = new JCheckBoxOperator(this);
        }
        return alsoReplace;
    }

    public JLabelOperator getError() {
        if (error == null) {
            error = new JLabelOperator(this);
        }
        return error;
    }

    public JRadioButtonOperator getRadPublic() {
        if (radPublic == null) {
            radPublic = new JRadioButtonOperator(this, 0);
        }
        return radPublic;
    }

    public JRadioButtonOperator getRadProtected() {
        if (radProtected == null) {
            radProtected = new JRadioButtonOperator(this, 1);
        }
        return radProtected;
    }

    public JRadioButtonOperator getRadDefault() {
        if (radDefault == null) {
            radDefault = new JRadioButtonOperator(this, 2);
        }
        return radDefault;
    }

    public JRadioButtonOperator getRadPrivate() {
        if (radPrivate == null) {
            radPrivate = new JRadioButtonOperator(this, 3);
        }
        return radPrivate;
    }
}
