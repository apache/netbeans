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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * http://netbeans.org/bugzilla/show_bug.cgi?id=141881
 * 
 * @author michaelnazarov@netbeans.org
 */

public class testCCIfSpacesInExpression extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_Issue141881";

  public testCCIfSpacesInExpression( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testCCIfSpacesInExpression.class ).addTest(
          "CreateApplication",
          "Issue141881"
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

  public void Issue141881( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );
    // Add new line
    //eoPHP.insert( "\nclass a\n{\nfunction xx( )\n{\n}\n}\n$aa = new a;\n" );
    //Sleep( 1000 );

    // Check constructor
    String sCode = "\nclass a\n{\nfunction xx( )\n{\n}\n}\n$aa = new a;\n$aa -";
    TypeCode( eoPHP, sCode );
    Sleep( 10000 );
    eoPHP.typeKey( '>' );

    // Check code completion list
    waitScanFinished();
    String[] asIdeals = { "xx" };

    CompletionInfo jCompl = GetCompletion( );
    if( null == jCompl )
      fail( "Unale to find completion list in any form." );
    //List list = jCompl.getCompletionItems( );
    // Magic CC number for complete list
    if( asIdeals.length != jCompl.size( ) )
      fail( "Invalid CC list size: " + jCompl.size( ) + ", expected: " + asIdeals.length );
    // Check each
    CheckCompletionItems( jCompl, asIdeals );

    jCompl.hideAll( );

    endTest( );
  }
}
