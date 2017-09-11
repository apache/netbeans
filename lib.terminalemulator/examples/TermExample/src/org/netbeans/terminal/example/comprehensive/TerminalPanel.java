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

/*
 * TerminalPanel.java
 *
 * Created on Feb 16, 2010, 5:44:59 PM
 */

package org.netbeans.terminal.example.comprehensive;

import org.netbeans.terminal.example.Config;
import org.netbeans.terminal.example.Config.AllowClose;
import org.netbeans.terminal.example.Config.ContainerStyle;
import org.netbeans.terminal.example.Config.Provider;
import org.netbeans.terminal.example.Config.DispatchThread;
import org.netbeans.terminal.example.Config.Execution;
import org.netbeans.terminal.example.Config.IOShuttling;

/**
 *
 * @author ivan
 */
public final class TerminalPanel extends javax.swing.JPanel {

    /** Creates new form TerminalPanel */
    public TerminalPanel() {
        initComponents();
    }

    public Config getConfig() {
	return new Config(
		getCommand(),
		getContainerProvider(),
		getIOProvider(),
		getAllowClose(),
		getThread(),
		getExecution(),
		getIOShuttling(),
		getContainerStyle(),
		isRestartable(),
		isHUPOnClose(),
		isKeep(),
                isDebug()
		);
    }

    private String getCommand() {
	return commandTextField.getText();
    }

    private Provider getContainerProvider() {
	if (containerProviderRadioButton_DEFAULT.isSelected())
	    return Provider.DEFAULT;
	else
	    return Provider.TERM;
    }

    private Provider getIOProvider() {
	if (ioProviderRadioButton_DEFAULT.isSelected())
	    return Provider.DEFAULT;
	else
	    return Provider.TERM;
    }

    private AllowClose getAllowClose() {
	if (allowCloseRadioButton_ALWAYS.isSelected())
	    return AllowClose.ALWAYS;
	else if (allowCloseRadioButton_NEVER.isSelected())
	    return AllowClose.NEVER;
	else if (allowCloseRadioButton_DISCONNECTED.isSelected())
	    return AllowClose.DISCONNECTED;
	else
	    return AllowClose.ALWAYS;
    }

    private DispatchThread getThread() {
	if (threadRadioButton_EDT.isSelected())
	    return DispatchThread.EDT;
	else
	    return DispatchThread.RP;
    }

    private Execution getExecution() {
	if (executionRadioButton_RICH.isSelected())
	    return Execution.RICH;
	else
	    return Execution.NATIVE;
    }

    private boolean isRestartable() {
	return restartableCheckBox.isSelected();
    }

    private boolean isHUPOnClose() {
	return hupOnCloseCheckBox.isSelected();
    }

    private boolean isKeep() {
	return keepCheckBox.isSelected();
    }

    private boolean isDebug() {
	return debugCheckBox.isSelected();
    }

    private IOShuttling getIOShuttling() {
	if (ioShutlingRadioButton_INTERNAL.isSelected())
	    return IOShuttling.INTERNAL;
	else
	    return IOShuttling.EXTERNAL;
    }

