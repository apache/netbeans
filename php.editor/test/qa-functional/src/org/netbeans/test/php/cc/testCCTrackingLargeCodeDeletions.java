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
