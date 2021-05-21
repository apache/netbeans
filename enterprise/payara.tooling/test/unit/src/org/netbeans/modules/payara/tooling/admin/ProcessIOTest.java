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
package org.netbeans.modules.payara.tooling.admin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.logging.Logger;
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
     * local asadmin interface on Payara.
     */
    @Test
    public void testProcessIOSuccess() {
        final String METHOD = "testProcessIOSuccess";
        final PayaraServer server = CommandRestTest.payaraServer();
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
     * local asadmin interface on Payara.
     */
    @Test(dependsOnMethods = "testProcessIOSuccess")
    public void testProcessIOFailure() {
        final String METHOD = "testProcessIOFailure";
        final PayaraServer server = CommandRestTest.payaraServer();
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
