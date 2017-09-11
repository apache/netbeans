/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.PersistentClassIndex;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.lucene.LuceneIndex;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcherAccessor;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class JavaBinaryIndexerTest  extends NbTestCase {

    private JavaBinaryIndexer.Factory factory;
    private URL me;
    private FileObject cache;
    private Logger luceneLogger;
    private Level luceneLogLevel;


    public JavaBinaryIndexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File workDir = getWorkDir();
        CacheFolder.setCacheFolder(FileUtil.createFolder(FileUtil.normalizeFile(new File(workDir, "cache")))); //NOI18N
        factory = new JavaBinaryIndexer.Factory();
        me = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        cache = CacheFolder.getDataFolder(me);
        luceneLogger = Logger.getLogger(LuceneIndex.class.getName());
        luceneLogLevel = luceneLogger.getLevel();
        luceneLogger.setLevel(Level.FINE);

    }

    @Override
    protected void tearDown() throws Exception {
        LowMemoryWatcherAccessor.setLowMemory(false);
        final TransactionContext ctx = TransactionContext.beginStandardTransaction(me, false, false, false);
        try {
            ClassIndexManager.getDefault().removeRoot(me);
        } finally {
            ctx.commit();
        }
        factory = null;
        cache = null;
        me = null;
        luceneLogger.setLevel(luceneLogLevel);
        luceneLogLevel = null;
        luceneLogger = null;
        super.tearDown();
    }

    @Test
    public void testIssue251735_batch() throws Exception {
        assertNotNull(me);
        assertNotNull(cache);
        Context ctx = SPIAccessor.getInstance().createContext(
            cache,
            me,
            JavaIndex.NAME,
            JavaIndex.VERSION,
            LuceneIndexFactory.getDefault(),
            false,
            false,
            false,
            SuspendSupport.NOP,
            null,
            null);
        SPIAccessor.getInstance().setAllFilesJob(ctx, true);
        try {
            factory.scanStarted(ctx);
            SPIAccessor.getInstance().index(factory.createIndexer(), ctx);
        } finally {
            factory.scanFinished(ctx);
        }
        ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(me, false);
        assertNotNull(uq);
        final Field indexFld = PersistentClassIndex.class.getDeclaredField("index");    //NOI18N
        indexFld.setAccessible(true);
        final LuceneIndex index = (LuceneIndex) indexFld.get(uq);
        assertNotNull(index);
        assertValidIndex(index);
    }

    @Test
    public void testIssue251735_perpartes() throws Exception {
        assertNotNull(me);
        assertNotNull(cache);
        Context ctx = SPIAccessor.getInstance().createContext(
            cache,
            me,
            JavaIndex.NAME,
            JavaIndex.VERSION,
            LuceneIndexFactory.getDefault(),
            false,
            false,
            false,
            SuspendSupport.NOP,
            null,
            null);
        LowMemoryWatcherAccessor.setLowMemory(true);
        SPIAccessor.getInstance().setAllFilesJob(ctx, true);
        try {
            factory.scanStarted(ctx);
            SPIAccessor.getInstance().index(factory.createIndexer(), ctx);
        } finally {
            factory.scanFinished(ctx);
        }
        ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(me, false);
        assertNotNull(uq);
        final Field indexFld = PersistentClassIndex.class.getDeclaredField("index");    //NOI18N
        indexFld.setAccessible(true);
        final LuceneIndex index = (LuceneIndex) indexFld.get(uq);
        assertNotNull(index);
        assertValidIndex(index);
    }

    @Test
    public void testIssue251735_perpartes_cancel() throws Exception {
        assertNotNull(me);
        assertNotNull(cache);
        final CancelRequestImpl cr = new CancelRequestImpl();
        final Context ctx = SPIAccessor.getInstance().createContext(
            cache,
            me,
            JavaIndex.NAME,
            JavaIndex.VERSION,
            LuceneIndexFactory.getDefault(),
            false,
            false,
            false,
            SuspendSupport.NOP,
            cr,
            null);
        LowMemoryWatcherAccessor.setLowMemory(true);
        luceneLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("Storing in TX {0}: {1} added, {2} deleted".equals(record.getMessage())) {
                    cr.signal.set(true);
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
        SPIAccessor.getInstance().setAllFilesJob(ctx, true);
        try {
            factory.scanStarted(ctx);
            SPIAccessor.getInstance().index(factory.createIndexer(), ctx);
        } finally {
            factory.scanFinished(ctx);
        }
        ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(me, false);
        assertNotNull(uq);
        final Field indexFld = PersistentClassIndex.class.getDeclaredField("index");    //NOI18N
        indexFld.setAccessible(true);
        final LuceneIndex index = (LuceneIndex) indexFld.get(uq);
        assertNotNull(index);
        assertValidIndex(index);
    }

    private void assertValidIndex(LuceneIndex index) throws ReflectiveOperationException {
        final Field dirCacheFld = LuceneIndex.class.getDeclaredField("dirCache");   //NOI18N
        dirCacheFld.setAccessible(true);
        final Object dirCache = dirCacheFld.get(index);
        assertNotNull(dirCache);
        final Field indexWriterRefFld = dirCache.getClass().getDeclaredField("indexWriterRef"); //NOI18N
        indexWriterRefFld.setAccessible(true);
        final Object indexWriterRef = indexWriterRefFld.get(dirCache);
        final Field modifiedFld = indexWriterRef.getClass().getDeclaredField("modified");   //NOI18N
        modifiedFld.setAccessible(true);
        assertFalse(modifiedFld.getBoolean(indexWriterRef));
        final Field txThreadFld = indexWriterRef.getClass().getDeclaredField("txThread");   //NOI18N
        txThreadFld.setAccessible(true);
        assertNull(txThreadFld.get(indexWriterRef));
        final Field openThreadFld = indexWriterRef.getClass().getDeclaredField("openThread");   //NOI18N
        openThreadFld.setAccessible(true);
        assertNull(openThreadFld.get(indexWriterRef));
    }

    private static final class CancelRequestImpl implements CancelRequest {
        private final AtomicBoolean signal = new AtomicBoolean();
        @Override
        public boolean isRaised() {
            return signal.get();
        }
    }
}
