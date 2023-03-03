/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.installer;

import java.util.logging.Logger;

/**
 *

 */
public class Installer {

    @org.junit.Test
    public void testInstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        Utils.phaseOne(data, "all");

        // Pages
          // Apache
        Utils.stepChooseComponent( "Apache Tomcat", data );
        // Welcome
        Utils.stepWelcome();
        // Agreement
        Utils.stepLicense();
        // Location
        Utils.stepSetDir(
            data,
            "Install the NetBeans IDE",
            data.GetNetBeansInstallPath( )
          );
        // GF
        Utils.stepSetDir(
            data,
            "Install GlassFish",
            data.GetApplicationServerInstallPath( )
          );
        if( data.m_bPreludePresents )
        {
          Utils.stepSetDir(
              data,
              "Install GlassFish prelude",
              data.GetApplicationServerPreludeInstallPath( )
            );
        }
        // Apache
        Utils.stepSetDir(
            data,
            "Install Apache Tomcat",
            data.GetTomcatInstallPath( )
          );
        // Summary
        Utils.stepInstall(data);
        //Installation
        //finish
        Utils.stepFinish();

        //Utils.phaseTwo(data);
        //Utils.phaseThree(data);
        Utils.phaseFour(data);

        //Utils.RunCommitTests( data );

        Utils.phaseFive( data );

        //Utils.phaseFourWOUninstall(data);
      //TODO Dir removed test
      //TODO Clean up work dir
    }
}
