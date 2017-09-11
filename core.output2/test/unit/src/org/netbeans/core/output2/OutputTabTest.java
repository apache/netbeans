/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * @author jhavlin
 */
public class OutputTabTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless()
                ? new TestSuite()
                : new TestSuite(OutputTabTest.class);
    }

    public OutputTabTest(String name) {
        super(name);
    }
    private IOContainer container;
    private NbIO io;
    JFrame jf = null;
    OutputTab tab = null;
    OutputPane pane = null;

    @Override
    protected void setUp() throws java.lang.Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                container = IOContainer.getDefault();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(LifecycleTest.getIOWindow(),
                        BorderLayout.CENTER);
                jf.setBounds(20, 20, 700, 300);
                jf.setVisible(true);
                io = (NbIO) new NbIOProvider().getIO("Test", false);
                io.select();
                tab = (OutputTab) container.getSelected();
                pane = (OutputPane) tab.getOutputPane();
            }
        });
        if (tab == null) {
            fail("Failed in setup - selected tab was null");
        }
    }

    @Override
    protected void tearDown() {
        tab = null;
        pane = null;
        if (jf != null) {
            jf.dispose();
        }
        jf = null;
        if (io != null) {
            NbIOProvider.dispose(io);
        }
        io.closeInputOutput();
        io = null;
        container = null;
    }

    /**
     * Bug 179768 - javax.swing.text.StateInvariantError: Bad caret position
     */
    public void testLineClicked() throws InterruptedException,
            InvocationTargetException {

        final NbWriter writer = io.writer();

        try {
            writer.println("text");
            writer.println("link", new ResetOnClickListener(writer));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail("Unable to write to writer.");
        }

        pane.sendCaretToPos(0, 0, false);

        final Semaphore s = new Semaphore(0);
        final ResultWrapper result = new ResultWrapper();

        // click the link
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger l = Logger.getLogger(Exceptions.class.getName());
                    Semaphore logSem = new Semaphore(0); // logging semaphore
                    ReleaseOnErrorHandler h = new ReleaseOnErrorHandler(logSem);
                    l.addHandler(h);
                    tab.lineClicked(1, 6);
                    boolean acquired = logSem.tryAcquire(1, 100,
                            TimeUnit.MILLISECONDS);
                    result.setErrorLogged(acquired);
                    l.removeHandler(h);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    s.release();
                }
            }
        });
        s.acquire();
        assertFalse("No error should be logged", result.isErrorLogged());
    }

    /**
     * Test for bug 217784.
     */
    public void testOptionsListener() throws InvocationTargetException,
            InterruptedException {

        final NbIO nbio = (NbIO) io;
        nbio.closeInputOutput();
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                nbio.getOptions().setColorError(Color.WHITE);
            }
        });
    }

    /**
     * Test for bug 230402 - NullPointerException at
     * org.netbeans.core.output2.ui.AbstractOutputPane.run. No exception should
     * be thrown.
     *
     * @throws java.lang.InterruptedException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void testEnsureCaretPosition() throws InterruptedException,
            InvocationTargetException {

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                tab.setDocument(new HTMLDocument());
                tab.getOutputPane().run();
            }
        });
    }

    /**
     * Test for bug 233924 - Can't reopen closed output window when using Maven.
     *
     * @throws java.lang.InterruptedException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void testWriterWillNotDisposeIfOutputReopened()
            throws InterruptedException, InvocationTargetException {
        OutWriter out = ((NbWriter) io.getOut()).out();
        out.println("x");
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                container.remove(tab);
            }
        });
        assertTrue(out.isDisposeOnClose());
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Controller.getDefault().performCommand(null, io,
                        IOEvent.CMD_CREATE, false, null); // create new tab for the IO.
            }
        });
        assertFalse(out.isDisposeOnClose());
        out.close();
    }

    /**
     * Log Handler that releases an internal semaphore if an error is logged.
     */
    private static class ReleaseOnErrorHandler extends Handler {

        private Semaphore s;

        public ReleaseOnErrorHandler(Semaphore s) {
            this.s = s;
        }

        @Override
        public void publish(LogRecord record) {

            System.out.println(record.getLevel());

            if ("SEVERE".equals(record.getLevel().toString())) {
                System.out.println("got it");
                s.release();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    /**
     * Class to wrap a result boolean value.
     */
    private static class ResultWrapper {

        private boolean errorLogged = false;

        public boolean isErrorLogged() {
            return errorLogged;
        }

        public void setErrorLogged(boolean value) {
            this.errorLogged = value;
        }
    }

    /**
     * Output Listener that resets the underlying writer if the line is clicked.
     */
    private static class ResetOnClickListener implements OutputListener {

        private final NbWriter writer;

        public ResetOnClickListener(NbWriter writer) {
            this.writer = writer;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            try {
                writer.reset();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }
}