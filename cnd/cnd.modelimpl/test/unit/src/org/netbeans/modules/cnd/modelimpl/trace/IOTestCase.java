/*
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.openide.util.Utilities;

/**
 * dummy tests
 */
public class IOTestCase extends ModelImplBaseTestCase {

    public IOTestCase(String testName) {
        super(testName);
    }

    public void testOut() throws IOException {
        File output = new File(getWorkDir(), "file.dat"); // NOI18N
        PrintStream streamOut = new PrintStream(output);
        PrintStream oldOut = System.out;
        System.out.println("output in standard before redirect"); 
        try {
            // redirect output
            System.setOut(streamOut);
            
            System.out.println("output in file");
        } finally {
            streamOut.close();
            System.setOut(oldOut);
        }
        System.out.println("output in standard after redirect");
    }
    
    public void testErr() throws IOException {
        File error = new File(getWorkDir(), "file.err"); // NOI18N
        PrintStream streamErr = new PrintStream(error);
        PrintStream oldErr = System.err;
        System.err.println("err in standard before redirect"); 
        try {
            // redirect 
            System.setErr(streamErr);
            
            System.err.println("err in file");
        } finally {
            streamErr.close();
            System.setErr(oldErr);
        }
        System.err.println("err in standard after redirect");           
    }
    
    public void testPath() throws IOException {
        System.err.println("ipAddr");
        String path = "/net/endif/export/home3/ide/mars";
        System.err.println("canonical path " + path + " is " + new File(path).getCanonicalPath());
        path = "/set/ide/mars";
        System.err.println("canonical path " + path + " is " + new File(path).getCanonicalPath());
        path = "/home/vv159170";
        System.err.println("canonical path " + path + " is " + new File(path).getCanonicalPath());
        path = "/net/localhost/home/vv159170";
        System.err.println("canonical path " + path + " is " + new File(path).getCanonicalPath());
        System.err.println("nonProxy is " + System.getProperty("http.nonProxyHosts"));
        System.err.println("host is " + System.getenv("env-host"));
    }
    
    public void testIPAndHost() {
        System.err.println("testIPAndHost");
        long time = System.currentTimeMillis();
        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();
            System.err.println("IP addreass is " + ((int)ipAddr[0] & 0xFF) + "." + ((int)ipAddr[1] & 0xFF)  + "." + ((int)ipAddr[2] & 0xFF)  + "." + ((int)ipAddr[3] & 0xFF) );
            // Get hostname
            String hostname = addr.getHostName();
            System.err.println("host name is " + hostname);
        } catch (UnknownHostException e) {
        }      
        time = System.currentTimeMillis() - time;
        System.err.println("getting IP and host took " + time + "ms");
    }
    
    public void testWindows() {
        System.setProperty("os.name", "Windows NT");
        assertTrue("must be Windows", Utilities.isWindows());
    }

}
