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
