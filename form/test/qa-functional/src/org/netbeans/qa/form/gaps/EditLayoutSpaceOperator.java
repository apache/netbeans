/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
