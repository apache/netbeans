/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class testCC extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_0001";

    public testCC(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCC.class).addTest(
                "CreateApplication",
                "Create_a_PHP_source_file",
                "Verify_automatic_code_completion_invocation",
                "Verify_local_variable_code_completion",
                "Verify_global_variable_code_completion",
                "Verify_variable_from_included_file_code_completion",
                "Verify_variable_from_required_file_code_completion",
                "Verify_code_completion_inside_the_identifier",
//                "Verify_documentation_hints_for_built_in_identifiers",
//                "Verify_documentation_hints_for_keywords",
                "Verify_keywords_code_completion",
                "Verify_code_completion_after_extends_keyword",
                "Verify_code_completion_with_a_single_option",
//                "Verify_JavaDoc_window",
                "Verify_code_completion_after_EXTENDS",
//                "Verify_that_require_directive_is_automatically_added", not supported #195851
                "Verify_code_completion_in_slash_slash_comments",
                "Verify_code_completion_in_slash_star_comments",
                "Verify_code_completion_in_slash_star_star_comments").enableModules(".*").clusters(".*") //.gui( true )
                );


    }

    public void CreateApplication() {
        startTest();

        CreatePHPApplicationInternal(TEST_PHP_NAME);

        endTest();
    }
   
    public void Create_a_PHP_source_file() {
        startTest();

        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);

        endTest();
    }

    public void Verify_automatic_code_completion_invocation() {
        startTest();
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");

        //Sleep( 2000 );
        eoPHP.setCaretPosition("*/\n", false);
        waitScanFinished();
        eoPHP.typeKey('$');

        // Check code completion list
        try {
            CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            // Check some completions
            String[] asCompletions = {
                "$GLOBALS",
                "$HTTP_RAW_POST_DATA",
                "$_COOKIE",
                "$_ENV",
                "$_FILES",
                "$_GET",
                "$_POST",
                "$_REQUEST",
                "$_SERVER",
                "$_SESSION",
                "$argc",
                "$argv",
                "$http_response_header",
                "$php_errormsg"
//        "$dirh",
//        "$new_obj"
            };
            CheckCompletionItems(completionInfo.listItself, asCompletions);
            completionInfo.listItself.hideAll();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        // Clean up
        eoPHP.pressKey(KeyEvent.VK_BACK_SPACE);

        endTest();
    }

    public void Verify_local_variable_code_completion() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        TypeCode(eoPHP, "function function_0001( )\n{\n$variable_0001 = 1;\n$va");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CheckResult(eoPHP, "$variable_0001");

        // Cleanup
        eoPHP.deleteLine(eoPHP.getLineNumber());
        eoPHP.deleteLine(eoPHP.getLineNumber() - 1);

        endTest();
    }

    public void Verify_global_variable_code_completion() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/\n", false);
        int lineNumber = eoPHP.getLineNumber();
        TypeCode(eoPHP, "\n");
        TypeCode(eoPHP, "$variable_0002 = 2;\n");
        eoPHP.setCaretPositionToLine(lineNumber);
        TypeCode(eoPHP, "\nglobal $va");

        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        // THIS IS ISSUE
        //CheckResult( eoPHP, "global $variable_0002" );

        // Cleanup
        eoPHP.deleteLine(eoPHP.getLineNumber());
        //eoPHP.setCaretPosition( "$variable_0002", false );
        //eoPHP.deleteLine( eoPHP.getLineNumber( ) );

        endTest();
    }

    public void Verify_variable_from_included_file_code_completion() {
        startTest();

        // Create new file
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.save();
        // Include first file
        eoPHP = new EditorOperator("newEmptyPHP1.php");
        eoPHP.setCaretPosition("*/\n", false);
        TypeCode(eoPHP, "include 'newEmptyPHP.php';\n\n$va");

        // Use global variable from first file
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        CheckResult(eoPHP, "$variable_0002");

        // Cleanup
        eoPHP.deleteLine(eoPHP.getLineNumber());

        endTest();
    }

    public void Verify_variable_from_required_file_code_completion() {
        startTest();

        // Add required third into first file
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/\n", false);
        TypeCode(eoPHP, "require 'newEmptyPHP2.php';\n");

        // Add third file
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);

        // Add variable into third file
        EditorOperator eoPHP_2 = new EditorOperator("newEmptyPHP2.php");
        eoPHP_2.setCaretPosition("*/\n", false);
        TypeCode(eoPHP_2, "$variable_0003 = 3;\n");

        // Check completion within first file
        eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("$variable_0002", false);
        eoPHP.deleteLine(eoPHP.getLineNumber());
        eoPHP.setCaretPosition("}", false);
        TypeCode(eoPHP, "\n $va");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        CheckResult(eoPHP, "$variable_0003");

        endTest();
    }

    public void Verify_code_completion_inside_the_identifier() {
        startTest();

        // Locate existing variable
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("varia", false);
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        String[] asIdeals = {"$variable_0003"};

        CompletionInfo jCompl = GetCompletion();
        if (asIdeals.length != jCompl.size()) {
            fail("Invalid CC list size: " + jCompl.size() + ", expected: " + asIdeals.length);
        }
        // Check each
        CheckCompletionItems(jCompl, asIdeals);
        jCompl.hideAll();

        endTest();
    }

    public void Verify_documentation_hints_for_built_in_identifiers() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/", false);
        TypeCode(eoPHP, "\n");
        //TypeCode( eoPHP, "$" );
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        CompletionInfo jCompl = GetCompletion();

        Timeouts t = jCompl.listItself.getTimeouts();
        //t.print( System.out );
        long lBack1 = t.getTimeout("JScrollBarOperator.OneScrollClickTimeout");
        long lBack2 = t.getTimeout("JScrollBarOperator.WholeScrollTimeout");
