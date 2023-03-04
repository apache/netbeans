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
package org.netbeans.test.php.operators;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling
 * Project Name and Location step of "New PHP Project" NbDialog.
 *
 * @author mrkam@netbeans.org
 */
public class NewPHPProjectNameLocationStepOperator extends JDialogOperator {

    /** Creates new NewPHPProjectNameLocationStepOperator that can handle it.
     */
    public NewPHPProjectNameLocationStepOperator() {
        super("New PHP Project");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblNameAndLocation;
    private JLabelOperator _lblProjectName;
    private JLabelOperator _lblSourcesFolder;
    private JLabelOperator _lblDefaultEncoding;
    private JTextFieldOperator _txtProjectName;
    private JComboBoxOperator _cboSourcesFolder;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblHint;
    private JComboBoxOperator _cboDefaultEncoding;
    public static final String ENCODING_BIG5 = "Big5";
    public static final String ENCODING_BIG5HKSCS = "Big5-HKSCS";
    public static final String ENCODING_EUCJP = "EUC-JP";
    public static final String ENCODING_EUCKR = "EUC-KR";
    public static final String ENCODING_GB18030 = "GB18030";
    public static final String ENCODING_GB2312 = "GB2312";
    public static final String ENCODING_GBK = "GBK";
    public static final String ENCODING_IBMTHAI = "IBM-Thai";
    public static final String ENCODING_IBM00858 = "IBM00858";
    public static final String ENCODING_IBM01140 = "IBM01140";
    public static final String ENCODING_IBM01141 = "IBM01141";
    public static final String ENCODING_IBM01142 = "IBM01142";
    public static final String ENCODING_IBM01143 = "IBM01143";
    public static final String ENCODING_IBM01144 = "IBM01144";
    public static final String ENCODING_IBM01145 = "IBM01145";
    public static final String ENCODING_IBM01146 = "IBM01146";
    public static final String ENCODING_IBM01147 = "IBM01147";
    public static final String ENCODING_IBM01148 = "IBM01148";
    public static final String ENCODING_IBM01149 = "IBM01149";
    public static final String ENCODING_IBM037 = "IBM037";
    public static final String ENCODING_IBM1026 = "IBM1026";
    public static final String ENCODING_IBM1047 = "IBM1047";
    public static final String ENCODING_IBM273 = "IBM273";
    public static final String ENCODING_IBM277 = "IBM277";
    public static final String ENCODING_IBM278 = "IBM278";
    public static final String ENCODING_IBM280 = "IBM280";
    public static final String ENCODING_IBM284 = "IBM284";
    public static final String ENCODING_IBM285 = "IBM285";
    public static final String ENCODING_IBM297 = "IBM297";
    public static final String ENCODING_IBM420 = "IBM420";
    public static final String ENCODING_IBM424 = "IBM424";
    public static final String ENCODING_IBM437 = "IBM437";
    public static final String ENCODING_IBM500 = "IBM500";
    public static final String ENCODING_IBM775 = "IBM775";
    public static final String ENCODING_IBM850 = "IBM850";
    public static final String ENCODING_IBM852 = "IBM852";
    public static final String ENCODING_IBM855 = "IBM855";
    public static final String ENCODING_IBM857 = "IBM857";
    public static final String ENCODING_IBM860 = "IBM860";
    public static final String ENCODING_IBM861 = "IBM861";
    public static final String ENCODING_IBM862 = "IBM862";
    public static final String ENCODING_IBM863 = "IBM863";
    public static final String ENCODING_IBM864 = "IBM864";
    public static final String ENCODING_IBM865 = "IBM865";
    public static final String ENCODING_IBM866 = "IBM866";
    public static final String ENCODING_IBM868 = "IBM868";
    public static final String ENCODING_IBM869 = "IBM869";
    public static final String ENCODING_IBM870 = "IBM870";
    public static final String ENCODING_IBM871 = "IBM871";
    public static final String ENCODING_IBM918 = "IBM918";
    public static final String ENCODING_ISO2022CN = "ISO-2022-CN";
    public static final String ENCODING_ISO2022JP = "ISO-2022-JP";
    public static final String ENCODING_ISO2022JP2 = "ISO-2022-JP-2";
    public static final String ENCODING_ISO2022KR = "ISO-2022-KR";
    public static final String ENCODING_ISO88591 = "ISO-8859-1";
    public static final String ENCODING_ISO885913 = "ISO-8859-13";
    public static final String ENCODING_ISO885915 = "ISO-8859-15";
    public static final String ENCODING_ISO88592 = "ISO-8859-2";
    public static final String ENCODING_ISO88593 = "ISO-8859-3";
    public static final String ENCODING_ISO88594 = "ISO-8859-4";
    public static final String ENCODING_ISO88595 = "ISO-8859-5";
    public static final String ENCODING_ISO88596 = "ISO-8859-6";
    public static final String ENCODING_ISO88597 = "ISO-8859-7";
    public static final String ENCODING_ISO88598 = "ISO-8859-8";
    public static final String ENCODING_ISO88599 = "ISO-8859-9";
    public static final String ENCODING_JIS_X0201 = "JIS_X0201";
    public static final String ENCODING_JIS_X02121990 = "JIS_X0212-1990";
    public static final String ENCODING_KOI8R = "KOI8-R";
    public static final String ENCODING_KOI8U = "KOI8-U";
    public static final String ENCODING_SHIFT_JIS = "Shift_JIS";
    public static final String ENCODING_TIS620 = "TIS-620";
    public static final String ENCODING_USASCII = "US-ASCII";
    public static final String ENCODING_UTF16 = "UTF-16";
    public static final String ENCODING_UTF16BE = "UTF-16BE";
    public static final String ENCODING_UTF16LE = "UTF-16LE";
    public static final String ENCODING_UTF32 = "UTF-32";
    public static final String ENCODING_UTF32BE = "UTF-32BE";
    public static final String ENCODING_UTF32LE = "UTF-32LE";
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_WINDOWS1250 = "windows-1250";
    public static final String ENCODING_WINDOWS1251 = "windows-1251";
    public static final String ENCODING_WINDOWS1252 = "windows-1252";
    public static final String ENCODING_WINDOWS1253 = "windows-1253";
    public static final String ENCODING_WINDOWS1254 = "windows-1254";
    public static final String ENCODING_WINDOWS1255 = "windows-1255";
    public static final String ENCODING_WINDOWS1256 = "windows-1256";
    public static final String ENCODING_WINDOWS1257 = "windows-1257";
    public static final String ENCODING_WINDOWS1258 = "windows-1258";
    public static final String ENCODING_WINDOWS31J = "windows-31j";
    public static final String ENCODING_XBIG5SOLARIS = "x-Big5-Solaris";
    public static final String ENCODING_XEUCJPLINUX = "x-euc-jp-linux";
    public static final String ENCODING_XEUCTW = "x-EUC-TW";
    public static final String ENCODING_XEUCJPOPEN = "x-eucJP-Open";
    public static final String ENCODING_XIBM1006 = "x-IBM1006";
    public static final String ENCODING_XIBM1025 = "x-IBM1025";
    public static final String ENCODING_XIBM1046 = "x-IBM1046";
    public static final String ENCODING_XIBM1097 = "x-IBM1097";
    public static final String ENCODING_XIBM1098 = "x-IBM1098";
    public static final String ENCODING_XIBM1112 = "x-IBM1112";
    public static final String ENCODING_XIBM1122 = "x-IBM1122";
    public static final String ENCODING_XIBM1123 = "x-IBM1123";
    public static final String ENCODING_XIBM1124 = "x-IBM1124";
    public static final String ENCODING_XIBM1381 = "x-IBM1381";
    public static final String ENCODING_XIBM1383 = "x-IBM1383";
    public static final String ENCODING_XIBM33722 = "x-IBM33722";
    public static final String ENCODING_XIBM737 = "x-IBM737";
    public static final String ENCODING_XIBM834 = "x-IBM834";
    public static final String ENCODING_XIBM856 = "x-IBM856";
    public static final String ENCODING_XIBM874 = "x-IBM874";
    public static final String ENCODING_XIBM875 = "x-IBM875";
    public static final String ENCODING_XIBM921 = "x-IBM921";
    public static final String ENCODING_XIBM922 = "x-IBM922";
    public static final String ENCODING_XIBM930 = "x-IBM930";
    public static final String ENCODING_XIBM933 = "x-IBM933";
    public static final String ENCODING_XIBM935 = "x-IBM935";
    public static final String ENCODING_XIBM937 = "x-IBM937";
    public static final String ENCODING_XIBM939 = "x-IBM939";
    public static final String ENCODING_XIBM942 = "x-IBM942";
    public static final String ENCODING_XIBM942C = "x-IBM942C";
    public static final String ENCODING_XIBM943 = "x-IBM943";
    public static final String ENCODING_XIBM943C = "x-IBM943C";
    public static final String ENCODING_XIBM948 = "x-IBM948";
    public static final String ENCODING_XIBM949 = "x-IBM949";
    public static final String ENCODING_XIBM949C = "x-IBM949C";
    public static final String ENCODING_XIBM950 = "x-IBM950";
    public static final String ENCODING_XIBM964 = "x-IBM964";
    public static final String ENCODING_XIBM970 = "x-IBM970";
    public static final String ENCODING_XISCII91 = "x-ISCII91";
    public static final String ENCODING_XISO2022CNCNS = "x-ISO-2022-CN-CNS";
    public static final String ENCODING_XISO2022CNGB = "x-ISO-2022-CN-GB";
    public static final String ENCODING_XISO885911 = "x-iso-8859-11";
    public static final String ENCODING_XJIS0208 = "x-JIS0208";
    public static final String ENCODING_XJISAUTODETECT = "x-JISAutoDetect";
    public static final String ENCODING_XJOHAB = "x-Johab";
    public static final String ENCODING_XMACARABIC = "x-MacArabic";
    public static final String ENCODING_XMACCENTRALEUROPE = "x-MacCentralEurope";
    public static final String ENCODING_XMACCROATIAN = "x-MacCroatian";
    public static final String ENCODING_XMACCYRILLIC = "x-MacCyrillic";
    public static final String ENCODING_XMACDINGBAT = "x-MacDingbat";
    public static final String ENCODING_XMACGREEK = "x-MacGreek";
    public static final String ENCODING_XMACHEBREW = "x-MacHebrew";
    public static final String ENCODING_XMACICELAND = "x-MacIceland";
    public static final String ENCODING_XMACROMAN = "x-MacRoman";
    public static final String ENCODING_XMACROMANIA = "x-MacRomania";
    public static final String ENCODING_XMACSYMBOL = "x-MacSymbol";
    public static final String ENCODING_XMACTHAI = "x-MacThai";
    public static final String ENCODING_XMACTURKISH = "x-MacTurkish";
    public static final String ENCODING_XMACUKRAINE = "x-MacUkraine";
    public static final String ENCODING_XMS932_0213 = "x-MS932_0213";
    public static final String ENCODING_XMS950HKSCS = "x-MS950-HKSCS";
    public static final String ENCODING_XMSWIN936 = "x-mswin-936";
    public static final String ENCODING_XPCK = "x-PCK";
    public static final String ENCODING_XSJIS_0213 = "x-SJIS_0213";
    public static final String ENCODING_XUTF16LEBOM = "x-UTF-16LE-BOM";
    public static final String ENCODING_XUTF32BEBOM = "X-UTF-32BE-BOM";
    public static final String ENCODING_XUTF32LEBOM = "X-UTF-32LE-BOM";
    public static final String ENCODING_XWINDOWS50220 = "x-windows-50220";
    public static final String ENCODING_XWINDOWS50221 = "x-windows-50221";
    public static final String ENCODING_XWINDOWS874 = "x-windows-874";
    public static final String ENCODING_XWINDOWS949 = "x-windows-949";
    public static final String ENCODING_XWINDOWS950 = "x-windows-950";
    public static final String ENCODING_XWINDOWSISO2022JP = "x-windows-iso2022jp";
    private JTextAreaOperator _txtHint;
    private JCheckBoxOperator _cbPutNetBeansMetadataIntoASeparateDirectory;
    private JLabelOperator _lblMetadataFolder;
    private JTextFieldOperator _txtMetadataFolder;
    private JButtonOperator _btBrowse2;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            // "Steps"
            _lblSteps = new JLabelOperator(this, 
                    Bundle.getStringTrimmed("org.openide.Bundle", "CTL_ContentName"));
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Name and Location" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNameAndLocation() {
        if (_lblNameAndLocation==null) {
            // "Name and Location"
            _lblNameAndLocation = new JLabelOperator(this,
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_ProjectNameLocation"));
        }
        return _lblNameAndLocation;
    }

    /** Tries to find "Project Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if (_lblProjectName==null) {
            // "Project Name:"
            _lblProjectName = new JLabelOperator(this,
                    Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_PrjChooser_ProjectName_Label"));
        }
        return _lblProjectName;
    }

    /** Tries to find "Sources Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcesFolder() {
        if (_lblSourcesFolder==null) {
            // "Sources Folder:"
            _lblSourcesFolder = new JLabelOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_Sources"));
        }
        return _lblSourcesFolder;
    }

    /** Tries to find "Default Encoding:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDefaultEncoding() {
        if (_lblDefaultEncoding==null) {
            // "Default Encoding:"
            _lblDefaultEncoding = new JLabelOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_Encoding"));
        }
        return _lblDefaultEncoding;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProjectName() {
        if (_txtProjectName==null) {
            _txtProjectName = new JTextFieldOperator(this);
        }
        return _txtProjectName;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourcesFolder() {
        if (_cboSourcesFolder==null) {
            _cboSourcesFolder = new JComboBoxOperator(this, 1);
        }
        return _cboSourcesFolder;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseSourceFolder() {
        if (_btBrowse==null) {
            // "Browse..."
            _btBrowse = new JButtonOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.options.Bundle", "LBL_Browse"));
        }
        return _btBrowse;
    }

    /** Tries to find 5th JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHint() {
        if (_lblHint==null) {
            _lblHint = new JLabelOperator(this, 4);
        }
        return _lblHint;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDefaultEncoding() {
        if (_cboDefaultEncoding==null) {
            _cboDefaultEncoding = new JComboBoxOperator(this, 2);
        }
        return _cboDefaultEncoding;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtHint() {
        if (_txtHint==null) {
            _txtHint = new JTextAreaOperator(this);
        }
        return _txtHint;
    }

    /** Tries to find "Put NetBeans metadata into a separate directory" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPutNetBeansMetadataIntoASeparateDirectory() {
        if (_cbPutNetBeansMetadataIntoASeparateDirectory==null) {
            // "Put NetBeans metadata into a separate directory"
            _cbPutNetBeansMetadataIntoASeparateDirectory = new JCheckBoxOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_SeparateProjectFolder"));
        }
        return _cbPutNetBeansMetadataIntoASeparateDirectory;
    }

    /** Tries to find "Metadata Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMetadataFolder() {
        if (_lblMetadataFolder==null) {
            // "Metadata Folder:"
            _lblMetadataFolder = new JLabelOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_MetadataFolder"));
        }
        return _lblMetadataFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtMetadataFolder() {
        if (_txtMetadataFolder==null) {
            _txtMetadataFolder = new JTextFieldOperator(this, 2);
        }
        return _txtMetadataFolder;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseMetadataFolder() {
        if (_btBrowse2==null) {
            // "Browse..."
            _btBrowse2 = new JButtonOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.options.Bundle", "LBL_Browse"), 1);
        }
        return _btBrowse2;
    }

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 6);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }

    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtProjectName
     * @return String text
     */
    public String getProjectName() {
        return txtProjectName().getText();
    }

    /** sets text for txtProjectName
     * @param text String text
     */
    public void setProjectName(String text) {
        txtProjectName().setText(text);
    }

    /** types text for txtProjectName
     * @param text String text
     */
    public void typeProjectName(String text) {
        txtProjectName().selectText(0, txtProjectName().getText().length());
        txtProjectName().typeText(text);
    }

    /** returns selected item for cboSourcesFolder
     * @return String item
     */
    public String getSelectedSourcesFolder() {
        String folderDump = cboSourcesFolder().getSelectedItem().toString();
        return folderDump.substring(folderDump.indexOf("srcRoot: ") 
                + "srcRoot: ".length(), folderDump.indexOf(", hint:"));
    }

    /** selects item for cboSourcesFolder
     * @param item String item
     */
    public void selectSourcesFolder(String item) {
        cboSourcesFolder().selectItem(item);
    }

    /** types text for cboSourcesFolder
     * @param text String text
     */
    public void typeSourcesFolder(String text) {
        cboSourcesFolder().getTextField().setText("");
        new EventTool().waitNoEvent(1000);
        cboSourcesFolder().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browseSourceFolder() {
        btBrowseSourceFolder().push();
    }

    /** returns selected item for cboDefaultEncoding
     * @return String item
     */
    public String getSelectedDefaultEncoding() {
        return cboDefaultEncoding().getSelectedItem().toString();
    }

    /** selects item for cboDefaultEncoding
     * @param item String item
     */
    public void selectDefaultEncoding(String item) {
        cboDefaultEncoding().selectItem(item);
    }

    /** gets text for txtHint
     * @return String text
     */
    public String getHint() {
        return txtHint().getText();
    }

    /** sets text for txtHint
     * @param text String text
     */
    public void setHint(String text) {
        txtHint().setText(text);
    }

    /** types text for txtHint
     * @param text String text
     */
    public void typeHint(String text) {
        txtHint().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPutNetBeansMetadataIntoASeparateDirectory(boolean state) {
        if (cbPutNetBeansMetadataIntoASeparateDirectory().isSelected()!=state) {
            cbPutNetBeansMetadataIntoASeparateDirectory().push();
        }
    }

    /** gets text for txtMetadataFolder
     * @return String text
     */
    public String getMetadataFolder() {
        return txtMetadataFolder().getText();
    }

    /** sets text for txtMetadataFolder
     * @param text String text
     */
    public void setMetadataFolder(String text) {
        txtMetadataFolder().setText(text);
    }

    /** types text for txtMetadataFolder
     * @param text String text
     */
    public void typeMetadataFolder(String text) {
        txtMetadataFolder().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browseMetadataFolder() {
        btBrowseMetadataFolder().push();
    }

    /** clicks on "< Back" JButton
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    public void finish() {
        btFinish().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewPHPProjectNameLocationStepOperator by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lblNameAndLocation();
        lblProjectName();
        lblSourcesFolder();
        lblDefaultEncoding();
        txtProjectName();
        cboSourcesFolder();
        btBrowseSourceFolder();
        lblHint();
        cboDefaultEncoding();
        txtHint();
        cbPutNetBeansMetadataIntoASeparateDirectory();
        lblMetadataFolder();
        txtMetadataFolder();
        btBrowseMetadataFolder();
        lblWizardDescriptor$FixedHeightLabel();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }

    /** Performs simple test of NewPHPProjectNameLocationStepOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NewPHPProjectNameLocationStepOperator().verify();
        System.out.println("NewPHPProject verification finished.");
    }
}

