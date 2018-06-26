/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
