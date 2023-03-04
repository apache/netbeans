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
package org.netbeans.modules.editor.settings.storage.fontscolors;

import java.net.URL;
import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorSettingsImpl;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class CompositeFCSTest extends NbTestCase {

    public CompositeFCSTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }
    
    public void testColoringsForMimeType() throws Exception {
        final String mimeType = "text/x-orig";
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
        
        // Check the API class
        Collection<? extends FontColorSettings> c = lookup.lookupAll(FontColorSettings.class);
        assertEquals("Wrong number of fcs", 1, c.size());
        
        FontColorSettings fcs = c.iterator().next();
        assertNotNull("FCS should not be null", fcs);
        assertTrue("Wrong fcs impl", fcs instanceof CompositeFCS);
        
        CompositeFCS compositeFcs = (CompositeFCS) fcs;
        assertEquals("CompositeFCS using wrong profile", EditorSettingsImpl.DEFAULT_PROFILE, compositeFcs.profile);
    }

    public void testColoringsForSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
        
        // Check the API class
        Collection<? extends FontColorSettings> c = lookup.lookupAll(FontColorSettings.class);
        assertEquals("Wrong number of fcs", 1, c.size());
        
        FontColorSettings fcs = c.iterator().next();
        assertNotNull("FCS should not be null", fcs);
        assertTrue("Wrong fcs impl", fcs instanceof CompositeFCS);
        
        CompositeFCS compositeFcs = (CompositeFCS) fcs;
        assertEquals("CompositeFCS should be using special test profile", "test123456", compositeFcs.profile);
    }

}
