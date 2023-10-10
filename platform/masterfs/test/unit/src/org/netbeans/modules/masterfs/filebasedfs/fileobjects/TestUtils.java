/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

public class TestUtils {
    private static final Logger LOG = Logger.getLogger(TestUtils.class.getName());
    
    public static String getFileObjectPath (File f) {
        return f.getAbsolutePath().replace('\\','/');//NOI18N
    }

    public static void gcAll() {
        LOG.info("doing gcAll");
        List<Reference<Object>> refs = new ArrayList<Reference<Object>>();
        for (FileObjectFactory fbs : FileObjectFactory.getInstances()) {
            fbs.allIBaseLock.readLock().lock();
            try {
                for (Object obj : fbs.allIBaseFileObjects.values()) {
                    if (obj instanceof Reference<?>) {
                        refs.add((Reference<Object>) obj);
                    } else {
                        refs.addAll((List<Reference<Object>>) obj);
                    }
                }
            } finally {
                fbs.allIBaseLock.readLock().unlock();
            }
        }

        for (Reference<Object> ref : refs) {
            Object obj = ref.get();
            String s = obj == null ? "null" : obj.toString();
            obj = null;
            try {
                NbTestCase.assertGC("GCing " + s, ref);
                LOG.log(Level.INFO, "GCed {0}", s);
            } catch (AssertionFailedError afe) {
                LOG.log(Level.INFO, "Not GCed {0}", s);
            }
        }
        LOG.info("done gcAll");
    }

    public static void logAll() {
        LOG.info("all existing file objects");
        List<Reference<Object>> refs = new ArrayList<Reference<Object>>();
        for (FileObjectFactory fbs : FileObjectFactory.getInstances()) {
            fbs.allIBaseLock.readLock().lock();
            try {
                for (Object obj : fbs.allIBaseFileObjects.values()) {
                    if (obj instanceof Reference<?>) {
                        refs.add((Reference<Object>) obj);
                    } else {
                        refs.addAll((List<Reference<Object>>) obj);
                    }
                }
            } finally {
                fbs.allIBaseLock.readLock().unlock();
            }
        }

        for (Reference<Object> ref : refs) {
            Object obj = ref.get();
            if (obj != null) {
                LOG.log(Level.INFO, "Existing {0}", obj);
            }
        }
        LOG.info("end of file objects");
    }

    public static Runnable findSlowRefresh(FileObject fo) throws FileStateInvalidException {
        Object r = fo.getFileSystem().getRoot().getAttribute("refreshSlow");
        Assert.assertNotNull("Runnable for refreshSlow found", r);
        Assert.assertEquals("Right class", RefreshSlow.class, r.getClass());
        RefreshSlow rs = (RefreshSlow)r;
        Assert.assertTrue("Can only be used when proper property is set", Boolean.getBoolean("org.netbeans.modules.masterfs.watcher.disable"));
        Assert.assertFalse("Watcher is really disabled", Watcher.isEnabled());
        return rs;
    }
}
