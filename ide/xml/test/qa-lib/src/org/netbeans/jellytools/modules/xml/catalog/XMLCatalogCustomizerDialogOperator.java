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
 * XMLCatalogCustomizerDialogDialogOperator.java
 *
 * Created on 11/13/03 4:25 PM
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
public class XMLCatalogCustomizerDialogOperator extends NbDialogOperator {

    /** Creates new XMLCatalogCustomizerDialogDialogOperator that can handle it.
     */
    public XMLCatalogCustomizerDialogOperator() {
        super("Customizer Dialog");
    }

    private JLabelOperator _lblXMLCatalogURL;
    private JTextFieldOperator _txtXMLCatalogURL;
    private JTextAreaOperator _txtJTextArea;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "XML Catalog URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblXMLCatalogURL() {
        if (_lblXMLCatalogURL==null) {
            _lblXMLCatalogURL = new JLabelOperator(this, "XML Catalog URL:");
        }
        return _lblXMLCatalogURL;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtXMLCatalogURL() {
        if (_txtXMLCatalogURL==null) {
            _txtXMLCatalogURL = new JTextFieldOperator(this);
        }
        return _txtXMLCatalogURL;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtXMLCatalogURL
     * @return String text
     */
    public String getXMLCatalogURL() {
        return txtXMLCatalogURL().getText();
    }

    /** sets text for txtXMLCatalogURL
     * @param text String text
     */
    public void setXMLCatalogURL(String text) {
        txtXMLCatalogURL().setText(text);
    }

    /** types text for txtXMLCatalogURL
     * @param text String text
     */
    public void typeXMLCatalogURL(String text) {
        txtXMLCatalogURL().typeText(text);
    }

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of XMLCatalogCustomizerDialogDialogOperator by accessing all its components.
     */
    public void verify() {
        lblXMLCatalogURL();
        txtXMLCatalogURL();
        txtJTextArea();
    }

    /** Performs simple test of XMLCatalogCustomizerDialogDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new XMLCatalogCustomizerDialogOperator().verify();
        System.out.println("XMLCatalogCustomizerDialogDialogOperator verification finished.");
    }
}

