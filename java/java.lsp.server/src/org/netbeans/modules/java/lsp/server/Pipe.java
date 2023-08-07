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
package org.netbeans.modules.java.lsp.server;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.StandardProtocolFamily;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Utilities;

public abstract class Pipe implements AutoCloseable {
    private final String name;

    private Pipe(String name) {
        this.name = name;
    }

    public static Pipe createListeningPipe(String prefix) throws IOException {
        if (Utilities.isWindows()) {
            return windowsPipe(prefix);
        } else {
            return unixPipe();
        }
    }

    public String getName() {
        return name;
    }

    public abstract Connection connect() throws IOException;

    public static abstract class Connection implements AutoCloseable {
        private final InputStream in;
        private final OutputStream out;

        public Connection(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public InputStream getIn() {
            return in;
        }

        public OutputStream getOut() {
            return out;
        }

    } 
//<editor-fold defaultstate="collapsed" desc="windows">
    private static final int FILE_FLAG_FIRST_PIPE_INSTANCE = 0x00080000;
    
    private static Pipe windowsPipe(String prefix) throws IOException {
        String name = "\\\\.\\pipe\\netbeans-" + prefix;
        
        return new Pipe(name) {
            @Override
            public Connection connect() throws IOException {
//                System.err.println("going to connect:");

                WinNT.HANDLE handle = Kernel32.INSTANCE.CreateNamedPipe(name, Kernel32.PIPE_ACCESS_DUPLEX | Kernel32.FILE_FLAG_OVERLAPPED, Kernel32.PIPE_TYPE_BYTE | Kernel32.PIPE_WAIT | Kernel32.PIPE_REJECT_REMOTE_CLIENTS, Kernel32.PIPE_UNLIMITED_INSTANCES, 8192, 8192, 0, null);
                if (handle == Kernel32.INVALID_HANDLE_VALUE) {
                    throwIOException();
                }

                if (!Kernel32.INSTANCE.ConnectNamedPipe(handle, null)) {
                    int error = Kernel32.INSTANCE.GetLastError();

                    if (error != Kernel32.ERROR_PIPE_CONNECTED) {
                        throwIOException();
                    }
                }
//                System.err.println("connected");
                InputStream in = new HandleInputStream(handle);
                OutputStream out = new HandleOutputStream(handle);
                return new Connection(in, out) {
                    @Override
                    public void close() throws Exception {
                        Kernel32.INSTANCE.CloseHandle(handle);
                    }
                };
            }
            @Override
            public void close() throws Exception {
                //XXX: close the last handle!
//                if (handle != null) { 
//                }
            }
        };
    }
    
    private static void throwIOException() throws IOException {
        throwIOException(Kernel32.INSTANCE.GetLastError());
    }
    
    private static void throwIOException(int error) throws IOException {
        throw new IOException("Operation failed, error: 0x" + Integer.toHexString(error));
    }
    
    private static final class HandleInputStream extends InputStream {
        
        private static int i = 0;
        private final WinNT.HANDLE handle;
        private final OutputStream debug;
        
