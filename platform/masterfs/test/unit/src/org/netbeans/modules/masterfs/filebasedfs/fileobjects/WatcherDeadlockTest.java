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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessorTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WatcherDeadlockTest extends NbTestCase {

    public WatcherDeadlockTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 5000;
    }

    public void testDeadlockWhileRefesh() throws IOException {
        clearWorkDir();
        
        MockServices.setServices(Watcher.class, AnnotationProviderImpl.class);
        final File root = new File(getWorkDir(), "root");
        
        File f = new File(new File(new File(root, "x"), "y"), "z");
        f.mkdirs();
        final FileObject r = FileUtil.toFileObject(root);
        r.refresh(true);
        
        Set<FileObject> all = new HashSet<FileObject>();
        Enumeration<? extends FileObject> en = r.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = en.nextElement();
            all.add(fileObject);
        }
        assertEquals("Some files: " + all, 3, all.size());
        
        FileChangeListener l = new FileChangeAdapter();
        FileUtil.addRecursiveListener(l, root);
        
        FileChangeListener l2 = new FileChangeAdapter();
        FileUtil.addRecursiveListener(l2, root);
    }

    public static class AnnotationProviderImpl extends BaseAnnotationProvider  {
        private ProvidedExtensionsImpl impl = new ProvidedExtensionsImpl();
        @Override
        public InterceptionListener getInterceptionListener() {
            return impl;
        }

        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            return name;
        }


        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            return name;
        }
    }
    
    private static class ProvidedExtensionsImpl extends ProvidedExtensions implements Runnable {
        private static final RequestProcessor RP = new RequestProcessor("refresh me");
        private ThreadLocal<Boolean> STOP = new ThreadLocal<Boolean>();
        private FileObjectFactory fact;
        @Override
        public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            fact = FileObjectFactory.getInstance(dir);
            BaseFileObj obj = fact.getValidFileObject(dir, FileObjectFactory.Caller.Others, true);
            assertNotNull("Obj found", obj);
            Object prev = STOP.get();
            if (prev == null) try {
                STOP.set(Boolean.TRUE);
                RP.post(this).waitFinished();
            } finally {
                STOP.set(null);
            }
            return -1;
        }
        
        @Override
        public void run() {
            fact.refresh(null, true, true);
        }
    }
}