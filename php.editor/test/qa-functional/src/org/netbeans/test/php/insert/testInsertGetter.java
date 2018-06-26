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

package org.netbeans.test.php.insert;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
//import org.netbeans.jemmy.util.Dumper;


/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testInsertGetter extends insert
{
  static final String TEST_PHP_NAME = "PhpProject_insert_0002";

  public testInsertGetter( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testInsertGetter.class ).addTest(
          "CreateApplication",
          "InsertGetter"
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

  public void InsertGetter( ) throws Exception
  {
    startTest( );

    // Invoke Alt+Insert without any code
    // List should contain two database related items

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here\n", false );
    eoPHP.insert( "\nclass name\n{\npublic $a;\nprotected $b;\nprivate $c, $d;\n}" );
    eoPHP.setCaretPosition( "$d;\n", false );
    Sleep( 1000 );
    InvokeInsert( eoPHP );
    Sleep( 1000 );

    JDialogOperator jdInsetter = new JDialogOperator( );
    JListOperator jlList = new JListOperator( jdInsetter );

    ClickListItemNoBlock( jlList, 1, 1 );

    JDialogOperator jdGenerator = new JDialogOperator( "Generate Getters" );
    
    // Select all but $c
    JTreeOperator jtTree = new JTreeOperator( jdGenerator, 0 );
    jtTree.clickOnPath( jtTree.findPath( "a" ) );
    jtTree.clickOnPath( jtTree.findPath( "b" ) );
    jtTree.clickOnPath( jtTree.findPath( "d" ) );
    new JCheckBoxOperator(jdGenerator, "Use Public Modifier").push();
    
    JButtonOperator jbOk = new JButtonOperator( jdGenerator, "OK" );
    jbOk.pushNoBlock( );
    jdGenerator.waitClosed( );

    // Check result
    /*
    String[] asResult =
    {
      "public function getA()",
      "{",
      "return $this->a;",
      "}",
      "",
      "public function getB()",
      "{",
      "return $this->b;",
      "}",
      "",
      "public function getD()",
      "{",
      "return $this->d;",
      "}"
    };
    CheckResult( eoPHP, asResult, -15 );
    */
    CheckFlex( eoPHP, "public function getA(){return $this->a;}public function getB(){return $this->b;}public function getD(){return $this->d;}", false );

    endTest( );
  }
}
