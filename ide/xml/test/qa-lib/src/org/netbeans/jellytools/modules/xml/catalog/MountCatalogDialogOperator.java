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
 * MountOASISCatalogDialogOperator.java
 *
 * Created on 11/13/03 4:20 PM
 */
package org.netbeans.jellytools.modules.xml.catalog;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Mount Catalog" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class MountCatalogDialogOperator extends NbDialogOperator {

    /** Creates new MountOASISCatalogDialogOperator that can handle it.
     */
    public MountCatalogDialogOperator() {
        super("Mount Catalog");
    }

    private JLabelOperator _lblCatalogType;
    private JComboBoxOperator _cboCatalogType;
    public static final String ITEM_NETBEANSCATALOG = "NetBeans Catalog";
    public static final String ITEM_OASISCATALOGRESOLVER = "OASIS Catalog Resolver";
    public static final String ITEM_XMLCATALOG = "XML Catalog";
    private JLabelOperator _lblCatalogURL;
    private JTextFieldOperator _txtCatalogURL;
    private JButtonOperator _btBrowse;
    private JCheckBoxOperator _cbPreferPublicID;
    private JTextAreaOperator _txtJTextArea;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Catalog Type:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCatalogType() {
        if (_lblCatalogType==null) {
            _lblCatalogType = new JLabelOperator(this, "Catalog Type:");
        }
        return _lblCatalogType;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboCatalogType() {
        if (_cboCatalogType==null) {
            _cboCatalogType = new JComboBoxOperator(this);
        }
        return _cboCatalogType;
    }

    /** Tries to find "Catalog URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCatalogURL() {
        if (_lblCatalogURL==null) {
            _lblCatalogURL = new JLabelOperator(this, "Catalog URL:");
        }
        return _lblCatalogURL;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCatalogURL() {
        if (_txtCatalogURL==null) {
            _txtCatalogURL = new JTextFieldOperator(this);
        }
        return _txtCatalogURL;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Prefer Public ID" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPreferPublicID() {
        if (_cbPreferPublicID==null) {
            _cbPreferPublicID = new JCheckBoxOperator(this, "Prefer Public ID");
        }
        return _cbPreferPublicID;
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

    /** returns selected item for cboCatalogType
     * @return String item
     */
    public String getSelectedCatalogType() {
        return cboCatalogType().getSelectedItem().toString();
    }

    /** selects item for cboCatalogType
     * @param item String item
     */
    public void selectCatalogType(String item) {
        cboCatalogType().selectItem(item);
    }

    /** gets text for txtCatalogURL
     * @return String text
     */
    public String getCatalogURL() {
        return txtCatalogURL().getText();
    }

    /** sets text for txtCatalogURL
     * @param text String text
     */
    public void setCatalogURL(String text) {
        txtCatalogURL().setText(text);
    }

    /** types text for txtCatalogURL
     * @param text String text
     */
    public void typeCatalogURL(String text) {
        txtCatalogURL().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPreferPublicID(boolean state) {
        if (cbPreferPublicID().isSelected()!=state) {
            cbPreferPublicID().push();
        }
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

    /** Performs verification of MountOASISCatalogDialogOperator by accessing all its components.
     */
    public void verifyOASIS() {
        lblCatalogType();
        cboCatalogType();
        lblCatalogURL();
        txtCatalogURL();
        btBrowse();
        cbPreferPublicID();
        txtJTextArea();
    }

    /** Performs simple test of MountOASISCatalogDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        MountCatalogDialogOperator mcdo = new MountCatalogDialogOperator();
        mcdo.selectCatalogType(mcdo.ITEM_NETBEANSCATALOG);
        mcdo.verifyNb();
        mcdo.selectCatalogType(mcdo.ITEM_OASISCATALOGRESOLVER);
        mcdo.verifyOASIS();
        mcdo.selectCatalogType(mcdo.ITEM_XMLCATALOG);
        mcdo.verifyXML();
        System.out.println("MountOASISCatalogDialogOperator verification finished.");
    }
    
    /**
     * Performs verification of MountNbCatalogDialogOperator by accessing all its components.
     */
    public void verifyNb() {
        lblCatalogType();
        cboCatalogType();
        txtJTextArea();
    }
    
    /**
     * Performs verification of MountXMLCatalogDialogOperator by accessing all its components.
     */
    public void verifyXML() {
        lblCatalogType();
        cboCatalogType();
        lblCatalogURL();
        txtCatalogURL();
        txtJTextArea();
    }
    
}

