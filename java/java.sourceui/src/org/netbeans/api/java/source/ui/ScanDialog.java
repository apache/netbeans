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
package org.netbeans.api.java.source.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for notifying user about the background scan.
 * @since 1.2
 * @author Tomas Zezula
 */
public class ScanDialog {
    
    private ScanDialog () {}

    /**
     * This is a helper method to provide support for delaying invocations of actions
     * depending on java model. 
     * <br>Behavior of this method is following:<br>
     * If classpath scanning is not in progress, runnable's run() is called. <br>
     * If classpath scanning is in progress, modal cancellable notification dialog with specified
     * tile is opened.
     * </ul>
     * As soon as classpath scanning finishes, this dialog is closed and runnable's run() is called.
     * This method must be called in AWT EventQueue. Runnable is performed in AWT thread.
     *
     * @param runnable Runnable instance which will be called.
     * @param actionName Title of wait dialog.
     * @return true action was cancelled <br>
     *         false action was performed
     * 
     * @see org.netbeans.api.java.source.JavaSource#runWhenScanFinished which provides delayed invocation
     * of action without UI notification.
     */
    public static boolean runWhenScanFinished (final Runnable runnable, final String actionName) {
        assert runnable != null;
        assert actionName != null;
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            
            class AL implements ActionListener {
                
                private Dialog dialog;
                private Future<Void> monitor;
                
                public synchronized void start (final Future<Void> monitor) {
                    assert monitor != null; 
                    this.monitor = monitor;
                    if (dialog != null) {
                        dialog.setVisible(true);                                        
                    }
                }
                
                public void actionPerformed(ActionEvent e) {                    
                    monitor.cancel(false);
                    close ();
                }
                
                synchronized  void close () {
                    if (dialog != null) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        dialog = null;
                    }
                }
            };
            final AL listener = new AL ();            
            JLabel label = new JLabel(NbBundle.getMessage(ScanDialog.class,"MSG_WaitScan"),
                    javax.swing.UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(12,12,11,11));
            DialogDescriptor dd = new DialogDescriptor(label, actionName, true, new Object[]{NbBundle.getMessage(ScanDialog.class,"LBL_CancelAction",actionName)}, null, 0, null, listener);
            listener.dialog = DialogDisplayer.getDefault().createDialog(dd);
            listener.dialog.pack();
            final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
                ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]));
            final JavaSource js = JavaSource.create(info);
            try {
                final Future<Void> monitor = js.runWhenScanFinished((cc) -> {                        
                        final Runnable r = () -> {
                            listener.close();
                            runnable.run();
                        };
                        if (SwingUtilities.isEventDispatchThread()) {
                            r.run();
                        } else {
                            SwingUtilities.invokeLater(r);         
                        }
                    }, true);
                if (!monitor.isDone()) {
                    listener.start(monitor);
                }                
                return monitor.isCancelled();
            }catch (IOException e) {
                Exceptions.printStackTrace(e);
                return true;
            }
        } else {
            runnable.run();
            return false;
        }
    }
    
}
