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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
