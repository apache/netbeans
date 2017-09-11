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
