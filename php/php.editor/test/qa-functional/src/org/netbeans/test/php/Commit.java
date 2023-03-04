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
package org.netbeans.test.php;

import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JListOperator;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.openide.util.NbBundle;

/* 
 * Commit validation test for PHP. Some code just copied from other tests
 * to avoid any affects from current changes in working test set.
 * 
 * @author michaelnazarov@netbeans.org
 */
public class Commit extends GeneralPHP {

    static final String PROJECT_NAME = "LoginSample";
    private static final String TEST_PHP_NAME_1 = "PhpProject_commit_0001";
    private static final String INDEX_PHP_INITIAL_CONTENT
            = "<!DOCTYPEhtml><!--Tochangethislicenseheader,chooseLicenseHeadersinProjectProperties.Tochangethistemplatefile,chooseTools|Templatesandopenthetemplateintheeditor.--><html><head><metacharset=\"UTF-8\"><title></title></head><body><?php//putyourcodehere?></body></html>";
    private static final String EMPTY_PHP_INITIAL_CONTENT
            = "<?php/**Tochangethislicenseheader,chooseLicenseHeadersinProjectProperties.*Tochangethistemplatefile,chooseTools|Templates*andopenthetemplateintheeditor.*/?>";
    private static final String CLASS_PHP_INITIAL_CONTENT
            = "<?php/**Tochangethislicenseheader,chooseLicenseHeadersinProjectProperties.*Tochangethistemplatefile,chooseTools|Templates*andopenthetemplateintheeditor.*//***DescriptionofnewPHPClass**@author" + System.getProperty("user.name") + "*/classnewPHPClass{//putyourcodehere}";
    private static final int COMPLETION_LIST_INCLASS = 22;
    private static boolean bUnzipped = false;

    public Commit(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return createModuleTest(Commit.class,
                "CreatePHPApplication",
                "ManipulateIndexPHP",
                "CreateEmptyPHP",
                "ManipulateEmptyPHP",
                "CreateTemplatePHP",
                "ManipulateTemplatePHP",
                //"OpenStandalonePHP",
                //"ManipulateStandalonePHP",
                //"CreateCustomPHPApplication",
                "CreatePHPWithExistingSources",
                "ManipulatePHPWithExistingSources");
    }

    @Override
    public void setUp() {
        System.out.println("#### " + getName() + " ####");
        if (!bUnzipped) {
            try {
                String sBase = getDataDir().getPath() + File.separator;
                //System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data" + File.separator;
                System.out.println("Unzipping projects.zip into \"" + sBase + "\"...");
                // Extract zip data
                ZipFile zf = new ZipFile(sBase + "projects.zip");
                Enumeration<? extends ZipEntry> ent = zf.entries();
                while (ent.hasMoreElements()) {
                    ZipEntry e = ent.nextElement();
                    String name = e.getName();
                    if (e.isDirectory()) {
                        (new File(sBase + name)).mkdirs();
                    } else {
                        InputStream is = zf.getInputStream(e);
                        //File f = new File( name );
                        //System.out.println( "-->" + f.getPath( ) );
                        OutputStream os = new FileOutputStream(sBase + name);
                        int r;
                        byte[] b = new byte[1024];
                        while (-1 != (r = is.read(b))) {
                            os.write(b, 0, r);
                        }
                        is.close();
                        os.flush();
                        os.close();
                    }
                }
                zf.close();

                // Open project
                //openDataProjects( PROJECT_NAME );

                bUnzipped = true;
            } catch (IOException ex) {
                System.out.println("ERROR: Unzipping projects.zip failed: " + ex.getMessage());
            }
        }
    }

