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
package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
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
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Dmitry Lipin
 */
public class InstallOSGiBundleTest extends NbTestCase {
    private static File catalogFile;
    private static URL catalogURL;
    private File tmpDirectory;

    public InstallOSGiBundleTest(String testName) {
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

    private void writeCatalog(String[] moduleElements) throws IOException {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\">"
                + "<module_updates timestamp=\"00/00/19/08/03/2006\">\n";
        for (String element : moduleElements) {
            res += element;
        }
        res += "</module_updates>\n";
        if (catalogFile == null) {
            catalogFile = File.createTempFile("catalog-", ".xml", tmpDirectory);
            catalogURL = Utilities.toURI(catalogFile).toURL();
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(catalogFile), StandardCharsets.UTF_8));
        pw.write(res);
        pw.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        tmpDirectory = new File(getWorkDirPath(), "tmp");
        tmpDirectory.mkdirs();

        writeCatalog(new String[0]);

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
            Thread.sleep(1000);
            r = support.doInstall(i, null);
        } catch (InterruptedException ex) {
            fail("Interrupted: " + ex.getMessage());
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

    private File generateJar(File f, String[] content, Manifest manifest) throws IOException {
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), manifest);

        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry(content[i]));
            os.closeEntry();
        }
        os.closeEntry();
        os.close();

        return f;
    }

    @RandomlyFails // NB-Core-Build #4187: module was not enabled after installation from OSGi bundle
    public void testOSGi() throws Exception {
        String moduleCNB = "org.netbeans.modules.mymodule";
        String moduleFile = moduleCNB.replace(".", "-");
        File osgi = new File(tmpDirectory, moduleFile + ".jar");
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Bundle-SymbolicName", moduleCNB);
        mf.getMainAttributes().putValue("Bundle-Version", "0");
        
        generateJar(osgi, new String[0], mf);
        String osgiModuleInfo = "<module codenamebase='" + moduleCNB
                + "' distribution='" + Utilities.toURI(osgi).toURL()
                + "' downloadsize='" + osgi.length()
                + "'>"
                + "<manifest "
                + "OpenIDE-Module='" + moduleCNB
                + "' OpenIDE-Module-Display-Category='hello' "
                + "OpenIDE-Module-Name='" + moduleCNB
                + "' OpenIDE-Module-Short-Description='Hello there!' "
                + "OpenIDE-Module-Specification-Version='0'/> "
                + "</module> ";
        writeCatalog(new String[]{osgiModuleInfo});

        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit moduleUnit = getUpdateUnit(moduleCNB);
        assertNull("cannot be installed", moduleUnit.getInstalled());
        UpdateElement moduleElement = getAvailableUpdate(moduleUnit, 0);
        assertEquals(moduleElement.getSpecificationVersion(), "0");
        OperationInfo<InstallSupport> independentInfo = installContainer.add(moduleElement);
        assertNotNull(independentInfo);
        doInstall(installContainer);
        assertTrue("module was not installed from OSGi bundle", moduleUnit.getInstalled() != null);
        assertTrue("module was not enabled after installation from OSGi bundle", moduleUnit.getInstalled().isEnabled());
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
