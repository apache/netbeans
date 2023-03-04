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

package org.netbeans.test.php.notes;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testNotes extends notes
{
  static final String TEST_PHP_NAME = "PhpProject_notes_0001";

  public testNotes( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testNotes.class ).addTest(
          "CreateApplication",
          "Notes"
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

  public void Notes( ) throws Exception
  {
    startTest( );

    try
    {
    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );
    // Add new line
    eoPHP.insert( "\n" );
    System.out.println( "===X" );
    TypeCode( eoPHP, "class a extends 1" );
    System.out.println( "===A" );
    Sleep( 30000 );
    System.out.println( "===B" );
    Object[] oo = eoPHP.getAnnotations( );
    System.out.println( "===C" );
    System.out.println( "+++" + oo.length );
    for( Object o : oo )
    {
      System.out.println( "***" + eoPHP.getAnnotationType( o ) + " : " + eoPHP.getAnnotationShortDescription( o ) );
    }
    Sleep( 10000 );
    }
    catch( Exception ex )
    {
      System.out.println( "!!!" );
    }

    endTest( );
  }
}
