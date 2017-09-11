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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.SAXException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jirka
 */
public class CatalogCacheTest extends NbTestCase {
    
    public CatalogCacheTest (String testName) {
        super (testName);
    }
    
    private URL URL_TO_TEST_CATALOG = null;
    private AutoupdateCatalogCache cache = null;
        
    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        super.setUp ();
        
        URL_TO_TEST_CATALOG = this.getClass ().getResource ("data/catalog.xml");
        System.setProperty ("netbeans.user", getWorkDirPath ());
        cache = AutoupdateCatalogCache.getDefault ();
    }
    
    public void testWriteToCache () throws IOException {
        URL catalogInCache = cache.writeCatalogToCache ("test-catalog", URL_TO_TEST_CATALOG);
        assertNotNull ("Cache exists!", catalogInCache);
    }
    
    public void testCompareOriginalAndCache () throws IOException, SAXException {
        assertEquals ("Number of items is same in both places.",
                AutoupdateCatalogParser.getUpdateItems(URL_TO_TEST_CATALOG, null).size (),
                AutoupdateCatalogParser.getUpdateItems (cache.writeCatalogToCache("test-catalog", URL_TO_TEST_CATALOG), null).size ());
    }
    
    public void testGetCatalogURL () throws IOException {
        URL stored1 = cache.writeCatalogToCache ("test-1-catalog", URL_TO_TEST_CATALOG);
        URL stored2 = cache.writeCatalogToCache ("test-2-catalog", URL_TO_TEST_CATALOG);
        assertNotNull (stored1);
        assertNotNull (stored2);
        assertEquals ("Get catalog URL as same as stored", stored1, cache.getCatalogURL ("test-1-catalog"));
        assertEquals ("Get catalog URL as same as stored", stored2, cache.getCatalogURL ("test-2-catalog"));
        assertFalse ("Stored URLs of two cache cannot be same.", stored2.equals(stored1));
        assertFalse ("Stored URLs of two cache cannot be same.", cache.getCatalogURL ("test-2-catalog").equals(cache.getCatalogURL ("test-1-catalog")));
    }
    
}
