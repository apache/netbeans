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

import org.netbeans.jemmy.operators.*;

/**
 * <p>
 * @author (stanislav.sazonov@oracle.com)
 */
public abstract class IntroduceOperator extends ParametersPanelOperator {

    private JTextFieldOperator textField;
    private JLabelOperator error;
    private JRadioButtonOperator radPublic;
    private JRadioButtonOperator radProtected;
    private JRadioButtonOperator radDefault;
    private JRadioButtonOperator radPrivate;
//    private JButtonOperator btnRefactor;

    public IntroduceOperator(String caption) {
        super(caption);
    }

    public JTextFieldOperator getNewName() {
        if(textField == null)
            textField = new JTextFieldOperator(this);
        return textField;
    }

    public JLabelOperator getError() {
        if(error == null)
            error = new JLabelOperator(this);
        return error;
    }

    public JRadioButtonOperator getRadPublic() {
        if(radPublic == null)
            radPublic = new JRadioButtonOperator(this, "public");
        return radPublic;
    }

    public JRadioButtonOperator getRadProtected() {
        if(radProtected == null)
            radProtected = new JRadioButtonOperator(this, "protected");
        return radProtected;
    }

    public JRadioButtonOperator getRadDefault() {
        if(radDefault == null)
            radDefault = new JRadioButtonOperator(this, "default");
        return radDefault;
    }

    public JRadioButtonOperator getRadPrivate() {
        if(radPrivate == null)
            radPrivate = new JRadioButtonOperator(this, "private");
        return radPrivate;
    }

//    public JButtonOperator getBtnRefactor() {
//        if(btnRefactor == null) {
//            btnRefactor = new JButtonOperator(this, "Refactor");
//        }
//        return btnRefactor;
//    }
}
