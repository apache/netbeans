/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.autoupdate.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class ListModulesTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger("TEST-" + ListModulesTest.class.getName());
    
    public ListModulesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        System.setProperty("netbeans.user", getWorkDirPath() + File.separator + "userdir");

        // initialize whole infra
        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testAModuleIsPrinted() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        CommandLine.getDefault().process(new String[] { "--modules",  "--list" }, System.in, os, err, new File("."));

        assertEquals("No error", 0, err.size());

        if (os.toString().indexOf("org.netbeans.bootstrap") < 0) {
            fail("We want default module to be found: " + os.toString());
        }
        if (os.toString().indexOf("org.my.module") != -1) {
            fail("module not found yet: " + os.toString());
        }
    }

    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f;
        int i = 0;
        for (;;) {
            f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) break;
        }
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
}