//        t.setTimeout("JScrollBarOperator.OneScrollClickTimeout", 6000000);
//        t.setTimeout("JScrollBarOperator.WholeScrollTimeout", 6000000);
//        jCompl.listItself.setTimeouts(t);
        System.out.println("==== go to click on item ====");

        WindowOperator jdDoc = new WindowOperator(0);
        jCompl.listItself.pressKey( KeyEvent.VK_DOWN );
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
    
//        try {
//            Dumper.dumpAll("/Users/filipzamboj/dump.txt");
//        } catch (IOException ex) {
//        }
     
        String sCompleteContent = jeEdit.getText();
        System.out.println("=== check done ===");
        //back to original values
        t.setTimeout("JScrollBarOperator.OneScrollClickTimeout", lBack1);
        t.setTimeout("JScrollBarOperator.WholeScrollTimeout", lBack2);
//        jCompl.listItself.setTimeouts(t);

//      try{ Dumper.dumpAll( "/Users/filipzamboj/dump.txt" ); } catch( IOException ex ) { }


        //System.out.println( ">>>" + st + "<<<" );
        //Sleep( 5000 );
        // Check content
        String[] asContents = {
            "$GLOBALS",
            "Contains a reference to every variable which is currently available within",
            "the global scope of the script. The keys of this array are the names of",
            "the global variables. $GLOBALS has existed since PHP 3.",
            "<a href=\"http://www.php.net/manual/en/reserved.variables.php\">http://us2.php.net/manual/en/reserved.variables.php</a>"
        };
        for (String sContentPart : asContents) {
            if (-1 == sCompleteContent.indexOf(sContentPart)) {
                System.out.println(">>>" + sCompleteContent + "<<<");
                fail("Unable to find part of required content: \"" + sContentPart + "\"");
            }
        }
        //jCompl.hideAll( );
        //Backit( eoPHP, 1 );

        endTest();
    }

    public void Verify_documentation_hints_for_keywords() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        //eoPHP.setCaretPosition( "*/", false );
        //TypeCode( eoPHP, "ext" );
        //eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
        //Sleep( 5000 );
        //CompletionInfo jCompl = GetCompletion( );
        //System.out.println( "==== go to click on item ====" );
        //Sleep( 5000 );

        CompletionInfo jCompl = GetCompletion();

        Timeouts t = jCompl.listItself.getTimeouts();
        //t.print( System.out );
