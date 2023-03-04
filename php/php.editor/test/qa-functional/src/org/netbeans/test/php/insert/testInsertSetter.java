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

public class testInsertSetter extends insert
{
  static final String TEST_PHP_NAME = "PhpProject_insert_0003";

  public testInsertSetter( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testInsertSetter.class ).addTest(
          "CreateApplication",
          "InsertSetter"
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

  public void InsertSetter( ) throws Exception
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

    ClickListItemNoBlock( jlList, 2, 1 );

    JDialogOperator jdGenerator = new JDialogOperator( "Generate Setters" );

    // Sleect all but $c
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
      "public function setA($a)",
      "{",
      "$this->a = $a;",
      "}",
      "",
      "public function setB($b)",
      "{",
      "$this->b = $b;",
      "}",
      "",
      "public function setD($d)",
      "{",
      "$this->d = $d;",
      "}"
    };
    CheckResult( eoPHP, asResult, -15 );
    */
    CheckFlex( eoPHP, "public function setA($a){$this->a=$a;}public function setB($b){$this->b=$b;}public function setD($d){$this->d=$d;}", false );

    endTest( );
  }
}
