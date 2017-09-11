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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * NbCatalogCustomizerDialogDialogOperator.java
 *
 * Created on 11/13/03 4:22 PM
 */
package org.netbeans.jellytools.modules.xml.catalog;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Customizer Dialog" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class NbCatalogCustomizerDialogOperator extends NbDialogOperator {

    /** Creates new NbCatalogCustomizerDialogDialogOperator that can handle it.
     */
    public NbCatalogCustomizerDialogOperator() {
        super("Customizer Dialog");
    }

    private JTextAreaOperator _txtSystemCatalogCustomizer$1;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null SystemCatalogCustomizer$1 in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtSystemCatalogCustomizer$1() {
        if (_txtSystemCatalogCustomizer$1==null) {
            _txtSystemCatalogCustomizer$1 = new JTextAreaOperator(this);
        }
        return _txtSystemCatalogCustomizer$1;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtSystemCatalogCustomizer$1
     * @return String text
     */
    public String getSystemCatalogCustomizer$1() {
        return txtSystemCatalogCustomizer$1().getText();
    }

    /** sets text for txtSystemCatalogCustomizer$1
     * @param text String text
     */
    public void setSystemCatalogCustomizer$1(String text) {
        txtSystemCatalogCustomizer$1().setText(text);
    }

    /** types text for txtSystemCatalogCustomizer$1
     * @param text String text
     */
    public void typeSystemCatalogCustomizer$1(String text) {
        txtSystemCatalogCustomizer$1().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NbCatalogCustomizerDialogDialogOperator by accessing all its components.
     */
    public void verify() {
        txtSystemCatalogCustomizer$1();
    }

    /** Performs simple test of NbCatalogCustomizerDialogDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NbCatalogCustomizerDialogOperator().verify();
        System.out.println("NbCatalogCustomizerDialogDialogOperator verification finished.");
    }
}

