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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateInfoParserTest extends DefaultTestCase {

    public AutoupdateInfoParserTest (String testName) {
        super (testName);
    }

    private static File NBM_FILE = null;
    private static final String LICENSE_NAME = "AD9FBBC9";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URL urlToFile = DefaultTestCase.class.getResource ("data/org-yourorghere-depending.nbm");
        NBM_FILE = Utilities.toFile(urlToFile.toURI ());
        assertNotNull ("data/org-yourorghere-depending.nbm file must found.", NBM_FILE);
    }

    public void testGetItems () throws IOException, SAXException {
        Map<String, UpdateItem> updateItems = AutoupdateInfoParser.getUpdateItems (NBM_FILE);
        assertNotNull ("UpdateItems found in " + NBM_FILE, updateItems);
        assertEquals ("Once item found.", 1, updateItems.keySet ().size ());
    }

    public void testLicense () throws IOException, SAXException {
        Map<String, UpdateItem> updateItems = AutoupdateInfoParser.getUpdateItems (NBM_FILE);
        assertNotNull ("org.yourorghere.depending_1.0 in map", updateItems.get ("org.yourorghere.depending_1.0"));
        UpdateItemImpl impl = Trampoline.SPI.impl (updateItems.get ("org.yourorghere.depending_1.0"));
        assertTrue ("UpdateItemImpl " + impl + " instanceof ModuleItem.", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertEquals (mi + " has license named " + LICENSE_NAME, LICENSE_NAME, mi.getUpdateLicenseImpl ().getName ());
        assertNotNull (mi + " has license.", mi.getAgreement ());
        assertFalse (mi + " has non-empty license.", mi.getAgreement ().length () == 0);
    }

}
