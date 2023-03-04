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

import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class FolderLookupBrokenListenersDontPreventQueriesTest extends NbTestCase implements LookupListener {
    private Lookup.Result<?> res;
    private FileObject fo;
    private int listenerVisited;

    public FolderLookupBrokenListenersDontPreventQueriesTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return null;// Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        fo = FileUtil.createFolder(FileUtil.getConfigRoot(), getName());
    }

    @Override
    protected void tearDown() throws Exception {
        synchronized (this) {
            // free the locked threads
            notifyAll();
        }
    }

    public void resultChanged(LookupEvent ev) {
        amIBrokenYesYouAre();
    }

    @RandomlyFails // NB-Core-Build #2892
    public void testIssue163315() throws Exception {
        FileObject ioe = FileUtil.createData(fo, "java-io-IOException.instance");
        FileObject iae = FileUtil.createData(fo, "java-lang-IllegalArgumentException.instance");

        @SuppressWarnings("deprecation")
        Lookup lkp = new FolderLookup(DataFolder.findFolder(fo)).getLookup();
        res = lkp.lookupResult(Exception.class);
        assertEquals("Two items found", 2, res.allInstances().size());
        res.addLookupListener(this);

        FileObject ise = FileUtil.createData(fo, "java-lang-IllegalStateException.instance");
        assertEquals("Three now", 3, res.allInstances().size());

        FileObject npe = FileUtil.createData(fo, "java-lang-NullPointerException.instance");
        assertEquals("Four now", 4, res.allInstances().size());

        if (listenerVisited == 0) {
            fail("Listener shall be notified at least once, well only once");
        }
    }

    private synchronized void amIBrokenYesYouAre() {
        listenerVisited++;
        try {
            // yes, you are broken: wait forever (almost)
            wait();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
