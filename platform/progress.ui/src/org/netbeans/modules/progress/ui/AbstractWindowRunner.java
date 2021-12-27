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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

abstract class AbstractWindowRunner<T> extends WindowAdapter implements Runnable, Callable<T> {

    private volatile JDialog dlg;
    private final boolean includeDetail;
    protected final ProgressHandle handle;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile T operationResult;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch waitForTaskAssignment = new CountDownLatch(1);
    private RunOffEDTImpl.CancellableFutureTask<T> future;
    private final boolean showCancel;
    private static final RequestProcessor RP = new RequestProcessor(AbstractWindowRunner.class.getName(), 10);

    AbstractWindowRunner(ProgressHandle handle, boolean includeDetail, boolean showCancel) {
        this.includeDetail = includeDetail;
        this.handle = handle;
        this.showCancel = showCancel;
    }

    @Override
    public T call() throws Exception {
        try {
            return runBackground();
        } finally {
            EventQueue.invokeLater(this);
        }
    }

    private Future<T> task() {
        Future<T> result;
        synchronized (handle) {
            result = this.future;
        }
        return result;
    }

    Future<T> waitForStart() throws InterruptedException {
        Future<T> result = task();
        if (!EventQueue.isDispatchThread()) {
            if (result == null) {
                startLatch.await();
                result = task();
            }
        }
        assert result != null;
        return result;
    }

    @Override
    public final void windowOpened(WindowEvent e) {
        dlg = (JDialog) e.getSource();
        if (!isDispatchThread) {
            createTask();
        }
        RunOffEDTImpl.CancellableFutureTask f;
        synchronized(this) {
             f = future;
        }
        //Runnable could theoretically complete before we have set the
        //handle, allowing
        //the callee to see a CancellableFutureTask with task == null.
        //So we block launch until the task has been assigned, which is now
        waitForTaskAssignment.countDown();
        grayOutMainWindow();
        f.task.schedule(0);
        startLatch.countDown();
    }

    private Future<T> createTask() {
        RunOffEDTImpl.CancellableFutureTask<T> ft = new RunOffEDTImpl.CancellableFutureTask<T>(this);
        ft.task = RP.create(ft);
        synchronized (handle) {
            future = ft;
        }
        return ft;
    }

    @Override
    public final void windowClosed(WindowEvent e) {
        ungrayMainWindow();
        latch.countDown();
    }

    final void await() throws InterruptedException {
        latch.await();
    }

    boolean isDispatchThread;
    Future<T> start() {
        if (EventQueue.isDispatchThread()) {
            isDispatchThread = true;
            Future<T> task = createTask();
            dlg = createModalProgressDialog(handle, includeDetail);
            dlg.setVisible(true);
            return task;
        } else {
            CountDownLatch dlgLatch = new CountDownLatch(1);
            DialogCreator dc = new DialogCreator(dlgLatch);
            EventQueue.invokeLater(dc);
            try {
                dlgLatch.await();
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            return null;
        }
    }

    protected abstract T runBackground();

    T getResult() {
        return operationResult;
    }

    @Override
    public void run() {
        if (!EventQueue.isDispatchThread()) {
            try {
                try {
                    waitForTaskAssignment.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                operationResult = runBackground();
            } finally {
                EventQueue.invokeLater(this);
            }
        } else {
            dlg.setVisible(false);
            dlg.dispose();
        }
    }

    private final class DialogCreator implements Runnable {

        private final CountDownLatch latch;

        DialogCreator(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            createModalProgressDialog(handle, includeDetail);
            latch.countDown();
        }
    }

    private JDialog createModalProgressDialog(ProgressHandle handle, boolean includeDetail) {
        assert EventQueue.isDispatchThread();
        int edgeGap = Utilities.isMac() ? 12 : 8;
        int compGap = Utilities.isMac() ? 9 : 5;
        JPanel panel = new JPanel(new GridLayout(includeDetail ? 3 : 2, 1, compGap, compGap));
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent(handle);
        Font f = mainLabel.getFont();
        if (f != null) {
            mainLabel.setFont(f.deriveFont(Font.BOLD));
        }
        panel.add(mainLabel);
        JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);
        progressBar.setMinimumSize(new Dimension(400, 32));
        GridBagLayout gb = new GridBagLayout();
        // give first row, which contains the progress bar and the cancel button, a minimum height
        gb.rowHeights = new int[] { mainLabel.getFontMetrics(mainLabel.getFont()).getHeight() };
        JPanel progressPanel = new JPanel(gb);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1D;
        gbc.weighty = 0D;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressPanel.add (progressBar, gbc);
        if (showCancel) {
            final JButton closeButton = new JButton();
            gbc.gridx = 1;
            gbc.weightx = 0D;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets (0, Utilities.isMac() ? 12 : 5, 0, 0);
            progressPanel.add (closeButton, gbc);
            closeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    task().cancel(true);
                }

            });
            closeButton.setBorderPainted(false);
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(false);

