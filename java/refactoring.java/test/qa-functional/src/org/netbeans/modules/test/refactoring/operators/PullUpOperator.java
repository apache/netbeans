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
import java.awt.Container;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * <p> @author (stanislav.sazonov@oracle.com)
 */
public class PullUpOperator extends IntroduceOperator {

    public PullUpOperator() {
        super("Pull Up");
    }
        
    public void setValueAt(int y, int x, boolean val){
        JTableOperator table = new JTableOperator(this, 0); 
        JTable t = ((JTable)table.getSource());                
        TableModel model = t.getModel();
        model.setValueAt(val, y, x);
    }
    
    public void setDestinationSupertype(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this);
        combo.selectItem(s);
    }
    
    public void pressPreview(){
        JButtonOperator button = new JButtonOperator(this, "Preview");
        button.clickMouse();
    }
    
    public void pressRefactor(){
        JButtonOperator button = new JButtonOperator(this, "Refactor");
        button.clickMouse();
    }
    
    public void presstCancel(){
        JButtonOperator button = new JButtonOperator(this, "Cancel");
        button.clickMouse();
    }
    
    public void pressHelp(){
        JButtonOperator button = new JButtonOperator(this, "Help");
        button.clickMouse();
    }
    
    public void printAllComponents(){
        System.out.println("**************************");
        printComp(getContentPane(), "");
        System.out.println("**************************");
    }
    
    public void printComp(Container c, String s){
        System.out.println(s + c.getClass().getName());
        if(c instanceof Container){
            for (Component com : c.getComponents()) {
                printComp((Container) com, s + "__");
            }
        }
    }
}
