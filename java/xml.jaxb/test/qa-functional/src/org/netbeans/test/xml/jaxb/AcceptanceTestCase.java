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

package org.netbeans.test.xml.jaxb;

import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
//import org.netbeans.test.xml.schema.lib.SchemaMultiView;
//import org.netbeans.test.xml.schema.lib.util.Helpers;

import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Robot;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyException;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCase extends JellyTestCase {
    
    static final String JAXB_CATEGORY_NAME = "XML";
    static final String JAXB_COMPONENT_NAME = "JAXB Binding";

    static final String BUTTON_NAME_VERBOSE = "verbose";
    static final String BUTTON_NAME_READONLY = "readOnly";
    static final String BUTTON_NAME_FINISH = "Finish";
    static final String BUTTON_NAME_YES = "Yes";

    static final String POPUP_CHANGE_JAXB_OPTIONS = "Change JAXB Options";
    static final String POPUP_DELETE = "Delete";

    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }
    
    // We need to create one from different points, so moved
    // code out of startTest/endTest field.
    public void CreateJAXBBindingInternal(
        String sBindingName,
        String sPackageName,
        String sApplicationName,
        String sBaseFile,
        boolean bSuppressInformation
      )
    {
        // Create JAXB Binding
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
        opNewFileWizard.selectCategory( JAXB_CATEGORY_NAME );
        opNewFileWizard.selectFileType( JAXB_COMPONENT_NAME );
        opNewFileWizard.next();

        JDialogOperator opCustomizer = new JDialogOperator( );
        new JTextFieldOperator( opCustomizer, 0 ).setText( sBindingName );

        new JButtonOperator( opCustomizer, 0 ).pushNoBlock( );
        JFileChooserOperator opFileChooser = new JFileChooserOperator( );
        opFileChooser.chooseFile( System.getProperty( "xtest.data" ) + File.separator + sBaseFile );

        if( bSuppressInformation )
        {
          JDialogOperator jinfo = new JDialogOperator( "Information" );
          JButtonOperator jbut = new JButtonOperator( jinfo, "OK" );
          jbut.push( );
          //jinfo.waitClose( );
        }

        new JTextFieldOperator( opCustomizer, 4 ).setText( sPackageName );

        new JCheckBoxOperator( opCustomizer, BUTTON_NAME_VERBOSE ).setSelected( true );

        // Ensure we will catch all with any slowness
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );

        opNewFileWizard.finish( );

        // Wait till JAXB really created
        int iWait = 2;
        while( true )
        {
          try
          {
            stt.waitText( "Finished building " + sApplicationName + " (jaxb-code-generation)." );
            break;
          }
          catch( JemmyException ex )
          {
            System.out.println( "--- ex" );
            if( 0 == --iWait )
              throw ex;
          }
        }
        stt.stop( );

        return;
    }

    public void ChangeJAXBOptionsInternal(
        String sBindingName,
        String sApplicationName
      )
    {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke( );

        ProjectRootNode prn = pto.getProjectRootNode( sApplicationName );
        prn.select( );

        Node bindingNode = new Node( prn, "JAXB Binding|" + sBindingName );
        bindingNode.select( );
        bindingNode.performPopupActionNoBlock( POPUP_CHANGE_JAXB_OPTIONS );

        NbDialogOperator opCustomizer = new NbDialogOperator( "Change JAXB options" );
        new JCheckBoxOperator( opCustomizer, BUTTON_NAME_READONLY ).setSelected( true );

        // Start waiting before pressing finish button
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );

        new JButtonOperator( opCustomizer, BUTTON_NAME_FINISH ).pushNoBlock( );
        
        stt.waitText( "Finished building " + sApplicationName + " (jaxb-clean-code-generation)." );
        stt.stop( );

        // Check options
        FilesTabOperator fto = FilesTabOperator.invoke( );

        Node projectNode = fto.getProjectNode( sApplicationName );
        projectNode.select( );

        Node nodeWalk = new Node( projectNode, "nbproject|xml_binding_cfg.xml" );
        nodeWalk.performPopupAction( "Edit" );
        EditorOperator eoXMLCode = new EditorOperator( "xml_binding_cfg.xml" );
        String sText = eoXMLCode.getText( );

        String[] asIdealCode =
        {
          "<xjc-options>",
          "<xjc-option name='-verbose' value='true'/>",
          "<xjc-option name='-readOnly' value='true'/>"
        };

        for( String sIdealCode : asIdealCode )
        {
          if( -1 == sText.indexOf( sIdealCode ) )
          {
            fail( "Unable to find required code inside xml_binding_cfg.xml : " + sIdealCode );
          }
        }
        eoXMLCode.close( false );
    }

    private void WaitCredCompletion( EditorOperator eoJavaCode, boolean bConstructor )
    {
      eoJavaCode.insert( "Cred" );
      eoJavaCode.typeKey( ' ', InputEvent.CTRL_MASK );
      int iPassed = 0;
      while( true )
      {
        String sCompletedText = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
        if( !sCompletedText.endsWith( "Cred\n" ) )
        {
          if( bConstructor )
            eoJavaCode.insert( "( )" );
          break;
        }
        if( ++iPassed > 10 )
        {
          try
          {
            CompletionJListOperator jCompl = new CompletionJListOperator( );
            jCompl.clickOnItem( 0, 2 );
            bConstructor = false;
          }
          catch( JemmyException ex )
          {
          }
        }
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }

    private void WaitEndOfLine( EditorOperator eoJavaCode, String sEnd )
    {
      while( true )
      {
        String sCompletedText = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
        if( !sCompletedText.endsWith( sEnd ) )
          break;
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }

    private CompletionJListOperator GetCompletion( )
    {
      CompletionJListOperator comp = null;
      while( true )
      {
        comp = new CompletionJListOperator( );
        try
        {
          Object o = comp.getCompletionItems( ).get( 0 );
          if( !o.toString( ).contains( "No suggestions" ) )
            return comp;
        }
        catch( java.lang.Exception ex )
        {
          return null;
        }
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }



    public void CodeCompletion1Internal( )
    {
        // Access java code with editor
        EditorOperator eoJavaCode = new EditorOperator( "Main.java" );

        eoJavaCode.setCaretPosition(
            "// TODO code application logic here",
            0,
            false
          );
        eoJavaCode.insert( "\n" );

        JEditorPaneOperator editor = eoJavaCode.txtEditorPane( );

        // First most important line like "CreditReport cr = new CreditReport( );"
        WaitCredCompletion( eoJavaCode, false );
        eoJavaCode.insert( " cr = new " );
        WaitCredCompletion( eoJavaCode, true );
        eoJavaCode.insert( ";\n" );

        // Next lines
        eoJavaCode.insert( "cr" );
        editor.typeKey( '.' );
        CompletionJListOperator jCompl = GetCompletion( );
        int iIndex = jCompl.findItemIndex( "setFirstName" );
        if( -1 == iIndex )
          fail( "Unable to find setFirstName() completion." );
        eoJavaCode.insert( "setFirstName( \"Hello\" );\n" );

        // Next lines
        eoJavaCode.insert( "cr" );
        editor.typeKey( '.' );
        jCompl = GetCompletion( );
        iIndex = jCompl.findItemIndex( "setLastName" );
        if( -1 == iIndex )
          fail( "Unable to find setLastName() completion." );
        eoJavaCode.insert( "setLastName( \"World\" );\n" );

        // Next lines
        eoJavaCode.insert( "cr" );
        editor.typeKey( '.' );
        jCompl = GetCompletion( );
        iIndex = jCompl.findItemIndex( "setScore" );
        if( -1 == iIndex )
          fail( "Unable to find setScore() completion." );
        eoJavaCode.insert( "setScore( 999 );\n" );

        // Next lines
        eoJavaCode.insert( "cr" );
        editor.typeKey( '.' );
        jCompl = GetCompletion( );
        iIndex = jCompl.findItemIndex( "setSsn" );
        if( -1 == iIndex )
          fail( "Unable to find setSsn() completion." );
        eoJavaCode.insert( "setSsn( \"123-456-ABC\" );\n" );

        // TODO : Check result
    }

    public void CodeCompletion2Internal( String sPackage ) {

        // Access java code with editor
        EditorOperator eoJavaCode = new EditorOperator( "Main.java" );

        // Use jaxbm template
        JEditorPaneOperator editor = eoJavaCode.txtEditorPane( );
        String sCode = "jaxbm\t";
        for( int i = 0; i < sCode.length( ); i++ )
          editor.typeKey( sCode.charAt( i ) );

        // Check result
        eoJavaCode.pushUpArrowKey( );
        String[] asIdealCodeLines =
        {
          "try {",
          "javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(args.getClass().getPackage().getName());|javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(obj2BMarshalled.getClass().getPackage().getName());|javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(cr.getClass().getPackage().getName());",
          "javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();",
          "marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N",
          "marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);",
          "marshaller.marshal(args, System.out);|marshaller.marshal(obj2BMarshalled, System.out);|marshaller.marshal(cr, System.out);",
          "} catch (javax.xml.bind.JAXBException ex) {",
          "// XXXTODO Handle exception",
          "java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N",
          "}",
          "}"
        };

        for( String sIdealCodeLine : asIdealCodeLines )
        {
          String sCodeLine = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
          while( sCodeLine.matches( "^[ \t\n]*$" ) )
          {
            eoJavaCode.pushDownArrowKey( );
            sCodeLine = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
          }
          String[] asIdealVersions = sIdealCodeLine.split( "\\|" );
          boolean bFound = false;
          for( String sIdealVersion : asIdealVersions )
          {
            if( -1 != sCodeLine.indexOf( sIdealVersion ) )
            {
              bFound = true;
              break;
            }
          }

          if( !bFound )
          {
            // Test <suite> failed
            fail(
                "Ideal code was not found at line #" + eoJavaCode.getLineNumber( ) +
                " : " + sIdealCodeLine
              );
          }
          eoJavaCode.pushDownArrowKey( );
        }

        // Replace args with cr
        eoJavaCode.setCaretPosition( "javax.xml.bind.JAXBContext.newInstance(", 0, false );
        editor = eoJavaCode.txtEditorPane( );
        editor.pushKey( KeyEvent.VK_DELETE, InputEvent.CTRL_MASK );
        editor.releaseKey( KeyEvent.VK_DELETE, InputEvent.CTRL_MASK );
        eoJavaCode.insert( "cr" );

        eoJavaCode.setCaretPosition( "marshaller.marshal(", 0, false );
        editor = eoJavaCode.txtEditorPane( );
        editor.pushKey( KeyEvent.VK_DELETE, InputEvent.CTRL_MASK );
        editor.releaseKey( KeyEvent.VK_DELETE, InputEvent.CTRL_MASK );
        eoJavaCode.insert( "cr" );

        // Correct imports
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Source|Fix Imports...");

        // Check there is required import statement
        String sText = eoJavaCode.getText( );
        if( -1 == sText.indexOf( "import " + sPackage + ".CreditReport;" ) )
          fail( "Unable to fix imports." );
    }

    public void RunTheProjectInternal( String sAppName ) {

        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );

        // Run
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Run|Run Main Project");

        stt.waitText( "Finished building " + sAppName + " (run)." );
        stt.stop( );

        // Check output
        OutputOperator out = new OutputOperator( );
        String sText = out.getText( );
        String[] asIdealOutput =
        {
          "run:",
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
          "<CreditReport xmlns=\"http://xml.netbeans.org/schema/CreditReport\">",
          "<firstName>Hello</firstName>",
          "<lastName>World</lastName>",
          "<ssn>123-456-ABC</ssn>",
          "<score>999</score>",
          "</CreditReport>",
          "BUILD SUCCESSFUL (total time: ",
        };
        for( String sChecker : asIdealOutput )
        {
          if( -1 == sText.indexOf( sChecker ) )
            fail( "Unable to find ideal output: " + sChecker + "\n" + sText );
        }
        if( -1 != sText.indexOf( "BUILD FAILED" ) )
          fail( "BUILD FAILED\n" + sText );
    }
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }

    protected void startTest(){
        super.startTest();
        //Helpers.closeUMLWarningIfOpened();
    }

}
