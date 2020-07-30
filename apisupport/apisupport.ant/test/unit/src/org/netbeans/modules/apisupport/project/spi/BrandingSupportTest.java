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

package org.netbeans.modules.apisupport.project.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteBrandingModel;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.customizer.SuitePropertiesTest;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Radek Matous
 */
public class BrandingSupportTest extends TestBase {
    private BrandingSupport instance = null;
    
    public BrandingSupportTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File suiteDir  = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        FileObject fo = FileUtil.toFileObject(suiteDir);
        SuiteProject suitePrj = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(suitePrj);

        BrandingModel model = new SuiteBrandingModel(SuitePropertiesTest.getSuiteProperties(suitePrj));
        model.init();
        instance = model.createBranding();
        instance.init();
    }
    
    public void testBranding1() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        implOfBundleKeyTest("org.netbeans.core.startup",
                "org/netbeans/core/startup/Bundle.properties", Collections.singleton("CTL_About_Title"), "About", instance.getBrandedBundleKeys());
    }
    
    public void testBranding2() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        implOfBundleKeyTest("org.netbeans.core.startup", null, Collections.singleton("CTL_About_Title"), "About", instance.getBrandedBundleKeys());
    }
    
    
    public void testBranding3() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        implOfBundleKeyTest("org.netbeans.core",
                "org/netbeans/core/ui/Bundle.properties", Collections.singleton("LBL_SwingBrowserDescription"), "Simple HTML Browser based on a Swing component", instance.getBrandedBundleKeys());
    }

    public void testBranding4() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        implOfBundleKeyTest("org.netbeans.core.windows",
                "org/netbeans/core/windows/view/ui/Bundle.properties", Collections.singleton("CTL_MainWindow_Title"), "Apache NetBeans Platform {0}", instance.getBrandedBundleKeys());
    }
    
    public void testBrandingFile() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        assertNotNull(instance.getBrandedFiles());
        assertEquals(0,instance.getBrandedFiles().size());
        BrandingSupport.BrandedFile bFile =
                instance.getBrandedFile("org.netbeans.core.startup","org/netbeans/core/startup/splash.gif");
        
        BrandingSupport.BrandedFile bFile2 =
                instance.getBrandedFile("org.netbeans.core.startup","org/netbeans/core/startup/splash.gif");
        
        assertEquals(bFile2, bFile);
        assertEquals(bFile2.getBrandingSource(), bFile.getBrandingSource());
        assertFalse(bFile.isModified());        
        
        assertNotNull(bFile);
        assertEquals(0,instance.getBrandedFiles().size());
        assertFalse(instance.isBranded(bFile));
        instance.brandFile(bFile);
        assertFalse(bFile.isModified());        
        
        assertFalse(instance.isBranded(bFile));
        assertEquals(0,instance.getBrandedFiles().size());
        
        File newSource = createNewSource(bFile);
        assertEquals(0,instance.getBrandedFiles().size());
        
        bFile.setBrandingSource(Utilities.toURI(newSource).toURL());
        assertTrue(bFile.isModified());        
        
        assertEquals(0,instance.getBrandedFiles().size());
        instance.brandFile(bFile);
        assertFalse(bFile.isModified());        
        
        
        assertEquals(1,instance.getBrandedFiles().size());
        assertTrue(instance.isBranded(bFile));
        assertEquals(bFile2, bFile);
        assertFalse(bFile2.getBrandingSource().equals(bFile.getBrandingSource()));

        
        
    }
    
    private File createNewSource(final BrandingSupport.BrandedFile bFile) throws MalformedURLException, FileNotFoundException, IOException {
        OutputStream os = null;
        InputStream is = null;
        File newSource = new File(getWorkDir(),"newSource.gif");
        
        try {
            
            os = new FileOutputStream(newSource);
            is = bFile.getBrandingSource().openStream();
            FileUtil.copy(is,os);
        } finally  {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
        return newSource;
    }
    
    
    private void implOfBundleKeyTest(final String moduleCodeNameBase, final String bundleEntry, final Set<String> keys, String expectedValue, Set<BrandingSupport.BundleKey> bundleKeys) throws IOException {
        Set<BrandingSupport.BundleKey> bKeys;
        if (bundleEntry != null) {
            bKeys= instance.getBundleKeys(moduleCodeNameBase,bundleEntry,keys, bundleKeys);
        } else {
            bKeys= instance.getLocalizingBundleKeys(moduleCodeNameBase,keys);
        }
        
        assertNotNull(bKeys);
        assertEquals(1, bKeys.size());
        
        BrandingSupport.BundleKey bKey = bKeys.iterator().next();
        assertFalse(instance.isBranded(bKey));
        assertFalse(instance.isBranded(bKey.getModuleEntry()));
        assertFalse(instance.getBrandingRoot().exists());
        assertFalse(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertFalse(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals(expectedValue, bKey.getValue());
        
        instance.brandBundleKeys(bKeys);
        assertFalse(instance.isBranded(bKey));
        assertFalse(instance.isBranded(bKey.getModuleEntry()));
        assertFalse(instance.getBrandingRoot().exists());
        assertFalse(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertFalse(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals(expectedValue, bKey.getValue());
        assertFalse(bKey.isModified());        
        
        bKey.setValue("brandedValue");
        assertTrue(bKey.isModified());                
        instance.brandBundleKeys(bKeys);
        assertFalse(bKey.isModified());        
        
        assertTrue(instance.isBranded(bKey));
        assertTrue(instance.isBranded(bKey.getModuleEntry()));
        assertTrue(instance.getBrandingRoot().exists());
        assertTrue(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertTrue(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals("brandedValue", bKey.getValue());
        
    }
    
}
