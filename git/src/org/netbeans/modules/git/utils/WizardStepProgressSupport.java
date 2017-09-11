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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.git.utils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.output.OutputLogger;

/**
 *
 * @author Tomas Stupka
 */
public abstract class WizardStepProgressSupport extends GitProgressSupport {   

    private OutputLogger logger;
    private JPanel progressComponent;
    private JLabel progressLabel;
    private JPanel panel;
    private JButton stopButton;
    private boolean noOutputLogging;
    
    public WizardStepProgressSupport(JPanel panel, boolean noOutputLogging) {
        this(panel);
        this.noOutputLogging = noOutputLogging;
    }
    
    public WizardStepProgressSupport(JPanel panel) {
        this.panel = panel;        
    }

    public abstract void setEnabled(boolean bl);

    @Override
    public void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProgressHandle progress = getProgressHandle(); // NOI18N
                JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
                stopButton = new JButton(org.openide.util.NbBundle.getMessage(WizardStepProgressSupport.class, "BK2022")); // NOI18N
                stopButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });
                
                panel.setLayout(new BorderLayout());
                progressComponent = new JPanel();
                progressComponent.setLayout(new BorderLayout(6, 0));
                progressLabel = new JLabel();
                progressLabel.setText(WizardStepProgressSupport.super.getDisplayName());
                progressComponent.add(progressLabel, BorderLayout.NORTH);
                progressComponent.add(bar, BorderLayout.CENTER);
                progressComponent.add(stopButton, BorderLayout.LINE_END);
                WizardStepProgressSupport.super.startProgress();
                
                panel.setVisible(true);
                panel.add(progressComponent);
                panel.revalidate();
            }
        });                                                
    }

    @Override
    protected void finishProgress() {        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                WizardStepProgressSupport.super.finishProgress();
                panel.remove(progressComponent);
                panel.revalidate();
                panel.repaint();
                panel.setVisible(false);
                setEnabled(true);
            }
        });                
    }

    @Override
    public void setDisplayName(String displayName) {
        if(progressLabel != null) progressLabel.setText(displayName);
        super.setDisplayName(displayName);
    }
    
    @Override
    public synchronized boolean cancel() {
        if(stopButton!=null) stopButton.setEnabled(false);
        setDisplayName(org.openide.util.NbBundle.getMessage(WizardStepProgressSupport.class, "MSG_Progress_Terminating"));
        return super.cancel();
    }
    
    @Override
    public final OutputLogger getLogger () {
        if(noOutputLogging) {
            if (logger == null) {
                logger = OutputLogger.getLogger(null);
            }
            return logger;
        }
        return super.getLogger();
    }
        
}
