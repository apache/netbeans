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
package org.netbeans.modules.progress.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.progress.spi.RunOffEDTProvider;
import org.netbeans.modules.progress.spi.RunOffEDTProvider.Progress;
import org.netbeans.modules.progress.spi.RunOffEDTProvider.Progress2;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.*;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Default RunOffEDTProvider implementation for ProgressUtils.runOffEventDispatchThread() methods
 * @author Jan Lahoda, Tomas Holy
 */
@ServiceProvider(service=RunOffEDTProvider.class, position = 100)
public class RunOffEDTImpl implements RunOffEDTProvider, Progress, Progress2 {

    private static final RequestProcessor TI_WORKER = new RequestProcessor("TI_" + ProgressUtils.class.getName(), 1, true);
    
    private static final Map<String, Long> CUMULATIVE_SPENT_TIME = new HashMap<String, Long>();
    private static final Map<String, Long> MAXIMAL_SPENT_TIME = new HashMap<String, Long>();
    private static final Map<String, Integer> INVOCATION_COUNT = new HashMap<String, Integer>();
    private static final int CANCEL_TIME = 1000;
    private static final int WARNING_TIME = Integer.getInteger("org.netbeans.modules.progress.ui.WARNING_TIME", 10000);
    private static final Logger LOG = Logger.getLogger(RunOffEDTImpl.class.getName());

    //@GuardedBy("rqByClz")
    private final Map<Class<?>,Pair<Integer,RequestProcessor>> rqByClz = new HashMap<Class<?>, Pair<Integer, RequestProcessor>>();
    private final boolean assertionsOn;

    @Override
    public void runOffEventDispatchThread(final Runnable operation, final String operationDescr,
            final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        Parameters.notNull("operation", operation); //NOI18N
        Parameters.notNull("cancelOperation", cancelOperation); //NOI18N
        if (!SwingUtilities.isEventDispatchThread()) {
            operation.run();
            return;
        }
        long startTime = System.currentTimeMillis();
        runOffEventDispatchThreadImpl(operation, operationDescr, cancelOperation, waitForCanceled, waitCursorTime, dlgTime);
        long elapsed = System.currentTimeMillis() - startTime;

        if (assertionsOn) {
            String clazz = operation.getClass().getName();
            Long cumulative = CUMULATIVE_SPENT_TIME.get(clazz);
            if (cumulative == null) {
                cumulative = 0L;
            }
            cumulative += elapsed;
            CUMULATIVE_SPENT_TIME.put(clazz, cumulative);
            Long maximal = MAXIMAL_SPENT_TIME.get(clazz);
            if (maximal == null) {
                maximal = 0L;
            }
            if (elapsed > maximal) {
                maximal = elapsed;
                MAXIMAL_SPENT_TIME.put(clazz, maximal);
            }
            Integer count = INVOCATION_COUNT.get(clazz);
            if (count == null) {
                count = 0;
            }
            count++;
            INVOCATION_COUNT.put(clazz, count);

            if (elapsed > WARNING_TIME) {
                LOG.log(Level.WARNING, "Lengthy operation: {0}:{1}:{2}:{3}:{4}", new Object[] {
                    clazz, cumulative, count, maximal, String.format("%3.2f", ((double) cumulative) / count)});
            }
        }
    }

