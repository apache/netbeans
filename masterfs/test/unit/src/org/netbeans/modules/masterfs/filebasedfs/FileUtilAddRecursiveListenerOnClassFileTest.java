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
