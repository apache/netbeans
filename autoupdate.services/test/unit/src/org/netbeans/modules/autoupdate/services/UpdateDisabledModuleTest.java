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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.netbeans.updater.UpdaterInternal;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class UpdateDisabledModuleTest extends NbTestCase {
    static Manifest man;
    private File ud;

    public UpdateDisabledModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "com.sun.testmodule.cluster");
        man.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        
        ud = new File(getWorkDir(), "ud");
        System.setProperty("netbeans.user", ud.getPath());
        new File(ud, "config").mkdirs();
        
        final File install = new File(getWorkDir(), "install");
        File platform = new File(install, "platform");
        System.setProperty("netbeans.home", platform.getPath());
        new File(platform, "config").mkdirs();
        
        File middle = new File(install, "middle");
        File last = new File(install, "last");
        System.setProperty("netbeans.dirs", middle.getPath() + File.pathSeparator + last.getPath());
        
        final String fn = moduleCodeNameBaseForTest().replace('.', '-') + ".xml";
        File conf = new File(new File(new File(middle, "config"), "Modules"), fn);
        conf.getParentFile().mkdirs();
        writeConfXML(conf, false);

        final File jar = new File(new File(last, "modules"), "com-sun-testmodule-cluster.jar");
        jar.getParentFile().mkdirs();
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar), man);
        os.close();

        File real = new File(new File(new File(last, "config"), "Modules"), fn);
        real.getParentFile().mkdirs();
        writeConfXML(real, true);
        
        FileUtil.getConfigRoot().getFileSystem().refresh(true);
        
        File ut = new File(new File(last, "update_tracking"), fn);
        ut.getParentFile().mkdirs();
        OutputStream utos = new FileOutputStream(ut);
        String utcfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
"<module codename=\"com.sun.testmodule.cluster\">\n" +
"    <module_version install_time=\"1280356738644\" last=\"true\" origin=\"installer\" specification_version=\"0.99\">\n" +
"        <file crc=\"3486416273\" name=\"config/Modules/com-sun-testmodule-cluster.xml\"/>\n" +
"        <file crc=\"3486416273\" name=\"modules/com-sun-testmodule-cluster.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";
        utos.write(utcfg.getBytes("UTF-8"));
        utos.close();
        
        StringBuilder msg = new StringBuilder();
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            msg.append(mi.getCodeNameBase()).append("\n");
            if (mi.getCodeNameBase().equals("com.sun.testmodule.cluster")) {
                assertFalse("Disabled", mi.isEnabled());
                return;
            }
        }
        fail("No com.sun.testmodule.cluster module found:\n" + msg);
    }

    private void writeConfXML(File conf, boolean enabled) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(conf);
        String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                "<module name='com.sun.testmodule.cluster'>\n" +
                "   <param name='autoload'>false</param>\n" +
                "   <param name='eager'>false</param>\n" +
                "   <param name='enabled'>" + enabled + "</param>\n" +
                "   <param name='jar'>modules/com-sun-testmodule-cluster.jar</param>\n" +
                "   <param name='reloadable'>false</param>\n" +
                "   <param name='specversion'>1.0</param>\n" +
                "</module>\n" +
                "\n";
        os.write(cfg.getBytes("UTF-8"));
        os.close();
    }

    String moduleCodeNameBaseForTest() {
        return "com.sun.testmodule.cluster"; //NOI18N
    }

    public void testSelf() throws Exception {
        File f = new File(new File(new File(ud, "config"), "Modules"), "com-sun-testmodule-cluster.xml");
        f.delete();
        
        assertFalse("No Config file before: " + f, f.exists());
        
        MockServices.setServices(UP.class);
        UpdateUnit update = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        
        assertNotNull("There is an NBM to update", update);
        OperationContainer<InstallSupport> oc = OperationContainer.createForUpdate();
        oc.add(update, update.getAvailableUpdates().get(0));
        final InstallSupport support = oc.getSupport();
        Validator down = support.doDownload(null, true);
        Installer inst = support.doValidate(down, null);
        Restarter res = support.doInstall(inst, null);
        System.setProperty("netbeans.close.no.exit", "true");
        support.doRestart(res, null);
        UpdaterInternal.update(null, null, null);

        assertFalse("No Config file created in for upgrade: " + f, f.exists());
    }
    
    public static final class UP implements UpdateProvider {

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getDisplayName() {
            return "test view";
        }

        @Override
        public String getDescription() {
            return "none";
        }

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.STANDARD;
        }

        @Override
        public Map<String, UpdateItem> getUpdateItems() throws IOException {
            Map<String, UpdateItem> m = new HashMap<String, UpdateItem>();
            m.put("com.sun.testmodule.cluster", UpdateItem.createModule(
                "com.sun.testmodule.cluster", "1.0", 
                DefaultTestCase.class.getResource("data/com-sun-testmodule-cluster.nbm"), 
                "jarda", "4000", "http://netbeans.de", 
                "2010/10/27", 
                "OK", 
                man, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, 
                "platform", 
                UpdateLicense.createUpdateLicense("CDDL", "Free to use")
            ));
            return m;
        }

        @Override
        public boolean refresh(boolean force) throws IOException {
            return true;
        }
        
    }
}
