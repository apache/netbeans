/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.css.test.operator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
/**
 *
 * @author Jindrich Sedek
 */
public class StyleRuleEditorOperator extends NbDialogOperator{
    private static final String radioClass = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "CLASS_NAME_LBL");
    private static final String radioHtmlElement = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "HTML_ELELEMT");
    private static final String radioElementID = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "ELEMENT_ID_LBL");
    private static final String addRule = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "ADD_RULE_LBL");
    private static final String moveUp = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "MOVE_RULE_UP_LBL");
    private static final String moveDown = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "MOVE_RULE_DOWN_LBL");

    public StyleRuleEditorOperator() {
        super(Bundle.getString("org.netbeans.modules.css.actions.Bundle", "STYLE_RULE_EDITOR_TITLE"));
    }
    
    public void up(String item){
        new JListOperator(this).selectItem(item);
        new JButtonOperator(this,moveUp).push();
    }

    public void down(String item){
        new JListOperator(this).selectItem(item);
        new JButtonOperator(this, moveDown).push();
    }
    
    public void selectClass(){
        new JRadioButtonOperator(this, radioClass).push();
    }
    
    public void selectClass(String elementName, String className){
        selectClass();
        setComboBox(elementName, 1);
        new JTextFieldOperator(this, 1).setText(className);
    }
    
    public void selectHtmlElement(){
        new JRadioButtonOperator(this, radioHtmlElement).push();
    }
    
    public void selectHtmlElement(String elementName){
        selectHtmlElement();
        setComboBox(elementName, 0);
    }
    
    private void setComboBox(String elementName, int comboBox){
        JComboBoxOperator operator = new JComboBoxOperator(this, comboBox);
        int i = 0;
        while (!operator.isEnabled()&&(i<10)){
            try{
                Thread.sleep(1000);
            }catch(InterruptedException interupt){
                throw new AssertionError(interupt);
            }
            i++;
        }
        if (!operator.isEnabled()) throw new AssertionError("COMBO BOX IS NOT ENABLED IN 10s");
        operator.selectItem(elementName);
    }
    
    public void selectElementID(){
        new JRadioButtonOperator(this, radioElementID).push();
    }
    
    public void selectElementID(String str){
        selectElementID();
        new JTextFieldOperator(this, 2).setText(str);
    }
    
    public void addRule(){
        new JButtonOperator(this, addRule).push();
    }
    
    public String getPreview(){
        return new JTextFieldOperator(this, 0).getText();
    }
    
}
