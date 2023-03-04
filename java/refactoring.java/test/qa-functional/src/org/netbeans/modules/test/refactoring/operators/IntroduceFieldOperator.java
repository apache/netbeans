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

import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 * <p> @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceFieldOperator extends IntroduceOperator {

    private JCheckBoxOperator declareFinal;
    private JCheckBoxOperator alsoReplace;
    
    private JRadioButtonOperator inCurrentMethod;
    private JRadioButtonOperator inField;
    private JRadioButtonOperator inConstructor;

    public IntroduceFieldOperator() {
        super("Introduce field");
    }

    public JCheckBoxOperator getDeclareFinal() {
        if (declareFinal == null) {
            declareFinal = new JCheckBoxOperator(this, 0);
        }
        return declareFinal;
    }
    
    public JCheckBoxOperator getAlsoReplace() {
        if (alsoReplace == null) {
            alsoReplace = new JCheckBoxOperator(this, 1);
        }
        return alsoReplace;
    }
    
    public JRadioButtonOperator getInCurrentMethod() {
        if (inCurrentMethod == null) {
            inCurrentMethod = new JRadioButtonOperator(this, "Current Method");
        }
        return inCurrentMethod;
    }

    public JRadioButtonOperator getInField() {
        if (inField == null) {
            inField = new JRadioButtonOperator(this, "Field");
        }
        return inField;
    }

    public JRadioButtonOperator getInConstructor() {
        if (inConstructor == null) {
            inConstructor = new JRadioButtonOperator(this, "Constructor(s)");
        }
        return inConstructor;
    }
}
