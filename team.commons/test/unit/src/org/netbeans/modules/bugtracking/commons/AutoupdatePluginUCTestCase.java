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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
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
