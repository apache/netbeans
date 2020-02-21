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
