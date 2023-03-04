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

package org.netbeans.test.php.navigation;

import java.awt.event.InputEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testNavigation extends navigation
{
  static final String TEST_PHP_NAME = "PhpProject_navigation_0001";

  public testNavigation( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testNavigation.class ).addTest(
          "CreateApplication",
          "Create_a_PHP_web_page",
          "Navigate_to_a_specified_line",
          "Navigate_to_an_invalid_line"
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

  protected void GoToLine( int iLineToGo, int iLineToCome )
  {
    EditorOperator eoPHP = new EditorOperator( "EmptyPHPWebPage.php" );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    eoPHP.typeKey('g', InputEvent.CTRL_MASK);
    JDialogOperator jdGoto = new JDialogOperator( "Go to Line or Bookmark" );
    JComboBoxOperator jcLine = new JComboBoxOperator( jdGoto, 0 );
    JTextFieldOperator jtTemp = jcLine.getTextField( );
    jtTemp.setText( "" + iLineToGo );
    JButtonOperator jbGoto = new JButtonOperator( jdGoto, "Go To" );
    jbGoto.push( );
    jdGoto.waitClosed( );
    int iLine = eoPHP.getLineNumber( );
    if( iLineToCome != iLine )
    {
      fail( "Navigate go to line came to incorrect one. Found: " + iLine + ", expected: " + iLineToCome );
    }
  }

  public void Navigate_to_a_specified_line( )
  {
    startTest( );

    GoToLine( 8, 8 );

    endTest( );
  }

  public void Navigate_to_an_invalid_line( )
  {
    startTest( );

    GoToLine( 100, 18 );

    endTest( );
  }

}
