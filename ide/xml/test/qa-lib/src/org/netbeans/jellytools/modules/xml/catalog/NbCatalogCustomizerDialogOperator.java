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

