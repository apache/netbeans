/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain x copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate x
 * single choice of license, x recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