    private void runOffEventDispatchThreadImpl(final Runnable operation, final String operationDescr,
            final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Dialog> d = new AtomicReference<Dialog>();
        RequestProcessor rp;
        synchronized (rqByClz) {
            final Class<?> clz = operation.getClass();
            int index;
            Pair<Integer,RequestProcessor> p = rqByClz.get(clz);
            if (p == null) {
                index = 0;
                rp = new RequestProcessor(String.format(
                        "%s for: %s",    //NOI18N
                        ProgressUtils.class.getName(),
                        clz.getName()),
                    1,
                    false);
            } else {
                index = p.first();
                rp = p.second();
            }
            p = Pair.<Integer,RequestProcessor>of(index+1, rp);
            rqByClz.put(clz, p);
        }
        rp.post(new Runnable() {
            public @Override void run() {
                if (cancelOperation.get()) {
                    return;
                }
		try {
		    operation.run();
		} finally {
                    synchronized (rqByClz) {
                        final Class<?> clz = operation.getClass();
                        Pair<Integer,RequestProcessor> p = rqByClz.remove(clz);
                        if (p.first() > 1) {
                            rqByClz.put(clz, Pair.<Integer,RequestProcessor>of(
                                p.first()-1,
                                p.second()));
                        }
                    }
		    latch.countDown();

		    SwingUtilities.invokeLater(new Runnable() {

			public @Override void run() {
			    Dialog dd = d.get();
			    if (dd != null) {
				dd.setVisible(false);
	                        dd.dispose();
			    }
			}
		    });
		}
            }
        });
        Window window = null;
        Component glassPane = null;
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner != null) {
            window = SwingUtilities.getWindowAncestor(focusOwner);
            if (window != null) {
                RootPaneContainer root = (RootPaneContainer) SwingUtilities.getAncestorOfClass(RootPaneContainer.class, focusOwner);
                glassPane = root.getGlassPane();   
            }
        } 
        if (window == null || glassPane == null) {
            window = WindowManager.getDefault().getMainWindow();
            glassPane = ((JFrame) window).getGlassPane();
        }
        if (waitMomentarily(glassPane, null, waitCursorTime, latch, window)) {
            return;
        }

        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

        if (waitMomentarily(glassPane, wait, dlgTime, latch, window)) {
            return;
        }

        String title = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.TITLE_Operation"); //NOI18N
        String cancelButton = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.BTN_Cancel"); //NOI18N

