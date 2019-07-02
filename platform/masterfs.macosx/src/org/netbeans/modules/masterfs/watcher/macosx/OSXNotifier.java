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

package org.netbeans.modules.masterfs.watcher.macosx;

import org.netbeans.modules.masterfs.providers.Notifier;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=Notifier.class, position=100)
public final class OSXNotifier extends Notifier<Void> {
    private static final Level DEBUG_LOG_LEVEL = Level.FINE;
    private static final Level PERF_LOG_LEVEL = Level.FINE;
    private static final long kFSEventStreamEventIdSinceNow = 0xFFFFFFFFFFFFFFFFL;
    private static final int kFSEventStreamCreateFlagNoDefer = 0x00000002;
    private static final int kFSEventStreamEventFlagMustScanSubDirs = 0x00000001;
    private static final int kFSEventStreamEventFlagMount = 0x00000040;
    private static final int kFSEventStreamEventFlagUnmount = 0x00000080;
    private static final double LATENCY = 1.0f;
    private static final int ENC_MAC_ROMAN = 0;
    private static final String DEFAULT_RUN_LOOP_MODE = "kCFRunLoopDefaultMode";    //NOI18N
    private static final Logger LOG = Logger.getLogger(OSXNotifier.class.getName());
    private final CoreFoundation cf;
    private final CoreServices cs;
    private final EventCallback callback;
    private final BlockingQueue<String> events;
    //@GuardedBy("this")
    private ExecutorService worker;
    //@GuardedBy("this")
    private Pointer[] rtData;
    
    private static final String ALL_CHANGE = "ALL-CHANGE";  //xxx - shouldn't be global in Notifier rather than using null?

    public OSXNotifier() {
        cf = (CoreFoundation) Native.loadLibrary("CoreFoundation",CoreFoundation.class);    //NOI18N
        cs = (CoreServices) Native.loadLibrary("CoreServices",CoreServices.class);          //NOI18N
        callback = new EventCallbackImpl();
        events = new LinkedBlockingQueue<String>();
    }


    public @Override Void addWatch(String path) throws IOException {
        return null;
    }

    public @Override void removeWatch(Void key) throws IOException {
        // ignore
    }

    public @Override String nextEvent() throws IOException, InterruptedException {
        final String event = events.take();
        return event == ALL_CHANGE ? null : event;
    }

    public synchronized void start() throws IOException {
        if (worker != null) {
            throw new IllegalStateException("FileSystemWatcher already started.");  //NOI18N
        }
        worker = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        final Exchanger<Object> exchanger = new Exchanger<Object>();
        worker.execute(new Runnable() {
            public @Override void run () {
                try {
                    Pointer[] _rtData = null;
                    try {
                        _rtData = createFSEventStream();
                    } catch (Throwable ex) {
                        exchanger.exchange(ex);
                    } finally {
                        if (_rtData != null) {
                            exchanger.exchange(_rtData);
                            cf.CFRunLoopRun();
                        }
                    }
                } catch (InterruptedException ie) {
                    LOG.log(Level.WARNING, "Watcher interruped during start", ie);  //NOI18N
                }
            }
        });
        final Object _data;
        try {
            _data = exchanger.exchange(null);
        } catch (InterruptedException ex) {
            throw (InterruptedIOException)new InterruptedIOException().initCause(ex);
        }
        assert _data != null;
        if (_data instanceof Throwable) {
            worker.shutdown();
            worker = null;
            throw new IOException((Throwable)_data);
        } else {
            rtData = (Pointer[]) _data;
        }
    }

    @Override
    public synchronized void stop() throws IOException {
        if (worker == null) {
            throw new IllegalStateException("FileSystemWatcher is not started.");  //NOI18N
        }
        assert rtData != null;
        assert rtData.length == 2;
        assert rtData[0] != null;
        assert rtData[1] != null;
        cs.FSEventStreamStop(rtData[0]);
        cs.FSEventStreamInvalidate(rtData[0]);
        cs.FSEventStreamRelease(rtData[0]);
        cf.CFRunLoopStop(rtData[1]);
        worker.shutdown();
        worker = null;
        rtData = null;
    }

    private Pointer[] createFSEventStream() throws IOException {
        final Pointer root = cf.CFStringCreateWithCString(Pointer.NULL,"/",ENC_MAC_ROMAN);  //NOI18N
        if (root == Pointer.NULL) {
            throw new IOException("Path creation failed.");     //NOI18N
        }
        final Pointer arr = cf.CFArrayCreateMutable(Pointer.NULL, new NativeLong(1), Pointer.NULL);
        if (arr == Pointer.NULL) {
            throw new IOException("Path list creation failed.");    //NOI18N
        }
        cf.CFArrayAppendValue(arr, root);

        final Pointer eventStream = cs.FSEventStreamCreate(Pointer.NULL, callback, Pointer.NULL, arr, kFSEventStreamEventIdSinceNow, LATENCY, kFSEventStreamCreateFlagNoDefer);
        if (eventStream == Pointer.NULL) {
            throw new IOException("Creation of FSEventStream failed."); //NOI18N
        }
        final Pointer loop = cf.CFRunLoopGetCurrent();
        if (eventStream == Pointer.NULL) {
            throw new IOException("Cannot find run loop for caller.");  //NOI18N
        }
        final Pointer kCFRunLoopDefaultMode = findDefaultMode(loop);
        if (kCFRunLoopDefaultMode == null) {
            throw new IOException("Caller has no defaul run loop mode.");   //NOI18N
        }
        cs.FSEventStreamScheduleWithRunLoop(eventStream, loop, kCFRunLoopDefaultMode);
        if (LOG.isLoggable(DEBUG_LOG_LEVEL)) {
            LOG.log(DEBUG_LOG_LEVEL, getStreamDescription(eventStream));
        }
        cs.FSEventStreamStart(eventStream);
        return new Pointer[] {eventStream, loop};
    }

