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
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@oracle.com
 */
public class RenameOperator extends ParametersPanelOperator{
    private JTextFieldOperator textField;
    private JCheckBoxOperator commments;
    private JCheckBoxOperator property;
    private JLabelOperator error;
    
    public RenameOperator() {
        super("Rename");
    }

    public JTextFieldOperator getNewName() {
        if(textField==null) {
            textField = new JTextFieldOperator(this);
        }
        return textField;
    }

    public JCheckBoxOperator getComments() {
        if(commments==null) {
            commments = new JCheckBoxOperator(this);
        }
        return commments;
    }
    
    public JCheckBoxOperator getProperty() {
        if(property == null) {
            property = new JCheckBoxOperator(this, 1);
        }
        return property;
    }

    public JLabelOperator getError() {
        if(error==null) {
            error = new JLabelOperator(this);
        }
        return error;
    }
           
}
