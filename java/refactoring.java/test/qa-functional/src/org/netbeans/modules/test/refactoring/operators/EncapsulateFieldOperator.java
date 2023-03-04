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

import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * <p> @author (stanislav.sazonov@oracle.com)
 */
public class EncapsulateFieldOperator extends IntroduceOperator {

    public EncapsulateFieldOperator() {
        super("Encapsulate Fields");
    }
        
    public void setValueAt(int y, int x, boolean val){
        JTableOperator table = new JTableOperator(this, 0); 
        JTable t = ((JTable)table.getSource());                
        TableModel model = t.getModel();
        model.setValueAt(val, y, x);
    }
    
    public void setValueAt(int y, int x, String val){
        JTableOperator table = new JTableOperator(this, 0); 
        JTable t = ((JTable)table.getSource());                
        TableModel model = t.getModel();
        model.setValueAt(val, y, x);
    }
    
    public void setInsertPoint(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 4);
        combo.selectItem(s);
    }
    
    public void setSortBy(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 2);
        combo.selectItem(s);
    }
    
    public void setJavadoc(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 1);
        combo.selectItem(s);
    }
    
    public void setFieldsVisibility(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem(s);
    }
    
    public void setAccessorsVisibility(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 3);
        combo.selectItem(s);
    }
    
    //------------------------------------------------
    public void setItemToScope(int index){
        JComboBoxOperator combo = new JComboBoxOperator(this, index);
        combo.selectItem(2);
    }
    
    public void setChreckbox(int index, boolean b){
        JCheckBoxOperator combo = new JCheckBoxOperator(this, index);
        combo.setSelected(b);
    }
    //------------------------------------------------
    
    public void setUseAccessorsEvenWhenFieldIsAccessible(boolean b){
        JCheckBoxOperator check = new JCheckBoxOperator(this, 2);
        check.setSelected(b);
    }
    
    public void setGeneratePropertyChangeSupport(boolean b){
        JCheckBoxOperator check = new JCheckBoxOperator(this, 1);
        check.setSelected(b);
    }
    
    public void setGenerateVetoableChangeSupport(boolean b){
        JCheckBoxOperator check = new JCheckBoxOperator(this, 0);
        check.setSelected(b);
    }
    
    public void selectAll(){
        JButtonOperator button = new JButtonOperator(this, "Select All");
        button.clickMouse();
    }
    
    public void selectNone(){
        JButtonOperator button = new JButtonOperator(this, "Select None");
        button.clickMouse();
    }
    
    public void selectGetters(){
        JButtonOperator button = new JButtonOperator(this, "Select Getters");
        button.clickMouse();
    }
    
    public void selectSetters(){
        JButtonOperator button = new JButtonOperator(this, "Select Setters");
        button.clickMouse();
    }
}
