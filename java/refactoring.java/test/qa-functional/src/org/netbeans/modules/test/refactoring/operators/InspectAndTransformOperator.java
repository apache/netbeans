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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 * <p> @author (stanislav.sazonov@oracle.com)
 */
public class InspectAndTransformOperator extends NbDialogOperator {

/*
    javax.swing.JPanel
    __org.netbeans.modules.refactoring.spi.impl.ParametersPanel
    ____javax.swing.JPanel
    ______javax.swing.JPanel
    ________javax.swing.JPanel
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    ________javax.swing.JCheckBox
    ______javax.swing.JPanel
    ________org.netbeans.modules.refactoring.spi.impl.TooltipLabel
    ____javax.swing.JPanel
    ______org.netbeans.modules.java.hints.spiimpl.refactoring.InspectAndRefactorPanel
    ________javax.swing.JLabel
    ________javax.swing.JLabel
    ________javax.swing.JRadioButton
    ________javax.swing.JRadioButton
    ________javax.swing.JComboBox
    __________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
    __________javax.swing.CellRendererPane
    ____________org.netbeans.modules.java.hints.spiimpl.refactoring.InspectionRenderer
    ________javax.swing.JComboBox
    __________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
    __________javax.swing.CellRendererPane
    ____________org.netbeans.modules.java.hints.spiimpl.refactoring.ConfigurationRenderer
    ________javax.swing.JComboBox
    __________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
    __________javax.swing.CellRendererPane
    ____________org.netbeans.modules.java.hints.spiimpl.refactoring.InspectAndRefactorPanel$JLabelRenderer
    ________javax.swing.JButton
    ________javax.swing.JButton
    ________javax.swing.JButton
    
    D:\IDE Repository\refactoring.api\src\org\netbeans\modules\refactoring\spi\impl\ParametersPanel.java
*/
    
    public InspectAndTransformOperator() {
        super("Inspect and Transform");
    }
    
    public void setInspect(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 2);
        combo.selectItem(s);
    }
    
    public void setConfiguration(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 1);
        combo.selectItem(s);
    }
    
    public void setSingleInspection(String s){
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem(s);
    }
    
    public void setSingleInspection(int id){
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem(id);
    }
    
    public void selectConfiguration(){
        JRadioButtonOperator button = new JRadioButtonOperator(this, 0);
        button.setSelected(true);
    }
    
    public void selectSingleInspection(){
        JRadioButtonOperator button = new JRadioButtonOperator(this, 1);
        button.setSelected(true);
    }
    
    public void pressInspect(){
        JButtonOperator button = new JButtonOperator(this, "Inspect");
        button.pushNoBlock();
    }
    
    public void pressCancel(){
        JButtonOperator button = new JButtonOperator(this, "Cancel");
        button.pushNoBlock();
    }
    
    public void pressHelp(){
        JButtonOperator button = new JButtonOperator(this, "Help");
        button.pushNoBlock();
    }
    
    public void pressBrowse(){
        JButtonOperator button = new JButtonOperator(this, "Browse...");
        button.pushNoBlock();
    }
    
    public void pressManage(){
        JButtonOperator button = new JButtonOperator(this, "Manage...");
        button.pushNoBlock();
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
