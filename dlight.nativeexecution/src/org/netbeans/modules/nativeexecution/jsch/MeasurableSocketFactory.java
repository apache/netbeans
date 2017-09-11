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
