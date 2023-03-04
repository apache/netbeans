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
package org.netbeans.qa.form.gaps;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Adam Senk adam.senk@oracle.com
 */
public class EditLayoutSpaceOperator extends JDialogOperator {
    
    public EditLayoutSpaceOperator(){
        super("Edit Layout Space"); 
    }
    
    public JButtonOperator btOk(){
        return new JButtonOperator(this, "OK");
    }
    
    public JButtonOperator btCancel(){
        return new JButtonOperator(this, "Cancel");
    }    
    
    public void Ok(){
        btOk().push();
    }
    
    public void Cancel(){
        btCancel().push();
    }
    
    public JComboBoxOperator cbDefinedSize(){
        return new JComboBoxOperator(this, 0);
    }
    public JComboBoxOperator cbLeft(){
        return new JComboBoxOperator(this, 0);
    }
    public JComboBoxOperator cbRight(){
        return new JComboBoxOperator(this, 1);
    }
    public JComboBoxOperator cbTop(){
        return new JComboBoxOperator(this, 2);
    }
    public JComboBoxOperator cbBottom(){
        return new JComboBoxOperator(this, 3);
    }
    
    public void setSizeOfGap(String size){
        cbDefinedSize().enterText(size);        
    }
    
    public void setSizeOfGapLeft(String size){
        cbLeft().enterText(size);        
    }
    public void setSizeOfGapRight(String size){
        cbRight().enterText(size);        
    }
    public void setSizeOfGapTop(String size){
        cbTop().enterText(size);        
    }
    public void setSizeOfGapBottom(String size){
        cbBottom().enterText(size);        
    }
    
    public void verifySmall(){
        btOk();
        btCancel();
        cbDefinedSize();
    }
    
    public void verify(){
        btOk();
        btCancel();
        cbLeft();
        cbRight();
        cbTop();
        cbBottom();
    }
}
