/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.jsch;

import com.jcraft.jsch.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openide.util.Exceptions;

/**
 *
 * @author akrasny
 */
public final class MeasurableSocketFactory implements SocketFactory {

    private static final MeasurableSocketFactory instance = new MeasurableSocketFactory();
    private final CopyOnWriteArrayList<IOListener> listeners = new CopyOnWriteArrayList<>();

    private MeasurableSocketFactory() {
    }

    public static MeasurableSocketFactory getInstance() {
        return instance;
    }

    public void addIOListener(IOListener listener) {
        listeners.add(listener);
    }

    public void removeIOListener(IOListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
            throw ex;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw ex;
        }
        return socket;
    }

    @Override
    public InputStream getInputStream(Socket socket) throws IOException {
        return new MyIS(socket.getInputStream());
    }

    @Override
    public OutputStream getOutputStream(Socket socket) throws IOException {
        return new MyOS(socket.getOutputStream());
    }

    private void fireDownload(int bytes) {
        for (IOListener l : listeners) {
            l.bytesDownloaded(bytes);
        }
    }

    private void fireUpload(int bytes) {
        for (IOListener l : listeners) {
            l.bytesUploaded(bytes);
        }
    }

    public static interface IOListener {

        public void bytesUploaded(int bytes);

        public void bytesDownloaded(int bytes);
    }

    private final class MyIS extends InputStream {

        private final InputStream in;

        MyIS(InputStream is) {
            this.in = is;
        }

        @Override
        public int read() throws IOException {
            int res = in.read();
            fireDownload(res);
            return res;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int res = in.read(b);
            fireDownload(res);
            return res;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int res = in.read(b, off, len);
            fireDownload(res);
            return res;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }
    }

    private final class MyOS extends OutputStream {

        private final OutputStream out;

        MyOS(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            fireUpload(1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            fireUpload(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            fireUpload(len);
        }

        @Override
        public void close() throws IOException {
            try {
                out.flush();
            } finally {
                out.close();
            }
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }
    }
}
