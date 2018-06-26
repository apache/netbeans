/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FIXME this is copied from extexecution API to be used in KillableProcess.
 * Anyway we should remove the KillableProcess with a native solution.
 *
 * @author Petr Hejl
 */
public final class ProcessInputStream extends FilterInputStream {

    private static final Logger LOGGER = Logger.getLogger(ProcessInputStream.class.getName());

    private final Process process;

    private byte[] buffer;

    private int position;

    private boolean closed;

    private boolean exhausted;

    public ProcessInputStream(Process process, InputStream in) {
        super(in);
        this.process = process;
    }

    @Override
    public synchronized int available() throws IOException {
        if (buffer != null && position < buffer.length) {
            return buffer.length - position;
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return 0;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.available();
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            close(false);
        }
    }

    @Override
    public void mark(int readlimit) {
        // noop
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized int read() throws IOException {
        if (buffer != null && position < buffer.length) {
            return buffer[position++];
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return -1;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (buffer != null) {
            int available = buffer.length - position;
            int size = Math.min(len, available);
            System.arraycopy(buffer, position, b, off, size);
            position += size;
            return size;
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return -1;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        // noop
    }

    @Override
    public long skip(long n) throws IOException {
        return 0;
    }

    public synchronized void close(boolean drain) throws IOException {
        closed = true;

        if (drain) {
            LOGGER.log(Level.FINE, "Draining process stream");

            boolean running = false;
            try {
                process.exitValue();
            } catch (IllegalThreadStateException ex) {
                running = true;
            }

            if (running) {
                LOGGER.log(Level.FINE, "Process is still running");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                if (running) {
                    while (super.available() > 0) {
                        os.write(super.read());
                    }
                } else {
                    int read;
                    while ((read = super.read()) >= 0) {
                        os.write(read);
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            buffer = os.toByteArray();
            LOGGER.log(Level.FINE, "Read {0} bytes from stream", buffer.length);
        }

        super.close();
    }
}
