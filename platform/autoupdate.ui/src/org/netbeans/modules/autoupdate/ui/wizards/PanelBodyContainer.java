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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.HTMLEditorKitEx;
import org.netbeans.modules.autoupdate.ui.actions.Installer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author  Jiri Rechtacek
 */
public class PanelBodyContainer extends javax.swing.JPanel {
    private String head = null;
    private String message = null;
    private JScrollPane customPanel;
    private JPanel bodyPanel = null;
    private JComponent progressPanel = null;
    private JComponent progress;
    private boolean isWaiting = false;
    
    /** Creates new form InstallPanelContainer */
    public PanelBodyContainer (String heading, String msg, JPanel bodyPanel) {
        head = heading;
        message = msg;
        this.bodyPanel = bodyPanel;
        initComponents ();
        
        HTMLEditorKit htmlkit = new HTMLEditorKitEx();
        // override the Swing default CSS to make the HTMLEditorKit use the
        // same font as the rest of the UI.

        // XXX the style sheet is shared by all HTMLEditorKits.  We must
        // detect if it has been tweaked by ourselves or someone else
        // (code completion javadoc popup for example) and avoid doing the
        // same thing again

        StyleSheet css = htmlkit.getStyleSheet();

        if (css.getStyleSheets() == null) {
            StyleSheet css2 = new StyleSheet();
            Font f = new JList().getFont();
            int size = f.getSize();
            css2.addRule(new StringBuffer("body { font-size: ").append(size) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            htmlkit.setStyleSheet(css2);
        }
        
        tpPanelHeader.setEditorKit(htmlkit);
        tpPanelHeader.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
        writeToHeader (head, message);
        initBodyPanel ();
    }
    
    @Override
    public void addNotify () {
        super.addNotify();
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                bodyPanel.scrollRectToVisible (new Rectangle (0, 0, 10, 10));
            }
        });
        if (isWaiting) {
            setWaitingState (true);
        }
    }
    
    public void setBody (final JPanel newBodyPanel) {
        if (SwingUtilities.isEventDispatchThread ()) {
            this.bodyPanel = newBodyPanel;
            initBodyPanel ();
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    bodyPanel = newBodyPanel;
                    initBodyPanel ();
                }
            });
        }
    }
    
    public void setWaitingState (boolean isWaiting) {
        setWaitingState (isWaiting, 0);
    }
    
    public void setWaitingState (boolean isWaiting, final long estimatedTime) {
        if (this.isWaiting == isWaiting) {
            return ;
        }
        this.isWaiting = isWaiting;
        if (isWaiting) {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    addProgressLine (estimatedTime);
                }
            });
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    removeProgressLine ();
                }
            });
        }
        Component rootPane = getRootPane ();
        /* Component parent = getParent ();
        if (parent != null) {
            parent.setEnabled (! isWaiting);
        } */
        if (rootPane != null) {
            if (isWaiting) {
                rootPane.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
            } else {
                rootPane.setCursor (null);
            }
        }
    }
    
    private Timer delay;
    private ProgressHandle handle;
    private void addProgressLine (final long estimatedTime) {
        handle = ProgressHandleFactory.createHandle ("PanelBodyContainer_ProgressLine"); // NOI18N
        JLabel title;
        if (bodyPanel instanceof LicenseApprovalPanel) {
            title = new JLabel (NbBundle.getMessage (PanelBodyContainer.class, "PanelBodyContainer_PleaseWaitForLicense")); // NOI18N
        } else if (estimatedTime > 0) {
            title = new JLabel(NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer_ProgressLine")); // NOI18N
        } else {
            title = new JLabel (NbBundle.getMessage (PanelBodyContainer.class, "PanelBodyContainer_PleaseWait")); // NOI18N
        }
        progress = ProgressHandleFactory.createProgressComponent (handle);        
        progressPanel = new JPanel (new GridBagLayout ());
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints ();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets (7, 7, 0, 12);
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        progress.setMinimumSize(new Dimension(70, progress.getMinimumSize().height));
        
        progressPanel.add (progress, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints ();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets (7, 0, 0, 20);
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        progressPanel.add (title, gridBagConstraints);
        progressPanel.setVisible(false);        
        delay = new Timer(900, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delay.stop();
                //adjustProgressWidth();
                progressPanel.setVisible(true);
                initBodyPanel();
            }
        });        
        
        delay.setRepeats(false);
        delay.start();
        final String progressDisplayName = NbBundle.getMessage (PanelBodyContainer.class, "PanelBodyContainer_ProgressLine"); // NOI18N
        if (estimatedTime == 0) {
            handle.start ();                    
            handle.progress (progressDisplayName);
        } else {
            assert estimatedTime > 0 : "Estimated time " + estimatedTime;
            final long friendlyEstimatedTime = estimatedTime + 2/*friendly constant*/;
            handle.start ((int) friendlyEstimatedTime * 10, friendlyEstimatedTime); 
            handle.progress (progressDisplayName, 0);

            new UpdateProgress (friendlyEstimatedTime, progressDisplayName).start();
        }
    }

    private void adjustProgressWidth() {
        //Issue #155752
        Dimension min = progress.getMinimumSize();
        Dimension preferred = progress.getPreferredSize();
        if (min != null && preferred != null && (min.width * 2) < preferred.width) {
            int width = preferred.width / 2 ;
            int height = min.height;
            progress.setMinimumSize(new Dimension(150, height));
        }        
    }

    private void initBodyPanel () {
        pBodyPanel.removeAll ();
        customPanel = new JScrollPane ();
        customPanel.setBorder (null);
        pBodyPanel.add (customPanel, BorderLayout.CENTER);
        if (isWaiting) {
            if (progressPanel != null) {
                pBodyPanel.add (progressPanel, BorderLayout.SOUTH);
            }
        }
        customPanel.setViewportView (bodyPanel);
        customPanel.getVerticalScrollBar ().setUnitIncrement (10);
        customPanel.getHorizontalScrollBar ().setUnitIncrement (10);
        revalidate ();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                pBodyPanel.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
            }
        });
    }
    
    private void removeProgressLine () {
        if (progressPanel != null) {
            pBodyPanel.remove (progressPanel);
            if (handle != null) {
                handle.finish();
            }
            revalidate ();
        }
    }
    
    public void setHeadAndContent (final String heading, final String content) {
        if (SwingUtilities.isEventDispatchThread ()) {
            writeToHeader (heading, content);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    writeToHeader (heading, content);
                }
            });
        }
    }
    
    private void writeToHeader (String heading, String msg) {
        tpPanelHeader.setText (null);
        tpPanelHeader.setText ("<br><b>" + heading + "</b> <br>" + msg); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pBodyPanel = new javax.swing.JPanel();
        spPanelHeader = new javax.swing.JScrollPane();
        tpPanelHeader = new javax.swing.JTextPane();

        pBodyPanel.setLayout(new java.awt.BorderLayout());

        tpPanelHeader.setEditable(false);
        tpPanelHeader.setContentType("text/html"); // NOI18N
        spPanelHeader.setViewportView(tpPanelHeader);
        tpPanelHeader.getAccessibleContext().setAccessibleName(head);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pBodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
            .addComponent(spPanelHeader, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spPanelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pBodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer_ACN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer_ACD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pBodyPanel;
    private javax.swing.JScrollPane spPanelHeader;
    private javax.swing.JTextPane tpPanelHeader;
    // End of variables declaration//GEN-END:variables

    private final class UpdateProgress implements Runnable {
        private final long friendlyEstimatedTime;
        private final Task task;
        private int i;

        @SuppressWarnings("LeakingThisInConstructor")
        public UpdateProgress(long friendlyEstimatedTime, String progressDisplayName) {
            this.friendlyEstimatedTime = friendlyEstimatedTime;
            this.task = Installer.RP.create(this);
            this.i = 0;
        }

        @Override
        public void run () {
            if (isWaiting && isShowing()) {
                if (friendlyEstimatedTime * 10 > i++) {
                    handle.progress (i);
                } else {
                    handle.switchToIndeterminate ();
                    return ;
                }
                task.schedule(100);
            }
        }

        final void start() {
            task.schedule(0);
        }
    }
    
}
