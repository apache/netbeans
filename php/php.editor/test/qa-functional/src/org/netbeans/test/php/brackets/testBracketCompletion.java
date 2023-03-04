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

package org.netbeans.test.php.brackets;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testBracketCompletion extends brackets
{
  static final String TEST_PHP_NAME = "PhpProject_brackets_0001";

  public testBracketCompletion( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testBracketCompletion.class ).addTest(
          "CreateApplication",
          "SimplePairs",
          "CurlyBrackets"
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

  protected void Backit( EditorOperator eoPHP, int iCount )
  {
    for( int i = 0; i < iCount; i++ )
      eoPHP.pressKey( KeyEvent.VK_BACK_SPACE );
  }

  protected void EnsureEmptyLine( EditorOperator eoPHP )
  {
    CheckResultRegex( eoPHP, "[ \t\r\n]*" );
  }

  protected void CompletePairCheck( EditorOperator eoPHP, String sCode, String sCheck )
  {
    TypeCodeCheckResult( eoPHP, sCode, sCheck );
    Backit( eoPHP, sCode.length( ) );
    EnsureEmptyLine( eoPHP );
  }

  protected String CreatePair( String sCode )
  {
    String sSuffix = "";
    boolean bQuote = true;
    for( int i = 0; i < sCode.length( ); i++ )
    {
      switch( sCode.charAt( i ) )
      {
        case '[':
          if( bQuote )
            sSuffix = "]" + sSuffix;
          break;
        case '(':
          if( bQuote )
            sSuffix = ")" + sSuffix;
          break;
        case ']':
          if( bQuote )
            sSuffix = sSuffix.substring( 1 );
          break;
        case ')':
          if( bQuote )
            sSuffix = sSuffix.substring( 1 );
          break;
        case '"':
          if( bQuote )
            sSuffix = "\"" + sSuffix;
          else
            sSuffix = sSuffix.substring( 1 );
          bQuote = !bQuote;
          break;
      }
    }
    return sCode + sSuffix;
  }

  public void SimplePairs( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );
    // Add new line
    eoPHP.insert( "\n" );
    Sleep( 1000 );

    // Predefined
    String[] asCheckers =
    {
      "[(\"",
      "[(\"\")]",
      "name(",
      "name()",
      "name[",
      "name[]",
      "hello(a[\"1"
    };

    for( String sChecker : asCheckers )
    {
      String[] asChecker = sChecker.split( "[|]" );
      CompletePairCheck(
          eoPHP,
          asChecker[ 0 ],
          ( 1 == asChecker.length ) ? CreatePair( asChecker[ 0 ] ) : asChecker[ 1 ]
        );
    }

    // Check something random
    // Yes I know about StringBuffer :)
    String sRandom = "";
    String sCharset = "abc123[[[[[((((((\"";
    for( int i = 0; i < 50; i++ )
      sRandom = sRandom + sCharset.charAt( ( int )( Math.random( ) * sCharset.length( ) ) );
    CompletePairCheck( eoPHP, sRandom, CreatePair( sRandom ) );

    endTest( );
  }

  protected void CheckCurlyCode(
      EditorOperator eoPHP,
      String sStartCode,
      String sCloseCode,
      int iDeep
    )
  {
    TypeCodeCheckResult( eoPHP, sStartCode, CreatePair( sStartCode ) );
    TypeCodeCheckResult( eoPHP, sCloseCode, "}", 1 );
    for( int i = 2; i <= iDeep; i++ )
      CheckResult( eoPHP, "}", i );
  }

  public void CurlyBrackets( )
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    eoPHP.pressKey( KeyEvent.VK_ENTER );

    // Empty block
    TypeCode( eoPHP, "{\n" );
    CheckResult( eoPHP, "}", 1 );

    // Define class
    TypeCode( eoPHP, "class a\n{\n" );
    CheckResult( eoPHP, "}", 1 );
    CheckResult( eoPHP, "}", 2 );

    String[] asDeep =
    {
      "function aa(",
      "for( $i = 0; $i < 10; $i++ ",
      "while( true ",
      "if( 1 == 2 "
    };

    for( int i = 0; i < asDeep.length; i++ )
    {
      CheckCurlyCode(
          eoPHP,
          asDeep[ i ],
          ")\n{\n",
          i + 3
        );
    }

    endTest( );
  }
}