        DialogDescriptor nd = new DialogDescriptor(operationDescr, title, true, new Object[]{cancelButton},
                cancelButton, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            public @Override void actionPerformed(ActionEvent e) {
                cancelOperation.set(true);
                d.get().setVisible(false);
                d.get().dispose();
            }
        });

        nd.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

        d.set(DialogDisplayer.getDefault().createDialog(nd));
        d.get().setVisible(true);

        if (waitForCanceled) {
            try {
                if (!latch.await(CANCEL_TIME, TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("Canceled operation did not finish in time."); //NOI18N
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
    }
    
    @Override
    public void runOffEventThreadWithCustomDialogContent(Runnable operation, String dialogTitle, JPanel content, int waitCursorAfter, int dialogAfter)
    {
        runOffEventThreadCustomDialogImpl(operation, dialogTitle, content, waitCursorAfter, dialogAfter);
    }

    @Override
    public void runOffEventThreadWithProgressDialog(final Runnable operation, final String operationDescr,
            ProgressHandle handle, boolean includeDetailLabel, int waitCursorAfter, int dialogAfter) {
        JPanel content = contentPanel(handle, includeDetailLabel);
        runOffEventThreadCustomDialogImpl(operation, operationDescr, content, waitCursorAfter, dialogAfter);
    }
    
    private void runOffEventThreadCustomDialogImpl(final Runnable operation, final String operationDescr,
            final JPanel contentPanel, int waitCursorAfter, int dialogAfter) {
        if (waitCursorAfter < 0) waitCursorAfter = 1000;
        if (dialogAfter < 0) dialogAfter = 2000;
        
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Dialog> d = new AtomicReference<Dialog>();
        final AtomicReference<RequestProcessor.Task> t  = new AtomicReference<RequestProcessor.Task>();
        
        JDialog dialog = createModalDialog(operation, operationDescr, contentPanel, d, t, operation instanceof Cancellable);

        final Task rt = TI_WORKER.post(new Runnable() {

            public @Override void run() {
		try {
		    operation.run();
		} finally {
		    latch.countDown();

		    SwingUtilities.invokeLater(new Runnable() {

			public @Override void run() {
			    Dialog dd = d.get();
			    if (dd != null) {
				dd.setVisible(false);
				dd.dispose();
			    }
			}
		    });
		}
            }
        });
        t.set(rt);

        Window window = null;
        Component glassPane = null;
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner != null) {
            window = SwingUtilities.getWindowAncestor(focusOwner);
            if (window != null) {
                RootPaneContainer root = (RootPaneContainer) SwingUtilities.getAncestorOfClass(RootPaneContainer.class, focusOwner);
                glassPane = root.getGlassPane();   
            }
        } 
        if (window == null || glassPane == null) {
            window = WindowManager.getDefault().getMainWindow();
            glassPane = ((JFrame) window).getGlassPane();
        }
        if (waitMomentarily(glassPane, null, waitCursorAfter, latch, window)) {
            return;
        }

        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

        if (waitMomentarily(glassPane, wait, dialogAfter, latch, window)) {
            return;
        }

        d.set(dialog);
        if (EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    d.get().setVisible(true);
                }
            });
        } else {
            d.get().setVisible(true);
        }
    }
    
    private static boolean waitMomentarily(Component glassPane, Cursor wait, int timeout, final CountDownLatch l, Window window) {
        Cursor originalWindow = window.getCursor();
        Cursor originalGlass = glassPane.getCursor();    

        try {
            if (wait != null) {
                window.setCursor(wait);
                glassPane.setCursor(wait);
            }

            glassPane.setVisible(true);
            try {
                return l.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
                return true;
            }
        } finally {
            glassPane.setVisible(false);
            window.setCursor(originalWindow);
            glassPane.setCursor(originalGlass);
        }
    }

    public RunOffEDTImpl() {
        boolean ea = false;
        assert ea = true;
        assertionsOn = ea;
    }

    @Override
    public <T> Future<T> showProgressDialogAndRunLater (ProgressRunnable<T> operation, ProgressHandle handle, boolean includeDetailLabel) {
       AbstractWindowRunner<T> wr = new ProgressBackgroundRunner<T>(operation, 
               handle, includeDetailLabel, operation instanceof Cancellable);
       Future<T> result = wr.start();
       assert EventQueue.isDispatchThread() == (result != null);
       if (result == null) {
           try {
               result = wr.waitForStart();
           } catch (InterruptedException ex) {
               LOG.log(Level.FINE, "Interrupted/cancelled during start {0}", operation); //NOI18N
               LOG.log(Level.FINER, "Interrupted/cancelled during start", ex); //NOI18N
               return null;
           }
       }
       return result;
    }

    @Override
    public <T> T showProgressDialogAndRun(ProgressRunnable<T> toRun, String displayName, boolean includeDetailLabel) {
        try {
            return showProgressDialogAndRunLater(toRun, toRun instanceof Cancellable ?
                ProgressHandle.createHandle(displayName, (Cancellable) toRun) :
                ProgressHandle.createHandle(displayName), includeDetailLabel).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            LOG.log(Level.FINER, "Cancelled " + toRun, ex); //NOI18N
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void showProgressDialogAndRun(Runnable toRun, ProgressHandle handle, boolean includeDetailLabel) {
       boolean showCancelButton = toRun instanceof Cancellable;
       AbstractWindowRunner<Void> wr = new ProgressBackgroundRunner<Void>(toRun, 
               handle, includeDetailLabel, showCancelButton);
       wr.start();
        try {
            try {
                wr.waitForStart().get();
            } catch (CancellationException ex) {
                LOG.log(Level.FINER, "Cancelled " + toRun, ex); //NOI18N
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static class CancellableFutureTask<T> extends FutureTask<T> implements Cancellable {
        volatile Task task;
        private final Callable<T> c;
        CancellableFutureTask(Callable<T> c) {
            super(c);
            this.c = c;
        }

        @Override
        public boolean cancel() {
            return cancel(true);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = c instanceof Cancellable ? ((Cancellable) c).cancel() : false;
            result &= super.cancel(mayInterruptIfRunning) & task.cancel();
            return result;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + c + "]"; //NOI18N
        }
    }

    static final class TranslucentMask extends JComponent { //pkg private for tests
        private static final String PROGRESS_WINDOW_MASK_COLOR = "progress.windowMaskColor"; //NOI18N
        TranslucentMask() {
            setVisible(false); //so we will trigger a property change
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void paint (Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Color translu = UIManager.getColor(PROGRESS_WINDOW_MASK_COLOR);
            if (translu == null) {
                translu = new Color (180, 180, 180, 148);
            }
            g2d.setColor(translu);
            g2d.fillRect (0, 0, getWidth(), getHeight());
        }
    }

    private static final class ProgressBackgroundRunner<T> extends AbstractWindowRunner<T> implements Cancellable {
        private final ProgressRunnable<T> toRun;
        ProgressBackgroundRunner(ProgressRunnable<T> toRun, String displayName, boolean includeDetail, boolean showCancel) {
            super (showCancel ?
                ProgressHandle.createHandle(displayName, (Cancellable)toRun, null) :
                ProgressHandle.createHandle(displayName, null, (Action)null), includeDetail, showCancel);
            this.toRun = toRun;
        }

        ProgressBackgroundRunner(ProgressRunnable<T> toRun, ProgressHandle handle, boolean includeDetail, boolean showCancel) {
            super (handle, includeDetail, showCancel);
            this.toRun = toRun;
        }

        ProgressBackgroundRunner(Runnable toRun, ProgressHandle handle, boolean includeDetail, boolean showCancel) {
            this (showCancel ? new CancellableRunnablePR<T>(toRun) : 
                new RunnablePR<T>(toRun), handle, includeDetail, showCancel);
        }

        @Override
        protected T runBackground() {
            handle.start();
            handle.switchToIndeterminate();
            T result;
            try {
                result = toRun.run(handle);
            } finally {
                handle.finish();
            }
            return result;
        }

        @Override
        public boolean cancel() {
            if (toRun instanceof Cancellable) {
                return ((Cancellable) toRun).cancel();
            }
            return false;
        }

        private static class RunnablePR<T> implements ProgressRunnable<T> {
            protected final Runnable toRun;
            RunnablePR(Runnable toRun) {
                this.toRun = toRun;
            }

            @Override
            public T run(ProgressHandle handle) {
                toRun.run();
                return null;
            }
        }

        private static final class CancellableRunnablePR<T> extends RunnablePR<T> implements Cancellable {
            CancellableRunnablePR(Runnable toRun) {
                super (toRun);
            }

            @Override
            public boolean cancel() {
                return ((Cancellable) toRun).cancel();
            }
        }

    }

    private static JPanel contentPanel(final ProgressHandle handle, boolean includeDetail) {
        // top panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        
        // main label
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent(handle);
        Font f = mainLabel.getFont();
        if (f != null) {
            mainLabel.setFont(f.deriveFont(Font.BOLD));
        }
        contentPanel.add(mainLabel, gridBagConstraints);
        
        // progress bar
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);
        contentPanel.add (progressBar, gridBagConstraints);
        
        if (includeDetail) {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            JLabel details = ProgressHandleFactory.createDetailLabelComponent(handle);
            contentPanel.add(details, gridBagConstraints);
        }
        
        // empty panel - for correct resizing
        JPanel emptyPanel = new JPanel();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = includeDetail ? 3 : 2;
        gridBagConstraints.weighty = 2.0;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        contentPanel.add(emptyPanel, gridBagConstraints);
        
        return contentPanel;
    }
    
    private static JDialog createModalDialog(
            final Runnable operation,
            final String title,
            final JPanel content,
            final AtomicReference<Dialog> d,
            final AtomicReference<RequestProcessor.Task> task,
            final boolean cancelAvail) 
    {
        assert EventQueue.isDispatchThread();
        
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        
        panel.add(content, gridBagConstraints);
        
        if (cancelAvail) {
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            String cancelButton = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.BTN_Cancel"); //NOI18N
            JButton cancel = new JButton(cancelButton);
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (operation instanceof Cancellable) {
                        ((Cancellable) operation).cancel();
                        task.get().cancel();
                        d.get().setVisible(false);
                        d.get().dispose();
                    }
                }
            });
            buttonsPanel.add(cancel);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            panel.add(buttonsPanel, gridBagConstraints);
        }
        
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog result = new JDialog(mainWindow, title, true);
        result.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        result.setSize(400, 150);
        result.setContentPane(panel);
        result.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        return result;
    }
}
