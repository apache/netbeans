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

package org.netbeans;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExClipboard;

@org.openide.util.lookup.ServiceProviders({
    @org.openide.util.lookup.ServiceProvider(service=java.awt.datatransfer.Clipboard.class),
    @org.openide.util.lookup.ServiceProvider(service=org.openide.util.datatransfer.ExClipboard.class)
})
public final class NbClipboard extends ExClipboard
implements LookupListener, FlavorListener, AWTEventListener
{
    private static final Logger log = Logger.getLogger(NbClipboard.class.getName());
    private final ThreadLocal<Boolean> FIRING = new ThreadLocal<>();
    private Clipboard systemClipboard;
    private ExClipboard.Convertor[] convertors;
    private Lookup.Result<ExClipboard.Convertor> result;
    final boolean slowSystemClipboard;
    private Transferable last;
    private long lastWindowActivated;
    private long lastWindowDeactivated;
    private Reference<Object> lastWindowDeactivatedSource = new WeakReference<>(null);
    private volatile Task setContentsTask = Task.EMPTY;
    private volatile Task getContentsTask = Task.EMPTY;
    private boolean anyWindowIsActivated = true;

    public NbClipboard() {
        //for unit testing
        this( Toolkit.getDefaultToolkit().getSystemClipboard() );
    }

    @SuppressWarnings("LeakingThisInConstructor")
    NbClipboard( Clipboard systemClipboard ) {
        super("NBClipboard");   // NOI18N
        this.systemClipboard = systemClipboard;

        result = Lookup.getDefault().lookupResult(ExClipboard.Convertor.class);
        result.addLookupListener(this);

        systemClipboard.addFlavorListener(this);

        resultChanged(null);

        if (System.getProperty("netbeans.slow.system.clipboard.hack") != null) { // NOI18N
            slowSystemClipboard = Boolean.getBoolean("netbeans.slow.system.clipboard.hack"); // NOI18N
        } else if (Utilities.isMac()) {
            slowSystemClipboard = false;
        }
        else {
            slowSystemClipboard = true;
        }




        if (System.getProperty("sun.awt.datatransfer.timeout") == null) { // NOI18N
            System.setProperty("sun.awt.datatransfer.timeout", "1000"); // NOI18N
        }
        if (slowSystemClipboard) {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    protected synchronized ExClipboard.Convertor[] getConvertors () {
        return convertors;
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        Collection<? extends ExClipboard.Convertor> c = result.allInstances();
        ExClipboard.Convertor[] temp = new ExClipboard.Convertor[c.size()];
        convertors = c.toArray(temp);
    }

    // XXX(-ttran) on Unix (and also on Windows as we discovered recently)
    // calling getContents() on the system clipboard is very expensive, the
    // call can take up to 1000 milliseconds.  We need to examine the clipboard
    // contents each time the Node is activated, the method must be fast.
    // Therefore we cache the contents of system clipboard and use the cache
    // when someone calls getContents().  The cache is sync'ed with the system
    // clipboard when _any_ of our Windows gets WINDOW_ACTIVATED event.  It
    // means if some other apps modify the contents of the system clipboard in
    // the background then the change won't be propagated to us immediately.
    // The other drawback is that if module code bypasses NBClipboard and
    // accesses the system clipboard directly then we don't see these changes.
    //
    // The other problem is an AWT bug
    //
    // http://developer.java.sun.com/developer/bugParade/bugs/4818143.html
    //
    // sun.awt.datatransfer.ClipboardTransferable.getClipboardData() can hang
    // for very long time (maxlong == eternity).  We tries to avoid the hang by
    // access the system clipboard from a separate thread.  If the hang happens
    // the thread will wait for the system clipboard forever but not the whole
    // IDE

    private static final RequestProcessor RP = new RequestProcessor("System clipboard synchronizer"); // NOI18N

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void setContents(Transferable contents, ClipboardOwner owner) {
        synchronized (this) {
            // XXX(-dstrupl) the following line might lead to a double converted
            // transferable. Can be fixed as Jesse describes in #32485
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "setContents called with: "); // NOI18N
                logFlavors (contents, Level.FINER, log.isLoggable(Level.FINEST));
            }
            contents = convert(contents);
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "After conversion:"); // NOI18N
                logFlavors (contents, Level.FINER, log.isLoggable(Level.FINEST));
            }

            if (slowSystemClipboard) {
                if (this.contents != null) {
                    transferableOwnershipLost(this.contents);
                }

                final ClipboardOwner oldOwner = this.owner;
                final Transferable oldContents = this.contents;

                this.owner = owner;
                this.contents = contents;

                    if (oldOwner != null && oldOwner != owner) {
                    EventQueue.invokeLater(() -> {
                        oldOwner.lostOwnership(NbClipboard.this, oldContents);
                    });
                }
            } else {
                if (last != null) transferableOwnershipLost(last);
                last = contents;
            }
            scheduleSetContents(contents, owner, 0);
        }
        fireChange();
    }

    @Override
    public Transferable getContents(Object requestor) {
        Transferable prev;

        try {
            log.log(Level.FINE, "getContents, slowSystemClipboard: {0}", slowSystemClipboard); // NOI18N
            if (slowSystemClipboard && !RP.isRequestProcessorThread()) {
                // The purpose of lastWindowActivated+100 is to ignore calls
                // which immediatelly follow WINDOW_ACTIVATED event.
                // This is workaround of JDK bug described in issue 41098.
                int waitTime = Integer.getInteger("sun.awt.datatransfer.timeout", 1000); // NOI18N
                if (waitTime > 0 && !Boolean.TRUE.equals(FIRING.get())) {
                    boolean ok = scheduleGetFromSystemClipboard(false).waitFinished (waitTime);
                    if (!ok) {
                        log.log(Level.FINE, "Time out waiting for sync with system clipboard for {0} ms", waitTime); // NOI18N
                    }
                }
                prev = super.getContents (requestor);
            } else {
                setContentsTask.waitFinished();
                getContentsTask.waitFinished();
                log.log(Level.FINE, "after syncTask clipboard wait"); // NOI18N
                try {
                    prev = systemClipboard.getContents (requestor);
                } catch( ThreadDeath td ) {
                    throw td;
                } catch( Throwable ex ) {
                    log.log (Level.INFO, "System clipboard not available.", ex); // NOI18N
                    prev = null;
                }
            }

            synchronized (this) {
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by {0}", requestor); // NOI18N
                    logFlavors (prev, Level.FINE, log.isLoggable(Level.FINEST));
                }
                if (prev == null) {
                    // if system clipboard has no contents
                    return null;
                }

                Transferable res = convert (prev);
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by {0}", requestor); // NOI18N
                    logFlavors (res, Level.FINE, log.isLoggable(Level.FINEST));

                    res = new LoggableTransferable (res);
                }
                return res;
            }
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Logger.getLogger(NbClipboard.class.getName()).log(Level.WARNING, null, ex);
            return null;
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public  FlavorListener[] getFlavorListeners() {
        return new FlavorListener[0];
    }

    @Override
    public synchronized void addFlavorListener(FlavorListener listener) {
        Boolean prev = FIRING.get();
        try {
            FIRING.set(true);
            super.addFlavorListener(listener);
        } finally {
            FIRING.set(prev);
        }
    }



    private void scheduleSetContents(final Transferable cnts, final ClipboardOwner ownr, int delay) {
        setContentsTask = RP.post(new SetContents(cnts, ownr), delay);
    }

    @SuppressWarnings("NestedAssignment")
    private Task scheduleGetFromSystemClipboard(boolean notify) {
        return getContentsTask = RP.post(new GetContents(notify));
    }

    /** For testing purposes.
     */
    final void waitFinished () {
        setContentsTask.waitFinished ();
        getContentsTask.waitFinished ();
    }

    final void activateWindowHack (boolean reschedule) {
        // if WINDOW_DEACTIVATED is followed immediatelly with
        // WINDOW_ACTIVATED then it is JDK bug described in
        // issue 41098.
        lastWindowActivated = System.currentTimeMillis();
        if (reschedule) {
            scheduleGetFromSystemClipboard(true);
        }
    }

    private void logFlavors (Transferable trans, Level level, boolean content) {
        if (trans == null) {
            log.log (level, "  no clipboard contents"); // NOI18N
        }
        else {
            if (content) {
                java.awt.datatransfer.DataFlavor[] arr = trans.getTransferDataFlavors();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.length; i++) {
                    sb.append("  ").append(i).append(" = ").append(arr[i]); // NOI18N
                    try {
                        sb.append(" contains: ").append(trans.getTransferData(arr[i])); // NOI18N
                    } catch (UnsupportedFlavorException | IOException ex) {
                        log.log(level, "Can't convert to " + arr[i], ex); // NOI18N
                    }
                    sb.append("\n");
                }
                log.log (level, sb.toString());
            } else {
                log.log (level, " clipboard contains data"); // NOI18N
            }
        }
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        if( !anyWindowIsActivated )
            return; //#227236 - don't react to system clipboard changes when the IDE window is in the background
        fireChange();
    }

    final void fireChange() {
        Boolean prev = FIRING.get();
        try {
            FIRING.set(true);
            FlavorEvent e = new FlavorEvent(this);
            fireClipboardChange();
            for (FlavorListener l : super.getFlavorListeners()) {
                l.flavorsChanged(e);
            }
        } finally {
            FIRING.set(prev);
        }
    }

    @Override
    public void eventDispatched(AWTEvent ev) {
        if (!(ev instanceof WindowEvent))
            return;

        if (ev.getID() == WindowEvent.WINDOW_DEACTIVATED) {
            lastWindowDeactivated = System.currentTimeMillis();
            lastWindowDeactivatedSource = new WeakReference<>(ev.getSource());
            anyWindowIsActivated = false;
            if( Utilities.isWindows() ) {
                //#247585 - even listening to clipboard changes when the window isn't active
                //may throw a MS Windows error as the 'clipboard copy' action doesn't have enough time to finish
                systemClipboard.removeFlavorListener(this);
            }
        }
        if (ev.getID() == WindowEvent.WINDOW_ACTIVATED) {
            if( Utilities.isWindows() ) {
                systemClipboard.addFlavorListener(this);
                // Catch up on any events missed while we were away.
                fireChange();
            }
            anyWindowIsActivated = true;
            if (System.currentTimeMillis() - lastWindowDeactivated < 100 &&
                ev.getSource() == lastWindowDeactivatedSource.get()) {
                activateWindowHack (false);
            }
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "window activated scheduling update"); // NOI18N
            }
            scheduleGetFromSystemClipboard(true);
        }
    }

    private synchronized void superSetContents(Transferable t, ClipboardOwner o) {
        if (this.contents != null) {
            transferableOwnershipLost(this.contents);
        }

        final ClipboardOwner oldOwner = this.owner;
        final Transferable oldContents = this.contents;

        this.owner = o;
        this.contents = t;

        if (oldOwner != null && oldOwner != owner) {
            EventQueue.invokeLater(() -> {
                oldOwner.lostOwnership(NbClipboard.this, oldContents);
            });
        }
    }

    /** Transferable that logs operations on itself.
     */
    private final class LoggableTransferable implements Transferable {
        private final Transferable delegate;

        public LoggableTransferable (Transferable delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
            log.log (Level.FINE, "Request for flavor: {0}", flavor); // NOI18N
            Object res = delegate.getTransferData (flavor);
            log.log (Level.FINE, "Returning value: {0}", res); // NOI18N
            return res;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors () {
            return delegate.getTransferDataFlavors ();
        }

        @Override
        public boolean isDataFlavorSupported (DataFlavor flavor) {
            boolean res = delegate.isDataFlavorSupported (flavor);
            log.log (Level.FINE, "isDataFlavorSupported: {0} result: {1}", new Object[]{flavor, res}); // NOI18N
            return res;
        }

    }

    private final class GetContents implements Runnable {
        private final boolean notify;

        public GetContents(boolean notify) {
            this.notify = notify;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            log.fine("Running update"); // NOI18N
            try {
                Transferable transferable = null;
                // There can be a race between multiple applications accessing
                // the clipboard. If access can't be optained directly, retry
                // for a maximum of 1s. This is called from the requestprocessor
                // that is used because accessing the clipboard can block
                // indefinitely. Running the access loop here is deemed similar
                // in nature.
                final int MAX_TRIES = 50;
                final long start = System.currentTimeMillis();
                for (int i = 0; i < MAX_TRIES; i++) {
                    try {
                        transferable = systemClipboard.getContents(this);
                        break;
                    } catch (IllegalStateException ex) {
                        // Throw exception if retries failed
                        if (i == (MAX_TRIES - 1) || (System.currentTimeMillis() - start) > 980L) {
                            throw ex;
                        } else {
                            log.log(Level.INFO, "systemClipboard#getContents threw IllegalStateException (try: {0})", i + 1); // NOI18N
                        }
                        Thread.sleep(20); // Give system time to settle
                    }
                }
                superSetContents(transferable, null);
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "internal clipboard updated:"); // NOI18N
                    logFlavors (transferable, Level.FINE, log.isLoggable(Level.FINEST));
                }
                if (notify) {
                    fireChange();
                }
            }
            catch (ThreadDeath ex) {
                throw ex;
            }
            catch (InterruptedException | RuntimeException ex) {
                log.log(Level.INFO, "systemClipboard not available", ex); // NOI18N
            }
        }
    }

    private final class SetContents implements Runnable {
        private final Transferable cnts;
        private final ClipboardOwner ownr;

        SetContents(Transferable cnts, ClipboardOwner ownr) {
            this.cnts = cnts;
            this.ownr = ownr;
        }

        @Override
        public void run() {
            try {
                systemClipboard.setContents(cnts, ownr);
            } catch (IllegalStateException e) {
                //#139616
                if(log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "systemClipboard not available", e); // NOI18N
                } else {
                    log.log(Level.INFO, "systemClipboard#setContents threw IllegalStateException"); // NOI18N
                }
                scheduleSetContents(cnts, ownr, 100);
                return;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "systemClipboard updated:"); // NOI18N
                logFlavors(cnts, Level.FINE, log.isLoggable(Level.FINEST));
            }
        }
    }
}
