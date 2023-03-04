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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 * @author Jaroslav Tulach
 */
public class FileUtilAddRecursiveListenerOnClassFileTest extends NbTestCase {
    static {
//        MockServices.setServices(ProvidedExtensionsTest.AnnotationProviderImpl.class);
    }

    private final Logger LOG;
    private File classFile;
    private FileObject root;

    public FileUtilAddRecursiveListenerOnClassFileTest(String name) {
        super(name);
        LOG = Logger.getLogger("TEST." + name);
    }

    @Override
    protected void setUp() throws Exception {
        System.getProperties().put("org.netbeans.modules.masterfs.watcher.disable", "true");
        clearWorkDir();
        
        
        classFile = new File(new File(new File(new File(getWorkDir(), "build"), "pkg"), "subpkg"), "My.class");
        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("File Object exists", root);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    public void testAddListenerCreateAndDelete() throws IOException {
        L listener = new L();
        FileUtil.addFileChangeListener(listener, classFile);
        
        classFile.getParentFile().mkdirs();
        classFile.createNewFile();
        
        root.refresh();
        
        listener.assertEvent("fileDataCreated", "My.class");
        
        FileObject myClass = FileUtil.toFileObject(classFile);
        assertNotNull("Class file object found", myClass);
        
        LOG.info("Renaming from build to simulate delete");
        boolean res = new File(getWorkDir(), "build").renameTo(new File(getWorkDir(), "build.old"));
        LOG.log(Level.INFO, "Rename succeeded: {0}", res);
        
        root.refresh();
        
        assertFalse("Class file no longer valid", myClass.isValid());
        
        listener.assertEvent("fileDeleted", "My.class");
    }
    
    private static final class L implements FileChangeListener {
        List<FileEvent> ev = new ArrayList<FileEvent>();
        List<String> msgs = new ArrayList<String>();

        @Override
        public void fileFolderCreated(FileEvent fe) {
            ev.add(fe);
            msgs.add("fileFolderCreated");
            msgs.add(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            ev.add(fe);
            msgs.add("fileDataCreated");
            msgs.add(fe.getFile().getNameExt());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            ev.add(fe);
            msgs.add("fileChanged");
            msgs.add(fe.getFile().getNameExt());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            ev.add(fe);
            msgs.add("fileDeleted");
            msgs.add(fe.getFile().getNameExt());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            ev.add(fe);
            msgs.add("fileRenamed");
            msgs.add(fe.getFile().getNameExt());
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            ev.add(fe);
            msgs.add("fileAttributeChanged");
            msgs.add(fe.getFile().getNameExt());
        }

        private void assertEvent(String... messages) {
            List<String> exp = Arrays.asList(messages);
            if (!exp.equals(msgs)) {
                fail("Messages are different. Expected:\n" + exp + "\nReceived:\n" + msgs + "\n\nDetails:\n" + ev);
            }
            ev.clear();
            msgs.clear();
        }
    }
}
