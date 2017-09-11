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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallDisabledModuleTest extends OperationsTestImpl {
    public InstallDisabledModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        File test = new File(getWorkDir(), "test");
        
        System.setProperty("netbeans.dirs", test.getPath());
        LOG.log(Level.INFO, "Setting netbeans.dirs property to {0}", System.getProperty("netbeans.dirs"));
        clearWorkDir();
        super.setUp();        
        assertEquals(test.getPath(), System.getProperty("netbeans.dirs"));
        
        File jar = new File(new File(test, "modules"), "com-sun-testmodule-cluster.jar");
        jar.getParentFile().mkdirs();
        jar.createNewFile();
        
        final String fn = moduleCodeNameBaseForTest().replace('.', '-') + ".xml";
        FileObject fo = FileUtil.getConfigFile("Modules").createData(fn);
        OutputStream os = fo.getOutputStream();
        String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                "<module name='com.sun.testmodule.cluster'>\n" +
                "   <param name='autoload'>false</param>\n" +
                "   <param name='eager'>false</param>\n" +
                "   <param name='enabled'>false</param>\n" +
                "   <param name='jar'>modules/com-sun-testmodule-cluster.jar</param>\n" +
                "   <param name='reloadable'>false</param>\n" +
                "   <param name='specversion'>1.0</param>\n" +
                "</module>\n" +
                "\n";
        os.write(cfg.getBytes("UTF-8"));
        os.close();
        LOG.info("Config file created");

        assertNotNull("File exists", FileUtil.getConfigFile("Modules/" + fn));
    }

    @Override
    boolean incrementNumberOfModuleConfigFiles() {
        return false;
    }
    @Override
    boolean writeDownConfigFile() {
        return true;
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.sun.testmodule.cluster"; //NOI18N
    }

    @RandomlyFails @Override
    public void testSelf() throws Throwable {
        LOG.info("testSelf starting");
        UpdateUnit install = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull("There is an NBM to install", install);
        LOG.log(Level.INFO, "module install found: {0}", install);
        Throwable t = null;
        try {
            installModule(install, null);//fail("OK");
        } catch (Throwable ex) {
            t = ex;
        }
        LOG.log(Level.INFO, "Info installModule over with {0}", t);
        
        File f = new File(new File(new File(new File(System.getProperty("netbeans.user")), "config"), "Modules"), "com-sun-testmodule-cluster.xml");
        LOG.log(Level.INFO, "Does {0} exists: {1}", new Object[]{f, f.exists()});
        File m = new File(new File(new File(getWorkDir(), "test"), "modules"), "com-sun-testmodule-cluster.jar");
        LOG.log(Level.INFO, "Does {0} exists: {1}", new Object[]{m, m.exists()});
        if (t != null) {
            throw t;
        }
        assertTrue("Config file created in userdirectory for install of new module: " + f, f.exists());
    }
}