    private Pointer findDefaultMode(final Pointer runLoop) {
        final Pointer modes = cf.CFRunLoopCopyAllModes(runLoop);
        if (modes != Pointer.NULL) {
            final int modesCount = cf.CFArrayGetCount(modes).intValue();
            for (int i=0; i< modesCount; i++) {
                final Pointer mode = cf.CFArrayGetValueAtIndex(modes, new NativeLong(i));
                if (mode != Pointer.NULL && DEFAULT_RUN_LOOP_MODE.equals(cf.CFStringGetCStringPtr(mode, ENC_MAC_ROMAN))) {
                    return mode;
                }
            }
        }
        return null;
    }

    private String getStreamDescription(final Pointer eventStream) {
        final Pointer desc = cs.FSEventStreamCopyDescription(eventStream);
        return desc == Pointer.NULL ? "" : cf.CFStringGetCStringPtr(desc, ENC_MAC_ROMAN);   //NOI18N
    }

    public static interface EventCallback extends Callback {
        void invoke(Pointer streamRef,
                    Pointer clientCallBackInfo,
                    NativeLong numEvents,
                    Pointer eventPaths,
                    Pointer eventFlags,
                    Pointer eventIds);
    }

    public static interface CoreFoundation extends Library {
        Pointer CFRunLoopGetCurrent();
        void CFRunLoopRun();
        void CFRunLoopStop(Pointer loop);
        Pointer CFRunLoopCopyAllModes(Pointer loop);

        Pointer CFArrayCreateMutable(Pointer allocator, NativeLong size, Pointer callback);
        void CFArrayAppendValue(Pointer theArray, Pointer value);
        Pointer CFArrayGetValueAtIndex(Pointer theArray, NativeLong index);
        NativeLong CFArrayGetCount(Pointer theArray);

        Pointer CFStringCreateWithCString(Pointer allocator, String string, int encoding);
        String CFStringGetCStringPtr(Pointer theString, int encoding);
    }

    public static interface CoreServices extends Library {
        Pointer FSEventStreamCreate(Pointer allocator, EventCallback callback, Pointer ctx, Pointer pathsToWatch, long sinceWhen, double latency, int flags);
        Pointer FSEventStreamCopyDescription(Pointer stream);
        void FSEventStreamScheduleWithRunLoop(Pointer stream, Pointer loop, Pointer mode);
        void FSEventStreamUnscheduleFromRunLoop(Pointer stream, Pointer loop, Pointer mode);
        void FSEventStreamStart(Pointer stream);
        void FSEventStreamStop(Pointer stream);
        void FSEventStreamInvalidate(Pointer stream);
        void FSEventStreamRelease(Pointer stream);
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }

    }

    private class EventCallbackImpl implements EventCallback {
        @Override
        public void invoke(Pointer streamRef, Pointer clientCallBackInfo, NativeLong numEvents, Pointer eventPaths, Pointer eventFlags, Pointer eventIds) {
            final long st = System.currentTimeMillis();
            final int length = numEvents.intValue();
            final Pointer[] pointers = eventPaths.getPointerArray(0, length);
            int flags[];
            if (eventFlags == null) {
                flags = new int[length];
                LOG.log(DEBUG_LOG_LEVEL, "FSEventStreamCallback eventFlags == null, expected int[] of size {0}", length); //NOI18N
            } else {
                flags = eventFlags.getIntArray(0, length);
            }
            for (int i=0; i<length; i++) {
                final Pointer p = pointers[i];
                int flag = flags[i];
                final String path = p.getString(0);

                if ((flag & kFSEventStreamEventFlagMustScanSubDirs) ==  kFSEventStreamEventFlagMustScanSubDirs ||
                    (flag & kFSEventStreamEventFlagMount) == kFSEventStreamEventFlagMount ||
                    (flag & kFSEventStreamEventFlagUnmount) == kFSEventStreamEventFlagUnmount) {
                    events.add(ALL_CHANGE);
                } else {
                    events.add(path);
                }
                LOG.log(DEBUG_LOG_LEVEL, "Event on {0}", new Object[]{path});
            }
            LOG.log(PERF_LOG_LEVEL, "Callback time: {0}", (System.currentTimeMillis() - st));
        }
    }
}
