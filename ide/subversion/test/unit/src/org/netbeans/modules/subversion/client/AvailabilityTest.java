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

package org.netbeans.modules.subversion.client;

import org.netbeans.modules.subversion.AbstractSvnTestCase;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.cli.CommandlineClient;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
public class AvailabilityTest extends AbstractSvnTestCase {
    private String exec = null;
    
    public AvailabilityTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        exec = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        SvnModuleConfig.getDefault().setExecutableBinaryPath(exec);
        super.tearDown();
    }

    public void testVersion() throws Exception {
        CommandlineClient c = new CommandlineClient();
        c.checkSupportedVersion();
    }
    
    public void testWrongPath() throws Exception {
        SvnModuleConfig.getDefault().setExecutableBinaryPath("schwabka");
        CommandlineClient c = new CommandlineClient();
        
        Exception ex = null;
        try {
            c.checkSupportedVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNotNull(ex);
    }
    
    public void testCorrectPath() throws Exception {
        String executablePath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if((Utilities.isUnix() || Utilities.isMac()) && (executablePath == null || executablePath.trim().equals(""))) {
            // lets guess
            executablePath = "/usr/bin";
            SvnModuleConfig.getDefault().setExecutableBinaryPath(executablePath);
        }
        CommandlineClient c = new CommandlineClient();
        
        Exception ex = null;
        try {
            c.checkSupportedVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(ex);        
                
        if(executablePath.endsWith("/")) {
            executablePath = executablePath.substring(0, executablePath.length() - 2);
        } else {
            executablePath += "/";
        }

        try {
            c.checkSupportedVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(ex);         
    }
    
    public void testDefaultPath() throws Exception {
        String executablePath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(executablePath != null && !executablePath.trim().equals("")) {
            SvnModuleConfig.getDefault().setExecutableBinaryPath("");
        }
        CommandlineClient c = new CommandlineClient();
        SvnModuleConfig.getDefault().setExecutableBinaryPath(executablePath); // fixit
        
        Exception ex = null;
        try {
            c.checkSupportedVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(ex);        
    }
}
