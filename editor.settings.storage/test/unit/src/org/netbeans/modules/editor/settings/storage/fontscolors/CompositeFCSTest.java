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
