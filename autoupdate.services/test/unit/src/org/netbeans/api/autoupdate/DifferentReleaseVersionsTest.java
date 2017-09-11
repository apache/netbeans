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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author dlm198383
 */
public class DifferentReleaseVersionsTest extends NbTestCase {

    protected List<UpdateUnit> keepItNotToGC;
    private static File catalogFile;
    private static URL catalogURL;
    private File tmpDirectory;
    private List<File> nbms = new ArrayList<File>();
    private List<String> moduleElements = new ArrayList<String>();

    public DifferentReleaseVersionsTest(String testName) {
        super(testName);
    }

    public static class MyProvider extends AutoupdateCatalogProvider {

        public MyProvider() {
            super("test-updates-provider", "test-updates-provider", catalogURL, UpdateUnitProvider.CATEGORY.STANDARD);
        }
    }

    private String getModuleElement(boolean visible, String codeName, String releaseVersion, String implVersion, String moduleName, String distr, String specVersion, String dependency) {
        String releaseVersionAppendix = ((releaseVersion!=null /*&& Integer.parseInt(releaseVersion)!=0*/) ? ("/" + releaseVersion): "");
        return "\n<module " +
                "\n     codenamebase='" + codeName + "' " +
                "\n     distribution='" + distr + "' " +
                "\n     downloadsize='0' " +
                "\n     homepage='' " +
                "\n     license='AD9FBBC9' " +
                "\n     moduleauthor='' " +
                "\n     needsrestart='false' " +
                "\n     releasedate='2007/01/30'>" +
                "\n    <manifest " +
                "\n       AutoUpdate-Show-In-Client='" + visible + "' " +
                "\n       OpenIDE-Module='" + codeName + releaseVersionAppendix + "'" +
                "\n       OpenIDE-Module-Implementation-Version='" + (implVersion==null ? "070130": implVersion) + "' " +
                "\n       OpenIDE-Module-Java-Dependencies='Java &gt; 1.4' " +
                "\n       OpenIDE-Module-Name='" + moduleName + "' " +
                (dependency != null ? " OpenIDE-Module-Module-Dependencies=\"" + dependency.replace(">", "&gt;") + "\" " : "") +
                "\n       OpenIDE-Module-Requires='org.openide.modules.ModuleFormat1' " +
                "\n       OpenIDE-Module-Specification-Version='" + specVersion + "'/>" +
                "\n    <license name='AD9FBBC9'>[NO LICENSE SPECIFIED]" +
                "\n</license>" +
                "\n</module>\n";
    }

    private String createInfoXML(boolean visible, String codeName, String releaseVersion, String implVersion, String moduleName, String distr, String specVersion, String dependency) {
        String moduleElement = getModuleElement(visible, codeName, releaseVersion, implVersion, moduleName, distr, specVersion, dependency);

        moduleElements.add(moduleElement);
        return "<?xml version='1.0' encoding='UTF-8'?>" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Autoupdate Module Info 2.5//EN' 'http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd'>" +
                moduleElement;
    }

    private void writeCatalog() throws IOException {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">" +
                "<module_updates timestamp=\"00/00/19/08/03/2006\">\n";
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
        return "<?xml version='1.0' encoding='UTF-8'?>" +
                " <!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN'" +
                " 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>" +
                " <module name='" + codeName + "'>" +
                " <param name='autoload'>false</param>" +
                " <param name='eager'>false</param>" +
                " <param name='enabled'>true</param>" +
                " <param name='jar'>modules/" + moduleFile + ".jar</param>" +
                " <param name='reloadable'>false</param>" +
                " <param name='specversion'>" + specVersion + "</param>" +
                " </module>";
    }

    private String getManifest(String codeName, String releaseVersion, String implVersion, String moduleDir, String specVersion, boolean visible, String dependency) {
        String releaseVersionAppendix = ((releaseVersion!=null /*&& Integer.parseInt(releaseVersion)!=0*/) ? ("/" + releaseVersion): "");
        return "Manifest-Version: 1.0\n" +
                "Ant-Version: Apache Ant 1.7.0\n" +
                "Created-By: 1.6.0-b105 (Sun Microsystems Inc.)\n" +
                "OpenIDE-Module-Public-Packages: -\n" +
                "OpenIDE-Module-Java-Dependencies: Java > 1.4\n" +
                "OpenIDE-Module-Implementation-Version: " + (implVersion==null ? "070130": implVersion) +
                "\n" +
                (dependency != null ? ("OpenIDE-Module-Module-Dependencies: " + dependency + "\n") : "") +
                "OpenIDE-Module: " + codeName + releaseVersionAppendix +
                "\n" +
                "OpenIDE-Module-Localizing-Bundle: " + moduleDir + "Bundle.properties\n" +
                "OpenIDE-Module-Specification-Version: " + specVersion + "\n" +
                "OpenIDE-Module-Requires: org.openide.modules.ModuleFormat1\n" +
                "AutoUpdate-Show-In-Client: " + visible + "\n" +
                "\n";
    }

