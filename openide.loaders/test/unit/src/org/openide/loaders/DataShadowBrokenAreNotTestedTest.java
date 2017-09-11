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

package org.openide.loaders;

import java.net.URL;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Test that URL is not requested if there are no broken shadows.
 * @author Jaroslav Tulach
 */
public class DataShadowBrokenAreNotTestedTest extends NbTestCase {
    
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    public DataShadowBrokenAreNotTestedTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        
        FileObject[] delete = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }
        DataShadow.waitUpdatesProcessed();
        UM.init();
    }
    
    @RandomlyFails // NB-Core-Build #2009
    public void testQueriedWhenBrokenShadowsExists() throws Exception {
        
        //
        // Note: if anyone lowers the number of queries done here,
        // then go on, this test is here just to describe the current behaviour
        //
        
        
        FileObject f1 = FileUtil.createData(FileUtil.getConfigRoot(), getName() + "/folder/original.txt");
        assertNotNull(f1);
        FileObject f2 = FileUtil.createData(FileUtil.getConfigRoot(), getName() + "/any/folder/original.txt");
        assertNotNull(f2);
        
        UM.assertAccess("No queries to UM yet", 0, 0);
        DataObject original = DataObject.find(f1);
        UM.assertAccess("No queries to UM still", 0, 0);
        DataShadow s = original.createShadow(original.getFolder());
        UM.assertAccess("One query to create the shadow and one to create the instance", 1, 1);
        original.delete();
        UM.assertAccess("One additional query to delete", 2, 2);
        DataObject brokenShadow = DataObject.find(s.getPrimaryFile());
        UM.assertAccess("Creating one broken shadow", 2, 1);
        
        DataObject original2 = DataObject.find(f2);
        UM.assertAccess("Additional query per very data object creation", 1, 0);
    }
    
    private static final class UM extends URLMapper {
        private static int toURLCnt;
        private static int toFOCnt;
        private static RuntimeException lastAccess;

        static void init() {
            toFOCnt = 0;
            toURLCnt = 0;
            lastAccess = null;
        }
        
        @Override
        public URL getURL(FileObject fo, int type) {
            toURLCnt++;
            lastAccess = new RuntimeException("getURL " + toURLCnt);
            return null;
        }
        
        @Override
        public FileObject[] getFileObjects(URL url) {
            toFOCnt++;
            lastAccess = new RuntimeException("getFileObjects " + toFOCnt);
            return null;
        }
        public static void assertAccess(String msg, int expectURL, int expectFO) {
            try {
                DataShadow.waitUpdatesProcessed();
                assertEquals(msg + " file object check", expectFO, toFOCnt);
                assertEquals(msg + " to url check", expectURL, toURLCnt);
                toFOCnt = 0;
                toURLCnt = 0;
            } catch (AssertionFailedError ex) {
                if (lastAccess != null) ex.initCause(lastAccess);
                throw ex;
            }
        }
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[] {
                Lookups.singleton(new UM()),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
            });
        }
    }
    
}
