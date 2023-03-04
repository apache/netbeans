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

package org.netbeans.test.php.project;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class testSimpleApplication extends project
{
  static final String TEST_PHP_NAME = "PhpProject_project_0001";

  public testSimpleApplication( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( testSimpleApplication.class ).addTest(
          "CreateSimpleApplicationDefault",
          "CreateSimpleApplicationCustom"
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  // Default name
  public void CreateSimpleApplicationDefault( )
  {
    startTest( );

    //for( int i = 0; i < 2; i++ )
    {
      String sProjectName = CreatePHPApplicationInternal( );

      // Check created in tree
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sProjectName + "|Source Files|" + "index.php"
        );
      prn.select( );

      // Check index.php in editor
      new EditorOperator( "index.php" );
    }

    endTest( );
  }

  // Custom name
  public void CreateSimpleApplicationCustom( )
  {
    startTest( );

    CreatePHPApplicationInternal( TEST_PHP_NAME );

    // Check created in tree
    ProjectsTabOperator pto = new ProjectsTabOperator( );
    ProjectRootNode prn = pto.getProjectRootNode(
        TEST_PHP_NAME + "|Source Files|" + "index.php"
      );
    prn.select( );

    // Check index.php in editor
    new EditorOperator( "index.php" );

    endTest( );
  }

}
