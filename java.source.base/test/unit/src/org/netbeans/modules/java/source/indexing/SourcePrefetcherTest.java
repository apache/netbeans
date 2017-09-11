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
package org.netbeans.modules.java.source.indexing;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.PrefetchableJavaFileObject;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePrefetcherTest extends NbTestCase {
    
    private static final int FILE_COUNT = 100;
    
    private Collection<? extends CompileTuple> files;
    
    public SourcePrefetcherTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wdFo = FileUtil.toFileObject(getWorkDir());
        final FileObject srcFolder = FileUtil.createFolder(wdFo, "src");        //NOI18N
        final Deque<CompileTuple> q = new ArrayDeque<CompileTuple>();
        for (int i=0; i<FILE_COUNT; i++) {
            final FileObject file = FileUtil.createData(srcFolder, String.format("file%d.txt",i)); //NOI18N
            final Indexable indexable = SPIAccessor.getInstance().create(new FileObjectIndexable(srcFolder, file));
            final PrefetchableJavaFileObject jfo = FileObjects.fileFileObject(FileUtil.toFile(file), FileUtil.toFile(srcFolder), null, null);
            q.offer(new CompileTuple(jfo,indexable));
        }
        files = q;
    }

    public void testWrongCallOrder() throws Exception {
        SourcePrefetcher pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
        while (pf.hasNext()) {
            pf.next();
            pf.remove();
        }
        pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
        try {
            while (pf.hasNext()) {
                pf.next();
            }
            assertTrue("Next should fail if no remove",false);
        } catch (IllegalStateException ise) {
        }
        pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
        try {
            while (pf.hasNext()) {
                pf.remove();
            }
            assertTrue("Remove should fail if no next",false);
        } catch (IllegalStateException ise) {
        }
    }
    
    public void testSeqPref() throws Exception {
        JavaIndexerWorker.TEST_DO_PREFETCH = false;
        final LogHandler handler = new LogHandler();
        handler.expect("Using sequential iterator");    //NOI18N
        final Logger log = Logger.getLogger(JavaIndexerWorker.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        try {
            SourcePrefetcher pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
            assertTrue(handler.isFound());
            final Deque<CompileTuple> got = new ArrayDeque<CompileTuple>(FILE_COUNT);
            while (pf.hasNext()) {
                final CompileTuple ct = pf.next();
                assertNull(getCache(ct.jfo));
                got.offer(ct);
                pf.remove();
            }
            assertCollectionsEqual(files,got);
        } finally {
            log.removeHandler(handler);
        }
    }
    
    public void testConPref() throws Exception {
        JavaIndexerWorker.TEST_DO_PREFETCH = true;
        final LogHandler handler = new LogHandler();
        handler.expect("Using concurrent iterator, {0} workers");    //NOI18N
        final Logger log = Logger.getLogger(JavaIndexerWorker.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        try {
            SourcePrefetcher pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
            assertTrue(handler.isFound());
            final Deque<CompileTuple> got = new ArrayDeque<CompileTuple>(FILE_COUNT);
            while (pf.hasNext()) {
                final CompileTuple ct = pf.next();
                assertNotNull(getCache(ct.jfo));
                got.offer(ct);
                pf.remove();
                assertNull(getCache(ct.jfo));
            }
            assertCollectionsEqual(files,got);
        } finally {
            log.removeHandler(handler);
        }
    }
    
    public void testDeadlock208663() throws Exception {
        CompileTuple ct = files.iterator().next();
        final FileObject fo = URLMapper.findFileObject(ct.indexable.getURL());
        final FileLock lck = fo.lock();
        try {
            final OutputStream out = new BufferedOutputStream(fo.getOutputStream(lck));
            try {
                for (int i = 0; i<101; i++) {
                    out.write('a');
                }
            } finally {
                out.close();
            }
        } finally {
            lck.releaseLock();
        }
        
        JavaIndexerWorker.TEST_DO_PREFETCH = true;
        JavaIndexerWorker.BUFFER_SIZE = 100;
        final LogHandler handler = new LogHandler();
        handler.expect("Using concurrent iterator, {0} workers");    //NOI18N
        final Logger log = Logger.getLogger(JavaIndexerWorker.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        try {
            SourcePrefetcher pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
            assertTrue(handler.isFound());
            final Deque<CompileTuple> got = new ArrayDeque<CompileTuple>(FILE_COUNT);
            while (pf.hasNext()) {
                ct = pf.next();
                assertNotNull(getCache(ct.jfo));
                got.offer(ct);
                pf.remove();
                assertNull(getCache(ct.jfo));
            }
            assertCollectionsEqual(files,got);
        } finally {
            log.removeHandler(handler);
        }
    }
    
    public void testDeletedFile() throws Exception {
        CompileTuple ct = files.iterator().next();
        final FileObject fo = URLMapper.findFileObject(ct.indexable.getURL());        
        fo.delete();
        
        JavaIndexerWorker.TEST_DO_PREFETCH = true;
        final LogHandler handler = new LogHandler();
        handler.expect("Using concurrent iterator, {0} workers");    //NOI18N
        final Logger log = Logger.getLogger(JavaIndexerWorker.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        try {
            SourcePrefetcher pf = SourcePrefetcher.create(files, SuspendSupport.NOP);
            assertTrue(handler.isFound());
            final Deque<CompileTuple> got = new ArrayDeque<CompileTuple>(FILE_COUNT);
            while (pf.hasNext()) {
                ct = pf.next();
                try {
                    if (ct != null) {
                        assertNotNull(getCache(ct.jfo));
                        got.offer(ct);
                    }
                } finally {
                    pf.remove();
                }
                if (ct != null) {
                    assertNull(getCache(ct.jfo));
                }
            }
            final List<CompileTuple> allButFirst = new LinkedList<CompileTuple>(files);
            allButFirst.remove(0);
            assertCollectionsEqual(allButFirst,got);
        } finally {
            log.removeHandler(handler);
        }
    }
    
    private <T> void assertCollectionsEqual(final Collection<? extends T> expected,
            final Collection<? extends T> got) {
        assertEquals(expected.size(), got.size());
        final HashSet<T> es = new HashSet<T>(expected);
        final HashSet<T> gt = new HashSet<T>(got);
        assertEquals(es, gt);
    }
    
    private CharSequence getCache(final PrefetchableJavaFileObject fo) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        if (!(fo instanceof FileObjects.FileBase)) {
            throw new IllegalArgumentException(fo.getClass().getName());
        }
        final Class<?> c = fo.getClass().getSuperclass();
        final Field f = c.getDeclaredField("data"); //NOI18N
        f.setAccessible(true);
        return (CharSequence) f.get(fo);
    }
    
    private final class LogHandler extends Handler {
        
        String expectedMessage;
        boolean found;
        
        void expect (final String msg) {
            expectedMessage = msg;
            found = false;
        }
        
        boolean isFound() {
            return found;
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null && msg.equals(expectedMessage)) {
                found = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
   
}
