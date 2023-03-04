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
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jiri Rechtacek
 */
public class TargetClusterWhenUpdateLocallyTest extends TargetClusterTestCase {
    
    public TargetClusterWhenUpdateLocallyTest (String testName) {
        super (testName);
    }
    
    private static UpdateElement installed = null;
    
    @Override
    protected String getCodeName (String target, Boolean global) {
        return "org.yourorghere.testupdatemodule";
    }
    
    @Override
    protected UpdateElement getInstalledUpdateElement () throws IOException, OperationException {
        if (installed == null) {
            // !!! origin module is installed in platformDir
            installed = installModule (getCodeName (null, null));
        }
        return installed;
    }

    @RandomlyFails
    public void testUpdateLocally () throws IOException, OperationException {
        // TODO: adjust changes (issue 128718)
        // If an update, overwrite the existing location, wherever that is.
        assertEquals ("Goes into platformDir", platformDir.getName (), getTargetCluster (null, null).getName ());
    }
    
}