    private Config.ContainerStyle getContainerStyle() {
	if (tabbedRadioButton.isSelected())
	    return ContainerStyle.TABBED;
	else
	    return ContainerStyle.MUXED;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                tcButtonGroup = new javax.swing.ButtonGroup();
                providerButtonGroup = new javax.swing.ButtonGroup();
                containerProviderButtonGroup = new javax.swing.ButtonGroup();
                ioProviderButtonGroup = new javax.swing.ButtonGroup();
                threadButtonGroup = new javax.swing.ButtonGroup();
                executionButtonGroup = new javax.swing.ButtonGroup();
                ioShuttlingButtonGroup = new javax.swing.ButtonGroup();
                allowCloseButtonGroup = new javax.swing.ButtonGroup();
                conainerStyleButtonGroup = new javax.swing.ButtonGroup();
                keepCheckBox1 = new javax.swing.JCheckBox();
                keepLabel1 = new javax.swing.JLabel();
                commandLabel = new javax.swing.JLabel();
                commandTextField = new javax.swing.JTextField();
                containerProviderLabel = new javax.swing.JLabel();
                containerProviderRadioButton_DEFAULT = new javax.swing.JRadioButton();
                containerProviderRadioButton_TERM = new javax.swing.JRadioButton();
                jLabel1 = new javax.swing.JLabel();
                ioProviderRadioButton_DEFAULT = new javax.swing.JRadioButton();
                ioProviderRadioButton_TERM = new javax.swing.JRadioButton();
                threadLabel = new javax.swing.JLabel();
                threadRadioButton_EDT = new javax.swing.JRadioButton();
                rpRadioButton = new javax.swing.JRadioButton();
                restartableLabel = new javax.swing.JLabel();
                restartableCheckBox = new javax.swing.JCheckBox();
                excutionLabel = new javax.swing.JLabel();
                executionRadioButton_RICH = new javax.swing.JRadioButton();
                excutionRadioButton_NATIVE = new javax.swing.JRadioButton();
                ioShuttlingLabel = new javax.swing.JLabel();
                ioShutlingRadioButton_INTERNAL = new javax.swing.JRadioButton();
                ioShuttlingRadioButton_EXTERNAL = new javax.swing.JRadioButton();
                allowCloseLabel = new javax.swing.JLabel();
                allowCloseRadioButton_NEVER = new javax.swing.JRadioButton();
                allowCloseRadioButton_ALWAYS = new javax.swing.JRadioButton();
                allowCloseRadioButton_DISCONNECTED = new javax.swing.JRadioButton();
                hupOnCloseLabel = new javax.swing.JLabel();
                hupOnCloseCheckBox = new javax.swing.JCheckBox();
                keepLabel = new javax.swing.JLabel();
                keepCheckBox = new javax.swing.JCheckBox();
                tabbedRadioButton = new javax.swing.JRadioButton();
                multiplexedRadioButton = new javax.swing.JRadioButton();
                debugLabel = new javax.swing.JLabel();
                debugCheckBox = new javax.swing.JCheckBox();

                keepCheckBox1.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.keepCheckBox1.text")); // NOI18N

                keepLabel1.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.keepLabel1.text")); // NOI18N

                commandLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.commandLabel.text")); // NOI18N

                commandTextField.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.commandTextField.text")); // NOI18N

                containerProviderLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.containerProviderLabel.text")); // NOI18N

                containerProviderButtonGroup.add(containerProviderRadioButton_DEFAULT);
                containerProviderRadioButton_DEFAULT.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.containerProviderRadioButton_DEFAULT.text")); // NOI18N

                containerProviderButtonGroup.add(containerProviderRadioButton_TERM);
                containerProviderRadioButton_TERM.setSelected(true);
                containerProviderRadioButton_TERM.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.containerProviderRadioButton_TERM.text")); // NOI18N

                jLabel1.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.jLabel1.text")); // NOI18N

                ioProviderButtonGroup.add(ioProviderRadioButton_DEFAULT);
                ioProviderRadioButton_DEFAULT.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.ioProviderRadioButton_DEFAULT.text")); // NOI18N

                ioProviderButtonGroup.add(ioProviderRadioButton_TERM);
                ioProviderRadioButton_TERM.setSelected(true);
                ioProviderRadioButton_TERM.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.ioProviderRadioButton_TERM.text")); // NOI18N

                threadLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.threadLabel.text")); // NOI18N

                threadButtonGroup.add(threadRadioButton_EDT);
                threadRadioButton_EDT.setSelected(true);
                threadRadioButton_EDT.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.threadRadioButton_EDT.text")); // NOI18N

                threadButtonGroup.add(rpRadioButton);
                rpRadioButton.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.rpRadioButton.text")); // NOI18N

                restartableLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.restartableLabel.text")); // NOI18N
                restartableLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.restartableLabel.toolTipText")); // NOI18N

                restartableCheckBox.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.restartableCheckBox.text")); // NOI18N

                excutionLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.excutionLabel.text")); // NOI18N

                executionButtonGroup.add(executionRadioButton_RICH);
                executionRadioButton_RICH.setSelected(true);
                executionRadioButton_RICH.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.executionRadioButton_RICH.text")); // NOI18N

                executionButtonGroup.add(excutionRadioButton_NATIVE);
                excutionRadioButton_NATIVE.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.excutionRadioButton_NATIVE.text")); // NOI18N

                ioShuttlingLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.ioShuttlingLabel.text")); // NOI18N

                ioShuttlingButtonGroup.add(ioShutlingRadioButton_INTERNAL);
                ioShutlingRadioButton_INTERNAL.setSelected(true);
                ioShutlingRadioButton_INTERNAL.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.ioShutlingRadioButton_INTERNAL.text")); // NOI18N

                ioShuttlingButtonGroup.add(ioShuttlingRadioButton_EXTERNAL);
                ioShuttlingRadioButton_EXTERNAL.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.ioShuttlingRadioButton_EXTERNAL.text")); // NOI18N

                allowCloseLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.allowCloseLabel.text")); // NOI18N

                allowCloseButtonGroup.add(allowCloseRadioButton_NEVER);
                allowCloseRadioButton_NEVER.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.allowCloseRadioButton_NEVER.text")); // NOI18N

                allowCloseButtonGroup.add(allowCloseRadioButton_ALWAYS);
                allowCloseRadioButton_ALWAYS.setSelected(true);
                allowCloseRadioButton_ALWAYS.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.allowCloseRadioButton_ALWAYS.text")); // NOI18N

                allowCloseButtonGroup.add(allowCloseRadioButton_DISCONNECTED);
                allowCloseRadioButton_DISCONNECTED.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.allowCloseRadioButton_DISCONNECTED.text")); // NOI18N

                hupOnCloseLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.hupOnCloseLabel.text")); // NOI18N

                hupOnCloseCheckBox.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.hupOnCloseCheckBox.text")); // NOI18N

                keepLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.keepLabel.text")); // NOI18N

                keepCheckBox.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.keepCheckBox.text")); // NOI18N

                conainerStyleButtonGroup.add(tabbedRadioButton);
                tabbedRadioButton.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.tabbedRadioButton.text")); // NOI18N

                conainerStyleButtonGroup.add(multiplexedRadioButton);
                multiplexedRadioButton.setSelected(true);
                multiplexedRadioButton.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.multiplexedRadioButton.text")); // NOI18N

                debugLabel.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.debugLabel.text")); // NOI18N

                debugCheckBox.setText(org.openide.util.NbBundle.getMessage(TerminalPanel.class, "TerminalPanel.debugCheckBox.text")); // NOI18N

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(debugLabel)
                                        .addComponent(keepLabel)
                                        .addComponent(hupOnCloseLabel)
                                        .addComponent(restartableLabel)
                                        .addComponent(threadLabel)
                                        .addComponent(excutionLabel)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(containerProviderLabel)
                                                        .addComponent(commandLabel))
                                                .addComponent(jLabel1)
                                                .addComponent(ioShuttlingLabel)
                                                .addComponent(allowCloseLabel)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(containerProviderRadioButton_DEFAULT)
                                                .addGap(18, 18, 18)
                                                .addComponent(containerProviderRadioButton_TERM))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(ioProviderRadioButton_DEFAULT)
                                                .addGap(18, 18, 18)
                                                .addComponent(ioProviderRadioButton_TERM))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(allowCloseRadioButton_NEVER)
                                                        .addComponent(ioShuttlingRadioButton_EXTERNAL))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(ioShutlingRadioButton_INTERNAL))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(9, 9, 9)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(excutionRadioButton_NATIVE)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(allowCloseRadioButton_ALWAYS)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(allowCloseRadioButton_DISCONNECTED))
                                                                        .addComponent(rpRadioButton)))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(tabbedRadioButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(multiplexedRadioButton))
                                        .addComponent(executionRadioButton_RICH)
                                        .addComponent(threadRadioButton_EDT)
                                        .addComponent(restartableCheckBox)
                                        .addComponent(hupOnCloseCheckBox)
                                        .addComponent(keepCheckBox)
                                        .addComponent(debugCheckBox))
                                .addContainerGap(76, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(commandLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(containerProviderLabel)
                                        .addComponent(containerProviderRadioButton_DEFAULT)
                                        .addComponent(containerProviderRadioButton_TERM))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tabbedRadioButton)
                                        .addComponent(multiplexedRadioButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(ioProviderRadioButton_DEFAULT)
                                        .addComponent(ioProviderRadioButton_TERM))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ioShuttlingLabel)
                                        .addComponent(ioShuttlingRadioButton_EXTERNAL)
                                        .addComponent(ioShutlingRadioButton_INTERNAL))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(allowCloseLabel)
                                        .addComponent(allowCloseRadioButton_NEVER)
                                        .addComponent(allowCloseRadioButton_DISCONNECTED)
                                        .addComponent(allowCloseRadioButton_ALWAYS))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(excutionLabel)
                                        .addComponent(executionRadioButton_RICH)
                                        .addComponent(excutionRadioButton_NATIVE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(threadLabel)
                                        .addComponent(threadRadioButton_EDT)
                                        .addComponent(rpRadioButton))
                                .addGap(4, 4, 4)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(restartableLabel)
                                        .addComponent(restartableCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(hupOnCloseLabel)
                                        .addComponent(hupOnCloseCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(keepLabel)
                                        .addComponent(keepCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(debugLabel)
                                        .addComponent(debugCheckBox))
                                .addGap(24, 24, 24))
                );
        }// </editor-fold>//GEN-END:initComponents


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.ButtonGroup allowCloseButtonGroup;
        private javax.swing.JLabel allowCloseLabel;
        private javax.swing.JRadioButton allowCloseRadioButton_ALWAYS;
        private javax.swing.JRadioButton allowCloseRadioButton_DISCONNECTED;
        private javax.swing.JRadioButton allowCloseRadioButton_NEVER;
        private javax.swing.JLabel commandLabel;
        private javax.swing.JTextField commandTextField;
        private javax.swing.ButtonGroup conainerStyleButtonGroup;
        private javax.swing.ButtonGroup containerProviderButtonGroup;
        private javax.swing.JLabel containerProviderLabel;
        private javax.swing.JRadioButton containerProviderRadioButton_DEFAULT;
        private javax.swing.JRadioButton containerProviderRadioButton_TERM;
        private javax.swing.JCheckBox debugCheckBox;
        private javax.swing.JLabel debugLabel;
        private javax.swing.JLabel excutionLabel;
        private javax.swing.JRadioButton excutionRadioButton_NATIVE;
        private javax.swing.ButtonGroup executionButtonGroup;
        private javax.swing.JRadioButton executionRadioButton_RICH;
        private javax.swing.JCheckBox hupOnCloseCheckBox;
        private javax.swing.JLabel hupOnCloseLabel;
        private javax.swing.ButtonGroup ioProviderButtonGroup;
        private javax.swing.JRadioButton ioProviderRadioButton_DEFAULT;
        private javax.swing.JRadioButton ioProviderRadioButton_TERM;
        private javax.swing.JRadioButton ioShutlingRadioButton_INTERNAL;
        private javax.swing.ButtonGroup ioShuttlingButtonGroup;
        private javax.swing.JLabel ioShuttlingLabel;
        private javax.swing.JRadioButton ioShuttlingRadioButton_EXTERNAL;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JCheckBox keepCheckBox;
        private javax.swing.JCheckBox keepCheckBox1;
        private javax.swing.JLabel keepLabel;
        private javax.swing.JLabel keepLabel1;
        private javax.swing.JRadioButton multiplexedRadioButton;
        private javax.swing.ButtonGroup providerButtonGroup;
        private javax.swing.JCheckBox restartableCheckBox;
        private javax.swing.JLabel restartableLabel;
        private javax.swing.JRadioButton rpRadioButton;
        private javax.swing.JRadioButton tabbedRadioButton;
        private javax.swing.ButtonGroup tcButtonGroup;
        private javax.swing.ButtonGroup threadButtonGroup;
        private javax.swing.JLabel threadLabel;
        private javax.swing.JRadioButton threadRadioButton_EDT;
        // End of variables declaration//GEN-END:variables

}
