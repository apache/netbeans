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
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
//import java.awt.Robot;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseXSD extends AcceptanceTestCase {
    
    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateJAXBBinding",
        "ExploreJAXBBinding",
        "ChangeJAXBOptions",
        "DeleteJAXBBinding",
        "OpenSchemaFile",
        "RefreshSchemaFile",
        "RegenerateJavaCode",
        "CodeCompletion1",
        "CodeCompletion2",
        "RunTheProject"
    };

    static final String TEST_JAVA_APP_NAME = "jaxbonxsd";
    static final String JAXB_BINDING_NAME = "CheckCredit";
    static final String JAXB_PACKAGE_NAME = "CreditReport";

    static final String JAVA_CATEGORY_NAME = "Java";
    static final String JAVA_PROJECT_NAME = "Java Application";

    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    public AcceptanceTestCaseXSD(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCaseXSD.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCaseXSD(strMethodName));
        }
        
        return testSuite;
    }
    
    public void CreateJavaApplication( )
    {
        startTest( );

        // Create Java application
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
        opNewProjectWizard.selectCategory( JAVA_CATEGORY_NAME );
        opNewProjectWizard.selectProject( JAVA_PROJECT_NAME );
        opNewProjectWizard.next( );

        NewJavaProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewJavaProjectNameLocationStepOperator( );
        opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.workdir" ) );
        opNewProjectNameLocationStep.txtProjectName( ).setText( TEST_JAVA_APP_NAME );
        opNewProjectWizard.finish( );

        org.netbeans.junit.ide.ProjectSupport.waitScanFinished( );

        endTest( );
    }

    public void CreateJAXBBinding( )
    {
        startTest();

        CreateJAXBBindingInternal(
            JAXB_BINDING_NAME,
            JAXB_PACKAGE_NAME,
            TEST_JAVA_APP_NAME,
            "CreditReport.xsd",
            false
          );

        endTest();
    }
    
    public void ExploreJAXBBinding( )
    {
        startTest( );

        CFulltextStringComparator cmp = new CFulltextStringComparator( );

        // Access to projects page
        ProjectsTabOperator pto = new ProjectsTabOperator( );

        //JTreeOperator treeProject = pto.tree( );
        ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME );
        prn.select( );

        String[] asProjectsToCheck =
        {
          "JAXB Bindings|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".xsd"
        };

        prn.setComparator( cmp );
        for( int i = 0; i < asProjectsToCheck.length; i++ )
        {
          Node node = new Node(
              prn,
              asProjectsToCheck[ i ]
            );
          if( null == node )
          {
            fail( "Unable to explore project node named: " + asProjectsToCheck[ i ] );
          }
        }

        // Access to files page
        FilesTabOperator fto = FilesTabOperator.invoke( );

        //JTreeOperator tree = fto.tree( );

        Node projectNode = fto.getProjectNode( TEST_JAVA_APP_NAME );
        projectNode.select( );

        String[] asFilesToCheck =
        {
          "build|classes|" + JAXB_PACKAGE_NAME,
          "build|classes|" + JAXB_PACKAGE_NAME + "|CreditQuery.class",
          "build|classes|" + JAXB_PACKAGE_NAME + "|CreditReport.class",
          "build|classes|" + JAXB_PACKAGE_NAME + "|ObjectFactory.class",
          "build|classes|" + JAXB_PACKAGE_NAME + "|package-info.class",

          "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME,
          "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME + "|CreditQuery.java",
          "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME + "|CreditReport.java",
          "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME + "|ObjectFactory.java",
          "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME + "|package-info.java",

          "nbproject",
          "nbproject|xml_binding_build.xml",
          "nbproject|xml_binding_cfg.xml",

          "nbproject|private|private.properties|default language",
          //"nbproject|private|private.properties|default language|jaxws.endorsed.dir",

          "xml-resources|jaxb|" + JAXB_BINDING_NAME,
          "xml-resources|jaxb|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".xsd"
        };

        // TODO : correct checking for jaxws.endorsed.dir item
        // JDK 5 -- should not check
        // JDK 6 -- should check

        projectNode.setComparator( cmp );
        for( int i = 0; i < asFilesToCheck.length; i++ )
        {
          Node node = new Node(
              projectNode,
              asFilesToCheck[ i ]
            );
          if( null == node )
          {
            fail( "Unable to explore files node named: " + asFilesToCheck[ i ] );
          }
        }

        Node nodeWalk = new Node( projectNode, "nbproject|xml_binding_build.xml" );
        nodeWalk.performPopupAction( "Open" );
        EditorOperator eoXMLCode = new EditorOperator( "xml_binding_build.xml" );
        String sText = eoXMLCode.getText( );

        String[] asIdealCode1 =
        {
          "<xjc package=\"" + JAXB_PACKAGE_NAME + "\"",
          "<arg value=\"-xmlschema\"/>",
          "<arg value=\"-verbose\"/>"
        };

        for( String sIdealCode : asIdealCode1 )
        {
          if( -1 == sText.indexOf( sIdealCode ) )
          {
            fail( "Unable to find required code inside xml_binding_build.xml : " + sIdealCode );
          }
        }
        eoXMLCode.close( false );

        nodeWalk = new Node( projectNode, "nbproject|xml_binding_cfg.xml" );
        nodeWalk.performPopupAction( "Edit" );
        eoXMLCode = new EditorOperator( "xml_binding_cfg.xml" );
        sText = eoXMLCode.getText( );

        String[] asIdealCode2 =
        {
          "<xjc-options>",
          "<xjc-option name='-verbose' value='true'/>"
        };

        for( String sIdealCode : asIdealCode2 )
        {
          if( -1 == sText.indexOf( sIdealCode ) )
          {
            fail( "Unable to find required code inside xml_binding_cfg.xml : " + sIdealCode );
          }
        }
        eoXMLCode.close( false );
        

        endTest( );
    }
    
    public void ChangeJAXBOptions( )
    {
        startTest( );

        ChangeJAXBOptionsInternal( JAXB_BINDING_NAME, TEST_JAVA_APP_NAME );

        endTest( );
    }

    public void DeleteJAXBBinding( )
    {
        startTest( );

        ProjectsTabOperator pto = ProjectsTabOperator.invoke( );

        ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME );
        prn.select( );

        Node bindingNode = new Node( prn, "JAXB Binding|" + JAXB_BINDING_NAME );
        bindingNode.select( );
        bindingNode.performPopupAction( POPUP_DELETE );

        JDialogOperator dlg = new JDialogOperator( "Confirm Object Deletion" );
        JButtonOperator btn = new JButtonOperator( dlg, BUTTON_NAME_YES );
        btn.pushNoBlock( );

        // Wait till JAXB really deleted
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );
        stt.waitText( "Finished building " + TEST_JAVA_APP_NAME + " (jaxb-clean-code-generation)." );
        stt.stop( );

        CreateJAXBBindingInternal(
            JAXB_BINDING_NAME,
            JAXB_PACKAGE_NAME,
            TEST_JAVA_APP_NAME,
            "CreditReport.xsd",
            false
          );

        endTest( );
    }

    protected boolean CheckSchemaView( String sView )
    {
      for( int i = 0; i < 2; i++ )
      {
        JMenuBarOperator bar = new JMenuBarOperator( MainWindowOperator.getDefault( ) );
        JMenuItemOperator menu = bar.showMenuItem("View|Editors|" + sView );
        boolean bres = menu.isSelected( );
        bar.closeSubmenus( );
        if( bres )
          return true;
      }
      return false;
    }

    public void OpenSchemaFile( )
    {
        startTest( );

        ProjectsTabOperator pto = ProjectsTabOperator.invoke( );

        ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME );
        prn.select( );

        Node bindingNode = new Node( prn, "JAXB Binding|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".xsd" );
        bindingNode.select( );
        bindingNode.performPopupActionNoBlock( "Open" );
        
        // TODO : expected fail
        // if( !CheckSchemaView( "Schema" ) )
          // fail( "Wrong schema view used, required \"Schema\"." );

        endTest( );
    }

    protected void AddItInternal(
        int iColumn,
        String sItName,
        String sMenuToAdd,
        String sRadioName,
        String sTypePath,
        String sAddedName
      )
    {
      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( JAXB_PACKAGE_NAME + ".xsd" );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( iColumn );
      opList.selectItem( sItName );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( sItName );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Attribute...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( sMenuToAdd + "..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( sMenuToAdd.replace( "|", " " ) );

      // Set unique name
      JTextFieldOperator txt = new JTextFieldOperator( jadd, 0 );
      txt.setText( sAddedName );

      // Use existing definition
      if( null != sRadioName )
      {
        JRadioButtonOperator jex = new JRadioButtonOperator( jadd, sRadioName );
        jex.setSelected( true );
      }

      // Get tree
      if( null != sTypePath )
      {
        JTreeOperator jtree = new JTreeOperator( jadd, 0 );
        TreePath path = jtree.findPath( sTypePath );
      
        jtree.selectPath( path );
        jtree.clickOnPath( path );
      }

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      // Check attribute was added successfully
      opList = opMultiView.getColumnListOperator( iColumn + 1 );
      iIndex = opList.findItemIndex( sAddedName );
      if( -1 == iIndex )
        fail( "It was not added." );

    }

    protected void WaitSaveAll( )
    {
      for( int i = 0; i < 2; i++ )
      {
        JMenuBarOperator bar = new JMenuBarOperator( MainWindowOperator.getDefault( ) );
        JMenuItemOperator menu = bar.showMenuItem("File|Save All" );
        boolean bres = menu.isEnabled( );
        bar.closeSubmenus( );
        if( bres )
          return;
      }
      return;
    }

    public void RefreshSchemaFile( )
    {
      startTest( );

      // TODO : Add elements using design.
      // TEMPORARY : Using Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      AddItInternal(
          0,
          "Elements",
          "Add Element",
          "Use Existing Type",
          "Built-in Types|string",
          "NewElementForRefresh"
        );

      // Save All
      WaitSaveAll( );
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("File|Save All");

      // Invoke Refresh
      ProjectsTabOperator pto = ProjectsTabOperator.invoke( );

      ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME );
      prn.select( );

      Node bindingNode = new Node( prn, "JAXB Binding|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".xsd" );
      bindingNode.select( );
      bindingNode.performPopupAction( "Refresh" );

      // TODO : check result
      // TEMPORARY : Using Schema view
      try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
      SchemaMultiView opMultiView = new SchemaMultiView( JAXB_PACKAGE_NAME + ".xsd" );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Elements" );
      opList = opMultiView.getColumnListOperator( 1 );
      int iIndex = opList.findItemIndex( "NewElementForRefresh" );
      if( -1 != iIndex )
      {
        fail( "Element still presents after schema Refresh." );
      }

      // Back to schema?
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      endTest( );
    }

    public void RegenerateJavaCode( )
    {
      startTest( );

      // TODO : Add elements using Design view.
      // TEMPORARY : Using Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      AddItInternal(
          0,
          "Complex Types",
          "Add Complex Type",
          null,
          null,
          "NewComplexTypeForRegeneration"
        );

      AddItInternal(
          2,
          "sequence",
          "Add|Element",
          "Use Existing Type",
          "Built-in Types|date",
          "SubElementDate"
        );

      //new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Design");

      // Invoke Refresh
      ProjectsTabOperator pto = ProjectsTabOperator.invoke( );

      ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME );
      prn.select( );

      Node bindingNode = new Node( prn, "JAXB Binding" );
      bindingNode.select( );

      // Wait till JAXB really deleted
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      bindingNode.performPopupAction( "Regenerate Java Code" );

      stt.waitText( "Finished building " + TEST_JAVA_APP_NAME + " (jaxb-code-generation)." );
      stt.stop( );

      // TODO : check result
      // Access to files page
      FilesTabOperator fto = FilesTabOperator.invoke( );

      Node projectNode = fto.getProjectNode( TEST_JAVA_APP_NAME );
      projectNode.select( );

      String[] asFilesToCheck =
      {
        "build|classes|" + JAXB_PACKAGE_NAME + "|NewComplexTypeForRegeneration.class",
        "build|generated|addons|jaxb|" + JAXB_PACKAGE_NAME + "|NewComplexTypeForRegeneration.java",
        "build|generated|jaxbCache|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + "|NewComplexTypeForRegeneration.java",
      };

      projectNode.setComparator( new CFulltextStringComparator( ) );
      for( int i = 0; i < asFilesToCheck.length; i++ )
      {
        Node node = new Node(
            projectNode,
            asFilesToCheck[ i ]
          );
        if( null == node )
        {
          fail( "Unable to explore files node named: " + asFilesToCheck[ i ] );
        }
      }

      // Close schema
      EditorOperator eoXMLCode = new EditorOperator( JAXB_PACKAGE_NAME + ".xsd" );
      eoXMLCode.close( );

      endTest( );
    }

    public void CodeCompletion1( )
    {
        startTest( );

        CodeCompletion1Internal( );

        endTest( );
    }

    public void CodeCompletion2( ) {
        startTest();

        CodeCompletion2Internal( JAXB_PACKAGE_NAME );

        endTest();
    }

    public void RunTheProject( ) {
        startTest();

        RunTheProjectInternal( TEST_JAVA_APP_NAME );

        endTest();
    }
    
}
