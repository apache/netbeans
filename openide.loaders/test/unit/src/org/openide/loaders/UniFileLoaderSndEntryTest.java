/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** Teasing UniFileLoader with recognizing entry during move operation.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UniFileLoaderSndEntryTest extends NbTestCase {
    private FileObject two;
    private FileObject one;
    private static FileObject data;
    
    public UniFileLoaderSndEntryTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(Pool.class);
        
        clearWorkDir();
        File tw = new File(new File(getWorkDir(), "one"), "two");
        tw.mkdirs();
        
        this.one = FileUtil.toFileObject(tw.getParentFile());
        this.two = FileUtil.toFileObject(tw);
        this.data = one.createData("I.knowYou");
    }
    
    public void testMoveAndQueryInMiddle() throws Throwable {
        DataFolder fTwo = DataFolder.findFolder(two);
        DataObject obj = DataObject.find(data);
        assertEquals(CntrlUniLoader.class, obj.getLoader().getClass());
        
        obj.move(fTwo);
        
        assertEquals("Parent has changed", fTwo, obj.getFolder());
        
        assertNotNull("Teaser recognizer started", Teaser.task);
        Teaser.task.waitFinished();
        
        assertNotNull("Data object found by teaser thread", Teaser.obj);
        if (Teaser.obj instanceof DataObjectNotFoundException) {
            return;
        }
        if (Teaser.obj instanceof Throwable) {
            throw (Throwable)Teaser.obj;
        } else {
            fail("Surprising content of obj: " + Teaser.obj);
        }
    }

    public static final class Pool extends DataLoaderPool {
        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(CntrlUniLoader.me());
        }
    }
    
    private static class Teaser implements Runnable {
        private static CountDownLatch WAIT;
        private static CountDownLatch WAITING;
        
        private static Object obj;
        private static Task task;
        
        @Override
        public void run() {
            try {
                obj = DataObject.find(data);
            } catch (Throwable ex) {
                obj = ex;
                // OK, finish the waiting
                WAITING.countDown();
                WAIT.countDown();
            }
        }
    }
    
    
    public static final class CntrlUniLoader extends UniFileLoader {
        
        public CntrlUniLoader() {
            super(MultiDataObject.class.getName());
        }
        
        public static CntrlUniLoader me() {
            return getLoader(CntrlUniLoader.class);
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            if (Teaser.WAIT != null) {
                Teaser.WAITING.countDown();
                try {
                    Teaser.WAIT.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return fo.hasExt("knowYou") ? fo : null;
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) 
        throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile) {
                @Override
                public FileObject move(FileObject f, String suffix) throws IOException {
                    try {
                        Teaser.WAIT = new CountDownLatch(1);
                        Teaser.WAITING = new CountDownLatch(1);
                        FileObject fo;
                        synchronized (DataObjectPool.getPOOL()) {
                            Teaser.task = RequestProcessor.getDefault().post(new Teaser());
                            Teaser.WAITING.await(300, TimeUnit.MILLISECONDS);
                            fo = super.move(f, suffix); 
                        }
                        Teaser.WAITING.await();
                        Teaser.WAIT.countDown();
                        return fo;
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                
            };
        }
        
        
    }
}
