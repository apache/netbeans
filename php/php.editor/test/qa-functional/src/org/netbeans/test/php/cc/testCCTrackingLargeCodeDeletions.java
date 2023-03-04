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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testCCTrackingLargeCodeDeletions extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_Issue141992";

  static final int AAA_LIST_SIZE = 999;

  public testCCTrackingLargeCodeDeletions( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testCCTrackingLargeCodeDeletions.class ).addTest(
          "CreateApplication"
      //    "Issue141992" // see #209795
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

  public void Issue141992( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );

    // Check constructor
    String sCode = "";
    for( int i = 1; i < 1000; i++ )
    {
      sCode = sCode + "\nclass a" + i + ( ( 1 == i ) ? "" : ( " extends a" + ( i - 1 ) ) ) + "\n{\npublic $a" + i + ";\n}";
    }
    eoPHP.insert( sCode );
    Sleep( 2000 );
    TypeCode( eoPHP, "\n\n$z = new a999();\n$z->" );

    // Check code completion list
    CompletionInfo jCompl = GetCompletion( );
    if( null == jCompl )
      fail( "Unable to find completion list in any form." );
    //List list = jCompl.getCompletionItems( );
    // Magic CC number for complete list
    if( AAA_LIST_SIZE != jCompl.size( ) )
      fail( "Invalid CC list size: " + jCompl.size( ) + ", expected: " + AAA_LIST_SIZE );

    jCompl.hideAll( );

    // Remove added code
    eoPHP.select( 10, eoPHP.getLineNumber( ) );
    eoPHP.pressKey( KeyEvent.VK_DELETE );

    // Strat new declaration
    eoPHP.setCaretPosition( "// put your code here", false );
    TypeCode( eoPHP, "\nclass a\n{\n" );
    Sleep( 1000 );
    TypeCode( eoPHP, "$" );

    endTest( );
  }
}
