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
package org.netbeans.test.web;

import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author jindra
 */
public class RenameDialogOperator extends JDialogOperator {

    public RenameDialogOperator() {
        super("Rename");
    }

    public void preview(){
        new JButtonOperator(this, "Preview").push();
    }

    public void refactor(){
        new JButtonOperator(this, "Refactor").push();
    }

    public void cancel(){
        new JButtonOperator(this, "Cancel").push();
    }

    public String getNewName(){
        return getNewNameFieldOperator().getText();
    }

    public void setNewName(String name){
        getNewNameFieldOperator().setText(name);
    }

    public JTextFieldOperator getNewNameFieldOperator(){
        JTextField jtf = (JTextField) new JLabelOperator(this, "New Name:").getLabelFor();
        return new JTextFieldOperator(jtf);
    }

    public void setEnabledUnrelatedOccurences(boolean enabled){
        getUnrelatedOccurencesOperator().changeSelection(enabled);
    }

    private JCheckBoxOperator getUnrelatedOccurencesOperator(){
        return new JCheckBoxOperator(this, "Unrelated Occurrences");
    }
}
