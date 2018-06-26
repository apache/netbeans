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
