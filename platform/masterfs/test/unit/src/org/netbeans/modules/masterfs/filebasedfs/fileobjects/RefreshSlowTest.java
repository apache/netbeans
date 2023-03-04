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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class RefreshSlowTest extends  NbTestCase {
    public RefreshSlowTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public void testByDefaultTheRefreshIgnoresRecListeners() throws IOException {
        Logger LOG = Logger.getLogger("test." + getName());
        if (!Watcher.isEnabled()) {
            LOG.warning("Have to skip the test, as native watching is disabled");
            LOG.log(Level.WARNING, "os.name: {0} os.version: {1} os.arch: {2}", new Object[] {
                    System.getProperty("os.name"), 
                    System.getProperty("os.version"), 
                    System.getProperty("os.arch")
            });
            return;
        }
        
        File d = new File(new File(getWorkDir(), "dir"), "subdir");
        d.mkdirs();
        
        FileChangeAdapter ad = new FileChangeAdapter();
        
        FileUtil.addRecursiveListener(ad, getWorkDir());

        final FileObject fo = FileUtil.toFileObject(getWorkDir());
        Runnable r = (Runnable) fo.getFileSystem().getRoot().getAttribute("refreshSlow");
        final int cnt[] = { 0 };
        ActionEvent ae = new ActionEvent(this, 0, "") {
            @Override
            public void setSource(Object newSource) {
                assertTrue(newSource instanceof Object[]);
                Object[] arr = (Object[]) newSource;
                assertTrue("Three elements at least ", 3 <= arr.length);
                assertTrue("3rd is fileobject", arr[2] instanceof FileObject);
                FileObject checked = (FileObject) arr[2];
                assertFalse(checked + " shall not be a children of " + fo, FileUtil.isParentOf(fo, checked));
                super.setSource(newSource);
                cnt[0]++;
                fail("" + checked +"\n" + fo);
            }
        };
        
        r.equals(ae);
        r.run();
        
        assertEquals("No calls to refresh", 0, cnt[0]);
    }

}