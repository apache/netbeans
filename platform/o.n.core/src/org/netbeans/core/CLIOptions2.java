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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.CLIHandler;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Shows the main window, so it is fronted when second instance of
 * NetBeans tries to start.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=CLIHandler.class)
public class CLIOptions2 extends CLIHandler implements Runnable {
    /** number of invocations */
    private int cnt;
    private static final Logger LOG = Logger.getLogger(CLIOptions2.class.getName());
    /** Time (in milliseconds) to wait for the event queue to become active. */
    private static final int EQ_TIMEOUT = 15 * 1000;
    private final RequestProcessor.Task task;
    static CLIOptions2 INSTANCE;

    /**
     * Create a default handler.
     */
    public CLIOptions2 () {
        super(WHEN_INIT);
        INSTANCE = this;
        task = RequestProcessor.getDefault().create(this);
    }

    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }

    final int cli(String[] args) {
        if (cnt++ == 0) return 0;
        
        if (!GraphicsEnvironment.isHeadless()) {
            LOG.fine("CLI running");
            SwingUtilities.invokeLater(this);
            task.schedule(EQ_TIMEOUT);
        }
        
        return 0;
    }
    
    @Override
    public void run () {
        if (!EventQueue.isDispatchThread()) {
            eqStuck();
            return;
        }
        LOG.fine("running in EQ");
        task.cancel();
        frontMainWindow ();
    }

    @SuppressWarnings("deprecation") // Thread.stop
    private void eqStuck() {
        Thread eq = TimableEventQueue.eq;
        if (eq == null) {
            LOG.warning("event queue thread not determined");
            return;
        }
        StackTraceElement[] stack = Thread.getAllStackTraces().get(eq);
        if (stack == null) {
            LOG.log(Level.WARNING, "no stack trace available for {0}", eq);
            return;
        }
        LOG.log(Level.INFO, "EQ stuck in " + eq, new EQStuck(stack));
        if (Dependency.JAVA_SPEC.compareTo(new SpecificationVersion("1.7")) >= 0) {
            LOG.log(Level.WARNING, "#198918: will not hard restart EQ when running on JDK 7");
            eq.interrupt(); // maybe this will be enough
            return;
        }
        for (StackTraceElement line : stack) {
            if (line.getMethodName().equals("<clinit>")) {
                LOG.log(Level.WARNING, "Will not hard restart EQ when inside a static initializer: {0}", line);
                eq.interrupt(); // maybe this will be enough
                return;
            }
        }
        eq.stop();
    }

    private static void frontMainWindow() {
        Frame f = WindowManager.getDefault().getMainWindow();

        // makes sure the frame is visible
        f.setVisible(true);
        // uniconifies the frame if it is inconified
        if ((f.getExtendedState() & Frame.ICONIFIED) != 0) {
            f.setExtendedState(~Frame.ICONIFIED & f.getExtendedState());
        }
        // moves it to front and requests focus
        f.toFront ();
    }
    private static class EQStuck extends Throwable {
        EQStuck(StackTraceElement[] stack) {
            super("GUI is not responsive"); // NOI18N
            setStackTrace(stack);
        }
        public @Override synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
    
    protected void usage(PrintWriter w) {}
    
}
