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

package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * http://netbeans.org/bugzilla/show_bug.cgi?id=141873
 * 
 * @author michaelnazarov@netbeans.org
 */

public class testCCConstructorsAndDestructors extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_Issue141873";

  public testCCConstructorsAndDestructors( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testCCConstructorsAndDestructors.class ).addTest(
          "CreateApplication",
          "Issue141873"
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

  public void Issue141873( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );
    // Add new line
    TypeCode(eoPHP, "\nclass a\n{\n" );
    Sleep( 1000 );

    // Check constructor
    String sCode = "function __con";
    String sIdeal = "            function __construct() {";
    TypeCode( eoPHP, sCode );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    WaitCompletionScanning( );

    // Get code
    String sText = eoPHP.getText( eoPHP.getLineNumber( ) - 1 );
    sText= sText.substring(0, sText.lastIndexOf("{")+1); // there is new line symbol

    // Check code completion list
    if( -1 == sText.indexOf( sIdeal ) )
      fail( "Invalid completion: \"" + sText + "\", should be: \"" + sIdeal + "\"" );

    eoPHP.setCaretPositionToEndOfLine(14);
    // Check destructor
    TypeCode(eoPHP, "\n" );
    Sleep( 1000 );
    sCode = "function __des";
    sIdeal = "            function __destruct() {";
    TypeCode( eoPHP, sCode );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    WaitCompletionScanning( );

    // Get code
    sText = eoPHP.getText( eoPHP.getLineNumber( ) -1 );

    // Check code completion list
    if( -1 == sText.indexOf( sIdeal ) )
      fail( "Invalid completion: \"" + sText + "\", should be: \"" + sIdeal + "\"" );

    endTest( );
  }
}
