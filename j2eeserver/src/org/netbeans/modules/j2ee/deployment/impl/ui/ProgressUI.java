/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;


/**
 * Progress UI provides a feedback for long lasting taks like deploying to a server,
 * starting or stopping a server, etc. The progress bar is indeterminate, displayed 
 * in the status bar if in non-modal mode, otherwise in a modal dialog.
 *
 * @author sherold
 */
public class ProgressUI implements ProgressListener {

    private static final Logger LOGGER = Logger.getLogger(ProgressUI.class.getName());

    private String title;
    private boolean modal;    
    private Deployment.Logger logger;
    
    private ProgressHandle handle;
    private ProgressObject progObj;
    
    private JDialog dialog;
    private JLabel messageLabel;
    private String lastMessage;
    private JComponent progressComponent;
    private boolean finished;
    
    /** helps to avoid double processing of the completion event */
    private boolean completionEventProcessed;
    
    /** Creates a new instance of ProgressUI */
    public ProgressUI(String title, boolean modal) {
        this(title, modal, null);
    }
    
    public ProgressUI(String title, boolean modal, Deployment.Logger logger) {
        this.modal = modal;
        this.title = title;
        this.logger = logger;        
        handle = ProgressHandleFactory.createHandle(title);
    }
    
    public void start() {
        start(null);
    }    
    
    /** Start the progress indication for indeterminate task. */
    public void start(Integer delay) {
        if (modal) {
            progressComponent = ProgressHandleFactory.createProgressComponent(handle);
        }
        if (delay != null) {
            handle.setInitialDelay(delay.intValue());
        }        
        handle.start();
    }
    
    /** Display the modal progress dialog. This method should be called from the
        AWT Event Dispatch thread. */
    public void showProgressDialog() {
        if (finished) {
            return; // do not display the dialog if we are done
        }
        dialog = new JDialog(WindowManager.getDefault().getMainWindow(), title, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        String lastMessageTmp;
        synchronized (this) {
            lastMessageTmp = lastMessage;
        }
        dialog.getContentPane().add(createProgressDialog(
                                        handle, 
                                        lastMessageTmp != null ? lastMessageTmp : title));
        dialog.pack();
        dialog.setBounds(Utilities.findCenterBounds(dialog.getSize()));
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
    }
    
    /** Displays a specified progress message. */
    public void progress(final String message) {
        String lastMessageTmp;
        synchronized (this) {
            lastMessageTmp = lastMessage;
        }
        // do not display blank message or the same message twice
        if (message != null && message.length() > 0 && !message.equals(lastMessageTmp)) {
            handle.progress(message);
            synchronized (this) {
                lastMessage = message;
            }
            log(message);
        }
        if (modal) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    if (messageLabel != null) {
                        messageLabel.setText(message);
                    } else {
                        synchronized (ProgressUI.this) {
                            lastMessage = message;
                        }
                    }
                }
            });
        }
    }
    
    /** Finish the task, unregister the progress object listener and dispose the ui. */
    public void finish() {
        handle.finish();
        ProgressObject po = null;
        synchronized (this) {
            po = progObj;
            progObj = null;
        }
        if (po != null) {
            po.removeProgressListener(this);
        }
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                finished = true;
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                    dialog = null;
                }
            }
        });
    }
    
    /** Display a failure dialog with the specified message and call finish. */
    public void failed(String message) {
        finish();
        if (logger != null) {
            log(message);
        }
    }
    
    /** Set a progress object this progress UI will monitor. */
    public void setProgressObject(ProgressObject obj) {
        // do not listen to the old progress object anymore
        ProgressObject po = null;
        synchronized (this) {
            po = progObj;
        }
        if (po != null) {
            po.removeProgressListener(this);
        }
        synchronized (this) {
            progObj = obj;
            if (obj == null) {
                return;
            }
            completionEventProcessed = false;
        }
        obj.addProgressListener(this);
        // safety-catch to not to miss the completion event, if it had come
        // before the progress listener was registered
        DeploymentStatus status = obj.getDeploymentStatus();
        if (status.isCompleted() || status.isFailed()) {
            handleDeploymentStatus(status);
        } else {
            progress(status.getMessage());
        }
    }
    
    // private helper methods
    
    private void log(String msg) {
        // TODO this needs to be fixed on lifecycle level
        try {
            if (logger != null && msg != null) {
                LOGGER.log(Level.FINEST, msg);

                logger.log(msg);
            }
        } catch (ThreadDeath e) {
            LOGGER.log(Level.FINE, null, e);
        }
    }
    
    private JComponent createProgressDialog(ProgressHandle handle, String message) {
        JPanel panel = new JPanel();                                                                                                                                                                           
        messageLabel = new JLabel();
                                                                                                                                                                           
        panel.setLayout(new java.awt.GridBagLayout());
                                                                                                                                                                           
        messageLabel.setText(message);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(12, 12, 0, 12);
        panel.add(messageLabel, gridBagConstraints);
                                                                                                                                                                           
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 12, 0, 12);
        panel.add(progressComponent, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(11, 12, 12, 12);
        JButton cancel = new JButton(NbBundle.getMessage(ProgressUI.class,"LBL_Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProgressUI.class,"AD_Cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        panel.add(cancel, gridBagConstraints);
        
        return panel;
    }
    
    // ProgressListener implementation ----------------------------------------
    
    public void handleProgressEvent(ProgressEvent progressEvent) {
        handleDeploymentStatus(progressEvent.getDeploymentStatus());
    }

    private void handleDeploymentStatus(DeploymentStatus status) {
        synchronized (this) {
            if (completionEventProcessed) {
                // this event has already been processed
                return;
            }
            if (status.isCompleted() || status.isFailed()) {
                completionEventProcessed = true;
            }
        }
        if (status.isFailed()) {
            failed(status.getMessage());
        } else {
            progress(status.getMessage());
        }
    }
}
