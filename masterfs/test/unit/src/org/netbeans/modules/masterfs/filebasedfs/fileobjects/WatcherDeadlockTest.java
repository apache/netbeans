/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            BaseFileObj obj = fact.getValidFileObject(dir, FileObjectFactory.Caller.Others);
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