        public HandleInputStream(WinNT.HANDLE handle) {
            this.handle = handle;
            try {
                debug = new FileOutputStream("C:\\log\\debug-input" + i++ + ".txt");
            } catch (FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public int read() throws IOException {
            byte[] data = new byte[1];
            int totalRead = read(data);
            if (totalRead < 0) {
                return totalRead;
            }
            return data[0];
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
//            if (off != 0) {
//                 debug.write(("reading to non-zero offset\n").getBytes());
//                byte[] newData = new byte[len];
//                int r = read(newData);
//                if (r > 0) {
//                    System.arraycopy(newData, 0, b, off, r);
//                }
//                return r;
//            }

            WinNT.HANDLE event = Kernel32.INSTANCE.CreateEvent(null, true, false, null);
            OVERLAPPED overlapped = new OVERLAPPED();
            overlapped.hEvent = event;
            overlapped.write();

            try (Memory buffer = new Memory(len)) { 
                debug.write(("debug: with Memory\n").getBytes());
                IntByReference lpNumberOfBytesRead = new IntByReference(0);
                int read;
                boolean result = Kernel32Ext.INSTANCE.ReadFile(handle, buffer, len, lpNumberOfBytesRead, overlapped.getPointer());
                debug.write(("ReadFile: result=" + result + ", " + "Kernel32.INSTANCE.GetLastError()=" + Kernel32.INSTANCE.GetLastError() + "\n").getBytes());
                if (!result) {
                    int error = Kernel32.INSTANCE.GetLastError();
                    if (error == Kernel32.ERROR_IO_PENDING) { 
                        debug.write(("going to wait").getBytes());
                        if (Kernel32.INSTANCE.WaitForSingleObject(event, Kernel32.INFINITE) != Kernel32.WAIT_OBJECT_0) {
                            //XXX: cancelIO
        //                    throwIOException();
                        }
                        debug.write(("finished wait").getBytes());
                        debug.write(("1. buffer: " + buffer.getByte(0) + "\n").getBytes());

                        IntByReference lpNumberOfBytesTransferred = new IntByReference(0);

                        if (!Kernel32Ext.INSTANCE.GetOverlappedResult(handle, overlapped.getPointer(), lpNumberOfBytesTransferred, true)) {
                            // throwIOException();
                            throwIOException();
                        }
                        debug.write(("2. buffer: " + buffer.getByte(0) + ", transferred: " + lpNumberOfBytesTransferred.getValue() + "\n").getBytes());
                        read = lpNumberOfBytesTransferred.getValue();
                        debug.write(("got result\n").getBytes());
                    } else if (error == Kernel32.ERROR_BROKEN_PIPE) {
                        return -1;
                    } else { 
                         debug.write(("error " + error + "\n").getBytes());
                         throwIOException(error);
                         read = -1;
                    }
                } else {
                    read = lpNumberOfBytesRead.getValue();
                } 

    //            System.err.println("read: " + new String(b, off, lpNumberOfBytesRead.getValue()));
                if (read == 1) {
                    b[off] = buffer.getByte(0);
                } else { 
                    buffer.read(0, b, off, read);
                }
                debug.write(("...read: " + new String(b, 0, read) + "\n").getBytes());
//                if (read == 1 && b[off] == 0) {
//                    b[off] = 'C';
//                }  
                Kernel32.INSTANCE.CloseHandle(event);
                return read;
            }
        }
        
    }
    
    private static final class HandleOutputStream extends OutputStream {
        
        private static int i = 0;
        private final WinNT.HANDLE handle;
        private final OutputStream debug;
        
        public HandleOutputStream(WinNT.HANDLE handle) {
            this.handle = handle;
            try {
                debug = new FileOutputStream("C:\\log\\debug-output" + i++ + ".txt");
            } catch (FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public void write(int b) throws IOException {
            write(new byte[] {(byte) b});
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            debug.write(("write called: " + new String(b, off, len)).getBytes());

            WinNT.HANDLE event = Kernel32.INSTANCE.CreateEvent(null, true, false, null);
            OVERLAPPED overlapped = new OVERLAPPED();
            overlapped.write();
            overlapped.hEvent = event;

            byte[] data = new byte[1];
            while (len > 0) {
                IntByReference lpNumberOfBytesWritten = new IntByReference(0);
                data[0] = b[off];
                debug.write(("going to write: " + new String(data, 0, 1)).getBytes());
                boolean result = Kernel32.INSTANCE.WriteFile(handle, data, 1, lpNumberOfBytesWritten, overlapped);
                if (!result) {
                    int error = Kernel32.INSTANCE.GetLastError();

                    if (error == Kernel32.ERROR_IO_PENDING) {
                        debug.write(("going to wait").getBytes());
                        if (Kernel32.INSTANCE.WaitForSingleObject(event, Kernel32.INFINITE) != Kernel32.WAIT_OBJECT_0) {
                            //XXX: cancelIO
        //                    throwIOException();
                        }
                        debug.write(("finished wait").getBytes());
                        // IntByReference lpNumberOfBytesTransferred = new IntByReference(0);
                        if (!Kernel32Ext.INSTANCE.GetOverlappedResult(handle, overlapped.getPointer(), lpNumberOfBytesWritten, true)) {
                            // throwIOException();
                            throwIOException();
                        }
                        debug.write(("written").getBytes());
                    } else { 
                         debug.write(("error " + error + "\n").getBytes());
                        throwIOException(error);
                    }
                }
                debug.write("...done\n".getBytes());
                off += lpNumberOfBytesWritten.getValue();
                len -= lpNumberOfBytesWritten.getValue();
            }
            Kernel32.INSTANCE.CloseHandle(event);
            Kernel32.INSTANCE.FlushFileBuffers(handle);
        }

    }
    public static interface Kernel32Ext extends Library, StdCallLibrary {
        Kernel32Ext INSTANCE = (Kernel32Ext)
            Native.loadLibrary("kernel32", Kernel32Ext.class);
        public boolean GetOverlappedResult(WinNT.HANDLE hFile, Pointer lpOverlapped, IntByReference lpNumberOfBytesTransferred, boolean bWait);
        public boolean ReadFile(WinNT.HANDLE hFile, Memory buffer, int len, IntByReference lpNumberOfBytesRead, Pointer lpOverlapped);
    }
//</editor-fold>

    private static Pipe unixPipe() throws IOException {
        throw new UnsupportedOperationException();
    }
//        Path name = Places.getCacheSubfile("socket/server/debugger").toPath();
//        Files.deleteIfExists(name);
//        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(name);
//        ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
//        
//        server.bind(socketAddress);
//        Thread listeningThread = new Thread(prefix + " listening at pipe " + name) {
//            @Override
//            public void run() {
//                while (true) {
//                    SocketChannel socket = null;
//                    try {
//                        socket = server.accept();
//                        close.add(socket);
//                        connectToSocket(socket, name.toString(), prefix, session, serverSetter, launcher);
//                    } catch (IOException ex) {
//                        if (isClosed(server)) {
//                            break;
//                        }
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//            }
//        };
//        listeningThread.start();
//        out.write((prefix + " listening at pipe " + name.toString()).getBytes());
//        out.flush();
//
//Pair.of(Channels.newInputStream(socket), Channels.newOutputStream(socket)), session);    } 
}
