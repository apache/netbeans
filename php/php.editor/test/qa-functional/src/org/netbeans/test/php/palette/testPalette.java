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

package org.netbeans.test.php.palette;

import javax.swing.JEditorPane;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import org.netbeans.jemmy.operators.JListOperator;
import javax.swing.ListModel;
import java.awt.Point;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.Timeout;
import java.awt.event.InputEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testPalette extends palette
{
  static final String TEST_PHP_NAME = "PhpProject_palette_0001";

  public testPalette( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testPalette.class ).addTest(
          "CreateApplication",
          "Create_a_PHP_web_page",
          "openPalette",
          "DnD_Table_HTML_component_to_HTML_code",
          "DnD_Table_HTML_component_to_PHP_code",
          "DnD_Table_HTML_component_outside_of_editor_view",
          "DnD_big_Table_HTML_component_to_HTML_code"
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  public void CreateApplication( )
  {
    startTest( );

    CreatePHPApplicationInternal( TEST_PHP_NAME );

    endTest( );
  }

  public void Create_a_PHP_web_page( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME, "PHP Web Page", null );
    
    endTest( );
  }
  
  public void openPalette() {
      startTest();
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Palette");
      endTest();
  }

  private void DragSomething(
      String sFile,
      String sLocation,
      String sComponent
    )
  {
    // Locate coords of HTML code
    EditorOperator eoPHP = new EditorOperator( sFile );
    eoPHP.setCaretPosition( sLocation, false );

   int iX = 0, iY = 0;

   JEditorPaneOperator txt = eoPHP.txtEditorPane( );
   JEditorPane epane =  ( JEditorPane )txt.getSource( );
   try
   {
     Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
     iX = rct.x;
     iY = rct.y;
   }
   catch( BadLocationException ex )
   {
     fail( "Unable to detect destination location." );
   }

   //TopComponentOperator top = new TopComponentOperator( "EmptyPHPWebPage.php" );
   TopComponentOperator pal = new TopComponentOperator( "Palette" );
   JListOperator list = new JListOperator( pal, 0 );

   ListModel lmd = list.getModel( );
   int iIndex = list.findItemIndex( sComponent );
   list.selectItem( iIndex );
   Point pt = list.getClickPoint( iIndex );

   MouseRobotDriver m_mouseDriver = new MouseRobotDriver(new Timeout("", 500));
   m_mouseDriver.moveMouse( list, pt.x, pt.y );
   m_mouseDriver.pressMouse( InputEvent.BUTTON1_MASK, 0 );
   m_mouseDriver.enterMouse( txt );
   m_mouseDriver.dragMouse( txt, iX, iY, InputEvent.BUTTON1_MASK, 0 );
   m_mouseDriver.releaseMouse( InputEvent.BUTTON1_MASK, 0 );

   return;
  }

  protected void CheckResultRegexp( EditorOperator edit, String sResult, int iOffset )
  {
    String sCode = edit.getText( edit.getLineNumber( ) + iOffset );
    if( !sCode.matches( "[ \t]*" + sResult + "[ \t\r\n]*" ) )
      fail( "Invalid code found." );
  }

  protected void CheckResultRegexpEx( EditorOperator edit, String sResult, int iOffset )
  {
    String sCode = edit.getText( edit.getLineNumber( ) + iOffset );
    if( !sCode.matches( ".*" + sResult + "[ \t\r\n]*" ) )
      fail( "Invalid code found." );
  }

  private void CheckInsertedTable(
      String sFile,
      int iRows,
      int iCols,
      int iBorder,
      int iWidth,
      int iSpacing,
      int iPadding
    )
  {
    // TODO
    EditorOperator eoPHP = new EditorOperator( sFile );
    CheckResultRegexp( eoPHP, "</table>", -1 );
    CheckResultRegexp( eoPHP, "</tbody>", -2 );
    int iOffset = -3;
    for( int i = 0; i < iRows; i++ )
    {
      CheckResultRegexp( eoPHP, "</tr>", iOffset-- );
      for( int j = 0; j < iCols; j++ )
        CheckResultRegexp( eoPHP, "<td></td>", iOffset-- );
      CheckResultRegexp( eoPHP, "<tr>", iOffset-- );
    }
    CheckResultRegexp( eoPHP, "<tbody>", iOffset-- );
    CheckResultRegexp( eoPHP, "</thead>", iOffset-- );
    CheckResultRegexp( eoPHP, "</tr>", iOffset-- );
    for( int j = 0; j < iCols; j++ )
      CheckResultRegexp( eoPHP, "<th></th>", iOffset-- );
    CheckResultRegexp( eoPHP, "<tr>", iOffset-- );
    CheckResultRegexp( eoPHP, "<thead>", iOffset-- );

    String sHeader = "<table";
    if( 0 != iBorder )
      sHeader = sHeader + " border=\"" + iBorder + "\"";
    if( 0 != iWidth )
      sHeader = sHeader + " width=\"" + iWidth + "\"";
    if( 0 != iSpacing )
      sHeader = sHeader + " cellspacing=\"" + iSpacing + "\"";
    if( 0 != iPadding )
      sHeader = sHeader + " cellpadding=\"" + iPadding + "\"";
    sHeader = sHeader + ">";

    CheckResultRegexpEx( eoPHP, sHeader, iOffset-- );
  }

  private int CheckValue( JDialogOperator dlg, int iIndex, int iValue )
  {
    JTextComponentOperator text = new JTextComponentOperator( dlg, iIndex );
    if( -1 == iValue )
      return Integer.parseInt( text.getText( ) );
    text.enterText( "" + iValue );
    return iValue;
  }

  private void DragTable(
      String sFile,
      String sLocation,
      int iRows,
      int iCols,
      int iBorder,
      int iWidth,
      int iSpacing,
      int iPadding
    )
  {
    DragSomething( sFile, sLocation, "Table" );
    JDialogOperator jdInsert = new JDialogOperator( "Insert Table" );

    // Check values
    iRows = CheckValue( jdInsert, 0, iRows );
    iCols = CheckValue( jdInsert, 1, iCols );
    iBorder = CheckValue( jdInsert, 2, iBorder );
    iWidth = CheckValue( jdInsert, 3, iWidth );
    iSpacing = CheckValue( jdInsert, 4, iSpacing );
    iPadding = CheckValue( jdInsert, 5, iPadding );

    Sleep( 1000 );

    JButtonOperator jbOk = new JButtonOperator( jdInsert, "OK" );
    jbOk.push( );
    jdInsert.waitClosed( );
    CheckInsertedTable(
        sFile,
        iRows,
        iCols,
        iBorder,
        iWidth,
        iSpacing,
        iPadding
      );
  }

  private void DragTable( String sFile, String sLocation )
  {
    // Use default values
    DragTable( sFile, sLocation, -1, -1, -1, -1, -1, -1 );
  }

  public void DnD_Table_HTML_component_to_HTML_code( )
  {
    startTest( );

    DragTable( "EmptyPHPWebPage.php", "<body>" );

    endTest( );
  }

  public void DnD_Table_HTML_component_to_PHP_code( )
  {
    startTest( );

    DragTable( "EmptyPHPWebPage.php", "<?php" );

    // Check errors
      // ToDo

    endTest( );
  }

  public void DnD_Table_HTML_component_outside_of_editor_view( )
  {
    startTest( );

    // TODO ?

    endTest( );
  }

  public void DnD_big_Table_HTML_component_to_HTML_code( )
  {
    startTest( );

    DragTable( "EmptyPHPWebPage.php", "?>", 25, 25, -1, -1, -1, -1 );

    endTest( );
  }
}