    public void CreatePHPApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME_1);
        endTest();
    }

    protected void EnsureEmptyLine(EditorOperator eoPHP) {
        CheckResultRegex(eoPHP, "[ \t\r\n]*");
    }

    protected void CompletePairCheck(EditorOperator eoPHP, String sCode, String sCheck) {
        TypeCodeCheckResult(eoPHP, sCode, sCheck);
        Backit(eoPHP, sCode.length());
        EnsureEmptyLine(eoPHP);
    }

    protected String CreatePair(String sCode) {
        String sSuffix = "";
        boolean bQuote = true;
        for (int i = 0; i < sCode.length(); i++) {
            switch (sCode.charAt(i)) {
                case '[':
                    if (bQuote) {
                        sSuffix = "]" + sSuffix;
                    }
                    break;
                case '(':
                    if (bQuote) {
                        sSuffix = ")" + sSuffix;
                    }
                    break;
                case ']':
                    if (bQuote) {
                        sSuffix = sSuffix.substring(1);
                    }
                    break;
                case ')':
                    if (bQuote) {
                        sSuffix = sSuffix.substring(1);
                    }
                    break;
                case '"':
                    if (bQuote) {
                        sSuffix = "\"" + sSuffix;
                    } else {
                        sSuffix = sSuffix.substring(1);
                    }
                    bQuote = !bQuote;
                    break;
            }
        }
        return sCode + sSuffix;
    }

    protected int GetNumber(EditorOperator eo, Object[] oo, String sType) {
        int iResult = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationType(o).equals(sType)) {
                iResult++;
            }
        }
        return iResult;
    }

    protected int GetErrorNumber(EditorOperator eo, Object[] oo) {
        return GetNumber(eo, oo, "org-netbeans-spi-editor-hints-parser_annotation_err");
    }

    protected int GetWarningNumber(EditorOperator eo, Object[] oo) {
        return GetNumber(eo, oo, "org-netbeans-spi-editor-hints-parser_annotation_verifier");
    }

    protected void TestPHPFile(
            String sProjectName,
            String sFileName,
            String sInitialContent,
            boolean bInitialWait,
            String sCodeLocator,
            boolean bInclass,
            boolean bFormat,
            int iAnnotations,
            int iWarningsExpected) {
        waitScanFinished();
        // Check file opened
        EditorOperator eoPHP = new EditorOperator(sFileName);

        // Check file content
        if (null != sInitialContent) {
            String sText = eoPHP.getText().replaceAll("[ \t\r\n]", "");
            if (!sText.equals(sInitialContent)) {
                fail("Invalid initial file content. Found: \"" + sText + "\". Expected: \"" + sInitialContent + "\"");
            }
        }

        // Work with content

        // Locate
        eoPHP.setCaretPosition(sCodeLocator, false);
        // Insert new line
        eoPHP.insert("\n");
        if (bInitialWait) {
            Sleep(20000);
        }
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(500);

        // Check code completion list
        try {
            CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }
            // Magic CC number for complete list
            if ((bInclass ? COMPLETION_LIST_INCLASS : COMPLETION_LIST_THRESHOLD)
                    > completionInfo.listItems.size()) {
                fail("CC list looks to small, there are only: " + completionInfo.listItems.size() + " items in.");
            }

            if (!bInclass) {
                // Check some completions
                String[] asCompletions = {
                    "$GLOBALS",
                    "LC_MONETARY",
                    "ibase_wait_event",
                    "mysqli_error",
                    "openssl_pkcs12_export_to_file",
                    "str_word_count"
                };
                CheckCompletionItems(completionInfo.listItself, asCompletions);
                //jCompl.clickOnItem( "$GLOBALS" );
                //Sleep( 500 );
                //CheckResult( eoPHP, "$GLOBALS" );

                CompletionJListOperator.hideAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        new EventTool().waitNoEvent(500);

        // Brackets
        // Predefined
        String[] asCheckers = {
            "[(\"",
            "[(\"\")]",
            "name(",
            "name()",
            "name[",
            "name[]",
            "hello(a[\"1"
        };

        for (String sChecker : asCheckers) {
            String[] asChecker = sChecker.split("[|]");
            CompletePairCheck(
                    eoPHP,
                    asChecker[ 0],
                    (1 == asChecker.length) ? CreatePair(asChecker[ 0]) : asChecker[ 1]);
        }

        // Check something random
        // Yes I know about StringBuffer :)
        String sRandom = "";
        String sCharset = "abc123[[[[[((((((\"";
        char previousChar = ' ';
        for (int i = 0; i < 50; i++) {
            char nextChar = sCharset.charAt((int) (Math.random() * sCharset.length()));
            // ignore two double quotes in row
            if (nextChar != '\"' || previousChar != nextChar) {
                sRandom += nextChar;
            }
            previousChar = nextChar;
        }

        // Okey, this is hack and should be removed later
        bRandomCheck = true;
        CompletePairCheck(eoPHP, sRandom, CreatePair(sRandom));
        bRandomCheck = false;

        // Formatting
    /*
        if( bFormat ) {
        TypeCode( eoPHP, "class a{function aa(){return;}}" );
        ClickForTextPopup( eoPHP, "format" );
        // Check return here
        // CheckResult( eoPHP, "some staff"
        // TODO
        }
         */

        // Completion
        if (bInclass) {
            /** bug  181710 */
            // start constructor
//      TypeCode( eoPHP, "__con" );
//      Sleep( 1500 );
//      eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
//      Sleep( 1500 );
//      CheckFlex( eoPHP, "__construct(){\n;\n}", true );
        } else {
            // start class declaration
            TypeCode(eoPHP, "class a ext");
            new EventTool().waitNoEvent(100);
            eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
            new EventTool().waitNoEvent(100);
            CheckResult(eoPHP, "class a extends");
        }

        // Insertion
        if (!bInclass) {
            // Complete class declaration before code insertion
            TypeCode(eoPHP, "\n{\n");
        }
        TypeCode(eoPHP, "public $a, $b;\nprotected $c, $d;\nprivate $e, $f;\n");

        // Check existing notes
        int iErrorChecks = 0;
        boolean bRecheck = true;
        while (bRecheck && iErrorChecks++ < 5) {
            new EventTool().waitNoEvent(300);
            Object[] oo = eoPHP.getAnnotations();
            int iErrors = GetErrorNumber(eoPHP, oo);
            int iWarnings = GetWarningNumber(eoPHP, oo);
            if (iAnnotations == iErrors && iWarningsExpected == iWarnings) {
                bRecheck = false;
            } else {
                System.out.println("---Error check failed (" + iErrorChecks + ")");
                for (Object o : oo) {
                    System.out.println("***" + EditorOperator.getAnnotationType(o) + " : " + EditorOperator.getAnnotationShortDescription(o));
                }
                continue;
            }
            assertEquals("Invalid number of detected errors.", iAnnotations, iErrors);
            assertEquals("Invalid number of detected warnings.", iWarningsExpected, iWarnings);
        }

        // Insert constructor
        if (getPlatform() != 4096) {
            eoPHP.pressKey(KeyEvent.VK_INSERT, InputEvent.ALT_MASK);
        } else {
            eoPHP.pressKey(KeyEvent.VK_I, InputEvent.CTRL_MASK);
        }

        JDialogOperator jdInsetter = new JDialogOperator();
        JListOperator jlList = new JListOperator(jdInsetter);

        ClickListItemNoBlock(jlList, 0, 1);

        JDialogOperator jdGenerator = new JDialogOperator(
                NbBundle.getBundle("org.netbeans.modules.php.editor.codegen.Bundle").getString("LBL_TITLE_CONSTRUCTOR") //"Generate Constructor"
                );

        // Select all but $c
        JTreeOperator jtTree = new JTreeOperator(jdGenerator, 0);
        jtTree.clickOnPath(jtTree.findPath("a"));
        jtTree.clickOnPath(jtTree.findPath("d"));
        jtTree.clickOnPath(jtTree.findPath("e"));
        JCheckBoxOperator publicCheckBox = new JCheckBoxOperator(jdGenerator, "Use Public Modifier");
        // unselect public check box
        if (publicCheckBox.isSelected()) {
            publicCheckBox.push();
        }

        JButtonOperator jbOk = new JButtonOperator(jdGenerator, "OK");
        jbOk.pushNoBlock();
        jdGenerator.waitClosed();

        // Check result
        CheckFlex(
                eoPHP,
                "function __construct($a,$d,$e){$this->a=$a;$this->d=$d;$this->e=$e;}",
                true);

        boolean b = true;
        // Insert get
        if (getPlatform() != 4096) {
            eoPHP.pressKey(KeyEvent.VK_INSERT, InputEvent.ALT_MASK);
        } else {
            eoPHP.pressKey(KeyEvent.VK_I, InputEvent.CTRL_MASK);
        }

        jdInsetter = new JDialogOperator();
        jlList = new JListOperator(jdInsetter);

        ClickListItemNoBlock(jlList, 3, 1);

        jdGenerator = new JDialogOperator(
                NbBundle.getBundle("org.netbeans.modules.php.editor.codegen.Bundle").getString("LBL_TITLE_GETTERS_AND_SETTERS") //"Generate Getters and Setters"
                );

        // Select all but $c
        jtTree = new JTreeOperator(jdGenerator, 0);
        jtTree.clickOnPath(jtTree.findPath("b"));
        jtTree.clickOnPath(jtTree.findPath("c"));
        jtTree.clickOnPath(jtTree.findPath("f"));
        publicCheckBox = new JCheckBoxOperator(jdGenerator, "Use Public Modifier");
        // select public check box
        if (!publicCheckBox.isSelected()) {
            publicCheckBox.push();
        }

        jbOk = new JButtonOperator(jdGenerator, "OK");
        jbOk.pushNoBlock();
        jdGenerator.waitClosed();

        // Check result
        CheckFlex(
                eoPHP,
                "public function getB(){return $this->b;}public function getC(){return $this->c;}public function getF(){return $this->f;}public function setB($b){$this->b=$b;}public function setC($c){$this->c=$c;}public function setF($f){$this->f=$f;}",
                true);

        // Close to prevent affect on next tests
        eoPHP.close(false);
    }

    public void ManipulateIndexPHP() {
        startTest();
        TestPHPFile(
                TEST_PHP_NAME_1,
                "index.php",
                INDEX_PHP_INITIAL_CONTENT,
                false,
                "// put your code here",
                false,
                true,
                1, //issue 1683650, for 65: ( 5 <= ++iErrorChecks )
                0);
        endTest();
    }

    public void CreateEmptyPHP() {
        startTest();
        EditorOperator eo = CreatePHPFile(TEST_PHP_NAME_1, "PHP File", null);
        // #209028 - need to close namespace
        eo.replace(" */", " */\n\n?>");
        endTest();
    }

    public void ManipulateEmptyPHP() {
        startTest();
        TestPHPFile(
                TEST_PHP_NAME_1,
                "EmptyPHP.php",
                EMPTY_PHP_INITIAL_CONTENT,
                false,
                "*/",
                false,
                true,
                1, //issue 1683650, for 65: ( 5 <= ++iErrorChecks )
                0);
        endTest();
    }

    public void CreateTemplatePHP() {
        startTest();
        CreatePHPFile(TEST_PHP_NAME_1, "PHP Class", null);
        endTest();
    }

    public void ManipulateTemplatePHP() {
        startTest();
        TestPHPFile(
                TEST_PHP_NAME_1,
                "PHPClass.php",
                CLASS_PHP_INITIAL_CONTENT,
                false,
                "//put your code here",
                true,
                false,
                0,
                0);
        endTest();
    }

    public void OpenStandalonePHP() {
        startTest();
        endTest();
    }

    public void ManipulateStandalonePHP() {
        startTest();
        endTest();
    }

    public void CretaeCustomPHPApplication() {
        startTest();
        endTest();
    }

    public void CreatePHPWithExistingSources() {
        startTest();
        //setUp( );
        NewProjectWizardOperator.invoke().cancel();

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(
                NbBundle.getBundle("org.netbeans.modules.php.project.ui.wizards.Bundle").getString("Templates/Project/PHP/existingPHPProject.php") //"PHP Application with Existing Sources"
                );

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project with Existing Sources");

        JButtonOperator jbBrowse = new JButtonOperator(jdNew, "Browse...", 1);
        jbBrowse.pushNoBlock();

        JDialogOperator jdBrowse = new JDialogOperator(
                NbBundle.getBundle("org.netbeans.modules.php.project.ui.wizards.Bundle").getString("LBL_SelectProjectFolder") //"Select Project Folder"
                );

        JTextComponentOperator jtLocation = new JTextComponentOperator(jdBrowse, 0);
        String sProjectPath = getDataDir().getPath() + File.separator + "LoginSample";
        jtLocation.setText(sProjectPath);

        JFileChooserOperator jfcOpen = new JFileChooserOperator();
        jfcOpen.approve();

        //JButtonOperator jbOpen = new JButtonOperator( jdBrowse, "Open" );
        //jbOpen.push( );

        jdBrowse.waitClosed();

        opNewProjectWizard.next();

        // Set index file
        jdNew = new JDialogOperator("New PHP Project");
        jbBrowse = new JButtonOperator(jdNew, "Browse...", 1);
        jbBrowse.push();
        jdBrowse = new JDialogOperator("Browse Files");
        JTreeOperator jtBrowse = new JTreeOperator(jdBrowse, 0);
        jtBrowse.selectPath(jtBrowse.findPath("LoginSample|LoginForm.php"));
        JButtonOperator jbSelect = new JButtonOperator(jdBrowse, "Select File");
        jbSelect.push();
        jdBrowse.waitClosed();

        opNewProjectWizard.finish();

        // Check created

        endTest();
    }

    public void ManipulatePHPWithExistingSources() {
        startTest();
        // Insert short php tags
        EditorOperator eoPHP = new EditorOperator("LoginForm.php");
        eoPHP.setCaretPosition("<p>&nbsp;</p>", false);
        eoPHP.insert("\n<?php\n\n?>");

        // test as usual
        TestPHPFile(
                "LoginSample",
                "LoginForm.php",
                null,
                false,
                "<?php",
                false,
                false,
                1,//issue 1683650, for 65: ( 5 <= ++iErrorChecks )
                0);
        endTest();
    }
}
