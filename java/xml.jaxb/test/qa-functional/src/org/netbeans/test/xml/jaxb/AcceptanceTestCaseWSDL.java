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

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseWSDL extends AcceptanceTestCase {
    
    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateJAXBBinding",
        "ExploreJAXBBinding",
        "ChangeJAXBOptions",
        "DeleteJAXBBinding",
        "OpenSchemaFile", // <--
        "RefreshSchemaFile", // <--
        "RegenerateJavaCode", // <--
        "CodeCompletion1", // <--
        "CodeCompletion2", // <--
        "RunTheProject"
    };

    static final String TEST_JAVA_APP_NAME = "jaxbonwsdl";
    static final String JAXB_BINDING_NAME = "CheckCredit";
    static final String JAXB_PACKAGE_NAME = "CreditReportSimple";

    static final String JAVA_CATEGORY_NAME = "Java";
    static final String JAVA_PROJECT_NAME = "Java Application";

    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    public AcceptanceTestCaseWSDL(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCaseWSDL.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCaseWSDL(strMethodName));
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

        endTest( );
    }
    
    public void CreateJAXBBinding() {
        startTest();

        CreateJAXBBindingInternal(
            JAXB_BINDING_NAME,
            JAXB_PACKAGE_NAME,
            TEST_JAVA_APP_NAME,
            "CreditReportSimple.wsdl",
            true
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
          "JAXB Bindings|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".wsdl"
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
          "xml-resources|jaxb|" + JAXB_BINDING_NAME + "|" + JAXB_PACKAGE_NAME + ".wsdl"
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
        if(
            -1 == sText.indexOf( "<xjc package=\"" + JAXB_PACKAGE_NAME + "\"" )
            || -1 == sText.indexOf( "<arg value=\"-verbose\"/>" )
          )
        {
          fail( "Unable to find required code inside xml_binding_build.xml" );
        }
        eoXMLCode.close( false );

        nodeWalk = new Node( projectNode, "nbproject|xml_binding_cfg.xml" );
        nodeWalk.performPopupAction( "Edit" );
        eoXMLCode = new EditorOperator( "xml_binding_cfg.xml" );
        sText = eoXMLCode.getText( );
        if(
            -1 == sText.indexOf( "<xjc-options>" )
            || -1 == sText.indexOf( "<xjc-option name='-verbose' value='true'/>" )
          )
        {
          fail( "Unable to find required code inside xml_binding_cfg.xml" );
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
            "CreditReportSimple.wsdl",
            true
          );

        endTest( );
    }

    public void OpenSchemaFile( )
    {
        startTest( );

        endTest( );
    }

    public void RefreshSchemaFile( )
    {
        startTest( );

        endTest( );
    }

    public void RegenerateJavaCode( )
    {
        startTest( );

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
