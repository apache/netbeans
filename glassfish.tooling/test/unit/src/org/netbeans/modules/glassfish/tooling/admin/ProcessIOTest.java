/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;


/**
 * Test process IO handlers.
 * <p/>
 * @author Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class ProcessIOTest {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Generate process IO data for test.
     */
    private static class DataSrc implements Runnable {

        /** Process standard input. */
        private final Reader srcIn ;

        /** Process standard output. */
        private final Writer srcOut;

        /** Content of process standard output. */
        private final String[] dataOut;

        /**
         * Create an instance of process IO data generator.
         * <p/>
         * @param in      Process standard input (second side of pipe
         *                to connect).
         * @param out     Process standard output (second side of pipe
         *                to connect).
         * @param dataOut Content of process standard output to be sent trough
         *                the pipe.
         * @throws IOException When there is an issue with connecting pipes
         *         or opening local {@link Reader}s and {@link Writer}s.
         */
        private DataSrc(final PipedOutputStream in, final PipedInputStream out,
                final String[] dataOut) throws IOException {
            try {
                srcIn = new InputStreamReader(new PipedInputStream(in));
                srcOut = new OutputStreamWriter(new PipedOutputStream(out));
                this.dataOut = dataOut != null ? dataOut : new String[0];
            } catch (IOException ex) {
                close();
                throw ex;
            }
            
        }

        /**
         * Thread main method.
         */
        @Override
        public void run() {
            final String METHOD = "run";
            int len = dataOut.length;
            try {
                for (int i = 0; i < len; i++) {
                    if (i < dataOut.length) {
                        srcOut.write(dataOut[i]);
                    }
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO,
                        METHOD, "io", ioe.getLocalizedMessage());
            } finally {
                close();
            }
        }

        /**
         * Close {@link Reader}s and {@link Writer}s.
         */
        private void close() {
            final String METHOD = "threadClose";
            try {
                if (srcIn != null) {
                    srcIn.close();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO,
                        METHOD, "in", ioe.getLocalizedMessage());
            } try {
                if (srcOut != null) {
                    srcOut.close();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO,
                        METHOD, "out", ioe.getLocalizedMessage());
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ProcessIOTest.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Close process IO streams.
     * <p/>
     * @param in  Process input stream {@link Writer}.
     * @param out Process output stream {@link Reader}.
     * @param err Process error output stream {@link Reader}.
     */
    private void close(final Writer in, final Reader out) {
        final String METHOD = "close";
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO,
                    METHOD, "in", ioe.getLocalizedMessage());
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO,
                    METHOD, "out", ioe.getLocalizedMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test change administrator's password command execution using
     * local asadmin interface on GlassFish v3.
     */
    @Test
    public void testProcessIOSuccess() {
        final String METHOD = "testProcessIOSuccess";
        final GlassFishServer server = CommandRestTest.glassFishServer();
        final PipedOutputStream in = new PipedOutputStream();
        final PipedInputStream out = new PipedInputStream();
        final Writer wIn = new OutputStreamWriter(in);
        final Reader rOut = new InputStreamReader(out);
        final ProcessIOContent content = new ProcessIOContent();
        content.addOutput(
                new String[] {"Command", "successfully"},
                new String[] {"failed", "error"});
        ProcessIOResult result = ProcessIOResult.UNKNOWN;
        try {
            final DataSrc dataSrc = new DataSrc(in, out, new String[] {
                "Command executed successfully."});
            final Thread dataThread = new Thread(dataSrc);
            dataThread.start();
            final ProcessIOParser parser
                    = new ProcessIOParser(wIn, rOut, content);
            result = parser.verify();  
            String output = parser.getOutput();
            LOGGER.log(Level.INFO, METHOD, "output", output);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO,
                    METHOD, "thread", ioe.getLocalizedMessage());
        } finally {
            close(wIn, rOut);
        }
        assertTrue(result == ProcessIOResult.SUCCESS,
                "Expected result: SUCCESS");
    }

    /**
     * Test change administrator's password command execution using
     * local asadmin interface on GlassFish v3.
     */
    @Test(dependsOnMethods = "testProcessIOSuccess")
    public void testProcessIOFailure() {
        final String METHOD = "testProcessIOFailure";
        final GlassFishServer server = CommandRestTest.glassFishServer();
        final PipedOutputStream in = new PipedOutputStream();
        final PipedInputStream out = new PipedInputStream();
        final Writer wIn = new OutputStreamWriter(in);
        final Reader rOut = new InputStreamReader(out);
        final ProcessIOContent content = new ProcessIOContent();
        content.addOutput(
                new String[] {"Command", "successfully"},
                new String[] {"failed", "error"});
        ProcessIOResult result = ProcessIOResult.UNKNOWN;
        try {
            final DataSrc dataSrc = new DataSrc(in, out, new String[] {
                "Command execution failed."});
            final Thread dataThread = new Thread(dataSrc);
            dataThread.start();
            final ProcessIOParser parser
                    = new ProcessIOParser(wIn, rOut, content);
            result = parser.verify();            
            String output = parser.getOutput();
            LOGGER.log(Level.INFO, METHOD, "output", output);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO,
                    METHOD, "thread", ioe.getLocalizedMessage());
        } finally {
            close(wIn, rOut);
        }
        assertTrue(result == ProcessIOResult.ERROR,
                "Expected result: ERROR");
    }

}
