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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.updater.UpdateTracking;

/** Issue http://www.netbeans.org/issues/show_bug.cgi?id=111701
 *
 * @author Jiri Rechtacek
 */
@RandomlyFails
public class TargetClusterTest extends TargetClusterTestCase {
    
    public TargetClusterTest (String testName) {
        super (testName);
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override protected String logRoot() {
        return "org.netbeans.modules.autoupdate";
    }
    
    public void testInstallGloballyNewIntoDeclaredPlatform () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), true).getName ());
    }

    @RandomlyFails // org.yourorghere.platform.null - UpdateUnit found.
    public void testInstallNewIntoDeclaredPlatform () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), null).getName ());
    }
    
    @RandomlyFails
    public void testInstallNewIntoDeclaredNextCluster () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }

    @RandomlyFails
    public void testInstallNewIntoDeclaredNextClusterAndFalseGlobal () throws IOException, OperationException {
        // target cluster has precedence than global
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }
    
    public void testInstallGloballyNew () throws IOException, OperationException {
        // Otherwise (no cluster name specified), if marked global, maybe put it into an "extra" cluster
        assertEquals ("Goes into " + UpdateTracking.EXTRA_CLUSTER_NAME,
                UpdateTracking.EXTRA_CLUSTER_NAME,
                getTargetCluster (null, true).getName ());
    }
    
    public void testInstallLocallyNew () throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, null).getName ());
    }
    
    public void testInstallNoDeclaredGlobalNew () throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, null).getName ());
    }
    
    public void testInstallDeclaredClusterForceLocal() throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals("Goes into " + userDir.getName(),
                userDir.getName(),
                getTargetCluster(UpdateTracking.EXTRA_CLUSTER_NAME, false).getName());
    }

    public void testInstallDeclaredClusterDefaultLocation() throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals("Goes into " + userDir.getName(),
                UpdateTracking.EXTRA_CLUSTER_NAME,
                getTargetCluster(UpdateTracking.EXTRA_CLUSTER_NAME, null).getName());
    }

}
