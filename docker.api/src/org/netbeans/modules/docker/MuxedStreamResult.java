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
package org.netbeans.modules.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class MuxedStreamResult implements StreamResult {

    private static final Logger LOGGER = Logger.getLogger(MuxedStreamResult.class.getName());

    private final Endpoint s;

    private final Charset charset;

    private final OutputStream outputStream;

    private final Demuxer demultiplexer;

    private final InputStream stdOut;

    private final InputStream stdErr;

    private StreamItem last = StreamItem.EMPTY;

    public MuxedStreamResult(Endpoint s, Charset charset, InputStream is) throws IOException {
        this.s = s;
        this.charset = charset;
        this.outputStream = s.getOutputStream();
        this.demultiplexer = new Demuxer(is == null ? s.getInputStream() : is);
        this.stdOut = new ResultInputStream(false);
        this.stdErr = new ResultInputStream(true);
    }

    public OutputStream getStdIn() {
        return outputStream;
    }

    public InputStream getStdOut() {
        return stdOut;
    }

    public InputStream getStdErr() {
        return stdErr;
    }

    @Override
    public boolean hasTty() {
        return false;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void close() {
        try {
            s.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    private class ResultInputStream extends InputStream {

        private final boolean error;

        public ResultInputStream(boolean error) {
            this.error = error;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            synchronized (MuxedStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                int limit = Math.min(len, last.getData().remaining());
                last.getData().get(b, off, limit);
                return limit;
            }
        }

        @Override
        public int read() {
            synchronized (MuxedStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                return last.getData().get();
            }
        }

        private int fetchData() {
            synchronized (MuxedStreamResult.this) {
                try {
                    if (last == null) {
                        return -1;
                    }
                    while (!last.getData().hasRemaining()) {
                        last = demultiplexer.fetch();
                        if (last == null) {
                            return -1;
                        }
                    }
                } finally {
                    MuxedStreamResult.this.notifyAll();
                }

                try {
                    while (last != null && last.isError() != error) {
                        MuxedStreamResult.this.wait();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                if (last == null) {
                    return -1;
                }
                return last.getData().remaining();
            }
        }
    }
}
