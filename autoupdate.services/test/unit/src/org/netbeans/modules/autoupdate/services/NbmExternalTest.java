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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class NbmExternalTest extends NbTestCase {

    protected List<UpdateUnit> keepItNotToGC;
    private static File catalogFile;
    private static URL catalogURL;
    private File tmpDirectory;
    private List<File> nbms = new ArrayList<File>();
    private List<String> moduleElements = new ArrayList<String>();

    public NbmExternalTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    
    public static class MyProvider extends AutoupdateCatalogProvider {

        public MyProvider() {
            super("test-updates-provider", "test-updates-provider", catalogURL, UpdateUnitProvider.CATEGORY.STANDARD);
        }
    }

    private String getModuleElement(boolean visible, String codeName, String releaseVersion, String implVersion, String moduleName, String distr, String specVersion, String dependency) {
        String releaseVersionAppendix = ((releaseVersion != null /*&& Integer.parseInt(releaseVersion)!=0*/) ? ("/" + releaseVersion) : "");
        return "\n<module "
                + "\n     codenamebase='" + codeName + "' "
                + "\n     distribution='" + distr + "' "
                + "\n     downloadsize='0' "
                + "\n     homepage='' "
                + "\n     license='AD9FBBC9' "
                + "\n     moduleauthor='' "
                + "\n     needsrestart='false' "
                + "\n     releasedate='2007/01/30'>"
                + "\n    <manifest "
                + "\n       AutoUpdate-Show-In-Client='" + visible + "' "
                + "\n       OpenIDE-Module='" + codeName + releaseVersionAppendix + "'"
                + "\n       OpenIDE-Module-Implementation-Version='" + (implVersion == null ? "070130" : implVersion) + "' "
                + "\n       OpenIDE-Module-Java-Dependencies='Java &gt; 1.4' "
                + "\n       OpenIDE-Module-Name='" + moduleName + "' "
                + (dependency != null ? " OpenIDE-Module-Module-Dependencies=\"" + dependency.replace(">", "&gt;") + "\" " : "")
                + "\n       OpenIDE-Module-Requires='org.openide.modules.ModuleFormat1' "
                + "\n       OpenIDE-Module-Specification-Version='" + specVersion + "'/>"
                + "\n</module>";
    }

    private String createInfoXML(boolean visible, String codeName, String releaseVersion, String implVersion, String moduleName, String distr, String specVersion, String dependency) {
        String moduleElement = getModuleElement(visible, codeName, releaseVersion, implVersion, moduleName, distr, specVersion, dependency);

        moduleElements.add(moduleElement);
        moduleElements.add("\n    <license name='AD9FBBC9'>[NO LICENSE SPECIFIED]"
                         + "\n</license>\n");
        return "<?xml version='1.0' encoding='UTF-8'?>"
                + "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Autoupdate Module Info 2.5//EN' 'http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd'>"
                + moduleElement;
    }

    private void writeCatalog() throws IOException {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">"
                + "<module_updates timestamp=\"00/00/19/08/03/2006\">\n";
        for (String element : moduleElements) {
            res += element;
        }
        res += "</module_updates>\n";
        if (catalogFile == null) {
            catalogFile = File.createTempFile("catalog-", ".xml", tmpDirectory);
            catalogURL = Utilities.toURI(catalogFile).toURL();
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(catalogFile), "UTF-8"));
        pw.write(res);
        pw.close();
    }

    private String getConfigXML(String codeName, String moduleFile, String specVersion) {
        return "<?xml version='1.0' encoding='UTF-8'?>"
                + " <!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN'"
                + " 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>"
                + " <module name='" + codeName + "'>"
                + " <param name='autoload'>false</param>"
                + " <param name='eager'>false</param>"
                + " <param name='enabled'>true</param>"
                + " <param name='jar'>modules/" + moduleFile + ".jar</param>"
                + " <param name='reloadable'>false</param>"
                + " <param name='specversion'>" + specVersion + "</param>"
                + " </module>";
    }

    private String getManifest(String codeName, String releaseVersion, String implVersion, String moduleDir, String specVersion, boolean visible, String dependency) {
        String releaseVersionAppendix = ((releaseVersion != null /*&& Integer.parseInt(releaseVersion)!=0*/) ? ("/" + releaseVersion) : "");
        return "Manifest-Version: 1.0\n"
                + "Ant-Version: Apache Ant 1.7.0\n"
                + "Created-By: 1.6.0-b105 (Sun Microsystems Inc.)\n"
                + "OpenIDE-Module-Public-Packages: -\n"
                + "OpenIDE-Module-Java-Dependencies: Java > 1.4\n"
                + "OpenIDE-Module-Implementation-Version: " + (implVersion == null ? "070130" : implVersion)
                + "\n"
                + (dependency != null ? ("OpenIDE-Module-Module-Dependencies: " + dependency + "\n") : "")
                + "OpenIDE-Module: " + codeName + releaseVersionAppendix
                + "\n"
                + "OpenIDE-Module-Localizing-Bundle: " + moduleDir + "Bundle.properties\n"
                + "OpenIDE-Module-Specification-Version: " + specVersion + "\n"
                + "OpenIDE-Module-Requires: org.openide.modules.ModuleFormat1\n"
                + "AutoUpdate-Show-In-Client: " + visible + "\n"
                + "\n";
    }

    private File prepareNBM(String codeName, String releaseVersion, String implVersion, String specVersion, boolean visible, String dependency) throws Exception {
        String moduleName = codeName.substring(codeName.lastIndexOf(".") + 1);
        String moduleFile = codeName.replace(".", "-");
        String moduleDir = codeName.replace(".", "/") + "/";
        File nbm = File.createTempFile(moduleFile + "-", ".nbm", tmpDirectory);

        final String MODULE_NAME_PROP = "OpenIDE-Module-Name";

        File jar = new File(tmpDirectory, "x.jar");
        jar.getParentFile().mkdirs();
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));
        int idx = moduleDir.indexOf("/");
        while (idx != -1) {
            jos.putNextEntry(new ZipEntry(moduleDir.substring(0, idx + 1)));
            idx = moduleDir.indexOf("/", idx + 1);
        }

        jos.putNextEntry(new ZipEntry(moduleDir + "Bundle.properties"));
        jos.write(new String(MODULE_NAME_PROP + "=" + moduleName).getBytes("UTF-8"));
        jos.putNextEntry(new ZipEntry("META-INF/"));
        jos.putNextEntry(new ZipEntry("META-INF/manifest.mf"));
        jos.write(getManifest(codeName, releaseVersion, implVersion, moduleDir, specVersion, visible, dependency).getBytes("UTF-8"));
        jos.close();
        File ext = new File(jar.getParentFile(), jar.getName() + ".external");
        FileOutputStream os = new FileOutputStream(ext);
        os.write(("CRC: " + UpdateTracking.getFileCRC(jar) + "\n").getBytes());
        os.write(("URL: " + Utilities.toURI(jar).toString() + "\n").getBytes());
        os.close();

        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");


        jos = new JarOutputStream(new FileOutputStream(nbm), mf);
        jos.putNextEntry(new ZipEntry("Info/"));
        jos.putNextEntry(new ZipEntry("Info/info.xml"));
        jos.write(createInfoXML(visible, codeName, releaseVersion, implVersion, moduleName, Utilities.toURI(nbm).toURL().toString(), specVersion, dependency).getBytes("UTF-8"));

        jos.putNextEntry(new ZipEntry("netbeans/"));
        jos.putNextEntry(new ZipEntry("netbeans/modules/"));
        jos.putNextEntry(new ZipEntry("netbeans/config/"));
        jos.putNextEntry(new ZipEntry("netbeans/config/Modules/"));
        jos.putNextEntry(new ZipEntry("netbeans/config/Modules/" + moduleFile + ".xml"));

        jos.write(getConfigXML(codeName, moduleFile, specVersion).getBytes("UTF-8"));


        jos.putNextEntry(new ZipEntry("netbeans/modules/" + moduleFile + ".jar.external"));

        FileInputStream fis = new FileInputStream(ext);
        FileUtil.copy(fis, jos);
        fis.close();
        ext.delete();
        jos.close();
        nbms.add(nbm);

        return nbm;
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        tmpDirectory = new File(getWorkDirPath(), "tmp");
        tmpDirectory.mkdirs();

        writeCatalog();

        TestUtils.setUserDir(getWorkDirPath());
        TestUtils.testInit();

        MainLookup.register(new MyProvider());
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
    }

    private void doInstall(OperationContainer<InstallSupport> installContainer) throws OperationException {
        InstallSupport support = installContainer.getSupport();
        assertNotNull(support);

        InstallSupport.Validator v = support.doDownload(null, false);
        assertNotNull(v);
        InstallSupport.Installer i = support.doValidate(v, null);
        assertNotNull(i);
        Restarter r = null;
        try {
            r = support.doInstall(i, null);
        } catch (OperationException ex) {
            if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType()) {
                // can ingore
                // module system cannot load the module either
            } else {
                fail(ex.toString());
            }
        }
        assertNull("Installing new element require restarting though it should not", r);
    }

    public void testNbmWithExternal() throws Exception {
        String moduleCNB = "org.netbeans.modules.mymodule";
        String moduleReleaseVersion = "1";
        String moduleImplVersion = "2";
        String moduleSpecVersion = "1.0";

        prepareNBM(moduleCNB, moduleReleaseVersion, moduleImplVersion, moduleSpecVersion, false, null);

        writeCatalog();
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit moduleUnit = getUpdateUnit(moduleCNB);
        assertNull("cannot be installed", moduleUnit.getInstalled());
        UpdateElement moduleElement = getAvailableUpdate(moduleUnit, 0);
        assertEquals(moduleElement.getSpecificationVersion(), moduleSpecVersion);
        OperationInfo<InstallSupport> independentInfo = installContainer.add(moduleElement);
        assertNotNull(independentInfo);
        doInstall(installContainer);
        
        File module = new File(new File(getWorkDir(), "modules"), "org-netbeans-modules-mymodule.jar");
        assertTrue("module file exists", module.exists());
        assertTrue("module was not installed from NBM external", moduleUnit.getInstalled() != null);
        File ext = new File(new File(getWorkDir(), "modules"), "org-netbeans-modules-mymodule.jar.external");
        assertFalse("Ext file is not written", ext.exists());
        File utf = new File(new File(getWorkDir(), "update_tracking"), "org-netbeans-modules-mymodule.xml");
        assertTrue("Update tracking exists", utf.exists());
        String content;
        {
            byte[] arr = new byte[(int)utf.length()];
            FileInputStream is = new FileInputStream(utf);
            is.read(arr);
            is.close();
            content = new String(arr);
        }
        if (!content.contains("\"modules/org-netbeans-modules-mymodule.jar\"")) {
            fail("Wrong content:\n" + content);
        }
        if (content.contains("\"modules/org-netbeans-modules-mymodule.jar.external\"")) {
            fail("Wrong content:\n" + content);
        }
        if (!content.contains("crc=\"" + UpdateTracking.getFileCRC(module) + "\"")) {
            fail("Wrong content:\n" + content);
        }
    }

    

    public UpdateUnit getUpdateUnit(String codeNameBase) {
        UpdateUnit uu = UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
        assertNotNull(uu);
        return uu;
    }

    public UpdateElement getAvailableUpdate(UpdateUnit updateUnit, int idx) {
        List<UpdateElement> available = updateUnit.getAvailableUpdates();
        assertTrue(available.size() > idx);
        return available.get(idx);

    }
}
