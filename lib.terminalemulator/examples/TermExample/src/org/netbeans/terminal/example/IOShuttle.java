/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.terminal.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.openide.ErrorManager;
import org.openide.windows.OutputWriter;

/**
 * Shuttles data between the io of a {@link java.lang.Process}, or any
 * sort of derivative/mimic, and an NB-style IOProvider.
 * @author ivan
 */
final class IOShuttle {

    private final OutputMonitor outputMonitor;
    private final InputMonitor inputMonitor;

    public IOShuttle(OutputStream pin, InputStream pout,
                      OutputWriter toIO, Reader fromIO) {
        InputStreamReader fromProc = new InputStreamReader(pout);
        outputMonitor = new IOShuttle.OutputMonitor(fromProc, toIO);
        if (fromIO != null) {
            final OutputStreamWriter toProc = new OutputStreamWriter(pin);
            inputMonitor = new IOShuttle.InputMonitor(fromIO, toProc);
        } else {
            inputMonitor = null;
        }
    }

    public void run() {
        outputMonitor.start();
        if (inputMonitor != null)
            inputMonitor.start();
    }

    private static class OutputMonitor extends Thread {

        private final InputStreamReader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        OutputMonitor(InputStreamReader reader, Writer writer) {
            super("ExecutorUnix.OutputMonitor");
            // NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                }
            } catch (IOException e) {
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }

    /**
     * Copies kestrokes in io window to external process.
     */
    private static class InputMonitor extends Thread {

        private final Reader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        InputMonitor(Reader reader, Writer writer) {
            super("ExecutorUnix.InputMonitor");			// NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                    writer.flush();
                }
                writer.close();
            } catch (IOException e) {
		// no-op
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
