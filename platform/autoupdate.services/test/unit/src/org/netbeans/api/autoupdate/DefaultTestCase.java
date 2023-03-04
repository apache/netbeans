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
package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.TestUtils.CustomItemsProvider;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
public class DefaultTestCase extends NbTestCase {
    private static File catalogFile;
    private static URL catalogURL;
    protected boolean modulesOnly = true;
    protected List<UpdateUnit> keepItNotToGC;
    @SuppressWarnings("NonConstantLogger")
    protected final Logger LOG;
    
    public DefaultTestCase(String testName) {
        super(testName);
        LOG = Logger.getLogger("test." + testName);
    }
        
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", catalogURL, UpdateUnitProvider.CATEGORY.STANDARD);
        }
    }

    public void populateCatalog(InputStream is) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(catalogFile);
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
            os.close();
        }
    }
    
    protected InputStream updateCatalogContents() {
        return TestUtils.class.getResourceAsStream("data/updates.xml");
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir ();
        catalogFile = new File(getWorkDir(), "updates.xml");
        if (!catalogFile.exists()) {
            catalogFile.createNewFile();
        }
        catalogURL = org.openide.util.Utilities.toURI(catalogFile).toURL();
        populateCatalog(updateCatalogContents());
        
        TestUtils.setUserDir (getWorkDirPath ());
        TestUtils.testInit();
        
        MainLookup.register(new MyProvider());
        MainLookup.register(new CustomItemsProvider());
        MainLookup.register(new InstallIntoNewClusterTest.NetBeansClusterCreator());
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        assert Lookup.getDefault().lookup(CustomItemsProvider.class) != null;
        UpdateUnitProviderFactory.getDefault().refreshProviders (null, true);
        
        File pf = new File (new File (getWorkDir(), "platform"), "installdir");
        pf.mkdirs ();
        new File (pf, "config").mkdir();
        TestUtils.setPlatformDir (pf.toString ());
        if (modulesOnly) {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        } else {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        }
            
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
