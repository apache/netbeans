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
package org.netbeans.modules.xsl.transform;

//import java.io.*;
import java.net.*;
import java.security.Permission;
//import java.util.*;

//import javax.servlet.*;
//import javax.servlet.http.*;

import junit.framework.*;
import org.netbeans.junit.*;

/*import org.openide.util.HttpServer;
import org.openide.filesystems.FileObject;
import org.openide.util.SharedClassObject;
import org.openide.filesystems.FileUtil;
import org.openide.execution.NbfsURLConnection;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xsl.utils.TransformUtil;
*/
/**
 *
 * @author Libor Kramolis
 */
public class TransformServletTest extends NbTestCase {
    
    public TransformServletTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformServletTest.class);
        
        return suite;
    }
    
        
    /** Test of getServletURL method, of class org.netbeans.modules.xsl.transform.TransformServlet. */
    public void testGetServletURL() {
        // issue #196602
        System.out.println("testGetServletURL");
        
        URL servletURL = null;
        boolean exceptionThrown = false;
        try {
            servletURL = TransformServlet.getServletURL();
        } catch (Exception exc) {
            System.err.println("!!! " + exc);
            exceptionThrown = true;
        }
        
        assertTrue ("I need correct Transform Servlet URL!", (servletURL!=null & exceptionThrown!= true));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SecurityManager sm = new SecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
            }
            
        };
        System.setSecurityManager(sm);
    }

    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(null);
        super.tearDown();
    }
    
}
