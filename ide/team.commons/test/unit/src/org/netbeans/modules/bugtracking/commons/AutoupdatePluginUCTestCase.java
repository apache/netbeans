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

package org.netbeans.modules.bugtracking.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public abstract class AutoupdatePluginUCTestCase extends NbTestCase {
    
    protected static File catalogFile;
    protected static URL catalogURL;
    
    public AutoupdatePluginUCTestCase(String testName) {
        super(testName);
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider.class)
    public static class MyProvider extends AutoupdateCatalogProvider {
        static MyProvider instance;
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", catalogURL, UpdateUnitProvider.CATEGORY.STANDARD);
            instance = this;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir ();
        catalogFile = new File(getWorkDir(), "updates.xml");
        if (!catalogFile.exists()) {
            catalogFile.createNewFile();
        }
        catalogURL = catalogFile.toURI().toURL();
        
        setUserDir (getWorkDir().getAbsolutePath());
//        MockLookup.setInstances(new MyProvider());
        MockLookup.setLayersAndInstances();
//        MainLookup.register();
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        if(MyProvider.instance != null) {
            MyProvider.instance.setUpdateCenterURL(catalogURL);
        }
    }

    public static void setUserDir(String path) {
        System.setProperty ("netbeans.user", path);
    }
    
    public void testNewAvailable() throws Throwable {
        String contents = MessageFormat.format(getContentFormat(), getCNB(), "999.9.9", "999.9.9");
        populateCatalog(contents);

        assertNotNull(getAutoupdateSupport().checkNewPluginAvailable());
    }

    public void testNewNotAvailable() throws Throwable {
        String contents = MessageFormat.format(getContentFormat(), getCNB(), "0.0.0", "0.0.0");
        populateCatalog(contents);

        assertNull(getAutoupdateSupport().checkNewPluginAvailable());
    }

    public void testIsNotAtUCAvailable() throws Throwable {
        String contents = MessageFormat.format(getContentFormat(), "org.netbeans.modules.ketchup", "1.0.0", "1.0.0");
        populateCatalog(contents);

        assertNull(getAutoupdateSupport().checkNewPluginAvailable());
    }    

    protected abstract AutoupdateSupport getAutoupdateSupport();
    protected abstract String getContentFormat();
    protected abstract String getCNB();

    private void populateCatalog(String contents) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(catalogFile);
        try {
            os.write(contents.getBytes());
        } finally {
            os.close();
        }
        UpdateUnitProviderFactory.getDefault().refreshProviders (null, true);
    }
    
}
