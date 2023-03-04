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
 * http://netbeans.org/bugzilla/show_bug.cgi?id=141855
 * 
 * @author michaelnazarov@netbeans.org
 */

public class testCCExceptionAfterInvokation extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_Issue141855";

  public testCCExceptionAfterInvokation( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testCCExceptionAfterInvokation.class ).addTest(
          "CreateApplication",
          "Issue141855"
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

  public void Issue141855( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "?>", false );
    // Add new line
    eoPHP.insert( "\n" );
    Sleep( 100 );
    // Press Ctrl+Space
    eoPHP.typeKey( '<' );
    Sleep( 100 );
    eoPHP.typeKey( '?' );
    Sleep( 100 );
    eoPHP.typeKey( ' ' );
    Sleep( 100 );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 100 );
    // Check code completion list
    CompletionInfo jCompl = GetCompletion( );
    //List list = jCompl.getCompletionItems( );
    // Magic CC number for complete list
    if( COMPLETION_LIST_THRESHOLD > jCompl.listItems.size( ) )
      fail( "Invalid CC list size: " + jCompl.listItems.size( ) + ", expected: " + COMPLETION_LIST_THRESHOLD );

    jCompl.listItself.hideAll( );

    endTest( );
  }
}