            Object img = UIManager.get("nb.progress.cancel.icon"); //NOI18N
            if( null != img ) {
                closeButton.setIcon( (img instanceof Icon) ? (Icon) img : ImageUtilities.image2Icon( (Image) img ) );
            } else {
                closeButton.setText ( NbBundle.getMessage(AbstractWindowRunner.class,
                        "ModalDialog.btnClose.text")); //NOI18N
            }
            img = UIManager.get("nb.progress.cancel.icon.mouseover"); //NOI18N
            if( null != img ) {
                closeButton.setRolloverEnabled(true);
                closeButton.setRolloverIcon( (img instanceof Icon) ? (Icon) img : ImageUtilities.image2Icon( (Image) img ) );
            }
            img = UIManager.get("nb.progress.cancel.icon.pressed"); //NOI18N
            if( null != img ) {
                closeButton.setPressedIcon( (img instanceof Icon) ? (Icon) img : ImageUtilities.image2Icon( (Image) img ) );
            }
            closeButton.setToolTipText(NbBundle.getMessage(AbstractWindowRunner.class,
                    "ModalDialog.btnClose.tooltip")); //NOI18N
            closeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AbstractWindowRunner.class,
                    "ModalDialog.btnClose.accessibleName")); //NOI18N
            closeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AbstractWindowRunner.class,
                    "ModalDialog.btnClose.accessibleDescription")); //NOI18N
            panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel"); //NOI18N
            panel.getActionMap().put("cancel", new AbstractAction() { //NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeButton.doClick();
                }
            });
        }
        panel.add(progressPanel);
        if (includeDetail) {
            JLabel details = ProgressHandleFactory.createDetailLabelComponent(handle);
            details.setMinimumSize(new Dimension(300, 16));
            panel.add(details);
        }
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(edgeGap, edgeGap, edgeGap, edgeGap)));
        panel.setMinimumSize(new Dimension(400, 100));
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog result = new JDialog(mainWindow, true);
        result.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        result.setUndecorated(true);
        result.setSize(400, 100);
        result.getContentPane().setLayout(new BorderLayout());
        result.getContentPane().add(panel, BorderLayout.CENTER);
        result.pack();
        int reqWidth = result.getWidth();
        result.setSize(Math.max(reqWidth, mainWindow instanceof JFrame ? ((JFrame) mainWindow).getContentPane().getWidth() / 3 : mainWindow.getWidth()), result.getHeight());
        result.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        result.addWindowListener(this);
        if (EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    result.setVisible(true);
                }
            });
        } else {
            result.setVisible(true);
        }
        
        return result;
    }

    private Component oldGlassPane;
    private void grayOutMainWindow() {
        assert EventQueue.isDispatchThread();
        Frame f = WindowManager.getDefault().getMainWindow();
        if (f instanceof JFrame) {
            Map<?, ?> hintsMap = (Map) (Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            //Avoid translucent painting on, for example, remote X terminal
            if (hintsMap == null || !RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(hintsMap.get(RenderingHints.KEY_TEXT_ANTIALIASING))) {
                JFrame jf = (JFrame) f;
                RunOffEDTImpl.TranslucentMask mask = new RunOffEDTImpl.TranslucentMask();
                oldGlassPane = jf.getGlassPane();        
                jf.setGlassPane(mask);
                mask.setVisible(true);
                mask.setBounds(0, 0, jf.getContentPane().getWidth(), jf.getContentPane().getHeight());
                mask.invalidate();
                mask.revalidate();
                mask.repaint();
                jf.getRootPane().paintImmediately(0, 0, jf.getRootPane().getWidth(), jf.getRootPane().getHeight());
            }
        }
    }

    private void ungrayMainWindow() {
        if (oldGlassPane != null) {
            JFrame jf = (JFrame) WindowManager.getDefault().getMainWindow();
            jf.setGlassPane(oldGlassPane);
            jf.getGlassPane().setVisible(false);          
            jf.invalidate();
            jf.repaint();
        }
    }
}