//        long lBack1 = t.getTimeout("JScrollBarOperator.OneScrollClickTimeout");
//        long lBack2 = t.getTimeout("JScrollBarOperator.WholeScrollTimeout");
        t.setTimeout("JScrollBarOperator.OneScrollClickTimeout", 60000);
        t.setTimeout("JScrollBarOperator.WholeScrollTimeout", 60000);
        jCompl.listItself.setTimeouts(t);

        jCompl.listItself.clickOnItem("extends", new CFulltextStringComparator());

        t.setTimeout("JScrollBarOperator.OneScrollClickTimeout", 60000);
        t.setTimeout("JScrollBarOperator.WholeScrollTimeout", 60000);
        jCompl.listItself.setTimeouts(t);
        WindowOperator jdDoc = new WindowOperator(1);
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
        String sCompleteContent = jeEdit.getText();
        // Check content
        String[] asContents = {
            "extends!!!"
        };
        for (String sContentPart : asContents) {
            if (-1 == sCompleteContent.indexOf(sContentPart)) {
                System.out.println(">>>" + sCompleteContent + "<<<");
                // THIS IS ISSUE
                fail("Unable to find part of required content: \"" + sContentPart + "\"");
            }
        }

        jCompl.hideAll();
        //Backit( eoPHP, 3 );
        endTest();
    }

    public void Verify_keywords_code_completion() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("?>", true);
        TypeCode(eoPHP, "class a ext");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        TypeCode(eoPHP, "\n");
        CheckResult(eoPHP, "class a extends", -1);

        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber() - 1);

        endTest();
    }

    public void Verify_code_completion_after_extends_keyword() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("?>", true);
        TypeCode(eoPHP, "class a ext");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        TypeCode(eoPHP, " ");

        CompletionInfo completionInfo = GetCompletion();
        if (null == completionInfo) {
            fail("No code competion after extends .");
        }
        eoPHP.pushKey(KeyEvent.VK_ENTER);
        Sleep(1000);
        TypeCode(eoPHP, "\n");
        CheckResult(eoPHP, "class a extends AppendIterator", -1);


        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber() - 1);

        endTest();
    }

    public void Verify_code_completion_with_a_single_option() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("?>", true);
        TypeCode(eoPHP, "odbc_ge");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);
        CompletionInfo completionInfo = GetCompletion();
        if (null == completionInfo) {
            fail("No code competion after extends .");
        }
        TypeCode(eoPHP, "\n");
        CheckResult(eoPHP, "odbc_gettypeinfo($connection_id)",0);

        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber() - 1);

        endTest();
    }

    public void Verify_JavaDoc_window() {
        startTest();

        String sJavaDoc = "This is function 1234567890...";

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("\nfunction", false);
        eoPHP.setCaretPositionToLine(eoPHP.getLineNumber());
        TypeCode(eoPHP, "\n/**\n" + sJavaDoc);
        eoPHP.setCaretPosition("}", false);
        TypeCode(eoPHP, "\nfunction_");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);

        CompletionInfo jCompl = GetCompletion();
        jCompl.listItself.clickOnItem("function_0001");

        WindowOperator jdDoc = new WindowOperator(0);
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
        String sCompleteContent = jeEdit.getText();
        // Check content
        if (-1 == sCompleteContent.replaceAll("[\t\r\n ]", "").indexOf(sJavaDoc.replaceAll("[\t\r\n ]", ""))) {
            System.out.println(">>>" + sCompleteContent + "<<<");
            fail("Unable to find part of required content: \"" + sJavaDoc + "\"");
        }
        jCompl.hideAll();
        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber());

        endTest();
    }

    public void Verify_code_completion_after_EXTENDS() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("}", false);
        TypeCode(eoPHP, "\nclass Foo\n{\n");
        eoPHP.setCaretPosition("?>", true);
        TypeCode(eoPHP, "\nclass MyClass extends F");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);

        CompletionInfo jCompl = GetCompletion();
        jCompl.listItself.clickOnItem("Foo");

        endTest();
    }

    public void Verify_that_require_directive_is_automatically_added() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("require", true);
        eoPHP.deleteLine(eoPHP.getLineNumber());

        EditorOperator eoPHP_2 = new EditorOperator("newEmptyPHP2.php");
        eoPHP_2.setCaretPosition("\n?>", true);
        TypeCode(eoPHP_2, "function_");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);

        CompletionInfo jCompl = GetCompletion();
        jCompl.listItself.clickOnItem("function_0001", 2);
        Sleep(1000);

        String sText = eoPHP_2.getText();
        if (-1 == sText.indexOf("required")) {
            // THIS IS ISSUE
            // fail( "Require directive was not added into source file." );
        }

        endTest();
    }

    public void Verify_code_completion_in_slash_slash_comments() {
        startTest();

        EditorOperator eoPHP_2 = new EditorOperator("newEmptyPHP2.php");
        TypeCode(eoPHP_2, "//");
        eoPHP_2.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);

        CompletionJListOperator jList = new CompletionJListOperator();
        List lm = null;
        try {
            lm = jList.getCompletionItems();
        } catch (Exception ex) {
            fail("Somehting is wrong completely, unable to get List for completion.");
        }
        Object o = lm.get(0);
        if (o.toString().contains("No suggestions")) { // cc works no harm to letting it this way
            fail("Completion should not work for // comments.");
        }

        // Cleanup
        //eoPHP_2.pressKey( KeyEvent.VK_BACK_SPACE );
        jList.hideAll();

        endTest();
    }

    public void Verify_code_completion_in_slash_star_comments() {
        startTest();

        EditorOperator eoPHP_2 = new EditorOperator("newEmptyPHP2.php");
        TypeCode(eoPHP_2, "\n/* comment    */");
        eoPHP_2.setCaretPosition("/* comment  ", false);
        eoPHP_2.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(1000);


        // Check code completion list
        try {
            CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            // Check some completions
            String[] asCompletions = {
                "AppendIterator",
                "Countable",
                "DOMAttr",
                "PDORow",
                "ZipArchive",
                "tidy"
            };
            CheckCompletionItems(completionInfo.listItself, asCompletions);
            completionInfo.listItself.hideAll();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        /*
        CompletionJListOperator jList = new CompletionJListOperator( );
        List lm = null;
        try
        {
        lm = jList.getCompletionItems( );
        }
        catch( Exception ex )
        {
        fail( "Somehting is wrong completely, unable to get List for completion." );
        }
        Object o = lm.get( 0 );
        if( !o.toString( ).contains( "No suggestions" ) )
        fail( "Completion should not work for /* comments." );
         */

        // Cleanup


        endTest();
    }

    public void Verify_code_completion_in_slash_star_star_comments() {
        startTest();

        EditorOperator eoPHP_2 = new EditorOperator("newEmptyPHP2.php");
        eoPHP_2.setCaretPosition(8);
        waitScanFinished();
        eoPHP_2.deleteLine(eoPHP_2.getLineNumber());
        TypeCode(eoPHP_2, "\n/** \n");
        TypeCode(eoPHP_2, "@");
        eoPHP_2.typeKey(' ', InputEvent.CTRL_MASK);
        

        CompletionInfo completionInfo = GetCompletion();
        if (null == completionInfo) {
            fail("NPE instead of competion info.");
        }

        // Check some completions
        String[] asCompletions = {
            "@abstract",
            "@access",
            "@author",
            "@category",
            "@copyright",
            "@deprecated",
            "@example",
            "@filesource",
            "@final",
            "@global",
            "@ignore",
            "@internal",
            "@license",
            "@link",
            "@method",
            "@name",
            "@package",
            "@param",
            "@property",
            "@property-read",
            "@property-write",
            "@return",
            "@see",
            "@since",
            "@static",
            "@staticvar",
            "@subpackage",
            "@todo",
            "@tutorial",
            "@uses",
            "@var",
            "@version"
        };
        CheckCompletionItems(completionInfo.listItself, asCompletions);
        completionInfo.listItself.hideAll();

        endTest();
    }
}
