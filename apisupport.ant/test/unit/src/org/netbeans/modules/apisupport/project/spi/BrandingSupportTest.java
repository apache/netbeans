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
                "org/netbeans/core/windows/view/ui/Bundle.properties", Collections.singleton("CTL_MainWindow_Title"), "NetBeans Platform {0}", instance.getBrandedBundleKeys());
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
