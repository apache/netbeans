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
package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOPosition;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tomas Holy
 */
public class IOExtensionsTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(IOExtensionsTest.class);
    }

    public IOExtensionsTest(String name) {
        super(name);
    }
    private IOContainer iowin;
    private NbIO io;
    private AbstractOutputPane pane;
    private JFrame jf = null;
    static int testNum;

    @Override
    protected void setUp() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                iowin = IOContainer.getDefault();
                JComponent wnd = LifecycleTest.getIOWindow();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(wnd, BorderLayout.CENTER);
                jf.setBounds(20, 20, 700, 300);
                jf.setVisible(true);
                io = (NbIO) new NbIOProvider().getIO("Test" + testNum++, false);
                pane = ((OutputTab) iowin.getSelected()).getOutputPane();
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        io.closeInputOutput();
        jf.dispose();
    }

    public void testSetDefColors() throws IOException {
        IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.GRAY);
        IOColors.setColor(io, IOColors.OutputType.ERROR, Color.PINK);
        IOColors.setColor(io, IOColors.OutputType.INPUT, Color.BLUE);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK, Color.MAGENTA);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.GREEN);
        io.getOut().println("Test out");
        io.getErr().println("Test err");
        io.getOut().println("Test hyperlink", new L(), false);
        io.getOut().println("Test important hyperlink", new L(), true);
        IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.BLACK);
        IOColors.setColor(io, IOColors.OutputType.ERROR, Color.RED);
        IOColors.setColor(io, IOColors.OutputType.INPUT, Color.BLACK);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK, Color.BLUE);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.MAGENTA);
    }

    public void testVertScroll() {
        for (int i = 0; i < 10; i++) {
            io.getOut().println("Test line " + i);
        }
        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);
        for (int i = 0; i < 50; i++) {
            io.getOut().println("Another test line " + i);
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int line = pane.getCaretLine();
        assertEquals(10, line);
    }

    public void testHorzScroll() {
        for (int i = 0; i < 10; i++) {
            io.getOut().print("Test part" + i + ". ");
        }
        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);
        for (int i = 0; i < 10; i++) {
            io.getOut().print("Another test " + i + ". ");
        }
        for (int i = 0; i < 50; i++) {
            io.getOut().println("New line " + i);
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int p = pane.getCaretPos();
        assertEquals(120, p);
    }

    public void testWrappedScroll() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ((OutputTab) iowin.getSelected()).getOutputPane().setWrapped(true);
            }
        });

        for (int i = 0; i < 100; i++) {
            io.getOut().print("Test part" + i + ". ");
        }

        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);

        for (int k = 0; k < 5; k++) {
            io.getOut().println();
            for (int i = 0; i < 100; i++) {
                io.getOut().print("Another part " + k + "_" + i + ". ");
            }
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int p = pane.getCaretPos();
        assertEquals(1290, p);
    }

    public void testColorLinePrinting() throws IOException {
        for (int i = 0; i < 11; i++) {
            int c = 55 + i * 20;
            IOColorLines.println(io, "Test line " + i, new Color(c, 0, 0));
            IOColorLines.println(io, "Test line " + i, new Color(0, c, 0));
            IOColorLines.println(io, "Test line " + i, new Color(0, 0, c));
        }
        IOColorLines.println(io, "Line with listener", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "          Listener starting with spaces", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "Important listener ended by spaces  ", new L(), true, new Color(0, 100, 255));
        IOColorLines.println(io, "\tMultiple\nline listener containing tabs\t...", new L(), true, new Color(50, 100, 55));
        IOColorLines.println(io, "\tListener started by tab", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "          Line with listener started by spaces, ended by tab\t", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "\tLine with\timportant\tlistener full of tabs\t", new L(), true, new Color(0, 100, 255));

        for (int i = 0; i < 50; i++) {
            io.getOut().println(i);
        }

        StringBuilder longLine = new StringBuilder("Long line ...");
        for (int i = 0; i < 100; i++) {
            longLine.append("...test " + i + " ...");
        }
        IOColorLines.println(io, longLine, new Color(255, 0, 0));
        IOColorLines.println(io, longLine, new L(), false, new Color(255, 0, 0));
    }

    public void testColorPrinting() throws IOException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            int c = 55 + i * 20;
            IOColorPrint.print(io, "Test line " + i, new Color(c, 0, 0));
            IOColorPrint.print(io, "more more " + i, new Color(0, c, 0));
            IOColorLines.println(io, "of them " + i, new Color(0, 0, c));
        }
        IOColorPrint.print(io, "1st with listeners\t", new L(), false, new Color(120, 120, 0));
        IOColorPrint.print(io, "          2nd listener", new L(), false, new Color(120, 255, 0));
        IOColorPrint.print(io, "   Listener \twith tab inside    \n", new L(), true, new Color(0, 100, 255));
        IOColorPrint.print(io, "\t\t\t\t   Important listener surounded by tabs\t\t\t", new L(), true, new Color(0, 100, 255));

        IOColorPrint.print(io, "No listener", new Color(120, 120, 0));
        IOColorPrint.print(io, "listener", new L(), false, new Color(120, 255, 0));
        IOColorPrint.print(io, "No listener\n", new Color(255, 120, 0));
        IOColorPrint.print(io, "\tMultiple\nline listener\t...\n", new L(), true, new Color(50, 100, 55));
        IOColorPrint.print(io, "\tMultiple line,\nmultiple tabs\t\n\tlistener\t...\n", new L(), true, new Color(150, 100, 55));

        IOColorPrint.print(io, "colored part", Color.MAGENTA);
        io.getOut().print("normal");
        io.getErr().print("error\n");

        io.getOut().print("normal");
        IOColorPrint.print(io, "listener", new L(), false, Color.MAGENTA);
        io.getErr().println("error");

        io.getOut().print("normal");
        IOColorPrint.print(io, "\tlistener\t", new L(), false, Color.MAGENTA);
        io.getOut().println("normal");

        for (int i = 0; i < 50; i++) {
            io.getOut().println(i);
        }

        StringBuilder longLine = new StringBuilder("Long line ...");
        for (int i = 0; i < 100; i++) {
            longLine.append("...test " + i + " ...");
        }
        IOColorPrint.print(io, longLine, new Color(255, 0, 0));
        IOColorPrint.print(io, longLine, new L(), false, new Color(255, 0, 0));
    }

    public class L implements OutputListener {

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
        }
    }

    private void waitEq() {
        try {
            Thread.sleep(500);
            while (IOEvent.pendingCount > 0) {
                Thread.sleep(10);
            }
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                }
            });
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
