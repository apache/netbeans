/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
        JPanel progressPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1D;
        gbc.weighty = 1.0D;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
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

            Image img = (Image)UIManager.get("nb.progress.cancel.icon"); //NOI18N
            if( null != img ) {
                closeButton.setIcon( ImageUtilities.image2Icon( img ) );
            } else {
                closeButton.setText ( NbBundle.getMessage(AbstractWindowRunner.class,
                        "ModalDialog.btnClose.text")); //NOI18N
            }
            img = (Image)UIManager.get("nb.progress.cancel.icon.mouseover"); //NOI18N
            if( null != img ) {
                closeButton.setRolloverEnabled(true);
                closeButton.setRolloverIcon( ImageUtilities.image2Icon( img ) );
            }
            img = (Image)UIManager.get("nb.progress.cancel.icon.pressed"); //NOI18N
            if( null != img ) {
                closeButton.setPressedIcon( ImageUtilities.image2Icon( img ) ); //NOI18N
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
