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

package org.netbeans.modules.masterfs.watcher.windows;

import org.netbeans.modules.masterfs.providers.Notifier;
import com.sun.jna.FromNativeContext;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.ByReference;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A {@link Notifier} implementation using Win32 API ReadDirectoryChangesW.
 * Based on JNA examples and platform library stubs.
 *
 * @author nenik
 */
@ServiceProvider(service=Notifier.class, position=400)
public final class WindowsNotifier extends Notifier<Void> {
    static final Logger LOG = Logger.getLogger(WindowsNotifier.class.getName());

    public static final class HANDLE extends PointerType {
        private boolean immutable;
        public HANDLE() { }
        public HANDLE(Pointer p) {
            setPointer(p);
            immutable = true;
        }

        /** Override to the appropriate object for INVALID_HANDLE_VALUE. */
        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (INVALID_HANDLE_VALUE.equals(o))
                return INVALID_HANDLE_VALUE;
            return o;
        }

        @Override
        public void setPointer(Pointer p) {
            if (immutable) {
                throw new UnsupportedOperationException("immutable reference");
            }

            super.setPointer(p);
        }
    }

    public static class ULONG_PTR extends IntegerType {
        public ULONG_PTR() {
                this(0);
        }

        public ULONG_PTR(long value) {
                super(Native.POINTER_SIZE, value);
        }
    }

    @SuppressWarnings("PublicField")
    @FieldOrder({"Internal", "InternalHigh", "Offset", "OffsetHigh", "hEvent"})
    public static class OVERLAPPED extends Structure {
        public ULONG_PTR Internal;
        public ULONG_PTR InternalHigh;
        public int Offset;
        public int OffsetHigh;
        public HANDLE hEvent;
    }

    public static HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(
    		Native.POINTER_SIZE == 8 ? -1 : 0xFFFFFFFFL));

    public static class HANDLEByReference extends ByReference {

    	public HANDLEByReference() {
            this(null);
        }

        public HANDLEByReference(HANDLE h) {
            super(Native.POINTER_SIZE);
            setValue(h);
        }

        public void setValue(HANDLE h) {
            getPointer().setPointer(0, h != null ? h.getPointer() : null);
        }

        public HANDLE getValue() {
            Pointer p = getPointer().getPointer(0);
            if (p == null)
                return null;
            if (INVALID_HANDLE_VALUE.getPointer().equals(p))
                return INVALID_HANDLE_VALUE;
            HANDLE h = new HANDLE();
            h.setPointer(p);
            return h;
        }
    }

    @SuppressWarnings("PublicField")
    @FieldOrder({"NextEntryOffset", "Action", "FileNameLength", "FileName"})
    public static class FILE_NOTIFY_INFORMATION extends Structure {
        public int NextEntryOffset;
        public int Action;
        public int FileNameLength;
        // filename is not nul-terminated, so we can't use a String/WString
        public char[] FileName = new char[1];

        private FILE_NOTIFY_INFORMATION() {}

        public FILE_NOTIFY_INFORMATION(int size) {
            if (size < size()) {
               throw new IllegalArgumentException("Size must greater than "
                               + size() + ", requested " + size);
            }
            allocateMemory(size);
        }

        /** WARNING: this filename may be either the short or long form of the filename. */
        public String getFilename() {
            return new String(FileName, 0, FileNameLength/2);
        }

        @Override
        public void read() {
            // avoid reading filename until we know how long it is
            FileName = new char[0];
            super.read();
            FileName = getPointer().getCharArray(12, FileNameLength/2);
        }

        public FILE_NOTIFY_INFORMATION next() {
            if (NextEntryOffset == 0)
                    return null;
            FILE_NOTIFY_INFORMATION next = new FILE_NOTIFY_INFORMATION();
            next.useMemory(getPointer(), NextEntryOffset);
            next.read();
            return next;
        }
    }

    @SuppressWarnings("PublicField")
    @FieldOrder({"nLength", "lpSecurityDescriptor", "bInheritHandle"})
    public static class SECURITY_ATTRIBUTES extends Structure {
        public final int nLength = size();
        public Pointer lpSecurityDescriptor;
        public boolean bInheritHandle;
    }

    interface Kernel32 extends StdCallLibrary {
        HANDLE CreateFile(String lpFileName, int dwDesiredAccess, int dwShareMode,
                    SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition,
                    int dwFlagsAndAttributes, HANDLE hTemplateFile);

        HANDLE CreateIoCompletionPort(HANDLE FileHandle, HANDLE ExistingCompletionPort,
                    Pointer CompletionKey, int NumberOfConcurrentThreads);

        int GetLastError();

        boolean GetQueuedCompletionStatus(HANDLE CompletionPort,
                    IntByReference lpNumberOfBytes, ByReference lpCompletionKey,
                    PointerByReference lpOverlapped, int dwMilliseconds);
       
        boolean PostQueuedCompletionStatus(HANDLE CompletionPort,
                    int dwNumberOfBytesTransferred, Pointer dwCompletionKey,
                    OVERLAPPED lpOverlapped);
       
        boolean CloseHandle(HANDLE hObject);

        interface OVERLAPPED_COMPLETION_ROUTINE extends StdCallCallback {
            void callback(int errorCode, int nBytesTransferred,
                            OVERLAPPED overlapped);
        }

      
        public boolean ReadDirectoryChangesW(HANDLE directory,
                    FILE_NOTIFY_INFORMATION info, int length, boolean watchSubtree,
                int notifyFilter, IntByReference bytesReturned, OVERLAPPED overlapped,
                OVERLAPPED_COMPLETION_ROUTINE completionRoutine);


    }

    final static Kernel32 KERNEL32 = Native.load("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

    public WindowsNotifier() { // prepare port, start thread?
    }

    public @Override void removeWatch(Void key) throws IOException {}


    public @Override String nextEvent() throws IOException, InterruptedException {
        return events.take();
    }

    public static final int INFINITE = 0xFFFFFFFF;

    public static final int FILE_NOTIFY_CHANGE_NAME = 0x00000003;
    public static final int FILE_NOTIFY_CHANGE_ATTRIBUTES = 0x00000004;
    public static final int FILE_NOTIFY_CHANGE_SIZE = 0x00000008;
    public static final int FILE_NOTIFY_CHANGE_LAST_WRITE = 0x00000010;
    public static final int FILE_NOTIFY_CHANGE_CREATION = 0x00000040;
    public static final int FILE_NOTIFY_CHANGE_SECURITY = 0x00000100;

    private static final int NOTIFY_MASK =
            FILE_NOTIFY_CHANGE_NAME |
            FILE_NOTIFY_CHANGE_ATTRIBUTES |
            FILE_NOTIFY_CHANGE_SIZE |
            FILE_NOTIFY_CHANGE_LAST_WRITE |
            FILE_NOTIFY_CHANGE_CREATION |
            FILE_NOTIFY_CHANGE_SECURITY;

    public static final int FILE_LIST_DIRECTORY = 0x00000001;
    public static final int OPEN_EXISTING =      3;

    public static final int FILE_SHARE_READ  = 0x00000001;
    public static final int FILE_SHARE_WRITE = 0x00000002;
    public static final int FILE_SHARE_DELETE = 0x00000004;

    public static final int FILE_FLAG_OVERLAPPED =           0x40000000;
    public static final int FILE_FLAG_BACKUP_SEMANTICS =     0x02000000;

    private class FileInfo {
        public final String path;
        public final HANDLE handle;
        public final FILE_NOTIFY_INFORMATION info = new FILE_NOTIFY_INFORMATION(BUFFER_SIZE);
        public final IntByReference infoLength = new IntByReference();
        public final OVERLAPPED overlapped = new OVERLAPPED();
        public FileInfo(String path, HANDLE h) {
            this.path = path;
            this.handle = h;
        }
    }

    private static int watcherThreadID;
    private Thread watcher;
    private HANDLE port;
    private final Map<String, FileInfo> rootMap = new HashMap<String, FileInfo>();
    private final Map<HANDLE, FileInfo> handleMap = new HashMap<HANDLE, FileInfo>();
    private final BlockingQueue<String> events = new LinkedBlockingQueue<String>();

    public @Override Void addWatch(String path) throws IOException {
        if (path.isEmpty()) {
            return null;
        }
        
        String root = null;

        if (path.length() >= 3 && path.charAt(1) == ':') { // classic drive letter (e.g. C:\)
            root = path.substring(0, 3).replace('/', '\\');
            if (root.charAt(2) != '\\') {
                throw new IOException("wrong path: " + path);
            } else if (path.length() == 3) { // Can be suspicious. #236930.
                LOG.log(Level.INFO, "Adding listener for drive {0}", path); //NOI18N
            }
        } else { // check if it is unc path
            String normedPath = path.replace('/', '\\');
            if (normedPath.startsWith("\\\\")) {
                int thirdBackslash = normedPath.indexOf('\\', 3);
                if (thirdBackslash != -1) {
                    int endOfRoot = normedPath.indexOf('\\', thirdBackslash + 1);
                    if (endOfRoot == -1) {
                        endOfRoot = normedPath.length();
                    }
                    root = normedPath.substring(0, endOfRoot);
                } else {
                    throw new IOException("wrong path: " + path);
                }
            }
        }

        if (rootMap.containsKey(root)) return null; // already listening
        path = root; // listen once on the rootpath instead

        int mask = FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE;
        int flags = FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OVERLAPPED;
        HANDLE handle = KERNEL32.CreateFile(path,
        		FILE_LIST_DIRECTORY,
        		mask, null, OPEN_EXISTING,
                flags, null);
        if (INVALID_HANDLE_VALUE.equals(handle)) {
            throw new IOException("Unable to open " + path + ": "
                                  + KERNEL32.GetLastError());
        }
        FileInfo finfo = new FileInfo(path, handle);
        rootMap.put(path, finfo);
        handleMap.put(handle, finfo);

        // Existing port is returned
        port = KERNEL32.CreateIoCompletionPort(handle, port, handle.getPointer(), 0);
        if (INVALID_HANDLE_VALUE.equals(port)) {
            throw new IOException("Unable to create/use I/O Completion port "
                    + "for " + path + ": " + KERNEL32.GetLastError());
        }

        if (!KERNEL32.ReadDirectoryChangesW(handle, finfo.info, finfo.info.size(),
                                        true, NOTIFY_MASK, finfo.infoLength,
                                        finfo.overlapped, null)) {
            int err = KERNEL32.GetLastError();
            throw new IOException("ReadDirectoryChangesW failed on "
                                  + finfo.path + ", handle " + handle
                                  + ": " + err);
        }
        if (watcher == null) {
            Thread t = new Thread("W32 File Monitor") {
                @Override
                public void run() {
                    FileInfo finfo;
                    while (watcher != null) {
                        finfo = waitForChange();
                        if (finfo == null) continue;

                        try {
                            handleChanges(finfo);
                        } catch(IOException e) {
                            LOG.log(Level.INFO, "handleChanges", e); 
                        }
                    }
                }
            };
            t.setDaemon(true);
            t.start();
            watcher = t;
        }

        return null;
    }
    
    @Override
    protected void start() throws IOException {
    }

    @Override
    public void stop() throws IOException {
        try {
            Thread w = watcher;
            if (w == null) {
                return;
            }
            watcher = null;
            w.interrupt();
            w.join(2000);
        } catch (InterruptedException ex) {
            throw (IOException)new InterruptedIOException().initCause(ex);
        }
    }

    private void notify(File file) {
        events.add(file.getPath());
    }


    private static final int BUFFER_SIZE = 4096;
        
    private void handleChanges(FileInfo finfo) throws IOException {
        FILE_NOTIFY_INFORMATION fni = finfo.info;
        // Lazily fetch the data from native to java - asynchronous update
        fni.read();
        do {
            File file = new File(finfo.path, fni.getFilename());
            notify(file);
            
            fni = fni.next();
        } while (fni != null);
        
        if (!KERNEL32.ReadDirectoryChangesW(finfo.handle, finfo.info,
        		finfo.info.size(), true, NOTIFY_MASK,
        		finfo.infoLength, finfo.overlapped, null)) {        	
        		int err = KERNEL32.GetLastError();
                throw new IOException("ReadDirectoryChangesW failed on "
                                  + finfo.path + ": " + err);
        }
    }

    private FileInfo waitForChange() {
        IntByReference rcount = new IntByReference();
        HANDLEByReference rkey = new HANDLEByReference();
        PointerByReference roverlap = new PointerByReference();
        KERNEL32.GetQueuedCompletionStatus(port, rcount, rkey, roverlap, INFINITE);
        
        synchronized (this) { 
            return handleMap.get(rkey.getValue());
        }
    }

}
