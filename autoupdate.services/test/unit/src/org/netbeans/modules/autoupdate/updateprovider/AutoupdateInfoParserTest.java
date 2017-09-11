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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
        URL urlToFile = AutoupdateInfoParserTest.class.getResource ("data/org-yourorghere-depending.nbm");
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
