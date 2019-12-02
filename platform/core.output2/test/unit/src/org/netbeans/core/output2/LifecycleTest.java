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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.IOSelect;
import org.openide.windows.OutputWriter;

/**
 *
 * @author tim
 */
@RandomlyFails // timed out in NB-Core-Build #3589; no thread dump despite timeOut override
public class LifecycleTest extends NbTestCase {

    public LifecycleTest(String testName) {
        super(testName);
    }

    protected @Override int timeOut() {
        return 300000;
    }

    private IOContainer container;
    private NbIO io;
    JFrame jf = null;

    OutputTab tab = null;
    OutputPane pane = null;
    @Override
    protected void setUp() throws java.lang.Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                container = IOContainer.getDefault();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(getIOWindow(), BorderLayout.CENTER);
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
        sleep();
    }
    
    private final void sleep() {
        dosleep();
        dosleep();
        dosleep();
        int ct = 0;
        while (IOEvent.pendingCount > 0) {
            dosleep();
            ct++;
            if (ct > 1000) {
                fail ("After 1000 cycles on the event queue, there is still some IOEvent which was not processed");
            }
        }
    }
    
    private final void dosleep() {
        try {
            Thread.sleep(200);
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            Thread.sleep(200);
        } catch (Exception e) {
            fail (e.getMessage());
        }
    }

    public void testGetErr() throws Exception {
        System.out.println("testGetOut");
        ErrWriter err = io.writer().err();
        assertNull ("Error output should not be created yet", err);
        err = io.writer().getErr();
        assertNotNull ("Error output should never be null from getErr()", err);
        assertTrue ("Error output should initially be closed", err.isClosed());
        err.println ("Hello");
        assertFalse ("Error output should not be closed after writing to it", err.isClosed());
        err.close();
        assertTrue ("Error output be closed after calling close()", err.isClosed());
    }

    public void testClose() throws Exception {
        System.out.println("testClose");
        NbWriter writer = (NbWriter) io.getOut();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        writer.reset();
        sleep();

        err.println ("hello");
        sleep();
        writer.println ("world");
        sleep();

//        assertTrue("Text in container not correct:\"" + pane.getTextView().getText() +"\"",
//            pane.getTextView().getText().equals ("hello\nworld\n\n\n"));

        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());

        err.close();
        sleep();
        assertFalse ("Out is open, err is closed, writer should return false from isClosed()", writer.isClosed());

        writer.close();
        sleep();
        assertTrue ("Out should be closed after calling close() on it", out.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());

        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());

        writer.reset();
        sleep();

        assertTrue ("After reset, err should be closed", err.isClosed());
        assertTrue ("After reset, writer should be closed", writer.isClosed());
        assertTrue ("After reset, out should be closed", out.isClosed());

        err.println ("goodbye");
        writer.println ("world");
        sleep();

        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());

        //Now close err & out in the opposite order
        writer.close();
        sleep();
        assertTrue ("Out should  be closed after calling close() on it", writer.isClosed());

        err.close();
        sleep();
        assertTrue ("Out is closed, err is closed, writer should return true from isClosed()", writer.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());

        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());

        err.println("I should be reopened now");
        sleep();

        assertFalse ("Err should be open", err.isClosed());
    }

    public void testReset() throws Exception {
        System.out.println("testReset");
        ErrWriter err = io.writer().getErr();
        OutWriter out = io.writer().out();
        NbWriter writer = io.writer();

        OutputDocument doc = (OutputDocument) pane.getDocument();
        assertNotNull ("Document should not be null", doc);

        err.println ("hello");
        writer.println ("world");
        sleep();
        writer.reset();
        sleep();

        assertTrue ("Same writer object should be used after a reset", io.writer() == writer);
        assertTrue ("Same err object should be used after a reset", io.writer().err() == err);
        assertTrue ("Different output should be used afer a reset", out != io.writer().out());

        assertNull ("Old document's Lines object not disposed - that means neither was its writer", doc.getLines());

        Exception e = null;
        try {
            out.getStorage();
        } catch (Exception exc) {
            e = exc;
        }
        assertNotNull ("OutWriter should have thrown an exception on trying to " +
            "fetch its storage after it was disposed.  It appears it wasn't disposed.", e);
    }

    public void testCloseInputOutput() throws Exception {

        System.out.println("testCloseInputOutput");
        ErrWriter err = io.writer().getErr();
        OutWriter out = io.writer().out();
        NbWriter writer = io.writer();

        err.println ("joy to the world");
        writer.println ("all the boys and girls");
        err.close();
        sleep();
        writer.close();
        sleep();
        io.closeInputOutput();
        sleep();
        assertNull ("Should be no selected tab after closeInputOutput", getSelectedTab());
    }

    public void testDisplayOfBackspace() throws Exception {

        OutWriter out = io.writer().out();
        out.println("Enter something:");
        out.print(">");
        sleep();
        out.println("\bPrompt removed.");
        sleep();
        out.println("xx");
        sleep();
        io.closeInputOutput();
    }

    public void testFilesCleanedUp() throws Exception {
        System.out.println("testFilesCleanedUp");
        NbWriter writer = io.writer();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        err.println ("hello");
        writer.println ("world");
        sleep();

        assertTrue ("Output should not have changed - was " + out + " now " + io.writer().out(), io.writer().out() == out);
        FileMapStorage storage = (FileMapStorage) writer.out().getStorage();
        String fname = storage.toString();
        assertTrue ("FileMapStorage should be returning a file name", fname.indexOf("[") == -1);
        assertTrue ("FileMapStorage should be pointing to an existing file", new File(fname).exists());

        err.close();
        sleep();
        writer.close();
        sleep();
        io.closeInputOutput();
        sleep();

        assertTrue (out.isDisposed());
        sleep();
//        assertFalse ("FileMapStorage's file should have been deleted", new File(fname).exists());
    }

    public void testFastResets() throws IOException, InterruptedException {
        System.out.println("testFastResets");
        OutputWriter out = io.getOut();
        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < 10; k++) {
                out.println(i + " " + k);
            }
            Thread.sleep(10);
            out.close();
            out.reset();
        }
    }

    public void testMultipleResetsAreHarmless() throws Exception {
        System.out.println("testMultipleResetsAreHarmless");
        NbWriter writer = io.writer();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        assertTrue ("Before any writes, out should be empty", out.isEmpty());

        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);

        writer.reset();
        writer.reset();
        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);

        writer.println ("Now there is data");
        writer.reset();
        sleep();

        assertFalse ("Reset on a used writer should replace its underlying output", writer.out() == out);

    }

    // Not really a Lifecycle feature but requires a bonafide TC container
    // which this Test provides.
    public void testFineSelect() {
        System.out.println("testFineSelect");

	EnumSet<IOSelect.AdditionalOperation> extraOps;

	extraOps = null;
	boolean sawException = false;
	try {
	    IOSelect.select(io, extraOps);
	} catch (NullPointerException ex) {
	    // extraOps is NonNull
	    sawException = true;
	}
	assertTrue("null pointer not caught", sawException);
	sleep();

	extraOps = EnumSet.noneOf(IOSelect.AdditionalOperation.class);
	IOSelect.select(io, extraOps);
	sleep();

	extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN);
	IOSelect.select(io, extraOps);
	sleep();

	extraOps = EnumSet.of(IOSelect.AdditionalOperation.REQUEST_ACTIVE);
	IOSelect.select(io, extraOps);
	sleep();

	extraOps = EnumSet.of(IOSelect.AdditionalOperation.REQUEST_VISIBLE);
	IOSelect.select(io, extraOps);
	sleep();

	extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		              IOSelect.AdditionalOperation.REQUEST_VISIBLE);
	IOSelect.select(io, extraOps);
	sleep();

	extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		              IOSelect.AdditionalOperation.REQUEST_VISIBLE,
			      IOSelect.AdditionalOperation.REQUEST_ACTIVE);
	IOSelect.select(io, extraOps);
	sleep();
    }

    /**
     * Test for bug 239194 - NPE at
     * org.netbeans.core.output2.FoldingSideBar.paintComponent.
     *
     * @throws java.lang.InterruptedException
     */
    public void testBug239194() throws InterruptedException {
        final Semaphore s = new Semaphore(0);
        final Exception[] excRef = new Exception[1];
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                NbIO io = (NbIO) NbIOProvider.getDefault().getIO("bug239194",
                        true);
                try {
                    FoldingSideBar fsb = pane.getFoldingSideBar();
                    JTextComponent jtc = pane.getTextView();
                    jtc.setVisible(false);
                    jtc.setMinimumSize(new Dimension(-1, -1));
                    jtc.setSize(new Dimension(-1, -1));
                    Graphics g = new BufferedImage(1, 1,
                            BufferedImage.TYPE_INT_RGB).getGraphics();
                    g.setClip(0,0,1,1);
                    fsb.paintComponent(g);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                    excRef[0] = e;
                } finally {
                    io.closeInputOutput();
                    io.dispose();
                }
                s.release();
            }
        });
        assertTrue("Timout", s.tryAcquire(30, TimeUnit.MINUTES));
        assertNull("No exception should be thrown", excRef[0]);
    }


    /**
     * Test for bug 242979.
     *
     * @throws java.lang.InterruptedException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void testDoubleReset() throws InterruptedException, InvocationTargetException {

        io.getOut().println("First line");
        final Object lock = new Object();

        class OutputResetter implements Runnable {

            @Override
            public void run() {
                try {
                    final OutputWriter pw = io.getOut();
                    synchronized (lock) {
                        pw.reset();
                        pw.println("test println");
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        final Semaphore s = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100); // block EDT for a while
                    s.release();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        Thread t1 = new Thread(new OutputResetter(), "Thread 1");
        Thread t2 = new Thread(new OutputResetter(), "Thread 2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        s.tryAcquire(100, TimeUnit.MILLISECONDS);

        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                // process scheduled events
            }
        });

        assertFalse(((NbWriter) io.getOut()).out().isDisposed());

        io.closeInputOutput();
    }

    static JComponent getIOWindow() {
        IOContainer container = IOContainer.getDefault();
        JComponent comp = null;
        try {
            Field f = container.getClass().getDeclaredField("provider");
            f.setAccessible(true);
            IOContainer.Provider prov = (IOContainer.Provider) f.get(container);
            Method m = prov.getClass().getDeclaredMethod("impl", new Class<?>[0]);
            m.setAccessible(true);
            comp = (JComponent) m.invoke(prov);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalArgumentException
                | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return comp;
    }

    JComponent getSelectedTab() {
        class R implements Runnable {
            JComponent tab;
            public void run() {
                tab = container.getSelected();
            }
        }
        R r = new R();
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return r.tab;
    }
    
}
