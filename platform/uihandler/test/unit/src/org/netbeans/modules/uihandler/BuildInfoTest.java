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

package org.netbeans.modules.uihandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Jan Horvath
 */
public class BuildInfoTest extends NbTestCase {
    private static final String[][] data = {
        {"Number", "080229"}, 
        {"Date", "29 Feb 2008"},
        {"Branding", ""},
        {"Branch", "trunk"},
        {"Tag", ""},
        {"Hg ID", "1f3f7e10583b tip"},
    };
    
    static File file;
    
    public BuildInfoTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        MockServices.setServices(MockInstalledFileLocator.class);
    }
    
    public void testLogBuildInfo() throws IOException {
        file = new File(getWorkDir(), "build_info");
        PrintStream ps = new PrintStream (new FileOutputStream(file));
        for (int i =0; i < data.length; i++){
            ps.println(data[i][0] + ":\t" + data[i][1]);
        }
        ps.close();
        
        Object[] params = BuildInfo.logBuildInfo().toArray();
        assertEquals(params.length, data.length);
        
        for (int i = 0; i < params.length; i++) {
            String msg = data[i][1];
            assertEquals(msg, params[i]);
        }

    }
    
    public static class MockInstalledFileLocator extends InstalledFileLocator {

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            assertEquals(BuildInfo.BUILD_INFO_FILE, relativePath);
            return file;
        }
        
    }
}
