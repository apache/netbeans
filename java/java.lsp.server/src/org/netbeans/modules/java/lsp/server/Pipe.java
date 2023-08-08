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
import java.lang.reflect.Method;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
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
            return unixPipe(prefix);
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
    private static Pipe windowsPipe(String prefix) throws IOException {
        String name = "\\\\.\\pipe\\netbeans-" + prefix;

        return new Pipe(name) {
            @Override
            public Connection connect() throws IOException {
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
                //TODO: close the last handle?
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

        private final WinNT.HANDLE handle;

        public HandleInputStream(WinNT.HANDLE handle) {
            this.handle = handle;
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
            WinNT.HANDLE event = Kernel32.INSTANCE.CreateEvent(null, true, false, null);
            OVERLAPPED overlapped = new OVERLAPPED();
            overlapped.hEvent = event;
            overlapped.write();

            try (Memory buffer = new Memory(len)) {
                IntByReference lpNumberOfBytesRead = new IntByReference(0);
                boolean result = Kernel32Ext.INSTANCE.ReadFile(handle, buffer, len, lpNumberOfBytesRead, overlapped.getPointer());

                if (!result) {
                    int error = Kernel32.INSTANCE.GetLastError();
                    if (error == Kernel32.ERROR_IO_PENDING) {
                        Kernel32.INSTANCE.WaitForSingleObject(event, Kernel32.INFINITE);

                        if (!Kernel32Ext.INSTANCE.GetOverlappedResult(handle, overlapped.getPointer(), lpNumberOfBytesRead, true)) {
                            throwIOException();
                        }
                    } else if (error == Kernel32.ERROR_BROKEN_PIPE) {
                        return -1;
                    } else {
                        throwIOException(error);
                    }
                }

                Kernel32.INSTANCE.CloseHandle(event);

                int read = lpNumberOfBytesRead.getValue();

                buffer.read(0, b, off, read);
                return read;
            }
        }

    }

    private static final class HandleOutputStream extends OutputStream {

        private final WinNT.HANDLE handle;

        public HandleOutputStream(WinNT.HANDLE handle) {
            this.handle = handle;
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] {(byte) b});
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            WinNT.HANDLE event = Kernel32.INSTANCE.CreateEvent(null, true, false, null);
            OVERLAPPED overlapped = new OVERLAPPED();

            while (len > 0) {
                try (Memory buffer = new Memory(len)) {
                    buffer.write(0, b, off, len);

                    overlapped.hEvent = event;
                    overlapped.write();

                    IntByReference lpNumberOfBytesWritten = new IntByReference(0);
                    boolean result = Kernel32Ext.INSTANCE.WriteFile(handle, buffer, len, lpNumberOfBytesWritten, overlapped.getPointer());

                    if (!result) {
                        int error = Kernel32.INSTANCE.GetLastError();

                        if (error == Kernel32.ERROR_IO_PENDING) {
                            Kernel32.INSTANCE.WaitForSingleObject(event, Kernel32.INFINITE);

                            if (!Kernel32Ext.INSTANCE.GetOverlappedResult(handle, overlapped.getPointer(), lpNumberOfBytesWritten, true)) {
                                throwIOException();
                            }
                        } else {
                            throwIOException(error);
                        }
                    }

                    off += lpNumberOfBytesWritten.getValue();
                    len -= lpNumberOfBytesWritten.getValue();
                }
                Kernel32.INSTANCE.CloseHandle(event);
            }
        }

    }
    public static interface Kernel32Ext extends Library, StdCallLibrary {
        Kernel32Ext INSTANCE = (Kernel32Ext)
            Native.loadLibrary("kernel32", Kernel32Ext.class);
        public boolean GetOverlappedResult(WinNT.HANDLE hFile, Pointer lpOverlapped, IntByReference lpNumberOfBytesTransferred, boolean bWait);
        public boolean ReadFile(WinNT.HANDLE hFile, Memory buffer, int len, IntByReference lpNumberOfBytesRead, Pointer lpOverlapped);
        public boolean WriteFile(WinNT.HANDLE hFile, Memory buffer, int len, IntByReference lpNumberOfBytesRead, Pointer lpOverlapped);
    }
//</editor-fold>

    private static Pipe unixPipe(String prefix) throws IOException {
        try {
            Path name = Places.getCacheSubfile("socket/server/" + shorten(prefix)).toPath();
            Files.deleteIfExists(name);
            Class<?> unixAddress = Class.forName("java.net.UnixDomainSocketAddress");
            Method unixAddressOf = unixAddress.getDeclaredMethod("of", Path.class);
            Method open = ServerSocketChannel.class.getMethod("open", ProtocolFamily.class);
            ServerSocketChannel server = (ServerSocketChannel) open.invoke(null, StandardProtocolFamily.valueOf("UNIX"));

            server.bind((SocketAddress) unixAddressOf.invoke(null, name));

            return new Pipe(name.toString()) {
                @Override
                public Connection connect() throws IOException {
                    SocketChannel socket = server.accept();

                    return new Connection(Channels.newInputStream(socket), Channels.newOutputStream(socket)) {
                        public void close() throws IOException {
                            socket.close();
                        }
                    };
                }

                @Override
                public void close() throws Exception {
                    server.close();
                }
            };
        } catch (ReflectiveOperationException ex) {
            throw new IOException(ex);
        }
    }

    private static String shorten(String str) {
        StringBuilder sb = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
