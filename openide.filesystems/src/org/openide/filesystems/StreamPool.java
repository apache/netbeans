/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.openide.filesystems;

import org.openide.util.*;

import java.io.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class keeps info about streams (these streams are registered) that was
 * not closed yet. Also for every issued stream is hold stracktrace.
 * Sometimes there is necessary to know who didn`t close stream.
 *
 * @author  rmatous
 */
final class StreamPool extends Object {
    static final Logger LOG = Logger.getLogger(StreamPool.class.getName());
    private static Map<FileObject, StreamPool> fo2StreamPool = new WeakHashMap<FileObject, StreamPool>();
    private static Map<FileSystem, StreamPool> fs2StreamPool = new WeakHashMap<FileSystem, StreamPool>();
    private static Boolean annotateUnclosedStreams;
    private Set<InputStream> iStreams;
    private Set<OutputStream> oStreams;

    /** Creates new StreamPool */
    private StreamPool() {
    }

    /**
     * This method creates subclassed  NotifyInputStream (extends InputStream).
     * NotifyInputStream saves stacktrace in constrcuctor (creates new Exception) that
     * is used in method annotate.
     * This method also register this NotifyInputStream as
     * mapping (AbstractFolder, NotifyInputStream) and
     * mapping (AbstractFolder.getFileSystem(), NotifyInputStream).
     * If NotifyInputStream is closed then registration is freed.
     * For fo is also created StreamPool unless it exists yet.
     * @param fo FileObject that issues is
     * @return subclassed InputStream that is registered as mentioned above */
    public static InputStream createInputStream(final AbstractFolder fo)
    throws FileNotFoundException {
        InputStream retVal = null;

        synchronized (StreamPool.class) {
            if (get(fo).waitForOutputStreamsClosed(fo, 2000)) {
                retVal = new NotifyInputStream(fo);
                get(fo).iStream().add(retVal);
                get(fo.getFileSystem()).iStream().add(retVal);
            }
        }

        if ((retVal != null) && (retVal instanceof NotifyInputStream)) {
            AbstractFileSystem abstractFileSystem = ((AbstractFileSystem) fo.getFileSystem());
            ((NotifyInputStream) retVal).setOriginal(abstractFileSystem.info.inputStream(fo.getPath()));
        } else {
            retVal = new InputStream() {
                        @Override
                        public int read() throws IOException {
                            FileAlreadyLockedException alreadyLockedEx = new FileAlreadyLockedException(fo.getPath());
                            get(fo).annotate(alreadyLockedEx);
                            throw alreadyLockedEx;
                        }
                    };
        }

        return retVal;
    }

    /** This method creates subclassed  NotifyOutputStream (extends OutputStream).
     * NotifyOutputStream saves stacktrace in constrcuctor (creates new Exception) that
     * is used in method annotate.
     * This method also register this NotifyOutputStream as
     * mapping (AbstractFolder, NotifyOutputStream) and
     * mapping (AbstractFolder.getFileSystem(), NotifyOutputStream).
     * If NotifyOutputStream is closed then registration is freed.
     * For fo is also created StreamPool unless it exists yet.
     * @return subclassed OutputStream that is registered as mentioned above
     * @param fireFileChanged defines if should be fired fileChanged event after close of stream
     * @param fo FileObject that issues is
     * */
    public static OutputStream createOutputStream(final AbstractFolder fo, boolean fireFileChanged)
    throws IOException {
        OutputStream retVal = null;

        synchronized (StreamPool.class) {
            if (get(fo).waitForInputStreamsClosed(fo, 2000) &&
                get(fo).waitForOutputStreamsClosed(fo, 2000)
            ) {
                retVal = new NotifyOutputStream(fo, fireFileChanged);
                get(fo).oStream().add(retVal);
                get(fo.getFileSystem()).oStream().add(retVal);
            }
        }

        if ((retVal != null) && (retVal instanceof NotifyOutputStream)) {
            AbstractFileSystem abstractFileSystem = ((AbstractFileSystem) fo.getFileSystem());
            ((NotifyOutputStream) retVal).setOriginal(abstractFileSystem.info.outputStream(fo.getPath()));
        } else {
            retVal = new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            FileAlreadyLockedException alreadyLockedEx = new FileAlreadyLockedException(fo.getPath());
                            get(fo).annotate(alreadyLockedEx);
                            throw alreadyLockedEx;
                        }
                    };
        }

        return retVal;
    }

    /**
     * This method finds StreamPool assiciated with fo or null. This StreamPool is
     * created by means of createInputStream or createOutputStream.
     * @param fo FileObject whose StreamPool is looked for
     * @return  StreamPool or null*/
    public static synchronized StreamPool find(FileObject fo) {
        return fo2StreamPool.get(fo);
    }

    /**
     * This method finds StreamPool assiciated with fs or null. This StreamPool is
     * created by means of createInputStream or createOutputStream.
     * @param fs FileSystem whose StreamPool is looked for
     * @return  StreamPool or null*/
    public static synchronized StreamPool find(FileSystem fs) {
        return fs2StreamPool.get(fs);
    }

    /**
     * Annotates ex with all exceptions of unclosed streams.
     * @param ex that should be annotated */
    public void annotate(Exception ex) {
        if (!annotateUnclosedStreams()) {
            return;
        }

        synchronized (StreamPool.class) {
            if (iStreams != null) {
                Iterator itIs = iStreams.iterator();
                NotifyInputStream nis;

                while (itIs.hasNext()) {
                    nis = (NotifyInputStream) itIs.next();

                    Exception annotation = nis.getException();

                    if (annotation != null) {
                        ExternalUtil.annotate(ex, annotation);
                    }
                }
            }

            if (oStreams != null) {
                Iterator itOs = oStreams.iterator();
                NotifyOutputStream nos;

                while (itOs.hasNext()) {
                    nos = (NotifyOutputStream) itOs.next();

                    Exception annotation = nos.getException();

                    if (annotation != null) {
                        ExternalUtil.annotate(ex, annotation);
                    }
                }
            }
        }
    }

    /**
     * @return  true if there is any InputStream that was not closed yet  */
    public boolean isInputStreamOpen() {
        return isStreamOpen(iStreams);
    }
    /**
     * @return  true if there is any OutputStream that was not closed yet  */
    public boolean isOutputStreamOpen() {
        return isStreamOpen(oStreams);
    }

    private boolean waitForInputStreamsClosed(FileObject fo, int timeInMs) {
        return waitForStreams(fo, timeInMs, iStreams);
    }
    
    private boolean waitForOutputStreamsClosed(FileObject fo, int timeInMs) {
        return waitForStreams(fo, timeInMs, oStreams);
    }
           
    private static boolean waitForStreams(FileObject fo, int timeInMs, Set<?> streams) {
        synchronized (StreamPool.class) {
            if (isStreamOpen(streams)) {
                long till = System.currentTimeMillis() + timeInMs;
                boolean interrupted = false;
                for (;;) {
                    long wait = till - System.currentTimeMillis();
                    if (wait <= 0) {
                        break;
                    }
                    try {
                        StreamPool.class.wait(wait);
                        break;
                    } catch (InterruptedException ex) {
                        interrupted = true;
                        continue;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                if (isStreamOpen(streams)) {
                    LOG.log(Level.FINE, "Open streams {0} for {1}", new Object[]{streams, fo});
                    return false;
                }
            }
            return true;
        }
    }
    
    private static boolean isStreamOpen(Set<?> set) {
        return (set != null) && !set.isEmpty();
    }

    /** All next methods are private (Not visible outside this class)*/
    private static StreamPool get(FileObject fo) {
        StreamPool strPool = fo2StreamPool.get(fo);

        if (strPool == null) {
            fo2StreamPool.put(fo, strPool = new StreamPool());
        }

        return strPool;
    }

    private static StreamPool get(FileSystem fs) {
        StreamPool strPool = fs2StreamPool.get(fs);

        if (strPool == null) {
            fs2StreamPool.put(fs, strPool = new StreamPool());
        }

        return strPool;
    }

    private Set<InputStream> iStream() {
        if (iStreams == null) {
            iStreams = new WeakSet<InputStream>();
        }

        return iStreams;
    }

    private Set<OutputStream> oStream() {
        if (oStreams == null) {
            oStreams = new WeakSet<OutputStream>();
        }

        return oStreams;
    }

    /** fireFileChange defines if should be fired fileChanged event after close of stream*/
    private static void closeOutputStream(AbstractFolder fo, OutputStream os, boolean fireFileChanged) {
        StreamPool foPool = find(fo);
        StreamPool fsPool = find(fo.getFileSystem());
        Set foSet = (foPool != null) ? foPool.oStreams : null;
        Set fsSet = (fsPool != null) ? fsPool.oStreams : null;

        removeStreams(fsSet, foSet, os);
        removeStreamPools(fsPool, foPool, fo);
        fo.outputStreamClosed(fireFileChanged);
    }

    private static void closeInputStream(AbstractFolder fo, InputStream is) {
        StreamPool foPool = find(fo);
        StreamPool fsPool = find(fo.getFileSystem());
        Set foSet = (foPool != null) ? foPool.iStreams : null;
        Set fsSet = (fsPool != null) ? fsPool.iStreams : null;

        removeStreams(fsSet, foSet, is);
        removeStreamPools(fsPool, foPool, fo);
    }

    private static synchronized void removeStreams(Set fsSet, Set foSet, Object stream) {
        if (foSet != null) {
            foSet.remove(stream);
        }

        if (fsSet != null) {
            fsSet.remove(stream);
        }
    }

    private static synchronized void removeStreamPools(StreamPool fsPool, StreamPool foPool, AbstractFolder fo) {
        boolean isIStreamEmpty = ((foPool == null) || (foPool.iStreams == null) || foPool.iStreams.isEmpty());
        boolean isOStreamEmpty = ((foPool == null) || (foPool.oStreams == null) || foPool.oStreams.isEmpty());

        if (isIStreamEmpty && isOStreamEmpty) {
            fo2StreamPool.remove(fo);
        }

        isIStreamEmpty = ((fsPool == null) || (fsPool.iStreams == null) || fsPool.iStreams.isEmpty());
        isOStreamEmpty = ((fsPool == null) || (fsPool.oStreams == null) || fsPool.oStreams.isEmpty());

        if (isIStreamEmpty && isOStreamEmpty) {
            fs2StreamPool.remove(fo.getFileSystem());
        }
    }

    private static final class NotifyOutputStream extends FilterOutputStream {
        private static final OutputStream emptyOs = new ByteArrayOutputStream();
        private Exception ex;
        private boolean closed = false;
        AbstractFolder fo;

        /** defines if should be fired fileChanged event after close of stream */
        private boolean fireFileChanged;

        public NotifyOutputStream(AbstractFolder fo, boolean fireFileChanged) {
            super(emptyOs);
            this.fo = fo;

            if (annotateUnclosedStreams()) {
                ex = new Exception();
            }

            this.fireFileChanged = fireFileChanged;
        }

        private void setOriginal(OutputStream os) {
            out = os;
        }

        /** Faster implementation of writing than is implemented in
         * the filter output stream.
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ex = null;
                try {
                    super.out.flush();
                    super.close();
                } finally {
                    closeOutputStream(fo, this, fireFileChanged);
                    MIMESupport.freeCaches();
                    synchronized (StreamPool.class) {
                        StreamPool.class.notifyAll();
                    }
                }
            }
        }

        public Exception getException() {
            return ex;
        }
    }

    private static final class NotifyInputStream extends FilterInputStream {
        private static final InputStream emptyIs = new ByteArrayInputStream(new byte[0]);
        private Exception ex;
        AbstractFolder fo;
        private boolean closed = false;

        public NotifyInputStream(AbstractFolder fo) {
            super(emptyIs);
            this.fo = fo;

            if (annotateUnclosedStreams()) {
                ex = new Exception();
            }
        }

        private void setOriginal(InputStream is) {
            in = is;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ex = null;
                super.close();
                closeInputStream(fo, this);

                synchronized (StreamPool.class) {
                    if (!StreamPool.get(fo).isInputStreamOpen()) {
                        StreamPool.class.notifyAll();
                    }
                }
            }
        }

        public Exception getException() {
            return ex;
        }
    }
    
    /** Whether to keep the stack traces. By default, don't - too expensive. */
    private static boolean annotateUnclosedStreams() {
        if (annotateUnclosedStreams != null) {
            return annotateUnclosedStreams;
        }
        String annotateProp = System.getProperty("org.openide.filesystems.annotateUnclosedStreams"); // NOI18N;
        annotateUnclosedStreams = Boolean.parseBoolean(annotateProp);
        boolean assertsOn = false;
        assert assertsOn = true;
        if (assertsOn) {
            annotateUnclosedStreams = annotateProp == null ? true : ! Boolean.FALSE.toString().equals(annotateProp);
        }
        return annotateUnclosedStreams;
    }
}
