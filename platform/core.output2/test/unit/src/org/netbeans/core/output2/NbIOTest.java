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

package org.netbeans.core.output2;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import junit.framework.TestCase;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public class NbIOTest extends TestCase {

    public NbIOTest(String testName) {
        super(testName);
    }

    public void test54117() throws Exception {
        NbIO io = new NbIO("test");
        assertFalse(io.isClosed());
        Reader str = io.getIn();
        assertNotNull(str);
        assertEquals(NbIO.IOReader.class, str.getClass());
        writeText(str);
        int read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        //reseting
        io.getOut().close();
        io.getErr().close();
        io.dispose();
        io.getOut().reset();
        io.getErr().reset();
        
        str = io.getIn();
        writeText(str);
        read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        
    }
    
    private void writeText(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.pushText("hello");

    }
    private void writeEof(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.eof();
    }
    
    public void testSynchronization223370SeveralTimes() {
        for (int i = 0; i < 10; i++) {
            try {
                checkSynchronization223370();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void testLimit() throws IOException {
        NbIO io = new NbIO("test");
        io.getOut().println("first");
        NbWriter nbWriter = (NbWriter) io.getOut();
        OutWriter ow = nbWriter.out();
        AbstractLines lines = (AbstractLines) ow.getLines();
        lines.setOutputLimits(new OutputLimits(6, 1024, 3));
        assertEquals(2, lines.getLineCount());
        io.getOut().println("second");
        io.getOut().println("third");
        OutputListener ol = new OutputListener() {
            @Override
            public void outputLineSelected(OutputEvent ev) {
            }

            @Override
            public void outputLineAction(OutputEvent ev) {
            }

            @Override
            public void outputLineCleared(OutputEvent ev) {
            }
        };
        IOColorLines.println(io, "fourth", ol, true, Color.yellow);
        assertNotNull(lines.getLineInfo(3).getFirstListener(new int[]{1, 2}));

        IOColorLines.println(io, "fifth", ol, false, Color.red);

        assertEquals(3, lines.getLineCount());

        assertNotNull("fourth\n", lines.getLine(0));
        assertNotNull(lines.getLineInfo(0).getFirstListener(new int[2]));
        assertTrue(lines.isImportantLine(0));
        assertEquals(Color.yellow, lines.getLineInfo(0).getLineSegments()
                .iterator().next().getColor());


        assertEquals("fifth\n", lines.getLine(1));
        assertNotNull(lines.getLineInfo(1).getFirstListener(new int[2]));
        assertFalse(lines.isImportantLine(1));
        assertEquals(Color.red, lines.getLineInfo(1).getLineSegments()
                .iterator().next().getColor());

        IOColorLines.println(io, "sixth", ol, true, Color.green);
        assertEquals("sixth\n", lines.getLine(2));
        assertNotNull(lines.getLineInfo(2).getFirstListener(new int[2]));
        assertTrue(lines.isImportantLine(2));
        assertEquals(Color.green, lines.getLineInfo(2).getLineSegments()
                .iterator().next().getColor());

        assertEquals("", lines.getLine(3));
    }

    private void checkSynchronization223370() throws InterruptedException {
        final NbIO nbio = new NbIO("test223370");
        final int[] nullOuts = new int[1];
        final int[] nullIns = new int[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    OutputWriter out = nbio.getOut();
                    Reader in = nbio.getIn();
                    if (out == null) {
                        synchronized (nullOuts) {
                            nullOuts[0]++;
                        }
                    }
                    if (in == null) {
                        synchronized (nullIns) {
                            nullIns[0]++;
                        }
                    }
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                for (int i = 0; i < 10000; i++) {
                    nbio.closeInputOutput();
                }
            }
        });
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                for (int i = 0; i < 10000; i++) {
                    nbio.dispose();
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
        synchronized (nullIns) {
            assertEquals(0, nullIns[0]);
        }
        synchronized (nullOuts) {
            assertEquals(0, nullOuts[0]);
        }
    }

    public void testIOColorsImpl() {
        NbIO io = new NbIO("test");
        io.getOptions().setColorStandard(Color.RED);
        io.getOptions().setColorError(Color.WHITE);
        io.getOptions().setColorInput(Color.YELLOW);
        io.getOptions().setColorLink(Color.PINK);
        io.getOptions().setColorLinkImportant(Color.LIGHT_GRAY);
        io.getOptions().setColorDebug(Color.CYAN);
        io.getOptions().setColorWarning(Color.BLACK);
        io.getOptions().setColorFailure(Color.MAGENTA);
        io.getOptions().setColorSuccess(Color.BLUE);
        assertEquals(Color.RED,
                IOColors.getColor(io, IOColors.OutputType.OUTPUT));
        assertEquals(Color.WHITE,
                IOColors.getColor(io, IOColors.OutputType.ERROR));
        assertEquals(Color.YELLOW,
                IOColors.getColor(io, IOColors.OutputType.INPUT));
        assertEquals(Color.PINK,
                IOColors.getColor(io, IOColors.OutputType.HYPERLINK));
        assertEquals(Color.LIGHT_GRAY,
                IOColors.getColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT));
        assertEquals(Color.CYAN,
                IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
        assertEquals(Color.BLACK,
                IOColors.getColor(io, IOColors.OutputType.LOG_WARNING));
        assertEquals(Color.MAGENTA,
                IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE));
        assertEquals(Color.BLUE,
                IOColors.getColor(io, IOColors.OutputType.LOG_SUCCESS));
    }

    public void testIOColorsImplSetting() {
        NbIO io = new NbIO("test");
        io.getOptions().setColorStandard(Color.ORANGE);
        io.getOptions().setColorDebug(Color.WHITE);
        assertEquals(Color.ORANGE,
                IOColors.getColor(io, IOColors.OutputType.OUTPUT));
        assertEquals(Color.WHITE,
                IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
        IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.BLUE);
        IOColors.setColor(io, IOColors.OutputType.LOG_DEBUG, Color.GREEN);
        assertEquals(Color.BLUE,
                IOColors.getColor(io, IOColors.OutputType.OUTPUT));
        assertEquals(Color.GREEN,
                IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
    }

    public void testBug201450() {
        NbIO io = new NbIO("test201450");
        io.getOut().println(randomString(1024, 1024));
        io.getOut().println(randomString(1024, 1024));
        io.dispose();
        io.closeInputOutput();
    }

    private String randomString(int lines, int lineLength) {
        StringBuilder sb = new StringBuilder();
        char lastChar = ' ';
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < lineLength; j++) {
                char c = (char) ((r.nextDouble() * 127) + 1);
                if (lastChar == '\u001B' && c == '[') { // prevent ANSI seqs
                    continue;
                }
                sb.append(c);
                lastChar = c;
            }
            sb.append('\t');
            sb.append('\b');
            sb.append('\n');
        }
        return sb.toString();
    }

    public void testBug201450AndMultipleErases() {
        NbIO io = new NbIO("test201450bb");
        io.getOut().println("abcdef\t\t\b\b\b\b\th");
        io.dispose();
        io.closeInputOutput();
    }

    /**
     * Test for bug 236128 - Dead lock after some clean in output log.
     *
     * This is not a good example of simulation of deadlock, but adding some
     * synchronization points to the code could affect performance, which is
     * something that should be prevented, if possible, in output window.
     *
     * @throws java.lang.InterruptedException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void testBug236128() throws InterruptedException,
            InvocationTargetException {

        for (int i = 0; i < 10; i++) {
            final NbIO io[] = new NbIO[1];

            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    io[0] = (NbIO) NbIOProvider.getDefault().getIO("Test", false);
                    io[0].setInputVisible(true);
                    io[0].getIn();
                }
            });
            assertNotNull(io[0]);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000; i++) {
                        io[0].getOut().println("Test from thread t");
                    }
                }
            });

            final Semaphore s = new Semaphore(0);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    io[0].getOut().println("Test from EDT");
                    io[0].getIOContainer().open();
                    io[0].reset();
                    s.release();
                }
            });
            t.start();
            s.acquire();
            t.join();
            io[0].closeInputOutput();
        }
    }
}