    private File prepareNBM(String codeName, String releaseVersion, String implVersion, String specVersion, boolean visible, String dependency) throws Exception {
        String moduleName = codeName.substring(codeName.lastIndexOf(".") + 1);
        String moduleFile = codeName.replace(".", "-");
        String moduleDir = codeName.replace(".", "/") + "/";
        File nbm = File.createTempFile(moduleFile + "-", ".nbm", tmpDirectory);

        final String MODULE_NAME_PROP = "OpenIDE-Module-Name";

        File jar = new File(tmpDirectory, "netbeans/modules/" + moduleFile + ".jar");
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


        jos.putNextEntry(new ZipEntry("netbeans/modules/" + moduleFile + ".jar"));

        FileInputStream fis = new FileInputStream(jar);
        FileUtil.copy(fis, jos);
        fis.close();
        jar.delete();
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

    private void doInstall(OperationContainer<InstallSupport> installContainer) throws OperationException{
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

    @RandomlyFails // NB-Core-Build #3718, and in manual tests
    public void testDifferentReleaseVersion() throws Exception {
        String parsingApiCNB   = "org.netbeans.modules.parsing.api";
        String scanOnDemandCNB = "org.netbeans.modules.scanondemand";
        String parsingApiReleaseVersion = "1";
        String parsingApiImplVersion    = "2";
        String parsingApiSpecVersion   = "1.0";
        String scanOnDemandSpecVersion = "1.0";
        prepareNBM(parsingApiCNB, parsingApiReleaseVersion, parsingApiImplVersion, parsingApiSpecVersion, false, null);

        writeCatalog();
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit parsingApiUnit = getUpdateUnit(parsingApiCNB);
        assertNull("cannot be installed", parsingApiUnit.getInstalled());
        UpdateElement parsingApiElement = getAvailableUpdate(parsingApiUnit, 0);
        assertEquals(parsingApiElement.getSpecificationVersion(), parsingApiSpecVersion);
        OperationInfo<InstallSupport> independentInfo = installContainer.add(parsingApiElement);
        assertNotNull(independentInfo);
        doInstall(installContainer);
        assertTrue("parsing.api module was not installed",  parsingApiUnit.getInstalled() != null);
        assertTrue("parsing.api module was not enabled",    parsingApiUnit.getInstalled().isEnabled());
        //have parsing.api installed at this moment


        prepareNBM(scanOnDemandCNB, null, null, scanOnDemandSpecVersion, true, parsingApiCNB + " = " + parsingApiImplVersion);

        writeCatalog();
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        UpdateUnit scanOnDemandUnit = getUpdateUnit(scanOnDemandCNB);
        assertEquals(1, scanOnDemandUnit.getAvailableUpdates().size());
        installContainer = OperationContainer.createForInstall();

        OperationInfo<InstallSupport> info = installContainer.add(scanOnDemandUnit.getAvailableUpdates().get(0));
        assertNotNull(info);

        Set <String> invalid = info.getBrokenDependencies();
        assertEquals("Should report problems since it is  not allowed to install " + scanOnDemandUnit +
                " due to difference between implicit release version of dependency(0) " +
                "and module release version (1)",
                invalid.size(), 1);
        doInstall(installContainer);

        assertTrue("scanondemand module was not installed", scanOnDemandUnit.getInstalled() != null);
        assertFalse("scanondemand module is enabled",   scanOnDemandUnit.getInstalled().isEnabled());
        /*
        prepareNBM(scanOnDemandCNB, null, null, scanOnDemandSpecVersion, true, parsingApiCNB + "/" + parsingApiReleaseVersion + " = " + parsingApiImplVersion);
        installContainer = OperationContainer.createForInstall();
        assertEquals(scanOnDemandUnit.getAvailableUpdates().size(), 1);
        
        info = installContainer.add(scanOnDemandUnit.getAvailableUpdates().get(0));
        assertNotNull(info);

        assertFalse("Installation of " + scanOnDemandUnit + " should be allowed", installContainer.listAll().isEmpty());
        assertTrue("No problems should be reported", installContainer.listInvalid().isEmpty());


        log("Will install... " + installContainer.listAll().get(0).getUpdateElement());
        doInstall(installContainer);
        assertTrue("Cannot install module with different release version set", scanOnDemandUnit.getInstalled() != null);
        assertTrue("Cannot enable module with different release version set", scanOnDemandUnit.getInstalled().isEnabled());
         */
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
