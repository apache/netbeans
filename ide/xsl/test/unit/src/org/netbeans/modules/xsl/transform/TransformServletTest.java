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
package org.netbeans.modules.xsl.transform;

import java.net.*;

import junit.framework.*;
import org.netbeans.junit.*;

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
    
}
