/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
